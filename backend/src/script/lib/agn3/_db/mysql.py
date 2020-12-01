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
from	typing import Any
from	..ignore import Ignore
#
with Ignore (ImportError):
	try:
		import	MySQLdb
		mysql_driver: Any = MySQLdb
	except ImportError:
		from	mysql import connector
		mysql_driver = connector
		
	from	typing import Any, Optional, Union
	from	typing import Dict, List
	from	typing import cast
	from	.types import Driver
	from	..dbapi import DBAPI
	from	..dbcore import Row, Cursor, Core

	class CursorMySQL (Cursor):
		"""MySQL sepcific Cursor implementation"""
		__slots__: List[str] = []
		def __init__ (self, db: Core, autocommit: bool) -> None:
			super ().__init__ (db, autocommit, True)

		def querys (self, req: str, parm: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Optional[Row]:
			rc = super ().querys (req, parm, cleanup)
			if rc is not None:
				with Ignore ():
					while cast (DBAPI.Cursor, self.curs).fetchmany ():
						pass
			return rc

	class MySQL (Core):
		"""MySQL specific Core implementation"""
		__slots__ = ['host', 'user', 'passwd', 'name']
		def __init__ (self, host: str, user: str, passwd: str, name: str) -> None:
			super ().__init__ ('mysql', cast (DBAPI.Vendor, mysql_driver), CursorMySQL)
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
			if self.driver.__name__ == 'mysql.connector':
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
			else:
				utf8 = {
					'charset': 'utf8',
					'use_unicode': True
				}
				if port is not None:
					self.db = self.driver.connect (host, self.user, self.passwd, self.name, port, **utf8)
				else:
					self.db = self.driver.connect (self.host, self.user, self.passwd, self.name, **utf8)

	mysql = Driver ('mysql', ['mariadb'], lambda cfg: MySQL (cfg ('host'), cfg ('user'), cfg ('password'), cfg ('name')))
