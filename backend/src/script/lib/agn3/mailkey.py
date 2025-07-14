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
from	datetime import datetime
from	typing import NamedTuple
from	.db import DB, TempDB
from	.parameter import MailingParameter
from	.stream import Stream
#
__all__ = ['MailKey']
#
class Key (NamedTuple):
	id: int
	company_id: int
	method: str
	parameter: dict[str, str]
	key: str
	creation_date: datetime
	change_date: datetime
	legacy: bool
	def get (self, option: str) -> None | str:
		return self.parameter.get (option)

class MailKey:
	__slots__ = ['mailkeys']
	def __init__ (self, company_id: None | int = None, db: None | DB = None) -> None:
		with TempDB (db = db) as tempdb, tempdb.request () as cursor:
			now = datetime.now ()
			parameter = MailingParameter ()
			def parse_parameter (para: str) -> dict[str, str]:
				parameter.loads (para)
				return parameter.data.copy ()
			#
			company_query = 'IN (0, :company_id)' if company_id else '= 0'
			data: dict[str, int] = {} if not company_id else {'company_id': company_id}
			self.mailkeys = Stream (cursor.stream (
					'SELECT dkim_id, company_id, domain, selector, domain_key, creation_date, timestamp '
					'FROM dkim_key_tbl '
					f'WHERE company_id {company_query} AND '
					'      (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND '
					'      (valid_end IS NULL OR valid_end > CURRENT_TIMESTAMP) AND '
					'	domain IS NOT NULL AND selector IS NOT NULL AND domain_key IS NOT NULL',
					data
				).map (lambda r: Key (
						id = r.dkim_id,
						company_id = r.company_id,
						method = 'dkim',
						parameter = {'selector': r.selector, 'domain': r.domain},
						key = r.domain_key,
						creation_date = r.creation_date if r.creation_date is not None else now,
						change_date = r.timestamp if r.timestamp is not None else now,
						legacy = True
					)
				).list () + cursor.stream (
					'SELECT mail_key_id, company_id, method, parameter, mail_key, creation_date, change_date '
					'FROM mail_key_tbl '
					f'WHERE company_id {company_query} AND '
					'       (valid_start IS NULL OR valid_start <= CURRENT_TIMESTAMP) AND '
					'       (valid_end IS NULL OR valid_end > CURRENT_TIMESTAMP) AND '
					'       method IS NOT NULL AND parameter IS NOT NULL AND mail_key IS NOT NULL',
					data
				).map (lambda r: Key (
						id = r.mail_key_id,
						company_id = r.company_id,
						method = r.method,
						parameter = parse_parameter (r.parameter),
						key = r.mail_key,
						creation_date = r.creation_date,
						change_date = r.change_date,
						legacy = False
					)
				).list ()
			).sorted (key = lambda k: (k.legacy, -k.company_id, -k.creation_date.timestamp ())).list ()

	@classmethod
	def key (cls, key: str | Key) -> str:
		keystr = key if isinstance (key, str) else key.key
		return keystr
