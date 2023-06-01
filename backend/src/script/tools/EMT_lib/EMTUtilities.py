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
import re
import os
import ssl
import datetime
import smtplib
import subprocess
import logging
import urllib.request, urllib.error, urllib.parse

from email.mime.application import MIMEApplication
from email.mime.multipart import MIMEMultipart
from email.mime.text import MIMEText
from email.utils import COMMASPACE, formatdate

debugMode = False

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
	os.system("clear")

def downloadVersionInfo(versionInfoUrl):
	global debugMode

	versionInfo = {}
	if versionInfoUrl is not None:
		if debugMode:
			print("VersionInfoUrl: " + versionInfoUrl)

		downloadDestinationFilePath = "/tmp/version.txt"
		if os.path.isfile(downloadDestinationFilePath):
			os.remove(downloadDestinationFilePath)

		downloadPageResponse = None
		try:
			downloadPageResponse = openUrlConnection(versionInfoUrl)
			with open(downloadDestinationFilePath, "wb") as downloadDestinationFile:
				chunk_size = 8192
				while True:
					chunk = downloadPageResponse.read(chunk_size)

					if not chunk:
						break

					downloadDestinationFile.write(chunk)
		except:
			errorText = "Download of version info failed"
			if debugMode:
				logging.exception(errorText + "\nUrl: " + str(versionInfoUrl))
				raise
			else:
				raise Exception(errorText)
		finally:
			if downloadPageResponse is not None:
				downloadPageResponse.close()

		if os.path.isfile(downloadDestinationFilePath):
			with open(downloadDestinationFilePath, "r", encoding="UTF-8") as versionFileHandle:
				for line in versionFileHandle:
					line = line.strip()
					if line and not line.startswith("#"):
						indexOfNameSeparator = line.index(":")
						name = line[0:indexOfNameSeparator].strip()
						if " " in line[indexOfNameSeparator + 1:].strip():
							versionNumber = line[indexOfNameSeparator + 1:].strip().split(" ")[0]
							downloadLink = line[indexOfNameSeparator + 1:].strip().split(" ")[1]
						else:
							versionNumber = line[indexOfNameSeparator + 1:].strip()
							downloadLink = None
						versionInfo[name] = [versionNumber, downloadLink]
		if os.path.isfile(downloadDestinationFilePath):
			os.remove(downloadDestinationFilePath)
	return versionInfo

def openUrlConnection(url, timeout=5):
	return urllib.request.urlopen(url, timeout = timeout, context = ssl.SSLContext(ssl.PROTOCOL_TLS))

def getDomainFromUrl(url):
	return urllib.parse.urlparse(url).netloc

def isBlank(text):
	if text and len(text.strip()) > 0:
		return False
	else:
		return True

def isNotBlank(text):
	return not isBlank(text)

def readTextFile(filePath):
	with open(filePath, "r", encoding="UTF-8") as fileHandle:
		return fileHandle.read()

def printError(message):
	sys.stderr.write(message)

def sslIsAvailable():
	try:
		import ssl
		return True
	except:
		return False

def createLink(referencedPath, linkPath, userName = None, groupName = None):
	os.symlink(referencedPath, linkPath)
	if groupName is not None or (userName is not None and userName != os.environ["USER"]):
		if groupName is not None:
			os.system("chown -h " + userName + ":" + groupName + " " + linkPath)
		else:
			os.system("chown -h " + userName + " " + linkPath)

def createDirectory(directoryPath, userName = None, groupName = None):
	os.mkdir(directoryPath)
	if groupName is not None or (userName is not None and userName != os.environ["USER"]):
		if groupName is not None:
			os.system("chown " + userName + ":" + groupName + " " + directoryPath)
		else:
			os.system("chown " + userName + " " + directoryPath)

def createDirectories(directoryPath, userName = None, groupName = None):
	os.makedirs(directoryPath)
	if groupName is not None or (userName is not None and userName != os.environ["USER"]):
		if groupName is not None:
			os.system("chown -R " + userName + ":" + groupName + " " + directoryPath)
		else:
			os.system("chown -R " + userName + " " + directoryPath)

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

def checkJavaAvailable(javaHome):
	if javaHome is None or javaHome == "" or not os.path.isfile(javaHome + "/bin/java"):
		return False

	try:
		subProcess = subprocess.Popen(javaHome + "/bin/java -version 2>&1 | awk -F '\"' '/version/ {print $2}'", shell=True, stdout=subprocess.PIPE)
		processOutput, processError = subProcess.communicate()
		return processError is None and processOutput is not None and processOutput.decode("UTF-8").strip() != ""
	except:
		return False

def getJavaVersion(javaHome):
	if javaHome is None or javaHome == "" or not os.path.isfile(javaHome + "/bin/java"):
		return None

	try:
		subProcess = subprocess.Popen(javaHome + "/bin/java -version 2>&1 | awk -F '\"' '/version/ {print $2}'", shell=True, stdout=subprocess.PIPE)
		processOutput, processError = subProcess.communicate()
		if processOutput is not None:
			return processOutput.decode("UTF-8").strip()
		else:
			return None
	except:
		return None

def getJavaVendor(javaHome):
	if javaHome is None or javaHome == "" or not os.path.isfile(javaHome + "/bin/java"):
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

def getTomcatVersion(javaHome, catalinaHome):
	if catalinaHome is None:
		return None
	elif not os.path.isdir(catalinaHome):
		return None
	elif not os.path.isfile(catalinaHome + "/lib/catalina.jar"):
		return None
	elif not checkJavaAvailable(javaHome):
		raise Exception("Java path is invalid for check of Tomcat version: " + str(javaHome))
	else:
		try:
			process = subprocess.Popen([javaHome + "/bin/java", "-cp", catalinaHome + "/lib/catalina.jar", "org.apache.catalina.util.ServerInfo"], stdout=subprocess.PIPE)
			processOutput, processError = process.communicate()
			if processOutput is not None:
				processOutput = processOutput.decode("UTF-8").strip()

			if processError is None and processOutput is not None:
				versionLineStartIndex = processOutput.index("Server number:")
				version = processOutput[versionLineStartIndex + 14:processOutput.index("\n", versionLineStartIndex + 14)].strip()
				return version
			else:
				return None
		except:
			if isDebugMode():
				logging.exception("Cannot read TomcatVersion")
			return None

def getTomcatNativeVersion(tomcatNativePath):
	if tomcatNativePath is None:
		return None
	elif not os.path.isdir(tomcatNativePath):
		return None
	elif not os.path.isfile(tomcatNativePath + "/include/tcn_version.h"):
		if os.path.isfile(tomcatNativePath + "/lib/libtcnative-1.so"):
			nativeLibFileName = os.readlink(tomcatNativePath + "/lib/libtcnative-1.so")
			nativeLibVersion = nativeLibFileName.replace("libtcnative-1.so.0", "1")
			return nativeLibVersion
		else:
			return None
	else:
		try:
			majorVersion = None
			minorVersion = 0
			patchVersion = 0
			devVersion = 0

			with open(tomcatNativePath + "/include/tcn_version.h", "r", encoding="UTF-8") as versionFileHandle:
				for line in versionFileHandle:
					if line and line.startswith("#define TCN_MAJOR_VERSION"):
						majorVersion = line.strip()[25:].strip()
					elif line and line.startswith("#define TCN_MINOR_VERSION"):
						minorVersion = line.strip()[25:].strip()
					elif line and line.startswith("#define TCN_PATCH_VERSION"):
						patchVersion = line.strip()[25:].strip()
					elif line and line.startswith("#define TCN_IS_DEV_VERSION"):
						devVersion = line.strip()[26:].strip()

			if majorVersion is None:
				return None
			else:
				return majorVersion + "." + minorVersion + "." + patchVersion
		except:
			if isDebugMode():
				logging.exception("Cannot read TomcatNativeVersion")
			return None

# Read the text value of a xml node
def getXmlNodeText(xmlNode):
	text = []
	for node in xmlNode.childNodes:
		if node.nodeType == node.TEXT_NODE:
			text.append(node.data)
	return "".join(text)

# Read a properties file (no comments read)
def readPropertiesFile(propertiesFilePath):
	properties = {}
	if os.path.isfile(propertiesFilePath):
		with open(propertiesFilePath, "r", encoding="UTF-8") as propertiesFileHandle:
			for line in propertiesFileHandle:
				line = line.strip()
				if line and not line.startswith("#") and "=" in line:
					name, value = line.split("=", 1)
					if value == "":
						properties[name.strip()] = None
					else:
						properties[name.strip()] = value.strip()
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
		foundKey = False
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
	if zipFilePath is None or zipFilePath.strip() == "":
		raise ValueError("Invalid empty parameter zipFilePath")
	elif directoryPathToExtractTo is None or directoryPathToExtractTo.strip() == "":
		raise ValueError("Invalid empty parameter directoryPathToExtractTo")
	elif directoryPathToExtractTo.endswith("/"):
		directoryPathToExtractTo = directoryPathToExtractTo[:-1]
	bashCommand = "unzip \"" + zipFilePath + "\" -d \"" + directoryPathToExtractTo + "/\" 1>/dev/null"
	os.system(bashCommand)

def unTarGzFile(tarGzFilePath, filePathToExtractTo, userName = None, groupName = None):
	if filePathToExtractTo.endswith("/"):
		filePathToExtractTo = filePathToExtractTo[:-1]

	if groupName is None and (userName is None or userName == os.environ["USER"]):
		bashCommand = "cd " + filePathToExtractTo + " && tar xfz \"" + tarGzFilePath + "\" -C \"" + filePathToExtractTo + "/\""
		os.system(bashCommand)
	else:
		now = datetime.datetime.now()
		nowString = now.strftime("%Y-%m-%d_%H-%M-%S")
		tempdir = "/tmp/EmmExtract_" + nowString
		createDirectory(tempdir)

		bashCommand = "cd " + tempdir + " && tar xfz \"" + tarGzFilePath + "\" -C \"" + tempdir + "/\""
		os.system(bashCommand)

		if groupName is not None:
			os.system("chown -R " + userName + ":" + groupName + " " + tempdir)
		else:
			os.system("chown -R " + userName + " " + tempdir)

		os.system("cp -rp " + tempdir + "/* " + filePathToExtractTo + "/")
		os.system("rm -r " + tempdir)

def unTarGzFileAsRoot(tarGzFilePath, filePathToExtractTo):
	if filePathToExtractTo.endswith("/"):
		filePathToExtractTo = filePathToExtractTo[:-1]

	bashCommand = "cd " + filePathToExtractTo + " && sudo tar xapf \"" + tarGzFilePath + "\" -C \"" + filePathToExtractTo + "/\""
	os.system(bashCommand)

def getSendmailVersion():
	try:
		try:
			sendmailPath = subprocess.check_output("which sendmail 2>/dev/null", shell=True).decode("UTF-8").strip()
		except:
			sendmailPath = None
		if sendmailPath is None or sendmailPath == "":
			return None
		else:
			try:
				rpmPath = subprocess.check_output("which rpm 2>/dev/null", shell=True).decode("UTF-8").strip()
			except:
				rpmPath = None

			if rpmPath is None or rpmPath == "":
				logging.exception("Cannot detect SendmailVersion: rpm is missing")
				return None
			else:
				allSendmailVersions = subprocess.check_output("rpm -qa | grep -i sendmail 2>/dev/null", shell=True).decode("UTF-8")
				versionMatch = re.search(r"sendmail-(\d+\.\d+\.\d+)-.*", allSendmailVersions)
				if versionMatch:
					return versionMatch.group(1)
				else:
					return None
	except:
		if isDebugMode():
			logging.exception("Cannot detect SendmailVersion")
		return None

def getPostfixVersion():
	try:
		try:
			postfixPath = subprocess.check_output("which postconf 2>/dev/null", shell=True).decode("UTF-8").strip()
		except:
			postfixPath = None
		if postfixPath is None or postfixPath == "":
			return None
		else:
			postfixVersion = subprocess.check_output("postconf -d mail_version 2>/dev/null", shell=True).decode("UTF-8")
			versionMatch = re.search(r".*(\d+\.\d+\.\d+).*", postfixVersion)
			if versionMatch:
				return versionMatch.group(1)
			else:
				return None
	except:
		if isDebugMode():
			logging.exception("Cannot detect PostfixVersion")
		return None

def hasRootPermissions():
	try:
		return os.getuid() == 0
	except:
		return False

def getVersionFromFilename(fileName):
	versionRegExpResult = re.findall("[0-9]+[.][0-9]+[.][0-9]+(?:[.][0-9]+)?", fileName)
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
	if version1 is None or len(version1.strip()) == 0 or version1 == "Unknown":
		if version2 is None or len(version2.strip()) == 0 or version2 == "Unknown":
			return 0
		else:
			return 1
	elif version2 is None or len(version2.strip()) == 0 or version2 == "Unknown":
		return -1
	else:
		version1Parts = version1.split(".")
		version2Parts = version2.split(".")
		for i in range(max(len(version1Parts), len(version2Parts))):
			if len(version1Parts) > i and len(version2Parts) > i:
				if int(version1Parts[i]) < int(version2Parts[i]):
					return 1
				elif int(version1Parts[i]) > int(version2Parts[i]):
					return -1
			elif len(version1Parts) > i:
				if int(version1Parts[i]) != 0:
					return -1
			elif len(version2Parts) > i:
				if int(version2Parts[i]) != 0:
					return 1
		return 0

def normalizeVersion(versionText, removeLeadingZeros = False):
	if versionText is None:
		return None
	elif len(versionText.strip()) == 0:
		return ""
	elif removeLeadingZeros:
		versionParts = versionText.split(".")
		newVersion = str(int(versionParts[0]))
		for i in range(1, len(versionParts)):
			newVersion = newVersion + "." + str(int(versionParts[i]))
		while newVersion.endswith(".0"):
			newVersion = newVersion[0:len(newVersion) - 2]
		return newVersion
	else:
		newVersion = versionText
		while True:
			lastPointIndex = newVersion.rfind(".")
			if lastPointIndex > 0:
				if int(newVersion[lastPointIndex + 1:]) == 0:
					newVersion = newVersion[0:lastPointIndex]
				else:
					break
			else:
				break
		return newVersion

def isWritable(path):
	if os.path.isfile(path):
		with open(path, "a") as filehandle:
			return filehandle.writable()
	elif os.path.isdir(path):
		return os.access(path, os.W_OK)
	else:
		return False

def checkDownloadFileIsAvailable(fileUrl):
	downloadPageResponse = None
	try:
		downloadPageResponse = openUrlConnection(fileUrl)
		return True
	except urllib.error.HTTPError as e:
		if isDebugMode():
			logging.exception("Error")
		if e.code == 404:
			# File not found
			return False
		else:
			return False
	except:
		if isDebugMode():
			logging.exception("Error")
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
		startIndex = fileData.index(startSign)
		while startIndex > -1:
			startSignFound = True
			endIndex = -1
			if endSign is not None:
				endIndex = fileData.index(endSign, startIndex)
			if endIndex > -1:
				fileData = fileData[0:startIndex] + fileData[endIndex + len(endSign)]
			else:
				fileData = fileData[0:startIndex]
			startIndex = fileData.index(startSign)
		if startSignFound:
			with open(filePath, "w", encoding="UTF-8") as fileHandle:
				fileHandle.write(fileData)
		return startSignFound
	else:
		raise Exception("No such file: " + filePath)
