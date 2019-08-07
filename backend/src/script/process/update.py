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
import	sys, os, getopt, time, signal, re, errno
import	gdbm, datetime, collections
import	agn, eagn, aps
agn.loglevel = agn.LV_INFO
#
delay = 30
parallel = True
class Plugin (aps.Plugin): #{{{
	pluginVersion = '1.0'
#}}}
class Duplicate (object): #{{{
	def __init__ (self, name, expiration):
		self.name = name
		self.expiration = expiration
		self.path = agn.mkpath (agn.base, 'var', 'run', 'duplicate-%s' % name)
		agn.createPath (self.path)
		self.dbs = None
		legacy = '%s.sqlite' % self.path
		if os.path.isfile (legacy):
			os.unlink (legacy)
			agn.log (agn.LV_INFO, 'dup', 'legacy file %s removed' % legacy)
	
	def __contains__ (self, line):
		for db in self.dbs:
			if db.has_key (line):
				return True
		self.dbs[0][line] = str (int (time.time ()))
		return False
	
	def open (self):
		self.dbs = []
		now = datetime.datetime.now ()
		today = now.toordinal ()
		filenames = (agn.Stream.range (self.expiration + 1)
			.map (lambda n: now.fromordinal (today - n))
			.map (lambda d: '%04d%02d%02d.gdbm' % (d.year, d.month, d.day))
			.list ()
		)
		current = filenames[0]
		current_exists = False
		for filename in sorted (os.listdir (self.path), reverse = True):
			path = agn.mkpath (self.path, filename)
			if os.path.isfile (path):
				if filename in filenames:
					if filename == current:
						current_exists = True
						mode = 'w'
					else:
						mode = 'r'
					self.dbs.append (gdbm.open (path, mode))
				else:
					agn.log (agn.LV_INFO, 'dup', 'Removing outdated file %s' % path)
					os.unlink (path)
		if not current_exists:
			path = agn.mkpath (self.path, current)
			self.dbs.insert (0, gdbm.open (path, 'c'))
		return bool (self.dbs)
	
	def close (self, do_expire = True):
		agn.Stream (self.dbs).each (lambda d: d.close ())
		self.dbs = None
#}}}
class Log (object): #{{{
	def __init__ (self, logpath, name):
		self.logpath = logpath
		self.name = name
		self.target = agn.mkpath (self.logpath, self.name)
		agn.createPath (self.target)
		self.now = lambda: int (time.time ())
		self.pack = lambda ts, sq: '%d-%d' % (ts, sq)
		self.unpack = lambda s: (int (_v) for _v in s.split ('-'))
	
	valid = re.compile ('^[0-9]+-[0-9]+$')
	def __iter__ (self):
		return iter (agn.Stream (os.listdir (self.target))
			.regexp (self.valid)
			.sorted (key = lambda f: tuple (self.unpack (f)))
			.map (lambda f: agn.mkpath (self.target, f))
		)
	
	def __len__ (self):
		return sum (1 for _ in self)
	
	def add (self, path, max_lines = 10000):
		posfile = '%s.position' % path
		with open (path) as fd:
			if os.path.isfile (posfile):
				with open (posfile) as fdp:
					with agn.Ignore (ValueError):
						pos = int (fdp.read ())
						if pos > 0:
							fd.seek (pos)
							agn.log (agn.LV_INFO, 'log/%s' % self.name, '%s: resume processing at position %d' % (path, pos))
			def save_position ():
				with open (posfile, 'w') as fdp:
					fdp.write ('%d' % fd.tell ())
			try:
				now = None
				seq = None
				fdw = None
				cur = 0
				count = 0
				for line in fd:
					if fdw is None:
						ts = self.now ()
						if ts != now:
							now = ts
							seq = self.__next_sequence (now)
						while fdw is None:
							output = agn.mkpath (self.target, self.pack (now, seq))
							seq += 1
							try:
								fno = os.open (output, os.O_CREAT |os.O_EXCL, 0666)
								try:
									fdw = open (output, 'w')
								finally:
									os.close (fno)
							except OSError as e:
								if e.errno != errno.EEXIST:
									raise
						cur = 0
						count += 1
					fdw.write (line)
					cur += 1
					if cur >= max_lines:
						fdw.close ()
						fdw = None
						save_position ()
				agn.log (agn.LV_INFO, 'log/%s' % self.name, 'Wrote %s to %s chunk%s' % (path, agn.numfmt (count), '' if count == 1 else 's'))
			finally:
				save_position ()
		try:
			os.unlink (path)
			if os.path.isfile (posfile):
				os.unlink (posfile)
		except OSError as e:
			agn.log (agn.LV_ERROR, 'log/%s' % self.name, 'Failed to remove proccessed file: %s' % e)
	
	def __next_sequence (self, timestamp):
		seq = 0
		for filename in os.listdir (self.target):
			with agn.Ignore (ValueError):
				(ts, sq) = self.unpack (filename)
				if ts == timestamp:
					seq = max (seq, sq + 1)
		return seq
#}}}
class Update (object): #{{{
	def __init__ (self, path, name):
		self.path = path
		self.name = name
		self.base = os.path.basename (self.path)
		if self.path.find (os.sep) != -1:
			directory = os.path.dirname (self.path)
		else:
			directory = None
		n = self.base.rfind ('.')
		if n != -1:
			b = self.base[:n] + '.fail'
		else:
			b = self.base + '.fail'
		if directory is None:
			self.fail = b
		else:
			self.fail = directory + os.sep + b
		self.cur = 1
		self.pid = os.getpid ()
		self.lineno = None
		self.log = Log (directory if directory is not None else '.', name)
		self.plugin = Plugin (manager = aps.LoggingManager)
		self.duplicate = Duplicate (name = self.name, expiration = 7)
		self.debug_enabled = False

	def done (self):
		pass

	def exists (self):
		return os.access (self.path, os.F_OK) or self.log

	def renameToTemp (self):
		tfname = self.path + '.%d.%d.%d' % (self.pid, time.time (), self.cur)
		self.cur += 1
		try:
			os.rename (self.path, tfname)
			agn.log (agn.LV_INFO, 'update', 'Renamed %s to %s' % (self.path, tfname))
			time.sleep (2)
		except OSError, e:
			agn.log (agn.LV_ERROR, 'update', 'Unable to rename %s to %s: %s' % (self.path, tfname, `e.args`))
			tfname = None
		return tfname

	def __save (self, fname, line):
		rc = False
		try:
			with open (fname, 'a') as fd:
				fd.write (line + '\n')
			rc = True
		except IOError, e:
			agn.log (agn.LV_ERROR, 'update', 'Failed to write to %s: %s' % (fname, `e.args`))
		return rc

	def saveToFail (self, line):
		return self.__save (self.fail, line)

	def __logfilename (self):
		now = time.localtime (time.time ())
		return agn.mkpath (agn.logpath, '%04d%02d%02d-%s' % (now[0], now[1], now[2], self.base))

	def saveToLog (self, line):
		return self.__save (self.__logfilename (), line)

	def updateStart (self, inst):
		raise agn.error ('Need to overwrite updateStart in your subclass')
	def updateEnd (self, inst):
		raise agn.error ('Need to overwrite updateEnd in your subclass')
	def updateLine (self, inst, line):
		raise agn.error ('Need to overwrite updateLine in your subclass')

	def update (self, inst, is_active):
		if os.path.isfile (self.path):
			tfname = self.renameToTemp ()
			if tfname is not None:
				try:
					count = agn.cstreamopen (tfname).count ()
					agn.log (agn.LV_INFO, 'update', '%s with %s entries to process' % (tfname, agn.numfmt (count)))
					self.log.add (tfname, 10000)
					agn.log (agn.LV_INFO, 'update', 'Added %s to log' % tfname)
				except IOError as e:
					agn.log (agn.LV_ERROR, 'update', 'Unable to open %s: %s' % (tfname, e))
		count = 0
		for path in self.log:
			with agn.Ignore (IOError):
				count += agn.cstreamopen (path).count ()
		agn.log (agn.LV_INFO, 'update', 'Files with %s entries to process' % agn.numfmt (count))
		#
		self.plugin ().start (self, inst)
		rc = self.updateStart (inst)
		if rc:
			self.duplicate.open ()
			self.lineno = 0
			for path in self.log:
				if os.path.isfile (path):
					do_remove = False
					new_path = '%s-%.3f' % (path, time.time ())
					try:
						os.rename (path, new_path)
						with open (new_path) as fd:
							do_remove = True
							for line in (_l.strip () for _l in fd):
								self.lineno += 1
								if line in self.duplicate:
									agn.log (agn.LV_DEBUG, 'update', 'Ignore duplicate line: %s' % line)
								else:
									if not self.updateLine (inst, line):
										if not self.saveToFail (line):
											do_remove = False
										rc = False
									else:
										if not self.saveToLog (line):
											do_remove = False
						if do_remove:
							os.unlink (new_path)
					except (OSError, IOError) as e:
						agn.log (agn.LV_ERROR, 'update', 'Failed to process %s as %s: %s' % (path, new_path, e))
					agn.log (agn.LV_INFO, 'update', '%s: Now at line %s of %s' % (self.name, agn.numfmt (self.lineno), agn.numfmt (count)))
					inst.sync ()
				else:
					agn.log (agn.LV_WARNING, 'update', '%s: File %s vanished' % (self.name, path))
				if not rc or not is_active ():
					break
			self.duplicate.close ()
		if not self.updateEnd (inst):
			rc = False
		self.plugin ().end (self, inst, rc)
		return rc
#}}}
class Updater(agn.Family.Child): #{{{
	def __init__(self, name):
		agn.Family.Child.__init__(self, name)
		self._inst = None

	def start(self):
		agn.log (agn.LV_INFO, self._name, 'Starting up')
		while self.active ():
			if self._updater.exists():
				db = agn.DBaseID()
				if not db:
					agn.log(agn.LV_ERROR, 'child %s' % self._name,
							'Unable to connect to database')
				else:
					cursor = db.cursor()
					if not cursor:
						agn.log(agn.LV_ERROR, 'child %s' % self._name,
							'Unable to get database base cursor')
					else:
						agn.log(agn.LV_INFO, 'child %s' % self._name,
								'Update for %s started' % self._updater.name)
						rv = self._updater.update(cursor, lambda: self.active ())
						if not rv:
							agn.log(agn.LV_INFO, 'loop',
									'Update for %s failed' % self._updater.name)
						cursor.close()
					db.close()
			delayCount = delay
			while delayCount > 0 and self.active ():
				delayCount -= 1
				time.sleep (1)
		self._updater.done()
		agn.log (agn.LV_INFO, self._name, 'Going down')
#}}}
class UpdateDetail: #{{{
	Ignore = 0
	Internal = 100
	Success = 200
	SoftbounceOther = 400
	SoftbounceReceiver = 410
	SoftbounceMailbox = 420
	HardbounceOther = 510
	HardbounceReceiver = 511
	HardbounceSystem = 512
	Softbounces = (SoftbounceOther, SoftbounceReceiver, SoftbounceMailbox)
	Hardbounces = (HardbounceOther, HardbounceReceiver, HardbounceSystem)
	names = {
		Ignore:			'Ignore',
		Internal:		'Internal',
		Success:		'Success',
		SoftbounceOther:	'SB-Other',
		SoftbounceReceiver: 	'SB-Recv',
		SoftbounceMailbox:	'SB-MBox',
		HardbounceOther:	'HB-Other',
		HardbounceReceiver: 	'HB-Recv',
		HardbounceSystem:	'HB-Sys'
	}
#}}}
class UpdateInfo: #{{{
	def __init__ (self, info):
		self.info = info
		self.map = {}
		for elem in info.split ('\t'):
			parts = elem.split ('=', 1)
			if len (parts) == 2:
				self.map[parts[0]] = parts[1]
			elif len (parts) == 1:
				self.map['stat'] = elem

	def __str__ (self):
		return agn.Stream (self.map.iteritems ()).map (lambda kv: '%s="%s"' % kv).join (', ')
		
	def __contains__ (self, var):
		return var in self.map
		
	def __getitem__ (self, var):
		if self.map.has_key (var):
			return self.map[var]
		return None

	def get (self, var, dflt = None):
		if self.map.has_key (var):
			return self.map[var]
		return dflt
#}}}
class UpdateBounce (Update): #{{{
	bouncelog = agn.mkpath (agn.base, 'log', 'extbounce.log')
	trackfile = agn.mkpath (agn.base, 'var', 'run', 'extbounce.track')
	trackage = 3 * 24 * 60 * 60

	class Translate (object): #{{{
		default = {
			UpdateDetail.Ignore:			[4, 40, 500, 41, 42, 43, 44, 45, 46, 47, 5, 515, 52, 53, 54, 55, 56, 57],
			UpdateDetail.Success:			[2],
			UpdateDetail.SoftbounceOther:		[50, 532, 541, 544, 570, 574],
			UpdateDetail.SoftbounceReceiver:	[411, 412, 413, 414, 416, 417, 418, 510, 514, 571, 51],
			UpdateDetail.SoftbounceMailbox:		[421, 422, 520, 521, 522, 523, 524],
			UpdateDetail.HardbounceOther:		[531],
			UpdateDetail.HardbounceReceiver:	[511, 513, 516, 517, 572],
 			UpdateDetail.HardbounceSystem:		[512, 518]
		}
		def __init__ (self, debug):
			self.debug = debug
			self.default_tab = {}
			for (key, value) in self.default.items ():
				for dsn in value:
					self.default_tab[dsn] = [(key, None)]
			self.tab = collections.defaultdict (dict)

		class Pattern (object):
			controlPattern = re.compile ('^/(.*)/([a-z]*)$')
			def __init__ (self, data, debug):
				try:
					self.data = agn.Parameter (data).data
				except Exception as e:
					agn.log (agn.LV_WARNING, 'trans', 'Failed to parse input data %r: %s' % (data, e))
					self.data = {}
				self.debug = debug
				self.checks = {}
				for (key, value) in self.data.items ():
					try:
						if key == 'stat':
							pattern = value
							flags = re.IGNORECASE
						elif key == 'relay':
							if len (value) > 2 and value.startswith ('/') and value.endswith ('/'):
								pattern = value[1:-1]
							else:
								relay = value
								if relay.endswith ('.'):
									post = '$'
								else:
									post = '.*'
								if relay.startswith ('.'):
									relay = relay[1:]
									pre = '(.+\\.)?'
								else:
									pre = '^'
								pattern = pre + re.escape (relay) + post
							flags = re.IGNORECASE
						else:
							flags = 0
							mtch = self.controlPattern.match (value)
							if mtch is not None:
								(pattern, opts) = mtch.groups ()
								for opt in opts:
									if opt == 'i':
										flags |= re.IGNORECASE
							else:
								pattern = value
						if self.debug:
							print ('\tTranslate: %s="%s" -> "%s"%s' % (key, value, pattern, ' (ignorecase)' if flags & re.IGNORECASE else ''))
						self.checks[key] = re.compile (pattern, flags)
					except re.error as e:
						agn.log (agn.WARNING, 'trans', 'Failed to parse regex in %s="%s": %s' % (key, value, e))

			def valid (self):
				return len (self.checks) > 0

			def match (self, infos):
				for (key, pattern) in [(_k.lower (), _v) for (_k, _v) in self.checks.items ()]:
					if key not in infos:
						return False
					if pattern.search (infos[key]) is None:
						return False
				return True

		def add (self, companyID, dsn, detail, pattern = None):
			tab = self.tab[companyID]
			if pattern is not None:
				if self.debug:
					print ('Pattern for companyID=%r for DSN=%r leading to Detail=%r using pattern %r' % (companyID, dsn, detail, pattern))
				pattern = self.Pattern (pattern, self.debug)
				if not pattern.valid ():
					agn.log (agn.LV_ERROR, 'translate', 'Invalid pattern "%s" for company %d found' % (pattern, companyID))
					return
			if dsn in tab:
				if pattern is not None:
					tab[dsn].insert (0, (detail, pattern))
				else:
					n = 0
					ltab = tab[dsn]
					while n < len (ltab):
						if ltab[n][1] is None:
							ltab[n] = (detail, pattern)
							break
						n += 1
					if n == len (ltab):
						ltab.append ((detail, pattern))
			else:
				tab[dsn] = [(detail, pattern)]

		def setup (self, cursor):
			for (companyID, dsn, detail, pattern) in cursor.query ('SELECT company_id, dsn, detail, pattern FROM bounce_translate_tbl'):
				self.add (companyID, dsn, detail, pattern)

		def trans (self, company, dsn, info):
			for tab in [self.tab[_k] for _k in (company, 0) if _k in self.tab] + [self.default_tab]:
				nr = dsn
				while nr > 0:
					if nr in tab:
						for (detail, pattern) in tab[nr]:
							if pattern is None or pattern.match (info):
								return detail
					nr //= 10
			return UpdateDetail.Ignore

		def dump (self, company, info):
			for n in range (0, 100):
				bnc = []
				for base in 200, 400, 500:
					bnc.append (self.trans (company, base + n, info))
				print ('x.%d.%d | %s' % (n // 10, n % 10, ' | '.join (['%-10.10s' % UpdateDetail.names[_b] for _b in bnc])))
	#}}}

	def __init__ (self, path = bouncelog):
		Update.__init__ (self, path, 'bounce')
		self.ustatus = agn.UserStatus ()
		self.mailingMap = {}
		self.dsnparse = re.compile ('^([0-9])\\.([0-9])\\.([0-9])$')
		self.igcount = None
		self.sucount = None
		self.sbcount = None
		self.hbcount = None
		self.blcount = None
		self.rvcount = None
		self.ccount = None
		self.companyInfo = {}
		self.companyInfoTS = 0
		self.translate = None
		self.store = None
		self.cache = agn.Cache (limit = 65536)
		self.expire = 0
		self.succeeded = None
		self.timestampParser = agn.ParseTimestamp ()

	def done (self):
		if not self.store is None:
			self.store.close ()
			self.store = None
		Update.done (self)

	userUnknown = re.compile ('user unknown|unknown user', re.IGNORECASE)
	def __todetail (self, dsn, info, company):
		(timestamp, ignore, detail, code, typ, remark, mailloop, infos) = (datetime.datetime.now (), False, UpdateDetail.Ignore, 0, agn.UserStatus.BOUNCE, None, None, None)
		match = self.dsnparse.match (dsn)
		if not match is None:
			grp = match.groups ()
			code = int (grp[0]) * 100 + int (grp[1]) * 10 + int (grp[2])
			infos = UpdateInfo (info)
			timestamp = self.timestampParser (infos['timestamp'], timestamp)
			mailloop = infos['mailloop']
			if mailloop is not None:
				if self.userUnknown.search (mailloop) is not None:
					code = 511
			if code % 100 == 99:
				if code // 100 == 5:
					detail = UpdateDetail.HardbounceOther
				elif code // 100 == 4:
					detail = UpdateDetail.SoftbounceOther
				else:
					detail = UpdateDetail.Internal
				admin = infos['admin']
				if admin is not None:
					typ = agn.UserStatus.ADMOUT
					remark = admin
				status = infos['status']
				if status is not None:
					ttyp = self.ustatus.findStatus (status)
					if ttyp is not None:
						typ = ttyp
			else:
				detail = self.translate.trans (company, code, infos)
			if detail == UpdateDetail.Ignore:
				ignore = True
		if remark is None:
			remark = 'bounce:%d' % detail
		return (timestamp, ignore, detail, code, typ, remark, mailloop, infos)

	def __mapMailingToCompany (self, inst, mailing):
		if not self.mailingMap.has_key (mailing):
			rec = inst.querys ('SELECT company_id FROM mailing_tbl WHERE mailing_id = :mailingID', {'mailingID': mailing})
			if rec is None:
				agn.log (agn.LV_ERROR, 'updBounce', 'No company_id for mailing %d found' % mailing)
				return 0
			else:
				self.mailingMap[mailing] = rec[0]
				return rec[0]
		else:
			return self.mailingMap[mailing]

	def __logSuccess (self, inst, company, now):
		if self.companyInfoTS + 300 < now:
			info = {}
			for r in inst.query ('SELECT company_id, mailtracking FROM company_tbl'):
				info[r[0]] = r[1] == 1
			self.companyInfo = info
			self.companyInfoTS = now
		try:
			rc = self.companyInfo[company]
		except KeyError:
			try:
				rc = self.companyInfo[0]
			except KeyError:
				rc = False
			self.companyInfo[company] = rc
		return rc

	def __trackExpire (self):
		if not self.store is None:
			agn.log (agn.LV_INFO, 'track', 'Start expiration')
			timestamp = int (time.time ()) - self.trackage
			toremove = []
			key = self.store.firstkey ()
			while not key is None:
				(ts, detail) = self.store[key].split (':')
				if int (ts) < timestamp:
					toremove.append (key)
				key = self.store.nextkey (key)
			for key in toremove:
				del self.store[key]
			agn.log (agn.LV_INFO, 'track', 'Expired %d entries' % len (toremove))
		self.cache.reset ()

	def __trackStore (self, now, mailing, customer, detail):
		if not self.store is None:
			key = '%d/%d' % (mailing, customer)
			try:
				try:
					(ts, det) = self.cache[key].split (':')
				except KeyError:
					(ts, det) = self.store[key].split (':')
				if int (det) not in UpdateDetail.Hardbounces and detail in UpdateDetail.Hardbounces:
					storeIt = True
				else:
					storeIt = False
			except KeyError:
				storeIt = True
			val = '%d:%d' % (now, detail)
			self.store[key] = val
			self.cache[key] = val
			return storeIt
		return True

	def updateStart (self, inst):
		self.igcount = 0
		self.sucount = 0
		self.sbcount = 0
		self.hbcount = 0
		self.blcount = 0
		self.rvcount = 0
		self.ccount = 0
		if self.store is None:
			self.store = gdbm.open (self.trackfile, 'c')
		if not self.store is None:
			now = time.localtime ()
			if self.expire != now.tm_mday:
				self.expire = now.tm_mday
				self.__trackExpire ()
		self.succeeded = {}
		self.translate = self.Translate (self.debug_enabled)
		self.translate.setup (inst)
		self.plugin ().startBounce (inst, UpdateDetail)
		return True

	def updateEnd (self, inst):
		if self.succeeded:
			agn.log (agn.LV_INFO, 'sent', 'Add %s mails to %d mailings' % (agn.numfmt (sum (self.succeeded.values ())), len (self.succeeded)))
			for mailingID in sorted (self.succeeded):
				inst.update ('UPDATE mailing_tbl SET delivered = delivered + :success WHERE mailing_id = :mailingID', {'success': self.succeeded[mailingID], 'mailingID': mailingID})
			inst.sync ()
		if not self.store is None:
			self.store.close ()
			self.store = None
		agn.log (agn.LV_INFO, 'udpBounce', 'Found %d hardbounces, %d softbounces (%d written), %d successes, %d blacklisted, %d revoked, %d ignored in %d lines' % (self.hbcount, self.sbcount, (self.sbcount - self.ccount), self.sucount, self.blcount, self.rvcount, self.igcount, self.lineno))
		return True

	def updateLine (self, inst, line):
		parts = line.split (';', 5)
		if len (parts) != 6:
			agn.log (agn.LV_WARNING, 'updBounce', 'Got invalid line: ' + line)
			return False
		try:
			(dsn, licence, mailing, media, customer, info) = (parts[0], int (parts[1]), int (parts[2]), int (parts[3]), int (parts[4]), parts[5])
		except ValueError:
			agn.log (agn.LV_WARNING, 'updBounce', 'Unable to parse line: ' + line)
			return False
		if licence != 0 and licence != agn.licence:
			agn.log (agn.LV_DEBUG, 'updBounce', 'Ignore bounce for other licenceID %d' % licence)
			return True
		if customer == 0:
			agn.log (agn.LV_DEBUG, 'updBounce', 'Ignore virtual recipient')
			return True
		if mailing <= 0 or customer < 0:
			agn.log (agn.LV_WARNING, 'updBounce', 'Got line with invalid mailing or customer: ' + line)
			return False
		company = self.__mapMailingToCompany (inst, mailing)
		if company <= 0:
			agn.log (agn.LV_WARNING, 'updBounce', 'Cannot map mailing %d to company for line: %s' % (mailing, line))
			return False
		(timestamp, ignore, detail, dsnnr, bouncetype, bounceremark, mailloopremark, infos) = self.__todetail (dsn, info, company)
		if ignore or detail == UpdateDetail.Ignore:
			agn.log (agn.LV_DEBUG, 'updBounce', 'Ignoring line: %s' % line)
			return True
		if detail < 0:
			agn.log (agn.LV_WARNING, 'updBounce', 'Got line with invalid detail (%d): %s' % (detail, line))
			return False
		now = int (time.time ())
		if detail == UpdateDetail.Success:
			try:
				self.succeeded[mailing] += 1
			except KeyError:
				self.succeeded[mailing] = 1
			logging = self.__logSuccess (inst, company, now)
		else:
			logging = True
		rc = True
		if logging:
			if detail == UpdateDetail.Success:
				data = {
					'customer': customer,
					'mailing': mailing,
					'ts': timestamp
				}
				try:
					query = ('INSERT INTO success_%d_tbl (customer_id, mailing_id, timestamp) '
						 'VALUES (:customer, :mailing, :ts)' % company)
					inst.update (query, data)
					self.plugin ().success (inst, now, company, mailing, customer, timestamp, infos)
				except agn.error as e:
					agn.log (agn.LV_ERROR, 'updBounce', 'Unable to add success for company %d: %s' % (company, e))
					rc = False
				#
				self.sucount += 1
				if self.sucount % 1000 == 0:
					inst.sync ()
			elif bouncetype == agn.UserStatus.BOUNCE:
				data = { 'company': company,
					 'customer': customer,
					 'detail': detail,
					 'mailing': mailing,
					 'dsn': dsnnr,
					 'ts': timestamp
				}
				try:
					store = self.__trackStore (now, mailing, customer, detail)
					if store:
						query = inst.qselect (
							oracle = 'INSERT INTO bounce_tbl (bounce_id, company_id, customer_id, detail, mailing_id, dsn, timestamp) VALUES (bounce_tbl_seq.nextval, :company, :customer, :detail, :mailing, :dsn, :ts)',
							mysql = 'INSERT INTO bounce_tbl (company_id, customer_id, detail, mailing_id, dsn, timestamp) VALUES (:company, :customer, :detail, :mailing, :dsn, :ts)'
						)
						inst.update (query, data)
					elif detail not in UpdateDetail.Hardbounces:
						self.ccount += 1
					self.plugin ().bounce (inst, store, now, company, mailing, customer, timestamp, dsnnr, detail, infos)
				except agn.error as e:
					agn.log (agn.LV_ERROR, 'updBounce', 'Unable to add bounce %r to database: %s' % (data, e))
					rc = False
			if detail in UpdateDetail.Hardbounces or (detail == UpdateDetail.Internal and bouncetype is not None and bounceremark is not None):
				if bouncetype == agn.UserStatus.BLACKLIST:
					self.blcount += 1
				else:
					self.hbcount += 1
				query = None
				data = None
				try:
					if mailloopremark is not None:
						query = 'DELETE FROM success_%d_tbl WHERE customer_id = :customerID AND mailing_id = :mailingID' % company
						data = {
							'customerID': customer,
							'mailingID': mailing
						}
						if inst.update (query, data, commit = True) == 1:
							self.rvcount += 1
					query = ('UPDATE customer_%d_binding_tbl '
						 'SET user_status = :status, timestamp = :ts, user_remark = :remark, exit_mailing_id = :mailing '
						 'WHERE customer_id = :customer AND user_status = 1 AND mediatype = :media'
						 % company)
					data = { 'status': bouncetype,
						 'remark': bounceremark,
						 'mailing': mailing,
						 'ts': timestamp,
						 'customer': customer,
						 'media': media
					}
					inst.update (query, data, commit = True)
				except agn.error as e:
					agn.log (agn.LV_ERROR, 'updBounce', 'Unable to unsubscribe %s for company %d from database using %s: %s' % (`data`, company, query, e))
					rc = False
			elif detail in UpdateDetail.Softbounces:
				self.sbcount += 1
				if self.sbcount % 1000 == 0:
					inst.sync ()
		else:
			self.igcount += 1
		return rc
#}}}
class UpdateAccount (Update): #{{{
	accountlog = agn.mkpath (agn.base, 'log', 'account.log')
	statusTemplate = agn.mkpath (agn.base, 'scripts', 'mailstatus.tmpl')
	statusTrack = agn.mkpath (agn.base, 'var', 'run', 'mailstatus.track')

	def __init__ (self, path = accountlog):
		Update.__init__ (self, path, 'account')
		self.tscheck = re.compile ('^([0-9]{4})-([0-9]{2})-([0-9]{2}):([0-9]{2}):([0-9]{2}):([0-9]{2})$')
		self.ignored = None
		self.inserted = None
		self.bccs = None
		self.failed = None
		self.status = {}
		self.changed = None

	def __mailingStatus (self, inst, statusID, count, ts):
		try:
			minfo = self.status[statusID]
			if not minfo.active:
				agn.log (agn.LV_DEBUG, 'udpAccount/minfo', 'Found inactive mailing %d for new accounting information' % minfo.mailingID)
			elif minfo.mailingID is None:
				agn.log (agn.LV_DEBUG, 'updAccount/minfo', 'Found unset mailingID for new account information with status %d' % statusID)
			else:
				agn.log (agn.LV_DEBUG, 'updAccount/minfo', 'Found active record with mailingID %d' % minfo.mailingID)
			if minfo.sendStart > ts:
				minfo.sendStart = ts
			if minfo.sendLast < ts:
				minfo.sendLast = ts
			minfo.sendCount += count
		except KeyError:
			minfo = agn.mutable ()
			minfo.active = True
			minfo.statusID = statusID
			minfo.mailingID = None
			minfo.inProduction = True
			minfo.createdMails = 0
			minfo.sendStart = ts
			minfo.sendLast = ts
			minfo.sendCount = count
			minfo.mailReceiver = None
			minfo.mailSent = False
			minfo.mailPercent = 50
			inst.sync ()
			for r in inst.query ('SELECT mailing_id FROM maildrop_status_tbl WHERE status_id = :status', { 'status': statusID }):
				minfo.mailingID = r[0]
				break
			if not minfo.mailingID is None:
				for r in inst.queryc ('SELECT statmail_recp, deleted FROM mailing_tbl WHERE mailing_id = :mid', {'mid': minfo.mailingID}):
					if r[0] is not None:
						receiver = r[0].strip ()
						try:
							(percent, nreceiver) = receiver.split ('%', 1)
							minfo.mailReceiver = nreceiver.strip ()
							minfo.mailPercent = int (percent)
							if minfo.mailPercent < 0 or minfo.mailPercent > 100:
								raise ValueError ('percent value out of range')
						except ValueError:
							minfo.mailReceiver = receiver
					if r[1]:
						agn.log (agn.LV_INFO, 'updAccount/minfo', 'Mailing with ID %d is marked as deleted, set to inactive' % minfo.mailingID)
						minfo.active = False
				inst.update ('UPDATE mailing_tbl SET work_status = \'mailing.status.sending\' WHERE mailing_id = :mid AND (work_status IS NULL OR work_status != \'mailing.status.sending\')', { 'mid': minfo.mailingID }, commit = True)
				agn.log (agn.LV_DEBUG, 'updAccount/minfo', 'Created new record for statusID %d with mailingID %d' % (minfo.statusID, minfo.mailingID))
			else:
				agn.log (agn.LV_DEBUG, 'updAccount/minfo', 'Created new record for statusID %d with no assigned mailingID' % statusID)
			self.status[statusID] = minfo
		if minfo.active and not minfo.mailingID is None:
			self.changed.add (minfo)

	def __mailingSendStatus (self, inst, minfo):
		try:
			fd = open (self.statusTemplate)
			template = fd.read ()
			fd.close ()
		except IOError, e:
			agn.log (agn.LV_WARNING, 'updAccount/minfo', 'Failed to read template %s: %r' % (self.statusTemplate, e.args))
			template = None
		if template:
			sender = None
			if minfo.sendLast == minfo.sendStart:
				last = int (time.time ())
			else:
				last = minfo.sendLast
			diff = ((last - minfo.sendStart) * minfo.createdMails) / minfo.sendCount
			end = minfo.sendStart + diff
			start = time.localtime (minfo.sendStart)
			then = time.localtime (end)
			rc = inst.querys ('SELECT shortname, company_id FROM mailing_tbl WHERE mailing_id = :mid', {'mid': minfo.mailingID})
			if not rc is None and not None in rc:
				mailingName = rc[0]
				companyID = rc[1]
				try:
					mailingName = (unicode (mailingName, 'UTF-8') if type (mailingName) is str else mailingName).encode ('ISO-8859-1')
				except (UnicodeDecodeError, UnicodeEncodeError), e:
					agn.log (agn.LV_INFO, 'updAccount/minfo', 'Failed to recode "%s" from UTF-8 to ISO-8859-1' % mailingName)
					if type (mailingName) is unicode:
						mailingName = mailingName.encode ('UTF-8')
			else:
				mailingName = ''
				companyID = 0
			receiver = [_r for _r in minfo.mailReceiver.split () if _r]
			ns = {
				'sender': sender,
				'receiver': ', '.join (receiver),
				'current': minfo.sendCount,
				'count': minfo.createdMails,
				'start': datetime.datetime (start[0], start[1], start[2], start[3], start[4], start[5]),
				'end': datetime.datetime (then[0], then[1], then[2], then[3], then[4], then[5]),
				'mailingID': minfo.mailingID,
				'mailingName': mailingName,
				'companyID': companyID,

				'format': agn.numfmt
			}
			tmpl = agn.Template (template)
			try:
				email = tmpl.fill (ns)
			except agn.error as e:
				agn.log (agn.LV_WARNING, 'updAccount/minfo', 'Failed to fill template %s: %s' % (self.statusTemplate, e))
				email = None
			if email:
				mail = eagn.EMail ()
				if sender:
					mail.setSender (sender)
				isTo = True
				for r in receiver:
					if isTo:
						mail.addTo (r)
						isTo = False
					else:
						mail.addCc (r)
				charset = tmpl.property ('charset')
				if charset:
					mail.setCharset (charset)
				subject = tmpl.property ('subject')
				if not subject:
					subject = tmpl['subject']
					if not subject:
						subject = 'Status report for mailing %d [%s]' % (minfo.mailingID, mailingName)
				else:
					tmpl = agn.Template (subject)
					subject = tmpl.fill (ns)
				mail.setSubject (subject)
				mail.setText (email)
				st = mail.sendMail ()
				if not st[0]:
					agn.log (agn.LV_WARNING, 'updAccount/minfo', 'Failed to send status mail to %s: [%s/%s]' % (minfo.mailReceiver, st[2].strip (), st[3].strip ()))
				else:
					agn.log (agn.LV_INFO, 'updAccount/minfo', 'Status mail for %s (%d) sent to %s' % (mailingName, minfo.mailingID, minfo.mailReceiver))

	def __mailingReached (self, minfo):
		if minfo.mailPercent == 100:
			return minfo.createdMails <= minfo.sendCount
		return int (float (minfo.createdMails * minfo.mailPercent) / 100.0) <= minfo.sendCount

	def __mailingSummary (self, inst):
		for minfo in self.status.values ():
			if minfo.inProduction and minfo.active:
				self.changed.add (minfo)
		if self.changed:
			inst.sync ()
			track = None
			for minfo in self.changed:
				if minfo.inProduction:
					for r in inst.queryc ('SELECT genstatus FROM maildrop_status_tbl WHERE status_id = :status', { 'status': minfo.statusID }):
						if r[0] == 3:
							for r in inst.query ('SELECT total_mails FROM mailing_backend_log_tbl WHERE status_id = :status', { 'status': minfo.statusID }):
								minfo.inProduction = False
								minfo.createdMails = r[0]
								agn.log (agn.LV_DEBUG, 'updAccount/minfo', 'Changed status for mailingID %d from production to finished with %d mails created' % (minfo.mailingID, minfo.createdMails))
								break
						break
					if minfo.inProduction:
						for r in inst.queryc ('SELECT deleted FROM mailing_tbl WHERE mailing_id = :mailingID', {'mailingID': minfo.mailingID}):
							if r[0]:
								agn.log (agn.LV_INFO, 'updAccount/minfo', 'Mailing with ID %d had been deleted, mark as inactive' % minfo.mailingID)
								minfo.active = False
				if not minfo.inProduction:
					if minfo.mailReceiver and not minfo.mailSent:
						if self.__mailingReached (minfo):
							if track is None:
								track = gdbm.open (self.statusTrack, 'c')
							mid = '%d' % minfo.mailingID
							if not track is None and not track.has_key (mid):
								self.__mailingSendStatus (inst, minfo)
								track[mid] = '%d:sent to %s' % (int (time.time ()), minfo.mailReceiver)
							minfo.mailSent = True
					for r in inst.query ('SELECT sum(no_of_mailings) FROM mailing_account_tbl WHERE mailing_id = :mid AND status_field = \'W\'', { 'mid': minfo.mailingID }):
						if r[0] == minfo.createdMails:
							if inst.update ('UPDATE mailing_tbl SET work_status = \'mailing.status.sent\' WHERE mailing_id = :mid', { 'mid': minfo.mailingID }, commit = True) == 1:
								agn.log (agn.LV_INFO, 'updAccount/minfo', 'Changed work status for mailingID %d to sent' % minfo.mailingID)
							else:
								agn.log (agn.LV_ERROR, 'updAccount/minfo', 'Failed to change work status for mailingID %d to sent: %s' % (minfo.mailingID, inst.lastError ()))
							minfo.active = False
							agn.log (agn.LV_DEBUG, 'updAccount/minfo', 'Changed status for mailingID %d from active to inactive' % minfo.mailingID)
						else:
							agn.log (agn.LV_DEBUG, 'updAccount/minfo', 'Mailing %d has currently %d out of %d sent mails' % (minfo.mailingID, r[0], minfo.createdMails))
						break
			if not track is None:
				track.close ()

	def updateStart (self, inst):
		self.ignored = 0
		self.inserted = 0
		self.bccs = 0
		self.failed = 0
		self.changed = set ()
		return True

	def updateEnd (self, inst):
		agn.log (agn.LV_INFO, 'updAccount', 'Insert %d (%d bccs), failed %d, ignored %d records in %d lines' % (self.inserted, self.bccs, self.failed, self.ignored, self.lineno))
		self.__mailingSummary (inst)
		self.changed = None
		return True

	def updateLine (self, inst, line):
		sql = 'INSERT INTO mailing_account_tbl ('
		values = 'VALUES ('
		sep = ''
		if inst.db.dbms == 'oracle':
			sql += 'mailing_account_id'
			values += 'mailing_account_tbl_seq.nextval'
			sep = ', '
		timestamp = datetime.datetime.now ()
		data = {}
		ignore = False
		record = {}
		bcc = agn.mutable (count = 0, bytes = 0)
		for tok in line.split ():
			tup = tok.split ('=', 1)
			if len (tup) == 2:
				name = None
				(var, val) = tup
				record[var] = val
				if var == 'licence':
					name = 'licence_id'
					try:
						licence = int (val)
						if licence != 0 and licence != agn.licence:
							ignore = True
					except ValueError:
						ignore = True
					name = None
				elif var == 'company':
					name = 'company_id'
				elif var == 'mailinglist':
					name = 'mailinglist_id'
				elif var == 'mailing':
					name = 'mailing_id'
				elif var == 'maildrop':
					name = 'maildrop_id'
				elif var == 'status_field':
					name = 'status_field'
				elif var == 'mediatype':
					name = 'mediatype'
				elif var in ('mailtype', 'subtype'):
					name = 'mailtype'
				elif var == 'count':
					name = 'no_of_mailings'
				elif var == 'bytes':
					name = 'no_of_bytes'
				elif var == 'block':
					name = 'blocknr'
				elif var == 'mailer':
					name = 'mailer'
				elif var == 'timestamp':
					m = self.tscheck.match (val)
					if m is not None:
						ts = [int (_g) for _g in m.groups ()]
						timestamp = datetime.datetime (ts[0], ts[1], ts[2], ts[3], ts[4], ts[5])
				elif var == 'bcc-count':
					bcc.count = agn.atoi (val)
				elif var == 'bcc-bytes':
					bcc.bytes = agn.atoi (val)
				if not name is None:
					sql += '%s%s' % (sep, name)
					values += '%s:%s' % (sep, name)
					sep = ', '
					data[name] = val
		sql += '%stimestamp) %s%s:timestamp)' % (sep, values, sep)
		data['timestamp'] = timestamp
		
		rc = True
		if not ignore:
			try:
				inst.update (sql, data, commit = True)
				self.inserted += 1
				try:
					if record['status_field'] == 'W':
						status_id = int (record['maildrop'])
						count = int (record['count'])
						try:
							timestamp = record['timestamp']
							if len (timestamp) != 19:
								now = time.time ()
							else:
								now = time.mktime ((int (timestamp[0:4]), int (timestamp[5:7]), int (timestamp[8:10]), int (timestamp[11:13]), int (timestamp[14:16]), int (timestamp[17:19]), 0, 0, -1))
						except (KeyError, ValueError):
							now = time.time ()
						self.__mailingStatus (inst, status_id, count, int (now))
				except (KeyError, ValueError), e:
					agn.log (agn.LV_WARNING, 'updAccount', 'Failed to track %s: %r' % (line, e.args))
				if bcc.count > 0:
					stmt = inst.qselect (
						oracle = 'INSERT INTO bcc_mailing_account_tbl (mailing_account_id, no_of_mailings, no_of_bytes) VALUES (mailing_account_tbl_seq.currval, :bccCount, :bccBytes)',
						mysql = 'INSERT INTO bcc_mailing_account_tbl (mailing_account_id, no_of_mailings, no_of_bytes) VALUES (last_insert_id(), :bccCount, :bccBytes)'
					)
					inst.update (
						stmt,
						{
							'bccCount': bcc.count,
							'bccBytes': bcc.bytes
						},
						commit = True
					)
					self.bccs += 1
			except agn.error as e:
				agn.log (agn.LV_ERROR, 'updAccount', 'Failed to insert %s into database: %s' % (line, e))
				rc = False
				self.failed += 1
		else:
			self.ignored += 1
		return rc
#}}}
class UpdateData (Update): #{{{
	datalog = agn.mkpath (agn.base, 'log', 'data.log')
	def __init__ (self, path = datalog):
		Update.__init__ (self, path, 'data')
		self.parser = agn.Parameter ()
		self.rc = lambda a: len (a) == len (filter (None, a))
		
	def updateStart (self, inst):
		return self.rc (self.plugin ().startData (inst))

	def updateEnd (self, inst):
		return self.rc (self.plugin ().endData (inst))
	
	def updateLine (self, inst, line):
		try:
			self.parser.loads (line, method = 'line')
		except Exception as e:
			agn.log (agn.LV_ERROR, 'updData', '%s: failed to parse: %s' % (line, str (e)))
			return False
		return self.rc (self.plugin ().lineData (inst, line, self.parser))
#}}}
#
term = False
def handler (sig, stack):
	global	term
	term = True
#
def main ():
	global	term, parallel

	modules = {
		'bounce':	UpdateBounce,
		'account':	UpdateAccount,
		'data':		UpdateData
	}
	signal.signal (signal.SIGINT, handler)
	signal.signal (signal.SIGTERM, handler)
	signal.signal (signal.SIGHUP, signal.SIG_IGN)
	signal.signal (signal.SIGPIPE, signal.SIG_IGN)
	#
	(opts, param) = getopt.getopt (sys.argv[1:], 'vsdD')
	verbose = False
	single = False
	data = True
	debug = False
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
			verbose = True
		elif opt[0] == '-s':
			single = True
			parallel = False
		elif opt[0] == '-d':
			data = False
		elif opt[0] == '-D':
			debug = True
	#
	if debug:
		sys.exit (0)
	#
	if data and 'data' not in param:
		param.append ('data')
	updates = []
	for u in param:
		try:
			updates.append (modules[u] ())
		except KeyError:
			agn.log (agn.LV_ERROR, 'main', 'Invalid update: %s' % u)
	if len (updates) == 0:
		agn.die (agn.LV_ERROR, 'main', 'No update procedure found')
	agn.lock ()
	agn.log (agn.LV_INFO, 'main', 'Starting up')
	if parallel:
		fmly = agn.Family()
		for update in updates:
			# see Updater.start() to check logic of forking
			updater = Updater(update.name)
			updater._updater = update
			fmly.add(updater)
		fmly.start()
		fmly.wait()
	else:
		while not term:
			db = None
			agn.mark(agn.LV_INFO, 'loop', 180)
			for upd in updates:
				if not term and upd.exists():
					if db is None:
						db = agn.DBaseID()
						if db is None:
							agn.log(agn.LV_ERROR, 'loop', 'Unable to connect to database')
					if db:
						if verbose:
							db.log = lambda a: sys.stdout.write('%s\n' % a)
						instance = db.cursor()
						if instance:
							if not upd.update(instance, lambda: not term):
								agn.log(agn.LV_ERROR, 'loop', 'Update for %s failed' % upd.name)
							instance.close()
						else:
							agn.log(agn.LV_ERROR, 'loop', 'Unable to get database cursor')
			if db:
				db.close()
			if single:
				term = True
				continue
			#
			# Zzzzz....
			countDelay = delay
			while countDelay > 0 and not term:
				time.sleep(1)
				countDelay -= 1
		for upd in updates:
			upd.done()
	#
	agn.log (agn.LV_INFO, 'main', 'Going down')
	agn.unlock ()
#
if __name__ == '__main__':
	main ()
