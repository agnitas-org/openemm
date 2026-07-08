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
	import	os, pickle, fcntl, logging
	import	sqlite3
	from	datetime import datetime
	from	typing import Any, Callable, Final
	from	typing import NamedTuple, Type
	from	typing import cast
	from	.types import Driver
	from	..dbapi import DBAPI
	from	..dbcore import Cursor, Core, DBType, Binary
	from	..emm.build import spec
	from	..exceptions import error
	from	..tools import atob

	logger = logging.getLogger (__name__)

	class CursorSQLite3 (Cursor):
		"""SQLite specific Cursor implementation

these database specific methods are available here:
	- mode: set the operation mode:
	- validate: performs an integrity check of the database
"""
		__slots__: list[str] = []
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
				raise error ('invalid mode: {mode} (expecting one of {modes})'.format (
					mode = m,
					modes = ', '.join (sorted (self.modes.keys ()))
				))

		def validate (self, collect: None | list[Any] = None, quick: bool = False, amount: None | int = None) -> bool:
			"""performs an integrity check of the database, returns True on okay, otherwise False

``collect'' can be a list where the results of the integrity check are
stored. ``quick'' can be set to True to just perform a quick check of
the database. ``amount'' can be numeric value to restrict the check."""
			rc = False
			method = 'quick_check' if quick else 'integrity_check'
			if amount is not None and amount >= 0:
				method += f'({amount})'
			for (count, r) in enumerate (self.queryc (f'PRAGMA {method}'), start = 1):
				if r[0]:
					if count == 1 and r[0] == 'ok':
						rc = True
					else:
						rc = False
					if collect is not None:
						collect.append (r[0])
			return rc

	class Layout (NamedTuple):
		statement: str
		parameter: None | dict[str, Any] = None
	class SQLite3 (Core):
		"""SQLite specific Core implementation"""
		__slots__ = [
			'filename', 'layout', 'updates', 
			'extended_types', 'extended_rows', 'functions',
			'lock_database', 'wait_for_lock', 'read_only', 'lock_fd'
		]
		in_memory_db: Final[str] = ':memory:'
		def __init__ (self,
			filename: str,
			*,
			layout: None | list[Layout] = None,
			updates: None | dict[str, list[Layout]] = None,
			extended_types: bool = False,
			extended_rows: bool = False,
			extended_functions: bool = False,
			lock_database: bool = False,
			wait_for_lock: bool = False,
			read_only: bool = False
		):
			super ().__init__ ('sqlite', cast (DBAPI.Vendor, sqlite3), CursorSQLite3)
			self.filename = filename
			self.layout = layout
			self.updates = updates
			self.extended_types = extended_types
			self.extended_rows = extended_rows
			self.functions: list[tuple[Callable[..., None], tuple[Any, ...]]] = []
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
			self.read_only = read_only and filename != self.in_memory_db
			self.lock_fd: None | int = None

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
				raise error (f'extended types disabled, {name} not registered')
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
		
		def __function (self, function: Callable[..., None], args: tuple[Any, ...]) -> None:
			self.functions.append ((function, args))
			if self.db is not None:
				function (*args)
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
					logger.warning (f'{self.filename}: failed to unlock: {e}')
				try:
					os.close (self.lock_fd)
				except OSError as e:
					logger.warning (f'{self.filename}: failed to close: {e}')
				self.lock_fd = None
			super ().close ()
			
		def connect (self) -> None:
			is_new_file = not os.path.isfile (self.filename)
			config: dict[str, Any] = {
				'database': self.filename if not self.read_only else f'file:{self.filename}?mode=ro'
			}
			if self.read_only:
				config['uri'] = True
			if self.extended_types:
				config['detect_types'] = sqlite3.PARSE_DECLTYPES | sqlite3.PARSE_COLNAMES
			config.update (self.connect_options)
			self.db = self.driver.connect (**config)
			if self.lock_database:
				try:
					self.lock_fd = os.open (self.filename, os.O_NOATIME)
					try:
						fcntl.flock (self.lock_fd, fcntl.LOCK_EX | (0 if self.wait_for_lock else fcntl.LOCK_NB))
					except IOError as e:
						logger.debug (f'{self.filename}: failed to lock: {e}')
						with Ignore (OSError):
							os.close (self.lock_fd)
						self.lock_fd = None
				except OSError as e:
					logger.warning (f'{self.filename}: failed to open file: {e}')
				if self.lock_fd is None:
					self.close ()
					raise error (f'{self.filename}: database locked')
			if self.extended_rows:
				cast (DBAPI.DriverSQLite3, self.db).row_factory = sqlite3.Row
			if self.db is not None:
				for (function, args) in self.functions:
					function (*args)
				if (is_new_file and self.layout) or self.updates:
					changelog_table = '_changelog'
					changelog_layout = [
						Layout (statement = (
							'CREATE TABLE _changelog (\n'
							'\tname\ttext NOT NULL PRIMARY KEY,\n'
							'\tcreated\ttext NOT NULL,\n'
							'\tversion\ttext NOT NULL\n'
							')'
						))
					]
					changelog_updates: dict[str, list[Layout]] = {
					}
					with self.cursor () as cursor:
						def setup (layout: list[Layout]) -> None:
							for entry in layout:
								cursor.update (entry.statement, entry.parameter)
						def update (updates: dict[str, list[Layout]], seen: set[str]) -> None:
							for (name, layout) in updates.items ():
								if name not in seen:
									setup (layout)
									seen.add (name)
									cursor.update (
										f'INSERT INTO {changelog_table} '
										'        (name, created, version) '
										'VALUES '
										'        (:name, :created, :version)',
										{
											'name': name,
											'created': f'{datetime.now ():%Y-%m-%d %H:%M:%S}',
											'version': spec.version
										}
									)
						#
						cast (CursorSQLite3, cursor).mode ('fast')
						if is_new_file and self.layout:
							setup (self.layout)
						if self.updates:
							rq = cursor.querys (
								'SELECT count(*) '
								'FROM sqlite_master '
								'WHERE type = :type AND name = :name',
								{
									'type': 'table',
									'name': changelog_table
								}
							)
							if rq is not None:
								seen: set[str]
								if rq[0] == 0:
									setup (changelog_layout)
									seen = set ()
								else:
									seen = cursor.stream (f'SELECT name FROM {changelog_table}').map (lambda r: r.name).set ()
								update (changelog_updates, seen)
								update (self.updates, seen)
						cursor.sync ()

	sqlite = Driver (
		'sqlite',
		{'sqlite3'},
		lambda cfg: SQLite3 (
			cfg['filename'],
			extended_types = atob (cfg ('extended-types)')),
			extended_rows = atob (cfg ('extended-rows')),
			extended_functions = atob (cfg ('extended-functions')),
			lock_database = atob (cfg ('lock-database')),
			wait_for_lock = atob (cfg ('wait-for-lock'))
		)
	)
	
