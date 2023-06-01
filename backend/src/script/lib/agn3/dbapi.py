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
from	types import ModuleType
from	typing import Any, Callable, Literal, Optional, Union
from	typing import Dict, List, Tuple, Type

class DBAPI:
	class Vendor (ModuleType):
		STRING: Any
		FIXED_CHAR: Any
		NUMBER: Any
		DATETIME: Any
		TIMESTAMP: Any
		CLOB: Any
		BLOB: Any
		BINARY: Any
		ROWID: Any
		paramstyle: Literal['qmark', 'numeric', 'named', 'format', 'pyformat']
		class Error (Exception): ...
		def connect (*args: Any, **kwargs: Any) -> DBAPI.Driver: ...
	class Driver:
		def commit (self) -> None: ...
		def rollback (self) -> None: ...
		def close (self) -> None: ...
		def cursor (self) -> DBAPI.Cursor: ...
	class DriverMariaDB (Driver):
		auto_reconnect: bool
	class DriverSQLite3 (Driver):
		row_factory: Any
		def create_function (self, name: str, number_of_parameter: int, method: Callable[..., None]) -> None: ...
		def create_collation (self, name: str, method: Callable[..., None]) -> None: ...
		def create_aggregate (self, name: str, number_of_parameter: int, cls: Type[Any]) -> None: ...
	class DriverOracle (Driver):
		stmtcachesize: int
		autocommit: int
	class Cursor:
		description: Optional[List[Tuple[str, Any, Optional[int], Optional[int], Optional[int], Optional[int], Optional[bool]]]]
		rowcount: int
		arraysize: int
		def execute (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None) -> int: ...
		def fetchone (self) -> List[Any]: ...
		def fetchmany (self) -> List[List[Any]]: ...
		def fetchall (self) -> List[List[Any]]: ...
		def close (self) -> None: ...
	class CursorOracle (Cursor):
		def setoutputsize (self, *args: Any) -> None: ...
		def setinputsizes (self, **kwargs: Any) -> None: ...

