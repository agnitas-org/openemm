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
import	os, stat, errno, signal, fcntl
import	time, pickle, mmap, subprocess
import	multiprocessing, logging
from	collections import deque
from	dataclasses import dataclass, field
from	datetime import datetime
from	enum import Enum
from	signal import Handlers
from	types import FrameType, TracebackType
from	typing import Any, Callable, Generic, NoReturn, Optional, Sequence, TypeVar, Union
from	typing import Deque, Dict, Generator, List, Set, Tuple, Type
from	typing import cast
from	.definitions import base
from	.exceptions import error, Timeout
from	.ignore import Ignore
from	.log import log
from	.parser import Parsable, unit
from	.stream import Stream
from	.tools import abstract
#
__all__ = ['Signal', 'Timer', 'Daemonic', 'Watchdog', 'EWatchdog', 'Daemon']
#
logger = logging.getLogger (__name__)
#
_T = TypeVar ('_T')
#
Handler = Union[Callable[[int, Optional[FrameType]], Any], int, Handlers, None]
class Signal:
	__slots__ = ['saved']
	known_signals: Dict[str, int] = (Stream (signal.__dict__.items ())
		.filter (lambda kv: isinstance (kv[1], int))
		.dict ()
	)
	known_handlers: Dict[str, Handlers] = (Stream (signal.__dict__.items ())
		.filter (lambda kv: isinstance (kv[1], Handlers))
		.dict ()
	)
	def __init__ (self,
		*args: Union[int, Handler, str],
		**kwargs: Union[Handler, str]
	) -> None:
		self.saved: Dict[int, Handler] = {}
		if len (args) % 2 == 1:
			raise error ('need pairs of signal and handler as positional arguments')
		if args:
			siglist = list (args)
			while siglist:
				signr = cast (Union[int, str], siglist.pop (0))
				handler = siglist.pop (0)
				self[signr] = handler
		for (signame, handler_name) in kwargs.items ():
			self[signame] = handler_name
	
	def __enter__ (self) -> Signal:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.restore ()
		return None

	def __setitem__ (self, sig: Union[int, str], handler: Union[Handler, str]) -> None:
		signr = self.find_signal (sig)
		old_handler = signal.signal (
			signr,
			self.find_handler (handler)
		)
		if signr not in self.saved:
			self.saved[signr] = old_handler

	def __getitem__ (self, sig: Union[int, str]) -> Handler:
		return signal.getsignal (self.find_signal (sig))
	
	def __delitem__ (self, sig: Union[int, str]) -> None:
		signr = self.find_signal (sig)
		try:
			old_handler = self.saved.pop (signr)
			signal.signal (signr, old_handler)
		except KeyError:
			raise KeyError (sig)
		
	def restore (self) -> None:
		Stream (self.saved.items ()).each (lambda kv: signal.signal (kv[0], kv[1]))
		self.clear ()
	
	def clear (self) -> None:
		self.saved.clear ()

	def find_signal (self, sig: Union[int, str]) -> int:
		if isinstance (sig, str):
			signame = sig.upper ()
			try:
				return Signal.known_signals[signame]
			except KeyError:
				try:
					return Signal.known_signals['SIG{name}'.format (name = signame)]
				except KeyError:
					raise KeyError (sig)
		else:
			return sig
	
	def find_handler (self, handler: Union[Handler, str]) -> Handler:
		if isinstance (handler, str):
			handlername = handler.upper ()
			try:
				return Signal.known_handlers[handlername]
			except KeyError:
				try:
					return Signal.known_handlers['SIG_{name}'.format (name = handlername)]
				except KeyError:
					raise KeyError (handler)
		else:
			return handler

class Timer:
	__slots__ = ['timeout', 'start']
	def __init__ (self, timeout: Parsable) -> None:
		self.timeout: Optional[float]
		if timeout is None or isinstance (timeout, float):
			self.timeout = timeout
		elif isinstance (timeout, str):
			self.timeout = float (unit.parse (timeout))
		else:
			self.timeout = float (timeout)
		self.start: Optional[float] = None
		
	def __enter__ (self) -> Timer:
		self.restart ()
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None
	
	def __call__ (self, delay: Optional[float] = None) -> Optional[float]:
		if self.start is not None:
			if delay is not None and delay > 0.0:
				time.sleep (delay)
			if self.timeout is not None:
				remain = self.timeout - (time.time () - self.start)
				if remain < 0.0:
					raise Timeout ()
				return remain
			return None
		raise error ('timer not started')
	
	def restart (self) -> None:
		self.start = time.time ()

	def reached (self, on_reach: Optional[Callable[[], None]] = None) -> bool:
		if self.timeout is not None:
			if self.start is not None:
				rc = time.time () - self.start > self.timeout
				if on_reach is not None and rc:
					on_reach ()
				return rc
			else:
				raise error ('timer not started')
		raise error ('Timer: no timeout specified')

	@staticmethod
	def guard (timeout: int, method: Callable[[], _T]) -> _T:
		def handler (sig: int, stack: Optional[FrameType]) -> Any:
			raise Timeout ()
		with Signal (signal.SIGALRM, handler):
			try:
				signal.alarm (timeout)
				return method ()
			finally:
				signal.alarm (0)
		
class Daemonic:
	"""Base class for daemon processes

This class provides the basic functionality to allow a process to run
as a daemon (a background process which runs continuously) and low
level process control. In general this (or one of its subclasses)
should be subclassed and extended for the process to implement."""
	__slots__ = ['running', 'signals']
	class Channel(Generic[_T]):
		__slots__ = ['_reader', '_writer']
		def __init__ (self) -> None:
			self._reader, self._writer = multiprocessing.Pipe (duplex = False)
			
		def __del__ (self) -> None:
			self.close ()
		
		def __enter__ (self) -> Daemonic.Channel[_T]:
			return self

		def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			self.close ()
			return None
			
		def close (self) -> None:
			self._close_writer ()
			self._close_reader ()

		def ready (self) -> bool:
			if not self._reader.closed and self._reader.poll ():
				return True
			return False

		def get (self) -> _T:
			self._close_writer ()
			try:
				return cast (_T, self._reader.recv ())
			finally:
				self._close_reader ()
		
		def put (self, value: _T) -> None:
			self._close_reader ()
			try:
				self._writer.send (value)
			finally:
				self._close_writer ()
		
		def _close_reader (self) -> None:
			if not self._reader.closed:
				self._reader.close ()
			
		def _close_writer (self) -> None:
			if not self._writer.closed:
				self._writer.close ()
			
	@classmethod
	def call (cls, method: Callable[..., Any], *args: Any, **kwargs: Any) -> Any:
		"""Call a method in a subprocess, return process status"""
		def wrapper (channel: Daemonic.Channel[Any], method: Callable[..., Any], *args: Any, **kwargs: Any) -> None:
			try:
				rc = method (*args, **kwargs)
			except BaseException as e:
				rc = e
			channel.put (rc)

		channel: Daemonic.Channel[Any]
		with Daemonic.Channel () as channel:
			d = cls ()
			p = d.join (join_pid = d.spawn (wrapper, channel, method, *args, **kwargs))
			if p.error is not None:
				raise p.error
			if not channel.ready ():
				return None
			rc: Any = channel.get ()
			if issubclass (type (rc), BaseException):
				if type (rc) is SystemExit:
					return rc.code
				raise rc
			return rc

	def __init__ (self) -> None:
		self.running = True
		self.signals = Signal ()

	def done (self) -> None:
		self.signals.restore ()
		
	def setsignal (self, signr: int, action: Handler) -> None:
		self.signals[signr] = action
			
	def signal_handler (self, sig: int, stack: Optional[FrameType]) -> Any:
		"""Standard signal handler for graceful termination"""
		self.running = False
	
	def setup_handler (self, master: bool = False) -> None:
		"""Setup signal handler, ``master'' should be True, if signal handling is not handled elsewhere, too"""
		self.setsignal (signal.SIGTERM, self.signal_handler)
		if master:
			self.setsignal (signal.SIGINT, self.signal_handler)
		else:
			self.setsignal (signal.SIGINT, signal.SIG_IGN)
		self.setsignal (signal.SIGCHLD, signal.SIG_DFL)
		self.setsignal (signal.SIGHUP, signal.SIG_IGN)
		self.setsignal (signal.SIGPIPE, signal.SIG_IGN)
	
	def push_to_background (self) -> bool:
		"""Fork a subprocess, returns True for the parent and False for the subprocess"""
		if os.fork () > 0:
			return True
		for fd in 0, 1, 2:
			with Ignore (OSError):
				os.close (fd)
		fd = os.open (os.devnull, os.O_RDWR)
		if fd == 0:
			if fcntl.fcntl (fd, fcntl.F_DUPFD) == 1:
				fcntl.fcntl (fd, fcntl.F_DUPFD)
		os.setsid ()
		return False
	
	def redirect (self, input_path: Optional[str], output_path: Optional[str]) -> None:
		"""Redirect stdin to ``input_path'' and stdout and stderr to ``output_path''"""
		for (path, flags, mode, targets) in [
			(input_path, os.O_RDONLY, 0, [0]),
			(output_path, os.O_WRONLY | os.O_APPEND | os.O_CREAT | os.O_SYNC, 0o600, [1, 2])
		]:
			if path is not None:
				fd = os.open (path, flags, mode)
				for nfd in targets:
					os.close (nfd)
					cfd = fcntl.fcntl (fd, fcntl.F_DUPFD)
					if cfd != nfd:
						logger.error (f'Expect new fd #{nfd}, but got #{cfd}')
				os.close (fd)

	def spawn (self, method: Callable[..., Any], *args: Any, **kwargs: Any) -> int:
		"""Create a subprocess and call ``method'' using ``args'' as its arguments"""
		pid = os.fork ()
		if pid == 0:
			ec = 1
			self.signals.clear ()
			try:
				self.setup_handler (False)
				ec = method (*args, **kwargs)
				if ec is None:
					ec = 0
				elif isinstance (ec, bool):
					ec = 0 if ec else 1
				elif not isinstance (ec, int):
					ec = 0
			except Exception as e:
				logger.exception (f'Failed to call {method}: {e}')
				raise
			finally:
				os._exit (ec)
		return pid
	
	def execute (self, cmd: List[str], fail_code: int = 127) -> NoReturn:
		"""Executes a command ``cmd'' (a list), replacing the currently running process"""
		if len (cmd) > 0:
			with Ignore (OSError):
				if cmd[0].startswith (os.path.sep):
					os.execv (cmd[0], cmd)
				else:
					os.execvp (cmd[0], cmd)
		os._exit (fail_code)

	@dataclass
	class Status:
		pid: int = 0
		status: Optional[int] = None
		exitcode: Optional[int] = None
		signal: Optional[int] = None
		nochild: bool = False
		error: Optional[OSError] = None
	def join (self, join_pid: int = -1, timeout: Parsable = None) -> Daemonic.Status:
		"""Waits for ``join_pid'' (if -1, then for any child
process), ``timeout'' is the time in seconds to max. wait. If this is
``None'', no timeout is used."""
		def waitfor (flags: int = 0) -> Daemonic.Status:
			rc = Daemonic.Status ()
			try:
				(rc.pid, rc.status) = os.waitpid (join_pid, flags)
				if rc.pid > 0:
					if os.WIFEXITED (rc.status):
						rc.exitcode = os.WEXITSTATUS (rc.status)
					elif os.WIFSIGNALED (rc.status):
						rc.signal = os.WTERMSIG (rc.status)
			except OSError as e:
				rc.error = e
				rc.nochild = (e.errno == errno.ECHILD)
			return rc
		#
		if timeout is None:
			return waitfor ()
		#
		rc = waitfor (os.WNOHANG)
		if timeout:
			with Ignore (Timeout), Timer (timeout) as timer:
				while not rc.pid and rc.error is None:
					timer (0.1)
					rc = waitfor (os.WNOHANG)
		return rc

	def term (self, pid: int, sig: Optional[int] = None) -> bool:
		"""Terminates a child process ``pid'' with signal ``sig'' (if None, SIGTERM is used)"""
		try:
			os.kill (pid, sig if sig is not None else signal.SIGTERM)
			return True
		except OSError as e:
			return e.errno != errno.ESRCH

	class State (Enum):
		"""Daemon State

This enumeration represents the current state of a member in a daemon
group. Currently these states are definied:
	- new: the member has been created, but not added to a group
	- schedulued: the member had been added to the group and is ready to be scheduled
	- running: the member had been started and is currently an active subprocess
	- dying: the member had been ended or singaled to terminate, but had not died and collected
	- terminated: the member had been finished and collected by the group
	- canceled: the member had been canceled before ever been started
"""
		new = 0
		scheduled = 1
		running = 2
		dying = 3
		terminated = 4
		canceled = 5
	@dataclass
	class Member (Generic[_T]):
		"""Daemon Member

This represents one method that is called in a subprocess. The
``context'' is any user provided data structure and its attributes
can be accessed through the member using attribute notation. The
``channel'' is a communication channel to report the exit status
(return value) of the subprocess back to the caller. The ``state''
reprensents (Daemonic.State) the current state of the member and
``status'' is filled with the Daemonic.Status when the subprocess
had terminated.
"""
		method: Callable[..., Any]
		args: Sequence[Any]
		kwargs: Dict[str, Any]
		context: _T
		channel: Optional[Daemonic.Channel[_T]] = None
		value: Any = None
		state: Daemonic.State = field (default_factory = lambda: Daemonic.State.new)
		status: Optional[Daemonic.Status] = None
		pid: Optional[int] = None
		launched: datetime = datetime (1970, 1, 1)
		def __getattr__ (self, attribute: str) -> Any:
			if self.context is not None:
				return getattr (self.context, attribute)
			raise AttributeError (attribute)
	@dataclass
	class Stats (Generic[_T]):
		scheduled: List[Daemonic.Member[_T]]
		active: List[Daemonic.Member[_T]]
		ended: List[Daemonic.Member[_T]]
	@dataclass
	class Group (Generic[_T]):
		"""Daemon Group

This class allows managing a group of process. Each member of the group
is represented by an instance of the Daemon.Member class. In general it
is not instantiated directly but using the method ``group'' of a Daemonic
instance. This class provides the context manager interface so it could
be used in a ``with ...:'' construct.

The ``limit'' is the maximum number of parallel running subprocess for
this group. This allows to schedule a large amount of subprocess but
guarantee to limit the number of actual running subprocesses.

One can either pass a list of ``Daemonic.Memeber'' instances during
instantiation or add them using the ``add'' method later. New members
can even be added when there are already subprocess running.

The method ``remove'' will remove a member from the group. If this
member had already been started as a subprocess, it will not be terminate
but removed from managing.

The method ``start'' starts as many scheduled process as possible up
to limit (or until all process had been started.)

The method ``stop'' will either remove a member, if it is not yet started
or terminate it. If option ``hard'' is passed, it is terminated with a
SIGKILL, otherwise with SIGTERM.

The method ``wait'' will wait for finished processes, updates their ``state''
and ``status'' and returns the number of joined processes.

The method ``cancel'' will remove all scheduled members from schedule queue,
update their ``state'' and ``status'' and will never start them.

The method ``term'' will call ``cancel'' first and then terminate all running
processes so after this call no more subprocesses for this group should be
active anymore.

The method ``run'' will use the above methods to manage the start/wait
process including an optional timeout for all processes. This method
is called at the exit of the context manager.
"""
		daemon: Daemonic
		limit: Optional[int] = None
		scheduled: Deque[Daemonic.Member[_T]] = field (default_factory = deque)
		active: Dict[int, Daemonic.Member[_T]] = field (default_factory = dict)
		status: List[Daemonic.Member[_T]] = field (default_factory = list)
		log: Optional[Callable[[Daemonic.Member[_T], str], None]] = None
		def __enter__ (self) -> Daemonic.Group[_T]:
			return self

		def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			self.run ()
			return None

		def __iter__ (self) -> Generator[Daemonic.Member[_T], None, None]:
			for member in [_s for _s in self.status if _s.state in (Daemonic.State.terminated, Daemonic.State.canceled)]:
				yield member
				self.status.remove (member)

		def stats (self) -> Daemonic.Stats[_T]:
			return Daemonic.Stats (
				scheduled = list (self.scheduled),
				active = list (self.active.values ()),
				ended = list (self)
			)

		def is_active (self) -> bool:
			return bool (self.scheduled or self.active)
		
		def is_idle (self) -> bool:
			return not self.is_active ()
			
		def add (self, member: Daemonic.Member[_T]) -> Daemonic.Member[_T]:
			member.state = Daemonic.State.scheduled
			self.scheduled.append (member)
			if self.log: self.log (member, 'added')
			return member
		
		def remove (self, member: Daemonic.Member[_T]) -> Daemonic.Member[_T]:
			with Ignore (ValueError):
				while True:
					self.scheduled.remove (member)
					if self.log: self.log (member, 'removed from schedule')
			for (pid, member) in Stream (self.active.items ()).filter (lambda kv: kv[1] is member).list ():
				del self.active[pid]
				if self.log: self.log (member, 'removed from active')
			with Ignore (ValueError):
				while True:
					self.status.remove (member)
					if self.log: self.log (member, 'removed from status')
			return member
		
		def start (self) -> int:
			def starter (member: Daemonic.Member[_T]) -> None:
				member.state = Daemonic.State.running
				member.pid = os.getpid ()
				member.launched = datetime.now ()
				try:
					if self.log: self.log (member, 'starting')
					rc = member.method (*member.args, **member.kwargs)
					if self.log: self.log (member, f'ending with {rc!r}')
				except Exception as e:
					logger.exception (f'{member}: failed due to: {e}')
					if self.log: self.log (member, f'terminating by {e}')
					rc = e
				if member.channel is not None:
					member.channel.put (rc)
					member.channel.close ()
					member.channel = None
				member.state = Daemonic.State.dying
			#
			counter = 0
			while self.scheduled and (self.limit is None or self.limit > len (self.active)):
				member = self.scheduled.popleft ()
				member.channel = Daemonic.Channel ()
				pid = self.daemon.spawn (starter, member)
				if pid > 0:
					member.state = Daemonic.State.running
					member.pid = pid
					member.launched = datetime.now ()
					if self.log: self.log (member, 'spawnd')
					self.active[pid] = member
					self.status.append (member)
					counter += 1
				else:
					if self.log: self.log (member, 'spawn failed')
					self.scheduled.appendleft (member)
					break
			return counter
		
		def stop (self, member: Daemonic.Member[_T], hard: bool = False) -> Daemonic.Member[_T]:
			if member.state == Daemonic.State.scheduled:
				with Ignore (ValueError):
					while True:
						self.scheduled.remove (member)
						if self.log: self.log (member, 'stop schedule')
			elif member.state in (Daemonic.State.running, Daemonic.State.dying):
				pid = Stream (self.active.items ()).filter (lambda kv: kv[1] is member).map (lambda kv: kv[0]).first (no = 0)
				if pid > 0:
					member.state = Daemonic.State.dying
					self.daemon.term (pid, signal.SIGTERM if not hard else signal.SIGKILL)
					if self.log: self.log (member, 'stop {how}'.format (how = 'hard' if hard else 'soft'))
			return member

		def wait (self, timeout: Parsable = None) -> int:
			counter = 0
			with Ignore (Timeout), Timer (timeout) as timer:
				while self.is_active ():
					self.start ()
					status = self.daemon.join (timeout = timer () if timeout else timeout)
					if status is not None:
						if status.pid:
							with Ignore (KeyError):
								member = self.active.pop (status.pid)
								if member.channel is not None:
									if member.channel.ready ():
										member.value = member.channel.get ()
									member.channel.close ()
									member.channel = None
								member.state = Daemonic.State.terminated
								member.status = status
								counter += 1
								if self.log: self.log (member, 'joined')
						elif status.error:
							break
			return counter

		def cancel (self) -> None:
			while self.scheduled:
				member = self.scheduled.popleft ()
				member.state = Daemonic.State.canceled
				self.status.append (member)
				if self.log: self.log (member, 'canceled')
			
		def term (self, hard_kill_delay: Parsable = None) -> None:
			self.cancel ()
			signr = signal.SIGTERM if hard_kill_delay else signal.SIGKILL
			while self.active:
				for (pid, member) in list (self.active.items ()):
					member.state = Daemonic.State.dying
					if not self.daemon.term (pid, signr):
						logger.warning (f'{pid}: failed to kill no (more) existing process')
						del self.active[pid]
						if self.log: self.log (member, 'term failed')
					else:
						if self.log: self.log (member, f'terminated with {signr}')
				self.wait (hard_kill_delay)
				signr = signal.SIGKILL
				hard_kill_delay = None

		def run (self, timeout: Parsable = None, hard_kill_delay: Parsable = None) -> List[Daemonic.Member[_T]]:
			try:
				with Timer (timeout) as timer:
					while self.daemon.running and self.is_active ():
						if self.scheduled:
							self.start ()
						if self.active:
							self.wait (0.1 if self.scheduled else timer ())
			except Timeout:
				self.term (hard_kill_delay)
			return self.status

	def group (self, limit: Optional[int] = None, *members: Daemonic.Member[_T]) -> Daemonic.Group[_T]:
		"""Create a process group, i.e. a controller for
several in parallel executing subprocesses. Optional the parameter
``limit'' definies the maximum number of parallel started
subprocesses. Every further positonal argument must be an instance of
``Daemonic.Member'' and will be added as part of the process group. It
is possible to add later more processes using the ``add'' method."""
		group: Daemonic.Group[_T] = Daemonic.Group (self, limit = limit)
		Stream (members).each (lambda m: group.add (m))
		return group

class Watchdog (Daemonic):
	"""Watchdog framework

This class is based on Daemonic to implement a watchdog functionality.
It provides serveral methods which can be overwritten by the subclass.
To start the process one must call the method ``mstart''. The method
``start'' is considered as legacy and should no longer be used. The
watchdog supports monitoring of more than one subprocess.

Every process to run under watchdog control must be an instance of
``Watchdog.Job'' or a subclass of this class."""
	__slots__: List[str] = []
	EC_EXIT = 0
	EC_RESTART_REQUEST = 11
	EC_RESTART_FAILURE = 12
	EC_STOP = 13
	class Job:
		"""Represents a single subprocess under watchdog control"""
		__slots__ = ['name', 'method', 'args', 'output', 'heartbeat', 'watchdog', 'pid', 'last', 'incarnation', 'hb', 'killed_by_heartbeat']
		class Restart (Exception):
			"""Exception to be thrown to force a restart of the process"""
		def __init__ (self,
			name: str,
			method: Callable[..., Any],
			args: Tuple[Any, ...],
			output: Optional[str] = None,
			heartbeat: Optional[int] = None
		) -> None:
			"""Setup the subprocess

``name'' is the name to identify the process, ``method'' is the method
to call with ``args'' as its arguments. The method must either return
True, if it terminates regular and no restart is required, or False if
the method returns with an error, but not restart should take place.
To enforce a restart, the method should raise the Job.Restart
exception. Every other exception will also result in a restart.

If ``output'' is not None, this is a path where stdout and stderr are
redirected to. In this case, stdin is redirected to ``/dev/null''. If
heartbeat is not None, it must be a positive numeric value which
represents seconds in between the child process must signal to be
active calling the method ``beat''. If there is no heartbeat, the
watchdog process will reststart the process assuming it is in some
undefined condition."""
			self.name = name
			self.method = method
			self.args = args
			self.output = output
			self.heartbeat = heartbeat
			self.watchdog: Optional[Watchdog] = None
			self.pid: Optional[int] = None
			self.last = 0
			self.incarnation = 0
			self.hb: Optional[Watchdog.Heart.Beat] = None
			self.killed_by_heartbeat = False
		
		def __str__ (self) -> str:
			return f'Job ({self.name!r}) with PID {self.pid!r}'
		__repr__ = __str__
		
		def __call__ (self) -> int:
			"""Entry point for Watchdog to start this job"""
			try:
				if self.output is not None and self.watchdog is not None:
					try:
						self.watchdog.redirect (os.devnull, self.output)
					except Exception as e:
						logger.error (f'Failed to establish redirection: {e}')
				rc = self.method (*self.args)
			except Watchdog.Job.Restart:
				return Watchdog.EC_RESTART_REQUEST
			except Exception as e:
				logger.exception (f'Execution failed: {e}')
				return Watchdog.EC_RESTART_FAILURE
			return Watchdog.EC_EXIT if rc else Watchdog.EC_STOP
		
		def restart (self) -> NoReturn:
			raise Watchdog.Job.Restart ()
		
		def beat (self) -> None:
			"""Signal an active subprocess"""
			if self.hb is not None:
				self.hb.set (int (time.time ()))
		
		def beating (self) -> bool:
			"""Checks for active subprocess (called by the Watchdog)"""
			if self.hb is not None:
				last = self.hb.get ()
				if last is not None:
					now = int (time.time ())
					return bool (last + cast (int, self.heartbeat) > now)
			return True
	
	class Heart:
		__slots__ = ['count', 'chunk', 'mem', 'cur']
		class Beat:
			__slots__ = ['mem', 'pos', 'size']
			def __init__ (self, mem: mmap.mmap, pos: int, size: int) -> None:
				self.mem = mem
				self.pos = pos
				self.size = size
			
			def clear (self) -> None:
				self.set (None)
			
			def set (self, value: Any) -> None:
				ser = pickle.dumps (value)
				if len (ser) < self.size - 8:
					self.mem[self.pos:self.pos + self.size] = b'%8d%s%s' % (len (ser), ser, b' ' * (self.size - len (ser) - 8))
			
			def get (self) -> Any:
				for retry in 0, 1, 2:
					ser = self.mem[self.pos:self.pos + self.size]
					size = int (ser[:8])
					try:
						return pickle.loads (ser[8:size + 8])
					except Exception as e:
						logger.error (f'Failed to unpickle input: {e}')
					time.sleep (0.1)
				return None
				
		def __init__ (self, count: int) -> None:
			self.count = count
			self.chunk = 512
			self.mem = mmap.mmap (-1, self.count * self.chunk)
			self.cur = 0

		def done (self) -> None:
			self.mem.close ()
		
		def slot (self) -> Watchdog.Heart.Beat:
			if self.cur >= self.count:
				raise IndexError ('no more slots left for new heartbeat entry')
			rc = self.Beat (self.mem, self.cur * self.chunk, self.chunk)
			rc.clear ()
			self.cur += 1
			return rc
			
	def mstart (self,
		jobs: Union[Watchdog.Job, List[Watchdog.Job]],
		restart_delay: Union[None, float, int, str] = None,
		termination_delay: Union[None, float, int, str] = None,
		max_incarnations: Optional[int] = None
	) -> None:
		"""Starts one or more ``jobs''

The ``jobs'' are (either an instance of Watchdog.Job or a list of
instances of Watchdog.Job. ``restart_delay'' is the amount in seconds
to restrart a subprocess which has terminated unexpected.
``termination_delay'' is the amount in seconds when a subprocess has
been terminated and is still active to kill it the hard way.
``maxIncarnation'', if not None, is the maximum number of restarts for
a process until the watchdog gives up."""
		restart_delay = unit.parse (restart_delay, 60)
		termination_delay = unit.parse (termination_delay, 10)
		joblist = [jobs] if isinstance (jobs, self.Job) else jobs
		hb: Optional[Watchdog.Heart] = None
		for job in joblist:
			job.watchdog = self
			if job.heartbeat is not None:
				if hb is None:
					hb = Watchdog.Heart (len (joblist))
				job.hb = hb.slot ()
			logger.info (f'Added job {job.name}')
		done: List[Watchdog.Job] = []
		self.setup_handler (True)
		logger.info ('Startup with {count} jobs'.format (count = len (joblist)))
		self.startup (joblist)
		nolog: Set[Watchdog.Job] = set ()
		while self.running and joblist:
			active = 0
			now = int (time.time ())
			for job in joblist:
				if job.pid is None:
					if job.last + restart_delay < now:
						job.killed_by_heartbeat = False
						job.beat ()
						self.spawning (job)
						def wrapper () -> Any:
							with log (job.name):
								self.started (job)
								rc: Any = None
								try:
									rc = job ()
								finally:
									rc = self.ended (job, rc)
								return rc
						job.pid = self.spawn (wrapper)
						job.last = now
						job.incarnation += 1
						logger.info (f'Launching job {job.name}')
						active += 1
						if job in nolog:
							nolog.remove (job)
					else:
						if job not in nolog:
							logger.info ('Job {name} is not ready, will start in {restart} seconds'.format (
								name = job.name,
								restart = job.last + restart_delay - now
							))
							nolog.add (job)
				else:
					active += 1
					if not job.beating ():
						logger.warning (f'Job {job.name} (PID {job.pid}) heart beat failed, force hard kill')
						if not self.term (job.pid, signal.SIGKILL):
							logger.error (f'Job {job} is no more existing')
						job.killed_by_heartbeat = True
			#
			if not self.running: continue
			time.sleep (1)
			if not self.running or not active: continue
			#
			rc = self.join (timeout = 0)
			if rc.pid == 0:
				continue
			#
			if rc.error is not None:
				if rc.nochild:
					logger.error (f'Even with {active} active jobs we cannot wait for one')
				elif rc.error.errno != errno.EINTR:
					logger.warning (f'Waiting for {active} jobs is interrupted by {rc.error}')
				continue
			#
			for job in joblist:
				if job.pid == rc.pid:
					found = job
					break
			else:
				logger.info (f'Collected PID {rc.pid} without matching job')
				continue
			#
			restart = True
			if rc.exitcode is not None:
				if rc.exitcode in (self.EC_EXIT, self.EC_STOP) or (max_incarnations and found.incarnation >= max_incarnations):
					if rc.exitcode == self.EC_EXIT:
						logger.info (f'Job {found.name} terminated normally, no restart scheduled')
					elif rc.exitcode == self.EC_STOP:
						logger.info (f'Job {found.name} terminates with error, but enforces no restart')
					else:
						logger.info (f'Job {found.name} terminates with error, but reached maximum incarnation counter {found.incarnation}')
					restart = False
				elif rc.exitcode == self.EC_RESTART_REQUEST:
					logger.info (f'Job {found.name} terminated for restart requested by application')
				elif rc.exitcode == self.EC_RESTART_FAILURE:
					logger.info (f'Job {found.name} terminated for restart due to execution failure')
				else:
					logger.info (f'Job {found.name} terminated with exit code {rc.exitcode}')
			elif rc.signal is not None:
				if rc.signal in (signal.SIGKILL, signal.SIGTERM):
					if rc.signal == signal.SIGKILL and found.killed_by_heartbeat:
						logger.info (f'Job {found.name} had been killed due to missing heart beat, restart scheduled')
					else:
						restart = False
						logger.info (f'Job {found.name} died due to signal {rc.signal}, no restart scheduled assuming forced termination')
				else:
					logger.info (f'Job {found.name} died due to signal {rc.signal}')
			if not restart:
				joblist.remove (found)
				done.append (found)
			self.joining (found, rc)
			found.pid = None
		#
		self.terminating (joblist, done)
		logger.info ('Terminating')
		n = termination_delay
		while joblist:
			rc = self.join (timeout = 0)
			for job in joblist[:]:
				if job.pid == rc.pid or job.pid is None:
					joblist.remove (job)
					done.append (job)
					if job.pid is not None:
						self.joining (job, rc)
			#
			if not joblist: continue
			#
			if rc.nochild and joblist:
				logger.error ('While waiting for {count} jobs ({jobs}), we got the information that no further child is active ({rc}), aberting'.format (
					count = len (joblist),
					jobs = Stream (joblist).join (', '),
					rc = rc
				))
				break
			#
			if n in (0, termination_delay):
				for job in joblist:
					self.term (cast (int, job.pid), signal.SIGKILL if n == 0 else signal.SIGTERM)
					logger.info ('Signaled job {name} to terminate {how}'.format (
						name = job.name,
						how = 'gracefully' if n == 0 else 'forced'
					))
			n -= 1
			time.sleep (1)
		self.teardown (done)
		if hb is not None:
			hb.done ()
		logger.info ('Teardown')
		self.done ()

	def start (self, *args: Any, **kwargs: Any) -> None:
		def starter (*args: Any, **kwargs: Any) -> Any:
			ec = self.run (*args, **kwargs)
			if ec == self.EC_RESTART_REQUEST:
				raise Watchdog.Job.Restart ()
			return ec == self.EC_EXIT
		self.mstart (self.Job ('default', starter, ()), *args, **kwargs)
	#
	# Methods to override
	#
	# Must implement as entry point for new process
	def run (self, *args: Any, **kwargs: Any) -> Any:
		"""The entry point for the legacy ``start'' method"""
		abstract ()
	#
	# called once during startup
	def startup (self, jobs: List[Watchdog.Job]) -> None:
		"""Is called after setup, but before any child process is started"""
	#
	# called once for teardonw
	def teardown (self, done: List[Watchdog.Job]) -> None:
		"""Is called after all child processs are terminated, but before the watchdog exits

``done'' is the list of jobs which are known to had terminated."""
	#
	# called once when ready to terminate
	def terminating (self, jobs: List[Watchdog.Job], done: List[Watchdog.Job]) -> None:
		"""Is called during exit before terminating the children

``jobs'' are a list of still active children and ``done'' is a list of
jobs already terminated."""
	#
	# called before every starting of a process
	def spawning (self, job: Watchdog.Job) -> None:
		"""Is called after ``job'' had been started"""
	#
	# called after every joining of a process
	def joining (self, job: Watchdog.Job, ec: Daemonic.Status) -> None:
		"""Is called after ``job'' has terminated with ``ec'' exit condition"""
	#
	# called in subprocess before invoking method
	def started (self, job: Watchdog.Job) -> None:
		"""Is called before invoking ``job'' entry point"""
	#
	# called in subprocess after method completed
	def ended (self, job: Watchdog.Job, rc: Any) -> Any:
		"""Is called after ``job'' method finished with return value"""
		return rc

class EWatchdog (Watchdog):
	"""External watchdog

Starts an external program under watchdog control."""
	__slots__ = ['cmd']
	def __init__ (self, cmd: List[str]) -> None:
		"""``cmd'' is the command (as a list) to be executed"""
		super ().__init__ ()
		self.cmd = cmd
	
	def run (self, *args: Any, **kwargs: Any) -> Any:
		self.execute (self.cmd, fail_code = self.EC_STOP)

class Daemon (Daemonic):
	"""Daemon process base class

For writing a daemon process you can subclass this class and overwrite
the ``run'' method."""
	__slots__ = ['name']
	def __init__ (self, name: str) -> None:
		"""``name'' is the name of the process"""
		super ().__init__ ()
		self.name = name
	
	def __check_script (self, path: str, script: str) -> None:
		if os.path.isfile (path):
			with open (path, 'r') as fd:
				content = fd.read ()
			renew = script != content
		else:
			renew = True
		for (cpath, mask, cmode) in (base, 0o111, 0o711), (os.path.dirname (path), 0o555, 0o755):
			try:
				st = os.stat (cpath)
				if not stat.S_ISDIR (st.st_mode):
					raise error (f'Required path "{cpath}" is no directory')
				nmode = st.st_mode | mask
				if nmode != st.st_mode:
					os.chmod (cpath, stat.S_IMODE (nmode))
			except OSError as e:
				if e.errno != errno.ENOENT:
					raise
				omask = os.umask (0)
				os.mkdir (cpath, cmode)
				os.umask (omask)
		if renew:
			with open (path, 'w') as fd:
				fd.write (script)
		st = os.stat (path)
		nmode = st.st_mode | 0o555
		if nmode != st.st_mode:
			os.chmod (path, stat.S_IMODE (nmode))
	
	def check_custom_script (self, script: str) -> bool:
		"""Check condition for process

This method starts a custom script to check if the runtime condition
are in a sane condition. If the script does not exists, it is created
using the content of ``script''."""
		path = os.path.join (base, 'bin', 'check_custom.sh')
		self.__check_script (path, script)
		pp = subprocess.Popen ([path], stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE, text = True, errors = 'backslashreplace')
		(pout, perr) = pp.communicate (None)
		if pp.returncode != 0:
			logger.warning (f'Check script returns {pp.returncode}:\nStdout: {pout}\nStderr: {perr}')
			return False
		return True
	
	def __starter (self, run_in_background: bool, runner: Callable[..., Any], *args: Any) -> None:
		if run_in_background and self.push_to_background ():
			return
		logger.info (f'{self.name}: starting up')
		self.setup_handler (True)
		runner (*args)
		logger.info (f'{self.name}: going down')
	
	@dataclass
	class Child:
		method: Callable[..., Any] = lambda: None
		pid: int = -1
		status: int = 0

	def __wait (self, active: Dict[int, Daemon.Child], block: bool = True) -> None:
		got = True
		while got and active:
			got = False
			st = self.join (timeout = None if block else 0)
			if st.error is not None:
				if st.nochild:
					logger.warning ('Unable to wait for any child even if {count} are looking active'.format (count = len (active)))
					for child in list (active.values ()):
						del active[child.pid]
			elif st.pid:
				try:
					child = active[st.pid]
					child.pid = -1
					child.status = st.status if st.status is not None else 0
					del active[st.pid]
					got = True
					block = False
					if st.exitcode is not None:
						how = 'exited'
						if st.exitcode > 0:
							how += f' with status {st.exitcode}'
					elif st.signal is not None:
						how = f'killed by {st.signal}'
					else:
						how = f'died with status {child.status}'
					logger.info (f'Child {child.method.__name__} {how}')
				except KeyError:
					logger.warning (f'Returned not active PID {st.pid}')

	def __watchdog (self, child_methods: List[Callable[..., Any]], restart_on_error: bool) -> None:
		children = [Daemon.Child (method = _c) for _c in child_methods]
		active: Dict[int, Daemon.Child] = {}
		logger.info ('Watchdog starting with {count} child(ren)'.format (count = len (children)))
		while self.running:
			for child in children:
				if child.pid == -1:
					if child.status == 0 or restart_on_error:
						child.pid = self.spawn (child.method)
						child.status = 0
						active[child.pid] = child
						logger.info (f'Child {child.method.__name__} started')
			if not active:
				self.running = False
			else:
				self.__wait (active)
		if active:
			logger.info ('Watching waiting for {count} active child(ren)'.format (count = len (active)))
			signr: Optional[int] = None
			while active:
				for child in list (active.values ()):
					if not self.term (child.pid, signr):
						logger.warning (f'Unexpected missing child process {child.method.__name__}')
						del active[child.pid]
				if active:
					for state in True, False:
						self.__wait (active, False)
						if state and active:
							time.sleep (1)
				signr = signal.SIGKILL
		logger.info ('Watchdog terminating')
	
	def start (self, run_in_background: bool = False) -> None:
		"""Start the daemon, push it to the background, if ``run_in_background'' is True"""
		self.__starter (run_in_background, self.run)
	
	def start_with_watchdog (self, child_methods:List[Callable[..., Any]], run_in_background: bool = False, restart_on_error: bool = False) -> None:
		"""Start the daemon under watchdog control"""
		self.__starter (run_in_background, self.__watchdog, child_methods, restart_on_error)

	def run (self, *args: Any, **kwargs: Any) -> Any:
		"""Entry point for implemention"""

