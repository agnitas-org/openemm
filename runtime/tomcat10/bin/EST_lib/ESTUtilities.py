import bisect
import inspect
import itertools
import shutil
import sys
import re
import os
import pwd
import ssl
import smtplib
import subprocess
import logging
import glob
import tarfile
import threading
import time
import tempfile
import urllib.request, urllib.error, urllib.parse

from contextlib import suppress
from email.message import EmailMessage
from email.mime.application import MIMEApplication
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.utils import COMMASPACE, formatdate
from pathlib import Path
from typing import Callable

try:
	from packaging.version import Version
except ImportError:
	from EST_lib.FallBackLibrary import Version

from pathlib import Path
from tarfile import TarFile, TarInfo
import zipfile

from . import Colors

debugMode = False

logger = logging.getLogger(__name__)

def setDebugMode(value):
	global debugMode

	debugMode = value
	return debugMode

def isDebugMode():
	global debugMode

	return debugMode

def clearTerminalScreen():
	global debugMode

	if not debugMode:
		# Reset Terminal, so that back scrolling does not work
		print("\033c")
		#xxx
		os.system("clear")

def userExists(username: str):
	return username in getListOfUsers()

def anyUserExists(usernames: list):
	return any(map(lambda user: user in getListOfUsers(), usernames))

def getListOfUsers():
	users = []
	for p in pwd.getpwall():
		users.append(p[0])
	return users

def getUserHomeDirectory(username):
	return os.path.expanduser("~" + username)

def flushStdInAndGetInput(promptText):
	time.sleep(0.25)
	try:
		import sys, termios
		termios.tcflush(sys.stdin, termios.TCIOFLUSH)
	except ImportError:
		import msvcrt
		while msvcrt.kbhit():
			msvcrt.getch()
	inputString = input(promptText)
	return inputString

def downloadVersionInfo(versionInfoUrl) -> dict:
	global debugMode

	versionInfo = {}
	if versionInfoUrl is not None:
		if debugMode:
			print("VersionInfoUrl: " + versionInfoUrl)

		with tempfile.TemporaryFile() as temporaryFileHandle:
			downloadPageResponse = None
			try:
				downloadPageResponse = openUrlConnection(versionInfoUrl)
				chunk_size = 8192
				while True:
					chunk = downloadPageResponse.read(chunk_size)

					if not chunk:
						break

					temporaryFileHandle.write(chunk)
			except:
				errorText = "Download of version info failed"
				if debugMode:
					logger.exception(errorText + "\nUrl: " + str(versionInfoUrl))
					raise
				else:
					raise Exception(errorText)
			finally:
				if downloadPageResponse is not None:
					downloadPageResponse.close()

				temporaryFileHandle.seek(0)
				for line in temporaryFileHandle.read().decode("UTF-8").splitlines():
					line = line.strip()
					if line and not line.startswith("#") and ":" in line:
						indexOfNameSeparator = line.index(":")
						name = line[0:indexOfNameSeparator].strip()
						if " " in line[indexOfNameSeparator + 1:].strip():
							versionNumber = line[indexOfNameSeparator + 1:].strip().split(" ")[0]
							downloadLink = line[indexOfNameSeparator + 1:].strip().split(" ")[1]
						else:
							versionNumber = line[indexOfNameSeparator + 1:].strip()
							downloadLink = None
						versionInfo[name] = [versionNumber, downloadLink]
	return versionInfo

def downloadTextFile(textFileUrl):
	if textFileUrl is not None:
		if debugMode:
			print("TextFileUrl for download: " + textFileUrl)

		downloadPageResponse = None
		try:
			textContent = ""
			downloadPageResponse = openUrlConnection(textFileUrl)
			chunk_size = 8192
			while True:
				chunk = downloadPageResponse.read(chunk_size)
				if not chunk:
					break
				else:
					textContent = textContent + chunk.decode("utf8", errors='replace')
			return textContent
		except:
			errorText = "Download of textfile failed"
			if debugMode:
				logger.exception(errorText + "\nUrl: " + str(textFileUrl))
				raise
			else:
				raise Exception(errorText)
		finally:
			if downloadPageResponse is not None:
				downloadPageResponse.close()
	return None

def openUrlConnection(url, timeout=5):
	return urllib.request.urlopen(url, timeout = timeout, context = ssl.SSLContext(ssl.PROTOCOL_TLS))

def getDomainFromUrl(url):
	return urllib.parse.urlparse(url).netloc

def isBlank(text):
	if text is not None and len(text.strip()) > 0:
		return False
	else:
		return True

def isNotBlank(text):
	return not isBlank(text)

def readTextFile(filePath):
	with open(filePath, "r", encoding="UTF-8") as fileHandle:
		return fileHandle.read()

def printStdError(message):
	sys.stderr.write(message)

def sslIsAvailable():
	try:
		import ssl
		ssl # silent pyflakes
		return True
	except ModuleNotFoundError:
		return False

def chown(filePath, userName = None, groupName = None):
	if groupName is not None or (userName is not None and userName != os.environ["USER"]):
		shutil.chown(filePath, user=userName, group=groupName)

def chownRecursive(filePath: str | Path, userName: str = None, groupName: str = None, keepPreviousGroup: bool = False):
	if groupName is not None or (userName is not None and userName != os.environ["USER"]):
		for item in Path(filePath).rglob('*'):
			shutil.chown(item, user=userName, group=item.stat().st_gid if keepPreviousGroup else groupName)

def createLink(referencedPath, linkPath, userName = None, groupName = None):
	os.symlink(referencedPath, linkPath)
	if groupName is not None or (userName is not None and userName != os.environ["USER"]):
		if groupName is not None:
			os.system("chown -h " + userName + ":" + groupName + " " + linkPath)
		else:
			os.system("chown -h " + userName + " " + linkPath)

def createDirectory(directoryPath: str | Path, userName: str = None, groupName: str = None):
	directoryPath = Path(directoryPath)
	directoryPath.mkdir()
	chown(directoryPath, userName, groupName)

def createDirectories(directoryPath, userName = None, groupName = None):
	if directoryPath is not None and isNotBlank(directoryPath):
		parentDirectory = os.path.dirname(directoryPath)
		if not os.path.isdir(parentDirectory) and not os.path.islink(parentDirectory):
			createDirectories(parentDirectory, userName, groupName)
		if not os.path.isdir(directoryPath) and not os.path.islink(directoryPath):
			createDirectory(directoryPath, userName, groupName)

def printAlignedTable(headers, rows):
	# data must be cached for oracle, which allows iterating on rows only once
	dataRows = []
	for row in rows:
		dataRows.append(row)

	columnSizes = []
	for header in headers:
		columnSizes.append(len(header))
	for row in dataRows:
		for i in range(0, min(len(row), len(columnSizes))):
			columnSizes[i] = max(columnSizes[i], len(str(row[i])))
	headerLine = ""
	for i in range(0, len(columnSizes)):
		spacing = (" " * (columnSizes[i] - len(str(headers[i])) + 1))
		headerLine = headerLine + headers[i] + spacing
	print(headerLine)
	for row in dataRows:
		dataLine = ""
		for i in range(0, len(columnSizes)):
			spacing = (" " * (columnSizes[i] - len(str(row[i])) + 1))
			dataLine = dataLine + str(row[i]) + spacing
		print(dataLine)

def printTextInBox(text, boxChar="*"):
	print(boxChar * (len(text) + 4))
	print(boxChar + " " + text + " " + boxChar)
	print(boxChar * (len(text) + 4))

def checkNodeAvailable():
	return bool(shutil.which("node"))

def checkVersionInvalid(version: str | None):
	return not version or not version.strip() or version in ["Unknown", "Unknown version"]

def checkJavaAvailable(javaHome):
	if isBlank(javaHome) or not os.path.isfile(javaHome + "/bin/java"):
		return False

	try:
		subProcess = subprocess.Popen(javaHome + "/bin/java -version 2>&1 | awk -F '\"' '/version/ {print $2}'", shell=True, stdout=subprocess.PIPE)
		processOutput, processError = subProcess.communicate()
		return processError is None and processOutput is not None and processOutput.decode("UTF-8").strip() != ""
	except:
		return False

def getJavaVersion(javaHome):
	if not javaHome or not Path(javaHome, "bin/java").is_file():
		return None
	try:
		output = subprocess.run([Path(javaHome,"bin/java"), "-version"], capture_output=True)
		#subProcess = subprocess.Popen(javaHome + "/bin/java -version 2>&1", shell=True, stdout=subprocess.PIPE)

		version_pattern = re.compile('([0-9.]+\\+[0-9]+)')
		if output and (version := version_pattern.search(output.stderr.decode("UTF-8"))):
			return version.group(1).replace("+", ".")
		else:
			return None
	except:
		return None

def getJavaVendor(javaHome):
	if isBlank(javaHome) or not os.path.isfile(javaHome + "/bin/java"):
		return None

	try:
		subProcess = subprocess.Popen(javaHome + "/bin/java -version 2>&1 | awk -F '\"' '/version/ {print $2}'", shell=True, stdout=subprocess.PIPE)
		processOutput, processError = subProcess.communicate()
		if processOutput is not None:
			if "openjdk" in processOutput.decode("UTF-8").strip():
				return "OpenJDK"
			else:
				return "Oracle"
		else:
			return None
	except:
		return None

def checkTomcatAvailable(javaHome, catalinaHome):
	return catalinaHome is not None and catalinaHome != "" and os.path.isfile(catalinaHome + "/lib/catalina.jar")

def getTomcatVersion(catalinaHome: str):
	if Path(catalinaHome).is_dir():
		catalinaHome += "/lib/catalina.jar"
	with zipfile.ZipFile(catalinaHome) as jar:
		content = jar.read("org/apache/catalina/util/ServerInfo.properties")
		return readProperties(content.decode("UTF-8")).get("server.number")

# Read the text value of a xml node
def getXmlNodeText(xmlNode):
	text = []
	for node in xmlNode.childNodes:
		if node.nodeType == node.TEXT_NODE:
			text.append(node.data)
	return "".join(text)

def readProperties(propertiesString):
	properties = {}
	for line in propertiesString.splitlines():
		line = line.strip()
		if line and not line.startswith("#") and "=" in line:
			name, value = line.split("=", 1)
			if value == "":
				properties[name.strip()] = None
			else:
				properties[name.strip()] = value.strip()
	return properties
# Read a properties file (no comments read)
def readPropertiesFile(propertiesFilePath):
	properties = {}
	if os.path.isfile(propertiesFilePath):
		with open(propertiesFilePath, "r", encoding="UTF-8") as propertiesFileHandle:
			properties = readProperties(propertiesFileHandle.read())
	return properties

# Write the special dbcfg file format
# Any comments within the existing file should be preserved
def updatePropertiesFile(filePath, changedProperties, makePropertiesBashUseable = False):
	if os.path.isfile(filePath):
		with open(filePath, "r", encoding="UTF-8") as propertiesFileHandle:
			propertiesData = propertiesFileHandle.read()
	else:
		propertiesData = ""

	for key in changedProperties:
		propertiesNewData = ""
		foundKey = False
		for line in propertiesData.splitlines():
			if line and not line.startswith("#") and "=" in line:
				currentKey, value = line.split("=", 1)
				currentKey = currentKey.strip()
				value = value.strip()
				if currentKey == key:
					foundKey = True
					line = key + ("=" if makePropertiesBashUseable else " = ") + ("" if changedProperties[key] is None else str(changedProperties[key]))
			propertiesNewData = propertiesNewData + line + "\n"
		if not foundKey:
			propertiesNewData = propertiesNewData + key + ("=" if makePropertiesBashUseable else " = ") + ("" if changedProperties[key] is None else str(changedProperties[key])) + "\n"
		propertiesData = propertiesNewData

	with open(filePath, "w", encoding="UTF-8") as propertiesFileHandle:
		propertiesFileHandle.write(propertiesData)

def removeEntryFromPropertiesFile(filePath, removeKey, makePropertiesBashUseable = False):
	if os.path.isfile(filePath):
		with open(filePath, "r", encoding="UTF-8") as propertiesFileHandle:
			propertiesData = propertiesFileHandle.read()

		propertiesNewData = ""
		for line in propertiesData.splitlines():
			if line and not line.startswith("#") and "=" in line:
				currentKey, value = line.split("=", 1)
				currentKey = currentKey.strip()
				value = value.strip()
				if currentKey != removeKey:
					propertiesNewData = propertiesNewData + line + "\n"
			else:
				propertiesNewData = propertiesNewData + line + "\n"
		propertiesData = propertiesNewData

		with open(filePath, "w", encoding="UTF-8") as propertiesFileHandle:
			propertiesFileHandle.write(propertiesData)

# Read an environment properties file (like setenv.sh, no comments read, contains export commands)
def readEnvironmentPropertiesFile(propertiesFilePath):
	properties = {}
	if os.path.isfile(propertiesFilePath):
		with open(propertiesFilePath, "r", encoding="UTF-8") as propertiesFileHandle:
			for line in propertiesFileHandle:
				line = line.strip()
				if line and not line.startswith("#") and line.lower().startswith("export ") and "=" in line:
					line = line[7:].strip()
					name, value = line.split("=", 1)
					name = name.strip()
					value = value.strip()
					if value.startswith("\"") and value.endswith("\""):
						value = value[1:-1]
					if value == "":
						value = None
					properties[name] = value
	return properties

# Write an environment properties file (like setenv.sh, contains export commands)
def updateEnvironmentPropertiesFile(filePath, username, properties):
	propertiesData = ""

	for key in properties:
		if properties[key] is None:
			propertiesData = propertiesData + "export " + key + "=\n"
		else:
			propertiesData = propertiesData + "export " + key + "=\"" + str(properties[key]) + "\"\n"

	propertiesData = propertiesData + "source $HOME/scripts/config.sh\n"

	with open(filePath, "w", encoding="UTF-8") as propertiesFileHandle:
		propertiesFileHandle.write(propertiesData)

	if username is not None and username != os.environ["USER"]:
		os.system("chown openemm " + filePath)
	os.system("chmod u+x " + filePath)

def encodeCSVLine(*values):
	returnString = ""
	csvSeparator = ";"
	csvTextDelimiter = "\""
	csvLineBreak = "\n"
	for value in values:
		valueString = str(value)
		if csvSeparator in valueString or csvTextDelimiter in valueString or csvLineBreak in valueString:
			valueString = csvTextDelimiter + valueString.replace(csvTextDelimiter, csvTextDelimiter + csvTextDelimiter) + csvTextDelimiter
		if len(returnString) > 0:
			returnString = returnString + csvSeparator
		returnString = returnString + valueString
	returnString = returnString + csvLineBreak
	return returnString

def walkSorted(directory):
	try:
		names = os.listdir(directory)
	except:
		return
	names.sort()
	links = []
	dirs = []
	files = []

	for name in names:
		if os.path.islink(os.path.join(directory, name)):
			links.append(name)
		elif os.path.isdir(os.path.join(directory, name)):
			dirs.append(name)
		else:
			files.append(name)

	yield directory, links, dirs, files
	for dirName in dirs:
		for subDirectoryYield in walkSorted(os.path.join(directory, dirName)):
			yield subDirectoryYield

def sendEmail(send_from, send_to, subject, text, files=None, server="127.0.0.1"):
	assert isinstance(send_to, list)

	msg = MIMEMultipart()
	msg["From"] = send_from
	msg["To"] = COMMASPACE.join(send_to)
	msg["Date"] = formatdate(localtime=True)
	msg["Subject"] = subject

	msg.attach(MIMEText(text))

	for f in files or []:
		with open(f, "rb") as fil:
			part = MIMEApplication(
				fil.read(),
				Name = os.path.basename(f)
			)
		part["Content-Disposition"] = 'attachment; filename="%s"' % os.path.basename(f)
		msg.attach(part)

	smtp = smtplib.SMTP(server)
	smtp.sendmail(send_from, send_to, msg.as_string())
	smtp.close()

def zipFile(zipFilePath, filePathToAddToZipFile, password=""):
	if password is not None and len(password) > 0:
		bashCommand = "zip -P \"" + password + "\" \"" + zipFilePath + "\" \"" + filePathToAddToZipFile + "\" 1>/dev/null"
	else:
		bashCommand = "zip \"" + zipFilePath + "\" \"" + filePathToAddToZipFile + "\" 1>/dev/null"
	os.system(bashCommand)

def unzipFile(zipFilePath, directoryPathToExtractTo):
	if isBlank(zipFilePath):
		raise ValueError("Invalid empty parameter zipFilePath")
	elif isBlank(directoryPathToExtractTo):
		raise ValueError("Invalid empty parameter directoryPathToExtractTo")
	elif directoryPathToExtractTo.endswith("/"):
		directoryPathToExtractTo = directoryPathToExtractTo[:-1]
	bashCommand = "unzip \"" + zipFilePath + "\" -d \"" + directoryPathToExtractTo + "/\" 1>/dev/null"
	os.system(bashCommand)

def unTarGzFile(tarGzFilePath: str | Path, filePathToExtractTo: str | Path, userName: str = None, groupName: str = None) -> Path:
	def extract_filter(member: tarfile.TarInfo, destination: str):
		if member.issym() and member.linkpath == '/opt/agnitas.com/software/tomcat/webapps/manager':
			return member
		return tarfile.data_filter(member, destination)
	filePathToExtractTo = Path(filePathToExtractTo)
	extractedItemNames = []
	toplevelDirectory: str

	with tarfile.open(tarGzFilePath, 'r:gz') as tar:
		tar: TarFile
		for member in tar:
			targetPathMember = filePathToExtractTo / member.name
			if targetPathMember.is_file():
				targetPathMember.unlink()
			tar.extract(member,filePathToExtractTo, filter=extract_filter)
			extractedItemNames.append(filePathToExtractTo / member.name)
		toplevelDirectory = os.path.commonpath(tar.getnames())
		if groupName or (userName or userName is not os.environ["USER"]):
			for extractedItem in extractedItemNames:
				chown(extractedItem, userName, groupName)
	if toplevelDirectory:
		return filePathToExtractTo / toplevelDirectory
	else:
		return filePathToExtractTo

def unTarGzFileAsRoot(tarGzFilePath, filePathToExtractTo):
	tarGzFilePath = Path(tarGzFilePath).expanduser().resolve().absolute()
	filePathToExtractTo = Path(filePathToExtractTo).expanduser().resolve().absolute()

	bashCommand = f"cd {filePathToExtractTo} && sudo tar xapf \"{tarGzFilePath}\" -C \"{filePathToExtractTo}/\""
	subprocess.run(bashCommand)


def spinner(message: str, stopEvent: threading.Thread, printCharacter=".", characterCount=4):
	spinner = itertools.cycle([(printCharacter * i).ljust(characterCount) for i in range(1, characterCount + 1)])
	while not stopEvent.is_set():
		sys.stdout.write(f"\r{message} {next(spinner)}\r")
		sys.stdout.flush()
		time.sleep(0.4)
		sys.stdout.write("\b")
		sys.stdout.flush()


def unTarGzFileWithSpinner(tarGzFilePath: str | Path, filePathToExtractTo: str | Path, userName: str = None,
                           groupName: str | None = None) -> Path | None:
	message = "Extracting {tarGzFilePath}"
	stopEvent = threading.Event()

	spinnerThread = threading.Thread(
		target=spinner,
		args=(message, stopEvent),
		daemon=True
	)
	spinnerThread.start()

	try:
		extractedDirectory = unTarGzFile(tarGzFilePath, filePathToExtractTo, userName, groupName)
	finally:
		stopEvent.set()
		spinnerThread.join(timeout=1)
	return extractedDirectory

def getPostfixVersion():
	try:
		try:
			postfixConfPath = subprocess.check_output("which postconf 2>/dev/null", shell=True).decode("UTF-8").strip()
		except:
			postfixConfPath = None
		if isBlank(postfixConfPath):
			return None
		else:
			postfixVersion = subprocess.check_output(postfixConfPath + " -d mail_version 2>/dev/null", shell=True).decode("UTF-8")
			versionMatch = re.search(r".*(\d+\.\d+\.\d+).*", postfixVersion)
			if versionMatch:
				return versionMatch.group(1)
			else:
				return None
	except:
		if isDebugMode():
			logger.exception("Cannot detect PostfixVersion")
		return None

def hasRootPermissions():
	try:
		return os.getuid() == 0
	except:
		return False

def getVersionFromFilename(fileName: str | Path):
	versionRegExpResult = re.findall("[0-9]+[.][0-9]+[.][0-9]+(?:[.][0-9]+)?", str(fileName))
	if len(versionRegExpResult) > 0:
		return versionRegExpResult[0]
	else:
		return None

def getFirstVersionFromText(text):
	versionRegExpResult = re.findall("[0-9]+[.][0-9]+(?:[.][0-9]+)*", text)
	if len(versionRegExpResult) > 0:
		return versionRegExpResult[0]
	else:
		return None

def compareVersions(version1, version2):
	if checkVersionInvalid(version1):
		if checkVersionInvalid(version2):
			return 0
		else:
			return 1
	elif checkVersionInvalid(version2):
		return -1
	else:
		version1 = Version(version1)
		version2 = Version(version2)
		if version1 < version2:
			return 1
		elif version1 > version2:
			return -1
		else:
			return 0

def normalizeVersion(versionText: str, removeLeadingZeros = False):
	if versionText and versionText.strip():
		if removeLeadingZeros:
			return str(Version(versionText.rstrip("0.")))
		else:
			return versionText.rstrip("0.")
	else:
		return ""

def isWritable(path):
	if os.path.isfile(path):
		with open(path, "a") as filehandle:
			return filehandle.writable()
	elif os.path.isdir(path):
		return os.access(path, os.W_OK)
	else:
		return False

def writePathIfWritable(path: str | Path) -> bool:
	try:
		Path(path).mkdir(parents=True, exist_ok=True)
		with tempfile.NamedTemporaryFile(dir=path) as tempFile: #Python EAFP principle
			pass
		return True
	except (PermissionError, OSError, FileNotFoundError):
		return False

def checkDownloadFileIsAvailable(fileUrl):
	downloadPageResponse = None
	try:
		downloadPageResponse = openUrlConnection(fileUrl)
		return True
	except urllib.error.HTTPError as e:
		if isDebugMode():
			logger.exception("Error")
		if e.code == 404:
			# File not found
			return False
		else:
			return False
	except:
		if isDebugMode():
			logger.exception("Error")
		return False
	finally:
		if downloadPageResponse is not None:
			downloadPageResponse.close()

def removeContentFromFile(filePath, startSign, endSign=None):
	if os.path.isfile(filePath):
		if startSign is None or len(startSign) == 0:
			raise Exception("Empty startSign")
		fileData = None
		startSignFound = False
		with open(filePath, "r", encoding="UTF-8") as fileHandle:
			fileData = fileHandle.read()
		while startSign in fileData:
			startIndex = fileData.index(startSign)
			startSignFound = True
			endIndex = -1
			if endSign is not None:
				endIndex = fileData.index(endSign, startIndex)
			if endIndex > -1:
				fileData = fileData[0:startIndex] + fileData[endIndex + len(endSign)]
			else:
				fileData = fileData[0:startIndex]
		if startSignFound:
			with open(filePath, "w", encoding="UTF-8") as fileHandle:
				fileHandle.write(fileData)
		return startSignFound
	else:
		raise Exception("No such file: " + filePath)

def getSingleFileByPattern(filePathPattern):
	filesList = glob.glob(filePathPattern)
	if filesList is not None and len(filesList) == 1:
		return filesList[0]
	else:
		return None

def manageTlsCertificateForTomcat(defaultTlsCertificateDirectory, filePathServerXml, applicationName, activateTls=None, useHttpPort=None):
	with open(filePathServerXml, "r") as file:
		serverXmlContent = file.read()

	if activateTls is None and "TLS configuration start" in serverXmlContent and "TLS configuration end" in serverXmlContent:
		print("Do you want to activate a TLS certificate (https) for " + applicationName + "? (N/y, Blank => Cancel):")
		answer = input(" > ").lower().strip()
		if answer.startswith("y") or answer.startswith("j"):
			activateTls = True
		else:
			activateTls = False
	if activateTls:
		defaultCertificateFilePath = getSingleFileByPattern(defaultTlsCertificateDirectory + "/*.crt")
		while True:
			if defaultCertificateFilePath is None:
				print("Please enter path to TLS certificate file (.crt) (Blank => Cancel):")
			else:
				print("Please enter path to TLS certificate file (.crt) (Blank => '" + defaultCertificateFilePath + "', 'cancel' => Cancel):")
			certificateFilePath = input(" > ").strip()
			if isBlank(certificateFilePath):
				if defaultCertificateFilePath is not None:
					certificateFilePath = defaultCertificateFilePath
				else:
					return
			elif "cancel" == certificateFilePath.lower():
				return
			if not os.path.isfile(certificateFilePath):
				print("File does not exist: " + certificateFilePath)
				print()
			else:
				break

		defaultKeyFilePath = getSingleFileByPattern(defaultTlsCertificateDirectory + "/*.key")
		while True:
			if defaultKeyFilePath is None:
				print("Please enter path to TLS key file (.key) (Blank => Cancel):")
			else:
				print("Please enter path to TLS key file (.key) (Blank => '" + defaultKeyFilePath + "', 'cancel' => Cancel):")
			keyFilePath = input(" > ").strip()
			if isBlank(keyFilePath):
				if defaultKeyFilePath is not None:
					keyFilePath = defaultKeyFilePath
				else:
					return
			elif "cancel" == keyFilePath.lower():
				return
			if not os.path.isfile(keyFilePath):
				print("File does not exist: " + keyFilePath)
				print()
			else:
				break

		defaultChainFilePath = getSingleFileByPattern(defaultTlsCertificateDirectory + "/*.ca-bundle")
		while True:
			if defaultChainFilePath is None:
				print("Please enter path to TLS certificate CA chain file (.ca-bundle) (Blank => Cancel):")
			else:
				print("Please enter path to TLS certificate CA chain file (.ca-bundle) (Blank => '" + defaultChainFilePath + "', 'cancel' => Cancel):")
			chainFilePath = input(" > ").strip()
			if isBlank(chainFilePath):
				if defaultChainFilePath is not None:
					chainFilePath = defaultChainFilePath
				else:
					return
			elif "cancel" == chainFilePath.lower():
				return
			if not os.path.isfile(chainFilePath):
				print("File does not exist: " + chainFilePath)
				print()
			else:
				break

		serverXmlContent = re.sub(r".*<!-- TLS configuration start.*", "\t\t<!-- TLS configuration start -->", serverXmlContent)
		serverXmlContent = re.sub(r".*TLS configuration end -->.*", "\t\t<!-- TLS configuration end -->", serverXmlContent)

		serverXmlContent = re.sub(r"certificateChainFile=\".*\"", 'certificateChainFile="' + chainFilePath + '"', serverXmlContent)
		serverXmlContent = re.sub(r"certificateFile=\".*\"", 'certificateFile="' + certificateFilePath + '"', serverXmlContent)
		serverXmlContent = re.sub(r"certificateKeyFile=\".*\"", 'certificateKeyFile="' + keyFilePath + '"', serverXmlContent)

		if "port=\"8443\"" in serverXmlContent:
			print("Do you want to use other port for the https protocol than 8443 ? (N/y, Blank => Cancel):")
			answer = input(" > ").lower().strip()
			if answer.startswith("y") or answer.startswith("j"):
				print("Enter https port? (Blank => Cancel):")
				answer = input(" > ").lower().strip()
				serverXmlContent = re.sub(r".port=\"8443\"", "port=\"" + answer + "\"", serverXmlContent)
	else:
		serverXmlContent = re.sub(r".*<!-- TLS configuration start.*", "\t\t<!-- TLS configuration start", serverXmlContent)
		serverXmlContent = re.sub(r".*TLS configuration end -->.*", "\t\tTLS configuration end -->", serverXmlContent)

	if "port=\"8080\"" in serverXmlContent:
		if useHttpPort is None:
			print("Do you want to use other port for the http protocol than 8080 ? (N/y, Blank => Cancel):")
			answer = input(" > ").lower().strip()
			if answer.startswith("y") or answer.startswith("j"):
				print("Enter http port? (Blank => Cancel):")
				answer = input(" > ").lower().strip()
				serverXmlContent = re.sub(r".port=\"8080\"", "port=\"" + answer + "\"", serverXmlContent)
		else:
			serverXmlContent = re.sub(r".port=\"8080\"", "port=\"" + str(useHttpPort) + "\"", serverXmlContent)

	with open(filePathServerXml, "w", encoding="UTF-8") as file:
		file.write(serverXmlContent)
		if activateTls:
			print("Successfully configured TLS certificate")
		else:
			print("Using no TLS certificate")
		print()

def getMainLtsVersion(versionString):
	if checkVersionInvalid(versionString):
		return None
	else:
		version = Version(versionString)
		oldMinorVersions = [4, 10]
		newMinorVersions = [5, 11]
		minorVersions = newMinorVersions if version.major > 24 else oldMinorVersions
		if not version.minor in minorVersions:
			index = bisect.bisect_left(minorVersions, version.minor)
			if index >= len(minorVersions):
				return f"{version.major + 1}.{minorVersions[0]:02}"
			else:
				return f"{version.major}.{minorVersions[index]:02}"
		elif len(version.release) >= 2:
			return f"{version.major}.{version.minor:02}"
		else:
			return None

def getMajorVersion(versionString):
	if checkVersionInvalid(versionString):
		return 0
	else:
		return Version(versionString).major

def getMinorVersion(versionString):
	if checkVersionInvalid(versionString):
		return 0
	else:
		return Version(versionString).minor

def getMicroVersion(versionString: str):
	if checkVersionInvalid(versionString):
		return 0
	else:
		return Version(versionString).micro

def printProgressBar(temsTodo, itemsDone, progressbarSize):
	percentageDone = itemsDone / float(temsTodo)
	sys.stdout.write("\033[F") # Cursor up one line
	sys.stdout.write("\033[F") # Cursor up one line
	doneCharsSize = int((progressbarSize - 2) * percentageDone)
	todoCharsSize = (progressbarSize - 2) - doneCharsSize
	print("[" + ("*" * doneCharsSize) + " " * todoCharsSize + "]")
	progressString = "{:,}".format(itemsDone) + " of " + "{:,}".format(temsTodo) + " Bytes (" + "{0:.0f}".format(percentageDone * 100) + "%)"
	print(progressString + " " * (progressbarSize - len(progressString)))

def getParentPath(childPath):
	return os.path.abspath(os.path.join(childPath, os.pardir))

def checkPackageIsInstalled(osVendor, packageName):
	if osVendor == "Suse":
		try:
			resultCode = os.system("zypper search -i " + packageName + " > /dev/null 2>&1")
			if resultCode == 0:
				return True
			else:
				return False
		except:
			if debugMode:
				logger.exception("Error while checking for installed package: " + str(packageName))
			return False
	else:
		try:
			resultCode = os.system("rpm -q " + packageName + " > /dev/null")
			if resultCode == 0:
				return True
			else:
				return False
		except:
			if debugMode:
				logger.exception("Error while checking for installed package: " + str(packageName))
			return False

def restart(scriptFilePath, *arguments):
	arguments = (re.sub ('/V[0-9]+(\\.[0-9]+)*/', '/current/', _a) for _a in arguments)
	if isDebugMode():
		print ('{yellow}Restarting ... ({executable} {arguments}){default}'.format (
			yellow = Colors.YELLOW,
			executable = sys.executable,
			arguments = ' '.join (arguments),
			default = Colors.DEFAULT
		))
	else:
		print(Colors.YELLOW + "Restarting ..." + Colors.DEFAULT)
	os.execl(sys.executable, sys.executable, *arguments)

def checkWriteAccessibility(path):
	if not os.access(path, os.W_OK):
		if isDebugMode():
			logger.exception("PermissionError: No write permission for '{path}'.")
		return False

	if os.path.isdir(path):
		for entry in os.listdir(path):
			entry_path = os.path.join(path, entry)
			if not os.access(entry_path, os.W_OK):
				return False
	return True

def checkPathAccessibility(path_str: str):
	path = Path(path_str)
	try:
		if not path.exists():
			return 0
		if path.is_dir():
			next(path.iterdir())
		else:
			path.stat()
		return 1
	except PermissionError:
		return -1
	except Exception as e:
		return -1

def str_to_int(string,default: int | None=None):
	try:
		return int(string)
	except ValueError:
		return default

def getNodeVersion():
	if shutil.which("node"):
		nodeCall = subprocess.run("node --version", shell=True, capture_output=True)
		if nodeCall.stdout:
			return nodeCall.stdout.decode().strip("\n")
	return None

def parse_header (content: str) -> dict[str, str]:
	if content:
		with suppress (ValueError):
			msg = EmailMessage ()
			msg['content-type'] = content
			return dict (msg['content-type'].params)
	return {}

def filter_argument_list(func: Callable, data: dict) -> dict:
	sig = inspect.signature(func)
	return {k:data[k] for k in sig.parameters.keys() if k in data}

def printWarning(*args):
	print(Colors.YELLOW, *args, Colors.DEFAULT, sep=None)

def printError(*args):
	print(Colors.RED, *args, Colors.DEFAULT, sep=None)

def printSuccess(*args):
	print(Colors.GREEN, *args, Colors.DEFAULT, sep=None)


def askNy(message: str, customDefaultName="No"):
	print(message, f"(N/y, Blank => {customDefaultName}):")
	choice = input(" > ").strip().lower()
	return choice in ["n","no","nein"] or choice == ""

def askYn(message: str, customDefaultName="No"):
	print(message, f"(Y/n, Blank => {customDefaultName}):")
	choice = input(" > ").strip().lower()
	return choice in ["y","yes","j","ja"] or choice == ""

def askNyCancel(message: str):
	return askNy(message, "Cancel")

def spinner(message: str, stopEvent: threading.Thread, printCharacter=".", characterCount=4):
	spinnerChar = itertools.cycle([(printCharacter * i).ljust(characterCount) for i in range(1, characterCount + 1)])
	while not stopEvent.is_set():
		sys.stdout.write(f"\r{message} {next(spinnerChar)}\r")
		sys.stdout.flush()
		time.sleep(0.4)
		sys.stdout.write("\b")

def executeCallableWithSpinner(message: str, callable: Callable,  printCharacter=".", characterCount=4) :
	stopEvent = threading.Event()

	spinnerThread = threading.Thread(
		target=spinner,
		args=(message, stopEvent, printCharacter, characterCount),
		daemon=True
	)
	spinnerThread.start()

	try:
		callableReturnValue = callable()
	finally:
		stopEvent.set()
		spinnerThread.join(timeout=1)
	return callableReturnValue