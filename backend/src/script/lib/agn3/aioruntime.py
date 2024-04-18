#!/usr/bin/env python3
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	os, time, logging, signal
import	asyncio
from	asyncinotify import Inotify, Mask
from	collections import deque
from	dataclasses import dataclass, field
from	datetime import datetime
from	functools import partial
from	io import StringIO
from	types import TracebackType
from	typing import Any, Awaitable, Callable, Generic, Literal, Optional, Sequence, TypeVar, Union
from	typing import AsyncIterator, Coroutine, Deque, Dict, List, Set, Tuple, Type
from	typing import cast, overload
from	.exceptions import error, Timeout, Stop
from	.log import log
from	.ignore import Ignore
from	.parser import Parsable, unit
from	.runtime import Runtime
from	.stream import Stream
#
__all__ = ['AIORuntime']
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

class AIORuntime (Runtime):
	__slots__ = ['_loop', '_stop', '_apply_delay', '_channels', '_tasks']
	with Ignore (ImportError):
		import	uvloop
		asyncio.set_event_loop_policy (uvloop.EventLoopPolicy ())

	class Channel (Generic[_T]):
		__slots__ = ['ref', 'name', 'queue', 'event']
		def __init__ (self, ref: AIORuntime, name: str) -> None:
			self.ref = ref
			self.name = name
			self.queue: Queue[_T] = Queue ()
			self.event = asyncio.Event ()

		def __len__ (self) -> int:
			return len (self.queue)
		
		def delete (self) -> None:
			self.ref.delete_channel (self.name)

		async def remove (self, value: _T) -> None:
			await self.queue.remove (value)
			if not self.queue:
				self.event.clear ()
				
		async def get (self, predicate: Optional[Callable[[_T], bool]] = None) -> _T:
			rc = await self.queue.get (predicate = predicate)
			if not len (self.queue):
				self.event.clear ()
			return rc
		
		async def put (self, element: _T, reject: bool = False) -> None:
			await self.queue.put (element)
			self.event.set ()
			await self.ref.aioyield ()
		
		async def pushback (self, element: _T) -> None:
			await self.put (element, reject = True)
		
		def __aiter__ (self) -> AIORuntime.Channel[_T]:
			return self
		
		async def __anext__ (self) -> _T:
			while self.ref.running:
				done, pending = await asyncio.wait ([self.ref._stop, self.get ()], return_when = asyncio.FIRST_COMPLETED)
				Stream (pending).filter (lambda f: f is not self.ref._stop).each (lambda f: f.cancel ())
				if self.ref._stop in done:
					break
				if done:
					return cast (_T, await done.pop ())
			raise StopAsyncIteration ()

	class Channels:
		__slots__ = ['ref', 'channels']
		def __init__ (self, ref: AIORuntime) -> None:
			self.ref = ref
			self.channels: Dict[str, AIORuntime.Channel[Any]] = {}
		
		def get (self, name: str) -> AIORuntime.Channel[Any]:
			try:
				return self.channels[name]
			except KeyError:
				channel: AIORuntime.Channel[Any]
				self.channels[name] = channel = AIORuntime.Channel (self.ref, name)
				return channel
		
		def get_as (self, name: str, t: Type[_T]) -> AIORuntime.Channel[_T]:
			return cast (AIORuntime.Channel[_T], self.get (name))
		
		def delete (self, name: str) -> None:
			with Ignore (KeyError):
				if (length := len (self.channels[name])) > 0:
					logger.warning (f'{name}: removing channel with {length} element(s) left')
				del self.channels[name]

		async def wait (self, channels: List[AIORuntime.Channel[_T]], timeout: Union[None, int, float] = None) -> AIORuntime.Channel[_T]:
			tasks = [asyncio.create_task (_c.event.wait ()) for _c in channels]
			done, pending = await asyncio.wait (tasks, timeout = timeout, return_when = asyncio.FIRST_COMPLETED)
			Stream (pending).each (lambda t: t.cancel ())
			if not done:
				raise Timeout ('timeout exceeded')
			channel = done.pop ()
			await channel
			return channels[tasks.index (channel)]
		
		async def select (self, channels: List[AIORuntime.Channel[_T]], timeout: Union[None, int, float] = None) -> Tuple[AIORuntime.Channel[_T], _T]:
			tasks = [asyncio.create_task (_c.queue.get ()) for _c in channels]
			done, pending = await asyncio.wait (tasks, timeout = timeout, return_when = asyncio.FIRST_COMPLETED)
			Stream (pending).each (lambda t: t.cancel ())
			if not done:
				raise Timeout ('timeout exceeded')
			channel = done.pop ()
			return (channels[tasks.index (channel)], await channel)

	@dataclass
	class Task (Generic[_T]):
		name: str
		coro: Coroutine[Any, Any, _T]
		task: asyncio.Task[_T]
		started: datetime = field (default_factory = lambda: datetime.now ())
		def __str__ (self) -> str:
			if self.task.cancelled ():
				status = 'cancelled'
			elif self.task.done ():
				if (exception := self.task.exception ()) is not None:
					status = f'exception {exception}'
				else:
					status = f'with result {self.task.result ()}'
			else:
				status = f'running {self.task.get_coro ()}'
			return f'{self.task.get_name ()}: {status}, started {self.started:%c}'

		__repr__ = __str__
		@overload
		async def join (self, timeout: None = ..., silent: Literal[False] = ...) -> _T: ...
		@overload
		async def join (self, timeout: None = ..., silent: Literal[True] = ...) -> Optional[_T]: ...
		@overload
		async def join (self, timeout: Union[int, float] = ..., silent: bool = ...) -> Optional[_T]: ...
		async def join (self, timeout: Union[None, int, float] = None, silent: bool = False) -> Optional[_T]:
			try:
				if timeout is None:
					return await self.task
				return await asyncio.wait_for (self.task, timeout)
			except asyncio.exceptions.TimeoutError:
				if not silent:
					raise error (f'{self.name}: timeout')
			except asyncio.exceptions.CancelledError:
				if not silent:
					raise error (f'{self.name}: canceled')
			except Exception as e:
				self._log_exception (logger.error, e)
				if not silent:
					raise error (f'{self.name}: exception {e}')
			return None
		
		def cancel (self) -> None:
			if not self.task.done ():
				self.task.cancel ()
			elif (e := self.task.exception ()) is not None:
				logger.info (f'cancel {self.name}: task already terminated due to exception')
				self._log_exception (logger.info, e)
			elif self.task.cancelled ():
				logger.debug (f'cancel {self.name}: task already cancelled')
			
		def _log_exception (self, method: Callable[[str], None], e: BaseException) -> None:
			_log_exception (method, e, self.task)
		
	class Tasks:
		__slots__ = ['tasks']
		def __init__ (self) -> None:
			self.tasks: Dict[str, AIORuntime.Task[Any]] = {}
		
		def task (self, name: str, coro: Coroutine[Any, Any, _T]) -> AIORuntime.Task[_T]:
			task = AIORuntime.Task (name, coro, asyncio.create_task (coro, name = name))
			return self.add (name, task)
		
		def add (self, name: str, task: AIORuntime.Task[_T]) -> AIORuntime.Task[_T]:
			self.tasks[name] = task
			def remover (task: asyncio.Task[_T]) -> None:
				with Ignore (KeyError):
					myself = self.tasks.pop (task.get_name ())
					with Ignore (asyncio.exceptions.CancelledError):
						if (e := task.exception ()):
							myself._log_exception (logger.debug, e)
					logger.debug (f'{myself}: removed')
			task.task.add_done_callback (remover)
			return task

		def launch (self, name: str, coro: Coroutine[Any, Any, _T]) -> AIORuntime.Task[_T]:
			if name in self.tasks:
				raise ValueError (f'{name}: already active as {self.tasks[name]}')
			return self.task (name, coro)

		def cancel (self, name: str) -> None:
			with Ignore (KeyError):
				task = self.tasks.pop (name)
				task.cancel ()
				logger.info (f'{task}: cancelled')

		def terminate (self) -> None:
			Stream (self.tasks).drain ().each (lambda n: self.cancel (n))
		
		async def watch (self, timeout: Union[None, int, float] = None, only_exceptions: bool = False) -> Optional[AIORuntime.Task[Any]]:
			if self.tasks:
				tasklist = dict ((_t.task, _t) for _t in self.tasks.values ())
				done, pending = await asyncio.wait (tasklist.keys (), timeout = timeout, return_when = asyncio.FIRST_EXCEPTION if only_exceptions else asyncio.FIRST_COMPLETED)
				for aio_task in done:
					task = tasklist[aio_task]
					if (e := aio_task.exception ()) is not None:
						logger.warning (f'{task}: terminates due to exception {e}')
					else:
						value = await task.join ()
						logger.info (f'{task}: normal termination with {value!r}')
					return task
			else:
				await asyncio.sleep (timeout if timeout is not None else 0)
			return None
	
	class Workers:
		__slots__ = [
			'base_group', 'limit', 'work_id',
			'cleanup_interval', 'cleanup_threshold', 'cleanup_last',
			'tasks', 'queued'
		]
		group_id = 0
		def __init__ (self, group: Optional[str], limit: Optional[int], cleanup_interval: Parsable, cleanup_threshold: int) -> None:
			self.__class__.group_id += 1
			self.base_group = '{group}_{group_id}'.format (
				group = group if group else 'worker',
				group_id = self.__class__.group_id
			)
			self.limit = limit
			self.work_id = 0
			self.cleanup_interval = unit (cleanup_interval)
			self.cleanup_threshold = cleanup_threshold
			self.cleanup_last = 0.0
			self.tasks: Set[asyncio.Task[None]] = set ()
			self.queued: Deque[Coroutine[Any, Any, None]] = deque ()
		
		def __len__ (self) -> int:
			return len (self.tasks) + len (self.queued)
		
		async def __aenter__ (self) -> AIORuntime.Workers:
			self.cleanup_last = time.time ()
			return self
		
		async def __aexit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			while self.queued:
				await self._cleanup (None)
			await self._cleanup (None)
			return None
		
		async def launch (self, coro: Coroutine[Any, Any, None]) -> None:
			timestamp = time.time ()
			if (
				(self.limit and len (self.tasks) >= self.limit)
				or
				(self.cleanup_interval and self.cleanup_last + self.cleanup_interval < timestamp)
				or
				(self.cleanup_threshold and len (self.tasks) >= self.cleanup_threshold)
			):
				await self._cleanup (0)
				self.cleanup_last = timestamp
			if self.limit and len (self.tasks) >= self.limit:
				self.queued.append (coro)
			else:
				self._start (coro)
		
		def cancel (self) -> None:
			if self.tasks:
				Stream (self.tasks).each (lambda t: t.cancel ())
			self.queued.clear ()
		
		async def _cleanup (self, timeout: Union[None, int, float]) -> None:
			if self.tasks:
				done, pending = await asyncio.wait (self.tasks, timeout = timeout)
				for task in done:
					if not task.cancelled ():
						if (e := task.exception ()) is not None:
							_log_exception (logger.error, e, task)
						else:
							await task
					self.tasks.remove (task)
			if self.limit:
				while len (self.tasks) < self.limit and self.queued:
					self._start (self.queued.popleft ())
		
		def _start (self, coro: Coroutine[Any, Any, None]) -> None:
			self.work_id += 1
			self.tasks.add (asyncio.create_task (coro, name = f'{self.base_group}_{self.work_id}'))
		
	def signal_aiohandler (self) -> None:
		super ().signal_handler (0, None)
		self.stop ()
	
	def signal_aiohandler_stats (self) -> None:
		with log ('stats'):
			for (name, channel) in Stream (self._channels.channels.items ()).sorted (lambda kv: kv[0]):
				logger.info (f'Channel {name}: {len (channel):,d} entries')
			for task in Stream (self._tasks.tasks.values ()).sorted (key = lambda t: t.name):
				logger.info (f'Registered task {task}')
			for aio_task in asyncio.all_tasks ():
				logger.info (f'Task: {aio_task}')
				
	def executor (self, controller: Optional[Callable[[], Coroutine[Any, Any, None]]] = None) -> bool:
		self._channels = AIORuntime.Channels (self)
		self._tasks = AIORuntime.Tasks ()
		async def execution (controller: Callable[[], Coroutine[Any, Any, None]]) -> None:
			self._loop = asyncio.get_running_loop ()
			self._stop = self.future (type (None))
			self._apply_delay: Set[asyncio.Task[Any]] = set ()
			for signr in self.signals.saved:
				if self.signals[signr] == self.signal_handler:
					self._loop.add_signal_handler (signr, self.signal_aiohandler)
			if self.ctx.debug:
				self._loop.add_signal_handler (signal.SIGUSR1, self.signal_aiohandler_stats)
			await controller ()
			self._tasks.terminate ()
			myself = asyncio.current_task ()
			for task in asyncio.all_tasks ():
				if task != myself:
					if not task.done ():
						task.cancel ()
			await self.aioyield ()
		try:
			asyncio.run (execution (controller if controller is not None else self.controller), debug = self.ctx.debug)
		except asyncio.exceptions.CancelledError as e:
			logger.exception (f'failed due to canceled task: {e}')
		return True

	def executors (self) -> Optional[List[Callable[[], bool]]]:
		if (controllers := self.controllers ()) is not None and controllers:
			executables: List[Callable[[], bool]] = []
			for controller in controllers:
				executable = partial (self.executor, controller)
				try:
					name = controller.__name__
				except AttributeError:
					name = str (controller)
				setattr (executable, '__name__', name)
				executables.append (executable)
			return executables
		return None
	
	def controllers (self) -> Optional[List[Callable[[], Coroutine[Any, Any, None]]]]:
		return None
	
	async def controller (self) -> None:
		await self.finished ()

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
		
	async def wait (self, *awaitables: Awaitable[_U], timeout: Union[None, int, float] = None) -> Tuple[Awaitable[_U], _U]:
		done, pending = await asyncio.wait ([cast (Awaitable[_U], self._stop)] + list (awaitables), timeout = timeout, return_when = asyncio.FIRST_COMPLETED)
		Stream (pending).filter (lambda f: f is not self._stop).each (lambda f: f.cancel ())
		if self._stop in done:
			raise Stop ()
		if not done:
			raise Timeout ()
		awaitable = done.pop ()
		return (awaitable, await awaitable)
		
	def channel (self, name: str, t: Type[_T]) -> AIORuntime.Channel[_T]:
		return self._channels.get_as (name, t)
	
	def delete_channel (self, name: str) -> None:
		self._channels.delete (name)
	
	async def wait_channel (self, channels: List[AIORuntime.Channel[_T]], timeout: Union[None, int, float] = None) -> AIORuntime.Channel[_T]:
		return (await self.wait (self._channels.wait (channels), timeout = timeout))[1]

	async def select_channel (self, channels: List[AIORuntime.Channel[_T]], timeout: Union[None, int, float] = None) -> Tuple[AIORuntime.Channel[_T], _T]:
		return (await self.wait (self._channels.select (channels), timeout = timeout))[1]
	
	def task (self, name: str, coro: Coroutine[Any, Any, _T]) -> AIORuntime.Task[_T]:
		return self._tasks.launch (name, coro)
	
	def cancel_task (self, name: str) -> None:
		self._tasks.cancel (name)
	
	async def wait_task (self, tasks: List[AIORuntime.Task[_U]], timeout: Union[None, int, float] = None) -> Tuple[Optional[AIORuntime.Task[_U]], Optional[_U]]:
		with Ignore (Timeout):
			awaitables = [cast (Awaitable[_U], _t.task) for _t in tasks]
			aw, rc = await self.wait (*awaitables, timeout = timeout)
			return (tasks[awaitables.index (aw)], rc)
		return (None, None)
	
	async def watch (self, timeout: Union[None, int, float] = None, only_exceptions: bool = False) -> Optional[AIORuntime.Task[Any]]:
		with Ignore (Timeout, Stop):
			return (await self.wait (self._tasks.watch (only_exceptions = only_exceptions), timeout = timeout))[1]
		return None
	
	def workers (self, group: Optional[str] = None, limit: Optional[int] = None, *, cleanup_interval: Parsable = None, cleanup_threshold: int = 0) -> AIORuntime.Workers:
		return AIORuntime.Workers (group, limit, cleanup_interval, cleanup_threshold)

	@dataclass
	class Process:
		name: str
		args: Sequence[str]
		future: asyncio.Future[None]
		process: Optional[asyncio.subprocess.Process] = None
		async def launch (self) -> AIORuntime.Process:
			self.process = await asyncio.create_subprocess_exec (
				*self.args,
				stdin = asyncio.subprocess.PIPE,
				stdout = asyncio.subprocess.PIPE,
				stderr = asyncio.subprocess.PIPE
			)
			self.future.set_result (None)
			return self
		
		async def launched (self) -> None:
			await self.future
		
		async def wait (self) -> int:
			await self.launched ()
			if self.process is not None:
				return await self.process.wait ()
			raise error (f'{self}: not started')
		
		async def communicate (self, input: Optional[bytes] = None) -> Tuple[int, bytes, bytes]:
			await self.launched ()
			if self.process is not None:
				(out, err) = await self.process.communicate (input)
				returncode = self.process.returncode
				return (returncode if isinstance (returncode, int) else -1, out, err)
			raise error (f'{self}: not started')
		
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

	async def process (self, name: str, *args: str) -> AIORuntime.Process:
		process = AIORuntime.Process (name, args, self._loop.create_future ())
		coro = process.launch ()
		self._tasks.add (name, AIORuntime.Task (name, coro, asyncio.create_task (coro, name = name)))
		await process.launched ()
		return process
	
	async def scan (self, path: str, rescan: Parsable = None, predicate: Optional[Callable[[str], bool]] = None) -> AsyncIterator[str]:
		with Inotify () as inotify:
			inotify.add_watch (path, Mask.CREATE | Mask.CLOSE_WRITE | Mask.MOVED_TO | Mask.MOVED_FROM | Mask.DELETE)
			rescan_in = unit (rescan)
			rescan_last = 0
			ready: Deque[str] = deque ()
			available: Dict[str, bool] = {}
			timeout: Optional[int]
			while self.running:
				while self.running and ready:
					filename = ready.popleft ()
					if predicate is None or predicate (filename):
						yield filename
				if not rescan_last or rescan_in:
					now = int (time.time ())
					if not rescan_last or rescan_last + rescan_in < now:
						rescan_last = now
						new_files: List[Tuple[str, float]] = []
						old_files = set (available.keys ())
						for filename in os.listdir (path):
							if filename not in available:
								try:
									st = os.stat (os.path.join (path, filename))
									mtime = st.st_mtime
								except OSError:
									mtime = 0.0
								new_files.append ((filename, mtime))
							else:
								old_files.discard (filename)
						for filename in old_files:
							del available[filename]
						for (filename, _) in sorted (new_files, key = lambda kv: kv[1]):
							available[filename] = True
							ready.append (filename)
						continue
				#
				if rescan_in:
					timeout = max (0, int (time.time ()) - rescan_last + rescan_in)
				else:
					timeout = None
				done, pending = await asyncio.wait ([self._stop, inotify.get ()], timeout = timeout, return_when = asyncio.FIRST_COMPLETED)
				Stream (pending).filter (lambda f: f is not self._stop).each (lambda f: f.cancel ())
				if self._stop in done:
					break
				if done:
					event = await done.pop ()
					if event is not None and event.name is not None:
						filename = str (event.name)
						if event.mask & (Mask.CLOSE_WRITE | Mask.MOVED_TO):
							if not available.get (filename):
								available[filename] = True
								ready.append (filename)
						elif event.mask & Mask.CREATE:
							available[filename] = False
						elif event.mask & (Mask.MOVED_FROM | Mask.DELETE):
							available.pop (filename, None)
			await self.aioyield ()

	async def finished (self) -> None:
		await self._stop

	def stop (self) -> None:
		if not self._stop.done ():
			self._stop.set_result (None)
	
