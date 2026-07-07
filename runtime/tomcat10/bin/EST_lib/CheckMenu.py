import os

from EST_lib.Environment import Environment
from EST_lib import Colors
from EST_lib import ESTUtilities
from hashlib import sha256

def executeCheckMenuAction(actionParameters):
	executeCheck()
	print()
	print("Press any key to continue")
	input(" > ")
	return

def executeCheck():
	print()
	print("Executing application check")
	print()
	foundError = False

	if Environment.isEmmFrontendServer:
		foundError = checkApplicationFileIntegrityWithOutput("Frontend", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm") or foundError
	if Environment.isEmmStatisticsServer:
		foundError = checkApplicationFileIntegrityWithOutput("Statistics", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/statistics") or foundError
	if Environment.isEmmRdirServer:
		foundError = checkApplicationFileIntegrityWithOutput("Rdir", ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/rdir") or foundError
	if Environment.isEmmWebservicesServer:
		foundError = checkApplicationFileIntegrityWithOutput("Webservices", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/webservices") or foundError
	if Environment.isEmmConsoleRdirServer:
		foundError = checkApplicationFileIntegrityWithOutput("Console-Rdir", ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/rdir") or foundError
	if Environment.isOpenEmmServer:
		foundError = checkApplicationFileIntegrityWithOutput("Frontend", ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm") or foundError
		foundError = checkApplicationFileIntegrityWithOutput("Statistics", ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/statistics") or foundError
		foundError = checkApplicationFileIntegrityWithOutput("Webservices", ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/webservices") or foundError

	if foundError:
		print(Colors.RED + "Errors found" + Colors.DEFAULT)
	else:
		print("No errors found")

	return foundError

def createCheckReport(colorize = True):
	if colorize:
		errorColorCode = Colors.RED
		defaultColorCode = Colors.DEFAULT
	else:
		errorColorCode = ""
		defaultColorCode = ""

	reportText = ""

	if Environment.isEmmFrontendServer:
		if checkApplicationFileIntegrity(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm"):
			reportText += "File integrity check confirms content of Frontend application files\n"
		else:
			reportText += errorColorCode + "Checksum is missing or wrong for Frontend" + defaultColorCode + "\n"
	if Environment.isEmmStatisticsServer:
		if checkApplicationFileIntegrity(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/statistics"):
			reportText += "File integrity check confirms content of Statistics application files\n"
		else:
			reportText += errorColorCode + "Checksum is missing or wrong for Statistics" + defaultColorCode + "\n"
	if Environment.isEmmRdirServer:
		if checkApplicationFileIntegrity(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/rdir"):
			reportText += "File integrity check confirms content of Rdir application files\n"
		else:
			reportText += errorColorCode + "Checksum is missing or wrong for Rdir" + defaultColorCode + "\n"
	if Environment.isEmmWebservicesServer:
		if checkApplicationFileIntegrity(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/webservices"):
			reportText += "File integrity check confirms content of Webservices application files\n"
		else:
			reportText += errorColorCode + "Checksum is missing or wrong for Webservices" + defaultColorCode + "\n"
	if Environment.isEmmConsoleRdirServer:
		if checkApplicationFileIntegrity(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/rdir"):
			reportText += "File integrity check confirms content of Console-Rdir application files\n"
		else:
			reportText += errorColorCode + "Checksum is missing or wrong for Console-Rdir" + defaultColorCode + "\n"
	if Environment.isOpenEmmServer:
		if checkApplicationFileIntegrity(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm"):
			reportText += "File integrity check confirms content of Frontend application files\n"
		else:
			reportText += errorColorCode + "Checksum is missing or wrong for Frontend" + defaultColorCode + "\n"
		if checkApplicationFileIntegrity(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/statistics"):
			reportText += "File integrity check confirms content of Statistics application files\n"
		else:
			reportText += errorColorCode + "Checksum is missing or wrong for Statistics" + defaultColorCode + "\n"
		if checkApplicationFileIntegrity(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/webservices"):
			reportText += "File integrity check confirms content of Webservices application files\n"
		else:
			reportText += errorColorCode + "Checksum is missing or wrong for Webservices" + defaultColorCode + "\n"
	return reportText

def checkApplicationFileIntegrity(applicationDirectoryPath):
	if os.path.isfile(applicationDirectoryPath + "/checksums.sha256"):
		sha256CheckSumsFilePath = applicationDirectoryPath + "/checksums.sha256"
	else:
		sha256CheckSumsFilePath = applicationDirectoryPath + "/WEB-INF/checksums.sha256"

	if os.path.isfile(sha256CheckSumsFilePath):
		invalidFiles = ssh256FileIntegrityCheck(applicationDirectoryPath, sha256CheckSumsFilePath)
		if invalidFiles is None:
			return True
		else:
			return False
	else:
		return False

# Returns "True" if an error was found
def checkApplicationFileIntegrityWithOutput(applicationName, applicationDirectoryPath):
	print("Checking " + applicationName + " file integrity")
	if os.path.isfile(applicationDirectoryPath + "/checksums.sha256"):
		sha256CheckSumsFilePath = applicationDirectoryPath + "/checksums.sha256"
	else:
		sha256CheckSumsFilePath = applicationDirectoryPath + "/WEB-INF/checksums.sha256"

	if os.path.isfile(sha256CheckSumsFilePath):
		invalidFiles = ssh256FileIntegrityCheck(applicationDirectoryPath, sha256CheckSumsFilePath)
		if invalidFiles is None:
			print("File integrity check: OK")
			print()
			return False
		else:
			showMaxFilePaths = 10
			print(Colors.RED + "File integrity check: FAIL" + Colors.DEFAULT)
			for invalidFileIndex in range(min(showMaxFilePaths, len(invalidFiles))):
				print(Colors.RED + invalidFiles[invalidFileIndex] + Colors.DEFAULT)
			if showMaxFilePaths < len(invalidFiles) :
				print(Colors.RED + "< Overall number of invalid files: " + str(len(invalidFiles)) + " >" + Colors.DEFAULT)
			print()
			return True
	else:
		print(Colors.YELLOW + "No checksum file available" + Colors.DEFAULT)
		print()
		return False

def ssh256FileIntegrityCheck(checkDirectoryPath, sha256CheckSumsFilePath = None):
	if sha256CheckSumsFilePath is None and checkDirectoryPath is not None:
		if os.path.isfile(checkDirectoryPath + "/checksums.sha256"):
			sha256CheckSumsFilePath = checkDirectoryPath + "/checksums.sha256"
		elif os.path.isfile(checkDirectoryPath + "/WEB-INF/checksums.sha256"):
			sha256CheckSumsFilePath = checkDirectoryPath + "/WEB-INF/checksums.sha256"

	if checkDirectoryPath is None or not os.path.isdir(checkDirectoryPath):
		raise Exception("Directory for file integrity check is missing or invalid: " + str(checkDirectoryPath)) 
	elif sha256CheckSumsFilePath is None or not os.path.isfile(sha256CheckSumsFilePath):
		raise Exception("Checksums file for file integrity check is missing or invalid: " + str(sha256CheckSumsFilePath)) 
	else:
		errorFilesList = []
		sha256sums = dict()
		with open(sha256CheckSumsFilePath) as sha256CheckSumsFileHandle:
			for line in sha256CheckSumsFileHandle:
				ssh256Checksum = line[0:64]
				filePath = line[66:].strip()
				sha256sums[filePath] = ssh256Checksum

		for sha256File, sha256Checksum in sha256sums.items():
			checksumFilePath = sha256File
			sha256sum = sha256()
			if checksumFilePath.startswith("./"):
				checksumFilePath = checkDirectoryPath + "/" + checksumFilePath[2:]
			with open(checksumFilePath, 'rb') as checksumFileHandle:
				data_chunk = checksumFileHandle.read(1024)
				while data_chunk:
					sha256sum.update(data_chunk)
					data_chunk = checksumFileHandle.read(1024)

			checksum = sha256sum.hexdigest()
			if checksum != sha256Checksum:
				errorFilesList.append(checksumFilePath)

		if len(errorFilesList) == 0:
			return None
		else:
			return errorFilesList
