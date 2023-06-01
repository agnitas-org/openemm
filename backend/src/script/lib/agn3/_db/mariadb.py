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
from	..ignore import Ignore
#
with Ignore (ImportError):
	try:
		import	mariadb as mariadb_driver
	except:
		raise ImportError ()
	import	logging, time
	from	typing import Any, Callable, Optional, Union
	from	typing import Dict, List, Tuple
	from	typing import cast
	from	.types import Driver
	from	..dbapi import DBAPI
	from	..dbcore import Row, Cursor, Core, DBType
	from	..exceptions import error
	from	..ignore import Ignore
	from	..tools import atob

	logger = logging.getLogger (__name__)

	class CursorMariaDB (Cursor):
		"""MariaDB sepcific Cursor implementation"""
		__slots__: List[str] = ['tries', 'rowmap']
		def __init__ (self, db: Core, autocommit: bool) -> None:
			super ().__init__ (db, autocommit)
			self.tries = 3
			self.rowmap: List[Callable[[Any], Any]] = []

		def executor (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None) -> int:
			last_error: Optional[Exception] = None
			for state in range (self.tries):
				if state:
					time.sleep (state)
				try:
					return super ().executor (statement, parameter)
				except mariadb_driver.Error as e:
					last_error = e
					self.db.log (f'Got mariadb error: {e}')
					try:
						if e.errno not in {
							# MySQL/MariaDB in common error codes
							1027,	# '%s' is locked against change
							1099,	# Table '%s' was locked with a READ lock and can't be updated
							1105,	# Unknown error
							1150,	# Delayed insert thread couldn't get requested lock for table %s
							1157,	# Couldn't uncompress communication packet
							1158,	# Got an error reading communication packets
							1159,	# Got timeout reading communication packets
							1160,	# Got an error writing communication packets
							1161,	# Got timeout writing communication packets
							1213,	# Deadlock found when trying to get lock; try restarting transaction
							1317,	# Query execution was interrupted
							# MariaDB exclusive error codes
							1924,	# Query cache is disabled (resize or similar command in progress); repeat this command later
							1931,	# Query execution was interrupted. The query examined at least %llu rows, which exceeds LIMIT ROWS EXAMINED (%llu). The query result may be incomplete.
							1969,	# Query execution was interrupted (max_statement_time exceeded)
							3024,	# Query execution was interrupted, maximum statement execution time exceeded
							3058	# Deadlock found when trying to get user-level lock; try rolling back transaction/releasing locks and restarting lock acquisition.
						}:
							raise
						logger.warning ('Failed to execute query {statement} {parameter!r} due to {e} in sqlstate "{sqlstate}"{retry}'.format (
							statement = statement,
							parameter = parameter,
							e = e,
							sqlstate = getattr (e, 'sqlsatate', '-'),
							retry = ', scheduled for retry' if state + 1 < self.tries else ''
						))
					except AttributeError:
						raise e
			raise last_error if last_error is not None else error (f'retry count {self.tries} exceeded')

		def make_row (self, data: List[Any]) -> Row:
			if self.rowtype is None:
				self.rowmap = [self.find_mapper (_d[0], _d[1]) for _d in self.description ()]
			return super ().make_row ([_m (_d) for (_m, _d) in zip (self.rowmap, data)])
			
		def querys (self, req: str, parm: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Optional[Row]:
			rc = super ().querys (req, parm, cleanup)
			if rc is not None:
				with Ignore ():
					while cast (DBAPI.Cursor, self.curs).fetchmany ():
						pass
				self.rowcount = 1
			return rc
		
		def find_mapper (self, name: str, typ: Any) -> Callable[[Any], Any]:
			if typ == mariadb_driver.STRING:
				return self.normalize_string
			return lambda a: a
		
		def normalize_string (self, value: Any) -> Optional[str]:
			if value is None or isinstance (value, str):
				return value
			if isinstance (value, bytes):
				try:
					return value.decode ('UTF-8')
				except UnicodeDecodeError:
					return value.decode ('UTF-8', errors = 'backslashreplace')
			return str (value)

	class MariaDB (Core):
		"""MariaDB specific Core implementation"""
		__slots__ = ['host', 'user', 'passwd', 'name']
		BINARY_FLAG = 128
		def __init__ (self, host: str, user: str, passwd: str, name: str) -> None:
			super ().__init__ ('mariadb', cast (DBAPI.Vendor, mariadb_driver), CursorMariaDB)
			self.fallbacks.insert (0, 'mysql')
			self.host = host
			self.user = user
			self.passwd = passwd
			self.name = name

		def typ (self, entry: Tuple[Any, ...]) -> DBType:
			if len (entry) > 7:
				if entry[1] == mariadb_driver.BINARY and (entry[7] & self.BINARY_FLAG) == 0:
					return DBType.STRING
			return super ().typ (entry)

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
				'autocommit': False
			}
			if port is not None:
				config['port'] = port
			config.update (self.connect_options)
			auto_reconnect = atob (config.pop ('auto_reconnect', False))
			self.db = self.driver.connect (**config)
			if auto_reconnect:
				cast (DBAPI.DriverMariaDB, self.db).auto_reconnect = auto_reconnect

	mariadb = Driver ('mariadb', None, lambda cfg: MariaDB (cfg ('host'), cfg ('user'), cfg ('password'), cfg ('name')))
