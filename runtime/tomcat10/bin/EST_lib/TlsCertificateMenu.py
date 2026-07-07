import os
import re

from EST_lib import ESTUtilities

from EST_lib.Environment import Environment

def executeTlsCertificateMenuAction(actionParameters):
	if Environment.isOpenEmmServer:
		ESTUtilities.manageTlsCertificateForTomcat(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/etc/ssl", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/server.xml", Environment.applicationName)
	elif Environment.isEmmFrontendServer:
		ESTUtilities.manageTlsCertificateForTomcat(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/sslcert", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/server.xml", Environment.applicationName)
	elif Environment.isEmmConsoleRdirServer:
		ESTUtilities.manageTlsCertificateForTomcat(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/sslcert", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/server.xml", Environment.applicationName)
	elif Environment.isEmmRdirServer:
		ESTUtilities.manageTlsCertificateForTomcat(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/sslcert", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml", Environment.applicationName)
	else:
		print("Not a frontend server. 'conf/server.xml' is not available")
		print()
		print("Press any key to continue")
		choice = input(" > ")
	return
