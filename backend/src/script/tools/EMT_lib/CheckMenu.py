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
import pwd
import re
import time
import ssl
import cgi
import datetime
import getpass
import shutil
import logging
import subprocess
import urllib.request, urllib.error, urllib.parse

from EMT_lib.Environment import Environment
from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities

from EMT_lib import SupplementalMenu

from hashlib import sha256

def executeCheckMenuAction(actionParameters):
	executeCheck()
	print()
	print("Press any key to continue")
	choice = input(" > ")
	return

def executeCheck():
	print()
	print("Executing application check")
	print()
	foundError = False

	if Environment.isEmmFrontendServer:
		foundError = checkApplicationFileIntegrityWithOutput("Frontend", "/home/console/webapps/emm") or foundError
	if Environment.isEmmStatisticsServer:
		foundError = checkApplicationFileIntegrityWithOutput("Statistics", "/home/console/webapps/statistics") or foundError
	if Environment.isEmmRdirServer:
		foundError = checkApplicationFileIntegrityWithOutput("Rdir", "/home/rdir/webapps/rdir") or foundError
	if Environment.isEmmWebservicesServer:
		foundError = checkApplicationFileIntegrityWithOutput("Webservices", "/home/console/webapps/webservices") or foundError
	if Environment.isEmmConsoleRdirServer:
		foundError = checkApplicationFileIntegrityWithOutput("Console-Rdir", "/home/console/webapps/rdir") or foundError
	if Environment.isOpenEmmServer:
		foundError = checkApplicationFileIntegrityWithOutput("Frontend", "/home/openemm/webapps/emm") or foundError
		foundError = checkApplicationFileIntegrityWithOutput("Statistics", "/home/openemm/webapps/statistics") or foundError
		foundError = checkApplicationFileIntegrityWithOutput("Webservices", "/home/openemm/webapps/webservices") or foundError

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
		if checkApplicationFileIntegrity("/home/console/webapps/emm"):
			reportText += "Frontend application integrity: OK\n"
		else:
			reportText += errorColorCode + "Frontend application integrity: ERROR" + defaultColorCode + "\n"
	if Environment.isEmmStatisticsServer:
		if checkApplicationFileIntegrity("/home/console/webapps/statistics"):
			reportText += "Statistics application integrity: OK\n"
		else:
			reportText += errorColorCode + "Statistics application integrity: ERROR" + defaultColorCode + "\n"
	if Environment.isEmmRdirServer:
		if checkApplicationFileIntegrity("/home/console/webapps/rdir"):
			reportText += "Rdir application integrity: OK\n"
		else:
			reportText += errorColorCode + "Rdir application integrity: ERROR" + defaultColorCode + "\n"
	if Environment.isEmmWebservicesServer:
		if checkApplicationFileIntegrity("/home/console/webapps/webservices"):
			reportText += "Webservices application integrity: OK\n"
		else:
			reportText += errorColorCode + "Webservices application integrity: ERROR" + defaultColorCode + "\n"
	if Environment.isEmmConsoleRdirServer:
		if checkApplicationFileIntegrity("/home/console/webapps/rdir"):
			reportText += "Console-Rdir application integrity: OK\n"
		else:
			reportText += errorColorCode + "Console-Rdir application integrity: ERROR" + defaultColorCode + "\n"
	if Environment.isOpenEmmServer:
		if checkApplicationFileIntegrity("/home/openemm/webapps/emm"):
			reportText += "Frontend application integrity: OK\n"
		else:
			reportText += errorColorCode + "Frontend application integrity: ERROR" + defaultColorCode + "\n"
		if checkApplicationFileIntegrity("/home/openemm/webapps/statistics"):
			reportText += "Statistics application integrity: OK\n"
		else:
			reportText += errorColorCode + "Statistics application integrity: ERROR" + defaultColorCode + "\n"
		if checkApplicationFileIntegrity("/home/openemm/webapps/webservices"):
			reportText += "Webservices application integrity: OK\n"
		else:
			reportText += errorColorCode + "Webservices application integrity: ERROR" + defaultColorCode + "\n"
	return reportText

def checkApplicationFileIntegrity(applicationDirectoryPath):
	if os.path.isfile(applicationDirectoryPath + "/checksums.sha256"):
		invalidFiles = ssh256FileIntegrityCheck(applicationDirectoryPath)
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
		invalidFiles = ssh256FileIntegrityCheck(applicationDirectoryPath)
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
		sha256CheckSumsFilePath = checkDirectoryPath + "/checksums.sha256"

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
