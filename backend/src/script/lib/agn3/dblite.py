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
from	typing import Optional
from	typing import List
from	.db import DB
from	.dbcore import Core
from	._db.sqlite import Layout, SQLite3
#
__all__ = ['DBLite', 'DB', 'Layout']
#
class DBLite (DB):
	__slots__ = ['_path', '_layout']
	def __init__ (self, path: str, layout: Optional[List[Layout]] = None) -> None:
		super ().__init__ ()
		self._path = path
		self._layout = layout
	
	def new (self) -> Core:
		return SQLite3 (
			filename = self._path,
			layout = self._layout,
			extended_types = True,
			extended_rows = True,
			extended_functions = True
		)
