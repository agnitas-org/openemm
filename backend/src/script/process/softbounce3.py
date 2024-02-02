#!/usr/bin/env python3
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
import	os, logging, time
from	collections import defaultdict, deque, namedtuple
from	datetime import datetime, timedelta
from	typing import Any, Final
from	typing import DefaultDict, Dict, Deque, NamedTuple, Tuple, Set
from	agn3.db import DB
from	agn3.dbm import DBM
from	agn3.definitions import base, unique
from	agn3.emm.bounce import Bounce
from	agn3.emm.config import Responsibility
from	agn3.emm.timestamp import Timestamp
from	agn3.emm.types import UserStatus
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.log import log
from	agn3.runtime import Runtime
from	agn3.stream import Stream
from	agn3.tools import Progress
#
logger = logging.getLogger (__name__)
#
def day_offset (offset: int) -> datetime:
	return datetime.fromordinal (datetime.now ().toordinal () - offset)
class Cache:
	incarnation = 0
	def __init__ (self, inmem: int) -> None:
		self.incarnation += 1
		fname = f'{base}/var/tmp/sbcache.%d.%d' % (os.getpid (), self.incarnation)
		self.gdb = DBM (fname, 'n')
		os.unlink (fname)
		self.inmem = inmem
		self.mem: Dict[int, int] = {}
		self.timeline: Deque[int] = deque ()
		self.count = 0
		
	def done (self) -> None:
		self.gdb.close ()
		
	def add (self, key: int) -> None:
		if key in self.mem:
			self.mem[key] += 1
		else:
			if self.count >= self.inmem:
				store = self.timeline.popleft ()
				self.gdb[str (store).encode ('UTF-8')] = str (self.mem[store]).encode ('UTF-8')
				del self.mem[store]
			else:
				self.count += 1
			skey = str (key).encode ('UTF-8')
			if skey in self.gdb:
				self.mem[key] = int (self.gdb[skey].decode ('UTF-8')) + 1
			else:
				self.mem[key] = 1
			self.timeline.append (key)
		
	def get (self, key: int, default: int) -> int:
		if key in self.mem:
			return self.mem[key]
		with Ignore (KeyError):
			return int (self.gdb[str (key).encode ('UTF-8')].decode ('UTF-8'))
		return default

class ProgressDB (Progress): #{{{
	__slots__ = ['db']
	def __init__ (self, name: str, db: DB) -> None:
		super ().__init__ (name)
		self.db = db
	
	def fin (self) -> int:
		if self.count > 0:
			return super ().fin ()
		self.handle ()
		return 0
		
	def log (self, s: str) -> None:
		logger.info (f'{self.name}: {s}')
			
	def handle (self) -> None:
		self.db.sync ()
#}}}
class Softbounce (Runtime):
	__slots__ = ['db', 'responsibilities', 'timestamp', 'bounce', 'companies', 'hardbounces', 'config_cache']
	legacy_timestamp_name: Final[str] = 'bounce-conversion'
	timestamp_name: Final[str] = f'bounce-conversion-{unique}'
	class Company (NamedTuple):
		active: bool = False
		mailtracking: bool = False
	class Hardbounce (NamedTuple):
		company_id: int
		mailing_id: int
		email: str
		
	def supports (self, option: str) -> bool:
		return option not in ('dryrun', 'background', 'watchdog')
	
	def executor (self) -> bool:
		rc = True
		with DB () as self.db:
			self.responsibilities = Responsibility (db = self.db)
			self.responsibilities.check ()
			self.timestamp = Timestamp (self.timestamp_name, initial_timestamp = 'now', initial_timestamp_from = self.legacy_timestamp_name)
			self.bounce = Bounce ()
			self.companies: DefaultDict[int, Softbounce.Company] = defaultdict (Softbounce.Company)
			self.hardbounces: Set[Softbounce.Hardbounce] = set ()
			self.config_cache: Dict[Tuple[int, str], int] = {}
			try:
				with log ('parameter'):
					self.setup_parameter ()
				with log ('expire'):
					self.expire_entries ()
				with log ('timestamp'):
					self.setup_timestamp ()
				with log ('collect'):
					self.collect_new_bounces ()
				with log ('timestamp'):
					self.finalize_timestamp (True)
				with log ('merge'):
					self.merge_new_bounces ()
				with log ('remove-active'):
					self.remove_active_receivers ()
				with log ('fade-out'):
					self.fade_out ()
				with log ('convert'):
					self.convert_to_hardbounce ()
			except error as e:
				logger.exception ('Failed due to %s' % e)
				self.finalize_timestamp (False)
				rc = False
			self.db.sync ()
		return rc

	def setup_parameter (self) -> None: #{{{
		logger.info ('Read bounce configuration')
		self.bounce.read (read_rules = False, read_config = True)
		logger.info ('Seupt companies')
		for row in self.db.query ('SELECT company_id, status, mailtracking FROM company_tbl'):
			if row.company_id in self.responsibilities:
				self.companies[row.company_id] = Softbounce.Company (active = row.status == 'active', mailtracking = row.mailtracking == 1)
	#}}}
	def config (self, company_id: int, var: str, default: int) -> int: #{{{
		try:
			return self.config_cache[(company_id, var)]
		except KeyError:
			rc = self.config_cache[(company_id, var)] = self.bounce.get_config (company_id, 0, Bounce.name_conversion).get (var, default)
			return rc
	#}}}
	def expire_entries (self) -> None: #{{{
		logger.info ('Expire entries from softbounce_email_tbl')
		for company_id in (
			self.db.stream (
				'SELECT distinct company_id '
				'FROM softbounce_email_tbl'
			)
			.map_to (int, lambda r: r.company_id)
			.filter (lambda c: c in self.responsibilities)
			.sorted ()
		):
			expire = self.config (company_id, 'expire', 1100)
			threshold = self.config (company_id, 'threshold', 5)
			if expire > 0:
				logger.info (f'{company_id}: expire old entries')
				removed = 0
				hardbounced = 0
				p = ProgressDB (f'expire {company_id}', self.db)
				for row in self.db.streamc (
					'SELECT email, bnccnt, mailing_id, creation_date '
					'FROM softbounce_email_tbl '
					'WHERE company_id = :company_id AND creation_date < :expire',
					{
						'company_id': company_id,
						'expire': datetime.now () - timedelta (days = expire)
					}
				):
					if row.bnccnt > threshold:
						logger.info (f'Email {row.email} expires to hardbounce due to creation {row.creation_date} and a bounce count of {row.bnccnt} as exceeding the threshold {threshold}')
						self.hardbounces.add (Softbounce.Hardbounce (company_id = company_id, mailing_id = row.mailing_id, email = row.email))
						hardbounced += 1
					else:
						logger.info (f'Email {row.email} expires due to creation {row.creation_date} and a bounce count of {row.bnccnt} which is less than the threshold {threshold}')
						removed += self.db.update (
							'DELETE FROM softbounce_email_tbl '
							'WHERE company_id = :company_id AND email = :email',
							{
								'company_id': company_id,
								'email': row.email
							}
						)
					p ()
				p.fin ()
				logger.info (f'{company_id}: removed {removed:,d} email(s) and {hardbounced:,d} email(s) marked as hardbounce')
		logger.info ('Expire entries from softbounce_email_tbl done')
	#}}}
	def setup_timestamp (self) -> None: #{{{
		logger.info ('Setup timestamp')
		self.timestamp.setup (self.db)
		time.sleep (1)
		logger.info ('Setup timestamp done')
	#}}}
	def collect_new_bounces (self) -> None: #{{{
		logger.info ('Start collecting new bounces')
		with self.db.request () as cursor:
			Update = namedtuple ('Update', ['customer_id', 'company_id', 'mailing_id', 'detail'])
			class Collect (Stream.Collector):
				def supplier (self) -> Any:
					self.data: Dict[Tuple[int, int], Update] = {}
					self.uniques = 0
					return self

				def accumulator (self, supplier: Any, element: Any) -> None:
					update = Update (*element)
					if update.detail >= 400 and update.detail < 510:
						key = (update.company_id, update.customer_id)
						try:
							if update.detail > self.data[key].detail:
								self.data[key] = update
						except KeyError:
							self.data[key] = update
							self.uniques += 1

				def finisher (self, supplier: Any, count: int) -> Any:
					return (count, self.uniques, self.data)

			records: int
			uniques: int
			updates: Dict[Tuple[int, int], Update]
			data: Dict[str, Any] = {}
			(records, uniques, updates) = (cursor.stream (
					'SELECT customer_id, company_id, mailing_id, detail '
					'FROM bounce_tbl '
					'WHERE {timestamp_clause}'.format (
						timestamp_clause = self.timestamp.make_between_clause ('timestamp', data)
					),
					data
				)
				.filter (lambda row: row.company_id in self.responsibilities)
				.collect (Collect ())
			)
			logger.info ('Read {records:,d} records ({uniques:,d} uniques) and have {updates:,d} for insert'.format (records = records, uniques = uniques, updates = len (updates)))
			#
			p = ProgressDB ('collect', self.db)
			for update in Stream (updates.values ()).sorted ():
				cursor.update (
					'INSERT INTO bounce_collect_tbl '
					'       (customer_id, company_id, mailing_id, timestamp) '
					'VALUES '
					'       (:customer_id, :company_id, :mailing_id, CURRENT_TIMESTAMP)',
					{
						'customer_id': update.customer_id,
						'company_id': update.company_id,
						'mailing_id': update.mailing_id
					}
				)
				p ()
			inserts = p.fin ()
			logger.info (f'Read {records:,d} records ({uniques:,d} uniques) and inserted {inserts:,d}')
			cursor.sync ()
	#}}}
	def finalize_timestamp (self, success: bool) -> None: #{{{
		logger.info ('Finalizing timestamp')
		self.timestamp.done (success)
		logger.info ('Timestamp finalized')
	#}}}
	def merge_new_bounces (self) -> None: #{{{
		logger.info ('Start merging new bounces into softbounce_email_tbl')
		with self.db.request () as cursor:
			for company_id in (
				self.db.streamc (
					'SELECT distinct company_id '
					'FROM bounce_collect_tbl'
				)
				.filter (lambda r: self.companies[r.company_id].active)
				.map_to (int, lambda r: r.company_id)
				.filter (lambda c: c in self.responsibilities)
				.sorted ()
			):
				logger.info ('Working on %d' % company_id)
				p = ProgressDB ('merge', self.db)
				for row in self.db.queryc (
					'SELECT mt.customer_id, mt.mailing_id, cust.email, sb.bnccnt '
					'FROM bounce_collect_tbl mt '
					f'    INNER JOIN customer_{company_id}_tbl cust ON cust.customer_id = mt.customer_id '
					'     LEFT OUTER JOIN softbounce_email_tbl sb ON sb.company_id = mt.company_id AND sb.email = cust.email '
					'WHERE mt.company_id = :company_id',
					{
						'company_id': company_id
					}
				):
					if row.bnccnt is None:
						cursor.update (
							'INSERT INTO softbounce_email_tbl '
							'           (company_id, email, bnccnt, mailing_id, creation_date, timestamp) '
							'VALUES '
							'           (:company_id, :email, 1, :mailing_id, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)',
							{
								'company_id': company_id,
								'email': row.email,
								'mailing_id': row.mailing_id
							}
						)
					else:
						cursor.update (
							'UPDATE softbounce_email_tbl '
							'SET mailing_id = :mailing_id, timestamp = CURRENT_TIMESTAMP, bnccnt=bnccnt+1 '
							'WHERE company_id = :company_id AND email = :email',
							{
								'mailing_id': row.mailing_id,
								'company_id': company_id,
								'email': row.email
							}
						)
					p ()
				p.fin ()
				rows = cursor.update (
					'DELETE FROM bounce_collect_tbl '
					'WHERE company_id = :company_id',
					{
						'company_id': company_id
					}
				)
				self.db.sync ()
				logger.info (f'Removed {rows:,d} entries from bounce_collect_tbl for {company_id}')
		logger.info ('Merging of new bounces done')
	#}}}
	def remove_active_receivers (self) -> None: #{{{
		logger.info ('Remove active receivers from being watched')
		all_count = 0
		for company_id in (self.db.streamc ('SELECT distinct company_id FROM softbounce_email_tbl')
			.filter (lambda r: self.companies[r.company_id].active and self.companies[r.company_id].mailtracking)
			.map_to (int, lambda r: r.company_id)
			.filter (lambda c: c in self.responsibilities)
			.sorted ()
		):
			table = f'success_{company_id}_tbl'
			if self.db.exists (table):
				query = self.db.qselect (
					oracle = (
						'DELETE FROM softbounce_email_tbl sb '
						f'WHERE company_id = {company_id} AND EXISTS '
						f'      (SELECT 1 FROM success_{company_id}_tbl su '
						'        WHERE su.customer_id IN '
						f'             (SELECT customer_id FROM customer_{company_id}_tbl cust WHERE cust.email = sb.email) '
						'              AND su.timestamp > sb.timestamp)'
					), mysql = (
						'DELETE sb.* '
						f'FROM softbounce_email_tbl sb INNER JOIN success_{company_id}_tbl su '
						f'WHERE sb.company_id = {company_id} AND su.customer_id IN '
						f'      (SELECT customer_id FROM customer_{company_id}_tbl cust WHERE cust.email = sb.email) '
						'       AND su.timestamp > sb.timestamp'
					)
				)
				count = self.db.update (query, commit = True)
				logger.info (f'Removed {count:,d} active receiver for company_id {company_id}')
				all_count += count
			else:
				logger.info (f'Skip removing active receivers for company_id {company_id} due to missing table {table}')
		logger.info (f'Finished removing {all_count:,d} receiver')
	#}}}
	def fade_out (self) -> None: #{{{
		logger.info ('Fade out addresses without mailtracking')
		for company_id in (self.db.streamc ('SELECT distinct company_id FROM softbounce_email_tbl')
			.map_to (int, lambda r: r.company_id)
			.filter (lambda c: not self.companies[c].mailtracking)
			.filter (lambda c: c in self.responsibilities)
			.sorted ()
		):
			fade_out = self.config (company_id, 'fade-out', 14)
			if fade_out > 0:
				logger.info (f'{company_id}: reduce bounce count')
				rows = self.db.update (
					'UPDATE softbounce_email_tbl '
					'SET bnccnt = bnccnt - 1 '
					'WHERE company_id = :company_id AND timestamp < :timestamp AND bnccnt > 1',
					{
						'company_id': company_id,
						'timestamp': datetime.now () - timedelta (days = fade_out)
					}
				)
				logger.info (f'{company_id}: reduced bounce count for {rows:,d} email(s)')
		rows = self.db.update (
			'DELETE FROM softbounce_email_tbl '
			'WHERE bnccnt = 0'
		)
		self.db.sync ()
		logger.info (f'Fade out done, removed {rows:,d} email(s) due to faded out bounce count to 0')
	#}}}
	def convert_to_hardbounce (self) -> None: #{{{
		logger.info ('Start converting softbounces to hardbounce')
		for company_id in (self.db.streamc ('SELECT distinct company_id FROM softbounce_email_tbl')
			.filter (lambda r: self.companies[r.company_id].active)
			.map_to (int, lambda r: r.company_id)
			.filter (lambda c: c in self.responsibilities)
			.sorted ()
			.list ()
		):
			logger.info (f'Working on {company_id}')
			logger.info (f'Setup rdir log cache for {company_id}')
			last_click_timestamp = day_offset (self.config (company_id, 'last-click', 30))
			last_open_timestamp = day_offset (self.config (company_id, 'last-open', 30))
			click_cache = Cache (1000)
			open_cache = Cache (1000 * 1000)
			for row in self.db.query (
				'SELECT customer_id '
				f'FROM rdirlog_{company_id}_tbl '
				'WHERE timestamp >= :timestamp',
				{
					'timestamp': last_click_timestamp
				}
			):
				click_cache.add (row.customer_id)
			logger.info (f'Setup onepixel log cache for {company_id}')
			for row in self.db.query (
				'SELECT customer_id '
				f'FROM onepixellog_{company_id}_tbl '
				'WHERE timestamp >= :timestamp',
				{
					'timestamp': last_open_timestamp
				}
			):
				open_cache.add (row.customer_id)
			#
			logger.info (f'Collect possible hardbounces for {company_id}')
			max_bounce_count = self.config (company_id, 'convert-bounce-count', 40)
			min_bounce_duration = timedelta (days = self.config (company_id, 'convert-bounce-duration', 30))
			for row in self.db.streamc (
				'SELECT email, mailing_id, creation_date, timestamp '
				'FROM softbounce_email_tbl  '
				'WHERE company_id = :company_id AND bnccnt >= :max_bounce_count',
				{
					'company_id': company_id,
					'max_bounce_count': max_bounce_count
				}
			).filter (lambda r: r.creation_date is None or r.timestamp is None or r.creation_date + min_bounce_duration < r.timestamp):
				self.hardbounces.add (Softbounce.Hardbounce (company_id = company_id, mailing_id = row.mailing_id, email = row.email))
			self.db.sync ()
			logger.info ('Setup completed')
			#
			p = ProgressDB ('evaluate', self.db)
			for (email, mailing_id) in ((_h.email, _h.mailing_id) for _h in self.hardbounces if _h.company_id == company_id):
				customer_ids = (self.db.stream (
						'SELECT customer_id '
						f'FROM customer_{company_id}_tbl '
						'WHERE email = :email',
						{
							'email': email
						}
					)
					.map_to (int, lambda r: r.customer_id)
					.list ()
				)
				if customer_ids:
					activities = (Stream (customer_ids)
						.map (lambda c: (click_cache.get (c, 0), open_cache.get (c, 0)))
						.reduce (lambda s, e: (s[0] + e[0], s[1] + e[1]))
					)
					if sum (activities):
						logger.info (f'Email {email} has {activities[0]:,d} klick(s) and {activities[1]:,d} onepix(es) -> active')
					else:
						logger.info (f'Email {email} has no klicks and no onepixes -> hardbounce')
						for customer_id in customer_ids:
							if self.db.update (
								f'UPDATE customer_{company_id}_binding_tbl '
								'SET user_status = :user_status, user_remark = :user_remark, exit_mailing_id = :mailing_id, timestamp = CURRENT_TIMESTAMP '
								'WHERE customer_id = :customer_id AND user_status IN (:user_status_active, :user_status_suspend)',
								{
									'user_status': UserStatus.BOUNCE.value,
									'user_remark': 'bounce:soft',
									'mailing_id': mailing_id,
									'customer_id': customer_id,
									'user_status_active': UserStatus.ACTIVE.value,
									'user_status_suspend': UserStatus.SUSPEND.value
								}
							) > 0:
								self.db.update (
									'INSERT INTO bounce_tbl '
									'       (company_id, customer_id, detail, mailing_id, timestamp, dsn) '
									'VALUES '
									'       (:company_id, :customer_id, :detail, :mailing_id, CURRENT_TIMESTAMP, :dsn)',
									{
										'company_id': company_id,
										'customer_id': customer_id,
										'detail': 510,
										'mailing_id': mailing_id,
										'dsn': 599
									}
								)
				else:
					logger.info (f'Email {email} has matching profile (anymore) -> discarded')
				self.db.update (
					'DELETE FROM softbounce_email_tbl '
					'WHERE company_id = :company_id AND email = :email',
					{
						'company_id': company_id,
						'email': email
					}
				)
				p ()
			p.fin ()
			click_cache.done ()
			open_cache.done ()
		logger.info ('Converting softbounces to hardbounce done')
	#}}}

if __name__ == '__main__':
	Softbounce.main ()
