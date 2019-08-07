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
import	sys, os, getopt, time, re, datetime
import	agn, aps, eagn
agn.loglevel = agn.LV_INFO
#
class Plugin (aps.Plugin): #{{{
	pluginVersion = '1.0'
#}}}
class Janitor (eagn.Janitor): #{{{
	def __init__ (self, *args, **kws):
		super (Janitor, self).__init__ (*args, **kws)
		self.plugin = Plugin ()
	
	def prepare (self, doit):
		super (Janitor, self).prepare (doit)
		self.plugin ().prepare (self)
	
	def done (self):
		self.plugin ().done ()
		super (Janitor, self).done ()

	def cleanupMailspool (self, compressAfterDays, removeAfterDays):
		spath = agn.mkpath (agn.base, 'var', 'spool', 'mail', 'store')
		if os.path.isdir (spath):
			self.cleanupTimestampFiles (spath, [(None, compressAfterDays)], [(None, removeAfterDays)])
#}}}

class OpenEMM (Janitor): #{{{
	def __init__ (self):
		super (OpenEMM, self).__init__ ('openemm')
		
	def __compareLogDone (self, path, fname, st):
		return self.mktimestamp (fname) + 30 < self.today

	def __cleanupLogDone (self):
		for path in self.select (agn.mkpath (agn.base, 'log', 'done'), False, self.__compareLogDone)[0]:
			self.remove (path)

	def __selectXML (self, path, offset, all_extensions):
		dt = time.localtime (time.time () - offset * 24 * 60 * 60)
		ts = '%04d%02d%02d' % (dt.tm_year, dt.tm_mon, dt.tm_mday)
		def selector (path, fname, st):
			rc = False
			m = agn.METAFile (fname)
			if m.valid and (all_extensions or m.extension == 'xml'):
				chk = m.timestamp[:8]
				rc = cmp (chk, ts) < 0
			return rc
		return self.select (path, False, selector)[0]
		
	def __cleanupSpool (self):
		meta = agn.mkpath (agn.base, 'var', 'spool', 'META')
		deleted = agn.mkpath (agn.base, 'var', 'spool', 'DELETED')
		def showCount (cnt, s):
			self.log (agn.LV_INFO, '%d files found, %d successful, %d failed: %s' % (sum (cnt), cnt[0], cnt[1], s))
		#
		count = [0, 0]
		for path in self.__selectXML (meta, 30, True):
			if self.remove (path):
				count[0] += 1
			else:
				count[1] += 1
		showCount (count, 'removed from %s' % meta)
		#
		count = [0, 0]
		for path in self.__selectXML (deleted, 30, True):
			if self.remove (path):
				count[0] += 1
			else:
				count[1] += 1
		showCount (count, 'removed from %s' % deleted)
		#
		archive = agn.mkpath (agn.base, 'var', 'spool', 'ARCHIVE')
		if os.path.isdir (archive):
			self.cleanupTimestampDirectories (archive, 7, 30)
		
	__filePattern = re.compile ('^[a-z0-9]+-[a-z0-9]+$', re.IGNORECASE)
	def __cleanupFilter (self):
		fpath = agn.mkpath (agn.base, 'var', 'spool', 'filter')
		if os.path.isdir (fpath):
			yday = datetime.datetime.now ().fromordinal (self.today - 1)
			apath = agn.mkpath (fpath, '%04d%02d%02d' % (yday.year, yday.month, yday.day))
			if not os.path.isdir (apath):
				agn.createPath (apath)
				for fname in [_f for _f in os.listdir (fpath) if self.__filePattern.match (_f)]:
					path = agn.mkpath (fpath, fname)
					if os.path.isfile (path):
						self.move (path, agn.mkpath (apath, fname))
				self.compress ([agn.mkpath (apath, _f) for _f in os.listdir (apath)])
			self.cleanupTimestampDirectories (fpath, 30, 180)

	def __cleanupLogfiles (self):
		self.cleanupLogfiles ([(None, 2)], [(re.compile ('.*-account\\.log.*'), 180), (None, 90)])

	def execute (self):
		self.log (agn.LV_INFO, 'Cleanup log/done')
		self.__cleanupLogDone ()
		self.log (agn.LV_INFO, 'Cleanup spool')
		self.__cleanupSpool ()
		self.log (agn.LV_INFO, 'Cleanup filter')
		self.__cleanupFilter ()
		self.log (agn.LV_INFO, 'Cleanup Logfiles')
		self.__cleanupLogfiles ()
		self.log (agn.LV_INFO, 'Cleanup mailspool')
		self.cleanupMailspool (2, 4)
		self.log (agn.LV_INFO, 'Cleanup done')
#}}}
def main ():
	doit = True
	(opts, parm) = getopt.getopt (sys.argv[1:], 'vn')
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
		elif opt[0] == '-n':
			doit = False
	agn.lock ()

	OpenEMM ().run (doit)
	agn.unlock ()
#
if __name__ == '__main__':
	main ()
