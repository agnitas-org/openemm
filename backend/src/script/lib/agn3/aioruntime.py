#!/usr/bin/env python3
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                              #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.                                                                                                       #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	os, logging, time
import	asyncio, uvloop
from	asyncio import CancelledError
from	asyncinotify import Inotify, Mask
from	collections import deque
from	contextlib import suppress
from	dataclasses import dataclass
from	datetime import datetime
from	functools import partial
from	io import StringIO
from	types import TracebackType
from	typing import Any, Awaitable, Callable, Generic, Iterable, Optional, Self, Sequence, TypeVar, Union
from	typing import AsyncIterator, Coroutine, Deque, Dict, List, NamedTuple, Set, Tuple, Type
from	typing import cast
from	typing_extensions import override
from	.definitions import program
from	.exceptions import Stop
from	.ignore import Ignore
from	.parser import Parsable, unit
from	.runtime import Runtime, Preset
from	.stream import Stream
#
__all__ = ['Controller', 'AIORuntime', 'Preset']
#
logger = logging.getLogger (__name__)
#
_T = TypeVar ('_T')
_U = TypeVar ('_U')
#
def _log_exception (method: Callable[[str], None], e: BaseException, task: asyncio.Task[Any]) -> None:
	method (f'{task.get_name ()}: {e}')
	buffer = StringIO ()
	task.print_stack (file = buffer)
	for line in buffer.getvalue ().strip ().split ('\n'):
		method (line)

async def _wait (
	task: asyncio.Task[_T],
	*,
	awaitable: None | asyncio.Future[_T] = None,
	timeout: None | int | float = None,
	log_with: None | Callable[[str], None] = logger.error
) -> asyncio.Task[_T] | Awaitable[_T] | BaseException:
	def _log_exc (exception: BaseException) -> None:
		if log_with is not None:
			_log_exception (log_with, exception, task)
	#
	done, _ = await asyncio.wait ([task] if awaitable is None else [task, awaitable], timeout = timeout, return_when = asyncio.FIRST_COMPLETED)
	if done:
		finished = done.pop ()
		if finished is task and task.done () and (exception := task.exception ()) is not None:
			_log_exc (exception)
			return exception
		return finished
	return TimeoutError ()

class Controller (NamedTuple):
	name: str
	method: Callable[[], Coroutine[Any, Any, None]]
	restart: None | str = None
	maintain: Parsable = None

class Queue (Generic[_T]):
	__slots__ = ['maxsize', 'loop', 'getter', 'putter', 'queue']
	class Entry (Generic[_U]):
		__slots__ = ['predicate', 'future']
		def __init__ (self, predicate: Optional[Callable[[_U], bool]], future: asyncio.Future[_U]) -> None:
			self.predicate = predicate
			self.future = future
		
	def __init__ (self, maxsize: int = 0) -> None:
		self.maxsize = maxsize
		self.loop = asyncio.get_running_loop ()
		self.getter: Deque[Queue.Entry[_T]] = deque ()
		self.putter: Deque[asyncio.Future[None]] = deque ()
		self.queue: Deque[_T] = deque ()
	
	def __len__ (self) -> int:
		return len (self.queue)

	def __aiter__ (self) -> Queue[_T]:
		return self
		
	async def __anext__ (self) -> _T:
		with Ignore (Stop):
			while True:
				task = asyncio.create_task (self.get ())
				done = await _wait (task)
				if done is task:
					return await task
				else:
					task.cancel ()
		raise StopAsyncIteration ()

	async def _wake_putter (self) -> None:
		if self.maxsize > 0 and self.putter and len (self.queue) < self.maxsize:
			for future in self.putter:
				if not future.done ():
					future.set_result (None)
			await asyncio.sleep (0)

	async def remove (self, value: _T) -> None:
		with Ignore (ValueError):
			while self.queue:
				self.queue.remove (value)
		await self._wake_putter ()
	
	async def get (self, predicate: Optional[Callable[[_T], bool]] = None) -> _T:
		if self.queue:
			for element in self.queue:
				if predicate is None or predicate (element):
					self.queue.remove (element)
					await self._wake_putter ()
					return element
		future: asyncio.Future[_T] = self.loop.create_future ()
		entry: Queue.Entry[_T] = Queue.Entry (predicate = predicate, future = future)
		self.getter.append (entry)
		try:
			return await future
		except:
			future.cancel ()
			raise
		finally:
			self.getter.remove (entry)
	
	async def put (self, element: _T, rejected: bool = False, key: Optional[Callable[[_T, _T], int]] = None) -> None:
		if self.maxsize > 0:
			while len (self.queue) >= self.maxsize:
				future = self.loop.create_future ()
				self.putter.append (future)
				try:
					await future
				except:
					future.cancel ()
					raise
				finally:
					self.putter.remove (future)
		#
		if key is None:
			if rejected:
				self.queue.appendleft (element)
			else:
				self.queue.append (element)
		else:
			if not self.queue or key (self.queue[-1], element) < 0:
				self.queue.append (element)
			elif key (self.queue[0], element) > 0:
				self.queue.appendleft (element)
			else:
				qlen = len (self.queue)
				start, end = 0, qlen
				pos = 0
				while start < end:
					pos = (start + end) // 2
					compare = key (self.queue[pos], element)
					if compare < 0:
						start = pos + 1
					elif compare > 0:
						end = pos
					else:
						break
				if rejected:
					while pos > 0 and key (self.queue[pos - 1], element) == 0:
						pos -= 1
				else:
					while pos < qlen - 1 and key (self.queue[pos + 1], element) == 0:
						pos += 1
				self.queue.insert (pos, element)
		#
		for entry in self.getter:
			if not entry.future.done ():
				for element in self.queue:
					if entry.predicate is None or entry.predicate (element):
						entry.future.set_result (element)
						self.queue.remove (element)
						break
		#
		await self._wake_putter ()

class CTask:
	__slots__ = ['name', 'coro', 'task']
	def __init__ (self, name: str, coro: Coroutine[Any, Any, None]) -> None:
		self.name = name
		self.coro = coro
		self.task: asyncio.Task[None]
	
	async def __aenter__ (self) -> Self:
		self.task = asyncio.create_task (self.coro, name = self.name)
		logger.debug (f'{self.name}: task started')
		return self
	
	async def __aexit__ (self, exc_type: None | Type[BaseException], exc_value: None | BaseException, traceback: None | TracebackType) -> None | bool:
		if exc_type is None or exc_type is Stop:
			if not self.task.cancelled ():
				await self.task
			logger.debug (f'{self.name}: task graceful finished')
		else:
			self.task.cancel ()
			logger.debug (f'{self.name}: task aborted: {exc_value}')
		return None
	
	def cancel (self) -> None:
		if not self.task.cancelled () and self.task.cancelling () == 0:
			self.task.cancel ()

class CWTask (CTask):
	__slots__ = ['producer', 'restart', 'terminate', 'ctx']
	@dataclass
	class Context:
		fin: asyncio.Future[None]
		task: None | asyncio.Task[None]
		async def value (self) -> None:
			if self.task is not None:
				if not self.task.cancelled ():
					await self.task
				self.task = None
			return None

	def __init__ (self, name: str, producer: Callable[[asyncio.Future[None]], Coroutine[Any, Any, None]], restart: int | float, terminate: None | int | float) -> None:
		self.name = name
		self.producer = producer
		self.restart = restart
		self.terminate = terminate
		self.ctx: CWTask.Context

	async def __aenter__ (self) -> Self:
		self.ctx = CWTask.Context (
			fin = asyncio.get_running_loop ().create_future (),
			task = None
		)
		async def restarter () -> None:
			delay = 0.0
			while not bool ((await asyncio.wait ([self.ctx.fin], timeout = max (0.0, delay), return_when = asyncio.FIRST_COMPLETED))[0]):
				self.ctx.task = asyncio.create_task (self.producer (self.ctx.fin), name = f'{self.name}[task]')
				logger.debug (f'{self.name}[task]: launched')
				started = time.time ()
				rc = await _wait (self.ctx.task, awaitable = self.ctx.fin)
				if rc is self.ctx.task:
					logger.debug (f'{self.name}[task]: task finished')
					return await self.ctx.value ()
				elif rc is not self.ctx.fin:
					if isinstance (rc, BaseException):
						logger.debug (f'{self.name}[task]: terminated due to: {rc}')
					else:
						logger.warning (f'{self.name}[task]: terminated unexpected with: {rc}')
					self.ctx.task = None
				delay = self.restart - (time.time () - started)
			#
			if self.ctx.task is not None and not self.ctx.task.cancelled ():
				rc = await _wait (self.ctx.task, timeout = self.terminate)
				if rc is self.ctx.task:
					logger.debug (f'{self.name}[task]: task finished in finalization')
					return await self.ctx.value ()
				self.ctx.task.cancel ()
				logger.debug (f'{self.name}[task]: cancelled')
			return None
		#
		self.task = asyncio.create_task (restarter (), name = f'{self.name}[restart]')
		logger.debug (f'{self.name}: task started')
		return self
		
	async def __aexit__ (self, exc_type: None | Type[BaseException], exc_value: None | BaseException, traceback: None | TracebackType) -> None | bool:
		with suppress (NameError):
			self.ctx.fin.set_result (None)
		return await super ().__aexit__ (exc_type, exc_value, traceback)

class Workers:
	__slots__ = [
		'base_group', 'limit', 'work_id',
		"warden", 'tasks', 'queued'
	]
	group_id = 0
	def __init__ (self, group: Optional[str], limit: Optional[int]) -> None:
		self.__class__.group_id += 1
		self.base_group = '{group}_{group_id}'.format (
			group = group if group else 'worker',
			group_id = self.__class__.group_id
		)
		self.limit = limit
		self.work_id = 0
		self.warden: Optional[asyncio.Task[None]] = None
		self.tasks: Set[asyncio.Task[None]] = set ()
		self.queued: Deque[Coroutine[Any, Any, None]] = deque ()
		
	def __len__ (self) -> int:
		return len (self.tasks) + len (self.queued)
		
	async def __aenter__ (self) -> Self:
		return self
		
	async def __aexit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		if exc_type is None:
			await self._warden_join ()
		else:
			self.cancel ()
		return None
		
	async def launch (self, coro: Coroutine[Any, Any, None]) -> None:
		if not self.tasks and not self.queued:
			await self._warden_join ()
		if self.limit and len (self.tasks) >= self.limit:
			self.queued.append (coro)
		else:
			self._start (coro)
		if self.warden is None:
			self.warden = asyncio.create_task (self._warden (), name = f'{self.base_group}_warden')
		
	def cancel (self) -> None:
		if self.tasks:
			Stream (self.tasks).each (lambda t: t.cancel ())
		self.queued.clear ()
		self._warden_cancel ()

	async def _warden (self) -> None:
		while self.tasks or self.queued:
			while self.queued and (not self.limit or len (self.tasks) < self.limit):
				self._start (self.queued.popleft ())
			if self.tasks:
				done, pending = await asyncio.wait (self.tasks, return_when = asyncio.FIRST_COMPLETED)
				for task in done:
					if not task.cancelled ():
						if (e := task.exception ()) is not None:
							_log_exception (logger.error, e, task)
						else:
							await task
					self.tasks.remove (task)
		
	async def _warden_join (self) -> None:
		if self.warden is not None:
			await self.warden
			self.warden = None

	def _warden_cancel (self) -> None:
		if self.warden is not None:
			self.warden.cancel ()
			self.warden = None

	def _start (self, coro: Coroutine[Any, Any, None]) -> None:
		self.work_id += 1
		self.tasks.add (asyncio.create_task (coro, name = f'{self.base_group}_{self.work_id}'))
		
@dataclass
class Process:
	name: str
	args: Sequence[str]
	process: None | asyncio.subprocess.Process = None
	async def __aenter__ (self) -> Self:
		await self.launched ()
		return self
		
	async def __aexit__ (self, exc_type: None | Type[BaseException], exc_value: None | BaseException, traceback: None | TracebackType) -> None | bool:
		if self.process is not None:
			if exc_type is None or exc_type is Stop:
				await self.wait ()
		self.process = None
		return None

	async def launched (self) -> asyncio.subprocess.Process:
		if self.process is None:
			self.process =  await asyncio.create_subprocess_exec (
				*self.args,
				stdin = asyncio.subprocess.PIPE,
				stdout = asyncio.subprocess.PIPE,
				stderr = asyncio.subprocess.PIPE
			)
		return self.process
		
	async def wait (self) -> int:
		return await (await self.launched ()).wait ()
		
	async def communicate (self, input: None | bytes = None) -> Tuple[int, bytes, bytes]:
		process = await self.launched ()
		(out, err) = await process.communicate (input)
		returncode = process.returncode
		return (returncode if isinstance (returncode, int) else -1, out, err)
		
	async def communicate_text (self,
		input: Optional[str] = None,
		input_charset: str = 'UTF-8',
		output_charset: str = 'UTF-8',
		errors: Optional[str] = None
	) -> Tuple[int, str, str]:
		(returncode, out, err) = await self.communicate (None if not input else input.encode (input_charset))
		if errors is not None:
			return (returncode, '' if not out else out.decode (output_charset, errors = errors), '' if not err else err.decode (output_charset, errors = errors))
		else:
			return (returncode, '' if not out else out.decode (output_charset), '' if not err else err.decode (output_charset))
	
class AIORuntime (Runtime):
	__slots__ = ['_loop', '_stop', '_controller', '_apply_delay', '_protected']
	class Protect (Generic[_T]):
		__slots__ = ['task', 'timeout', 'protected']
		def __init__ (self, task: asyncio.Task[_T], timeout: Union[None, int, float], protected: Dict[asyncio.Task[Any], AIORuntime.Protect[Any]]) -> None:
			self.task = task
			self.timeout = timeout
			self.protected = protected

		def _enter (self) -> asyncio.Future[_T]:
			self.protected[self.task] = self
			return asyncio.shield (self.task)
		
		def _exit (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			self.protected.pop (self.task, None)
			return None
			
		def __enter__ (self) -> asyncio.Future[_T]:
			return self._enter ()
		
		def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			return self._exit (exc_type, exc_value, traceback)

		async def __aenter__ (self) -> asyncio.Future[_T]:
			return self._enter ()
		
		async def __aexit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			return self._exit (exc_type, exc_value, traceback)

	def signal_aiohandler (self) -> None:
		super ().signal_handler (0, None)
		self.stop ()
	
	@override
	def executor (self, controller: None | Controller = None) -> bool:
		self._protected: Dict[asyncio.Task[Any], AIORuntime.Protect[Any]] = {}
		#
		self._loop: asyncio.AbstractEventLoop
		self._stop: asyncio.Future[None]
		self._controller: Controller
		self._apply_delay: Set[asyncio.Task[Any]]
		#
		async def restarter () -> bool:
			if (self.ctx.watchdog or self.ctx.debug) and self._controller.restart is not None:
				mode: str
				option: None | str
				try:
					(mode, option) = self._controller.restart.split (':', 1)
				except ValueError:
					(mode, option) = (self._controller.restart, None)
				delay: None | int = None
				match mode:
					case 'daily':
						offset = unit.parse (option, 0)
						now = datetime.now ()
						tomorrow = datetime.fromordinal (now.toordinal () + 1)
						delay = int ((tomorrow - now).total_seconds ()) + offset
					case _:
						if option is None:
							option = mode
							mode = 'in'
						if mode == 'in' and option is not None:
							delay = unit.parse (option)
						else:
							logger.warning (f'restart: unknown restart expression: "{self._controller.restart}", restart disabled')
				#
				if delay is not None:
					with Ignore (Stop):
						delay = max (delay, self.ctx.restart_delay if self.ctx.restart_delay is not None else 0)
						logger.info ('restart: scheduled in {hour}:{min:02d}:{sec:02d} hh:mm:ss'.format (
							hour = delay // 3600,
							min = (delay // 60) % 60,
							sec = delay % 60
						))
						done, pending = await asyncio.wait ([self._stop], timeout = delay, return_when = asyncio.ALL_COMPLETED)
						if self._stop in pending:
							if not self._stop.done ():
								logger.info ('restart: initiated')
								self.running = False
								self._stop.set_result (None)
								return True
			return False
		#
		async def execution (controller: Controller) -> bool:
			self._loop = asyncio.get_running_loop ()
			self._stop = self.future (type (None))
			self._controller = controller
			self._apply_delay = set ()
			for signr in self.signals.saved:
				if self.signals[signr] == self.signal_handler:
					self._loop.add_signal_handler (signr, self.signal_aiohandler)
			#
			restarter_task = asyncio.create_task (restarter (), name = f'{controller.name}:restart')
			async with self.ctask (controller.name, controller.method ()):
				delay = unit (controller.maintain if controller.maintain is not None else self.maintain_in ())
				if delay and delay > 0:
					logger.info (f'maintain: started with {delay} seconds interval')
					while await self.delay (delay):
						self.maintain ()
					logger.info ('maintain: finished')
				else:
					await self.finished ()
			restart = await restarter_task
			#
			myself = asyncio.current_task ()
			for task in asyncio.all_tasks ():
				if task != myself and not task.done ():
					try:
						protect = self._protected[task]
						with Ignore (CancelledError):
							if protect.timeout is not None:
								try:
									logger.info (f'{task}: protected task running, waiting for {protect.timeout} seconds to finish')
									await asyncio.wait_for (task, protect.timeout)
									logger.info (f'{task}: protected task finished within timeout {protect.timeout} seconds')
								except TimeoutError:
									logger.info (f'{task}: protected task cancelled after reaching timeout {protect.timeout} seconds')
							else:
								logger.info (f'{task}: protected task running, waiting for finish')
								await task
								logger.info (f'{task}: protected task finished')
					except KeyError:
						task.cancel ()
						logger.info (f'{task}: running task cancelled during shutdown')
			await self.aioyield ()
			return restart
		try:
			if uvloop.run (execution (controller if controller is not None else Controller (program, self.controller, self.restart_in (), self.maintain_in ())), debug = self.ctx.debug):
				logger.info ('restart: triggered')
				self.restart ()
		except CancelledError as e:
			logger.exception (f'failed due to canceled task: {e}')
		return True

	@override
	def executors (self) -> Optional[List[Callable[[], bool]]]:
		if (controllers := self.controllers ()) is not None and controllers:
			executables: List[Callable[[], bool]] = []
			for controller in controllers:
				executable = partial (self.executor, controller)
				setattr (executable, '__name__', controller.name)
				executables.append (executable)
			return executables
		return None
	
	def controllers (self) -> None | list[Controller]:
		return None
	
	async def controller (self) -> None:
		await self.finished ()
	
	def restart_in (self) -> None | str:
		return None
	
	def maintain_in (self) -> Parsable:
		return '1h'
		
	def future (self, t: Type[_T]) -> asyncio.Future[_T]:
		return self._loop.create_future ()

	async def aioyield (self) -> None:
		await asyncio.sleep (0)

	async def delay (self, timeout: Union[int, float]) -> bool:
		if self.running:
			task = asyncio.current_task ()
			apply_delay = task in self._apply_delay
			done, _ = await asyncio.wait ([self._stop], timeout = timeout if timeout > 0.0 and apply_delay else 0)
			if done:
				self.running = False
			elif not apply_delay and task is not None:
				self._apply_delay.add (task)
		return self.running

	async def wait1 (self, tasks: list[asyncio.Task[_U]], timeout: None | int | float = None) -> Tuple[asyncio.Task[_U], _U]:
		done, _ = await asyncio.wait ([cast (asyncio.Task[_U], self._stop)] + tasks, timeout = timeout, return_when = asyncio.FIRST_COMPLETED)
		if self._stop in done:
			raise Stop ()
		try:
			task = [_t for _t in done if not _t.cancelled ()][0]
		except IndexError:
			raise CancelledError ()
		else:
			if (e := task.exception ()) is not None:
				_log_exception (logger.error, e, task)
				raise e
			return (task, await task)
		
	async def wait (self, *tasks: asyncio.Task[_U], timeout: Union[None, int, float] = None) -> Tuple[asyncio.Task[_U], _U]:
		done, _ = await self._wait (tasks, timeout = timeout)
		task = done.pop ()
		return (task, await task)
		
	def protect (self, coro: Coroutine[Any, Any, _T], timeout: Union[None, int, float] = None) -> AIORuntime.Protect[_T]:
		return AIORuntime.Protect (asyncio.create_task (coro), timeout, self._protected)
	
	def ctask (self, name: str, coro: Coroutine[Any, Any, None]) -> CTask:
		return CTask (name, coro)
	
	def cwtask (self, name: str, producer: Callable[[asyncio.Future[None]], Coroutine[Any, Any, None]], restart: int | float = 60, terminate: None | int | float = None) -> CWTask:
		return CWTask (name, producer, restart, terminate)

	def workers (self, group: Optional[str] = None, limit: Optional[int] = None) -> Workers:
		return Workers (group, limit)
	
	def process (self, name: str, args: Sequence[str]) -> Process:
		return Process (name, args)

	async def scan (self, path: str, prescan: bool = True, predicate: Optional[Callable[[str], bool]] = None) -> AsyncIterator[str]:
		with Inotify () as inotify:
			inotify.add_watch (path, Mask.CLOSE_WRITE | Mask.MOVED_TO | Mask.MOVED_FROM | Mask.DELETE)
			ready: Deque[str] = deque ()
			scanned: Optional[Set[str]] = None
			#
			if prescan:
				files: List[Tuple[str, float]] = []
				for filename in os.listdir (path):
					try:
						st = os.stat (os.path.join (path, filename))
						mtime = st.st_mtime
					except OSError:
						mtime = 0.0
					files.append ((filename, mtime))
				for (filename, _) in sorted (files, key = lambda kv: kv[1]):
					ready.append (filename)
					logger.debug (f'scan[{path}]: added "{filename}" from prescan')
				if ready:
					scanned = set (ready)
			#
			while self.running:
				while self.running and ready:
					filename = ready.popleft ()
					if predicate is None or predicate (filename):
						yield filename
						logger.debug (f'scan[{path}]: yielded "{filename}"')
					else:
						logger.debug (f'scan[{path}]: dropped "{filename}"')
				with Ignore (Stop):
					done, _ = await self._wait ([asyncio.create_task (inotify.get ())])
					event = await done.pop ()
					if event is not None and event.name is not None:
						filename = str (event.name)
						if event.mask & (Mask.CLOSE_WRITE | Mask.MOVED_TO):
							if scanned is None or filename not in scanned:
								ready.append (filename)
								logger.debug (f'scan[{path}]: added "{filename}" from event')
						elif event.mask & (Mask.MOVED_FROM | Mask.DELETE):
							with Ignore (ValueError):
								ready.remove (filename)
								logger.debug (f'scan[{path}]: drop removed file "{filename}"')
							if scanned is not None:
								scanned.discard (filename)
								if not scanned:
									scanned = None
			await self.aioyield ()

	async def finished (self) -> None:
		await self._stop

	def stop (self) -> None:
		if not self._stop.done ():
			self._stop.set_result (None)
		self.running = False

	async def _wait (self,
		tasks: Iterable[asyncio.Task[_U]],
		*,
		timeout: None | int | float = None,
		return_when: str = asyncio.FIRST_COMPLETED
	) -> tuple[set[asyncio.Task[_U]], set[asyncio.Task[_U]]]:
		done, pending = await asyncio.wait (list (tasks) + [cast ('asyncio.Task[_U]', self._stop)], timeout = timeout, return_when = return_when)
		Stream (pending).filter (lambda t: t is not self._stop).each (lambda t: t.cancel ())
		with Ignore (KeyError):
			done.remove (cast ('asyncio.Task[_U]', self._stop))
			raise Stop ()
		if not done:
			raise TimeoutError ()
		return done, pending
