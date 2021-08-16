#!/usr/bin/env python3
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
import	os, logging, time
from	collections import defaultdict, deque, namedtuple
from	datetime import datetime
from	dataclasses import dataclass
from	typing import Any, Callable, Final, Optional
from	typing import DefaultDict, Dict, Deque, List, NamedTuple, Tuple
from	agn3.db import DB
from	agn3.dbm import DBM
from	agn3.emm.companyconfig import CompanyConfig
from	agn3.emm.timestamp import Timestamp
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.log import log
from	agn3.parameter import Parameter
from	agn3.runtime import Runtime
from	agn3.stream import Stream
#
logger = logging.getLogger (__name__)
#
class Cache:
	incarnation = 0
	def __init__ (self, inmem: int) -> None:
		self.incarnation += 1
		fname = '/var/tmp/sbcache.%d.%d' % (os.getpid (), self.incarnation)
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

class Softbounce (Runtime):
	__slots__ = ['db', 'timestamp', 'config', 'companies']
	timestamp_name: Final[str] = 'bounce-conversion'
	conversion_name: Final[str] = 'bounce-conversion-parameter'
	class Company (NamedTuple):
		active: bool = False
		mailtracking: bool = False
		
	def supports (self, option: str) -> bool:
		return option not in ('dryrun', 'background', 'watchdog')
	
	def executor (self) -> bool:
		rc = True
		with DB () as self.db:
			self.timestamp = Timestamp (self.timestamp_name)
			self.config: Dict[int, Parameter] = {}
			self.companies: DefaultDict[int, Softbounce.Company] = defaultdict (Softbounce.Company)
			try:
				with log ('parameter'):
					self.setup_parameter ()
				with log ('remove-olds'):
					self.remove_old_entries ()
				with log ('timestamp'):
					self.setup_timestamp ()
				with log ('collect'):
					self.collect_new_bounces ()
				with log ('timestamp'):
					self.finalize_timestamp (True)
				with log ('merge'):
					self.merge_new_bounces ()
				with log ('convert'):
					self.convert_to_hardbounce ()
			except error as e:
				logger.exception ('Failed due to %s' % e)
				self.finalize_timestamp (False)
				rc = False
			self.db.sync ()
		return rc
	def __cfg (self, company_id: int, var: str, default: int) -> int: #{{{
		with Ignore (KeyError, ValueError):
			return int (self.config[company_id][var])
		if company_id != 0:
			return self.__cfg (0, var, default)
		return default
	#}}}
	def setup_parameter (self) -> None: #{{{
		logger.info ('Reading parameter from company info table')
		cc = CompanyConfig (self.db)
		cc.read ()
		for (company_id, company_config) in cc.company_info.items ():
			with Ignore (KeyError):
				parameter = company_config[self.conversion_name]
				try:
					self.config[company_id] = Parameter (parameter)
				except Exception:
					logger.exception ('Failed to parse parameter %r for company_id %d' % (parameter, company_id))
					raise error (f'failed to parse parameter {parameter} for {company_id}')
		logger.info ('%d parameter read' % len (self.config))
		for row in self.db.query ('SELECT company_id, status, mailtracking FROM company_tbl'):
			self.companies[row.company_id] = Softbounce.Company (active = row.status == 'active', mailtracking = row.mailtracking == 1)
	#}}}
	def __do (self,
		query: str,
		data: Optional[Dict[str, Any]],
		what: str
	) -> None: #{{{
		try:
			with self.db.request () as cursor:
				logger.info (what)
				rows = cursor.update (query, data, commit = True)
				logger.info ('%s: affected %d rows' % (what, rows))
		except error as e:
			logger.error ('%s: failed using query %s %r: %s (%s)' % (what, query, data, e, self.db.last_error ()))
	#}}}
	def __ccoll (self,
		query: str,
		data: Optional[Dict[str, Any]],
		keys: Tuple[str, ...],
		defaults: Tuple[int, ...]
	) -> Dict[Tuple[int, ...], List[int]]: #{{{
		rc: Dict[Tuple[int, ...], List[int]] = {}
		rkeys = 'keys %s' % ', '.join (list (keys))
		try:
			with self.db.request () as cursor:
				logger.info (f'Collect compamy info for {rkeys}')
				count = 0
				for row in cursor.query (query, data):
					values = tuple ([self.__cfg (row.company_id, _k, _d) for (_k, _d) in zip (keys, defaults)])
					try:
						rc[values].append (row.company_id)
					except KeyError:
						rc[values] = [row.company_id]
					count += 1
				logger.info ('Collected %d companies for %s distributed over %d entries' % (count, rkeys, len (rc)))
		except error as e:
			logger.error ('Failed to collect companies for %s using %s: %s (%s)' % (rkeys, query, e, self.db.last_error ()))
		return rc
	#}}}
	def __cdo (self,
		collection: Dict[Tuple[int, ...], List[int]],
		query: Callable[[Tuple[int, ...]], Optional[str]],
		data: Optional[Dict[str, Any]],
		filler: Callable[[Dict[str, Any], Tuple[int, ...]], None],
		what: str
	) -> None: #{{{
		for (key, companies) in collection.items ():
			ndata = data.copy () if data is not None else {}
			basequery = query (key)
			if basequery is not None:
				filler (ndata, key)
				companies = sorted (companies)
				while companies:
					chunk = companies[:20]
					companies = companies[20:]
					if len (chunk) > 1:
						nquery = '%s IN (%s)' % (basequery, ', '.join ([str (_c) for _c in chunk]))
					else:
						nquery = '%s = %d' % (basequery, chunk[0])
					self.__do (nquery, ndata, '%s for %s' % (what, ', '.join ([str (_c) for _c in chunk])))
			else:
				logger.info ('Skip execution for key %r' % (key, ))
	#}}}
	def remove_old_entries (self) -> None: #{{{
		logger.info ('Remove old entries from softbounce_email_tbl')
		coll = self.__ccoll ('SELECT distinct company_id FROM softbounce_email_tbl', None, ('max-age-create', 'max-age-change'), (180, 60))
		def query_data (key: Tuple[int, ...]) -> Optional[str]:
			q = ['DELETE FROM softbounce_email_tbl WHERE']
			if key[0] > 0:
				if key[1] > 0:
					q.append ('(creation_date <= :expire_create OR timestamp <= :expire_change)')
				else:
					q.append ('creation_date <= :expire_create')
			else:
				if key[1] > 0:
					q.append ('timestamp <= :expire_change')
				else:
					q.clear ()
			if q:
				q.append ('AND company_id')
				return ' '.join (q)
			return None
		def fill_data (data: Dict[str, Any], key: Tuple[int, ...]) -> None:
			if key[0] > 0:
				ts = time.localtime (time.time () - key[0] * 24 * 60 * 60)
				data['expire_create'] = datetime (ts.tm_year, ts.tm_mon, ts.tm_mday)
			if key[1] > 0:
				ts = time.localtime (time.time () - key[1] * 24 * 60 * 60)
				data['expire_change'] = datetime (ts.tm_year, ts.tm_mon, ts.tm_mday)
		self.__cdo (coll, query_data, None, fill_data, 'Remove old addresses from softbounce_email_tbl')
		logger.info ('Removing of old entries from softbounce_email_tbl done')
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
			data: Dict[str, Any] = {}
			query =  (
				'SELECT customer_id, company_id, mailing_id, detail '
				'FROM bounce_tbl '
				'WHERE %s ORDER BY company_id, customer_id'
				% self.timestamp.make_between_clause ('timestamp', data)
			)
			#
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
			(records, uniques, updates) = cursor.stream (query, data).collect (Collect ())
			logger.info ('Read {records:,d} records ({uniques:,d} uniques) and have {updates:,d} for insert'.format (records = records, uniques = uniques, updates = len (updates)))
			#
			inserts = 0		
			query = ('INSERT INTO bounce_collect_tbl (customer_id, company_id, mailing_id, timestamp) VALUES (:customer_id, :company_id, :mailing_id, current_timestamp)')
			for update in Stream (updates.values ()).sorted ():
				cursor.update (query, {
					'customer_id': update.customer_id,
					'company_id': update.company_id,
					'mailing_id': update.mailing_id
				})
				inserts += 1
				if inserts % 10000 == 0:
					cursor.sync ()
			logger.info (f'Inserted {inserts:,d} records into bounce_collect_tbl')
			cursor.sync ()
			logger.info (f'Read {records:,d} records ({uniques:,d} uniques) and inserted {inserts:,d}')
		#
		company_ids: List[int] = []
		query = 'SELECT distinct company_id FROM bounce_collect_tbl'
		for record in self.db.query (query):
			if record.company_id is not None and record.company_id > 0:
				company_ids.append (record.company_id)
	#}}}
	def finalize_timestamp (self, success: bool) -> None: #{{{
		logger.info ('Finalizing timestamp')
		self.timestamp.done (success)
		logger.info ('Timestamp finalized')
	#}}}
	def merge_new_bounces (self) -> None: #{{{
		logger.info ('Start merging new bounces into softbounce_email_tbl')
		with self.db.request () as icurs, self.db.request () as ucurs, self.db.request () as scurs, self.db.request () as dcurs:
			iquery = (
				'INSERT INTO softbounce_email_tbl '
				'           (company_id, email, bnccnt, mailing_id, creation_date, timestamp) '
				'VALUES '
				'           (:company_id, :email, 1, :mailing_id, current_timestamp, current_timestamp)'
			)
			uquery = (
				'UPDATE softbounce_email_tbl '
				'SET mailing_id = :mailing_id, timestamp = current_timestamp, bnccnt=bnccnt+1 '
				'WHERE company_id = :company_id AND email = :email'
			)
			squery = (
				'SELECT count(*) '
				'FROM softbounce_email_tbl '
				'WHERE company_id = :company_id AND email = :email'
			)
			dquery = (
				'DELETE FROM bounce_collect_tbl '
				'WHERE company_id = :company_id'
			)
			for company_id in (self.db.stream ('SELECT distinct company_id FROM bounce_collect_tbl')
				.filter (lambda r: self.companies[r.company_id].active)
				.map (lambda r: r.company_id)
				.sorted ()
				.list ()
			):
				logger.info ('Working on %d' % company_id)
				query = (
					'SELECT mt.customer_id, mt.mailing_id, cust.email '
					f'FROM bounce_collect_tbl mt, customer_{company_id}_tbl cust '
					'WHERE cust.customer_id = mt.customer_id '
					'AND mt.company_id = :company_id '
					'ORDER BY cust.email, mt.mailing_id'
				)
				for row in self.db.queryc (query, {'company_id': company_id}):
					parm = {
						'company_id': company_id,
						'customer_id': row.customer_id,
						'mailing_id': row.mailing_id,
						'email': row.email
					}
					data = scurs.querys (squery, parm, cleanup = True)
					if not data is None:
						if data[0] == 0:
							icurs.update (iquery, parm, cleanup = True)
						else:
							ucurs.update (uquery, parm, cleanup = True)
				parm = {
					'company_id': company_id
				}
				dcurs.update (dquery, parm, cleanup = True)
				self.db.sync ()
		logger.info ('Merging of new bounces done')
		#
		logger.info ('Remove active receivers from being watched')
		all_count = 0
		for company_id in (self.db.stream ('SELECT distinct company_id FROM softbounce_email_tbl')
			.filter (lambda r: self.companies[r.company_id].active)
			.map (lambda r: r.company_id)
			.sorted ()
			.list ()
		):
			table = 'success_%d_tbl' % company_id
			if self.companies[company_id].mailtracking and self.db.exists (table):
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
				if self.companies[company_id].mailtracking:
					logger.info (f'Skip removing active receivers for company_id {company_id} due to missing table {table}')
				else:
					logger.info (f'Skip removing active receivers for company_id {company_id} due to disabled mailtracking')
		logger.info (f'Finished removing {all_count:,d} receiver')
		#
		logger.info ('Fade out addresses')
		coll = self.__ccoll ('SELECT distinct company_id FROM softbounce_email_tbl', None, ('fade-out', ), (14, ))
		def query_data (key: Tuple[int, ...]) -> Optional[str]:
			if key[0] > 0:
				return 'UPDATE softbounce_email_tbl SET bnccnt = bnccnt - 1 WHERE timestamp <= :fade AND bnccnt > 0 AND company_id'
			return None
		def fill_data (data: Dict[str, Any], key: Tuple[int, ...]) -> None:
			ts = time.localtime (time.time () - key[0] * 24 * 60 * 60)
			data['fade'] = datetime (ts.tm_year, ts.tm_mon, ts.tm_mday)
		self.__cdo (coll, query_data, None, fill_data, 'Fade out non bounced watched')
		query = 'DELETE FROM softbounce_email_tbl WHERE bnccnt = 0'
		self.__do (query, None, 'Remove faded out addresses from softbounce_email_tbl')
		logger.info ('Fade out completed')
	#}}}
	def convert_to_hardbounce (self) -> None: #{{{
		logger.info ('Start converting softbounces to hardbounce')
		stats = []
		for company_id in (self.db.stream ('SELECT distinct company_id FROM softbounce_email_tbl')
			.filter (lambda r: self.companies[r.company_id].active)
			.map (lambda r: r.company_id)
			.sorted ()
			.list ()
		):
			with self.db.request () as dcurs, self.db.request () as ucurs, self.db.request () as scurs:
				cstat = [company_id, 0, 0]
				stats.append (cstat)
				logger.info ('Working on %d' % company_id)
				dquery = (
					'DELETE FROM softbounce_email_tbl '
					'WHERE company_id = %d AND email = :email'
					% company_id
				)
				uquery = (
					'UPDATE customer_%d_binding_tbl '
					'SET user_status = 2, user_remark = \'bounce:soft\', exit_mailing_id = :mailing_id, timestamp = current_timestamp '
					'WHERE customer_id = :customer_id AND user_status = 1'
					% company_id
				)
				bquery = (
					'INSERT INTO bounce_tbl '
					'            (company_id, customer_id, detail, mailing_id, timestamp, dsn) '
					'VALUES '
					'            (%d, :customer_id, 510, :mailing_id, current_timestamp, 599)'
					% company_id
				)
				squery =  'SELECT email, mailing_id, bnccnt, creation_date, timestamp FROM softbounce_email_tbl '
				bnccnt = self.__cfg (company_id, 'convert-bounce-count', 40)
				daydiff = self.__cfg (company_id, 'convert-bounce-duration', 30)
				squery += self.db.qselect (
					oracle = 'WHERE company_id = %d AND bnccnt > %d AND timestamp-creation_date > %d' % (company_id, bnccnt, daydiff),
					mysql = 'WHERE company_id = %d AND bnccnt > %d AND DATEDIFF(timestamp,creation_date) > %d' % (company_id, bnccnt, daydiff)
				)
				last_click = self.__cfg (company_id, 'last-click', 30)
				last_open = self.__cfg (company_id, 'last-open', 30)
				def to_datetime (offset: int) -> datetime:
					return datetime.fromordinal (datetime.now ().toordinal () - offset)
				last_click_timestamp = to_datetime (last_click)
				last_open_timestamp = to_datetime (last_open)
				click_cache = Cache (1000)
				open_cache = Cache (1000 * 1000)
				logger.info ('Setup rdir log cache for %d' % company_id)
				query = 'SELECT customer_id FROM rdirlog_%d_tbl WHERE timestamp > :ts' % company_id
				parm: Dict[str, Any] = {
					'ts': last_click_timestamp
				}
				with self.db.request () as ccurs:
					for record in ccurs.query (query, parm):
						click_cache.add (record[0])
					logger.info ('Setup one pixel log cache for %d' % company_id)
					query = 'SELECT customer_id FROM onepixellog_%d_tbl WHERE timestamp > :ts' % company_id
					parm = {'ts': last_open_timestamp}
					for record in ccurs.query (query, parm):
						open_cache.add (record[0])
				logger.info ('Setup completed')
				ccount = 0
				for record in scurs.queryc (squery):
					parm = {
						'email': record[0],
						'mailing_id': record[1],
						'bouncecount': record[2],
						'creationdate': record[3],
						'timestamp': record[4],
						'customer_id': 0
					}
					query =  'SELECT customer_id FROM customer_%d_tbl WHERE email = :email ' % company_id
					data = self.db.querys (query, parm, cleanup = True)
					if data is None:
						continue
					@dataclass
					class Stats:
						id: int
						click: int = 0
						open: int = 0
					custs = [Stats (id = _d) for _d in data if _d]
					if not custs:
						continue
					for c in custs:
						c.click += click_cache.get (c.id, 0)
						c.open += open_cache.get (c.id, 0)
					for c in custs:
						if c.click > 0 or c.open > 0:
							cstat[1] += 1
							logger.info ('Email %s [%d] has %d klick(s) and %d onepix(es) -> active' % (parm['email'], c.id, c.click, c.open))
						else:
							cstat[2] += 1
							logger.info ('Email %s [%d] has no klicks and no onepixes -> hardbounce' % (parm['email'], c.id))
							parm['customer_id'] = c.id
							ucurs.update (uquery, parm, cleanup = True)
							ucurs.update (bquery, parm, cleanup = True)
					dcurs.update (dquery, parm, cleanup = True)
					ccount += 1
					if ccount % 1000 == 0:
						logger.info (f'Commiting at {ccount:,d}')
						self.db.sync ()
				click_cache.done ()
				open_cache.done ()
				self.db.sync ()
		for cstat in stats:
			logger.info ('Company %d has %d active and %d marked as hardbounced users' % tuple (cstat))
		logger.info ('Converting softbounces to hardbounce done')
	#}}}

if __name__ == '__main__':
	Softbounce.main ()
