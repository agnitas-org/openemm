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
from	typing import Generic, Optional, TypeVar
from	typing import Dict, NamedTuple, Tuple
from	..db import DB, TempDB
from	..dbconfig import DBConfig
from	..definitions import dbid_default
from	..ignore import Ignore
#
__all__ = ['Sourcegroup', 'SGKey', 'SGValue', 'Datasource', 'DSKey', 'DSValue']
#
_K = TypeVar ('_K')
_V = TypeVar ('_V')
#
class _Cache (Generic[_K, _V]):
	__slots__ = ['_cache']
	dbid_default: Optional[str] = None
	def __init__ (self) -> None:
		self._cache: Dict[Tuple[str, _K], _V] = {}
		
	def get (self, db: Optional[DB], key: _K) -> _V:
		ckey = (self._find_dbid (db), key)
		with Ignore (KeyError):
			return self._cache[ckey]
		with TempDB (db) as tdb:
			self._cache[ckey] = value = self.retrieve (tdb, key)
		return value
	
	def retrieve (self, db: DB, key: _K) -> _V:
		raise NotImplementedError ()

	def _find_dbid (self, db: Optional[DB]) -> str:
		if db is not None and db.dbid is not None:
			return db.dbid
		if self.__class__.dbid_default is None:
			self.__class__.dbid_default = DBConfig ().dbid_default
			if self.__class__.dbid_default is None:
				self.__class__.dbid_default = dbid_default
		return self.__class__.dbid_default if self.__class__.dbid_default is not None else dbid_default

class SGKey (NamedTuple):
	typ: str
class SGValue (NamedTuple):
	id: int
	name: str
class Sourcegroup (_Cache[SGKey, SGValue]):
	def retrieve (self, db: DB, key: SGKey) -> SGValue:
		rq = db.querys (
			'SELECT sourcegroup_id, description '
			'FROM sourcegroup_tbl '
			'WHERE sourcegroup_type = :sourcegroup_type',
			{
				'sourcegroup_type': key.typ
			}
		)
		if rq is not None:
			return SGValue (
				id = rq.sourcegroup_id,
				name = rq.description
			)
			
		db.update (
			db.qselect (
				oracle = (
					'INSERT INTO sourcegroup_tbl '
					'       (sourcegroup_id, sourcegroup_type, description, timestamp, creation_date) '
					'VALUES '
					'       (sourcegroup_tbl_seq.nextval, :sourcegroup_type, :description, current_timestamp, current_timestamp)'
				), mysql = (
					'INSERT INTO sourcegroup_tbl '
					'       (sourcegroup_type, description, timestamp, creation_date) '
					'VALUES '
					'       (:sourcegroup_type, :description, current_timestamp, current_timestamp)'
				)
			), {
				'sourcegroup_type': key.typ,
				'description': key.typ
			},
			commit = True
		)
		rq = db.querys (
			db.qselect (
				oracle = 'SELECT sourcegroup_tbl_seq.currval FROM DUAL',
				mysql = 'SELECT last_insert_id()'
			)
		)
		return SGValue (
			id = 0 if rq is None or rq[0] is None else rq[0],
			name = key.typ
		)

class DSKey (NamedTuple):
	company_id: int
	name: str
	sourcegroup: str
class DSValue (NamedTuple):
	id: int
	sourcegroup: SGValue
	
class Datasource (_Cache[DSKey, DSValue]):
	def retrieve (self, db: DB, key: DSKey) -> DSValue:
		sg = Sourcegroup ().get (db, SGKey (typ = key.sourcegroup))
		rq = db.querys (
			'SELECT datasource_id '
			'FROM datasource_description_tbl '
			'WHERE company_id = :company_id AND description = :description AND sourcegroup_id = :sourcegroup_id',
			{
				'company_id': key.company_id,
				'description': key.name,
				'sourcegroup_id': sg.id
			}
		)
		if rq is not None:
			return DSValue (
				id = rq.datasource_id,
				sourcegroup = sg
			)

		db.update (
			db.qselect (
				oracle = (
					'INSERT INTO datasource_description_tbl '
					'       (datasource_id, description, company_id, sourcegroup_id, timestamp) '
					'VALUES '
					'       (datasource_description_tbl_seq.nextval, :description, :company_id, :sourcegroup_id, current_timestamp)'
				), mysql = (
					'INSERT INTO datasource_description_tbl '
					'       (description, company_id, sourcegroup_id, timestamp) '
					'VALUES '
					'       (:description, :company_id, :sourcegroup_id, current_timestamp)'
				)
			), {
				'description': key.name,
				'company_id': key.company_id,
				'sourcegroup_id': sg.id
			},
			commit = True
		)
		rq = db.querys (
			db.qselect (
				oracle = 'SELECT datasource_description_tbl_seq.currval FROM DUAL',
				mysql = 'SELECT last_insert_id()'
			)
		)
		return DSValue (
			id = 0 if rq is None or rq[0] is None else rq[0],
			sourcegroup = sg
		)
