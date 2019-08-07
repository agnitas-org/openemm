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
import	sys, os, getopt, signal, time, errno, socket
import	re
import	subprocess
import	email.Message, email.Header, email.Charset
import	agn
agn.loglevel = agn.LV_INFO
#
charset = 'UTF-8'
_csetname = charset.lower ()
_cset = email.Charset.CHARSETS[_csetname]
email.Charset.CHARSETS[_csetname] = (email.Charset.QP, email.Charset.QP, _cset[2])
del _csetname, _cset

def fileReader (fname):
	with open (fname, 'r') as fd:
		rc = [line.rstrip ('\r\n') for line in fd if not line[0] in '\n#']
	return rc

class Autoresponder:
	directory = agn.mkpath (agn.base, 'var', 'lib')
	def __init__ (self, rid, timestamp, sender, subject, text, html, armid, arst):
		self.rid = rid
		self.timestamp = timestamp
		self.sender = sender
		self.subject = subject
		self.text = self._encode (text)
		self.html = self._encode (html)
		self.armid = armid
		self.arst = arst
		self.fname = self.directory + os.sep + 'ar_%s.mail' % rid
		self.limit = self.directory + os.sep + 'ar_%s.limit' % rid

	def _encode (self, s):
		global	charset
		
		if s and charset != 'UTF-8':
			try:
				s = agn.toutf8 (s, charset)
			except Exception, e:
				agn.log (agn.LV_ERROR, 'auto', 'Failed to convert autoresponder text for %s %s' % (self.rid, str (e)))
		return s

	def _mkheader (self, s):
		global	charset
		
		rc = ''
		for w in s.split ():
			needEncode = False
			for c in w:
				if ord (c) > 127:
					needEncode = True
					break
			if rc:
				rc += ' '
			if needEncode:
				h = email.Header.Header (w, charset)
				rc += h.encode ()
			else:
				rc += w
		return rc
	
	def _prepmsg (self, m, isroot, mtype, pl):
		global	charset

		m.set_payload (pl)
		m.set_charset (charset)
		m.set_type (mtype)
		if not isroot:
			del m['mime-version']

	def writeFile (self):
		if self.armid:
			if os.path.isfile (self.fname):
				try:
					os.unlink (self.fname)
				except OSError, e:
					agn.log (agn.LV_ERROR, 'auto', 'Unable to remove file %s: %s' % (self.fname, str (e)))
		else:
			msg = email.Message.Message ()
			if self.sender:
				msg['From'] = self._mkheader (self.sender)
			if self.subject:
				msg['Subject'] = self._mkheader (self.subject)
			if not self.html:
				self._prepmsg (msg, True, 'text/plain', self.text)
			else:
				text = email.Message.Message ()
				html = email.Message.Message ()
				self._prepmsg (text, False, 'text/plain', self.text)
				self._prepmsg (html, False, 'text/html', self.html)
				msg.set_type ('multipart/alternative')
				msg.attach (text)
				msg.attach (html)
			try:
				fd = open (self.fname, 'w')
				fd.write (msg.as_string (False) + '\n')
				fd.close ()
			except IOError, e:
				agn.log (agn.LV_ERROR, 'auto', 'Unable to write message %s %s' % (self.fname, `e.args`))
	
	def removeFile (self):
		for fname in self.fname, self.limit:
			if os.path.isfile (fname):
				try:
					os.unlink (fname)
				except OSError, e:
					agn.log (agn.LV_ERROR, 'auto', 'Unable to remove file %s: %s' % (fname, str (e)))
#
class Data (object): 
	configFilename = agn.mkpath (agn.base, 'var', 'lib', 'bav.conf')
	localFilename = agn.mkpath (agn.base, 'var', 'lib', 'bav.conf-local')
	ruleDirectory = agn.mkpath (agn.base, 'var', 'lib')
	ruleFile = agn.mkpath (agn.base, 'lib', 'bav.rule')
	rulePattern = re.compile ('bav_([0-9]+).rule')
	ruleFormat = agn.mkpath (ruleDirectory, 'bav_%d.rule')
	updateLog = agn.mkpath (agn.base, 'var', 'run', 'bav-update.log')
	controlSendmail = agn.mkpath (agn.base, 'bin', 'smctrl')
	restartSendmail = agn.mkpath (agn.base, 'var', 'run', 'sendmail-control.sh')
	sendmailBase = '/etc/mail'
	def __init__ (self):
		self.fixdomain = agn._syscfg.get ('filter-name', 'localhost')
		self.mta = agn.MTA ()
		self.domains = []
		self.prefix = 'aml_'
		self.last = None
		self.autoresponder = []
		self.mtdom = {}
		self.readMailertable ()
		try:
			files = os.listdir (Autoresponder.directory)
			for fname in files:
				if len (fname) > 8 and fname[:3] == 'ar_' and fname[-5:] == '.mail':
					rid = fname[3:-5]
					self.autoresponder.append (Autoresponder (rid, 0, None, None, None, None, None, None))
		except OSError, e:
			agn.log (agn.LV_ERROR, 'data', 'Unable to read directory %s %s' % (Autoresponder.directory, `e.args`))
		self.updateCount = 0
	
	def readMailertable (self, newDomains = None):
		self.domains = []
		self.mtdom = {}
		if self.mta.mta == 'postfix':
			def find (key, defaultValue):
				rc = agn.mutable (path = None, content = [], modified = False, hash = None)
				try:
					for element in self.mta.getlist (key):
						try:
							(hash, path) = element.split (':', 1)
						except ValueError:
							hash = None
							path = element
						if path.startswith (agn.base):
							if rc.path is None:
								rc.path = path
								rc.hash = hash
							if not os.path.isfile (path):
								agn.createPath (os.path.dirname (path))
								open (path, 'w').close ()
								if hash is not None:
									self.mta.postfixMake (path)
					if rc.path is not None:
						try:
							fd = open (rc.path)
							for line in (_l.strip () for _l in fd):
								try:
									(var, val) = [_v.strip () for _v in line.split (None, 1)]
								except ValueError:
									var = line
									val = defaultValue
									rc.modified = True
								if var not in [_c[0] for _c in rc.content]:
									rc.content.append ((var, val))
								else:
									rc.modified = True
							fd.close ()
							agn.log (agn.LV_VERBOSE, 'data', 'Read %d lines from %s' % (len (rc.content), rc.path))
						except OSError, e:
							agn.log (agn.LV_ERROR, 'data', 'Failed to read %s: %s' % (rc.path, str (e)))
					else:
						agn.log (agn.LV_WARNING, 'data', 'No path for postfix parameter %s found' % key)
				except KeyError:
					pass
				return rc
			def save (ct):
				if ct.path is not None and (ct.modified or not os.path.isfile (ct.path)):
					try:
						fd = open (ct.path, 'w')
						if ct.content:
							fd.write ('\n'.join (['%s\t%s' % _c for _c in ct.content]) + '\n')
						fd.close ()
						agn.log (agn.LV_INFO, 'data', 'Written %d lines to %s' % (len (ct.content), ct.path))
						if ct.hash is not None:
							self.mta.postfixMake (ct.path)
					except OSError, e:
						agn.log (agn.LV_ERROR, 'data', 'Failed to save %s: %s' % (ct.path, str (e)))
			#
			relayDefaultValue = 'dummy'
			relays = find ('relay_domains', relayDefaultValue)
			for (domain, _) in relays.content:
				self.mtdom[domain] = 0
			#
			def addRelayDomain (domainToAdd):
				if domainToAdd and domainToAdd not in self.mtdom:
					relays.content.append ((domainToAdd, relayDefaultValue))
					relays.modified = True
					self.mtdom[domainToAdd] = 0
			#
			if newDomains:
				for domain in newDomains:
					addRelayDomain (domain)
			transportDefaultValue = 'mailloop:'
			transports = find ('transport_maps', transportDefaultValue)
			db = agn.DBaseID ()
			cursor = db.cursor ()
			if cursor is not None:
				for row in cursor.query ('SELECT mailloop_domain FROM company_tbl WHERE mailloop_domain IS NOT NULL AND status = :status', {'status': 'active'}):
					if row[0]:
						addRelayDomain (row[0].strip ().lower ())
				cursor.close ()
			db.close ()
			transportDomains = set ([_c[0] for _c in transports.content])
			for (domain, value) in relays.content[:]:
				if domain not in transportDomains:
					transports.content.append ((domain, transportDefaultValue))
					transports.modified = True
				self.mtdom[domain] += 1
			save (relays)
			save (transports)
			if relays.modified or transports.modified:
				cmd = agn.which ('smctrl')
				if cmd is not None:
					n = subprocess.call ([cmd, 'service', 'reload'])
					if n == 0:
						agn.log (agn.LV_INFO, 'data', 'Reloaded')
					else:
						agn.log (agn.LV_ERROR, 'data', 'Reloading failed: %d' % n)
			self.domains = [_c[0] for _c in relays.content]
		else:
			try:
				for line in fileReader (self.sendmailBase + '/mailertable'):
					parts = line.split ()
					if len (parts) > 0 and parts[0][0] != '.':
						self.domains.append (parts[0])
						self.mtdom[parts[0]] = 0
			except IOError, e:
				agn.log (agn.LV_ERROR, 'data', 'Unable to read mailertable %s' % `e.args`)
			try:
				for line in fileReader (self.sendmailBase + '/relay-domains'):
					if self.mtdom.has_key (line):
						self.mtdom[line] += 1
					else:
						agn.log (agn.LV_ERROR, 'data', 'We relay domain "%s" without catching it in mailertable' % line)
				for key in self.mtdom.keys ():
					if self.mtdom[key] == 0:
						agn.log (agn.LV_ERROR, 'data', 'We define domain "%s" in mailertable, but do not relay it' % key)
			except IOError, e:
				agn.log (agn.LV_ERROR, 'data', 'Unable to read relay-domains %s' % `e.args`)
	
	def removeUpdateLog (self):
		try:
			os.unlink (self.updateLog)
		except OSError, e:
			if e.args[0] != errno.ENOENT:
				agn.log (agn.LV_ERROR, 'data', 'Failed to remove old update log %s %s' % (self.updateLog, `e.args`))
		
	def done (self):
		self.removeUpdateLog ()

	def readMailFiles (self):
		rc = []
		try:
			lhost = socket.getfqdn ()
			if lhost:
				rc.append ('@%s\taccept:rid=local' % lhost)
		except Exception, e:
			agn.log (agn.LV_ERROR, 'data', 'Unable to find local FQDN %s' % `e.args`)
		if self.mta.mta == 'sendmail':
			try:
				for line in fileReader (self.sendmailBase + '/local-host-names'):
					rc.append ('@%s\taccept:rid=local' % line)
			except IOError, e:
				agn.log (agn.LV_ERROR, 'data', 'Unable to read local-host-names %s' % `e.args`)
			try:
				for line in fileReader (self.sendmailBase + '/virtusertable'):
					parts = line.split ()
					if len (parts) == 2:
						rc.append ('%s\taccept:rid=virt,fwd=%s' % (parts[0], parts[1]))
			except IOError, e:
				agn.log (agn.LV_ERROR, 'data', 'Unable to read virtusertable %s' % `e.args`)
		return rc

	def updateRules (self, rules):
		inuse = set ()
		globalRule = None
		for (rid, rule) in rules.items ():
			if globalRule is None:
				try:
					fd = open (self.ruleFile)
					globalRule = fd.read ()
					fd.close ()
					if not globalRule.endswith ('\n'):
						globalRule += '\n'
				except IOError as e:
					agn.log (agn.LV_ERROR, 'rule', 'Failed to open "%s" for reading: %s' % (self.ruleFile, str (e)))
			inuse.add (rid)
			fname = self.ruleFormat % rid
			try:
				fd = open (fname, 'w')
				if globalRule:
					fd.write (globalRule)
				for sect in sorted (rule):
					fd.write ('[%s]\n' % agn.toutf8 (sect))
					for line in rule[sect]:
						fd.write ('%s\n' % agn.toutf8 (line))
				fd.close ()
			except IOError, e:
				agn.log (agn.LV_ERROR, 'rule', 'Failed to open "%s" for writing: %r' % (fname, e.args))
		todel = []
		try:
			for fname in os.listdir (self.ruleDirectory):
				m = self.rulePattern.match (fname)
				if m is not None:
					rid = int (m.groups ()[0])
					if rid not in inuse:
						todel.append (fname)
			for fname in todel:
				path = agn.mkpath (self.ruleDirectory, fname)
				try:
					os.unlink (path)
				except OSError, e:
					agn.log (agn.LV_ERROR, 'rule', 'Failed to remove "%s": %r' % (fname, e.args))
		except OSError, e:
			agn.log (agn.LV_ERROR, 'rule', 'Failed to access ruleDirectory "%s": %r' % (self.ruleDirectory, e.args))

	def readDatabase (self, auto):
		rc = []
		db = agn.DBaseID ()
		if not db:
			agn.log (agn.LV_ERROR, 'data', 'Unable to create database connection')
			raise agn.error ('readDatabase.open')
		try:
			i = db.cursor ()
			if not i:
				agn.log (agn.LV_ERROR, 'data', 'Unable to get database cursor')
				raise agn.error ('readDatabase.cursor')
			try:
				fqdn = socket.getfqdn ().lower ()
				company_list = []
				newDomains = {}
				forwards = []
				seen = set ()
				acceptedForwards = set ()
				ctab = {}
				if self.domains:
					domain = self.domains[0]
				else:
					domain = self.fixdomain
				seen.add (domain)
				rc.append ('fbl@%s\taccept:rid=unsubscribe' % domain)
				query = 'SELECT company_id, mailloop_domain FROM company_tbl WHERE status = \'active\''
				missing = []
				for record in i.query (query):
					if record[1]:
						ctab[record[0]] = record[1]
						if record[1] not in seen:
							rc.append ('fbl@%s\talias:fbl@%s' % (record[1], domain))
							if record[1] not in self.mtdom and record[1].lower () != fqdn:
								newDomains[record[1]] = agn.mutable (rid = 0, domain = record[1])
							seen.add (record[1])
					else:
						missing.append (record[0])
					company_list.append (record[0])
				if missing:
					missing.sort ()
					agn.log (agn.LV_VERBOSE, 'data', 'Missing mailloop_domain for %s' % ', '.join ([str (m) for m in missing]))
				seen.clear ()
				#
				query = 'SELECT rid, shortname, company_id, filter_address, forward_enable, forward, ar_enable, ar_sender, ar_subject, ar_text, ar_html, subscribe_enable, mailinglist_id, form_id, timestamp, spam_email, spam_required, spam_forward, autoresponder_mailing_id, security_token FROM mailloop_tbl'
				apattern = re.compile ('alias(es)?="([^"]+)"')
				dpattern = re.compile ('domains?="([^"]+)"')
				for record in i.query (query):
					subscribe_enable = None
					mailinglist_id = None
					form_id = None
					(rid, shortname, company_id, filteraddr, forward_enable, forward, ar_enable, ar_sender, ar_subject, ar_text, ar_html, subscribe_enable, mailinglist_id, form_id, timestamp, spam_email, spam_required, spam_forward, autoresponder_mailing_id, security_token) = record
					if not company_id in company_list:
						continue
					if not rid is None:
						seen.add (rid)
						rid = str (rid)
						domains = None
						if timestamp is None:
							now = time.localtime ()
							timestamp = now[5] + now[4] * 100 + now[3] * 10000 + now[2] * 1000000 + now[1] * 100000000 + now[0] *  10000000000
						else:
							timestamp = timestamp.second + timestamp.minute * 100 + timestamp.hour * 10000 + timestamp.day * 1000000 + timestamp.month * 100000000 + timestamp.year * 10000000000
						aliases = None
						if not filteraddr is None:
							aliases = filteraddr.split ()
							if not aliases:
								aliases = None
						collect_domains = False
						if not shortname is None:
							mtch = apattern.search (shortname)
							if not mtch is None:
								tempaliases = (mtch.groups ()[1]).split ()
								if aliases is None:
									aliases = tempaliases
								else:
									aliases += tempaliases
							mtch = dpattern.search (shortname)
							if not mtch is None:
								domains = (mtch.groups ()[0]).split ()
								for domain in domains:
									if not self.mtdom.has_key (domain) and not domain in newDomains:
										newDomains[domain] = agn.mutable (rid = rid, domain = domain)
							else:
								collect_domains = True	
						if not aliases is None:
							for alias in aliases:
								if not alias.startswith (self.prefix):
									parts = alias.split ('@')
									if len (parts) == 2:
										if collect_domains:
											if domains is None:
												domains = [parts[1]]
											else:
												domains.append (parts[1])
										if not self.mtdom.has_key (parts[1]) and not parts[1] in newDomains:
											newDomains[parts[1]] = agn.mutable (rid = rid, domain = parts[1])
						if autoresponder_mailing_id:
							try:
								autoresponder_mailing_id = int (autoresponder_mailing_id)
								if not security_token:
									agn.log (agn.LV_ERROR, 'data', '%s: Autoresponder has mailing id, but no security token' % rid)
									autoresponder_mailing_id = None
							except ValueError, e:
								agn.log (agn.LV_ERROR, 'data', '%s: Failed to parse autoresponder_mailing_id %r: %s' % (rid, autoresponder_mailing_id, str (e)))
								autoresponder_mailing_id = None

						if ar_enable and not ar_text and not autoresponder_mailing_id:
							ar_enable = False
						if ar_enable:
							def nvl (s):
								if s is not None and type (s) not in (str, unicode):
									return str (s)
								return s
							auto.append (Autoresponder (rid, timestamp, ar_sender, ar_subject, nvl (ar_text), nvl (ar_html), autoresponder_mailing_id, security_token))
						if domains is None:
							try:
								cdomain = ctab[company_id]
								if self.domains and cdomain != self.domains[0]:
									domains = [self.domains[0]]
								else:
									domains = []
								if not self.domains or cdomain in self.domains:
									domains.append (cdomain)

								else:
									agn.log (agn.LV_ERROR, 'data', 'Companys domain "%s" not found in mailertable' % cdomain)
							except KeyError:
								agn.log (agn.LV_DEBUG, 'data', 'No domain for company found, further processing')
						if domains is None:
							domains = self.domains
						elif self.domains and self.domains[0] not in domains:
							domains.insert (0, self.domains[0])
						extra = 'rid=%s' % rid
						if company_id:
							extra += ',cid=%d' % company_id
						if forward_enable and forward:
							extra += ',fwd=%s' % forward
							forwards.append (agn.mutable (rid = rid, address = forward))
                                                if spam_email:
                                                        extra += ',spam_email=%s' % spam_email
                                                if spam_forward is not None:
                                                        extra += ',spam_fwd=%d' % spam_forward
                                                if spam_required is not None:
                                                        extra += ',spam_req=%d' % spam_required
						if ar_enable:
							extra += ',ar=%s' % rid
							if autoresponder_mailing_id:
								extra += ',armid=%d' % autoresponder_mailing_id
						if subscribe_enable and mailinglist_id and form_id:
							extra += ',sub=%d:%d' % (mailinglist_id, form_id)

						for domain in domains:
							line = '%s%s@%s\taccept:%s' % (self.prefix, rid, domain, extra)
							agn.log (agn.LV_VERBOSE, 'data', 'Add line: ' + line)
							rc.append (line)
						if aliases and domains:
							for alias in aliases:
								rc.append ('%s\talias:%s%s@%s' % (alias, self.prefix, rid, domains[0]))
								acceptedForwards.add (alias)
				if seen:
					rules = {}
					query = 'SELECT rid, section, pattern FROM mailloop_rule_tbl'
					for (rid, section, pattern) in i.query (query):
						if rid in seen:
							try:
								rule = rules[rid]
							except KeyError:
								rule = {}
								rules[rid] = rule
							try:
								sect = rule[section]
							except KeyError:
								sect = []
								rule[section] = sect
							sect.append (pattern)
					self.updateRules (rules)

				for forward in forwards:
					parts = forward.address.split ('@')
					if len (parts) == 2:
						fdomain = parts[1].lower ()
						for domain in self.mtdom:
							if domain == fdomain and forward not in acceptedForwards:
								agn.log (agn.LV_WARNING, 'db', '%s: using address "%s" with local handled domain "%s"' % (forward.rid, forward.address, domain))
						refuse = []
						for domain in newDomains:
							if domain == fdomain:
								nd = newDomains[domain]
								agn.log (agn.LV_WARNING, 'db', '%s: try to add new domain for already existing forward address "%s" in %s, refused' % (nd.rid, forward.address, forward.rid))
								refuse.append (domain)
						for domain in refuse:
							del newDomains[domain]

				if newDomains:
					if agn.sDNS is not None:
						try:
							dns = agn.sDNS ()
							laddrs = dns.getAllAddresses (fqdn)
							for domain in newDomains:
								checks = dns.getMailexchanger (domain)
								if not checks:
									checks = [domain]
								if checks:
									for check in checks:
										caddrs = dns.getAllAddresses (check)
										for caddr in caddrs:
											if caddr not in laddrs:
												agn.log (agn.LV_ERROR, 'db', '%s: checked address "%s" for "%s" does not point to local machines address %s' % (domain, caddr, check, ', '.join (laddrs)))
								else:
									agn.log (agn.LV_ERROR, 'db', '%s: failed to resolve' % domain)
						except Exception, e:
							agn.log (agn.LV_ERROR, 'db', 'Failed to verify new domains: %s' % str (e))
					if self.mta.mta == 'sendmail':
						chandler = signal.signal (signal.SIGUSR1, signal.SIG_IGN)
						cmd = [self.controlSendmail, 'add']
						for domain in newDomains:
							cmd.append (domain)
						agn.log (agn.LV_INFO, 'db', 'Found new domains, add them using ' + `cmd`)
						subprocess.call (cmd)
						agn.log (agn.LV_INFO, 'db', 'Restarting sendmail due to domain update')
						subprocess.call ([self.restartSendmail])
						signal.signal (signal.SIGUSR1, chandler)
					self.readMailertable (newDomains)
			finally:
				i.close ()
		finally:
			db.close ()
		return rc
	
	def readLocalFiles (self):
		rc = []
		try:
			rc += fileReader (self.localFilename)
		except IOError, e:
			agn.log (agn.LV_VERBOSE, 'local', 'Unable to read local file %s %s' % (self.localFilename, `e.args`))
		return rc
	
	def updateAutoresponder (self, auto):
		newlist = []
		for new in auto:
			found = None
			for old in self.autoresponder:
				if new.rid == old.rid:
					found = old
					break
			if not found or new.timestamp > found.timestamp:
				new.writeFile ()
				newlist.append (new)
			else:
				newlist.append (found)
		for old in self.autoresponder:
			found = False
			for new in newlist:
				if old.rid == new.rid:
					found = True
					break
			if not found:
				old.removeFile ()
		self.autoresponder = newlist
	
	def renameFile (self, oldFile, newFile):
		try:
			os.rename (oldFile, newFile)
		except OSError, e:
			agn.log (agn.LV_ERROR, 'data', 'Unable to rename %s to %s %s' % (oldFile, newFile, `e.args`))
			try:
				os.unlink (oldFile)
			except OSError, e:
				agn.log (agn.LV_WARNING, 'data', 'Failed to remove temp. file %s %s' % (oldFile, `e.args`))
			raise agn.error ('renameFile')

	def updateConfigfile (self, new):
		if new != self.last:
			temp = '%s.%d' % (self.configFilename, os.getpid ())
			try:
				fd = open (temp, 'w')
				fd.write (new)
				fd.close ()
				self.renameFile (temp, self.configFilename)
				self.last = new
			except IOError, e:
				agn.log (agn.LV_ERROR, 'data', 'Unable to write %s %s' % (temp, `e.args`))
				raise agn.error ('updateConfigfile.open')

	def writeUpdateLog (self, text):
		try:
			fd = open (self.updateLog, 'a')
			fd.write ('%d %s\n' % (self.updateCount, text))
			fd.close ()
			self.updateCount += 1
		except IOError, e:
			agn.log (agn.LV_ERROR, 'data', 'Unable to write update log %s %s' % (self.updateLog, `e.args`))

	def update (self, forced):
		try:
			auto = []
			new = self.readMailFiles ()
			new += self.readDatabase (auto)
			new += self.readLocalFiles ()
			self.updateAutoresponder (auto)
			self.updateConfigfile ('\n'.join (new) + '\n')
			updateText = 'success'
		except agn.error as e:
			agn.log (agn.LV_ERROR, 'data', 'Update failed: %s' % e)
			updateText = 'failed: %s' % e
		if forced:
			self.writeUpdateLog (updateText)
#
running = True
reread = False

def handler (sig, stack):
	global	running, reread
	
	if sig == signal.SIGUSR1:
		reread = True
	else:
		running = False

def main ():
	global	running, reread

	delay = 180
	(opts, param) = getopt.getopt (sys.argv[1:], 'vd:')
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
		elif opt[0] == '-d':
			delay = int (opt[1])

	signal.signal (signal.SIGINT, handler)
	signal.signal (signal.SIGTERM, handler)
	signal.signal (signal.SIGUSR1, handler)
	signal.signal (signal.SIGHUP, signal.SIG_IGN)
	signal.signal (signal.SIGPIPE, signal.SIG_IGN)
	agn.lock ()
	agn.log (agn.LV_INFO, 'main', 'Starting up')
	data = Data ()
	while running:
		forcedUpdate = reread
		reread = False
		data.update (forcedUpdate)
		n = delay
		while n > 0 and running and not reread:
			time.sleep (1)
			n -= 1
	data.done ()
	agn.log (agn.LV_INFO, 'main', 'Going down')
	agn.unlock ()

if __name__ == '__main__':
	main ()
