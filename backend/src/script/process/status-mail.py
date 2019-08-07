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
import	sys, os, getopt, socket
import	agn, dagent
#
def getUser ():
	user = None
	mode = 0
	while user is None:
		try:
			if mode == 0:
				user = os.environ['USER']
			elif mode == 1:
				import	pwd
				user = pwd.getpwuid (os.getuid ()).pw_name
			else:
				user = '[%d]' % os.getuid ()
		except KeyError:
			pass
		mode += 1
	return user

def main ():
	ok = True
	name = 'dataagent for %s on %s (%s)' % (getUser (), agn.host, socket.getfqdn ())
	text = None
	opts = getopt.getopt (sys.argv[1:], 'of10n:c:t:', ['ok', 'fail', 'name=', 'content=', 'text='])
	for opt in opts[0]:
		if opt[0] in ('-o', '-1', '--ok'):
			ok = True
		elif opt[0] in ('-f', '-0', '--fail'):
			ok = False
		elif opt[0] in ('-n', '--name'):
			name = opt[1]
		elif opt[0] in ('-c', '--content'):
			fd = open (opt[1], 'r')
			text = fd.read ()
			fd.close ()
		elif opt[0] in ('-t', '--text'):
			text = opt[1]
	sm = dagent.StatusMail (ok, name)
	if text:
		sm.setText (text)
	for fname in opts[1]:
		sm.addLogfile (fname)
	if not sm.sendMail ():
		sys.exit (1)
main ()
