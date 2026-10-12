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
import	os, re, json
from	typing import Final
from	typing import Iterator
from	.definitions import base, syscfg, dbid_default
#
class DBConfig:
	"""Database configuration handler

reads and stores configuration from a configuration file"""
	__slots__ = ['path', 'data', 'dbid_default']
	config_env: Final[str] = 'DBCFG'
	config_path_env: Final[str] = 'DBCFG_PATH'
	default_config_path: Final[str] = '/opt/agnitas.com/etc/dbcfg'
	@classmethod
	def record (cls, line: str) -> dict[str, str]:
		return dict ((_k.strip (), _v.strip ()) for (_k, _v) in (_e.split ('=', 1) for _e in line.split (', ') if '=' in _e))
		
	def __init__ (self, path: None | str = None) -> None:
		self.path = self._findpath (path)
		self.data: dict[str, dict[str, str]] = {}
		self.dbid_default = dbid_default
		self.read ()
		if len (self.data) == 1:
			self.dbid_default = list (self.data.keys ())[0]
	
	def _findpath (self, path: None | str) -> str:
		if path is not None:
			return path
		if (path := os.environ.get (self.config_path_env)) is not None:
			return path
		path = os.path.join (base, 'etc', 'dbcfg')
		if os.path.isfile (path):
			return path
		return self.default_config_path

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
						self.data[jid] = self.record (jparam) if isinstance (jparam, str) else jparam
			except Exception:
				content = dbcfg
		elif os.path.isfile (self.path):
			with open (self.path) as fd:
				content = fd.read ()
		else:
			if (dbcfg := syscfg.get ('dbcfg')) is not None:
				self.data[self.dbid_default] = self.record (dbcfg)
			return
		#
		if content is not None:
			for line in (_l.strip () for _l in content.split ('\n')):
				if line and not line.startswith ('#') and (mtch := self.parse_line.match (line)) is not None:
					(id, param) = mtch.groups ()
					self.data[id] = self.record (param)

	def __getitem__ (self, id: None | str) -> dict[str, str]:
		return self.data[id if id is not None else self.dbid_default]

	def __iter__ (self) -> Iterator[str]:
		return iter (self.data)
	
	def keys (self) -> Iterator[str]:
		return iter (self.data.keys ())
	
	def values (self) -> Iterator[dict[str, str]]:
		return iter (self.data.values ())

	def items (self) -> Iterator[tuple[str, dict[str, str]]]:
		return iter (self.data.items ())

	def inject (self, id: str, param: dict[str, str]) -> None:
		"""adds a configuration which is not part of the configuration file

with this method it is possible to add a configuration for a database
which is not part of the configuration file."""
		self.data[id] = param
	
	def remove (self, id: str) -> None:
		"""remove a configuration entry"""
		if id in self.data:
			del self.data[id]
