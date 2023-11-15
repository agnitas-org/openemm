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
import stat
import sys
import logging
import datetime
import subprocess
import getpass
import time

from pathlib import Path

from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities
from EMT_lib import License
from EMT_lib.Environment import Environment
from EMT_lib.License import License
from EMT_lib import CheckMenu

def statusMenuAction(actionParameters):
	print(Environment.applicationName + " status:")
	print(readApplicationStatus(True))
	choice = input(" > ")

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

	if not EMTUtilities.sslIsAvailable():
		statusText += errorColorCode + "SSL connections are not supported by the current python installation" + defaultColorCode + "\n"

	# File permissions
	if DbConnector.dbcfgPropertiesFilePath != None:
		if os.path.isfile(DbConnector.dbcfgPropertiesFilePath):
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

	Environment.sendmailVersion = EMTUtilities.getSendmailVersion()
	Environment.postfixVersion = EMTUtilities.getPostfixVersion()
	if Environment.sendmailVersion is not None:
		statusText += "Sendmail Version: " + Environment.sendmailVersion + "\n"
	if Environment.postfixVersion is not None:
		statusText += "Postfix Version: " + Environment.postfixVersion + "\n"
	if Environment.sendmailVersion is None and Environment.postfixVersion is None:
		statusText += errorColorCode + "NO MTA (sendmail or postfix) found" + defaultColorCode + "\n"

	# Database client status
	if DbConnector.getDbClientPath() is not None:
		try:
			dbClientVersion = DbConnector.getDbClientVersion()
			if dbClientVersion is None:
				statusText += errorColorCode + "Database client status: NOT available" + defaultColorCode + "\n"
			else:
				statusText += "Database client version: " + dbClientVersion + "\n"
		except:
			if EMTUtilities.isDebugMode():
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
			if EMTUtilities.isDebugMode():
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
			if EMTUtilities.isDebugMode():
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
			if EMTUtilities.isDebugMode():
				logging.exception("Detect database version")
			statusText += errorColorCode + "Detect database version: ERROR" + defaultColorCode + "\n"

		# Check for existing database scheme
		if DbConnector.checkDbStructureExists():
			structureVersion = "Unknown"
			try:
				structureVersion = DbConnector.selectValue("SELECT MAX(version_number) FROM agn_dbversioninfo_tbl")
			except:
				if EMTUtilities.isDebugMode():
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
			if EMTUtilities.isDebugMode():
				logging.exception("Database table emm_db_errorlog_tbl")
			statusText += errorColorCode + "Database table emm_db_errorlog_tbl: ERROR" + defaultColorCode + "\n"

		# Mandatory configuration values
		birtUrlLinesCount = DbConnector.selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'birt' AND name = 'url' AND value != '[to be defined]'")
		if birtUrlLinesCount == 0:
			statusText += errorColorCode + "BirtURL configvalue: ERROR ('birt.url' not set)" + defaultColorCode + "\n"
		else:
			statusText += "BirtURL configvalue: OK" + "\n"
		undefinedMailAdresses = DbConnector.select("SELECT name FROM config_tbl WHERE class = 'mailaddress' AND value = '[to be defined]'")
		if undefinedMailAdresses != None and len(undefinedMailAdresses) > 0:
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
			if EMTUtilities.isDebugMode():
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
				if EMTUtilities.isDebugMode():
					logging.exception("DKIM keys")
				statusText += "No DKIM keys" + "\n"
		else:
			statusText += "No DKIM keys" + "\n"

	# Java version
	try:
		statusText += "Java version: " + EMTUtilities.getJavaVersion(Environment.javaHome) + " (" + EMTUtilities.getJavaVendor(Environment.javaHome) + ")" + "\n"
	except:
		if EMTUtilities.isDebugMode():
			logging.exception("Error while checking for Java version")
		statusText += errorColorCode + "Error while checking for Java version" + defaultColorCode + "\n"

	# Tomcat version
	try:
		version = EMTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
		if version is not None:
			statusText += "Tomcat version: " + version + "\n"
		else:
			statusText += errorColorCode + "Detect Tomcat version: ERROR" + defaultColorCode + "\n"
	except:
		if EMTUtilities.isDebugMode():
			logging.exception("Error while detecting Tomcat version")
		statusText += errorColorCode + "Error while detecting Tomcat version" + defaultColorCode + "\n"

	# Tomcat-Native version
	try:
		version = EMTUtilities.getTomcatNativeVersion(Environment.tomcatNative)
		if version is not None:
			statusText += "Tomcat-Native version: " + version + "\n"
		else:
			statusText += "Tomcat-Native version: Undefined\n"
	except:
		if EMTUtilities.isDebugMode():
			logging.exception("Error while detecting Tomcat-Native version")
		statusText += errorColorCode + "Error while detecting Tomcat-Native version" + defaultColorCode + "\n"

	# Wkhtml version
	if Environment.wkhtmltopdf is not None and os.path.isfile(Environment.wkhtmltopdf):
		try:
			process = subprocess.Popen([Environment.wkhtmltopdf, "-V"], stdout=subprocess.PIPE)
			processOutput, processError = process.communicate()
			if processOutput is not None:
				processOutput = processOutput.decode("UTF-8").strip()

			if processError is None and processOutput is not None:
				version = processOutput.strip()
				statusText += "Wkhtml version: " + version + "\n"
			else:
				statusText += errorColorCode + "Detect Wkhtml version: ERROR" + defaultColorCode + "\n"
				statusText += errorColorCode + "error: " + processError + defaultColorCode + "\n"
		except:
			if EMTUtilities.isDebugMode():
				logging.exception("Error while detecting Wkhtml version")
			statusText += errorColorCode + "Error while detecting Wkhtml version" + defaultColorCode + "\n"
	else:
		if not Environment.isEmmRdirServer:
			statusText += errorColorCode + "Wkhtml file not available: " + str(Environment.wkhtmltopdf) + defaultColorCode + "\n"

	# Application status
	if Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer:
		try:
			if EMTUtilities.hasRootPermissions():
				if Environment.isOpenEmmServer:
					processOutput = subprocess.check_output("su -c \"ps ux | grep -v grep | grep org.apache.catalina | grep '/home/openemm'\" openemm", shell=True).decode("UTF-8")
				else:
					if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
						processOutput = subprocess.check_output("su -c \"ps ux | grep -v grep | grep org.apache.catalina | grep '/home/console'\" console", shell=True).decode("UTF-8")
					elif Environment.isEmmConsoleRdirServer:
						processOutput = subprocess.check_output("su -c \"ps ux | grep -v grep | grep org.apache.catalina | grep '/home/rdir'\" rdir", shell=True).decode("UTF-8")

			else:
				if Environment.isOpenEmmServer:
					processOutput = subprocess.check_output("ps ux | grep -v grep | grep org.apache.catalina | grep '/home/openemm'", shell=True).decode("UTF-8")
				else:
					if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
						processOutput = subprocess.check_output("ps ux | grep -v grep | grep org.apache.catalina | grep '/home/console'", shell=True).decode("UTF-8")
					elif Environment.isEmmRdirServer:
						processOutput = subprocess.check_output("ps ux | grep -v grep | grep org.apache.catalina | grep '/home/rdir'", shell=True).decode("UTF-8")
			if EMTUtilities.isBlank(processOutput):
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
				if EMTUtilities.isDebugMode():
					logging.exception("Error while checking for " + Environment.applicationName + "Application running")
				statusText += errorColorCode + "Error while checking for " + Environment.applicationName + "Application running" + defaultColorCode + "\n"
		except:
			if EMTUtilities.isDebugMode():
				logging.exception("Error while checking for " + Environment.applicationName + "Application running")
			statusText += errorColorCode + "Error while checking for " + Environment.applicationName + "Application running" + defaultColorCode + "\n"

	# Backend status
	if Environment.isOpenEmmServer or Environment.isEmmMergerServer or Environment.isEmmMailerServer or Environment.isEmmMailloopServer:
		if os.path.isfile("/home/" + Environment.getBackendApplicationUserName() + "/bin/backend.sh"):
			try:
				# Text output comes via stderr
				if EMTUtilities.hasRootPermissions():
					processOutput = subprocess.check_output("su -c \"/home/" + Environment.getBackendApplicationUserName() + "/bin/backend.sh status\" " + Environment.getBackendApplicationUserName(), stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				else:
					processOutput = subprocess.check_output("/home/" + Environment.getBackendApplicationUserName() + "/bin/backend.sh status", stderr=subprocess.STDOUT, shell=True).decode("UTF-8")
				if EMTUtilities.isBlank(processOutput):
					statusText += errorColorCode + Environment.applicationName + "Backend is NOT installed or NOT running" + defaultColorCode + "\n"
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
						statusText += errorColorCode + Environment.applicationName + " Backend is NOT running" + defaultColorCode + "\n"
			except subprocess.CalledProcessError as e:
				print(e)
				if e.returncode == 1:
					statusText += errorColorCode + Environment.applicationName + " Backend is NOT installed or NOT running" + defaultColorCode + "\n"
				else:
					if EMTUtilities.isDebugMode():
						logging.exception("Error while checking for Backend running")
					statusText += errorColorCode + "Error while checking for Backend running" + defaultColorCode + "\n"
			except:
				if EMTUtilities.isDebugMode():
					logging.exception("Error while checking for Backend running")
				statusText += errorColorCode + "Error while checking for Backend running" + defaultColorCode + "\n"

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

			if EMTUtilities.hasRootPermissions():
				if Environment.applicationName == "OpenEMM":
					applicationUserName = "openemm"
				else:
					applicationUserName = "console"
			else:
				applicationUserName = Environment.username

			applicationUserTempDirectory = "/home/" + applicationUserName + "/temp"
			if not os.path.isdir(applicationUserTempDirectory):
				EMTUtilities.createDirectory(applicationUserTempDirectory, applicationUserName)

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

				if os.path.isfile("/home/" + applicationUserName + "/tomcat/conf/version.txt"):
					with open("/home/" + applicationUserName + "/tomcat/conf/version.txt") as runtimeVersionFile:
						Environment.runtimeVersion = runtimeVersionFile.read().strip()
				else:
					Environment.runtimeVersion = "Unknown"
				statusText += "Runtime Version: " + Environment.runtimeVersion + "\n"

				if Environment.isOpenEmmServer:
					if os.path.isdir("/home/openemm/webapps/manual/de"):
						manualApplicationPath = os.path.realpath("/home/openemm/webapps/manual/de")
						manualVersion = EMTUtilities.getVersionFromFilename(manualApplicationPath)
						if manualVersion is not None:
							Environment.manualVersion = manualVersion
						else:
							Environment.manualVersion = "Unknown"
					else:
						Environment.manualVersion = "Unknown"
				elif Environment.isEmmFrontendServer:
					if os.path.isdir("/home/console/webapps/manual/de"):
						manualApplicationPath = os.path.realpath("/home/console/webapps/manual/de")
						manualVersion = EMTUtilities.getVersionFromFilename(manualApplicationPath)
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

				if os.path.islink("/home/" + applicationUserName + "/release/backend/current"):
					statusText += Environment.applicationName + "Backend Version: " + os.path.basename(os.path.realpath("/home/" + applicationUserName + "/release/backend/current")) + "\n"

				if os.path.islink("/root/release/current"):
					statusText += Environment.applicationName + "Backend Startup Version: " + os.path.basename(os.path.realpath("/root/release/current")) + "\n"

				statusText += readApplicationStatus(False)

				with open(statusExportFilePath, "w", encoding="UTF-8") as statusExportFile:
					statusExportFile.write(statusText)
				EMTUtilities.zipFile(configAndLogsZipFilePath, statusExportFilePath, password)
				os.remove(statusExportFilePath)
			except:
				print(Colors.RED + "Cannot export status" + Colors.DEFAULT)
				if EMTUtilities.isDebugMode():
					logging.exception("Cannot export status")
					raise

			# Update log files of application user
			if os.path.isdir("/home/" + applicationUserName + "/release/log"):
				updateLogFiles = [logFile for logFile in os.listdir("/home/" + applicationUserName + "/release/log") if (os.path.isfile(os.path.join("/home/" + applicationUserName + "/release/log", logFile)) and logFile.endswith(".log"))]
				for logFile in updateLogFiles:
					EMTUtilities.zipFile(configAndLogsZipFilePath, "/home/" + applicationUserName + "/release/log/" + logFile, password)

			# Update log files of root user
			if EMTUtilities.hasRootPermissions():
				if os.path.isdir("/root/release/log"):
					updateLogFiles = [logFile for logFile in os.listdir("/root/release/log") if (os.path.isfile(os.path.join("/root/release/log", logFile)) and logFile.endswith(".log"))]
					for logFile in updateLogFiles:
						EMTUtilities.zipFile(configAndLogsZipFilePath, "/root/release/log/" + logFile, password)

			# Tomcat log file
			if os.path.isfile("/home/" + applicationUserName + "/logs/catalina.out"):
				EMTUtilities.zipFile(configAndLogsZipFilePath, "/home/" + applicationUserName + "/logs/catalina.out", password)

			# Frontend application log files
			if os.path.isdir("/home/" + applicationUserName + "/logs/webapps"):
				webappsLogFiles = [logFile for logFile in os.listdir("/home/" + applicationUserName + "/logs/webapps") if (os.path.isfile(os.path.join("/home/" + applicationUserName + "/logs/webapps", logFile)) and logFile.endswith(".log"))]
				for logFile in webappsLogFiles:
					EMTUtilities.zipFile(configAndLogsZipFilePath, "/home/" + applicationUserName + "/logs/webapps/" + logFile, password)

			# Backend application log files
			nowDateString = datetime.datetime.now().strftime("%Y%m%d")
			for homeUsername in os.listdir("/home"):
				homeDir = "/home/" + homeUsername
				if os.path.isdir(homeDir + "/var/log"):
					varLogFiles = [homeDir + "/var/log/" + logFile for logFile in os.listdir(homeDir + "/var/log") if (os.path.isfile(os.path.join(homeDir + "/var/log", logFile)) and logFile.endswith(".log") and (nowDateString in logFile))]
					for varLogFile in varLogFiles:
						EMTUtilities.zipFile(configAndLogsZipFilePath, varLogFile, password)

			# emm and emm-ws properties
			if Environment.isOpenEmmServer:
				for (directory, directories, files) in os.walk("/home/openemm/webapps", True, None, True):
					for file in files:
						if file == "emm.properties" or file == "emm-ws.properties":
							EMTUtilities.zipFile(configAndLogsZipFilePath, os.path.join(directory, file), password)
			elif Environment.isEmmRdirServer:
				for (directory, directories, files) in os.walk("/home/rdir/webapps", True, None, True):
					for file in files:
						if file == "emm.properties" or file == "emm-ws.properties":
							EMTUtilities.zipFile(configAndLogsZipFilePath, os.path.join(directory, file), password)
			elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
				for (directory, directories, files) in os.walk("/home/console/webapps", True, None, True):
					for file in files:
						if file == "emm.properties" or file == "emm-ws.properties":
							EMTUtilities.zipFile(configAndLogsZipFilePath, os.path.join(directory, file), password)

			# Backend information

			# system.cfg and licence.cfg
			if Environment.systemCfgFileSearchPaths is not None and len(Environment.systemCfgFileSearchPaths) > 0:
				for systemCfgFileSearchPath in Environment.systemCfgFileSearchPaths:
					if os.path.isfile(systemCfgFileSearchPath):
						try:
							EMTUtilities.zipFile(configAndLogsZipFilePath, Environment.systemCfgFilePath, password)
						except IOError:
							print("Could not read: " + Environment.systemCfgFilePath)

			# MTA information
			if Environment.sendmailVersion is not None:
				sendmailInfoFilePaths = ["/etc/mail/sendmail.mc", "/etc/mail/relay-domains", "/etc/mail/mailertable"]
				for sendmailInfoFilePath in sendmailInfoFilePaths:
					if os.path.isfile(sendmailInfoFilePath):
						try:
							EMTUtilities.zipFile(configAndLogsZipFilePath, sendmailInfoFilePath, password)
						except IOError:
							print("Could not read: " + sendmailInfoFilePath)

			if Environment.postfixVersion is not None:
				postfixInfoFilePaths = ["/etc/postfix/main.cf", "/etc/postfix/master.cf", "/home/mailloop/var/run/relay.domains", "/home/mailloop/var/run/transport.maps"]
				for postfixInfoFilePath in postfixInfoFilePaths:
					if os.path.isfile(postfixInfoFilePath):
						try:
							EMTUtilities.zipFile(configAndLogsZipFilePath, postfixInfoFilePath, password)
						except IOError:
							print("Could not read: " + postfixInfoFilePath)

			# User bin data
			userBinDataString = subprocess.check_output("for path in /home/*/bin/; do echo \"*** $path ***\"; ls -la \"$path\"; echo; done", shell=True).decode("UTF-8")
			userBinDataExportFilePath = applicationUserTempDirectory + "/userBinData.txt"
			with open(userBinDataExportFilePath, "w", encoding="UTF-8") as userBinDataExportFile:
				userBinDataExportFile.write(userBinDataString)
			EMTUtilities.zipFile(configAndLogsZipFilePath, userBinDataExportFilePath, password)
			os.remove(userBinDataExportFilePath)

			# xmlback data
			xmlbackDataString = subprocess.check_output("for path in /home/*/bin/xmlback; do if [ -x \"$path\" ]; then echo \"*** $path ***\"; \"$path\" -V; fi; done", shell=True).decode("UTF-8")
			xmlbackDataExportFilePath = applicationUserTempDirectory + "/xmlbackData.txt"
			with open(xmlbackDataExportFilePath, "w", encoding="UTF-8") as xmlbackDataExportFile:
				xmlbackDataExportFile.write(xmlbackDataString)
			EMTUtilities.zipFile(configAndLogsZipFilePath, xmlbackDataExportFilePath, password)
			os.remove(xmlbackDataExportFilePath)

			if Environment.postfixVersion is not None:
				# postconf data
				postconfDataString = subprocess.check_output("postconf", shell=True).decode("UTF-8")
				postconfDataExportFilePath = applicationUserTempDirectory + "/postconf.txt"
				with open(postconfDataExportFilePath, "w", encoding="UTF-8") as postconfDataExportFile:
					postconfDataExportFile.write(postconfDataString)
				EMTUtilities.zipFile(configAndLogsZipFilePath, postconfDataExportFilePath, password)
				os.remove(postconfDataExportFilePath)

			# config table
			configTblExportFilePath = applicationUserTempDirectory + "/configTblExport.csv"
			try:
				if os.path.isfile(configTblExportFilePath):
					os.remove(configTblExportFilePath)
				with open(configTblExportFilePath, "w", encoding="UTF-8") as configTblExportFile:
					result = DbConnector.select("SELECT class, name, hostname, value, creation_date, change_date, description FROM config_tbl ORDER BY class, name, hostname")
					configTblExportFile.write(EMTUtilities.encodeCSVLine("class", "name", "hostname", "value", "creation_date", "change_date", "description"))
					for row in result:
						configClass = row[0]
						name = row[1]
						hostname = row[2]
						value = row[3]
						creation_date = row[4]
						change_date = row[5]
						description = row[6]
						configTblExportFile.write(EMTUtilities.encodeCSVLine(configClass, name, hostname, value, creation_date, change_date, description))

				EMTUtilities.zipFile(configAndLogsZipFilePath, configTblExportFilePath, password)
				os.remove(configTblExportFilePath)
			except:
				print(Colors.RED + "Cannot export config_tbl" + Colors.DEFAULT)
				if EMTUtilities.isDebugMode():
					logging.exception("Cannot export config_tbl")
					raise

			# company info table
			companyInfoTblExportFilePath = applicationUserTempDirectory + "/companyInfoTblExport.csv"
			try:
				if os.path.isfile(companyInfoTblExportFilePath):
					os.remove(companyInfoTblExportFilePath)
				with open(companyInfoTblExportFilePath, "w", encoding="UTF-8") as companyInfoTblExportFile:
					result = DbConnector.select("SELECT company_id, cname, hostname, cvalue, creation_date, timestamp, description FROM company_info_tbl ORDER BY company_id, cname, hostname")
					companyInfoTblExportFile.write(EMTUtilities.encodeCSVLine("company_id", "cname", "hostname", "cvalue", "creation_date", "timestamp", "description"))
					for row in result:
						companyID = row[0]
						cname = row[1]
						hostname = row[2]
						cvalue = row[3]
						creation_date = row[4]
						timestamp = row[5]
						description = row[6]
						companyInfoTblExportFile.write(EMTUtilities.encodeCSVLine(companyID, cname, hostname, cvalue, creation_date, timestamp, description))

				EMTUtilities.zipFile(configAndLogsZipFilePath, companyInfoTblExportFilePath, password)
				os.remove(companyInfoTblExportFilePath)
			except:
				print(Colors.RED + "Cannot export company_info_tbl" + Colors.DEFAULT)
				if EMTUtilities.isDebugMode():
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
					releaselogTblExportFile.write(EMTUtilities.encodeCSVLine("startup_timestamp", "host_name", "application_name", "version_number"))
					for row in result:
						startupTimestamp = row[0]
						hostname = row[1]
						applicationName = row[2]
						versionNumber = row[3]
						releaselogTblExportFile.write(EMTUtilities.encodeCSVLine(str(startupTimestamp), hostname, applicationName, versionNumber))

				EMTUtilities.zipFile(configAndLogsZipFilePath, releaselogTblExportFilePath, password)
				os.remove(releaselogTblExportFilePath)
			except:
				print(Colors.RED + "Cannot export release_log_tbl" + Colors.DEFAULT)
				if EMTUtilities.isDebugMode():
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
						serverSetTblExportFile.write(EMTUtilities.encodeCSVLine("set_id", "mailer"))
						for row in result:
							setID = row[0]
							mailer = row[1]
							serverSetTblExportFile.write(EMTUtilities.encodeCSVLine(setID, mailer))

					EMTUtilities.zipFile(configAndLogsZipFilePath, serverSetTblExportFilePath, password)
					os.remove(serverSetTblExportFilePath)
				except:
					print(Colors.RED + "Cannot export serverset_tbl" + Colors.DEFAULT)
					if EMTUtilities.isDebugMode():
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
						serverSetDescTblExportFile.write(EMTUtilities.encodeCSVLine("set_id", "set_name"))
						for row in result:
							setID = row[0]
							setName = row[1]
							serverSetDescTblExportFile.write(EMTUtilities.encodeCSVLine(setID, setName))

					EMTUtilities.zipFile(configAndLogsZipFilePath, serverSetDescTblExportFilePath, password)
					os.remove(serverSetDescTblExportFilePath)
				except:
					print(Colors.RED + "Cannot export serverset_desc_tbl" + Colors.DEFAULT)
					if EMTUtilities.isDebugMode():
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
						serverPropTblExportFile.write(EMTUtilities.encodeCSVLine("mailer", "mvar", "mval", "mailer_id"))
						for row in result:
							mailer = row[0]
							mvar = row[1]
							mval = row[2]
							mailerID = row[3]
							serverPropTblExportFile.write(EMTUtilities.encodeCSVLine(mailer, mvar, mval, mailerID))

					EMTUtilities.zipFile(configAndLogsZipFilePath, serverPropTblExportFilePath, password)
					os.remove(serverPropTblExportFilePath)
				except:
					print(Colors.RED + "Cannot export serverprop_tbl" + Colors.DEFAULT)
					if EMTUtilities.isDebugMode():
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
					companyTblExportFile.write(EMTUtilities.encodeCSVLine("company_id", "shortname", "status", "mailerset", "rdir_domain", "mailloop_domain"))
					for row in result:
						companyID = row[0]
						shortname = row[1]
						status = row[2]
						mailerset = row[3]
						rdirDomain = row[4]
						mailloopDomain = row[5]
						companyTblExportFile.write(EMTUtilities.encodeCSVLine(companyID, shortname, status, mailerset, rdirDomain, mailloopDomain))

				EMTUtilities.zipFile(configAndLogsZipFilePath, companyTblExportFilePath, password)
				os.remove(companyTblExportFilePath)
			except:
				print(Colors.RED + "Cannot export company_tbl" + Colors.DEFAULT)
				if EMTUtilities.isDebugMode():
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
					homeDirectoryTreeExportFile.write(os.path.abspath(os.path.join("/home/" + Environment.username, os.pardir)) + os.sep)
					for directory, linkNames, dirNames, fileNames in EMTUtilities.walkSorted("/home/" + Environment.username):
						level = directory.replace("/home/" + Environment.username, "").count(os.sep)
						changeTimeString = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.path.getmtime(directory)))
						homeDirectoryTreeExportFile.write("\t" * level + os.path.basename(directory) + "/\tchanged: " + changeTimeString + "\n")
						indent = "\t" * (level + 1)
						for fileName in fileNames:
							changeTimeString = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.path.getmtime(os.path.join(directory, fileName))))
							homeDirectoryTreeExportFile.write(indent + fileName + "\tchanged: " + changeTimeString + " size: " + str(os.path.getsize(os.path.join(directory, fileName))) + "\n")
						for linkName in linkNames:
							changeTimeString = time.strftime("%Y-%m-%d %H:%M:%S", time.localtime(os.lstat(os.path.join(directory, linkName)).st_mtime))
							homeDirectoryTreeExportFile.write(indent + linkName + " -> " + os.path.realpath(os.path.join(directory, linkName)) + "\tchanged: " + changeTimeString + "\n")
				EMTUtilities.zipFile(configAndLogsZipFilePath, homeDirectoryTreeExportFilePath, password)
			except:
				print(Colors.RED + "Cannot export Home directory files tree" + Colors.DEFAULT)
				if EMTUtilities.isDebugMode():
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

				if os.path.isfile("/home/" + applicationUserName + "/tomcat/conf/version.txt"):
					with open("/home/" + applicationUserName + "/tomcat/conf/version.txt") as runtimeVersionFile:
						Environment.runtimeVersion = runtimeVersionFile.read().strip()
				else:
					Environment.runtimeVersion = "Unknown"
				emailText += "Runtime Version: " + Environment.runtimeVersion + "\n"

				if Environment.isEmmFrontendServer:
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

				if os.path.islink("/home/" + applicationUserName + "/release/backend/current"):
					emailText += Environment.applicationName + "Backend Version: " + os.path.basename(os.path.realpath("/home/" + applicationUserName + "/release/backend/current")) + "\n"

				EMTUtilities.sendEmail(Environment.applicationName + "Installer@" + Environment.hostname, [emailAddress], Environment.applicationName + "Installer@" + Environment.hostname + ": configuration and logs", emailText, [configAndLogsZipFilePath])
			except:
				Environment.errors.append("Cannot send email. File created for manually sending: " + configAndLogsZipFilePath)
				if EMTUtilities.isDebugMode():
					logging.exception("Cannot send email")

			if Environment.errors is None or len(Environment.errors) == 0:
				Environment.messages.append("Email with configuration and log files was sent to '" + emailAddress + "'\nFor content see " + configAndLogsZipFilePath)
		else:
			Environment.errors.append("Send configuration and log files canceled (no password)")
	else:
		Environment.errors.append("Send configuration and log files canceled (no email address)")
