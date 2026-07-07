import sys
import os
import shutil
import subprocess
import random
import string
import logging
import tempfile
import time
import ssl
import datetime

import urllib.request, urllib.error, urllib.parse
import shlex

from EST_lib.Environment import Environment
from EST_lib import Colors, FrontendRuntimeInstaller
from EST_lib import DbConnector
from EST_lib import ESTUtilities
from EST_lib.License import License
from EST_lib.ESTUtilities import restart, parse_header

def masterPasswordMenuAction(actionParameters):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()

	emmSaltFilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/conf/keys/emm.salt"
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

	if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/classes"):
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
				os.chdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/lib")
				generatedPasswordHash = subprocess.check_output(Environment.javaHome + "/bin/java -cp ./emm-core.jar com.agnitas.emm.core.admin.encrypt.InitialPasswordEncryptor \"" + emmSaltFilePath + "\" -p '" + generatedPassword + "'", shell=True).decode("UTF-8")
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
				if ESTUtilities.isDebugMode():
					logging.exception("Error while creating new initial password for user 'emm-master'")
				Environment.errors.append("Error while creating new initial password for user 'emm-master'")
		else:
			Environment.messages.append("New initial 'emm-master' password canceled")

def licenseFileMenuAction(actionParameters):
	print(Environment.applicationName + " install license file")

	print("Please enter local license file path or downloadlink (Blank => Cancel):")
	deleteLocalFile = False
	licenseFilePath = input(" > ")
	if len(licenseFilePath.strip()) <= 0:
		Environment.messages.append("Installation of license file canceled")
	elif licenseFilePath.startswith("http://") or licenseFilePath.startswith("https://"):
		licenseFilePath = downloadLicenseFile(licenseFilePath)
		deleteLocalFile = True
	elif not os.path.isfile(licenseFilePath):
		Environment.errors.append("Given path does not exist or is not a file")
	elif not licenseFilePath.endswith(".zip") and not licenseFilePath.endswith(".tar.gz"):
		Environment.errors.append("Given file is not a compressed archive (*.zip or *.tar.gz)")

	if licenseFilePath is not None:
		with tempfile.TemporaryDirectory() as licenseTempDirectory:
			if licenseFilePath.endswith(".zip"):
				ESTUtilities.unzipFile(licenseFilePath, licenseTempDirectory)
			elif licenseFilePath.endswith(".tar.gz"):
				ESTUtilities.unTarGzFile(licenseFilePath, licenseTempDirectory)

			if not os.path.isfile(licenseTempDirectory + "/emm.license.xml") or not os.path.isfile(licenseTempDirectory + "/emm.license.xml.sig"):
				Environment.errors.append("Given license file archive does not contain expected files (emm.license.xml, emm.license.xml.sig)")
			else:
				with open(licenseTempDirectory + "/emm.license.xml", "rb") as licenseDataFileHandle:
					licenseData = licenseDataFileHandle.read()
				with open(licenseTempDirectory + "/emm.license.xml.sig", "rb") as licenseSignatureFileHandle:
					licenseSignature = licenseSignatureFileHandle.read()

				License.updateLicense(licenseData, licenseSignature)

				DbConnector.update("INSERT INTO server_command_tbl(command, server_name, execution_date, admin_id, description, timestamp) VALUES ('RELOAD_LICENSE_DATA', 'ALL', CURRENT_TIMESTAMP, 1, 'New license data uploaded by Maintenance Tool', CURRENT_TIMESTAMP)")

				Environment.messages.append("Successfully installed license file")

				if os.path.isfile(licenseTempDirectory + "/emm.salt"):
					if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf"):
						os.mkdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf")
					if os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/keys"):
						os.mkdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/keys")
					shutil.copy(licenseTempDirectory + "/emm.salt", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/tomcat/conf/keys/emm.salt")
					Environment.messages.append("Successfully installed emm.salt file")

			if deleteLocalFile and os.path.isfile(licenseFilePath):
				os.remove(licenseFilePath)

def restartMenuAction(actionParameters):
	print("Restart " + Environment.applicationName + " Server")
	print("Are you sure? (N/y, Blank => Cancel):")

	choice = input(" > ")
	choice = choice.strip().lower()
	if choice.startswith("y") or choice.startswith("j"):
		restartApplication()
	else:
		Environment.messages.append("Restart canceled")

def backupTomcatConfigAction(actionParameters):

	print("Please enter the absolute path of the desired directories, separated by space: (Blank => Cancel)")
	choice = input("> ")
	backup_choices = shlex.split(choice)
	if backup_choices:
		output_path = FrontendRuntimeInstaller.createTarGZSystemFileBackup(backup_choices)
		if output_path:
			Environment.messages.append("Created Backup  folder: " + output_path)

def restartEST(actionParameters):
	restart(Environment.scriptFilePath, *sys.argv)

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
				ESTUtilities.printAlignedTable(["Startup time", "Hostname", "Version"], result)
			else:
				result = DbConnector.select("SELECT startup_timestamp, version_number FROM release_log_tbl WHERE application_name = '" + application + "' ORDER BY startup_timestamp DESC")
				ESTUtilities.printAlignedTable(["Startup time", "Version"], result)
			print()
	except Exception:
		if ESTUtilities.isDebugMode():
			logging.exception("Cannot read release_log_tbl")

	print()

	print("Data is readonly (Blank => Back)")
	input(" > ")
	return

def restartApplication():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
		applicationUserName = Environment.getFrontendApplicationUserName()
	elif Environment.isEmmRdirServer:
		applicationUserName = Environment.getRdirApplicationUserName()
	elif Environment.isEmmMergerServer:
		applicationUserName = "merger"
	elif Environment.isEmmMailerServer:
		applicationUserName = "mailout"
	elif Environment.isEmmMailloopServer:
		applicationUserName = "mailloop"

	restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
	print("Restart log in file: " + restartLogFile);

	if ESTUtilities.hasRootPermissions() and os.path.isfile("/root/bin/" + Environment.restartToolName):
		print("Restarting with: " + "/root/bin/" + Environment.restartToolName)
		os.system("/root/bin/" + Environment.restartToolName + " restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	elif os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + Environment.restartToolName):
		print("Restarting with: " + Environment.restartToolName)
		if ESTUtilities.hasRootPermissions():
			os.system("su -c \"" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + Environment.restartToolName + " restart 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
		else:
			os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + Environment.restartToolName + " restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	elif applicationUserName == "merger" and os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/merger.sh"):
		print("Restarting with: merger.sh")
		if ESTUtilities.hasRootPermissions():
			os.system("su -c \"" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/merger.sh restart 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
		else:
			os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/merger.sh restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	elif applicationUserName == "mailout" and os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/mailer.sh"):
		print("Restarting with: mailer.sh")
		if ESTUtilities.hasRootPermissions():
			os.system("su -c \"" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/mailer.sh restart 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
		else:
			os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/mailer.sh restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	elif applicationUserName == "mailloop" and os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/mailloop.sh"):
		print("Restarting with: mailloop.sh")
		if ESTUtilities.hasRootPermissions():
			os.system("su -c \"" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/mailloop.sh restart 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
		else:
			os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/mailloop.sh restart 2>&1 | tee --append " + restartLogFile)
		Environment.rebootNeeded = False
		Environment.messages.append(Environment.applicationName + " system restarted")
	else:
		if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/emm.sh"):
			print("Force-Restarting with: emm.sh")
			if ESTUtilities.hasRootPermissions():
				os.system("su -c \"" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/emm.sh force-restart 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
			else:
				os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/emm.sh force-restart 2>&1 | tee --append " + restartLogFile)
			Environment.rebootNeeded = False
			Environment.messages.append(Environment.applicationName + " restarted")
		else:
			Environment.errors.append("Cannot restart " + Environment.applicationName)

def downloadLicenseFile(licenceFileUrl):
	if not licenceFileUrl.endswith("/download"):
		licenceFileUrl = licenceFileUrl + "/download"

	if not ESTUtilities.checkDownloadFileIsAvailable(licenceFileUrl):
		Environment.errors.append("Licensefile download url '" + licenceFileUrl + "' is not available")
		return None

	downloadPageResponse = None
	try:
		username = None
		password = None
		passwordManager = urllib.request.HTTPPasswordMgrWithDefaultRealm()
		passwordManager.add_password(None, licenceFileUrl, username, password)
		authhandler = urllib.request.HTTPBasicAuthHandler(passwordManager)
		opener = urllib.request.build_opener(authhandler, urllib.request.HTTPSHandler(context = ssl.SSLContext(ssl.PROTOCOL_TLS)))
		downloadPageResponse = opener.open(licenceFileUrl)
		params = parse_header(downloadPageResponse.headers.get("Content-Disposition", ""))
		downloadFilename = params["filename"]

		downloadDestinationFilePath = "/tmp/" + downloadFilename
		# Clean up old update attempt left overs
		if os.path.isfile(downloadDestinationFilePath):
			os.remove(downloadDestinationFilePath)

		with open(downloadDestinationFilePath, "wb") as downloadDestinationFile:
			total_size = int(downloadPageResponse.info().get_all("Content-Length")[0].strip())
			bytes_read = 0
			chunk_size = 8192
			progressbarSize = 80
			print()
			print()
			nextProgressBarUpdate = None
			while True:
				chunk = downloadPageResponse.read(chunk_size)
				bytes_read += len(chunk)

				if not chunk:
					break

				downloadDestinationFile.write(chunk)

				# Update progress bar
				now = datetime.datetime.now()
				if nextProgressBarUpdate is None or nextProgressBarUpdate < now:
					ESTUtilities.printProgressBar(total_size, bytes_read, progressbarSize)
					nextProgressBarUpdate = now + datetime.timedelta(0, 1)
			ESTUtilities.printProgressBar(total_size, total_size, progressbarSize)

		return downloadDestinationFilePath
	except:
		raise
	finally:
		if downloadPageResponse is not None:
			downloadPageResponse.close()
