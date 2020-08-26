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
import	logging
import	os, signal, time, errno, re, subprocess
import	multiprocessing
from	abc import abstractmethod
from	collections import namedtuple
from	signal import Signals
from	queue import Queue
from	types import FrameType, TracebackType
from	typing import Any, Callable, Optional, Union
from	typing import Dict, Iterator, List, Set, Tuple, Type
from	typing import cast
from	.definitions import program, user
from	.exceptions import error
from	.ignore import Ignore
from	.log import log
from	.template import Template
#
__all__ = ['Family', 'Processtable', 'Processtitle', 'Parallel']
#
logger = logging.getLogger (__name__)
#
class Family:
	"""Control multiple child processes

this class provides a framework to start and control multiple child
processes, optional limit the started of child processes to a fixed
number.

To use this class one must subclass Family.Child and add an
implementation of the method ``start'' as the entry point for the
child process. The child process itself can check if it should
continue processing by calling its method ``active()''.

For each child process this class must be instanced and pased to the
class using Family.add(). After adding in general Family.start() is
called. This forks new processes for each child.

Using Family.wait() will wait (optional with a timeout) for
termination of the child processes. If the number of subprocesses is
limited, then this method will also start remaining children, when one
active child exits."""
	__slots__ = ['limit', 'children', 'running']
	class Child:
		"""Meta class for child process"""
		__slots__ = ['_name', '_pid', '_status', '_state', '_running']
		NEW = 0
		SCHEDULE = 1
		RUNNING = 2
		DEAD = 3
		_incarnation = 0
		def __init__ (self, name: Optional[str] = None) -> None:
			"""``name'' is an optional identifier for this child"""
			if name is not None:
				self._name = name
			else:
				self.__class__._incarnation += 1
				self._name = 'Child-%d' % self.__class__._incarnation
			self._pid = -1
			self._status = -1
			self._state = self.NEW
			self._running = True

		def __catch (self, sig: Signals, stack: FrameType) -> None:
			self._running = False

		def setupHandler (self) -> None:
			"""setup the signal handler for process"""
			signal.signal (signal.SIGTERM, self.__catch)
			signal.signal (signal.SIGINT, signal.SIG_IGN)
			signal.signal (signal.SIGHUP, signal.SIG_IGN)
			signal.signal (signal.SIGPIPE, signal.SIG_IGN)
			signal.signal (signal.SIGCHLD, signal.SIG_DFL)

		def exited (self) -> int:
			"""returns the exit status, if child exited, else -1"""
			if os.WIFEXITED (self._status):
				return os.WEXITSTATUS (self._status)
			return -1

		def terminated (self) -> int:
			"""returns the termination signal, if child was killed, else -1"""
			if os.WIFSIGNALED (self._status):
				return os.WTERMSIG (self._status)
			return -1

		def reset (self) -> None:
			"""reset the state of the child"""
			self._pid = -1
			self._status = -1
			self._state = self.NEW
			self._running = True

		def active (self) -> bool:
			"""checks if the child is started (for the parent) or if it is still active (for the child itself)"""
			if self._pid > 0:
				return True
			elif self._pid == 0:
				return self._running
			return False

		@abstractmethod
		def start (self) -> Any:
			"""Entry point for the subclass for the child process"""

	def __init__ (self, limit: Optional[int] = None) -> None:
		"""if ``limit'' larger than 0, this limits the number
of running child processes at the same time."""
		signal.signal (signal.SIGCHLD, self.__catch)
		self.limit = limit
		self.children: List[Family.Child] = []
		self.running = 0

	def __enter__ (self) -> Family:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.start ()
		while self.wait () > 0:
			self.start ()
		return None

	def __catch (self, sig: int, stack: FrameType) -> None:
		pass

	def __iter__ (self) -> Iterator[Family.Child]:
		return iter (self.children)

	def reset (self) -> None:
		"""Resets the monitored children, running children stay active!"""
		if self.children:
			self.debug ('Reset: %s' % ', '.join ([_c._name for _c in self.children]))
		else:
			self.debug ('Reset with no children')
		self.children = []

	def add (self, child: Family.Child) -> None:
		"""Add a new child or reschedule a terminated child"""
		if child in self.children:
			if not child.active ():
				self.debug ('Reactived %s' % child._name)
				child.reset ()
			else:
				self.debug ('Add already active %s' % child._name)
		else:
			self.debug ('Add %s' % child._name)
			child.reset ()
			self.children.append (child)

	def __spawn (self, child: Family.Child) -> None:
		child._state = child.RUNNING
		self.debug ('Starting %s' % child._name)
		child._status = 0
		child._pid = os.fork ()
		if child._pid == 0:
			child.setupHandler ()
			try:
				ec = child.start ()
				self.debug ('%s: Exiting with %r' % (child._name, ec))
				if type (ec) is bool:
					ec = 0 if ec else 1
				elif type (ec) is float:
					ec = int (ec)
				elif type (ec) is not int:
					ec = 0
			except Exception as e:
				self.debug ('%s: Exiting due to exception: %s' % (child._name, str (e)))
				logger.exception (child._name)
				ec = 127
			os._exit (ec)
		self.running += 1

	def __join (self, child: Family.Child, status: int = -1) -> None:
		child._pid = -1
		child._status = status
		child._state = child.DEAD
		self.running -= 1

	def start (self) -> None:
		"""Start ready to run children until reaching the limit, if set"""
		for child in self.children:
			if child._state == child.NEW:
				child._state = child.SCHEDULE
				self.debug ('%s: scheduled' % child._name)
			if child._state == child.SCHEDULE and (self.limit is None or self.running < self.limit):
				self.__spawn (child)

	def __kill (self, sig: Signals) -> None:
		for child in self.children:
			if child._state == child.RUNNING:
				try:
					os.kill (child._pid, sig)
					self.debug ('%s: signalled with %d' % (child._name, sig))
				except OSError as e:
					self.debug ('%s: failed signalling with %d: %s' % (child._name, sig, str (e)))

	def stop (self) -> None:
		"""Send SIGTERM to all active children"""
		self.__kill (signal.SIGTERM)

	def kill (self) -> None:
		"""Send SIGKILL to all active children"""
		self.__kill (signal.SIGKILL)

	def wait (self, timeout: Optional[int] = None) -> int:
		"""Wait for termination of children, returning the
number of remaining active children. ``timeout'' is a timeout in
seconds to wait for children to terminate."""
		active = [_c for _c in self.children if _c._state in (_c.SCHEDULE, _c.RUNNING)]
		schedule = [_c for _c in self.children if _c._state == _c.SCHEDULE]
		if timeout is not None and timeout > 0:
			start = time.time ()
		while active:
			while schedule and (self.limit is None or self.running < self.limit):
				self.__spawn (schedule.pop (0))
			if timeout is not None and timeout >= 0:
				flag = os.WNOHANG
			else:
				flag = 0
			try:
				(pid, status) = os.waitpid (-1, flag)
				if pid > 0:
					for child in active:
						if child._pid == pid:
							self.__join (child, status)
							active.remove (child)
							self.debug ('%s: exited with %d' % (child._name, status))
							break
			except OSError as e:
				self.debug ('Wait aborted with %s' % str (e))
				if e.args[0] == errno.ECHILD:
					for child in active:
						if child._state == child.RUNNING:
							self.__join (child, 126)
							active.remove (child)
			if timeout is not None:
				if timeout == 0 or (timeout > 0 and active and start + timeout < time.time ()):
					break
				time.sleep (1)
		return len (active)

	def active (self) -> int:
		"""return the number of active children"""
		return self.wait (0)

	def cancel (self) -> None:
		"""prevend scheduled children from being started"""
		for child in self.children:
			if child._state == child.SCHEDULE:
				self.__join (child, 125)

	def abort (self) -> None:
		"""cancel not started children and stop running children"""
		self.cancel ()
		self.stop ()
		if self.wait (2):
			self.kill ()
			self.wait (0)

	def clean (self) -> List[Family.Child]:
		"""clean monitored list from inactive children"""
		cleaned: List[Family.Child] = []
		for child in [_c for _c in self.children if _c._pid == -1]:
			cleaned.append (child)
			self.children.remove (child)
			self.debug ('Removed %s' % child._name)
		return cleaned

	def debug (self, m: str) -> None:
		"""Can be overwritten to log the activty of Family"""
		pass

Processstats = namedtuple ('Processstats', ['pid', 'ppid', 'user', 'group', 'etime', 'time', 'tty', 'size', 'comm', 'cmd'])
class Processentry:
	"""Information about one process

when retrieving the process table using class Processtable this
represents a single process"""
	__slots__ = ['stats', 'parent', 'sibling', 'child']
	def __init__ (self, stats: Processstats) -> None:
		self.stats = stats
		self.parent: Optional[Processentry] = None
		self.sibling: Optional[Processentry] = None
		self.child: Optional[Processentry] = None

	def relate (self, other: Processentry) -> int:
		"""relate (self, other)
if other is parent (or grandparent), this returns a number
greater than 0, counting the generations, if other is a
child (or even younger), than a number less than 0 is
returned. If the two processes are not related to each other
(they are not related, even if they are siblings), 0 is returned."""
		cur: Optional[Processentry]
		for (cur, target, modifier) in ((self, other, 1), (other, self, -1)):
			n = 0
			while cur and cur != target:
				cur = cur.parent
				n += modifier
			if cur:
				return n
		return 0
	
class Processtable:
	"""Reads processtable for further processing

This class reads the current running processes and stores them for
further retrivial. This can be used to read the process table once and
perform serveral queries on the result."""
	__slots__ = ['table', 'root']
	def __timeparse (self, s: str) -> int:
		part = s.split ('-')
		if len (part) == 2:
			sec = int (part[0]) * 60 * 60 * 24
			sstr = part[1]
		else:
			sec = 0
			sstr = part[0]
		part = sstr.split (':')
		nsec = 0
		for p in part:
			nsec *= 60
			nsec += int (p)
		return sec + nsec

	def __init__ (self) -> None:
		self.table: List[Processentry] = []
		environ = {'COLUMNS': '4096'}
		sub = subprocess.Popen (['ps', '-e', '-o', 'pid,ppid,user,group,etime,time,tty,vsz,comm,args'], stdout = subprocess.PIPE, env = environ, text = True, errors = 'backslashreplace')
		(out, err) = sub.communicate (None)
		if sub.returncode == 0:
			for line in [_o.strip () for _o in out.split ('\n')]:
				elem = line.split ()
				if len (elem) > 7:
					with Ignore ():
						self.table.append (Processentry (Processstats (
							int (elem[0]),
							int (elem[1]),
							elem[2],
							elem[3],
							self.__timeparse (elem[4]),
							self.__timeparse (elem[5]),
							elem[6],
							int (elem[7]) * 1024,
							elem[8],
							' '.join (elem[9:])
						)))
			self.table.sort (key = lambda a: a.stats.pid)
			self.root = None
			for p in self.table:
				if p.stats.ppid == 0:
					if p.stats.pid == 1:
						self.root = p
				else:
					for pp in self.table:
						if p.stats.ppid == pp.stats.pid:
							p.parent = pp
							if pp.child:
								sib = pp.child
								while sib.sibling:
									sib = sib.sibling
								sib.sibling = p
							else:
								pp.child = p
		else:
			raise error ('Unable to read process table: %r' % err)
	
	def find (self, val: Union[int, str]) -> Processentry:
		"""Returns a process with PID ``val'' (if numeric) or tries to read the PID from the file ``val'' (if an absolute path)"""
		pid: Optional[int] = None
		if type (val) is int:
			pid = cast (int, val)
		elif type (val) is str:
			sval = cast (str, val)
			if len (sval) > 0 and sval[0] == '/':
				with Ignore (), open (sval, 'r') as fd:
					line = fd.readline ().strip ()
					if line != '':
						pid = int (line)
		if pid == None:
			with Ignore (ValueError):
				pid = int (val)
		if pid == None:
			raise error ('Given paramter "%r" cannot be mapped to a pid' % (val, ))
		match = None
		for p in self.table:
			if p.stats.pid == pid:
				match = p
				break
		if not match:
			raise error ('No process with pid %r (extracted from %r) found' % (pid, val))
		return match
	
	def select (self, user: Optional[str] = None, group: Optional[str] = None, comm: Optional[str] = None, rcmd: Optional[str] = None, ropt: int = 0) -> List[Processentry]:
		"""find processes according to the passed parameter

these keyword parameter are used. If they are None, then these are not
used to filter the process table, i.e. with no keyword parameter this
method returns the whole process table. These keyword parameter are
supported:
	- user: name of user the process is owned by
	- group: name of group the process is owned by
	- comm: the command of the process
	- rcmd: a regex to check against the commands name
	- ropt: flags for regex, if ``rcmd'' is used
"""
		regcmd = re.compile (rcmd, ropt) if rcmd else None
		rc: List[Processentry] = []
		for t in self.table:
			if user and t.stats.user != user:
				continue
			if group and t.stats.group != group:
				continue
			if comm and t.stats.comm != comm:
				continue
			if regcmd and not regcmd.search (t.stats.cmd):
				continue
			rc.append (t)
		return rc
	
	def tree (self, indent: int = 2) -> str:
		"""Builds a visualisation tree of the processtables, indents child processes by ``indent'' spaces"""
		rc = ''
		up: List[Optional[Processentry]] = []
		cur = self.root
		while cur:
			lu = len (up) * indent
			rc += '%*.*s%s\n' % (lu, lu, '', cur.stats.cmd)
			if cur.child:
				up.append (cur.sibling)
				cur = cur.child
			else:
				cur = cur.sibling
			if not cur:
				while len (up) > 0:
					cur = up.pop ()
					if cur:
						break
		return rc

class Processtitle:
	"""Modify process name in process table"""
	__slots__ = ['_set', '_get', '_original', '_current', '_template', '_ns', '_stack']
	default_template = '$original [$title]'
	def __init__ (self, use_template: Optional[str] = None) -> None:
		try:
			import	setproctitle
			
			self._set: Callable[[Optional[str]], None] = setproctitle.setproctitle
			self._get: Callable[[], str] = setproctitle.getproctitle
		except ImportError:
			self._set = lambda t: None
			self._get = lambda: ''
		self._original = self._get ()
		self._current = self._original
		self._template = Template (use_template if use_template is not None else self.default_template)
		self._ns = {
			'original': self._original,
			'program': program,
			'user': user
		}
		self._stack: List[str] = []

	def __del__ (self) -> None:
		self ()
		
	def __call__ (self, title: Optional[str] = None) -> None:
		if title is None:
			new = self._original
		else:
			self._ns['title'] = title
			new = self._template.fill (self._ns)
		if new != self._current:
			self._current = new
			self._set (self._current)
	
	def __getitem__ (self, key: str) -> str:
		return self._ns[key]
	def __setitem__ (self, key: str, value: str) -> None:
		self._ns[key] = value

	def __enter__ (self) -> Processtitle:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self ()
		return None

	def _get_template (self) -> str:
		return self._template.content
	def _set_template (self, value: Optional[str]) -> None:
		self._template = Template (value if value is not None else self.default_template)
	def _del_template (self) -> None:
		self._set_template (None)
	template = property (_get_template, _set_template, _del_template)

	def push (self, title: str) -> None:
		self._stack.append (self._current)
		self (title)
	
	def pop (self) -> None:
		if self._stack:
			new = self._stack.pop (0)
			if new != self._current:
				self._current = new
				self._set (self._current)

class Parallel:
	"""Simple parallel framework using multiprocessing"""
	__slots__:  List[str] = ['active', 'processtitle', 'worker_name']
	class Process (multiprocessing.Process):
		__slots__ = ['method', 'args', 'kwargs', 'logname', 'resultq', 'value', 'processtitle']
		def __init__ (self,
			method: Callable[..., Any],
			args: Optional[Tuple[Any, ...]],
			kwargs: Optional[Dict[str, Any]],
			name: Optional[str],
			logname: Optional[str],
			processtitle: Processtitle,
			worker_name: str
		) -> None:
			super ().__init__ (name = name)
			self.method = method
			self.args: Tuple[Any, ...] = args if args is not None else tuple ()
			self.kwargs: Dict[str, Any] = kwargs if kwargs is not None else dict ()
			self.logname = logname
			self.resultq: Queue[Any] = multiprocessing.Queue ()
			self.value = None
			self.processtitle = processtitle
			self.worker_name = worker_name

		def run (self) -> None:
			if self.logname is not None:
				log.name = self.logname
			if self.name is not None:
				self.processtitle (f'{self.worker_name} {self.name}')
			else:
				self.processtitle (f'{self.worker_name}')
			self.resultq.put (self.method (*self.args, **self.kwargs))
			self.processtitle ()

		def result (self) -> Any:
			if self.value is None:
				if not self.resultq.empty ():
					self.value = self.resultq.get ()
			return self.value

	def __init__ (self, master_name: Optional[str] = None, worker_name: Optional[str] = None) -> None:
		self.active: Set[Parallel.Process] = set ()
		self.processtitle = Processtitle ()
		self.processtitle (master_name if master_name is not None else 'master')
		self.worker_name = worker_name if worker_name is not None else 'worker'
	
	def done (self) -> None:
		self.processtitle ()

	def fork (self,
		method: Callable[..., Any],
		args: Optional[Tuple[Any, ...]] = None,
		kwargs: Optional[Dict[str, Any]] = None,
		name: Optional[str] = None,
		logname: Optional[str] = None
	) -> Parallel.Process:
		"""Setup and start a subprocess to call a method"""
		p = Parallel.Process (
			method,
			args,
			kwargs,
			name,
			logname,
			self.processtitle,
			self.worker_name
		)
		self.active.add (p)
		p.start ()
		return p

	def living (self) -> Tuple[int, int]:
		"""Return number of active subprocesses"""
		return (len ([_p for _p in self.active if _p.is_alive ()]), len (self.active))

	def wait (self, name: Optional[str] = None, timeout: Union[None, int, float] = None, count: Optional[int] = None) -> Dict[str, Tuple[Optional[int], Any]]:
		"""Wait for one or all (if name is None) active processes"""
		done = set ()
		rc: Dict[str, Tuple[Optional[int], Any]] = {}
		for p in self.active:
			if name is None or p.name == name:
				p.join (timeout)
				if timeout is None or not p.is_alive ():
					rc[p.name] = (p.exitcode, p.result ())
					done.add (p)
					if count is not None:
						count -= 1
						if count == 0:
							break
		self.active = self.active.difference (done)
		return rc

	def ready (self) -> Optional[Tuple[Optional[int], Any]]:
		"""Check if at least one process has finished and return its value"""
		for p in self.active:
			if not p.is_alive ():
				rc = self.wait (name = p.name, count = 1)
				if rc is not None:
					return list (rc.values ())[0]
		return None

	def term (self, name: Optional[str] = None) -> Dict[str, Tuple[Optional[int], Any]]:
		"""Terminate one or all (if name is None) active processes"""
		for p in self.active:
			if name is None or p.name == name:
				p.terminate ()
		return self.wait (name = name)
