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
import	multiprocessing
from	queue import Queue
from	types import TracebackType
from	typing import Any, Callable, Optional, Union
from	typing import Dict, List, Set, Tuple, Type
from	.definitions import program, user
from	.log import log
from	.template import Template
#
__all__ = ['Processtitle', 'Parallel']
#
logger = logging.getLogger (__name__)
#
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
