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
import	sys, os, getopt, time, pwd
import	agn
#
class Activator (object):
	layout = [
"""CREATE TABLE Service (
	name	text,
	active	integer,
	created	integer,
	changed	integer
)"""
	]
	def __init__ (self):
		self.path = agn.mkpath (agn.base, 'var', 'run', 'activator.db')
		self.db = None
		self.cursor = None
		try:
			self.defaultStatus = {True: 1, False: 0}[agn.atob (os.environ['ACTIVATOR_DEFAULT_STATUS'])]
		except KeyError:
			self.defaultStatus = 1
		
	def __del__ (self):
		self.__close ()
	
	def __close (self):
		if self.cursor is not None:
			self.cursor.sync ()
			self.cursor.close ()
			self.cursor = None
		if self.db is not None:
			self.db.close ()
			self.db = None

	__defaults = {
	}
	def __setupDefault (self):
		try:
			user = pwd.getpwuid (os.getuid ()).pw_name
		except KeyError:
			try:
				user = os.environ['USER']
			except KeyError:
				user = None
		if user is not None:
			defaults = {}
			dpath = agn.mkpath (agn.base, 'var', 'lib', 'activation.defaults')
			if os.path.isfile (dpath):
				fd = open (dpath)
				for line in fd:
					try:
						(statusKey, id) = line.strip ().split ('=')
						defaults[id] = {'activate': True, 'deactivate': False}[statusKey]
					except (ValueError, KeyError):
						pass
				fd.close ()
			for mapping in self.__defaults, defaults:
				for (id, status) in mapping.items ():
					try:
						(duser, dservice) = id.split (':')
						if duser == user:
							try:
								status = {True: 1, False: 0}[status]
							except KeyError:
								pass
							self.__create (dservice, status)
					except ValueError:
						pass
		
	def __open (self):
		if self.db is None or not self.db.isOpen ():
			isnew = not os.path.isfile (self.path)
			if isnew:
				directory = os.path.dirname (self.path)
				if not os.path.isdir (directory):
					agn.log (agn.LV_INFO, 'open', 'Create missing path %s' % directory)
					agn.createPath (directory)
			self.db = agn.DBSQLite3 (self.path, self.layout)
			self.db.open ()
			if isnew:
				self.cursor = self.db.cursor ()
				if self.cursor is not None:
					agn.log (agn.LV_INFO, 'open', 'Setup newly created database')
					self.__setupDefault ()
					agn.log (agn.LV_INFO, 'open', 'Setup of newly created database done')
		if self.cursor is None:
			self.cursor = self.db.cursor ()
		return self.cursor is not None
	
	def __create (self, service, status):
		now = int (time.time ())
		cnt = self.cursor.execute ('INSERT INTO Service (name, active, created, changed) VALUES (:name, :active, :now, :now)', {'name': service, 'active': status, 'now': now})
		return cnt == 1
	
	def __change (self, service, status):
		now = int (time.time ())
		cnt = self.cursor.execute ('UPDATE Service SET active = :active, changed = :now WHERE name = :name AND active != :active', {'name': service, 'active': status, 'now': now})
		if cnt == 0:
			rq = self.cursor.querys ('SELECT count(*) FROM Service WHERE name = :name AND active = :active', {'name': service, 'active': status})
			if rq is None or rq[0] == 0:
				return self.__create (service, status)
			return True
		return cnt == 1
	
	def __remove (self, service):
		cnt = self.cursor.execute ('DELETE FROM Service WHERE name = :name', {'name': service})
		return cnt == 1
	
	def check (self, services):
		rc = True
		while services:
			service = services.pop (0)
			if self.__open ():
				rq = self.cursor.querys ('SELECT active FROM Service WHERE name = :name', {'name': service})
				if rq is None:
					agn.log (agn.LV_INFO, 'check', 'Create missing entry for %s' % service)
					self.__create (service, 1)
				elif rq[0] is None or rq[0] == 0:
					agn.log (agn.LV_DEBUG, 'check', '%s: is inactive: %r' % (service, rq[0]))
					rc = False
					break
				else:
					agn.log (agn.LV_DEBUG, 'check', '%s: is active: %r' % (service, rq[0]))
			else:
				rc = False
		self.__close ()
		return rc
		
	def show (self, services):
		if not self.__open ():
			return False
		#
		try:
			def showActive (a):
				return 'active  ' if a > 0 else 'inactive'
			def showDate (d):
				ts = time.localtime (d)
				return '%2d.%02d.%04d %02d:%02d:%02d' % (ts.tm_mday, ts.tm_mon, ts.tm_year, ts.tm_hour, ts.tm_min, ts.tm_sec)
					
			first = True
			seen = set ()
			for (name, active, created, changed) in self.cursor.query ('SELECT name, active, created, changed FROM Service ORDER BY name'):
				if not services or name in services:
					if first:
						print ('Service              Status     created               changed')
						first = False
					print ('%-20s %s   %s   %s' % (name, showActive (active), showDate (created), showDate (changed)))
					seen.add (name)
			if not first:
				print ('')
			for service in [_s for _s in services if _s not in seen]:
				print ('** service "%s" is not configured, it is active, by default' % service)
		finally:
			self.__close ()
		return True
	
	def __modify (self, services, status):
		rc = True
		for service in services:
			if self.__open ():
				if not self.__change (service, status):
					rc = False
			else:
				rc = False
		self.show (services)
		self.__close ()
		agn.log (agn.LV_INFO if rc else agn.LV_ERROR, 'activate', 'Services %s %s%sactivated' % (', '.join (services), '' if rc else 'NOT ', 'de' if status == 0 else ''))
		return rc

	def activate (self, services):
		return self.__modify (services, 1)
	
	def deactivate (self, services):
		return self.__modify (services, 0)
	
	def remove (self, services):
		rc = True
		for service in services:
			if self.__open ():
				if not self.__remove (service):
					rc = False
			else:
				rc = False
		self.__close ()
		if not self.show (services):
			rc = False
		return rc
#
def main ():
	act = Activator ()
	action = act.check
	(opts, parm) = getopt.getopt (sys.argv[1:], 'vsaedrh?')
	for opt in opts:
		if opt[0] == '-v':
			agn.outlevel = agn.LV_DEBUG
			agn.outstream = sys.stderr
		elif opt[0] == '-s':
			action = act.show
		elif opt[0] in ('-a', '-e'):
			action = act.activate
		elif opt[0] == '-d':
			action = act.deactivate
		elif opt[0] == '-r':
			action = act.remove
		elif opt[0] in ('-h', '-?'):
			print ("""Usage: %s [<option>] [<service(s)>]
Function: manages active/inactive services
Options:
	-v      verbose/debug mode, show log on stderr
	-s      show current status for selected or all services
	-a, -e  activate/enable services
	-d      deactivate/disable services
	-r      remove services
""" % sys.argv[0])
	if not action (parm):
		sys.exit (1)

if __name__ == '__main__':
	main ()
