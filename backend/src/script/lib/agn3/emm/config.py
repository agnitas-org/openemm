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
import	logging, time
from	collections import defaultdict
from	dataclasses import dataclass, field
from	datetime import datetime
from	types import TracebackType
from	typing import Any, Callable, Iterable, Optional, Sequence, Union
from	typing import DefaultDict, Dict, Iterator, List, NamedTuple, Set, Tuple, Type
from	typing import cast, overload
from	..db import DB, TempDB
from	..definitions import syscfg, licence
from	..ignore import Ignore
from	..parser import Parsable, unit
from	..stream import Stream
from	..systemconfig import Systemconfig
from	..tools import atob, listsplit, listjoin
#
__all__ = ['EMMConfig', 'EMMCompany', 'EMMMailerset', 'Responsibility']
#
logger = logging.getLogger (__name__)
#
class _Config:
	__slots__ = ['db', 'reread', 'last', 'selection']
	sentinel = object ()
	def __init__ (self, db: Optional[DB] = None, reread: Parsable = None, selection: Optional[Systemconfig.Selection] = None) -> None:
		self.db = db
		self.reread: Optional[int] = int (unit.parse (reread)) if reread is not None else None
		self.selection: Systemconfig.Selection = selection if selection is not None else syscfg.selection ()
		self.last: Union[None, int, float] = None
	
	def check (self, force: bool = False) -> bool:
		now = time.time ()
		if force or self.last is None or (self.reread is not None and now >= self.last + self.reread):
			with TempDB (self.db) as db:
				self.retrieve (db)
			self.last = now
			return True
		return False
	
	def normalize_hostname (self, hostname: Optional[str]) -> Optional[str]:
		if hostname is not None:
			hostname = hostname.strip ()
			if hostname:
				return hostname.lower ()
		return None
	
	def keylistclause (self, keys: Sequence[str], data: Dict[str, str]) -> str:
		for (nr, key) in enumerate (keys, start = 1):
			data[f'key{nr}'] = key
		if nr == 1:
			return '= :key1'
		return 'IN ({keylist})'.format (keylist = Stream (range (1, nr + 1)).map (lambda n: f':key{n}').join (', '))
	
	def retrieve (self, db: DB) -> None:
		pass

class EMMConfig (_Config):
	"""Read/write config_tbl

read and write configuration from/to the config_tbl as a separate
class to enable a more granular access, if only one configuration
source is accessed.

>>> from agn3.dblite import DBLite, Layout
>>> db = DBLite (layout = [Layout (statement = 'create table config_tbl (class text, name text, value text, hostname text)')])
>>> db.update ('INSERT INTO config_tbl VALUES (\\'class\\', \\'name\\', \\'value\\', NULL)')
1
>>> db.sync ()
>>> ec = EMMConfig (db = db, reread = '5m')
>>> ec.get ('class', 'name')
'value'
>>> db.update ('UPDATE config_tbl SET value = \\'new value\\'')
1
>>> db.sync ()
>>> ec.get ('class', 'name')
'value'
>>> ec.check ()
False
>>> ec.get ('class', 'name')
'value'
>>> ec.check (force = True)
True
>>> ec.get ('class', 'name')
'new value'
"""


	__slots__ = ['class_names', 'config']
	class Value (NamedTuple):
		class_name: str
		name: str
		value: str

	def __init__ (self, db: Optional[DB] = None, reread: Parsable = None, selection: Optional[Systemconfig.Selection] = None, class_names: Optional[Sequence[str]] = None) -> None:
		super ().__init__ (db, reread, selection)
		self.class_names = class_names
		self.config: Dict[Tuple[str, str], str] = {}

	def __enter__ (self) -> EMMConfig:
		return self
		
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None

	def retrieve (self, db: DB) -> None:
		self.config.clear ()
		collect: DefaultDict[Tuple[str, str], Dict[Optional[str], str]] = defaultdict (dict)
		query = (
			'SELECT class AS class_name, name, value, hostname '
			'FROM config_tbl'
		)
		data: Optional[Dict[str, str]] = None
		if self.class_names:
			data = {}
			query += ' WHERE class {clause}'.format (clause = self.keylistclause (self.class_names, data))
		with db.request () as cursor:
			for row in cursor.query (query, data):
				if row.class_name is not None and row.name is not None and row.value is not None:
					key = (row.class_name, row.name)
					collect[key][self.normalize_hostname (row.hostname)] = row.value
					for (key, value) in collect.items ():
						with Ignore (KeyError):
							self.config[key] = self.selection.pick (value)

	@overload
	def get (self, class_name: str, name: str, default: str) -> str: ...
	@overload
	def get (self, class_name: str, name: str, default: None) -> Optional[str]: ...
	@overload
	def get (self, class_name: str, name: str, default: Any = ...) -> Any: ...
	def get (self, class_name: str, name: str, default: Any = _Config.sentinel) -> Any:
		"""retrieve an entry from the config_tbl using
``class_name'' for the configuration class and ``name'' for the name of
the configuration. If ``default'' is not None, this value will be
returned, if no configuration had been found, otherwise a KeyError is
raised.
"""
		self.check ()
		with Ignore (KeyError):
			return self.config[(class_name, name)]
		#
		if default is not _Config.sentinel:
			return default
		#
		raise KeyError ((class_name, name))

	def iget (self, class_name: str, name: str, default: int) -> int:
		try:
			return int (self.config[(class_name, name)])
		except (KeyError, ValueError):
			return default
	
	def fget (self, class_name: str, name: str, default: float) -> float:
		try:
			return float (self.config[(class_name, name)])
		except (KeyError, ValueError):
			return default
	
	def scan (self, class_names: Optional[Sequence[str]] = None, single_value: bool = False) -> Iterator[EMMConfig.Value]:
		"""Scans the config_tbl, if ``class_names'' is not
None it is used as an iterable or a str to limit the scan to these
configuration classes. If ``single_value'' is ``False'', then the
value is split off by comma. """
		self.check ()
		for ((class_name, name), value_list) in self.config.items ():
			if class_names is None or class_name in class_names:
				if single_value:
					yield EMMConfig.Value (class_name, name, value_list)
				else:
					for value in listsplit (value_list):
						yield EMMConfig.Value (class_name, name, value)

	def __setup_parameter (self, class_name: str, name: str, hostname: Optional[str]) -> Tuple[Dict[str, str], str, str]:
		data = {
			'class': class_name,
			'name': name
		}
		clause = 'class = :class AND name = :name AND '
		if hostname is not None:
			data['hostname'] = hostname
			clause += 'hostname = :hostname'
			value = ':hostname'
		else:
			clause += 'hostname IS NULL'
			value = 'NULL'
		return (data, clause, value)
		
	def remove (self, class_name: str, name: str, hostname: Optional[str] = None) -> bool:
		with TempDB (self.db) as db, db.request () as cursor:
			data, clause, _ = self.__setup_parameter (class_name, name, hostname)
			count = cursor.update (
				'DELETE FROM config_tbl '
				f'WHERE {clause}',
				data
			)
			if count > 0:
				db.sync ()
				self.last = None
				return True
		return False

	def read (self, class_name: str, name: str, hostname: Optional[str] = None) -> str:
		with TempDB (self.db) as db, db.request () as cursor:
			data, clause, _ = self.__setup_parameter (class_name, name, hostname)
			rq = cursor.querys (
				'SELECT value '
				'FROM config_tbl '
				f'WHERE {clause}',
				data
			)
			if rq is not None and rq.value is not None:
				return cast (str, rq.value)
			raise KeyError (f'{class_name}:{name}' + (f'@{hostname}' if hostname else ''))

	def changed (self, class_name: str, name: str, hostname: Optional[str] = None) -> Optional[datetime]:
		with TempDB (self.db) as db, db.request () as cursor:
			data, clause, _ = self.__setup_parameter (class_name, name, hostname)
			rq = cursor.querys (
				'SELECT change_date '
				'FROM config_tbl '
				f'WHERE {clause}',
				data
			)
			if rq is not None:
				return cast (Optional[datetime], rq.change_date)
			raise KeyError (f'{class_name}:{name}' + (f'@{hostname}' if hostname else ''))
		
	def touch (self, class_name: str, name: str, hostname: Optional[str] = None) -> bool:
		with TempDB (self.db) as db, db.request () as cursor:
			data, clause, _ = self.__setup_parameter (class_name, name, hostname)
			if cursor.update (
				'UPDATE config_tbl '
				'SET change_date = CURRENT_TIMESTAMP '
				f'WHERE {clause}',
				data
			) > 0:
				db.sync ()
				return True
		return False
		
	def write (self, class_name: str, name: str, value: str, description: str, hostname: Optional[str] = None) -> bool:
		with TempDB (self.db) as db, db.request () as cursor:
			data, clause, hostname_value = self.__setup_parameter (class_name, name, hostname)
			rq = cursor.querys (
				'SELECT value '
				'FROM config_tbl '
				f'WHERE {clause}',
				data
			)
			data['value'] = value
			if rq is not None:
				if rq.value != value and cursor.update (
					'UPDATE config_tbl '
					'SET value = :value, change_date = CURRENT_TIMESTAMP '
					f'WHERE {clause}',
					data
				) != 1:
					return False
			else:
				data['description'] = description
				if cursor.update (
					'INSERT INTO config_tbl '
					'       (class, name, value, description, hostname, creation_date, change_date) '
					'VALUES '
					f'       (:class, :name, :value, :description, {hostname_value}, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)',
					data
				) != 1:
					return False
			db.sync ()
			self.last = None
			return True
		return False
			
	def update (self, class_name: str, name: str, values: Union[str, List[str]], description: str, hostname: Optional[str] = None) -> bool:
		use_values: List[str] = [values] if isinstance (values, str) else values
		new_values = use_values
		with TempDB (self.db) as db, db.request () as cursor:
			data, clause, _ = self.__setup_parameter (class_name, name, hostname)
			rq = cursor.querys (
				'SELECT value '
				'FROM config_tbl '
				f'WHERE {clause}',
				data
			)
			if rq is not None and rq.value is not None:
				old_values = list (listsplit (rq.value))
				new_values = old_values[:]
				for value in use_values:
					if value not in new_values:
						new_values.append (value)
				if old_values == new_values:
					return True
		return self.write (class_name, name, listjoin (new_values), description, hostname)
	
class EMMCompany (_Config):
	__slots__ = ['keys', 'company_id', 'company_info', 'enabled_cache']
	class Value (NamedTuple):
		company_id: int
		name: str
		value: str
	@dataclass
	class Enable:
		enabled: Dict[int, bool] = field (default_factory = dict)
		default: bool = False
		enabled0: bool = False
		def __post_init__ (self) -> None:
			self.enabled0 = self.enabled.get (0, self.default)
			
		def __call__ (self, company_id: int) -> bool:
			return self.enabled.get (company_id, self.enabled0)
			
	def __init__ (self, db: Optional[DB] = None, reread: Parsable = None, selection: Optional[Systemconfig.Selection] = None, keys: Optional[Sequence[str]] = None) -> None:
		super ().__init__ (db, reread, selection)
		self.keys = keys
		self.company_id: Optional[int] = None
		self.company_info: DefaultDict[int, Dict[str, str]] = defaultdict (dict)
		self.enabled_cache: Dict[Tuple[str, bool], EMMCompany.Enable] = {}

	def __enter__ (self) -> EMMCompany:
		return self
		
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None

	def retrieve (self, db: DB) -> None:
		self.company_info.clear ()
		self.enabled_cache.clear ()
		collect: DefaultDict[Tuple[int, str], Dict[Optional[str], str]] = defaultdict (dict)
		query = (
			'SELECT company_id, cname, cvalue, hostname '
			'FROM company_info_tbl'
		)
		data: Optional[Dict[str, str]] = None
		if self.keys:
			data = {}
			query += ' WHERE cname {clause}'.format (clause = self.keylistclause (self.keys, data))
		with db.request () as cursor:
			for row in cursor.query (query, data):
				if row.cname is not None:
					collect[(row.company_id, row.cname)][self.normalize_hostname (row.hostname)] = row.cvalue if row.cvalue is not None else ''
			for ((company_id, name), value) in collect.items ():
				with Ignore (KeyError):
					self.company_info[company_id][name] = self.selection.pick (value)

	def set_company_id (self, company_id: Optional[int] = None) -> None:
		"""Set the ``company_id'' for queries which are company dependend"""
		self.company_id = company_id

	def get (self,
		name: str,
		company_id: Optional[int] = None,
		index: Optional[str] = None,
		default: Any = _Config.sentinel,
		convert: Optional[Callable[[Any], Any]] = None
	) -> Any:
		"""Retrieves an entry from the company_info_tbl using
``name'' for the configuration name for ``company_id'', if not None,
else for the instance variable self.company_id (if set). ``index'' is
an optional index for the configuration name and ``default'' will be
used, if no entry had been found, otherwise a KeyError is raised.
``convert'' is an optional callable which will be invoked with the
found value to convert it before returning it."""
		self.check ()
		if index is not None:
			with Ignore (KeyError):
				return self.get (f'{name}[{index}]', company_id = company_id, convert = convert)
		#
		if company_id is None:
			company_id = self.company_id
		for cid in [company_id, 0]:
			if cid is not None:
				with Ignore (KeyError):
					value = self.company_info[cid][name]
					return convert (value) if convert is not None else value
		#
		if default is not _Config.sentinel:
			return default
		#
		raise KeyError (name)
	
	def scan (self, company_id: Optional[int] = None) -> Iterable[EMMCompany.Value]:
		"""Scan the company_info_tbl for values for one company"""
		self.check ()
		if company_id is None:
			company_id = self.company_id
		available: Dict[str, EMMCompany.Value] = {}
		for cid in [0, company_id]:
			if cid is not None and cid in self.company_info:
				for (key, value) in self.company_info[cid].items ():
					available[key] = EMMCompany.Value (cid, key, value)
		return available.values ()
	
	def scan_all (self) -> Iterator[EMMCompany.Value]:
		"""Scan the company_info_tbl as an iterator and return a tuple with [company_id, key, value]."""
		self.check ()
		for (company_id, company_config) in sorted (self.company_info.items ()):
			for (key, value) in company_config.items ():
				yield EMMCompany.Value (company_id, key, value)

	def enabled (self, key: str, default: bool = False) -> EMMCompany.Enable:
		try:
			rc = self.enabled_cache[(key, default)]
		except KeyError:
			rc = self.enabled_cache[(key, default)] = EMMCompany.Enable (Stream (self.scan_all ())
				.filter (lambda v: v.name == key)
				.map (lambda v: (v.company_id, atob (v.value)))
				.dict (),
				default = default
			)
		return rc
	
	def write (self,
		company_id: int,
		name: str,
		value: str,
		*,
		index: Optional[str] = None,
		description: Optional[str] = None,
		hostname: Optional[str] = None
	) -> bool:
		with TempDB (self.db) as db, db.request () as cursor:
			data = {
				'company_id': company_id,
				'cname': f'{name}[{index}]' if index is not None else name
			}
			if hostname is not None:
				data['hostname'] = hostname
				hostname_clause = 'hostname = :hostname'
				hostname_value = ':hostname'
			else:
				hostname_clause = 'hostname IS NULL'
				hostname_value = 'NULL'
			rq = cursor.querys (
				'SELECT cvalue '
				'FROM company_info_tbl '
				f'WHERE company_id = :company_id AND cname = :cname AND {hostname_clause}',
				data
			)
			data['cvalue'] = value
			data['description'] = description
			if description is not None:
				description_assignment = ', description = :description'
			else:
				description_assignment = ''
			if (rq is not None and (rq.cvalue == value or cursor.update (
				'UPDATE company_info_tbl '
				f'SET cvalue = :cvalue, timestamp = CURRENT_TIMESTAMP{description_assignment} '
				f'WHERE company_id = :company_id AND cname = :cname AND {hostname_clause}',
				data,
				cleanup = True
			) == 1)) or cursor.update (
				'INSERT INTO company_info_tbl '
				'       (company_id, cname, cvalue, description, creation_date, timestamp, hostname) '
				'VALUES '
				f'       (:company_id, :cname, :cvalue, :description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, {hostname_value})',
				data
			) == 1:
				db.sync ()
				self.last = None
				return True
		return False

class EMMMailerset (_Config):
	__slots__ = ['mailersets', 'mailers']
	@dataclass
	class Mailer:
		name: str
		properties: Dict[str, List[str]] = field (default_factory = dict)
		mailersets: Set[int] = field (default_factory = set)
		user: str = 'mailout'
		owner: int = licence
		capacity: int = 2
		active: bool = True
		fetchlog: bool = True
		valid: bool = True

		@property
		def address (self) -> str:
			return f'{self.user}@{self.name}'

		def add (self, set_id: int) -> None:
			self.mailersets.add (set_id)

	@dataclass
	class Mailerset:
		id: int
		name: str
		mailers: List[EMMMailerset.Mailer]
		
	def __init__ (self, db: Optional[DB] = None, reread: Parsable = None, selection: Optional[Systemconfig.Selection] = None) -> None:
		super ().__init__ (db, reread, selection)
		self.mailersets: Dict[int, EMMMailerset.Mailerset] = {}
		self.mailers: Dict[str, EMMMailerset.Mailer] = {}

	def __enter__ (self) -> EMMMailerset:
		return self
		
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None

	def find_mailer (self, name: str) -> EMMMailerset.Mailer:
		try:
			mailer = self.mailers[name]
		except KeyError:
			mailer = self.mailers[name] = EMMMailerset.Mailer (name)
		return mailer

	def retrieve (self, db: DB) -> None:
		self.mailersets.clear ()
		self.mailers.clear ()
		with db.request () as cursor:
			for row in cursor.query (
				'SELECT ss.set_id, ss.mailer, sd.set_name '
				'FROM serverset_tbl ss '
				'     LEFT OUTER JOIN serverset_desc_tbl sd ON sd.set_id = ss.set_id'
			):
				mailer = self.find_mailer (row.mailer)
				try:
					self.mailersets[row.set_id].mailers.append (mailer)
				except KeyError:
					self.mailersets[row.set_id] = EMMMailerset.Mailerset (row.set_id, row.set_name, [mailer])
				mailer.add (row.set_id)
			for row in cursor.query (
				'SELECT mailer, mvar, mval '
				'FROM serverprop_tbl'
			):
				mailer = self.find_mailer (row.mailer)
				try:
					mailer.properties[row.mvar].append (row.mval)
				except KeyError:
					mailer.properties[row.mvar] = [row.mval]
		for mailer in list (self.mailers.values ()):
			for key in ('user', 'owner', 'capacity', 'active', 'fetchlog'):
				with Ignore (KeyError):
					value = self.selection.pick_pattern (mailer.properties, key)
					if key == 'user':
						mailer.user = value[0]
					elif key == 'owner':
						mailer.owner = int (value[0])
					elif key == 'capacity':
						mailer.capacity = int (value[0])
					elif key == 'active':
						mailer.active = atob (value[0])
					elif key == 'fetchlog':
						mailer.fetchlog = atob (value[0])
			with Ignore (KeyError):
				mailer.valid = set (mailer.properties['hostname']) in self.selection
		for sset in list (self.mailersets.values ()):
			mailers = [_m for _m in sset.mailers if _m.valid]
			if not mailers:
				del self.mailersets[sset.id]
			else:
				self.mailersets[sset.id].mailers = mailers
	
	def mailer_stream (self) -> Stream[EMMMailerset.Mailer]:
		self.check ()
		return Stream (self.mailers.values ()).filter (lambda m: m.valid)
	
	def mailerset_stream (self) -> Stream[EMMMailerset.Mailerset]:
		self.check ()
		return Stream (self.mailersets.values ()).sorted (lambda m: m.id)

class Responsibility (_Config):
	def __init__ (self, *args: Any, **kws: Any) -> None: pass
	def __enter__ (self) -> Responsibility: return self
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]: return None
	def __contains__ (self, company_id: int) -> bool: return True
	def reset (self) -> None: pass
	def is_responsible_for (self, *args: Any, **kws: Any) -> bool: return True
