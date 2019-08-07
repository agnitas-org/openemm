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
import	re
import	agn
#
def main ():
	target = agn.mkpath (agn.base, 'JAVA')
	webroot = agn.mkpath (agn.base, 'webapps', 'emm')
	libs = agn.mkpath (webroot, 'WEB-INF', 'lib')
	syslibs = agn.mkpath (agn.base, 'tomcat', 'lib')
	classes = agn.mkpath (webroot, 'WEB-INF', 'classes')
	for lib in libs, syslibs:
		agn.relink (lib, target, [re.compile ('.*\\.(jar|zip)$', re.IGNORECASE)])
	agn.relink (classes, target, [re.compile ('^(com|org)$')])

if __name__ == '__main__':
	main ()
