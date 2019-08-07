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
#
import	sys, os, getopt, time, gzip, re, codecs, datetime
import	subprocess, socket
import	agn, eagn
agn.loglevel = agn.LV_DEBUG
#
class Mailing (agn.mutable): #{{{
	meta = agn.mkpath (agn.base, 'var', 'spool', 'META')
	archive = agn.mkpath (agn.base, 'var', 'spool', 'ARCHIVE')
	track = agn.mkpath (agn.base, 'var', 'track')
	def __init__ (self, statusID, statusField, mailingID, companyID, check): #{{{
		self.statusID = statusID
		self.statusField = statusField
		self.mailingID = mailingID
		self.companyID = companyID
		self.check = check
		self.seen = None
		self.pattern = None
		self.tempFile = agn.mkpath (self.meta, '.recover-%d.temp' % self.statusID)
		self.recoverFile = agn.mkpath (self.meta, 'recover-%d.list' % self.statusID)
		self.count = 0
		self.active = True
		self.current = 0
		self.last = 0
	#}}}
	def done (self): #{{{
		for path in [self.tempFile, self.recoverFile]:
			if os.path.isfile (path):
				os.unlink (path)
	#}}}
	def __cmp__ (self, other): #{{{
		return cmp (self.statusID, other.statusID)
	#}}}
	def __parseXML (self, path): #{{{
		pattern = re.compile ('<receiver customer_id="([0-9]+)"')
		fd = gzip.open (path, 'r')
		try:
			current = set ()
			mode = 0
			for line in fd:
				if mode == 0:
					if '<receivers>' in line:
						mode = 1
				elif mode == 1:
					mtch = pattern.search (line)
					if not mtch is None:
						current.add (int (mtch.groups ()[0]))
			self.seen.update (current)
		except IOError, e:
			agn.log (agn.LV_WARNING, 'parse', 'Failed to parse "%s": %r' % (path, e.args))
		fd.close ()
	#}}}
	def __collect (self, path, remove): #{{{
		files = os.listdir (path)
		for fname in [_f for _f in files if not self.pattern.match (_f) is None]:
			fpath = agn.mkpath (path, fname)
			if remove:
				try:
					os.unlink (fpath)
					agn.log (agn.LV_DEBUG, 'collect', 'File "%s" removed' % fpath)
				except OSError, e:
					agn.log (agn.LV_ERROR, 'collect', 'Failed to remove file "%s": %r' % (fpath, e.args))
			elif fname.endswith ('.xml.gz'):
				self.__parseXML (fpath)
	#}}}
	def __collectTrack (self, path): #{{{
		for fname in os.listdir (path):
			tpath = agn.mkpath (path, fname)
			with open (tpath, 'r') as fd:
				self.seen.update (int (_l.strip ()) for _l in fd)
	#}}}
	def collectSeen (self): #{{{
		self.seen = set ()
		self.pattern = re.compile ('^AgnMail(-[0-9]+)?=D[0-9]{14}=%d=%d=[^=]+=liaMngA\\.(stamp|final|xml\\.gz)$' % (self.companyID, self.mailingID))
		self.__collect (self.meta, True)
		for sdir in self.check:
			spath = agn.mkpath (self.archive, sdir)
			if os.path.isdir (spath):
				self.__collect (spath, False)
		track = agn.mkpath (self.track, str (self.companyID), str (self.mailingID))
		if os.path.isdir (track):
			self.__collectTrack (track)
	#}}}
	def createFilelist (self): #{{{
		if self.seen:
			fd = open (self.tempFile, 'w')
			fd.write ('\n'.join ([str (_s) for _s in self.seen]) + '\n')
			fd.close ()
			os.rename (self.tempFile, self.recoverFile)
	#}}}
	def setGeneratedCount (self, count): #{{{
		self.count = count
		agn.log (agn.LV_INFO, 'mailing', 'Seen %d customers, logged %d' % (len (self.seen), self.count))
	#}}}
#}}}
class Recovery: #{{{
	def __init__ (self, maxAge, startUpDelay, restrictToMailings, doit): #{{{
		self.maxAge = maxAge
		self.startUpDelay = startUpDelay
		self.restrictToMailings = restrictToMailings
		self.doit = doit
		self.db = None
		self.cursor = None
		self.mailings = []
		self.mailingInfo = {}
		self.report = []
	#}}}
	def done (self): #{{{
		for m in self.mailings:
			m.done ()
		self.dbClose ()
	#}}}
	def setup (self): #{{{
		return self.dbOpen ()
	#}}}
	def dbOpen (self, force = False): #{{{
		if force or self.cursor is None:
			self.dbClose ()
		if self.db is None:
			self.db = agn.DBaseID ()
			if not self.db is None:
				self.cursor = self.db.cursor ()
			else:
				self.cursor = None
		return not self.cursor is None
	#}}}
	def dbClose (self, commit = True): #{{{
		if not self.db is None:
			if not self.cursor is None:
				self.cursor.sync (commit)
				self.cursor.close ()
			self.db.close ()
		self.db = None
		self.cursor = None
	#}}}
	def __makeRange (self, start, end): #{{{
		rc = []
		s = time.mktime ((start[0], start[1], start[2], 12, 0, 0, -1, -1, -1))
		e = time.mktime ((end[0], end[1], end[2], 12, 0, 0, -1, -1, -1))
		while s <= e:
			ts = time.gmtime (s)
			rc.append ('%04d%02d%02d' % (ts.tm_year, ts.tm_mon, ts.tm_mday))
			s += 24 * 60 * 60
		return rc
	#}}}
	def __toiso (self, s): #{{{
		try:
			if s is None:
				return s
			return codecs.encode (unicode (s, 'UTF8') if type (s) is str else s, 'ISO-8859-1')
		except (UnicodeDecodeError, UnicodeEncodeError):
			return s
	#}}}
	def __mail (self, mailingID): #{{{
		try:
			rc = self.mailingInfo[mailingID]
		except KeyError:
			r = self.cursor.querys ('SELECT company_id, shortname, deleted FROM mailing_tbl WHERE mailing_id = :mid', {'mid': mailingID})
			if r is not None:
				rc = agn.mutable (companyID = r[0], name = self.__toiso (r[1]), deleted = bool (r[2]))
			else:
				rc = agn.mutable (companyID = 0, name = '#%d not found' % mailingID, deleted = False)
			self.mailingInfo[mailingID] = rc
		return rc
	#}}}
	def __mailingName (self, mailingID): #{{{
		return self.__mail (mailingID).name
	#}}}
	def __mailingDeleted (self, mailingID): #{{{
		return self.__mail (mailingID).deleted
	#}}}
	def collectMailings (self): #{{{
		todate = lambda a: datetime.datetime (a.tm_year, a.tm_mon, a.tm_mday, a.tm_hour, a.tm_min, a.tm_sec)
		now = time.localtime ()
		expire = time.localtime (time.time () - self.maxAge * 24 * 60 * 60)
		yesterday = time.localtime (time.time () - 24 * 60 * 60)
		query = ('SELECT status_id, mailing_id '
			 'FROM maildrop_status_tbl '
			 'WHERE genstatus = 2 AND status_field = \'R\' AND genchange > :expire AND genchange < current_timestamp')
		check = self.cursor.qselect (
			oracle = 'SELECT count(*) FROM rulebased_sent_tbl WHERE mailing_id = :mid AND to_char (lastsent, \'YYYY-MM-DD\') = to_char (sysdate - 1, \'YYYY-MM-DD\')',
			mysql = 'SELECT count(*) FROM rulebased_sent_tbl WHERE mailing_id = :mid AND date_format (lastsent, \'%%Y-%%m-%%d\') = \'%04d-%02d-%02d\'' %  (yesterday.tm_year, yesterday.tm_mon, yesterday.tm_mday)
		)
		update = ('UPDATE maildrop_status_tbl '
			  'SET genstatus = 1, genchange = current_timestamp '
			  'WHERE status_id = :sid')
		for (statusID, mailingID) in self.cursor.queryc (query, {'expire': todate (expire)}):
			if self.restrictToMailings is None or mailingID in self.restrictToMailings:
				count = self.cursor.querys (check, {'mid': mailingID})
				if count is not None and count[0] == 1:
					agn.log (agn.LV_INFO, 'collect', 'Reactivate rule based mailing %d: %s' % (mailingID, self.__mailingName (mailingID)))
					if self.doit:
						self.cursor.update (update, {'sid': statusID})
					self.report.append ('%s [%d]: Reactivate rule based mailing' % (self.__mailingName (mailingID), mailingID))
				else:
					agn.log (agn.LV_WARNING, 'collect', 'Rule based mailing %d (%s) not reactivated as it had not been sent out yesterday' % (mailingID, self.__mailingName (mailingID)))
					self.report.append ('%s [%d]: Not reactivating rule based mailing as it had not been sent out yesterday' % (self.__mailingName (mailingID), mailingID))
		if self.doit:
			self.cursor.sync ()
		query = ('SELECT status_id, mailing_id, company_id, status_field, senddate '
			 'FROM maildrop_status_tbl '
			 'WHERE genstatus IN (1, 2) AND genchange > :expire AND genchange < current_timestamp AND status_field = \'W\'')
		for (statusID, mailingID, companyID, statusField, sendDate) in self.cursor.queryc (query, {'expire': todate (expire)}):
			if self.restrictToMailings is None or mailingID in self.restrictToMailings:
				check = self.__makeRange ([sendDate.year, sendDate.month, sendDate.day], [now.tm_year, now.tm_mon, now.tm_mday])
				if not self.__mailingDeleted (mailingID) and self.__mailingName (mailingID):
					self.mailings.append (Mailing (statusID, statusField, mailingID, companyID, check))
					agn.log (agn.LV_INFO, 'collect', 'Mark mailing %d (%s) for recovery' % (mailingID, self.__mailingName (mailingID)))
		self.mailings.sort ()
		agn.log (agn.LV_INFO, 'collect', 'Found %d mailing(s) to recover' % len (self.mailings))
	#}}}
	def recoverMailings (self): #{{{
		if self.doit and self.mailings and self.startUpDelay > 0:
			agn.log (agn.LV_INFO, 'recover', 'Wait for backend to start up')
			time.sleep (self.startUpDelay)
		for m in self.mailings:
			m.collectSeen ()
			if self.doit:
				m.createFilelist ()
				count = 0
				for (totalMails, ) in self.cursor.query ('SELECT total_mails FROM mailing_backend_log_tbl WHERE status_id = :sid', {'sid': m.statusID}):
					if not totalMails is None and totalMails > count:
						count = totalMails
				m.setGeneratedCount (count)
				self.cursor.execute ('DELETE FROM mailing_backend_log_tbl WHERE status_id = :sid', {'sid': m.statusID})
				self.cursor.execute ('DELETE FROM world_mailing_backend_log_tbl WHERE mailing_id = :mid', {'mid': m.mailingID})
				self.cursor.execute ('UPDATE maildrop_status_tbl SET genstatus = 1 WHERE status_id = :sid', {'sid': m.statusID})
				self.cursor.sync ()
				agn.log (agn.LV_INFO, 'recover', 'Start backend using statusID %d for %s' % (m.statusID, self.__mailingName (m.mailingID)))
				pp = subprocess.Popen (['java', 'org.agnitas.util.MailoutClient', 'fire', '%d' % m.statusID], stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
				(out, err) = pp.communicate (None)
				if pp.returncode:
					agn.log (agn.LV_ERROR, 'recover', 'Failed to trigger mailing %d, Stdout:%s\nStderr:%s' % (m.mailingID, out, err))
					self.report.append ('%s [%d]: Failed to trigger mailing' % (self.__mailingName (m.mailingID), m.mailingID))
					break
			else:
				print ('%s: %d recipients already seen' % (self.__mailingName (m.mailingID), len (m.seen)))
			self.report.append ('%s [%d]: Start recovery using statusID %d' % (self.__mailingName (m.mailingID), m.mailingID, m.statusID))
			if self.doit:
				query = 'SELECT genstatus FROM maildrop_status_tbl WHERE status_id = :statusID'
				start = int (time.time ())
				ok = True
				while m.active and ok:
					now = int (time.time ())
					row = self.cursor.querys (query, {'statusID': m.statusID})
					if row is None or row[0] is None:
						agn.log (agn.LV_INFO, 'recover', 'Failed to query status for mailing %d' % m.mailingID)
						self.report.append ('%s [%d]: Recovery failed due to missing status' % (self.__mailingName (m.mailingID), m.mailingID))
						ok = False
					else:
						genStatus = row[0]
						if genStatus == 3:
							agn.log (agn.LV_INFO, 'recover', 'Mailing %d terminated as expected' % m.mailingID)
							self.report.append ('%s [%d]: Recovery finished' % (self.__mailingName (m.mailingID), m.mailingID))
							m.active = False
						elif genStatus == 2:
							if m.last:
								current = 0
								for (currentMails, ) in self.cursor.query ('SELECT current_mails FROM mailing_backend_log_tbl WHERE status_id = :sid', {'sid': m.statusID}):
									if not currentMails is None:
										current = currentMails
								if current != m.current:
									m.current = current
									m.last = now
								else:
									if (current > 0 and m.last + 1200 < now) or \
									   (current == 0 and m.last + 3600 < now):
									   	agn.log (agn.LV_INFO, 'recover', 'Mailing %d terminated due to inactivity after %d mails' % (m.mailingID, current))
										self.report.append ('%s [%d]: Recovery timed out' % (self.__mailingName (m.mailingID), m.mailingID))
										ok = False
							else:
								m.last = now
						elif genStatus == 1:
							if start + 1800 < now:
								agn.log (agn.LV_INFO, 'recover', 'Mailing %d terminated while not starting up' % m.mailingID)
								self.report.append ('%s [%d]: Recovery not started' % (self.__mailingName (m.mailingID), m.mailingID))
								ok = False
						elif genStatus > 3:
							agn.log (agn.LV_INFO, 'recover',  'Mailing %d terminated with status %d' % (m.mailingID, genStatus))
							self.report.append ('%s [%d]: Recovery ended with unexpected status %d' % (self.__mailingName (m.mailingID), m.mailingID, genStatus))
							m.active = False
					if m.active and ok:
						if start + 30 * 60 < now:
							agn.log (agn.LV_INFO, 'recvoer', 'Failed due to global timeout to recover %d' % m.mailingID)
							self.report.append ('%s [%d]: Recovery ended due to global timeout' % (self.__mailingName (m.mailingID), m.mailingID))
							ok = False
						else:
							time.sleep (1)
				if not m.active:
					count = 0
					for (totalMails, ) in self.cursor.query ('SELECT total_mails FROM mailing_backend_log_tbl WHERE status_id = :sid', {'sid': m.statusID}):
						if not totalMails is None:
							count = totalMails
					count += len (m.seen)
					self.cursor.execute ('UPDATE mailing_backend_log_tbl SET total_mails = :cnt, current_mails = :cnt WHERE status_id = :sid', {'sid': m.statusID, 'cnt': count})
					self.cursor.execute ('UPDATE world_mailing_backend_log_tbl SET total_mails = :cnt, current_mails = :cnt WHERE mailing_id = :mid', {'mid': m.mailingID, 'cnt': count})
				if not ok:
					break
	#}}}
	def reportMailings (self): #{{{
		mails = []
		query = 'SELECT status_id, mailing_id, genstatus, genchange, status_field, senddate FROM maildrop_status_tbl WHERE '
		query += 'genstatus IN (1, 2) AND status_field IN (\'W\', \'R\', \'D\')'
		for (statusID, mailingID, genStatus, genChange, statusField, sendDate) in self.cursor.queryc (query):
			if statusField in ('R', 'D') and genStatus == 1:
				continue
			info = self.__mail (mailingID)
			mail = agn.mutable (statusID = statusID,
					    statusField = statusField,
					    mailingID = mailingID,
					    mailingName = info.name,
					    companyID = info.companyID,
					    deleted = info.deleted,
					    genChange = genChange,
					    sendDate = sendDate)
			mails.append (mail)
		if self.report or mails:
			template = agn.mkpath (agn.base, 'scripts', 'recovery.tmpl')
			try:
				fd = open (template, 'r')
				content = fd.read ()
				fd.close ()
			except IOError, e:
				agn.log (agn.LV_ERROR, 'report', 'Unable to find template "%s": %r' % (template, e.args))
				content = None
			if content:
				ns = {	'host': socket.getfqdn (),
					'report': self.report,
					'mails': mails
				}
				tmpl = agn.Template (content)
				try:
					body = tmpl.fill (ns)
				except agn.error as e:
					agn.log (agn.LV_ERROR, 'report', 'Failed to fill template "%s": %s' % (template, e))
					body = None
				if body:
					charset = tmpl.property ('charset')
					subject = tmpl.property ('subject')
					if not subject:
						subject = tmpl['subject']
					if not subject:
						subject = 'Recovery report for %s' % ns['host']
					else:
						stmpl = agn.Template (subject)
						subject = stmpl.fill (ns)
					sender = tmpl.property ('sender')
					if not sender:
						sender = 'openemm'
					receiver = tmpl.property ('receiver')
					if not receiver:
						receiver = 'openemm'
					if self.doit:
						if charset:
							eagn.EMail.forceEncoding (charset, 'qp')
						mail = eagn.EMail ()
						mail.setSender (sender)
						for recv in [_r.strip () for _r in receiver.split (',')]:
							if recv:
								mail.addTo (recv)
						if charset:
							mail.setCharset (charset)
						mail.setSubject (subject)
						mail.setText (body)
						mail.sendMail ()
					else:
						print ('From: %s' % sender)
						print ('To: %s' % receiver)
						print ('Subject: %s' % subject)
						print ('')
						print (body)
	#}}}
#}}}
if __name__ == '__main__': #{{{
	def usage (msg = None):
		sys.stderr.write ("""Syntax: %(pgm)s [-n] [-a <days>]
Function: recovers previously aborted mailings
Options:
\t-n           do not execute recovery, just print what will be done
\t-a <days>    maximum age in days to restart mailings [1]
""" % {'pgm': sys.argv[0]})
		if not msg is None:
			sys.stderr.write ('%s\n' % msg)
			sys.exit (1)
		sys.exit (0)

	def main ():
		doit = True
		maxAge = 1
		startUpDelay = 60
		mailings = None
		try:
			(opts, parm) = getopt.getopt (sys.argv[1:], '?nva:d:m:')
			for opt in opts:
				if opt[0] == '-?':
					usage ()
				elif opt[0] == '-n':
					doit = False
				elif opt[0] == '-v':
					agn.outlevel = agn.LV_DEBUG
					agn.outstream = sys.stderr
				elif opt[0] == '-a':
					agn.validate (opt[1], '[0-9]+', (lambda a: int (a) > 0, 'age must be > 0'), reason = 'Numeric value expected for age')
					maxAge = int (opt[1])
				elif opt[0] == '-d':
					startUpDelay = int (opt[1])
				elif opt[0] == '-m':
					if mailings is None:
						mailings = []
					mailings.append (int (opt[1]))
		except (getopt.GetoptError, agn.error) as e:
			usage (str (e))
		agn.lock ()
		agn.log (agn.LV_INFO, 'main', 'Starting up')
		rc = False
		rec = Recovery (maxAge, startUpDelay, mailings, doit)
		if rec.setup ():
			try:
				rec.collectMailings ()
				rec.recoverMailings ()
				rec.reportMailings ()
				rc = True
			except agn.error as e:
				agn.log (agn.LV_ERROR, 'main', 'Failed recovery: %s' % e)
		else:
			agn.log (agn.LV_ERROR, 'main', 'Failed to setup recovery process')
		rec.done ()
		agn.log (agn.LV_INFO, 'main', 'Going down')
		agn.unlock ()
		if not rc:
			sys.exit (1)
	#
	main ()
#}}}
