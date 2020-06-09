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
from	..ignore import Ignore
#
with Ignore (ImportError):
	import	os, pickle, fcntl, logging
	from	collections import namedtuple
	import	sqlite3
	from	typing import Any, Callable, Optional
	from	typing import List, Tuple, Type
	from	typing import cast
	from	.types import Driver
	from	..dbapi import DBAPI
	from	..dbcore import Cursor, Core, DBType, Binary
	from	..exceptions import error
	from	..tools import atob

	logger = logging.getLogger (__name__)

	class CursorSQLite3 (Cursor):
		"""SQLite specific Cursor implementation

these database specific methods are available here:
	- mode: set the operation mode:
	- validate: performs an integrity check of the database
"""
		__slots__: List[str] = []
		def __init__ (self, db: Core, autocommit: bool) -> None:
			super ().__init__ (db, autocommit, False)

		modes = {
			'fast':	[
				'PRAGMA cache_size = 65536',
				'PRAGMA automatic_index = ON',
				'PRAGMA journal_mode = OFF',
				'PRAGMA synchronous = OFF',
				'PRAGMA secure_delete = OFF',
			],
			'exclusive': [
				'PRAGMA locking_mode = EXCLUSIVE'
			]
		}
		def mode (self, m: str) -> None:
			"""set operation mode for the database
			
currently these two modes are implemented, which can also be used in conjunction:
	- "fast" for fast, but insecure database access
	- "exclusive" for exclusive locking
"""
			try:
				for op in self.modes[m]:
					self.execute (op)
			except KeyError:
				raise error ('invalid mode: %s (expecting one of %s)' % (m, ', '.join (sorted (self.modes.keys ()))))

		def validate (self, collect: Optional[List[Any]] = None, quick: bool = False, amount: Optional[int] = None) -> bool:
			"""performs an integrity check of the database, returns True on okay, otherwise False

``collect'' can be a list where the results of the integrity check are
stored. ``quick'' can be set to True to just perform a quick check of
the database. ``amount'' can be numeric value to restrict the check."""
			rc = False
			if quick:
				method = 'quick_check'
			else:
				method = 'integrity_check'
			if amount is not None and amount >= 0:
				method += '(%d)' % amount
			for (count, r) in enumerate (self.queryc ('PRAGMA %s' % method), start = 1):
				if r[0]:
					if count == 1 and r[0] == 'ok':
						rc = True
					else:
						rc = False
					if collect is not None:
						collect.append (r[0])
			return rc

	Layout = namedtuple ('Layout', ['statement', 'parameter'])
	class SQLite3 (Core):
		"""SQLite specific Core implementation"""
		__slots__ = [
			'filename', 'layout', 'extended_types', 'extended_rows', 'functions',
			'lock_database', 'wait_for_lock', 'lock_fd'
		]
		def __init__ (self,
			filename: str,
			layout: Optional[List[Layout]] = None,
			extended_types: bool = False,
			extended_rows: bool = False,
			extended_functions: bool = False,
			lock_database: bool = False,
			wait_for_lock: bool = False
		):
			super ().__init__ ('sqlite', cast (DBAPI.Vendor, sqlite3), CursorSQLite3)
			self.filename = filename
			self.layout = layout
			self.extended_types = extended_types
			self.extended_rows = extended_rows
			self.functions: List[Tuple[Callable[..., None], Tuple[Any, ...]]] = []
			if extended_types:
				self.register_class (Binary)
			if extended_functions:
				def __nvl (value: Any, default: Any) -> Any:
					if value is None:
						return default
					return value
				self.create_function ('nvl', 2, __nvl)
				#
				def __decode (*args: Any) -> Any:
					alen = len (args)
					n = 0
					while n + 2 < alen:
						if args[n] == args[n + 1]:
							return args[n + 2]
						n += 3
					if n + 1 == alen:
						return args[n]
					return None
				self.create_function ('decode', -1, __decode)
			self.lock_database = lock_database
			self.wait_for_lock = wait_for_lock
			self.lock_fd: Optional[int] = None

		def register_type (self,
			name: str,
			register_class: Type[Any],
			adapt: Callable[[Any], bytes],
			convert: Callable[[bytes], Any],
			datatype: DBType
		) -> None:
			"""registers a new type to be interpreted by this driver

``name'' is the name of the type to register, ``register_class'' is the
class to represent the data of this type, ``adapt'' is a callable
which transforms the class to a string based representation,
``convert'' is for parsing a string into the class and ``datatype'' is
used to map this new type to a normalized data type. """
			if not self.extended_types:
				raise error ('extended types disabled, %s not registered' % name)
			sqlite3.register_adapter (register_class, adapt)
			sqlite3.register_converter (name, convert)
			if datatype is not None:
				self.types[name] = datatype
		
		def register_class (self, register_class: Type[Any]) -> None:
			"""registers a new type and use the class attributes for configuration

the class must provide these attributes to be used (see register_type for the meaing of them):
	- name
	- dump (for adapt)
	- load (for convert)
	- datatype
"""
			self.register_type (register_class.name, register_class, register_class.dump, register_class.load, register_class.datatype)

		def register (self, name: str, register_class: Type[Any], datatype: DBType) -> None:
			"""registers a new type and use python pickler for converting from/to string representation"""
			self.register_type (name, register_class, lambda o: pickle.dumps (o), lambda s: pickle.loads (s), datatype)
		
		def __function (self, name: Callable[..., None], args: Tuple[Any, ...]) -> None:
			self.functions.append ((name, args))
			if self.db is not None:
				name (*args)
		def __create_function (self, name: str, number_of_parameter: int, method: Callable[..., None]) -> None:
			cast (DBAPI.DriverSQLite3, self.db).create_function (name, number_of_parameter, method)
		def create_function (self, name: str, number_of_parameter: int, method: Callable[..., None]) -> None:
			self.__function (self.__create_function, (name, number_of_parameter, method))
		def __create_collation (self, name: str, method: Callable[..., None]) -> None:
			cast (DBAPI.DriverSQLite3, self.db).create_collation (name, method)
		def create_collation (self, name: str, method: Callable[..., None]) -> None:
			self.__function (self.__create_collation, (name, method))
		def __create_aggregate (self, name: str, number_of_parameter: int, cls: Type[Any]) -> None:
			cast (DBAPI.DriverSQLite3, self.db).create_aggregate (name, number_of_parameter, cls)
		def create_aggregate (self, name: str, number_of_parameter: int, cls: Type[Any]) -> None:
			self.__function (self.__create_aggregate, (name, number_of_parameter, cls))

		def close (self) -> None:
			if self.lock_database and self.lock_fd is not None:
				try:
					fcntl.flock (self.lock_fd, fcntl.LOCK_UN)
				except IOError as e:
					logger.warning ('%s: failed to unlock: %s' % (self.filename, e))
				try:
					os.close (self.lock_fd)
				except OSError as e:
					logger.warning ('%s: failed to close: %s' % (self.filename, e))
				self.lock_fd = None
			super ().close ()
			
		def connect (self) -> None:
			isNew = not os.path.isfile (self.filename)
			if self.extended_types:
				self.db = self.driver.connect (self.filename, detect_types = sqlite3.PARSE_DECLTYPES | sqlite3.PARSE_COLNAMES)
			else:
				self.db = self.driver.connect (self.filename)
			if self.lock_database:
				try:
					self.lock_fd = os.open (self.filename, os.O_NOATIME)
					try:
						fcntl.flock (self.lock_fd, fcntl.LOCK_EX | (0 if self.wait_for_lock else fcntl.LOCK_NB))
					except IOError as e:
						logger.debug ('%s: failed to lock: %s' % (self.filename, e))
						with Ignore (OSError):
							os.close (self.lock_fd)
						self.lock_fd = None
				except OSError as e:
					logger.warning ('%s: failed to open file: %s' % (self.filename, e))
				if self.lock_fd is None:
					self.close ()
					raise error ('%s: database locked' % self.filename)
			if self.extended_rows:
				cast (DBAPI.DriverSQLite3, self.db).row_factory = sqlite3.Row
			if self.db is not None:
				for (name, args) in self.functions:
					name (*args)
				if isNew and self.layout is not None:
					c = self.cursor ()
					cast (CursorSQLite3, c).mode ('fast')
					for entry in self.layout:
						c.update (entry.statement, entry.parameter)
					c.close ()

	sqlite = Driver (
		'sqlite',
		['sqlite3'],
		lambda cfg: SQLite3 (
			cfg ('filename'),
			extended_types = atob (cfg ('extended-types', False)),
			extended_rows = atob (cfg ('extended-rows', False)),
			extended_functions = atob (cfg ('extended-functions', False)),
			lock_database = atob (cfg ('lock-database', False)),
			wait_for_lock = atob (cfg ('wait-for-lock', False))
		)
	)
	
