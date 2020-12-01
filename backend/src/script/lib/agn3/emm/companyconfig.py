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
import	time
from	collections import defaultdict
from	types import TracebackType
from	typing import Any, Callable, Iterable, Optional, Union
from	typing import DefaultDict, Dict, Iterator, List, NamedTuple, Tuple, Type
from	..config import Config
from	..db import DB
from	..definitions import host, fqdn, user
from	..exceptions import error
from	..ignore import Ignore
from	..tools import listsplit, listjoin
#
__all__ = ['CompanyConfig']
#
class ConfigValue (NamedTuple):
	class_name: str
	name: str
	value: str
class CompanyValue (NamedTuple):
	company_id: int
	name: str
	value: str
class CompanyConfig:
	"""Manage configuration from database

With this class one can query the content of the two configuration
tables config_tbl and company_info_tbl from the database. This is a
more convinient way to query these parameter and is also faster when
querying several paramater."""
	__slots__ = ['db', 'reread', 'last', 'company_id', 'company_info', 'config']
	__sentinel = object ()
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
		
	def __init__ (self, db: Optional[DB] = None, reread: Optional[int] = None) -> None:
		"""``cursor'' is an open database cursor to use
(otherwise the database will be opened on demand), ``reread'' can be
either None (do only read the database once and do not reread it
later) or a numeric value as seconds after which the content should be
refreshed from the database.
"""
		self.db = db
		self.reread = reread
		self.last: Union[None, int, float] = None
		self.company_id: Optional[int] = None
		self.company_info: DefaultDict[int, Dict[str, str]] = defaultdict (dict)
		self.config: Dict[Tuple[str, str], str] = {}
	
	def __reread (self, now: Union[int, float]) -> None:
		self.company_info.clear ()
		self.config.clear ()
		def asnull (s: Optional[str]) -> Optional[str]:
			if s is not None:
				s = s.strip ()
			return None if not s else s.lower ()
		
		selections = [f'{user}@{fqdn}', f'{user}@{host}', f'{user}@', fqdn, host, None]
		def pick (collection: Dict[Optional[str], str]) -> str:
			for selection in selections:
				with Ignore (KeyError):
					return collection[selection]
			raise KeyError ()

		with CompanyConfig.TempDB (self.db) as db, db.request () as cursor:
			collect1: DefaultDict[Tuple[int, str], Dict[Optional[str], str]] = defaultdict (dict)
			for row in cursor.query (
				'SELECT company_id, cname, cvalue, hostname '
				'FROM company_info_tbl'
			):
				if row.cname is not None:
					collect1[(row.company_id, row.cname)][asnull (row.hostname)] = row.cvalue if row.cvalue is not None else ''
			for ((company_id, name), value) in collect1.items ():
				with Ignore (KeyError):
					self.company_info[company_id][name] = pick (value)
			#
			collect2: DefaultDict[Tuple[str, str], Dict[Optional[str], str]] = defaultdict (dict)
			for row in cursor.query (
				'SELECT class AS cls, name, value, hostname '
				'FROM config_tbl'
			):
				if row.cls is not None and row.name is not None and row.value is not None:
					collect2[(row.cls, row.name)][asnull (row.hostname)] = row.value
			for (key, value) in collect2.items ():
				with Ignore (KeyError):
					self.config[key] = pick (value)
			self.last = now
	
	def __check (self, force: bool = False) -> None:
		now = time.time ()
		if force or self.last is None or (self.reread is not None and self.last + self.reread < now):
			self.__reread (now)

	def read (self) -> None:
		"""Force (re)reading the configuration from the database"""
		self.__check (True)

	def set_company_id (self, company_id: Optional[int] = None) -> None:
		"""Set the ``company_id'' for queries which are company dependend"""
		self.company_id = company_id
	
	def get_config (self, class_name: str, name: str, default: Any = __sentinel) -> Any:
		"""retreive an entry from the config_tbl using
``class_name'' for the configuration class and ``name'' for the name of
the configuration. If ``default'' is not None, this value will be
returned, if no configuration had been found, otherwise a KeyError is
raised.
"""
		self.__check ()
		with Ignore (KeyError):
			return self.config[(class_name, name)]
		#
		if default is not self.__sentinel:
			return default
		#
		raise KeyError ((class_name, name))
	
	def scan_config (self, class_names: Optional[Iterable[str]] = None, single_value: bool = False) -> Iterator[ConfigValue]:
		"""Scans the config_tbl, if ``class_names'' is not
None it is used as an iterable or a str to limit the scan to these
configuration classes. If ``single_value'' is ``False'', then the
value is split off by comma. """
		self.__check ()
		for (key, value_list) in self.config.items ():
			if class_names is None or key[0] in class_names:
				if single_value:
					yield ConfigValue (key[0], key[1], value_list)
				else:
					for value in listsplit (value_list):
						yield ConfigValue (key[0], key[1], value)
	
	def get_company_info (self,
		name: str,
		company_id: Optional[int] = None,
		index: Optional[str] = None,
		default: Any = __sentinel,
		convert: Optional[Callable[[Any], Any]] = None
	) -> Any:
		"""Retrieves an entry from the company_info_tbl using
``name'' for the configuration name for ``company_id'', if not None,
else for the instance variable self.company_id (if set). ``index'' is
an optional index for the configuration name and ``default'' will be
used, if no entry had been found, otherwise a KeyError is raised.
``convert'' is an optional callable which will be invoked with the
found value to convert it before returning it."""
		self.__check ()
		if index is not None:
			with Ignore (KeyError):
				return self.get_company_info (f'{name}[{index}]', company_id = company_id, convert = convert)
		#
		if company_id is None:
			company_id = self.company_id
		for cid in [company_id, 0]:
			if cid is not None:
				with Ignore (KeyError):
					value = self.company_info[cid][name]
					return convert (value) if convert is not None and callable (convert) else value
		#
		if default is not self.__sentinel:
			return default
		#
		raise KeyError (name)
	
	def scan_company_info (self, company_id: Optional[int] = None) -> Iterable[CompanyValue]:
		"""Scan the company_info_tbl for values for one company"""
		self.__check ()
		if company_id is None:
			company_id = self.company_id
		available: Dict[str, CompanyValue] = {}
		for cid in [0, company_id]:
			if cid is not None and cid in self.company_info:
				for (key, value) in self.company_info[cid].items ():
					available[key] = CompanyValue (cid, key, value)
		return available.values ()
	
	def scan_all_company_info (self) -> Iterator[CompanyValue]:
		"""Scan the company_info_tbl as an iterator and return a tuple with [company_id, key, value]."""
		self.__check ()
		for (company_id, company_config) in sorted (self.company_info.items ()):
			for (key, value) in company_config.items ():
				yield CompanyValue (company_id, key, value)

	def convert (self, section_config: Optional[str] = None) -> Config:
		"""Converts the configuration into an instance of
class Config using ``section_config'' to store the content of the
config_tbl. The content of the company_info_tbl is stored in the
default section of the configuration while each company specific value
is stored in the section with the company_id as its name."""
		self.__check ()
		rc = Config ()
		for (skey, value) in self.config.items ():
			key = '.'.join ([_k for _k in skey if _k])
			if key:
				if section_config is not None:
					ckey = f'{section_config}.{key}'
				else:
					ckey = key
				rc[ckey] = ', '.join ([_v if _v is not None else '' for _v in value])
		for (company_id, cfg) in self.company_info.items ():
			if company_id:
				rc.push (str (company_id))
			for (cikey, civalue) in cfg.items ():
				rc[cikey] = civalue
			if company_id:
				rc.pop ()
		return rc

	def write_config (self, class_name: str, name: str, value: str, description: str, hostname: Optional[str] = None) -> bool:
		with CompanyConfig.TempDB (self.db) as db, db.request () as cursor:
			data = {
				'class': class_name,
				'name': name
			}
			if hostname is not None:
				data['hostname'] = hostname
				hostname_clause = 'hostname = :hostname'
				hostname_value = ':hostname'
			else:
				hostname_clause = 'hostname IS NULL'
				hostname_value = 'NULL'
			rq = cursor.querys (
				'SELECT value '
				'FROM config_tbl '
				f'WHERE class = :class AND name = :name AND {hostname_clause}',
				data
			)
			data['value'] = value
			if rq is not None:
				if rq.value != value and cursor.update (
					'UPDATE config_tbl '
					'SET value = :value, change_date = current_timestamp '
					f'WHERE class = :class AND name = :name AND {hostname_clause}',
					data
				) != 1:
					return False
			else:
				data['description'] = description
				if cursor.update (
					'INSERT INTO config_tbl '
					'       (class, name, value, description, hostname, creation_date, change_date) '
					'VALUES '
					f'       (:class, :name, :value, :description, {hostname_value}, current_timestamp, current_timestamp)',
					data
				) != 1:
					return False
			db.sync ()
			self.last = None
			return True
		return False
			
	def update_config (self, class_name: str, name: str, values: Union[str, List[str]], description: str, hostname: Optional[str] = None) -> bool:
		use_values: List[str] = [values] if isinstance (values, str) else values
		new_values = use_values
		with CompanyConfig.TempDB (self.db) as db, db.request () as cursor:
			data = {
				'class': class_name,
				'name': name
			}
			if hostname is not None:
				data['hostname'] = hostname
				hostname_clause = 'hostname = :hostname'
			else:
				hostname_clause = 'hostname IS NULL'

			rq = cursor.querys (
				'SELECT value '
				'FROM config_tbl '
				f'WHERE class = :class AND name = :name AND {hostname_clause}',
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
		return self.write_config (class_name, name, listjoin (new_values), description, hostname)
	
	def write_company_info (self,
		company_id: int,
		name: str,
		value: str,
		*,
		index: Optional[str] = None,
		description: Optional[str] = None,
		hostname: Optional[str] = None
	) -> bool:
		with CompanyConfig.TempDB (self.db) as db, db.request () as cursor:
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
				f'SET cvalue = :cvalue, timestamp = current_timestamp{description_assignment} '
				f'WHERE company_id = :company_id AND cname = :cname AND {hostname_clause}',
				data,
				cleanup = True
			) == 1)) or cursor.update (
				'INSERT INTO company_info_tbl '
				'       (company_id, cname, cvalue, description, creation_date, timestamp, hostname) '
				'VALUES '
				f'       (:company_id, :cname, :cvalue, :description, current_timestamp, current_timestamp, {hostname_value})',
				data
			) == 1:
				db.sync ()
				self.last = None
				return True
		return False
