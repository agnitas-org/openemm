####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
from	__future__ import annotations
import	logging
import	re, subprocess, multiprocessing
from	multiprocessing.queues import Queue
from	types import TracebackType
from	typing import Any, Callable, Optional, Union
from	typing import Dict, List, NamedTuple, Set, Tuple, Type
from	.definitions import program, user
from	.exceptions import error
from	.ignore import Ignore
from	.log import log
from	.template import Template
#
__all__ = ['Processtable', 'Processtitle', 'Parallel']
#
logger = logging.getLogger (__name__)
#
class Processstats (NamedTuple):
	pid: int
	ppid: int
	user: str
	group: str
	etime: int
	time: int
	tty: str
	size: int
	comm: str
	cmd: str
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
		self.root: Optional[Processentry] = None
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
			raise error (f'Unable to read process table: {err!r}')
	
	def find (self, val: Union[int, str]) -> Processentry:
		"""Returns a process with PID ``val'' (if numeric) or tries to read the PID from the file ``val'' (if an absolute path)"""
		pid: Optional[int] = None
		if isinstance (val, int):
			pid = val
		else:
			if len (val) > 0 and val[0] == '/':
				with Ignore (), open (val, 'r') as fd:
					line = fd.readline ().strip ()
					if line != '':
						pid = int (line)
		if pid is None:
			with Ignore (ValueError):
				pid = int (val)
		if pid is None:
			raise error (f'Given paramter "{val!r}" cannot be mapped to a pid')
		for p in self.table:
			if p.stats.pid == pid:
				return p
		raise error (f'No process with pid {pid} (extracted from {val!r}) found')
	
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
			
			self._set: Callable[[str], None] = setproctitle.setproctitle
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
	
	class ProcesstitleContext:
		__slots__ = ['ref', 'title', 'log_id']
		def __init__ (self, ref: Processtitle, title: str) -> None:
			self.ref = ref
			self.title = title
			self.log_id = log (title)
			
		def __enter__ (self) -> Processtitle.ProcesstitleContext:
			self.log_id.__enter__ ()
			self.ref.push (self.title)
			return self

		def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			self.ref.pop ()
			return self.log_id.__exit__ (exc_type, exc_value, traceback)
	
	def title (self, title: str) -> Processtitle.ProcesstitleContext:
		return Processtitle.ProcesstitleContext (self, title)

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
			self.value: Any = None
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
