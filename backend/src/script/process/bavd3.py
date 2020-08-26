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
import	logging, argparse, errno
import	urllib.request,	urllib.parse, urllib.error
import	sys, os, signal, time, re
import	subprocess
from	datetime import datetime
from	email import message_from_string
from	email.message import Message
from	email.utils import parseaddr
from	dataclasses import dataclass
from	typing import Optional
from	typing import Dict, List, NamedTuple, Tuple
from	typing import cast
from	agn3.db import DB, Row
from	agn3.dbm import DBM, dbmerror
from	agn3.definitions import base, licence, syscfg
from	agn3.email import ParseEMail
from	agn3.emm.datasource import Datasource
from	agn3.emm.types import UserStatus
from	agn3.exceptions import error
from	agn3.ignore import Ignore
from	agn3.io import which
from	agn3.log import log
from	agn3.parser import ParseTimestamp
from	agn3.runtime import Runtime
from	agn3.spool import Mailspool
from	agn3.uid import UID
#
logger = logging.getLogger (__name__)
#
class Autoresponder:
	whitelist_file = os.path.join (base, 'var', 'lib', 'ar.whitelist')
	whitelist_pattern = os.path.join (base, 'var', 'lib', 'ar_%s.whitelist')
	limit_pattern = os.path.join (base, 'var', 'lib', 'ar_%s.limit')
	def __init__ (self, aid: str, sender: str) -> None:
		self.aid = aid
		self.sender = sender
	
	def is_in_whitelist (self) -> bool:
		for arwlist in [Autoresponder.whitelist_pattern % self.aid, Autoresponder.whitelist_file]:
			with Ignore (IOError):
				with open (arwlist) as fd:
					for line in (l.rstrip ('\r\n') for l in fd if not l[0] in '\n#'):
						if line == self.sender:
							logger.debug ('Sender %s is on whitelist file %s' % (self.sender, arwlist))
							return True
		return False
	
	def is_limited (self, dryrun: bool) -> bool:
		may_receive = False
		try:
			arlimit = Autoresponder.limit_pattern % self.aid
			now = time.time ()
			with DBM (arlimit, 'c') as dbf:
				sender_as_key = self.sender.encode ('UTF-8')
				if sender_as_key not in dbf:
					logger.debug ('Never sent mail to %s from this autoresponder %s' % (self.sender, self.aid))
					may_receive = True
				else:
					with Ignore (ValueError):
						last = int (dbf[sender_as_key].decode ('UTF-8'))
						if last + 24 * 60 * 60 < now:
							logger.debug ('Last mail to "%s" is older than 24 hours' % self.sender)
							may_receive = True
						else:
							diff = (now - last) / 60
							logger.info ('Reject mail to "%s", sent already mail in last 24 hours (%d:%02d)' % (self.sender, diff / 60, diff % 60))
				if may_receive and not dryrun:
					dbf[sender_as_key] = '{now:d}'.encode ('UTF-8')
		except dbmerror as e:
			logger.error ('Unable to acess %s %s' % (arlimit, e))
			may_receive = False
		return not may_receive
	
	def allow (self, parm: Dict[str, str], dryrun: bool) -> bool:
		if self.is_in_whitelist ():
			return True
		if self.is_limited (dryrun):
			return False
		return True

	def trigger_message (self, cinfo: Optional[ParseEMail.Origin], parm: Dict[str, str], dryrun: bool) -> None:
		try:
			mailing_id = int (parm['armid'])
			company_id = int (parm['cid'])
			if cinfo is None:
				raise error ('failed to determinate origin of mail')
			if not cinfo.valid:
				raise error ('uid from foreign instance %s (expected from %s)' % (cinfo.licence_id, licence))
			customer_id = cinfo.customer_id
			rdir_domain = None
			with DB () as db:
				rq = db.querys (
					'SELECT mailinglist_id, company_id, deleted '
					'FROM mailing_tbl '
					'WHERE mailing_id = :mailing_id',
					{'mailing_id': mailing_id}
				)
				if rq is None:
					raise error ('mailing %d not found' % mailing_id)
				if rq.company_id != company_id:
					raise error ('mailing %d belongs to company %d, but mailloop belongs to company %d' % (mailing_id, rq[1], company_id))
				if rq.deleted:
					logger.info ('mailing %d is marked as deleted' % mailing_id)
				mailinglist_id = rq.mailinglist_id
				#
				rq = db.querys (
					'SELECT user_type '
					'FROM customer_%d_binding_tbl '
					'WHERE customer_id = :customer_id AND mailinglist_id = :mailinglist_id AND mediatype = 0'
					% company_id,
					{
						'customer_id': customer_id,
						'mailinglist_id': mailinglist_id
					}
				)
				if rq is None or rq.user_type not in ('A', 'T', 't'):
					if not self.allow (parm, dryrun):
						raise error ('recipient is not allowed to received (again) an autoresponder')
				else:
					logger.info ('recipient %d on %d for %d is admin/test recipient and not blocked' % (customer_id, mailinglist_id, mailing_id))
				#
				rq = db.querys (
					'SELECT rdir_domain '
					'FROM mailinglist_tbl '
					'WHERE mailinglist_id = :mailinglist_id',
					{'mailinglist_id': mailinglist_id}
				)
				if rq is not None and rq.rdir_domain is not None:
					rdir_domain = rq.rdir_domain
				else:
					rq = db.querys (
						'SELECT rdir_domain '
						'FROM company_tbl '
						'WHERE company_id = :company_id',
						{'company_id': company_id}
					)
					if rq is not None:
						rdir_domain = rq.rdir_domain
				if rdir_domain is None:
					raise error ('failed to determinate rdir-domain')
				#
				rq = db.querys (
					'SELECT security_token '
					'FROM mailloop_tbl '
					'WHERE rid = :rid',
					{'rid': int (self.aid)}
				)
				if rq is None:
					raise error ('no entry in mailloop_tbl for %s found' % self.aid)
				security_token = rq.security_token if rq.security_token else ''
				url = ('%s/sendMailloopAutoresponder.do?'
				       'mailloopID=%s&companyID=%d&customerID=%d&securityToken=%s'
				       % (rdir_domain, self.aid, company_id, customer_id, urllib.parse.quote (security_token)))
				if dryrun:
					print ('Would trigger mail using %s' % url)
				else:
					try:
						uh = urllib.request.urlopen (url)
						response = uh.read ()
						uh.close ()
						logger.info ('Autoresponder mailing %d for customer %d triggered: %s' % (mailing_id, customer_id, response))
					except (urllib.error.URLError, urllib.error.HTTPError) as e:
						logger.error ('Failed to trigger %s: %s' % (url, str (e)))
		except error as e:
			logger.info ('Failed to send autoresponder: %s' % str (e))
		except (KeyError, ValueError, TypeError) as e:
			logger.error ('Failed to parse %r: %s' % (parm, str (e)))
#
class Entry:
	def __init__ (self, line: str) -> None:
		self.eid = line
		self.parm: Optional[str] = None
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
	
	def match (self, line: str) -> bool:
		return self.regexp.search (line) is not None

class Section:
	def __init__ (self, name: str) -> None:
		self.name = name
		self.entries: List[Entry] = []
	
	def append (self, line: str) -> None:
		try:
			self.entries.append (Entry (line))
		except re.error as e:
			logger.error ('Got illegal regular expression "%s" in [%s]: %s' % (line, self.name, e))

	def match (self, line: str) -> Optional[Entry]:
		for e in self.entries:
			if e.match (line):
				return e
		return None

@dataclass
class Scan:
	section: Optional[Section] = None
	entry: Optional[Entry] = None
	reason: Optional[str] = None
	dsn: Optional[str] = None
	etext: Optional[str] = None
	minfo: Optional[ParseEMail.Origin] = None

class Rule:
	lifetime = 180
	rule_pattern = os.path.join (base, 'var', 'lib', 'bav_%s.rule')
	rule_file = os.path.join (base, 'lib', 'bav.rule')
	DSNRE = (
		re.compile ('[45][0-9][0-9] +([0-9]\\.[0-9]\\.[0-9]) +(.*)'),
		re.compile ('\\(#([0-9]\\.[0-9]\\.[0-9])\\)'),
		re.compile ('^([0-9]\\.[0-9]\\.[0-9])')
	)
	
	def __init__ (self, rid: str, now: float) -> None:
		self.rid = rid
		self.created = now
		self.sections: Dict[str, Section] = {}
		for fname in [Rule.rule_pattern % rid, Rule.rule_file]:
			try:
				with open (fname) as fd:
					cur = None
					for line in [l.rstrip ('\r\n') for l in fd if len (l) > 0 and not l[0] in '\n#']:
						if line[0] == '[' and line[-1] == ']':
							name = line[1:-1]
							if name in self.sections:
								cur = self.sections[name]
							else:
								cur = self.sections[name] = Section (name)
						elif cur:
							cur.append (line)
				logger.debug ('Reading rules from %s' % fname)
				break
			except IOError as e:
				logger.debug ('Unable to open %s %s' % (fname, e))
	
	def __collect_sections (self, use: List[str]) -> List[Section]:
		return [_s for (_n, _s) in self.sections.items () if _n in use]
	
	def __match (self, line: str, sects: List[Section]) -> Tuple[Optional[Section], Optional[Entry]]:
		for s in sects:
			entry = s.match (line)
			if entry:
				return (s, entry)
		return (None, None)
	
	def __check_header (self, msg: Message, sects: List[Section]) -> Optional[Tuple[Section, Entry, str]]:
		for (key, value) in msg.items ():
			line = f'{key}: {value}'
			(sec, ent) = self.__match (line, sects)
			if sec and ent:
				reason = '[%s/%s] %s' % (sec.name, ent.eid, line)
				return (sec, ent, reason)
		return None

	def match_header (self, msg: Message, use: List[str]) -> Optional[Tuple[Section, Entry, str]]:
		return self.__check_header (msg, self.__collect_sections (use))

	def __scan (self, msg: Message, scan: Scan, sects: List[Section], checkheader: bool, level: int) -> None:
		if checkheader:
			if not scan.section:
				rc = self.__check_header (msg, sects)
				if rc:
					(scan.section, scan.entry, scan.reason) = rc
		subj = msg['subject']
		if not scan.dsn:
			if subj:
				mt = Rule.DSNRE[0].search (str (subj))
				if mt:
					grps = mt.groups ()
					scan.dsn = grps[0]
					scan.etext = grps[1]
					logger.debug ('Found DSN in Subject: %s' % subj)
			if not scan.dsn:
				action = msg['action']
				status = msg['status']
				if action and status:
					mt = Rule.DSNRE[2].match (str (status))
					if mt:
						scan.dsn = mt.groups ()[0]
						scan.etext = f'Action: {action}'
						logger.debug ('Found DSN in Action: %s / Status: %s' % (action, status))
		pl = msg.get_payload (decode = True)
		if not pl:
			pl = msg.get_payload ()
		if type (pl) is str:
			for line in cast (str, pl).split ('\n'):
				if not scan.section:
					(sec, ent) = self.__match (line, sects)
					if sec and ent:
						scan.section = sec
						scan.entry = ent
						scan.reason = line
						logger.debug ('Found pattern "%s" in body "%s"' % (scan.entry.pattern, line))
				if not scan.dsn:
					mt = Rule.DSNRE[0].search (line)
					if mt:
						grps = mt.groups ()
						scan.dsn = grps[0]
						scan.etext = grps[1]
						logger.debug ('Found DSN %s / Text "%s" in body: %s' % (scan.dsn, scan.etext, line))
					else:
						mt = Rule.DSNRE[1].search (line)
						if mt:
							scan.dsn = mt.groups ()[0]
							scan.etext = line
							logger.debug ('Found DSN %s in body: %s' % (scan.dsn, line))
		elif type (pl) is list:
			for p in cast (List[Message], pl):
				self.__scan (p, scan, sects, True, level + 1)
				if scan.section and scan.dsn and scan.minfo:
					break
	
	def scan_message (self, cinfo: Optional[ParseEMail.Origin], msg: Message, use: List[str]) -> Scan:
		rc = Scan ()
		rc.minfo = cinfo
		sects = self.__collect_sections (use)
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
	rules: Dict[str, Rule] = {}
	x_agn = 'X-AGNMailloop'
	has_spamassassin = which ('spamassassin') is not None
	config_file = os.path.join (base, 'var', 'lib', 'bav.conf')
	save_pattern = os.path.join (base, 'var', 'spool', 'filter', '%s-%s')
	ext_bouncelog = os.path.join (base, 'log', 'extbounce.log')
	class From (NamedTuple):
		realname: str
		address: str
	
	def __init__ (self, raw: str, msg: Message, dryrun: bool = False) -> None:
		self.raw = raw
		self.msg = msg
		self.dryrun = dryrun
		self.parsedEMail = ParseEMail (raw)
		self.cinfo = self.parsedEMail.get_origin ()
		self.parm: Dict[str, str] = {}
		if BAV.x_agn not in self.msg:
			if 'return-path' in self.msg:
				rp = cast (str, self.msg['return-path']).strip ()
				if rp[0] == '<' and rp[-1] == '>':
					addr = rp[1:-1].lower ()
					try:
						with open (BAV.config_file) as fd:
							for line in [l for l in fd if len (l) > 0 and l[0] != '#']:
								parts = line.split (None, 1)
								if parts[0].lower () == addr and len (parts) == 2:
									data = (parts[1]).rstrip ('\r\n')
									if data.startswith ('accept:'):
										self.msg[BAV.x_agn] = data.split (':', 1)[-1]
									break
					except IOError as e:
						logger.warning ('Cannot read file %s %s' % (BAV.config_file, e))
			else:
				logger.warning ('No %s header, neither Return-Path: found' % BAV.x_agn)
		if BAV.x_agn in self.msg:
			for pair in cast (str, self.msg[BAV.x_agn]).split (','):
				try:
					(var, val) = pair.split ('=', 1)
					self.parm[var.strip ()] = val
				except ValueError:
					logger.warning ('Hit invalid control line: %r' % self.msg[BAV.x_agn])
		self.rid = self.parm.get ('rid', 'unknown')
		try:
			sender = self.parm['from']
			if len (sender) > 1 and sender[0] == '<' and sender[-1] == '>':
				sender = sender[1:-1]
				if sender == '':
					sender = 'MAILER-DAEMON'
		except KeyError:
			sender = 'postmaster'
		self.sender = sender
		self.header_from: Optional[BAV.From] = BAV.From (*parseaddr (cast (str, self.msg['from']))) if 'from' in self.msg else None
		try:
			self.company_id = int (self.parm['cid'])
		except (KeyError, ValueError):
			self.company_id = -1
		if not msg.get_unixfrom ():
			msg.set_unixfrom (time.strftime ('From ' + sender + '  %c'))
		now = time.time ()
		if self.rid not in self.rules or self.rules[self.rid].created + Rule.lifetime < now:
			self.rule = self.rules[self.rid] = Rule (self.rid, now)
		else:
			self.rule = self.rules[self.rid]
		self.reason = ''

	def save_message (self, mid: str) -> None:
		fname = BAV.save_pattern % (mid, self.rid)
		if self.dryrun:
			print ('Would save message to "%s"' % fname)
		else:
			try:
				with open (fname, 'a') as fd:
					fd.write (self.msg.as_string (True) + '\n')
			except IOError as e:
				logger.error ('Unable to save mail copy to %s %s' % (fname, e))
	
	def sendmail (self, msg: Message, to: str) -> None:
		if self.dryrun:
			print ('Would send mail to "%s"' % to)
		else:
			try:
				mailtext = msg.as_string (False)
				cmd = syscfg.sendmail (to)
				pp = subprocess.Popen (cmd, stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE, text = True, errors = 'backslashreplace')
				(pout, perr) = pp.communicate (mailtext)
				if pout:
					logger.debug ('Sendmail to "%s" outputs this for information:\n%s' % (to, pout))
				if pp.returncode or perr:
					logger.warning ('Sendmail to "%s" returns %d:\n%s' % (to, pp.returncode, perr))
			except Exception as e:
				logger.exception ('Sending mail to %s failed %s' % (to, e))

	__find_score = re.compile ('score=([0-9]+(\\.[0-9]+)?)')
	def filter_with_spam_assassin (self, fwd: Optional[str]) -> Optional[str]:
		pp = subprocess.Popen (['spamassassin'], stdin = subprocess.PIPE, stdout = subprocess.PIPE, stderr = subprocess.PIPE, text = True, errors = 'backslashreplace')
		(pout, perr) = pp.communicate (self.msg.as_string (False))
		if pp.returncode:
			logger.warning ('Failed to filter mail through spam assassin, returns %d' % pp.returncode)
			if perr:
				logger.warning ('Error message:\n%s' % perr)
		elif pout:
			nmsg = message_from_string (pout)
			if nmsg is not None:
				self.msg = nmsg
				spam_status = nmsg['x-spam-status']
				if spam_status is not None:
					try:
						m = self.__find_score.search (cast (str, spam_status))
						if m is not None:
							spam_score = float (m.group (1))
							if 'spam_req' in self.parm and float (self.parm['spam_req']) < spam_score:
								fwd = None
							elif 'spam_fwd' in self.parm and float (self.parm['spam_fwd']) < spam_score:
								fwd = self.parm.get ('spam_email')
					except ValueError as e:
						logger.warning ('Failed to parse spam score/spam parameter: %s' % str (e))
				else:
					logger.warning ('Do not find spam assassin header after filtering')
			else:
				logger.warning ('Failed to parse filtered mail')
		else:
			logger.warning ('Failed to retrieve filtered mail')
		return fwd
	
	def unsubscribe (self, customer_id: int, mailing_id: int) -> None:
		with DB () as db:
			rq = db.querys ('SELECT mailinglist_id, company_id FROM mailing_tbl WHERE mailing_id = :mailing_id', {'mailing_id': mailing_id})
			if rq is not None:
				mailinglist_id = rq.mailinglist_id
				company_id = rq.company_id
				cnt = db.update (
					'UPDATE customer_%d_binding_tbl '
					'SET user_status = :userStatus, user_remark = :user_remark, timestamp = current_timestamp, exit_mailing_id = :mailing_id '
					'WHERE customer_id = :customer_id AND mailinglist_id = :mailinglist_id'
					% company_id,
					{
						'userStatus': UserStatus.ADMOUT.value,
						'user_remark': 'Opt-out by mandatory CSA link (mailto)',
						'mailing_id': mailing_id,
						'customer_id': customer_id,
						'mailinglist_id': mailinglist_id
					}
				)
				if cnt == 1:
					logger.info ('Unsubscribed customer %d for company %d on mailinglist %d due to mailing %d' % (customer_id, company_id, mailinglist_id, mailing_id))
				else:
					logger.warning ('Failed to unsubscribe customer %d for company %d on mailinglist %d due to mailing %d, matching %d rows (expected one row)' % (customer_id, company_id, mailinglist_id, mailing_id, cnt))
				db.sync ()

	def subscribe (self, address: str, fullname: str, company_id: int, mailinglist_id: int, formular_id: int) -> None:
		if self.dryrun:
			print ('Would try to subscribe "%s" (%r) on %d/%d sending DOI using %r' % (address, fullname, company_id, mailinglist_id, formular_id))
			return
		with DB () as db:
			logger.info ('Try to subscribe %s (%s) for %d to %d using %d' % (address, fullname, company_id, mailinglist_id, formular_id))
			customer_id: Optional[int] = None
			new_binding = True
			send_mail = True
			user_remark = 'Subscribe via mailloop #%s' % self.rid
			custids = db.stream ('SELECT customer_id FROM customer_%d_tbl WHERE email = :email' % company_id, {'email': address }).map (lambda r: r.customer_id).list ()
			if custids:
				logger.info ('Found these customer_ids %s for the email %s' % (custids, address))
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
				use: Optional[Row] = None
				for rec in db.query (query):
					logger.info ('Found binding [cid, status] %s' % rec)
					if rec.user_status == UserStatus.ACTIVE.value:
						if use is None or use.user_status != UserStatus.ACTIVE.value or rec.user_status > use.user_status:
							use = rec
					elif use is None or (use.user_status != UserStatus.ACTIVE.value and rec.customer_id > use.customer_id):
						use = rec
				if use is not None:
					logger.info ('Use customer_id %d with user_status %d' % (use.customer_id, use.user_status))
					customer_id = use.customer_id
					new_binding = False
					if use.user_status in (UserStatus.ACTIVE.value, UserStatus.WAITCONFIRM.value):
						logger.info ('User status is %d, stop processing here' % use.user_status)
						send_mail = False
					else:
						logger.info ('Set user status to 5')
						db.update (
							'UPDATE customer_%d_binding_tbl '
							'SET timestamp = current_timestamp, user_status = :user_status, user_remark = :user_remark '
							'WHERE customer_id = :customer_id AND mailinglist_id = :mailinglist_id AND mediatype = 0'
							% company_id,
							{
								'user_status': UserStatus.WAITCONFIRM.value,
								'user_remark': user_remark,
								'customer_id': customer_id,
								'mailinglist_id': mailinglist_id
							},
							commit = True
						)
				else:
					customer_id = max (custids)
					logger.info ('No matching binding found, use cutomer_id %s' % customer_id)
			else:
				datasource_description = 'Mailloop #%s' % self.rid
				dsid = Datasource ()
				datasource_id = dsid.get_id (datasource_description, company_id, 4)
				if db.dbms in ('mysql', 'mariadb'):
					db.update (
						'INSERT INTO customer_%d_tbl (email, gender, mailtype, timestamp, creation_date, datasource_id) '
						'VALUES (:email, 2, 1, current_timestamp, current_timestamp, :datasource_id)'
						% company_id,
						{
							'email': address,
							'datasource_id': datasource_id
						},
						commit = True
					)
					for rec in db.query ('SELECT customer_id FROM customer_%d_tbl WHERE email = :email' % company_id, {'email': address}):
						customer_id = rec.customer_id
				elif db.dbms == 'oracle':
					for rec in db.query ('SELECT customer_%d_tbl_seq.nextval FROM dual' % company_id):
						customer_id = rec.customer_id
					logger.info ('No customer for email %s found, use new customer_id %s' % (address, customer_id))
					if customer_id is not None:
						logger.info ('Got datasource id %s for %s' % (datasource_id, datasource_description))
						prefix = 'INSERT INTO customer_%d_tbl (customer_id, email, gender, mailtype, timestamp, creation_date, datasource_id' % company_id
						values = 'VALUES (:customer_id, :email, 2, 1, sysdate, sysdate, :datasource_id'
						data = {
							'customer_id': customer_id,
							'email': address,
							'datasource_id': datasource_id
						}
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
						logger.info ('Using "%s" with %s to write customer to database' % (query, data))
						try:
							db.update (query, data, commit = True)
						except Exception as e:
							logger.error ('Failed to insert new customer %r: %s' % (address, e))
							customer_id = None
			if customer_id is not None:
				if new_binding:
					db.update (
						'INSERT INTO customer_%d_binding_tbl '
						'            (customer_id, mailinglist_id, user_type, user_status, user_remark, timestamp, creation_date, mediatype) '
						'VALUES '
						'            (:customer_id, :mailinglist_id, :user_type, :user_status, :user_remark, current_timestamp, current_timestamp, 0)'
						% company_id,
						{
							'customer_id': customer_id,
							'mailinglist_id': mailinglist_id,
							'user_type': 'W',
							'user_status': UserStatus.WAITCONFIRM.value,
							'user_remark': user_remark
						},
						commit = True
					)
					logger.info ('Created new binding using')
				if send_mail:
					formname = None
					rdir = None
					secret = None
					for rec in db.query ('SELECT formname FROM userform_tbl WHERE form_id = %d AND company_id = %d' % (formular_id, company_id)):
						if rec.forname:
							formname = rec.forname
					for rec in db.query ('SELECT rdir_domain FROM mailinglist_tbl WHERE mailinglist_id = %d' % mailinglist_id):
						rdir = rec.rdir_domain
					for rec in db.query ('SELECT rdir_domain, secret_key FROM company_tbl WHERE company_id = %d' % company_id):
						if rdir is None:
							rdir = rec.rdir_domain
						secret = rec.secret_key
					if not formname is None and not rdir is None:
						uid = None
						if secret:
							mailing_id: Optional[int] = None
							creation_date: Optional[datetime] = None
							for rec in db.queryc ('SELECT mailing_id, creation_date FROM mailing_tbl WHERE company_id = %d AND (deleted = 0 OR deleted IS NULL)' % company_id):
								if rec.mailing_id is not None:
									mailing_id = rec.mailing_id
									creation_date = rec.creation_date
									break
							if mailing_id is not None and creation_date is not None:
								uid = UID ()
								uid.company_id = company_id
								uid.mailing_id = mailing_id
								uid.customer_id = customer_id
								uid.secret = secret
								uid.set_timestamp (creation_date)
							else:
								logger.error ('Failed to find active mailing for company %d' % company_id)
						if uid is not None:
							url = '%s/form.do?agnCI=%d&agnFN=%s&agnUID=%s' % (rdir, company_id, urllib.parse.quote (formname), uid.create ())
							logger.info ('Trigger mail using "%s"' % url)
							try:
								uh = urllib.request.urlopen (url)
								resp = uh.read ()
								uh.close ()
								logger.info ('Subscription request returns "%r"' % resp)
								if len (resp) < 2 or resp[:2].lower () != 'ok':
									logger.error ('Subscribe formular "%s" returns error "%r"' % (url, resp))
							except urllib.error.HTTPError as e:
								logger.error ('Failed to trigger [http] forumlar using "%s": %s' % (url, str (e)))
							except urllib.error.URLError as e:
								logger.error ('Failed to trigger [prot] forumlar using "%s": %s' % (url, e))
						else:
							logger.error ('Failed to create UID')
					else:
						if not formname:
							logger.error ('No formular with id #%d found' % formular_id)
						if not rdir:
							logger.error ('No rdir domain for company #%d/mailinglist #%d found' % (company_id, mailinglist_id))

	def execute_is_no_systemmail (self) -> bool:
		if self.parsedEMail.unsubscribe:
			return False
		match = self.rule.match_header (self.msg, ['systemmail'])
		if match is not None and not match[1].inverse:
			return False
		return True
	
	def execute_filter_or_forward (self) -> bool:
		if self.parsedEMail.ignore:
			parm = 'ignore'
		else:
			match = self.rule.match_header (self.msg, ['filter'])
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
			if self.has_spamassassin:
				fwd = self.filter_with_spam_assassin (fwd)
		self.save_message (parm)
		if parm == 'sent':
			while BAV.x_agn in self.msg:
				del self.msg[BAV.x_agn]
			if fwd is not None:
				self.sendmail (self.msg, fwd)

			if 'ar' in self.parm:
				ar = self.parm['ar']
				if 'from' in self.parm and self.header_from is not None and self.header_from.address:
					sender = self.header_from.address
					auto_responder = Autoresponder (ar, sender)
					if 'armid' in self.parm:
						logger.info ('Trigger autoresponder message for %s' % sender)
						auto_responder.trigger_message (self.cinfo, self.parm, self.dryrun)
					else:
						logger.warning ('Old autorepsonder without content found')
				else:
					logger.info ('No sender in original message found')
			if 'sub' in self.parm and 'cid' in self.parm and 'from' in self.parm:
				try:
					(mlist, form) = self.parm['sub'].split (':', 1)
					cid = self.parm['cid']
					if self.header_from is not None and self.header_from.address:
						self.subscribe (self.header_from.address.lower (), self.header_from.realname, int (cid), int (mlist), int (form))
				except ValueError as e:
					logger.exception ('Failed to parse subscribe parameter: %r' % (e.args, ))
		return True

	def execute_scan_and_unsubscribe (self) -> bool:
		if self.parsedEMail.ignore:
			parm = 'ignore'
		else:
			parm = 'unsub' if self.parsedEMail.unsubscribe else 'unspec'
			scan = self.rule.scan_message (self.cinfo, self.msg, ['hard', 'soft'])
			if scan and scan.minfo:
				if self.parsedEMail.unsubscribe:
					parm = 'unsubscribe'
					if self.dryrun:
						print ('Would unsubscribe %d due to mailing %d' % (scan.minfo.customer_id, scan.minfo.mailing_id))
					else:
						self.unsubscribe (scan.minfo.customer_id, scan.minfo.mailing_id)
				else:
					if scan.section:
						if self.dryrun:
							print ('Would write bouncelog with: %s, %d, %d, %s' % (scan.dsn, scan.minfo.mailing_id, scan.minfo.customer_id, scan.etext))
						else:
							try:
								with open (BAV.ext_bouncelog, 'a') as fd:
									fd.write ('%s;%s;%s;0;%s;timestamp=%s\tmailloop=%s\n' % (scan.dsn, licence, scan.minfo.mailing_id, scan.minfo.customer_id, ParseTimestamp ().dump (datetime.now ()), scan.etext))
							except IOError as e:
								logger.error ('Unable to write %s %s' % (BAV.ext_bouncelog, e))
					if scan.entry and scan.entry.parm:
						parm = scan.entry.parm
		self.save_message (parm)
		return True

class BAVD (Runtime):
	def check_procmailrc (self, now: time.struct_time, spool: Mailspool) -> None:
		prc = os.path.join (base, '.procmailrc')
		try:
			with open (prc) as fd:
				ocontent = fd.read ()
		except IOError as e:
			if e.args[0] != errno.ENOENT:
				logger.warning ('Failed to read "%s": %r' % (prc, e.args))
			ocontent = ''
		ncontent = """# This file is generated by bavd, do not edit it by hand
:0:
%(path)s/%(ts)s-inbox
""" % {'path': spool.store, 'ts': '%04d%02d%02d' % (now.tm_year, now.tm_mon, now.tm_mday)}
		if ocontent != ncontent:
			try:
				with open (prc, 'w') as fd:
					fd.write (ncontent)
				os.chmod (prc, 0o600)
				logger.info ('Create new procmailrc file "%s".' % prc)
			except (IOError, OSError) as e:
				logger.error ('Failed to install procmailrc file "%s": %r' % (prc, e.args))

	def bav_debug (self, files: List[str]) -> None:
		log.outlevel = logging.DEBUG
		log.outstream = sys.stderr
		log.loglevel = logging.FATAL
		print ('Entering simulation mode')
		for fname in files:
			print ('Try to interpret: %s' % fname)
			try:
				with open (fname) as fd:
					content = fd.read ()
				bav = BAV (content, message_from_string (content), True)
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
			except IOError as e:
				print ('Failed to open %s: %r' % (fname, e.args))
	
	def setup (self) -> None:
		self.max_children = 10
		self.delay = 10
		self.spooldir = os.path.join (base, 'var', 'spool', 'mail')
		self.worksize = None
		self.size = 65536
		self.files: List[str] = []
	
	def supports (self, option: str) -> bool:
		return option != 'dryrun'
		
	def add_arguments (self, parser: argparse.ArgumentParser) -> None:
		parser.add_argument ('-C', '--max-children', action = 'store', type = int, default = self.max_children, help = f'Set maximum number of subprocesses (default {self.max_children})', dest = 'max_children')
		parser.add_argument ('-D', '--delay', action = 'store', type = int, default = self.delay, help = f'Set delay in seconds between scan for new mails (defailt {self.delay})')
		parser.add_argument ('-S', '--spool-directory', action = 'store', default = self.spooldir, help = f'Set directory for mail processing (default {self.spooldir})', dest = 'spool_directory')
		parser.add_argument ('-W', '--worksize', action = 'store', type = int, default = self.worksize, help = 'Set size for each single batch to process')
		parser.add_argument ('-L', '--size-limit', action = 'store', type = int, default = self.size, help = f'Set limit for incoming mails before they are truncated (default {self.size} bytes)', dest = 'size_limit')
		
	def use_arguments (self, args: argparse.Namespace) -> None:
		self.max_children = args.max_children
		self.delay = args.delay
		self.spooldir = args.spool_directory
		self.worksize = args.worksize
		self.size = args.size_limit
		self.files = args.parameter
	
	class Child:
		__slots__ = ['ref', 'ws', 'pid', 'active']
		def __init__ (self, ref: BAVD, ws: Mailspool.Workspace) -> None:
			self.ref = ref
			self.ws = ws
			self.pid: Optional[int] = None
			self.active = False
		
		def execute (self, size: int) -> None:
			for path in self.ws:
				ok = False
				try:
					with open (path, errors = 'backslashreplace') as fd:
						body = fd.read (size)
					msg = message_from_string (body)
					bav = BAV (body, msg)
					if bav.execute_is_no_systemmail ():
						ok = bav.execute_filter_or_forward ()
					else:
						ok = bav.execute_scan_and_unsubscribe ()
				except IOError as e:
					logger.error ('Failed to open %s: %r' % (path, e.args))
				except Exception as e:
					logger.exception ('Fatal: catched failure: %s' % e)
				if ok:
					self.ws.success (bav.sender)
				else:
					self.ws.fail ()
				if not self.ref.running:
					break
			self.ws.done ()
		
		def start (self, size: int) -> None:
			self.pid = os.fork ()
			if self.pid == 0:
				try:
					self.ref.setup_handler (master = False)
					self.execute (size)
				except Exception as e:
					logger.exception (f'{self.ws}: failed due to {e}')
				os._exit (0)
			else:
				self.active = self.pid > 0
		
		def signal (self) -> None:
			if self.active and self.pid is not None:
				try:
					os.kill (self.pid, signal.SIGTERM)
				except OSError as e:
					if e.args[0] == errno.ECHILD:
						self.active = False

		def wait (self) -> None:
			if self.active and self.pid is not None:
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
							logger.error ('Child for ws %s %s' % (self.ws.path, err))
						self.active = False
				except OSError as e:
					if e.args[0] == errno.ECHILD:
						self.active = False
	
	def wait_for_children (self, children: List[BAVD.Child]) -> List[BAVD.Child]:
		nchildren: List[BAVD.Child] = []
		for child in children:
			child.wait ()
			if child.active:
				nchildren.append (child)
		if len (children) != len (nchildren):
			count = len (children) - len (nchildren)
			logger.debug ('%d child%s terminated' % (count, count != 1 and 'ren' or ''))
		return nchildren
	
	def executor (self) -> bool:
		if self.files:
			self.bav_debug (self.files)
			return True
		#
		lastCheck = -1
		children: List[BAVD.Child] = []
		spool = Mailspool (self.spooldir, worksize = self.worksize, scan = False, store_size = self.size)
		while self.running:
			now = time.localtime ()
			if now.tm_yday != lastCheck:
				self.check_procmailrc (now, spool)
				lastCheck = now.tm_yday
			if len (children) < self.max_children:
				if len (children) == 0:
					spool.scan_workspaces ()
				for ws in spool:
					logger.debug ('New child starting in %s' % ws.path)
					ch = BAVD.Child (self, ws)
					ch.start (self.size)
					children.append (ch)
					if len (children) >= self.max_children:
						break
			n = self.delay
			while self.running and n > 0:
				time.sleep (1)
				if children:
					children = self.wait_for_children (children)
				n -= 1
		while children:
			logger.debug ('Wait for %d children to terminate' % len (children))
			for child in children:
				child.signal ()
			time.sleep (1)
			children = self.wait_for_children (children)
		return True
	#
if __name__ == '__main__':
	BAVD.main ()
