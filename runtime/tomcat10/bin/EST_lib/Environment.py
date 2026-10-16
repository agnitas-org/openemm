import datetime
import pathlib
import sys
import os
import shutil
import logging
import subprocess
from pathlib import Path
from typing import Callable

from EST_lib import Colors
from EST_lib import DbConnector
from EST_lib import ESTUtilities
from EST_lib.License import License

logger = logging.getLogger(__name__)

class Environment:
	################################################
	# OpenEMM and EMM specific settings start here #
	################################################

	toolVersion = "26.11.053" #Dummy version number. This is going to be replaced by build tools.
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
	dbClientPostgresSearchPaths = None
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

	osVendor = "Unknown"
	osVersion = "Unknown"

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
	applicationVersions = None

	isOpenEmmServer = False

	isEmmFrontendServer = False

	isEmmStatisticsServer = False

	isEmmWebservicesServer = False

	isEmmConsoleRdirServer = False

	isEmmRdirServer = False


	isEmmMergerServer = False

	isEmmMailerServer = False

	isEmmMailloopServer = False
	
	isEmmUnifiedServer = False
	unifiedBackendVersion = "Unknown"


	overrideNextMenu = None
	configTableMenu = None

	numberOfMajorMinorVersionsToKeep = 2
	updateVersionHotfixVersionsToKeep = 2

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
		Environment.determineOS()

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
					if Path("/etc/systemd/user.conf").is_file():
						print("Changing /etc/systemd/user.conf")
						os.system("echo 'DefaultLimitNOFILE=" + str(Environment.noFileLimit) + "' | sudo tee --append /etc/systemd/user.conf > /dev/null")
					if Path("/etc/systemd/system.conf").is_file():
						print("Changing /etc/systemd/system.conf")
						os.system("echo 'DefaultLimitNOFILE=" + str(Environment.noFileLimit) + "' | sudo tee --append /etc/systemd/system.conf > /dev/null")

					print(Colors.RED + "To let this changes take effect the system needs to be rebooted" + Colors.DEFAULT)
				except:
					print(Colors.RED + "Change of system value for maximum parallel files open (= ulimit) was not successful." + Colors.DEFAULT)
					if ESTUtilities.isDebugMode():
						logger.exception("ulimit change was not successful")
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

		if not Path(DbConnector.dbcfgPropertiesFilePath).is_file():
			if ESTUtilities.anyUserExists(("openemm",Environment.getFrontendApplicationUserName(),Environment.getRdirApplicationUserName(),"merger", "mailloop")):
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

		if DbConnector.dbcfgProperties and not DbConnector.dbcfgEntry and Environment.applicationDbcfgEntryDefaultName in DbConnector.dbcfgProperties:
			DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[Environment.applicationDbcfgEntryDefaultName]

		if DbConnector.dbcfgProperties and DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties:
			dbVendor = DbConnector.dbcfgEntry.get("dbms")
			if "oracle" == dbVendor.lower().strip():
				DbConnector.emmDbVendor = "oracle"
			elif "mariadb" == dbVendor.lower().strip():
				DbConnector.emmDbVendor = "mariadb"
			elif "postgres" == dbVendor.lower().strip():
				DbConnector.emmDbVendor = "postgres"
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

			if DbConnector.dbcfgProperties and DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties:
				dbVendor = DbConnector.dbcfgEntry["dbms"]
				if "oracle" == dbVendor.lower().strip():
					DbConnector.emmDbVendor = "oracle"
				elif "mariadb" == dbVendor.lower().strip():
					DbConnector.emmDbVendor = "mariadb"
				elif "postgres" == dbVendor.lower().strip():
					DbConnector.emmDbVendor = "postgres"
				else:
					Environment.errors.append("Invalid database vendor in dbcfg: " + dbVendor.lower())
			elif ESTUtilities.userExists("openemm") or ESTUtilities.userExists(Environment.getFrontendApplicationUserName()) or ESTUtilities.userExists(Environment.getRdirApplicationUserName()) or ESTUtilities.userExists("merger") or ESTUtilities.userExists("mailloop"):
				Environment.errors.append("Invalid DbcfgEntryName defined: " + DbConnector.applicationDbcfgEntryName)
		
		if DbConnector.emmDbVendor == "mariadb":
			Environment.checkForMandatoryMariaDBPackages(Environment.osVendor)

		if sys.version_info[0] < 3 or (sys.version_info[0] == 3 and sys.version_info[1] < 8):
			print("This python installation (" + str(sys.version_info[0]) + "." + str(sys.version_info[1]) + "." + str(sys.version_info[2]) + ") does not support the needed database drivers.\nPlease install at least python version 3.8 or above")
			sys.exit(1)
		elif not "oracle" in Environment.allowedDbmsSystems and not "postgres" in Environment.allowedDbmsSystems and not DbConnector.isMariadbDriverModuleAvailable():
			installCommandMariadbPythonModule = "sudo " + Environment.getPythonCommand() + " -m pip install mariadb"

			if Environment.osVendor == "Suse":
				print("The mandatory database vendor MariaDB is not supported by this python installation.\nPlease install mandatory driver via")
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
					print("The mandatory database vendor MariaDB is not supported by this python installation.\nPlease install mandatory driver manually")
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
										logger.exception("Error")
									sys.exit(1)
							else:
								print(Colors.RED + "Installation of MariaDB was not successful" + Colors.DEFAULT)
								if ESTUtilities.isDebugMode():
									logger.exception("Error")
								sys.exit(1)

						try:
							os.system(installCommandMariadbPythonModule)
						except:
							print(Colors.RED + "Installation of MariaDB-python was not successful" + Colors.DEFAULT)
							if ESTUtilities.isDebugMode():
								logger.exception("Error")
							sys.exit(1)

						DbConnector.recheckMariadbDriverModuleAvailability()

						if not DbConnector.isMariadbDriverModuleAvailable():
							print(Colors.RED + "The mandatory database vendor MariaDB is not supported by this python installation.\nPlease install mandatory driver via")
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
					print("The mandatory database vendor MariaDB is not supported by this python installation.\nPlease install mandatory driver manually")
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
										logger.exception("Error")
									sys.exit(1)
							else:
								print(Colors.RED + "Installation of MariaDB was not successful" + Colors.DEFAULT)
								if ESTUtilities.isDebugMode():
									logger.exception("Error")
								sys.exit(1)

						try:
							os.system(installCommandMariadbPythonModule)
						except:
							print(Colors.RED + "Installation of MariaDB-python was not successful" + Colors.DEFAULT)
							if ESTUtilities.isDebugMode():
								logger.exception("Error")
							sys.exit(1)

						if not DbConnector.recheckMariadbDriverModuleAvailability():
							print(Colors.RED + "The mandatory database vendor MariaDB is not supported by this python installation.\nPlease install mandatory driver via")
							print("  '" + installCommandMariadb + "'")
							print("  '" + installCommandMariadbPythonModule + "'")
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
					logger.exception("Cannot create initial setenv.sh file")
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
					logger.exception("Cannot create initial setenv.sh file")
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

		DbConnector.detectDbClientPath(DbConnector.emmDbVendor, Environment.dbClientOracleSearchPaths, Environment.dbClientMariadbSearchPaths, Environment.dbClientPostgresSearchPaths)

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
				if not DbConnector.checkDbExists():
					Environment.errors.append("Required Database does not seem to exist. (Maybe database name for " + Environment.applicationName + " is malformed or not yet configured)")
				else:
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

		Environment.applicationVersions = ApplicationVersions()
		Environment.checkMigration()

	@staticmethod
	def readApplicationValues():
		# Check application types
		if "OpenEMM" in Environment.applicationName and ESTUtilities.userExists("openemm"):
			Environment.isOpenEmmServer = True
			openemmHome = Path(ESTUtilities.getUserHomeDirectory("openemm"))
			openemmNeededDirectories = ["bin", "release", "webapps", "temp", ]
			openemmNeededSymlinks = {
				"conf": "tomcat/conf",
				"logs": "tomcat/logs",
				"JAVA": "/release/backend/current/JAVA",
				"lib": "release/backend/current/lib",
				"scripts": "/release/backend/current/scripts",
			}
			
			for subDirectory in openemmNeededDirectories:
				if not (openemmDirectory := openemmHome / subDirectory).is_dir():
					ESTUtilities.createDirectory(openemmDirectory, "openemm")

			for symlink, targetDirectory in openemmNeededSymlinks.items():
				symlinkPath = openemmHome / symlink
				targetDirectoryPath = openemmHome / targetDirectory
				if not symlinkPath.exists() and targetDirectoryPath.exists():
					symlinkPath.symlink_to(targetDirectoryPath)

			serverXMLPath = openemmHome / "tomcat/conf/server.xml"
			serverXMLTemplatePath = openemmHome / "/tomcat/conf/server.xml.template"
			if not serverXMLPath.is_file() and serverXMLTemplatePath.is_file():
				shutil.copy(serverXMLTemplatePath, serverXMLPath)
				ESTUtilities.chown(serverXMLPath, "openemm", "openemm")
				ESTUtilities.manageTlsCertificateForTomcat(openemmHome / "etc/ssl", serverXMLPath, Environment.applicationName, False, 8080)

			restartToolPath = openemmHome /"bin" / Environment.restartToolName
			restartToolFullPath = openemmHome / "tomcat/bin" / Environment.restartToolName
			if not restartToolPath.exists() and restartToolFullPath.is_file():
				restartToolPath.symlink_to(restartToolFullPath)
			Environment.emmLicenseFilePath = openemmHome / "/webapps/emm/WEB-INF/classes/emm.license.xml"

		else:
			if ESTUtilities.hasRootPermissions():
				if not os.path.isfile("/usr/local/bin/EST.sh") and not os.path.islink("/usr/local/bin/EST.sh") and Environment.scriptFilePath.startswith("/home/") and len(Environment.scriptFilePath) > 6:
					scriptHomeUser = Environment.scriptFilePath[6:]
					scriptHomeUser = scriptHomeUser.partition("/")[0]
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

			if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/manual") and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/manual/de"):
					manualApplicationPath = os.path.realpath(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/manual/de")

			if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/statistics") and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				Environment.isEmmStatisticsServer = True
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName, Environment.getFrontendApplicationUserName())

				Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/statistics/WEB-INF/classes/emm.license.xml"

			if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/webservices") and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				Environment.isEmmWebservicesServer = True
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName, Environment.getFrontendApplicationUserName())

				Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/webservices/WEB-INF/classes/emm.license.xml"

			if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/rdir") and (Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
				# Also check for the application "rdir" installed under user "console". This is indeed the configuration on some "Inhouse" customer systems.
				Environment.isEmmConsoleRdirServer = True
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/" + Environment.restartToolName, Environment.getFrontendApplicationUserName())

				Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.license.xml"

			if ESTUtilities.userExists(Environment.getRdirApplicationUserName()) \
					and (Environment.username == Environment.getRdirApplicationUserName() or ESTUtilities.hasRootPermissions()) \
					and os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/rdir"):
				Environment.isEmmRdirServer = True
				if not os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin/" + Environment.restartToolName, Environment.getRdirApplicationUserName())

				Environment.emmLicenseFilePath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.license.xml"

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

			if ESTUtilities.userExists("merger") and (Environment.username == "merger" or ESTUtilities.hasRootPermissions()):
				Environment.isEmmMergerServer = True
				if not os.path.islink(ESTUtilities.getUserHomeDirectory("merger") + "/bin"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("merger") + "/release/current/bin", ESTUtilities.getUserHomeDirectory("merger") + "/bin", "merger")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory("merger") + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("merger") + "/release", "merger")
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if ESTUtilities.userExists("mailout") and (Environment.username == "mailout" or ESTUtilities.hasRootPermissions()):
				Environment.isEmmMailerServer = True
				if not os.path.islink(ESTUtilities.getUserHomeDirectory("mailout") + "/bin"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("mailout") + "/release/current/bin", ESTUtilities.getUserHomeDirectory("mailout") + "/bin", "mailout")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory("mailout") + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("mailout") + "/release", "mailout")
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if ESTUtilities.userExists("mailloop") and (Environment.username == "mailloop" or ESTUtilities.hasRootPermissions()):
				Environment.isEmmMailloopServer = True
				if not os.path.islink(ESTUtilities.getUserHomeDirectory("mailloop") + "/bin"):
					ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory("mailloop") + "/release/current/bin", ESTUtilities.getUserHomeDirectory("mailloop") + "/bin", "mailloop")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory("mailloop") + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("mailloop") + "/release", "mailloop")
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if DbConnector.dbcfgEntry is None and Environment.applicationDbcfgEntryDefaultName in DbConnector.dbcfgProperties:
				DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[Environment.applicationDbcfgEntryDefaultName]

		if not Environment.isOpenEmmServer \
			and not Environment.isEmmFrontendServer and not Environment.isEmmStatisticsServer and not Environment.isEmmWebservicesServer \
			and not Environment.isEmmConsoleRdirServer and not Environment.isEmmRdirServer \
			and not Environment.isEmmMergerServer and not Environment.isEmmMailerServer and not Environment.isEmmMailloopServer:
				Environment.errors.append("No " + Environment.applicationName + " application is currently installed")


	@staticmethod
	def determineOS():
		if Path("/etc/os-release").is_file():
			osProperties = ESTUtilities.readPropertiesFile("/etc/os-release")
			osVendor = osProperties.get("NAME", "").strip(' "')
			if osVendor:
				if "AlmaLinux" in Environment.osVendor:
					Environment.osVendor = "Alma"
				elif "CentOS" in Environment.osVendor:
					Environment.osVendor = "CentOS"
				elif "Red Hat" in Environment.osVendor or "RedHat" in Environment.osVendor:
					Environment.osVendor = "RedHat"
				elif "SLES" in Environment.osVendor or "SUSE" in Environment.osVendor:
					Environment.osVendor = "Suse"

			osVersion = osProperties.get("VERSION_ID", "").strip(' "')
			if osVersion:
				Environment.osVersion = osVersion.partition(".")[0]

		elif Path("/etc/issue").is_file():
			osPropertiesText = ESTUtilities.readTextFile("/etc/issue")
			if "CentOS" in osPropertiesText:
				Environment.osVendor = "CentOS"
				Environment.osVersion = ESTUtilities.getFirstVersionFromText(osPropertiesText)

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
			if DbConnector.checkDbConnection():
				if DbConnector.checkColumnExists("workflow_tbl", "is_legacy_mode"):
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
				logger.exception("Cannot check migration")
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
						logger.exception("Error while checking for Backend running")
					return None
			except:
				if ESTUtilities.isDebugMode():
					logger.exception("Error while checking for Backend running")
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
						logger.exception("Error while checking for Backend running")
					return None
			except:
				if ESTUtilities.isDebugMode():
					logger.exception("Error while checking for Backend running")
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
						logger.exception("Error while checking for Backend running")
					return None
			except:
				if ESTUtilities.isDebugMode():
					logger.exception("Error while checking for Backend running")
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
						logger.exception("Error while checking for Backend running")
					return None
			except:
				if ESTUtilities.isDebugMode():
					logger.exception("Error while checking for Backend running")
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
				"output": Environment.applicationVersions.runtime,
				"condition": condition_any_frontend_based_server or condition_current_user_is_frontend_app_user
			},
			"Frontend": {
				"output": Environment.applicationVersions.frontend,
				"condition": Environment.isEmmFrontendServer or condition_current_user_is_frontend_app_user
			},
			"Statistics": {
				"output": Environment.applicationVersions.statistics,
				"condition": Environment.isEmmStatisticsServer
			},
			"Webservices": {
				"output": Environment.applicationVersions.webservices,
				"condition": Environment.isEmmWebservicesServer
			},
			"Console-Rdir": {
				"output": Environment.applicationVersions.consoleRdir,
				"condition": Environment.isEmmConsoleRdirServer
			},
			"Backend": {
				"output": Environment.applicationVersions.emmBackend,
				"condition": condition_any_frontend_based_server or condition_current_user_is_frontend_app_user
			},
			"Rdir Runtime": {
				"output": Environment.applicationVersions.rdirRuntime,
				"condition": Environment.isEmmRdirServer and condition_any_frontend_based_server,
				"alternative_name_if_error": "Runtime"
			},
			"Rdir": {
				"output": Environment.applicationVersions.rdirRuntime,
				"condition": Environment.isEmmRdirServer
			},
			"Rdir Backend": {
				"output": Environment.applicationVersions.rdirBackend,
				"condition": Environment.isEmmRdirServer and bool(Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer),
				"alternative_name_if_error": "Backend"
			}
		}

		backend_application_mapping = {
			"Runtime Backend": {
				"output": Environment.applicationVersions.runtimeBackend,
				"condition": Environment.applicationVersions.runtimeBackend != "Unknown"
			},
			"Merger Backend": {
				"output": Environment.applicationVersions.mergerBackend,
				"condition": Environment.isEmmMergerServer
			},
			"Mailer Backend": {
				"output": Environment.applicationVersions.mailerBackend,
				"condition": Environment.isEmmMailerServer
			},
			"Mailloop Backend": {
				"output": Environment.applicationVersions.mailloopBackend,
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

class ApplicationVersions:
	_frontendUserHome = ESTUtilities.getUserHomeDirectory("openemm" if Environment.isOpenEmmServer else Environment.getFrontendApplicationUserName())
	_rdirUserHome = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName())
	_frontedVersionFile: Callable[[str],str] = lambda s: f"webapps/{s}/WEB-INF/classes/emm.properties"

	_backendExtractor: Callable[[Path],str] = lambda p: p.resolve().name.lstrip("V")
	_runtimeExtractor: Callable[[str],str] = lambda p: Path.read_text(p).strip(" \n")
	_frontedExtractor: Callable[[str],str] = lambda p: ESTUtilities.readPropertiesFile(p).get("ApplicationVersion")

	_applicationVersions = {}
	_applications: dict[str,list] = {
		"runtime": [
			_frontendUserHome,
			"tomcat/conf/version.txt",
			_runtimeExtractor
		],
		"frontend": [
			_frontendUserHome,
			_frontedVersionFile("emm"),
			_frontedExtractor
		],
		"statistics": [
			_frontendUserHome,
			_frontedVersionFile("statistics"),
			_frontedExtractor
		],
		"webservices": [
			_frontendUserHome,
			_frontedVersionFile("webservices"),
			_frontedExtractor
		],
		"consoleRdir": [
			_frontendUserHome,
			_frontedVersionFile("rdir"),
			_frontedExtractor
		],
		"rdir": [
			_rdirUserHome,
			_frontedVersionFile("rdir"),
			_frontedExtractor
		],
		"rdirRuntime": [
			_rdirUserHome,
			"tomcat/conf/version.txt",
			_runtimeExtractor
		],
		"emmBackend": [
			_frontendUserHome,
			"release/backend/current",
			_backendExtractor
		],
		"rdirBackend": [
			_rdirUserHome,
			"release/backend/current",
			_backendExtractor
		],
		"runtimeBackend": [
			"/root",
			"release/current",
			_backendExtractor
		],
		"mergerBackend": [
			ESTUtilities.getUserHomeDirectory("merger"),
			"release/current",
			_backendExtractor
		],
		"mailerBackend": [
			ESTUtilities.getUserHomeDirectory("mailout"),
			"release/current",
			_backendExtractor
		],
		"mailloopBackend": [
			ESTUtilities.getUserHomeDirectory("mailloop"),
			"release/current",
			_backendExtractor
		],
		#"unifiedBackend": [],
		"manual":[
			_frontendUserHome,
			"webapps/manual/de",
			lambda p: ESTUtilities.getVersionFromFilename(str(p.resolve()))
		],
		"java": [
			"",
			lambda: Environment.javaHome or (Environment.optDefaultPath or Environment.optDefaultPathSecondary or "") + "/java",
			ESTUtilities.getJavaVersion
		],
		"tomcat": [
			"",
			lambda: (Environment.catalinaHome or (Environment.optDefaultPath or "") + "/tomcat") +"/lib/catalina.jar",
			ESTUtilities.getTomcatVersion

		]
	}
	_aliases: dict = {
		"emm": "frontend",
		"openemm": "frontend",
		"backend-console": "emmBackend",
		"backend": "emmBackend"
	}
	def __init__(self):
		self._applicationVersions = self._evaluate()

	class VersionString(str):
		def __new__(cls, value, refreshReference):
			instance = super().__new__(cls, value)
			instance.refreshReference = refreshReference
			return instance

		def refresh(self):
			return self.__class__(
				self.refreshReference(),
				self.refreshReference
			)

	def refresh(self, identifier=None):
		return self._evaluate(identifier)


	def _evaluate(self, singleApplication: str | None=None):
		if not singleApplication:
			applicationResult: dict[str, str] = {}
			for application in self._applications.keys():
				applicationResult[application] = self._evaluate(application)
			return applicationResult
		else:
			user, path, extractor = self._applications[singleApplication]
			if callable(path):
				path = path()
			checkPath = Path(user,path)
			if os.access(checkPath, os.F_OK) and checkPath.exists():
				return self.VersionString(extractor(Path(user, path)), lambda: self.refresh(singleApplication))
			else:
				return self.VersionString("Unknown", lambda: self.refresh(singleApplication))

	def getAll(self):
		return self._evaluate()

	def __getattr__(self, applicationName):
		if applicationName in self._applications:
			return self._applicationVersions.get(applicationName)
		elif applicationName in self._aliases:
			return self._applicationVersions.get(self._aliases.get(applicationName))
		return self.VersionString("Unknown", lambda: "Unknown")