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
import	os, pickle
from	contextlib import suppress
from	types import TracebackType
from	typing import Generic, Optional, TypeVar
from	typing import Type
#
__all__ = ['Persist']
#
_T = TypeVar ('_T')
#
class Persist (Generic[_T]):
	__slots__ = ['path', 'data']
	def __init__ (self, path: str, initial: _T) -> None:
		self.path = path
		self.data = initial
	
	def __enter__ (self) -> _T:
		self.load ()
		return self.data

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.save (exc_type is not None)
		return None

	def load (self) -> None:
		with suppress (FileNotFoundError), open (self.path, 'rb') as fd:
			self.data = pickle.load (fd)
	
	def save (self, backup: bool = False) -> None:
		if backup and os.path.isfile (self.path):
			with suppress (OSError):
				os.rename (self.path, f'${self.path}~')
		if bool (self.data):
			with open (self.path, 'wb') as fd:
				pickle.dump (self.data, fd)
		elif os.path.isfile (self.path):
			os.unlink (self.path)
