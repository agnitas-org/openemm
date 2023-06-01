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
import logging
import subprocess

from EMT_lib.Environment import Environment
from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities

def dbcfgMenuAction(actionParameters):
	if Environment.unsavedDbcfgChanges is not None:
		dbEntryName = Environment.unsavedDbcfgChanges["dbEntryName"]
	elif len(DbConnector.dbcfgProperties) == 1:
		dbEntryName = list(DbConnector.dbcfgProperties.keys())[0]
	else:
		print("Please choose database entry (Blank => Back):")
		for key, value in sorted(DbConnector.dbcfgProperties.items()):
			print(" " + key)

		choice = input(" > ")
		choice = choice.strip()
		if choice == "":
			return False
		elif not choice in DbConnector.dbcfgProperties:
			Environment.errors.append("Invalid database entry name: " + choice)
			return False
		else:
			dbEntryName = choice

	print("Database configuration for " + dbEntryName + ":")

	if not "secure" in DbConnector.dbcfgProperties[dbEntryName] and DbConnector.dbcfgProperties[dbEntryName]["dbms"] == "oracle":
		DbConnector.dbcfgProperties[dbEntryName]["secure"] = "false"

	for key, value in sorted(DbConnector.dbcfgProperties[dbEntryName].items()):
		color = ""
		if key in Environment.readonlyDbcfgProperties:
			print(Colors.RED + " " + key + " = " + value + Colors.DEFAULT)
		elif Environment.unsavedDbcfgChanges is not None and key in Environment.unsavedDbcfgChanges:
			print(Colors.YELLOW + " " + key + " = " + Environment.unsavedDbcfgChanges[key] + Colors.DEFAULT)
		else:
			print(" " + key + " = " + value)

	print()

	if os.access(DbConnector.dbcfgPropertiesFilePath, os.W_OK):
		print("Please choose entry name to change (Blank => Back" + (", 'save' to save changes, 'cancel' to drop changes" if Environment.unsavedDbcfgChanges is not None else "") + "):")

		choice = input(" > ")
		choice = choice.strip()

		if choice == "":
			return
		elif choice in Environment.readonlyDbcfgProperties:
			Environment.errors.append(choice + " is read-only and may not be changed.")
			return False
		elif Environment.unsavedDbcfgChanges is not None and choice.lower() == "save":
			if not os.access(DbConnector.dbcfgPropertiesFilePath, os.W_OK):
				Environment.errors.append("File is readonly: " + DbConnector.dbcfgPropertiesFilePath)
				return False

			if ((not "jdbc-connect" in DbConnector.dbcfgProperties[dbEntryName]) or DbConnector.dbcfgProperties[dbEntryName]["jdbc-connect"] is None or DbConnector.dbcfgProperties[dbEntryName]["jdbc-connect"] == ""):
				if Environment.unsavedDbcfgChanges["dbms"] == "oracle":
					Environment.unsavedDbcfgChanges["jdbc-connect"] = "[to be defined]"
				elif Environment.unsavedDbcfgChanges["dbms"] == "mariadb":
					Environment.unsavedDbcfgChanges["jdbc-connect"] = "jdbc:mariadb://" + DbConnector.dbcfgProperties[dbEntryName]["host"] + "/" + DbConnector.dbcfgProperties[dbEntryName]["name"] + "?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=UTF-8"
				elif Environment.unsavedDbcfgChanges["dbms"] == "mysql":
					Environment.unsavedDbcfgChanges["jdbc-connect"] = "jdbc:mysql://" + DbConnector.dbcfgProperties[dbEntryName]["host"] + "/" + DbConnector.dbcfgProperties[dbEntryName]["name"] + "?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=UTF-8"

			if (not "jdbc-driver" in DbConnector.dbcfgProperties[dbEntryName]) or DbConnector.dbcfgProperties[dbEntryName]["jdbc-driver"] is None or DbConnector.dbcfgProperties[dbEntryName]["jdbc-driver"] == "":
				if Environment.unsavedDbcfgChanges["dbms"] == "oracle":
					Environment.unsavedDbcfgChanges["jdbc-driver"] = "[to be defined]"
				elif Environment.unsavedDbcfgChanges["dbms"] == "mariadb":
					Environment.unsavedDbcfgChanges["jdbc-driver"] = "org.mariadb.jdbc.Driver"
				elif Environment.unsavedDbcfgChanges["dbms"] == "mysql":
					Environment.unsavedDbcfgChanges["jdbc-driver"] = "com.mysql.cj.jdbc.Driver"

			try:
				DbConnector.updateDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath, Environment.unsavedDbcfgChanges)
				DbConnector.dbcfgProperties = DbConnector.readDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath)
				DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[dbEntryName] if dbEntryName in DbConnector.dbcfgProperties else None
				DbConnector.emmDbVendor = DbConnector.dbcfgProperties[dbEntryName]["dbms"]
			except:
				if EMTUtilities.isDebugMode():
					logging.exception("Cannot save file: " + DbConnector.dbcfgPropertiesFilePath)
				Environment.errors.append("Cannot save file: " + DbConnector.dbcfgPropertiesFilePath)
				return False

			Environment.unsavedDbcfgChanges = None
			Environment.rebootNeeded = True
			Environment.otherSystemsNeedConfig = True
			Environment.messages.append("Changes saved.")

			print("Do you want to create a new database and database user with this credentials? (N/y, Blank => Cancel)")
			choice = input(" > ")
			choice = choice.strip().lower()
			if choice.startswith("y") or choice.startswith("j"):
				if not DbConnector.checkDbServiceAvailable():
					Environment.errors.append("Database: Not running or not reachable")
				elif not DbConnector.createDatabaseAndUser(DbConnector.dbcfgProperties[dbEntryName]["host"], DbConnector.dbcfgProperties[dbEntryName]["name"], DbConnector.dbcfgProperties[dbEntryName]["user"], DbConnector.dbcfgProperties[dbEntryName]["password"]):
					Environment.errors.append("Database and user do not exist and cannot be created. Please create database and user yourself.")
				else:
					Environment.messages.append("New database and user credentials created.")

			if Environment.getSystemUrl() is None or Environment.getSystemUrl().strip() == "" or Environment.getSystemUrl().strip() == "Unknown" and DbConnector.checkDbServiceAvailable() and DbConnector.checkDbStructureExists():
				Environment.errors.append("Basic configuration is missing. Please configure.")
				Environment.overrideNextMenu = Environment.configTableMenu

			return True
		elif Environment.unsavedDbcfgChanges is not None and choice.lower() == "cancel":
			Environment.unsavedDbcfgChanges = None
			DbConnector.dbcfgProperties = DbConnector.readDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath)
			Environment.messages.append("Changes reverted")
			return True
		elif choice in DbConnector.dbcfgProperties[dbEntryName]:
			dbcfgKey = choice
			if dbcfgKey == "dbms":
				print("Please enter new value for key '" + dbcfgKey + "' (Allowed values are '" + "', '".join(Environment.allowedDbmsSystems) + "'): ")
			elif dbcfgKey == "secure":
				print("Please enter new value for key '" + dbcfgKey + "' (Allowed values are 'true' or 'false'): ")
			else:
				print("Please enter new value for key '" + dbcfgKey + "': ")
			dbcfgValue = input(" > ")
			if dbcfgKey == "dbms" and not dbcfgValue in Environment.allowedDbmsSystems:
				Environment.errors.append("Invalid dbms-type '" + dbcfgValue + "' for key '" + dbcfgKey + "'. Only '" + "', '".join(Environment.allowedDbmsSystems) + "' allowed.")
			elif dbcfgKey == "secure" and not dbcfgValue in ["true", "false"]:
					Environment.errors.append("Invalid secure attribute '" + dbcfgValue + "' for key '" + dbcfgKey + "'. Only 'true' or 'false' allowed.")
			elif "," in dbcfgValue:
				Environment.errors.append("Invalid ',' character in new value '" + dbcfgValue + "' for key '" + dbcfgKey + "'")
			else:
				dbcfgValue = dbcfgValue.strip()
				DbConnector.dbcfgProperties[dbEntryName][dbcfgKey] = dbcfgValue
				if Environment.unsavedDbcfgChanges is None:
					Environment.unsavedDbcfgChanges = {}
					Environment.unsavedDbcfgChanges["dbEntryName"] = dbEntryName
				Environment.unsavedDbcfgChanges[dbcfgKey] = dbcfgValue

			return True
		else:
			Environment.errors.append("Invalid database entry key name: " + choice)
			return False
	else:
		print("Data is readonly (Blank => Back)")
		choice = input(" > ")
		return

def clearDatabaseMenuAction(actionParameters):
	dbRootPassword = None
	dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName] if DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties else None

	if DbConnector.checkDbExists(DbConnector.dbcfgEntry["name"]):
		print("Are you sure to drop all current data from database? ('DROP' => drop data, Blank => Back):")
		choice = input(" > ")
		choice = choice.strip()
		if choice.upper() == "":
			return
		elif choice.upper() == "DROP":
			print("Please enter database root user password:")
			dbRootPassword = input(" > ")

			print("Dropping database data (" + dbcfgEntry["name"] + ") ...")

			if dbRootPassword is not None and dbRootPassword != "":
				passwordParameterPart = " -p'" + dbRootPassword + "'"
			else:
				passwordParameterPart = ""

			sqlUpdateReturnCode = os.system(DbConnector.getDbClientPath() + " -u root" + passwordParameterPart + " --default-character-set=utf8 -e \"DROP DATABASE " + dbcfgEntry["name"] + "\"")
			if sqlUpdateReturnCode != 0:
				Environment.errors.append("Error while dropping database")
				return
			else:
				print("Database data was dropped")
				Environment.messages.append("Database data was dropped")

	print("Creating database (" + dbcfgEntry["name"] + ") and user (" + dbcfgEntry["user"] + ") ...")
	dbCreationSuccess = DbConnector.createDatabaseAndUser(dbcfgEntry["host"], dbcfgEntry["name"], dbcfgEntry["user"], dbcfgEntry["password"], dbRootPassword)
	if not dbCreationSuccess:
		Environment.errors.append("Cannot create database")
		return
	print("Database and user were created")

	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"

	if DbConnector.checkDbStructureExists():
		print("Database structure already exists")
		Environment.messages.append("Database structure already exists")
	elif not os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql")\
		and not os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql"):
		print("Database structure file is not available")
		Environment.messages.append("Database structure file is not available. Please install " + Environment.applicationName + " application.")
	elif os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql"):
		print("Creating new database structure")
		fullDbScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql")
		if not fullDbScriptSuccess:
			Environment.errors.append("Error while executing full database script")
			return
		elif os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-extended.sql"):
			print("Creating extended database structure ...")
			fullDbExtendedScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-extended.sql")
			if not fullDbExtendedScriptSuccess:
				Environment.errors.append("Error while executing full database extended script")
				return
		else:
			if os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-openemm.sql"):
				print("Creating OpenEMM database structure ...")
				openemmFullDbScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-openemm.sql")
				if not openemmFullDbScriptSuccess:
					Environment.errors.append("Error while executing OpenEMM full database script")
					return

		print("Database update started")

		if DbConnector.emmDbVendor == "oracle":
			os.system("chmod u+x /home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-missing-updates.sh")
			sqlUpdateReturnCode = os.system("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)
		elif DbConnector.emmDbVendor == "mysql" or DbConnector.emmDbVendor == "mariadb":
			if os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh"):
				os.system("chmod u+x /home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh")
				sqlUpdateReturnCode = os.system("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)
			else:
				os.system("chmod u+x /home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh")
				sqlUpdateReturnCode = os.system("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)

		if sqlUpdateReturnCode != 0:
			print("SQL database updates caused an error. Press any key to continue.")
			choice = input(" > ")
			Environment.errors.append("Error while executing database update scripts")
			print("Database update had errors")
		else:
			DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
			DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
			DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else "console")
			print("Database update finished")

		Environment.messages.append("Database data was dropped and recreated")
		return
	elif os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql"):
		print("Creating new database structure")
		fullDbScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql")
		if not fullDbScriptSuccess:
			Environment.errors.append("Error while executing full database script")
			return
		elif os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-extended.sql"):
			print("Creating extended database structure ...")
			fullDbExtendedScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-extended.sql")
			if not fullDbExtendedScriptSuccess:
				Environment.errors.append("Error while executing full database extended script")
				return
		else:
			if os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-openemm.sql"):
				print("Creating OpenEMM database structure ...")
				openemmFullDbScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-openemm.sql")
				if not openemmFullDbScriptSuccess:
					Environment.errors.append("Error while executing OpenEMM full database script")
					return

		print("Database update started")

		if DbConnector.emmDbVendor == "oracle":
			os.system("chmod u+x /home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-missing-updates.sh")
			sqlUpdateReturnCode = os.system("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)
		elif DbConnector.emmDbVendor == "mysql" or DbConnector.emmDbVendor == "mariadb":
			if os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh"):
				os.system("chmod u+x /home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh")
				sqlUpdateReturnCode = os.system("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)
			else:
				os.system("chmod u+x /home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh")
				sqlUpdateReturnCode = os.system("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)

		if sqlUpdateReturnCode != 0:
			print("SQL database updates caused an error. Press any key to continue.")
			choice = input(" > ")
			Environment.errors.append("Error while executing database update scripts")
			print("Database update had errors")
		else:
			DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
			DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
			DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else "console")
			print("Database update finished")

		Environment.messages.append("Database data was dropped and recreated")
		return

def dbBackupMenuAction(actionParameters):
	if Environment.isOpenEmmServer:
		currentVersion = Environment.frontendVersion
	elif Environment.isEmmFrontendServer:
		currentVersion = Environment.frontendVersion
	elif Environment.isEmmStatisticsServer:
		currentVersion = Environment.statisticsVersion
	elif Environment.isEmmWebservicesServer:
		currentVersion = Environment.webservicesVersion
	elif Environment.isEmmConsoleRdirServer:
		currentVersion = Environment.consoleRdirVersion
	elif Environment.isEmmRdirServer:
		currentVersion = Environment.rdirVersion
	elif Environment.isEmmMergerServer:
		currentVersion = Environment.mergerBackendVersion
	elif Environment.isEmmMailerServer:
		currentVersion = Environment.mailerBackendVersion
	elif Environment.isEmmMailloopServer:
		currentVersion = Environment.mailloopBackendVersion
	else:
		currentVersion = "Unknown"
	dumpFile = DbConnector.createDatabaseBackup(Environment.applicationName, currentVersion)
	if dumpFile is None:
		Environment.errors.append("Cannot create database backup")
	else:
		Environment.messages.append("Database backup created in file '" + dumpFile + "'")

def dbRestoreMenuAction(actionParameters):
	dbcfgEntry = DbConnector.dbcfgProperties[DbConnector.applicationDbcfgEntryName] if DbConnector.applicationDbcfgEntryName in DbConnector.dbcfgProperties else None

	print(Environment.applicationName + " database restore")

	print(Colors.RED + "Restoring the database will overwrite all existing data.!" + Colors.DEFAULT)
	print(Colors.RED + "Please consider creating a backup of your existing data." + Colors.DEFAULT)
	print("Do you want to create a database backup now? (Y/n, Blank => Yes):")
	answer = input(" > ").lower().strip()
	if answer == "" or answer.startswith("y") or answer.startswith("j"):
		if Environment.isOpenEmmServer:
			currentVersion = Environment.frontendVersion
		elif Environment.isEmmFrontendServer:
			currentVersion = Environment.frontendVersion
		elif Environment.isEmmStatisticsServer:
			currentVersion = Environment.statisticsVersion
		elif Environment.isEmmWebservicesServer:
			currentVersion = Environment.webservicesVersion
		elif Environment.isEmmConsoleRdirServer:
			currentVersion = Environment.consoleRdirVersion
		elif Environment.isEmmRdirServer:
			currentVersion = Environment.rdirVersion
		elif Environment.isEmmMergerServer:
			currentVersion = Environment.mergerBackendVersion
		elif Environment.isEmmMailerServer:
			currentVersion = Environment.mailerBackendVersion
		elif Environment.isEmmMailloopServer:
			currentVersion = Environment.mailloopBackendVersion
		else:
			currentVersion = "Unknown"
		dumpFile = DbConnector.createDatabaseBackup(Environment.applicationName, currentVersion)
		if (dumpFile is not None):
			print("Database backup created in file '" + dumpFile + "'")
			print()
			Environment.messages.append("Database backup created in file '" + dumpFile + "'")
		else:
			return False

	print("Please enter backup file path (Blank => Cancel):")
	while True:
		backupFilePath = input(" > ")
		backupFilePath = backupFilePath.strip()
		backupFilePath = os.path.expanduser(backupFilePath)
		if backupFilePath == "":
			return
		else:
			if backupFilePath.startswith("'") and backupFilePath.endswith("'"):
				backupFilePath = backupFilePath[1:-1]
			elif backupFilePath.startswith("\"") and backupFilePath.endswith("\""):
				backupFilePath = backupFilePath[1:-1]
			if not os.path.isfile(backupFilePath):
				Environment.errors.append("Invalid file path")
				return False
			else:
				break

	print(Colors.RED + "Restoring the database will overwrite all existing data!" + Colors.DEFAULT)
	print(Colors.RED + "Are you sure?" + Colors.DEFAULT + " (N/y, Blank => Cancel):")
	answer = input(" > ").lower().strip()
	if answer.startswith("y") or answer.startswith("j"):
		try:
			connection = DbConnector.openDbConnection()
			if connection is None:
				raise Exception("Cannot establish db connection")
			cursor = connection.cursor()

			print("Restoring database from file ...")
			print("Backup file: " + backupFilePath)
			subprocess.check_output(DbConnector.getDbClientPath() + " -u " + dbcfgEntry["user"] + " -h " + dbcfgEntry["host"] + " " + dbcfgEntry["name"] + " -p'" + dbcfgEntry["password"] + "' < " + backupFilePath, shell=True).decode("UTF-8").strip()
			print("Restore of database finished")
			Environment.messages.append("Database restored from file '" + backupFilePath + "'")
			Environment.rebootNeeded = True
		except Exception as e:
			errorText = "Error while restoring database from file: " + str(e)
			Environment.errors.append(errorText)
			logging.exception(errorText)
		finally:
			if cursor is not None:
				cursor.close()
			if connection is not None:
				connection.commit()
				connection.close()
