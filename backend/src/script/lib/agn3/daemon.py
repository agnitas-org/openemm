####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	os, stat, errno, signal, queue, select, fcntl
import	time, pickle, mmap, subprocess
import	platform, multiprocessing, logging
from	abc import abstractmethod
from	collections import deque
from	dataclasses import dataclass
from	queue import Queue
from	signal import Signals, Handlers
from	types import FrameType, TracebackType
from	typing import Any, Callable, NoReturn, Optional, Union
from	typing import Deque, Dict, List, Set, Tuple, Type
from	typing import cast
from	.config import Config
from	.definitions import base, host
from	.exceptions import error
from	.ignore import Ignore
from	.log import log
from	.parser import Unit
from	.process import Parallel
from	.rpc import XMLRPC
from	.stream import Stream
#
__all__ = ['Signal', 'Daemonic', 'Watchdog', 'EWatchdog', 'Daemon', 'Worker']
#
logger = logging.getLogger (__name__)
#
Handler = Union[Callable[[Signals, FrameType], None], int, Handlers, None]
class Signal:
	__slots__ = ['saved']
	known_signals: Dict[str, Signals] = (Stream.of (signal.__dict__.items ())
		.filter (lambda kv: isinstance (kv[1], Signals))
		.dict ()
	)
	known_handlers: Dict[str, Handlers] = (Stream.of (signal.__dict__.items ())
		.filter (lambda kv: isinstance (kv[1], Handlers))
		.dict ()
	)
	def __init__ (self,
		*args: Union[Signals, Handler],
		**kwargs: Handler
	) -> None:
		self.saved: Dict[Signals, Handler] = {}
		if len (args) % 2 == 1:
			raise error ('need pairs of signal and handler as positional arguments')
		if args:
			siglist = list (args)
			while siglist:
				signr = cast (Union[Signals, str], siglist.pop (0))
				handler = siglist.pop (0)
				self[signr] = handler
		for (signame, handler) in kwargs.items ():
			self[signame] = handler
	
	def __enter__ (self) -> Signal:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.restore ()
		return None

	def __setitem__ (self, sig: Union[Signals, str], handler: Union[Handler, str]) -> None:
		signr = self.find_signal (sig)
		old_handler = signal.signal (
			signr,
			self.find_handler (handler)
		)
		if signr not in self.saved:
			self.saved[signr] = old_handler

	def __getitem__ (self, sig: Union[Signals, str]) -> Handler:
		return signal.getsignal (self.find_signal (sig))
	
	def __delitem__ (self, sig: Union[Signals, str]) -> None:
		signr = self.find_signal (sig)
		try:
			old_handler = self.saved.pop (signr)
			signal.signal (signr, old_handler)
		except KeyError:
			raise KeyError (sig)
		
	def restore (self) -> None:
		Stream.of (self.saved.items ()).each (lambda kv: signal.signal (kv[0], kv[1]))
		self.clear ()
	
	def clear (self) -> None:
		self.saved.clear ()

	def find_signal (self, sig: Union[Signals, str]) -> Signals:
		if type (sig) is not str:
			return cast (Signals, sig)
		signame = cast (str, sig)
		try:
			return Signal.known_signals[signame.upper ()]
		except KeyError:
			try:
				return Signal.known_signals['SIG{name}'.format (name = signame.upper ())]
			except KeyError:
				raise KeyError (signame)
	
	def find_handler (self, handler: Union[Handler, str]) -> Handler:
		if type (handler) is not str:
			return cast (Handler, handler)
		handlername = cast (str, handler)
		try:
			return Signal.known_handlers[handlername.upper ()]
		except KeyError:
			try:
				return Signal.known_handlers['SIG_{name}'.format (name = handlername.upper ())]
			except KeyError:
				raise KeyError (handlername)
			
class Daemonic:
	"""Base class for daemon processes

This class provides the basic functionality to allow a process to run
as a daemon (a background process which runs continuously) and low
level process control. In general this (or one of its subclasses)
should be subclassed and extended for the process to implement."""
	__slots__ = ['running', 'signals']
	try:
		devnull = platform.DEV_NULL
	except AttributeError:
		devnull = '/dev/null'
	@classmethod
	def call (cls, method: Callable[..., Any], *args: Any, **kwargs: Any) -> Any:
		"""Call a method in a subprocess, return process status"""
		def wrapper (queue: Queue[Any], method: Callable[..., Any], *args: Any, **kwargs: Any) -> None:
			try:
				rc = method (*args, **kwargs)
			except BaseException as e:
				rc = e
			queue.put (rc)
		queue: Queue[Any] = multiprocessing.Queue ()
		d = cls ()
		p = d.join (join_pid = d.spawn (wrapper, queue, method, *args, **kwargs))
		if p.error is not None:
			raise p.error
		if queue.empty ():
			return None
		rc = queue.get ()
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
		
	def setsignal (self, signr: Signals, action: Union[None, Handlers, Callable[[Signals, FrameType], None]]) -> None:
		self.signals[signr] = action
			
	def signal_handler (self, sig: Signals, stack: FrameType) -> None:
		"""Standard signal handler for graceful termination"""
		self.running = False
	
	def setup_handler (self, master: bool) -> None:
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
		fd = os.open (Daemonic.devnull, os.O_RDWR)
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
						logger.error ('Expect new fd #%d, but got #%d' % (nfd, cfd))
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
				elif type (ec) is bool:
					ec = 0 if ec else 1
				elif type (ec) is not int:
					ec = 0
			except Exception as e:
				logger.exception ('Failed to call %s: %s' % (method, e))
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
		pid: Optional[int] = None
		status: Optional[int] = None
		exitcode: Optional[int] = None
		signal: Optional[int] = None
		nochild: bool = False
		killed: bool = False
		error: Optional[Exception] = None
	def join (self, join_pid: int = -1, block: bool = True) -> Daemonic.Status:
		"""Waits for ``join_pid'' (if -1, then for any child process), ``block'' is True to wait for a child process to terminate"""
		if block:
			flags = 0
		else:
			flags = os.WNOHANG
		rc = Daemonic.Status ()
		try:
			(pid, status) = os.waitpid (join_pid, flags)
			rc.pid = pid
			rc.status = status
			if os.WIFEXITED (status):
				rc.exitcode = os.WEXITSTATUS (status)
			elif os.WIFSIGNALED (status):
				rc.signal = os.WTERMSIG (status)
		except OSError as e:
			rc.error = e
			rc.nochild = (rc.error.args[0] == errno.ECHILD)
		return rc

	def term (self, pid: int, provided_sig: Optional[Signals] = None) -> Daemonic.Status:
		"""Terminates a child process ``pid'' with signal ``sig'' (if None, SIGTERM is used)"""
		sig = provided_sig if provided_sig is not None else signal.SIGTERM
		rc = Daemonic.Status ()
		try:
			os.kill (pid, sig)
			rc.killed = True
		except OSError as e:
			rc.error = e
		return rc

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
			pass
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
		
		def __call__ (self) -> int:
			"""Entry point for Watchdog to start this job"""
			try:
				if self.output is not None and self.watchdog is not None:
					try:
						self.watchdog.redirect (Daemonic.devnull, self.output)
					except Exception as e:
						logger.error ('Failed to establish redirection: %s' % e)
				rc = self.method (*self.args)
			except self.Restart:
				return Watchdog.EC_RESTART_REQUEST
			except Exception as e:
				logger.exception ('Execution failed: %s' % e)
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
					return cast (bool, last + cast (int, self.heartbeat) > now)
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
						logger.error ('Failed to unpickle input: %s' % e)
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
		unit = Unit ()
		restart_delay = unit.parse (restart_delay, 60)
		termination_delay = unit.parse (termination_delay, 10)
		joblist = [jobs] if isinstance (jobs, self.Job) else jobs
		hb = None
		for job in joblist:
			job.watchdog = self
			if job.heartbeat is not None:
				if hb is None:
					hb = self.Heart (len (joblist))
				job.hb = hb.slot ()
			logger.info ('Added job %s' % job.name)
		done = []
		self.setup_handler (True)
		logger.info ('Startup with %d job%s' % (len (joblist), '' if len (joblist) == 1 else 's'))
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
						logger.info ('Launching job %s' % job.name)
						active += 1
						if job in nolog:
							nolog.remove (job)
					else:
						if job not in nolog:
							logger.info ('Job %s is not ready, will start in %d seconds' % (job.name, job.last + restart_delay - now))
							nolog.add (job)
				else:
					active += 1
					if not job.beating ():
						logger.warning ('Job %s (PID %d) heart beat failed, force hard kill' % (job.name, job.pid))
						self.term (job.pid, signal.SIGKILL)
						job.killed_by_heartbeat = True
			#
			if not self.running: continue
			time.sleep (1)
			if not self.running or not active: continue
			#
			rc = self.join (block = False)
			if rc.pid == 0:
				continue
			#
			if rc.error is not None:
				if rc.nochild:
					logger.error ('Even with %d active jobs we cannot wait for one' % active)
				elif rc.error.args[0] != errno.EINTR:
					logger.warning ('Waiting for %d jobs is interrupted by %s' % (active, str (rc.error)))
				continue
			#
			found = None
			for job in joblist:
				if job.pid == rc.pid:
					found = job
					break
			if found is None:
				logger.info ('Collected PID %s without matching job' % rc.pid)
				continue
			#
			restart = True
			if rc.exitcode is not None:
				if rc.exitcode in (self.EC_EXIT, self.EC_STOP) or (max_incarnations and found.incarnation >= max_incarnations):
					if rc.exitcode == self.EC_EXIT:
						logger.info ('Job %s terminated normally, no restart scheduled' % found.name)
					elif rc.exitcode == self.EC_STOP:
						logger.info ('Job %s terminates with error, but enforces no restart' % found.name)
					else:
						logger.info ('Job %s terminates with error, but reached maximum incarnation counter %d' % (found.name, found.incarnation))
					restart = False
				elif rc.exitcode == self.EC_RESTART_REQUEST:
					logger.info (f'Job {found.name} terminated for restart requested by application')
				elif rc.exitcode == self.EC_RESTART_FAILURE:
					logger.info (f'Job {found.name} terminated for restart due to execution failure')
				else:
					logger.info ('Job %s terminated with exit code %d' % (found.name, rc.exitcode))
			elif rc.signal is not None:
				if rc.signal in (signal.SIGKILL, signal.SIGTERM):
					if rc.signal == signal.SIGKILL and found.killed_by_heartbeat:
						logger.info ('Job %s had been killed due to missing heart beat, restart scheduled' % found.name)
					else:
						restart = False
						logger.info ('Job %s died due to signal %d, no restart scheduled assuming forced termination' % (found.name, rc.signal))
				else:
					logger.info ('Job %s died due to signal %d' % (found.name, rc.signal))
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
			rc = self.join (block = False)
			for job in joblist:
				if job.pid == rc.pid or job.pid is None:
					joblist.remove (job)
					done.append (job)
					if job.pid is not None:
						self.joining (job, rc)
			#
			if not joblist: continue
			#
			if n in (0, termination_delay):
				for job in joblist:
					self.term (cast (int, job.pid), signal.SIGKILL if n == 0 else signal.SIGTERM)
					logger.info ('Signaled job %s to terminate' % job.name)
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
				raise self.Job.Restart ()
			return ec == self.EC_EXIT
		self.mstart (self.Job ('default', starter, ()), *args, **kwargs)
	#
	# Methods to override
	#
	# Must implement as entry point for new process
	@abstractmethod
	def run (self, *args: Any, **kwargs: Any) -> Any:
		"""The entry point for the legacy ``start'' method"""
	#
	# called once during startup
	def startup (self, jobs: List[Watchdog.Job]) -> None:
		"""Is called after setup, but before any child process is started"""
		pass
	#
	# called once for teardonw
	def teardown (self, done: List[Watchdog.Job]) -> None:
		"""Is called after all child processs are terminated, but before the watchdog exits

``done'' is the list of jobs which are known to had terminated."""
		pass
	#
	# called once when ready to terminate
	def terminating (self, jobs: List[Watchdog.Job], done: List[Watchdog.Job]) -> None:
		"""Is called during exit before terminating the children

``jobs'' are a list of still active children and ``done'' is a list of
jobs already terminated."""
		pass
	#
	# called before every starting of a process
	def spawning (self, job: Watchdog.Job) -> None:
		"""Is called after ``job'' had been started"""
		pass
	#
	# called after every joining of a process
	def joining (self, job: Watchdog.Job, ec: Daemonic.Status) -> None:
		"""Is called after ``job'' has terminated with ``ec'' exit condition"""
		pass
	#
	# called in subprocess before invoking method
	def started (self, job: Watchdog.Job) -> None:
		"""Is called before invoking ``job'' entry point"""
		pass
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
					raise error ('Required path "%s" is no directory' % cpath)
				nmode = st.st_mode | mask
				if nmode != st.st_mode:
					os.chmod (cpath, stat.S_IMODE (nmode))
			except OSError as e:
				if e.args[0] != errno.ENOENT:
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
			logger.warning ('Check script returns %d:\nStdout: %s\nStderr: %s' % (pp.returncode, pout, perr))
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
			st = self.join (block = block)
			if st.error is not None:
				if st.nochild:
					logger.warning ('Unable to wait for any child even if %d are looking active' % len (active))
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
							how += ' with status %d' % st.exitcode
					elif st.signal is not None:
						how = 'killed by %d' % st.signal
					else:
						how = 'died with status %d' % child.status
					logger.info ('Child %s %s' % (child.method.__name__, how))
				except KeyError:
					logger.warning ('Returned not active PID %d' % st.pid)

	def __watchdog (self, child_methods: List[Callable[..., Any]], restart_on_error: bool) -> None:
		children = [Daemon.Child (method = _c) for _c in child_methods]
		active: Dict[int, Daemon.Child] = {}
		logger.info ('Watchdog starting with %d child(ren)' % len (children))
		while self.running:
			for child in children:
				if child.pid == -1:
					if child.status == 0 or restart_on_error:
						child.pid = self.spawn (child.method)
						child.status = 0
						active[child.pid] = child
						logger.info ('Child %s started' % child.method.__name__)
			if not active:
				self.running = False
			else:
				self.__wait (active)
		if active:
			logger.info ('Watching waiting for %d active child(ren)' % len (active))
			signr = None
			while active:
				for child in list (active.values ()):
					st = self.term (child.pid, signr)
					if st.error is not None and st.error.args[0] == errno.ESRCH:
						logger.warning ('Unexpected missing child process %s' % child.method.__name__)
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
		pass

class Worker (Daemon):
	"""Framework for a XML-RPC daemon with worker process

This provides a two process framework where one serves client requests
via XML-RPC )controller) while the other performs the operation as a
worker (executor). Subclass this class and implement the desired
overwritable methods. 

1.) Controller:
- controller_config: request a configuration value for a parameter, currently these are available:
	- xmlrpc-server-timeout (float, 5.0): defines the timeout for the XML-RPC server communication
- controller_setup: is called once at the beginning to inititalize the controller part of the service
	This method returns a generic context which is passed to further methods as first parameter
- controller_teardown: is called once at the end of the controller
- controller_register: is used to register all XML-RPC methods which should be callable
- controller_step: is regulary called during server operation

The controller methods can call ``enqueue'' to pass an object to the
worker part, the executor.

2.) Executor:
- executor_config: request a configuration value for a parameter, currently these are available:
	- ignore-duplicate-requests (bool, False): If identical requests are found in the queue, only one is passed for processing, if this is set to True
	- handle-multiple-requests (bool, False): If False, enqueued objects are passed one by one for processing, otherwise they are passed in chunks
- executor_setup: is called once at the beginning to inititalize the executor part of the service
	This method returns a generic context which is passed to further methods as first parameter
- executor_teardown: is called once at the end of the executor
- executor_step: is regulary called during server operation
- executor_request_preparse: preparses a received object from the controller, must return the final object
- executor_request_next: prepare the next object (or objects as a list, if "handle-multiple-requests" is True); must return the value to finally process
- executor_request_handle: processes the object (or the list of objects) in a subprocess
"""
	__slots__: List[str] = []
	nameCtrl = 'ctrl'
	nameExec = 'exec'
	
	def controller_config (self, what: str, default: Any) -> Any:
		"""returns configuration value for ``what'', using ``default'' if no custom value is available"""
		return default
	def controller_setup (self) -> Any:
		"""setup up context to use in further calls"""
		return None
	def controller_teardown (self, ctx: Any) -> None:
		"""cleanup used resources"""
		pass
	def controller_register (self, ctx: Any, serv: XMLRPC) -> None:
		"""register methods to XML-RPC server ``serv''"""
		pass
	def controller_step (self, ctx: Any) -> None:
		"""called periodically"""
		pass
		
	def __controller (self) -> None:
		logger.debug ('Controller starting')
		ctx = self.controller_setup ()
		timeout = self.controller_config ('xmlrpc-server-timeout', 5.0)
		serv = XMLRPC (self.cfg, timeout = timeout)
		self.controller_register (ctx, serv)
		while self.running:
			self.controller_step (ctx)
			while self.backlog and not self.queue.full ():
				self.queue.put (self.backlog.popleft ())
			with Ignore (select.error):
				serv.server.handle_request ()
		self.controller_teardown (ctx)
		logger.debug ('Controller terminating')

	def executor_config (self, what: str, default: Any) -> Any:
		"""returns configuration value for ``what'', using ``default'' if no custom value is available"""
		return default
	def executor_setup (self) -> Any:
		"""setup up context to use in further calls"""
		return None
	def executor_teardown (self, ctx: Any) -> None:
		"""cleanup used resources"""
		pass
	def executor_step (self, ctx: Any) -> None:
		"""called periodically"""
		pass
	def executor_request_preparse (self, ctx: Any, rq: Any) -> Any:
		"""preparses request ``rq'' after fetching from queue"""
		return rq
	def executor_request_next (self, ctx: Any, rq: Any) -> Any:
		"""prepare request(s) ``rq'' before being processed"""
		return rq
	def executor_request_handle (self, ctx: Any, rq: Any) -> None:
		"""process request(s) ``rq''"""
		pass
		
	def __executor (self) -> None:
		logger.debug ('Executor starting')
		ctx = self.executor_setup ()
		ignoreDups = self.executor_config ('ignore-duplicate-requests', False)
		multipleRequests = self.executor_config ('handle-multiple-requests', False)
		pending: List[Any] = []
		child = None
		while self.running:
			self.executor_step (ctx)
			with Ignore (IOError, queue.Empty, error):
				rq = self.queue.get (timeout = 1.0)
				logger.debug ('Received %r while %d requests pending' % (rq, len (pending)))
				rq = self.executor_request_preparse (ctx, rq)
				if not ignoreDups or rq not in pending:
					pending.append (rq)
				else:
					logger.debug ('Ignore request %r as already one is pending' % (rq, ))
			#
			while len (pending) > 0:
				logger.debug ('%d pending requests' % len (pending))
				if child is not None:
					child.join (0)
					if not child.is_alive ():
						child.close ()
						child = None
						logger.debug ('Previous instance had finished')
				if child is None:
					if multipleRequests:
						rq = pending
						pending = []
					else:
						rq = pending.pop (0)
					try:
						rq = self.executor_request_next (ctx, rq)
						child = multiprocessing.Process (target = self.executor_request_handle, args = (ctx, rq))
						child.start ()
						logger.debug ('Started instance process')
					except error as e:
						logger.error ('Failed to start child: %s' % e)
				else:
					logger.debug ('Busy')
					break
		if child is not None:
			child.join ()
			child.close ()
		self.executor_teardown (ctx)
		logger.debug ('Executor terminating')
	
	def enqueue (self, obj: Any, oob: bool = False) -> None:
		"""Put ``obj'' from controller to executor, in front of queue, when ``oob'' is set"""
		if self.queue.full () or self.backlog:
			if oob:
				self.backlog.appendleft (obj)
			else:
				self.backlog.append (obj)
		if not self.queue.full ():
			if self.backlog:
				while self.backlog and not self.queue.full ():
					self.queue.put (self.backlog.popleft ())
			else:
				self.queue.put (obj)

	def run (self, *args: Any, **kwargs: Any) -> Any:
		"""Entry point for starting process"""
		path = os.path.join (base, 'scripts', '%s.cfg' % (self.name if self.name is not None else host, ))
		self.cfg = Config ()
		if os.path.isfile (path):
			self.cfg.read (path)
		self.queue: Queue[Any] = multiprocessing.Queue ()
		self.backlog: Deque[Any] = deque ()
		self.ctrl = Parallel ()
		self.ctrl.fork (self.__controller, name = self.nameCtrl, logname = f'{log.name}-{self.nameCtrl}')
		self.ctrl.fork (self.__executor, name = self.nameExec, logname = f'{log.name}-{self.nameExec}')
		while self.running:
			time.sleep (1)
		self.ctrl.wait (timeout = 2.0)
		self.ctrl.term ()
		self.ctrl.done ()
