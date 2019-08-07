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
import	sys, os, getopt, time, shutil
import	agn, eagn
agn.loglevel = agn.LV_DEBUG
#
class Processor (object):
	def __init__ (self):

		self.incoming = agn.mkpath (agn.base, 'var', 'spool', 'DIRECT')
		self.archive = agn.mkpath (agn.base, 'var', 'spool', 'ARCHIVE')
		self.recover = agn.mkpath (agn.base, 'var', 'spool', 'RECOVER')
		self.queues = [agn.mkpath (agn.base, 'var', 'spool', 'QUEUE')]
		self.cur = agn.multiprocessing.Value ('i', 0)
		self.mta = agn.MTA ()
	
	def __nextQueue (self):
		cval = self.cur.value
		if cval < 0 or cval >= len (self.queues):
			cval = 0
		rc = self.queues[cval]
		cval += 1
		if cval == len (self.queues):
			cval = 0
		self.cur.value = cval
		return rc
	
	def done (self):
		pass

	def doit (self, basename):
		(src, stamp, final) = [agn.mkpath (self.incoming, '%s.%s' % (basename, _e)) for _e in 'xml.gz', 'stamp', 'final']
		if os.path.isfile (src):
			target = self.recover
			ok = True
			for path in stamp, final:
				if os.path.isfile (path):
					try:
						os.unlink (path)
					except OSError, e:
						agn.log (agn.LV_ERROR, 'doit', 'Failed to remove %s: %s' % (path, str (e)))
						ok = False
				else:
					agn.log (agn.LV_ERROR, 'doit', 'Failed to find file %s' % path)
					ok = False
			if ok:
				queue = self.__nextQueue ()
				if self.mta (src, targetDirectory = queue, flushCount = '2'):
					agn.log (agn.LV_INFO, 'doit', 'Unpacked %s in %s' % (src, queue))
					try:
						target = agn.mkArchiveDirectory (self.archive)
					except agn.error as e:
						agn.log (agn.LV_ERROR, 'unpack', 'Failed to setup archive directory %s: %s' % (self.archive, e))
						target = self.archive
				else:
					agn.log (agn.LV_ERROR, 'doit', 'Failed to unpack %s in %s' % (src, queue))
					target = self.recover
			else:
				agn.log (agn.LV_INFO, 'doit', 'Do not process %s as control file(s) is/are missing' % src)
			dst = agn.mkpath (target, os.path.basename (src))
			try:
				shutil.move (src, dst)
			except (shutil.Error, IOError, OSError), e:
				agn.log (agn.LV_ERROR, 'doit', 'Failed to move %s to %s: %s' % (src, dst, str (e)))
				try:
					os.unlink (src)
				except OSError, e:
					agn.log (agn.LV_ERROR, 'doit', 'Failed to remove file %s: %s' % (src, str (e)))
		else:
			agn.log (agn.LV_DEBUG, 'doit', 'Skip requested file %s which is already processed' % src)
			for path in stamp, final:
				if os.path.isfile (path):
					try:
						os.unlink (path)
					except OSError, e:
						agn.log (agn.LV_ERROR, 'doit', 'Failed to remove stale file %s: %s' % (path, str (e)))
	
	def process (self, basename):
		basename = os.path.basename (basename)
		agn.log (agn.LV_INFO, 'process', 'Requested %s to process' % basename)
		self.doit (basename)

	def scan (self):
		files = sorted (os.listdir (self.incoming))
		for fname in [_f for _f in files if _f.endswith ('.final')]:
			basename = fname.split ('.')[0]
			if '%s.xml.gz' % basename in files and '%s.stamp' % basename in files:
				agn.log (agn.LV_INFO, 'scan', 'Found %s to process' % fname)
				self.doit (basename)

class DirectPath (eagn.Worker):
	def __unpack (self, filename):
		if filename:
			self.enqueue (filename)
		return True

	def controllerSetup (self):
		defaults = [
			('xmlrpc.host', 'localhost'),
			('xmlrpc.port', 9400),
			('xmlrpc.allow_none', True)
		]
		for (var, value) in defaults:
			if var not in self.cfg:
				self.cfg[var] = value
		ctx = agn.mutable (last = 0)
		return ctx

	def controllerRegister (self, ctx, serv):
		serv.addMethod (self.__unpack, name = 'unpack')
	
	def controllerStep (self, ctx):
		now = time.time ()
		if ctx.last + 60 < now:
			self.enqueue (None, oob = True)
			ctx.last = now

	def executorConfig (self, what, default):
		if what == 'handle-multiple-requests':
			return True
		return eagn.Worker.executorConfig (self, what, default)

	def executorSetup (self):
		ctx = agn.mutable (
			process = Processor ()
		)
		return ctx

	def executorTeardown (self, ctx):
		ctx.process.done ()
		
	def executorRequestHandle (self, ctx, rqs):
		if len (rqs) > 1 or rqs[0] is None:
			ctx.process.scan ()
		else:
			ctx.process.process (rqs[0])

def main ():
	background = True
	(opts, param) = getopt.getopt (sys.argv[1:], 'f')
	for opt in opts:
		if opt[0] == '-f':
			background = False
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
	dpath = DirectPath ('direct-path')
	dpath.start (background)
#
if __name__ == '__main__':
	main ()
