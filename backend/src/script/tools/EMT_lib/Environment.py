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
import shutil
import logging
import subprocess

from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities
from EMT_lib.License import License

class Environment:
	################################################
	# OpenEMM and EMM specific settings start here #
	################################################

	toolVersion = "23.04.032"
	toolName = None

	applicationName = None
	scriptFilePath = None
	applicationUserNamesToCheck = None
	applicationDbcfgEntryDefaultName = None

	agnitasDownloadSiteUrl = None
	agnitasDownloadPathVersionInfo = None
	updateChannel = None
	agnitasUpdateChannels = None

	defaultLandingPage = None
	multiServerSystem = None

	pythonInstallationSearchPaths = None
	dbClientMysqlSearchPaths = None
	dbClientOracleSearchPaths = None
	javaSearchPaths = None
	tomcatSearchPaths = None
	tomcatNativeEmmShDefault = None
	tomcatNativeSearchPaths = None
	dbcfgPropertiesFileSearchPaths = None
	optDefaultPath = None
	systemCfgFileSearchPaths = None
	allowedDbmsSystems = None
	restartToolName = None

	javaHome = None
	catalinaHome = None
	tomcatNative = None
	wkhtmltopdf = None
	wkhtmltoimage = None

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
	frontendUserName = None
	currentUserHomeDirectory = os.path.expanduser("~")

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

	unsavedClientChanges = None
	visibleClientProperties = ["rdir_domain", "mailloop_domain", "importalwaysinformemail", "DefaultLinkExtension"]
	companyInfoProperties = ["importalwaysinformemail", "DefaultLinkExtension"]
	readonlyClientProperties = ["company_id"]

	rebootNeeded = False
	otherSystemsNeedConfig = False

	sendmailVersion = None
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

	startupBackendVersion = None

	isEmmMergerServer = False
	mergerBackendVersion = "Unknown"

	isEmmMailerServer = False
	mailerBackendVersion = "Unknown"

	isEmmMailloopServer = False
	mailloopBackendVersion = "Unknown"

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
		if Environment.frontendUserName is None:
			if Environment.username == "openemm":
				Environment.frontendUserName = "openemm"
			elif Environment.username == "console":
				Environment.frontendUserName = "console"
			elif Environment.username == "rdir":
				Environment.frontendUserName = "rdir"
			elif Environment.scriptFilePath.startswith("/home/") and len(Environment.scriptFilePath) > 6:
				scriptHomeUser = Environment.scriptFilePath[6:]
				Environment.frontendUserName = scriptHomeUser[0:scriptHomeUser.index("/")]
			elif os.path.isdir("/home/openemm"):
				Environment.frontendUserName = "openemm"
			elif os.path.isdir("/home/console"):
				Environment.frontendUserName = "console"
			elif os.path.isdir("/home/rdir"):
				Environment.frontendUserName = "rdir"
			elif not Environment.username == "root":
				Environment.frontendUserName = Environment.username
			else:
				Environment.frontendUserName = "console"

		if os.path.isfile("/opt/agnitas.com/etc/agnitas.update.channel"):
			with open("/opt/agnitas.com/etc/agnitas.update.channel") as channelFile:
				Environment.updateChannel = channelFile.readlines()[0].strip()
			if Environment.updateChannel in Environment.agnitasUpdateChannels:
				Environment.agnitasDownloadPathVersionInfo = Environment.agnitasUpdateChannels[Environment.updateChannel]
			else:
				Environment.agnitasDownloadPathVersionInfo = None
		elif os.path.isfile("/home/openemm/etc/agnitas.update.channel"):
			with open("/home/openemm/etc/agnitas.update.channel") as channelFile:
				Environment.updateChannel = channelFile.readlines()[0].strip()
			if Environment.updateChannel in Environment.agnitasUpdateChannels:
				Environment.agnitasDownloadPathVersionInfo = Environment.agnitasUpdateChannels[Environment.updateChannel]
			else:
				Environment.agnitasDownloadPathVersionInfo = None

		if os.path.isfile("/etc/os-release"):
			osProperties = EMTUtilities.readPropertiesFile("/etc/os-release")
			if not EMTUtilities.isBlank(osProperties["NAME"]):
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

			if not EMTUtilities.isBlank(osProperties["VERSION_ID"]):
				Environment.osVersion = osProperties["VERSION_ID"].strip().strip("\"").strip()
				if "." in Environment.osVersion:
					Environment.osVersion = Environment.osVersion[0:Environment.osVersion.index(".")]
			else:
				Environment.osVersion = "Unknown"
		elif os.path.isfile("/etc/issue"):
			osPropertiesText = EMTUtilities.readTextFile("/etc/issue")
			if "CentOS" in osPropertiesText:
				Environment.osVendor = "CentOS"
				Environment.osVersion = EMTUtilities.getFirstVersionFromText(osPropertiesText)
				if EMTUtilities.isBlank(Environment.osVersion):
					Environment.osVersion = "Unknown"
			else:
				Environment.osVendor = "Unknown"
				Environment.osVersion = "Unknown"
		else:
			Environment.osVendor = "Unknown"
			Environment.osVersion = "Unknown"

		# Check allowed users
		# Check and create mandatory directories
		foundApplicationUserName = False
		for applicationUserNameToCheck in Environment.applicationUserNamesToCheck:
			if os.path.isdir("/home/" + applicationUserNameToCheck):
				foundApplicationUserName = True
				break
		if not foundApplicationUserName:
			print(Colors.RED + "\nYou must create at least one of the mandatory operating system users '" + "', '".join(Environment.applicationUserNamesToCheck) + "' first!" + Colors.DEFAULT + "\n")
			sys.exit(1)

		# Check ulimit (maximum parallel files open)
		ulimitValueString = subprocess.check_output("ulimit -n", shell=True).decode("UTF-8")
		ulimitValueString = ulimitValueString.strip()
		if int(ulimitValueString) < Environment.noFileLimit:
			print("System value for maximum parallel files open (= ulimit) is " + ulimitValueString + ". Must be at least " + str(Environment.noFileLimit) + ".")
			if not EMTUtilities.hasRootPermissions():
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
					if EMTUtilities.isDebugMode():
						logging.exception("ulimit change was not successful")
				sys.exit(1)
			else:
				print(Colors.RED + "System value for maximum parallel files open (= ulimit) is too low." + Colors.DEFAULT)
				print("Start anyway (N/y, Blank => Cancel):")
				answer = input(" > ").lower().strip()
				if not (answer.startswith("y") or answer.startswith("j")):
					sys.exit(1)

		DbConnector.dbcfgPropertiesFilePath = Environment.dbcfgPropertiesFileSearchPaths[0]
		for dbcfgPropertiesFileSearchPath in Environment.dbcfgPropertiesFileSearchPaths:
			if os.path.isfile(dbcfgPropertiesFileSearchPath):
				DbConnector.dbcfgPropertiesFilePath = dbcfgPropertiesFileSearchPath
		if not os.path.isdir(os.path.dirname(DbConnector.dbcfgPropertiesFilePath)):
			if "OpenEMM" in Environment.applicationName and os.path.isdir("/home/openemm"):
				EMTUtilities.createDirectories(os.path.dirname(DbConnector.dbcfgPropertiesFilePath), "openemm")
			elif EMTUtilities.hasRootPermissions():
				EMTUtilities.createDirectories(os.path.dirname(DbConnector.dbcfgPropertiesFilePath), "root", "dbcfg")

		if DbConnector.applicationDbcfgEntryName is None:
			DbConnector.applicationDbcfgEntryName = Environment.applicationDbcfgEntryDefaultName

		if not os.path.isfile(DbConnector.dbcfgPropertiesFilePath):
			if os.path.isdir("/home/openemm") or os.path.isdir("/home/console") or os.path.isdir("/home/rdir") or os.path.isdir("/home/merger") or os.path.isdir("/home/mailloop"):
				with open(DbConnector.dbcfgPropertiesFilePath, "w", encoding="UTF-8") as propertiesFileHandle:
					propertiesFileHandle.write(DbConnector.applicationDbcfgEntryName + ": dbms=, host=localhost, user=, password=, name=" + Environment.applicationName.lower())
				DbConnector.dbcfgProperties = DbConnector.readDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath)
				if EMTUtilities.hasRootPermissions():
					if "OpenEMM" in Environment.applicationName and os.path.isdir("/home/openemm"):
						os.system("chown openemm:openemm " + DbConnector.dbcfgPropertiesFilePath)
					else:
						os.system("chown root:dbcfg " + DbConnector.dbcfgPropertiesFilePath)
		else:
			DbConnector.dbcfgProperties = DbConnector.readDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath)

		if os.path.isfile("/home/" + Environment.username + "/tomcat/bin/emm.sh.additional.properties"):
			additionalProperties = EMTUtilities.readPropertiesFile("/home/" + Environment.username + "/tomcat/bin/emm.sh.additional.properties")
			if "DB_PROPERTIES_ITEM_NAME" in additionalProperties:
				DbConnector.applicationDbcfgEntryName = additionalProperties["DB_PROPERTIES_ITEM_NAME"].strip()
				DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName] if DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties else None
			if "LD_LIBRARY_PATH" in additionalProperties and additionalProperties["LD_LIBRARY_PATH"] is not None:
				ldLibraryPath = additionalProperties["LD_LIBRARY_PATH"].strip()
				if len(ldLibraryPath) > 0:
					if ldLibraryPath.endswith("/lib"):
						Environment.tomcatNative = ldLibraryPath[:-len("/lib")]
					else:
						Environment.tomcatNative = ldLibraryPath
		elif os.path.isfile("/home/" + Environment.frontendUserName + "/tomcat/bin/emm.sh.additional.properties"):
			additionalProperties = EMTUtilities.readPropertiesFile("/home/" + Environment.frontendUserName + "/tomcat/bin/emm.sh.additional.properties")
			if "DB_PROPERTIES_ITEM_NAME" in additionalProperties:
				DbConnector.applicationDbcfgEntryName = additionalProperties["DB_PROPERTIES_ITEM_NAME"].strip()
				DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName] if DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties else None
			if "LD_LIBRARY_PATH" in additionalProperties and additionalProperties["LD_LIBRARY_PATH"] is not None:
				ldLibraryPath = additionalProperties["LD_LIBRARY_PATH"].strip()
				if len(ldLibraryPath) > 0:
					if ldLibraryPath.endswith("/lib"):
						Environment.tomcatNative = ldLibraryPath[:-len("/lib")]
					else:
						Environment.tomcatNative = ldLibraryPath

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
					DbConnector.updateDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath, DbConnector.dbcfgEntry)
			else:
				Environment.errors.append("Invalid database vendor in dbcfg: " + dbVendor.lower())
		else:
			# Check for fallback "agnitas.emm"
			if os.path.isdir("/home/merger") and (Environment.username == "merger" or EMTUtilities.hasRootPermissions()):
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]
			if os.path.isdir("/home/mailloop") and (Environment.username == "mailloop" or EMTUtilities.hasRootPermissions()):
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
						DbConnector.updateDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath, DbConnector.dbcfgProperties)
				else:
					Environment.errors.append("Invalid database vendor in dbcfg: " + dbVendor.lower())
			elif os.path.isdir("/home/openemm") or os.path.isdir("/home/console") or os.path.isdir("/home/rdir") or os.path.isdir("/home/merger") or os.path.isdir("/home/mailloop"):
				Environment.errors.append("Invalid DbcfgEntryName defined: " + DbConnector.applicationDbcfgEntryName)

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
									if EMTUtilities.isDebugMode():
										logging.exception("Error")
									sys.exit(1)
							else:
								print(Colors.RED + "Installation of MariaDB was not successful" + Colors.DEFAULT)
								if EMTUtilities.isDebugMode():
									logging.exception("Error")
								sys.exit(1)

						try:
							os.system(installCommandMariadbPythonModule)
						except:
							print(Colors.RED + "Installation of MariaDB-python was not successful" + Colors.DEFAULT)
							if EMTUtilities.isDebugMode():
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
									if EMTUtilities.isDebugMode():
										logging.exception("Error")
									sys.exit(1)
							else:
								print(Colors.RED + "Installation of MariaDB was not successful" + Colors.DEFAULT)
								if EMTUtilities.isDebugMode():
									logging.exception("Error")
								sys.exit(1)

						try:
							os.system(installCommandMariadbPythonModule)
						except:
							print(Colors.RED + "Installation of MariaDB-python was not successful" + Colors.DEFAULT)
							if EMTUtilities.isDebugMode():
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
							if EMTUtilities.isDebugMode():
								logging.exception("Error")
							sys.exit(1)

						try:
							os.system(installCommandMysqlPythonModule)
						except:
							print(Colors.RED + "Installation of MySQL-python was not successful" + Colors.DEFAULT)
							if EMTUtilities.isDebugMode():
								logging.exception("Error")
							sys.exit(1)

						if not DbConnector.recheckMysqlDriverModuleAvailability():
							print(Colors.RED + "The mandatory database vendor MySQL is not supported by this python installation.\nPlease install mandatory driver via")
							print("  '" + installCommandMysql + "'")
							print("  '" + installCommandMysqlPythonModule + "'")
							print("and restart" + Colors.DEFAULT)
							sys.exit(1)

		Environment.sendmailVersion = EMTUtilities.getSendmailVersion()
		Environment.postfixVersion = EMTUtilities.getPostfixVersion()

		if os.path.isfile("/home/" + Environment.username + "/bin/setenv.sh"):
			Environment.environmentConfigurationFilePath = "/home/" + Environment.username + "/bin/setenv.sh"
		elif os.path.isfile("/home/" + Environment.frontendUserName + "/bin/setenv.sh"):
			Environment.environmentConfigurationFilePath = "/home/" + Environment.frontendUserName + "/bin/setenv.sh"
		elif os.path.isfile("/home/merger/lbin/custom.sh"):
			Environment.environmentConfigurationFilePath = "/home/mailmergerout/lbin/custom.sh"
		elif os.path.isfile("/home/mailloop/lbin/custom.sh"):
			Environment.environmentConfigurationFilePath = "/home/mailloop/lbin/custom.sh"
		elif os.path.isfile("/home/mailout/lbin/custom.sh"):
			Environment.environmentConfigurationFilePath = "/home/mailout/lbin/custom.sh"
		elif os.path.isdir("/home/" + Environment.frontendUserName):
			# Create empty setenv.sh and migrate existing /opt/agnitas.com/software libraries
			Environment.environmentConfigurationFilePath = "/home/" + Environment.frontendUserName + "/bin/setenv.sh"
			try:
				with open(Environment.environmentConfigurationFilePath, "w", encoding="UTF-8") as environmentConfigurationFileHandle:
					javaHome = ""
					if os.path.islink("/opt/agnitas.com/software/java") or os.path.isdir("/opt/agnitas.com/software/java"):
						javaHome = "/opt/agnitas.com/software/java"
					environmentConfigurationFileData = "export JAVA_HOME=" + javaHome + "\n"

					catalinaHome = ""
					if os.path.islink("/opt/agnitas.com/software/tomcat") or os.path.isdir("/opt/agnitas.com/software/tomcat"):
						catalinaHome = "/opt/agnitas.com/software/tomcat"
					environmentConfigurationFileData += "export CATALINA_HOME=" + catalinaHome + "\n"

					wkHtmlToPdf = ""
					if os.path.isfile("/opt/agnitas.com/software/wkhtmltox/bin/wkhtmltopdf"):
						wkHtmlToPdf = "/opt/agnitas.com/software/wkhtmltox/bin/wkhtmltopdf"
					environmentConfigurationFileData += "export WKHTMLTOPDF=" + wkHtmlToPdf + "\n"

					wkHtmlToImage = ""
					if os.path.isfile("/opt/agnitas.com/software/wkhtmltox/bin/wkhtmltoimage"):
						wkHtmlToImage = "/opt/agnitas.com/software/wkhtmltox/bin/wkhtmltoimage"
					environmentConfigurationFileData += "export WKHTMLTOIMAGE=" + wkHtmlToImage + "\n"

					if os.path.islink("/opt/agnitas.com/software/tomcat-native") or os.path.isdir("/opt/agnitas.com/software/tomcat-native"):
						tomcatNative = ""
						tomcatNative = "/opt/agnitas.com/software/tomcat-native"
						environmentConfigurationFileData += "export TOMCAT_NATIVE=" + tomcatNative + "\n"

					environmentConfigurationFileData += "source $HOME/scripts/config.sh\n"

					environmentConfigurationFileHandle.write(environmentConfigurationFileData)
				os.system("chown -R " + Environment.frontendUserName + " " + Environment.environmentConfigurationFilePath)
				os.system("chmod u+x " + Environment.environmentConfigurationFilePath)
				Environment.messages.append("Created initial setenv.sh file '" + Environment.environmentConfigurationFilePath + "'")
			except:
				if EMTUtilities.isDebugMode():
					logging.exception("Cannot create initial setenv.sh file")
				Environment.errors.append("Cannot create initial setenv.sh file '" + Environment.environmentConfigurationFilePath + "'")
				Environment.environmentConfigurationFilePath = None

		if not os.access(DbConnector.dbcfgPropertiesFilePath, os.R_OK):
			if os.path.isdir("/home/openemm") or os.path.isdir("/home/console") or os.path.isdir("/home/rdir") or os.path.isdir("/home/merger") or os.path.isdir("/home/mailloop"):
				Environment.errors.append("Mandatory dbcfg file is not readable: " + DbConnector.dbcfgPropertiesFilePath)
			DbConnector.dbcfgProperties = {}
		else:
			DbConnector.dbcfgProperties = DbConnector.readDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath)

		# Read and use WebProxy settings if available
		if Environment.environmentConfigurationFilePath is not None and os.path.isfile(Environment.environmentConfigurationFilePath):
			environmentProperties = EMTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)

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
				EMTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, None, environmentProperties)

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
			environmentProperties = EMTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)
			if "JAVA_HOME" in environmentProperties:
				Environment.javaHome = environmentProperties["JAVA_HOME"]
			if "CATALINA_HOME" in environmentProperties:
				Environment.catalinaHome = environmentProperties["CATALINA_HOME"]
			if "TOMCAT_NATIVE" in environmentProperties:
				Environment.tomcatNative = environmentProperties["TOMCAT_NATIVE"]

		if Environment.tomcatNative is None or Environment.tomcatNative == "" or EMTUtilities.getTomcatNativeVersion(Environment.tomcatNative) is None:
			Environment.tomcatNative = None

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

		DbConnector.detectDbClientPath(DbConnector.emmDbVendor, Environment.dbClientOracleSearchPaths, Environment.dbClientMysqlSearchPaths)

		# Read and check WKTML configuration (Database wins if set)
		if DbConnector.checkDbServiceAvailable() and DbConnector.checkDbStructureExists():
			wkhtmltopdfFromDb = DbConnector.readConfigurationValueFromDB("system", "wkhtmltopdf", Environment.hostname)
			if wkhtmltopdfFromDb is not None and (wkhtmltopdfFromDb == "" or not os.path.isfile(wkhtmltopdfFromDb)):
				wkhtmltopdfFromDb = None
			wkhtmltoimageFromDb = DbConnector.readConfigurationValueFromDB("system", "wkhtmltoimage", Environment.hostname)
			if wkhtmltoimageFromDb is not None and (wkhtmltoimageFromDb == "" and not os.path.isfile(wkhtmltoimageFromDb)):
				wkhtmltoimageFromDb = None

			if Environment.environmentConfigurationFilePath is not None and os.path.isfile(Environment.environmentConfigurationFilePath):
				environmentProperties = EMTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)

				if wkhtmltopdfFromDb is not None and wkhtmltopdfFromDb != "":
					Environment.wkhtmltopdf = wkhtmltopdfFromDb
					if not "WKHTMLTOPDF" in environmentProperties or environmentProperties["WKHTMLTOPDF"] != wkhtmltopdfFromDb:
						environmentProperties["WKHTMLTOPDF"] = wkhtmltopdfFromDb

						EMTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, None, environmentProperties)
				elif "WKHTMLTOPDF" in environmentProperties and environmentProperties["WKHTMLTOPDF"] is not None and environmentProperties["WKHTMLTOPDF"] != "":
					Environment.wkhtmltopdf = environmentProperties["WKHTMLTOPDF"]
					if DbConnector.checkDbConnection():
						DbConnector.updateConfigurationValueInDB("system", "wkhtmltopdf", Environment.wkhtmltopdf, Environment.hostname)

				if wkhtmltoimageFromDb is not None and wkhtmltoimageFromDb != "":
					Environment.wkhtmltoimage = wkhtmltoimageFromDb
					if not "WKHTMLTOIMAGE" in environmentProperties or environmentProperties["WKHTMLTOIMAGE"] != wkhtmltoimageFromDb:
						environmentProperties["WKHTMLTOIMAGE"] = wkhtmltoimageFromDb
						EMTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, None, environmentProperties)
				elif "WKHTMLTOIMAGE" in environmentProperties and environmentProperties["WKHTMLTOIMAGE"] is not None and environmentProperties["WKHTMLTOIMAGE"] != "":
					Environment.wkhtmltoimage = environmentProperties["WKHTMLTOIMAGE"]
					if DbConnector.checkDbConnection():
						DbConnector.updateConfigurationValueInDB("system", "wkhtmltoimage", Environment.wkhtmltoimage, Environment.hostname)
			else:
				Environment.wkhtmltopdf = wkhtmltopdfFromDb
				Environment.wkhtmltoimage = wkhtmltoimageFromDb
		elif Environment.environmentConfigurationFilePath is not None and os.path.isfile(Environment.environmentConfigurationFilePath):
			environmentProperties = EMTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)
			if "WKHTMLTOPDF" in environmentProperties and environmentProperties["WKHTMLTOPDF"] is not None and environmentProperties["WKHTMLTOPDF"] != "":
				Environment.wkhtmltopdf = environmentProperties["WKHTMLTOPDF"]
			if "WKHTMLTOIMAGE" in environmentProperties and environmentProperties["WKHTMLTOIMAGE"] is not None and environmentProperties["WKHTMLTOIMAGE"] != "":
				Environment.wkhtmltoimage = environmentProperties["WKHTMLTOIMAGE"]

		if Environment.systemCfgFileSearchPaths is not None and len(Environment.systemCfgFileSearchPaths) > 0:
			Environment.systemCfgFilePath = Environment.systemCfgFileSearchPaths[0]
			for systemCfgFileSearchPath in Environment.systemCfgFileSearchPaths:
				if os.path.isfile(systemCfgFileSearchPath):
					Environment.systemCfgFilePath = systemCfgFileSearchPath
			if not os.path.isdir(os.path.dirname(Environment.systemCfgFilePath)):
				EMTUtilities.createDirectories(os.path.dirname(Environment.systemCfgFilePath), "root", "root")
			if os.path.isfile(Environment.systemCfgFilePath):
				Environment.systemCfgProperties = EMTUtilities.readPropertiesFile(Environment.systemCfgFilePath)
			else:
				Environment.systemCfgFilePath = None
		else:
			Environment.systemCfgFilePath = None

		if Environment.systemCfgFilePath is not None and os.path.isfile(Environment.systemCfgFilePath):
			Environment.systemCfgProperties = EMTUtilities.readPropertiesFile(Environment.systemCfgFilePath)
			systemCfgDbcfgEntryName = "emm"
			if "dbid" in Environment.systemCfgProperties:
				systemCfgDbcfgEntryName = Environment.systemCfgProperties["dbid"]
			if systemCfgDbcfgEntryName != DbConnector.applicationDbcfgEntryName:
				Environment.errors.append("DbcfgEntryName defined in '" + Environment.systemCfgFilePath + "' (" + systemCfgDbcfgEntryName + ") does not match with that defined in '" + DbConnector.dbcfgPropertiesFilePath + "' (" + DbConnector.applicationDbcfgEntryName + ")")

		if Environment.agnitasDownloadPathVersionInfo is not None:
			response = None
			try:
				response = EMTUtilities.openUrlConnection(Environment.agnitasDownloadPathVersionInfo)
				Environment.agnitasDownloadSiteUrlReachable = response.getcode() == 200 or response.getcode() == 403
				if not Environment.agnitasDownloadSiteUrlReachable:
					if EMTUtilities.isDebugMode():
						print("Cannot reach: " + Environment.agnitasDownloadPathVersionInfo + " (" + str(response.getcode()) + ")")
			except:
				if EMTUtilities.isDebugMode():
					print("Cannot reach: " + Environment.agnitasDownloadPathVersionInfo)
				Environment.agnitasDownloadSiteUrlReachable = False
				if not EMTUtilities.sslIsAvailable():
					Environment.errors.append("SSL connections are not supported by the current python installation")
			finally:
				if response is not None:
					response.close()

		if Environment.sendmailVersion is None and Environment.postfixVersion is None:
			Environment.errors.append("No MTA (sendmail or postfix) is installed")

		if Environment.agnitasCloudUrl is not None:
			response = None
			try:
				response = EMTUtilities.openUrlConnection(Environment.agnitasCloudUrl)
				Environment.agnitasCloudUrlReachable = response.getcode() == 200
			except:
				Environment.agnitasCloudUrlReachable = False
				if not EMTUtilities.sslIsAvailable():
					Environment.errors.append("SSL connections are not supported by the current python installation")
			finally:
				if response is not None:
					response.close()

		if DbConnector.checkDbServiceAvailable():
			if not DbConnector.checkDbConnection():
				Environment.errors.append("Database Connection cannot be established. (Maybe database user or database connection parameters for " + Environment.applicationName + " were not configured)")
			if not DbConnector.checkDbStructureExists():
				Environment.errors.append("Database structure does not exist. (Maybe " + Environment.applicationName + " is not installed)")
			License.checkLicenseStatus()
		# Cleanup legacy data
		if os.path.isfile("/home/" + Environment.username + "/bin/EmmMaintenanceTool.sh"):
			os.remove("/home/" + Environment.username + "/bin/EmmMaintenanceTool.sh")
		if os.path.islink("/home/" + Environment.username + "/bin/EmmMaintenanceTool.sh"):
			os.remove("/home/" + Environment.username + "/bin/EmmMaintenanceTool.sh")

		Environment.checkMigration()

	@staticmethod
	def readApplicationValues():
		# Check application types
		if "OpenEMM" in Environment.applicationName and os.path.isdir("/home/openemm"):
			Environment.isOpenEmmServer = True

			if EMTUtilities.hasRootPermissions() and not os.path.isfile("/usr/local/bin/OMT.sh"):
				if os.path.isfile("/home/openemm/bin/OMT.sh"):
					EMTUtilities.createLink("/home/openemm/bin/OMT.sh", "/usr/local/bin/OMT.sh", "root")
				elif os.path.isfile("/home/" + Environment.frontendUserName + "/bin/OMT.sh"):
					EMTUtilities.createLink("/home/" + Environment.frontendUserName + "/bin/OMT.sh", "/usr/local/bin/OMT.sh", "root")

			if not os.path.isdir("/home/openemm/bin"):
				EMTUtilities.createDirectory("/home/openemm/bin", "openemm")
			if not os.path.isdir("/home/openemm/release"):
				EMTUtilities.createDirectory("/home/openemm/release", "openemm")
			if not os.path.isdir("/home/openemm/webapps"):
				EMTUtilities.createDirectory("/home/openemm/webapps", "openemm")
			if not os.path.isdir("/home/openemm/temp"):
				EMTUtilities.createDirectory("/home/openemm/temp", "openemm")
			if not os.path.isdir("/home/openemm/conf") and not os.path.islink("/home/openemm/conf"):
				EMTUtilities.createLink("/home/openemm/tomcat/conf", "/home/openemm/conf", "openemm")
			if not os.path.isfile("/home/openemm/tomcat/conf/server.xml") and os.path.isfile("/home/openemm/tomcat/conf/server.xml.template"):
				shutil.copy("/home/openemm/tomcat/conf/server.xml.template", "/home/openemm/tomcat/conf/server.xml")
				EMTUtilities.chown("/home/openemm/tomcat/conf/server.xml", "openemm", "openemm")
				EMTUtilities.manageTlsCertificateForTomcat("/home/openemm/etc/ssl", "/home/openemm/tomcat/conf/server.xml", Environment.applicationName)
			if not os.path.isdir("/home/openemm/logs") and not os.path.islink("/home/openemm/logs"):
				EMTUtilities.createLink("/home/openemm/tomcat/logs", "/home/openemm/logs", "openemm")
			if os.path.isdir("/home/openemm/release/backend/current"):
				Environment.emmBackendVersion = os.path.basename(os.path.realpath("/home/openemm/release/backend/current"))
				if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
					Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]

			if not os.path.isfile("/home/openemm/bin/" + Environment.restartToolName) and os.path.isfile("/home/openemm/tomcat/bin/" + Environment.restartToolName):
				EMTUtilities.createLink("/home/openemm/tomcat/bin/" + Environment.restartToolName, "/home/openemm/bin/" + Environment.restartToolName, "openemm")

			if os.path.isfile("/home/openemm/tomcat/conf/version.txt"):
				with open("/home/openemm/tomcat/conf/version.txt") as runtimeVersionFile:
					Environment.runtimeVersion = runtimeVersionFile.read().strip()
			else:
				Environment.runtimeVersion = "Unknown"

			Environment.emmLicenseFilePath = "/home/openemm/webapps/emm/WEB-INF/classes/emm.license.xml"
			if os.path.isfile("/home/openemm/webapps/emm/WEB-INF/classes/emm.properties"):
				Environment.frontendVersion = EMTUtilities.readPropertiesFile("/home/openemm/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if os.path.isfile("/home/openemm/webapps/statistics/WEB-INF/classes/emm.properties"):
				Environment.statisticsVersion = EMTUtilities.readPropertiesFile("/home/openemm/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if os.path.isfile("/home/openemm/webapps/webservices/WEB-INF/classes/emm.properties"):
				Environment.webservicesVersion = EMTUtilities.readPropertiesFile("/home/openemm/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]

			if os.path.isdir("/home/openemm/webapps/manual/de"):
				manualApplicationPath = os.path.realpath("/home/openemm/webapps/manual/de")
				manualVersion = EMTUtilities.getVersionFromFilename(manualApplicationPath)
				if manualVersion is not None:
					Environment.manualVersion = manualVersion
		else:
			if EMTUtilities.hasRootPermissions():
				if not os.path.isfile("/usr/local/bin/EMT.sh") and Environment.scriptFilePath.startswith("/home/") and len(Environment.scriptFilePath) > 6:
					scriptHomeUser = Environment.scriptFilePath[6:]
					scriptHomeUser = scriptHomeUser[0:scriptHomeUser.index("/")]
					if os.path.isfile("/home/" + scriptHomeUser + "/bin/EMT.sh"):
						EMTUtilities.createLink("/home/" + scriptHomeUser + "/bin/EMT.sh", "/usr/local/bin/EMT.sh", "root")
			else:
				if os.path.isdir("/home/" + Environment.username + "/bin") and not os.path.isfile("/home/" + Environment.username + "/bin/EMT.sh"):
					if Environment.username == "merger" or Environment.username == "mailout" or Environment.username == "mailloop":
						emtPath = "/home/" + Environment.username + "/release/current/bin/EMT.sh"
					else:
						emtPath = "/home/" + Environment.username + "/bin/EMT.sh"

					if os.path.isfile(emtPath):
						EMTUtilities.createLink(emtPath, "/home/" + Environment.username + "/bin/EMT.sh", Environment.username)

			# Console Runtime information
			if os.path.isdir("/home/console") and (Environment.username == "console" or EMTUtilities.hasRootPermissions()):
				if os.path.isfile("/home/console/tomcat/conf/version.txt"):
					with open("/home/console/tomcat/conf/version.txt") as runtimeVersionFile:
						Environment.runtimeVersion = runtimeVersionFile.read().strip()
				if not os.path.isdir("/home/console/bin"):
					EMTUtilities.createDirectory("/home/console/bin", "console")
				if not os.path.isdir("/home/console/release"):
					EMTUtilities.createDirectory("/home/console/release", "console")
				if not os.path.isdir("/home/console/webapps"):
					EMTUtilities.createDirectory("/home/console/webapps", "console")
				if not os.path.isdir("/home/console/temp"):
					EMTUtilities.createDirectory("/home/console/temp", "console")
				if not os.path.isdir("/home/console/conf") and not os.path.islink("/home/console/conf"):
					EMTUtilities.createLink("/home/console/tomcat/conf", "/home/console/conf", "console")
				if not os.path.isfile("/home/console/tomcat/conf/server.xml") and os.path.isfile("/home/console/tomcat/conf/server.xml.template"):
					shutil.copy("/home/console/tomcat/conf/server.xml.template", "/home/console/tomcat/conf/server.xml")
					EMTUtilities.chown("/home/openemm/tomcat/conf/server.xml", "console", "console")
					EMTUtilities.manageTlsCertificateForTomcat("/home/console/sslcert", "/home/console/tomcat/conf/server.xml", Environment.applicationName)
				if not os.path.isdir("/home/console/logs") and not os.path.islink("/home/console/logs"):
					EMTUtilities.createLink("/home/console/tomcat/logs", "/home/console/logs", "console")

			# EMM information
			if os.path.isdir("/home/console/release/emm") and (Environment.username == "console" or EMTUtilities.hasRootPermissions()):
				Environment.isEmmFrontendServer = True
				if not os.path.isfile("/home/console/bin/" + Environment.restartToolName) and os.path.isfile("/home/console/tomcat/bin/" + Environment.restartToolName):
					EMTUtilities.createLink("/home/console/tomcat/bin/" + Environment.restartToolName, "/home/console/bin/" + Environment.restartToolName, "console")

				Environment.emmLicenseFilePath = "/home/console/webapps/emm/WEB-INF/classes/emm.license.xml"
				if os.path.isfile("/home/console/webapps/emm/WEB-INF/classes/emm.properties"):
					Environment.frontendVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]

				if os.path.isdir("/home/console/release/backend/current"):
					Environment.emmBackendVersion = os.path.basename(os.path.realpath("/home/console/release/backend/current"))
					if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
						Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]

			if os.path.isdir("/home/console/release/manual") and (Environment.username == "console" or EMTUtilities.hasRootPermissions()):
				if os.path.isdir("/home/console/webapps/manual/de"):
					manualApplicationPath = os.path.realpath("/home/console/webapps/manual/de")
					manualVersion = EMTUtilities.getVersionFromFilename(manualApplicationPath)
					if manualVersion is not None:
						Environment.manualVersion = manualVersion
					else:
						Environment.manualVersion = "Unknown"
				else:
					Environment.manualVersion = "Unknown"

			if os.path.isdir("/home/console/release/statistics") and (Environment.username == "console" or EMTUtilities.hasRootPermissions()):
				Environment.isEmmStatisticsServer = True
				if not os.path.isfile("/home/console/bin/" + Environment.restartToolName) and os.path.isfile("/home/console/tomcat/bin/" + Environment.restartToolName):
					EMTUtilities.createLink("/home/console/tomcat/bin/" + Environment.restartToolName, "/home/console/bin/" + Environment.restartToolName, "console")

				Environment.emmLicenseFilePath = "/home/console/webapps/statistics/WEB-INF/classes/emm.license.xml"
				if os.path.isfile("/home/console/webapps/statistics/WEB-INF/classes/emm.properties"):
					Environment.statisticsVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]

			if os.path.isdir("/home/console/release/webservices") and (Environment.username == "console" or EMTUtilities.hasRootPermissions()):
				Environment.isEmmWebservicesServer = True
				if not os.path.isfile("/home/console/bin/" + Environment.restartToolName) and os.path.isfile("/home/console/tomcat/bin/" + Environment.restartToolName):
					EMTUtilities.createLink("/home/console/tomcat/bin/" + Environment.restartToolName, "/home/console/bin/" + Environment.restartToolName, "console")

				Environment.emmLicenseFilePath = "/home/console/webapps/webservices/WEB-INF/classes/emm.license.xml"
				if os.path.isfile("/home/console/webapps/webservices/WEB-INF/classes/emm.properties"):
					Environment.webservicesVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]

				if os.path.isdir("/home/console/release/backend/current"):
					Environment.emmBackendVersion = os.path.basename(os.path.realpath("/home/console/release/backend/current"))
					if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
						Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]

			if os.path.isdir("/home/console/release/rdir") and (Environment.username == "console" or EMTUtilities.hasRootPermissions()):
				# Also check for the application "rdir" installed under user "console". This is indeed the configuration on some "Inhouse" customer systems.
				Environment.isEmmConsoleRdirServer = True
				if not os.path.isfile("/home/console/bin/" + Environment.restartToolName) and os.path.isfile("/home/console/tomcat/bin/" + Environment.restartToolName):
					EMTUtilities.createLink("/home/console/tomcat/bin/" + Environment.restartToolName, "/home/console/bin/" + Environment.restartToolName, "console")

				Environment.emmLicenseFilePath = "/home/console/webapps/rdir/WEB-INF/classes/emm.license.xml"
				if os.path.isfile("/home/console/webapps/rdir/WEB-INF/classes/emm.properties"):
					Environment.consoleRdirVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]

				if os.path.isdir("/home/console/release/backend/current"):
					Environment.emmBackendVersion = os.path.basename(os.path.realpath("/home/console/release/backend/current"))
					if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
						Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]

			if os.path.isdir("/home/rdir") and (Environment.username == "rdir" or EMTUtilities.hasRootPermissions()):
				Environment.isEmmRdirServer = True;
				if not os.path.isfile("/home/rdir/bin/" + Environment.restartToolName) and os.path.isfile("/home/rdir/tomcat/bin/" + Environment.restartToolName):
					EMTUtilities.createLink("/home/rdir/tomcat/bin/" + Environment.restartToolName, "/home/rdir/bin/" + Environment.restartToolName, "rdir")

				if os.path.isfile("/home/rdir/tomcat/conf/version.txt"):
					with open("/home/rdir/tomcat/conf/version.txt") as runtimeVersionFile:
						Environment.rdirRuntimeVersion = runtimeVersionFile.read().strip()

				Environment.emmLicenseFilePath = "/home/rdir/webapps/rdir/WEB-INF/classes/emm.license.xml"
				if os.path.isfile("/home/rdir/webapps/rdir/WEB-INF/classes/emm.properties"):
					Environment.rdirVersion = EMTUtilities.readPropertiesFile("/home/rdir/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]

				if not os.path.isdir("/home/rdir/bin"):
					EMTUtilities.createDirectory("/home/rdir/bin", "rdir")
				if not os.path.isdir("/home/rdir/release"):
					EMTUtilities.createDirectory("/home/rdir/release", "rdir")
				if not os.path.isdir("/home/rdir/webapps"):
					EMTUtilities.createDirectory("/home/rdir/webapps", "rdir")
				if not os.path.isdir("/home/rdir/temp"):
					EMTUtilities.createDirectory("/home/rdir/temp", "rdir")
				if not os.path.isdir("/home/rdir/conf") and not os.path.islink("/home/rdir/conf"):
					EMTUtilities.createLink("/home/rdir/tomcat/conf", "/home/rdir/conf", "rdir")
				if not os.path.isfile("/home/rdir/tomcat/conf/server.xml") and os.path.isfile("/home/rdir/tomcat/conf/server.xml.template"):
					shutil.copy("/home/rdir/tomcat/conf/server.xml.template", "/home/rdir/tomcat/conf/server.xml")
					EMTUtilities.chown("/home/openemm/tomcat/conf/server.xml", "rdir", "rdir")
					EMTUtilities.manageTlsCertificateForTomcat("/home/rdir/sslcert", "/home/rdir/tomcat/conf/server.xml", Environment.applicationName)
				if not os.path.isdir("/home/rdir/logs") and not os.path.islink("/home/rdir/logs"):
					EMTUtilities.createLink("/home/rdir/tomcat/logs", "/home/rdir/logs", "rdir")
				if os.path.isdir("/home/rdir/release/backend/current"):
					Environment.rdirBackendVersion = os.path.basename(os.path.realpath("/home/rdir/release/backend/current"))
					if Environment.rdirBackendVersion is not None and Environment.rdirBackendVersion.startswith("V"):
						Environment.rdirBackendVersion = Environment.rdirBackendVersion[1:len(Environment.rdirBackendVersion)]

			if os.path.isdir("/home/merger") and (Environment.username == "merger" or EMTUtilities.hasRootPermissions()):
				Environment.isEmmMergerServer = True
				if not os.path.islink("/home/merger/bin"):
					EMTUtilities.createLink("/home/merger/release/current/bin", "/home/merger/bin", "merger")
				if not os.path.isdir("/home/merger/release"):
					EMTUtilities.createDirectory("/home/merger/release", "merger")
				if os.path.isdir("/home/merger/release/current"):
					Environment.mergerBackendVersion = os.path.basename(os.path.realpath("/home/merger/release/current"))
					if Environment.mergerBackendVersion is not None and Environment.mergerBackendVersion.startswith("V"):
						Environment.mergerBackendVersion = Environment.mergerBackendVersion[1:len(Environment.mergerBackendVersion)]
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if os.path.isdir("/home/mailout") and (Environment.username == "mailout" or EMTUtilities.hasRootPermissions()):
				Environment.isEmmMailerServer = True
				if not os.path.islink("/home/mailout/bin"):
					EMTUtilities.createLink("/home/mailout/release/current/bin", "/home/mailout/bin", "mailout")
				if not os.path.isdir("/home/mailout/release"):
					EMTUtilities.createDirectory("/home/mailout/release", "mailout")
				if os.path.isdir("/home/mailout/release/current"):
					Environment.mailerBackendVersion = os.path.basename(os.path.realpath("/home/mailout/release/current"))
					if Environment.mailerBackendVersion is not None and Environment.mailerBackendVersion.startswith("V"):
						Environment.mailerBackendVersion = Environment.mailerBackendVersion[1:len(Environment.mailerBackendVersion)]
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if os.path.isdir("/home/mailloop") and (Environment.username == "mailloop" or EMTUtilities.hasRootPermissions()):
				Environment.isEmmMailloopServer = True
				if not os.path.islink("/home/mailloop/bin"):
					EMTUtilities.createLink("/home/mailloop/release/current/bin", "/home/mailloop/bin", "mailloop")
				if not os.path.isdir("/home/mailloop/release"):
					EMTUtilities.createDirectory("/home/mailloop/release", "mailloop")
				if os.path.isdir("/home/mailloop/release/current"):
					Environment.mailloopBackendVersion = os.path.basename(os.path.realpath("/home/mailloop/release/current"))
					if Environment.mailloopBackendVersion is not None and Environment.mailloopBackendVersion.startswith("V"):
						Environment.mailloopBackendVersion = Environment.mailloopBackendVersion[1:len(Environment.mailloopBackendVersion)]
				if "agnitas.emm" in DbConnector.dbcfgProperties:
					DbConnector.applicationDbcfgEntryName = "agnitas.emm"
					DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName]

			if DbConnector.dbcfgEntry is None and Environment.applicationDbcfgEntryDefaultName in DbConnector.dbcfgProperties:
				DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[Environment.applicationDbcfgEntryDefaultName]

		if os.path.isdir("/root/release/current"):
			Environment.startupBackendVersion = os.path.basename(os.path.realpath("/root/release/current"))
			if Environment.startupBackendVersion is not None and Environment.startupBackendVersion.startswith("V"):
				Environment.startupBackendVersion = Environment.startupBackendVersion[1:len(Environment.startupBackendVersion)]

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

				problematicWorkflowsWithReportIcon = DbConnector.select("SELECT shortname, company_id, workflow_id FROM workflow_tbl WHERE LOWER(workflow_schema) LIKE '%type\":\"report%'")
				if problematicWorkflowsWithReportIcon is not None and len(problematicWorkflowsWithReportIcon) > 0:
					listOfWorkflowNames = ""
					for row in problematicWorkflowsWithReportIcon:
						shortname = row[0]
						companyID = row[1]
						workflowID = row[2]
						if len(listOfWorkflowNames) > 0:
							listOfWorkflowNames = listOfWorkflowNames + ", "
						listOfWorkflowNames = listOfWorkflowNames + "\"" + shortname + "\" (clientID: " + str(companyID) + ", workflowID: " + str(workflowID) + ")"
					Environment.warnings.append("Your " + Environment.applicationName + " database contains campaign workflows " + listOfWorkflowNames + " which contain a report icon. Please remove the report icon from these workflows since they will no longer work with " + Environment.applicationName + " 23.10 and later. If you need help, please contact support.")

				oldFormDoLinks = DbConnector.selectValue("SELECT COUNT(*) FROM rdir_url_tbl WHERE full_url like '%/form.do%'")
				if oldFormDoLinks is not None and oldFormDoLinks > 0:
					Environment.warnings.append("Your " + Environment.applicationName + " database contains userform links in mailings of the old '.../form.do?...' format. Please switch those to the new format '.../form.action?...'. If you need help, please contact support.")
		except Exception as e:
			if EMTUtilities.isDebugMode():
				logging.exception("Cannot check migration")
			if hasattr(e, 'message'):
				Environment.errors.append("Cannot check migration: " + str(e.message))
			else:
				Environment.errors.append("Cannot check migration")

	@staticmethod
	def getBackendApplicationUserName():
		if Environment.isOpenEmmServer:
			return "openemm"
		elif Environment.isEmmMergerServer:
			return "merger"
		elif Environment.isEmmMailerServer:
			return "mailout"
		elif Environment.isEmmMailloopServer:
			return "mailloop"
		else:
			return "merger"
