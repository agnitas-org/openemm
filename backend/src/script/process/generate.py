#!/usr/bin/env python2
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
#	-*- mode: python; mode: fold -*-
#
import	sys, os, getopt
import	time, datetime, json, collections
import	agn, eagn
try:
	from	activator import Activator
except ImportError:
	class Activator (object):
		def check (self, *args, **kws):
			return True
agn.loglevel = agn.LV_DEBUG

class Entry (object): #{{{
	__slots__ = ['name', 'companyID', 'mailingID', 'statusID', 'statusField', 'sendDate', 'genDate', 'genChange', 'startDate']
	def __init__ (self, name, companyID, mailingID, statusID, statusField, sendDate, genDate, genChange, startDate = None):
		if name is None:
			self.name = '#(statusID) %d' % statusID
		else:
			self.name = name
		self.companyID = companyID
		self.mailingID = mailingID
		self.statusID = statusID
		self.statusField = statusField
		self.sendDate = sendDate
		self.genDate = genDate
		self.genChange = genChange
		self.startDate = startDate
	
	def __repr__ (self):
		return 'Entry (%r, %r, %r, %r, %r, %r, %r, %r, %r)' % (self.name, self.companyID, self.mailingID, self.statusID, self.statusField, self.sendDate, self.genDate, self.genChange, self.startDate)
	__str__ = __repr__
#}}}
class Task (object): #{{{
	name = 'task'
	immediately = False
	def __init__ (self, ref):
		self.ref = ref
		self.db = None
		self.unit = eagn.Unit ()
		
	def __del__ (self):
		self.close ()
		
	def __call__ (self):
		rc = True
		if self.open ():
			self.ref.processtitle.push (self.name)
			try:
				self.execute ()
				self.ref.pendings ()
			except Exception as e:
				agn.logexc (agn.LV_ERROR, self.name, 'failed due to: %s' % e)
				rc = False
			finally:
				self.close ()
				self.ref.processtitle.pop ()
				self.ref.lockTitle ()
		return rc
	
	def configuration (self, key, default = None):
		return self.ref.configuration (key, name = self.name, default = self.default_for (key, default))
	
	def title (self, title = None):
		self.ref.processtitle ('%s%s' % (self.name, (' %s' % title) if title else ''))
	
	def log (self, level, message):
		agn.log (level, self.name, message)

	def open (self):
		for state in 1, 2:
			if self.db is None:
				self.db = eagn.DB ()
			if not self.db.isopen ():
				self.log (agn.LV_ERROR, 'Failed to open database: %s' % self.db.lastError ())
				self.close ()
			else:
				break
		return self.db is not None
	
	def close (self):
		if self.db is not None:
			self.db.close ()
			self.db = None
	
	def default_for (self, key, default):
		return default
#}}}
class Sending (Task): #{{{
	def __init__ (self, *args, **kws):
		super (Sending, self).__init__ (*args, **kws)
		self.in_queue = collections.OrderedDict ()
		self.in_progress = {}
		self.pending = None
		self.pending_path = agn.mkpath (agn.base, 'var', 'run', 'generate-%s.pending' % self.name)
		self.load_pending ()
	
	def load_pending (self):
		if os.path.isfile (self.pending_path):
			with open (self.pending_path, 'rb') as fd:
				self.pending = json.load (fd)
				self.log (agn.LV_DEBUG, 'loaded pending: %r' % (self.pending, ))
			os.unlink (self.pending_path)
	
	def save_pending (self, pending):
		if pending:
			with open (self.pending_path, 'wb') as fd:
				json.dump (pending, fd, indent = 2, sort_keys = True)
				self.log (agn.LV_DEBUG, 'saved pending: %r' % (pending, ))
	
	def add_to_queue (self, entry):
		self.in_queue[entry.statusID] = entry
		self.log (agn.LV_DEBUG, '%s: added to queue' % entry.name)
	
	def remove_from_queue (self, entry):
		with agn.Ignore (KeyError):
			removed_entry = entry
			try:
				del self.in_queue[entry.statusID]
			except AttributeError:
				removed_entry = self.in_queue[entry]
				del self.in_queue[entry]
			self.log (agn.LV_DEBUG, '%s: removed from queue' % removed_entry.name)

	def is_active (self):
		return self.in_queue or self.in_progress
	
	def change_genstatus (self, status_id, old_status, new_status):
		query = (
			'UPDATE maildrop_status_tbl '
			'SET genstatus = :newStatus, genchange = :now '
			'WHERE status_id = :statusID'
		)
		data = {
			'newStatus': new_status,
			'now': datetime.datetime.now (),
			'statusID': status_id
			}
		if old_status is not None:
			query += ' AND genstatus = :oldStatus'
			data['oldStatus'] = old_status
		return self.db.cursor.execute (query, data, commit = True) == 1

	def invalidate_maildrop_entry (self, status_id, old_status = None):
		return self.change_genstatus (status_id, old_status = old_status, new_status = 4)
		
	def execute (self):
		new = not self.is_active ()
		self.collect_entries_for_sending ()
		self.pending = None
		if new and self.is_active ():
			self.ref.defer (self, self.starter)
	
	def starter (self):
		if self.open ():
			duration_format = lambda d: ('%d:%02d:%02d' % (d // 3600, (d // 60) % 60, d % 60)) if d >= 3600 else ('%d:%02d' % (d // 60, d % 60))
			mailing = eagn.Mailing (merger = self.configuration ('merger', 'localhost'))
			expire = self.unit.parse (self.configuration ('expire', '30m'))
			parallel = self.unit.parse (self.configuration ('parallel', '4'))
			startup = self.unit.parse (self.configuration ('startup', '5m'))
			now = datetime.datetime.now ()
			if self.in_progress:
				self.title ('checking %d mailings for temination' % len (self.in_progress))
				seen = set ()
				for row in self.db.cursor.queryc (
					'SELECT status_id, genstatus, genchange '
					'FROM maildrop_status_tbl '
					'WHERE status_id IN (%s)'
					% (agn.Stream (self.in_progress.itervalues ())
						.map (lambda e: e.statusID)
						.join (', '),
					)
				):
					seen.add (row.status_id)
					entry = self.in_progress.pop (row.status_id)
					if row.genstatus in (3, 4) or (row.genstatus == 1 and row.genchange > entry.genChange):
						duration = int ((row.genchange - entry.startDate).total_seconds ())
						self.log (agn.LV_INFO, '%s: generation finished after %s' % (entry.name, duration_format (duration)))
					else:
						duration = int ((now - row.genchange).total_seconds ())
						if row.genstatus == 1:
							if duration >= startup:
								self.log (agn.LV_WARNING, '%s: startup time exceeded, respool entry' % entry.name)
								if self.db.cursor.execute (
									'DELETE FROM rulebased_sent_tbl WHERE mailing_id = :mailingID',
									{
										'mailingID': entry.mailingID
									},
									commit = True
								) > 0:
									self.log (agn.LV_INFO, '%s: entry from rulebased_sent_tbl had been removed' % entry.name)
								else:
									self.log (agn.LV_INFO, '%s: no entry for %s in rulebased_sent_tbl found to remove' % entry.name)
								self.add_to_queue (entry)
							else:
								self.log (agn.LV_DEBUG, '%s: during startup since %s up to %s' % (entry.name, duration_format (duration), duration_format (startup)))
								self.in_progress[entry.statusID] = entry
						elif row.genstatus == 2:
							if duration > expire:
								self.log (agn.LV_WARNING, '%s: creation exceeded expiration time, leave it running' % entry.name)
							else:
								self.log (agn.LV_DEBUG, '%s: still in generation for %s up to %s' % (entry.name, duration_format (duration), duration_format (expire)))
								self.in_progress[entry.statusID] = entry
						else:
							self.log (agn.LV_ERROR, '%s: unexpected genstatus %s, leave it alone' % (entry.name, row.genstatus))
				self.db.commit ()
				for entry in agn.Stream (self.in_progress.itervalues ()).filter (lambda e: e.statusID not in seen).list ():
					self.log (agn.LV_WARNING, '%s: maildrop status entry vanished, remove from observing' % entry.name)
					self.in_progress.pop (entry.statusID)
				self.title ()
			#
			if len (self.in_progress) < parallel and self.in_queue:
				if not self.ref._running:
					self.log (agn.LV_INFO, 'Postpone generation of %d mailings due to shutdown in progress' % len (self.in_queue))
					self.save_pending (self.in_queue.keys ())
					self.in_queue.clear ()
					self.in_progress.clear ()
				else:
					self.title ('try to start %d out of %d mailings' % (parallel - len (self.in_progress), len (self.in_queue)))
					for entry in list (self.in_queue.itervalues ()):
						if not mailing.active ():
							self.log (agn.LV_ERROR, '%s: merger not active, abort' % entry.name)
							break
						if self.ref.islocked (entry.companyID) and entry.status_field != 'T':
							self.log (agn.LV_DEBUG, '%s: company %d is locked' % (entry.name, entry.companyID))
							continue
						self.remove_from_queue (entry)
						if self.ready_to_send (now, entry):
							if mailing.fire (statusID = entry.statusID, cursor = self.db.cursor):
								entry.startDate = now
								self.in_progress[entry.statusID] = entry
								self.log (agn.LV_INFO, '%s: started' % entry.name)
								if len (self.in_progress) >= parallel:
									break
							else:
								self.log (agn.LV_ERROR, '%s: failed to start' % entry.name)
								if self.resume_entry (entry):
									self.add_to_queue (entry)
					self.db.commit ()
					self.title ()
		return self.is_active ()
#}}}
class Rulebased (Sending): #{{{
	name = 'rulebased'
	interval = '1h'
	immediately = True
	priority = 3
	defaults = {
		'expire':	'10m',
		'parallel':	'4',
		'startup':	'5m'
	}
	def default_for (self, key, default):
		try:
			return self.defaults[key]
		except KeyError:
			return default
		
	def ready_to_send (self, now, entry):
		ready = False
		rq = self.db.cursor.querys (
			'SELECT lastsent '
			'FROM rulebased_sent_tbl '
			'WHERE mailing_id = :mailingID',
			{
				'mailingID': entry.mailingID
			}
		)
		if rq is None or rq.lastsent is None or rq.lastsent.toordinal () != now.toordinal ():
			new = rq is None
			for state in 1, 2:
				rq = self.db.cursor.querys (
					'SELECT genchange '
					'FROM maildrop_status_tbl '
					'WHERE status_id = :statusID',
					{
						'statusID': entry.statusID
					}
				)
				if rq is None:
					break
				elif rq.genchange is None and state == 1:
					self.db.cursor.execute (
						'UPDATE maildrop_status_tbl '
						'SET genchange = :now '
						'WHERE status_id = :statusID',
						{
							'now': now,
							'statusID': entry.statusID
						}
					)
				else:
					break
			if rq is not None and rq.genchange is not None:
				entry.genChange = rq.genchange
				count = self.db.cursor.execute (
					'INSERT INTO rulebased_sent_tbl (mailing_id, lastsent) VALUES (:mailingID, :now)'
						if new else
					'UPDATE rulebased_sent_tbl SET lastsent = :now WHERE mailing_id = :mailingID',
					{
						'mailingID': entry.mailingID,
						'now': now
					},
					commit = True
				)
				if count == 1:
					ready = True
				else:
					self.log (agn.LV_ERROR, '%s: failed to %s entry in rulebased_sent_tbl' % (entry.name, 'set' if new else 'update'))
			else:
				self.log (agn.LV_ERROR, '%s: failed to query current genchange' % entry.name)
		else:
			self.log (agn.LV_WARNING, '%s: unexpected this mailing had been generated this day' % entry.name)
		return ready

	def resume_entry (self, entry):
		return True

	def collect_entries_for_sending (self):
		now = datetime.datetime.now ()
		today = now.toordinal ()
		query = (
			'SELECT md.status_id, md.status_field, md.senddate, md.gendate, md.genchange, md.company_id, '
			'       co.shortname AS company_name, co.status, '
			'       mt.mailing_id, mt.shortname AS mailing_name, mt.deleted, '
			'       rb.lastsent '
			'FROM maildrop_status_tbl md '
			'     INNER JOIN company_tbl co ON (co.company_id = md.company_id) '
			'     INNER JOIN mailing_tbl mt ON (mt.mailing_id = md.mailing_id) '
			'     LEFT OUTER JOIN rulebased_sent_tbl rb ON (rb.mailing_id = md.mailing_id) '
			'WHERE md.genstatus = :genStatus AND md.status_field = :statusField AND md.senddate <= :now'
		)
		data = {
			'genStatus': 1,
			'statusField': 'R',
			'now': now
		}
		for row in self.db.cursor.queryc (query, data):
			msg = '%s (%d) for %s (%d)/%s' % (row.mailing_name, row.mailing_id, row.company_name, row.company_id, row.senddate)
			#
			# 1.) Skip not allowed companies
			if not self.ref.allow (row.company_id):
				self.log (agn.LV_DEBUG, '%s: is not on my list of allowed companies' % msg)
				continue
			#
			# 2.) Skip already sent mails for today
			if row.lastsent is not None and row.lastsent.toordinal () == today:
				self.log (agn.LV_DEBUG, '%s: already sent today' % msg)
				continue
			#
			# 3.) Skip already queued mails
			if row.status_id in self.in_queue:
				self.log (agn.LV_DEBUG, '%s: already in queue' % msg)
				continue
			#
			# 4.) Skip already processing entry
			if row.status_id in self.in_progress:
				self.log (agn.LV_DEBUG, '%s: is currently in production' % msg)
				continue
			#
			# 5.) Skip not yet ready to send
			if row.senddate.hour > now.hour:
				self.log (agn.LV_DEBUG, '%s: not yet ready to process' % msg)
				continue
			#
			# 6.) Skip outdated and keep pending entries
			if row.senddate.hour < now.hour:
				if self.pending is not None and row.status_id in self.pending:
					self.log (agn.LV_DEBUG, '%s: pending' % msg)
				else:
					self.log (agn.LV_DEBUG, '%s: outdated' % msg)
					continue
			#
			# 7.) Skip invalid company or mailing
			if row.status != 'active' or row.deleted:
				if row.status != 'active':
					self.log (agn.LV_DEBUG, '%s: company %s (%d) is not active, but %s' % (msg, row.company_name, row.company_id, row.status))
				if row.deleted:
					self.log (agn.LV_DEBUG, '%s: mailing marked as deleted' % msg)
				self.invalidate_maildrop_entry (row.status_id)
				continue
			self.add_to_queue (Entry (msg, row.company_id, row.mailing_id, row.status_id, row.status_field, row.senddate, row.gendate, row.genchange))
		self.db.commit ()
#}}}
class Generate (Sending): #{{{
	name = 'generate'
	interval = '15m'
	priority = 4
	def ready_to_send (self, now, entry):
		if not self.change_genstatus (entry.statusID, old_status = 0, new_status = 1):
			self.log (agn.LV_ERROR, '%s: failed to update maildrop status table' % entry.name)
			return False
		#
		entry.genChange = self.db.cursor.stream (
			'SELECT genchange FROM maildrop_status_tbl WHERE status_id = :statusID',
			{
				'statusID': entry.statusID
			}
		).map (lambda r: r[0]).first (no = None)
		if entry.genChange is None:
			self.log (agn.LV_ERROR, '%s: failed to query current gen change' % entry.name)
			if not self.resume_entry (entry):
				return False
		self.db.commit ()
		return True

	def resume_entry (self, entry):
		if not self.change_genstatus (entry.statusID, old_status = 1, new_status = 0):
			self.log (agn.LV_FATAL, '%s: failed to revert status id from 1 to 0' % entry.name)
			return False
		else:
			self.log (agn.LV_INFO, '%s: resumed setting genstatus from 1 to 0' % entry.name)
			return True

	def collect_entries_for_sending (self):
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
		now = datetime.datetime.now ()
		limit = None
		if self.ref.oldest >= 0:
			limit = now.fromordinal (now.toordinal () - self.ref.oldest)
		for row in self.db.cursor.queryc (query, {'now': now}):
			msg = 'Mailing "%s" (mailingID=%d, statusID=%d, statusField=%r, company=%s, companyID=%d)' % (
				row.mailing_name, row.mailing_id, 
				row.status_id, row.status_field,
				row.company_name, row.company_id
			)
			if not self.ref.allow (row.company_id):
				self.log (agn.LV_DEBUG, '%s: not in my list of allowed companies' % msg)
				continue
			startIt = False
			keepIt = False
			if row.status != 'active':
				self.log (agn.LV_INFO, '%s: company is not active, but %s' % (msg, row.status))
			elif row.deleted:
				self.log (agn.LV_INFO, '%s: mailing is marked as deleted' % msg)
			elif limit is not None and row.gendate is not None and row.gendate < limit:
				self.log (agn.LV_INFO, '%s: mailing gendate is too old (%s, limit is %s)' % (msg, str (row.gendate), str (limit)))
			else:
				if (
					row.optimize_mail_generation is not None and
					row.optimize_mail_generation == 'day' and
					row.senddate is not None and
					row.senddate.toordinal () > now.toordinal ()
				):
					self.log (agn.LV_INFO, '%s: day based optimized mailing will not be generate on a previous day, deferred' % msg)
					keepIt = True
				else:
					startIt = True
			if startIt:
				if row.status_id in self.in_queue:
					self.log (agn.LV_INFO, '%s: already queued' % msg)
				else:
					self.add_to_queue (Entry (msg, row.company_id, row.mailing_id, row.status_id, row.status_field, row.senddate, row.gendate, row.genchange))
					self.log (agn.LV_INFO, '%s: added to queue' % msg)
			else:
				if row.status_id in self.in_queue:
					self.log (agn.LV_INFO, '%s: queued, remove it from queue' % msg)
					self.remove_from_queue (row.status_id)
				if not keepIt:
					if self.invalidate_maildrop_entry (row.status_id, old_status = 0):
						self.log (agn.LV_INFO, '%s: disabled' % msg)
					else:
						self.log (agn.LV_ERROR, '%s: failed to disable' % msg);
		self.db.commit ()
#}}}
class Schedule (eagn.Schedule): #{{{
	__slots__ = ['modules', 'oldest', 'processes', 'pending', 'deferred', 'config', 'companies', 'locks', 'processtitle']
	def __init__ (self, modules, oldest, processes, *args, **kws):
		super (Schedule, self).__init__ (*args, **kws)
		self.modules = modules
		self.oldest = oldest
		self.processes = processes
		self.pending = collections.namedtuple ('Control', ['control', 'queued', 'running']) (
			eagn.Daemonic (), {}, []
		)
		self.deferred = []
		self.config = {}
		self.companies = None
		self.locks = {}
		self.processtitle = eagn.Processtitle ('$original [$title]')
	
	def readConfiguration (self):
		config = None
		db = eagn.DB ()
		try:
			if db.isopen ():
				config = (db.cursor.stream ('SELECT name, value FROM config_tbl WHERE class = :class', {'class': 'generate'})
					.filter (lambda nv: nv[0] is not None)
					.dict ()
				)
			else:
				raise agn.error ('access to database: %s' % db.lastError ())
		except Exception as e:
			agn.log (agn.LV_ERROR, 'sched', 'Failed to read configuration: %s' % e)
		finally:
			db.done ()
		if config is not None:
			self.config = config
			self.companies = self.configuration (
				'companies',
				convert = lambda v: agn.Stream.ifelse (agn.Range (v))
			)
			if self.companies:
				agn.log (agn.LV_INFO, 'sched', 'Limit operations on these companies: %s' % self.companies)
	
	def configuration (self, key, name = None, default = None, convert = None):
		return (
			agn.Stream.of (
				('%s:%s[%s]' % (name, key, agn.host)) if name is not None else None,
				'%s[%s]' % (key, agn.host),
				('%s:%s' % (name, key)) if name is not None else None,
				key
			)
			.filter (lambda k: k is not None and k in self.config)
			.map (lambda k: self.config[k])
			.first (no = default, finisher = lambda v: v if convert is None or v is None else convert (v))
		)
	
	def allow (self, companyID):
		return self.companies is None or companyID in self.companies

	def title (self, info = None):
		self.processtitle ('schedule%s' % ((' %s' % info) if info else ''))

	def reload (self, sig, stack):
		self.readConfiguration ()
		
	def status (self, sig, stack):
		agn.log (agn.LV_REPORT, 'sched', 'Currently blocked companies: %s' % (agn.Stream.of (self.locks.iteritems ()).map (lambda kv: '%s=%r' % kv).join (', ') if self.locks else 'none', ))
		for (name, queue) in agn.Stream.of (self.pending.queued.iteritems ()).map (lambda kv: ('queued prio %d' % kv[0], kv[1])).list () + [('running', self.pending.running)]:
			agn.log (agn.LV_REPORT, 'sched', 'Currently %d %s processes' % (len (queue), name))
			for entry in queue:
				agn.log (agn.LV_REPORT, 'sched', '  %s%s' % (entry.description, (' (%d)' % entry.pid if entry.pid is not None else '')))
		for job in self.deferred:
			agn.log (agn.LV_REPORT, 'sched', '%s: deferred' % job.description)
		if self._schedule.queue:
			agn.log (agn.LV_REPORT, 'sched', 'Currently scheduled events:')
			for event in self._schedule.queue:
				ts = event.time % (24 * 60 * 60)
				agn.log (agn.LV_REPORT, 'sched', '\t%2d:%02d:%02d [Prio %d]: %s' % (
					ts // 3600, (ts // 60) % 60, ts % 60,
					event.priority, event.action.name
				))
	
	def log (self, area, message):
		agn.log (agn.LV_DEBUG, 'sched/%s' % area, message)

	def start (self):
		self.title ()
		self.readConfiguration ()
		(agn.Stream.of (self.modules.itervalues ())
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
					convert = lambda v: agn.Stream.ifelse (v, alternator = task.interval)
				),
				task.priority,
				task (self),
				()
			))
		)
		def configurator ():
			self.readConfiguration ()
			return True
		def stopper ():
			while self.pending.running:
				if self.wait () is None:
					break
			if not self.pending.running and not self.deferred:
				self.stop ()
				return False
			return True
		self.every ('reload', False, '1h', 0, configurator, ())
		self.every ('restart', False, '24h', 0, stopper, ())
		super (Schedule, self).start ()
	
	def term (self):
		super (Schedule, self).term ()
		if self.pending.running:
			self.title ('in termination')
			(agn.Stream.of (self.pending.running)
				.peek (lambda p: agn.log (agn.LV_INFO, 'par', 'Sending terminal signal to %s' % p.description))
				.each (lambda p: self.pending.control.term (p.pid))
			)
			while self.pending.running:
				agn.log (agn.LV_INFO, 'par', 'Waiting for %d remaining child processes' % len (self.pending.running))
				self.wait (True)
		if self.deferred:
			agn.log (agn.LV_INFO, 'defer', 'Schedule final run for %d deferred tasks' % len (self.deferred))
			self.defers ()

	def wait (self, block = False):
		w = None
		while self.pending.running and w is None:
			rc = self.pending.control.join (block = block)
			if not rc.pid:
				break
			#
			w = (agn.Stream.of (self.pending.running)
				.filter (lambda r: r.pid == rc.pid)
				.first (no = None)
			)
			if w is not None:
				agn.log (agn.LV_DEBUG, 'par', '%s: returned with %s' % (w.description, ('exit with %d' % rc.exitcode) if rc.exitcode is not None else ('died due to signal %d' % rc.signal)))
				if w.finalize is not None:
					try:
						w.finalize (rc, *w.args, **w.kws)
					except Exception as e:
						agn.logexc (agn.LV_ERROR, 'per', '%s: finalize fails: %s' % (w.description, e))
				self.pending.running.remove (w)
				self.lockTitle ()
		return w

	DeferredJob = collections.namedtuple ('DeferredJob', ['task', 'description', 'method', 'args', 'kws'])
	def defers (self):
		for job in self.deferred[:]:
			agn.log (agn.LV_DEBUG, 'defer', '%s: starting' % job.description)
			self.processtitle.push (job.description)
			try:
				if not job.method (*job.args, **job.kws):
					agn.log (agn.LV_DEBUG, 'defer', '%s: finished' % job.description)
					self.deferred.remove (job)
					job.task.close ()
				else:
					agn.log (agn.LV_DEBUG, 'defer', '%s: still active' % job.description)
			finally:
				self.processtitle.pop ()
		return bool (self.deferred)
	
	def defer (self, task, method, *args, **kws):
		self.deferred.append (self.DeferredJob (task, task.name, method, args, kws))
		if len (self.deferred) == 1:
			self.every ('defer', False, '10s', 0, self.defers, ())

	Pending = agn.namedmutable ('Pending', ['description', 'method', 'prepare', 'finalize', 'args', 'kws', 'pid'])
	def pendings (self):
		while self.pending.running:
			w = self.wait ()
			if w is None:
				break
			agn.log (agn.LV_DEBUG, 'par', '%s: process finished' % w.description)
		while self.pending.queued and len (self.pending.running) < self.processes:
			prio = agn.Stream.of (self.pending.queued.keys ()).sorted ().first ()
			w = self.pending.queued[prio].pop (0)
			if not self.pending.queued[prio]:
				del self.pending.queued[prio]
			#
			if w.prepare is not None:
				try:
					w.prepare (*w.args, **w.kws)
				except Exception as e:
					agn.logexc (agn.LV_ERROR, 'par', '%s: prepare fails: %s' % (w.description, e))
			def starter ():
				self.processtitle (w.description)
				rc = w.method (*w.args, **w.kws)
				self.processtitle ()
				return rc
			w.pid = self.pending.control.spawn (starter)
			self.pending.running.append (w)
			agn.log (agn.LV_DEBUG, 'par', '%s: launched' % w.description)
		return self.pending.running or self.pending.queued

	def launch (self, prio, description, method, prepare, finalize, *args, **kws):
		active = self.pendings ()
		launching = self.Pending (
			description = description,
			method = method,
			prepare = prepare,
			finalize = finalize,
			args = args,
			kws = kws,
			pid = None
		)
		try:
			self.pending.queued[prio].append (launching)
		except KeyError:
			self.pending.queued[prio] = [launching]
		if not active:
			self.every ('pending', False, '1m', 0, self.pendings, ())

	def lockTitle (self):
		self.title (agn.Stream.of (self.locks.iteritems ())
			.map (lambda kv: '%s=%r' % kv)
			.join (', ', lambda s: ('active locks %s' % s) if s else s)
		)

	def islocked (self, key):
		return key in self.locks
	
	def lock (self, key):
		try:
			self.locks[key] += 1
			agn.log (agn.LV_DEBUG, 'lock', '%r increased' % key)
		except KeyError:
			self.locks[key] = 1
			agn.log (agn.LV_DEBUG, 'lock', '%r set' % key)
	
	def unlock (self, key):
		try:
			self.locks[key] -= 1
			if self.locks[key] <= 0:
				del self.locks[key]
				agn.log (agn.LV_DEBUG, 'lock', '%r removed' % key)
			else:
				agn.log (agn.LV_DEBUG, 'lock', '%r decreased' % key)
		except KeyError:
			pass
#}}}
class Jobqueue (eagn.Jobqueue): #{{{
	def __init__ (self, schedule):
		super (Jobqueue, self).__init__ (schedule)
		schedule.processtitle ('watchdog')

	def setupHandler (self, master):
		super (Jobqueue, self).setupHandler (master)
		if not master:
			self.setupCustomHandler (
				hup = self.schedule.reload,
				usr1 = self.schedule.status
			)
#}}}
def main (): #{{{
	oldest = 3
	processes = 4
	restartDelay = '1m'
	terminationDelay = '5m'
	(opts, param) = getopt.getopt (sys.argv[1:], 'vo:p:r:t:')
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
			agn.loglevel = agn.LV_DEBUG
		elif opt[0] == '-o':
			oldest = int (opt[1])
		elif opt[0] == '-p':
			processes = int (opt[1])
		elif opt[0] == '-r':
			restartDelay = opt[1]
		elif opt[0] == '-t':
			terminationDelay = opt[1]
	#
	activator = Activator ()
	modules = (agn.Stream.of (globals ().itervalues ())
		.filter (lambda module: type (module) is type and issubclass (module, Task) and hasattr (module, 'interval'))
		.filter (lambda module: activator.check (['%s-%s' % (agn.logname, module.name)]))
		.map (lambda module: (module.name, module))
		.dict ()
	)
	#
	agn.lock ()
	agn.log (agn.LV_INFO, 'main', 'Starting up')
	agn.log (agn.LV_INFO, 'main', 'Active modules: %s' % ', '.join (sorted (modules.iterkeys ())))
	schedule = Schedule (modules, oldest, processes)
	if param:
		schedule.readConfiguration ()
		for name in param:
			if name not in modules:
				print ('** %s not known' % name)
			else:
				agn.log (agn.LV_INFO, name, 'Module found')
				module = modules[name] (schedule)
				rc = module ()
				schedule.status (None, None)
				agn.log (agn.LV_INFO, name, 'Module returns %r' % (rc, ))
				if schedule.pending.queued or schedule.pending.running:
					agn.log (agn.LV_INFO, name, 'Execute backgound processes')
					try:
						while schedule.pending.queued:
							schedule.status (None, None)
							schedule.pendings ()
							if len (schedule.pending.running) == processes:
								agn.log (agn.LV_INFO, name, 'Currently %d processes running, wait for at least one to terminate' % len (schedule.pending.running))
								schedule.status (None, None)
								schedule.wait (True)
						agn.log (agn.LV_INFO, name, 'Wait for %d background process to teminate' % len (schedule.pending.running))
						while schedule.pending.running:
							schedule.status (None, None)
							if not schedule.wait (True):
								break
					except KeyboardInterrupt:
						agn.log (agn.LV_INFO, name, '^C, terminate all running processes')
						for p in schedule.pending.running[:]:
							schedule.pending.control.term (p.pid)
							schedule.wait ()
						agn.log (agn.LV_INFO, name, 'Waiting for 2 seconds to kill all remaining processes')
						time.sleep (2)
						for p in schedule.pending.running:
							schedule.pending.control.term (p.pid, eagn.signal.SIGKILL)
						agn.log (agn.LV_INFO, name, 'Waiting for killed processes to terminate')
						while schedule.wait (True) is not None:
							pass
					agn.log (agn.LV_INFO, name, 'Background processes done')
				if schedule.deferred:
					agn.log (agn.LV_INFO, name, 'Deferred jobs active, process them')
					try:
						last = -1
						while schedule.defers ():
							cur = len (schedule.deferred)
							if cur != last:
								agn.log (agn.LV_INFO, name, '%d jobs remaining' % cur)
								last = cur
							time.sleep (1)
					except KeyboardInterrupt:
						agn.log (agn.LV_INFO, name, '^C, terminating')
	else:
		jq = Jobqueue (schedule)
		jq.start (restartDelay = restartDelay, terminationDelay = terminationDelay)
	agn.log (agn.LV_INFO, 'main', 'Going down')
	agn.unlock ()
#}}}
if __name__ == '__main__':
	main ()
