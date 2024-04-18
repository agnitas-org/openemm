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
from	typing import Final, Optional
from	typing import List
from	.db import DB, Row
from	.dbcore import Core
from	._db.sqlite import Layout, SQLite3
#
__all__ = ['DBLite', 'DB', 'Row', 'Layout']
#
class DBLite (DB):
	__slots__ = ['_path', '_layout', '_lock']
	in_memory_db: Final[str] = ':memory:'
	def __init__ (self, path: str = in_memory_db, layout: Optional[List[Layout]] = None, lock: bool = False) -> None:
		super ().__init__ ()
		self._path = path
		self._layout = layout
		self._lock = lock
	
	def new (self) -> Core:
		return SQLite3 (
			filename = self._path,
			layout = self._layout,
			extended_types = True,
			extended_rows = True,
			extended_functions = True,
			lock_database = self._lock,
			wait_for_lock = self._lock
		)
