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
import	logging, argparse
import	os, signal, shutil
import	time, json
from	collections import defaultdict
from	datetime import datetime, timedelta
from	dataclasses import dataclass
from	types import FrameType
from	typing import Any, Callable, Optional
from	typing import DefaultDict, Dict, List, NamedTuple, Tuple, Type
from	typing import cast
from	agn3.daemon import Daemonic
from	agn3.db import DB
from	agn3.definitions import base, program, fqdn, syscfg
from	agn3.emm.activator import Activator
from	agn3.emm.config import EMMConfig, Responsibility
from	agn3.emm.rulebased import Rulebased as RulebasedMailing
from	agn3.emm.mailing import Mailing
from	agn3.emm.metafile import METAFile
from	agn3.ignore import Ignore
from	agn3.io import ArchiveDirectory
from	agn3.flow import Schedule, Jobqueue
from	agn3.log import log, log_limit
from	agn3.parser import ParseTimestamp, Unit, Line, Field, Tokenparser
from	agn3.process import Processtitle
from	agn3.report import Report
from	agn3.runtime import Runtime
from	agn3.stream import Stream
from	agn3.tools import listsplit
#
logger = logging.getLogger (__name__)
#
@dataclass
class Entry:
	name: str
	company_id: int
	company_name: str
	mailing_id: int
	mailing_name: str
	status_id: int
	status_field: str
	senddate: datetime
	gendate: datetime
	genchange: datetime
	startdate: Optional[datetime] = None
	retry: Optional[datetime] = None
	# rulebased specific entries
	clearance_email: Optional[str] = None
	clearance_threshold: Optional[int] = None
#}}}
class Task: #{{{
	name = 'task'
	interval: str
	priority: int
	immediately = False
	def __init__ (self, ref: ScheduleGenerate) -> None:
		self.ref = ref
		self.db = DB ()
		self.unit = Unit ()
		
	def __del__ (self) -> None:
		self.close ()
	
	def __call__ (self) -> bool:
		rc = False
		if self.db.isopen ():
			self.ref.processtitle.push (self.name)
			try:
				self.execute ()
				self.ref.pendings ()
			except Exception as e:
				logger.exception ('failed due to: %s' % e)
			else:
				rc = True
			finally:
				self.close ()
				self.ref.processtitle.pop ()
				self.ref.lockTitle ()
		return rc
	
	def close (self) -> None:
		self.db.close ()
		
	def configuration (self, key: str, default: Any = None) -> Any:
		return self.ref.configuration (key, name = self.name, default = self.default_for (key, default))
	
	def title (self, title: Optional[str] = None) -> None:
		self.ref.processtitle ('%s%s' % (self.name, (' %s' % title) if title else ''))
	
	def default_for (self, key: str, default: Any) -> Any:
		return default
	#
	# to overwrite
	def execute (self) -> None:
		pass
#}}}
class Sending (Task): #{{{
	def __init__ (self, *args: Any, **kwargs: Any) -> None:
		super ().__init__ (*args, **kwargs)
		self.in_queue: Dict[int, Entry] = {}
		self.in_progress: Dict[int, Entry] = {}
		self.pending: Optional[List[int]] = None
		self.pending_path = os.path.join (base, 'var', 'run', 'generate-%s.pending' % self.name)
		self.load_pending ()
		self.datareader = log.datareader ('generate')
		self.generated: DefaultDict[int, List[Line]] = defaultdict (list)
	
	def load_pending (self) -> None:
		if os.path.isfile (self.pending_path):
			with open (self.pending_path) as fd:
				self.pending = json.load (fd)
				logger.debug ('loaded pending: %r' % (self.pending, ))
			os.unlink (self.pending_path)
	
	def save_pending (self, pending: List[int]) -> None:
		if pending:
			with open (self.pending_path, 'w') as fd:
				json.dump (pending, fd, indent = 2, sort_keys = True)
				logger.debug ('saved pending: %r' % (pending, ))
	
	def add_to_queue (self, entry: Entry) -> None:
		self.in_queue[entry.status_id] = entry
		logger.debug ('%s: added to queue' % entry.name)
	
	def remove_from_queue (self, entry: Entry) -> None:
		with Ignore (KeyError):
			removed_entry = entry
			del self.in_queue[entry.status_id]
			logger.debug ('%s: removed from queue' % removed_entry.name)

	def is_active (self) -> bool:
		return bool (self.in_queue or self.in_progress)
	
	def change_genstatus (self, status_id: int, old_status: Optional[int], new_status: int) -> bool:
		query = (
			'UPDATE maildrop_status_tbl '
			'SET genstatus = :new_status, genchange = :now, processed_by = :processed_by '
			'WHERE status_id = :status_id'
		)
		data = {
			'new_status': new_status,
			'now': datetime.now (),
			'processed_by': fqdn,
			'status_id': status_id
		}
		if old_status is not None:
			query += ' AND genstatus = :old_status'
			data['old_status'] = old_status
		return self.db.update (query, data, commit = True) == 1

	def invalidate_maildrop_entry (self, status_id: int, old_status: Optional[int] = None) -> bool:
		return self.change_genstatus (status_id, old_status = old_status, new_status = 4)
		
	def execute (self) -> None:
		new = not self.is_active ()
		self.collect_entries_for_sending ()
		self.pending = None
		if new and self.is_active ():
			self.ref.defer (self, self.starter)
	
	def starter (self) -> bool:
		if self.db.isopen ():
			def duration_format (d: int) -> str:
				return ('%d:%02d:%02d' % (d // 3600, (d // 60) % 60, d % 60)) if d >= 3600 else ('%d:%02d' % (d // 60, d % 60))
			mailing = Mailing (merger = self.configuration ('merger', 'localhost'))
			expire = self.unit.parse (self.configuration ('expire', '30m'))
			parallel = self.unit.parse (self.configuration ('parallel', '4'))
			startup = self.unit.parse (self.configuration ('startup', '5m'))
			now = datetime.now ()
			if self.in_progress:
				self.title ('checking %d mailings for temination' % len (self.in_progress))
				seen = set ()
				for row in self.db.queryc (
					'SELECT status_id, genstatus, genchange '
					'FROM maildrop_status_tbl '
					'WHERE status_id IN (%s)'
					% (Stream (self.in_progress.values ())
						.map (lambda e: e.status_id)
						.join (', '),
					)
				):
					seen.add (row.status_id)
					entry = self.in_progress.pop (row.status_id)
					if row.genstatus in (3, 4) or (row.genstatus == 1 and row.genchange > entry.genchange):
						logger.info ('{name}: generation finished after {duration}'.format (
							name = entry.name,
							duration = duration_format (int ((row.genchange - entry.startdate).total_seconds ()) if entry.startdate is not None else 0)
						))
						self.finished_entry (entry, self.find_generate_status (entry))
					elif entry.startdate is not None:
						duration = int ((now - entry.startdate).total_seconds ())
						if row.genstatus == 1:
							if duration >= startup:
								logger.warning ('%s: startup time exceeded, respool entry' % entry.name)
								if self.reschedule_entry (entry):
									self.add_to_queue (entry)
							else:
								logger.debug ('%s: during startup since %s up to %s' % (entry.name, duration_format (duration), duration_format (startup)))
								self.in_progress[entry.status_id] = entry
						elif row.genstatus == 2:
							if expire and duration > expire and self.expired_entry (entry):
								logger.info ('%s: creation exceeded expiration time, leave it running' % entry.name)
							else:
								logger.debug ('%s: still in generation for %s up to %s' % (entry.name, duration_format (duration), duration_format (expire)))
								self.in_progress[entry.status_id] = entry
						else:
							logger.error ('%s: unexpected genstatus %s, leave it alone' % (entry.name, row.genstatus))
					else:
						logger.warning (f'{entry}: unset startdate, but had been started, startdate set to {now}')
						entry.startdate = now
				self.db.sync ()
				for entry in Stream (self.in_progress.values ()).filter (lambda e: e.status_id not in seen).list ():
					logger.warning ('%s: maildrop status entry vanished, remove from observing' % entry.name)
					self.in_progress.pop (entry.status_id)
				self.title ()
			#
			if len (self.in_progress) < parallel and self.in_queue:
				if not self.ref._running:
					logger.info ('Postpone generation of %d mailings due to shutdown in progress' % len (self.in_queue))
					self.save_pending (list (self.in_queue.keys ()))
					self.in_queue.clear ()
					self.in_progress.clear ()
				else:
					self.title ('try to start %d out of %d mailings' % (min (len (self.in_queue), parallel - len (self.in_progress)), len (self.in_queue)))
					for entry in list (self.in_queue.values ()):
						if self.ref.islocked (entry.company_id) and entry.status_field != 'T':
							logger.debug ('%s: company %d is locked' % (entry.name, entry.company_id))
							continue
						if not self.start_entry (entry):
							logger.debug ('%s: start denied' % entry.name)
							continue
						if entry.retry is not None and now < entry.retry:
							logger.debug (f'{entry.name}: retry time {entry.retry} not reached')
							continue
						if not mailing.active ():
							logger.error ('%s: merger not active, abort' % entry.name)
							break
						self.remove_from_queue (entry)
						if self.ready_to_send (now, entry):
							if mailing.fire (status_id = entry.status_id, cursor = self.db.cursor):
								entry.startdate = now
								self.in_progress[entry.status_id] = entry
								logger.info ('%s: started' % entry.name)
								if len (self.in_progress) >= parallel:
									break
							else:
								logger.error ('%s: failed to start' % entry.name)
								if self.resume_entry (entry):
									self.add_to_queue (entry)
					self.db.sync ()
					self.title ()
		if not self.is_active ():
			logger.info (f'{self.name} batch finished')
		else:
			log_limit (logger.info, '{name} sending active with {in_queue} mailings queued and {in_progress} mailings currently in process'.format (
				name = self.name,
				in_queue = len (self.in_queue),
				in_progress = len (self.in_progress)
			), key = f'{self.name}:sending active', duration = '15m', limit_identical = True)
		return self.is_active ()
	
	tokenparser = Tokenparser (
		Field ('company_id', source = 'company', converter = int),
		Field ('mailinglist_id', source = 'mailinglist', converter = int),
		Field ('mailing_id', source = 'mailing', converter = int),
		Field ('count', converter = int),
		Field ('status_field'),
		Field ('timestamp', converter = ParseTimestamp (), optional = True)
	)
	def find_generate_status (self, entry: Entry) -> Optional[Line]:
		for line in self.datareader (entry.startdate):
			with Ignore ():
				record = self.tokenparser (line)
				self.generated[record.mailing_id].append (record)
		try:
			return (Stream (self.generated[entry.mailing_id])
				.filter (lambda l: bool (l.status_field == entry.status_field))
				.last ()
			)
		except (KeyError, ValueError):
			return None
	#
	# to overwrite
	def collect_entries_for_sending (self) -> None:
		pass
	def ready_to_send (self, now: datetime, entry: Entry) -> bool:
		return False
	def start_entry (self, entry: Entry) -> bool:
		return True
	def resume_entry (self, entry: Entry) -> bool:
		return False
	def reschedule_entry (self, entry: Entry) -> bool:
		return True
	def finished_entry (self, entry: Entry, status: Optional[Line]) -> None:
		pass
	def expired_entry (self, entry: Entry) -> bool:
		return True
#}}}
class Rulebased (Sending): #{{{
	name = 'rulebased'
	interval = '1h'
	immediately = True
	priority = 3
	defaults = {
		'expire':	'4h',
		'parallel':	'4',
		'startup':	'5m'
	}
	def default_for (self, key: str, default: Any) -> Any:
		try:
			return self.defaults[key]
		except KeyError:
			return default

	def ready_to_send (self, now: datetime, entry: Entry) -> bool:
		with RulebasedMailing (self.db) as rulebased:
			mailing = rulebased.retrieve (entry.mailing_id)
			if mailing.lastsent.toordinal () == now.toordinal ():
				retry = False
				if entry.retry is not None:
					entry.retry = None
					if entry.genchange is not None:
						rq = self.db.querys (
							'SELECT genchange '
							'FROM maildrop_status_tbl '
							'WHERE status_id = :status_id',
							{
								'status_id': entry.status_id
							}
						)
						if rq is None:
							logger.warning (f'{entry.name}: maildrop_status_tbl entry for {entry.status_id} has vanished')
						elif rq.genchange is None:
							logger.warning (f'{entry.name}: maildrop_status_tbl entry for {entry.status_id} has no current genchange')
						elif rq.genchange != entry.genchange:
							logger.info (f'{entry.name}: genchange has changed from {entry.genchange} to {rq.genchange}, assuming generation has already finished')
						else:
							logger.info (f'{entry.name}: retry time reached after failed start and no generation already detected')
							retry = True
				if not retry:
					logger.warning (f'{entry.name}: unexpected this mailing had been generated this day')
					return False
			#
			ready = False
			for state in 1, 2:
				rq = self.db.querys (
					'SELECT genchange '
					'FROM maildrop_status_tbl '
					'WHERE status_id = :status_id',
					{
						'status_id': entry.status_id
					}
				)
				if rq is None:
					break
				elif rq.genchange is None and state == 1:
					self.db.update (
						'UPDATE maildrop_status_tbl '
						'SET genchange = :now '
						'WHERE status_id = :status_id',
						{
							'now': now,
							'status_id': entry.status_id
						},
						commit = True
					)
				else:
					break
			if rq is not None and rq.genchange is not None:
				entry.genchange = rq.genchange
				mailing.lastsent = now
				if entry.clearance_threshold is not None:
					mailing.clearance = False
					mailing.status = 'process'
				else:
					mailing.clearance = True
				if rulebased.store (mailing):
					logger.debug (f'{entry.name}: lastsent date set, ready for generation')
					ready = True
				else:
					logger.error (f'{entry.name}: failed to set lastsent date')
			else:
				logger.error ('%s: failed to query current genchange' % entry.name)
			self.db.sync ()
			return ready

	def start_entry (self, entry: Entry) -> bool:
		return Stream (self.in_progress.values ()).filter (lambda e: bool (e.company_id == entry.company_id)).count () == 0
			
	def resume_entry (self, entry: Entry) -> bool:
		entry.retry = datetime.now () + timedelta (minutes = 5)
		return True

	def reschedule_entry (self, entry: Entry) -> bool:
		rulebased = RulebasedMailing (self.db)
		mailing = rulebased.retrieve (entry.mailing_id)
		mailing.lastsent = None
		if not rulebased.store (mailing):
			logger.error (f'{entry.name}: failed to reset lastsent date for reschedule')
			return False
		logger.info (f'{entry.name}: ready for reschedule')
		return True
		
	def finished_entry (self, entry: Entry, status: Optional[Line]) -> None:
		with RulebasedMailing (self.db) as rulebased:
			mailing = rulebased.retrieve (entry.mailing_id)
			if not mailing.clearance:
				if (
					status is not None
					and
					entry.clearance_threshold is not None
					and
					status.count > entry.clearance_threshold
					and
					(emails := list (listsplit (entry.clearance_email)))
				):
					logger.info (f'{entry.name}: recipient count {status.count:,d} exceeds threshold {entry.clearance_threshold:,d}, inform "{entry.clearance_email}", refuse clearance')
					self.send_threshold_exceeds_message (entry, status, entry.clearance_threshold, emails)
					mailing.status = 'refuse'
				else:
					if entry.clearance_threshold is not None:
						if status is None:
							logger.error (f'{entry.name}: failed to retrieve status for mailing-id {entry.mailing_id} even clearance threshold is set to {entry.clearance_threshold}, clearance is granted')
						elif not entry.clearance_email:
							logger.error (f'{entry.name}: no email address for clearance mail provided, force granting clearance when sending to {status.count} recipients and having a threshold of {entry.clearance_threshold}')
						elif status.count <= entry.clearance_threshold:
							logger.info (f'{entry.name}: number of recipients {status.count} does not exceed threshhold {entry.clearance_threshold}, clearance granted')
						else:
							logger.error (f'{entry.name}: unknown reason for missing clearance, granting it (status={status!r}, entry={entry!r})')
					mailing.clearance = True
					mailing.status = None
			else:
				mailing.status = None
			if not rulebased.store (mailing):
				logger.error (f'{entry.name}: failed to provide clearance for mailing')
			else:
				logger.debug (f'{entry.name}: clearance for mailing provided')
	
	def expired_entry (self, entry: Entry) -> bool:
		rq = self.db.querys (
			'SELECT clearance '
			'FROM rulebased_sent_tbl '
			'WHERE mailing_id = :mailing_id',
			{
				'mailing_id': entry.mailing_id
			}
		)
		if rq is None:
			logger.warning (f'{entry.name}: entry in rulebased_sent_tbl vanished, continue expiration')
			return True
		#
		if rq.clearance:
			logger.info (f'{entry.name}: clearance already granted, continue expiration')
			return True
		log_limit (logger.warning, f'{entry.name}: entry is ready to expire, but still no clearance granted, postpone expiration', duration = '30m')
		return False

	def send_threshold_exceeds_message (self, entry: Entry, status: Line, threshold: int, emails: List[str]) -> None:
		try:
			carbon_copies = syscfg.lget ('clearance-cc')
			blind_carbon_copies = syscfg.lget ('clearance-bcc')
			for email in emails:
				rq = self.db.querys (
					'SELECT admin_lang '
					'FROM admin_tbl '
					'WHERE company_id = :company_id AND lower(email) = :email',
					{
						'company_id': entry.company_id,
						'email': email.lower ()
					}
				)
				language = rq.admin_lang if rq is not None else None
				if not Report ('clearance', language = language).create (
					recipients = [email],
					carbon_copies = carbon_copies,
					blind_carbon_copies = blind_carbon_copies,
					namespace = {
						'entry': entry,
						'status': status,
						'threshold': threshold
					}
				):
					logger.error (f'{entry.name}: failed to send clearance request')
				else:
					logger.debug (f'{entry.name}: clearance request sent to {emails}')
				carbon_copies = None
				blind_carbon_copies = None
		except Exception as e:
			logger.exception (f'{entry.name}: failed to send report: {e}', e)

	def collect_entries_for_sending (self) -> None:
		now = datetime.now ()
		today = now.toordinal ()
		query = (
			'SELECT md.status_id, md.status_field, md.senddate, md.gendate, md.genchange, md.company_id, '
			'       co.shortname AS company_name, co.status, '
			'       mt.mailing_id, mt.shortname AS mailing_name, mt.deleted, mt.clearance_email, mt.clearance_threshold, '
			'       rb.lastsent, rb.clearance, rb.clearance_change '
			'FROM maildrop_status_tbl md '
			'     INNER JOIN company_tbl co ON (co.company_id = md.company_id) '
			'     INNER JOIN mailing_tbl mt ON (mt.mailing_id = md.mailing_id) '
			'     LEFT OUTER JOIN rulebased_sent_tbl rb ON (rb.mailing_id = md.mailing_id) '
			'WHERE md.genstatus = :genStatus AND md.status_field = :status_field AND md.senddate <= :now'
		)
		data = {
			'genStatus': 1,
			'status_field': 'R',
			'now': now
		}
		for row in self.db.queryc (query, data):
			msg = '%s (%d) for %s (%d)/%s' % (row.mailing_name, row.mailing_id, row.company_name, row.company_id, row.senddate)
			#
			# 1.) Skip not allowed companies
			if not self.ref.allow (row.company_id):
				logger.debug ('%s: is not on my list of allowed companies' % msg)
				continue
			#
			# 2.) Skip already sent mails for today
			if row.lastsent is not None and row.lastsent.toordinal () == today:
				if not row.clearance and row.clearance_change is not None and row.gendate is not None:
					if row.status_id not in self.in_progress:
						if row.clearance_change < row.gendate:
							logger.info (f'{msg}: no clearance available, add it for further processing')
							self.in_progress[row.status_id] = Entry (
								name = msg,
								company_id = row.company_id,
								company_name = row.company_name,
								mailing_id = row.mailing_id,
								mailing_name = row.mailing_name,
								status_id = row.status_id,
								status_field = row.status_field,
								senddate = row.senddate,
								gendate = row.gendate,
								genchange = row.clearance_change,
								startdate = now,
								clearance_email = row.clearance_email,
								clearance_threshold = row.clearance_threshold,
							)
						else:
							logger.warning (f'{msg}: no clearance available, but mailing not in processing any more ({row})')
				else:
					logger.debug (f'{msg}: already sent today')
				continue
			#
			# 3.) Skip already queued mails
			if row.status_id in self.in_queue:
				logger.debug ('%s: already in queue' % msg)
				continue
			#
			# 4.) Skip already processing entry
			if row.status_id in self.in_progress:
				logger.debug ('%s: is currently in production' % msg)
				continue
			#
			# 5.) Skip not yet ready to send
			if row.senddate.hour > now.hour:
				logger.debug ('%s: not yet ready to process' % msg)
				continue
			#
			# 6.) Skip outdated and keep pending entries
			if row.senddate.hour < now.hour:
				if self.pending is not None and row.status_id in self.pending:
					logger.debug ('%s: pending' % msg)
				else:
					logger.debug ('%s: outdated' % msg)
					continue
			#
			# 7.) Skip invalid company or mailing
			if row.status != 'active' or row.deleted:
				if row.status != 'active':
					logger.debug ('%s: company %s (%d) is not active, but %s' % (msg, row.company_name, row.company_id, row.status))
				if row.deleted:
					logger.debug ('%s: mailing marked as deleted' % msg)
				self.invalidate_maildrop_entry (row.status_id)
				continue
			#
			# 8.) Skip mailingings without a clearance and stale blocks
			if row.clearance is not None and row.clearance == 0:
				if not self.clear_stale_blocks (row.mailing_id):
					logger.warning (f'{msg}: no clearance to send and failed to cleanup old files')
					continue
				else:
					logging.info (f'{msg}: no clearance for previous run granted, cleaned up old files')
			#
			self.add_to_queue (
				Entry (
					name = msg,
					company_id = row.company_id,
					company_name = row.company_name,
					mailing_id = row.mailing_id,
					mailing_name = row.mailing_name,
					status_id = row.status_id,
					status_field = row.status_field,
					senddate = row.senddate,
					gendate = row.gendate,
					genchange = row.genchange,
					clearance_email = row.clearance_email,
					clearance_threshold = row.clearance_threshold
				)
			)
		#
		self.db.sync ()
	
	def clear_stale_blocks (self, mailing_id: int) -> bool:
		blocks = (Stream (os.listdir (METAFile.meta_directory))
			.map (lambda f: METAFile (os.path.join (METAFile.meta_directory, f)))
			.filter (lambda m: m.valid and m.mailing_id == mailing_id and m.extension != 'xml')
			.sorted (key = lambda m: m.filename)
			.list ()
		)
		failures = 0
		for block in blocks:
			try:
				shutil.move (block.path, os.path.join (ArchiveDirectory.make (METAFile.outdated_directory), block.filename))
				logger.info (f'{block.path}: moved to {METAFile.outdated_directory}')
			except OSError as e:
				logger.warning (f'{block.path}: failed to move to {METAFile.outdated_directory}: {e}')
				failures += 1
		return failures == 0
#}}}
class Worldmailing (Sending): #{{{
	name = 'generate'
	interval = '15m'
	priority = 4
	def ready_to_send (self, now: datetime, entry: Entry) -> bool:
		if not self.change_genstatus (entry.status_id, old_status = 0, new_status = 1):
			logger.error ('%s: failed to update maildrop status table' % entry.name)
			return False
		#
		try:
			entry.genchange = self.db.streamc (
				'SELECT genchange FROM maildrop_status_tbl WHERE status_id = :status_id',
				{
					'status_id': entry.status_id
				}
			).map_to (datetime, lambda r: r.genchange).first ()
		except ValueError:
			logger.error ('%s: failed to query current gen change' % entry.name)
			if not self.resume_entry (entry):
				return False
		self.db.sync ()
		return True

	def resume_entry (self, entry: Entry) -> bool:
		if not self.change_genstatus (entry.status_id, old_status = 1, new_status = 0):
			logger.critical ('%s: failed to revert status id from 1 to 0' % entry.name)
			return False
		else:
			logger.info ('%s: resumed setting genstatus from 1 to 0' % entry.name)
			return True

	def collect_entries_for_sending (self) -> None:
		query = (
			'SELECT md.status_id, md.status_field, md.senddate, md.gendate, md.genchange, md.company_id, md.optimize_mail_generation, '
			'       co.shortname AS company_name, co.status, mt.mailing_id, mt.shortname AS mailing_name, mt.deleted '
			'FROM maildrop_status_tbl md INNER JOIN company_tbl co ON (co.company_id = md.company_id) INNER JOIN mailing_tbl mt ON (mt.mailing_id = md.mailing_id) '
			'WHERE md.genstatus = 0 AND ( '
			'      (md.status_field = \'W\' AND md.gendate <= :now) '
			'      OR '
			'      (md.status_field = \'T\' AND md.senddate <= :now) '
			') ORDER BY md.status_id'
		)
		now = datetime.now ()
		limit = None
		if self.ref.oldest >= 0:
			limit = now.fromordinal (now.toordinal () - self.ref.oldest)
		for row in self.db.queryc (query, {'now': now}):
			msg = 'Mailing "%s" (mailing_id=%d, status_id=%d, status_field=%r, company=%s, company_id=%d)' % (
				row.mailing_name, row.mailing_id, 
				row.status_id, row.status_field,
				row.company_name, row.company_id
			)
			#
			if not self.ref.allow (row.company_id):
				logger.debug ('%s: not in my list of allowed companies' % msg)
				continue
			#
			startIt = False
			keepIt = False
			if row.status != 'active':
				logger.info ('%s: company is not active, but %s' % (msg, row.status))
			elif row.deleted:
				logger.info ('%s: mailing is marked as deleted' % msg)
			elif limit is not None and row.gendate is not None and row.gendate < limit:
				logger.info ('%s: mailing gendate is too old (%s, limit is %s)' % (msg, str (row.gendate), str (limit)))
			else:
				if (
					row.optimize_mail_generation is not None and
					row.optimize_mail_generation == 'day' and
					row.senddate is not None and
					row.senddate.toordinal () > now.toordinal ()
				):
					logger.info ('%s: day based optimized mailing will not be generate on a previous day, deferred' % msg)
					keepIt = True
				else:
					startIt = True
			if startIt:
				if row.status_id in self.in_queue:
					logger.info ('%s: already queued' % msg)
				else:
					self.add_to_queue (
						Entry (
							name = msg,
							company_id = row.company_id,
							company_name = row.company_name,
							mailing_id = row.mailing_id,
							mailing_name = row.mailing_name,
							status_id = row.status_id,
							status_field = row.status_field,
							senddate = row.senddate,
							gendate = row.gendate,
							genchange = row.genchange
						)
					)
					logger.info ('%s: added to queue' % msg)
			else:
				if row.status_id in self.in_queue:
					logger.info ('%s: queued, remove it from queue' % msg)
					self.remove_from_queue (row.status_id)
				if not keepIt:
					if self.invalidate_maildrop_entry (row.status_id, old_status = 0):
						logger.info ('%s: disabled' % msg)
					else:
						logger.error ('%s: failed to disable' % msg);
		self.db.sync ()
#}}}
class ScheduleGenerate (Schedule): #{{{
	__slots__ = [
		'modules', 'oldest', 'processes',
		'control', 'deferred', 'config',
		'responsibilities', 'responsibility_recheck',
		'locks', 'processtitle']
	@dataclass
	class Pending:
		description: str
		method: Callable[..., bool]
		prepare: Optional[Callable[..., None]]
		finalize: Optional[Callable[..., None]]
		args: Tuple[Any, ...]
		kwargs: Dict[str, Any]
		pid: Optional[int]
	class Control (NamedTuple):
		subprocess: Daemonic
		queued: Dict[int, List[ScheduleGenerate.Pending]]
		running: List[ScheduleGenerate.Pending]
	class Deferred (NamedTuple):
		task: Task
		description: str
		method: Callable[..., bool]
		args: Tuple[Any, ...]
		kwargs: Dict[str, Any]
	def __init__ (self, modules: Dict[str, Type[Task]], oldest: int, processes: int, *args: Any, **kwargs: Any) -> None:
		super ().__init__ (*args, **kwargs)
		self.modules = modules
		self.oldest = oldest
		self.processes = processes
		self.control = ScheduleGenerate.Control (Daemonic (), {}, [])
		self.deferred: List[ScheduleGenerate.Deferred] = []
		self.config: Dict[str, str] = {}
		self.responsibilities = Responsibility ()
		self.responsibility_recheck = True
		self.locks: Dict[int, int] = {}
		self.processtitle = Processtitle ('$original [$title]')

	def intercept (self) -> None:
		self.responsibility_recheck = True

	def read_configuration (self) -> None:
		self.config = (Stream (EMMConfig (class_names = ['generate']).scan (single_value = True))
			.map (lambda cv: (cv.name, cv.value))
			.dict ()
		)
	
	def configuration (self, key: str, name: Optional[str] = None, default: Any = None, convert: Optional[Callable[[Any], Any]] = None) -> Any:
		return (
			Stream ([
				('%s:%s' % (name, key)) if name is not None else None,
				key
			])
			.filter (lambda k: k is not None and k in self.config)
			.map (lambda k: self.config[cast (str, k)])
			.first (no = default, finisher = lambda v: v if convert is None or v is None else convert (v))
		)
	
	def allow (self, company_id: int) -> bool:
		if self.responsibility_recheck:
			self.responsibilities.check (force = True)
			self.responsibility_recheck = False
		return company_id in self.responsibilities

	def title (self, info: Optional[str] = None) -> None:
		self.processtitle ('schedule%s' % ((' %s' % info) if info else ''))

	def reload (self, sig: int, stack: Optional[FrameType]) -> Any:
		self.read_configuration ()
		
	def status (self, sig: int, stack: Optional[FrameType]) -> Any:
		self.show_status ()
	
	def show_status (self) -> None:
		logger.info ('Currently blocked companies: %s' % (Stream (self.locks.items ()).map (lambda kv: '%s=%r' % kv).join (', ') if self.locks else 'none', ))
		for (name, queue) in Stream (self.control.queued.items ()).map (lambda kv: ('queued prio %d' % kv[0], kv[1])).list () + [('running', self.control.running)]:
			logger.info ('Currently %d %s processes' % (len (queue), name))
			for entry in queue:
				logger.info ('  %s%s' % (entry.description, (' (%d)' % entry.pid if entry.pid is not None else '')))
		for job in self.deferred:
			logger.info ('%s: deferred' % job.description)
		if self._schedule.queue:
			logger.info ('Currently scheduled events:')
			for event in self._schedule.queue:
				ts = event.time % (24 * 60 * 60)
				logger.info ('\t%2d:%02d:%02d [Prio %d]: %s' % (
					ts // 3600, (ts // 60) % 60, ts % 60,
					event.priority, cast (Schedule.Job, event.action).name
				))
	
	def log (self, area: str, message: str) -> None:
		logger.debug (f'sched/{area}: {message}')

	def start (self) -> None:
		self.title ()
		self.read_configuration ()
		(Stream (self.modules.values ())
			.sorted (key = lambda task: task.priority)
			.each (lambda task: self.every (
				task.name,
				self.configuration (
					'immediately',
					name = task.name,
					default = task.immediately
				),
				self.configuration (
					'interval',
					name = task.name,
					default = task.interval,
					convert = lambda v: Stream.ifelse (v, alternator = task.interval)
				),
				task.priority,
				task (self),
				()
			))
		)
		def configurator () -> bool:
			self.read_configuration ()
			return True
		def stopper () -> bool:
			while self.control.running:
				if self.wait () is None:
					break
			if not self.control.running and not self.deferred:
				self.stop ()
				return False
			return True
		self.every ('reload', False, '1h', 0, configurator, ())
		self.every ('restart', False, '24h', 0, stopper, ())
		super ().start ()
	
	def term (self) -> None:
		super ().term ()
		if self.control.running:
			self.title ('in termination')
			(Stream (self.control.running)
				.peek (lambda p: logger.info ('Sending terminal signal to %s' % p.description))
				.each (lambda p: self.control.subprocess.term (cast (int, p.pid)))
			)
			while self.control.running:
				logger.info ('Waiting for %d remaining child processes' % len (self.control.running))
				self.wait (True)
		if self.deferred:
			logger.info ('Schedule final run for %d deferred tasks' % len (self.deferred))
			self.defers ()

	def wait (self, block: bool = False) -> Optional[ScheduleGenerate.Pending]:
		w: Optional[ScheduleGenerate.Pending] = None
		while self.control.running and w is None:
			rc = self.control.subprocess.join (timeout = None if block else 0)
			if not rc.pid:
				break
			#
			w = (Stream (self.control.running)
				.filter (lambda r: bool (r.pid == rc.pid))
				.first (no = None)
			)
			if w is not None:
				logger.debug ('{desc}: returned with {ec}'.format (
					desc = w.description,
					ec = f'exit with {rc.exitcode}' if rc.exitcode is not None else f'died due to signal {rc.signal}'
				))
				if w.finalize is not None:
					try:
						w.finalize (rc, *w.args, **w.kwargs)
					except Exception as e:
						logger.exception ('%s: finalize fails: %s' % (w.description, e))
				self.control.running.remove (w)
				self.lockTitle ()
		return w

	def defers (self) -> bool:
		for job in self.deferred[:]:
			logger.debug ('%s: starting' % job.description)
			self.processtitle.push (job.description)
			try:
				if not job.method (*job.args, **job.kwargs):
					logger.debug ('%s: finished' % job.description)
					self.deferred.remove (job)
					job.task.close ()
				else:
					logger.debug ('%s: still active' % job.description)
			finally:
				self.processtitle.pop ()
		return bool (self.deferred)
	
	def defer (self, task: Task, method: Callable[..., bool], *args: Any, **kwargs: Any) -> None:
		self.deferred.append (ScheduleGenerate.Deferred (task, task.name, method, args, kwargs))
		if len (self.deferred) == 1:
			self.every ('defer', False, '10s', 0, self.defers, ())

	def pendings (self) -> bool:
		while self.control.running:
			pending = self.wait ()
			if pending is None:
				break
			logger.debug ('%s: process finished' % pending.description)
		while self.control.queued and len (self.control.running) < self.processes:
			prio = Stream (self.control.queued.keys ()).sorted ().first ()
			w = self.control.queued[prio].pop (0)
			if not self.control.queued[prio]:
				del self.control.queued[prio]
			#
			if w.prepare is not None:
				try:
					w.prepare (*w.args, **w.kwargs)
				except Exception as e:
					logger.exception ('%s: prepare fails: %s' % (w.description, e))
			def starter () -> bool:
				self.processtitle (w.description)
				rc = w.method (*w.args, **w.kwargs)
				self.processtitle ()
				return rc
			w.pid = self.control.subprocess.spawn (starter)
			self.control.running.append (w)
			logger.debug ('%s: launched' % w.description)
		return bool (self.control.running or self.control.queued)

	def launch (self,
		prio: int,
		description: str,
		method: Callable[..., bool],
		prepare: Optional[Callable[..., None]],
		finalize: Optional[Callable[..., None]],
		*args: Any,
		**kwargs: Any
	) -> None:
		active = self.pendings ()
		launching = ScheduleGenerate.Pending (
			description = description,
			method = method,
			prepare = prepare,
			finalize = finalize,
			args = args,
			kwargs = kwargs,
			pid = None
		)
		try:
			self.control.queued[prio].append (launching)
		except KeyError:
			self.control.queued[prio] = [launching]
		if not active:
			self.every ('pending', False, '1m', 0, self.pendings, ())

	def lockTitle (self) -> None:
		self.title (Stream (self.locks.items ())
			.map (lambda kv: '%s=%r' % kv)
			.join (', ', lambda s: ('active locks %s' % s) if s else s)
		)

	def islocked (self, key: int) -> bool:
		return key in self.locks
	
	def lock (self, key: int) -> None:
		try:
			self.locks[key] += 1
			logger.debug ('%r increased' % key)
		except KeyError:
			self.locks[key] = 1
			logger.debug ('%r set' % key)
	
	def unlock (self, key: int) -> None:
		with Ignore (KeyError):
			self.locks[key] -= 1
			if self.locks[key] <= 0:
				del self.locks[key]
				logger.debug ('%r removed' % key)
			else:
				logger.debug ('%r decreased' % key)
#}}}
class JobqueueGenerate (Jobqueue): #{{{
	__slots__: List[str] = ['reload', 'status']
	def __init__ (self, schedule: ScheduleGenerate) -> None:
		super ().__init__ (schedule)
		self.reload = schedule.reload
		self.status = schedule.status
		schedule.processtitle ('watchdog')

	def setup_handler (self, master: bool = False) -> None:
		super ().setup_handler (master)
		if not master:
			self.setsignal (signal.SIGHUP, self.reload)
			self.setsignal (signal.SIGUSR1, self.status)
#}}}
class Generate (Runtime):
	__slots__ = ['oldest', 'processes', 'modules']
	def supports (self, option: str) -> bool:
		return option != 'dryrun'

	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		unit = Unit ()
		parser.add_argument (
			'-O', '--oldest',
			action = 'store', type = unit, default = unit ('3d'),
			help = 'oldest entry to process'
		)
		parser.add_argument (
			'-P', '--processes', '--parallel',
			action = 'store', type = int, default = 4,
			help = 'specifiy the number of parallel generation processes'
		)
	
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.oldest = args.oldest
		self.processes = args.processes
		self.modules = args.parameter
	
	def executor (self) -> bool:
		with Activator () as activator:
			modules = (Stream (globals ().values ())
				.filter (lambda module: type (module) is type and issubclass (module, Task) and hasattr (module, 'interval'))
				.filter (lambda module: bool (activator.check (['%s-%s' % (program, module.name)])))
				.map (lambda module: (module.name, module))
				.dict ()
			)
		logger.info ('Active modules: %s' % ', '.join (sorted (modules.keys ())))
		schedule = ScheduleGenerate (modules, self.oldest, self.processes)
		if self.modules:
			schedule.read_configuration ()
			for name in self.modules:
				if name not in modules:
					print ('** %s not known' % name)
				else:
					logger.info (f'Module "{name}" found')
					module = modules[name] (schedule)
					rc = module ()
					schedule.show_status ()
					logger.info ('Module returns %r' % (rc, ))
					if schedule.control.queued or schedule.control.running:
						logger.info ('Execute backgound processes')
						try:
							while schedule.control.queued:
								schedule.show_status ()
								schedule.pendings ()
								if len (schedule.control.running) == self.processes:
									logger.info ('Currently %d processes running, wait for at least one to terminate' % len (schedule.control.running))
									schedule.show_status ()
									schedule.wait (True)
							logger.info ('Wait for %d background process to teminate' % len (schedule.control.running))
							while schedule.control.running:
								schedule.show_status ()
								if not schedule.wait (True):
									break
						except KeyboardInterrupt:
							logger.info ('^C, terminate all running processes')
							for p in schedule.control.running[:]:
								if p.pid is not None:
									schedule.control.subprocess.term (p.pid)
									schedule.wait ()
								else:
									schedule.control.running.remove (p)
							logger.info ('Waiting for 2 seconds to kill all remaining processes')
							time.sleep (2)
							for p in schedule.control.running[:]:
								if p.pid is not None:
									schedule.control.subprocess.term (p.pid, signal.SIGKILL)
								else:
									schedule.control.running.remove (p)
							logger.info ('Waiting for killed processes to terminate')
							while schedule.wait (True) is not None:
								pass
						logger.info ('Background processes done')
					if schedule.deferred:
						logger.info ('Deferred jobs active, process them')
						try:
							last = -1
							while schedule.defers ():
								cur = len (schedule.deferred)
								if cur != last:
									logger.info ('%d jobs remaining' % cur)
									last = cur
								time.sleep (1)
						except KeyboardInterrupt:
							logger.info ('^C, terminating')
		else:
			jq = JobqueueGenerate (schedule)
			jq.start (restart_delay = '1m', termination_delay = '5m')
		return True
#}}}
if __name__ == '__main__':
	Generate.main ()
