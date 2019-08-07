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
import	sys, getopt, threading, socket
import	agn, eagn, xagn
agn.loglevel = agn.LV_DEBUG
#
def numParse (s):
	if s is not None:
		try:
			rc = int (s)
			if rc <= 0:
				rc = None
		except ValueError:
			rc = None
	else:
		rc = None
	return rc

class DBAccess (eagn.DB):
	lock = threading.Lock ()
	def __init__ (self):
		eagn.DB.__init__ (self)
		self.lock.acquire ()
	
	def done (self):
		self.lock.release ()
		eagn.DB.done (self)

class Trigger (object):
	def __init__ (self, cfg):
		self.execlock = threading.Lock ()
		self.cfg = cfg
		self.merger = xagn.XMLRPCClient (self.cfg, 'merger')
		self.timeout = self.cfg.iget ('merger.timeout', 5)

	def fire (self, statusID):
		rc = -1
		report = ''
		if self.execlock.acquire ():
			otimeout = socket.getdefaulttimeout ()
			socket.setdefaulttimeout (self.timeout)
			try:
				report = self.merger.Merger.remote_control ('fire', str (statusID))
				agn.log (agn.LV_DEBUG, 'fire', 'statusID %d fired: %r' % (statusID, report))
				rc = 0
			except Exception, e:
				agn.log (agn.LV_WARNING, 'fire', 'Failed to trigger statusID %d: %s' % (statusID, str (e)))
				report = 'Failed to start merger: %s' % str (e)
			finally:
				socket.setdefaulttimeout (otimeout)
				self.execlock.release ()
		else:
			agn.log (agn.LV_ERROR, 'fire', 'Failed to get exclusive lock to trigger statusID %d' % statusID)
			report += 'Failed to get exclusive access\n'
		return (rc, report)
	
	def demand (self, mailingID):
		rc = -1
		report = ''
		statusID = None
		dba = DBAccess ()
		try:
			if dba.isopen ():
				genStatus = None
				for r in dba.cursor.queryc ('SELECT status_id, genstatus FROM maildrop_status_tbl WHERE mailing_id = :mid AND status_field = \'D\'', {'mid': mailingID}):
					if r[0] and (statusID is None or statusID < r[0]):
						if statusID is None or r[1] in (1, 3):
							statusID = r[0]
							genStatus = r[1]
				if statusID is None:
					agn.log (agn.LV_WARNING, 'demand', 'No entry in maildrop_status_tbl for mailing_id %r found' % mailingID)
					report += 'Mailing %r is not an on demand mailing\n' % mailingID
				elif genStatus not in (1, 3):
					agn.log (agn.LV_WARNING, 'demand', 'Mailing %d has genstatus %d, unable to restart at the moment' % (mailingID, genStatus))
					report += 'Mailing %r has genstatus %r and can not be restarted at this point\n' % (mailingID, genStatus)
				else:
					if genStatus == 3:
						agn.log (agn.LV_VERBOSE, 'demand', 'Reset mailing')
						rows = dba.cursor.execute ('UPDATE maildrop_status_tbl SET genstatus = 1 WHERE status_id = :statusID AND genstatus = 3', {'statusID': statusID})
						dba.commit (rows == 1)
					else:
						agn.log (agn.LV_DEBUG, 'demand', 'Found mailing %r with statusID %d' % (mailingID, statusID))
			else:
				agn.log (agn.LV_ERROR, 'demand', 'Failed to setup database')
				report += 'No access to database\n'
		finally:
			dba.done ()
		if statusID is not None:
			(rc, report) = self.fire (statusID)
		return (rc, report)
	
	def reset (self, statusID):
		rc = -1
		report = ''
		dba = DBAccess ()
		try:
			if dba.isopen ():
				rows = dba.cursor.update ('UPDATE maildrop_status_tbl SET genstatus = 1, genchange = sysdate WHERE status_id = :sid', {'sid': statusID})
				if rows == 1:
					rc = 0
				else:
					report += 'Expected one row to update, updated %d rows' % rows
				dba.commit (rc == 0)
			else:
				report += 'Failed to access database\n'
		finally:
			dba.done ()
		return (rc, report)
	
	def __answer (self, ok, s):
		if ok:
			agn.log (agn.LV_DEBUG, 'answer', 'OK : %s' % s)
			content = '+OK: %s\n' % s
		else:
			agn.log (agn.LV_INFO, 'answer', 'ERR: %s' % s)
			content = '-ERR: %s\n' % s
		return agn.mutable (code = 200, mime = 'text/plain', headers = None, content = content)

	def doPing (self, path, param, parsedParam):
		return self.__answer (True, 'pong')
	
	def doDemand (self, path, param, parsedParam):
		mailingID = numParse (param)
		if mailingID is None:
			return self.__answer (False, 'Invalid parameter %r, mailingID expected' % param)
		(rc, report) = self.demand (mailingID)
		return self.__answer (rc == 0, report)
	
	def doFire (self, path, param, parsedParam):
		statusID = numParse (param)
		if statusID is None:
			return self.__answer (False, 'Invalid parameter %r, statusID expected' % param)
		(rc, report) = self.fire (statusID)
		return self.__answer (rc == 0, report)

	def doReset (self, path, param, parsedParam):
		statusID = numParse (param)
		if statusID is None:
			return self.__answer (False, 'Invalid parameter %r, statusID expected' % param)
		(rc, report) = self.reset (statusID)
		return self.__answer (rc == 0, report)

class Callback (xagn.XMLRPC.RObject):
	def __init__ (self, trigger):
		xagn.XMLRPC.RObject.__init__ (self)
		self.trigger = trigger
	
	def __response (self, rc):
		return [rc[0] == 0, rc[1]]

	def startMailing (self, statusID):
		return self.__response (self.trigger.fire (statusID))

	def startOnDemandMailing (self, mailingID):
		return self.__response (self.trigger.demand (mailingID))
	
	def __stateCheck (self, t):
		err = 'Unspecified'
		path = '/proc/%d/status' % t.pid
		try:
			fd = open (path, 'r')
			status = fd.read ()
			fd.close ()
			state = None
			for line in status.split ('\n'):
				try:
					(var, val) = [_v.strip () for _v in line.split (':', 1)]
				except ValueError:
					pass
				if var == 'State' and val:
					state = val[0]
			if state is not None:
				if state == 'Z':
					err = 'is zombie'
				elif state == 'T':
					err = 'is stopped'
				else:
					err = None
			else:
				err = 'Failed to find state for process ID %d in\n%s' % (t.pid, status)
		except IOError, e:
			err = 'Failed to read rocess path %s: %s' % (path, str (e))
		return err
		
	def isActive (self):
		rc = False
		reason = []
		proc = eagn.Processtable ()
		be = proc.select (user = 'openemm', comm = 'java', rcmd = 'org.agnitas.backend.MailoutServerXMLRPC')
		if len (be) == 1:
			err = self.__stateCheck (be[0])
			if err is None:
				rc = True
			else:
				reason.append ('Backend: %s' % err)
		elif len (be) > 1:
			reason.append ('There are more than one backend running: %s' % ', '.join ([str (_t.pid) for _t in be]))
		else:
			reason.append ('No java process is active')
		if not rc:
			agn.log (agn.LV_WARNING, 'active', 'Backend activity error: %s' % ', '.join (reason))
		elif reason:
			agn.log (agn.LV_INFO, 'active', 'Backend is active: %s' % ', '.join (reason))
		return rc

class Launcher (eagn.Watchdog):
	def __init__ (self, cfg):
		eagn.Watchdog.__init__ (self)
		self.cfg = cfg
	
	def run (self):
		server = xagn.XMLRPC (self.cfg, full = True)
		trigger = Trigger (self.cfg)
		server.addHandler ('/ping', trigger.doPing)
		server.addHandler ('/demand', trigger.doDemand)
		server.addHandler ('/fire', trigger.doFire)
		server.addHandler ('/reset', trigger.doReset)
		cb = Callback (trigger)
		server.addInstance (cb)
		server.run ()

def main ():
	cfgfile = None
	port = agn._syscfg.iget ('trigger-port', 8080)
	(opts, param) = getopt.getopt (sys.argv[1:], 'vc:p:')
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
		elif opt[0] == '-c':
			cfgfile = opt[1]
		elif opt[0] == '-p':
			port = int (opt[1])
	cfg = eagn.Config ()
	cfg['xmlrpc.port'] = str (port)
	cfg['xmlrpc.server'] = 'forking'
	cfg['merger.port'] = '8089'
	if cfgfile is not None:
		cfg.read (cfgfile)
	agn.lock ()
	agn.log (agn.LV_INFO, 'main', 'Starting up')
	launch = Launcher (cfg)
	launch.mstart (launch.Job ('webserver', launch.run, ()), restartDelay = 5)
	agn.log (agn.LV_INFO, 'main', 'Going down')
	agn.unlock ()

if __name__ == '__main__':
	main ()
