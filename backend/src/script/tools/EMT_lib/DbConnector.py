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
import re
import time
import subprocess
import logging
import telnetlib

from EMT_lib import EMTUtilities

oraclePythonDriverAvailable = None
mysqlPythonDriverAvailable = None
mariadbPythonDriverAvailable = None
dbClientPath = None
emmDbVendor = None

dbcfgPropertiesFilePath = None
dbcfgProperties = None
applicationDbcfgEntryName = None
dbcfgEntry = None

def isOracleDriverModuleAvailable():
	global oraclePythonDriverAvailable

	if oraclePythonDriverAvailable is None:
		try:
			import cx_Oracle
			oraclePythonDriverAvailable = True
		except:
			oraclePythonDriverAvailable = False
	return oraclePythonDriverAvailable

def isMariadbDriverModuleAvailable():
	global mariadbPythonDriverAvailable

	if mariadbPythonDriverAvailable is None:
		try:
			import mariadb
			mariadbPythonDriverAvailable = True
		except:
			mariadbPythonDriverAvailable = False
	return mariadbPythonDriverAvailable

def recheckMariadbDriverModuleAvailability():
	global mariadbPythonDriverAvailable

	try:
		import mariadb
		mariadbPythonDriverAvailable = True
	except:
		mariadbPythonDriverAvailable = False
	return mariadbPythonDriverAvailable

def isMysqlDriverModuleAvailable():
	global mysqlPythonDriverAvailable

	if mysqlPythonDriverAvailable is None:
		try:
			import MySQLdb
			mysqlPythonDriverAvailable = True
		except:
			mysqlPythonDriverAvailable = False
	return mysqlPythonDriverAvailable

def recheckMysqlDriverModuleAvailability():
	global mysqlPythonDriverAvailable

	try:
		import MySQLdb
		mysqlPythonDriverAvailable = True
	except:
		mysqlPythonDriverAvailable = False
	return mysqlPythonDriverAvailable

# Read the special dbcfg file format (no comments read)
# Attention: ', ' characters are not allowed in keypair names or values, especially in passwords
def readDbcfgPropertiesFile(filePath):
	dbcfgProperties = {}
	dbcfgLinePattern = re.compile ("([a-z0-9._+-]+):[ \t]*(.*)$", re.IGNORECASE)
	with open(filePath, "r", encoding="UTF-8") as dbcfgPropertiesFileHandle:
		for line in dbcfgPropertiesFileHandle:
			line = line.strip()
			if line and not line.startswith("#"):
				lineMatch = dbcfgLinePattern.match(line)
				if not lineMatch is None:
					(dbEntryName, parameters) = lineMatch.groups()
					if parameters:
						properties = {}
						for keyValuePairString in [_p.strip () for _p in parameters.split(", ")]:
							keyValueParts = [_e.strip () for _e in keyValuePairString.split("=", 1)]
							if len(keyValueParts) == 2:
								properties[keyValueParts[0]] = keyValueParts[1]
					dbcfgProperties[dbEntryName] = properties
	return dbcfgProperties

# Write the special dbcfg file format
# Any comments within the existing file should be preserved
def updateDbcfgPropertiesFile(filePath, changedDbcfgProperties):
	dbcfgLinePattern = re.compile ("([a-z0-9._+-]+):[ \t]*(.*)$", re.IGNORECASE)

	with open(filePath, "r", encoding="UTF-8") as propertiesFileHandle:
		dbcfgPropertiesData = propertiesFileHandle.read()

	dbEntryName = changedDbcfgProperties["dbEntryName"]
	dbcfgPropertiesNewData = ""
	for line in dbcfgPropertiesData.splitlines():
		if line and not line.startswith("#"):
			lineMatch = dbcfgLinePattern.match(line)
			if lineMatch is not None:
				(currentDbEntryName, parameters) = lineMatch.groups()
				currentDbEntryName = currentDbEntryName.strip()
				if currentDbEntryName == dbEntryName:
					oldProperties = {}

					oldPropertiesValuePairs = line[line.find(":") + 1:].split(",")
					for valuePair in oldPropertiesValuePairs:
						name, value = valuePair.strip().split("=", 1)
						oldProperties[name] = value

					for key, value in list(changedDbcfgProperties.items()):
						if key != "dbEntryName":
							oldProperties[key] = value

					line = ""
					for key, value in list(oldProperties.items()):
						if not (key == "secure" and value == "false"):
							line = line + (", " if len(line) > 0 else "") + key + "=" + value
					line = dbEntryName + ": " + line
		dbcfgPropertiesNewData = dbcfgPropertiesNewData + line + "\n"
	dbcfgPropertiesData = dbcfgPropertiesNewData

	if os.access(filePath, os.W_OK):
		with open(filePath, "w", encoding="UTF-8") as propertiesFileHandle:
			propertiesFileHandle.write(dbcfgPropertiesData)
	else:
		raise Exception("Cannot update DbcfgProperties in readonly file '" + filePath + "'")
	
	dbcfgProperties = readDbcfgPropertiesFile(filePath)
	if dbcfgProperties[dbEntryName]["dbms"] == "oracle":
		if "secure" in dbcfgPropertiesData[dbEntryName] and dbcfgPropertiesData[dbEntryName]["secure"] == "true":
			if os.path.isdir("/home/openemm") and (os.getlogin() == "openemm" or EMTUtilities.hasRootPermissions()):
				setupSecureOracleDbLibrariesForStartup("openemm")
			if os.path.isdir("/home/console") and (os.getlogin() == "console" or EMTUtilities.hasRootPermissions()):
				setupSecureOracleDbLibrariesForStartup("console")
			if os.path.isdir("/home/rdir") and (os.getlogin() == "rdir" or EMTUtilities.hasRootPermissions()):
				setupSecureOracleDbLibrariesForStartup("rdir")
		else:
			if os.path.isdir("/home/openemm") and (os.getlogin() == "openemm" or EMTUtilities.hasRootPermissions()):
				removeSecureOracleDbLibrariesForStartup("openemm")
			if os.path.isdir("/home/console") and (os.getlogin() == "console" or EMTUtilities.hasRootPermissions()):
				removeSecureOracleDbLibrariesForStartup("console")
			if os.path.isdir("/home/rdir") and (os.getlogin() == "rdir" or EMTUtilities.hasRootPermissions()):
				removeSecureOracleDbLibrariesForStartup("rdir")

def parseJdbcConnectionString(jdbcConnectionString):
	if jdbcConnectionString is None or len(jdbcConnectionString.strip()) == 0:
		return None
	else:
		jdbcConnectionProperties = {}
		protocolEndIndex = jdbcConnectionString.find("@")
		if protocolEndIndex > 0:
			jdbcConnectionProperties["protocol"] = jdbcConnectionString[0:protocolEndIndex]
		else:
			raise Exception("Invalid jdbcConnectionString (protocol is missing): " + jdbcConnectionString)
		dbIdentifier = jdbcConnectionString[protocolEndIndex + 1:]
		dbIdentifierParts = dbIdentifier.replace("/", ":").split(":")
		if len(dbIdentifierParts) == 1:
			# TNS names based SID
			jdbcConnectionProperties["sid"] = dbIdentifierParts[0]
		elif len(dbIdentifierParts) == 3:
			# Format hostname:port:dbname
			host = dbIdentifierParts[0]
			while host.startswith("/"):
				host = host[1:]
			jdbcConnectionProperties["host"] = host
			jdbcConnectionProperties["port"] = int(dbIdentifierParts[1])
			jdbcConnectionProperties["dbname"] = dbIdentifierParts[2]
		else:
			raise Exception("Invalid jdbcConnectionString (Unknown dbIdentifier format): " + jdbcConnectionString)
		return jdbcConnectionProperties

def openDbConnection():
	global dbcfgEntry

	if dbcfgEntry["dbms"].lower() == "oracle":
		if oraclePythonDriverAvailable == False:
			raise Exception("The database vendor " + dbcfgEntry["dbms"] + " is not supported by this python installation")

		import cx_Oracle

		if "secure" in dbcfgEntry and len(dbcfgEntry["secure"]) > 0 and dbcfgEntry["secure"].lower().strip() == "true":
			host = dbcfgEntry["host"]

			portStartIndex = host.find(":")
			if portStartIndex > 0:
				portString = host[portStartIndex + 1:]
				if "/" in portString:
					portString = portString[0:portString.find("/")]
				port = int(portString)
				host = host[0:portStartIndex]
			else:
				port = 2848

			connect_string = "(description= (retry_count=20)(retry_delay=3)"\
				"(address="\
				"(protocol=tcps)"\
				"(port=" + str(port) + ")"\
				"(host=" + host + ")"\
				")"\
				"(connect_data=(service_name=" + dbcfgEntry["sid"] + "))"\
				")"

			connection = cx_Oracle.connect(user=dbcfgEntry["user"], password=dbcfgEntry["password"], dsn=connect_string)
		elif "host" in dbcfgEntry and len(dbcfgEntry["host"]) > 0:
			host = dbcfgEntry["host"]

			portStartIndex = host.find(":")
			if portStartIndex > 0:
				portString = host[portStartIndex + 1:]
				if "/" in portString:
					portString = portString[0:portString.find("/")]
				port = int(portString)
				host = host[0:portStartIndex]
			else:
				port = 1521

			dsn_tns = cx_Oracle.makedsn(host, port, service_name=dbcfgEntry["sid"])
			connection = cx_Oracle.connect(user=dbcfgEntry["user"], password=dbcfgEntry["password"], dsn=dsn_tns)
		else:
			connection = cx_Oracle.connect(dbcfgEntry["user"] + "/" + dbcfgEntry["password"] + "@" + dbcfgEntry["sid"])
		return connection
	elif dbcfgEntry["dbms"].lower() == "mysql":
		if mysqlPythonDriverAvailable == False:
			raise Exception("The database vendor " + dbcfgEntry["dbms"] + " is not supported by this python installation")

		host = dbcfgEntry["host"]
		if host is None or len(host) == 0:
			print("Missing database host configuration")
			sys.exit(1)

		portStartIndex = host.find(":")
		if portStartIndex > 0:
			portString = host[portStartIndex + 1:]
			if "/" in portString:
				portString = portString[0:portString.find("/")]
			port = int(portString)
			host = host[0:portStartIndex]
		else:
			port = 3306

		import MySQLdb

		if "secure" in dbcfgEntry and len(dbcfgEntry["secure"]) > 0 and dbcfgEntry["secure"].lower().strip() == "true":
			connection = MySQLdb.connect(host, dbcfgEntry["user"], dbcfgEntry["password"], dbcfgEntry["name"], port, "UTF-8", ssl=True)
		else:
			if host is not None and host.lower() == "localhost":
				connection = MySQLdb.connect(host, dbcfgEntry["user"], dbcfgEntry["password"], dbcfgEntry["name"])
			else:
				connection = MySQLdb.connect(host, dbcfgEntry["user"], dbcfgEntry["password"], dbcfgEntry["name"], port, "UTF-8")
		return connection
	elif dbcfgEntry["dbms"].lower() == "mariadb":
		if mariadbPythonDriverAvailable == False:
			raise Exception("The database vendor " + dbcfgEntry["dbms"] + " is not supported by this python installation")

		host = dbcfgEntry["host"]
		if host is None or len(host) == 0:
			print("Missing database host configuration")
			sys.exit(1)

		portStartIndex = host.find(":")
		if portStartIndex > 0:
			portString = host[portStartIndex + 1:]
			if "/" in portString:
				portString = portString[0:portString.find("/")]
			port = int(portString)
			host = host[0:portStartIndex]
		else:
			port = 3306

		import mariadb

		if "secure" in dbcfgEntry and len(dbcfgEntry["secure"]) > 0 and dbcfgEntry["secure"].lower().strip() == "true":
			connection = mariadb.connect(host=host, user=dbcfgEntry["user"], password=dbcfgEntry["password"], database=dbcfgEntry["name"], port=port, ssl=True)
		else:
			if host is not None and host.lower() == "localhost":
				connection = mariadb.connect(host=host, user=dbcfgEntry["user"], password=dbcfgEntry["password"], database=dbcfgEntry["name"])
			else:
				connection = mariadb.connect(host=host, user=dbcfgEntry["user"], password=dbcfgEntry["password"], database=dbcfgEntry["name"], port=port)
		return connection
	else:
		raise Exception("Invalid database type: " + dbcfgEntry["dbms"])

def detectDbClientPath(emmDbVendor, dbClientOracleSearchPaths, dbClientMysqlSearchPaths):
	global dbClientPath

	if emmDbVendor == "oracle":
		try:
			dbClientPath = subprocess.check_output("which sqlplus 2>/dev/null", shell=True).decode("UTF-8").strip()
			dbClientPath = os.path.realpath(dbClientPath)
		except:
			dbClientPath = None
		if dbClientPath is None:
			for dbClientSearchPath in dbClientOracleSearchPaths:
				if os.path.isfile(dbClientSearchPath):
					dbClientPath = dbClientSearchPath
					break
	else:
		try:
			dbClientPath = subprocess.check_output("which mysql 2>/dev/null", shell=True).decode("UTF-8").strip()
			dbClientPath = os.path.realpath(dbClientPath)
		except:
			dbClientPath = None
		if dbClientPath is None:
			try:
				dbClientPath = subprocess.check_output("which mariadb 2>/dev/null", shell=True).decode("UTF-8").strip()
			except:
				dbClientPath = None
		if dbClientPath is None:
			for dbClientSearchPath in dbClientMysqlSearchPaths:
				if os.path.isfile(dbClientSearchPath):
					dbClientPath = dbClientSearchPath
					break
	return dbClientPath

def getDbClientPath():
	global dbClientPath

	return dbClientPath

def getDbClientVersion():
	global dbClientPath

	try:
		process = subprocess.Popen([dbClientPath, "-V"], stdout=subprocess.PIPE)
		processOutput, processError = process.communicate()
		if processError is None and processOutput is not None and processOutput != "":
			return processOutput.decode("UTF-8").strip()
		else:
			return None
	except:
		return None

def update(sqlStatement, *parameters):
	executeSql(sqlStatement, True, *parameters)

def select(sqlStatement, *parameters):
	return executeSql(sqlStatement, False, *parameters)

def selectValue(sqlSelect, *parameters):
	result = select(sqlSelect, *parameters)
	if result is None:
		return None
	else:
		for row in result:
			# return first value only
			return row[0]

def executeSql(sqlStatement, isUpdate, *parameters):
	if sqlStatement is None or len(sqlStatement.strip()) == 0:
		raise Exception("Invalid empty sql statement")
	connection = None
	cursor = None
	try:
		connection = openDbConnection()
		if connection is None:
			raise Exception("Cannot establish db connection")
		cursor = connection.cursor()

		if "oracle" in str(type(connection)).lower():
			result = []
			parameterMap = {}
			parameterIndex = 0
			while "?" in sqlStatement:
				parameterIndex += 1
				if parameters is None or len(parameters) < parameterIndex:
					raise Exception("Invalid number of sql parameters: More '?' (" + str(parameterIndex) + ") in sql statement then parameters given (" + str(len(parameters)) + ")")
				else:
					sqlStatement = sqlStatement.replace("?", ":parameter" + str(parameterIndex), 1)
					parameterMap["parameter" + str(parameterIndex)] = parameters[parameterIndex - 1]
			if parameters is not None and len(parameters) > parameterIndex:
				raise Exception("Invalid number of sql parameters: Less '?' (" + str(parameterIndex) + ") in sql statement then parameters given (" + str(len(parameters)) + ")")
			cursor.execute(sqlStatement, parameterMap)
			if not isUpdate:
				for row in cursor:
					rowArray = []
					for i in range(len(row)):
						if row[i] is not None and "cx_Oracle.LOB" in str(type(row[i])):
							rowArray.append(row[i].read())
						else:
							rowArray.append(row[i])
					result.append(rowArray)
		else:
			result = []
			parameterList = []
			parameterIndex = 0
			while "?" in sqlStatement:
				parameterIndex += 1
				if parameters is None or len(parameters) < parameterIndex:
					raise Exception("Invalid number of sql parameters: More '?' (" + str(parameterIndex) + ") in sql statement then parameters given (" + str(len(parameters)) + ")")
				else:
					sqlStatement = sqlStatement.replace("?", "%s", 1)
					parameterList.append(parameters[parameterIndex - 1])
			if parameters is not None and len(parameters) > parameterIndex:
				raise Exception("Invalid number of sql parameters: Less '?' (" + str(parameterIndex) + ") in sql statement then parameters given (" + str(len(parameters)) + ")")
			cursor.execute(sqlStatement, tuple(parameterList))
			if not isUpdate:
				for row in cursor:
					rowArray = []
					for i in range(len(row)):
						rowArray.append(row[i])
					result.append(rowArray)

		return result
	except:
		if EMTUtilities.isDebugMode():
			print("SQL: " + sqlStatement)
			print("Parameters: " + str(parameters))
			logging.exception("executeSql")
		return None
	finally:
		if cursor is not None:
			cursor.close()
		if connection is not None:
			connection.commit()
			connection.close()

def executeSqlScriptFile(sqlScriptFilePath):
	global dbcfgEntry

	if dbcfgEntry["dbms"].lower() == "mysql" or dbcfgEntry["dbms"].lower() == "mariadb":

		host = dbcfgEntry["host"]
		if host is None or len(host) == 0:
			print("Missing database host configuration")
			sys.exit(1)

		portStartIndex = host.find(":")
		if portStartIndex > 0:
			portString = host[portStartIndex + 1:]
			if "/" in portString:
				portString = portString[0:portString.find("/")]
			port = int(portString)
			host = host[0:portStartIndex]
		else:
			port = 3306

		sqlUpdateReturnCode = os.system(getDbClientPath() + " -h " + host + " -P " + str(port) + " --protocol=TCP -u " + dbcfgEntry["user"] + " -p'" + dbcfgEntry["password"] + "' --database=" + dbcfgEntry["name"] + " --default-character-set=utf8 < " + sqlScriptFilePath)
		return sqlUpdateReturnCode == 0
	else:
		print("Invalid database type: " + dbcfgEntry["dbms"])
		sys.exit(1)

def checkDbServiceAvailable():
	global dbcfgProperties
	global dbcfgEntry

	if dbcfgProperties is None or len(dbcfgProperties) == 0:
		return False
	elif dbcfgEntry is None or len(dbcfgEntry) == 0:
		return False

	if dbcfgEntry["dbms"] is not None and dbcfgEntry["dbms"].lower() == "oracle":
		jdbcConnectProperties = parseJdbcConnectionString(dbcfgEntry["jdbc-connect"])
		if jdbcConnectProperties is None:
			return False

		if "host" in jdbcConnectProperties and "port" in jdbcConnectProperties:
			telnetConnection = None
			try:
				telnetConnection = telnetlib.Telnet()
				telnetConnection.open(jdbcConnectProperties["host"], jdbcConnectProperties["port"], 1)
				return True
			except:
				return False
			finally:
				if telnetConnection is not None:
					telnetConnection.close()
		return True
	else:
		host = dbcfgEntry["host"]
		if host is None or len(host) == 0:
			return False

		portStartIndex = host.find(":")
		if portStartIndex > 0:
			portString = host[portStartIndex + 1:]
			if "/" in portString:
				portString = portString[0:portString.find("/")]
			port = int(portString)
			host = host[0:portStartIndex]
		else:
			port = 3306

		try:
			telnetConnection = telnetlib.Telnet()
			telnetConnection.open(host, port, 1)
			return True
		except:
			return False
		finally:
			if telnetConnection is not None:
				telnetConnection.close()

def checkDbConnection():
	global emmDbVendor
	global dbcfgEntry

	if emmDbVendor == "mariadb":
		try:
			versionText = selectValue("SELECT VERSION()")
			if versionText is None:
				raise Exception("Cannot detect database vendor and version")
			elif not "MariaDB" in versionText:
				raise Exception("Configured database vendor is MariaDB, but actually used database is no MariaDB. Please adjust database configuration in menu Configuration, sub-menu Change configuration of database connection.")
			else:
				if not dbcfgEntry["jdbc-driver"] == "org.mariadb.jdbc.Driver":
					raise Exception("Configured database vendor is MariaDB and used database is MariaDB, but the configured database driver is not MariaDB. Please adjust database configuration in menu Configuration, sub-menu Change configuration of database connection.")
		except:
			if EMTUtilities.isDebugMode():
				logging.exception("Error in checkDbConnection")
			return False
	elif emmDbVendor == "mysql":
		try:
			versionText = selectValue("SELECT VERSION()")
			if versionText is None:
				raise Exception("Cannot detect database vendor and version")
			elif "MariaDB" in versionText:
				raise Exception("Configured database vendor is MySQL, but actually used database is no MySQL. Please adjust database configuration in menu Configuration, sub-menu Change configuration of database connection.")
			else:
				if not dbcfgEntry["jdbc-driver"] == "com.mysql.jdbc.Driver" and not dbcfgEntry["jdbc-driver"] == "com.mysql.cj.jdbc.Driver":
					raise Exception("Configured database vendor is MariaDB and used database is MariaDB, but the configured database driver is not MariaDB. Please adjust database configuration in menu Configuration, sub-menu Change configuration of database connection.")
		except:
			if EMTUtilities.isDebugMode():
				logging.exception("Error in checkDbConnection")
			return False
	else:
		connection = None
		cursor = None
		try:
			connection = openDbConnection()
			if connection is None:
				raise Exception("Cannot establish db connection")
			cursor = connection.cursor()
		except:
			if EMTUtilities.isDebugMode():
				logging.exception("Error in checkDbConnection")
			return False
		finally:
			if cursor is not None:
				cursor.close()
			if connection is not None:
				connection.commit()
				connection.close()
	return True

def checkDbExists(databaseName):
	try:
		return selectValue("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?", databaseName) >= 0
	except:
		if EMTUtilities.isDebugMode():
			logging.exception("Error in checkDbExists")
		return False

def checkDbStructureExists():
	connection = None
	cursor = None
	try:
		connection = openDbConnection()
		if connection is None:
			raise Exception("Cannot establish db connection")
		cursor = connection.cursor()

		cursor.execute("SELECT COUNT(*) FROM agn_dbversioninfo_tbl")

		for row in cursor:
			if row[0] >= 0:
				return True
		return False
	except:
		return False
	finally:
		if cursor is not None:
			cursor.close()
		if connection is not None:
			connection.commit()
			connection.close()

def createDatabaseBackup(applicationName, currentVersion):
	global dbcfgEntry

	try:
		connection = openDbConnection()
		if connection is None:
			raise Exception("Cannot establish db connection")
		cursor = connection.cursor()

		reloadGranted = False
		selectOnMysqlProcGranted = False
		cursor.execute("SHOW GRANTS")
		for row in cursor:
			if "GRANT ALL PRIVILEGES ".lower() in row[0].lower() or " reload " in row[0].lower() or " reload," in row[0].lower().replace('`','').replace('',''):
				reloadGranted = True
			if "GRANT ALL PRIVILEGES ON *.* ".lower() in row[0].lower() or "GRANT SELECT ON mysql.proc TO ".lower() in row[0].lower().replace('`','').replace('',''):
				selectOnMysqlProcGranted = True
		if reloadGranted and selectOnMysqlProcGranted:
			dumpFile = os.path.expanduser("~/database_" + applicationName + "_" + currentVersion + "_" + time.strftime("%Y%m%d-%H%M%S") +".dmp")
			print("Creating backup of database ...")
			print("Dumpfile: " + dumpFile)
			dbDumpToolPath = getDbClientPath() + "dump";
			if not os.path.isfile(dbDumpToolPath):
				dbDumpToolPath = dbDumpToolPath.replace("mariadbdump", "mysqldump")
			if not os.path.isfile(dbDumpToolPath):
				raise Exception("Database dump tool is not available: " + dbDumpToolPath)
				return
			subprocess.check_output(dbDumpToolPath + " -aCceQx --hex-blob --routines --triggers -u " + dbcfgEntry["user"] + " -h " + dbcfgEntry["host"] + " " + dbcfgEntry["name"] + " -p'" + dbcfgEntry["password"] + "' > " + dumpFile, shell=True).decode("UTF-8").strip()
			print("Creation of database backup finished")
			return dumpFile
		else:
			if not reloadGranted:
				raise Exception("Database user needs grant 'RELOAD' to check and change table format. (e.g. GRANT RELOAD ON *.* TO '" + dbcfgEntry["user"] + "'@'%';)")
			if not selectOnMysqlProcGranted:
				raise Exception("Database user needs grant 'SELECT ON mysql.proc' to backup database data. (e.g. GRANT SELECT ON mysql.proc TO '" + dbcfgEntry["user"] + "'@'%';)")
	except Exception as e:
		errorText = "Error while creating backup of database: " + str(e)
		logging.exception(errorText)
		raise Exception(errorText)
	finally:
		if cursor is not None:
			cursor.close()
		if connection is not None:
			connection.commit()
			connection.close()

def checkTableExists(tableName):
	try:
		selectValue("SELECT COUNT(*) FROM " + tableName + " WHERE 1 = 0")
		return True
	except:
		return False

def createDatabaseAndUser(host, dbname, username, userpassword, dbRootPassword = None):
	if not checkDbServiceAvailable():
		return False
	if getDbClientPath() is None:
		return False
	if checkDbConnection():
		return True

	if username == "root":
		dbRootPassword = userpassword

		if dbRootPassword is not None and dbRootPassword != "":
			passwordParameterPart = " -p'" + dbRootPassword + "'"
		else:
			passwordParameterPart = ""

		sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"CREATE DATABASE " + dbname + " CHARACTER SET utf8 COLLATE utf8_unicode_ci\"")
		if sqlUpdateReturnCode == 0 and checkDbConnection():
			return True
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"GRANT ALL PRIVILEGES ON " + dbname + ".* TO '" + username + "'\"")
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"GRANT SUPER ON *.* TO '" + username + "'\"")
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"GRANT SELECT ON mysql.proc TO '" + username + "'\"")
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"GRANT RELOAD ON *.* TO '" + username + "'\"")
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"FLUSH PRIVILEGES\"")
		return sqlUpdateReturnCode == 0
	else:
		if dbRootPassword is None: 
			print("Please enter database root user password:")
			dbRootPassword = input(" > ")

		if dbRootPassword is not None and dbRootPassword != "":
			passwordParameterPart = " -p'" + dbRootPassword + "'"
		else:
			passwordParameterPart = ""

		sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"CREATE DATABASE " + dbname + " CHARACTER SET utf8 COLLATE utf8_unicode_ci\" ")
		if sqlUpdateReturnCode == 0 and checkDbConnection():
			return True
		if sqlUpdateReturnCode == 0:
			# Drop already existing user before creating a new one. Ignore result of drop user.
			os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"DROP USER '" + username + "'@'localhost'\" 2>/dev/null")

			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"CREATE USER '" + username + "'@'localhost' IDENTIFIED BY '" + userpassword + "'\"")
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"GRANT ALL PRIVILEGES ON " + dbname + ".* TO '" + username + "'@'localhost'\"")
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"GRANT SUPER ON *.* TO '" + username + "'@'localhost'\"")
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"GRANT SELECT ON mysql.proc TO '" + username + "'@'localhost'\"")
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"GRANT RELOAD ON *.* TO '" + username + "'@'localhost'\"")
		if sqlUpdateReturnCode == 0:
			sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"FLUSH PRIVILEGES\"")
		return sqlUpdateReturnCode == 0

def readConfigurationFromDB():
	configurationValues = []
	result = select("SELECT class, name, value, hostname FROM config_tbl ORDER BY class, name")
	for row in result:
		className = row[0]
		configName = row[1]
		value = row[2]
		hostname = row[3]
		configurationValues.append({"class": className, "name": configName, "value": value, "hostname": hostname})
	return configurationValues

def readConfigurationValueFromDB(configClass, configName, hostname):
	try:
		configurationValue = []
		result = select("SELECT class, name, hostname, value FROM config_tbl WHERE (hostname IS NULL OR TRIM(hostname) = '' OR hostname = ?) AND class = ? AND name = ?", hostname, configClass, configName)
		valueBuffer = None

		for row in result:
			rowName = row[0]
			rowName = row[1]
			rowHostname = row[2]
			rowValue = row[3]

			if rowHostname is not None and len(rowHostname.strip()) > 0:
				return rowValue
			else:
				valueBuffer = rowValue

		return valueBuffer
	except:
		if EMTUtilities.isDebugMode():
			logging.exception("Error in readConfigurationValueFromDB")
		return None

def updateConfigurationValueInDB(configClass, configName, configValue, hostname):
	# Check if entry already exists. SQL-Update of an entry with same value returns rowcount 0, so a SQL-Select is used.
	if configValue == "<delete>":
		update("DELETE FROM config_tbl WHERE class = ? AND name = ? AND (hostname = ? OR hostname IS NULL)", configClass, configName, hostname)
	else:
		itemExists = selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = ? AND name = ? AND hostname = ?", configClass, configName, hostname) > 0
		if itemExists:
			update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = ? AND name = ? AND hostname = ?", configValue, configClass, configName, hostname)
		else:
			itemExists = selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = ? AND name = ? AND hostname != ?", configClass, configName, hostname) > 0
			if itemExists:
				update("INSERT INTO config_tbl (class, name, hostname, value, creation_date, change_date, description) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", configClass, configName, hostname, configValue)
			else:
				itemExists = selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = ? AND name = ?", configClass, configName) > 0
				if itemExists:
					update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = ? AND name = ?", configValue, configClass, configName)
				else:
					update("INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", configClass, configName, configValue)

def readJobQueueHostsFromDB():
	jobQueueHosts = {}
	result = select("SELECT hostname, value FROM config_tbl WHERE class = 'jobqueue' AND name = 'execute' ORDER BY hostname")
	for row in result:
		hostName = "*" if row[0] is None else row[0]
		jobQueueHosts[hostName] = int(row[1]) > 0
	return jobQueueHosts

def storeJobQueueHostInDB(hostName, status):
	if hostName == "*" or hostName is None:
		itemExists = selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'jobqueue' AND name = 'execute' AND (hostname IS NULL OR hostname = '')") > 0
		if itemExists:
			update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = 'jobqueue' AND name = 'execute' AND (hostname IS NULL OR hostname = '')", 1 if status else 0)
		else:
			update("INSERT INTO config_tbl (class, name, hostname, value, creation_date, change_date, description) VALUES ('jobqueue', 'execute', NULL, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", 1 if status else 0)
	else:
		itemExists = selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'jobqueue' AND name = 'execute' AND hostname = ?", hostName) > 0
		if itemExists:
			update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = 'jobqueue' AND name= 'execute' AND hostname = ?", 1 if status else 0, hostName)
		else:
			update("INSERT INTO config_tbl (class, name, hostname, value, creation_date, change_date, description) VALUES ('jobqueue', 'execute', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", hostName, 1 if status else 0)

def removeJobQueueHostFromDB(hostName):
	if hostName == "*" or hostName is None:
		update("DELETE FROM config_tbl WHERE class = 'jobqueue' AND name = 'execute' AND (hostname IS NULL OR hostname = '')")
	else:
		update("DELETE FROM config_tbl WHERE class = 'jobqueue' AND name = 'execute' AND hostname = ?", hostName)

def insertBirtKeysInDb(applicationUserName):
	itemExists = selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'birt' AND name = 'privatekey' AND value = '[to be defined]'") > 0
	if itemExists and os.path.isfile("/home/" + applicationUserName + "/tomcat/conf/keys/birt_private.pem"):
		with open("/home/" + applicationUserName + "/tomcat/conf/keys/birt_private.pem", "r", encoding="UTF-8") as keyFileHandle:
			privateKeyFileData = keyFileHandle.read()
			privateKeyFileData = privateKeyFileData.replace("-----BEGIN RSA PRIVATE KEY-----", "")
			privateKeyFileData = privateKeyFileData.replace("-----END RSA PRIVATE KEY-----", "")
			privateKeyFileData = privateKeyFileData.replace("\r", "").replace("\n", "")

		update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = 'birt' AND name = 'privatekey'", privateKeyFileData)

	itemExists = selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'birt' AND name = 'publickey' AND value = '[to be defined]'") > 0
	if itemExists and os.path.isfile("/home/" + applicationUserName + "/tomcat/conf/keys/birt_public.pem"):
		with open("/home/" + applicationUserName + "/tomcat/conf/keys/birt_public.pem", "r", encoding="UTF-8") as keyFileHandle:
			publicKeyFileData = keyFileHandle.read()
			publicKeyFileData = publicKeyFileData.replace("-----BEGIN PUBLIC KEY-----", "")
			publicKeyFileData = publicKeyFileData.replace("-----END PUBLIC KEY-----", "")
			publicKeyFileData = publicKeyFileData.replace("\r", "").replace("\n", "")

		update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = 'birt' AND name = 'publickey'", publicKeyFileData)

def checkConfigurationInDB():
	mandatoryConfigEntries = []
	mandatoryConfigEntries.append(["system", "url", "[to be defined]"])
	mandatoryConfigEntries.append(["system", "defaultRdirDomain", "[to be defined]"])
	mandatoryConfigEntries.append(["system", "defaultMailloopDomain", "[to be defined]"])
	mandatoryConfigEntries.append(["birt", "url", "[to be defined]"])
	mandatoryConfigEntries.append(["birt", "privatekey", "[to be defined]"])
	mandatoryConfigEntries.append(["birt", "publickey", "[to be defined]"])
	mandatoryConfigEntries.append(["webservices", "url", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "bounce", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "error", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "feature_support", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "frontend", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "replyto", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "report_archive", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "sender", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "support", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "upload.database", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "upload.support", "[to be defined]"])
	mandatoryConfigEntries.append(["mailaddress", "info.cleaner", "[to be defined]"])
	mandatoryConfigEntries.append(["mailout", "ini.domain", "[to be defined]"])

	for mandatoryConfigEntry in mandatoryConfigEntries:
		itemExists = selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = ? AND name = ?", mandatoryConfigEntry[0], mandatoryConfigEntry[1]) > 0
		if not itemExists:
			update("INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", mandatoryConfigEntry[0], mandatoryConfigEntry[1], mandatoryConfigEntry[2])

def readLandingPagesFromDB(defaultLandingPage):
	landingPages = {}
	result = select("SELECT value FROM config_tbl WHERE name LIKE 'RdirLandingpage'")
	for row in result:
		landingPages["Default"] = row[0]
	if len(landingPages) == 0:
		landingPages["Default"] = defaultLandingPage
	result = select("SELECT domain, landingpage FROM landingpage_tbl ORDER BY domain")
	for row in result:
		landingPages[row[0]] = row[1]
	return landingPages

def updateLandingPageInDB(domain, landingPage):

	connection = None
	cursor = None
	try:
		connection = openDbConnection()
		if connection is None:
			raise Exception("Cannot establish db connection")
		cursor = connection.cursor()

		if domain.lower() == "default":
			if landingPage.lower() == "remove":
				if "oracle" in str(type(connection)).lower():
					cursor.execute("DELETE FROM config_tbl WHERE class = :class AND name = :name", {"class" : "system", "name" : "RdirLandingpage"})
				else:
					cursor.execute("DELETE FROM config_tbl WHERE class = %s AND name = %s", ("system", "RdirLandingpage"))
			else:
				if "oracle" in str(type(connection)).lower():
					cursor.execute("SELECT COUNT(*) FROM config_tbl WHERE class = :class AND name = :name", {"class" : "system", "name" : "RdirLandingpage"})
				else:
					cursor.execute("SELECT COUNT(*) FROM config_tbl WHERE class = %s AND name = %s", ("system", "RdirLandingpage"))
				for row in cursor:
					itemExists = row[0] > 0
				if itemExists:
					if "oracle" in str(type(connection)).lower():
						cursor.execute("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = :value WHERE class = :class AND name = :name", {"value": landingPage, "class" : "system", "name" : "RdirLandingpage"})
					else:
						cursor.execute("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = %s WHERE class = %s AND name = %s", (landingPage, "system", "RdirLandingpage"))
				else:
					if "oracle" in str(type(connection)).lower():
						cursor.execute("INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES (:class, :name, :value, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", {"class" : "system", "name": "RdirLandingpage", "value": landingPage})
					else:
						cursor.execute("INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES (%s, %s, %s, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", ("system", "RdirLandingpage", landingPage))
		else:
			if landingPage.lower() == "remove":
				if "oracle" in str(type(connection)).lower():
					cursor.execute("DELETE FROM landingpage_tbl WHERE domain = :domain", {"domain" : domain})
				else:
					cursor.execute("DELETE FROM landingpage_tbl WHERE domain = %s", (domain,))
			else:
				if "oracle" in str(type(connection)).lower():
					cursor.execute("SELECT COUNT(*) FROM landingpage_tbl WHERE domain = :domain", {"domain" : domain})
				else:
					cursor.execute("SELECT COUNT(*) FROM landingpage_tbl WHERE domain = %s", (domain,))
				for row in cursor:
					itemExists = row[0] > 0
				if itemExists:
					if "oracle" in str(type(connection)).lower():
						cursor.execute("UPDATE landingpage_tbl SET landingpage = :landingpage WHERE domain = :domain", {"landingpage": landingPage, "domain" : domain})
					else:
						cursor.execute("UPDATE landingpage_tbl SET landingpage = %s WHERE domain = %s", (landingPage, domain))
				else:
					if "oracle" in str(type(connection)).lower():
						cursor.execute("INSERT INTO landingpage_tbl (domain, landingpage) VALUES (:domain, :landingpage)", {"domain" : domain, "landingpage": landingPage})
					else:
						cursor.execute("INSERT INTO landingpage_tbl (domain, landingpage) VALUES (%s, %s)", (domain, landingPage))
	finally:
		if cursor is not None:
			cursor.close()
		if connection is not None:
			connection.commit()
			connection.close()

def setupSecureOracleDbLibrariesForStartup(username):
	additionalPropertiesData = ""
	additionalPropertiesFilePath = "/home/" + username + "/tomcat/bin/emm.sh.additional.properties"
	if os.path.isfile(additionalPropertiesFilePath):
		with open(additionalPropertiesFilePath, "r", encoding="UTF-8") as propertiesFileHandle:
			additionalPropertiesData = propertiesFileHandle.read()
		if not "# OracleDB secure connection setup: start" in additionalPropertiesData:
			additionalPropertiesData = additionalPropertiesData\
				+ "# OracleDB secure connection setup: start\n"\
				+ "rm -f ~/tomcat/lib/ojdbc8.jar\n"\
				+ "rm -f ~/tomcat/lib/orai18n.jar\n"\
				+ "rm -f ~/tomcat/lib/ucp.jar\n"\
				+ "rm -f ~/tomcat/lib/xstreams.jar\n"\
				+ "ln -s /opt/agnitas.com/software/oracle-instantclient/*.jar ~/tomcat/lib/\n"\
				+ "export ORACLE_HOME=/opt/agnitas.com/software/oracle-instantclient\n"\
				+ "export TNS_ADMIN=/opt/agnitas.com/etc\n"\
				+ "if [ \"${LD_LIBRARY_PATH}\" ]; then\n"\
				+ "	export LD_LIBRARY_PATH=\"${ORACLE_HOME}:${LD_LIBRARY_PATH}\"\n"\
				+ "else\n"\
				+ "	export LD_LIBRARY_PATH=\"${ORACLE_HOME}\"\n"\
				+ "fi\n"\
				+ "echo \"LD_LIBRARY_PATH is '${LD_LIBRARY_PATH}'\"\n"\
				+ "# OracleDB secure connection setup: end\n"
			with open(additionalPropertiesFilePath, "w", encoding="UTF-8") as additionalPropertiesFileHandle:
				additionalPropertiesFileHandle.write(additionalPropertiesData)

def removeSecureOracleDbLibrariesForStartup(username):
	additionalPropertiesFilePath = "/home/" + username + "/tomcat/bin/emm.sh.additional.properties"
	EMTUtilities.removeContentFromFile(additionalPropertiesFilePath, "# OracleDB secure connection setup: start", "# OracleDB secure connection setup: end")
