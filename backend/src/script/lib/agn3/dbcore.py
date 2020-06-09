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
import	base64, re, keyword
from	abc import abstractmethod
from	collections import namedtuple
from	itertools import zip_longest
from	enum import Enum
from	types import TracebackType
from	typing import Any, Callable, Optional, Protocol, Union
from	typing import Dict, Iterator, List, NamedTuple, Set, Tuple, Type
from	typing import cast
from	.dbapi import DBAPI
from	.exceptions import error
from	.ignore import Ignore
from	.stream import Stream
#
__all__ = ['Row', 'Cursor', 'DBType', 'Core']
#
class Row (Protocol):
	def __getattr__ (self, attr: str) -> Any: ...
	def __setattr__ (self, attr: str, value: Any) -> None: ...
	def __getitem__ (self, item: int) -> Any: ...
	def __iter__ (self) -> Iterator[Any]: ...

class Cursor:
	"""Metaclass for database cursor

this is the base class for a database specific cursor and should
inherit this class. This class should not be instantiated by an
application, use ``Core.cursor()'' instead."""
	__slots__ = [
		'db', 'autocommit', 'need_reformat', 'id', 'curs',
		'rowtype',
		'cache_reformat', 'cache_variables',
		'log'
	]
	def __init__ (self, db: Core, autocommit: bool, need_reformat: bool = False) -> None:
		"""``db'' is the database specific subclass of Core.

If ``autocommit'' is set, then every update is followed by a commit to
write the content directly to the database. ``need_reformat'' should be
set to True, if the database does not support named placeholder."""
		self.db = db
		self.autocommit = autocommit
		self.need_reformat = need_reformat
		self.id: Optional[str] = None
		self.curs: Optional[DBAPI.Cursor] = None
		self.rowtype: Optional[Type[Row]] = None
		self.cache_reformat: Dict[str, Tuple[str, List[Any]]] = {}
		self.cache_variables: Dict[str, Set[str]] = {}
		self.log = db.log

	def __enter__ (self) -> Cursor:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.close ()
		return None

	def set_id (self, id: Optional[str]) -> None:
		"""Set internal id for logging"""
		self.id = id

	def mode (self, mode: str) -> None:
		raise KeyError (mode)
		
	def qselect (self, **args: str) -> str:
		"""See agn3.db.Core.qselect"""
		return self.db.qselect (**args)

	def last_error (self) -> str:
		"""returns the last occured error string"""
		if self.db is not None:
			return self.db.last_error ()
		return 'no database interface active'

	def error (self, errmsg: Optional[Exception]) -> None:
		"""sets a new error condititon"""
		if self.db is not None:
			self.db.lasterr = errmsg
		self.close ()

	def executor (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None) -> int:
		"""internally used to intercept a database access before it is passed to the database"""
		if self.curs is not None:
			return self.curs.execute (statement, parameter) if parameter is not None else self.curs.execute (statement)
		raise error ('no active cursor')
		
	def close (self) -> None:
		"""closes the current cursor"""
		try:
			self.db.release (self)
			if self.log: self.log ('Cursor closed')
		except self.db.driver.Error as e:
			self.db.lasterr = e
			if self.log: self.log ('Cursor closing failed: %s' % self.last_error ())
		self.curs = None

	def open (self) -> bool:
		"""opens the cursor used by Core.cursor()"""
		self.close ()
		if self.db is not None:
			try:
				self.curs = self.db.get_cursor ()
				if self.curs is not None:
					if self.log: self.log ('Cursor opened')
				else:
					if self.log: self.log ('Cursor open failed')
			except self.db.driver.Error as e:
				self.error (e)
				if self.log: self.log ('Cursor opening failed: %s' % self.last_error ())
		else:
			if self.log: self.log ('Cursor opeing failed: no database available')
		return self.curs is not None

	class Field (NamedTuple):
		name: str
		typ: DBType
		display_size: Optional[int] = None
		size: Optional[int] = None
		precision: Optional[int] = None
		scale: Optional[int] = None
		null_ok: bool = True
	def description (self, normalize: bool = False) -> List[Any]:
		"""returns the layout description of last query call

if ``normalize'' is True then a normalized version is returned to be
portable across different databases."""
		if self.curs is not None and self.curs.description is not None:
			return [
				Cursor.Field (
					name = _d[0].lower (),
					typ = self.db.typ (_d[1]),
					display_size = _d[2],
					size = _d[3],
					precision = _d[4],
					scale = _d[5],
					null_ok = bool (_d[6])
				) for _d in self.curs.description
			] if normalize else self.curs.description
		raise error ('no active query')

	__variable_pattern = re.compile ('\'[^\']*\'|:[a-z0-9_]+', re.IGNORECASE)
	def variables_in_query (self, query: str) -> Set[str]:
		"""Internally used method to find all variables in a query"""
		try:
			varlist = self.cache_variables[query]
		except KeyError:
			varlist = (Stream (self.__variable_pattern.findall (query))
				.filter (lambda p: cast (str, p).startswith (':'))
				.map (lambda p: p[1:])
				.set ()
			)
			self.cache_variables[query] = varlist
		return varlist
		
	def reformat (self, statement: str, parameter: Dict[str, Any]) -> Tuple[str, List[Any]]:
		"""internally used method to reformat a query with named placeholder for databases not supporting them."""
		varlist: List[str]
		try:
			(new_statement, varlist) = self.cache_reformat[statement]
		except KeyError:
			def reformater (query: str, varlist: List[str]) -> Iterator[str]:
				for (plain, match) in zip_longest (self.__variable_pattern.split (statement), self.__variable_pattern.findall (statement)):
					yield plain.replace ('%', '%%')
					if match is not None:
						if match.startswith (':'):
							varlist.append (match[1:])
							yield '%s'
						else:
							yield match.replace ('%', '%%')
			varlist = []
			new_statement = ''.join (reformater (statement, varlist))
			self.cache_reformat[statement] = (new_statement, varlist)
		#
		return (new_statement, [parameter[_k] for _k in varlist])

	def cleanup (self, statement: str, parameter: Dict[str, Any]) -> Dict[str, Any]:
		"""internally used method to cleanup query parameter that are not used in the query itself."""
		return dict ((_k, _v) for (_k, _v) in parameter.items () if _k in self.variables_in_query (statement))

	def qvalidate (self, statement: str, parameter: Dict[str, Any]) -> Tuple[bool, List[str], List[str]]:
		"""validates a query against its query parameter

this is a useful tool for a complex query and mismatched query
parameter. This returns a tuple with three elements, the first is a
bool which is True if the query and its parameter are looking okay (in
this case the remaining two elements are empty lists) or False if not.
In this case the second element of the tuple contains the parameter
which are used in the query but are not found in the query parameter,
the third element contains the keys of the query parameter which are
not used in the query."""
		var = self.variables_in_query (statement)
		missing = [_v for _v in var if _v not in parameter]
		keys = [_k for _k in parameter if _k not in var]
		return (not missing and not keys, missing, keys)
	
	def __valid (self) -> None:
		if self.curs is None:
			if not self.open ():
				raise error ('Unable to setup cursor: ' + self.last_error ())

	__valid_name = re.compile ('^[a-z][a-z0-9_]*$', re.IGNORECASE)
	def __make_row (self, data: List[Any]) -> Row:
		if self.rowtype is None:
			d = self.description ()
			rowspec = ['_%d' % (_n + 1) for _n in range (len (data))] if d is None else [_d[0].lower () for _d in d]
			#
			seen: Set[str] = set ()
			def norm (n: int, c: str) -> str:
				if self.__valid_name.match (c) is None or keyword.iskeyword (c) or c in seen:
					c = f'column_{n}'
				seen.add (c)
				return c
			try:
				self.rowtype = cast (Type[Row], namedtuple ('row', [norm (_n, _c) for (_n, _c) in enumerate (rowspec, start = 1)]))
			except:
				self.rowtype = cast (Type[Row], namedtuple ('row', ['column_{_n}'.format (_n = _n) for _n in range (1, len (rowspec) + 1)]))
		return self.rowtype (*data)
		
	def __iter__ (self) -> Iterator[Row]:
		while True:
			try:
				data = cast (DBAPI.Cursor, self.curs).fetchone ()
			except self.db.driver.Error as e:
				self.error (e)
				raise error ('query next failed: ' + self.last_error ())
			if data is None:
				break
			yield self.__make_row (data)

	def set_output_size (self, *args: Any) -> None:
		"""dbms specific method to handle LOBs in queries"""
		self.__valid ()

	def set_input_sizes (self, **args: Any) -> None:
		"""dbms specific method to handle LOBs in inserts/updates"""
		self.__valid ()

	__ph = re.compile (':([a-z0-9_]+)', re.IGNORECASE)
	def __parameter_format (self, statement: str, parameter: Dict[str, Any]) -> str:
		try:
			return Stream.of (self.__ph.findall (statement)).map (lambda ph: '%s=%r' % (ph, parameter[ph])).join (', ')
		except Exception as e:
			return '%r (%s)' % (parameter, e)
	
	def __execute (self, what: str, statement: str, parameter: Union[None, List[Any], Dict[str, Any]], cleanup: bool) -> int:
		self.__valid ()
		try:
			if parameter is None:
				if self.log: self.log (f'{what}: {statement}')
				return self.executor (statement)
			else:
				if type (parameter) is dict:
					if self.need_reformat:
						(statement, parameter_list) = self.reformat (statement, cast (Dict[str, Any], parameter))
						if self.log: self.log ('{what}: {statement} [{parameter}]'.format (what = what, statement = statement, parameter = ', '.join (['{_p:r}' for _p in parameter_list])))
						return self.executor (statement, parameter_list)
					elif cleanup:
						parameter = self.cleanup (statement, cast (Dict[str, Any], parameter))
				if self.log: self.log ('{what}: {statement} [{parameter}]'.format (what = what, statement = statement, parameter = self.__parameter_format (statement, cast (Dict[str, Any], parameter))))
				return self.executor (statement, parameter)
		except self.db.driver.Error as e:
			self.error (e)
			if self.log:
				if parameter is None:
					self.log ('{what} {statement} failed: {error}'.format (what = what, statement = statement, error = self.last_error ()))
				else:
					self.log ('{what} {statement} using {parameter:r} failed: {error}'.format (what = what, statement = statement, parameter = parameter, error = self.last_error ()))
			raise error ('{what} start failed: {error}'.format (what = what.lower (), error = self.last_error ()))
	
	def query (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Cursor:
		"""Query the database

``statement'' is the query itself, ``parameter'' are the optional query parameter
as a dict, if the query is using a prepared statement. ``cleanup''
should be set to True, if the query parameter may contain more keys
than are used in the query.

This method return an iterable realizied by itself."""
		self.rowtype = None
		self.__execute ('Query', statement, parameter, cleanup)
		if self.log: self.log ('Query started')
		return self

	def queryc (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> List[Row]:
		"""See query, but returns a cached version of the query. Use with care on large result sets!"""
		if self.query (statement, parameter, cleanup) == self:
			try:
				data = cast (DBAPI.Cursor, self.curs).fetchall ()
				return [self.__make_row (_d) for _d in data]
			except self.db.driver.Error as e:
				self.error (e)
				if self.log:
					if parameter is None:
						self.log ('Queryc %s fetch failed: %s' % (statement, self.last_error ()))
					else:
						self.log ('Queryc %s using %r fetch failed: %s' % (statement, parameter, self.last_error ()))
				raise error ('query all failed: ' + self.last_error ())
		if self.log:
			if parameter is None:
				self.log ('Queryc %s failed: %s' % (statement, self.last_error ()))
			else:
				self.log ('Queryc %s using %r failed: %s' % (statement, parameter, self.last_error ()))
		raise error ('unable to setup query: ' + self.last_error ())

	def querys (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Optional[Row]:
		"""See query, but returns only one (the first) row or None, if no row is found at all."""
		for rec in self.query (statement, parameter, cleanup):
			return rec
		return None

	def sync (self, commit: bool = True) -> bool:
		"""Ends a transaction by either writing the changes to the database (``commit'' is True) or by rollbacking the changes."""
		rc = False
		if self.db is not None:
			if self.db.db is not None:
				try:
					if self.log:
						if commit:
							self.log ('Sync: commit')
						else:
							self.log ('Sync: rollback')
					self.db.sync (commit)
					if self.log:
						if commit:
							self.log ('Sync done commiting')
						else:
							self.log ('Sync done rollbacking')
					rc = True
				except self.db.driver.Error as e:
					self.error (e)
					if self.log:
						if commit:
							self.log ('Sync failed commiting')
						else:
							self.log ('Sync failed rollbacking')
			else:
				if self.log: self.log ('Sync failed: database not open')
		else:
			if self.log: self.log ('Sync failed: database not available')
		return rc

	def update (self, statement: str, parameter: Union[Any, List[Any], Dict[str, Any]] = None, commit: bool = False, cleanup: bool = False) -> int:
		"""Performs non query database calls. For parameter see query. Returns number of changed rows, if applicated."""
		self.__execute ('Update', statement, parameter, cleanup)
		rows = cast (DBAPI.Cursor, self.curs).rowcount
		if rows > 0 and (commit or self.autocommit):
			if not self.sync ():
				if self.log:
					if parameter is None:
						self.log ('Commit after execute failed for %s: %s' % (statement, self.last_error ()))
					else:
						self.log ('Commit after execute failed for %s using %r: %s' % (statement, parameter, self.last_error ()))
				raise error ('commit failed: ' + self.last_error ())
		return rows
	
	def execute (self, statement: str) -> int:
		return self.__execute ('Execute', statement, None, False)
	
	def stream (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Stream:
		"""creates a stream using this cursor and using ``*args'' and ``**kwargs'' for Cursor.query()"""
		return Stream (self.query (statement, parameter, cleanup))

class DBType (Enum):
	STRING = 0
	BINARY = 1
	NUMBER = 2
	TIMESTAMP = 3
	ROWID = 4

class Core:
	"""Meta class for database driver

this is the base class for all database specific drivers and should
inherit this class. """
	__slots__ = ['dbms', 'driver', 'cursor_class', 'db', 'lasterr', 'log', 'cursors', 'types']
	def __init__ (self, dbms: str, driver: DBAPI.Vendor, cursor_class: Type[Cursor]) -> None:
		"""``dbms'' is the symbolic name, ``driver'' the real driver class and ``cursor_class'' based on Cursor

for verbose logging you can assing the instance variable ``log'' a
callable which is called in several stages with one argument, a
logging information. This is useful for debugging the database level
but can produce lots of output in prdocutive use."""
		self.dbms = dbms
		self.driver = driver
		self.cursor_class = cursor_class
		self.db: Optional[DBAPI.Driver] = None
		self.lasterr: Optional[Exception] = None
		self.log: Optional[Callable[[str], None]] = None
		self.cursors: List[Cursor] = []
		self.types: Dict[Any, DBType] = {None: DBType.STRING}
		names: List[str]
		for (id, names) in [
			(DBType.BINARY, ['BINARY', 'BLOB', 'LOB']),
			(DBType.NUMBER, ['NUMBER', ]),
			(DBType.TIMESTAMP, ['TIMESTAMP', 'DATETIME', 'DATE', 'TIME']),
			(DBType.ROWID, ['ROWID', ])
		]:
			for name in names:
				if name in driver.__dict__:
					try:
						for t in driver.__dict__[name]:
							self.types[t] = id
					except TypeError:
						self.types[driver.__dict__[name]] = id

	def __enter__ (self) -> Core:
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		if self.db is not None:
			if exc_type is None:
				self.commit ()
			else:
				self.rollback ()
			self.close ()
		return None
	
	def setup (self) -> None:
		"""hook to add driver specific setup code"""
		pass
	
	def typ (self, t: Any) -> DBType:
		"""returns a normalized type of the database specific value type"""
		try:
			return self.types[t]
		except KeyError:
			return self.types[None]

	def error (self, errmsg: Any) -> None:
		"""is called in error condition

this will store the message in lasterr and retrieved using the method
``last_error'' and closes the database connection"""
		self.lasterr = errmsg
		self.close ()

	def qselect (self, **args: str) -> str:
		"""Selects a database sepcific query variant

it is not always possible to write database neutral code. So if you
have to use a database specific query, you can use this method to
automatically select the query required for the currently used
database. The known databases are detected by the used driver and are
normalized to these names:
	- mysql: for MySQL or MariaDB databases
	- oracle: for Oracle RDBMs
	- sqlite: for using a SQLite3 database

e.g. you want to create a new record, but you use a sequence in oracle
and an autoincrement in mysql:
	query = cursor.qselect (
		oracle = 'INSERT INTO some_table (id, content) VALUES (some_sequence.nextval, :content)',
		mysql = 'INSERT INTO some_table (content) VALUES (:content)'
	)
And to get the query to select the newly created ID:
	query = cursor.qselect (
		oracle = 'SELECT some_sequence.currval FROM dual',
		mysql = 'SELECT last_insert_id()'
	)
"""
		return args[self.dbms]

	def repr_last_error (self) -> str:
		"""returns string representation of last occured error"""
		return str (self.lasterr) if self.lasterr is not None else 'success'

	def last_error (self) -> str:
		"""return string representation, if an error has occured, otherwise "success"."""
		if self.lasterr is not None:
			return self.repr_last_error ()
		return 'success'

	def sync (self, commit: bool = True) -> None:
		"""flushes the current transaction

if ``commit'' is True, then the last changes are written back to the
database, otherwise they are discarded (rollback). On databases
without transaction support this may be a NO-OP."""
		if self.db is not None:
			if commit:
				self.db.commit ()
			else:
				self.db.rollback ()

	def commit (self) -> None:
		"""shortcat for self.sync (True)"""
		self.sync (True)

	def rollback (self) -> None:
		"""shortcat for self.sync (False)"""
		self.sync (False)

	def close (self) -> None:
		"""closes database and all created cursors"""
		if self.db is not None:
			for c in self.cursors[:]:
				if self.log is not None and c.id is not None:
					self.log ('Closing pending cursor %s' % c.id)
				with Ignore (self.driver.Error):
					c.close ()
			try:
				self.db.close ()
			except self.driver.Error as e:
				self.lasterr = e
			self.db = None
		self.cursors = []

	def open (self) -> bool:
		"""opens the database and returns True on success, False otherwise"""
		self.close ()
		try:
			self.connect ()
		except self.driver.Error as e:
			self.error (e)
		return self.is_open ()
	
	def reopen (self, commit: bool = True) -> bool:
		if self.db is not None:
			self.sync (commit)
			self.db.close ()
			if not self.open ():
				for cursor in self.cursors:
					with Ignore (self.driver.Error):
						cursor.close ()
			else:
				for cursor in self.cursors:
					cursor.open ()
		return self.is_open ()

	def is_open (self) -> bool:
		"""returns True, if database is open, else False"""
		return self.db is not None

	def get_cursor (self) -> Any:
		"""create a new cursor

this method creates a new cursor. This method should NOT be used by an
application as the cursor will not be kept track of and cannnot be
automatically closed.

Use the method ``cursor'' instead."""
		if self.is_open () or self.open ():
			try:
				if self.db is not None:
					curs = self.db.cursor ()
					if curs is not None:
						with Ignore (AttributeError):
							if curs.arraysize < 100:
								curs.arraysize = 100
						return curs
			except self.driver.Error as err:
				self.error (err)
		return None

	def cursor (self, autocommit: bool = False, id: Optional[str] = None) -> Cursor:
		"""request a new cursor

if ``autocommit'' is on, every change to the database is directly
followed by a commit. ``id'' can be set to some readable values and is
used during logging."""
		if self.is_open () or self.open ():
			c = self.cursor_class (self, autocommit)
			if c is not None:
				c.set_id (id)
				self.cursors.append (c)
				return c
		raise error ('no database connection')

	def release (self, cursor: Cursor) -> None:
		"""releases and closes a cursor

this is internally used by Cursor to ensure that the cursor is
closed and removed from the internal tracking."""
		if cursor in self.cursors:
			self.cursors.remove (cursor)
			if cursor.curs:
				cursor.curs.close ()
				cursor.curs = None
		elif self.log is not None and cursor.id is not None:
			self.log ('Try to release a not managed cursor %s' % cursor.id)

	def stream (self, *args: Any, **kwargs: Any) -> Stream:
		"""creates a stream using a dedicated cursor and using ``*args'' and ``**kwargs'' for Cursor.query()"""
		cursor = self.cursor ()
		return Stream.defer (cursor.query (*args, **kwargs), lambda o: cursor.close ())

	@abstractmethod
	def connect (self) -> None:
		"""Establish a connection to the database"""

class Binary:
	"""Generic wrapper to represent binaries"""
	__slots__ = ['value']
	name = 'binary'
	datatype = DBType.BINARY
	def __init__ (self, value: Any) -> None:
		self.value = value

	@classmethod
	def dump (cls, o: Any) -> Optional[str]:
		if o.value is None:
			return None
		return base64.encodestring (o.value).decode ('us-ascii')
		
	@classmethod
	def load (cls, s: Optional[str]) -> Binary:
		if s is None:
			return cls (None)
		try:
			return cls (base64.decodestring (s.encode ('us-ascii')))
		except Exception:
			return cls (s)

