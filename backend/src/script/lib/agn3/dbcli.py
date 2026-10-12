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
import	os
from	contextlib import suppress
from	subprocess import Popen, PIPE, STDOUT
from	tempfile import mkstemp
from	types import TracebackType
from	typing import Self
from	typing import NamedTuple, TextIO, Type
from	.definitions import base, program
#
class DBCLI:
	__slots__ = ['command', 'env', 'prolog', 'epilog']
	class Result (NamedTuple):
		returncode: int
		output: str
	
	def __init__ (self,
		command: list[str],
		*,
		env: None | dict[str, str] = None,
		prolog: None | str = None,
		epilog: None | str = None
	) -> None:
		self.command = command
		self.env = env
		self.prolog = prolog
		self.epilog = epilog
		
	def __enter__ (self) -> Self:
		return self
	
	def __exit__ (self, exc_type: None | Type[BaseException], exc_value: None | BaseException, traceback: None | TracebackType) -> None | bool:
		return None

	_temp_prefix = f'{program}.'
	_temp_directory = os.path.join (base, 'var', 'tmp')
	def invoke (self,
		input: None | str = None,
		input_file: None | str | TextIO = None,
		*,
		command: None | list[str] = None,
		env: None | dict[str, str] = None,
		prolog: None | str = None,
		epilog: None | str = None
	) -> DBCLI.Result:
		environ = os.environ.copy ()
		if env is None:
			env = self.env
		if env:
			environ.update (env)
		if prolog is None:
			prolog = self.prolog
		if epilog is None:
			epilog = self.epilog
		fd: None | TextIO = None
		try:
			if input_file is not None:
				if isinstance (input_file, str):
					fd = open (input_file)
				else:
					fd = input_file
				if prolog or epilog or input:
					(fdtemp, filename) = mkstemp (prefix = self._temp_prefix, dir = self._temp_directory, text = True)
					with suppress (OSError):
						os.unlink (filename)
					for chunk in [prolog, input, fd.read (), epilog]:
						if chunk:
							os.write (fdtemp, chunk.encode ('UTF-8'))
					os.lseek (fdtemp, 0, os.SEEK_SET)
					if fd is not input_file:
						with suppress (IOError):
							fd.close ()
					fd = open (fdtemp)
			elif prolog or epilog:
				input = ''.join ([_p for _p in (prolog, input, epilog) if _p is not None])
			pp = Popen (
				command if command is not None else self.command,
				stdin = PIPE if fd is None else fd,
				stdout = PIPE,
				stderr = STDOUT,
				env = environ,
				text = True,
				errors = 'backslashreplace'
			)
			(out, _) = pp.communicate (input if fd is None else None)
			return DBCLI.Result (
				returncode = pp.returncode,
				output = out
			)
		finally:
			if fd is not None and fd is not input_file:
				with suppress (IOError):
					fd.close ()

	def process_file (self, filename: str) -> DBCLI.Result:
		return self.invoke (input_file = filename)
