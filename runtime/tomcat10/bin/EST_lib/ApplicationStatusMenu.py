import os
import stat
import sys
import logging
import datetime
import subprocess
import getpass
import time

from pathlib import Path

from EST_lib import Colors
from EST_lib import DbConnector
from EST_lib import ESTUtilities
from EST_lib.Environment import Environment
from EST_lib.License import License
from EST_lib import CheckMenu

def statusMenuAction(actionParameters):
	print(Environment.applicationName + " status:")
	print(readApplicationStatus(True))
	input(" > ")

def readApplicationStatus(colorize = True):
	if colorize:
		errorColorCode = Colors.RED
		defaultColorCode = Colors.DEFAULT
	else:
		errorColorCode = ""
		defaultColorCode = ""

	statusText = ""

	# OS version
	statusText += "Operating System (OS): " + str(Environment.osVendor) + " " + str(Environment.osVersion) + "\n"

	# Python version
	statusText += "Python version: " + str(sys.version_info[0]) + "." + str(sys.version_info[1]) + "." + str(sys.version_info[2]) + "\n"

	if DbConnector.emmDbVendor == "mariadb":
		if not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "MariaDB-server") and not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "mariadb-server"):
			statusText += errorColorCode + "Mandatory system package 'MariaDB-server' (or lower case) is not installed" + defaultColorCode + "\n"
		if not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "MariaDB-common") and not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "mariadb-common"):
			statusText += errorColorCode + "Mandatory system package 'MariaDB-common' (or lower case) is not installed" + defaultColorCode + "\n"
		if not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "MariaDB-client") and not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "mariadb-client"):
			statusText += errorColorCode + "Mandatory system package 'MariaDB-client' (or lower case) is not installed" + defaultColorCode + "\n"
		if not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "MariaDB-shared") and not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "mariadb-shared"):
			statusText += errorColorCode + "Mandatory system package 'MariaDB-shared' (or lower case) is not installed" + defaultColorCode + "\n"
		if not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "MariaDB-devel") and not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "mariadb-devel"):
			statusText += errorColorCode + "Mandatory system package 'MariaDB-devel' (or lower case) is not installed" + defaultColorCode + "\n"
		if not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "mariadb-connector-c-devel") and not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "MariaDB-devel") and not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "mariadb-devel"):
			statusText += errorColorCode + "Mandatory system package 'mariadb-connector-c-devel' is not installed" + defaultColorCode + "\n"
		if not ESTUtilities.checkPackageIsInstalled(Environment.osVendor, "galera"):
			statusText += "Optional system package 'galera' for MariaDB clusters is not installed" + "\n"

	if not ESTUtilities.sslIsAvailable():
		statusText += errorColorCode + "SSL connections are not supported by the current python installation" + defaultColorCode + "\n"

	# File permissions
	if DbConnector.dbcfgPropertiesFilePath is not None:
		if os.path.isfile(DbConnector.dbcfgPropertiesFilePath):
			if DbConnector.dbcfgPropertiesFilePath.startswith (f'{Environment.currentUserHomeDirectory}{os.path.sep}'):
				if os.access (DbConnector.dbcfgPropertiesFilePath, os.R_OK):
					statusText += 'DBCFG: local OK\n'
				else:
					statusText += f'{errorColorCode}DBCFG: file "{DbConnector.dbcfgPropertiesFilePath}" not readable{defaultColorCode}\n'
			else:
				groupName = Path(DbConnector.dbcfgPropertiesFilePath).group()
				dbcfgStat = os.stat(DbConnector.dbcfgPropertiesFilePath).st_mode
				groupReadable = bool(dbcfgStat & stat.S_IRGRP)
				if "dbcfg" != groupName and Environment.applicationName == "EMM":
					statusText += errorColorCode + "DBCFG: Group of file '" + DbConnector.dbcfgPropertiesFilePath + "' is not 'dbcfg'" + defaultColorCode + "\n"
				elif not groupReadable:
					statusText += errorColorCode + "DBCFG: Group is missing read permission on file '" + DbConnector.dbcfgPropertiesFilePath + "'" + defaultColorCode + "\n"
				else:
					statusText += "DBCFG: OK" + "\n"
		else:
			statusText += errorColorCode + "DBCFG: File '" + DbConnector.dbcfgPropertiesFilePath + "' is missing" + defaultColorCode + "\n"
	else:
		statusText += errorColorCode + "DBCFG: File is not configured" + defaultColorCode + "\n"

	dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName] if DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties else None
	if not "oracle" in Environment.allowedDbmsSystems and not DbConnector.isMysqlDriverModuleAvailable() and not DbConnector.isMariadbDriverModuleAvailable():
		statusText += errorColorCode + "The database vendor " + dbcfgEntry["dbms"] + " is not supported by this python installation" + defaultColorCode + "\n"
	elif not DbConnector.isOracleDriverModuleAvailable() and not DbConnector.isMysqlDriverModuleAvailable() and not DbConnector.isMariadbDriverModuleAvailable():
		statusText += errorColorCode + "No database vendor 'oracle', 'mariadb, 'maysql' is supported by this python installation" + defaultColorCode + "\n"

	Environment.postfixVersion = ESTUtilities.getPostfixVersion()

	if Environment.postfixVersion is not None:
		statusText += "Postfix Version: " + Environment.postfixVersion + "\n"
	else:
		statusText += errorColorCode + "NO MTA (postfix) found" + defaultColorCode + "\n"

	# Database client status
	if DbConnector.getDbClientPath() is not None:
		try:
			dbClientVersion = DbConnector.getDbClientVersion()
			if dbClientVersion is None:
				statusText += errorColorCode + "Database client status: NOT available" + defaultColorCode + "\n"
			else:
				statusText += "Database client version: " + dbClientVersion + "\n"
		except:
			if ESTUtilities.isDebugMode():
				logging.exception("Database client status")
			statusText += errorColorCode + "Database client status: ERROR" + defaultColorCode + "\n"
	else:
		statusText += errorColorCode + "Database client NOT available" + defaultColorCode + "\n"

	# Database Connection status via dbcfg
	dbConnectionWorked = False
	if not DbConnector.checkDbServiceAvailable():
		statusText += errorColorCode + "Database: Not running or not reachable" + defaultColorCode + "\n"
	else:
		connection = None
		try:
			connection = DbConnector.openDbConnection()
			if connection is None:
				statusText += errorColorCode + "Database Connection: ERROR" + defaultColorCode + "\n"
			else:
				statusText += "Database Connection: OK" + "\n"
				dbConnectionWorked = True
		except:
			if ESTUtilities.isDebugMode():
				logging.exception("Database Connection")
			statusText += errorColorCode + "Database Connection: ERROR" + defaultColorCode + "\n"
		finally:
			if connection is not None:
				connection.commit()
				connection.close()

	statusText += "System-Time: " + datetime.datetime.now().strftime("%Y-%m-%d %H:%M:%S") + "\n"

	if dbConnectionWorked:
		# Database time
		try:
			if DbConnector.emmDbVendor == "oracle":
				dbTime = DbConnector.selectValue("SELECT CURRENT_TIMESTAMP FROM dual")
			else:
				dbTime = DbConnector.selectValue("SELECT CURRENT_TIMESTAMP")
			statusText += "System-Time-DB: " + str(dbTime) + "\n"
		except:
			if ESTUtilities.isDebugMode():
				logging.exception("Detect database time")
			statusText += errorColorCode + "System-Time-DB: ERROR" + defaultColorCode + "\n"

		# Database version
		try:
			if DbConnector.emmDbVendor == "oracle":
				versionText = DbConnector.selectValue("SELECT banner FROM V$VERSION WHERE LOWER(banner) LIKE 'oracle%'")
				# Oracle is already contained in version text
				dbVersion = versionText
			elif DbConnector.emmDbVendor == "mariadb":
				versionText = DbConnector.selectValue("SELECT VERSION()")
				dbVersion = "MariaDB " + versionText
			elif DbConnector.emmDbVendor == "mysql":
				versionText = DbConnector.selectValue("SELECT VERSION()")
				dbVersion = "MySQL " + versionText

			statusText += "Database version: " + dbVersion + "\n"
		except:
			if ESTUtilities.isDebugMode():
				logging.exception("Detect database version")
			statusText += errorColorCode + "Detect database version: ERROR" + defaultColorCode + "\n"

		# Check for existing database scheme
		if DbConnector.checkDbStructureExists():
			structureVersion = "Unknown"
			try:
				structureVersion = DbConnector.selectValue("SELECT MAX(version_number) FROM agn_dbversioninfo_tbl")
			except:
				if ESTUtilities.isDebugMode():
					logging.exception("Database structure")
				statusText += errorColorCode + "Database structure: ERROR" + defaultColorCode + "\n"
			statusText += Environment.applicationName + " database structure exists (Version " + structureVersion + ")" + "\n"
			databaseAvailable = True
		else:
			statusText += errorColorCode + Environment.applicationName + " database structure does NOT exist" + defaultColorCode + "\n"
			databaseAvailable = False
	else:
		databaseAvailable = False

	if databaseAvailable:
		# Database table emm_db_errorlog_tbl
		try:
			errorLinesCount = DbConnector.selectValue("SELECT COUNT(*) FROM emm_db_errorlog_tbl")
			if errorLinesCount > 0:
				statusText += errorColorCode + "Database table emm_db_errorlog_tbl contains error messages: " + str(errorLinesCount) + defaultColorCode + "\n"
			else:
				statusText += "Database table emm_db_errorlog_tbl: OK" + "\n"
		except:
			if ESTUtilities.isDebugMode():
				logging.exception("Database table emm_db_errorlog_tbl")
			statusText += errorColorCode + "Database table emm_db_errorlog_tbl: ERROR" + defaultColorCode + "\n"

		# Mandatory configuration values
		birtUrlLinesCount = DbConnector.selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'birt' AND name = 'url' AND value != '[to be defined]'")
		if birtUrlLinesCount == 0:
			statusText += errorColorCode + "BirtURL configvalue: ERROR ('birt.url' not set)" + defaultColorCode + "\n"
		else:
			statusText += "BirtURL configvalue: OK" + "\n"
		undefinedMailAdresses = DbConnector.select("SELECT name FROM config_tbl WHERE class = 'mailaddress' AND value = '[to be defined]'")
		if undefinedMailAdresses is not None and len(undefinedMailAdresses) > 0:
			undefinedMailAdressesString = ""
			for row in undefinedMailAdresses:
				if len(undefinedMailAdressesString) > 0:
					undefinedMailAdressesString += ", "
				undefinedMailAdressesString += row[0]
			statusText += errorColorCode + "Mailaddresses: ERROR (" + undefinedMailAdressesString + " not set)" + defaultColorCode + "\n"
		else:
			statusText += "Mailaddresses: OK" + "\n"

		# Jobqueue status
		try:
			if DbConnector.emmDbVendor == "oracle":
				result = DbConnector.select("SELECT description FROM job_queue_tbl WHERE deleted = 0 AND ((lastresult != 'OK' AND lastresult IS NOT NULL) OR (nextstart < CURRENT_TIMESTAMP - INTERVAL '15' MINUTE) OR (running = 1 AND laststart < CURRENT_TIMESTAMP - INTERVAL '5' HOUR))")
			else:
				result = DbConnector.select("SELECT description FROM job_queue_tbl WHERE deleted = 0 AND ((lastresult != 'OK' AND lastresult IS NOT NULL) OR (nextstart < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 15 MINUTE)) OR (running = 1 AND laststart < DATE_SUB(CURRENT_TIMESTAMP, INTERVAL 5 HOUR)))")
			errorneousJobs = []
			for row in result:
				description = row[0]
				errorneousJobs.append(description)
			if len(errorneousJobs) > 0:
				errorneousJobsText = ""
				errorneousJobsTextCount = 0
				for description in errorneousJobs:
					errorneousJobsTextCount = errorneousJobsTextCount + 1
					if errorneousJobsTextCount <= 3:
						if errorneousJobsTextCount > 1:
							errorneousJobsText = errorneousJobsText + ", "
						errorneousJobsText = errorneousJobsText + description
					else:
						errorneousJobsText = errorneousJobsText + ", ..."
						break
				statusText += errorColorCode + "Jobqueue status: " + str(len(errorneousJobs)) + " errorneous jobs (" + errorneousJobsText + ")" + defaultColorCode + "\n"
			else:
				statusText += "Jobqueue status: OK" + "\n"
		except:
			if ESTUtilities.isDebugMode():
				logging.exception("Check Jobqueue status")
			statusText += errorColorCode + "Check Jobqueue status: ERROR" + defaultColorCode + "\n"

		# Show DKIM domains
		if DbConnector.checkTableExists("dkim_key_tbl"):
			try:
				result = DbConnector.select("SELECT DISTINCT domain FROM dkim_key_tbl WHERE valid_end IS NULL OR valid_end > CURRENT_TIMESTAMP ORDER BY domain")
				dkimDomains = ""
				for (domain,) in result:
					if len(dkimDomains) > 0:
						dkimDomains = dkimDomains + ", "
					dkimDomains = dkimDomains + domain

				if len(dkimDomains) <= 0:
					dkimDomains = "None"

				statusText += "DKIM keys available for domains: " + dkimDomains + "\n"
			except:
				if ESTUtilities.isDebugMode():
					logging.exception("DKIM keys")
				statusText += "No DKIM keys" + "\n"
		else:
			statusText += "No DKIM keys" + "\n"

	# Java version
	try:
		statusText += "Java version: " + ESTUtilities.getJavaVersion(Environment.javaHome) + " (" + ESTUtilities.getJavaVendor(Environment.javaHome) + ")" + "\n"
	except:
		if ESTUtilities.isDebugMode():
			logging.exception("Error while checking for Java version")
		statusText += errorColorCode + "Error while checking for Java version" + defaultColorCode + "\n"

	# Tomcat version
	try:
		version = ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
		if version is not None:
			statusText += "Tomcat version: " + version + "\n"
		else:
			statusText += errorColorCode + "Detect Tomcat version: ERROR" + defaultColorCode + "\n"
	except:
		if ESTUtilities.isDebugMode():
			logging.exception("Error while detecting Tomcat version")
		statusText += errorColorCode + "Error while detecting Tomcat version" + defaultColorCode + "\n"

	# Application status
	if Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer:
		try:
			if ESTUtilities.hasRootPermissions():
				if Environment.isOpenEmmServer:
					processOutput = subprocess.check_output("su -c \"ps uxww | grep -v grep | grep org.apache.catalina | grep '" + ESTUtilities.getUserHomeDirectory("openemm") + "'\" - openemm", shell=True).decode("UTF-8")
				elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
					processOutput = subprocess.check_output("su -c \"ps uxww | grep -v grep | grep org.apache.catalina | grep '" + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "'\" - " + Environment.getFrontendApplicationUserName(), shell=True).decode("UTF-8")
				elif Environment.isEmmRdirServer:
					processOutput = subprocess.check_output("su -c \"ps uxww | grep -v grep | grep org.apache.catalina | grep '" + ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "'\" - " + Environment.getRdirApplicationUserName(), shell=True).decode("UTF-8")
			else:
				if Environment.isOpenEmmServer:
					processOutput = subprocess.check_output("ps uxww | grep -v grep | grep org.apache.catalina | grep '" + ESTUtilities.getUserHomeDirectory("openemm") + "'", shell=True).decode("UTF-8")
				elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
					processOutput = subprocess.check_output("ps uxww | grep -v grep | grep org.apache.catalina | grep '" + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "'", shell=True).decode("UTF-8")
				elif Environment.isEmmRdirServer:
					processOutput = subprocess.check_output("ps uxww | grep -v grep | grep org.apache.catalina | grep '" + ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "'", shell=True).decode("UTF-8")
			if ESTUtilities.isBlank(processOutput):
				statusText += errorColorCode + Environment.applicationName + "Application is NOT running" + defaultColorCode + "\n"
			else:
				if len(processOutput) > 0:
					processOutputParts = processOutput.split()
					processID = processOutputParts[1]
					startTime = processOutputParts[8] + " " + processOutputParts[9]

					statusText += Environment.applicationName + " Application is running (Tomcat ProcessID: " + processID + ", started at: " + startTime + ")" + "\n"
				else:
					statusText += errorColorCode + Environment.applicationName + " Application is NOT running" + defaultColorCode + "\n"
		except subprocess.CalledProcessError as e:
			if e.returncode == 1:
				statusText += errorColorCode + Environment.applicationName + " Application is NOT running" + defaultColorCode + "\n"
			else:
				if ESTUtilities.isDebugMode():
					logging.exception("Error while checking for " + Environment.applicationName + "Application running")
				statusText += errorColorCode + "Error while checking for " + Environment.applicationName + "Application running" + defaultColorCode + "\n"
		except:
			if ESTUtilities.isDebugMode():
				logging.exception("Error while checking for " + Environment.applicationName + "Application running")
			statusText += errorColorCode + "Error while checking for " + Environment.applicationName + "Application running" + defaultColorCode + "\n"

	# Backend status
	if Environment.isOpenEmmServer:
		backendStates = Environment.getOpenEmmBackendStatusList()
		if backendStates is not None:
			if len(backendStates) > 0:
				for state in backendStates:
					if state == "running" or state == "ok":
						stateColorCode = defaultColorCode
					else:
						stateColorCode = errorColorCode
					statusText += stateColorCode + Environment.applicationName + " Backend " + state + ": " + ", ".join(backendStates[state]) + defaultColorCode + "\n"
			else:
				statusText += errorColorCode + Environment.applicationName + " Backend is NOT running on this server" + defaultColorCode + "\n"
		else:
			statusText += errorColorCode + Environment.applicationName + " Backend is NOT installed or NOT running" + defaultColorCode + "\n"
	
	if Environment.isEmmMergerServer:
		backendStates = Environment.getMergerStatusList()
		if backendStates is not None:
			if len(backendStates) > 0:
				for state in backendStates:
					if state == "running" or state == "ok":
						stateColorCode = defaultColorCode
					else:
						stateColorCode = errorColorCode
					statusText += stateColorCode + Environment.applicationName + " Backend Merger " + state + ": " + ", ".join(backendStates[state]) + defaultColorCode + "\n"
			else:
				statusText += errorColorCode + Environment.applicationName + " Backend Merger is NOT running on this server" + defaultColorCode + "\n"
		else:
			statusText += errorColorCode + Environment.applicationName + " Backend Merger is NOT installed or NOT running" + defaultColorCode + "\n"

	if Environment.isEmmMailerServer:
		backendStates = Environment.getMailerStatusList()
		if backendStates is not None:
			if len(backendStates) > 0:
				for state in backendStates:
					if state == "running" or state == "ok":
						stateColorCode = defaultColorCode
					else:
						stateColorCode = errorColorCode
					statusText += stateColorCode + Environment.applicationName + " Backend Mailer " + state + ": " + ", ".join(backendStates[state]) + defaultColorCode + "\n"
			else:
				statusText += errorColorCode + Environment.applicationName + " Backend Mailer is NOT running on this server" + defaultColorCode + "\n"
		else:
			statusText += errorColorCode + Environment.applicationName + " Backend Mailer is NOT installed or NOT running" + defaultColorCode + "\n"

	if Environment.isEmmMailloopServer:
		backendStates = Environment.getMailloopStatusList()
		if backendStates is not None:
			if len(backendStates) > 0:
				for state in backendStates:
					if state == "running" or state == "ok":
						stateColorCode = defaultColorCode
					else:
						stateColorCode = errorColorCode
					statusText += stateColorCode + Environment.applicationName + " Backend Mailloop " + state + ": " + ", ".join(backendStates[state]) + defaultColorCode + "\n"
			else:
				statusText += errorColorCode + Environment.applicationName + " Backend Mailloop is NOT running on this server" + defaultColorCode + "\n"
		else:
			statusText += errorColorCode + Environment.applicationName + " Backend Mailloop is NOT installed or NOT running" + defaultColorCode + "\n"

	# Ping hostnames from system.cfg
	if Environment.systemCfgProperties is not None and len(Environment.systemCfgProperties) > 0:
		for key, value in sorted(Environment.systemCfgProperties.items()):
			if key.startswith("hostname-") and value is not None:
				for host in value.split(","):
					host = host.strip()
					if len(host) > 0:
						pingResponse = os.system("ping -c 1 " + host + " > /dev/null 2>&1")
						if pingResponse == 0:
							statusText += "Ping " + key[9:] + " (" + host + "): OK" + "\n"
						else:
							statusText += errorColorCode + "Ping " + key[9:] + " (" + host + "): ERROR" + defaultColorCode + "\n"

	statusText += CheckMenu.createCheckReport()
	return statusText

def sendConfigAndLogsAction(actionParameters):
	print("Send configuration and log files")
	print("Email address (Blank => Cancel):")

	choice = input(" > ")
	emailAddress = choice.strip().lower()
	if len(emailAddress) > 0:
		print("Please enter a password for the zip file (Blank => Cancel):")
		password = getpass.getpass(" > ")
		if len(password) > 0:
			print("Creating config and logs data zip file. This may take a moment.")

			if ESTUtilities.hasRootPermissions():
				if Environment.applicationName == "OpenEMM":
					applicationUserName = "openemm"
				elif ESTUtilities.userExists(Environment.getRdirApplicationUserName()) and Environment.username == Environment.getRdirApplicationUserName():
					applicationUserName = Environment.getRdirApplicationUserName()
				else:
					applicationUserName = Environment.getFrontendApplicationUserName()
			else:
				applicationUserName = Environment.username

			applicationUserTempDirectory = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/temp"
			if not os.path.isdir(applicationUserTempDirectory):
				ESTUtilities.createDirectory(applicationUserTempDirectory, applicationUserName)

			configAndLogsZipFilePath = applicationUserTempDirectory + "/configAndLogs.zip"

			if os.path.isfile(configAndLogsZipFilePath):
				os.remove(configAndLogsZipFilePath)

			# Server Status
			statusExportFilePath = applicationUserTempDirectory + "/status.txt"
			try:
				if os.path.isfile(statusExportFilePath):
					os.remove(statusExportFilePath)

				statusText = ""

				statusText += Environment.toolName + " v" + Environment.toolVersion + "\n"
				statusText += "Host: " + Environment.hostname + "\n"
				if License.getLicenseName() is not None:
					statusText += "License: " + License.getLicenseName() + " (ID: " + License.getLicenseID() + ")\n"

				if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName + "/tomcat/conf/version.txt")):
					with open(ESTUtilities.getUserHomeDirectory(applicationUserName + "/tomcat/conf/version.txt")) as runtimeVersionFile:
						Environment.runtimeVersion = runtimeVersionFile.read().strip()
				else:
					Environment.runtimeVersion = "Unknown"
				statusText += "Runtime Version: " + Environment.runtimeVersion + "\n"

				if Environment.isOpenEmmServer:
					if os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/manual/de"):
						manualApplicationPath = os.path.realpath(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/manual/de")
						manualVersion = ESTUtilities.getVersionFromFilename(manualApplicationPath)
						if manualVersion is not None:
							Environment.manualVersion = manualVersion
						else:
							Environment.manualVersion = "Unknown"
					else:
						Environment.manualVersion = "Unknown"
				elif Environment.isEmmFrontendServer:
					if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/manual/de"):
						manualApplicationPath = os.path.realpath(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/manual/de")
						manualVersion = ESTUtilities.getVersionFromFilename(manualApplicationPath)
						if manualVersion is not None:
							Environment.manualVersion = manualVersion
						else:
							Environment.manualVersion = "Unknown"
					else:
						Environment.manualVersion = "Unknown"

				if Environment.isEmmFrontendServer:
					statusText += Environment.applicationName + " Version: " + Environment.frontendVersion + "\n"
				elif Environment.isEmmStatisticsServer:
					statusText += Environment.applicationName + " Statistics Version: " + Environment.statisticsVersion + "\n"
				elif Environment.isEmmWebservicesServer:
					statusText += Environment.applicationName + " Webservices Version: " + Environment.webservicesVersion + "\n"
				elif Environment.isEmmConsoleRdirServer:
					statusText += Environment.applicationName + " Console-Rdir Version: " + Environment.consoleRdirVersion + "\n"
				elif Environment.isEmmRdirServer:
					statusText += Environment.applicationName + " Rdir Version: " + Environment.rdirVersion + "\n"
				elif Environment.isEmmMergerServer:
					statusText += Environment.applicationName + " Merger Version: " + Environment.mergerBackendVersion + "\n"
				elif Environment.isEmmMailerServer:
					statusText += Environment.applicationName + " Mailer Version: " + Environment.mailerBackendVersion + "\n"
				elif Environment.isEmmMailloopServer:
					statusText += Environment.applicationName + " Mailloop Version: " + Environment.mailloopBackendVersion + "\n"
				else:
					statusText += Environment.applicationName + " Version: Unknown\n"

				statusText += "System-Url: " + Environment.getSystemUrl() + "\n"

				if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName + "/release/backend/current")):
					statusText += Environment.applicationName + " Backend Version: " + os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current")) + "\n"

				if os.path.islink("/root/release/current"):
					statusText += Environment.applicationName + " Backend Runtime Version: " + os.path.basename(os.path.realpath("/root/release/current")) + "\n"

				statusText += readApplicationStatus(False)

				with open(statusExportFilePath, "w", encoding="UTF-8") as statusExportFile:
					statusExportFile.write(statusText)
				ESTUtilities.zipFile(configAndLogsZipFilePath, statusExportFilePath, password)
				os.remove(statusExportFilePath)
			except:
				print(Colors.RED + "Cannot export status" + Colors.DEFAULT)
				if ESTUtilities.isDebugMode():
					logging.exception("Cannot export status")
					raise

			# Update log files of application user
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName + "/release/log")):
				updateLogFiles = [logFile for logFile in os.listdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log") if (os.path.isfile(os.path.join(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log", logFile)) and logFile.endswith(".log"))]
				for logFile in updateLogFiles:
					ESTUtilities.zipFile(configAndLogsZipFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/" + logFile, password)

			# Update log files of root user
			if ESTUtilities.hasRootPermissions():
				if os.path.isdir("/root/release/log"):
					updateLogFiles = [logFile for logFile in os.listdir("/root/release/log") if (os.path.isfile(os.path.join("/root/release/log", logFile)) and logFile.endswith(".log"))]
					for logFile in updateLogFiles:
						ESTUtilities.zipFile(configAndLogsZipFilePath, "/root/release/log/" + logFile, password)

			# Tomcat log file
			if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/logs/catalina.out"):
				ESTUtilities.zipFile(configAndLogsZipFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/logs/catalina.out", password)

			# Frontend application log files
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/logs/webapps"):
				webappsLogFiles = [logFile for logFile in os.listdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/logs/webapps") if (os.path.isfile(os.path.join(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/logs/webapps", logFile)) and logFile.endswith(".log"))]
				for logFile in webappsLogFiles:
					ESTUtilities.zipFile(configAndLogsZipFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/logs/webapps/" + logFile, password)

			# Backend application log files
			nowDateString = datetime.datetime.now().strftime("%Y%m%d")
			for homeUsername in ESTUtilities.getListOfUsers():
				homeDir = ESTUtilities.getUserHomeDirectory(homeUsername)
				if os.path.isdir(homeDir + "/var/log"):
					varLogFiles = [homeDir + "/var/log/" + logFile for logFile in os.listdir(homeDir + "/var/log") if (os.path.isfile(os.path.join(homeDir + "/var/log", logFile)) and logFile.endswith(".log") and (nowDateString in logFile))]
					for varLogFile in varLogFiles:
						ESTUtilities.zipFile(configAndLogsZipFilePath, varLogFile, password)

			# emm and emm-ws properties
			if Environment.isOpenEmmServer:
				for (directory, directories, files) in os.walk(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps", True, None, True):
					for file in files:
						if file == "emm.properties" or file == "emm-ws.properties":
							ESTUtilities.zipFile(configAndLogsZipFilePath, os.path.join(directory, file), password)
			elif Environment.isEmmRdirServer:
				for (directory, directories, files) in os.walk(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps", True, None, True):
					for file in files:
						if file == "emm.properties" or file == "emm-ws.properties":
							ESTUtilities.zipFile(configAndLogsZipFilePath, os.path.join(directory, file), password)
			elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
				for (directory, directories, files) in os.walk(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps", True, None, True):
					for file in files:
						if file == "emm.properties" or file == "emm-ws.properties":
							ESTUtilities.zipFile(configAndLogsZipFilePath, os.path.join(directory, file), password)

			# Backend information

			# system.cfg and licence.cfg
			if Environment.systemCfgFileSearchPaths is not None and len(Environment.systemCfgFileSearchPaths) > 0:
				for systemCfgFileSearchPath in Environment.systemCfgFileSearchPaths:
					if os.path.isfile(systemCfgFileSearchPath):
						try:
							ESTUtilities.zipFile(configAndLogsZipFilePath, Environment.systemCfgFilePath, password)
						except IOError:
							print("Could not read: " + Environment.systemCfgFilePath)

			# MTA information
			if Environment.postfixVersion is not None:
				postfixInfoFilePaths = ["/etc/postfix/main.cf", "/etc/postfix/master.cf", ESTUtilities.getUserHomeDirectory("mailloop") + "/var/run/relay.domains", ESTUtilities.getUserHomeDirectory("mailloop") + "/var/run/transport.maps"]
				for postfixInfoFilePath in postfixInfoFilePaths:
					if os.path.isfile(postfixInfoFilePath):
						try:
							ESTUtilities.zipFile(configAndLogsZipFilePath, postfixInfoFilePath, password)
						except IOError:
							print("Could not read: " + postfixInfoFilePath)

			# User bin data
			userBinDataString = subprocess.check_output("for path in /home/*/bin/; do echo \"*** $path ***\"; ls -la \"$path\"; echo; done", shell=True).decode("UTF-8")
			userBinDataExportFilePath = applicationUserTempDirectory + "/userBinData.txt"
			with open(userBinDataExportFilePath, "w", encoding="UTF-8") as userBinDataExportFile:
				userBinDataExportFile.write(userBinDataString)
			ESTUtilities.zipFile(configAndLogsZipFilePath, userBinDataExportFilePath, password)
			os.remove(userBinDataExportFilePath)

			# xmlback data
			xmlbackDataString = subprocess.check_output("for path in /home/*/bin/xmlback; do if [ -x \"$path\" ]; then echo \"*** $path ***\"; \"$path\" -V; fi; done", shell=True).decode("UTF-8")
			xmlbackDataExportFilePath = applicationUserTempDirectory + "/xmlbackData.txt"
			with open(xmlbackDataExportFilePath, "w", encoding="UTF-8") as xmlbackDataExportFile:
				xmlbackDataExportFile.write(xmlbackDataString)
			ESTUtilities.zipFile(configAndLogsZipFilePath, xmlbackDataExportFilePath, password)
			os.remove(xmlbackDataExportFilePath)

			if Environment.postfixVersion is not None:
				# postconf data
				postconfDataString = subprocess.check_output("postconf", shell=True).decode("UTF-8")
				postconfDataExportFilePath = applicationUserTempDirectory + "/postconf.txt"
				with open(postconfDataExportFilePath, "w", encoding="UTF-8") as postconfDataExportFile:
					postconfDataExportFile.write(postconfDataString)
				ESTUtilities.zipFile(configAndLogsZipFilePath, postconfDataExportFilePath, password)
				os.remove(postconfDataExportFilePath)

			# config table
			configTblExportFilePath = applicationUserTempDirectory + "/configTblExport.csv"
			try:
				if os.path.isfile(configTblExportFilePath):
					os.remove(configTblExportFilePath)
				with open(configTblExportFilePath, "w", encoding="UTF-8") as configTblExportFile:
					result = DbConnector.select("SELECT class, name, hostname, value, creation_date, change_date, description FROM config_tbl ORDER BY class, name, hostname")
					configTblExportFile.write(ESTUtilities.encodeCSVLine("class", "name", "hostname", "value", "creation_date", "change_date", "description"))
					for row in result:
						configClass = row[0]
						name = row[1]
						hostname = row[2]
						value = row[3]
						creation_date = row[4]
						change_date = row[5]
						description = row[6]
						configTblExportFile.write(ESTUtilities.encodeCSVLine(configClass, name, hostname, value, creation_date, change_date, description))

				ESTUtilities.zipFile(configAndLogsZipFilePath, configTblExportFilePath, password)
				os.remove(configTblExportFilePath)
			except:
				print(Colors.RED + "Cannot export config_tbl" + Colors.DEFAULT)
				if ESTUtilities.isDebugMode():
					logging.exception("Cannot export config_tbl")
					raise

			# company info table
			companyInfoTblExportFilePath = applicationUserTempDirectory + "/companyInfoTblExport.csv"
			try:
				if os.path.isfile(companyInfoTblExportFilePath):
					os.remove(companyInfoTblExportFilePath)
				with open(companyInfoTblExportFilePath, "w", encoding="UTF-8") as companyInfoTblExportFile:
					result = DbConnector.select("SELECT company_id, cname, hostname, cvalue, creation_date, timestamp, description FROM company_info_tbl ORDER BY company_id, cname, hostname")
					companyInfoTblExportFile.write(ESTUtilities.encodeCSVLine("company_id", "cname", "hostname", "cvalue", "creation_date", "timestamp", "description"))
					for row in result:
						companyID = row[0]
						cname = row[1]
						hostname = row[2]
						cvalue = row[3]
						creation_date = row[4]
						timestamp = row[5]
						description = row[6]
						companyInfoTblExportFile.write(ESTUtilities.encodeCSVLine(companyID, cname, hostname, cvalue, creation_date, timestamp, description))

				ESTUtilities.zipFile(configAndLogsZipFilePath, companyInfoTblExportFilePath, password)
				os.remove(companyInfoTblExportFilePath)
			except:
				print(Colors.RED + "Cannot export company_info_tbl" + Colors.DEFAULT)
				if ESTUtilities.isDebugMode():
					logging.exception("Cannot export company_info_tbl")
					raise
			finally:
				if os.path.isfile(configTblExportFilePath):
					os.remove(configTblExportFilePath)

			# releaselog table
			releaselogTblExportFilePath = applicationUserTempDirectory + "/releaselogTblExport.csv"
			try:
				if os.path.isfile(releaselogTblExportFilePath):
					os.remove(releaselogTblExportFilePath)
				with open(releaselogTblExportFilePath, "w", encoding="UTF-8") as releaselogTblExportFile:
					result = DbConnector.select("SELECT startup_timestamp, host_name, application_name, version_number FROM release_log_tbl ORDER BY startup_timestamp")
					releaselogTblExportFile.write(ESTUtilities.encodeCSVLine("startup_timestamp", "host_name", "application_name", "version_number"))
					for row in result:
						startupTimestamp = row[0]
						hostname = row[1]
						applicationName = row[2]
						versionNumber = row[3]
						releaselogTblExportFile.write(ESTUtilities.encodeCSVLine(str(startupTimestamp), hostname, applicationName, versionNumber))

				ESTUtilities.zipFile(configAndLogsZipFilePath, releaselogTblExportFilePath, password)
				os.remove(releaselogTblExportFilePath)
			except:
				print(Colors.RED + "Cannot export release_log_tbl" + Colors.DEFAULT)
				if ESTUtilities.isDebugMode():
					logging.exception("Cannot export release_log_tbl")
					raise

			if Environment.applicationName == "EMM":
				# ServerSet table
				serverSetTblExportFilePath = applicationUserTempDirectory + "/serverSetTblExport.csv"
				try:
					if os.path.isfile(serverSetTblExportFilePath):
						os.remove(serverSetTblExportFilePath)
					with open(serverSetTblExportFilePath, "w", encoding="UTF-8") as serverSetTblExportFile:
						result = DbConnector.select("SELECT set_id, mailer FROM serverset_tbl ORDER BY set_id, mailer")
						serverSetTblExportFile.write(ESTUtilities.encodeCSVLine("set_id", "mailer"))
						for row in result:
							setID = row[0]
							mailer = row[1]
							serverSetTblExportFile.write(ESTUtilities.encodeCSVLine(setID, mailer))

					ESTUtilities.zipFile(configAndLogsZipFilePath, serverSetTblExportFilePath, password)
					os.remove(serverSetTblExportFilePath)
				except:
					print(Colors.RED + "Cannot export serverset_tbl" + Colors.DEFAULT)
					if ESTUtilities.isDebugMode():
						logging.exception("Cannot export serverset_tbl")
						raise
				finally:
					if os.path.isfile(serverSetTblExportFilePath):
						os.remove(serverSetTblExportFilePath)

				# ServerSetDesc table
				serverSetDescTblExportFilePath = applicationUserTempDirectory + "/serverSetDescTblExport.csv"
				try:
					if os.path.isfile(serverSetDescTblExportFilePath):
						os.remove(serverSetDescTblExportFilePath)
					with open(serverSetDescTblExportFilePath, "w", encoding="UTF-8") as serverSetDescTblExportFile:
						result = DbConnector.select("SELECT set_id, set_name FROM serverset_desc_tbl ORDER BY set_id")
						serverSetDescTblExportFile.write(ESTUtilities.encodeCSVLine("set_id", "set_name"))
						for row in result:
							setID = row[0]
							setName = row[1]
							serverSetDescTblExportFile.write(ESTUtilities.encodeCSVLine(setID, setName))

					ESTUtilities.zipFile(configAndLogsZipFilePath, serverSetDescTblExportFilePath, password)
					os.remove(serverSetDescTblExportFilePath)
				except:
					print(Colors.RED + "Cannot export serverset_desc_tbl" + Colors.DEFAULT)
					if ESTUtilities.isDebugMode():
						logging.exception("Cannot export serverset_desc_tbl")
						raise
				finally:
					if os.path.isfile(serverSetDescTblExportFilePath):
						os.remove(serverSetDescTblExportFilePath)

				# ServerProp table
				serverPropTblExportFilePath = applicationUserTempDirectory + "/serverPropTblExport.csv"
				try:
					if os.path.isfile(serverPropTblExportFilePath):
						os.remove(serverPropTblExportFilePath)
					with open(serverPropTblExportFilePath, "w", encoding="UTF-8") as serverPropTblExportFile:
						result = DbConnector.select("SELECT mailer, mvar, mval, mailer_id FROM serverprop_tbl ORDER BY mailer, mvar")
						serverPropTblExportFile.write(ESTUtilities.encodeCSVLine("mailer", "mvar", "mval", "mailer_id"))
						for row in result:
							mailer = row[0]
							mvar = row[1]
							mval = row[2]
							mailerID = row[3]
							serverPropTblExportFile.write(ESTUtilities.encodeCSVLine(mailer, mvar, mval, mailerID))

					ESTUtilities.zipFile(configAndLogsZipFilePath, serverPropTblExportFilePath, password)
					os.remove(serverPropTblExportFilePath)
				except:
					print(Colors.RED + "Cannot export serverprop_tbl" + Colors.DEFAULT)
					if ESTUtilities.isDebugMode():
						logging.exception("Cannot export serverprop_tbl")
						raise
				finally:
					if os.path.isfile(serverPropTblExportFilePath):
						os.remove(serverPropTblExportFilePath)

			# Company table
			companyTblExportFilePath = applicationUserTempDirectory + "/companyTblExport.csv"
			try:
				if os.path.isfile(companyTblExportFilePath):
					os.remove(companyTblExportFilePath)
				with open(companyTblExportFilePath, "w", encoding="UTF-8") as companyTblExportFile:
					result = DbConnector.select("SELECT company_id, shortname, status, mailerset, rdir_domain, mailloop_domain FROM company_tbl ORDER BY company_id")
					companyTblExportFile.write(ESTUtilities.encodeCSVLine("company_id", "shortname", "status", "mailerset", "rdir_domain", "mailloop_domain"))
					for row in result:
						companyID = row[0]
						shortname = row[1]
						status = row[2]
						mailerset = row[3]
						rdirDomain = row[4]
						mailloopDomain = row[5]
						companyTblExportFile.write(ESTUtilities.encodeCSVLine(companyID, shortname, status, mailerset, rdirDomain, mailloopDomain))

				ESTUtilities.zipFile(configAndLogsZipFilePath, companyTblExportFilePath, password)
				os.remove(companyTblExportFilePath)
			except:
				print(Colors.RED + "Cannot export company_tbl" + Colors.DEFAULT)
				if ESTUtilities.isDebugMode():
					logging.exception("Cannot export company_tbl")
					raise
			finally:
				if os.path.isfile(companyTblExportFilePath):
					os.remove(companyTblExportFilePath)

			# Home directory files tree
			homeDirectoryTreeExportFilePath = applicationUserTempDirectory + "/homeDirectoryTree.txt"
			try:
				if os.path.isfile(homeDirectoryTreeExportFilePath):
					os.remove(homeDirectoryTreeExportFilePath)
				with open(homeDirectoryTreeExportFilePath, "w", encoding="UTF-8") as homeDirectoryTreeExportFile:
					homeDirectoryTreeExportFile.write(os.path.abspath(os.path.join(ESTUtilities.getUserHomeDirectory(Environment.username), os.pardir)) + os.sep)
					for directory, linkNames, dirNames, fileNames in ESTUtilities.walkSorted(ESTUtilities.getUserHomeDirectory(Environment.username)):
						level = directory.replace(ESTUtilities.getUserHomeDirectory(Environment.username), "").count(os.sep)
						changeTimeString = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.path.getmtime(directory)))
						homeDirectoryTreeExportFile.write("\t" * level + os.path.basename(directory) + "/\tchanged: " + changeTimeString + "\n")
						indent = "\t" * (level + 1)
						for fileName in fileNames:
							changeTimeString = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.path.getmtime(os.path.join(directory, fileName))))
							homeDirectoryTreeExportFile.write(indent + fileName + "\tchanged: " + changeTimeString + " size: " + str(os.path.getsize(os.path.join(directory, fileName))) + "\n")
						for linkName in linkNames:
							changeTimeString = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.lstat(os.path.join(directory, linkName)).st_mtime))
							homeDirectoryTreeExportFile.write(indent + linkName + " -> " + os.path.realpath(os.path.join(directory, linkName)) + "\tchanged: " + changeTimeString + "\n")
				ESTUtilities.zipFile(configAndLogsZipFilePath, homeDirectoryTreeExportFilePath, password)
			except:
				print(Colors.RED + "Cannot export Home directory files tree" + Colors.DEFAULT)
				if ESTUtilities.isDebugMode():
					logging.exception("Cannot export Home directory files tree")
					raise
			finally:
				if os.path.isfile(homeDirectoryTreeExportFilePath):
					os.remove(homeDirectoryTreeExportFilePath)

			# Send email
			try:
				emailText = ""

				emailText += Environment.toolName + " v" + Environment.toolVersion + "\n"
				emailText += "Host: " + Environment.hostname + "\n"

				if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/version.txt"):
					with open(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/version.txt") as runtimeVersionFile:
						Environment.runtimeVersion = runtimeVersionFile.read().strip()
				else:
					Environment.runtimeVersion = "Unknown"
				emailText += "Runtime Version: " + Environment.runtimeVersion + "\n"

				if Environment.isOpenEmmServer:
					emailText += Environment.applicationName + " Version: " + Environment.frontendVersion + "\n"
				elif Environment.isEmmFrontendServer:
					emailText += Environment.applicationName + " Version: " + Environment.frontendVersion + "\n"
				elif Environment.isEmmStatisticsServer:
					emailText += Environment.applicationName + " Statistics Version: " + Environment.statisticsVersion + "\n"
				elif Environment.isEmmWebservicesServer:
					emailText += Environment.applicationName + " Webservices Version: " + Environment.webservicesVersion + "\n"
				elif Environment.isEmmConsoleRdirServer:
					emailText += Environment.applicationName + " Console-Rdir Version: " + Environment.consoleRdirVersion + "\n"
				elif Environment.isEmmRdirServer:
					emailText += Environment.applicationName + " Rdir Version: " + Environment.rdirVersion + "\n"
				elif Environment.isEmmMergerServer:
					emailText += Environment.applicationName + " Merger Version: " + Environment.mergerBackendVersion + "\n"
				elif Environment.isEmmMailerServer:
					emailText += Environment.applicationName + " Mailer Version: " + Environment.mailerBackendVersion + "\n"
				elif Environment.isEmmMailloopServer:
					emailText += Environment.applicationName + " Mailloop Version: " + Environment.mailloopBackendVersion + "\n"
				else:
					emailText += Environment.applicationName + " Version: Unknown\n"

				emailText += "System-Url: " + Environment.getSystemUrl() + "\n"

				if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current"):
					emailText += Environment.applicationName + " Backend Version: " + os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current")) + "\n"

				ESTUtilities.sendEmail(Environment.applicationName + "Installer@" + Environment.hostname, [emailAddress], Environment.applicationName + "Installer@" + Environment.hostname + ": configuration and logs", emailText, [configAndLogsZipFilePath])
			except:
				Environment.errors.append("Cannot send email. File created for manually sending: " + configAndLogsZipFilePath)
				if ESTUtilities.isDebugMode():
					logging.exception("Cannot send email")

			if Environment.errors is None or len(Environment.errors) == 0:
				Environment.messages.append("Email with configuration and log files was sent to '" + emailAddress + "'\nFor content see " + configAndLogsZipFilePath)
		else:
			Environment.errors.append("Send configuration and log files canceled (no password)")
	else:
		Environment.errors.append("Send configuration and log files canceled (no email address)")
