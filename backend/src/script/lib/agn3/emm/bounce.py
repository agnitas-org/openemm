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
import	os, logging, json, re, time
from	collections import defaultdict
from	datetime import datetime
from	types import TracebackType
from	typing import Any, Final, Optional, Union
from	typing import DefaultDict, Dict, List, Set, Tuple, Type
from	.config import EMMCompany
from	..db import DB, TempDB
from	..definitions import base
from	..ignore import Ignore
from	..parameter import Parameter
from	..parser import Parsable, unit
from	..stream import Stream
#
__all__ = ['Bounce']
#
logger = logging.getLogger (__name__)
#
class Bounce:
	__slots__ = [
		'db',
		'rules', 'config',
		'rules_cache', 'config_cache',
		'rules_latest', 'config_latest',
		'last_check', 'recheck_interval'
	]
	bav_rule_legacy_path: Final[str] = os.path.join (base, 'lib', 'bav.rule')
	bounce_rule_table: Final[str] = 'bounce_rule_tbl'
	bounce_config_table: Final[str] = 'bounce_config_tbl'
	name_conversion: Final[str] = 'conversion'
	name_company_info_conversion: Final[str] = 'bounce-conversion-parameter'
	epoch: Final[datetime] = datetime (1970, 1, 1)
	def __init__ (self, db: Optional[DB] = None, recheck_interval: Parsable = '3m') -> None:
		self.db = db
		self.rules: Dict[Tuple[int, int], Dict[str, List[str]]] = {}
		self.config: DefaultDict[Tuple[int, int], DefaultDict[str, Dict[str, Any]]] = defaultdict (lambda: defaultdict (dict))
		self.rules_cache: Dict[Tuple[int, int], Dict[str, List[str]]] = {}
		self.config_cache: Dict[Tuple[int, int, str], Dict[str, Any]] = {}
		self.rules_latest = self.epoch
		self.config_latest = self.epoch
		self.last_check = 0
		self.recheck_interval = min (60, unit.parse (recheck_interval))
	
	def __enter__ (self) -> Bounce:
		self.read ()
		return self
		
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None
	
	def serialize (self, obj: Any) -> str:
		return json.dumps (obj, indent = 2, sort_keys = True)
	
	def deserialize (self, representation: str) -> Any:
		return json.loads (representation)

	def get_rule (self, company_id: int, rid: int) -> Dict[str, List[str]]:
		with Ignore (KeyError):
			return self.rules_cache[(company_id, rid)]
		#
		result: DefaultDict[str, List[str]] = defaultdict (list)
		seen: Set[Tuple[int, int]] = set ()
		for key in [
			(company_id, rid),
			(0, rid),
			(company_id, 0),
			(0, 0)
		]:
			if key not in seen:
				seen.add (key)
				with Ignore (KeyError):
					for (section, patterns) in self.rules[key].items ():
						if patterns:
							result[section] += patterns
		self.rules_cache[(company_id, rid)] = rc = dict (result)
		return rc
	
	def set_rule (self, company_id: int, rid: int, obj: Dict[str, Any]) -> None:
		with TempDB (self.db) as db, db.request () as cursor:
			data: Dict[str, Union[int, str]]  = {
				'company_id': company_id,
				'rid': rid
			}
			rq = cursor.querys (
				'SELECT definition '
				f'FROM {self.bounce_rule_table} '
				'WHERE company_id = :company_id AND rid = :rid',
				data
			)
			data['definition'] = self.serialize (obj)
			if rq is not None:
				try:
					need_update = self.deserialize (str (rq.definition)) != obj
				except:
					need_update = True
				if need_update:
					cursor.update (
						f'UPDATE {self.bounce_rule_table} '
						'SET definition = :definition, change_date = CURRENT_TIMESTAMP '
						'WHERE company_id = :company_id AND rid = :rid',
						data
					)
			else:
				cursor.update (
					f'INSERT INTO {self.bounce_rule_table} '
					'        (company_id, rid, definition, creation_date, change_date) '
					'VALUES '
					'        (:company_id, :rid, :definition, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)',
					data
				)
			cursor.sync ()
		self.read (read_rules = True, read_config = False)

	def get_config (self, company_id: int, rid: int, name: str) -> Dict[str, Any]:
		with Ignore (KeyError):
			return self.config_cache[(company_id, rid, name)]
		#
		result: Dict[str, Any] = {}
		seen: Set[Tuple[int, int]] = set ()
		for key in [
			(0, 0),
			(company_id, 0),
			(0, rid),
			(company_id, rid)
		]:
			if key not in seen:
				seen.add (key)
				with Ignore (KeyError):
					result.update (self.config[key][name])
		self.config_cache[(company_id, rid, name)] = result
		return result
	
	def set_config (self, company_id: int, rid: int, name: str, obj: Dict[str, Any], description: Optional[str] = None) -> None:
		with TempDB (self.db) as db, db.request () as cursor:
			data = {
				'company_id': company_id,
				'rid': rid,
				'name': name
			}
			rq = cursor.querys (
				'SELECT value '
				f'FROM {self.bounce_config_table} '
				'WHERE company_id = :company_id AND rid = :rid AND name = :name',
				data
			)
			data['value'] = self.serialize (obj)
			if rq is not None:
				try:
					need_update = self.deserialize (str (rq.value)) != obj
				except:
					need_update = True
				if need_update:
					cursor.update (
						f'UPDATE {self.bounce_config_table} '
						'SET value = :value, change_date = CURRENT_TIMESTAMP '
						'WHERE company_id = :company_id AND rid = :rid AND name = :name',
						data
					)
			else:
				data['description'] = description
				cursor.update (
					f'INSERT INTO {self.bounce_config_table} '
					'        (company_id, rid, name, value, description, creation_date, change_date) '
					'VALUES '
					'        (:company_id, :rid, :name, :value, :description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)',
					data
				)
			cursor.sync ()
		self.read (read_rules = False, read_config = True)

	def check (self, *, read_rules: bool = True, read_config: bool = True) -> None:
		now = int (time.time ())
		if self.last_check + self.recheck_interval < now:
			self.last_check = now
			with TempDB (self.db) as db, db.request () as cursor:
				def check (read: bool, latest: datetime, table: str) -> bool:
					if read and latest is not self.epoch:
						rq = cursor.querys (
							f'SELECT count(*) FROM {table} WHERE change_date > :latest',
							{'latest': latest}
						)
						if rq is not None and rq[0] == 0:
							read = False
					return read
				read_rules = check (read_rules, self.rules_latest, self.bounce_rule_table)
				read_config = check (read_config, self.config_latest, self.bounce_config_table)
			#
			if read_rules or read_config:
				self.read (read_rules = read_rules, read_config = read_config)
		
	def read (self, *, read_rules: bool = True, read_config: bool = True) -> None:
		rule: Dict[str, List[str]]
		obj: Dict[str, Any]
		with TempDB (self.db) as db, db.request () as cursor:
			if read_rules:
				self.rules.clear ()
				if db.exists (self.bounce_rule_table):
					rule_fallback = False
					rule_migrate = True
					for row in cursor.query (f'SELECT company_id, rid, definition, change_date FROM {self.bounce_rule_table}'):
						if row.change_date:
							self.rules_latest = max (self.rules_latest, row.change_date)
						rule_migrate = False
						if row.definition is not None:
							definition = str (row.definition)
							try:
								rule = self.deserialize (definition)
								if isinstance (rule, dict):
									self.rules[(int (row.company_id), int (row.rid))] = rule
								else:
									raise TypeError (f'"{definition}" is not a map')
							except Exception as e:
								logger.warning (f'{self.bounce_rule_table} [{row.company_id}:{row.rid}]: {e} [{definition}]')
				else:
					rule_fallback = True
					rule_migrate = False
				if (rule_fallback or rule_migrate) and os.path.isfile (self.bav_rule_legacy_path):
					with open (self.bav_rule_legacy_path) as fd:
						section: Optional[str] = None
						self.rules[(0, 0)] = rule = {}
						for (lineno, line) in enumerate ((_l.rstrip () for _l in fd), start = 1):
							if not line.lstrip ().startswith ('#'):
								if line.startswith ('[') and line.endswith (']'):
									section = line[1:-1].strip ()
								elif section is not None:
									try:
										# check for valid pattern, get rid of optional leading filename prefix before
										re.compile (line.split ('}', 1)[-1] if line.startswith ('{') else line, re.IGNORECASE)
										try:
											content = rule[section]
										except KeyError:
											content = rule[section] = []
										content.append (line)
									except Exception as e:
										logger.warning (f'{self.bav_rule_legacy_path}:{lineno}:invalid pattern "{line}": {e}')
						if db.exists ('mailloop_rule_tbl'):
							for row in cursor.query ('SELECT rid, section, pattern FROM mailloop_rule_tbl'):
								try:
									re.compile (row.pattern, re.IGNORECASE)
									key = (0, int (row.rid))
									try:
										rule = self.rules[key]
									except KeyError:
										rule = self.rules[key] = {}
									try:
										content = rule[row.section]
									except KeyError:
										content = rule[row.section] = []
									content.append (row.pattern)
								except Exception as e:
									logger.warning (f'mailloop_rule_tbl: {row.rid}[{row.section}] "{row.pattern}" failed: {e}')
						#
						if rule_migrate:
							for ((company_id, rid), rule) in self.rules.items ():
								cursor.update (
									f'INSERT INTO {self.bounce_rule_table} '
									'        (company_id, rid, definition, creation_date, change_date) '
									'VALUES '
									'        (:company_id, :rid, :definition, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)',
									{
										'company_id': company_id,
										'rid': rid,
										'definition': self.serialize (rule)
									}
								)
				self.rules_cache.clear ()
			#
			if read_config:
				self.config.clear ()
				if db.exists (self.bounce_config_table):
					config_fallback = False
					config_migrate = True
					for row in cursor.query (f'SELECT company_id, rid, name, value, change_date FROM {self.bounce_config_table}'):
						if row.change_date is not None:
							self.config_latest = max (self.config_latest, row.change_date)
						config_migrate = False
						if row.value is not None:
							value = str (row.value)
							try:
								obj = self.deserialize (value)
								self.config[(int (row.company_id), int (row.rid))][row.name] = obj
							except Exception as e:
								logger.warning (f'{self.bounce_config_table} {row.company_id}:{row.rid} {row.name}: invalid value: {e} [{value}]')
				else:
					config_fallback = True
					config_migrate = False
				if config_fallback or config_migrate:
					with EMMCompany (db = db, keys = [self.name_company_info_conversion]) as emmcompany:
						for cvalue in emmcompany.scan_all ():
							self.config[(cvalue.company_id, 0)][self.name_conversion] = obj = (
								Stream (Parameter (cvalue.value).items ())
								.map (lambda kv: (kv[0], int (kv[1])))
								.dict ()
							)
							if config_migrate:
								cursor.update (
									f'INSERT INTO {self.bounce_config_table} '
									'        (company_id, rid, name, value, description, creation_date, change_date) '
									'VALUES '
									'        (:company_id, :rid, :name, :value, :description, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)',
									{
										'company_id': cvalue.company_id,
										'rid': 0,
										'name': self.name_conversion,
										'value': self.serialize (obj),
										'description': f'migrated from company_info_tbl.cname = \'{self.name_company_info_conversion}\''
									}
								)
				self.config_cache.clear ()
			#
			cursor.sync ()
