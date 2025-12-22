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
import	logging, csv
from	collections import defaultdict
from	datetime import datetime, timedelta
from	types import TracebackType
from	typing import Any, Callable, Iterable, Literal, Optional, Union
from	typing import Dict, IO, List, NamedTuple, Set, Tuple, Type
from	typing import cast, overload
from	.dbdriver import DBDriver
from	.dbcore import _T, Row, Core, Cursor, Description
from	.definitions import epoch, program
from	.exceptions import error
from	.ignore import Ignore
from	.io import copen, csv_default, csv_writer
from	.parser import parse_timestamp, unit
from	.stream import Stream
#
__all__ = ['Row', 'DBIgnore', 'DB', 'TempDB', 'Cursor']
#
logger = logging.getLogger (__name__)
#
class DBIgnore (Ignore):
	def __init__ (self, loglevel: int = logging.WARNING, logexception: bool = True) -> None:
		super ().__init__ (error, loglevel = loglevel, logexception = logexception)

class DB:
	"""Higher level database abstraction class

This class is used to wrap a database driver and its connections to
one class which provides some convenient methods for dtabase
handling."""
	__slots__ = [
		'dbid', 'db', 'cursor',
		'_last_error', 
		'_table_cache', '_view_cache', '_synonym_cache', '_index_cache', '_tablespace_cache',
		'_scratch_tables', '_scratch_number',
		'_cache', '_cached_description', '_checkpoints'
	]
	@staticmethod
	def quote (s: str) -> str:
		return s.replace ('\'', '\'\'')
		
	def __init__ (self, dbid: Optional[str] = None) -> None:
		"""``dbid'' is the database id to create a new driver instance"""
		self.dbid = dbid
		self.db: Optional[Core] = None
		self.cursor: Optional[Cursor] = None
		self._last_error: Optional[str] = None
		self._table_cache: Dict[str, bool] = {}
		self._view_cache: Dict[str, bool] = {}
		self._synonym_cache: Dict[str, bool] = {}
		self._index_cache: Dict[Tuple[str, str], bool] = {}
		self._tablespace_cache: Dict[Optional[str], Optional[str]] = {}
		self._scratch_tables: List[str] = []
		self._scratch_number = 0
		self._cache = None
		self._cached_description: Optional[List[Tuple[Any, ...]]] = None
		self._checkpoints: Optional[Dict[Tuple[str, str], DB.Checkpoint]] = None
		
	def __del__ (self) -> None:
		self.close ()
		
	def __enter__ (self) -> DB:
		self.check_open ()
		return self

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		self.close (exc_type is None)
		return None

	def close (self, commit: bool = True) -> None:
		"""Close the database driver if open, closing (and commiting changes, if ``commit'' is True) all open cursors and checkpoints, remove temp. tables"""
		if self._checkpoints is not None:
			for cp in self._checkpoints.values ():
				cp.done (commit)
			self._checkpoints = None
		if self.cursor is not None:
			while self._scratch_tables:
				table = self._scratch_tables.pop ()
				self.cursor.execute (f'TRUNCATE TABLE {table}')
				self.cursor.execute (f'DROP TABLE {table}')
		if self.db is not None:
			if self.cursor is not None:
				self.cursor.sync (commit)
				self.cursor.close ()
			self.db.close ()
			self._last_error = self.db.last_error ()
		self.db = None
		self.cursor = None
	
	def logging (self, log: Optional[Callable[[str], None]]) -> Core.LogState:
		return self.check_open ().logging (log)
		
	def reset (self) -> None:
		"""Resets (rollbacks) changes done"""
		if self.cursor is not None:
			self.cursor.sync (False)
		
	def new (self) -> Core:
		"""Creates a new database driver instance"""
		try:
			db = DBDriver.request (self.dbid)
		except IOError as e:
			raise error (f'{self.dbid}: failed to find valid driver: {e}') from e
		except KeyError as e:
			raise error (f'{self.dbid}: failed to find proper configuration: {e}') from e
		return db
		
	def open (self) -> bool:
		"""Opens (closes a currently open) database driver and sets up the standard cursor"""
		self.close ()
		self.db = self.new ()
		if self.db is not None:
			self.cursor = self.db.cursor ()
			if self.cursor is None:
				self.close ()
		self._tablespace_cache.clear ()
		return self.cursor is not None

	def isopen (self) -> bool:
		"""Checks if database is available and tries to open it, if not"""
		if self.db is None or self.cursor is None:
			self.open ()
		return self.cursor is not None
	
	def check_open (self) -> Core:
		if not self.isopen ():
			raise error ('database not open: {last_error}'.format (last_error = self.last_error ()))
		return cast (Core, self.db)
		
	def check_open_cursor (self) -> Cursor:
		self.check_open ()
		return cast (Cursor, self.cursor)
	
	def request (self) -> Cursor:
		"""Requests a new cursor"""
		return self.check_open ().cursor ()
		
	def release (self, cursor: Cursor, commit: bool = True) -> None:
		"""Releases a cursor, if ``commit'' is True commits changes"""
		if cursor is not None and cursor is not self.cursor:
			cursor.sync (commit)
			cursor.close ()
		
	def sync (self, commit: bool = True) -> None:
		"""If ``commit'' is true, changes are commited, else rollbacked"""
		if self.cursor is not None:
			self.cursor.sync (commit)
		
	def last_error (self) -> str:
		"""Returns last database error message"""
		if self.db is not None:
			return self.db.last_error ()
		return self._last_error if self._last_error else f'No active database interface ({self.dbid})'
	
	def qselect (self, **args: _T) -> _T:
		if self.db is not None:
			return self.db.qselect (**args)
		raise error ('database not open: {last_error}'.format (last_error = self.last_error ()))
	
	def qvalidate (self, statement: str, parameter: Dict[str, Any]) -> Tuple[bool, List[str], List[str]]:
		if self.db is not None:
			return self.db.qvalidate (statement, parameter)
		raise error ('database not open: {last_error}'.format (last_error = self.last_error ()))

	@overload
	def description (self, normalize: Literal[False] = ...) -> List[Tuple[Any, ...]]: ...
	@overload
	def description (self, normalize: Literal[True]) -> List[Description]: ...
	@overload
	def description (self, normalize: bool = False) -> Union[List[Description], List[Tuple[Any, ...]]]: ...
	def description (self, normalize: bool = False) -> Union[List[Description], List[Tuple[Any, ...]]]:
		if self._cache is not None:
			if self._cached_description and self.db is not None:
				return Description.normalize (self.db, self._cached_description) if normalize else self._cached_description
		elif self.cursor is not None:
			return self.cursor.description (normalize)
		raise error ('database not open: {last_error}'.format (last_error = self.last_error ()))
	
	@overload
	def layout (self, table: str, normalize: Literal[False] = ...) -> List[Tuple[Any, ...]]: ...
	@overload
	def layout (self, table: str, normalize: Literal[True]) -> List[Description]: ...
	@overload
	def layout (self, table: str, normalize: bool = False) -> Union[List[Description], List[Tuple[Any, ...]]]: ...
	def layout (self, table: str, normalize: bool = False) -> Union[List[Description], List[Tuple[Any, ...]]]:
		with self.request () as cursor:
			cursor.querys (f'SELECT * FROM {table} WHERE 1 = 0')
			return cursor.description (normalize)

	def setup_table_optimizer (self, table: str, estimate_percent: int = 30) -> None:
		"""setup a newly created table according to the dbms requirements"""
		self.check_open ().setup_table_optimizer (table, estimate_percent)

	@property
	def dbms (self) -> Optional[str]:
		return self.db.dbms if self.db else None


	def query (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Iterable[Row]:
		"""Execute a query try to resolve the query from the cache, if caching is enabled"""
		if self._cache is not None:
			key = (statement, parameter, cleanup)
			try:
				rc = self._cache[key]
			except KeyError:
				cursor = self.check_open_cursor ()
				rows = cursor.queryc (statement, parameter, cleanup)
				self._cache[key] = rc = (cursor.description (), rows)
			self._cached_description = rc[0]
			return rc[1]
		#
		return self.check_open_cursor ().query (statement, parameter, cleanup)

	def queryc (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Iterable[Row]:
		"""Execute a cached query try to resolve the query from the cache, if caching is enabled"""
		if self._cache is not None:
			return self.query (statement, parameter, cleanup)
		#
		return self.check_open_cursor ().queryc (statement, parameter, cleanup)
		
	def querys (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Optional[Row]:
		"""Execute a single row query try to resolve the query from the cache, if caching is enabled"""
		if self._cache is not None:
			for rec in self.query (statement, parameter, cleanup):
				return rec
		#
		return self.check_open_cursor ().querys (statement, parameter, cleanup)
			
	def update (self,
		statement: str,
		parameter: Union[None, List[Any], Dict[str, Any]] = None,
		commit: bool = False,
		cleanup: bool = False,
		sync_and_retry: bool = False,
		callback_between: Optional[Callable[[], Any]] = None
	) -> int:
		"""Update database content, a convenient method to complete the minimal cursor interface"""
		return self.check_open_cursor ().update (statement, parameter, commit, cleanup, sync_and_retry, callback_between)
	
	def execute (self, statement: str) -> int:
		"""Excecute a statement on the database"""
		return self.check_open_cursor ().execute (statement)

	def stream (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Stream[Row]:
		if self._cache is not None:
			return Stream (self.query (statement, parameter, cleanup))
		return self.check_open ().stream (statement, parameter, cleanup)
	
	def streamc (self, statement: str, parameter: Union[None, List[Any], Dict[str, Any]] = None, cleanup: bool = False) -> Stream[Row]:
		if self._cache is not None:
			return Stream (self.queryc (statement, parameter, cleanup))
		return self.check_open ().streamc (statement, parameter, cleanup)

	def exists_table (self, table_name: str, cachable: bool = False) -> bool:
		"""Check database if ``table_name'' exists as table, if ``cachable'' is True, cache the result for faster future access"""
		if cachable:
			cache_table_name = table_name.lower ()
			if cache_table_name in self._table_cache:
				return self._table_cache[cache_table_name]
		#
		rc = False
		cursor = self.check_open_cursor ()
		query = cursor.qselect (
			oracle = 'SELECT count(*) FROM user_tables WHERE lower(table_name) = lower(:table_name)',
			mysql = 'SELECT count(*) FROM information_schema.tables WHERE lower(table_name) = lower(:table_name) AND table_schema=(SELECT SCHEMA())',
			sqlite = 'SELECT count(*) FROM sqlite_master WHERE lower(name) = lower(:table_name) AND type = \'table\''
		)
		rq = cursor.querys (query, {'table_name': table_name})
		if rq is not None:
			rc = rq[0] > 0
			if cachable:
				self._table_cache[cache_table_name] = rc
		return rc
		
	def exists_view (self, view_name: str, cachable: bool = False) -> bool:
		"""Check database if ``view_name'' exists as view, if ``cachable'' is True, cache the result for faster future access"""
		if cachable:
			cache_view_name = view_name.lower ()
			if cache_view_name in self._view_cache:
				return self._view_cache[cache_view_name]
		#
		rc = False
		cursor = self.check_open_cursor ()
		query = cursor.qselect (
			oracle = 'SELECT count(*) FROM user_views WHERE lower(view_name) = lower(:view_name)',
			mysql = 'SELECT count(*) FROM information_schema.views WHERE lower(table_name) = lower(:view_name) AND table_schema=(SELECT SCHEMA())',
			sqlite = 'SELECT count(*) FROM sqlite_master WHERE lower(name) = lower(:view_name) AND type = \'view\''
		)
		rq = cursor.querys (query, {'view_name': view_name})
		if rq is not None:
			rc = rq[0] > 0
			if cachable:
				self._view_cache[cache_view_name] = rc
		return rc
	
	def exists_synonym (self, synonym_name: str, cachable: bool = False) -> bool:
		"""Check database if ``synonym_name'' exists as synonym, if ``cachable'' is True, cache the result for faster future access"""
		if self.dbms == 'oracle':
			if cachable:
				cache_synonym_name = synonym_name.lower ()
				if cache_synonym_name in self._synonym_cache:
					return self._synonym_cache[cache_synonym_name]
			#
			rc = False
			cursor = self.check_open_cursor ()
			query = 'SELECT count(*) FROM user_synonyms WHERE lower(synonym_name) = lower(:synonym_name)'
			rq = cursor.querys (query, {'synonym_name': synonym_name})
			if rq is not None:
				rc = rq[0] > 0
				if cachable:
					self._synonym_cache[cache_synonym_name] = rc
			return rc
		return False
	
	def exists (self, table_name: str, cachable: bool = False) -> bool:
		return self.exists_table (table_name, cachable) or self.exists_view (table_name, cachable) or self.exists_synonym (table_name, cachable)

	def exists_index (self, table_name: str, index_name: str, cachable: bool = False) -> bool:
		"""Check database if ``index_name' index exists for ``table_name'', if ``cachable'' is True, cache the result for faster future access"""
		if cachable:
			cache_index_key = (table_name.lower (), index_name.lower ())
			if cache_index_key in self._index_cache:
				return self._index_cache[cache_index_key]
		#
		rc = False
		cursor = self.check_open_cursor ()
		query = cursor.qselect (
			oracle = 'SELECT count(*) FROM user_indexes WHERE lower(table_name) = lower(:table_name) AND lower(index_name) = lower(:index_name)',
			mysql = 'SELECT count(*) FROM information_schema.statistics WHERE lower(table_name) = lower(:table_name) AND lower(index_name) = lower(:index_name) AND table_schema=(SELECT SCHEMA())',
			sqlite = 'SELECT count(*) FROM sqlite_master WHERE lower(tbl_name) = lower(:table_name) AND lower(name) = lower(:index_name) AND type = \'index\''
		)
		rq = cursor.querys (query, {'table_name': table_name, 'index_name': index_name})
		if rq is not None:
			rc = rq[0] > 0
			if cachable:
				self._index_cache[cache_index_key] = rc
		return rc
	
	def find_tablespace (self, tablespace: Optional[str], *args: str) -> Optional[str]:
		"""Oracle only: find a proper tablespace by name, use default tablespace, if ``tablespace'' does not exists"""
		try:
			return self._tablespace_cache[tablespace]
		except KeyError:
			self.check_open ()
			rc: Optional[str] = None
			if self.dbms == 'oracle':
				cursor = self.request ()
				try:
					if tablespace is not None:
						for ts in [tablespace] + list (args):
							query = 'SELECT tablespace_name FROM user_tablespaces WHERE lower(tablespace_name) = lower(:tablespace_name)'
							data = {'tablespace_name': ts}
							rq = cursor.querys (query, data)
							if rq is not None and rq.tablespace_name is not None:
								rc = rq.tablespace_name
								break
					if rc is None:
						query = 'SELECT default_tablespace FROM user_users'
						for row in cursor.query (query):
							if row.default_tablespace is not None:
								rc = row.default_tablespace
								break
				finally:
						self.release (cursor)
			self._tablespace_cache[tablespace] = rc
			return rc
	
	def tablespace (self, tablespace: Optional[str], *args: str) -> str:
		ts = self.find_tablespace (tablespace, *args) if self.db and self.db.has_tablespaces else None
		return f' TABLESPACE {ts}' if ts else ''
		
	class Column (NamedTuple):
		name: str
		datatype: str
		comment: None | str
		
	class Index (NamedTuple):
		name: str
		columns: Union[str, List[str]]
		tablespace: Optional[str] = None
	
	def get_layout (self) -> dict[str, list[Column]]:
		rc: defaultdict[str, list[DB.Column]] = defaultdict (list)
		with self.request () as cursor:
			match self.dbms:
				case 'oracle':
					comments: dict[str, str] = cursor.stream (
						'SELECT table_name, column_name, comments '
						'FROM user_col_comments'
					).filter (lambda r: bool (r.comments)).map (lambda r: (f'{r.table_name.lower ()}.{r.column_name.lower ()}', r.comments)).dict ()
					#
					def oracle_mkcolumn (table: str, name: str, typ: str, length: None | int, precision: None | int, nullable: bool, default: None | str) -> DB.Column:
						match typ:
							case 'VARCHAR2' if length:
								typ += f'({length})'
							case 'NUMBER' if length and precision:
								typ += f'({length},{precision})'
							case 'NUMBER' if length:
								typ += f'({length})'
						if not nullable:
							typ += ' NOT NULL'
						if default:
							typ += f' DEFAULT {default}'
						return DB.Column (
							name = name,
							datatype = typ,
							comment = comments.get (f'{table}.{name}')
						)
					#
					cursor.stream (
						'SELECT table_name, column_name, data_type, data_length, data_precision, nullable, data_default '
						'FROM user_tab_columns'
					).each (lambda r: rc[r.table_name.lower ()].append (oracle_mkcolumn (r.table_name.lower (), r.column_name.lower (), r.data_type, r.data_length, r.data_precision, r.nullable == 'Y', r.data_default)))
				case 'mariadb':
					def mariadb_mkcolumn (name: str, typ: str, nullable: bool, default: None | str, comment: None | str) -> DB.Column:
						if not nullable:
							typ += ' NOT NULL'
						if default:
							typ += f' DEFAULT {default}'
						return DB.Column (
							name = name,
							datatype = typ,
							comment = comment)
					#
					cursor.stream (
						'SELECT table_name, column_name, column_type, column_default, is_nullable, column_comment '
						'FROM information_schema.columns WHERE table_schema=(SELECT SCHEMA())'
					).each (lambda r: rc[r.table_name.lower ()].append (mariadb_mkcolumn (r.column_name.lower (), r.column_type, r.is_nullable == 'YES', r.column_default, r.column_comment)))
		return dict (rc)
	
	def update_layout (self,
		table: str,
		*,
		columns: Optional[List[Column]] = None,
		indexes: Optional[List[Index]] = None
	) -> None:
		cursor = self.check_open_cursor ()
		if columns:
			layout = self.layout (table, normalize = True)
			available: Set[str] = {_l.name for _l in layout} if layout else set ()
			columns = Stream (columns).filter (lambda c: c.name.lower () not in available).list ()
			if columns:
				def comment (column: DB.Column) -> str:
					if self.dbms == 'mariadb' and column.comment:
						return ' COMMENT \'{comment}\''.format (
							comment = self.quote (column.comment)
						)
					return ''
				cursor.execute ('ALTER TABLE {table} ADD ({columns})'.format (
					table = table,
					columns = (Stream (columns)
						.map (lambda c: f'{c.name} {c.datatype}{comment (c)}')
						.join (', ')
					)
				))
				if self.dbms == 'oracle':
					for column in columns:
						if column.comment:
							cursor.execute (
								'COMMENT ON COLUMN {table}.{column} IS \'{comment}\''.format (
									table = table,
									column = column.name,
									comment = self.quote (column.comment)
								)
							)
		if indexes:
			available = cursor.streamc (
				cursor.qselect (
					oracle = 'SELECT index_name FROM user_indexes WHERE lower(table_name) = lower(:table)',
					mysql = 'SELECT index_name FROM information_schema.statistics WHERE lower(table_name) = lower(:table) AND table_schema=(SELECT SCHEMA())',
					sqlite = 'SELECT name AS index_name FROM sqlite_master WHERE lower(tbl_name) = lower(:table) AND type = \'index\''
				), {
					'table': table
				}
			).map_to (str, lambda r: r.index_name.lower ()).set ()
			for index in indexes:
				if index.name.lower () not in available:
					cursor.execute ('CREATE INDEX {name} ON {table} ({columns}){tablespace}'.format (
						name = index.name,
						table = table,
						columns = index.columns if isinstance (index.columns, str) else ', '.join (index.columns),
						tablespace = self.tablespace (index.tablespace)
					))

	def scratch_request (self,
		name: Optional[str] = None,
		layout: Optional[str] = None,
		indexes: Optional[List[str]] = None,
		select: Optional[str] = None,
		tablespace: Optional[str] = None,
		unique: Optional[str] = None,
		reuse: bool = False
	) -> Optional[str]:
		"""Request (and create) a scratch table for temporary data

``name'' will become part of the table name of the scratch table, so
keep it short. ``layout'' is the layout of the table as one string
where the columns are comma separated. ``indexes'' can be None or a
list of strings with columns to create as index on the scratch table,
``select'' is a select statement to fill the scratch table.
``tablespace'' is the the tablespace where the table should be created
in (Oracle ony), ``unique'' a host unique ID to avoid clashes in an
active/active environment and if ``reuse'' is True, then an existing
table is reused by this process, otherwise it will scan for a non
existing one."""
		if name is None:
			name = program
		cursor = self.check_open_cursor ()
		while True:
			self._scratch_number += 1
			scratch_unique = str (self._scratch_number) if unique is None else f'{unique}{self._scratch_number}'
			table = f'TMP_SCRATCH_{name}_{scratch_unique}_TBL'
			exists = self.exists (table)
			if exists and reuse:
				cursor.execute (f'TRUNCATE TABLE {table}')
				cursor.execute (f'DROP TABLE {table}')
				exists = self.exists (table)
			if not exists:
				if layout or select:
					query = f'CREATE TABLE {table}'
					if layout:
						query += f' ({layout})'
					query += self.tablespace (tablespace)
					if select:
						query += f' AS SELECT {select}'
					try:
						cursor.execute (query)
					except error as e:
						if not self.exists (table):
							raise
						logger.debug (f'{table}: try create already existing table, ignored error condition {e}')
					else:
						if indexes is not None:
							for (index_number, index) in enumerate (indexes, start = 1):
								iname = 'TS${name}${scratch_unique}${index_number}$IDX'
								query = f'CREATE INDEX {iname} ON {table} ({index})' + self.tablespace (tablespace)
								cursor.execute (query)
						self.setup_table_optimizer (table)
				self._scratch_tables.append (table)
				break
		return table
		
	def scratch_release (self, table: str) -> bool:
		"""Release (and removes) a scratch table"""
		rc = False
		cursor = self.check_open_cursor ()
		while table in self._scratch_tables:
			if not rc:
				if self.exists (table):
					cursor.execute (f'TRUNCATE TABLE {table}')
					cursor.execute (f'DROP TABLE {table}')
				rc = True
			self._scratch_tables.remove (table)
		return rc

	def export (self,
		stream: Union[str, Callable[[int], str]],
		query: str,
		data: Optional[Dict[str, Any]] = None,
		ignore: Optional[List[str]] = None,
		dialect: Optional[str] = None,
		conversion: Optional[Dict[Any, Callable[[str, Any], Any]]] = None,
		checkpoint: Optional[int] = None,
		checkpoint_log: Optional[Callable[[int], None]] = None,
		modifier: Optional[Callable[[Row], Row]] = None,
		validator: Optional[Callable[[Row], bool]] = None,
		limit: Optional[int] = None
	) -> Tuple[bool, str]:
		"""exports a query to one or more csv files

``stream'' is either a filename or a callable which creates a filename
(only if ``limit'' is used). ``query'' is the query to be executed
using ``data'' as the named parameter. ``ignore'' is a list of columns
not to output to the file. ``dialect'' is the csv dialect to use for
the output. ``conversion'' is a optional dict to convert input data to
output strings. ``checkpoint'' is a numeric value after which a
checkpoint should be executed which means a commit is performed to
release database resources and the optional method ``checkpoint_log''
is called to provide logging which is called with a parameter, the
current number of records written. ``modifier'' is called for each row
to allow further modifications before the data is written to the file.
If ``validator'' is not None it will be called for every row. If this
method returns False, then this row will not be written to the file.
``limit'' is the number of rows per file, this allows to create more
files to limit the size of each file."""
		rc = False
		msg = []
		self.check_open ()
		cmap = conversion.copy () if conversion is not None else {}
		driver = cast (Core, self.db).driver
		for (typ, convert) in [
			(None, lambda h, v: str (v) if v is not None else v),
			(driver.DATETIME, lambda h, v: v.isoformat () if v is not None else v)
		]:
			if typ not in cmap:
				cmap[typ] = convert
		if checkpoint is None:
			checkpoint = 10000
		def handle_checkpoint (count: int) -> None:
			self.sync ()
			if checkpoint_log is not None and callable (checkpoint_log):
				checkpoint_log (count)
		fd: Optional[IO[str]] = None
		wr: Optional[csv_writer] = None
		written = 0
		try:
			fileno = 0
			header: List[str] = []
			converter: List[Callable[[str, Any], Any]] = []
			reducer: Callable[[Any], Any] = lambda a: a
			handle_checkpoint (0)
			for (count, row) in enumerate (cast (Cursor, self.cursor).query (query, data), start = 1):
				if count % checkpoint == 0:
					handle_checkpoint (count)
				if count == 1:
					desc = cast (Cursor, self.cursor).description ()
					if desc is not None:
						if ignore is not None:
							ign = [_d[0].lower () not in ignore for _d in desc]
							reducer = lambda a: [_f[1] for _f in filter (lambda b: b[0], [_l for _l in zip (ign, a)])]
						header = reducer ([_h[0] for _h in desc])
						for h in reducer (desc):
							try:
								converter.append (cmap[h[1]])
							except KeyError:
								converter.append (cmap[None])
								msg.append ('No converter for type {typ!r} for {column} found'.format (
									typ = h[1],
									column = h[0]
								))
				if validator is not None and not validator (row):
					continue
				if modifier is not None:
					row = modifier (row)
				if wr is not None and limit is not None and limit > 0 and fd is not None and written > limit:
					fd.close ()
					wr = None
					fd = None
				if wr is None:
					if limit is not None:
						fileno += 1
						if callable (stream):
							filename = stream (fileno)
						else:
							filename = stream % fileno
					else:
						filename = cast (str, stream)
					fd = copen (filename, 'w')
					wr = csv.writer (fd, dialect = dialect if dialect else csv_default)
					written = wr.writerow (header)
				written += wr.writerow ([_c (_h, _v) for (_c, _h, _v) in zip (converter, header, reducer (row))])
			if count % checkpoint != 0:
				handle_checkpoint (count)
			rc = True
		finally:
			if fd is not None:
				fd.close ()
		return (rc, ', '.join (msg))

	class Checkpoint:
		__slots__ = ['db', 'name', 'table', 'cursor', 'start', 'end', 'persist', 'phash', 'chash', 'finalized']
		def __init__ (self, db: DB, name: str, table: str, cursor: Cursor, start: datetime, end: datetime) -> None:
			self.db = db
			self.name = name
			self.table = table
			self.cursor = cursor
			self.start = start
			self.end = end
			self.persist: Optional[str] = None
			self.phash = hash (self.persist)
			self.chash = hash (None)
			self.finalized = False
			
		def __del__ (self) -> None:
			self.done (False)
		
		def __enter__ (self) -> DB.Checkpoint:
			return self
			
		def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
			self.done (exc_type is None)
			return None

		def setup (self) -> None:
			query = f'SELECT checkpoint, persist FROM {self.table} WHERE name = :name'
			rq = self.cursor.querys (query, {'name': self.name})
			if rq is not None and rq.checkpoint is not None:
				self.start = rq.checkpoint
				self.persist = str (rq.persist) if rq.persist is not None else None
				self.phash = hash (self.persist)
				self.chash = hash (self.start)
			
		def done (self, commit: bool = True) -> None:
			if not self.finalized:
				self.finalized = True
				if commit:
					self.save ()
				self.db.release (self.cursor)
				self.db.uncheckpoint (self)
		
		def save (self) -> None:
			insert = False
			data = {
				'name': self.name,
				'checkpoint': self.end,
				'persist': self.persist
			}
			phash = hash (self.persist)
			chash = hash (self.end)
			if chash != self.chash or phash != self.phash:
				query = f'UPDATE {self.table} SET checkpoint = :checkpoint, persist = :persist WHERE name = :name'
				if self.db.dbms == 'oracle':
					self.cursor.set_input_sizes (persist = cast (Core, self.db.db).driver.CLOB)
				if self.cursor.update (query, data) == 0:
					insert = True
			else:
				query = f'SELECT count(*) FROM {self.table} WHERE name = :name'
				rq = self.cursor.querys (query, data, cleanup = True)
				if rq is not None and rq[0] == 0:
					insert = True
			if insert:
				query = f'INSERT INTO {self.table} (name, checkpoint, persist) VALUES (:name, :checkpoint, :persist)'
				if self.db.dbms == 'oracle':
					self.cursor.set_input_sizes (persist = cast (Core, self.db.db).driver.CLOB)
				if self.cursor.update (query, data) == 0:
					raise error (f'failed to create new entry for {self.name} in {self.table}')
			self.phash = phash
			self.chash = chash
			self.cursor.sync (True)
		
	def uncheckpoint (self, cp: DB.Checkpoint) -> None:
		"""Releases a checkpoint"""
		if self._checkpoints and self._checkpoints.get ((cp.table, cp.name)) is cp:
			del self._checkpoints[(cp.table, cp.name)]

	def checkpoint (self,
		table: str,
		name: str,
		start_expr: Union[None, str, datetime] = None,
		end_expr: Union[None, str, datetime] = None,
		fresh: bool = False,
		tablespace: Optional[str] = None,
		endoffset: Union[None, str, int, float] = None
	) -> DB.Checkpoint:
		"""Requests a checkpoint, create it, if not existing

A checkpoint instance is used to limit a query to create delta results
since last time queried. ``table'' is the table to store the last
checkpoint. If table does not exists, it is created using
``tablespace'', if not None (Oracle only). ``name'' is the name for
this checkpoint, ``start_expr'' is the start timestamp if the checkpoint is
new (1970-01-01 by default), ``end_expr'' is the end for this delta
(default is now). If ``fresh'' is True then the checkpoint behaves as
if it had not existed."""
		if self._checkpoints and (cp := self._checkpoints.get ((table, name))) is not None:
			return cp
		#
		self.check_open ()
		with self.request () as cursor:
			if not self.exists (table):
				layout = cursor.qselect (
					oracle = (
						f'CREATE TABLE {table} (\n'
						'	name		varchar2(100)	PRIMARY KEY NOT NULL,\n'
						'	checkpoint	date		NOT NULL,\n'
						'       persist         clob\n'
						')' + self.tablespace (tablespace)
					), mysql = (
						f'CREATE TABLE {table} (\n'
						'	name		varchar(100)	PRIMARY KEY NOT NULL,\n'
						'	checkpoint	timestamp	NOT NULL,\n'
						'       persist         longtext\n'
						')'
					), sqlite = (
						f'CREATE TABLE {table} (\n'
						'	name		text		PRIMARY KEY NOT NULL,\n'
						'	checkpoint	timestamp	NOT NULL,\n'
						'       persist         text\n'
						')'
					)
				)
				cursor.execute (layout)
			else:
				cursor.querys (f'SELECT * FROM {table} WHERE 1 = 0')
				desc = cursor.description (normalize = True)
				if desc is not None and 'persist' not in [_d[0] for _d in desc]:
					cursor.execute (cursor.qselect (
						oracle = f'ALTER TABLE {table} ADD persist clob',
						mysql = f'ALTER TABLE {table} ADD persist longtext',
						sqlite = f'ALTER TABLE {table} ADD persist text'
					))
			if self.exists (table) and name is not None:
				start = parse_timestamp (start_expr)
				if start is None:
					start = epoch
				end = parse_timestamp (end_expr)
				if end is None:
					now = datetime.now ()
					end = datetime (now.year, now.month, now.day, now.hour, now.minute, now.second)
					if endoffset is not None:
						offset: int
						if isinstance (endoffset, str):
							parsed = unit.parse (endoffset, -1)
							if parsed == -1:
								raise error (f'{endoffset}: invalid offset expression')
							offset = parsed
						elif isinstance (endoffset, float):
							offset = int (endoffset)
						elif isinstance (endoffset, int):
							offset = endoffset
						else:
							raise error ('{endoffset!r}: invalid offset type {typ}'.format (
								endoffset = endoffset,
								typ = type (endoffset)
							))
						if offset != 0:
							end -= timedelta (seconds = offset)
				rc = DB.Checkpoint (self, name, table, cursor, start, end)
				if not fresh:
					rc.setup ()
				if self._checkpoints is None:
					self._checkpoints = {}
				self._checkpoints[(table, name)] = rc
				return rc
		#
		raise error ('failed to create checkpoint: {last_error}'.format (last_error = self.last_error ()))

class TempDB:
	__slots__ = ['db', 'use']
	def __init__ (self, db: Optional[DB]) -> None:
		self.db = db
		self.use: DB = db if db is not None else DB ()
		
	def __enter__ (self) -> DB:
		if self.use.isopen ():
			return self.use
		if self.db is not None:
			self.db = None
			self.use = DB ()
			if self.use.isopen ():
				return self.use
		raise error ('failed to open database: {error}'.format (error = self.use.last_error ()))

	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		if self.use != self.db:
			self.use.close ()
		return None
		
