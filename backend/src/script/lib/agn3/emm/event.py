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
import	os
from	datetime import datetime
from	enum import Enum
from	types import TracebackType
import	typing
from	typing import Optional, Union
from	typing import Tuple
from	.config import EMMConfig
from	..cache import Cache
from	..db import DB
from	..definitions import base, licence
#
__all__ = ['Webhook']
#
class Webhook:
	__slots__ = ['path', 'cache']
	class Type (Enum):
		mailing_opened = 1
		link_clicked = 2
		mailing_delivered = 3
		hard_bounce = 4
		mailing_processed = 5
		binding_changed = 6
		testmail_delivered = 7
		
	known_types = dict ((_v.value, _v) for _v in Type.__members__.values ())
	default_path = os.path.join (base, 'log', 'webhook.log')

	@classmethod
	def event_type (cls, event_value: int) -> Webhook.Type:
		return cls.known_types[event_value]
		
	def __init__ (self, path: Optional[str] = None, cache_limit: int = 0, cache_timeout: str = '30m') -> None:
		self.path = path if path is not None else self.default_path
		self.cache: Cache[Union[Tuple[int, int], Tuple[int, int, int]], bool] = Cache (
			limit = cache_limit,
			timeout = cache_timeout
		)
	
	def __enter__ (self) -> Webhook:
		return self
	
	def __exit__ (self, exc_type: Optional[typing.Type[BaseException]], exc_value: Optional[BaseException], traceback: Optional[TracebackType]) -> Optional[bool]:
		return None

	def __call__ (self,
		db: Optional[DB],
		event_type: Webhook.Type,
		*,
		licence_id: Optional[int] = None,
		company_id: Optional[int] = None,
		mailing_id: Optional[int] = None,
		customer_id: Optional[int] = None,
		timestamp: Optional[datetime] = None
	) -> None:
		self.add (
			db,
			licence_id = licence_id if licence_id is not None else licence,
			company_id = company_id if company_id is not None else 0,
			mailing_id = mailing_id if mailing_id is not None else 0,
			customer_id = customer_id if customer_id is not None else 0,
			event_type = event_type,
			timestamp = timestamp if timestamp is not None else datetime.now ()
		)

	def add (self,
		db: Optional[DB],
		licence_id: int,
		company_id: int,
		mailing_id: int,
		customer_id: int,
		event_type: Webhook.Type,
		timestamp: datetime
	) -> None:
		if db is None or self.active (db, licence_id, company_id, event_type):
			line = '{licence_id};{company_id};{mailing_id};{customer_id};{event_type};{timestamp:%Y-%m-%d %H:%M:%S}'.format (
				licence_id = licence_id,
				company_id = company_id,
				mailing_id = mailing_id,
				customer_id = customer_id,
				event_type = event_type.value,
				timestamp = timestamp
			)
			with open (self.path, 'a') as fd:
				fd.write (f'{line}\n')

	def active (self, db: DB, licence_id: int, company_id: int, event_type: Optional[Webhook.Type]) -> bool:
		company_key = (licence_id, company_id)
		try:
			if not self.cache[company_key]:
				return False
		except KeyError:
			try:
				db_valid = int (EMMConfig (db = db, class_names = ['system']).get ('system', 'licence')) == licence
			except (TypeError, ValueError):
				db_valid = False
			company_valid = False
			if db_valid:
				with db.request () as cursor:
					rq = cursor.querys (
						'SELECT status '
						'FROM company_tbl '
						'WHERE company_id = :company_id',
						{
							'company_id': company_id
						}
					)
					if rq is not None and rq.status == 'active':
						company_valid = True
						for row in cursor.queryc (
							'SELECT event_type '
							'FROM webhook_url_tbl '
							'WHERE company_ref = :company_id',
							{
								'company_id': company_id
							}
						):
							if row.event_type in self.known_types:
								event_key = (licence_id, company_id, row.event_type)
								self.cache[event_key] = True
			#
			self.cache[company_key] = rc = db_valid and company_valid
			if not rc:
				return False
			#
		if event_type is None:
			return True
		#
		event_key = (licence_id, company_id, event_type.value)
		try:
			return self.cache[event_key]
		except KeyError:
			self.cache[event_key] = False
			return False
