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
from	types import TracebackType
from	typing import Any, Optional, Callable
from	typing import Type
from	.definitions import program
#
__all__ = ['Ignore', 'ignore', 'Experimental']
#
logger = logging.getLogger (__name__)
#
class Ignore:
	"""Context to ignore selected exceptions as a replacement for

try:
	...
except ...:
	pass

If no exception are specified, all exceptions are ignored and loglevel
is set to WARNING. This is general a bad idea and should be used with
care."""
	__slots__ = ['exceptions', 'on_exception']
	def __init__ (self, *exceptions: Type[BaseException]) -> None:
		self.exceptions = exceptions
		self.on_exception: Optional[Callable[[Optional[Type[BaseException]], Optional[BaseException], Optional[TracebackType]], None]] = None
		
	def __enter__ (self) -> Ignore:
		return self
		
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		if exc_type is not None:
			try:
				if not self.exceptions or exc_type in self.exceptions or len ([_e for _e in self.exceptions if issubclass (exc_type, _e)]) > 0:
					if self.on_exception is not None and callable (self.on_exception):
						self.on_exception (exc_type, exc_value, traceback)
					return True
			except Exception:
				logger.exception ('ignore: failed during expection reporting')
		return False

def ignore (callback: Callable[[], Any], default: Any, *exceptions: Type[BaseException]) -> Any:
	with Ignore (*exceptions):
		return callback ()
	return default

class Experimental (Ignore):
	"""This is a derivated class from Ignore to provide a context
manager for experimental code. This could be used, if new code is
added to an already productive program to avoid abortion of the
program due to bugs in the new code. This should NOT be used, if
failure of the new code harms the result of the program.

Instead of passing excpetions, use a speaking name as an argument to
create an instance."""
	__slots__ = ['name']
	def __init__ (self, name: Optional[str] = None) -> None:
		super ().__init__ ()
		self.on_exception = self.__on_exception
		self.name = name if name is not None else program

	def __on_exception (self,
		exc_type: Optional[Type[BaseException]],
		exc_value: Optional[BaseException],
		traceback: Optional[TracebackType]
	) -> None:
		logger.exception (f'{self.name}: failed in experimental code {exc_value}')
