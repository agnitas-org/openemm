import importlib.util
import itertools
import sys
import os
import re
import time
import subprocess
import logging
import socket
from contextlib import suppress, contextmanager
from urllib.parse import urlparse
from itertools import chain
from operator import itemgetter
from pathlib import Path

from EST_lib import ESTUtilities

logger = logging.getLogger(__name__)

oraclePythonDriverAvailable = None
mariadbPythonDriverAvailable = None
postgresPythonDriverAvailable = None
dbClientPath = None
emmDbVendor = None

dbcfgPropertiesFilePath = None
dbcfgProperties = None
applicationDbcfgEntryName = None
dbcfgEntry: dict = {}
oracledb = None
attemptedInitializingThickClient = False

class DatabaseMissingException(Exception):
	def __init__(self, message, errors):
		super().__init__(message)
		self.errors = errors

class MissingTableException(Exception):
	def __init__(self, table=None):
		if table:
			super().__init__(f"The table {table} does not exist.")
		else:
			super().__init__("A table required for statement execution does not exist.")

class MissingDatabaseVendorException(Exception):
	def __init__(self, dbms):
		super().__init__(f"The database vendor {dbms} is not supported by this python installation")

def is_connectable(host: str, port: int | str, timeout: None | int | float = None, verbose: bool = False) -> bool:
	sock: None | socket.socket = None
	try:
		sock = socket.create_connection((host, port if isinstance(port, int) else int(port)), timeout=timeout)
		return True
	except Exception as e:
		if verbose:
			logger.warning(f'{host}:{port}: failed to connect: {e}')
	finally:
		if sock is not None:
			with suppress(Exception):
				sock.close()
	return False

def isOracleDriverModuleAvailable():
	global oraclePythonDriverAvailable

	if oraclePythonDriverAvailable is None:
		try:
			import oracledb
			oraclePythonDriverAvailable = True
		except ModuleNotFoundError:
			oraclePythonDriverAvailable = False
	return oraclePythonDriverAvailable

def isMariadbDriverModuleAvailable():
	global mariadbPythonDriverAvailable

	if mariadbPythonDriverAvailable is None:
		try:
			import mariadb
			mariadbPythonDriverAvailable = True
		except ModuleNotFoundError:
			mariadbPythonDriverAvailable = False
	return mariadbPythonDriverAvailable

def recheckMariadbDriverModuleAvailability():
	global mariadbPythonDriverAvailable

	try:
		import mariadb
		mariadbPythonDriverAvailable = True
	except ModuleNotFoundError:
		mariadbPythonDriverAvailable = False
	return mariadbPythonDriverAvailable

def isPostgresDriverModuleAvailable():
	global postgresPythonDriverAvailable

	if postgresPythonDriverAvailable is None:
		try:
			import psycopg2
			postgresPythonDriverAvailable = True
		except ModuleNotFoundError:
			postgresPythonDriverAvailable = False
	return postgresPythonDriverAvailable

# Read the special dbcfg file format (no comments read)
# Attention: ', ' characters are not allowed in keypair names or values, especially in passwords
def readDbcfgPropertiesFile(filePath):
	dbcfgProperties = {}
	dbcfgLinePattern = re.compile("([a-z0-9._+-]+):[ \t]*(.*)$", re.IGNORECASE)
	with open(filePath, "r", encoding="UTF-8") as dbcfgPropertiesFileHandle:
		for line in dbcfgPropertiesFileHandle:
			line = line.strip()
			if line and not line.startswith("#"):
				lineMatch = dbcfgLinePattern.match(line)
				if not lineMatch is None:
					(dbEntryName, parameters) = lineMatch.groups()
					if parameters:
						properties = {}
						for keyValuePairString in [_p.strip() for _p in parameters.split(", ")]:
							keyValueParts = [_e.strip() for _e in keyValuePairString.split("=", 1)]
							if len(keyValueParts) == 2:
								properties[keyValueParts[0]] = keyValueParts[1]
						dbcfgProperties[dbEntryName] = properties
	return dbcfgProperties

# Write the special dbcfg file format
# Any comments within the existing file should be preserved
def updateDbcfgPropertiesFile(filePath, changedDbcfgProperties, usernameForSecureLibImportScript):
	dbcfgLinePattern = re.compile("([a-z0-9._+-]+):[ \t]*(.*)$", re.IGNORECASE)

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
		if "secure" in dbcfgProperties[dbEntryName] and dbcfgProperties[dbEntryName]["secure"] == "true":
			if ESTUtilities.userExists(usernameForSecureLibImportScript) and (os.environ["USER"] == usernameForSecureLibImportScript or ESTUtilities.hasRootPermissions()):
				setupSecureOracleDbLibrariesForStartup(usernameForSecureLibImportScript)
			if ESTUtilities.userExists(usernameForSecureLibImportScript) and (os.environ["USER"] == usernameForSecureLibImportScript or ESTUtilities.hasRootPermissions()):
				setupSecureOracleDbLibrariesForStartup(usernameForSecureLibImportScript)
			if ESTUtilities.userExists("rdir") and (os.environ["USER"] == "rdir" or ESTUtilities.hasRootPermissions()):
				setupSecureOracleDbLibrariesForStartup("rdir")
		else:
			if ESTUtilities.userExists(usernameForSecureLibImportScript) and (os.environ["USER"] == usernameForSecureLibImportScript or ESTUtilities.hasRootPermissions()):
				removeSecureOracleDbLibrariesForStartup(usernameForSecureLibImportScript)

# Example JDBC ConnectionStrings:
# jdbc:oracle:oci:@XE
# jdbc:oracle:thin:@127.0.0.1:1521:emm
# jdbc:oracle:thin:@test.domain.local:1521:EMM
# jdbc:oracle:thin:@//test.domain.local:1521/special.domain.local
# -- not parsed hereby jdbc:mariadb://mariadb/emm?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=UTF-8
# -- not parsed hereby jdbc:mariadb://localhost/emm?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=UTF-8
# -- not parsed hereby jdbc:mariadb://127.0.0.1:3306/emm?zeroDateTimeBehavior=convertToNull&useUnicode=true&characterEncoding=UTF-8
def parseOracleJdbcConnectionString(jdbcConnectionString):
	if jdbcConnectionString is None or len(jdbcConnectionString.strip()) == 0:
		return None
	else:
		jdbcConnectionString = jdbcConnectionString.replace("@//", "@")
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
			# Format "hostname:port:dbname"
			host = dbIdentifierParts[0]
			while host.startswith("/"):
				host = host[1:]
			jdbcConnectionProperties["host"] = host
			jdbcConnectionProperties["port"] = int(dbIdentifierParts[1])
			jdbcConnectionProperties["dbname"] = dbIdentifierParts[2]
		else:
			raise Exception("Invalid jdbcConnectionString (Unknown dbIdentifier format): " + jdbcConnectionString)
		return jdbcConnectionProperties

def checkModuleAvailable(moduleName: str) -> bool:
	return importlib.util.find_spec(moduleName) is not None


def _openOracleConnection():
	global attemptedInitializingThickClient

	try:
		import oracledb

		if not attemptedInitializingThickClient and dbClientPath:
			attemptedInitializingThickClient = True
			oracledb.init_oracle_client()

		def _connect(dbcfg: dict):
			parameters = {}
			host = dbcfg.get("host", "")

			if host:
				parsedHost = urlparse(f"tcp://{host}")
				parameters["port"] = parsedHost.port
				parameters["host"] = parsedHost.hostname or host

			parameters["user"] = dbcfg.get("user", "")
			parameters["password"] = dbcfg.get("password", "")
			pyConnect = dbcfg.get("py-connect", {})
			if isinstance(pyConnect, dict):
				parameters.update(pyConnect)

			if dbcfg.get("secure", "").lower().strip() == "true":
				connectString = ("(description= (retry_count=20)(retry_delay=3)"
								 f"(address=(protocol=tcps)(port={parameters.get('port', 2848)})(host={host}))"
								 f"(connect_data=(service_name={dbcfg.get('sid')})))"
								)
				parameters["dsn"] = connectString
			elif parameters.get("host"):
				parameters["dsn"] = oracledb.makedsn(host, parameters.get('port', 1521), service_name=dbcfg["sid"])
			else:
				parameters = {"dsn": parameters["user"] + "/" + parameters["password"] + "@" + dbcfg["sid"]}
			try:
				connection = oracledb.connect(**parameters)
				return connection
			except Exception as e:
				if 'DPY-3010' in str(e):
					try:
						import cx_oracle
						connection = cx_oracle.connect(**parameters)
						return connection
					except Exception as e:
						raise Exception(str(e))
				logging.error(f"Instantiation of oracledb related functionality failed unexpectedly: {e}")

		return _connect
	except ImportError:
		pass

def _openPostgresConnection():
	try:
		import psycopg2

		def _connect(dbcfg: dict):
			parameters = {}

			host = dbcfg.get("host", "")

			if host:
				parsedHost = urlparse(f"tcp://{host}")
				parameters["port"] = parsedHost.port
				parameters["host"] = parsedHost.hostname or host

			parameters["user"] = dbcfg.get("user", "")
			parameters["password"] = dbcfg.get("password", "")
			parameters["dbname"] = dbcfg.get("name", "")
			pyConnect = dbcfg.get("py-connect", {})
			if isinstance(pyConnect, dict):
				parameters.update(pyConnect)
			try:
				connection = psycopg2.connect(**parameters)
				return connection
			except Exception as e:
				logger.error(f"Instantiation of postgreSQL related functionality failed unexpectedly: {e}")

		return _connect
	except ImportError:
		pass

def _openMariadbConnection():
	try:
		import mariadb
		def _connect(dbcfg: dict):
			parameters = {}

			host = dbcfg.get("host", "")
			if host:
				parsedHost = urlparse(f"tcp://{host}")
				parameters["port"] = parsedHost.port or 3306
				parameters["host"] = parsedHost.hostname or host

			parameters["user"] = dbcfg.get("user", "")
			parameters["password"] = dbcfg.get("password", "")
			parameters["database"] = dbcfg.get("name", "")
			pyConnect = dbcfg.get("py-connect", {})
			if isinstance(pyConnect, dict):
				parameters.update(pyConnect)

			if dbcfg.get("secure", "").lower().strip() == "true":
				parameters["ssl"] = True
			try:
				return mariadb.connect(**parameters)

			except mariadb.ProgrammingError as e:
				if str(e).startswith("Unknown database"):
					errorMessage = f"Database {dbcfg.get('name')} is not accessible or does not exist"
					if ESTUtilities.isDebugMode():
						logger.exception(errorMessage)
					raise DatabaseMissingException(errorMessage, e)
				return None

		return _connect
	except ImportError:
		pass


def detectNeededConnectionMethod(dbms: str) -> "mariadb.Connection oracledb.Connection":
	if dbms.lower() == "oracle":
		if not checkModuleAvailable('oracledb'):
			raise MissingDatabaseVendorException(dbms)
		return _openOracleConnection()
	elif dbms.lower() == "mariadb":
		if not checkModuleAvailable('mariadb'):
			raise MissingDatabaseVendorException(dbms)
		return _openMariadbConnection()
	elif dbms.lower() == "postgres":
		if not checkModuleAvailable('psycopg2'):
			raise MissingDatabaseVendorException(dbms)
		return _openPostgresConnection()
	else:
		raise Exception("The Database Management System {dbms} is not supported by this Tool.")

@contextmanager
def openDbConnection(dbcfg: dict | None = None):
	if not dbcfg:
		dbcfg = dbcfgEntry

	connection = None
	try:
		connectionMethod = detectNeededConnectionMethod(dbcfg.get("dbms"))
		connection = connectionMethod(dbcfg)
		yield connection
	except Exception as e:
		errorMessage = str(e)
		if 'ORA-00942' in errorMessage:
			raise MissingTableException() from e
		elif errorMessage.startswith("Table") and errorMessage.endswith("doesn't exist"):
			table = errorMessage.split("'")[1::3]
			raise MissingTableException(table)
	finally:
		if connection:
			connection.close()

def detectDbClientPath(emmDbVendor, dbClientOracleSearchPaths, dbClientMariadbSearchPaths, dbClientPostgresSearchPaths):
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
	elif emmDbVendor == "postgres":
		try:
			dbClientPath = subprocess.check_output("which psql 2>/dev/null", shell=True).decode("UTF-8").strip()
			dbClientPath = os.path.realpath(dbClientPath)
		except:
			dbClientPath = None
		if dbClientPath is None:
			for dbClientSearchPath in dbClientPostgresSearchPaths:
				if os.path.isfile(dbClientSearchPath):
					dbClientPath = dbClientSearchPath
					break
	else:
		try:
			dbClientPath = subprocess.check_output("which mariadb 2>/dev/null", shell=True).decode("UTF-8").strip()
			dbClientPath = os.path.realpath(dbClientPath)
		except:
			dbClientPath = None
		if dbClientPath is None:
			for dbClientSearchPath in dbClientMariadbSearchPaths:
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
	if result and len(result[0]) > 0:
		return result[0][0]
	else:
		return None
def selectIntValue(sqlSelect, *parameters):
	return selectValue(sqlSelect, *parameters) or 0

def executeSql(sqlStatement: str, isUpdate: bool = True, *parameters, withoutDatabase: bool = False):
	if sqlStatement is None or len(sqlStatement.strip()) == 0:
		raise Exception("Invalid empty sql statement")

	dbcfgArgument: dict = dbcfgEntry.copy()
	if withoutDatabase:
		dbcfgArgument.pop("name", None)

	try:
		with openDbConnection(dbcfgArgument) as connection:
			cursor = connection.cursor()

			placeholderOccurrence = sqlStatement.count('?')
			if len(parameters) < placeholderOccurrence:
				raise Exception("Invalid number of sql parameters: More '?' (" + str(
					placeholderOccurrence) + ") in sql statement then parameters given (" + str(len(parameters)) + ")")
			if len(parameters) > placeholderOccurrence:
				raise Exception("Invalid number of sql parameters: Less '?' (" + str(
					placeholderOccurrence) + ") in sql statement then parameters given (" + str(len(parameters)) + ")")
			result = []
			if "oracle" in str(type(connection)).lower():
				for parameterIndex in range(0, placeholderOccurrence):
					sqlStatement = sqlStatement.replace("?", f":{parameterIndex}", 1)
				cursor.execute(sqlStatement, parameters, fetch_lobs=False)
			elif "psycopg2" in str(type(connection)).lower():
				sqlStatement = sqlStatement.replace("?", "%s")
				cursor.execute(sqlStatement,parameters)
			else:
				cursor.execute(sqlStatement, parameters)
			if not isUpdate:
				for row in cursor:
					rowArray = []
					for i in range(len(row)):
						if isinstance(row[i],memoryview):
							rowArray.append(row[i].tobytes())
						else:
							rowArray.append(row[i])
					result.append(rowArray)
			connection.commit()
			return result
	except:
		if ESTUtilities.isDebugMode():
			print("SQL: " + sqlStatement)
			print("Parameters: " + str(parameters))
			logger.exception("executeSql")
		return None


def executeMissingUpdates(sqlUpdateScriptFilePath: str | Path):
	sqlUpdateScriptFilePath = Path(sqlUpdateScriptFilePath)
	sqlUpdateVersions: dict = {}
	sqlUpdateFiles = chain.from_iterable([sqlUpdateScriptFilePath.joinpath().glob(i) for i in
	                                      ["emm-*update*.sql", "emm-*-messages*.sql", "../userrights.sql",
	                                       "emm-*-migration.sql"]])
	versionFilePattern = re.compile(r"[0-9]+\.[0-9]+\.[0-9]+((\.|-hf)[0-9]+)?")
	necessarySQLScripts = []
	for file in sqlUpdateFiles:
		sqlUpdateScript = versionFilePattern.search(file.name)
		if sqlUpdateScript:
			sqlUpdateVersions[sqlUpdateScript.group(0)] = file.name
		else:
			necessarySQLScripts.append(file)

	query = """
	SELECT uv.version_number
	FROM (
	SELECT ? AS version_number FROM dual 
		{}
	) uv 
	WHERE NOT EXISTS ( 
	SELECT 1 FROM agn_dbversioninfo_tbl version_tbl 
	WHERE version_tbl.version_number = uv.version_number
	)
	""".format("UNION ALL SELECT ? FROM dual " * (len(sqlUpdateVersions) - 1))
	missingVersions = select(query, *sqlUpdateVersions.keys())

	if missingVersions:
		missingVersions = list(itertools.chain.from_iterable(missingVersions))
		for version in set(sqlUpdateVersions.keys()) - set(missingVersions):
			print("DB already contains version", version)

		updateFilesNeeded = itemgetter(*missingVersions)(sqlUpdateVersions)
		updateFilesNeeded = updateFilesNeeded if isinstance(updateFilesNeeded, (list, tuple)) else [updateFilesNeeded]
		for sqlFile in updateFilesNeeded:
			executeSqlScriptFile(sqlUpdateScriptFilePath / sqlFile)

	for sqlFile in necessarySQLScripts:
		executeSqlScriptFile(sqlUpdateScriptFilePath / sqlFile)

def executeSqlScriptFile(sqlScriptFilePath, logFilePath=None):
	global dbcfgEntry
	sqlScriptFilePath = Path(sqlScriptFilePath) if sqlScriptFilePath else None
	if not sqlScriptFilePath or not sqlScriptFilePath.name:
		raise Exception("Invalid empty sql statement")
	elif not sqlScriptFilePath.is_file() and not sqlScriptFilePath.is_symlink():
		raise Exception("Scriptfile does not exist: " + str(sqlScriptFilePath))

	if emmDbVendor == "oracle":
		try:
			dsn = dbcfgEntry["user"] + "/" + dbcfgEntry["password"] + "@" + dbcfgEntry["sid"]
			sqlInput = f"""
			WHENEVER SQLERROR EXIT SQL.SQLCODE;
			SET SERVEROUTPUT ON;
			SET ECHO OFF;
			SET DEFINE OFF;
			SET HEADING OFF;
			@{sqlScriptFilePath}
			EXIT;
			"""
			output = subprocess.run([getDbClientPath(), "-S", dsn], input=sqlInput, capture_output=True, text=True,
			                        check=False)
			return True
		except Exception as e:
			if ESTUtilities.isDebugMode():
				logger.exception(f"Error in executeSqlScriptFile: {e}")
			return False

	elif emmDbVendor in ["mariadb", "postgresql"]:
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

		commandText = getDbClientPath() + " -h " + host + " -P " + str(port) + " --protocol=TCP -u " + dbcfgEntry["user"] + " -p\"" + dbcfgEntry["password"] + "\" --database=" + dbcfgEntry["name"] + " --default-character-set=utf8 < " + str(sqlScriptFilePath)

		if logFilePath is not None:
			sqlUpdateReturnCode = os.system("/bin/bash -c '" + commandText + " 2>&1 | tee " + logFilePath + "; test ${PIPESTATUS[0]} -eq 0'")
		else:
			sqlUpdateReturnCode = os.system(commandText)

		if sqlUpdateReturnCode == 0:
			return True
		else:
			if ESTUtilities.isDebugMode():
				print("Error while executing '" + commandText + "'")
			return False
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
		jdbcConnectProperties = parseOracleJdbcConnectionString(dbcfgEntry["jdbc-connect"])
		if jdbcConnectProperties is None:
			return False

		if "host" in jdbcConnectProperties and "port" in jdbcConnectProperties:
			return is_connectable(jdbcConnectProperties["host"], jdbcConnectProperties["port"], 1)
		return True
	else:
		host = dbcfgEntry["host"] if "host" in dbcfgEntry else None
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
			if dbcfgEntry["dbms"].lower() == "postgres":
				port = 5432
			else:
				port = 3306

		return is_connectable(host, port, 1)

def checkDbConnectionWithoutDatabase():
	if emmDbVendor == "mariadb":
		result = executeSql("SELECT VERSION()", withoutDatabase=True)
		if result:
			return True
	else:
		try:
			with openDbConnection() as connection:
				if connection is None:
					raise Exception("Cannot establish db connection")
				connection.cursor()
		except:
			if ESTUtilities.isDebugMode():
				logger.exception("Error in checkDbConnection")
			return False
	return True

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
			if ESTUtilities.isDebugMode():
				logger.exception("Error in checkDbConnection")
			return False
	else:
		try:
			with openDbConnection() as connection:
				if connection is None:
					raise Exception("Cannot establish db connection")
		except:
			if ESTUtilities.isDebugMode():
				logger.exception("Error in checkDbConnection")
			return False
	return True

def checkDbExists(databaseName: str | None = None):
	try:
		if not databaseName:
			databaseName = dbcfgEntry.get("name")
		return selectIntValue("SELECT COUNT(*) FROM information_schema.schemata WHERE schema_name = ?", databaseName) >= 0
	except:
		if ESTUtilities.isDebugMode():
			logger.exception("Error in checkDbExists")
		return False

def checkDbStructureExists():
	return checkTableExists("agn_dbversioninfo_tbl") and checkTableExists("config_tbl");

def checkTableExists(tableName):
	try:
		with openDbConnection() as connection:
			if connection is None:
				raise Exception("Cannot establish db connection")
			cursor = connection.cursor()

			cursor.execute("SELECT COUNT(*) FROM " + tableName + " WHERE 1 = 0")

			for row in cursor:
				if row[0] >= 0:
					return True
			return False
	except:
		return False

def checkColumnExists(tableName, columnName):
	try:
		with openDbConnection() as connection:
			if connection is None:
				raise Exception("Cannot establish db connection")
			cursor = connection.cursor()

			cursor.execute("SELECT " + columnName + " FROM " + tableName + " WHERE 1 = 0")

			for row in cursor:
				if row[0] >= 0:
					return True
			return False
	except:
		return False

def createDatabaseBackup(applicationName, currentVersion):
	global dbcfgEntry

	try:
		with openDbConnection() as connection:
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
		logger.exception(errorText)
		raise Exception(errorText)

def createDatabaseAndUser(host, dbname, username, userpassword, dbRootPassword=None):
	if not checkDbServiceAvailable():
		return False
	if getDbClientPath() is None:
		return False
	if not checkDbConnectionWithoutDatabase():
		return False

	if username == "root":
		dbRootPassword = userpassword

		if dbRootPassword is not None and dbRootPassword != "":
			passwordParameterPart = " -p'" + dbRootPassword + "'"
		else:
			passwordParameterPart = ""

		if not checkDbRootCredentials(host, dbRootPassword):
			dbRootPassword = None
			print("Database root user password is invalid")
			print("Press any key to continue.")
			choice = input(" > ")
			return

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

def readConfigurationFromDB(hostname):
	configurationValues = []
	result = select("SELECT class, name, value, hostname FROM config_tbl WHERE hostname IS NULL OR TRIM(hostname) = '' OR hostname = ? ORDER BY class, name", hostname)
	for row in result:
		className = row[0]
		configName = row[1]
		value = row[2]
		valueHostname = row[3]

		foundValue = False
		if valueHostname == hostname:
			for configurationValue in configurationValues:
				if configurationValue["class"] == className and configurationValue["name"] == configName:
					configurationValue["value"] = value
					configurationValue["hostname"] = hostname
					foundValue = True
					break
		if not foundValue:
			configurationValues.append({"class": className, "name": configName, "value": value, "hostname": valueHostname})
	return configurationValues

def readConfigurationValueFromDB(configClass, configName, hostname):
	try:
		configurationValue = []
		result = select("SELECT class, name, hostname, value FROM config_tbl WHERE (hostname IS NULL OR TRIM(hostname) = '' OR hostname = ?) AND class = ? AND name = ?", hostname, configClass, configName)

		if result == None:
			return None
		else:
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
		if ESTUtilities.isDebugMode():
			logger.exception("Error in readConfigurationValueFromDB")
		return None

def updateConfigurationValueInDB(configClass, configName, configValue, hostname):
	if checkTableExists("config_tbl"):
		# Check if entry already exists. SQL-Update of an entry with same value returns rowcount 0, so a SQL-Select is used.
		if configValue == "<delete>":
			update("DELETE FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR TRIM(hostname) = '' OR hostname = ?)", configClass, configName, hostname)
		else:
			itemExists = selectIntValue("SELECT COUNT(*) FROM config_tbl WHERE class = ? AND name = ? AND hostname = ?", configClass, configName, hostname) > 0
			if itemExists:
				update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = ? AND name = ? AND hostname = ?", configValue, configClass, configName, hostname)
			else:
				itemExists = selectIntValue("SELECT COUNT(*) FROM config_tbl WHERE class = ? AND name = ? AND hostname != ?", configClass, configName, hostname) > 0
				if itemExists:
					update("INSERT INTO config_tbl (class, name, hostname, value, creation_date, change_date, description) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", configClass, configName, hostname, configValue)
				else:
					itemExists = selectIntValue("SELECT COUNT(*) FROM config_tbl WHERE class = ? AND name = ?", configClass, configName) > 0
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
		itemExists = selectIntValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'jobqueue' AND name = 'execute' AND (hostname IS NULL OR TRIM(hostname) = '')") > 0
		if itemExists:
			update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = 'jobqueue' AND name = 'execute' AND (hostname IS NULL OR TRIM(hostname) = '')", 1 if status else 0)
		else:
			update("INSERT INTO config_tbl (class, name, hostname, value, creation_date, change_date, description) VALUES ('jobqueue', 'execute', NULL, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", 1 if status else 0)
	else:
		itemExists = selectIntValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'jobqueue' AND name = 'execute' AND hostname = ?", hostName) > 0
		if itemExists:
			update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = 'jobqueue' AND name= 'execute' AND hostname = ?", 1 if status else 0, hostName)
		else:
			update("INSERT INTO config_tbl (class, name, hostname, value, creation_date, change_date, description) VALUES ('jobqueue', 'execute', ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", hostName, 1 if status else 0)

def removeJobQueueHostFromDB(hostName):
	if hostName == "*" or hostName is None:
		update("DELETE FROM config_tbl WHERE class = 'jobqueue' AND name = 'execute' AND (hostname IS NULL OR TRIM(hostname) = '')")
	else:
		update("DELETE FROM config_tbl WHERE class = 'jobqueue' AND name = 'execute' AND hostname = ?", hostName)

def insertBirtKeysInDb(applicationUserName):
	itemExists = selectIntValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'birt' AND name = 'privatekey' AND value = '[to be defined]'") > 0
	if itemExists and os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/keys/birt_private.pem"):
		with open(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/keys/birt_private.pem", "r", encoding="UTF-8") as keyFileHandle:
			privateKeyFileData = keyFileHandle.read()
			privateKeyFileData = privateKeyFileData.replace("-----BEGIN RSA PRIVATE KEY-----", "")
			privateKeyFileData = privateKeyFileData.replace("-----END RSA PRIVATE KEY-----", "")
			privateKeyFileData = privateKeyFileData.replace("\r", "").replace("\n", "")

		update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = 'birt' AND name = 'privatekey'", privateKeyFileData)

	itemExists = selectIntValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'birt' AND name = 'publickey' AND value = '[to be defined]'") > 0
	if itemExists and os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/keys/birt_public.pem"):
		with open(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/keys/birt_public.pem", "r", encoding="UTF-8") as keyFileHandle:
			publicKeyFileData = keyFileHandle.read()
			publicKeyFileData = publicKeyFileData.replace("-----BEGIN PUBLIC KEY-----", "")
			publicKeyFileData = publicKeyFileData.replace("-----END PUBLIC KEY-----", "")
			publicKeyFileData = publicKeyFileData.replace("\r", "").replace("\n", "")

		update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = 'birt' AND name = 'publickey'", publicKeyFileData)

def checkConfigurationInDB():
	if checkTableExists("config_tbl"):
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
			itemExists = selectIntValue("SELECT COUNT(*) FROM config_tbl WHERE class = ? AND name = ?", mandatoryConfigEntry[0], mandatoryConfigEntry[1]) > 0
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
	try:
		if domain.lower() == "default":
			if landingPage.lower() == "remove":
				update("DELETE FROM config_tbl WHERE class = ? AND name = ?", "system", "RdirLandingpage")
			else:
				itemCount: int = selectIntValue("SELECT COUNT(*) FROM config_tbl WHERE class = ? AND name = ?",
												"system", "RdirLandingpage")
				if itemCount > 0:
					update("UPDATE config_tbl SET change_date = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', value = ? WHERE class = ? AND name = ?", landingPage, "system", "RdirLandingpage")
				else:
					update("INSERT INTO config_tbl (class, name, value, creation_date, change_date, description) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')","system", "RdirLandingpage", landingPage)
		else:
			if landingPage.lower() == "remove":
				update("DELETE FROM landingpage_tbl WHERE domain = ?", domain)
			else:
				itemCount: int = selectIntValue("SELECT COUNT(*) FROM landingpage_tbl WHERE domain = ?", domain)
				if itemCount > 0:
					update("UPDATE landingpage_tbl SET landingpage = ? WHERE domain = ?", landingPage, domain)
				else:
					update("INSERT INTO landingpage_tbl (domain, landingpage) VALUES (?, ?)", domain, landingPage)
	except:
		if ESTUtilities.isDebugMode():
			logger.exception("Executing SQL to update Landing Page failed")
		return None

def setupSecureOracleDbLibrariesForStartup(username):
	additionalPropertiesData = ""
	additionalPropertiesFilePath = ESTUtilities.getUserHomeDirectory(username) + "/tomcat/bin/emm.sh.additional.properties"
	if os.path.isfile(additionalPropertiesFilePath):
		with open(additionalPropertiesFilePath, "r", encoding="UTF-8") as propertiesFileHandle:
			additionalPropertiesData = propertiesFileHandle.read()
		if not "# OracleDB secure connection setup: start" in additionalPropertiesData:
			additionalPropertiesData = additionalPropertiesData\
				+ "# OracleDB secure connection setup: start\n"\
				+ "rm -f ~/tomcat/lib/ojdbc8*.jar\n"\
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
	additionalPropertiesFilePath = ESTUtilities.getUserHomeDirectory(username) + "/tomcat/bin/emm.sh.additional.properties"
	ESTUtilities.removeContentFromFile(additionalPropertiesFilePath, "# OracleDB secure connection setup: start", "# OracleDB secure connection setup: end")

def checkDbRootCredentials(host, dbRootPassword):
	if dbRootPassword is not None and dbRootPassword != "":
		passwordParameterPart = " -p'" + dbRootPassword + "'"
	else:
		passwordParameterPart = ""

	sqlUpdateReturnCode = os.system(getDbClientPath() + " -u root -h " + host + passwordParameterPart + " --default-character-set=utf8 -e \"SELECT VERSION()\" ")
	return sqlUpdateReturnCode == 0

def getCompaniesWithToDelete():
	result = select("SELECT company_id, shortname FROM company_tbl WHERE status='todelete'")
	return result

def createOrUpdateCompanyInfoTableEntry(companyId: int, cname: str, cvalue: str):
	valueExists = selectValue("SELECT cname, cvalue FROM company_info_tbl WHERE company_id = ? AND cname = ?",companyId, cname)
	if not valueExists:
		update("INSERT INTO company_info_tbl(company_id, cname, cvalue) VALUES(?,?,?)",companyId, cname, cvalue)
	else:
		update("UPDATE company_info_tbl SET cvalue=? WHERE company_id=? AND cname=?", cvalue, companyId, cname)

def deleteCompanyInfoTableEntry(companyId: int, cname: str):
		update("DELETE FROM company_info_tbl WHERE company_id=? AND cname=?", companyId, cname)

