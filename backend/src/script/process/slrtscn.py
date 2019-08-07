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

class Scanner (object):
	def __init__ (self, maillog, saveFile, bounceLog):
		self.maillog = maillog
		self.saveFile = saveFile
		self.bounceLog = bounceLog
		self.plugin = Plugin ()
		self.timestampParser = agn.ParseTimestamp ()

	def done (self):
		pass

	def writeBounce (self, dsn, licenceID, mailingID, customerID, timestamp, reason, relay):
		try:
			info = [
				'timestamp=%s' % self.timestampParser.dump (timestamp),
				'stat=%s' % reason, 
				'relay=%s' % relay
			]
			self.plugin ().addBounceInfo (dsn, licenceID, mailingID, customerID, info)
			with open (self.bounceLog, 'a') as fd:
				fd.write ('%s;%d;%d;0;%d;%s\n' % (dsn, licenceID, mailingID, customerID, '\t'.join (info)))
		except IOError as e:
			agn.log (agn.LV_ERROR, 'scan', 'Unable to write %s: %s' % (self.bounceLog, str (e)))

	def scan (self):
		global	term

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
		try:
			for line in fp:
				try:
					if not self.parse (line):
						agn.log (agn.LV_WARNING, 'scan', 'Unparsable line: %s' % line)
				except Exception as e:
					agn.logexc (agn.LV_ERROR, 'scan', 'Failed to parse line: %s: %s' % (line, e))
				if term:
					break
		finally:
			fp.close ()
	
	def parse (self, line):
		raise agn.error ('Subclass must implement parse()')

class ScannerSendmail (Scanner):
	isstat = re.compile ('sendmail\\[[0-9]+\\]: *([0-9A-F]{6}[0-9A-Z]{3}[0-9A-F]{8})[G-Zg-z]?:.*stat=(.*)$')
	parser = re.compile ('^([a-z]{3} +[0-9]+ [0-9]{2}:[0-9]{2}:[0-9]{2}) +([^ ]+) +sendmail\\[[0-9]+\\]: *[0-9A-F]{6}([0-9A-Z]{3})[0-9A-F]{8}[G-Z]?:(.*)$', re.IGNORECASE)
	def __parseline (self, pline):
		rc = {
			'__line': pline
		}
		pmtch = self.parser.match (pline)
		if not pmtch is None:
			g = pmtch.groups ()
			rc['__timestamp'] = g[0]
			rc['__mailer'] = g[1]
			rc['__licence'] = g[2]
			parms = g[3].split (',')
			for parm in parms:
				p = parm.split ('=', 1)
				if len (p) == 2:
					rc[p[0].strip ()] = p[1].strip ()
		return rc
	
	acceptable = re.compile ('|'.join (_s for _s in (
		'(sendmail|sm-msp-queue)\\[[0-9]+\\]: starting daemon',
		'sendmail\\[[0-9]+\\]: [0-9]*[a-z][0-9A-Za-z]+: (from|to)=',
		'sendmail\\[[0-9]+\\]: STARTTLS',
		'sendmail\\[[0-9]+\\]: ruleset=tls_server,',
		'sendmail\\[[0-9]+\\]: (runqueue|grew WorkList for)',
		'sendmail\\[[0-9]+\\]: [0-9A-Fa-f]+T[0-9A-Fa-f]+:',
		'did not issue MAIL/EXPN/VRFY/ETRN during connection to MTA',
		'timeout waiting for input from [^ ]+ during client',
	)))
	def __unparsable_as_expected (self, line):
		return self.acceptable.search (line) is not None
		
	def parse (self, line):
		mtch = self.isstat.search (line)
		if mtch is None:
			if self.__unparsable_as_expected (line):
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
		details = self.__parseline (line)
		get = lambda k: details[k] if k in details else ''
		timestamp = datetime.datetime.now ()
		if details.has_key ('__timestamp'):
			timestamp = self.timestampParser (details['__timestamp'], timestamp)
		if details.has_key ('dsn'):
			dsn = details['dsn']
			if len (dsn) > 0:
				self.writeBounce (dsn, licence, mailing, customer, timestamp, stat, get ('relay'))
			else:
				agn.log (agn.LV_WARNING, 'parse', 'Line has no valid DSN %r: %s' % (dsn, line))
		else:
			agn.log (agn.LV_WARNING, 'parse', 'Line has no DSN: %s' % line)
		return True
#
class Tracker (object):
	key_created = '@created'
	key_updated = '@updated'
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
	
class ScannerPostfix (Scanner):
	messageidLog = agn.mkpath (agn.base, 'log', 'messageid.log')
	messageidTracker = agn.mkpath (agn.base, 'var', 'run', 'messageid.track')
	SEC_MESSAGEID = 'message-id'
	SEC_POSTFIXID = 'postfix-id'
	
	def __init__ (self, *args, **kws):
		super (ScannerPostfix, self).__init__ (*args, **kws)
		self.mtrack = None
		self.last = 0

	def done (self):
		if self.mtrack is not None:
			self.mtrack.close ()
			self.mtrack = None
		super (ScannerPostfix, self).done ()
	
	def __expireTracker (self):
		now = datetime.datetime.now ()
		if self.last != now.day:
			self.mtrack.close ()
			eagn.Daemonic.call (self.mtrack.expire, '7d')
			self.last = now.day

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
	
	def __processCompleted (self):
		max_count = 25000		# max # of records to delete per batch
		to_remove = []
		agn.log (agn.LV_DEBUG, 'proc', 'Start processing completed records')
		for (section, key, value) in self.mtrack:
			diff = int (time.time ()) - value[self.mtrack.key_created]
			if diff < 3600:
				diffstr = '%d:%02d' % (diff // 60, diff % 60)
			else:
				diffstr = '%d:%02d:%02d' % (diff // 3600, (diff // 60) % 60, diff % 60)
			#
			if section != self.SEC_POSTFIXID:
				agn.log (agn.LV_DEBUG, 'proc', 'Ignore non %s record: %s:%s' % (self.SEC_POSTFIXID, section, key))
				continue
			#
			if not value.get ('complete'):
				agn.log (agn.LV_DEBUG, 'proc/%s' % key, 'Ignore not (yet) completed record since %s' % diffstr)
				continue
			#
			if 'messageID' in value:
				messageID = value['messageID']
				if messageID:
					midinfo = self.mtrack.get (self.SEC_MESSAGEID, messageID)
					if midinfo:
						try:
							self.writeBounce (
								value['dsn'],
								midinfo['licenceID'],
								midinfo['mailingID'],
								midinfo['customerID'],
								value.get ('timestamp', datetime.datetime.now ()),
								value.get ('status', ''),
								value.get ('relay', '')
							)
							to_remove.append ((self.SEC_MESSAGEID, messageID))
						except KeyError as e:
							agn.log (agn.LV_INFO, 'proc/%s' % key, 'Incomplete record found, missing: %s' % e)
					else:
						agn.log (agn.LV_VERBOSE, 'proc/%s' % key, 'No matching message-id (yet) available since %s, expire' % diffstr)
				else:
					agn.log (agn.LV_VERBOSE, 'proc/%s' % key, 'Discard record from mailer daemon')
			else:
				agn.log (agn.LV_WARNING, 'proc/%s' % key, 'Completed record without messageID found, remove')

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
				
	def scan (self):
		try:
			self.mtrack = Tracker (self.messageidTracker)
			self.__expireTracker ()
			super (ScannerPostfix, self).scan ()
			self.__handleMessageIDs ()
			self.__processCompleted ()
		finally:
			if self.mtrack is not None:
				self.mtrack.close ()
				self.mtrack = None

	ignoreID = set (['statistics', 'NOQUEUE'])
	ignorePart = set (['postfix/smtpd', 'postfix/master'])
	patternLine = re.compile ('^([a-z]{3} +[0-9]+ [0-9]+:[0-9]+:[0-9]+) +[^ ]+ +([a-z/]+)\\[[0-9]+\\]: +([^:]+): (.*)$', re.IGNORECASE)
	patternLineOther = re.compile ('^[a-z]{3} +[0-9]+ [0-9]+:[0-9]+:[0-9]+ +[^ ]+ +([a-z/]+)\\[[0-9]+\\]: +(.*)$', re.IGNORECASE)
	patternEnvelopeFrom = re.compile ('from=<([^>]*)>')
	patternMessageID = re.compile ('message-id=<([^>]*)>')
	def parse (self, line):
		mtch = self.patternLine.match (line)
		if mtch is None:
			mtch = self.patternLineOther.match (line)
			if mtch is not None:
				(part, data) = mtch.groups ()
				if part == 'postfix/smtpd':
					if (
						data.startswith ('connect from ') or
						data.startswith ('disconnect from ') or
						data.startswith ('lost connection ') or
						data.startswith ('timeout after ') or
						data == 'initializing the server-side TLS engine' or
						data.startswith ('setting up TLS connection from ') or
						data.startswith ('SSL_accept:')
					):
						return True
				elif part == 'postfix/smtp':
					if (
						data.startswith ('connect to ') or
						data.startswith ('SSL_connect error to ')
					):
						return True
				elif part == 'postfix/postfix-script':
					if data == 'starting the Postfix mail system':
						return True
			return False
		#
		(date, part, id, data) = mtch.groups ()
		if id in self.ignoreID or part in self.ignorePart:
			return True
		#
		if part == 'postfix/pickup':
			mtch2 = self.patternEnvelopeFrom.search (data)
			if mtch2 is not None:
				envelopeFrom = mtch2.group (1)
				self.mtrack.update (self.SEC_POSTFIXID, id, envelopeFrom = envelopeFrom)
				agn.log (agn.LV_DEBUG, 'parse/%s' % id, 'Found envelopeFrom=%s' % envelopeFrom)
		elif part == 'postfix/cleanup':
			mtch2 = self.patternMessageID.search (data)
			if mtch2 is not None:
				messageID = mtch2.group (1)
				self.mtrack.update (self.SEC_POSTFIXID, id, messageID = messageID)
				agn.log (agn.LV_DEBUG, 'parse/%s' % id, 'Found messageID=%s' % messageID)
		elif part == 'postfix/qmgr' and data == 'removed':
			rec = self.mtrack.get (self.SEC_POSTFIXID, id)
			if rec is not None:
				self.mtrack.update (self.SEC_POSTFIXID, id, complete = True)
				agn.log (agn.LV_DEBUG, 'parse/%s' % id, 'postfix processing completed')
		elif part in ('postfix/qmgr', 'postfix/smtp', 'postfix/error', 'postfix/local'):
			parsed = {}
			for p in data.split (', '):
				with agn.Ignore (ValueError):
					(var, val) = [_p.strip () for _p in p.split ('=', 1)]
					parsed[var] = val
			rec = self.mtrack.get (self.SEC_POSTFIXID, id)
			update = {}
			if date and 'timestamp' not in rec:
				timestamp = self.timestampParser (date)
				if timestamp is not None:
					rec['timestamp'] = timestamp
			if 'from' in parsed:
				update['envelopeFrom'] = parsed['from']
			if 'to' in parsed and 'envelopeTo' not in rec:
				update['envelopeTo'] = parsed['to']
			for key in 'dsn', 'status', 'relay':
				if key in parsed:
					update[key] = parsed[key]
			if update:
				rec.update (update)
				self.mtrack.put (self.SEC_POSTFIXID, id, rec)
				agn.log (agn.LV_DEBUG, 'parse/%s' % id, 'Update tracking entry: %s' % str (rec))
		else:
			agn.log (agn.LV_INFO, 'parse/%s' % id, 'Not used: %s' % line)
		#
		return True
#
def main ():
	global	term
	
	maillog = '/var/log/maillog'
	saveFile = agn.mkpath (agn.base, 'var', 'run', 'slrtscn.save')
	bounceLog = agn.mkpath (agn.base, 'log', 'extbounce.log')
	(opts, param) = getopt.getopt (sys.argv[1:], 'vm:s:b:')
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
	scanners = {
		None:		ScannerSendmail,
		'sendmail':	ScannerSendmail,
		'postfix':	ScannerPostfix
	}
	mta = agn.MTA ()
	scanner = scanners.get (mta.mta, scanners[None]) (maillog, saveFile, bounceLog)
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
