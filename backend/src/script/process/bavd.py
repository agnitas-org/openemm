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
#	-*- python -*-
#
import	urllib, urllib2
import	sys, os, signal, time, re, types, mutex
import	fnmatch, gdbm, datetime
import	subprocess
import	email, email.Header
import	agn, eagn
#
agn.loglevel = agn.LV_VERBOSE
#
alock = mutex.mutex ()
class Autoresponder:
	msgPattern = agn.mkpath (agn.base, 'var', 'lib', 'ar_%s.mail')
	wlFile = agn.mkpath (agn.base, 'var', 'lib', 'ar.whitelist')
	wlPattern = agn.mkpath (agn.base, 'var', 'lib', 'ar_%s.whitelist')
	limitPattern = agn.mkpath (agn.base, 'var', 'lib', 'ar_%s.limit')
	
	def __init__ (self, aid, sender):
		self.aid = aid
		self.sender = sender
	
	def isInBlacklist (self, company_id):
		accept = True
		db = agn.DBaseID ()
		if db:
			i = db.cursor ()
			if i:
				for state in [ 0, 1 ]:
					table = None

					if state == 0 and company_id:
						table = 'cust%s_ban_tbl' % company_id
					if not table:
						continue
					for r in i.queryc ('SELECT email FROM %s' % table):
						if not r[0] is None:
							eMail = r[0]
							if '_' in eMail or '%' in eMail:
								pattern = eMail.replace ('%', '*').replace ('_', '?')
								if fnmatch.fnmatch (self.sender, pattern):
									accept = False
							elif eMail == self.sender:
								accept = False
							if not accept:
								agn.log (agn.LV_INFO, 'blist', 'Autoresponder disabled due to blacklist entry "%s" on %s' % (eMail, table))
								break
						if not accept:
							break
				i.close ()
			else:
				agn.log (agn.LV_ERROR, 'blist', 'Unable to get cursor for blacklisting')
			db.close ()
		else:
			agn.log (agn.LV_ERROR, 'blist', 'Unable to setup database interface for blacklisting')
		return not accept
	
	def isInWhitelist (self):
		mayReceive = False
		for arwlist in [Autoresponder.wlPattern % self.aid, Autoresponder.wlFile]:
			try:
				fd = open (arwlist)
				for line in (l.rstrip ('\r\n') for l in fd if not l[0] in '\n#'):
					if line == self.sender:
						mayReceive = True
						agn.log (agn.LV_VERBOSE, 'ar', 'Sender %s is on whitelist file %s' % (self.sender, arwlist))
						break
				fd.close ()
			except IOError:
				pass
			if mayReceive:
				break
		return mayReceive
	
	def isLimited (self, dryrun):
		mayReceive = False
		hasLock = False
		retry = 5
		while retry >= 0:
			if alock.testandset ():
				hasLock = True
				break
			time.sleep (1)
			retry -= 1
		if hasLock:
			try:
				arlimit = Autoresponder.limitPattern % self.aid
				now = time.time ()
				dbf = gdbm.open (arlimit, 'c')
				if not dbf.has_key (self.sender):
					agn.log (agn.LV_DEBUG, 'ar', 'Never sent mail to %s from this autoresponder %s' % (self.sender, self.aid))
					mayReceive = True
				else:
					try:
						last = int (dbf[self.sender])
						if last + 24 * 60 * 60 < now:
							agn.log (agn.LV_DEBUG, 'ar', 'Last mail to "%s" is older than 24 hours' % self.sender)
							mayReceive = True
						else:
							diff = (now - last) / 60
							agn.log (agn.LV_INFO, 'ar', 'Reject mail to "%s", sent already mail in last 24 hours (%d:%02d)' % (self.sender, diff / 60, diff % 60))
					except ValueError:
						pass
				if mayReceive and not dryrun:
					dbf[self.sender] = '%d' % now
				dbf.close ()
			except gdbm.error, e:
				agn.log (agn.LV_ERROR, 'ar', 'Unable to acess %s %s' % (arlimit, `e.args`))
				mayReceive = False
			alock.unlock ()
		else:
			agn.log (agn.LV_WARNING, 'ar', 'Unable to get global lock for %s' % self.aid)
		return not mayReceive
	
	def allow (self, parm, dryrun):
		if self.isInWhitelist ():
			return True
		if self.isLimited (dryrun):
			return False
		if parm.has_key ('cid'):
			cid = parm['cid']
		else:
			cid = None
		if self.isInBlacklist (cid):
			return False
		return True

	def createMessage (self, orig, parm, dryrun):
		global	alock

		fname = Autoresponder.msgPattern % self.aid
		if not os.access (fname, os.R_OK):
			agn.log (agn.LV_WARNING, 'ar', 'No autoresponder mail %s for enabled autoresponder %s found' % (fname, self.aid))
			return None
		#
		if not self.allow (parm, dryrun):
			return None
		#
		try:
			fd = open (fname)
			armail = fd.read ()
			fd.close ()
			armsg = email.message_from_string (armail)
			armsg['To'] = '<%s>' % self.sender
			if not armsg.has_key ('subject') and orig.has_key ('subject'):
				subj = orig['subject']
				if len (subj) < 4 or subj[:4].lower () != 're: ':
					subj = 'Re: ' + subj
				armsg['Subject'] = subj
		except IOError, e:
			armsg = None
			agn.log (agn.LV_ERROR, 'ar', 'Unable to read %s %s' % (armail, `e.args`))
		return armsg
	
	def triggerMessage (self, cinfo, parm, dryrun):
		try:
			mailingID = int (parm['armid'])
			companyID = int (parm['cid'])
			if cinfo is None:
				raise agn.error ('failed to determinate origin of mail')
			if not cinfo.valid:
				raise agn.error ('uid from foreign instance %d (expected from %d)' % (cinfo.licenceID, agn.licence))
			customerID = cinfo.customerID
			rdirDomain = None
			with eagn.DB () as db:
				rq = db.cursor.querys (
					'SELECT mailinglist_id, company_id, deleted '
					'FROM mailing_tbl '
					'WHERE mailing_id = :mailingID',
					{'mailingID': mailingID}
				)
				if rq is None:
					raise agn.error ('mailing %d not found' % mailingID)
				if rq.company_id != companyID:
					raise agn.error ('mailing %d belongs to company %d, but mailloop belongs to company %d' % (mailingID, rq[1], companyID))
				if rq.deleted:
					agn.log (agn.LV_INFO, 'ar', 'mailing %d is marked as deleted' % mailingID)
				mailinglistID = rq.mailinglist_id
				#
				rq = db.cursor.querys (
					'SELECT user_type '
					'FROM customer_%d_binding_tbl '
					'WHERE customer_id = :customerID AND mailinglist_id = :mailinglistID AND mediatype = 0'
					% companyID,
					{
						'customerID': customerID,
						'mailinglistID': mailinglistID
					}
				)
				if rq is None or rq.user_type not in ('A', 'T', 't'):
					if not self.allow (parm, dryrun):
						raise agn.error ('recipient is not allowed to received (again) an autoresponder')
				else:
					agn.log (agn.LV_INFO, 'ar', 'recipient %d on %d for %d is admin/test recipient and not blocked' % (customerID, mailinglistID, mailingID))
				#
				rq = db.cursor.querys (
					'SELECT rdir_domain '
					'FROM mailinglist_tbl '
					'WHERE mailinglist_id = :mailinglistID',
					{'mailinglistID': mailinglistID}
				)
				if rq is not None and rq.rdir_domain is not None:
					rdirDomain = rq.rdir_domain
				else:
					rq = db.cursor.querys (
						'SELECT rdir_domain '
						'FROM company_tbl '
						'WHERE company_id = :companyID',
						{'companyID': companyID}
					)
					if rq is not None:
						rdirDomain = rq.rdir_domain
				if rdirDomain is None:
					raise agn.error ('failed to determinate rdir-domain')
				#
				rq = db.cursor.querys (
					'SELECT security_token '
					'FROM mailloop_tbl '
					'WHERE rid = :rid',
					{'rid': int (self.aid)}
				)
				if rq is None:
					raise agn.error ('no entry in mailloop_tbl for %s found' % self.aid)
				securityToken = rq.security_token if rq.security_token else ''
				url = ('%s/sendMailloopAutoresponder.do?'
				       'mailloopID=%s&companyID=%d&customerID=%d&securityToken=%s'
				       % (rdirDomain, self.aid, companyID, customerID, urllib.quote (securityToken)))
				if dryrun:
					print ('Would trigger mail using %s' % url)
				else:
					try:
						uh = urllib2.urlopen (url)
						response = uh.read ()
						uh.close ()
						agn.log (agn.LV_INFO, 'ar', 'Autoresponder mailing %d for customer %d triggered: %s' % (mailingID, customerID, response))
					except (urllib2.URLError, urllib2.HTTPError), e:
						agn.log (agn.LV_ERROR, 'ar', 'Failed to trigger %s: %s' % (url, str (e)))
		except agn.error, e:
			agn.log (agn.LV_INFO, 'ar', 'Failed to send autoresponder: %s' % str (e))
		except (KeyError, ValueError, TypeError), e:
			agn.log (agn.LV_ERROR, 'ar', 'Failed to parse %r: %s' % (parm, str (e)))
#
class Entry:
	def __init__ (self, line):
		self.eid = line
		self.parm = None
		if line[0] == '{':
			n = line.find ('}')
			if n != -1:
				self.parm = line[1:n]
				line = line[n + 1:]
		if line.startswith ('!'):
			line = line[1:]
			self.inverse = True
		else:
			self.inverse = False
		self.pattern = line
		self.regexp = re.compile (self.pattern, re.IGNORECASE)
	
	def match (self, line):
		if self.regexp.search (line):
			return True
		return False

class Section:
	def __init__ (self, name):
		self.name = name
		self.entries = []
	
	def append (self, line):
		try:
			self.entries.append (Entry (line))
		except re.error:
			agn.log (agn.LV_ERROR, 'section', 'Got illegal regular expression "%s" in [%s]' % (line, self.name))

	def match (self, line):
		for e in self.entries:
			if e.match (line):
				return e
		return None

class Scan:
	def __init__ (self):
		self.section = None
		self.entry = None
		self.reason = None
		self.dsn = None
		self.etext = None
		self.minfo = None
	
	def __str__ (self):
		rc = '[ Section: '
		if self.section:
			rc += self.section.name
		else:
			rc += '*none*'
		rc += ', Entry: '
		if self.entry:
			rc += self.entry.eid
		else:
			rc += '*none*'
		rc += ', Reason: '
		if self.reason:
			rc += self.reason
		else:
			rc += '*none*'
		rc += ', DSN: '
		if self.dsn:
			rc += self.dsn
		else:
			rc += '*none*'
		rc += ', EText: '
		if self.etext:
			rc += self.etext
		else:
			rc += '*none*'
		rc += ', MInfo: '
		if self.minfo:
			rc += str (self.minfo)
		else:
			rc += '*none*'
		rc += ' ]'
		return rc

class Rule:
	lifetime = 180
	rulePattern = agn.mkpath (agn.base, 'var', 'lib', 'bav_%s.rule')
	ruleFile = agn.mkpath (agn.base, 'lib', 'bav.rule')
	enrichPattern = agn.mkpath (agn.base, 'var', 'lib', 'bav_%s.enrich')
	DSNRE = (re.compile ('[45][0-9][0-9] +([0-9]\\.[0-9]\\.[0-9]) +(.*)'),
		 re.compile ('\\(#([0-9]\\.[0-9]\\.[0-9])\\)'),
		 re.compile ('^([0-9]\\.[0-9]\\.[0-9])'))
	
	def __init__ (self, rid, now):
		self.rid = rid
		self.created = now
		self.sections = {}
		for fname in [Rule.rulePattern % rid, Rule.ruleFile]:
			try:
				fd = open (fname, 'r')
				agn.log (agn.LV_DEBUG, 'rule', 'Reading rules from %s' % fname)
			except IOError, e:
				agn.log (agn.LV_DEBUG, 'rule', 'Unable to open %s %s' % (fname, `e.args`))
				fd = None
			if fd:
				break
		if fd:
			cur = None
			for line in [l.rstrip ('\r\n') for l in fd if len (l) > 0 and not l[0] in '\n#']:
				if line[0] == '[' and line[-1] == ']':
					name = line[1:-1]
					if self.sections.has_key (name):
						cur = self.sections[name]
					else:
						cur = Section (name)
						self.sections[name] = cur
				elif cur:
					cur.append (line)
		fname = Rule.enrichPattern % rid
		try:
			fd = open (fname)
			data = fd.read ()
			fd.close ()
			self.enrich = agn.Template (data, """
import	codecs
def dbstr (s):
	return codecs.encode (unicode (s, 'UTF-8'), mailinfo.charset)
""")
			try:
				self.enrich.compile ()
			except agn.error as e:
				self.enrich = None
				agn.log (agn.LV_ERROR, 'enrich', 'Failed to compile template for rule %s: %s' % (rid, e))
		except IOError:
			self.enrich = None
	
	def __collectSections (self, use):
		rc = []
		if not use is None:
			if type (use) in types.StringTypes:
				use = [use]
			for u in use:
				if self.sections.has_key (u):
					rc.append (self.sections[u])
		return rc
	
	def __decode (self, h):
		rc = ''
		try:
			parts = []
			for (part, charset) in email.Header.decode_header (h):
				if charset:
					parts.append (agn.toutf8 (part, charset))
				else:
					parts.append (part)
			rc = ' '.join (parts)
		except (email.Header.HeaderParseError, ValueError, LookupError):
			rc = h
		return rc.replace ('\n', ' ')

	def __match (self, line, sects):
		sec = None
		entry = None
		for s in sects:
			entry = s.match (line)
			if entry:
				sec = s
				break
		return (sec, entry)
	
	def __checkHeader (self, msg, sects):
		rc = None
		for (key, value) in msg.items ():
			line = key + ': ' + self.__decode (value)
			(sec, ent) = self.__match (line, sects)
			if sec:
				reason = '[%s/%s] %s' % (sec.name, ent.eid, line)
				rc = (sec, ent, reason)
				break
		return rc

	def matchHeader (self, msg, use):
		return self.__checkHeader (msg, self.__collectSections (use))

	def __scan (self, msg, scan, sects, checkheader, level):
		if checkheader:
			if not scan.section:
				rc = self.__checkHeader (msg, sects)
				if rc:
					(scan.section, scan.entry, scan.reason) = rc
		subj = msg['subject']
		if not scan.dsn:
			if subj:
				mt = Rule.DSNRE[0].search (self.__decode (subj))
				if mt:
					grps = mt.groups ()
					scan.dsn = grps[0]
					scan.etext = grps[1]
					agn.log (agn.LV_VERBOSE, 'dsn', 'Found DSN in Subject: %s' % subj)
			if not scan.dsn:
				action = msg['action']
				status = msg['status']
				if action and status:
					mt = Rule.DSNRE[2].match (self.__decode (status))
					if mt:
						scan.dsn = mt.groups ()[0]
						scan.etext = 'Action: ' + action
						agn.log (agn.LV_VERBOSE, 'dsn', 'Found DSN in Action: %s / Status: %s' % (action, status))
		pl = msg.get_payload (decode = True)
		if not pl:
			pl = msg.get_payload ()
		if type (pl) in types.StringTypes:
			for line in pl.split ('\n'):
				if not scan.section:
					(sec, ent) = self.__match (line, sects)
					if sec:
						scan.section = sec
						scan.entry = ent
						scan.reason = line
						agn.log (agn.LV_VERBOSE, 'match', 'Found pattern "%s" in body "%s"' % (scan.entry.pattern, line))
				if not scan.dsn:
					mt = Rule.DSNRE[0].search (line)
					if mt:
						grps = mt.groups ()
						scan.dsn = grps[0]
						scan.etext = grps[1]
						agn.log (agn.LV_VERBOSE, 'dsn', 'Found DSN %s / Text "%s" in body: %s' % (scan.dsn, scan.etext, line))
					else:
						mt = Rule.DSNRE[1].search (line)
						if mt:
							scan.dsn = mt.groups ()[0]
							scan.etext = line
							agn.log (agn.LV_VERBOSE, 'dsn', 'Found DSN %s in body: %s' % (scan.dsn, line))
		elif type (pl) in [ types.TupleType, types.ListType ]:
			for p in pl:
				self.__scan (p, scan, sects, True, level + 1)
				if scan.section and scan.dsn and scan.minfo:
					break
	
	def scanMessage (self, cinfo, msg, use):
		rc = Scan ()
		rc.minfo = cinfo
		sects = self.__collectSections (use)
		self.__scan (msg, rc, sects, False, 0)
		if rc.section:
			if not rc.dsn:
				if rc.section.name == 'hard':
					rc.dsn = '5.9.9'
				else:
					rc.dsn = '4.9.9'
		if not rc.etext:
			if rc.reason:
				rc.etext = rc.reason
			else:
				rc.etext = ''
		return rc
#
class BAV:
	rules = {}
	rlock = mutex.mutex ()
	x_agn = 'X-AGNMailloop'
	hasSpamAssassin = agn.which ('spamassassin') is not None
	configFile = agn.mkpath (agn.base, 'var', 'lib', 'bav.conf')
	savePattern = agn.mkpath (agn.base, 'var', 'spool', 'filter', '%s-%s')
	extBouncelog = agn.mkpath (agn.base, 'log', 'extbounce.log')
	
	def __init__ (self, raw, msg, mode, dryrun = False):
		self.raw = raw
		self.msg = msg
		self.mode = mode
		self.dryrun = dryrun
		self.parsedEMail = eagn.ParseEMail (raw)
		self.cinfo = self.parsedEMail.getOrigin ()
		self.parm = {}
		if not self.msg.has_key (BAV.x_agn):
			if self.msg.has_key ('return-path'):
				rp = self.msg['return-path'].strip ()
				if rp[0] == '<' and rp[-1] == '>':
					addr = rp[1:-1].lower ()
					try:
						fd = open (BAV.configFile)
						for line in [l for l in fd if len (l) > 0 and l[0] != '#']:
							parts = line.split (None, 1)
							if parts[0].lower () == addr and len (parts) == 2:
								data = (parts[1]).rstrip ('\r\n')
								if data[:7] == 'accept:':
									self.msg[BAV.x_agn] = data[7:]
								break
						fd.close ()
					except IOError, e:
						agn.log (agn.LV_WARNING, 'bav', 'Cannot read file %s %s' % (BAV.configFile, `e.args`))
			else:
				agn.log (agn.LV_WARNING, 'bav', 'No %s header, neither Return-Path: found' % BAV.x_agn)
		if self.msg.has_key (BAV.x_agn):
			for pair in self.msg[BAV.x_agn].split (','):
				try:
					(var, val) = pair.split ('=', 1)
					self.parm[var.strip ()] = val
				except ValueError as e:
					agn.log (agn.LV_WARNING, 'bav', 'Hit invalid control line: %r' % self.msg[BAV.x_agn])
		try:
			rid = self.parm['rid']
		except KeyError:
			rid = 'unspec'
		self.rid = rid
		try:
			sender = self.parm['from']
			if len (sender) > 1 and sender[0] == '<' and sender[-1] == '>':
				sender = sender[1:-1]
				if sender == '':
					sender = 'MAILER-DAEMON'
		except KeyError:
			sender = 'postmaster'
		self.sender = sender
		try:
			self.headerFrom = email.Utils.parseaddr (self.msg['from'])
		except KeyError:
			self.headerFrom = None
		try:
			self.companyID = int (self.parm['cid'])
		except (KeyError, ValueError):
			self.companyID = -1
		if not msg.get_unixfrom ():
			msg.set_unixfrom (time.strftime ('From ' + sender + '  %c'))
		now = time.time ()
		if not self.rules.has_key (rid):
			self.rule = Rule (rid, now)
			if self.rlock.testandset ():
				self.rules[rid] = self.rule
				self.rlock.unlock ()
		else:
			self.rule = self.rules[rid]
			if self.rule.created + Rule.lifetime < now:
				self.rule = Rule (rid, now)
				if self.rlock.testandset ():
					self.rules[rid] = self.rule
					self.rlock.unlock ()
		self.reason = ''

	def saveMessage (self, mid):
		fname = BAV.savePattern % (mid, self.rid)
		if self.dryrun:
			print ('Would save message to "%s"' % fname)
		else:
			try:
				fd = open (fname, 'a')
				fd.write (self.msg.as_string (True) + '\n')
				fd.close ()
			except IOError as e:
				agn.log (agn.LV_ERROR, 'save', 'Unable to save mail copy to %s %s' % (fname, e))
	
	def sendmail (self, msg, to):
		if self.dryrun:
			print ('Would send mail to "%s"' % to)
		else:
			try:
				mailtext = msg.as_string (False)
				cmd = agn._syscfg.sendmail (to)
				pp = subprocess.Popen (cmd, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
				(pout, perr) = pp.communicate (mailtext)
				if pout:
					agn.log (agn.LV_VERBOSE, 'sendmail', 'Sendmail to "%s" outputs this for information:\n%s' % (to, pout))
				if pp.returncode or perr:
					agn.log (agn.LV_WARNING, 'sendmail', 'Sendmail to "%s" returns %d:\n%s' % (to, pp.returncode, str (perr)))
			except Exception, e:
				agn.log (agn.LV_ERROR, 'sendmail', 'Sending mail to %s failed %s' % (to, `e.args`))

	def makeNamespace (self, mailinfo, limitClause):
		ns = {'agn': agn, 'mailinfo': mailinfo, 'user': None, 'mail': None}
		cinfo = self.cinfo
		if cinfo is None:
			cinfo = agn.mutable (customerID = None, mailingID = None)
		if cinfo.customerID is None or cinfo.mailingID is None:
			sm = self.rule.scanMessage (cinfo, self.msg, None)
			if not sm.minfo is None:
				if cinfo.customerID is None:
					cinfo.customerID = sm.minfo.customerID
				if cinfo.mailingID is None:
					cinfo.mailingID = sm.minfo.mailingID
		if self.companyID > 0:
			db = agn.DBaseID ()
			if not db is None:
				cursor = db.cursor ()
				if not cursor is None:
					if cinfo.customerID is None:
						for state in [0, 1]:
							if state == 0:
								if not limitClause:
									continue
								addClause = ' AND %s' % limitClause
							else:
								addClause = ''
							for eMail in [self.sender, self.headerFrom and self.headerFrom[1].lower () or None]:
								if not eMail:
									continue
								for r in cursor.query ('SELECT customer_id FROM customer_%d_tbl WHERE email = :email%s' % (self.companyID, addClause), {'email': eMail}):
									if not r[0] is None:
										if cinfo.customerID is None or r[0] > cinfo.customerID:
											cinfo.customerID = r[0]
								if not cinfo.customerID is None:
									break
							if not cinfo.customerID is None:
								break
					if not cinfo.customerID is None:
						user = agn.mutable ()
						for r in cursor.query ('SELECT * FROM customer_%d_tbl WHERE customer_id = :cid' % self.companyID, {'cid': cinfo.customerID}):
							desc = cursor.description ()
							if not desc is None:
								n = 0
								while n < len (desc):
									user.__dict__[desc[n][0].lower ()] = r[n]
									n += 1
								break
						user.binding = {}
						for r in cursor.query ('SELECT mailinglist_id, user_type, user_status, user_remark, timestamp, creation_date, exit_mailing_id, mediatype FROM customer_%d_binding_tbl WHERE customer_id = :cid' % self.companyID, {'cid': cinfo.customerID}):
							bind = agn.mutable (
								list_id = r[0],
								type = r[1],
								status = r[2],
								remark = r[3],
								timestamp = r[4],
								creation_date = r[5],
								exit_id = r[6],
								media = r[7]
							)
							user.binding[r[0]] = bind
						ns['user'] = user
					if not cinfo.mailingID is None:
						mail = agn.mutable ()
						for r in cursor.query ('SELECT shortname, description, creation_date, mailinglist_id FROM mailing_tbl WHERE mailing_id = :mid', {'mid': cinfo.mailingID}):
							mail.shortname = r[0]
							mail.description = r[1]
							mail.creation_date = r[2]
							mail.list_id = r[3]
						mail.status_field = None
						for r in cursor.query ('SELECT status_field, senddate FROM maildrop_status_tbl WHERE mailing_id = :mid', {'mid': cinfo.mailingID}):
							if not r[0] is None:
								if r[0] in ('W', 'D', 'R', 'E'):
									mail.status_field = r[0]
									if r[0] == 'W':
										mail.senddate = r[1]
									elif r[0] == 'R':
										mail.sendhour = r[1].hour
								elif mail.status_field is None or (mail.status_field == 'A' and r[0] == 'T'):
									mail.status_field = r[0]
						ns['mail'] = mail
					cursor.close ()
				db.close ()
		ns['rid'] = self.rid
		ns['company_id'] = self.companyID
		ns['sender'] = self.sender
		ns['customer_id'] = cinfo.customerID
		ns['mailing_id'] = cinfo.mailingID
		return ns
		
	def enrich (self):
		rc = self.msg
		if not self.rule.enrich is None:
			mailinfo = agn.mutable (type = 'text/plain', charset = 'ISO-8859-1')
			inlined = False
			try:
				filled = self.rule.enrich.fill (self.makeNamespace (mailinfo, self.rule.enrich.property ('limit-clause')))
				inlined = self.rule.enrich.property ('inline')
			except agn.error as e:
				filled = None
				agn.log (agn.LV_ERROR, 'enrich', 'Failed to fill template for rule %s: %s' % (self.rule.rid, e))
			if not filled is None and len (filled.strip ()) > 0:
				rc = email.Message.Message ()
				for (var, val) in self.msg.items ():
					rc[var] = val
				if inlined:
					if self.msg.is_multipart ():
						text = self.msg.as_string (False).replace ('\r\n', '\n')
						n = text.find ('\n\n')
						if n != -1:
							text = text[n + 2:]
					else:
						text = self.msg.get_payload (decode = True)
					if text is None:
						text = filled
					else:
						text = filled + '\n' + text
					rc.set_payload (text, mailinfo.charset)
				else:
					rc.set_type ('multipart/mixed')
					info = email.Message.Message ()
					info.set_type (mailinfo.type)
					info.set_payload (filled, mailinfo.charset)
					rc.attach (info)
					rc.attach (self.msg)
		return rc

	__findScore = re.compile ('score=([0-9]+(\\.[0-9]+)?)')
	def filterWithSpamAssassin (self, fwd):
		pp = subprocess.Popen (['spamassassin'], stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE)
		(pout, perr) = pp.communicate (self.msg.as_string (False))
		if pp.returncode:
			agn.log (agn.LV_WARNING, 'spam', 'Failed to filter mail through spam assassin, returns %d' % pp.returncode)
			if perr:
				agn.log (agn.LV_WARNING, 'spam', 'Error message:\n%s' % perr)
		elif pout:
			nmsg = email.message_from_string (pout)
			if nmsg is not None:
				self.msg = nmsg
				try:
					spamStatus = nmsg['x-spam-status']
					m = self.__findScore.search (spamStatus)
					spamScore = float (m.group (1))
					check = True
					if 'spam_req' in self.parm:
						spamRequired = float (self.parm['spam_req'])
						if spamScore >= spamRequired:
							fwd = None
							check = False
					if check and 'spam_fwd' in self.parm:
						spamForward = float (self.parm['spam_fwd'])
						if spamScore >= spamForward:
							if 'spam_email' in self.parm:
								fwd = self.parm['spam_email']
							else:
								fwd = None
							check = False
				except ValueError, e:
					agn.log (agn.LV_WARNING, 'spam', 'Failed to parse spam score/spam parameter: %s' % str (e))
				except KeyError:
					agn.log (agn.LV_WARNING, 'spam', 'Do not find spam assassin header after filtering')
			else:
				agn.log (agn.LV_WARNING, 'spam', 'Failed to parse filtered mail')
		else:
			agn.log (agn.LV_WARNING, 'spam', 'Failed to retrieve filtered mail')
		return fwd
	
	def unsubscribe (self, customerID, mailingID):
		db = agn.DBaseID ()
		if db is not None:
			cursor = db.cursor ()
			if cursor is not None:
				rq = cursor.querys ('SELECT mailinglist_id, company_id FROM mailing_tbl WHERE mailing_id = :mailingID', {'mailingID': mailingID})
				if rq is not None:
					mailinglistID = rq[0]
					companyID = rq[1]
					query = cursor.rselect ('UPDATE customer_%d_binding_tbl SET user_status = :userStatus, user_remark = :userRemark, timestamp = %%(sysdate)s, exit_mailing_id = :mailingID WHERE customer_id = :customerID AND mailinglist_id = :mailinglistID' % companyID)
					data = {
						'userStatus': agn.UserStatus.ADMOUT,
						'userRemark': 'Opt-out by mandatory CSA link (mailto)',
						'mailingID': mailingID,
						'customerID': customerID,
						'mailinglistID': mailinglistID
					}
					cnt = cursor.update (query, data)
					if cnt == 1:
						agn.log (agn.LV_INFO, 'unsub', 'Unsubscribed customer %d for company %d on mailinglist %d due to mailing %d' % (customerID, companyID, mailinglistID, mailingID))
					else:
						agn.log (agn.LV_WARNING, 'unsub', 'Failed to unsubscribe customer %d for company %d on mailinglist %d due to mailing %d, matching %d rows (expected one row)' % (customerID, companyID, mailinglistID, mailingID, cnt))
					cursor.sync ()
				else:
					agn.log (agn.LV_WARNING, 'unsub', 'No mailinglist for mailing %d found, do not unsubscribe %d' % (mailingID, customerID))
				cursor.close ()
			else:
				agn.log (agn.LV_ERROR, 'unsub', 'Failed to open database: %s' % db.lastError ())
			db.close ()
		else:
			agn.log (agn.LV_ERROR, 'unsub', 'Failed to setup database interface')

	def subscribe (self, address, fullname, company_id, mailinglist_id, formular_id):
		if self.dryrun:
			print ('Would try to subscribe "%s" (%r) on %d/%d sending DOI using %r' % (address, fullname, company_id, mailinglist_id, formular_id))
			return
		db = agn.DBaseID ()
		if not db is None:
			curs = db.cursor ()
			if not curs is None:
				agn.log (agn.LV_INFO, 'sub', 'Try to subscribe %s (%s) for %d to %d using %d' % (address, fullname, company_id, mailinglist_id, formular_id))
				customer_id = None
				newBinding = True
				sendMail = True
				userRemark = 'Subscribe via mailloop #%s' % self.rid
				custids = []
				for rec in curs.query ('SELECT customer_id FROM customer_%d_tbl WHERE email = :email' % company_id, {'email': address }):
					custids.append (rec[0])
				if custids:
					agn.log (agn.LV_INFO, 'sub', 'Found these customer_ids %s for the email %s' % (`custids`, address))
					query = 'SELECT customer_id, user_status FROM customer_%d_binding_tbl WHERE customer_id ' % company_id
					if len (custids) > 1:
						query += 'IN ('
						sep = ''
						for custid in custids:
							query += '%s%d' % (sep, custid)
							sep = ', '
						query += ')'
					else:
						query += '= %d' % custids[0]
					query += ' AND mailinglist_id = %d AND mediatype = 0' % mailinglist_id
					use = None
					for rec in curs.query (query):
						agn.log (agn.LV_INFO, 'sub', 'Found binding [cid, status] %s' % `rec`)
						if rec[1] == agn.UserStatus.ACTIVE:
							if use is None or use[1] != agn.UserStatus.ACTIVE or rec[0] > use[0]:
								use = rec
						elif use is None or (use[1] != agn.UserStatus.ACTIVE and rec[0] > use[0]):
							use = rec
					if not use is None:
						agn.log (agn.LV_INFO, 'sub', 'Use customer_id %d with user_status %d' % (use[0], use[1]))
						customer_id = use[0]
						newBinding = False
						if use[1] in (agn.UserStatus.ACTIVE, agn.UserStatus.WAITCONFIRM):
							agn.log (agn.LV_INFO, 'sub', 'User status is %d, stop processing here' % use[1])
							sendMail = False
						else:
							agn.log (agn.LV_INFO, 'sub', 'Set user status to 5')
							query = 'UPDATE customer_%d_binding_tbl SET timestamp = current_timestamp, user_status = %d, user_remark = :remark WHERE customer_id = %d AND mailinglist_id = %d AND mediatype = 0' % (company_id, agn.UserStatus.WAITCONFIRM, customer_id, mailinglist_id)
							curs.update (query, {'remark': userRemark}, commit = True)
					else:
						customer_id = max (custids)
						agn.log (agn.LV_INFO, 'sub', 'No matching binding found, use cutomer_id %d' % customer_id)
				else:
					datasrcdesc = 'Mailloop #%s' % self.rid
					dsid = agn.Datasource ()
					datasrcid = dsid.getID (datasrcdesc, company_id, 4)
					if db.dbms in ('mysql', 'mariadb'):
						query = 'INSERT INTO customer_%d_tbl (email, gender, mailtype, timestamp, creation_date, datasource_id) ' % company_id + \
							'VALUES (:email, 2, 1, current_timestamp, current_timestamp, %d)' % datasrcid
						data = {'email': address}
						curs.update (query, data, commit = True)
						for rec in curs.query ('SELECT customer_id FROM customer_%d_tbl WHERE email = :email' % company_id, data):
							customer_id = rec[0]
					elif db.dbms == 'oracle':
						for rec in curs.query ('SELECT customer_%d_tbl_seq.nextval FROM dual' % company_id):
							customer_id = rec[0]
						agn.log (agn.LV_INFO, 'sub', 'No customer for email %s found, use new customer_id %d' % (address, customer_id))
						if not customer_id is None:
							agn.log (agn.LV_INFO, 'sub', 'Got datasource id %d for %s' % (datasrcid, datasrcdesc))
							prefix = 'INSERT INTO customer_%d_tbl (customer_id, email, gender, mailtype, timestamp, creation_date, datasource_id' % company_id
							values = 'VALUES (%d, :email, 2, 1, sysdate, sysdate, %d' % (customer_id, datasrcid)
							data = {'email': address}
							parts = fullname.split ()
							while len (parts) > 2:
								if parts[0] and parts[0][-1] == '.':
									parts = parts[1:]
								elif parts[1] and parts[1][-1] == '.':
									parts = parts[:1] + parts[2:]
								else:
									temp = [parts[0], ' '.join (parts[1:])]
									parts = temp
							if len (parts) == 2:
								prefix += ', firstname, lastname'
								values += ', :firstname, :lastname'
								data['firstname'] = parts[0]
								data['lastname'] = parts[1]
							elif len (parts) == 1:
								prefix += ', lastname'
								values += ', :lastname'
								data['lastname'] = parts[0]
							query = prefix + ') ' + values + ')'
							agn.log (agn.LV_INFO, 'sub', 'Using "%s" with %s to write customer to database' % (query, `data`))
							curs.update (query, data, commit = True)
				if not customer_id is None:
					if newBinding:
						query = 'INSERT INTO customer_%d_binding_tbl (customer_id, mailinglist_id, user_type, user_status, user_remark, timestamp, creation_date, mediatype) VALUES (%d, %d, \'W\', %d, :remark, current_timestamp, current_timestamp, 0)' % (company_id, customer_id, mailinglist_id, agn.UserStatus.WAITCONFIRM)
						agn.log (agn.LV_INFO, 'sub', 'Create new binding using "%s"' % query)
						curs.update (query, {'remark': userRemark}, commit = True)
					if sendMail:
						formname = None
						rdir = None
						secret = None
						for rec in curs.query ('SELECT formname FROM userform_tbl WHERE form_id = %d AND company_id = %d' % (formular_id, company_id)):
							if rec[0]:
								formname = rec[0]
						for rec in curs.query ('SELECT rdir_domain FROM mailinglist_tbl WHERE mailinglist_id = %d' % mailinglist_id):
							rdir = rec[0]
						for rec in curs.query ('SELECT rdir_domain, secret_key FROM company_tbl WHERE company_id = %d' % company_id):
							if rdir is None:
								rdir = rec[0]
							secret = rec[1]
						if not formname is None and not rdir is None:
							uid = None
							if secret:
								mailingID = None
								creationDate = None
								for rec in curs.queryc ('SELECT mailing_id, creation_date FROM mailing_tbl WHERE company_id = %d AND (deleted = 0 OR deleted IS NULL)' % company_id):
									if rec[0] is not None:
										mailingID = rec[0]
										creationDate = rec[1]
										break
								if mailingID is not None:
									uid = agn.UID ()
									uid.companyID = company_id
									uid.mailingID = mailingID
									uid.customerID = customer_id
									uid.secret = secret
									uid.setTimestamp (creationDate)
								else:
									agn.log (agn.LV_ERROR, 'sub', 'Failed to find active mailing for company %d' % company_id)
							if uid is not None:
								url = '%s/form.do?agnCI=%d&agnFN=%s&agnUID=%s' % (rdir, company_id, urllib.quote (formname), uid.createUID ())
								agn.log (agn.LV_INFO, 'sub', 'Trigger mail using "%s"' % url)
								try:
									uh = urllib2.urlopen (url)
									resp = uh.read ()
									uh.close ()
									agn.log (agn.LV_INFO, 'sub', 'Subscription request returns "%r"' % resp)
									if len (resp) < 2 or resp[:2].lower () != 'ok':
										agn.log (agn.LV_ERROR, 'sub', 'Subscribe formular "%s" returns error "%r"' % (url, resp))
								except urllib2.HTTPError, e:
									agn.log (agn.LV_ERROR, 'sub', 'Failed to trigger [http] forumlar using "%s": %s' % (url, str (e)))
								except urllib2.URLError, e:
									agn.log (agn.LV_ERROR, 'sub', 'Failed to trigger [prot] forumlar using "%s": %s' % (url, `e.reason`))
							else:
								agn.log (agn.LV_ERROR, 'sub', 'Failed to create UID')
						else:
							if not formname:
								agn.log (agn.LV_ERROR, 'sub', 'No formular with id #%d found' % formular_id)
							if not rdir:
								agn.log (agn.LV_ERROR, 'sub', 'No rdir domain for company #%d/mailinglist #%d found' % (company_id, mailinglist_id))
				curs.close ()
				agn.log (agn.LV_INFO, 'sub', 'Subscribe finished')
			else:
				agn.log (agn.LV_ERROR, 'sub', 'Failed to get database cursor')
			db.close ()
		else:
			agn.log (agn.LV_ERROR, 'sub', 'Failed to setup database')

	def execute_is_no_systemmail (self):
		match = self.rule.matchHeader (self.msg, 'systemmail')
		if not match is None and not match[1].inverse:
			return False
		return True
	
	def execute_filter_or_forward (self):
		if self.parsedEMail.ignore:
			parm = 'ignore'
		else:
			match = self.rule.matchHeader (self.msg, 'filter')
			if not match is None and not match[1].inverse:
				if not match[1].parm:
					parm = 'save'
				else:
					parm = match[1].parm
			else:
				parm = 'sent'
		fwd = None
		if parm == 'sent':
			if 'fwd' in self.parm:
				fwd = self.parm['fwd']
			if self.hasSpamAssassin:
				fwd = self.filterWithSpamAssassin (fwd)
		self.saveMessage (parm)
		if parm == 'sent':
			while self.msg.has_key (BAV.x_agn):
				del self.msg[BAV.x_agn]
			if fwd is not None:
				sendMsg = self.enrich ()
				self.sendmail (sendMsg, fwd)

			if self.parm.has_key ('ar'):
				ar = self.parm['ar']
				if self.parm.has_key ('from') and self.headerFrom and self.headerFrom[1]:
					sender = self.headerFrom[1]
					ar = Autoresponder (ar, sender)
					if self.parm.has_key ('armid'):
						agn.log (agn.LV_INFO, 'fof', 'Trigger autoresponder message for %s' % sender)
						ar.triggerMessage (self.cinfo, self.parm, self.dryrun)
					else:
						nmsg = ar.createMessage (self.msg, self.parm, self.dryrun)
						if nmsg:
							agn.log (agn.LV_INFO, 'fof', 'Forward newly generated autoresponder message to %s' % sender)
							self.sendmail (nmsg, sender)
				else:
					agn.log (agn.LV_INFO, 'fof', 'No sender in original message found')
			if self.parm.has_key ('sub') and self.parm.has_key ('cid') and self.parm.has_key ('from'):
				try:
					(mlist, form) = self.parm['sub'].split (':', 1)
					cid = self.parm['cid']
					if self.headerFrom and self.headerFrom[1]:
						self.subscribe (self.headerFrom[1].lower (), self.headerFrom[0], int (cid), int (mlist), int (form))
				except ValueError, e:
					agn.logexc (agn.LV_ERROR, 'fof', 'Failed to parse subscribe parameter: %r' % (e.args, ))
		return True

	def execute_scan_and_unsubscribe (self):
		if self.parsedEMail.ignore:
			parm = 'ignore'
		else:
			parm = 'unspec'
			scan = self.rule.scanMessage (self.cinfo, self.msg, ['hard', 'soft'])
			if scan and scan.minfo:
				if self.parsedEMail.unsubscribe:
					parm = 'unsubscribe'
					if self.dryrun:
						print ('Would unsubscribe %d due to mailing %d' % (scan.minfo.customerID, scan.minfo.mailingID))
					else:
						self.unsubscribe (scan.minfo.customerID, scan.minfo.mailingID)
				else:
					if scan.section:
						if self.dryrun:
							print ('Would write bouncelog with: %s, %d, %d, %s' % (scan.dsn, scan.minfo.mailingID, scan.minfo.customerID, scan.etext.strip ()))
						else:
							try:
								fd = open (BAV.extBouncelog, 'a')
								fd.write ('%s;%d;%d;0;%d;timestamp=%s\tmailloop=%s\n' % (scan.dsn, agn.licence, scan.minfo.mailingID, scan.minfo.customerID, agn.ParseTimestamp ().dump (datetime.datetime.now ()), scan.etext))
								fd.close ()
							except IOError, e:
								agn.log (agn.LV_ERROR, 'log', 'Unable to write %s %s' % (BAV.extBouncelog, `e.args`))
					if scan.entry and scan.entry.parm:
						parm = scan.entry.parm
		self.saveMessage (parm)
		return True

	def execute (self):
		if self.mode == 0:
			return self.execute_is_no_systemmail ()
		elif self.mode == 1:
			return self.execute_filter_or_forward ()
		elif self.mode == 2:
			return self.execute_scan_and_unsubscribe ()
		self.reason = 'Invalid mode %d' % self.mode
		return False

if __name__ == '__main__':
	import	getopt, errno
	
	running = True
	
	def handler (sig, stack):
		global	running
		running = False
	
	def setupSignals ():
		signal.signal (signal.SIGTERM, handler)
		signal.signal (signal.SIGINT, handler)
		signal.signal (signal.SIGHUP, signal.SIG_IGN)
		signal.signal (signal.SIGPIPE, signal.SIG_IGN)

	class Child:
		def __init__ (self, ws):
			self.ws = ws
			self.pid = None
			self.active = False
		
		def execute (self, size):
			global	running
			
			for path in self.ws:
				ok = True
				try:
					fd = open (path, 'r')
					body = fd.read (size)
					fd.close ()
				except IOError, e:
					agn.log (agn.LV_ERROR, 'child', 'Failed to open %s: %r' % (path, e.args))
					ok = False
				if ok:
					try:
						msg = email.message_from_string (body)
						bav = BAV (body, msg, -1)
						if bav.execute_is_no_systemmail ():
							ok = bav.execute_filter_or_forward ()
						else:
							ok = bav.execute_scan_and_unsubscribe ()
					except Exception as e:
						agn.logexc (agn.LV_ERROR, 'child', 'Fatal: catched failure: %s' % e)
						ok = False
				if ok:
					self.ws.success (bav.sender)
				else:
					self.ws.fail ()
				
				if not running:
					break
			self.ws.done ()
		
		def start (self, size):
			self.pid = os.fork ()
			if self.pid == 0:
				setupSignals ()
				self.execute (size)
				sys.exit (0)
			else:
				self.active = self.pid > 0
		
		def signal (self):
			if self.active:
				try:
					os.kill (self.pid, signal.SIGTERM)
				except OSError, e:
					if e.args[0] == errno.ECHILD:
						self.active = False

		def wait (self):
			if self.active:
				try:
					(pid, status) = os.waitpid (self.pid, os.WNOHANG)
					if pid == self.pid:
						if status:
							if os.WIFEXITED (status):
								err = 'exited with return code %d' % os.WEXITSTATUS (status)
							elif os.WIFSIGNALED (status):
								err = 'died due to signal %d' % os.WTERMSIG (status)
							else:
								err = 'terminated due to unknown status %d' % status
							agn.log (agn.LV_ERROR, 'child', 'Child for ws %s %s' % (self.ws.ws, err))
						self.active = False
				except OSError, e:
					if e.args[0] == errno.ECHILD:
						self.active = False
	
	def waitForChildren (children):
		nchildren = []
		for child in children:
			child.wait ()
			if child.active:
				nchildren.append (child)
		if len (children) != len (nchildren):
			count = len (children) - len (nchildren)
			agn.log (agn.LV_VERBOSE, 'bavd', '%d child%s terminated' % (count, count != 1 and 'ren' or ''))
		return nchildren
	
	def checkProcmailrc (now, destpath):
		prc = agn.mkpath (agn.base, '.procmailrc')
		try:
			fd = open (prc, 'r')
			ocontent = fd.read ()
			fd.close ()
		except IOError, e:
			if e.args[0] != errno.ENOENT:
				agn.log (agn.LV_WARNING, 'chk', 'Failed to read "%s": %r' % (prc, e.args))
			ocontent = ''
		ncontent = """# This file is generated by bavd, do not edit it by hand
:0:
%(path)s/%(ts)s-inbox
""" % {'path': destpath, 'ts': '%04d%02d%02d' % (now.tm_year, now.tm_mon, now.tm_mday)}
		if ocontent != ncontent:
			try:
				fd = open (prc, 'w')
				fd.write (ncontent)
				fd.close ()
				os.chmod (prc, 0600)
				agn.log (agn.LV_INFO, 'chk', 'Create new procmailrc file "%s".' % prc)
			except (IOError, OSError), e:
				agn.log (agn.LV_ERROR, 'chk', 'Failed to install procmailrc file "%s": %r' % (prc, e.args))

	def bavDebug (files):
		agn.outlevel = agn.LV_DEBUG
		agn.outstream = sys.stderr
		agn.loglevel = agn.LV_NONE
		print ('Entering simulation mode')
		for fname in files:
			print ('Try to interpret: %s' % fname)
			try:
				fd = open (fname, 'r')
				content = fd.read ()
				fd.close ()
			except IOError, e:
				print ('Failed to open %s: %r' % (fname, e.args))
				content = None
			if content is not None:
				bav = BAV (content, email.message_from_string (content), -1, True)
				if bav.execute_is_no_systemmail ():
					print ('--> Filter or forward')
					ok = bav.execute_filter_or_forward ()
				else:
					print ('--> Scan and unsubscribe')
					ok = bav.execute_scan_and_unsubscribe ()
				if ok:
					print ('OK')
				else:
					print ('Failed')
	def main ():
		global	running

		setupSignals ()
		maxChildren = 10
		delay = 10
		spooldir = agn.mkpath (agn.base, 'var', 'spool', 'mail')
		worksize = None
		size = 65536
		(opts, parm) = getopt.getopt (sys.argv[1:], 'c:d:s:w:m:')
		for opt in opts:
			if opt[0] == '-c':
				maxChildren = int (opt[1])
			elif opt[0] == '-d':
				delay = int (opt[1])
			elif opt[0] == '-s':
				spooldir = opt[1]
			elif opt[0] == '-w':
				worksize = int (opt[1])
			elif opt[0] == '-m':
				size = int (opt[1])
		if len (parm) > 0:
			bavDebug (parm)
			sys.exit (0)
		agn.lock ()
		agn.log (agn.LV_INFO, 'bavd', 'Starting up')
		lastCheck = -1
		children = []
		spool = eagn.Mailspool (spooldir, worksize = worksize, scan = False, storeSize = size)
		while running:
			now = time.localtime ()
			if now.tm_yday != lastCheck:
				checkProcmailrc (now, spool.store)
				lastCheck = now.tm_yday
			if len (children) < maxChildren:
				if len (children) == 0:
					spool.scanWorkspaces ()
				for ws in spool:
					agn.log (agn.LV_VERBOSE, 'bavd', 'New child starting in %s' % ws.ws)
					ch = Child (ws)
					ch.start (size)
					children.append (ch)
					if len (children) >= maxChildren:
						break
			n = delay
			while running and n > 0:
				time.sleep (1)
				if children:
					children = waitForChildren (children)
				n -= 1
		while children:
			agn.log (agn.LV_VERBOSE, 'bavd', 'Wait for %d children to terminate' % len (children))
			for child in children:
				child.signal ()
			time.sleep (1)
			children = waitForChildren (children)
		agn.log (agn.LV_INFO, 'bavd', 'Going down')
		agn.unlock ()
	#
	main ()
