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
import	os, re, json
from	typing import Any, Final, Optional
from	typing import Dict, Iterator, Tuple
from	typing import overload
from	.definitions import base, syscfg, dbid_default
#
class DBConfig:
	"""Database configuration handler

reads and stores configuration from a configuration file"""
	__slots__ = ['path', 'data', 'dbid_default']
	config_env: Final[str] = 'DBCFG'
	config_path_env: Final[str] = 'DBCFG_PATH'
	default_config_path: Final[str] = '/opt/agnitas.com/etc/dbcfg'
	def __init__ (self, path: Optional[str] = None) -> None:
		self.path = self._findpath (path)
		self.data: Dict[str, DBConfig.DBRecord] = {}
		self.dbid_default = dbid_default
		self.read ()
		if len (self.data) == 1:
			self.dbid_default = list (self.data.keys ())[0]
	
	def _findpath (self, path: Optional[str]) -> str:
		if path is not None:
			return path
		if (path := os.environ.get (self.config_path_env)) is not None:
			return path
		path = os.path.join (base, 'etc', 'dbcfg')
		if os.path.isfile (path):
			return path
		return self.default_config_path

	class DBRecord:
		"""A Record for one database configuration line"""
		__slots__ = ['dbid', 'data']
		def __init__ (self, dbid: None | str, param: Optional[str] = None) -> None:
			self.dbid = dbid
			self.data: Dict[str, str] = {}
			if param is not None:
				for elem in [_p.strip () for _p in param.split (', ')]:
					elem = elem.strip ()
					parts = [_e.strip () for _e in elem.split ('=', 1)]
					if len (parts) == 2:
						self.data[parts[0]] = parts[1]
		
		def __str__ (self) -> str:
			return f'{self.__class__.__name__} (dbid = {self.dbid!r}, data = {self.data!r}'
		__repr__ = __str__

		def __getitem__ (self, id: str) -> str:
			return self.data[id]
		
		def __setitem__ (self, id: str, value: str) -> None:
			self.data[id] = value

		def __contains__ (self, id: str) -> bool:
			return id in self.data

		@overload
		def __call__ (self, id: str, default: None = ...) -> None | str: ...
		@overload
		def __call__ (self, id: str, default: str) -> str: ...
		def __call__ (self, id: str, default: None | str = None) -> Any:
			try:
				return self[id]
			except KeyError:
				return default
		
		def update (self, source: Dict[str, str]) -> None:
			self.data.update (source)
		
		def copy (self) -> DBConfig.DBRecord:
			rc = self.__class__ (self.dbid, None)
			rc.update (self.data)
			return rc

	parse_line = re.compile ('([a-z0-9._+-]+):[ \t]*(.*)$', re.IGNORECASE)
	def read (self) -> None:
		self.data.clear ()
		content: None | str = None
		if (dbcfg := os.environ.get (self.config_env)) is not None:
			try:
				parsed = json.loads (dbcfg)
				if isinstance (parsed, dict):
					jid: str
					jparam: str | dict[str, str]
					for (jid, jparam) in parsed.items ():
						self.data[jid] = record = DBConfig.DBRecord (jid, jparam if isinstance (jparam, str) else None)
						if isinstance (jparam, dict):
							record.update (jparam)
			except Exception:
				content = dbcfg
		elif os.path.isfile (self.path):
			with open (self.path) as fd:
				content = fd.read ()
		else:
			if (dbcfg := syscfg.get ('dbcfg')) is not None:
				self.data[self.dbid_default] = DBConfig.DBRecord (self.dbid_default, dbcfg)
			return
		#
		if content is not None:
			for line in (_l.strip () for _l in content.split ('\n')):
				if line and not line.startswith ('#') and (mtch := self.parse_line.match (line)) is not None:
					(id, param) = mtch.groups ()
					self.data[id] = DBConfig.DBRecord (id, param)

	def __getitem__ (self, id: Optional[str]) -> DBConfig.DBRecord:
		return self.data[id if id is not None else self.dbid_default]

	def __iter__ (self) -> Iterator[str]:
		return iter (self.data)
	
	def keys (self) -> Iterator[str]:
		return iter (self.data.keys ())
	
	def values (self) -> Iterator[DBConfig.DBRecord]:
		return iter (self.data.values ())

	def items (self) -> Iterator[Tuple[str, DBConfig.DBRecord]]:
		return iter (self.data.items ())

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
