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
from	..ignore import Ignore
#
with Ignore (ImportError):
	try:
		import	MySQLdb
	except:
		raise ImportError ()
	from	typing import Any, Optional, Union
	from	typing import Dict, List
	from	typing import cast
	from	.types import Driver
	from	..dbapi import DBAPI
	from	..dbcore import Row, Cursor, Core

	class CursorMySQL (Cursor):
		"""MySQL sepcific Cursor implementation"""
		__slots__: List[str] = []
		def querys (self, req: str, parm: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Optional[Row]:
			rc = super ().querys (req, parm, cleanup)
			if rc is not None:
				with Ignore ():
					while cast (DBAPI.Cursor, self.curs).fetchmany ():
						pass
				self.rowcount = 1
			return rc

	class MySQL (Core):
		"""MySQL specific Core implementation"""
		__slots__ = ['host', 'user', 'passwd', 'name']
		def __init__ (self, host: str, user: str, passwd: str, name: str) -> None:
			super ().__init__ ('mysql', cast (DBAPI.Vendor, MySQLdb), CursorMySQL)
			self.host = host
			self.user = user
			self.passwd = passwd
			self.name = name

		def repr_last_error (self) -> str:
			if self.lasterr is not None:
				return 'MySQL-{nr}: {msg}'.format (
					nr = self.lasterr.args[0],
					msg = self.lasterr.args[1].strip ()
				)
			return super ().repr_last_error ()

		def connect (self) -> None:
			try:
				(host, port_str) = self.host.split (':', 1)
				port: Optional[int] = int (port_str)
			except ValueError:
				(host, port) = (self.host, None)
			config = {
				'host': host,
				'user': self.user,
				'password': self.passwd,
				'database': self.name,
				'charset': 'utf8',
				'use_unicode': True
			}
			if port is not None:
				config['port'] = port
			self.db = self.driver.connect (**config)

	mysql = Driver (
		'mysql',
		None,
		lambda cfg: MySQL (
			cfg['host'],
			cfg['user'],
			cfg['password'],
			cfg['name']
		)
	)
