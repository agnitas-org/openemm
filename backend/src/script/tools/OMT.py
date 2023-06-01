#!/usr/bin/python3
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

import sys
import os

from EMT_lib.Environment import Environment
from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities
from EMT_lib.Menu import Menu

from EMT_lib import ApplicationStatusMenu
from EMT_lib import InstallAndUpdateMenu
from EMT_lib import CheckMenu
from EMT_lib import DbManagementMenu
from EMT_lib import BasicWebappMenu
from EMT_lib import LayoutMenu
from EMT_lib import VersionSwitchMenu
from EMT_lib import ConfigurationMenu
from EMT_lib import SupplementalMenu

def configureTool():
	Environment.toolName = "OpenEMM Maintenance Tool (OMT)"

	# OpenEMM at GitHub: https://github.com/agnitas-org/OpenEMM
	Environment.applicationName = "OpenEMM"
	if os.path.isfile("/home/" + os.getlogin() + "/.OMT/OMT.override.properties"):
		customProperty = EMTUtilities.readPropertiesFile("/home/" + os.getlogin() + "/.OMT/OMT.override.properties")
		Environment.applicationUserNamesToCheck = customProperty["Environment.applicationUserNamesToCheck"].split(",")
	else:
		Environment.applicationUserNamesToCheck = ["openemm"]

	Environment.applicationDbcfgEntryDefaultName = "openemm"

	Environment.agnitasDownloadSiteUrl = "https://www.agnitas.de/download"
	Environment.agnitasDownloadPathVersionInfo = Environment.agnitasDownloadSiteUrl + "/openemm-version-22.10/"
	Environment.updateChannel = None
	Environment.agnitasUpdateChannels = {"TEST": Environment.agnitasDownloadSiteUrl + "/openemm-version-22.10_TEST/"}

	Environment.defaultLandingPage = None
	Environment.multiServerSystem = False

	Environment.pythonInstallationSearchPaths = ["/home/openemm/opt/python3/bin/python3", "/usr/bin/python3"]
	Environment.dbClientMysqlSearchPaths = ["/usr/bin/mysql", "/opt/rh/rh-mariadb101/root/usr/bin/mysql", "/opt/rh/rh-mariadb102/root/usr/bin/mysql", "/opt/rh/rh-mariadb103/root/usr/bin/mysql"]
	Environment.dbClientOracleSearchPaths = []
	Environment.javaSearchPaths = ["/opt/openemm/java"]
	Environment.tomcatSearchPaths = ["/home/openemm/opt/tomcat", "/opt/openemm/tomcat"]
	Environment.tomcatNativeEmmShDefault = None
	Environment.tomcatNativeSearchPaths = ["/home/openemm/opt/tomcat-native", "/opt/openemm/tomcat-native"]
	Environment.dbcfgPropertiesFileSearchPaths = ["/home/openemm/etc/dbcfg"]
	Environment.optDefaultPath = "/home/openemm/opt"
	Environment.systemCfgFileSearchPaths = None
	Environment.allowedDbmsSystems = ["mariadb", "mysql"]
	Environment.restartToolName = "openemm.sh"

	Environment.javaHome = None
	Environment.catalinaHome = None
	Environment.tomcatNative = None
	Environment.wkhtmltopdf = None
	Environment.wkhtmltoimage = None

def main():
	if "-debug" in sys.argv or "--debug" in sys.argv:
		EMTUtilities.setDebugMode(True)

	if not EMTUtilities.isDebugMode():
		sys.tracebacklimit = 0

	configureTool()

	if "?" in sys.argv or "-?" in sys.argv or "--?" in sys.argv or "help" in sys.argv or "-help" in sys.argv or "--help" in sys.argv:
		EMTUtilities.printTextInBox(Environment.toolName + " v" + Environment.toolVersion, "=")
		print("Commandline parameters:\n")
		print("\t-help")
		print("\t\tShow this help text\n")
		print("\t-update <UpdatePackageFilePath>")
		print("\t\tUpdate with given update package file or cloud url without any user interaction")
		print("\t\tLeave <UpdatePackageFilePath> empty for website update")
		print("\t-check")
		print("\t\tIntegrity check of the installation\n")
		print()
		sys.exit(0)

	Environment.init()

	if "-update" in sys.argv:
		errorsOccurred = InstallAndUpdateMenu.executeUnattendedUpdate()
	elif "-check" in sys.argv:
		foundError = CheckMenu.executeCheck()
		sys.exit(1 if foundError else 0)
	else:
		foundApplicationUserName = False
		for applicationUserNameToCheck in Environment.applicationUserNamesToCheck:
			if Environment.username == applicationUserNameToCheck:
				foundApplicationUserName = True
				break
		if not foundApplicationUserName and not EMTUtilities.hasRootPermissions() and not EMTUtilities.isDebugMode():
			print(Colors.RED + "\nYou must start this program as one of the allowed users '" + "', '".join(Environment.applicationUserNamesToCheck) + "' or with root permissions (sudo)!" + Colors.DEFAULT + "\n")
			sys.exit(1)

	menu = Menu("Main")

	menu.addSubMenu(Menu("Show " + Environment.applicationName + " status", lambda: Environment.username != "mailout").setAction(ApplicationStatusMenu.statusMenuAction))

	configurationMenu = menu.addSubMenu(Menu("Configuration"))
	basicWebappMenu = configurationMenu.addSubMenu(Menu("Configure basic environment (Java, Tomcat, Tomcat-Native, Wkhtml, Proxy)", lambda: Environment.username != "mailout").setAction(BasicWebappMenu.basicWebappMenuAction))
	dbcfgMenu = configurationMenu.addSubMenu(Menu("Change configuration of database connection", lambda: Environment.username != "mailout").setAction(DbManagementMenu.dbcfgMenuAction))
	Environment.configTableMenu = Menu("Change basic configuration", lambda: DbConnector.checkDbStructureExists()).setAction(ConfigurationMenu.configTableMenuAction)
	configurationMenu.addSubMenu(Environment.configTableMenu)
	if Environment.systemCfgFilePath is not None and os.path.isfile(Environment.systemCfgFilePath):
		configurationMenu.addSubMenu(Menu("Change system.cfg").setAction(ConfigurationMenu.systemCfgMenuAction))
	configurationMenu.addSubMenu(Menu("Change client/account configuration", lambda: DbConnector.checkDbStructureExists()).setAction(ConfigurationMenu.clientMenuAction))
	configurationMenu.addSubMenu(Menu("Change jobqueue configuration", lambda: DbConnector.checkDbStructureExists()).setAction(ConfigurationMenu.jobqueueMenuAction))
	if Environment.defaultLandingPage is not None:
		configurationMenu.addSubMenu(Menu("Change landing page url", lambda: DbConnector.checkDbStructureExists()).setAction(ConfigurationMenu.landingPageMenuAction))

	databaseMenu = menu.addSubMenu(Menu("Database Maintenance", lambda: Environment.username != "mailout"))
	databaseMenu.addSubMenu(Menu("Check MariaDB table format", lambda: DbConnector.checkDbStructureExists() and DbConnector.emmDbVendor == "mariadb").setAction(SupplementalMenu.dbTableFormatCheckMenuAction))
	databaseMenu.addSubMenu(Menu("Backup MySQL/MariaDB database", lambda: DbConnector.checkDbStructureExists() and DbConnector.getDbClientPath() is not None and DbConnector.emmDbVendor == "mysql" or DbConnector.emmDbVendor == "mariadb").setAction(DbManagementMenu.dbBackupMenuAction))
	databaseMenu.addSubMenu(Menu("Restore MySQL/MariaDB database", lambda: DbConnector.getDbClientPath() is not None and DbConnector.emmDbVendor == "mysql" or DbConnector.emmDbVendor == "mariadb").setAction(DbManagementMenu.dbRestoreMenuAction))
	databaseMenu.addSubMenu(Menu("Create new database (drop existing data)", lambda: DbConnector.getDbClientPath() is not None).setAction(DbManagementMenu.clearDatabaseMenuAction))

	securityMenu = menu.addSubMenu(Menu("Security"))
	securityMenu.addSubMenu(Menu("Create new initial 'emm-master' password", lambda: DbConnector.checkDbStructureExists()).setAction(SupplementalMenu.masterPasswordMenuAction))
	if Environment.isEmmFrontendServer:
		securityMenu.addSubMenu(Menu("Install license file", lambda: DbConnector.checkDbStructureExists()).setAction(SupplementalMenu.licenseFileMenuAction))

	menu.addSubMenu(Menu("Install or update package from AGNITAS Website", lambda: Environment.agnitasDownloadSiteUrlReachable and Environment.agnitasDownloadPathVersionInfo is not None).setAction(InstallAndUpdateMenu.siteUpdateMenuAction))
	menu.addSubMenu(Menu("Install or update package from local file").setAction(InstallAndUpdateMenu.fileUpdateMenuAction))
	menu.addSubMenu(Menu("Install or update package from AGNITAS Cloud", lambda: Environment.agnitasCloudUrlReachable).setAction(InstallAndUpdateMenu.cloudUpdateMenuAction))
	menu.addSubMenu(Menu("Show update history", lambda: Environment.username != "mailout").setAction(SupplementalMenu.updateHistoryMenuAction))
	menu.addSubMenu(Menu("Switch " + Environment.applicationName + " version", lambda: True).setAction(VersionSwitchMenu.switchVersionMenuAction))
	menu.addSubMenu(Menu("Restart " + Environment.applicationName, lambda: Environment.catalinaHome is not None and Environment.systemUrl is not None).setAction(SupplementalMenu.restartMenuAction))
	menu.addSubMenu(Menu("Send configuration and log files in email", lambda: DbConnector.checkDbStructureExists()).setAction(ApplicationStatusMenu.sendConfigAndLogsAction))

	try:
		if Environment.javaHome is None or Environment.javaHome == "" or not EMTUtilities.checkJavaAvailable(Environment.javaHome):
			Environment.errors.append("Basic webapplication configuration for JAVA is missing or invalid. Please configure.")
			Environment.overrideNextMenu = basicWebappMenu
		elif Environment.catalinaHome is None or Environment.catalinaHome == "" or not EMTUtilities.checkTomcatAvailable(Environment.javaHome, Environment.catalinaHome):
			Environment.errors.append("Basic webapplication configuration for Tomcat/CatalinaHome is missing or invalid. Please configure.")
			Environment.overrideNextMenu = basicWebappMenu
		elif not Environment.isEmmRdirServer and (Environment.wkhtmltopdf is None or Environment.wkhtmltopdf == "" or not os.path.isfile(Environment.wkhtmltopdf)):
			Environment.errors.append("Basic webapplication configuration for WKHTML (wkhtmltopdf) is missing or invalid. Please configure.")
			Environment.overrideNextMenu = basicWebappMenu
		elif not Environment.isEmmRdirServer and (Environment.wkhtmltoimage is None or Environment.wkhtmltoimage == "" or not os.path.isfile(Environment.wkhtmltoimage)):
			Environment.errors.append("Basic webapplication configuration for WKHTML (wkhtmltoimage) is missing or invalid. Please configure.")
			Environment.overrideNextMenu = basicWebappMenu
		elif (DbConnector.emmDbVendor is None or not DbConnector.checkDbServiceAvailable()) and Environment.username != "mailout":
			Environment.errors.append("Database is not running or host is invalid. Please configure.")
			Environment.overrideNextMenu = dbcfgMenu
		elif Environment.getSystemUrl() is None or Environment.getSystemUrl().strip() == "" or Environment.getSystemUrl().strip() == "Unknown" and DbConnector.checkDbServiceAvailable() and DbConnector.checkDbStructureExists():
			Environment.errors.append("Basic configuration is missing. Please configure.")
			Environment.overrideNextMenu = Environment.configTableMenu
		menu.show()
	except (KeyboardInterrupt):
		print(Colors.RED + "\nKilled by user\n" + Colors.DEFAULT)
		sys.exit(1)

	print()
	print("Bye")
	print()

main()
