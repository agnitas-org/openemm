#!/usr/bin/env python3
####################################################################################################################################################################################################################################################################
#                                                                                                                                                                                                                                                                  #
#                                                                                                                                                                                                                                                                  #
#        Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)                                                                                                                                                                                                   #
#                                                                                                                                                                                                                                                                  #
#        This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.    #
#        This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.           #
#        You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.                                                                                                            #
#                                                                                                                                                                                                                                                                  #
####################################################################################################################################################################################################################################################################
#
import	os, re
from	agn3.definitions import base
from	agn3.io import relink
from	agn3.runtime import CLI
#
class JSync (CLI):
	def executor (self) -> bool:
		target = os.path.join (base, 'JAVA')
		webroot = os.path.join (base, 'webapps', 'emm')
		libs = os.path.join (webroot, 'WEB-INF', 'lib')
		syslibs = os.path.join (base, 'tomcat', 'lib')
		classes = os.path.join (webroot, 'WEB-INF', 'classes')
		for lib in libs, syslibs:
			relink (lib, target, [re.compile ('.*\\.(jar|zip)$', re.IGNORECASE)])
		relink (classes, target, [re.compile ('^(com|org)$')])
		return True

if __name__ == '__main__':
	JSync.main ()
