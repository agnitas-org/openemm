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
import	os, logging, argparse
import	time, re
from	datetime import datetime, timedelta
from	dataclasses import dataclass
from	typing import Dict, List, NamedTuple, Pattern, Set
from	agn3.db import DB
from	agn3.definitions import base, fqdn, user, ams
from	agn3.email import EMail
from	agn3.emm.config import EMMConfig, Responsibility
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.io import copen
from	agn3.lock import Lock
from	agn3.log import log
from	agn3.parser import Unit
from	agn3.runtime import CLI
from	agn3.template import Template
import	agn3.emm.mailing
#
logger = logging.getLogger (__name__)
#
class Mailing: #{{{
	meta = os.path.join (base, 'var', 'spool', 'META')
	archive = os.path.join (base, 'var', 'spool', 'ARCHIVE')
	track = os.path.join (base, 'var', 'track')
	def __init__ (self, status_id: int, status_field: str, mailing_id: int, company_id: int, check: List[str]) -> None: #{{{
		self.status_id = status_id
		self.status_field = status_field
		self.mailing_id = mailing_id
		self.company_id = company_id
		self.check = check
		self.seen: Set[int] = set ()
		self.tempFile = os.path.join (self.meta, '.recover-%d.temp' % self.status_id)
		self.recoverFile = os.path.join (self.meta, 'recover-%d.list' % self.status_id)
		self.count = 0
		self.active = True
		self.current = 0
		self.last = 0
	#}}}
	def done (self) -> None: #{{{
		for path in [self.tempFile, self.recoverFile]:
			if os.path.isfile (path):
				os.unlink (path)
	#}}}
	def __parse_xml (self, path: str) -> None: #{{{
		pattern = re.compile ('<receiver customer_id="([0-9]+)"')
		with copen (path) as fd:
			try:
				current = set ()
				mode = 0
				for line in fd:
					if mode == 0:
						if '<receivers>' in line:
							mode = 1
					elif mode == 1:
						mtch = pattern.search (line)
						if mtch is not None:
							current.add (int (mtch.groups ()[0]))
				self.seen.update (current)
			except IOError as e:
				logger.warning ('Failed to parse "%s": %r' % (path, e.args))
	#}}}
	def __collect (self, pattern: Pattern[str], path: str, remove: bool) -> None: #{{{
		files = os.listdir (path)
		for fname in [_f for _f in files if pattern.match (_f) is not None]:
			fpath = os.path.join (path, fname)
			if remove:
				try:
					os.unlink (fpath)
					logger.debug ('File "%s" removed' % fpath)
				except OSError as e:
					logger.error ('Failed to remove file "%s": %s' % (fpath, e))
			elif fname.endswith ('.xml.gz'):
				self.__parse_xml (fpath)
	#}}}
	def __collect_track (self, path: str) -> None: #{{{
		for fname in os.listdir (path):
			tpath = os.path.join (path, fname)
			with open (tpath, 'r') as fd:
				self.seen.update (int (_l.strip ().split (';')[0]) for _l in fd)
	#}}}
	def collect_seen (self) -> None: #{{{
		self.seen.clear ()
		if self.status_field not in ('A', 'T'):
			pattern = re.compile ('^AgnMail(-[0-9]+)?=D[0-9]{14}=%d=%d=[^=]+=liaMngA\\.(stamp|final|xml\\.gz)$' % (self.company_id, self.mailing_id))
			self.__collect (pattern, self.meta, True)
			for sdir in self.check:
				spath = os.path.join (self.archive, sdir)
				if os.path.isdir (spath):
					self.__collect (pattern, spath, False)
			track = os.path.join (self.track, str (self.company_id), str (self.mailing_id))
			if os.path.isdir (track):
				self.__collect_track (track)
	#}}}
	def create_filelist (self) -> None: #{{{
		if self.seen:
			with open (self.tempFile, 'w') as fd:
				fd.write ('\n'.join ([str (_s) for _s in self.seen]) + '\n')
			os.rename (self.tempFile, self.recoverFile)
	#}}}
	def set_generated_count (self, count: int) -> None: #{{{
		self.count = count
		logger.info ('Seen %d customers, logged %d' % (len (self.seen), self.count))
	#}}}
#}}}
class Recovery (CLI): #{{{
	@dataclass
	class MailingInfo:
		company_id: int
		name: str
		exists: bool
		deleted: bool

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument ('-n', '--dryrun', action = 'store_true', help = 'just show')
		parser.add_argument ('-A', '--age', action = "store", type = int, default = 1, help = 'set maximum age for a mailing to be recovered in days')
		parser.add_argument ('-D', '--startup-delay', action = "store", default = "1m", help = 'set delay to wait for startup of backend in seconds or an offset (e.g. "1m")', dest = 'startup_delay')
		parser.add_argument ('parameter', nargs = '*', help = 'optional list of mailing_ids to restrict recovery to')

	def use_arguments (self, args: argparse.Namespace) -> None:
		unit = Unit ()
		self.dryrun = args.dryrun
		self.max_age = args.age
		self.startup_delay = unit.parse (args.startup_delay)
		self.restrict_to_mailings = set (int (_p) for _p in args.parameter) if args.parameter else None

	def prepare (self) -> None:
		self.db = DB ()
		self.responsibilities = Responsibility (db = self.db)
		self.mailings: List[Mailing] = []
		self.mailing_info: Dict[int, Recovery.MailingInfo] = {}
		self.report: List[str] = []
		self.db.check_open ()
		
	def cleanup (self, success: bool) -> None:
		for m in self.mailings:
			m.done ()
		self.db.close ()

	def executor (self) -> bool:
		log.set_loglevel ('debug')
		try:
			if ams and self.startup_delay > 0:
				with log ('delay'):
					delay = self.startup_delay
					while self.running and delay > 0:
						delay -= 1
						time.sleep (1)

			if self.running:
				with Lock ():
					with log ('collect'):
						self.collect_mailings ()
					with log ('recover'):
						self.recover_mailings ()
					with log ('report'):
						self.report_mailings ()
			return True
		except error as e:
			logger.exception ('Failed recovery: %s' % e)
			return False
		
	def __make_range (self, start: datetime, end: datetime) -> List[str]: #{{{
		rc: List[str] = []
		current_day = start.toordinal ()
		end_day = end.toordinal ()
		while current_day <= end_day:
			day = datetime.fromordinal (current_day)
			rc.append (f'{day.year:04d}{day.month:02d}{day.day:02d}')
			current_day += 1
		return rc
	#}}}
	def __mail (self, mailing_id: int) -> Recovery.MailingInfo: #{{{
		with Ignore (KeyError):
			return self.mailing_info[mailing_id]
		#
		rq = self.db.querys ('SELECT company_id, shortname, deleted FROM mailing_tbl WHERE mailing_id = :mid', {'mid': mailing_id})
		self.mailing_info[mailing_id] = rc = Recovery.MailingInfo (
			company_id = rq.company_id if rq is not None else 0,
			name = rq.shortname if rq is not None else f'#{mailing_id} not found',
			exists = rq is not None,
			deleted = bool (rq.deleted) if rq is not None else False
		)
		return rc
	#}}}
	def __mailing_name (self, mailing_id: int) -> str: #{{{
		return self.__mail (mailing_id).name
	#}}}
	def __mailing_exists (self, mailing_id: int) -> bool: #{{{
		return self.__mail (mailing_id).exists
	#}}}
	def __mailing_deleted (self, mailing_id: int) -> bool: #{{{
		return self.__mail (mailing_id).deleted
	#}}}
	def __mailing_valid (self, mailing_id: int) -> bool: #{{{
		return self.__mailing_exists (mailing_id) and not self.__mailing_deleted (mailing_id)
	#}}}
	def collect_mailings (self) -> None: #{{{
		now = datetime.now ()
		expire = now - timedelta (days = self.max_age)
		yesterday = now - timedelta (days = 1)
		query = (
			'SELECT status_id, mailing_id, company_id, processed_by '
			'FROM maildrop_status_tbl '
			'WHERE genstatus = 2 AND status_field = \'R\' AND genchange > :expire AND genchange < CURRENT_TIMESTAMP'
		)
		check_query = self.db.qselect (
			oracle = 'SELECT count(*) FROM rulebased_sent_tbl WHERE mailing_id = :mid AND to_char (lastsent, \'YYYY-MM-DD\') = to_char (sysdate - 1, \'YYYY-MM-DD\')',
			mysql = 'SELECT count(*) FROM rulebased_sent_tbl WHERE mailing_id = :mid AND date_format(lastsent, \'%%Y-%%m-%%d\') = \'%04d-%02d-%02d\'' % (yesterday.year, yesterday.month, yesterday.day)
		)
		update = (
			'UPDATE maildrop_status_tbl '
			'SET genstatus = 1, genchange = CURRENT_TIMESTAMP '
			'WHERE status_id = :sid'
		)
		for row in (self.db.streamc (query, {'expire': expire})
			.filter (lambda r: r.company_id in self.responsibilities and (not r.processed_by or r.processed_by == fqdn))
			.filter (lambda r: self.restrict_to_mailings is None or r.mailing_id in self.restrict_to_mailings)
			.filter (lambda r: self.__mailing_valid (r.mailing_id))
		):
			count = self.db.querys (check_query, {'mid': row.mailing_id})
			if count is not None and count[0] == 1:
				logger.info ('Reactivate rule based mailing %d: %s' % (row.mailing_id, self.__mailing_name (row.mailing_id)))
				if not self.dryrun:
					self.db.update (update, {'sid': row.status_id})
				self.report.append ('%s [%d]: Reactivate rule based mailing' % (self.__mailing_name (row.mailing_id), row.mailing_id))
			else:
				logger.warning ('Rule based mailing %d (%s) not reactivated as it had not been sent out yesterday' % (row.mailing_id, self.__mailing_name (row.mailing_id)))
				self.report.append ('%s [%d]: Not reactivating rule based mailing as it had not been sent out yesterday' % (self.__mailing_name (row.mailing_id), row.mailing_id))
		if not self.dryrun:
			self.db.sync ()
		#
		query = (
			'SELECT status_id, mailing_id, company_id, status_field, genchange, senddate, processed_by '
			'FROM maildrop_status_tbl '
			'WHERE genstatus IN (1, 2) AND genchange > :expire AND genchange < CURRENT_TIMESTAMP AND status_field IN (\'A\', \'T\', \'W\')'
		)
		limit_restart_test_and_admin_mailings = datetime.now () - timedelta (hours = 1)
		for row in (self.db.streamc (query, {'expire': expire})
			.filter (lambda r: r.company_id in self.responsibilities and (not r.processed_by or r.processed_by == fqdn))
			.filter (lambda r: self.restrict_to_mailings is None or r.mailing_id in self.restrict_to_mailings)
			.filter (lambda r: self.__mailing_valid (r.mailing_id))
		):
			mailing_name = self.__mailing_name (row.mailing_id)
			if row.status_field == 'W' or (row.genstatus == 1 and row.genchange is not None and row.genchange > limit_restart_test_and_admin_mailings):
				check = self.__make_range (row.senddate, now)
				self.mailings.append (Mailing (row.status_id, row.status_field, row.mailing_id, row.company_id, check))
				logger.info ('Mark mailing %d (%s) for recovery' % (row.mailing_id, mailing_name))
			else:
				if self.db.update (
					'UPDATE maildrop_status_tbl '
					'SET genstatus = 4 '
					'WHERE status_id = :status_id',
					{
						'status_id': row.status_id
					}
				) > 0:
					logger.info (f'Mark {row.status_field}-mailing {row.mailing_id} ({mailing_name}) as finished in an unknown state')
				else:
					logger.warning (f'Failed to mark {row.status_id} for finished')
		if not self.dryrun:
			self.db.sync ()
		self.mailings.sort (key = lambda m: m.status_id)
		logger.info ('Found %d mailing(s) to recover' % len (self.mailings))
	#}}}
	def recover_mailings (self) -> None: #{{{
		if not self.dryrun and self.mailings and self.startup_delay > 0:
			logger.info ('Wait for backend to start up')
			n = self.startup_delay
			while n > 0:
				time.sleep (1)
				n -= 1
				if not self.running:
					raise error ('abort due to process termination')

		for m in self.mailings:
			m.collect_seen ()
			if self.dryrun:
				print ('%s: %d recipients already seen' % (self.__mailing_name (m.mailing_id), len (m.seen)))
			else:
				m.create_filelist ()
				count = 0
				for (total_mails, ) in self.db.query ('SELECT total_mails FROM mailing_backend_log_tbl WHERE status_id = :sid', {'sid': m.status_id}):
					if total_mails is not None and total_mails > count:
						count = total_mails
				m.set_generated_count (count)
				self.db.update ('DELETE FROM mailing_backend_log_tbl WHERE status_id = :sid', {'sid': m.status_id})
				self.db.update ('DELETE FROM world_mailing_backend_log_tbl WHERE mailing_id = :mid', {'mid': m.mailing_id})
				self.db.update ('UPDATE maildrop_status_tbl SET genstatus = 1 WHERE status_id = :sid', {'sid': m.status_id})
				self.db.sync ()
				logger.info ('Start backend using status_id %d for %s' % (m.status_id, self.__mailing_name (m.mailing_id)))
				starter = agn3.emm.mailing.Mailing ()
				if not starter.fire (status_id = m.status_id, cursor = self.db.cursor):
					logger.error ('Failed to trigger mailing %d' % m.mailing_id)
					self.report.append ('%s [%d]: Failed to trigger mailing' % (self.__mailing_name (m.mailing_id), m.mailing_id))
					break
				self.db.sync ()
			self.report.append ('%s [%d]: Start recovery using status_id %d' % (self.__mailing_name (m.mailing_id), m.mailing_id, m.status_id))
			if not self.dryrun:
				query = 'SELECT genstatus FROM maildrop_status_tbl WHERE status_id = :status_id'
				start = int (time.time ())
				ok = True
				last_generation_status = 1
				while self.running and m.active and ok:
					now = int (time.time ())
					self.db.sync (False)
					row = self.db.querys (query, {'status_id': m.status_id})
					if row is None or row[0] is None:
						logger.info ('Failed to query status for mailing %d' % m.mailing_id)
						self.report.append ('%s [%d]: Recovery failed due to missing status' % (self.__mailing_name (m.mailing_id), m.mailing_id))
						ok = False
					else:
						generation_status = row[0]
						if generation_status != last_generation_status:
							logger.info (f'Mailings {m.mailing_id} generation status has changed from {last_generation_status} to {generation_status}')
							last_generation_status = generation_status
						if generation_status == 3:
							logger.info ('Mailing %d terminated as expected' % m.mailing_id)
							self.report.append ('%s [%d]: Recovery finished' % (self.__mailing_name (m.mailing_id), m.mailing_id))
							m.active = False
						elif generation_status == 2:
							if m.last:
								current = 0
								for (currentMails, ) in self.db.query ('SELECT current_mails FROM mailing_backend_log_tbl WHERE status_id = :sid', {'sid': m.status_id}):
									if currentMails is not None:
										current = currentMails
								if current != m.current:
									logger.debug (f'Mailing {m.mailing_id} has created {current:,d} vs. {m.current:,d} when last checked')
									m.current = current
									m.last = now
								else:
									if (current > 0 and m.last + 1200 < now) or (current == 0 and m.last + 3600 < now):
										logger.info ('Mailing %d terminated due to inactivity after %d mails' % (m.mailing_id, current))
										self.report.append ('%s [%d]: Recovery timed out' % (self.__mailing_name (m.mailing_id), m.mailing_id))
										ok = False
							else:
								m.last = now
						elif generation_status == 1:
							if start + 1800 < now:
								logger.info ('Mailing %d terminated while not starting up' % m.mailing_id)
								self.report.append ('%s [%d]: Recovery not started' % (self.__mailing_name (m.mailing_id), m.mailing_id))
								ok = False
						elif generation_status > 3:
							logger.info ('Mailing %d terminated with status %d' % (m.mailing_id, generation_status))
							self.report.append ('%s [%d]: Recovery ended with unexpected status %d' % (self.__mailing_name (m.mailing_id), m.mailing_id, generation_status))
							m.active = False
					if m.active and ok:
						if start + 30 * 60 < now:
							logger.info ('Failed due to global timeout to recover %d' % m.mailing_id)
							self.report.append ('%s [%d]: Recovery ended due to global timeout' % (self.__mailing_name (m.mailing_id), m.mailing_id))
							ok = False
						else:
							time.sleep (1)
				if not m.active:
					count = 0
					for (total_mails, ) in self.db.query ('SELECT total_mails FROM mailing_backend_log_tbl WHERE status_id = :sid', {'sid': m.status_id}):
						if total_mails is not None:
							count = total_mails
					count += len (m.seen)
					self.db.update ('UPDATE mailing_backend_log_tbl SET total_mails = :cnt, current_mails = :cnt WHERE status_id = :sid', {'sid': m.status_id, 'cnt': count})
					self.db.update ('UPDATE world_mailing_backend_log_tbl SET total_mails = :cnt, current_mails = :cnt WHERE mailing_id = :mid', {'mid': m.mailing_id, 'cnt': count})
					self.db.sync ()
				if not self.running or not ok:
					break
	#}}}
	def report_mailings (self) -> None: #{{{
		class MailInfo (NamedTuple):
			status_id: int
			status_field: str
			mailing_id: int
			mailing_name: str
			company_id: int
			deleted: bool
			genchange: datetime
			senddate: datetime
		mails = []
		for row in (self.db.streamc (
				'SELECT status_id, mailing_id, company_id, genstatus, genchange, status_field, senddate, processed_by '
				'FROM maildrop_status_tbl '
				'WHERE genstatus IN (1, 2) AND status_field IN (\'W\', \'R\', \'D\')'
			)
			.filter (lambda r: r.company_id in self.responsibilities and (not r.processed_by or r.processed_by == fqdn))
			.filter (lambda r: bool (r.status_field == 'W' or r.genstatus == 2))
		):
			info = self.__mail (row.mailing_id)
			mails.append (
				MailInfo (
					status_id = row.status_id,
					status_field = row.status_field,
					mailing_id = row.mailing_id,
					mailing_name = info.name,
					company_id = info.company_id,
					deleted = info.deleted,
					genchange = row.genchange,
					senddate = row.senddate
				)
			)
		if self.report or mails:
			template = os.path.join (base, 'scripts', 'recovery3.tmpl')
			try:
				with open (template, 'r') as fd:
					content = fd.read ()
				ns = {
					'user': user,
					'host': fqdn,
					'report': self.report,
					'mails': mails
				}
				tmpl = Template (content)
				try:
					body = tmpl.fill (ns)
					charset = tmpl.property ('charset', default = 'UTF-8')
					subject = tmpl.property ('subject')
					if not subject:
						subject = tmpl['subject']
					if not subject:
						subject = 'Recovery report for %s' % ns['host']
					else:
						subject = Template (subject).fill (ns)
					with EMMConfig (db = self.db, class_names = ['recover-report']) as emmcfg:
						sender = emmcfg.get ('recover-report', 'sender', tmpl.property ('sender', f'{user}@{fqdn}'))
						receiver = emmcfg.get ('recover-report', 'receiver', tmpl.property ('receiver'))
					if receiver:
						receiver = Template (receiver).fill (ns)
						if self.dryrun:
							print ('From: %s' % sender)
							print ('To: %s' % receiver)
							print ('Subject: %s' % subject)
							print ('')
							print (body)
						else:
							EMail.force_encoding (charset, 'qp')
							mail = EMail ()
							if sender:
								mail.set_sender (sender)
							for recv in [_r.strip () for _r in receiver.split (',')]:
								if recv:
									mail.add_to (recv)
							if charset:
								mail.set_charset (charset)
							mail.set_subject (subject)
							mail.set_text (body)
							mail.send_mail ()
				except error as e:
					logger.error ('Failed to fill template "%s": %s' % (template, e))
			except IOError as e:
				logger.error ('Unable to find template "%s": %s' % (template, e))
	#}}}
#}}}
if __name__ == '__main__':
	Recovery.main ()
