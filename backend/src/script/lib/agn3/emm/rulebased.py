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
import	logging
from	datetime import datetime
from	types import TracebackType
from	typing import Any, Final, Optional
from	typing import Dict, Type
from	..db import DB, TempDB
from	..definitions import fqdn
from	..stream import Stream
#
__all__ = ['Rulebased']
#
logger = logging.getLogger (__name__)
#
class Rulebased:
	"""Tools to handle date based (also known as rulebased) mailings."""
	__slots__ = ['temp_db']
	rulebased_sent_table: Final[str] = 'rulebased_sent_tbl'
	ancient: Final[datetime] = datetime (1980, 1, 1, 0, 0, 0)
	class Mailing:
		__slots__ = ['_mailing_id', '_lastsent', '_clearance', '_status', '_lastsent_original', '_clearance_original', '_status_original', '_dirty']
		def __init__ (self, mailing_id: int, lastsent: datetime, clearance: bool, status: Optional[str], dirty: bool) -> None:
			self._mailing_id = mailing_id
			self._lastsent = lastsent
			self._clearance = clearance
			self._status = status
			self.update ()
			self._dirty = dirty

		def __str__ (self) -> str:
			return f'Mailing (mailing_id = {self._mailing_id}, lastsent = {self._lastsent}, clearance = {self._clearance}, status = {self._status!r})'
		__repr__ = __str__
	
		def update (self) -> None:
			self._lastsent_original = self._lastsent
			self._clearance_original = self._clearance
			self._status_original = self._status
			self._dirty = False

		def changed (self) -> bool:
			return self._dirty or self.lastsent_changed () or self.clearance_changed () or self.status_changed ()

		def lastsent_changed (self) -> bool:
			if self._lastsent is not None and self._lastsent_original is not None:
				return self._lastsent.toordinal () != self._lastsent_original.toordinal ()
			return self._lastsent is not self._lastsent_original

		def clearance_changed (self) -> bool:
			return self._clearance != self._clearance_original
		
		def status_changed (self) -> bool:
			return self._status != self._status_original
	
		@property
		def mailing_id (self) -> int:
			return self._mailing_id

		def _get_lastsent (self) -> datetime:
			return self._lastsent
		def _set_lastsent (self, lastsent: datetime) -> None:
			self._lastsent = lastsent
		def _del_lastsent (self) -> None:
			self._lastsent = self._lastsent_original
		lastsent = property (_get_lastsent, _set_lastsent, _del_lastsent)
	
		def _get_clearance (self) -> bool:
			return self._clearance
		def _set_clearance (self, clearance: bool) -> None:
			self._clearance = clearance
		def _del_clearance (self) -> None:
			self._clearance = self._clearance_original
		clearance = property (_get_clearance, _set_clearance, _del_clearance)
		
		def _get_status (self) -> Optional[str]:
			return self._status
		def _set_status (self, status: Optional[str]) -> None:
			self._status = status
			if isinstance (self._status, str):
				self._status = self._status.strip ()
				if not self._status:
					self._status = None
		def _del_status (self) -> None:
			self._status = self._status_original
		status = property (_get_status, _set_status, _del_status)
		
	def __init__ (self, db: Optional[DB] = None) -> None:
		self.temp_db = TempDB (db)

	def __enter__ (self) -> Rulebased:
		return self
		
	def __exit__ (self, exc_type: Optional[Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None

	def retrieve (self, mailing_id: int) -> Rulebased.Mailing:
		with self.temp_db as db:
			rq = db.querys (
				'SELECT lastsent, clearance, clearance_status '
				f'FROM {Rulebased.rulebased_sent_table} '
				'WHERE mailing_id = :mailing_id',
				{
					'mailing_id': mailing_id
				}
			)
			if rq is not None:
				rc = Rulebased.Mailing (
					mailing_id = mailing_id,
					lastsent = rq.lastsent if rq.lastsent is not None else self.ancient,
					clearance = (rq.clearance > 0) if rq.clearance is not None else True,
					status = rq.clearance_status,
					dirty = False
				)
			else:
				rc = Rulebased.Mailing (
					mailing_id = mailing_id,
					lastsent = self.ancient,
					clearance = True,
					status = None,
					dirty = True
				)
				self.store (rc)
			return rc
	
	def store (self, mailing: Rulebased.Mailing) -> bool:
		rc = True
		if mailing.changed ():
			with self.temp_db as db, db.request () as cursor:
				columns = [
					('change_date', 'current_timestamp')
				]
				data: Dict[str, Any] = {
					'mailing_id': mailing.mailing_id
				}
				if mailing.lastsent_changed ():
					columns.append (('lastsent', ':lastsent'))
					data['lastsent'] = mailing.lastsent if mailing.lastsent is not self.ancient else None
				if mailing.clearance_changed ():
					columns.append (('clearance', ':clearance'))
					columns.append (('clearance_change', 'current_timestamp'))
					columns.append (('clearance_origin', ':clearance_origin'))
					data['clearance'] = 1 if mailing.clearance else 0
					data['clearance_origin'] = fqdn
				if mailing.status_changed ():
					columns.append (('clearance_status', ':clearance_status'))
					data['clearance_status'] = mailing.status
				query = (
					f'UPDATE {Rulebased.rulebased_sent_table} '
					'SET {columns} '
					'WHERE mailing_id = :mailing_id'
					.format (
						columns = Stream (columns).map (lambda c: f'{c[0]} = {c[1]}').join (', ')
					)
				)
				if (rows := cursor.update (query, data)) == 1:
					logger.debug (f'{mailing.mailing_id}: updated mailing using {data}')
				elif rows == 0:
					if (rows := cursor.update (
						f'INSERT INTO {Rulebased.rulebased_sent_table} '
						'        (mailing_id, creation_date, change_date, lastsent, clearance, clearance_change, clearance_origin, clearance_status) '
						'VALUES '
						'        (:mailing_id, current_timestamp, current_timestamp, :lastsent, :clearance, current_timestamp, :clearance_origin, :clearance_status)',
						{
							'mailing_id': mailing.mailing_id,
							'lastsent': mailing.lastsent if mailing.lastsent is not self.ancient else None,
							'clearance': mailing.clearance,
							'clearance_origin': fqdn,
							'clearance_status': mailing.status
						}
					)) == 1:
						logger.debug (f'{mailing.mailing_id}: created mailing for {mailing}')
					else:
						logger.error (f'{mailing.mailing_id}: failed to create mailing using {mailing} ({rows} rows affected)')
						rc = False
				else:
					logger.error (f'{mailing.mailing_id}: failed to update mailing using {mailing}, {rows} rows affected')
					rc = False
				if rc:
					cursor.sync ()
					mailing.update ()
		return rc

	def remove (self, mailing: Rulebased.Mailing) -> None:
		with self.temp_db as db, db.request () as cursor:
			cursor.update (
				f'DELETE FROM {Rulebased.rulebased_sent_table} '
				'WHERE mailing_id = :mailing_id',
				{
					'mailing_id': mailing.mailing_id
				}
			)
			cursor.sync ()
				
