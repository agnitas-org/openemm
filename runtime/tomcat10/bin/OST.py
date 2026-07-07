#!/usr/bin/python3

import sys
import os

# Allow improved editing in terminal inputs
import readline

from EST_lib.Environment import Environment
from EST_lib import Colors
from EST_lib import DbConnector
from EST_lib import ESTUtilities
from EST_lib.Menu import Menu

from EST_lib import ApplicationStatusMenu
from EST_lib import InstallAndUpdateMenu
from EST_lib import CheckMenu
from EST_lib import DbManagementMenu
from EST_lib import BasicWebappMenu
from EST_lib import LayoutMenu
from EST_lib import VersionSwitchMenu
from EST_lib import ConfigurationMenu
from EST_lib import SupplementalMenu
from EST_lib import TlsCertificateMenu
from packaging.version import Version


def configureTool():
	Environment.toolName = "OpenEMM Support Tool (OST)"

	# OpenEMM at GitHub: https://github.com/agnitas-org/OpenEMM
	Environment.applicationName = "OpenEMM"
	Environment.scriptFilePath = os.path.dirname(os.path.realpath(__file__)) + "/OST.py"

	Environment.applicationDbcfgEntryDefaultName = "openemm"

	Environment.readonlyDbcfgProperties = ["host"]

	Environment.agnitasDownloadSiteUrl = "https://www.agnitas.de/download"
	Environment.updateChannel = None
	mainLtsVersion = ESTUtilities.getMainLtsVersion(Environment.toolVersion)
	if Environment.toolVersion is not None and mainLtsVersion is not None:
		Environment.agnitasDownloadPathVersionInfo = Environment.agnitasDownloadSiteUrl + "/openemm-version-" + mainLtsVersion + "/"
		Environment.agnitasUpdateChannels = {"TEST": Environment.agnitasDownloadSiteUrl + "/openemm-version-" + mainLtsVersion + "_TEST/"}
	else:
		Environment.agnitasDownloadPathVersionInfo = Environment.agnitasDownloadSiteUrl + "/openemm-version-24.10/"
		Environment.agnitasUpdateChannels = {"TEST": Environment.agnitasDownloadSiteUrl + "/openemm-version-24.10_TEST/"}

	Environment.defaultLandingPage = None
	Environment.multiServerSystem = False

	Environment.pythonInstallationSearchPaths = [ESTUtilities.getUserHomeDirectory("openemm") + "/opt/python3/bin/python3", "/usr/bin/python3"]
	Environment.dbClientMariadbSearchPaths = ["/usr/bin/mariadb", "/usr/bin/mysql", "/opt/rh/rh-mariadb101/root/usr/bin/mysql", "/opt/rh/rh-mariadb102/root/usr/bin/mysql", "/opt/rh/rh-mariadb103/root/usr/bin/mysql"]
	Environment.dbClientOracleSearchPaths = []
	Environment.javaSearchPaths = ["/opt/openemm/java"]
	Environment.tomcatSearchPaths = [ESTUtilities.getUserHomeDirectory("openemm") + "/opt/tomcat", "/opt/openemm/tomcat"]
	Environment.dbcfgPropertiesFileSearchPaths = [ESTUtilities.getUserHomeDirectory("openemm") + "/etc/dbcfg"]
	Environment.optDefaultPath = ESTUtilities.getUserHomeDirectory("openemm") + "/opt"
	Environment.optDefaultPathSecondary = ESTUtilities.getUserHomeDirectory("openemm") + "/opt"
	Environment.systemCfgFileSearchPaths = None
	Environment.allowedDbmsSystems = ["mariadb"]
	Environment.restartToolName = "openemm.sh"

	Environment.javaHome = None
	Environment.catalinaHome = None

def main():
	if "-debug" in sys.argv or "--debug" in sys.argv:
		ESTUtilities.setDebugMode(True)

	if not ESTUtilities.isDebugMode():
		sys.tracebacklimit = 0

	configureTool()

	if "?" in sys.argv or "-?" in sys.argv or "--?" in sys.argv or "help" in sys.argv or "-help" in sys.argv or "--help" in sys.argv:
		ESTUtilities.printTextInBox(str(Environment.toolName) + " v" + str(Environment.toolVersion), "=")
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

	if ESTUtilities.hasRootPermissions() and not ESTUtilities.userExists("openemm"):
		print(Colors.RED + "Mandatory operating system user 'openemm' is missing on this system" + Colors.DEFAULT)
		print("Do you want to create the user 'openemm' now? (Y/n, Blank => Exit):")
		answer = input(" > ").lower().strip()
		if answer == "" or answer.startswith("y") or answer.startswith("j"):
			createUserResponse = os.system("sudo useradd -m openemm")
			if createUserResponse == 0:
				print("User 'openemm' was created successfully")
				print("Press any key to continue.")
				choice = input(" > ")
			else:
				print(Colors.RED + "Cannot create mandatory operating system user 'openemm'. Please create user manually." + Colors.DEFAULT)
				print("Press any key to continue.")
				choice = input(" > ")
				sys.exit(1)

	if "-update" in sys.argv:
		InstallAndUpdateMenu.executeUnattendedUpdate()
	elif "-check" in sys.argv:
		foundError = CheckMenu.executeCheck()
		sys.exit(1 if foundError else 0)

	menu = Menu("Main")

	menu.addSubMenu(Menu("Show " + Environment.applicationName + " status", lambda: Environment.username != "mailout").setAction(ApplicationStatusMenu.statusMenuAction))

	configurationMenu = menu.addSubMenu(Menu("Configuration"))
	basicWebappMenu = configurationMenu.addSubMenu(Menu("Configure basic environment (Java, Tomcat, Proxy)", lambda: Environment.username != "mailout").setAction(BasicWebappMenu.basicWebappMenuAction))
	dbcfgMenu = configurationMenu.addSubMenu(Menu("Change configuration of database connection", lambda: Environment.username != "mailout").setAction(DbManagementMenu.dbcfgMenuAction))
	Environment.configTableMenu = Menu("Change basic configuration", lambda: DbConnector.checkDbStructureExists()).setAction(ConfigurationMenu.configTableMenuAction)
	configurationMenu.addSubMenu(Environment.configTableMenu)
	if Environment.systemCfgFilePath is not None and os.path.isfile(Environment.systemCfgFilePath):
		configurationMenu.addSubMenu(Menu("Change system.cfg").setAction(ConfigurationMenu.systemCfgMenuAction))
	configurationMenu.addSubMenu(Menu("Change layout images", lambda: DbConnector.checkDbStructureExists()).setAction(LayoutMenu.layoutImagesTableMenuAction))
	configurationMenu.addSubMenu(Menu("Change client/account configuration", lambda: DbConnector.checkDbStructureExists()).setAction(ConfigurationMenu.clientMenuAction))
	configurationMenu.addSubMenu(Menu("Change jobqueue configuration", lambda: DbConnector.checkDbStructureExists()).setAction(ConfigurationMenu.jobqueueMenuAction))
	configurationMenu.addSubMenu(Menu("Backup directories into a tar.gz archive",).setAction(SupplementalMenu.backupTomcatConfigAction))
	if Environment.defaultLandingPage is not None:
		configurationMenu.addSubMenu(Menu("Change default redirect homepage url", lambda: DbConnector.checkDbStructureExists()).setAction(ConfigurationMenu.landingPageMenuAction))

	databaseMenu = menu.addSubMenu(Menu("Database Maintenance", lambda: Environment.username != "mailout"))
	databaseMenu.addSubMenu(Menu("Check MariaDB table format", lambda: DbConnector.checkDbStructureExists() and DbConnector.emmDbVendor == "mariadb").setAction(SupplementalMenu.dbTableFormatCheckMenuAction))
	databaseMenu.addSubMenu(Menu("Backup MySQL/MariaDB database", lambda: DbConnector.checkDbStructureExists() and DbConnector.getDbClientPath() is not None and DbConnector.emmDbVendor == "mysql" or DbConnector.emmDbVendor == "mariadb").setAction(DbManagementMenu.dbBackupMenuAction))
	databaseMenu.addSubMenu(Menu("Restore MySQL/MariaDB database", lambda: DbConnector.getDbClientPath() is not None and DbConnector.emmDbVendor == "mysql" or DbConnector.emmDbVendor == "mariadb").setAction(DbManagementMenu.dbRestoreMenuAction))
	databaseMenu.addSubMenu(Menu("Create new database (drop existing data)", lambda: DbConnector.getDbClientPath() is not None).setAction(DbManagementMenu.clearDatabaseMenuAction))

	securityMenu = menu.addSubMenu(Menu("Security"))
	securityMenu.addSubMenu(Menu("Create new initial 'emm-master' password", lambda: DbConnector.checkDbStructureExists()).setAction(SupplementalMenu.masterPasswordMenuAction))
	if Environment.isEmmFrontendServer:
		securityMenu.addSubMenu(Menu("Install license file", lambda: DbConnector.checkDbStructureExists()).setAction(SupplementalMenu.licenseFileMenuAction))
	if Environment.isOpenEmmServer:
		securityMenu.addSubMenu(Menu("Configure TLS certificate (https)", None).setAction(TlsCertificateMenu.executeTlsCertificateMenuAction))

	menu.addSubMenu(Menu("Install or update package from AGNITAS Website", lambda: Environment.agnitasDownloadSiteUrlReachable and Environment.agnitasDownloadPathVersionInfo is not None).setAction(InstallAndUpdateMenu.siteUpdateMenuAction))
	menu.addSubMenu(Menu("Install or update package from local file").setAction(InstallAndUpdateMenu.fileUpdateMenuAction))
	menu.addSubMenu(Menu("Install or update package from AGNITAS Cloud", lambda: Environment.agnitasCloudUrlReachable).setAction(InstallAndUpdateMenu.cloudUpdateMenuAction))
	menu.addSubMenu(Menu("Show update history", lambda: Environment.username != "mailout").setAction(SupplementalMenu.updateHistoryMenuAction))
	menu.addSubMenu(Menu("Switch " + Environment.applicationName + " version", lambda: True).setAction(VersionSwitchMenu.switchVersionMenuAction))
	menu.addSubMenu(Menu("Restart " + Environment.applicationName, lambda: Environment.catalinaHome is not None and Environment.systemUrl is not None).setAction(SupplementalMenu.restartMenuAction))
	menu.addSubMenu(Menu("Send configuration and log files in email", lambda: DbConnector.checkDbStructureExists()).setAction(ApplicationStatusMenu.sendConfigAndLogsAction))

	try:
		if ESTUtilities.isBlank(Environment.javaHome) or not ESTUtilities.checkJavaAvailable(Environment.javaHome):
			Environment.errors.append("Basic webapplication configuration for JAVA is missing or invalid. Please configure.")
			Environment.overrideNextMenu = basicWebappMenu
		elif ESTUtilities.isBlank(Environment.catalinaHome) or not ESTUtilities.checkTomcatAvailable(Environment.javaHome, Environment.catalinaHome):
			Environment.errors.append("Basic webapplication configuration for Tomcat/CatalinaHome is missing or invalid. Please configure.")
			Environment.overrideNextMenu = basicWebappMenu
		elif (DbConnector.emmDbVendor is None or not DbConnector.checkDbServiceAvailable()) and Environment.username != "mailout":
			Environment.errors.append("Database is not running or host is invalid. Please configure.")
			Environment.overrideNextMenu = dbcfgMenu
		elif ESTUtilities.isBlank(Environment.getSystemUrl()) or Environment.getSystemUrl().strip() == "Unknown" and DbConnector.checkDbServiceAvailable() and DbConnector.checkDbStructureExists():
			Environment.errors.append("Basic configuration is missing. Please configure.")
			Environment.overrideNextMenu = Environment.configTableMenu

		if Version(ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)).major < 11:
			Environment.warnings.append("A tomcat version below 11 was detected. It is recommended to run EMM with at least version 11.")
		if not ESTUtilities.checkNodeAvailable():
			Environment.errors.append("Node.js was not detected in path. This is needed during the Frontend Installation. Please configure.")
		elif Version(ESTUtilities.getNodeVersion()).major < 22:
			Environment.warnings.append("A Node version below 22 was detected. It is recommended to run EMM with at least version 22")

		missingPuppeteerRequirements = Environment.checkForMissingPuppeteerRequirements()
		if missingPuppeteerRequirements:
			Environment.errors.append(
				"Following System Package Requirements for Nodejs have been detected to be missing: " + ", ".join(
					missingPuppeteerRequirements))

		menu.show()
	except (KeyboardInterrupt):
		print(Colors.RED + "\nKilled by user\n" + Colors.DEFAULT)
		sys.exit(1)

	print()
	print("Bye")
	print()

main()
