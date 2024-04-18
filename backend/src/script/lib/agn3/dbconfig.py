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
import	os, re
from	typing import Any, Optional
from	typing import Dict, Iterator
from	.definitions import syscfg
#
class DBConfig:
	"""Database configuration handler

reads and stores configuration from a configuration file"""
	__slots__ = ['path', 'data', 'default_id']
	default_config_path = os.environ.get ('DBCFG_PATH', '/opt/agnitas.com/etc/dbcfg')
	def __init__ (self, path: Optional[str] = None) -> None:
		self.path = path if path is not None else self.default_config_path
		self.data: Dict[str, DBConfig.DBRecord] = {}
		dbid = syscfg.get ('dbid')
		if dbid is not None:
			self.default_id = dbid
		else:
			self.default_id = 'emm'
		self.read ()
		if len (self.data) == 1:
			self.default_id = list (self.data.keys ())[0]

	class DBRecord:
		"""A Record for one database configuration line"""
		__slots__ = ['dbid', 'data']
		def __init__ (self, dbid: Optional[str], param: Optional[str] = None) -> None:
			self.dbid = dbid
			self.data: Dict[str, str] = {}
			if param is not None:
				for elem in [_p.strip () for _p in param.split (', ')]:
					elem = elem.strip ()
					parts = [_e.strip () for _e in elem.split ('=', 1)]
					if len (parts) == 2:
						self.data[parts[0]] = parts[1]

		def __getitem__ (self, id: str) -> str:
			return self.data[id]
		
		def __setitem__ (self, id: str, value: str) -> None:
			self.data[id] = value

		def __contains__ (self, id: str) -> bool:
			return id in self.data

		def __call__ (self, id: str, default: Any = None) -> Any:
			try:
				return self[id]
			except KeyError:
				return default
		
		def update (self, source: Dict[str, str]) -> None:
			self.data.update (source)
		
		def copy (self) -> DBConfig.DBRecord:
			rc = self.__class__ (self.dbid, None)
			rc.data = self.data.copy ()
			return rc

	parseLine = re.compile ('([a-z0-9._+-]+):[ \t]*(.*)$', re.IGNORECASE)
	def read (self) -> None:
		self.data.clear ()
		if not os.path.isfile (self.path):
			dbcfg = syscfg.get ('dbcfg')
			if dbcfg is not None:
				self.data[syscfg.get ('dbid', 'emm')] = DBConfig.DBRecord (self.default_id, dbcfg)
				return
		#
		with open (self.path, 'r') as fd:
			for line in fd:
				line = line.strip ()
				if not line or line.startswith ('#'):
					continue
				mtch = self.parseLine.match (line)
				if mtch is None:
					continue
				(id, param) = mtch.groups ()
				self.data[id] = DBConfig.DBRecord (id, param)

	def __getitem__ (self, id: Optional[str]) -> DBConfig.DBRecord:
		return self.data[id if id is not None else self.default_id]

	def __iter__ (self) -> Iterator[str]:
		return iter (self.data)

	def inject (self, id: str, param: Dict[str, str]) -> None:
		"""adds a configuration which is not part of the configuration file

with this method it is possible to add a configuration for a database
which is not part of the configuration file."""
		rec = DBConfig.DBRecord (id, None)
		rec.data = param
		self.data[id] = rec
	
	def remove (self, id: str) -> None:
		"""remove a configuration entry"""
		if id in self.data:
			del self.data[id]
