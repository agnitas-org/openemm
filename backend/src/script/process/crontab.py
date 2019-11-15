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
import	sys, getopt
import	agn, aps, eagn

class Plugin (aps.Plugin): #{{{
	pluginVersion = '1.0'
#}}}
def main ():
	(opts, param) = getopt.getopt (sys.argv[1:], '')
	pt = eagn.Processtable ()
	for comm in 'cron', 'crond':
		p = pt.select (comm = comm, user = 'root')
		if p:
			break
	if not p:
		raise agn.error ('cron: no such process, please install and activate service cron or crond (depending on your system)')

	c = eagn.Crontab ()
	c.update ([
		'10 2 * * * /home/openemm/bin/janitor.sh',
		'45 20 * * * /home/openemm/bin/bouncemanagement.sh',
	], user = None, runas = 'openemm', remove = [
	])
#
if __name__ == '__main__':
	main ()
