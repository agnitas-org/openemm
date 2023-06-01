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
import	logging, signal
import	asyncio
from	collections import deque
from	dataclasses import dataclass, field
from	datetime import datetime
from	types import FrameType
from	typing import Any, Awaitable, Callable, Generic, Optional, TypeVar, Union
from	typing import Coroutine, Deque, Dict, List, Tuple, Type
from	typing import cast
from	.exceptions import error, Timeout, Stop
from	.log import log
from	.ignore import Ignore
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
	
	async def get (self, predicate: Optional[Callable[[_T], bool]] = None) -> _T:
		if self.queue:
			for element in self.queue:
				if predicate is None or predicate (element):
					self.queue.remove (element)
					await asyncio.sleep (0)
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
		if self.maxsize > 0 and self.putter and len (self.queue) < self.maxsize:
			for future in self.putter:
				if not future.done ():
					future.set_result (None)
		await asyncio.sleep (0)

class AIORuntime (Runtime):
	__slots__ = ['_loop', '_stop', '_channels', '_tasks']
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
		
		async def get (self, predicate: Optional[Callable[[_T], bool]] = None) -> _T:
			rc = await self.queue.get (predicate = predicate)
			if not len (self.queue):
				self.event.clear ()
			return rc
		
		async def put (self, element: _T, reject: bool = False) -> None:
			await self.queue.put (element)
			self.event.set ()
			await asyncio.sleep (0)
		
		async def pushback (self, element: _T) -> None:
			await self.put (element, reject = True)
		
		def __aiter__ (self) -> AIORuntime.Channel[_T]:
			return self
		
		async def __anext__ (self) -> _T:
			if not self.ref.running:
				raise StopAsyncIteration ()
			return await self.get ()

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
		async def join (self) -> _T:
			try:
				return await self.task
			except asyncio.exceptions.CancelledError:
				raise error (f'{self.name}: canceled')
		
		def cancel (self) -> None:
			self.task.cancel ()
			
	class Tasks:
		__slots__ = ['tasks']
		def __init__ (self) -> None:
			self.tasks: Dict[str, AIORuntime.Task[Any]] = {}
		
		def task (self, name: str, coro: Coroutine[Any, Any, _T]) -> AIORuntime.Task[_T]:
			self.tasks[name] = task = AIORuntime.Task (name, coro, asyncio.create_task (coro, name = name))
			def remover (task: asyncio.Task[_T]) -> None:
				with Ignore (KeyError):
					myself = self.tasks.pop (task.get_name ())
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
			Stream (self.tasks).gather ().each (lambda n: self.cancel (n))
		
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
		
	def signal_handler (self, sig: int, stack: Optional[FrameType]) -> Any:
		super ().signal_handler (sig, stack)
		with Ignore (AttributeError):
			self.stop ()
	
	def signal_handler_stats (self, sig: int, stack: Optional[FrameType]) -> Any:
		with log ('stats'):
			for (name, channel) in Stream (self._channels.channels.items ()).sorted (lambda kv: kv[0]):
				logger.info (f'Channel {name}: {len (channel):,d} entries')
			for task in Stream (self._tasks.tasks.values ()).sorted (key = lambda t: t.name):
				logger.info (f'Registered task {task}')
			for aio_task in asyncio.all_tasks ():
				logger.info (f'Task: {aio_task}')

	def executor (self) -> bool:
		self._channels = AIORuntime.Channels (self)
		self._tasks = AIORuntime.Tasks ()
		async def execution () -> None:
			if self.ctx.debug:
				self.setsignal (signal.SIGUSR1, self.signal_handler_stats)
			self._loop = asyncio.get_running_loop ()
			self._stop = self.future (type (None))
			await self.controller ()
			self._tasks.terminate ()
			myself = asyncio.current_task ()
			for task in asyncio.all_tasks ():
				if task != myself:
					if not task.done ():
						task.cancel ()
			await asyncio.sleep (0)
		try:
			asyncio.run (execution (), debug = self.ctx.debug)
		except asyncio.exceptions.CancelledError as e:
			logger.exception (f'failed due to canceled task: {e}')
		return True
	
	async def controller (self) -> None:
		await self._stop

	def future (self, t: Type[_T]) -> asyncio.Future[_T]:
		return self._loop.create_future ()

	async def delay (self, timeout: Union[int, float]) -> bool:
		await asyncio.wait ([self._stop], timeout = timeout if timeout > 0.0 else 0)
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
	
	async def watch (self, timeout: Union[None, int, float] = None, only_exceptions: bool = False) -> Optional[AIORuntime.Task[Any]]:
		with Ignore (Timeout, Stop):
			return (await self.wait (self._tasks.watch (only_exceptions = only_exceptions), timeout = timeout))[1]
		return None

	def stop (self) -> None:
		if not self._stop.done ():
			self._stop.set_result (None)

