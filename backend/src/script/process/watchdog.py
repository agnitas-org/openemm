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
import	sys, os, getopt, shlex
import	agn, eagn
#
agn.loglevel = agn.LV_INFO
#
class EWatchdog (eagn.EWatchdog):
	def __init__ (self, cmd, output = False):
		super (EWatchdog, self).__init__ (cmd)
		self.prior = None
		if output:
			self.is_temp_output = (output == '-')
			if self.is_temp_output:
				self.output_path = agn.mkpath (agn.base, 'var', 'tmp', '%s-%06d.log' % (agn.logname, os.getpid ()))
				if os.path.isfile (self.output_path):
					os.unlink (self.output_path)
			else:
				self.output_path = output
		else:
			self.output_path = None
		self.limit = None
		
	def setPrior (self, cmd):
		self.prior = shlex.split (cmd)
	
	def setLimit (self, limit):
		self.limit = limit

	def joining (self, job, ec):
		if self.output_path is not None and self.is_temp_output and os.path.isfile (self.output_path):
			if ec.exitcode is None or ec.exitcode != self.EC_EXIT:
				with open (self.output_path, 'r') as fd:
					if self.limit:
						st = os.fstat (fd.fileno ())
						if st.st_size > self.limit * 1024:
							fd.seek (-self.limit * 1024, 2)
							truncated = True
						else:
							truncated = False
						lines = agn.Stream.of (fd).remain (self.limit + 1).list ()
						if len (lines) > self.limit:
							truncated = True
						if truncated:
							lines[0] = '[..]'
						output = '\n'.join (lines) + '\n'
					else:
						output = fd.read ()
				if output:
					agn.log (agn.LV_INFO, 'watchdog', 'Output of unexpected terminated process:\n%s' % output)
			os.unlink (self.output_path)

	def run (self):
		if self.prior:
			def doit ():
				self.execute (self.prior)
			pid = self.spawn (doit)
			self.join (pid)
		if self.output_path is not None:
			self.redirect (None, self.output_path)
		eagn.EWatchdog.run (self)

def main ():
	background = False
	restartDelay = '60'
	terminationDelay = '10'
	output = None
	prior = None
	limit = 100
	(opts, param) = getopt.getopt (sys.argv[1:], 'vi:br:t:o:p:l:')
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
		elif opt[0] == '-i':
			if opt[1] == '-':
				if param:
					agn.logname = '%s-wd' % (os.path.basename (param).split ('.', 1)[0], )
			else:
				agn.logname = opt[1]
		elif opt[0] == '-b':
			background = True
		elif opt[0] == '-r':
			restartDelay = opt[1]
		elif opt[0] == '-t':
			terminationDelay = opt[1]
		elif opt[0] == '-o':
			output = opt[1]
		elif opt[0] == '-p':
			prior = opt[1]
		elif opt[0] == '-l':
			limit = int (opt[1]) if opt[1] != '-' else None
	if not len (param):
		raise agn.error ('No command given to run under watchdog control')
	agn.lock ()
	agn.log (agn.LV_INFO, 'main', 'Starting up')
	wd = EWatchdog (param, output)
	if background and wd.pushToBackground ():
		return
	if prior:
		wd.setPrior (prior)
	wd.setLimit (limit)
	wd.mstart (wd.Job (agn.logname, wd.run, ()), restartDelay, terminationDelay)
	agn.log (agn.LV_INFO, 'main', 'Going down')
	agn.unlock ()
	
if __name__ == '__main__':
	main ()
