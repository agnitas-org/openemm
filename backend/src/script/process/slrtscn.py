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
import	sys, os, getopt, signal
import	time, re, datetime, gdbm, collections
import	cPickle as pickle
import	agn, eagn, aps
agn.loglevel = agn.LV_VERBOSE
#
term = False
def handler (sig, stack):
	global	term
	term = True

class Plugin (aps.Plugin):
	pluginVersion = '1.0'

class SyslogParser (object):
	Info = collections.namedtuple ('Info', ['timestamp', 'server', 'service', 'content', 'items', 'keywords'])
	def __init__ (self):
		self.parser = self.guess
		self.tzcache = {}
	
	def __call__ (self, line):
		return self.parser (line)

	def guess (self, line):
		for parser in self.parse_rfc3164, self.parse_rfc5424:
			rc = parser (line)
			if rc is not None:
				self.parser = parser
				return rc
		return None
	
	pattern_rfc3164 = re.compile ('^([A-Z][a-z]{2}) +([0-9]+) ([0-9]{2}):([0-9]{2}):([0-9]{2}) +([^ ]+) +([^] ]+)\\[[0-9]+\\]: *(.*)$')
	def parse_rfc3164 (self, line):
		m = self.pattern_rfc3164.match (line)
		if m is not None:
			with agn.Ignore (ValueError, KeyError):
				(month_name, day, hour, minute, second, server, service, content) = m.groups ()
				month = {
					'Jan':  1, 'Feb':  2, 'Mar':  3, 'Apr':  4, 'May':  5, 'Jun':  6,
					'Jul':  7, 'Aug':  8, 'Sep':  9, 'Oct': 10, 'Nov': 11, 'Dec': 12
				}[month_name]
				now = datetime.datetime.now ()
				year = now.year
				if now.month < month:
					year -= 1
				return self.new_info (
					timestamp = datetime.datetime (year, month, int (day), int (hour), int (minute), int (second)),
					server = server,
					service = service,
					content = content
				)
		return None
			
	pattern_rfc5424 = re.compile ('^([0-9]{4})-([0-9]{2})-([0-9]{2})T([0-9]{2}):([0-9]{2}):([0-9]{2})(\\.[0-9]+)?(Z|[-+][0-9]{2}(:[0-9]{2})?)? +([^ ]+) +([^] ]+)\\[[0-9]+\\]: *(.*)$')
	def parse_rfc5424 (self, line):
		m = self.pattern_rfc5424.match (line)
		if m is not None:
			with agn.Ignore (ValueError):
				(year, month, day, hour, minute, second, fraction, timezone, _, server, service, content) = m.groups ()
				microseconds = int (float (fraction) * 1000 * 1000) if fraction else 0
				return self.new_info (
					timestamp = datetime.datetime (int (year), int (month), int (day), int (hour), int (minute), int (second), microseconds),
					server = server,
					service = service,
					content = content
				)
		return None

	def new_info (self, timestamp, server, service, content):
		rc = self.Info (
			timestamp = timestamp,
			server = server,
			service = service,
			content = content,
			items = {},
			keywords = set ()
		)
		for p in content.split (', '):
			try:
				(var, val) = [_p.strip () for _p in p.split ('=', 1)]
				vars = var.split ()
				if len (vars) > 1:
					var = vars.pop ()
					for kw in vars:
						rc.keywords.add (kw)
				rc.items[var] = val
			except ValueError:
				rc.keywords.add (p.strip ())
		return rc

class Tracker (object):
	key_created = '@created'
	key_updated = '@updated'
	key_log= '@log'
	def __init__ (self, filename):
		self.filename = filename
		self.db = None
		self.decode = pickle.loads
		self.encode = pickle.dumps
	
	def __iter__ (self):
		self.open ()
		dbkey = self.db.firstkey ()
		while dbkey is not None:
			with agn.Ignore (ValueError):
				(section, key) = dbkey.split (':', 1)
				yield (section, key, self.get (section, key))
			dbkey = self.db.nextkey (dbkey)

	def open (self):
		if self.db is None:
			self.db = gdbm.open (self.filename, 'c')
	
	def close (self):
		if self.db is not None:
			self.db.close ()
			self.db = None

	def key (self, section, key):
		self.open ()
		return '%s:%s' % (section, key)
	
	def get (self, section, key):
		self.open ()
		try:
			return self.decode (self.db[self.key (section, key)])
		except KeyError:
			return {}
	
	def put (self, section, key, value):
		self.open ()
		now = int (time.time ())
		old = self.get (section, key)
		value[self.key_created] = old.get (self.key_created, now) if old else now
		value[self.key_updated] = now
		self.db[self.key (section, key)] = self.encode (value)

	def line (self, section, key, line):
		record = self.get (section, key)
		try:
			record[self.key_log].append (line)
		except KeyError:
			record[self.key_log] = [line]
		self.put (section, key, record)
		
	def update (self, section, key, **kws):
		if kws:
			self.open ()
			cur = self.get (section, key)
			cur.update (kws)
			self.put (section, key, cur)

	def delete (self, section, key):
		self.open ()
		with agn.Ignore (KeyError):
			del self.db[self.key (section, key)]
	
	unit = eagn.Unit ()
	def expire (self, created = None, updated = None):
		created = self.unit.parse (created, created)
		updated = self.unit.parse (updated, updated)
		if created is not None or updated is not None:
			self.open ()
			max_count = 10000
			agn.log (agn.LV_INFO, 'expire', 'Expire old tracking entries')
			stats = collections.defaultdict (int)
			now = int (time.time ())
			while True:
				collect = {}
				dbkey = self.db.firstkey ()
				while dbkey is not None:
					with agn.Ignore (ValueError):
						(section, key) = dbkey.split (':', 1)
						value = self.get (section, key)
						if value:
							for (expiration, timestamp_key) in [
								(created, self.key_created),
								(updated, self.key_updated)
							]:
								if expiration is not None:
									try:
										if value[timestamp_key] + expiration < now:
											collect[(section, key)] = True
											stats[section] += 1
											break
									except KeyError:
										collect[(section, key)] = False
							if len (collect) >= max_count:
								break
					dbkey = self.db.nextkey (dbkey)
				if collect:
					agn.log (agn.LV_INFO, 'expire', 'Process %d tracking entries' % len (collect))
					for ((section, key), to_delete) in collect.iteritems ():
						if to_delete:
							self.delete (section, key)
						else:
							self.put (section, key, self.get (section, key))
				else:
					break
			agn.log (agn.LV_INFO, 'expire', 'Expiration finished (%s), reorganize database' % (agn.Stream (stats.iteritems ()).map (lambda kv: '%s: %d' % kv).join (', ') if stats else '-', ))
			self.db.reorganize ()
			agn.log (agn.LV_INFO, 'expire', 'Reorganization finished')
			self.close ()
#
#
#
class Scanner (object):
	SEC_MTAID = 'mta-id'
	def __init__ (self, maillog, saveFile, bounceLog, providerLog):
		self.maillog = maillog
		self.saveFile = saveFile
		self.bounceLog = bounceLog
		self.providerLog = providerLog
		self.plugin = Plugin ()
		self.mtrack = Tracker (self.trackerPath)
		self.last = 0

	def done (self):
		self.mtrack.close ()

	def writeBounce (self, dsn, licenceID, mailingID, customerID, timestamp, reason, relay, recipient):
		try:
			info = [
				'timestamp=%04d-%02d-%02d %02d:%02d:%02d' % (timestamp.year, timestamp.month, timestamp.day, timestamp.hour, timestamp.minute, timestamp.second),
				'stat=%s' % reason, 
				'relay=%s' % relay
			]
			self.plugin ().addBounceInfo (dsn, licenceID, mailingID, customerID, info)
			with open (self.bounceLog, 'a') as fd:
				fd.write ('%s;%d;%d;0;%d;%s\n' % (dsn, licenceID, mailingID, customerID, '\t'.join (info)))
		except IOError as e:
			agn.log (agn.LV_ERROR, 'scan', 'Unable to write %s: %s' % (self.bounceLog, str (e)))

	def line (self, qid, line):
		self.mtrack.line (self.SEC_MTAID, qid, line)

	def scan (self):
		global	term

		self.expireTracker ()
		try:
			fp = agn.Filepos (self.maillog, self.saveFile, checkpoint = 1000)
		except agn.error as e:
			agn.log (agn.LV_INFO, 'main', 'Unable to open %s: %s, try to gain access' % (self.maillog, e))
			n = agn.call ([agn.mkpath (agn.base, 'bin', 'smctrl'), 'logaccess'])
			if n != 0:
				agn.log (agn.LV_ERROR, 'main', 'Failed to gain access to %s (%d)' % (self.maillog, n))
			with agn.Ignore (OSError):
				st = os.stat (self.saveFile)
				if st.st_size == 0:
					agn.log (agn.LV_ERROR, 'main', 'Remove corrupt empty file %s' % self.saveFile)
					os.unlink (self.saveFile)
			return
		self.mtrack.open ()
		try:
			sp = SyslogParser ()
			for line in fp:
				try:
					info = sp (line)
					if info is not None:
						if not self.parse (info, line):
							agn.log (agn.LV_WARNING, 'scan', 'Unparsable line: %s (%r)' % (line, info))
					else:
						agn.log (agn.LV_WARNING, 'scan', 'Unparsable format: %s' % line)
				except Exception as e:
					agn.logexc (agn.LV_ERROR, 'scan', 'Failed to parse line: %s: %s' % (line, e))
				if term:
					break
		finally:
			fp.close ()
			self.mtrack.close ()
		self.processCompleted ()
	
	def expireTracker (self):
		now = datetime.datetime.now ()
		if self.last != now.day:
			self.mtrack.close ()
			eagn.Daemonic.call (self.mtrack.expire, '7d')
			self.last = now.day

	def processCompleted (self):
		max_count = 25000		# max # of records to delete per batch
		outdated = 24 * 60 * 60		# if record reached this value, the record is assumed to be done
		to_remove = []
		agn.log (agn.LV_DEBUG, 'proc', 'Start processing completed records')
		for (section, key, value) in self.mtrack:
			diff = int (time.time ()) - value[self.mtrack.key_created]
			if diff < 3600:
				diffstr = '%d:%02d' % (diff // 60, diff % 60)
			else:
				diffstr = '%d:%02d:%02d' % (diff // 3600, (diff // 60) % 60, diff % 60)
			#
			if section != self.SEC_MTAID:
				agn.log (agn.LV_DEBUG, 'proc', 'Ignore non %s record: %s:%s' % (self.SEC_MTAID, section, key))
				continue
			#
			if not value.get ('complete'):
				if diff < outdated:
					agn.log (agn.LV_DEBUG, 'proc/%s' % key, 'Ignore not (yet) completed record since %s' % diffstr)
					continue
				agn.log (agn.LV_INFO, 'proc/%s' % key, 'Found outdated incomplete record since %s' % diffstr)
				value['outdated'] = True
			#
			self.completed (key, value, to_remove)
			#
			to_remove.append ((section, key))
			if len (to_remove) >= max_count:
				agn.log (agn.LV_INFO, 'proc', 'Reached limit of %s, defer further processing' % agn.numfmt (max_count))
				break
		#
		if to_remove:
			agn.log (agn.LV_VERBOSE, 'proc', 'Remove %s processed keys' % agn.numfmt (len (to_remove)))
			for (section, key) in to_remove:
				self.mtrack.delete (section, key)
				agn.log (agn.LV_DEBUG, 'proc', 'Removed %s:%s' % (section, key))
			agn.log (agn.LV_VERBOSE, 'proc', 'Removed processed keys done')
				
	def completed (self, key, value, to_remove):
		pass
		
	def parse (self, info, line):
		raise agn.error ('Subclass must implement parse()')

class ScannerSendmail (Scanner):
	trackerPath = agn.mkpath (agn.base, 'var', 'run', 'scanner-sendmail.track')
	isstat = re.compile ('([0-9A-F]{6}[0-9A-Z]{3}[0-9A-F]{8})[G-Zg-z]?:.*stat=(.*)$')
	acceptable = re.compile ('|'.join (_s for _s in (
		'starting daemon',
		'[0-9]*[a-z][0-9A-Za-z]+: (from|to)=',
		'STARTTLS',
		'ruleset=tls_server,',
		'(runqueue|grew WorkList for)',
		'[0-9A-Fa-f]+T[0-9A-Fa-f]+:',
		'did not issue MAIL/EXPN/VRFY/ETRN during connection to MTA',
		'timeout waiting for input from [^ ]+ during client',
		'error connecting to filter',
		'Milter .*: to error state',
		'sender notify:',
	)))
	def __unparsable_as_expected (self, line):
		return self.acceptable.search (line) is not None
		
	def parse (self, info, line):
		if info.service not in ('sendmail', 'sm-msp-queue'):
			agn.log (agn.LV_DEBUG, 'parse', 'Skip non sendmail line: %s' % line)
			return True
		mtch = self.isstat.search (info.content)
		if mtch is None:
			if self.__unparsable_as_expected (info.content):
				return True
			return False
		#
		(qid, stat) = mtch.groups ()
		mailing = int (qid[:6], 16)
		licence = int (qid[6:9], 16)
		if len (qid) == 17:
			customer = int (qid[9:], 16)
		else:
			customer = int (qid[10:], 16)
		#
		if customer >= 0xf0000000:
			agn.log (agn.LV_VERBOSE, 'parse', 'Line leads to test customerID 0x%x: %s' % (customer, line))
			return True
		#
		self.line (qid, line)
		dsn = info.items.get ('dsn')
		record = self.mtrack.get (self.SEC_MTAID, qid)
		update = {
			'timestamp': info.timestamp
		}
		if dsn is not None and (dsn.startswith ('2') or dsn.startswith ('5')):
			update['complete'] = True
		if 'envelopeFrom' not in record:
			envelopeFrom = info.items.get ('ctladdr', '').strip ('<>')
			if envelopeFrom:
				update['envelopeFrom'] = envelopeFrom
		if 'to' in info.items and 'envelopeTo' not in record:
			update['envelopeTo'] = info.items['to'].strip ('<>')
		for key in 'to', 'dsn', 'status', 'relay':
			if key in info.items:
				update[key] = info.items[key]
		record.update (update)
		self.mtrack.put (self.SEC_MTAID, qid, record)
		if dsn:
			self.writeBounce (dsn, licence, mailing, customer, info.timestamp, stat, info.items.get ('relay', ''), info.items.get ('to', ''))
		else:
			agn.log (agn.LV_WARNING, 'parse', 'Line has no DSN: %s' % line)
		return True
#
class ScannerPostfix (Scanner):
	messageidLog = agn.mkpath (agn.base, 'log', 'messageid.log')
	trackerPath = agn.mkpath (agn.base, 'var', 'run', 'scanner-postfix.track')
	SEC_MESSAGEID = 'message-id'
	
	def __init__ (self, *args, **kws):
		super (ScannerPostfix, self).__init__ (*args, **kws)
		self.uid = agn.UID ()

	def processCompleted (self):
		self.__handleMessageIDs ()
		super (ScannerPostfix, self).processCompleted ()
		
	def __handleMessageIDs (self):
		if os.path.isfile (self.messageidLog):
			pfname = '%s.%d' % (self.messageidLog, int (time.time ()))
			nfname = pfname
			n = 0
			while os.path.isfile (nfname):
				n += 1
				nfname = '%s.%d' % (pfname, n)
			try:
				os.rename (self.messageidLog, nfname)
				time.sleep (2)
			except OSError, e:
				agn.log (agn.LV_ERROR, 'mid', 'Failed to rename %s to %s: %s' % (self.messageidLog, nfname, str (e)))
				return
			agn.log (agn.LV_DEBUG, 'mid', 'Scanning input file %s' % nfname)
			try:
				with open (nfname, 'r') as fdi, open (agn.logdataname ('messageid'), 'a') as fdo:
					for line in fdi:
						fdo.write (line)
						line = line.strip ()
						try:
							parts = line.split (';', 5)
							if len (parts) == 6:
								rec = {
									'licenceID': int (parts[0]),
									'companyID': int (parts[1]),
									'mailinglistID': int (parts[2]),
									'mailingID': int (parts[3]),
									'customerID': int (parts[4])
								}
								self.mtrack.put (self.SEC_MESSAGEID, parts[5], rec)
								agn.log (agn.LV_DEBUG, 'mid', 'Saved licenceID=%s, companyID=%s, mailinglistID=%s, mailingID=%s, customerID=%s for message-id %s' % (parts[0], parts[1], parts[2], parts[3], parts[4], parts[5]))
							else:
								raise ValueError ('expect 6 elements, got only %d' % len (parts))
						except ValueError, e:
							agn.log (agn.LV_ERROR, 'mid', 'Failed to parse %s: %s' % (line, str (e)))
			except IOError, e:
				agn.log (agn.LV_ERROR, 'mid', 'Failed to write messagid file: %s' % str (e))
			finally:
				os.unlink (nfname)
	
	def __writeBounce (self, id, record):
		try:
			messageID = record['messageID']
			if messageID:
				try:
					midinfo = self.mtrack.get (self.SEC_MESSAGEID, messageID)
					if midinfo:
						licenceID, mailingID, customerID = midinfo['licenceID'], midinfo['mailingID'], midinfo['customerID']
					else:
						self.uid.parseUID (messageID.split ('@')[0], None, doValidate = False)
						licenceID, mailingID, customerID = self.uid.licenceID, self.uid.mailingID, self.uid.customerID
						agn.log (agn.LV_DEBUG, 'proc/%s' % id, 'Extract recipient information from mailing-id')
					self.writeBounce (
						record['dsn'],
						licenceID,
						mailingID,
						customerID,
						record.get ('timestamp', datetime.datetime.now ()),
						record.get ('status', ''),
						record.get ('relay', ''),
						record.get ('to', '')
					)
				except agn.error as e:
					agn.log (agn.LV_DEBUG, 'proc/%s' % id, 'No mailing info found for message-id %s: %s' % (messageID, e))
			else:
				agn.log (agn.LV_DEBUG, 'proc/%s' % id, 'Discard record from mailer daemon')
		except KeyError as e:
			agn.log (agn.LV_DEBUG, 'proc/%s' % id, 'Ignore incomplete record: %s' % e)
	
	def completed (self, key, value, to_remove):
		if 'messageID' in value:
			messageID = value['messageID']
			if messageID:
				to_remove.append ((self.SEC_MESSAGEID, messageID))
		else:
			agn.log (agn.LV_WARNING, 'proc/%s' % key, 'Completed record without messageID found, remove')

	ignoreID = set (['statistics', 'NOQUEUE'])
	ignoreService = set (['postfix/smtpd', 'postfix/master'])
	patternLine = re.compile ('([^:]+): (.*)$', re.IGNORECASE)
	patternEnvelopeFrom = re.compile ('from=<([^>]*)>')
	patternMessageID = re.compile ('message-id=<([^>]*)>')
	def parse (self, info, line):
		match = self.patternLine.match (info.content)
		if match is None:
			if info.service == 'postfix/smtpd':
				if (
					info.content.startswith ('connect from ') or
					info.content.startswith ('disconnect from ') or
					info.content.startswith ('lost connection ') or
					info.content.startswith ('timeout after ') or
					info.content == 'initializing the server-side TLS engine' or
					info.content.startswith ('setting up TLS connection from ') or
					info.content.startswith ('SSL_accept:') or
					info.content == 'SSL3 alert read:fatal:certificate unknown'
				):
					return True
			elif info.service == 'postfix/smtp':
				if (
					info.content.startswith ('connect to ') or
					info.content.startswith ('SSL_connect error to ')
				):
					return True
			elif info.service == 'postfix/postfix-script':
				if info.content == 'starting the Postfix mail system':
					return True
			return False
		#
		(qid, data) = match.groups ()
		if qid in self.ignoreID or info.service in self.ignoreService:
			return True
		#
		self.line (qid, line)
		if info.service == 'postfix/pickup':
			mtch2 = self.patternEnvelopeFrom.search (data)
			if mtch2 is not None:
				envelopeFrom = mtch2.group (1)
				self.mtrack.update (self.SEC_MTAID, qid, envelopeFrom = envelopeFrom)
				agn.log (agn.LV_DEBUG, 'parse/%s' % qid, 'Found envelopeFrom=%s' % envelopeFrom)
		elif info.service == 'postfix/cleanup':
			mtch2 = self.patternMessageID.search (data)
			if mtch2 is not None:
				messageID = mtch2.group (1)
				self.mtrack.update (self.SEC_MTAID, qid, messageID = messageID)
				agn.log (agn.LV_DEBUG, 'parse/%s' % qid, 'Found messageID=%s' % messageID)
		elif info.service == 'postfix/qmgr' and data == 'removed':
			rec = self.mtrack.get (self.SEC_MTAID, qid)
			if rec is not None:
				self.mtrack.update (self.SEC_MTAID, qid, complete = True)
				agn.log (agn.LV_DEBUG, 'parse/%s' % qid, 'postfix processing completed')
		elif info.service in ('postfix/qmgr', 'postfix/smtp', 'postfix/error', 'postfix/local'):
			rec = self.mtrack.get (self.SEC_MTAID, qid)
			update = {
				'timestamp': info.timestamp
			}
			if 'from' in info.items:
				update['envelopeFrom'] = info.items['from']
			if 'to' in info.items and 'envelopeTo' not in rec:
				update['envelopeTo'] = info.items['to']
			for key in 'to', 'dsn', 'status', 'relay':
				if key in info.items:
					update[key] = info.items[key]
			if update:
				rec.update (update)
				self.mtrack.put (self.SEC_MTAID, qid, rec)
				agn.log (agn.LV_DEBUG, 'parse/%s' % qid, 'Update tracking entry: %s' % str (rec))
				if 'dsn' in update:
					self.__writeBounce (qid, rec)
		else:
			agn.log (agn.LV_INFO, 'parse/%s' % qid, 'Not used: %s' % line)
		#
		return True
#
#
#
def main ():
	global	term
	
	maillog = '/var/log/maillog'
	saveFile = agn.mkpath (agn.base, 'var', 'run', 'slrtscn.save')
	bounceLog = agn.mkpath (agn.base, 'log', 'extbounce.log')
	providerLog = agn.normalize_path (None)
	(opts, param) = getopt.getopt (sys.argv[1:], 'vm:s:b:p:')
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
		elif opt[0] == '-m':
			maillog = opt[1]
		elif opt[0] == '-s':
			saveFile = opt[1]
		elif opt[0] == '-b':
			bounceLog = opt[1]
		elif opt[0] == '-p':
			providerLog = opt[1]
	scanners = {
		None:		ScannerSendmail,
		'sendmail':	ScannerSendmail,
		'postfix':	ScannerPostfix
	}
	mta = agn.MTA ()
	scanner = scanners.get (mta.mta, scanners[None]) (maillog, saveFile, bounceLog, providerLog)
	#
	signal.signal (signal.SIGINT, handler)
	signal.signal (signal.SIGTERM, handler)
	signal.signal (signal.SIGHUP, signal.SIG_IGN)
	signal.signal (signal.SIGPIPE, signal.SIG_IGN)
	#
	agn.lock ()
	agn.log (agn.LV_INFO, 'main', 'Starting up')
	agn.log (agn.LV_INFO, 'main', 'Scanning for %s using %s' % (mta.mta, scanner.__class__.__name__))
	while not term:
		time.sleep (1)
		agn.mark (agn.LV_INFO, 'loop', 180)
		scanner.scan ()
	#
	scanner.done ()
	agn.log (agn.LV_INFO, 'main', 'Going down')
	agn.unlock ()
	
if __name__ == '__main__':
	main ()
