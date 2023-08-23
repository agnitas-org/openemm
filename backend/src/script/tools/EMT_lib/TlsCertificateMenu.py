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
import os
import re

from EMT_lib import EMTUtilities

from EMT_lib.Environment import Environment

def executeTlsCertificateMenuAction(actionParameters):
	if Environment.frontendUserName == "openemm":
		EMTUtilities.manageTlsCertificateForTomcat("/home/openemm/etc/ssl", "/home/openemm/tomcat/conf/server.xml", Environment.applicationName)
	elif Environment.frontendUserName == "console":
		EMTUtilities.manageTlsCertificateForTomcat("/home/console/sslcert", "/home/console/tomcat/conf/server.xml", Environment.applicationName)
	elif Environment.frontendUserName == "rdir":
		EMTUtilities.manageTlsCertificateForTomcat("/home/rdir/sslcert", "/home/rdir/tomcat/conf/server.xml", Environment.applicationName)
	else:
		print("Not a frontend server. 'conf/server.xml' is not available")

	print()
	print("Press any key to continue")
	choice = input(" > ")
	return
