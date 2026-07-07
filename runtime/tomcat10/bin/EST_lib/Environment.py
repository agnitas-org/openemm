import datetime
import pathlib
import sys
import os
import shutil
import logging
import subprocess
from collections import defaultdict

from EST_lib import Colors
from EST_lib import DbConnector
from EST_lib import ESTUtilities
from EST_lib.License import License

class Environment:
	################################################
	# OpenEMM and EMM specific settings start here #
	################################################

	toolVersion = "26.05.043" #Dummy version number. This is going to be replaced by build tools.
	toolName = None

	applicationName = None
	scriptFilePath = None

	frontendApplicationUsername = None
	rdirApplicationUsername = None
	mergerApplicationUsername = None
	mailerApplicationUsername = None
	mailloopApplicationUsername = None

	applicationDbcfgEntryDefaultName = None

	agnitasDownloadSiteUrl = None
	agnitasDownloadPathVersionInfo = None
	updateChannel = None
	agnitasUpdateChannels = None

	defaultLandingPage = None
	multiServerSystem = None

	pythonInstallationSearchPaths = None
	dbClientMariadbSearchPaths = None
	dbClientOracleSearchPaths = None
	javaSearchPaths = None
	tomcatSearchPaths = None
	dbcfgPropertiesFileSearchPaths = None
	optDefaultPath = None
	optDefaultPathSecondary = None
	systemCfgFileSearchPaths = None
	allowedDbmsSystems = None
	restartToolName = None

	javaHome = None
	catalinaHome = None

	####################################
	# General tool settings start here #
	####################################

	osVendor = None
	osVersion = None

	agnitasDownloadSiteUrlReachable = False

	agnitasCloudUrl = "https://share.agnitas.de"
	agnitasCloudWebdavContext = "/public.php/webdav"
	agnitasCloudUrlReachable = False

	hostname = os.uname()[1]
	systemUrl = None
	username = os.environ["USER"]
	currentUserHomeDirectory = ESTUtilities.getUserHomeDirectory(username)

	systemSpecificConfigFiles = ["tomcat/conf/server.xml", "tomcat/conf/hostname", "tomcat/conf/web.xml", "conf/keys/emm.salt", "tomcat/bin/emm.sh.additional.properties"]
	directoriesToPreserve = ["tomcat/logs"]

	environmentConfigurationFilePath = None
	emmLicenseFilePath = None
	systemCfgProperties = None
	systemCfgFilePath = None

	messages = []
	warnings = []
	errors = []

	unsavedDbcfgChanges = None
	readonlyDbcfgProperties = []

	unsavedSystemCfgChanges = None
	readonlyLicenseCfgProperties = ["licence"]

	unsavedConfigChanges = None

	readonlyConfigProperties = ["system.licence"]
	companySpecificValues = ["maximumNumberOfAccessLimitingTargetgroupsPerCompany", "maximumNumberOfAccessLimitingMailinglistsPerCompany"]

	unsavedClientChanges = None
	visibleClientProperties = ["rdir_domain", "mailloop_domain", "importalwaysinformemail", "DefaultLinkExtension"]
	companyInfoProperties = ["importalwaysinformemail", "DefaultLinkExtension"]
	readonlyClientProperties = ["company_id"]

	puppeteerRequirements = {
		"Rhel": ["gcc-c++", "mesa-libgbm", "atk", "at-spi2-atk", "libXcomposite", "libXdamage", "libXrandr", "libXrender", "libxkbcommon", "pango"],
		"Suse": ["gcc-c++", "libgbm1", "libgbm-devel"]
	}
	contentHubUrl = None

	rebootNeeded = False
	otherSystemsNeedConfig = False

	postfixVersion = None

	# "ulimit -n": Needed minimum of simultaneously opened files allowed by OS
	noFileLimit = 16384

	runtimeVersion = "Unknown"

	isOpenEmmServer = False

	frontendVersion = "Unknown"
	isEmmFrontendServer = False

	statisticsVersion = "Unknown"
	isEmmStatisticsServer = False

	webservicesVersion = "Unknown"
	isEmmWebservicesServer = False

	consoleRdirVersion = "Unknown"
	isEmmConsoleRdirServer = False

	rdirVersion = "Unknown"
	isEmmRdirServer = False
	rdirRuntimeVersion = "Unknown"

	emmBackendVersion = "Unknown"
	rdirBackendVersion = "Unknown"

	runtimeBackendVersion = None

	isEmmMergerServer = False
	mergerBackendVersion = "Unknown"

	isEmmMailerServer = False
	mailerBackendVersion = "Unknown"

	isEmmMailloopServer = False
	mailloopBackendVersion = "Unknown"
	
	isEmmUnifiedServer = False
	unifiedBackendVersion = "Unknown"

	manualVersion = "Unknown"

	overrideNextMenu = None
	configTableMenu = None

	@staticmethod
	def getPythonCommand():
		for searchPath in Environment.pythonInstallationSearchPaths:
			if os.path.isfile(searchPath):
				return searchPath
		return "python3"

	@staticmethod
	def init():
		Environment.readSystemValues()
		Environment.readApplicationValues()

	@staticmethod
	def readSystemValues():
		Environment.refreshUsernameCaches()

		possibleUpdateChannelFilePaths = [
			Environment.currentUserHomeDirectory + "/etc/agnitas.update.channel",
			ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/etc/agnitas.update.channel",
			ESTUtilities.getUserHomeDirectory("openemm") + "/etc/agnitas.update.channel",
			"/opt/agnitas.com/etc/agnitas.update.channel"
		]
		for channelFilePath in possibleUpdateChannelFilePaths:
			if os.path.isfile(channelFilePath):
				with open(channelFilePath) as channelFile:
					Environment.updateChannel = channelFile.readlines()[0].strip()
				if Environment.updateChannel in Environment.agnitasUpdateChannels:
					Environment.agnitasDownloadPathVersionInfo = Environment.agnitasUpdateChannels[Environment.updateChannel]
					break

		if os.path.isfile("/etc/os-release"):
			osProperties = ESTUtilities.readPropertiesFile("/etc/os-release")
			if not ESTUtilities.isBlank(osProperties["NAME"]):
				Environment.osVendor = osProperties["NAME"].strip().strip("\"").strip()
				if "AlmaLinux" in Environment.osVendor:
					Environment.osVendor = "Alma"
				elif "CentOS" in Environment.osVendor:
					Environment.osVendor = "CentOS"
				elif "Red Hat" in Environment.osVendor or "RedHat" in Environment.osVendor:
					Environment.osVendor = "RedHat"
				elif "SLES" in Environment.osVendor or "SUSE" in Environment.osVendor:
					Environment.osVendor = "Suse"
			else:
				Environment.osVendor = "Unknown"

			if not ESTUtilities.isBlank(osProperties["VERSION_ID"]):
				Environment.osVersion = osProperties["VERSION_ID"].strip().strip("\"").strip()
				if "." in Environment.osVersion:
					Environment.osVersion = Environment.osVersion[0:Environment.osVersion.index(".")]
			else:
				Environment.osVersion = "Unknown"
		elif os.path.isfile("/etc/issue"):
			osPropertiesText = ESTUtilities.readTextFile("/etc/issue")
			if "CentOS" in osPropertiesText:
				Environment.osVendor = "CentOS"
				Environment.osVersion = ESTUtilities.getFirstVersionFromText(osPropertiesText)
				if ESTUtilities.isBlank(Environment.osVersion):
					Environment.osVersion = "Unknown"
			else:
				Environment.osVendor = "Unknown"
				Environment.osVersion = "Unknown"
		else:
			Environment.osVendor = "Unknown"
			Environment.osVersion = "Unknown"

		# Check ulimit (maximum parallel files open)
		ulimitValueString = subprocess.check_output("ulimit -n", shell=True).decode("UTF-8")
		ulimitValueString = ulimitValueString.strip()
		if int(ulimitValueString) < Environment.noFileLimit:
			print("System value for maximum parallel files open (= ulimit) is " + ulimitValueString + ". Must be at least " + str(Environment.noFileLimit) + ".")
			if not ESTUtilities.hasRootPermissions():
				print(Colors.RED + "You need to start the program with root permissions (sudo) to update the systems 'ulimit' value" + Colors.DEFAULT)
				sys.exit(1)

			print("Change it now (N/y, Blank => Cancel):")
			answer = input(" > ").lower().strip()
			if answer.startswith("y") or answer.startswith("j"):
				try:
					print("Changing /etc/security/limits.conf")
					os.system("echo '* hard nofile " + str(Environment.noFileLimit) + "' | sudo tee --append /etc/security/limits.conf > /dev/null")
					os.system("echo '* soft nofile " + str(Environment.noFileLimit) + "' | sudo tee --append /etc/security/limits.conf > /dev/null")
					os.system("echo 'root hard nofile " + str(Environment.noFileLimit) + "' | sudo tee --append /etc/security/limits.conf > /dev/null")
					os.system("echo 'root soft nofile " + str(Environment.noFileLimit) + "' | sudo tee --append /etc/security/limits.conf > /dev/null")
					if os.path.isfile("/etc/systemd/user.conf"):
						print("Changing /etc/systemd/user.conf")
						os.system("echo 'DefaultLimitNOFILE=" + str(Environment.noFileLimit) + "' | sudo tee --append /etc/systemd/user.conf > /dev/null")
					if os.path.isfile("/etc/systemd/system.conf"):
						print("Changing /etc/systemd/system.conf")
						os.system("echo 'DefaultLimitNOFILE=" + str(Environment.noFileLimit) + "' | sudo tee --append /etc/systemd/system.conf > /dev/null")

					print(Colors.RED + "To let this changes take effect the system needs to be rebooted" + Colors.DEFAULT)
				except:
					print(Colors.RED + "Change of system value for maximum parallel files open (= ulimit) was not successful." + Colors.DEFAULT)
					if ESTUtilities.isDebugMode():
						logging.exception("ulimit change was not successful")
				sys.exit(1)
			else:
				print(Colors.RED + "System value for maximum parallel files open (= ulimit) is too low." + Colors.DEFAULT)
				print("Start anyway (N/y, Blank => Cancel):")
				answer = input(" > ").lower().strip()
				if not (answer.startswith("y") or answer.startswith("j")):
					sys.exit(1)

		if ESTUtilities.isNotBlank(Environment.getFrontendApplicationUserName()) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/.bashrc"):
			with open(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/.bashrc", "r") as bashRcFile:
				bashRcFileContent = bashRcFile.read()
			if bashRcFileContent is not None and "PATH=\"$HOME/.local/bin:$HOME/bin:$PATH\"" not in bashRcFileContent:
				bashRcFileContent = bashRcFileContent + "\n" \
					+ "PATH=\"$HOME/.local/bin:$HOME/bin:$PATH\"\n" \
					+ "export PATH\n"
			
				with open(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/.bashrc", "w", encoding="UTF-8") as bashRcFile:
					bashRcFile.write(bashRcFileContent)

		DbConnector.dbcfgPropertiesFilePath = Environment.dbcfgPropertiesFileSearchPaths[0]
		for dbcfgPropertiesFileSearchPath in Environment.dbcfgPropertiesFileSearchPaths:
			if os.path.isfile(dbcfgPropertiesFileSearchPath):
				DbConnector.dbcfgPropertiesFilePath = dbcfgPropertiesFileSearchPath
				break
		if not os.path.isdir(os.path.dirname(DbConnector.dbcfgPropertiesFilePath)):
			if "OpenEMM" in Environment.applicationName and ESTUtilities.userExists("openemm"):
				ESTUtilities.createDirectories(os.path.dirname(DbConnector.dbcfgPropertiesFilePath), "openemm")
			elif ESTUtilities.hasRootPermissions():
				ESTUtilities.createDirectories(os.path.dirname(DbConnector.dbcfgPropertiesFilePath), "root", "dbcfg")

		if DbConnector.applicationDbcfgEntryName is None:
			DbConnector.applicationDbcfgEntryName = Environment.applicationDbcfgEntryDefaultName

		if not os.path.isfile(DbConnector.dbcfgPropertiesFilePath):
			if ESTUtilities.userExists("openemm") or ESTUtilities.userExists(Environment.getFrontendApplicationUserName()) or ESTUtilities.userExists(Environment.getRdirApplicationUserName()) or ESTUtilities.userExists("merger") or ESTUtilities.userExists("mailloop"):
				with open(DbConnector.dbcfgPropertiesFilePath, "w", encoding="UTF-8") as propertiesFileHandle:
					propertiesFileHandle.write(DbConnector.applicationDbcfgEntryName + ": dbms=, host=localhost, user=, password=, name=" + Environment.applicationName.lower())
				DbConnector.dbcfgProperties = DbConnector.readDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath)
				if ESTUtilities.hasRootPermissions():
					if "OpenEMM" in Environment.applicationName and ESTUtilities.userExists("openemm"):
						os.system("chown openemm:openemm " + DbConnector.dbcfgPropertiesFilePath)
					else:
						os.system("chown root:dbcfg " + DbConnector.dbcfgPropertiesFilePath)
		else:
			DbConnector.dbcfgProperties = DbConnector.readDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath)

		if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/emm.sh.additional.properties"):
			additionalProperties = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/emm.sh.additional.properties")
			if "DB_PROPERTIES_ITEM_NAME" in additionalProperties:
				DbConnector.applicationDbcfgEntryName = additionalProperties["DB_PROPERTIES_ITEM_NAME"].strip()
				DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName] if DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties else None

		elif os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/bin/emm.sh.additional.properties"):
			additionalProperties = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/bin/emm.sh.additional.properties")
			if "DB_PROPERTIES_ITEM_NAME" in additionalProperties:
				DbConnector.applicationDbcfgEntryName = additionalProperties["DB_PROPERTIES_ITEM_NAME"].strip()
				DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName] if DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties else None

		if DbConnector.dbcfgProperties is not None and DbConnector.dbcfgEntry is None and Environment.applicationDbcfgEntryDefaultName in DbConnector.dbcfgProperties:
			DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[Environment.applicationDbcfgEntryDefaultName]

		if DbConnector.dbcfgProperties is not None and DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties:
			dbVendor = DbConnector.dbcfgEntry["dbms"]
			if "oracle" == dbVendor.lower().strip():
				DbConnector.emmDbVendor = "oracle"
			elif "mariadb" == dbVendor.lower().strip():
				DbConnector.emmDbVendor = "mariadb"
			elif "mysql" == dbVendor.lower().strip():
				DbConnector.emmDbVendor = "mysql"
				if DbConnector.dbcfgEntry["jdbc-driver"] == "com.mysql.jdbc.Driver":
					Environment.messages.append("Updating MySQL jdbc driver class name in dbcfg")
					DbConnector.dbcfgEntry["jdbc-driver"] = "com.mysql.cj.jdbc.Driver"
					DbConnector.dbcfgEntry["dbEntryName"] = DbConnector.applicationDbcfgEntryName

					if ESTUtilities.userExists("openemm") and (os.environ["USER"] == "openemm" or ESTUtilities.hasRootPermissions()):
						usernameForSecureLibImportScript = "openemm"
					elif ESTUtilities.userExists(Environment.getRdirApplicationUserName()) and (os.environ["USER"] == Environment.getRdirApplicationUserName() or ESTUtilities.hasRootPermissions()):
						usernameForSecureLibImportScript = Environment.getRdirApplicationUserName()
					elif ESTUtilities.userExists(Environment.getFrontendApplicationUserName()) and (os.environ["USER"] == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
						usernameForSecureLibImportScript = Environment.getFrontendApplicationUserName()
						
					DbConnector.updateDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath, DbConnector.dbcfgEntry, usernameForSecureLibImportScript)
			else:
				Environment.errors.append("Invalid database vendor in dbcfg: " + dbVendor.lower())
		else:
			# Check for fallback "agnitas.emm"
			if ESTUtilities.userExists("merger") and (Environment.username == "merger" or ESTUtilities.hasRootPermissions()):
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]
			if ESTUtilities.userExists("mailloop") and (Environment.username == "mailloop" or ESTUtilities.hasRootPermissions()):
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if DbConnector.dbcfgProperties is not None and DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties:
				dbVendor = DbConnector.dbcfgEntry["dbms"]
				if "oracle" == dbVendor.lower().strip():
					DbConnector.emmDbVendor = "oracle"
				elif "mariadb" == dbVendor.lower().strip():
					DbConnector.emmDbVendor = "mariadb"
				elif "mysql" == dbVendor.lower().strip():
					DbConnector.emmDbVendor = "mysql"
					if DbConnector.dbcfgEntry["jdbc-driver"] == "com.mysql.jdbc.Driver":
						Environment.messages.append("Updating MySQL jdbc driver class name in dbcfg")
						DbConnector.dbcfgEntry["jdbc-driver"] = "com.mysql.cj.jdbc.Driver"
						DbConnector.dbcfgEntry["dbEntryName"] = DbConnector.applicationDbcfgEntryName

						if ESTUtilities.userExists("openemm") and (os.environ["USER"] == "openemm" or ESTUtilities.hasRootPermissions()):
							usernameForSecureLibImportScript = "openemm"
						elif ESTUtilities.userExists(Environment.getRdirApplicationUserName()) and (os.environ["USER"] == Environment.getRdirApplicationUserName() or ESTUtilities.hasRootPermissions()):
							usernameForSecureLibImportScript = Environment.getRdirApplicationUserName()
						elif ESTUtilities.userExists(Environment.getFrontendApplicationUserName()) and (os.environ["USER"] == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
							usernameForSecureLibImportScript = Environment.getFrontendApplicationUserName()

						DbConnector.updateDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath, DbConnector.dbcfgProperties, usernameForSecureLibImportScript)
				else:
					Environment.errors.append("Invalid database vendor in dbcfg: " + dbVendor.lower())
			elif ESTUtilities.userExists("openemm") or ESTUtilities.userExists(Environment.getFrontendApplicationUserName()) or ESTUtilities.userExists(Environment.getRdirApplicationUserName()) or ESTUtilities.userExists("merger") or ESTUtilities.userExists("mailloop"):
				Environment.errors.append("Invalid DbcfgEntryName defined: " + DbConnector.applicationDbcfgEntryName)
		
		if DbConnector.emmDbVendor == "mariadb":
			Environment.checkForMandatoryMariaDBPackages(Environment.osVendor)

		if sys.version_info[0] < 3 or (sys.version_info[0] == 3 and sys.version_info[1] < 8):
			print("This python installation (" + str(sys.version_info[0]) + "." + str(sys.version_info[1]) + "." + str(sys.version_info[2]) + ") does not support the needed database drivers.\nPlease install at least python version 3.8 or above")
			sys.exit(1)
		elif not "oracle" in Environment.allowedDbmsSystems and not DbConnector.isMysqlDriverModuleAvailable() and not DbConnector.isMariadbDriverModuleAvailable():
			installCommandMysqlPythonModule = "sudo " + Environment.getPythonCommand() + " -m pip install mysqlclient"
			installCommandMariadbPythonModule = "sudo " + Environment.getPythonCommand() + " -m pip install mariadb"

			if Environment.osVendor == "Suse":
				print("The mandatory database vendor MySQL/MariaDB is not supported by this python installation.\nPlease install mandatory driver via")
				print(" for MySQL:")
				print("  'zypper install -y libmariadb-devel python-devel'")
				print("  '" + installCommandMysqlPythonModule + "'")
				print("or")
				print(" for MariaDB:")
				print("  Configure the official MariaDB repository 'https://downloads.mariadb.org/mariadb/repositories/'")
				print("  'zypper install -y MariaDB-devel MariaDB-shared MariaDB-client'")
				print("  '" + installCommandMariadbPythonModule + "'")
				print("and restart")
				sys.exit(1)
			else:
				installCommandMariadbAlternative = None
				if Environment.osVendor == "Alma" and Environment.osVersion == "7":
					installCommandMysql = "sudo yum install -y MySQL-python"
					installCommandMariadb = "sudo yum install -y MariaDB-shared MariaDB-devel"
					installCommandMariadbAlternative = "sudo yum install -y mariadb-common"
				elif Environment.osVendor == "Alma":
					installCommandMysql = "sudo yum install -y MySQL-python"
					installCommandMariadb = "sudo yum install -y mariadb-connector-c mariadb-connector-c-devel"
				elif Environment.osVendor == "CentOS" and Environment.osVersion == "7":
					installCommandMysql = "sudo yum install -y MySQL-python"
					installCommandMariadb = "sudo yum install -y MariaDB-shared MariaDB-devel"
					installCommandMariadbAlternative = "sudo yum install -y mariadb-common"
				elif Environment.osVendor == "CentOS":
					installCommandMysql = "sudo yum install -y MySQL-python"
					installCommandMariadb = "sudo yum install -y mariadb-connector-c mariadb-connector-c-devel"
				elif Environment.osVendor == "RedHat" and Environment.osVersion == "7":
					installCommandMysql = "sudo yum install -y MySQL-python"
					installCommandMariadb = "sudo yum install -y MariaDB-shared MariaDB-devel"
					installCommandMariadbAlternative = "sudo yum install -y mariadb-common"
				elif Environment.osVendor == "RedHat":
					installCommandMysql = "sudo yum install -y MySQL-python"
					installCommandMariadb = "sudo yum install -y mariadb-connector-c mariadb-connector-c-devel"
				else:
					installCommandMysql = "sudo " + Environment.getPythonCommand() + " -m pip install mysqlclient"
					installCommandMariadb = "sudo " + Environment.getPythonCommand() + " -m pip install mariadb"

				detectMariaDbDriverSuccess = (os.system("ld -o /dev/null -shared -lmariadb") == 0)

				if detectMariaDbDriverSuccess:
					# MariaDB driver is not available, but special driver detection was successful. This is an undefined state and cannot be handled automatically.
					print("The mandatory database vendor MySQL/MariaDB is not supported by this python installation.\nPlease install mandatory driver manually")
					sys.exit(1)
				else:
					print("The mandatory database vendor MySQL/MariaDB is not supported by this python installation.\nPlease install mandatory driver via")
					print(" for MySQL:")
					print("  '" + installCommandMysql + "'")
					print("  '" + installCommandMysqlPythonModule + "'")
					print("or")
					print(" for MariaDB:")
					print("  '" + installCommandMariadb + "'")
					if installCommandMariadbAlternative is not None and installCommandMariadbAlternative.strip() != "":
						print("  or")
						print("  '" + installCommandMariadbAlternative + "'")
					print("  '" + installCommandMariadbPythonModule + "'")
					print("and restart")

					while not DbConnector.isMysqlDriverModuleAvailable() and not DbConnector.isMariadbDriverModuleAvailable():
						print()
						print("Install mandatory python database driver for MariaDB now? (N/y, Blank => Cancel):")
						choice = input(" > ")
						choice = choice.strip().lower()
						if not (choice.startswith("y") or choice.startswith("j")):
							print("Declined needed python database driver installation\n")
							sys.exit(1)

						try:
							os.system(installCommandMariadb)
						except:
							if installCommandMariadbAlternative is not None and installCommandMariadbAlternative.strip() != "":
								try:
									os.system(installCommandMariadbAlternative)
								except:
									print(Colors.RED + "Installation of MariaDB was not successful" + Colors.DEFAULT)
									if ESTUtilities.isDebugMode():
										logging.exception("Error")
									sys.exit(1)
							else:
								print(Colors.RED + "Installation of MariaDB was not successful" + Colors.DEFAULT)
								if ESTUtilities.isDebugMode():
									logging.exception("Error")
								sys.exit(1)

						try:
							os.system(installCommandMariadbPythonModule)
						except:
							print(Colors.RED + "Installation of MariaDB-python was not successful" + Colors.DEFAULT)
							if ESTUtilities.isDebugMode():
								logging.exception("Error")
							sys.exit(1)

						DbConnector.recheckMariadbDriverModuleAvailability()
						DbConnector.recheckMysqlDriverModuleAvailability()

						if not DbConnector.isMysqlDriverModuleAvailable() and not DbConnector.isMariadbDriverModuleAvailable():
							print(Colors.RED + "The mandatory database vendor MySQL/MariaDB is not supported by this python installation.\nPlease install mandatory driver via")
							print(" for MySQL:")
							print("  '" + installCommandMysql + "'")
							print("  '" + installCommandMysqlPythonModule + "'")
							print("or")
							print(" for MariaDB:")
							print("  '" + installCommandMariadb + "'")
							print("  '" + installCommandMariadbPythonModule + "'")
							print("and restart" + Colors.DEFAULT)
							sys.exit(1)
		elif DbConnector.emmDbVendor == "mariadb" and not DbConnector.isMariadbDriverModuleAvailable():
			installCommandMariadbPythonModule = "sudo " + Environment.getPythonCommand() + " -m pip install mariadb"

			if Environment.osVendor == "Suse":
				print("The database vendor MariaDB is not supported by this python installation.\nPlease install driver via")
				print("  Configure the official MariaDB repository 'https://downloads.mariadb.org/mariadb/repositories/'")
				print("  'zypper install -y MariaDB-devel MariaDB-shared MariaDB-client'")
				print("  '" + installCommandMariadbPythonModule + "'")
				print("and restart")
				sys.exit(1)
			else:
				installCommandMariadbAlternative = None
				if Environment.osVendor == "Alma" and Environment.osVersion == "7":
					installCommandMariadb = "sudo yum install -y MariaDB-shared MariaDB-devel"
					installCommandMariadbAlternative = "sudo yum install -y mariadb-common"
				elif Environment.osVendor == "Alma":
					installCommandMariadb = "sudo yum install -y mariadb-connector-c mariadb-connector-c-devel"
				elif Environment.osVendor == "CentOS" and Environment.osVersion == "7":
					installCommandMariadb = "sudo yum install -y MariaDB-shared MariaDB-devel"
					installCommandMariadbAlternative = "sudo yum install -y mariadb-common"
				elif Environment.osVendor == "CentOS":
					installCommandMariadb = "sudo yum install -y mariadb-connector-c mariadb-connector-c-devel"
				elif Environment.osVendor == "RedHat" and Environment.osVersion == "7":
					installCommandMariadb = "sudo yum install -y MariaDB-shared MariaDB-devel"
					installCommandMariadbAlternative = "sudo yum install -y mariadb-common"
				elif Environment.osVendor == "RedHat":
					installCommandMariadb = "sudo yum install -y mariadb-connector-c mariadb-connector-c-devel"
				else:
					installCommandMariadb = "sudo " + Environment.getPythonCommand() + " -m pip install mariadb"

				detectMariaDbDriverSuccess = (os.system("ld -o /dev/null -shared -lmariadb") == 0)

				if detectMariaDbDriverSuccess:
					# MariaDB driver is not available, but special driver detection was successful. This is an undefined state and cannot be handled automatically.
					print("The mandatory database vendor MySQL/MariaDB is not supported by this python installation.\nPlease install mandatory driver manually")
					sys.exit(1)
				else:
					print("The mandatory database vendor MariaDB is not supported by this python installation.\nPlease install mandatory driver via")
					print("  '" + installCommandMariadb + "'")
					if installCommandMariadbAlternative is not None and installCommandMariadbAlternative.strip() != "":
						print("  or")
						print("  '" + installCommandMariadbAlternative + "'")
					print("  '" + installCommandMariadbPythonModule + "'")
					print("and restart")

					while not DbConnector.isMariadbDriverModuleAvailable():
						print()
						print("Install mandatory python database driver for MariaDB now? (N/y, Blank => Cancel):")
						choice = input(" > ")
						choice = choice.strip().lower()
						if not (choice.startswith("y") or choice.startswith("j")):
							print("Declined needed python database driver installation\n")
							sys.exit(1)

						try:
							os.system(installCommandMariadb)
						except:
							if installCommandMariadbAlternative is not None and installCommandMariadbAlternative.strip() != "":
								try:
									os.system(installCommandMariadbAlternative)
								except:
									print(Colors.RED + "Installation of MariaDB was not successful" + Colors.DEFAULT)
									if ESTUtilities.isDebugMode():
										logging.exception("Error")
									sys.exit(1)
							else:
								print(Colors.RED + "Installation of MariaDB was not successful" + Colors.DEFAULT)
								if ESTUtilities.isDebugMode():
									logging.exception("Error")
								sys.exit(1)

						try:
							os.system(installCommandMariadbPythonModule)
						except:
							print(Colors.RED + "Installation of MariaDB-python was not successful" + Colors.DEFAULT)
							if ESTUtilities.isDebugMode():
								logging.exception("Error")
							sys.exit(1)

						if not DbConnector.recheckMariadbDriverModuleAvailability():
							print(Colors.RED + "The mandatory database vendor MariaDB is not supported by this python installation.\nPlease install mandatory driver via")
							print("  '" + installCommandMariadb + "'")
							print("  '" + installCommandMariadbPythonModule + "'")
							print("and restart" + Colors.DEFAULT)
							sys.exit(1)
		elif DbConnector.emmDbVendor == "mysql" and not DbConnector.isMysqlDriverModuleAvailable():
			installCommandMysqlPythonModule = "sudo " + Environment.getPythonCommand() + " -m pip install mysqlclient"

			if Environment.osVendor == "Suse":
				print("The database vendor MySQL is not supported by this python installation.\nPlease install driver via")
				print("  'zypper install -y libmariadb-devel python-devel'")
				print("  '" + installCommandMysqlPythonModule + "'")
				print("and restart")
				sys.exit(1)
			else:
				if Environment.osVendor == "Alma" and Environment.osVersion == "7":
					installCommandMysql = "sudo yum install -y MySQL-python"
				elif Environment.osVendor == "Alma":
					installCommandMysql = "sudo yum install -y MySQL-python"
				elif Environment.osVendor == "CentOS" and Environment.osVersion == "7":
					installCommandMysql = "sudo yum install -y MySQL-python"
				elif Environment.osVendor == "CentOS":
					installCommandMysql = "sudo yum install -y MySQL-python"
				elif Environment.osVendor == "RedHat" and Environment.osVersion == "7":
					installCommandMysql = "sudo yum install -y MySQL-python"
				elif Environment.osVendor == "RedHat":
					installCommandMysql = "sudo yum install -y MySQL-python"
				else:
					installCommandMysql = "sudo " + Environment.getPythonCommand() + " -m pip install mysqlclient"

				detectMariaDbDriverSuccess = (os.system("ld -o /dev/null -shared -lmariadb") == 0)

				if detectMariaDbDriverSuccess:
					# MariaDB driver is not available, but special driver detection was successful. This is an undefined state and cannot be handled automatically.
					print("The mandatory database vendor MySQL/MariaDB is not supported by this python installation.\nPlease install mandatory driver manually")
					sys.exit(1)
				else:
					print("The mandatory database vendor MySQL is not supported by this python installation.\nPlease install mandatory driver via")
					print("  '" + installCommandMysql + "'")
					print("  '" + installCommandMysqlPythonModule + "'")
					print("and restart")

					while not DbConnector.isMysqlDriverModuleAvailable():
						print()
						print("Install mandatory python database driver for MySQL now? (N/y, Blank => Cancel):")
						choice = input(" > ")
						choice = choice.strip().lower()
						if not (choice.startswith("y") or choice.startswith("j")):
							print("Declined needed python database driver installation\n")
							sys.exit(1)

						try:
							os.system(installCommandMysql)
						except:
							print(Colors.RED + "Installation of MySQL was not successful" + Colors.DEFAULT)
							if ESTUtilities.isDebugMode():
								logging.exception("Error")
							sys.exit(1)

						try:
							os.system(installCommandMysqlPythonModule)
						except:
							print(Colors.RED + "Installation of MySQL-python was not successful" + Colors.DEFAULT)
							if ESTUtilities.isDebugMode():
								logging.exception("Error")
							sys.exit(1)

						if not DbConnector.recheckMysqlDriverModuleAvailability():
							print(Colors.RED + "The mandatory database vendor MySQL is not supported by this python installation.\nPlease install mandatory driver via")
							print("  '" + installCommandMysql + "'")
							print("  '" + installCommandMysqlPythonModule + "'")
							print("and restart" + Colors.DEFAULT)
							sys.exit(1)

		Environment.postfixVersion = ESTUtilities.getPostfixVersion()

		if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/setenv.sh"):
			Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/setenv.sh"
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin/setenv.sh"):
			Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin/setenv.sh"
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory("merger") + "/lbin/custom.sh"):
			Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory("merger") + "/lbin/custom.sh"
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory("mailloop") + "/lbin/custom.sh"):
			Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory("mailloop") + "/lbin/custom.sh"
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory("mailout") + "/lbin/custom.sh"):
			Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory("mailout") + "/lbin/custom.sh"
		elif ESTUtilities.userExists(Environment.getFrontendApplicationUserName()):
			# Create empty setenv.sh and migrate existing /opt/agnitas.com/software libraries
			Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/setenv.sh"
			try:
				if not os.path.isdir(os.path.dirname(Environment.environmentConfigurationFilePath)):
					ESTUtilities.createDirectories(os.path.dirname(Environment.environmentConfigurationFilePath), Environment.getFrontendApplicationUserName())

				with open(Environment.environmentConfigurationFilePath, "w", encoding="UTF-8") as environmentConfigurationFileHandle:
					if "OpenEMM" in Environment.applicationName and ESTUtilities.userExists("openemm"):
						javaHome = ""
						if os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/opt/java") or os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/opt/java"):
							javaHome = ESTUtilities.getUserHomeDirectory("openemm") + "/opt/java"
						environmentConfigurationFileData = "export JAVA_HOME=" + javaHome + "\n"

						catalinaHome = ""
						if os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/opt/tomcat") or os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/opt/tomcat"):
							catalinaHome = ESTUtilities.getUserHomeDirectory("openemm") + "/opt/tomcat"
						environmentConfigurationFileData += "export CATALINA_HOME=" + catalinaHome + "\n"

						environmentConfigurationFileData += "source $HOME/scripts/config.sh\n"
					else:
						javaHome = ""
						if os.path.islink("/opt/agnitas.com/software/java") or os.path.isdir("/opt/agnitas.com/software/java"):
							javaHome = "/opt/agnitas.com/software/java"
						environmentConfigurationFileData = "export JAVA_HOME=" + javaHome + "\n"

						catalinaHome = ""
						if os.path.islink("/opt/agnitas.com/software/tomcat") or os.path.isdir("/opt/agnitas.com/software/tomcat"):
							catalinaHome = "/opt/agnitas.com/software/tomcat"
						environmentConfigurationFileData += "export CATALINA_HOME=" + catalinaHome + "\n"

						environmentConfigurationFileData += "source $HOME/scripts/config.sh\n"

					environmentConfigurationFileHandle.write(environmentConfigurationFileData)
				os.system("chown -R " + Environment.getFrontendApplicationUserName() + " " + Environment.environmentConfigurationFilePath)
				os.system("chmod u+x " + Environment.environmentConfigurationFilePath)
				Environment.messages.append("Created initial setenv.sh file '" + Environment.environmentConfigurationFilePath + "'")
			except:
				if ESTUtilities.isDebugMode():
					logging.exception("Cannot create initial setenv.sh file")
				Environment.errors.append("Cannot create initial setenv.sh file '" + Environment.environmentConfigurationFilePath + "'")
				Environment.environmentConfigurationFilePath = None
		elif ESTUtilities.userExists(Environment.getRdirApplicationUserName()):
			# Create empty setenv.sh and migrate existing /opt/agnitas.com/software libraries
			Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin/setenv.sh"
			try:
				if not os.path.isdir(os.path.dirname(Environment.environmentConfigurationFilePath)):
					ESTUtilities.createDirectories(os.path.dirname(Environment.environmentConfigurationFilePath), Environment.getRdirApplicationUserName())

				with open(Environment.environmentConfigurationFilePath, "w", encoding="UTF-8") as environmentConfigurationFileHandle:
					if "OpenEMM" in Environment.applicationName and ESTUtilities.userExists("openemm"):
						javaHome = ""
						if os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/opt/java") or os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/opt/java"):
							javaHome = ESTUtilities.getUserHomeDirectory("openemm") + "/opt/java"
						environmentConfigurationFileData = "export JAVA_HOME=" + javaHome + "\n"

						catalinaHome = ""
						if os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/opt/tomcat") or os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/opt/tomcat"):
							catalinaHome = ESTUtilities.getUserHomeDirectory("openemm") + "/opt/tomcat"
						environmentConfigurationFileData += "export CATALINA_HOME=" + catalinaHome + "\n"

						environmentConfigurationFileData += "source $HOME/scripts/config.sh\n"
					else:
						javaHome = ""
						if os.path.islink("/opt/agnitas.com/software/java") or os.path.isdir("/opt/agnitas.com/software/java"):
							javaHome = "/opt/agnitas.com/software/java"
						environmentConfigurationFileData = "export JAVA_HOME=" + javaHome + "\n"

						catalinaHome = ""
						if os.path.islink("/opt/agnitas.com/software/tomcat") or os.path.isdir("/opt/agnitas.com/software/tomcat"):
							catalinaHome = "/opt/agnitas.com/software/tomcat"
						environmentConfigurationFileData += "export CATALINA_HOME=" + catalinaHome + "\n"

						environmentConfigurationFileData += "source $HOME/scripts/config.sh\n"

					environmentConfigurationFileHandle.write(environmentConfigurationFileData)
				os.system("chown -R " + Environment.getRdirApplicationUserName() + " " + Environment.environmentConfigurationFilePath)
				os.system("chmod u+x " + Environment.environmentConfigurationFilePath)
				Environment.messages.append("Created initial setenv.sh file '" + Environment.environmentConfigurationFilePath + "'")
			except:
				if ESTUtilities.isDebugMode():
					logging.exception("Cannot create initial setenv.sh file")
				Environment.errors.append("Cannot create initial setenv.sh file '" + Environment.environmentConfigurationFilePath + "'")
				Environment.environmentConfigurationFilePath = None

		if not os.access(DbConnector.dbcfgPropertiesFilePath, os.R_OK):
			if ESTUtilities.userExists("openemm") or ESTUtilities.userExists(Environment.getFrontendApplicationUserName()) or ESTUtilities.userExists(Environment.getRdirApplicationUserName()) or ESTUtilities.userExists("merger") or ESTUtilities.userExists("mailloop"):
				Environment.errors.append("Mandatory dbcfg file is not readable: " + DbConnector.dbcfgPropertiesFilePath)
			DbConnector.dbcfgProperties = {}
		else:
			DbConnector.dbcfgProperties = DbConnector.readDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath)

		# Read and use WebProxy settings if available
		if Environment.environmentConfigurationFilePath is not None and os.path.isfile(Environment.environmentConfigurationFilePath):
			environmentProperties = ESTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)

			# Migrate old proxy settings
			if "PROXY_HOST" in environmentProperties:
				if "PROXY_PORT" in environmentProperties:
					if environmentProperties["PROXY_PORT"] is not None:
						proxy = environmentProperties["PROXY_HOST"] + ":" + environmentProperties["PROXY_PORT"]
					else:
						proxy = environmentProperties["PROXY_HOST"]
					environmentProperties.pop("PROXY_PORT", None)
				else:
					proxy = environmentProperties["PROXY_HOST"]
				environmentProperties.pop("PROXY_HOST", None)
				if not "PROXY" in environmentProperties:
					environmentProperties["PROXY"] = proxy
				ESTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, None, environmentProperties)

			if "PROXY" in environmentProperties:
				if environmentProperties["PROXY"] is not None and len(environmentProperties["PROXY"].strip()) > 0:
					os.environ["http_proxy"] = environmentProperties["PROXY"]
					os.environ["https_proxy"] = environmentProperties["PROXY"]
					if "NO_PROXY_HOSTS" in environmentProperties and environmentProperties["NO_PROXY_HOSTS"] is not None and len(environmentProperties["NO_PROXY_HOSTS"].strip()) > 0:
						os.environ["no_proxy"] = environmentProperties["NO_PROXY_HOSTS"]
				else:
					os.environ.pop("http_proxy", None)
					os.environ.pop("https_proxy", None)
					os.environ.pop("no_proxy", None)
			else:
				if "http_proxy" in environmentProperties:
					if environmentProperties["http_proxy"] is not None and len(environmentProperties["http_proxy"].strip()) > 0:
						os.environ["http_proxy"] = environmentProperties["http_proxy"]
					else:
						os.environ.pop("http_proxy", None)
				if "https_proxy" in environmentProperties:
					if environmentProperties["https_proxy"] is not None and len(environmentProperties["https_proxy"].strip()) > 0:
						os.environ["https_proxy"] = environmentProperties["https_proxy"]
					else:
						os.environ.pop("https_proxy", None)
				if "no_proxy" in environmentProperties:
					if "no_proxy" in environmentProperties and environmentProperties["no_proxy"] is not None and len(environmentProperties["no_proxy"].strip()) > 0:
						os.environ["no_proxy"] = environmentProperties["no_proxy"]
					else:
						os.environ.pop("http_proxy", None)

		if Environment.environmentConfigurationFilePath is not None and os.path.isfile(Environment.environmentConfigurationFilePath):
			environmentProperties = ESTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)
			if "JAVA_HOME" in environmentProperties:
				Environment.javaHome = environmentProperties["JAVA_HOME"]
			if "CATALINA_HOME" in environmentProperties:
				Environment.catalinaHome = environmentProperties["CATALINA_HOME"]

		environmentWasChanged = False
		if DbConnector.emmDbVendor == "oracle":
			defaultOracleFullClient = "/opt/agnitas.com/software/oracle/product/11.2.0/client_1"
			defaultOracleInstantClient = "/opt/agnitas.com/software/oracle-instantclient"
			# Check environment for Oracle database connections
			if (not "ORACLE_HOME" in os.environ) or os.environ["ORACLE_HOME"] == "" or os.environ["ORACLE_HOME"] is None:
				if os.path.isdir(defaultOracleFullClient):
					os.environ["ORACLE_HOME"] = defaultOracleFullClient
					environmentWasChanged = True
					if (not "LD_LIBRARY_PATH" in os.environ) or os.environ["LD_LIBRARY_PATH"] == "" or os.environ["LD_LIBRARY_PATH"] is None:
						os.environ["LD_LIBRARY_PATH"] = defaultOracleFullClient + "/lib"
					else:
						os.environ["LD_LIBRARY_PATH"] = os.environ["LD_LIBRARY_PATH"] + ";" + defaultOracleFullClient + "/lib"
				elif os.path.isdir(defaultOracleInstantClient):
					os.environ["ORACLE_HOME"] = defaultOracleInstantClient
					environmentWasChanged = True
					if (not "LD_LIBRARY_PATH" in os.environ) or os.environ["LD_LIBRARY_PATH"] == "" or os.environ["LD_LIBRARY_PATH"] is None:
						os.environ["LD_LIBRARY_PATH"] = defaultOracleInstantClient
					else:
						os.environ["LD_LIBRARY_PATH"] = os.environ["LD_LIBRARY_PATH"] + ";" + defaultOracleInstantClient
			elif (not "LD_LIBRARY_PATH" in os.environ) or os.environ["LD_LIBRARY_PATH"] == "" or os.environ["LD_LIBRARY_PATH"] is None:
				if os.path.isdir(os.environ["ORACLE_HOME"] + "/lib"):
					os.environ["LD_LIBRARY_PATH"] = os.environ["ORACLE_HOME"] + "/lib"
					environmentWasChanged = True
				elif os.path.isdir(os.environ["ORACLE_HOME"]):
					os.environ["LD_LIBRARY_PATH"] = os.environ["ORACLE_HOME"]
					environmentWasChanged = True

			if (not "NLS_LANG" in os.environ) or os.environ["NLS_LANG"] == "" or os.environ["NLS_LANG"] is None:
				# Only set "NLS_LANG", if Oracle product is available and "NLS_LANG" is not set yet
				os.environ["NLS_LANG"] = "american_america.UTF8"
				environmentWasChanged = True

		if environmentWasChanged:
			# Restart is needed to let new environment variables like "ORACLE_HOME" take effect.
			# Just setting them in os.environ at runtime is not enough for Oracle driver.
			os.execv(sys.argv[0], sys.argv)

		DbConnector.detectDbClientPath(DbConnector.emmDbVendor, Environment.dbClientOracleSearchPaths, Environment.dbClientMariadbSearchPaths)

		if Environment.systemCfgFileSearchPaths is not None and len(Environment.systemCfgFileSearchPaths) > 0:
			Environment.systemCfgFilePath = Environment.systemCfgFileSearchPaths[0]
			for systemCfgFileSearchPath in Environment.systemCfgFileSearchPaths:
				if os.path.isfile(systemCfgFileSearchPath):
					Environment.systemCfgFilePath = systemCfgFileSearchPath
					break
			if not os.path.isdir(os.path.dirname(Environment.systemCfgFilePath)):
				ESTUtilities.createDirectories(os.path.dirname(Environment.systemCfgFilePath), "root", "root")
			if os.path.isfile(Environment.systemCfgFilePath):
				Environment.systemCfgProperties = ESTUtilities.readPropertiesFile(Environment.systemCfgFilePath)
			else:
				Environment.systemCfgFilePath = None
		else:
			Environment.systemCfgFilePath = None

		if Environment.systemCfgFilePath is not None and os.path.isfile(Environment.systemCfgFilePath):
			Environment.systemCfgProperties = ESTUtilities.readPropertiesFile(Environment.systemCfgFilePath)
			systemCfgDbcfgEntryName = "emm"
			if "dbid" in Environment.systemCfgProperties:
				systemCfgDbcfgEntryName = Environment.systemCfgProperties["dbid"]
			if systemCfgDbcfgEntryName != DbConnector.applicationDbcfgEntryName:
				Environment.errors.append("DbcfgEntryName defined in '" + Environment.systemCfgFilePath + "' (" + systemCfgDbcfgEntryName + ") does not match with that defined in '" + DbConnector.dbcfgPropertiesFilePath + "' (" + DbConnector.applicationDbcfgEntryName + ")")

		if Environment.agnitasDownloadPathVersionInfo is not None:
			response = None
			try:
				response = ESTUtilities.openUrlConnection(Environment.agnitasDownloadPathVersionInfo)
				Environment.agnitasDownloadSiteUrlReachable = response.getcode() == 200 or response.getcode() == 403
				if not Environment.agnitasDownloadSiteUrlReachable:
					if ESTUtilities.isDebugMode():
						print("Cannot reach: " + Environment.agnitasDownloadPathVersionInfo + " (" + str(response.getcode()) + ")")
			except:
				if ESTUtilities.isDebugMode():
					print("Cannot reach: " + Environment.agnitasDownloadPathVersionInfo)
				Environment.agnitasDownloadSiteUrlReachable = False
				if not ESTUtilities.sslIsAvailable():
					Environment.errors.append("SSL connections are not supported by the current python installation")
			finally:
				if response is not None:
					response.close()

		if Environment.postfixVersion is None:
			Environment.errors.append("No MTA (postfix) is installed (sendmail is no longer supported)")

		if Environment.agnitasCloudUrl is not None:
			response = None
			try:
				response = ESTUtilities.openUrlConnection(Environment.agnitasCloudUrl)
				Environment.agnitasCloudUrlReachable = response.getcode() == 200
			except:
				Environment.agnitasCloudUrlReachable = False
				if not ESTUtilities.sslIsAvailable():
					Environment.errors.append("SSL connections are not supported by the current python installation")
			finally:
				if response is not None:
					response.close()

		if DbConnector.checkDbServiceAvailable():
			if not DbConnector.checkDbConnection():
				Environment.errors.append("Database Connection cannot be established. (Maybe database user or database connection parameters for " + Environment.applicationName + " were not configured)")
			elif not DbConnector.checkDbStructureExists():
				Environment.errors.append("Database structure does not exist. (Maybe " + Environment.applicationName + " is not installed)")
			else:
				License.checkLicenseStatus()
		# Cleanup legacy data
		if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/EmmMaintenanceTool.sh"):
			os.remove(ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/EmmMaintenanceTool.sh")
		if os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/EmmMaintenanceTool.sh"):
			os.remove(ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/EmmMaintenanceTool.sh")

		Environment.checkMigration()

	@staticmethod
	def readApplicationValues():
		# Check application types
		if "OpenEMM" in Environment.applicationName and ESTUtilities.userExists("openemm"):
			Environment.isOpenEmmServer = True

			if ESTUtilities.hasRootPermissions() and not os.path.isfile("/usr/local/bin/OMT.sh") and not os.path.islink("/usr/local/bin/OMT.sh"):
				if os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/bin/OMT.sh"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("openemm") + "/bin/OMT.sh", "/usr/local/bin/OMT.sh", "root")
				elif os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/OMT.sh"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/OMT.sh", "/usr/local/bin/OMT.sh", "root")

			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/bin"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("openemm") + "/bin", "openemm")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/release"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("openemm") + "/release", "openemm")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps", "openemm")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/temp"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("openemm") + "/temp", "openemm")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/conf") and not os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/conf"):
				ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf", ESTUtilities.getUserHomeDirectory("openemm") + "/conf", "openemm")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/JAVA") and not os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/JAVA") and os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/release/backend/current/JAVA"):
				ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("openemm") + "/release/backend/current/JAVA", ESTUtilities.getUserHomeDirectory("openemm") + "/JAVA", "openemm")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/lib") and not os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/lib") and os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/release/backend/current/lib"):
				ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("openemm") + "/release/backend/current/lib", ESTUtilities.getUserHomeDirectory("openemm") + "/lib", "openemm")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/scripts") and not os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/scripts") and os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/release/backend/current/scripts"):
				ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("openemm") + "/release/backend/current/scripts", ESTUtilities.getUserHomeDirectory("openemm") + "/scripts", "openemm")
			if not os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf/server.xml") and os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf/server.xml.template"):
				shutil.copy(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf/server.xml.template", ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf/server.xml")
				ESTUtilities.chown(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf/server.xml", "openemm", "openemm")
				ESTUtilities.manageTlsCertificateForTomcat(ESTUtilities.getUserHomeDirectory("openemm") + "/etc/ssl", ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf/server.xml", Environment.applicationName, False, 8080)
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/logs") and not os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/logs"):
				ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/logs", ESTUtilities.getUserHomeDirectory("openemm") + "/logs", "openemm")
			if os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/release/backend/current"):
				Environment.emmBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory("openemm") + "/release/backend/current"))
				if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
					Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]

			if not os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/bin/" + Environment.restartToolName):
				ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory("openemm") + "/bin/" + Environment.restartToolName, "openemm")

			if os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf/version.txt"):
				with open(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf/version.txt") as runtimeVersionFile:
					Environment.runtimeVersion = runtimeVersionFile.read().strip()
			else:
				Environment.runtimeVersion = "Unknown"

			Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm/WEB-INF/classes/emm.license.xml"
			if os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm/WEB-INF/classes/emm.properties"):
				Environment.frontendVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/statistics/WEB-INF/classes/emm.properties"):
				Environment.statisticsVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/webservices/WEB-INF/classes/emm.properties"):
				Environment.webservicesVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]

			if os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/manual/de"):
				manualApplicationPath = os.path.realpath(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/manual/de")
				manualVersion = ESTUtilities.getVersionFromFilename(manualApplicationPath)
				if manualVersion is not None:
					Environment.manualVersion = manualVersion
		else:
			if ESTUtilities.hasRootPermissions():
				if not os.path.isfile("/usr/local/bin/EST.sh") and not os.path.islink("/usr/local/bin/EST.sh") and Environment.scriptFilePath.startswith("/home/") and len(Environment.scriptFilePath) > 6:
					scriptHomeUser = Environment.scriptFilePath[6:]
					scriptHomeUser = scriptHomeUser[0:scriptHomeUser.index("/")]
					if os.path.isfile(ESTUtilities.getUserHomeDirectory(scriptHomeUser) + "/bin/EST.sh"):
						ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(scriptHomeUser) + "/bin/EST.sh", "/usr/local/bin/EST.sh", "root")
			else:
				if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin") and not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/EST.sh"):
					if Environment.username == "merger" or Environment.username == "mailout" or Environment.username == "mailloop":
						estPath = ESTUtilities.getUserHomeDirectory(Environment.username) + "/release/current/bin/EST.sh"
					else:
						estPath = ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/EST.sh"

					if os.path.isfile(estPath):
						ESTUtilities.createLink(estPath, ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/EST.sh", Environment.username)
						if os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/EMT.sh"):
							os.unlink(ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/EMT.sh")


			# Rdir Runtime information
			if ESTUtilities.userExists(Environment.getRdirApplicationUserName()) and (Environment.username == Environment.getRdirApplicationUserName() or ESTUtilities.hasRootPermissions()):
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/version.txt"):
					with open(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/version.txt") as runtimeVersionFile:
						Environment.runtimeVersion = runtimeVersionFile.read().strip()
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/temp"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/temp", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/conf") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/conf"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/conf", Environment.getRdirApplicationUserName())

				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/JAVA") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/JAVA") and os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend/current/JAVA"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend/current/JAVA", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/JAVA", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/lib") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/lib") and os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend/current/lib"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend/current/lib", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/lib", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/scripts") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/scripts") and os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend/current/scripts"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend/current/scripts", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/scripts", Environment.getRdirApplicationUserName())

				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml") and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml.template"):
					shutil.copy(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml.template", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml")
					ESTUtilities.chown(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml", Environment.getRdirApplicationUserName(), Environment.getRdirApplicationUserName())
					ESTUtilities.manageTlsCertificateForTomcat(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/sslcert", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml", Environment.applicationName, False, 8080)
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/logs") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/logs"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/logs", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/logs", Environment.getRdirApplicationUserName())

			# Frontend Runtime information
			if ESTUtilities.userExists(Environment.getFrontendApplicationUserName()) and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/version.txt"):
					with open(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/version.txt") as runtimeVersionFile:
						Environment.runtimeVersion = runtimeVersionFile.read().strip()
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin", Environment.getFrontendApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release", Environment.getFrontendApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps", Environment.getFrontendApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/temp"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/temp", Environment.getFrontendApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/conf") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/conf"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/conf", Environment.getFrontendApplicationUserName())

				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/JAVA") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/JAVA") and os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current/JAVA"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current/JAVA", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/JAVA", Environment.getFrontendApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/lib") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/lib") and os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current/lib"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current/lib", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/lib", Environment.getFrontendApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/scripts") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/scripts") and os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current/scripts"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current/scripts", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/scripts", Environment.getFrontendApplicationUserName())

				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/server.xml") and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/server.xml.template"):
					shutil.copy(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/server.xml.template", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/server.xml")
					ESTUtilities.chown(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/server.xml", Environment.getFrontendApplicationUserName(), Environment.getFrontendApplicationUserName())
					ESTUtilities.manageTlsCertificateForTomcat(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/sslcert", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/server.xml", Environment.applicationName, False, 8080)
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/logs") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/logs"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/logs", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/logs", Environment.getFrontendApplicationUserName())

			# EMM information
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/emm") and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				Environment.isEmmFrontendServer = True
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName, Environment.getFrontendApplicationUserName())

				Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/classes/emm.license.xml"
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/classes/emm.properties"):
					Environment.frontendVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]

				if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current"):
					Environment.emmBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current"))
					if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
						Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]

			if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/manual") and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/manual/de"):
					manualApplicationPath = os.path.realpath(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/manual/de")
					manualVersion = ESTUtilities.getVersionFromFilename(manualApplicationPath)
					if manualVersion is not None:
						Environment.manualVersion = manualVersion
					else:
						Environment.manualVersion = "Unknown"
				else:
					Environment.manualVersion = "Unknown"

			if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/statistics") and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				Environment.isEmmStatisticsServer = True
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName, Environment.getFrontendApplicationUserName())

				Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/statistics/WEB-INF/classes/emm.license.xml"
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/statistics/WEB-INF/classes/emm.properties"):
					Environment.statisticsVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]

			if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/webservices") and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				Environment.isEmmWebservicesServer = True
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName, Environment.getFrontendApplicationUserName())

				Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/webservices/WEB-INF/classes/emm.license.xml"
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/webservices/WEB-INF/classes/emm.properties"):
					Environment.webservicesVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]

				if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current"):
					Environment.emmBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current"))
					if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
						Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]

			if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/rdir") and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				# Also check for the application "rdir" installed under user "console". This is indeed the configuration on some "Inhouse" customer systems.
				Environment.isEmmConsoleRdirServer = True
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName, Environment.getFrontendApplicationUserName())

				Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.license.xml"
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.properties"):
					Environment.consoleRdirVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]

				if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current"):
					Environment.emmBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/current"))
					if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
						Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]

			if ESTUtilities.userExists(Environment.getRdirApplicationUserName()) \
					and (Environment.username == Environment.getRdirApplicationUserName() or ESTUtilities.hasRootPermissions()) \
					and os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/rdir"):
				Environment.isEmmRdirServer = True
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin/" + Environment.restartToolName, Environment.getRdirApplicationUserName())

				if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/version.txt"):
					with open(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/version.txt") as runtimeVersionFile:
						Environment.rdirRuntimeVersion = runtimeVersionFile.read().strip()

				Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.license.xml"
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.properties"):
					Environment.rdirVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]

				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/temp"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/temp", Environment.getRdirApplicationUserName())
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/conf") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/conf"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/conf", Environment.getRdirApplicationUserName())
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml") and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml.template"):
					shutil.copy(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml.template", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml")
					ESTUtilities.chown(ESTUtilities.getUserHomeDirectory("openemm") + "/tomcat/conf/server.xml", Environment.getRdirApplicationUserName(), Environment.getRdirApplicationUserName())
					ESTUtilities.manageTlsCertificateForTomcat(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/sslcert", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/conf/server.xml", Environment.applicationName)
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/logs") and not os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/logs"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/logs", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/logs", Environment.getRdirApplicationUserName())
				if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend/current"):
					Environment.rdirBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend/current"))
					if Environment.rdirBackendVersion is not None and Environment.rdirBackendVersion.startswith("V"):
						Environment.rdirBackendVersion = Environment.rdirBackendVersion[1:len(Environment.rdirBackendVersion)]

			if ESTUtilities.userExists("merger") and (Environment.username == "merger" or ESTUtilities.hasRootPermissions()):
				Environment.isEmmMergerServer = True
				if not os.path.islink(ESTUtilities.getUserHomeDirectory("merger") + "/bin"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("merger") + "/release/current/bin", ESTUtilities.getUserHomeDirectory("merger") + "/bin", "merger")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory("merger") + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("merger") + "/release", "merger")
				if os.path.isdir(ESTUtilities.getUserHomeDirectory("merger") + "/release/current"):
					Environment.mergerBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory("merger") + "/release/current"))
					if Environment.mergerBackendVersion is not None and Environment.mergerBackendVersion.startswith("V"):
						Environment.mergerBackendVersion = Environment.mergerBackendVersion[1:len(Environment.mergerBackendVersion)]
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if ESTUtilities.userExists("mailout") and (Environment.username == "mailout" or ESTUtilities.hasRootPermissions()):
				Environment.isEmmMailerServer = True
				if not os.path.islink(ESTUtilities.getUserHomeDirectory("mailout") + "/bin"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("mailout") + "/release/current/bin", ESTUtilities.getUserHomeDirectory("mailout") + "/bin", "mailout")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory("mailout") + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("mailout") + "/release", "mailout")
				if os.path.isdir(ESTUtilities.getUserHomeDirectory("mailout") + "/release/current"):
					Environment.mailerBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory("mailout") + "/release/current"))
					if Environment.mailerBackendVersion is not None and Environment.mailerBackendVersion.startswith("V"):
						Environment.mailerBackendVersion = Environment.mailerBackendVersion[1:len(Environment.mailerBackendVersion)]
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if ESTUtilities.userExists("mailloop") and (Environment.username == "mailloop" or ESTUtilities.hasRootPermissions()):
				Environment.isEmmMailloopServer = True
				if not os.path.islink(ESTUtilities.getUserHomeDirectory("mailloop") + "/bin"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("mailloop") + "/release/current/bin", ESTUtilities.getUserHomeDirectory("mailloop") + "/bin", "mailloop")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory("mailloop") + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("mailloop") + "/release", "mailloop")
				if os.path.isdir(ESTUtilities.getUserHomeDirectory("mailloop") + "/release/current"):
					Environment.mailloopBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory("mailloop") + "/release/current"))
					if Environment.mailloopBackendVersion is not None and Environment.mailloopBackendVersion.startswith("V"):
						Environment.mailloopBackendVersion = Environment.mailloopBackendVersion[1:len(Environment.mailloopBackendVersion)]
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if DbConnector.dbcfgEntry is None and Environment.applicationDbcfgEntryDefaultName in DbConnector.dbcfgProperties:
				DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[Environment.applicationDbcfgEntryDefaultName]

		if os.path.isdir("/root/release/current"):
			Environment.runtimeBackendVersion = os.path.basename(os.path.realpath("/root/release/current"))
			if Environment.runtimeBackendVersion is not None and Environment.runtimeBackendVersion.startswith("V"):
				Environment.runtimeBackendVersion = Environment.runtimeBackendVersion[1:len(Environment.runtimeBackendVersion)]

		if not Environment.isOpenEmmServer \
			and not Environment.isEmmFrontendServer and not Environment.isEmmStatisticsServer and not Environment.isEmmWebservicesServer \
			and not Environment.isEmmConsoleRdirServer and not Environment.isEmmRdirServer \
			and not Environment.isEmmMergerServer and not Environment.isEmmMailerServer and not Environment.isEmmMailloopServer:
				Environment.errors.append("No " + Environment.applicationName + " application is currently installed")

	@staticmethod
	def getSystemUrl():
		try:
			if (Environment.systemUrl is None or Environment.systemUrl == "Unknown") and DbConnector.checkDbConnection():
				Environment.systemUrl = DbConnector.selectValue("SELECT value FROM config_tbl WHERE class = 'system' AND name = 'url' AND (hostname IS NULL OR TRIM(hostname) = '' OR hostname = ?)", Environment.hostname)

			if "[to be defined]" in Environment.systemUrl or Environment.systemUrl.strip() == "":
				Environment.systemUrl = None

			if Environment.systemUrl is None:
				return "Unknown"
			else:
				return Environment.systemUrl
		except:
			return "Unknown"

	@staticmethod
	def checkMigration():
		try:
			# To be removed in near future: GWUA-5514
			if DbConnector.checkDbConnection() and DbConnector.checkColumnExists("workflow_tbl", "is_legacy_mode"):
				problematicInActiveWorkflows = DbConnector.select("SELECT shortname, company_id, workflow_id FROM workflow_tbl WHERE status != 2 AND workflow_id IN (SELECT workflow_id FROM workflow_reaction_tbl WHERE is_legacy_mode = 1)")
				if len(problematicInActiveWorkflows) > 0:
					listOfWorkflowNames = ""
					for row in problematicInActiveWorkflows:
						shortname = row[0]
						companyID = row[1]
						workflowID = row[2]
						if len(listOfWorkflowNames) > 0:
							listOfWorkflowNames = listOfWorkflowNames + ", "
						listOfWorkflowNames = listOfWorkflowNames + "\"" + shortname + "\" (clientID: " + str(companyID) + ", workflowID: " + str(workflowID) + ")"
					Environment.warnings.append("Your " + Environment.applicationName + " database contains inactive legacy workflows " + listOfWorkflowNames + ". Please delete those workflows since they will no longer work with " + Environment.applicationName + " 23.10 and later.")

				problematicActiveWorkflows = DbConnector.select("SELECT shortname, company_id, workflow_id FROM workflow_tbl WHERE status = 2 AND workflow_id IN (SELECT workflow_id FROM workflow_reaction_tbl WHERE is_legacy_mode = 1)")
				if len(problematicActiveWorkflows) > 0:
					listOfWorkflowNames = ""
					for row in problematicActiveWorkflows:
						shortname = row[0]
						companyID = row[1]
						workflowID = row[2]
						if len(listOfWorkflowNames) > 0:
							listOfWorkflowNames = listOfWorkflowNames + ", "
						listOfWorkflowNames = listOfWorkflowNames + "\"" + shortname + "\" (clientID: " + str(companyID) + ", workflowID: " + str(workflowID) + ")"
					Environment.warnings.append("Your " + Environment.applicationName + " database contains active legacy workflows " + listOfWorkflowNames + " which can not be migrated. Please rebuild these workflows from scratch and delete the legacy workflows since they will no longer work with " + Environment.applicationName + " 23.10 and later. If you need help, please contact support.")

			oldFormDoLinks = DbConnector.selectValue("SELECT COUNT(*) FROM rdir_url_tbl WHERE full_url like '%/form.do%' AND deleted = 0 AND mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE deleted = 0)")
			if oldFormDoLinks is not None and oldFormDoLinks > 0:
				Environment.warnings.append("Your " + Environment.applicationName + " database contains userform links in mailings of the old '.../form.do?...' format. Please switch those to the new format '.../form.action?...'. If you need help, please contact support.")
		except Exception as e:
			if ESTUtilities.isDebugMode():
				logging.exception("Cannot check migration")
			if hasattr(e, 'message'):
				Environment.errors.append("Cannot check migration: " + str(e.message))
			else:
				Environment.errors.append("Cannot check migration")

	@staticmethod
	def getFrontendApplicationUserName():
		if Environment.frontendApplicationUsername is None:
			Environment.refreshUsernameCaches()
		return Environment.frontendApplicationUsername

	@staticmethod
	def getRdirApplicationUserName():
		if Environment.rdirApplicationUsername is None:
			Environment.refreshUsernameCaches()
		return Environment.rdirApplicationUsername

	@staticmethod
	def getMergerApplicationUserName():
		if Environment.mergerApplicationUsername is None:
			Environment.refreshUsernameCaches()
		return Environment.mergerApplicationUsername

	@staticmethod
	def getMailerApplicationUserName():
		if Environment.mailerApplicationUsername is None:
			Environment.refreshUsernameCaches()
		return Environment.mailerApplicationUsername

	@staticmethod
	def getMailloopApplicationUserName():
		if Environment.mailloopApplicationUsername is None:
			Environment.refreshUsernameCaches()
		return Environment.mailloopApplicationUsername

	@staticmethod
	def refreshUsernameCaches():
		if "OpenEMM" == Environment.applicationName:
			if ESTUtilities.userExists("openemm"):
				Environment.frontendApplicationUsername = "openemm"
				Environment.rdirApplicationUsername = "openemm"
				Environment.mergerApplicationUsername = "openemm"
				Environment.mailerApplicationUsername = "openemm"
				Environment.mailloopApplicationUsername = "openemm"
			else:
				raise Exception("OMT needs an operating system user called 'openemm'. Please create one to execute OMT and maintain your OpenEMM installation")
		else:
			if ESTUtilities.hasRootPermissions():
				if ESTUtilities.userExists("console"):
					Environment.frontendApplicationUsername = "console"
				else:
					Environment.frontendApplicationUsername = Environment.username
			else:
				Environment.frontendApplicationUsername = Environment.username

			if ESTUtilities.hasRootPermissions():
				if ESTUtilities.userExists("rdir"):
					Environment.rdirApplicationUsername = "rdir"
				elif ESTUtilities.userExists("console"):
					Environment.rdirApplicationUsername = "console"
				else:
					Environment.rdirApplicationUsername = Environment.username
			else:
				Environment.rdirApplicationUsername = Environment.username

			if ESTUtilities.hasRootPermissions():
				if ESTUtilities.userExists("merger"):
					Environment.mergerApplicationUsername = "merger"
				else:
					Environment.mergerApplicationUsername = Environment.username
			else:
				Environment.mergerApplicationUsername = Environment.username

			if ESTUtilities.hasRootPermissions():
				for name in 'mailout', 'mailgun':
					if ESTUtilities.userExists(name):
						Environment.mailerApplicationUsername = name
						break
				else:
					Environment.mailerApplicationUsername = Environment.username
			else:
				Environment.mailerApplicationUsername = Environment.username

			if ESTUtilities.hasRootPermissions():
				if ESTUtilities.userExists("mailloop"):
					Environment.mailloopApplicationUsername = "mailloop"
				else:
					Environment.mailloopApplicationUsername = Environment.username
			else:
				Environment.mailloopApplicationUsername = Environment.username

	@staticmethod
	def getOpenEmmBackendStatusList():
		statusCheckTool = None
		statusCheckToolUsername = Environment.getMailerApplicationUserName()
		if os.path.isfile(ESTUtilities.getUserHomeDirectory(statusCheckToolUsername) + "/bin/backend.sh"):
			statusCheckTool = ESTUtilities.getUserHomeDirectory(statusCheckToolUsername) + "/bin/backend.sh"
		else:
			statusCheckToolUsername = Environment.getMergerApplicationUserName()
			if os.path.isfile(ESTUtilities.getUserHomeDirectory(statusCheckToolUsername) + "/bin/backend.sh"):
				statusCheckTool = ESTUtilities.getUserHomeDirectory(statusCheckToolUsername) + "/bin/backend.sh"
			else:
				statusCheckToolUsername = Environment.getMailloopApplicationUserName()
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(statusCheckToolUsername) + "/bin/backend.sh"):
					statusCheckTool = ESTUtilities.getUserHomeDirectory(statusCheckToolUsername) + "/bin/backend.sh"

		if statusCheckTool is not None:
			try:
				# Text output comes via stderr
				if ESTUtilities.hasRootPermissions():
					processOutput = subprocess.check_output("su -c \"" + statusCheckTool + " status\" - " + statusCheckToolUsername, stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				else:
					processOutput = subprocess.check_output(statusCheckTool + " status", stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				if ESTUtilities.isBlank(processOutput):
					return None
				else:
					if len(processOutput) > 0:
						backendStates = {}
						for processOutputLine in processOutput.splitlines():
							items = processOutputLine.split(":")
							if len(items) > 1:
								state = items[1].strip()
								if "(" in state:
									state = state[0:state.index("(")].strip()
								if not state in backendStates:
									backendStates[state] = []
								backendStates[state].append(items[0].strip())
						return backendStates
					else:
						return None
			except subprocess.CalledProcessError as e:
				print(e)
				if e.returncode == 1:
					return None
				else:
					if ESTUtilities.isDebugMode():
						logging.exception("Error while checking for Backend running")
					return None
			except:
				if ESTUtilities.isDebugMode():
					logging.exception("Error while checking for Backend running")
				return None
		else:
			return None

	@staticmethod
	def getMailerStatusList():
		mailerUserName = Environment.getMailerApplicationUserName()
		statusCheckTool = None
		if os.path.isfile(ESTUtilities.getUserHomeDirectory(mailerUserName) + "/bin/mailer.sh"):
			statusCheckTool = ESTUtilities.getUserHomeDirectory(mailerUserName) + "/bin/mailer.sh"
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory(mailerUserName) + "/bin/backend.sh"):
			statusCheckTool = ESTUtilities.getUserHomeDirectory(mailerUserName) + "/bin/backend.sh"

		if statusCheckTool is not None:
			try:
				# Text output comes via stderr
				if ESTUtilities.hasRootPermissions():
					processOutput = subprocess.check_output("su -c \"" + statusCheckTool + " status\" - " + mailerUserName, stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				else:
					processOutput = subprocess.check_output(statusCheckTool + " status", stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				if ESTUtilities.isBlank(processOutput):
					return None
				else:
					if len(processOutput) > 0:
						backendStates = {}
						for processOutputLine in processOutput.splitlines():
							items = processOutputLine.split(":")
							if len(items) > 1:
								state = items[1].strip()
								if "(" in state:
									state = state[0:state.index("(")].strip()
								if not state in backendStates:
									backendStates[state] = []
								backendStates[state].append(items[0].strip())
						return backendStates
					else:
						return None
			except subprocess.CalledProcessError as e:
				print(e)
				if e.returncode == 1:
					return None
				else:
					if ESTUtilities.isDebugMode():
						logging.exception("Error while checking for Backend running")
					return None
			except:
				if ESTUtilities.isDebugMode():
					logging.exception("Error while checking for Backend running")
				return None
		else:
			return None
	@staticmethod
	def getApplicationUsername():
		if Environment.isOpenEmmServer:
			return "openemm"
		elif Environment.isEmmRdirServer:
			return Environment.getRdirApplicationUserName()
		elif Environment.isEmmMergerServer:
			return "merger"
		elif Environment.isEmmMailerServer:
			return "mailout"
		elif Environment.isEmmMailloopServer:
			return "mailloop"
		else:
			return Environment.getFrontendApplicationUserName()

	@staticmethod
	def getMergerStatusList():
		mergerUserName = Environment.getMergerApplicationUserName()
		statusCheckTool = None
		if os.path.isfile(ESTUtilities.getUserHomeDirectory(mergerUserName) + "/bin/merger.sh"):
			statusCheckTool = ESTUtilities.getUserHomeDirectory(mergerUserName) + "/bin/merger.sh"
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory(mergerUserName) + "/bin/backend.sh"):
			statusCheckTool = ESTUtilities.getUserHomeDirectory(mergerUserName) + "/bin/backend.sh"

		if statusCheckTool is not None:
			try:
				# Text output comes via stderr
				if ESTUtilities.hasRootPermissions():
					processOutput = subprocess.check_output("su -c \"" + statusCheckTool + " status\" - " + mergerUserName, stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				else:
					processOutput = subprocess.check_output(statusCheckTool + " status", stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				if ESTUtilities.isBlank(processOutput):
					return None
				else:
					if len(processOutput) > 0:
						backendStates = {}
						for processOutputLine in processOutput.splitlines():
							items = processOutputLine.split(":")
							if len(items) > 1:
								state = items[1].strip()
								if "(" in state:
									state = state[0:state.index("(")].strip()
								if not state in backendStates:
									backendStates[state] = []
								backendStates[state].append(items[0].strip())
						return backendStates
					else:
						return None
			except subprocess.CalledProcessError as e:
				print(e)
				if e.returncode == 1:
					return None
				else:
					if ESTUtilities.isDebugMode():
						logging.exception("Error while checking for Backend running")
					return None
			except:
				if ESTUtilities.isDebugMode():
					logging.exception("Error while checking for Backend running")
				return None
		else:
			return None

	@staticmethod
	def getMailloopStatusList():
		mailloopUserName = Environment.getMailloopApplicationUserName()
		statusCheckTool = None
		if os.path.isfile(ESTUtilities.getUserHomeDirectory(mailloopUserName) + "/bin/mailloop.sh"):
			statusCheckTool = ESTUtilities.getUserHomeDirectory(mailloopUserName) + "/bin/mailloop.sh"
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory(mailloopUserName) + "/bin/backend.sh"):
			statusCheckTool = ESTUtilities.getUserHomeDirectory(mailloopUserName) + "/bin/backend.sh"

		if statusCheckTool is not None:
			try:
				# Text output comes via stderr
				if ESTUtilities.hasRootPermissions():
					processOutput = subprocess.check_output("su -c \"" + statusCheckTool + " status\" - " + mailloopUserName, stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				else:
					processOutput = subprocess.check_output(statusCheckTool + " status", stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				if ESTUtilities.isBlank(processOutput):
					return None
				else:
					if len(processOutput) > 0:
						backendStates = {}
						for processOutputLine in processOutput.splitlines():
							items = processOutputLine.split(":")
							if len(items) > 1:
								state = items[1].strip()
								if "(" in state:
									state = state[0:state.index("(")].strip()
								if not state in backendStates:
									backendStates[state] = []
								backendStates[state].append(items[0].strip())
						return backendStates
					else:
						return None
			except subprocess.CalledProcessError as e:
				print(e)
				if e.returncode == 1:
					return None
				else:
					if ESTUtilities.isDebugMode():
						logging.exception("Error while checking for Backend running")
					return None
			except:
				if ESTUtilities.isDebugMode():
					logging.exception("Error while checking for Backend running")
				return None
		else:
			return None

	@staticmethod
	def checkForMandatoryMariaDBPackages(osVendor):
		if not ESTUtilities.checkPackageIsInstalled(osVendor, "MariaDB-server") and not ESTUtilities.checkPackageIsInstalled(osVendor, "mariadb-server"):
			Environment.warnings.append("Mandatory system package 'MariaDB-server' (or lower case) is not installed")
		if not ESTUtilities.checkPackageIsInstalled(osVendor, "MariaDB-common") and not ESTUtilities.checkPackageIsInstalled(osVendor, "mariadb-common"):
			Environment.warnings.append("Mandatory system package 'MariaDB-common' (or lower case) is not installed")
		if not ESTUtilities.checkPackageIsInstalled(osVendor, "MariaDB-client") and not ESTUtilities.checkPackageIsInstalled(osVendor, "mariadb-client"):
			Environment.warnings.append("Mandatory system package 'MariaDB-client' (or lower case) is not installed")
		if not ESTUtilities.checkPackageIsInstalled(osVendor, "MariaDB-shared") and not ESTUtilities.checkPackageIsInstalled(osVendor, "mariadb-shared"):
			Environment.warnings.append("Mandatory system package 'MariaDB-shared' (or lower case) is not installed")
		if not ESTUtilities.checkPackageIsInstalled(osVendor, "MariaDB-devel") and not ESTUtilities.checkPackageIsInstalled(osVendor, "mariadb-devel"):
			Environment.warnings.append("Mandatory system package 'MariaDB-devel' (or lower case) is not installed")
		if not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "mariadb-connector-c-devel") and not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "MariaDB-devel") and not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "mariadb-devel"):
			Environment.warnings.append("Mandatory system package 'mariadb-connector-c-devel' is not installed")

	@staticmethod
	def printApplicationMapping(application_mapping: dict):
		for identifier, application in application_mapping.items():
			output = application.get('output')
			if application.get("condition"):
				print(f"EMM {identifier} Version: {output}")
			elif alternative_name := application.get("alternativ_name_if_error"):
				print(f"EMM {alternative_name} Version: {output}")

	@staticmethod
	def printApplicationVersion():
		## "application_name": [version, check_if_applicable_to_show]
		condition_any_frontend_based_server = Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer
		condition_current_user_is_frontend_app_user = Environment.username == Environment.getFrontendApplicationUserName()

		frontend_application_mapping = {
			"Runtime": {
				"output" : Environment.runtimeVersion,
				"condition" : condition_any_frontend_based_server or condition_current_user_is_frontend_app_user
			},
			"Frontend": {
				"output": Environment.frontendVersion,
				"condition": Environment.isEmmFrontendServer or condition_current_user_is_frontend_app_user
			},
			"Statistics": {
				"output": Environment.statisticsVersion,
				"condition": Environment.isEmmStatisticsServer
			},
			"Webservices": {
				"output": Environment.webservicesVersion,
				"condition": Environment.isEmmWebservicesServer
			},
			"Console-Rdir": {
				"output": Environment.consoleRdirVersion,
				"condition": Environment.isEmmConsoleRdirServer
			},
			"Backend": {
				"output": Environment.emmBackendVersion,
				"condition": condition_any_frontend_based_server or condition_current_user_is_frontend_app_user
			},
			"Rdir Runtime": {
				"output": Environment.rdirRuntimeVersion,
				"condition": Environment.isEmmRdirServer and condition_any_frontend_based_server,
				"alternative_name_if_error": "Runtime"
			},
			"Rdir": {
				"output": Environment.rdirRuntimeVersion,
				"condition":  Environment.isEmmRdirServer
			},
			"Rdir Backend": {
				"output": Environment.rdirBackendVersion,
				"condition":  Environment.isEmmRdirServer and bool(Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer),
				"alternative_name_if_error": "Backend"
			}
		}

		backend_application_mapping = {
			"Runtime Backend": {
				"output": Environment.runtimeBackendVersion,
				"condition": Environment.runtimeBackendVersion is not None
			},
			"Merger Backend": {
				"output": Environment.mergerBackendVersion,
				"condition": Environment.isEmmMergerServer
			},
			"Mailer Backend": {
				"output": Environment.mailerBackendVersion,
				"condition": Environment.isEmmMailerServer
			},
			"Mailloop Backend": {
				"output": Environment.mailloopBackendVersion,
				"condition": Environment.isEmmMailloopServer
			},
		}

		Environment.printApplicationMapping(frontend_application_mapping)
		Environment.printApplicationMapping(backend_application_mapping)




	@staticmethod
	def check_tomcat_process_for_user(username: str):
		try:
			cmd = ['ps', '-eo', 'user,pid,stime,etime,cmd', '--width', '1000']
			output = subprocess.check_output(cmd, text=True)
			lines = output.strip().split('\n')
			matching = {}
			for line in lines[1:]:
				if username in line and 'org.apache.catalina' in line:
					parts = line.split(maxsplit=4)
					if len(parts) >= 5:
						user, pid, start_time, elapsed, command = parts
						matching = {
							'user': user,
							'pid': pid,
							'start_time': start_time,
							'elapsed': elapsed,
							'command': command
						}
			return matching
		except subprocess.CalledProcessError as e:
			print(f"Error: {e}")
			return False

	@staticmethod
	def logState():
		now = datetime.datetime.now()
		nowString = now.strftime("%Y-%m-%d_%H-%M-%S")

		if ESTUtilities.hasRootPermissions():
			pathlib.Path('/root/release/log').mkdir(parents=True, exist_ok=True)
			logfilePath = "/root/release/log/crash_" + nowString + ".log"
		else:
			pathlib.Path(ESTUtilities.getUserHomeDirectory(Environment.username) +"/release/log").mkdir(parents=True, exist_ok=True)
			logfilePath = ESTUtilities.getUserHomeDirectory(Environment.username) + "/release/log/crash_" + nowString + ".log"

		with open(logfilePath, "w", encoding="UTF-8") as logfile:
			logfile.write("Crash occurred, Environment Variables at runtime: \n" + ', \n'.join(f"{k}: {v}" for k, v in vars(Environment).items()))
		print(Colors.RED + "Crash occurred. Logs can be found at: " + logfilePath + Colors.DEFAULT)

	@staticmethod
	def checkForMissingPuppeteerRequirements():
		Vendors = "Rhel" if Environment.osVendor in ["Alma", "CentOS", "RedHat"] else Environment.osVendor
		return list(filter(lambda package: not ESTUtilities.checkPackageIsInstalled(Environment.osVendor,package), Environment.puppeteerRequirements.get(Vendors,list())))