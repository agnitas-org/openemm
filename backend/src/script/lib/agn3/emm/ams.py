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
from	types import TracebackType
from	typing import Any, Optional, Protocol
from	typing import Type
from	..definitions import ams
from	..parser import Parsable
#
__all__ = ['AMS', 'AMSLock']
#
class AMSLockProtocol (Protocol):
	def __init__ (self, *args: Any, **kwargs: Any) -> None: ...
	def __enter__ (self) -> AMSLockProtocol: ...
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]: ...
	def refresh (self, name: str) -> bool: ...
	def lock (self, name: str, ttl: Parsable = None, *, throw: bool = False) -> bool: ...
	def pick (self, name: str) -> bool: ...
	def unlock (self, name: str) -> bool: ...

AMSLock: Type[AMSLockProtocol]

if ams:
	from	._ams import AMS, AMSLock
else:
	from	._amsmock import AMSLock
