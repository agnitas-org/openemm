import os
import logging
import subprocess
import datetime
import urllib.parse
import getpass
from urllib.error import HTTPError

from EST_lib.Environment import Environment
from EST_lib import Colors
from EST_lib import DbConnector
from EST_lib import ESTUtilities

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
			return None
		elif not choice in DbConnector.dbcfgProperties:
			Environment.errors.append("Invalid database entry name: " + choice)
			return False
		else:
			dbEntryName = choice

	print("Database configuration for " + dbEntryName + ":")

	if DbConnector.dbcfgProperties[dbEntryName]["dbms"] == "oracle":
		if not "secure" in DbConnector.dbcfgProperties[dbEntryName]:
			if Environment.unsavedDbcfgChanges is None:
				Environment.unsavedDbcfgChanges = {}
				Environment.unsavedDbcfgChanges["dbEntryName"] = dbEntryName
			Environment.unsavedDbcfgChanges["secure"] = "false"
		if not "tablespaces" in DbConnector.dbcfgProperties[dbEntryName]:
			if Environment.unsavedDbcfgChanges is None:
				Environment.unsavedDbcfgChanges = {}
				Environment.unsavedDbcfgChanges["dbEntryName"] = dbEntryName
			Environment.unsavedDbcfgChanges["tablespaces"] = "true"

	if DbConnector.dbcfgProperties[dbEntryName]["dbms"] == "mariadb":
		Environment.checkForMandatoryMariaDBPackages(Environment.osVendor)

	for key, value in sorted(DbConnector.dbcfgProperties[dbEntryName].items()):
		color = ""
		if key in Environment.readonlyDbcfgProperties:
			print(Colors.RED + " " + key + " = " + value + Colors.DEFAULT)
		elif Environment.unsavedDbcfgChanges is not None and key in Environment.unsavedDbcfgChanges:
			print(Colors.YELLOW + " " + key + " = " + Environment.unsavedDbcfgChanges[key] + Colors.DEFAULT)
		else:
			print(" " + key + " = " + value)
	if Environment.unsavedDbcfgChanges is not None:
		for key, value in sorted(Environment.unsavedDbcfgChanges.items()):
			if key != "dbEntryName" and key not in DbConnector.dbcfgProperties[dbEntryName]:
				print(Colors.YELLOW + " " + key + " = " + Environment.unsavedDbcfgChanges[key] + Colors.DEFAULT)


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

			try:
				if ESTUtilities.userExists("openemm") and (os.environ["USER"] == "openemm" or ESTUtilities.hasRootPermissions()):
					usernameForSecureLibImportScript = "openemm"
				elif ESTUtilities.userExists(Environment.getRdirApplicationUserName()) and (os.environ["USER"] == Environment.getRdirApplicationUserName() or ESTUtilities.hasRootPermissions()):
					usernameForSecureLibImportScript = Environment.getRdirApplicationUserName()
				elif ESTUtilities.userExists(Environment.getFrontendApplicationUserName()) and (os.environ["USER"] == Environment.getFrontendApplicationUserName() or ESTUtilities.hasRootPermissions()):
					usernameForSecureLibImportScript = Environment.getFrontendApplicationUserName()

				DbConnector.updateDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath, Environment.unsavedDbcfgChanges, usernameForSecureLibImportScript)
				DbConnector.dbcfgProperties = DbConnector.readDbcfgPropertiesFile(DbConnector.dbcfgPropertiesFilePath)
				DbConnector.dbcfgEntry = DbConnector.dbcfgProperties[dbEntryName] if dbEntryName in DbConnector.dbcfgProperties else None
				DbConnector.emmDbVendor = DbConnector.dbcfgProperties[dbEntryName]["dbms"]
			except:
				if ESTUtilities.isDebugMode():
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

			if ESTUtilities.isBlank(Environment.getSystemUrl()) or Environment.getSystemUrl().strip() == "Unknown" and DbConnector.checkDbServiceAvailable() and DbConnector.checkDbStructureExists():
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
			elif dbcfgKey == "tablespaces":
				print("Please enter new value for key '" + dbcfgKey + "' (Allowed values are 'true' or 'false'): ")
			else:
				print("Please enter new value for key '" + dbcfgKey + "': ")
			dbcfgValue = input(" > ")
			if dbcfgKey == "dbms" and not dbcfgValue in Environment.allowedDbmsSystems:
				Environment.errors.append("Invalid dbms-type '" + dbcfgValue + "' for key '" + dbcfgKey + "'. Only '" + "', '".join(Environment.allowedDbmsSystems) + "' allowed.")
			elif dbcfgKey == "secure" and not dbcfgValue in ["true", "false"]:
					Environment.errors.append("Invalid secure attribute '" + dbcfgValue + "' for key '" + dbcfgKey + "'. Only 'true' or 'false' allowed.")
			elif dbcfgKey == "tablespaces" and not dbcfgValue in ["true", "false"]:
					Environment.errors.append("Invalid secure attribute '" + dbcfgValue + "' for key '" + dbcfgKey + "'. Only 'true' or 'false' allowed.")
			else:
				dbcfgValue = dbcfgValue.strip()
				DbConnector.dbcfgProperties[dbEntryName][dbcfgKey] = dbcfgValue
				if Environment.unsavedDbcfgChanges is None:
					Environment.unsavedDbcfgChanges = {}
					Environment.unsavedDbcfgChanges["dbEntryName"] = dbEntryName
				Environment.unsavedDbcfgChanges[dbcfgKey] = dbcfgValue

				if "dbms" in Environment.unsavedDbcfgChanges and Environment.unsavedDbcfgChanges["dbms"] == "mariadb":
					Environment.checkForMandatoryMariaDBPackages(Environment.osVendor)

			if dbcfgKey == "dbms":
				# Use default values
				if Environment.unsavedDbcfgChanges["dbms"] == "oracle":
					Environment.unsavedDbcfgChanges["jdbc-connect"] = "jdbc:oracle:thin:@[to be defined]:1521:emm"
					Environment.unsavedDbcfgChanges["jdbc-driver"] = "oracle.jdbc.driver.OracleDriver"
					Environment.unsavedDbcfgChanges["sid"] = "[to be defined]"
					Environment.unsavedDbcfgChanges["secure"] = "false"
					Environment.unsavedDbcfgChanges["tablespaces"] = "true"
				elif Environment.unsavedDbcfgChanges["dbms"] == "mariadb":
					Environment.unsavedDbcfgChanges["jdbc-connect"] = "jdbc:mariadb://localhost/" + Environment.applicationName.lower() + "?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=UTF-8"
					Environment.unsavedDbcfgChanges["jdbc-driver"] = "org.mariadb.jdbc.Driver"
					Environment.unsavedDbcfgChanges["secure"] = "false"
					if "sid" in Environment.unsavedDbcfgChanges: Environment.unsavedDbcfgChanges.pop("sid")
					if "tablespaces" in Environment.unsavedDbcfgChanges: Environment.unsavedDbcfgChanges.pop("tablespaces")
				elif Environment.unsavedDbcfgChanges["dbms"] == "mysql":
					Environment.unsavedDbcfgChanges["jdbc-connect"] = "jdbc:mysql://localhost/" + Environment.applicationName.lower() + "?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=UTF-8"
					Environment.unsavedDbcfgChanges["jdbc-driver"] = "com.mysql.cj.jdbc.Driver"
					Environment.unsavedDbcfgChanges["secure"] = "false"
					if "sid" in Environment.unsavedDbcfgChanges: Environment.unsavedDbcfgChanges.pop("sid")
					if "tablespaces" in Environment.unsavedDbcfgChanges: Environment.unsavedDbcfgChanges.pop("tablespaces")

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

	if DbConnector.emmDbVendor == "oracle":
		if DbConnector.checkDbServiceAvailable():
			if DbConnector.checkDbStructureExists():
				print("Are you sure to drop all current data from database? ('DROP' => drop data, Blank => Back):")
				choice = input(" > ")
				choice = choice.strip()
				if choice.upper() == "":
					return
				elif choice.upper() == "DROP":
					print("Dropping EMM Oracle database data ...")

					dbObjectsList = DbConnector.select("SELECT object_type, object_name FROM user_objects WHERE object_type != 'LOB' ORDER BY object_type DESC, object_name");
					for dbObject in dbObjectsList:
						nextSql = ""
						if dbObject[0] == "TABLE":
							nextSql = "TRUNCATE " + dbObject[0] + " " + dbObject[1]
							if ESTUtilities.isDebugMode():
								print("SQL: " + str(nextSql))
							try:
								DbConnector.executeSql(nextSql);
							except Exception as e:
								if ESTUtilities.isDebugMode():
									logging.exception("SQL caused error: " + str(nextSql))

							nextSql = "DROP " + dbObject[0] + " " + dbObject[1] + " CASCADE CONSTRAINTS"
							if ESTUtilities.isDebugMode():
								print("SQL: " + str(nextSql))
							try:
								DbConnector.executeSql(nextSql);
							except Exception as e:
								if ESTUtilities.isDebugMode():
									logging.exception("SQL caused error: " + str(nextSql))
						else:
							nextSql = "DROP " + dbObject[0] + " " + dbObject[1]
							if ESTUtilities.isDebugMode():
								print("SQL: " + str(nextSql))
							try:
								DbConnector.executeSql(nextSql);
							except Exception as e:
								if ESTUtilities.isDebugMode():
									logging.exception("SQL caused error: " + str(nextSql))

					leftOverDbObjectsList = DbConnector.select("SELECT object_type, object_name FROM user_objects WHERE object_type != 'LOB'");
					if len(leftOverDbObjectsList) == 0:
						print("Database data was dropped")
						Environment.messages.append("Database data was dropped")
					else:
						print("Leftover database objects after data was dropped")
						for dbObject in dbObjectsList:
							print(dbObject[0] + ": " + dbObject[1]);
						Environment.errors.append("Database data was dropped")

			if not DbConnector.checkDbStructureExists():
				fullDbBashScriptFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-initial-fulldb.sh"
				if os.path.isfile(fullDbBashScriptFilePath):
					print("Creating basic database structure ...")
					os.system("chmod u+x \"" + fullDbBashScriptFilePath + "\"")
					sqlUpdateReturnCode = os.system("\"" + fullDbBashScriptFilePath + "\" " + DbConnector.dbcfgPropertiesFilePath)
					if sqlUpdateReturnCode != 0:
						raise Exception("SQL database updates caused an error.")

					updateDbBashScriptFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-missing-updates.sh"
					if os.path.isfile(updateDbBashScriptFilePath):
						os.system("chmod u+x \"" + updateDbBashScriptFilePath + "\"")
						sqlUpdateReturnCode = os.system("\"" + updateDbBashScriptFilePath + "\" " + DbConnector.dbcfgPropertiesFilePath)

					DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
					DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
					DbConnector.insertBirtKeysInDb(Environment.getFrontendApplicationUserName())
				else:
					print("No EMM installed, so there is no SQL script to create the basic database structure")
			else:
				Environment.messages.append("Database structure exists and was not created now")
		return
	else:
		if DbConnector.checkDbExists(DbConnector.dbcfgEntry["name"]):
			host = DbConnector.dbcfgEntry["host"]
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
				
				if not DbConnector.checkDbRootCredentials(host, dbRootPassword):
					dbRootPassword = None
					print("Database root user password is invalid")
					print("Press any key to continue.")
					choice = input(" > ")
					return

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
			applicationUserName = Environment.getFrontendApplicationUserName()

		if DbConnector.checkDbStructureExists():
			print("Database structure already exists")
			Environment.messages.append("Database structure already exists")
		elif not os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql")\
			and not os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql"):
			print("Database structure file is not available")
			Environment.messages.append("Database structure file is not available. Please install " + Environment.applicationName + " application.")
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql"):
			print("Creating new database structure")
			sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mariadb-fulldb-basic_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
			fullDbScriptSuccess = DbConnector.executeSqlScriptFile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql", sqlUpdateLogfilePath)
			if not fullDbScriptSuccess:
				if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
					with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
						sqlLogContent = sqlUpdateLogfile.read()
						print(str(sqlLogContent))
				Environment.errors.append("Error while executing full database script")
				return
			elif os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-extended.sql"):
				print("Creating extended database structure ...")
				sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mariadb-fulldb-extended_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
				fullDbExtendedScriptSuccess = DbConnector.executeSqlScriptFile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-extended.sql", sqlUpdateLogfilePath)
				if not fullDbExtendedScriptSuccess:
					if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
						with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
							sqlLogContent = sqlUpdateLogfile.read()
							print(str(sqlLogContent))
					Environment.errors.append("Error while executing full database extended script")
					return
			else:
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-openemm.sql"):
					print("Creating OpenEMM database structure ...")
					sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mariadb-fulldb-openemm_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
					openemmFullDbScriptSuccess = DbConnector.executeSqlScriptFile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-openemm.sql", sqlUpdateLogfilePath)
					if not openemmFullDbScriptSuccess:
						if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
							with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
								sqlLogContent = sqlUpdateLogfile.read()
								print(str(sqlLogContent))
						Environment.errors.append("Error while executing OpenEMM full database script")
						return

			print("Database update started")

			if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh"):
				os.system("chmod u+x " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh")
				sqlUpdateReturnCode = os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)
			else:
				os.system("chmod u+x " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh")
				sqlUpdateReturnCode = os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)

			if sqlUpdateReturnCode != 0:
				print("SQL database updates caused an error. Press any key to continue.")
				choice = input(" > ")
				Environment.errors.append("Error while executing database update scripts")
				print("Database update had errors")
			else:
				DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
				DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
				DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else Environment.getFrontendApplicationUserName())
				print("Database update finished")

			Environment.messages.append("Database data was dropped and recreated")

			DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
			DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
			DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else Environment.getFrontendApplicationUserName())
			return
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql"):
			print("Creating new database structure")
			sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mysql-fulldb-basic_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
			fullDbScriptSuccess = DbConnector.executeSqlScriptFile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql", sqlUpdateLogfilePath)
			if not fullDbScriptSuccess:
				if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
					with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
						sqlLogContent = sqlUpdateLogfile.read()
						print(str(sqlLogContent))
				Environment.errors.append("Error while executing full database script")
				return
			elif os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-extended.sql"):
				print("Creating extended database structure ...")
				sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mysql-fulldb-extended_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
				fullDbExtendedScriptSuccess = DbConnector.executeSqlScriptFile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-extended.sql", sqlUpdateLogfilePath)
				if not fullDbExtendedScriptSuccess:
					if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
						with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
							sqlLogContent = sqlUpdateLogfile.read()
							print(str(sqlLogContent))
					Environment.errors.append("Error while executing full database extended script")
					return
			else:
				if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-openemm.sql"):
					print("Creating OpenEMM database structure ...")
					sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/emm-mysql-fulldb-openemm_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
					openemmFullDbScriptSuccess = DbConnector.executeSqlScriptFile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-openemm.sql", sqlUpdateLogfilePath)
					if not openemmFullDbScriptSuccess:
						if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
							with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
								sqlLogContent = sqlUpdateLogfile.read()
								print(str(sqlLogContent))
						Environment.errors.append("Error while executing OpenEMM full database script")
						return

			print("Database update started")

			if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh"):
				os.system("chmod u+x " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh")
				sqlUpdateReturnCode = os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)
			else:
				os.system("chmod u+x " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh")
				sqlUpdateReturnCode = os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath)

			if sqlUpdateReturnCode != 0:
				print("SQL database updates caused an error. Press any key to continue.")
				choice = input(" > ")
				Environment.errors.append("Error while executing database update scripts")
				print("Database update had errors")
			else:
				DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
				DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
				DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else Environment.getFrontendApplicationUserName())
				print("Database update finished")

			Environment.messages.append("Database data was dropped and recreated")

			DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
			DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
			DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else Environment.getFrontendApplicationUserName())
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

def deleteMarkedCompaniesAction(actionParameters):
	while True:
		companyIDsWithStatusToDelete = DbConnector.getCompaniesWithToDelete()
		print("Please choose company for the final deletion: (Blank => Back)")

		for index, (company_id, company_name) in enumerate(companyIDsWithStatusToDelete):
			print(f"{index + 1}. {company_name} (id: {company_id})")

		choice = input(" > ").strip()
		if choice == "":
			break
		choice = ESTUtilities.str_to_int(choice) -1
		if choice is not None and 0 <= choice <= len(companyIDsWithStatusToDelete):
			cid = companyIDsWithStatusToDelete[choice][0]

			print("Please enter your Superviser username: ")
			svu = input(" > ").strip()
			if not svu:
				return

			print("Please enter your Superviser password: ")
			pwd = getpass.getpass(" > ")
			if not pwd:
				return
			try:
				connection = ESTUtilities.openUrlConnection(Environment.getSystemUrl() + "/demoaccount?companyID=" + str(cid) +"&svu=" + urllib.parse.quote_plus(svu) +"&method=deleteaccount&pwd=" + urllib.parse.quote_plus(pwd))
				if connection.read() == "Access denied":
					print("Supervisor credentials are incorrect")
				elif connection.status == 200:
					Environment.messages.append(f"Deletion of company {cid} started")
				elif connection.status == 403:
					print("Request returned unauthorized")
				elif connection.status == 500:
					print("There was an error with the request")
			except HTTPError:
				print("Supervisor credentials are incorrect")
			return
		else:
			if len(companyIDsWithStatusToDelete) == 1:
				print("Please select an existing index, like in this case '1'", end="\n\n")
			elif len(companyIDsWithStatusToDelete) > 1:
				print("Please select a number between 1 and", len(companyIDsWithStatusToDelete) - 1, end="\n\n")
