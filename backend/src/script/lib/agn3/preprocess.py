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
import	sys, re
from	contextlib import suppress
from	dataclasses import dataclass
from	io import StringIO
from	typing import Any, Callable, Iterable, NoReturn
from	typing import TextIO
#
class Preprocess:
	__slots__ = ['prefix', 'control_pattern', 'tokens', 'definitions', 'ns', 'imported', 'nodes', 'filename', 'fdout', 'lineno', 'line', 'abort']
	@dataclass
	class Node:
		lineno: int
		line: str
		valid: bool = False
		final: bool = False

	def __init__ (self, prefix: None | str = None, definitions: None | dict[str, Any] = None) -> None:
		self.prefix = prefix if prefix is not None else '#[ \t]*'
		self.control_pattern = re.compile (f'^{self.prefix}([a-z]+)([ \t]+.*|\\(.*\\))?$')
		self.tokens: dict[str, None | Callable[[None | str], None]] = {}
		self.ns: dict[str, Any] = {}
		self.definitions: dict[str, Any] = {} if definitions is None else definitions.copy ()
		self.nodes: list[Preprocess.Node] = []
		self.imported: set[str] = set ()
		self.filename: str
		self.fdout: TextIO
		self.lineno: int
		self.line: str
		self.abort: None | str = None
		
	def inherit (self, other: Preprocess) -> None:
		self.ns = other.ns
		self.definitions = other.definitions
		self.imported = other.imported
		
	def __setitem__ (self, option: Any, value: Any) -> None:
		self.ns[option] = value
		
	def __getitem__ (self, option: Any) -> Any:
		return self.ns[option]
		
	def __delitem__ (self, option: Any) -> None:
		del self.ns[option]
		
	def get (self, option: Any, default: Any = None) -> Any:
		return self.ns.get (option, default)
	
	def update (self, updates: dict[str, Any] | Iterable[tuple[str, Any]]) -> None:
		self.definitions.update (updates)

	def define (self, option: str, value: Any) -> None:
		self.definitions[option] = value
		
	def undefine (self, option: str) -> None:
		self.definitions.pop (option, None)

	def process (self, *, in_stream: None | str | TextIO = None, out_stream: None | str | TextIO = None, filename: None | str = None) -> None:
		if filename is not None:
			self.filename = filename
		elif isinstance (in_stream, str):
			self.filename = in_stream
		elif in_stream is None or in_stream is sys.stdin:
			self.filename = '<stdin>'
		else:
			self.filename = '<unknown>'
		try:
			fdin: TextIO
			if in_stream is None:
				if filename is not None:
					fdin = open (filename)
				else:
					fdin = sys.stdin
			elif isinstance (in_stream, str):
				fdin = open (in_stream)
			else:
				fdin = in_stream
			if out_stream is None:
				self.fdout = sys.stdout
			elif isinstance (out_stream, str):
				self.fdout = open (out_stream, 'w')
			else:
				self.fdout = out_stream
			for (self.lineno, line) in enumerate (fdin, start = 1):
				self.line = line.rstrip ()
				mtch = self.control_pattern.match (self.line)
				if mtch is not None:
					(token, parameter) = mtch.groups ()
					try:
						method = self.tokens[token]
					except KeyError:
						method = None
						with suppress (AttributeError):
							method = getattr (self, f'do_{token}')
							if not callable (method):
								method = None
						self.tokens[token] = method
					if method is not None:
						method (None if parameter is None else (parameter[1:-1] if parameter.startswith ('(') and parameter.endswith (')') else parameter.lstrip ()))
						if self.abort is not None:
							self.nodes.clear ()
							self.line = f'<{self.abort}>'
							break
						continue
				if self.enabled ():
					self.output (line)
			else:
				self.line = '<EOF>'
		finally:
			with suppress (NameError):
				if fdin is not sys.stdin and fdin is not in_stream:
					fdin.close ()
			if self.fdout is not sys.stdout and self.fdout is not out_stream:
				self.fdout.close ()
			else:
				self.fdout.flush ()
		if self.nodes:
			self.failure ('\n'.join ([f'non closed {self.filename}:{_n.lineno}:{_n.line}' for _n in self.nodes]))
	
	def filter (self, source: str, *, filename: None | str = None) -> str:
		with StringIO (source) as inbuf, StringIO () as outbuf:
			self.process (in_stream = inbuf, out_stream = outbuf, filename = filename)
			return outbuf.getvalue ()

	def enabled (self) -> bool:
		return not self.nodes or self.nodes[-1].valid

	def do_include (self, parameter: None | str) -> None:
		self._include (parameter, False)

	def do_import (self, parameter: None | str) -> None:
		self._include (parameter, True)
		
	def _include (self, parameter: None | str, check_seen: bool) -> None:
		if self.enabled ():
			parameter = self._check_parameter (parameter)
			if not check_seen or parameter not in self.imported:
				if check_seen:
					self.imported.add (parameter)
				try:
					with open (parameter) as fd, StringIO () as output:
						pp = self.__class__ (prefix = self.prefix)
						pp.inherit (self)
						pp.process (in_stream = fd, out_stream = output, filename = parameter)
						self.fdout.write (output.getvalue ())
				except OSError as e:
					self.failure (f'failed to read {parameter}: {e}')

	def do_pragma (self, parameter: None | str) -> None:
		if self.enabled ():
			self.pragma (self._check_parameter (parameter))

	def do_define (self, parameter: None | str) -> None:
		if self.enabled ():
			parameter = self._check_parameter (parameter)
			value: int | str
			try:
				(option, value) = parameter.split (None, 1)
			except ValueError:
				(option, value) = (parameter, 1)
			self.define (option, value)

	def do_undef (self, parameter: None | str) -> None:
		if self.enabled ():
			self.undefine (self._check_parameter (parameter))

	def do_if (self, parameter: None | str) -> None:
		self._if (parameter, False)
		
	def do_elif (self, parameter: None | str) -> None:
		self._if (parameter, True)
		
	def _if (self, parameter: None | str, update: bool) -> None:
		try:
			if self._node_valid (True, update):
				ns = self.ns.copy ()
				ns['defined'] = lambda s: s in self.definitions
				valid = bool (eval (self._check_parameter (parameter), ns, self.definitions))
			else:
				valid = False
		except Exception as e:
			self.failure (f'failed evaluation of expression: {e}')
		else:
			if update:
				self._node_update (final = False, valid = valid)
			else:
				self._node_add (valid)

	def do_ifdef (self, parameter: None | str) -> None:
		self._ifdef (parameter, False, False)

	def do_elifdef (self, parameter: None | str) -> None:
		self._ifdef (parameter, False, True)
		
	def do_ifndef (self, parameter: None | str) -> None:
		self._ifdef (parameter, True, False)

	def do_elifndef (self, parameter: None | str) -> None:
		self._ifdef (parameter, True, True)
		
	def_parser = re.compile ('[a-z_][a-z0-9_]*|\\|\\||&&|.', re.IGNORECASE)
	def _ifdef (self, parameter: None | str, invert: bool, update: bool) -> None:
		parsed = ' '.join ([
			_m if _m in {' ', '\t', '(', ')'} else {
				'!':	'not',
				'||':	'or',
				'&&':	'and'
			}.get (_m, f'{_m!r} in definitions')
			for _m in self.def_parser.findall (self._check_parameter (parameter))
		])
		try:
			valid = invert ^ bool (eval (parsed, {
				'definitions': self.definitions
			}))
		except Exception as e:
			self.failure (f'invalid definition expression "{parsed}": {e}')
		else:
			if update:
				self._node_update (final = False, valid = valid)
			else:
				self._node_add (valid)

	def do_else (self, parameter: None | str) -> None:
		self._check_no_parameter (parameter)
		self._node_update (final = True)
		
	def do_endif (self, parameter: None | str) -> None:
		self._check_no_parameter (parameter)
		self._node_remove ()

	def do_abort (self, parameter: None | str) -> None:
		self.abort = f'ABORT:{parameter}' if parameter else 'ABORT'

	def _node_valid (self, valid: bool, update: bool) -> bool:
		offset = 2 if update else 1
		return valid and ((len (self.nodes) < offset) or self.nodes[-offset].valid)
		
	def _node_add (self, valid: bool) -> None:
		self.nodes.append (Preprocess.Node (self.lineno, self.line, self._node_valid (valid, False), False))
		
	def _node_update (self, final: bool, valid: bool = False) -> None:
		node = self._check_nodes ()
		if node.final:
			self.failure ('no more if after else allowed')
		node.valid = self._node_valid (not node.valid if final else valid, True)
		node.lineno = self.lineno
		node.line = self.line
		node.final = final
		
	def _node_remove (self) -> None:
		self._check_nodes ()
		self.nodes.pop ()

	def _check_parameter (self, parameter: None | str) -> str:
		if parameter is None:
			self.failure ('missing parameter')
		return parameter
		
	def _check_no_parameter (self, parameter: None | str) -> None:
		if parameter is not None:
			self.failure ('parameter not allowed')
		return parameter

	def _check_nodes (self) -> Preprocess.Node:
		if not self.nodes:
			self.failure ('no open node found')
		return self.nodes[-1]

	def failure (self, message: str) -> NoReturn:
		raise SyntaxError (f'{self.filename}:{self.lineno}:{self.line}\n{message}')

	def output (self, line: str) -> None:
		self.fdout.write (line)
		
	def pragma (self, pragma: str) -> None:
		pass
