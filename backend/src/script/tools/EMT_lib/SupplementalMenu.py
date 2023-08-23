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
import shutil
import subprocess
import random
import string
import logging
import tempfile
import time

from EMT_lib.Environment import Environment
from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities
from EMT_lib.License import License

def masterPasswordMenuAction(actionParameters):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"

	emmSaltFilePath = "/home/" + applicationUserName + "/conf/keys/emm.salt"
	if not os.path.isfile(emmSaltFilePath):
		generatedEmmSaltRaw = "".join(random.SystemRandom().choice(string.ascii_uppercase) for _ in range(15)) \
			+ "".join(random.SystemRandom().choice(string.ascii_lowercase) for _ in range(15)) \
			+ "".join(random.SystemRandom().choice(string.digits) for _ in range(2))
		generatedEmmSalt = ""
		for i in range(0, len(generatedEmmSaltRaw)):
			nextCharIndex = int(random.SystemRandom().random() * len(generatedEmmSaltRaw))
			generatedEmmSalt = generatedEmmSalt + generatedEmmSaltRaw[nextCharIndex]
			generatedEmmSaltRaw = generatedEmmSaltRaw[:nextCharIndex] + generatedEmmSaltRaw[nextCharIndex + 1:]
		with open(emmSaltFilePath, "w", encoding="UTF-8") as emmSaltFile:
			emmSaltFile.write(generatedEmmSalt)
		Environment.messages.append("New emm.salt file generated")

	if not os.path.isdir("/home/" + applicationUserName + "/webapps/emm/WEB-INF/classes"):
		Environment.errors.append("Error while creating new initial password for user 'emm-master': No " + Environment.applicationName + " application is installed")
		return
	else:
		print("Create new initial 'emm-master' password")
		print("Are you sure? (N/y, Blank => Cancel):")

		choice = input(" > ")
		choice = choice.strip().lower()
		if choice.startswith("y") or choice.startswith("j"):
			generatedPasswordRaw = "".join(random.SystemRandom().choice(string.ascii_uppercase) for _ in range(3)) \
				+ "".join(random.SystemRandom().choice(string.ascii_lowercase) for _ in range(3)) \
				+ "".join(random.SystemRandom().choice(string.digits) for _ in range(1)) \
				+ "".join(random.SystemRandom().choice("!$%&") for _ in range(1))
			generatedPassword = ""
			for i in range(0, len(generatedPasswordRaw)):
				nextCharIndex = int(random.SystemRandom().random() * len(generatedPasswordRaw))
				generatedPassword = generatedPassword + generatedPasswordRaw[nextCharIndex]
				generatedPasswordRaw = generatedPasswordRaw[:nextCharIndex] + generatedPasswordRaw[nextCharIndex + 1:]

			try:
				os.chdir("/home/" + applicationUserName + "/webapps/emm/WEB-INF/classes")
				generatedPasswordHash = subprocess.check_output(Environment.javaHome + "/bin/java com.agnitas.emm.core.admin.encrypt.InitialPasswordEncryptor \"" + emmSaltFilePath + "\" -p '" + generatedPassword + "'", shell=True).decode("UTF-8")
				if generatedPasswordHash is not None:
					generatedPasswordHash = generatedPasswordHash.strip()
					if DbConnector.emmDbVendor == "oracle":
						DbConnector.update("UPDATE admin_tbl SET secure_password_hash = ?, pwdchange_date = CURRENT_TIMESTAMP - 91 WHERE admin_id = ?", generatedPasswordHash.upper(), 1)
					else:
						DbConnector.update("UPDATE admin_tbl SET secure_password_hash = ?, pwdchange_date = DATE_ADD(CURRENT_TIMESTAMP, INTERVAL -91 DAY) WHERE admin_id = ?", generatedPasswordHash.upper(), 1)
					Environment.messages.append("New initial password for user 'emm-master': " + generatedPassword)
				else:
					print(Colors.RED + "New initial password for user 'emm-master': ERROR" + Colors.DEFAULT)
			except:
				if EMTUtilities.isDebugMode():
					logging.exception("Error while creating new initial password for user 'emm-master'")
				Environment.errors.append("Error while creating new initial password for user 'emm-master'")
		else:
			Environment.messages.append("New initial 'emm-master' password canceled")

def licenseFileMenuAction(actionParameters):
	print(Environment.applicationName + " install license file")

	print("Please enter local license file path (Blank => Cancel):")
	licenseFilePath = input(" > ")
	if len(licenseFilePath.strip()) <= 0:
		Environment.messages.append("Installation of license file canceled")
	elif not os.path.isfile(licenseFilePath):
		Environment.errors.append("Given path does not exist or is not a file")
	elif not licenseFilePath.endswith(".zip") and not licenseFilePath.endswith(".tar.gz"):
		Environment.errors.append("Given file is not a compressed archive (*.zip or *.tar.gz)")
	else:
		with tempfile.TemporaryDirectory() as licenseTempDirectory:
			if licenseFilePath.endswith(".zip"):
				EMTUtilities.unzipFile(licenseFilePath, licenseTempDirectory)
			elif licenseFilePath.endswith(".tar.gz"):
				EMTUtilities.unTarGzFile(licenseFilePath, licenseTempDirectory)

			if not os.path.isfile(licenseTempDirectory + "/emm.license.xml") or not os.path.isfile(licenseTempDirectory + "/emm.license.xml.sig"):
				Environment.errors.append("Given license file archive does not contain expected files (emm.license.xml, emm.license.xml.sig)")
			else:
				with open(licenseTempDirectory + "/emm.license.xml", "r", encoding="UTF-8") as licenseDataFileHandle:
					licenseData = licenseDataFileHandle.read()
				with open(licenseTempDirectory + "/emm.license.xml.sig", "rb") as licenseSignatureFileHandle:
					licenseSignature = licenseSignatureFileHandle.read()

				License.updateLicense(licenseData, licenseSignature)

				DbConnector.update("INSERT INTO server_command_tbl(command, server_name, execution_date, admin_id, description, timestamp) VALUES ('RELOAD_LICENSE_DATA', 'ALL', CURRENT_TIMESTAMP, 1, 'New license data uploaded by Maintenance Tool', CURRENT_TIMESTAMP)")

				Environment.messages.append("Successfully installed license file")

				if os.path.isfile(licenseTempDirectory + "/emm.salt"):
					if os.path.isdir("/home/console/tomcat") and not os.path.isdir("/home/console/tomcat/conf"):
						os.mkdir("/home/console/tomcat/conf")
					if os.path.isdir("/home/console/tomcat/conf") and not os.path.isdir("/home/console/tomcat/conf/keys"):
						os.mkdir("/home/console/tomcat/conf/keys")
					shutil.copy(licenseTempDirectory + "/emm.salt", "/home/console/tomcat/conf/keys/emm.salt")
					Environment.messages.append("Successfully installed emm.salt file")

def restartMenuAction(actionParameters):
	print("Restart " + Environment.applicationName + " Server")
	print("Are you sure? (N/y, Blank => Cancel):")

	choice = input(" > ")
	choice = choice.strip().lower()
	if choice.startswith("y") or choice.startswith("j"):
		restartApplication()
	else:
		Environment.messages.append("Restart canceled")

def dbTableFormatCheckMenuAction(actionParameters):
	print(Environment.applicationName + "MariaDB table format check")
	print("Check database table format and set 'ROW_FORMAT = DYNAMIC'? (N/y, Blank => Cancel):")
	choice = input(" > ")
	choice = choice.strip().lower()
	if choice.startswith("y") or choice.startswith("j"):
		connection = None
		cursor = None
		try:
			connection = DbConnector.openDbConnection()
			if connection is None:
				raise Exception("Cannot establish db connection")
			cursor = connection.cursor()

			processGranted = False
			reloadGranted = False
			cursor.execute("SHOW GRANTS")
			for row in cursor:
				if "GRANT ALL PRIVILEGES ".lower() in row[0].lower() or " process " in row[0].lower() or " process," in row[0].lower().replace('`','').replace('',''):
					processGranted = True
				if "GRANT ALL PRIVILEGES ON *.* ".lower() in row[0].lower() or " reload " in row[0].lower() or " reload," in row[0].lower().replace('`','').replace('',''):
					reloadGranted = True
			if processGranted and reloadGranted:
				cursor.execute("SELECT COUNT(*) FROM information_schema.innodb_sys_tables WHERE name LIKE CONCAT(SCHEMA(), '/%') AND row_format = 'Compact'")

				for row in cursor:
					compactTablesAmount = row[0]
					if compactTablesAmount >= 0:
						print("Found " + str(row[0]) + " tables with 'row_format = Compact'. Change row_format to Dynamic ? (N/y, Blank => Cancel):")
						choice = input(" > ")
						choice = choice.strip().lower()
						if choice.startswith("y") or choice.startswith("j"):
							cursor.execute("DROP PROCEDURE IF EXISTS tmp_convert_row")
							cursor.execute("CREATE PROCEDURE tmp_convert_row()\n"
								+ "BEGIN\n"
								+ "DECLARE done INT DEFAULT FALSE; DECLARE dbtable VARCHAR(100);\n"
								+ "DECLARE tab_cursor cursor FOR SELECT SUBSTRING(name, length(SCHEMA()) + 2) FROM information_schema.innodb_sys_tables WHERE name LIKE CONCAT(SCHEMA(), '/%') AND row_format = 'Compact';\n"
								+ "DECLARE CONTINUE HANDLER FOR NOT FOUND SET done = TRUE;\n"
								+ "OPEN tab_cursor;\n"
								+ "read_loop: LOOP\n"
								+ "FETCH tab_cursor INTO dbtable;\n"
								+ "IF done THEN LEAVE read_loop; END IF;\n"
								+ "SET @SQLText = CONCAT('ALTER TABLE ', dbtable, ' ROW_FORMAT=DYNAMIC');\n"
								+ "PREPARE stmt FROM @SQLText; EXECUTE stmt; DEALLOCATE PREPARE stmt;\n"
								+ "END LOOP;\n"
								+ "CLOSE tab_cursor;\n"
								+ "END")
							cursor.execute("CALL tmp_convert_row();")

							Environment.messages.append("MariaDB table format check executed successully. " + compactTablesAmount + " tables changed.")
					else:
						Environment.messages.append("MariaDB table format check executed successully. No tables found for change.")
			else:
				dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName] if DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties else None
				if not processGranted:
					Environment.errors.append("Database user needs grant 'PROCESS' to check and change table format. (e.g. GRANT PROCESS ON *.* TO '" + dbcfgEntry["user"] + "'@'%';)")
				if not reloadGranted:
					Environment.errors.append("Database user needs grant 'RELOAD' to check and change table format. (e.g. GRANT RELOAD ON *.* TO '" + dbcfgEntry["user"] + "'@'%';)")
		except Exception as e:
			errorText = "Error while checking MariaDB table format: " + str(e)
			Environment.errors.append(errorText)
			logging.exception(errorText)
		finally:
			if cursor is not None:
				cursor.close()
			if connection is not None:
				connection.commit()
				connection.close()

def updateHistoryMenuAction(actionParameters):
	print(Environment.applicationName + " update history")

	print()

	try:
		applications = []
		result = DbConnector.select("SELECT DISTINCT application_name FROM release_log_tbl ORDER BY application_name")
		for row in result:
			applications.append(row[0])
		for application in applications:
			print("Application: " + application)
			if Environment.applicationName.lower() != "openemm":
				result = DbConnector.select("SELECT startup_timestamp, host_name, version_number FROM release_log_tbl WHERE application_name = '" + application + "' ORDER BY startup_timestamp DESC")
				EMTUtilities.printAlignedTable(["Startup time", "Hostname", "Version"], result)
			else:
				result = DbConnector.select("SELECT startup_timestamp, version_number FROM release_log_tbl WHERE application_name = '" + application + "' ORDER BY startup_timestamp DESC")
				EMTUtilities.printAlignedTable(["Startup time", "Version"], result)
			print()
	except Exception as e:
		if EMTUtilities.isDebugMode():
			logging.exception("Cannot read release_log_tbl")

	print()

	print("Data is readonly (Blank => Back)")
	choice = input(" > ")
	return

def restartApplication():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
		applicationUserName = "console"
	elif Environment.isEmmRdirServer:
		applicationUserName = "rdir"
	elif Environment.isEmmMergerServer:
		applicationUserName = "merger"
	elif Environment.isEmmMailerServer:
		applicationUserName = "mailout"
	elif Environment.isEmmMailloopServer:
		applicationUserName = "mailloop"

	restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
	print("Restart log in file: " + restartLogFile);

	if EMTUtilities.hasRootPermissions() and os.path.isfile("/root/bin/" + Environment.restartToolName):
		print("Restarting with: " + "/root/bin/" + Environment.restartToolName)
		os.system("/root/bin/" + Environment.restartToolName + " restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	elif os.path.isfile("/home/" + applicationUserName + "/bin/" + Environment.restartToolName):
		print("Restarting with: " + Environment.restartToolName)
		if EMTUtilities.hasRootPermissions():
			os.system("su -c \"/home/" + applicationUserName + "/bin/" + Environment.restartToolName + " restart 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
		else:
			os.system("/home/" + applicationUserName + "/bin/" + Environment.restartToolName + " restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	elif applicationUserName == "merger" and os.path.isfile("/home/" + applicationUserName + "/bin/merger.sh"):
		print("Restarting with: merger.sh")
		if EMTUtilities.hasRootPermissions():
			os.system("su -c \"/home/" + applicationUserName + "/bin/merger.sh restart 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
		else:
			os.system("/home/" + applicationUserName + "/bin/merger.sh restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	elif applicationUserName == "mailout" and os.path.isfile("/home/" + applicationUserName + "/bin/mailer.sh"):
		print("Restarting with: mailer.sh")
		if EMTUtilities.hasRootPermissions():
			os.system("su -c \"/home/" + applicationUserName + "/bin/mailer.sh restart 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
		else:
			os.system("/home/" + applicationUserName + "/bin/mailer.sh restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	elif applicationUserName == "mailloop" and os.path.isfile("/home/" + applicationUserName + "/bin/mailloop.sh"):
		print("Restarting with: mailloop.sh")
		if EMTUtilities.hasRootPermissions():
			os.system("su -c \"/home/" + applicationUserName + "/bin/mailloop.sh restart 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
		else:
			os.system("/home/" + applicationUserName + "/bin/mailloop.sh restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	else:
		if os.path.isfile("/home/" + applicationUserName + "/bin/emm.sh"):
			print("Force-Restarting with: emm.sh")
			if EMTUtilities.hasRootPermissions():
				os.system("su -c \"/home/" + applicationUserName + "/bin/emm.sh force-restart 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
			else:
				os.system("/home/" + applicationUserName + "/bin/emm.sh force-restart 2>&1 | tee --append " + restartLogFile)
			Environment.rebootNeeded = False
			Environment.messages.append(Environment.applicationName + " restarted")
		else:
			Environment.errors.append("Cannot restart " + Environment.applicationName)
