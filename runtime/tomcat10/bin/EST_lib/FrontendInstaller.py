import os
import re
import datetime

from EST_lib.Environment import Environment
from EST_lib.License import License
from EST_lib import DbConnector
from EST_lib import ESTUtilities

# Frontend, Statistics, Webservices or Rdir
def installFile(interactive, logfile, applicationUserName, applicationName, updatedApplicationDirectoryPath, packageFilePath):
	packageFilename = os.path.basename(packageFilePath)
	applicationDirectoryName = packageFilename.replace(".tar.gz", "")

	if DbConnector.emmDbVendor is None:
		Environment.errors.append("Mandatory database configuration not found")
		return
	elif applicationName == "gui" and DbConnector.getDbClientPath() is None:
		Environment.errors.append("Mandatory database client is NOT available, but needed for frontend application update")
		return

	# Extract targz-file
	ESTUtilities.createDirectory(updatedApplicationDirectoryPath, applicationUserName)
	ESTUtilities.unTarGzFile(packageFilePath, updatedApplicationDirectoryPath, applicationUserName)

	# Remove obsolete sub directory when the applicationdirectory in tar.gz is in an sub level directory
	if os.path.isdir(updatedApplicationDirectoryPath + "/" + applicationDirectoryName):
		os.system("mv '" + updatedApplicationDirectoryPath + "/" + applicationDirectoryName + "' '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'")
		os.system("mv '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'/* '" + updatedApplicationDirectoryPath + "/'")
		os.system("rmdir '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'")

	logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

	# Update and install npm packages
	if applicationName == "gui" and os.path.isfile(updatedApplicationDirectoryPath + "/WEB-INF/package.json"):
		print()
		print("Installing required dependencies:")
		commandToExecute = "cd " + updatedApplicationDirectoryPath + "/WEB-INF && npm install"
		if ESTUtilities.hasRootPermissions():
			os.system("su -c \"" + commandToExecute + "\" - " + applicationUserName)
		else:
			os.system(commandToExecute)
		print()
		logfile.write("Updated npm packages\n")

	# When this is EMM LTS <majorVersion>.10.x, we check for a former startup of EMM LTS <majorVersion>.04.x,
	# when this is EMM LTS <majorVersion>.04.x, we check for a former startup of EMM LTS (<majorVersion> -1).10.x,
	# because EMM may contain java code migrations in a former version of EMM which have to be started before this version
	versionToInstall = ESTUtilities.getVersionFromFilename(packageFilename)
	if applicationName == "gui" and versionToInstall is not None and versionToInstall != "Unknown":
		formerVersionToCheck = None;
		versionToInstallMajorVersion = ESTUtilities.getMajorVersion(versionToInstall)
		versionToInstallMinorVersion = ESTUtilities.getMinorVersion(versionToInstall)
		versionToInstallMicroVersion = ESTUtilities.getMicroVersion(versionToInstall)
		if versionToInstallMinorVersion == 4 and versionToInstallMicroVersion == 0:
			formerVersionToCheck = str(versionToInstallMajorVersion - 1) + ".10.000";
		elif versionToInstallMinorVersion == 10 and versionToInstallMicroVersion == 0:
			formerVersionToCheck = str(versionToInstallMajorVersion) + ".04.000";

		if formerVersionToCheck is not None and DbConnector.checkTableExists("release_log_tbl)"):
			startupCount = DbConnector.selectValue("SELECT COUNT(*) FROM release_log_tbl WHERE application_name = 'EMM'")
			if startupCount is None:
				startupCount = 0

			if DbConnector.emmDbVendor == "oracle":
				startupCountOfInstallVersion = DbConnector.selectValue("SELECT COUNT(*) FROM release_log_tbl WHERE application_name = ? AND REGEXP_LIKE(version_number, '^0*' || ? || '.0*' || ? || '.000(.[0-9]+)*$')", 'EMM', ESTUtilities.getMajorVersion(versionToInstall), ESTUtilities.getMinorVersion(versionToInstall))
			else:
				startupCountOfInstallVersion = DbConnector.selectValue("SELECT COUNT(*) FROM release_log_tbl WHERE application_name = ? AND version_number REGEXP CONCAT('^0*', ?, '.0*', ?, '.000(.[0-9]+)*$')", 'EMM', ESTUtilities.getMajorVersion(versionToInstall), ESTUtilities.getMinorVersion(versionToInstall))
			if startupCountOfInstallVersion is None:
				startupCountOfInstallVersion = 0

			if DbConnector.emmDbVendor == "oracle":
				startupCountOfFormerLtsVersion = DbConnector.selectValue("SELECT COUNT(*) FROM release_log_tbl WHERE application_name = ? AND REGEXP_LIKE(version_number, '^0*' || ? || '.0*' || ? || '.000(.[0-9]+)*$')", 'EMM', ESTUtilities.getMajorVersion(formerVersionToCheck), ESTUtilities.getMinorVersion(formerVersionToCheck))
			else:
				startupCountOfFormerLtsVersion = DbConnector.selectValue("SELECT COUNT(*) FROM release_log_tbl WHERE application_name = ? AND version_number REGEXP CONCAT('^0*', ?, '.0*', ?, '.000(.[0-9]+)*$')", 'EMM', ESTUtilities.getMajorVersion(formerVersionToCheck), ESTUtilities.getMinorVersion(formerVersionToCheck))
			if startupCountOfFormerLtsVersion is None:
				startupCountOfFormerLtsVersion = 0

			if startupCount > 0 and startupCountOfInstallVersion < 1 and startupCountOfFormerLtsVersion < 1:
				errorText = "EMM version " + formerVersionToCheck + " needs to be started first because of data migration issues. Please install and run EMM version " + formerVersionToCheck + " first before this EMM version " + versionToInstall
				Environment.errors.append(errorText)
				logfile.write(errorText + "\n")
				return

	# Preserve session timeout settings
	if applicationName == "gui" and os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/web.xml") and os.path.isfile(updatedApplicationDirectoryPath + "/WEB-INF/web.xml"):
		pattern = re.compile("<session-timeout>(\d+)</session-timeout>")
		sessionTimeoutMinutes = None
		for i, line in enumerate(open(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/web.xml")):
			for match in re.finditer(pattern, line):
				sessionTimeoutMinutes = match.group(1)
		if sessionTimeoutMinutes is not None:
			print("Preserving session timeout: " + sessionTimeoutMinutes + "\n")
			logfile.write("Preserving session timeout: " + sessionTimeoutMinutes + "\n")
			with open(updatedApplicationDirectoryPath + "/WEB-INF/web.xml", 'r+') as fileHandle:
				text = fileHandle.read()
				text = re.sub("<session-timeout>(\d+)</session-timeout>", "<session-timeout>" + sessionTimeoutMinutes + "</session-timeout>", text)
				fileHandle.seek(0)
				fileHandle.write(text)
				fileHandle.truncate()

	# Create new webapps-link
	if applicationName == "gui":
		if Environment.isOpenEmmServer:
			if os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm"):
				os.unlink(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm")
			ESTUtilities.createLink(updatedApplicationDirectoryPath, ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm", "openemm")
			logfile.write("Created new application link 'emm'\n")
		else:
			if os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm"):
				os.unlink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm")
			ESTUtilities.createLink(updatedApplicationDirectoryPath, ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm", Environment.getFrontendApplicationUserName())
			logfile.write("Created new application link 'emm'\n")
	else:
		if Environment.isOpenEmmServer:
			if os.path.islink(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/" + applicationName):
				os.unlink(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/" + applicationName)
			ESTUtilities.createLink(updatedApplicationDirectoryPath, ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/" + applicationName, "openemm")
			logfile.write("Created new application link '" + applicationName + "'\n")
		elif Environment.isEmmRdirServer:
			if os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/" + applicationName):
				os.unlink(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/" + applicationName)
			ESTUtilities.createLink(updatedApplicationDirectoryPath, ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/" + applicationName, Environment.getRdirApplicationUserName())
			logfile.write("Created new application link '" + applicationName + "'\n")
		else:
			if os.path.islink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/" + applicationName):
				os.unlink(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/" + applicationName)
			ESTUtilities.createLink(updatedApplicationDirectoryPath, ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/" + applicationName, Environment.getFrontendApplicationUserName())
			logfile.write("Created new application link '" + applicationName + "'\n")

	# Execute sql database update scripts
	if applicationName == "gui":
		if Environment.isOpenEmmServer:
			applicationUserName = "openemm"
		else:
			applicationUserName = Environment.getFrontendApplicationUserName()

		if not DbConnector.checkDbStructureExists():
			if DbConnector.emmDbVendor == "oracle":
				print("Creating basic database structure ...")

				fullDbBashScriptFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-initial-fulldb.sh"
				os.system("chmod u+x \"" + fullDbBashScriptFilePath + "\"")
				sqlUpdateReturnCode = os.system("\"" + fullDbBashScriptFilePath + "\" " + DbConnector.dbcfgPropertiesFilePath)
				if sqlUpdateReturnCode != 0:
					raise Exception("SQL database updates caused an error.")

				DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
				DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
				DbConnector.insertBirtKeysInDb(Environment.getFrontendApplicationUserName())
			elif os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql"):
				print("Creating basic database structure ...")
				sqlUpdatePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql"
				sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mariadb-fulldb-basic_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
				fullDbScriptSuccess = DbConnector.executeSqlScriptFile(sqlUpdatePath, sqlUpdateLogfilePath)
				if not fullDbScriptSuccess:
					if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
						with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
							sqlLogContent = sqlUpdateLogfile.read()
							logfile.write(sqlLogContent + "\n")
							print(str(sqlLogContent))
					errorText = "Error while executing full database script"
					Environment.errors.append(errorText)
					logfile.write(errorText + "\n")
					return
				elif os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-extended.sql"):
					print("Creating extended database structure ...")
					sqlUpdatePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-extended.sql"
					sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mariadb-fulldb-extended_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
					fullDbExtendedScriptSuccess = DbConnector.executeSqlScriptFile(sqlUpdatePath, sqlUpdateLogfilePath)
					if not fullDbExtendedScriptSuccess:
						if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
							with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
								sqlLogContent = sqlUpdateLogfile.read()
								logfile.write(sqlLogContent + "\n")
								print(str(sqlLogContent))
						errorText = "Error while executing full database extended script"
						Environment.errors.append(errorText)
						logfile.write(errorText + "\n")
						return
				else:
					if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-openemm.sql"):
						print("Creating OpenEMM database structure ...")
						sqlUpdatePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-openemm.sql"
						sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mariadb-fulldb-openemm_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
						openemmFullDbScriptSuccess = DbConnector.executeSqlScriptFile(sqlUpdatePath, sqlUpdateLogfilePath)
						if not openemmFullDbScriptSuccess:
							if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
								with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
									sqlLogContent = sqlUpdateLogfile.read()
									logfile.write(sqlLogContent + "\n")
									print(str(sqlLogContent))
							errorText = "Error while executing OpenEMM full database script"
							Environment.errors.append(errorText)
							logfile.write(errorText + "\n")
							return
				DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
				DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
				DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else Environment.getFrontendApplicationUserName())
			elif os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql"):
				print("Creating basic database structure ...")
				sqlUpdatePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql"
				sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mysql-fulldb-basic_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
				fullDbScriptSuccess = DbConnector.executeSqlScriptFile(sqlUpdatePath, sqlUpdateLogfilePath)
				if not fullDbScriptSuccess:
					if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
						with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
							sqlLogContent = sqlUpdateLogfile.read()
							logfile.write(sqlLogContent + "\n")
							print(str(sqlLogContent))
					errorText = "Error while executing full database script"
					Environment.errors.append(errorText)
					logfile.write(errorText + "\n")
					return
				elif os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-extended.sql"):
					print("Creating extended database structure ...")
					sqlUpdatePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-extended.sql"
					sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mysql-fulldb-extended_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
					fullDbExtendedScriptSuccess = DbConnector.executeSqlScriptFile(sqlUpdatePath, sqlUpdateLogfilePath)
					if not fullDbExtendedScriptSuccess:
						if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
							with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
								sqlLogContent = sqlUpdateLogfile.read()
								logfile.write(sqlLogContent + "\n")
								print(str(sqlLogContent))
						errorText = "Error while executing full database extended script"
						Environment.errors.append(errorText)
						logfile.write(errorText + "\n")
						return
				else:
					if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-openemm.sql"):
						print("Creating OpenEMM database structure ...")
						sqlUpdatePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-openemm.sql"
						sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mysql-fulldb-openemm_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
						openemmFullDbScriptSuccess = DbConnector.executeSqlScriptFile(sqlUpdatePath, sqlUpdateLogfilePath)
						if not openemmFullDbScriptSuccess:
							if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
								with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
									sqlLogContent = sqlUpdateLogfile.read()
									logfile.write(sqlLogContent + "\n")
									print(str(sqlLogContent))
							errorText = "Error while executing OpenEMM full database script"
							Environment.errors.append(errorText)
							logfile.write(errorText + "\n")
							return
				DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
				DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
				DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else Environment.getFrontendApplicationUserName())

			if not DbConnector.checkDbStructureExists():
				errorText = "Error while executing full database script. EMM Db structure is still missing"
				Environment.errors.append(errorText)
				logfile.write(errorText + "\n")
				return

		print("Executing database structure updates ...")

		sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/sql_update_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log"):
			ESTUtilities.createDirectories(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log", applicationUserName)
		if DbConnector.emmDbVendor == "oracle":
			os.system("chmod u+x " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-missing-updates.sh")
			sqlUpdateReturnCode = os.system("/bin/bash -c '" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath + " 2>&1 | tee " + sqlUpdateLogfilePath + "; test ${PIPESTATUS[0]} -eq 0'")
		elif DbConnector.emmDbVendor == "mysql" or DbConnector.emmDbVendor == "mariadb":
			if License.getLicenseID() == "0" and Environment.applicationName != "OpenEMM":
				sqlUpdateReturnCode = None
				warningText = "Skipped execution of missing EMM database structure updates because database is in OpenEMM state"
				print(warningText)
				Environment.warnings.append(warningText)
			else:
				if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb"):
					os.system("chmod u+x " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh")
					sqlUpdateReturnCode = os.system("/bin/bash -c '" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath + " 2>&1 | tee " + sqlUpdateLogfilePath + "; test ${PIPESTATUS[0]} -eq 0'")
				else:
					os.system("chmod u+x " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh")
					sqlUpdateReturnCode = os.system("/bin/bash -c '" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath + " 2>&1 | tee " + sqlUpdateLogfilePath + "; test ${PIPESTATUS[0]} -eq 0'")

		if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
			with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
				sqlLogContent = sqlUpdateLogfile.read()
				logfile.write("SQL Update:\n" + sqlLogContent + "\n")

		if sqlUpdateReturnCode is not None and sqlUpdateReturnCode != 0:
			if interactive:
				print("SQL database updates caused an error. Press any key to continue.")
				choice = input(" > ")

			errorText = "Error while executing database update scripts"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			print("Database update had errors")
		else:
			logfile.write("Sql database update scripts successfully executed" + "\n")
			print("Database update finished")
			logfile.write("Database update executed\n")
	Environment.rebootNeeded = True

	# Read new version info
	if Environment.isOpenEmmServer:
		if os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm/WEB-INF/classes/emm.properties"):
			Environment.frontendVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]
		if os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/statistics/WEB-INF/classes/emm.properties"):
			Environment.statisticsVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]
		if os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/webservices/WEB-INF/classes/emm.properties"):
			Environment.webservicesVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]
	else:
		if Environment.username == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions():
			if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/classes/emm.properties"):
				Environment.frontendVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/statistics/WEB-INF/classes/emm.properties"):
				Environment.statisticsVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/webservices/WEB-INF/classes/emm.properties"):
				Environment.webservicesVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.properties"):
				Environment.consoleRdirVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]
		if Environment.username == Environment.getRdirApplicationUserName() or ESTUtilities.hasRootPermissions():
			if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.properties"):
				Environment.consoleRdirVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]
