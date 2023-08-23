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
import tempfile
import urllib.request, urllib.error, urllib.parse

from EMT_lib.Environment import Environment
from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities
from EMT_lib import License

from EMT_lib import SupplementalMenu

def installFile(packageFilePath, logfile, interactive):
	logfile.write("Installing \"" + packageFilePath + "\"\n")

	packageFilename = os.path.basename(packageFilePath)

	if "-runtime-" in packageFilename:
		applicationName = "runtime"
		releaseSubdirectoryName = "runtime"
	elif "backend-console-" in packageFilename:
		applicationName = "backend-console"
		releaseSubdirectoryName = "backend"
	elif "backend-rdir-" in packageFilename:
		applicationName = "backend-rdir"
		releaseSubdirectoryName = "backend"
	elif "-startup-" in packageFilename:
		applicationName = "backend-startup"
		releaseSubdirectoryName = ""
	elif "-merger-" in packageFilename:
		applicationName = "backend-merger"
		releaseSubdirectoryName = ""
	elif "-mailer-" in packageFilename:
		applicationName = "backend-mailer"
		releaseSubdirectoryName = ""
	elif "-mailloop-" in packageFilename:
		applicationName = "backend-mailloop"
		releaseSubdirectoryName = ""
	elif "-backend-" in packageFilename:
		applicationName = "backend"
		releaseSubdirectoryName = "backend"
	elif "-manual-" in packageFilename:
		applicationName = "manual"
		releaseSubdirectoryName = "manual"
	elif "-emm-" in packageFilename or "-gui-" in packageFilename:
		applicationName = "gui"
		releaseSubdirectoryName = "emm"
	elif "-statistics-" in packageFilename:
		applicationName = "statistics"
		releaseSubdirectoryName = "statistics"
	elif "-webservices-" in packageFilename:
		applicationName = "webservices"
		releaseSubdirectoryName = "webservices"
	elif "-rdir-" in packageFilename:
		applicationName = "rdir"
		releaseSubdirectoryName = "rdir"
	elif "-tomcat-native-" in packageFilename:
		applicationName = "tomcat-native"
		releaseSubdirectoryName = None

		# Check for Environment.tomcatNative base dir with link
		if Environment.tomcatNative is None:
			if not os.path.isdir(Environment.optDefaultPath):
				os.mkdir(Environment.optDefaultPath)
				if EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
					os.chown(Environment.optDefaultPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)
			installBaseDirectoryPath = Environment.optDefaultPath

			Environment.tomcatNative = installBaseDirectoryPath + "/tomcat-native"
		elif os.path.islink(Environment.tomcatNative) or not os.path.exists(Environment.tomcatNative):
			installBaseDirectoryPath = os.path.abspath(os.path.join(Environment.tomcatNative, os.pardir))
		else:
			errorText = "Cannot update Tomcat-Native in defined single directory path"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Check is writable or use root
		if not EMTUtilities.isWritable(installBaseDirectoryPath):
			errorText = "The Tomcat-Native installation directory '" + installBaseDirectoryPath + "' is not writable for the current user '" + Environment.username + "'. So only users with root permissions (sudo) may install or update Tomcat-Native here.\nPlease restart the tool with root permissions (sudo) for installation or update here."
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return
	elif "-tomcat-" in packageFilename:
		applicationName = "tomcat"
		releaseSubdirectoryName = None

		# Check for Environment.catalinaHome base dir with link
		if Environment.catalinaHome is None:
			if not os.path.isdir(Environment.optDefaultPath):
				os.mkdir(Environment.optDefaultPath)
				if EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
					os.chown(Environment.optDefaultPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)
			installBaseDirectoryPath = Environment.optDefaultPath
			Environment.catalinaHome = installBaseDirectoryPath + "/tomcat"
		elif os.path.islink(Environment.catalinaHome) or not os.path.exists(Environment.catalinaHome):
			installBaseDirectoryPath = os.path.abspath(os.path.join(Environment.catalinaHome, os.pardir))
		else:
			errorText = "Cannot update Tomcat in defined single directory path"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Check is writable or use root
		if not EMTUtilities.isWritable(installBaseDirectoryPath):
			errorText = "The Tomcat installation directory '" + installBaseDirectoryPath + "' is not writable for the current user '" + Environment.username + "'. So only users with root permissions (sudo) may install or update Tomcat here.\nPlease restart the tool with root permissions (sudo) for installation or update here."
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

	elif "-java-" in packageFilename:
		applicationName = "java"
		releaseSubdirectoryName = None

		# Check for Environment.javaHome base dir with link
		if Environment.javaHome is None:
			if not os.path.isdir(Environment.optDefaultPath):
				os.mkdir(Environment.optDefaultPath)
				if EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
					os.chown(Environment.optDefaultPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)
			installBaseDirectoryPath = Environment.optDefaultPath
			Environment.javaHome = installBaseDirectoryPath + "/java"
		elif os.path.islink(Environment.javaHome) or not os.path.exists(Environment.javaHome):
			installBaseDirectoryPath = os.path.abspath(os.path.join(Environment.javaHome, os.pardir))
		else:
			if not os.path.isdir(Environment.optDefaultPath):
				os.mkdir(Environment.optDefaultPath)
				if EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
					os.chown(Environment.optDefaultPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)
			installBaseDirectoryPath = Environment.optDefaultPath
			Environment.javaHome = installBaseDirectoryPath + "/java"

		# Check is writable or use root
		if not EMTUtilities.isWritable(installBaseDirectoryPath):
			errorText = "The JAVA installation directory '" + installBaseDirectoryPath + "' is not writable for the current user '" + Environment.username + "'. So only users with root permissions (sudo) may install or update Tomcat here.\nPlease restart the tool with root permissions (sudo) for installation or update here."
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return
	else:
		# Handle as complete package
		# Extract files in /tmp/
		now = datetime.datetime.now()
		nowString = now.strftime("%Y-%m-%d_%H-%M-%S")
		temporaryExtractionDirectory = "/tmp/Emm_" + nowString
		EMTUtilities.createDirectory(temporaryExtractionDirectory)
		EMTUtilities.unTarGzFile(packageFilePath, temporaryExtractionDirectory)

		# Apply installation order
		frontendFile = None
		statisticsFile = None
		webservicesFile = None
		consoleRdirFile = None
		rdirFile = None
		manualFile = None
		backendConsoleFile = None
		backendRdirFile = None
		backendStartupFile = None
		backendMergerFile = None
		backendMailerFile = None
		backendMailloopFile = None
		backendGlobalFile = None

		updateFilesInInstallOrder = []
		for listedFileName in os.listdir(temporaryExtractionDirectory):
			if frontendFile is None and ("-emm-" in listedFileName or "-gui-" in listedFileName):
				frontendFile = listedFileName
			elif statisticsFile is None and "-statistics-" in listedFileName:
				statisticsFile = listedFileName
			elif webservicesFile is None and "-webservices-" in listedFileName:
				webservicesFile = listedFileName
			elif rdirFile is None and "-rdir-" in listedFileName:
				rdirFile = listedFileName
			elif listedFileName.startswith("backend-console-"):
				backendConsoleFile = listedFileName
			elif listedFileName.startswith("backend-rdir-"):
				backendRdirFile = listedFileName
			elif "-startup-" in listedFileName:
				backendStartupFile = listedFileName
			elif "-merger-" in listedFileName:
				backendMergerFile = listedFileName
			elif "-mailer-" in listedFileName:
				backendMailerFile = listedFileName
			elif "-mailloop-" in listedFileName:
				backendMailloopFile = listedFileName
			elif backendGlobalFile is None and "-backend-" in listedFileName:
				backendGlobalFile = listedFileName
			else:
				updateFilesInInstallOrder.append(listedFileName)
		if frontendFile is not None:
			updateFilesInInstallOrder.append(frontendFile)
		if statisticsFile is not None:
			updateFilesInInstallOrder.append(statisticsFile)
		if webservicesFile is not None:
			updateFilesInInstallOrder.append(webservicesFile)
		if rdirFile is not None:
			updateFilesInInstallOrder.append(rdirFile)
		if backendConsoleFile is not None:
			updateFilesInInstallOrder.append(backendConsoleFile)
		if backendRdirFile is not None:
			updateFilesInInstallOrder.append(backendRdirFile)
		if backendStartupFile is not None:
			updateFilesInInstallOrder.append(backendStartupFile)
		if backendMergerFile is not None:
			updateFilesInInstallOrder.append(backendMergerFile)
		if backendMailerFile is not None:
			updateFilesInInstallOrder.append(backendMailerFile)
		if backendMailloopFile is not None:
			updateFilesInInstallOrder.append(backendMailloopFile)
		if backendGlobalFile is not None:
			updateFilesInInstallOrder.append(backendGlobalFile)

		# Install each application package
		for listedFile in updateFilesInInstallOrder:
			if Environment.errors is not None and len(Environment.errors) > 0:
				break
			else:
				installFile(temporaryExtractionDirectory + "/" + listedFile, logfile, interactive)
		shutil.rmtree(temporaryExtractionDirectory)
		return

	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	elif Environment.isEmmRdirServer and (applicationName == "rdir" or applicationName == "backend-startup"):
		applicationUserName = "rdir"
	elif Environment.isEmmMergerServer and (applicationName == "backend-merger" or applicationName == "backend-startup"):
		applicationUserName = "merger"
	elif Environment.isEmmMailerServer and (applicationName == "backend-mailer" or applicationName == "backend-startup"):
		applicationUserName = "mailout"
	elif Environment.isEmmMailloopServer and (applicationName == "backend-mailloop" or applicationName == "backend-startup"):
		applicationUserName = "mailloop"
	else:
		applicationUserName = "console"

	if releaseSubdirectoryName is not None:
		if not os.path.isdir("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName):
			if interactive and Environment.isOpenEmmServer:
				print()
				print("Package contains new application '" + getApplicationDisplayName(applicationName) + "'")
				print("Initially install this application? (N/y, Blank => Cancel):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if not (choice.startswith("y") or choice.startswith("j")):
					logfile.write("Declined new application type '" + applicationName + "'\n")
					return
			else:
				logfile.write("Skipped not needed application type '" + applicationName + "'\n")
				return

		if (applicationName == "backend-startup" or applicationName == "backend-merger" or applicationName == "backend-mailer" or applicationName == "backend-mailloop") \
			and not EMTUtilities.hasRootPermissions():
			errorText = "You need to start the program with root permissions (sudo) to update the backend application"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return
		elif applicationName == "backend" \
			and not EMTUtilities.hasRootPermissions() \
			and Environment.isOpenEmmServer:
			errorText = "You need to start the program with root permissions (sudo) to update the backend application"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		if Environment.isOpenEmmServer:
			if not os.path.isdir("/home/openemm/release"):
				EMTUtilities.createDirectory("/home/openemm/release", applicationUserName)
				logfile.write("Created directory: /home/openemm/release" + "\n")
			if not os.path.isdir("/home/openemm/release/" + releaseSubdirectoryName):
				EMTUtilities.createDirectory("/home/openemm/release/" + releaseSubdirectoryName, "openemm")
				logfile.write("Created directory: /home/openemm/release/" + releaseSubdirectoryName + "\n")
			expectedFilePath = "/home/openemm/release/" + releaseSubdirectoryName + "/" + packageFilename
			installBaseDirectoryPath = "/home/openemm/release/" + releaseSubdirectoryName
		else:
			if (Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer) and applicationName == "runtime":
				if not os.path.isdir("/home/console/release"):
					EMTUtilities.createDirectory("/home/console/release", "console")
					logfile.write("Created directory: /home/console/release" + "\n")
				if not os.path.isdir("/home/console/release/" + releaseSubdirectoryName):
					EMTUtilities.createDirectory("/home/console/release/" + releaseSubdirectoryName, "console")
					logfile.write("Created directory: /home/console/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = "/home/console/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = None
			elif Environment.isEmmRdirServer and applicationName == "runtime":
				if not os.path.isdir("/home/rdir/release"):
					EMTUtilities.createDirectory("/home/rdir/release", "rdir")
					logfile.write("Created directory: /home/rdir/release" + "\n")
				if not os.path.isdir("/home/rdir/release/" + releaseSubdirectoryName):
					EMTUtilities.createDirectory("/home/rdir/release/" + releaseSubdirectoryName, "rdir")
					logfile.write("Created directory: /home/rdir/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = "/home/rdir/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = None
			elif Environment.isEmmFrontendServer and applicationName == "gui":
				if not os.path.isdir("/home/console/release"):
					EMTUtilities.createDirectory("/home/console/release", "console")
					logfile.write("Created directory: /home/console/release" + "\n")
				if not os.path.isdir("/home/console/release/" + releaseSubdirectoryName):
					EMTUtilities.createDirectory("/home/console/release/" + releaseSubdirectoryName, "console")
					logfile.write("Created directory: /home/console/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = "/home/console/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = "/home/console/release/" + releaseSubdirectoryName
			elif Environment.isEmmFrontendServer and applicationName == "manual":
				if not os.path.isdir("/home/console/release"):
					EMTUtilities.createDirectory("/home/console/release", "console")
					logfile.write("Created directory: /home/console/release" + "\n")
				if not os.path.isdir("/home/console/release/" + releaseSubdirectoryName):
					EMTUtilities.createDirectory("/home/console/release/" + releaseSubdirectoryName, "console")
					logfile.write("Created directory: /home/console/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = "/home/console/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = "/home/console/release/" + releaseSubdirectoryName
			elif Environment.isEmmStatisticsServer and applicationName == "statistics":
				if not os.path.isdir("/home/console/release"):
					EMTUtilities.createDirectory("/home/console/release", "console")
					logfile.write("Created directory: /home/console/release" + "\n")
				if not os.path.isdir("/home/console/release/" + releaseSubdirectoryName):
					EMTUtilities.createDirectory("/home/console/release/" + releaseSubdirectoryName, "console")
					logfile.write("Created directory: /home/console/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = "/home/console/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = "/home/console/release/" + releaseSubdirectoryName
			elif Environment.isEmmWebservicesServer and applicationName == "webservices":
				if not os.path.isdir("/home/console/release"):
					EMTUtilities.createDirectory("/home/console/release", "console")
					logfile.write("Created directory: /home/console/release" + "\n")
				if not os.path.isdir("/home/console/release/" + releaseSubdirectoryName):
					EMTUtilities.createDirectory("/home/console/release/" + releaseSubdirectoryName, "console")
					logfile.write("Created directory: /home/console/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = "/home/console/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = "/home/console/release/" + releaseSubdirectoryName
			elif Environment.isEmmRdirServer and applicationName == "rdir":
				if not os.path.isdir("/home/rdir/release"):
					EMTUtilities.createDirectory("/home/rdir/release", "rdir")
					logfile.write("Created directory: /home/rdir/release" + "\n")
				if not os.path.isdir("/home/rdir/release/" + releaseSubdirectoryName):
					EMTUtilities.createDirectory("/home/rdir/release/" + releaseSubdirectoryName, "rdir")
					logfile.write("Created directory: /home/rdir/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = "/home/rdir/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = "/home/rdir/release/" + releaseSubdirectoryName
			elif Environment.isEmmConsoleRdirServer and applicationName == "rdir":
				if not os.path.isdir("/home/console/release"):
					EMTUtilities.createDirectory("/home/console/release", "console")
					logfile.write("Created directory: /home/console/release" + "\n")
				if not os.path.isdir("/home/console/release/" + releaseSubdirectoryName):
					EMTUtilities.createDirectory("/home/console/release/" + releaseSubdirectoryName, "console")
					logfile.write("Created directory: /home/console/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = "/home/console/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = "/home/console/release/" + releaseSubdirectoryName
			elif applicationName == "backend-startup":
				if not os.path.isdir("/root/release"):
					EMTUtilities.createDirectory("/root/release", "root")
					logfile.write("Created directory: /root/release" + "\n")
				expectedFilePath = "/root/release/" + packageFilename
				installBaseDirectoryPath = "/root/release"
			elif Environment.isEmmMergerServer and applicationName == "backend-merger":
				if not os.path.isdir("/home/merger/release"):
					EMTUtilities.createDirectory("/home/merger/release", "merger")
					logfile.write("Created directory: /home/merger/release" + "\n")
				expectedFilePath = "/home/merger/release/" + packageFilename
				installBaseDirectoryPath = "/home/merger/release"
			elif Environment.isEmmMailerServer and applicationName == "backend-mailer":
				if not os.path.isdir("/home/mailout/release"):
					EMTUtilities.createDirectory("/home/mailout/release", "mailout")
					logfile.write("Created directory: /home/mailout/release" + "\n")
				expectedFilePath = "/home/mailout/release/" + packageFilename
				installBaseDirectoryPath = "/home/mailout/release"
			elif Environment.isEmmMailloopServer and applicationName == "backend-mailloop":
				if not os.path.isdir("/home/mailloop/release"):
					EMTUtilities.createDirectory("/home/mailloop/release", "mailloop")
					logfile.write("Created directory: /home/mailloop/release" + "\n")
				expectedFilePath = "/home/mailloop/release/" + packageFilename
				installBaseDirectoryPath = "/home/mailloop/release"
			elif (Environment.isEmmFrontendServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer) and applicationName == "backend-console":
				if not os.path.isdir("/home/console/release"):
					EMTUtilities.createDirectory("/home/console/release", "console")
					logfile.write("Created directory: /home/console/release" + "\n")
				if not os.path.isdir("/home/console/release/backend"):
					EMTUtilities.createDirectory("/home/console/release/backend", "console")
					logfile.write("Created directory: /home/console/release/backend" + "\n")
				expectedFilePath = "/home/console/release/backend/" + packageFilename
				installBaseDirectoryPath = "/home/console/release/backend"
			elif Environment.isEmmRdirServer and applicationName == "backend-rdir":
				if not os.path.isdir("/home/rdir/release"):
					EMTUtilities.createDirectory("/home/rdir/release", "rdir")
					logfile.write("Created directory: /home/rdir/release" + "\n")
				if not os.path.isdir("/home/rdir/release/backend"):
					EMTUtilities.createDirectory("/home/rdir/release/backend", "rdir")
					logfile.write("Created directory: /home/rdir/release/backend" + "\n")
				expectedFilePath = "/home/rdir/release/backend/" + packageFilename
				installBaseDirectoryPath = "/home/rdir/release/backend"
			else:
				# Application is not needed for this server
				return
		updatedApplicationDirectoryPath = expectedFilePath.replace(".tar.gz", "")
	else:
		expectedFilePath = None
		if not os.path.isdir(Environment.optDefaultPath):
			os.mkdir(Environment.optDefaultPath)
			if EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
				os.chown(Environment.optDefaultPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)
		updatedApplicationDirectoryPath = Environment.optDefaultPath + "/" + packageFilename.replace(".tar.gz", "")

	if installBaseDirectoryPath is not None and not os.path.isdir(installBaseDirectoryPath):
		EMTUtilities.createDirectory(installBaseDirectoryPath, applicationUserName)

	# Clean up old update attempt left overs
	if os.path.isdir(updatedApplicationDirectoryPath):
		shutil.rmtree(updatedApplicationDirectoryPath)
		logfile.write("Removed already existing directory '" + updatedApplicationDirectoryPath + "'\n")

	# Check if file is already where it should be
	if expectedFilePath is not None and not packageFilePath == expectedFilePath:
		# Clean up old update attempt left overs
		if os.path.isfile(expectedFilePath):
			os.remove(expectedFilePath)
			logfile.write("Removed already existing file '" + expectedFilePath + "'\n")

		shutil.copy(packageFilePath, expectedFilePath)
		packageFilePath = expectedFilePath

	if applicationName == "java":
		# Extract targz-file (java package contains subdirectory with same name as file)
		EMTUtilities.unTarGzFile(packageFilePath, installBaseDirectoryPath, applicationUserName)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + installBaseDirectoryPath + "'" + "\n")

		if os.path.islink(Environment.javaHome):
			os.unlink(Environment.javaHome)
			EMTUtilities.createLink(updatedApplicationDirectoryPath, Environment.javaHome, applicationUserName)
			logfile.write("Created new application link '" + Environment.javaHome + "'\n")
			Environment.rebootNeeded = True
		elif os.path.isfile(Environment.javaHome) or os.path.isdir(Environment.javaHome):
			errorText = "Cannot update JAVA in '" + Environment.javaHome + "'"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
		else:
			Environment.javaHome = os.path.abspath(installBaseDirectoryPath + "/java")
			EMTUtilities.createLink(updatedApplicationDirectoryPath, Environment.javaHome, applicationUserName)
			logfile.write("Created new application link '" + Environment.javaHome + "'\n")
			Environment.rebootNeeded = True
		if os.path.isfile(packageFilePath):
			os.remove(packageFilePath)
	elif applicationName == "tomcat":
		# Extract targz-file (tomcat package contains subdirectory with same name as file)
		EMTUtilities.unTarGzFile(packageFilePath, installBaseDirectoryPath, applicationUserName)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + installBaseDirectoryPath + "'" + "\n")

		if os.path.islink(Environment.catalinaHome):
			os.unlink(Environment.catalinaHome)
			EMTUtilities.createLink(updatedApplicationDirectoryPath, Environment.catalinaHome, applicationUserName)
			logfile.write("Created new application link '" + Environment.catalinaHome + "'\n")
			Environment.rebootNeeded = True
		elif os.path.isfile(Environment.catalinaHome) or os.path.isdir(Environment.catalinaHome):
			errorText = "Cannot update tomcat in '" + Environment.catalinaHome + "'"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
		else:
			Environment.catalinaHome = os.path.abspath(installBaseDirectoryPath + "/tomcat")
			EMTUtilities.createLink(updatedApplicationDirectoryPath, Environment.catalinaHome, applicationUserName)
			logfile.write("Created new application link '" + Environment.catalinaHome + "'\n")
			Environment.rebootNeeded = True
		if os.path.isfile(packageFilePath):
			os.remove(packageFilePath)
	elif applicationName == "tomcat-native":
		# Extract targz-file (tomcat-native package contains subdirectory with same name as file)
		EMTUtilities.unTarGzFile(packageFilePath, installBaseDirectoryPath, applicationUserName)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + installBaseDirectoryPath + "'" + "\n")

		if Environment.tomcatNative is None:
			Environment.tomcatNative = os.path.abspath(installBaseDirectoryPath + "/tomcat-native")

		if Environment.environmentConfigurationFilePath != None and os.path.isfile(Environment.environmentConfigurationFilePath):
			environmentProperties = EMTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)
			environmentProperties["TOMCAT_NATIVE"] = Environment.tomcatNative
			if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
				username = "console"
			elif Environment.isEmmConsoleRdirServer:
				if Environment.isEmmRdirServer:
					username = "rdir"
				else:
					username = "console"
			else:
				username = "console"
			EMTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, username, environmentProperties)

		if os.path.islink(Environment.tomcatNative):
			os.unlink(Environment.tomcatNative)
			EMTUtilities.createLink(updatedApplicationDirectoryPath, Environment.tomcatNative, applicationUserName)
			logfile.write("Created new application link '" + Environment.tomcatNative + "'\n")
			Environment.rebootNeeded = True
		elif os.path.isfile(Environment.tomcatNative) or os.path.isdir(Environment.tomcatNative):
			errorText = "Cannot update tomcat in '" + Environment.catalinaHome + "'"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
		else:
			Environment.tomcatNative = os.path.abspath(installBaseDirectoryPath + "/tomcat-native")
			EMTUtilities.createLink(updatedApplicationDirectoryPath, Environment.tomcatNative, applicationUserName)
			logfile.write("Created new application link '" + Environment.tomcatNative + "'\n")
			Environment.rebootNeeded = True
		if os.path.isfile(packageFilePath):
			os.remove(packageFilePath)

		if Environment.osVendor == "Suse":
			try:
				os.system("zypper install -y libapr1 libapr-util1")
			except:
				print(Colors.RED + "Installation of Apache Tomcat Native was not successful" + Colors.DEFAULT)
				if EMTUtilities.isDebugMode():
					logging.exception("Error")
				sys.exit(1)
		else:
			try:
				os.system("yum install -y apr apr-util")
			except:
				print(Colors.RED + "Installation of Apache Tomcat Native was not successful" + Colors.DEFAULT)
				if EMTUtilities.isDebugMode():
					logging.exception("Error")
				sys.exit(1)
	elif applicationName == "runtime":
		# Create needed directories, if missing
		if Environment.isOpenEmmServer:
			if not os.path.isdir("/home/openemm/temp"):
				EMTUtilities.createDirectory("/home/openemm/temp", "openemm")
			if not os.path.isdir("/home/openemm/bin"):
				EMTUtilities.createDirectory("/home/openemm/bin", "openemm")
		elif Environment.isEmmRdirServer:
			if not os.path.isdir("/home/rdir/temp"):
				EMTUtilities.createDirectory("/home/rdir/temp", "rdir")
			if not os.path.isdir("/home/rdir/bin"):
				EMTUtilities.createDirectory("/home/rdir/bin", "rdir")
		elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
			if not os.path.isdir("/home/console/temp"):
				EMTUtilities.createDirectory("/home/console/temp", "console")
			if not os.path.isdir("/home/console/bin"):
				EMTUtilities.createDirectory("/home/console/bin", "console")

		createTemporarySystemFileBackup()

		# Delete old runtime data
		if os.path.islink("/home/" + applicationUserName + "/tomcat"):
			os.unlink("/home/" + applicationUserName + "/tomcat")
		if os.path.isdir("/home/" + applicationUserName + "/webapps-leave-empty"):
			shutil.rmtree("/home/" + applicationUserName + "/webapps-leave-empty")
		for listedFile in os.listdir("/home/" + applicationUserName):
			if re.search("^tomcat.*$", listedFile) and os.path.isdir(listedFile):
				shutil.rmtree(listedFile)

		# Extract targz-file for runtime
		EMTUtilities.unTarGzFile(packageFilePath, "/home/" + applicationUserName + "/", applicationUserName)

		restoreTemporarySystemFileBackup()

		logfile.write("Extracted file '" + packageFilePath + "' in path '/home/" + applicationUserName + "/" + "'" + "\n")

		Environment.messages.append(Environment.applicationName + " runtime was updated")

		# Create needed links, if missing
		if not os.path.isfile("/home/" + applicationUserName + "/bin/" + Environment.restartToolName) and os.path.isfile("/home/" + applicationUserName + "/tomcat/bin/" + Environment.restartToolName):
			EMTUtilities.createLink("/home/" + applicationUserName + "/tomcat/bin/" + Environment.restartToolName, "/home/" + applicationUserName + "/bin/" + Environment.restartToolName, applicationUserName)
		if not os.path.isfile("/home/" + applicationUserName + "/bin/emm.sh") and os.path.isfile("/home/" + applicationUserName + "/tomcat/bin/emm.sh"):
			EMTUtilities.createLink("/home/" + applicationUserName + "/tomcat/bin/emm.sh", "/home/" + applicationUserName + "/bin/emm.sh", applicationUserName)
		if not os.path.islink("/home/" + applicationUserName + "/conf") and not os.path.isdir("/home/" + applicationUserName + "/conf"):
			EMTUtilities.createLink("/home/" + applicationUserName + "/tomcat/conf", "/home/" + applicationUserName + "/conf", applicationUserName)
		if not os.path.islink("/home/" + applicationUserName + "/logs") and not os.path.isdir("/home/" + applicationUserName + "/logs"):
			EMTUtilities.createLink("/home/" + applicationUserName + "/tomcat/logs", "/home/" + applicationUserName + "/logs", applicationUserName)

		# Read new version info
		if os.path.isfile("/home/" + applicationUserName + "/tomcat/conf/version.txt"):
			with open("/home/" + applicationUserName + "/tomcat/conf/version.txt") as runtimeVersionFile:
				Environment.runtimeVersion = runtimeVersionFile.read().strip()
		else:
			Environment.runtimeVersion = "Unknown"

		if Environment.scriptFilePath.startswith("/home/console/") or Environment.scriptFilePath.startswith("/home/rdir/") or Environment.scriptFilePath.startswith("/home/openemm/"):
			if interactive:
				print(Colors.YELLOW + "To let the new Runtime version take effect, you must exit and restart this program!" + Colors.DEFAULT)
				print("Do you want to exit and restart now? (Y/n, Blank => Exit):")
				answer = input(" > ").lower().strip()
				if answer == "" or answer.startswith("y") or answer.startswith("j"):
					print(Colors.YELLOW + "Restarting ..." + Colors.DEFAULT)
					os.execl(sys.executable, Environment.scriptFilePath, *sys.argv)
					sys.exit(0)
			else:
				print(Colors.YELLOW + "Restart for Runtime Update..." + Colors.DEFAULT)
				os.execl(sys.executable, Environment.scriptFilePath, *sys.argv)
				sys.exit(0)
	elif applicationName == "backend-startup":
		newBackendVersion = EMTUtilities.getVersionFromFilename(packageFilename)
		if newBackendVersion is None:
			errorText = "Invalid backend package file name: " + packageFilename
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Remove existing older extracted directory of same version
		if os.path.isdir("/root/release/V" + newBackendVersion):
			shutil.rmtree("/root/release/V" + newBackendVersion)

		# Extract targz-file for backend
		EMTUtilities.unTarGzFileAsRoot(packageFilePath, "/root/release/")

		updatedApplicationDirectoryPath = "/root/release/V" + newBackendVersion

		if not os.path.isdir(updatedApplicationDirectoryPath):
			Environment.errors.append("Extraction of updatePackageFile '" + packageFilePath + "' was not successful")
			logfile.write("Extraction of updatePackageFile '" + packageFilePath + "' was not successful in path '" + updatedApplicationDirectoryPath + "'" + "\n")
			return

		os.remove(packageFilePath)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		print("new Backend Version: " + newBackendVersion)

		# Create new sym-link
		if os.path.islink("/root/release/previous"):
			os.unlink("/root/release/previous")
		if os.path.islink("/root/release/current"):
			os.rename("/root/release/current", "/root/release/previous")
		EMTUtilities.createLink("V" + newBackendVersion, "/root/release/current", "root")

		# Create basic symlink only on initial installation
		if not os.path.islink("/root/bin") and not os.path.isdir("/root/bin") and os.path.isdir("/root/release/current/bin"):
			EMTUtilities.createLink("/root/release/current/bin", "/root/bin", "root")

		logfile.write("Created new backend symlinks\n")

		if Environment.scriptFilePath.startswith("/root/"):
			if interactive:
				print(Colors.YELLOW + "To let the new version take effect, you must exit and restart this program!" + Colors.DEFAULT)
				print("Do you want to exit and restart now? (Y/n, Blank => Exit):")
				answer = input(" > ").lower().strip()
				if answer == "" or answer.startswith("y") or answer.startswith("j"):
					print(Colors.YELLOW + "Restarting ..." + Colors.DEFAULT)
					os.execl(sys.executable, Environment.scriptFilePath, *sys.argv)
					sys.exit(0)
			else:
				print(Colors.YELLOW + "Restart for Update..." + Colors.DEFAULT)
				os.execl(sys.executable, Environment.scriptFilePath, *sys.argv)
				sys.exit(0)
	elif applicationName == "backend-merger" or applicationName == "backend-mailer" or applicationName == "backend-mailloop":
		newBackendVersion = EMTUtilities.getVersionFromFilename(packageFilename)
		if newBackendVersion is None:
			errorText = "Invalid backend package file name: " + packageFilename
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Remove existing older extracted directory of same version
		if os.path.isdir("/home/" + applicationUserName + "/release/V" + newBackendVersion):
			shutil.rmtree("/home/" + applicationUserName + "/release/V" + newBackendVersion)

		# Extract targz-file for backend
		EMTUtilities.unTarGzFileAsRoot(packageFilePath, "/home/" + applicationUserName + "/release/")

		updatedApplicationDirectoryPath = "/home/" + applicationUserName + "/release/V" + newBackendVersion

		if not os.path.isdir(updatedApplicationDirectoryPath):
			Environment.errors.append("Extraction of updatePackageFile '" + packageFilePath + "' was not successful")
			logfile.write("Extraction of updatePackageFile '" + packageFilePath + "' was not successful in path '" + updatedApplicationDirectoryPath + "'" + "\n")
			return

		os.remove(packageFilePath)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		print("new Backend Version: " + newBackendVersion)

		if applicationName == "backend-merger":
			backendRestartTool = "/home/" + applicationUserName + "/bin/merger.sh"
		elif applicationName == "backend-mailer":
			backendRestartTool = "/home/" + applicationUserName + "/bin/mailer.sh"
		elif applicationName == "backend-mailloop":
			backendRestartTool = "/home/" + applicationUserName + "/bin/mailloop.sh"

		# Stop existing backend
		if os.path.islink(backendRestartTool):
			restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
			print("Restart log in file: " + restartLogFile);

			print("Stopping with: " + backendRestartTool)
			if EMTUtilities.hasRootPermissions():
				os.system("su -c \"" + backendRestartTool + " stop 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
			else:
				os.system(backendRestartTool + " stop 2>&1 | tee --append " + restartLogFile)

		# Create new sym-link
		if os.path.islink("/home/" + applicationUserName + "/release/previous"):
			os.unlink("/home/" + applicationUserName + "/release/previous")
		if os.path.islink("/home/" + applicationUserName + "/release/current"):
			os.rename("/home/" + applicationUserName + "/release/current", "/home/" + applicationUserName + "/release/previous")
		EMTUtilities.createLink("V" + newBackendVersion, "/home/" + applicationUserName + "/release/current", applicationUserName)

		# Create basic backend symlinks only on initial installation
		if applicationName == "backend-mailloop":
			if os.path.islink("/home/" + applicationUserName + "/lib"):
				os.unlink("/home/" + applicationUserName + "/lib")
			elif os.path.isdir("/home/" + applicationUserName + "/lib"):
				shutil.rmtree("/home/" + applicationUserName + "/lib")
			if not os.path.islink("/home/" + applicationUserName + "/lib") and not os.path.isdir("/home/" + applicationUserName + "/lib") and os.path.isdir("/home/" + applicationUserName + "/release/current/lib"):
				EMTUtilities.createLink("/home/" + applicationUserName + "/release/current/lib", "/home/" + applicationUserName + "/lib", applicationUserName)

		if not os.path.islink("/home/" + applicationUserName + "/scripts") and not os.path.isdir("/home/" + applicationUserName + "/scripts"):
			EMTUtilities.createLink("/home/" + applicationUserName + "/release/current/scripts", "/home/" + applicationUserName + "/scripts", applicationUserName)
			os.system("cd /home/" + applicationUserName + "/bin && ln -s ../release/backend/current/bin/* .")
			logfile.write("Created new initial backend symlinks\n")

		if applicationName == "backend-merger":
			if not os.path.islink("/home/" + applicationUserName + "/JAVA") and not os.path.isdir("/home/" + applicationUserName + "/JAVA"):
				EMTUtilities.createLink("/home/" + applicationUserName + "/release/current/JAVA", "/home/" + applicationUserName + "/JAVA", applicationUserName)

		logfile.write("Created new backend symlinks\n")

		if applicationName == "backend-merger":
			if os.path.isdir("/home/" + applicationUserName + "/release/current"):
				Environment.mergerBackendVersion = os.path.basename(os.path.realpath("/home/" + applicationUserName + "/release/current"))
				if Environment.mergerBackendVersion is not None and Environment.mergerBackendVersion.startswith("V"):
					Environment.mergerBackendVersion = Environment.mergerBackendVersion[1:len(Environment.mergerBackendVersion)]
			else:
				Environment.mergerBackendVersion = None
		elif applicationName == "backend-mailer":
			if os.path.isdir("/home/" + applicationUserName + "/release/current"):
				Environment.mailerBackendVersion = os.path.basename(os.path.realpath("/home/" + applicationUserName + "/release/current"))
				if Environment.mailerBackendVersion is not None and Environment.mailerBackendVersion.startswith("V"):
					Environment.mailerBackendVersion = Environment.mailerBackendVersion[1:len(Environment.mailerBackendVersion)]
			else:
				Environment.mailerBackendVersion = None
		elif applicationName == "backend-mailloop":
			if os.path.isdir("/home/" + applicationUserName + "/release/current"):
				Environment.mailloopBackendVersion = os.path.basename(os.path.realpath("/home/" + applicationUserName + "/release/current"))
				if Environment.mailloopBackendVersion is not None and Environment.mailloopBackendVersion.startswith("V"):
					Environment.mailloopBackendVersion = Environment.mailloopBackendVersion[1:len(Environment.mailloopBackendVersion)]
			else:
				Environment.mailloopBackendVersion = None

		# Start new backend
		if os.path.isfile(backendRestartTool):
			restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
			print("Restart log in file: " + restartLogFile);

			print("Starting with: " + backendRestartTool)
			if EMTUtilities.hasRootPermissions():
				os.system("su -c \"" + backendRestartTool + " start 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
			else:
				os.system(backendRestartTool + " start 2>&1 | tee --append " + restartLogFile)
		else:
			Environment.errors.append("Cannot find backend start tool '" + backendRestartTool + "'")
	elif applicationName == "backend-console" or applicationName == "backend-rdir":
		newBackendVersion = EMTUtilities.getVersionFromFilename(packageFilename)
		if newBackendVersion is None:
			errorText = "Invalid backend package file name: " + packageFilename
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Remove existing older extracted directory of same version
		if os.path.isdir("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion):
			shutil.rmtree("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion)

		# Extract targz-file for backend
		EMTUtilities.unTarGzFile(packageFilePath, "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/")

		updatedApplicationDirectoryPath = "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion

		if not os.path.isdir(updatedApplicationDirectoryPath):
			Environment.errors.append("Extraction of updatePackageFile '" + packageFilePath + "' was not successful")
			logfile.write("Extraction of updatePackageFile '" + packageFilePath + "' was not successful in path '" + updatedApplicationDirectoryPath + "'" + "\n")
			return

		os.remove(packageFilePath)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		print("new Backend Version: " + newBackendVersion)

		# Create new sym-link
		if os.path.islink("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/previous"):
			os.unlink("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/previous")
		if os.path.islink("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/current"):
			os.rename("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/current", "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/previous")
		EMTUtilities.createLink("V" + newBackendVersion, "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/current", applicationUserName)

		# Create basic backend symlinks only on initial installation
		if not os.path.islink("/home/" + applicationUserName + "/scripts") and not os.path.isdir("/home/" + applicationUserName + "/scripts"):
			EMTUtilities.createLink("/home/" + applicationUserName + "/release/backend/current/scripts", "/home/" + applicationUserName + "/scripts", applicationUserName)

			os.system("cd /home/" + applicationUserName + "/bin && ln -s ../release/backend/current/bin/* .")
			logfile.write("Created new initial backend symlinks\n")

		logfile.write("Created new backend symlinks\n")

		if applicationName == "backend-console":
			if os.path.isdir("/home/" + applicationUserName + "/release/backend/current"):
				Environment.emmBackendVersion = os.path.basename(os.path.realpath("/home/" + applicationUserName + "/release/backend/current"))
				if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
					Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]
			else:
				Environment.emmBackendVersion = None
		elif applicationName == "backend-rdir":
			if os.path.isdir("/home/" + applicationUserName + "/release/backend/current"):
				Environment.rdirBackendVersion = os.path.basename(os.path.realpath("/home/" + applicationUserName + "/release/backend/current"))
				if Environment.rdirBackendVersion is not None and Environment.rdirBackendVersion.startswith("V"):
					Environment.rdirBackendVersion = Environment.rdirBackendVersion[1:len(Environment.rdirBackendVersion)]
			else:
				Environment.rdirBackendVersion = None

	elif applicationName == "backend":
		newBackendVersion = EMTUtilities.getVersionFromFilename(packageFilename)
		if newBackendVersion is None:
			errorText = "Invalid backend package file name: " + packageFilename
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Remove existing older extracted directory of same version
		if os.path.isdir("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion):
			shutil.rmtree("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion)

		# Extract targz-file for backend
		EMTUtilities.unTarGzFileAsRoot(packageFilePath, "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/")

		updatedApplicationDirectoryPath = "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion

		if not os.path.isdir(updatedApplicationDirectoryPath):
			Environment.errors.append("Extraction of updatePackageFile '" + packageFilePath + "' was not successful")
			logfile.write("Extraction of updatePackageFile '" + packageFilePath + "' was not successful in path '" + updatedApplicationDirectoryPath + "'" + "\n")
			return

		os.remove(packageFilePath)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		print("new Backend Version: " + newBackendVersion)

		# Stop existing backend
		if os.path.islink("/home/" + applicationUserName + "/bin/backend.sh"):
			restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
			print("Restart log in file: " + restartLogFile);

			print("Stopping with: backend.sh")
			if EMTUtilities.hasRootPermissions():
				os.system("su -c \"/home/" + applicationUserName + "/bin/backend.sh stop 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
			else:
				os.system("/home/" + applicationUserName + "/bin/backend.sh stop 2>&1 | tee --append " + restartLogFile)

		# Create new sym-link
		if os.path.islink("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/previous"):
			os.unlink("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/previous")
		if os.path.islink("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/current"):
			os.rename("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/current", "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/previous")
		EMTUtilities.createLink("V" + newBackendVersion, "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/current", applicationUserName)

		# Create basic backend symlinks only on initial installation
		if os.path.islink("/home/" + applicationUserName + "/lib"):
			os.unlink("/home/" + applicationUserName + "/lib")
		elif os.path.isdir("/home/" + applicationUserName + "/lib"):
			shutil.rmtree("/home/" + applicationUserName + "/lib")
		if not os.path.islink("/home/" + applicationUserName + "/lib") and not os.path.isdir("/home/" + applicationUserName + "/lib"):
			EMTUtilities.createLink("/home/" + applicationUserName + "/release/backend/current/lib", "/home/" + applicationUserName + "/lib", applicationUserName)

		if not os.path.islink("/home/" + applicationUserName + "/scripts") and not os.path.isdir("/home/" + applicationUserName + "/scripts"):
			EMTUtilities.createLink("/home/" + applicationUserName + "/release/backend/current/scripts", "/home/" + applicationUserName + "/scripts", applicationUserName)
		if not os.path.islink("/home/" + applicationUserName + "/JAVA") and not os.path.isdir("/home/" + applicationUserName + "/JAVA"):
			EMTUtilities.createLink("/home/" + applicationUserName + "/release/backend/current/JAVA", "/home/" + applicationUserName + "/JAVA", applicationUserName)
			os.system("cd /home/" + applicationUserName + "/bin && ln -s ../release/backend/current/bin/* .")
			logfile.write("Created new initial backend symlinks\n")

		logfile.write("Created new backend symlinks\n")

		if os.path.isdir("/home/" + applicationUserName + "/release/backend/current"):
			Environment.emmBackendVersion = os.path.basename(os.path.realpath("/home/" + applicationUserName + "/release/backend/current"))
			if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
				Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]
		else:
			Environment.emmBackendVersion = None

		# Start new backend
		if os.path.isfile("/home/" + applicationUserName + "/bin/backend.sh"):
			restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
			print("Restart log in file: " + restartLogFile);

			print("Starting with: backend.sh")
			if EMTUtilities.hasRootPermissions():
				os.system("su -c \"/home/" + applicationUserName + "/bin/backend.sh start 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
			else:
				os.system("/home/" + applicationUserName + "/bin/backend.sh start 2>&1 | tee --append " + restartLogFile)
		else:
			Environment.errors.append("Cannot start backend")
	elif applicationName == "manual":
		# Extract targz-file for manuals
		EMTUtilities.createDirectory(updatedApplicationDirectoryPath, applicationUserName)
		EMTUtilities.unTarGzFile(packageFilePath, updatedApplicationDirectoryPath, applicationUserName)

		# Remove obsolete sub directory
		if os.path.isdir(updatedApplicationDirectoryPath + "/" + packageFilename.replace(".tar.gz", "")):
			os.system("mv '" + updatedApplicationDirectoryPath + "/" + packageFilename.replace(".tar.gz", "") + "' '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'")
			os.system("mv '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'/* '" + updatedApplicationDirectoryPath + "/'")
			os.system("rmdir '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'")

		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		# Create default directory and link for manuals
		if not os.path.isdir("/home/" + applicationUserName + "/webapps/manual"):
			EMTUtilities.createDirectory("/home/" + applicationUserName + "/webapps/manual", applicationUserName)
		if not os.path.islink("/home/" + applicationUserName + "/webapps/manual/default"):
			EMTUtilities.createLink("/home/" + applicationUserName + "/webapps/manual/en", "/home/" + applicationUserName + "/webapps/manual/default", applicationUserName)

		# Create new webapps-link for manuals
		if os.path.islink("/home/" + applicationUserName + "/webapps/manual/en"):
			os.unlink("/home/" + applicationUserName + "/webapps/manual/en")
		EMTUtilities.createLink(updatedApplicationDirectoryPath + "/en", "/home/" + applicationUserName + "/webapps/manual/en", applicationUserName)
		if os.path.islink("/home/" + applicationUserName + "/webapps/manual/de"):
			os.unlink("/home/" + applicationUserName + "/webapps/manual/de")
		EMTUtilities.createLink(updatedApplicationDirectoryPath + "/de", "/home/" + applicationUserName + "/webapps/manual/de", applicationUserName)
		logfile.write("Created new manual links '/home/" + applicationUserName + "/webapps/manual" + "'\n")

		# Read new version info
		manualApplicationPath = os.path.realpath("/home/" + applicationUserName + "/webapps/manual/de")
		manualVersion = EMTUtilities.getVersionFromFilename(manualApplicationPath)
		if manualVersion is not None:
			Environment.manualVersion = manualVersion
		else:
			Environment.manualVersion = "Unknown"
	else:
		# Frontend, Statistics, Webservices or Rdir
		if DbConnector.emmDbVendor is None:
			Environment.errors.append("Mandatory database configuration not found")
			return
		elif applicationName == "gui" and DbConnector.getDbClientPath() is None:
			Environment.errors.append("Mandatory database client is NOT available, but needed for frontend application update")
			return

		# Extract targz-file
		EMTUtilities.createDirectory(updatedApplicationDirectoryPath, applicationUserName)
		EMTUtilities.unTarGzFile(packageFilePath, updatedApplicationDirectoryPath, applicationUserName)

		# Remove obsolete sub directory
		if os.path.isdir(updatedApplicationDirectoryPath + "/" + packageFilename.replace(".tar.gz", "")):
			os.system("mv '" + updatedApplicationDirectoryPath + "/" + packageFilename.replace(".tar.gz", "") + "' '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'")
			os.system("mv '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'/* '" + updatedApplicationDirectoryPath + "/'")
			os.system("rmdir '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'")

		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		# Preserve session timeout
		if applicationName == "gui" and os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/web.xml") and os.path.isfile(updatedApplicationDirectoryPath + "/WEB-INF/web.xml"):
			pattern = re.compile("<session-timeout>(\d+)</session-timeout>")
			sessionTimeoutMinutes = None
			for i, line in enumerate(open("/home/" + applicationUserName + "/webapps/emm/WEB-INF/web.xml")):
				for match in re.finditer(pattern, line):
					sessionTimeoutMinutes = match.group(1)
			if sessionTimeoutMinutes is not None:
				print("Preserving session timeout: " + sessionTimeoutMinutes + "\n")
				logfile.write("Preserving session timeout: " + sessionTimeoutMinutes + "\n")
				with open(updatedApplicationDirectoryPath + "/WEB-INF/web.xml", 'r+') as f:
					text = f.read()
					text = re.sub("<session-timeout>(\d+)</session-timeout>", "<session-timeout>" + sessionTimeoutMinutes + "</session-timeout>", text)
					f.seek(0)
					f.write(text)
					f.truncate()

		# Create new webapps-link
		if applicationName == "gui":
			if Environment.isOpenEmmServer:
				if os.path.islink("/home/openemm/webapps/emm"):
					os.unlink("/home/openemm/webapps/emm")
				EMTUtilities.createLink(updatedApplicationDirectoryPath, "/home/openemm/webapps/emm", "openemm")
				logfile.write("Created new application link 'emm'\n")
			else:
				if os.path.islink("/home/console/webapps/emm"):
					os.unlink("/home/console/webapps/emm")
				EMTUtilities.createLink(updatedApplicationDirectoryPath, "/home/console/webapps/emm", "console")
				logfile.write("Created new application link 'emm'\n")
		else:
			if Environment.isOpenEmmServer:
				if os.path.islink("/home/openemm/webapps/" + applicationName):
					os.unlink("/home/openemm/webapps/" + applicationName)
				EMTUtilities.createLink(updatedApplicationDirectoryPath, "/home/openemm/webapps/" + applicationName, "openemm")
				logfile.write("Created new application link '" + applicationName + "'\n")
			elif Environment.isEmmRdirServer:
				if os.path.islink("/home/rdir/webapps/" + applicationName):
					os.unlink("/home/rdir/webapps/" + applicationName)
				EMTUtilities.createLink(updatedApplicationDirectoryPath, "/home/rdir/webapps/" + applicationName, "rdir")
				logfile.write("Created new application link '" + applicationName + "'\n")
			else:
				if os.path.islink("/home/console/webapps/" + applicationName):
					os.unlink("/home/console/webapps/" + applicationName)
				EMTUtilities.createLink(updatedApplicationDirectoryPath, "/home/console/webapps/" + applicationName, "console")
				logfile.write("Created new application link '" + applicationName + "'\n")

		# Execute sql database update scripts
		if applicationName == "gui":
			if Environment.isOpenEmmServer:
				applicationUserName = "openemm"
			else:
				applicationUserName = "console"

			if not DbConnector.checkDbStructureExists() and os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql"):
				print("Creating basic database structure ...")
				fullDbScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-basic.sql")
				if not fullDbScriptSuccess:
					errorText = "Error while executing full database script"
					Environment.errors.append(errorText)
					logfile.write(errorText + "\n")
					return
				elif os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-extended.sql"):
					print("Creating extended database structure ...")
					fullDbExtendedScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-extended.sql")
					if not fullDbExtendedScriptSuccess:
						errorText = "Error while executing full database extended script"
						Environment.errors.append(errorText)
						logfile.write(errorText + "\n")
						return
				else:
					if os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-openemm.sql"):
						print("Creating OpenEMM database structure ...")
						openemmFullDbScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-openemm.sql")
						if not openemmFullDbScriptSuccess:
							errorText = "Error while executing OpenEMM full database script"
							Environment.errors.append(errorText)
							logfile.write(errorText + "\n")
							return
				DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
				DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
				DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else "console")
			elif not DbConnector.checkDbStructureExists() and os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql"):
				print("Creating basic database structure ...")
				fullDbScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-basic.sql")
				if not fullDbScriptSuccess:
					errorText = "Error while executing full database script"
					Environment.errors.append(errorText)
					logfile.write(errorText + "\n")
					return
				elif os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-extended.sql"):
					print("Creating extended database structure ...")
					fullDbExtendedScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-extended.sql")
					if not fullDbExtendedScriptSuccess:
						errorText = "Error while executing full database extended script"
						Environment.errors.append(errorText)
						logfile.write(errorText + "\n")
						return
				else:
					if os.path.isfile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-openemm.sql"):
						print("Creating OpenEMM database structure ...")
						openemmFullDbScriptSuccess = DbConnector.executeSqlScriptFile("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-fulldb-openemm.sql")
						if not openemmFullDbScriptSuccess:
							errorText = "Error while executing OpenEMM full database script"
							Environment.errors.append(errorText)
							logfile.write(errorText + "\n")
							return
				DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
				DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
				DbConnector.insertBirtKeysInDb("openemm" if Environment.isOpenEmmServer else "console")

			print("Executing database structure updates ...")

			sqlUpdateLogfilePath = "/home/" + applicationUserName + "/release/log/sql_update_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
			if not os.path.isdir("/home/" + applicationUserName + "/release/log"):
				EMTUtilities.createDirectories("/home/" + applicationUserName + "/release/log", applicationUserName)
			if DbConnector.emmDbVendor == "oracle":
				os.system("chmod u+x /home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-missing-updates.sh")
				sqlUpdateReturnCode = os.system("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/oracle/emm-oracle-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath + " 2>&1 | tee " + sqlUpdateLogfilePath + "; test ${PIPESTATUS[0]} -eq 0")
			elif DbConnector.emmDbVendor == "mysql" or DbConnector.emmDbVendor == "mariadb":
				if os.path.isdir("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb"):
					os.system("chmod u+x /home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh")
					sqlUpdateReturnCode = os.system("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath + " 2>&1 | tee " + sqlUpdateLogfilePath + "; test ${PIPESTATUS[0]} -eq 0")
				else:
					os.system("chmod u+x /home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh")
					sqlUpdateReturnCode = os.system("/home/" + applicationUserName + "/webapps/emm/WEB-INF/sql/mysql/emm-mysql-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath + " 2>&1 | tee " + sqlUpdateLogfilePath + "; test ${PIPESTATUS[0]} -eq 0")

			if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
				with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
					sqlLogContent = sqlUpdateLogfile.read()
					logfile.write("SQL Update:\n" + sqlLogContent + "\n")
				os.remove(sqlUpdateLogfilePath)

			if sqlUpdateReturnCode != 0:
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
			if os.path.isfile("/home/openemm/webapps/emm/WEB-INF/classes/emm.properties"):
				Environment.frontendVersion = EMTUtilities.readPropertiesFile("/home/openemm/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if os.path.isfile("/home/openemm/webapps/statistics/WEB-INF/classes/emm.properties"):
				Environment.statisticsVersion = EMTUtilities.readPropertiesFile("/home/openemm/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if os.path.isfile("/home/openemm/webapps/webservices/WEB-INF/classes/emm.properties"):
				Environment.webservicesVersion = EMTUtilities.readPropertiesFile("/home/openemm/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]
		else:
			if Environment.username == "console" or EMTUtilities.hasRootPermissions():
				if os.path.isfile("/home/console/webapps/emm/WEB-INF/classes/emm.properties"):
					Environment.frontendVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]
				if os.path.isfile("/home/console/webapps/statistics/WEB-INF/classes/emm.properties"):
					Environment.statisticsVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]
				if os.path.isfile("/home/console/webapps/webservices/WEB-INF/classes/emm.properties"):
					Environment.webservicesVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]
				if os.path.isfile("/home/console/webapps/rdir/WEB-INF/classes/emm.properties"):
					Environment.consoleRdirVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]
			if Environment.username == "rdir" or EMTUtilities.hasRootPermissions():
				if os.path.isfile("/home/rdir/webapps/rdir/WEB-INF/classes/emm.properties"):
					Environment.consoleRdirVersion = EMTUtilities.readPropertiesFile("/home/rdir/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]

def createTemporarySystemFileBackup():
	# Create temporary backup of system specific configuration files

	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	elif Environment.isEmmRdirServer:
		applicationUserName = "rdir"
	elif Environment.isEmmMergerServer:
		applicationUserName = "merger"
	elif Environment.isEmmMailerServer:
		applicationUserName = "mailout"
	elif Environment.isEmmMailloopServer:
		applicationUserName = "mailloop"
	else:
		applicationUserName = "console"

	if os.path.isdir("/tmp/EmmConfigurationBackup"):
		shutil.rmtree("/tmp/EmmConfigurationBackup")
	os.mkdir("/tmp/EmmConfigurationBackup")
	for systemSpecificConfigFile in Environment.systemSpecificConfigFiles:
		if os.path.isfile("/home/" + applicationUserName + "/" + systemSpecificConfigFile):
			if os.path.isfile("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile):
				os.remove("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile)
			os.makedirs(os.path.dirname("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile), exist_ok=True)
			shutil.copy("/home/" + applicationUserName + "/" + systemSpecificConfigFile, "/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile)
	for directoryToPreserve in Environment.directoriesToPreserve:
		if os.path.isdir("/home/" + applicationUserName + "/" + directoryToPreserve):
			if os.path.isdir("/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve):
				shutil.rmtree("/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve)
			shutil.move("/home/" + applicationUserName + "/" + directoryToPreserve, "/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve)

def restoreTemporarySystemFileBackup():
	# Restore system specific configuration files from temporary backup

	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	elif Environment.isEmmRdirServer:
		applicationUserName = "rdir"
	elif Environment.isEmmMergerServer:
		applicationUserName = "merger"
	elif Environment.isEmmMailerServer:
		applicationUserName = "mailout"
	elif Environment.isEmmMailloopServer:
		applicationUserName = "mailloop"
	else:
		applicationUserName = "console"

	if os.path.isdir("/tmp/EmmConfigurationBackup"):
		for systemSpecificConfigFile in Environment.systemSpecificConfigFiles:
			if os.path.isfile("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile) and not os.path.isfile("/home/" + applicationUserName + "/" + systemSpecificConfigFile):
				shutil.copy("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile, "/home/" + applicationUserName + "/" + systemSpecificConfigFile)
		for directoryToPreserve in Environment.directoriesToPreserve:
			if os.path.isdir("/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve):
				if os.path.isdir("/home/" + applicationUserName + "/" + directoryToPreserve):
					shutil.rmtree("/home/" + applicationUserName + "/" + directoryToPreserve)
				shutil.move("/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve, "/home/" + applicationUserName + "/" + directoryToPreserve)
		shutil.rmtree("/tmp/EmmConfigurationBackup")

def getApplicationDisplayName(applicationName):
	if applicationName == "complete":
		return Environment.applicationName
	elif applicationName == "frontend":
		return Environment.applicationName
	elif applicationName == "gui":
		return Environment.applicationName
	elif applicationName == "runtime":
		return "Runtime"
	elif applicationName == "backend":
		return "Backend"
	elif applicationName == "manual":
		return "Manual"
	elif applicationName == "statistics":
		return "Statistics"
	elif applicationName == "webservices":
		return "Webservices"
	elif applicationName == "rdir":
		return "Rdir"
	elif applicationName == "tomcat-native":
		return "Apache Tomcat Native"
	elif applicationName == "tomcat":
		return "Apache Tomcat"
	elif applicationName == "java":
		return "JAVA"
	else:
		return applicationName.upper()

def installFileFromCloud(packageUrl, interactive, alreadyOpenLogFile = None):
	print()
	print("Starting " + Environment.applicationName + " Update ...")
	if EMTUtilities.isDebugMode():
		print("Package Url: " + packageUrl)

	now = datetime.datetime.now()
	nowString = now.strftime("%Y-%m-%d_%H-%M-%S")
	logfile = None
	try:
		if Environment.isOpenEmmServer:
			applicationUserName = "openemm"
		elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
			applicationUserName = "console"
		elif Environment.isEmmRdirServer:
			applicationUserName = "rdir"
		elif Environment.isEmmMergerServer:
			applicationUserName = "merger"
		elif Environment.isEmmMailerServer:
			applicationUserName = "mailout"
		elif Environment.isEmmMailloopServer:
			applicationUserName = "mailloop"
		else:
			applicationUserName = "console"

		if EMTUtilities.hasRootPermissions():
			if not os.path.isdir("/root/release"):
				EMTUtilities.createDirectory("/root/release", "root")
			if not os.path.isdir("/root/release/log"):
				EMTUtilities.createDirectory("/root/release/log", "root")

			if alreadyOpenLogFile is None:
				logfilePath = "/root/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile
		else:
			if not os.path.isdir("/home/" + applicationUserName + "/release"):
				EMTUtilities.createDirectory("/home/" + applicationUserName + "/release", applicationUserName)
			if not os.path.isdir("/home/" + applicationUserName + "/release/log"):
				EMTUtilities.createDirectory("/home/" + applicationUserName + "/release/log", applicationUserName)

			if alreadyOpenLogFile is None:
				logfilePath = "/home/" + applicationUserName + "/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile

		logfile.write("Update package url/id: " + packageUrl + "\n")

		if packageUrl.endswith("/download"):
			packageUrl = packageUrl[:-9]

		packageDownloadID = packageUrl.split("/")[-1]

		if not EMTUtilities.checkDownloadFileIsAvailable(Environment.agnitasCloudUrl + "/index.php/s/" + packageDownloadID):
			Environment.errors.append("Unknown update package id: " + packageDownloadID)
			return "CLOUDUPDATE"

		tryDownload = True
		packagePassword = ""
		while tryDownload:
			downloadPageResponse = None
			try:
				username = packageDownloadID
				password = packagePassword
				passwordManager = urllib.request.HTTPPasswordMgrWithDefaultRealm()
				passwordManager.add_password(None, Environment.agnitasCloudUrl + Environment.agnitasCloudWebdavContext, username, password)
				authhandler = urllib.request.HTTPBasicAuthHandler(passwordManager)
				opener = urllib.request.build_opener(authhandler, urllib.request.HTTPSHandler(context = ssl.SSLContext(ssl.PROTOCOL_TLS)))
				downloadPageResponse = opener.open(Environment.agnitasCloudUrl + Environment.agnitasCloudWebdavContext)
				_, params = cgi.parse_header(downloadPageResponse.headers.get("Content-Disposition", ""))
				packageFilename = params["filename"]
				downloadPageResponse.close()
				downloadPageResponse = None
				tryDownload = False
			except urllib.error.HTTPError as e:
				if downloadPageResponse is not None:
					downloadPageResponse.close()

				if e.code == 401:
					# Unauthorized
					if packagePassword != "":
						print(Colors.RED + "Invalid password" + Colors.DEFAULT)
					print("Please enter password (Blank => Cancel):")
					packagePassword = getpass.getpass(" > ")
					if packagePassword == "":
						Environment.messages.append("Update canceled")
						return
					else:
						tryDownload = True
				else:
					raise

		logfile.write("Update package file: " + packageFilename + "\n")

		if "-runtime-" in packageFilename:
			applicationName = "runtime"
			releaseSubdirectoryName = "runtime"
			currentVersion = Environment.runtimeVersion
		elif "backend-console-" in packageFilename:
			applicationName = "backend-console"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.emmBackendVersion
		elif "backend-rdir-" in packageFilename:
			applicationName = "backend-rdir"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.rdirBackendVersion
		elif "-startup-" in packageFilename:
			applicationName = "backend-startup"
			releaseSubdirectoryName = ""
			currentVersion = Environment.startupBackendVersion
		elif "-merger-" in packageFilename:
			applicationName = "backend-merger"
			releaseSubdirectoryName = ""
			currentVersion = Environment.mergerBackendVersion
		elif "-mailer-" in packageFilename:
			applicationName = "backend-mailer"
			releaseSubdirectoryName = ""
			currentVersion = Environment.mailerBackendVersion
		elif "-mailloop-" in packageFilename:
			applicationName = "backend-mailloop"
			releaseSubdirectoryName = ""
			currentVersion = Environment.mailloopBackendVersion
		elif "-backend-" in packageFilename:
			applicationName = "backend"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.emmBackendVersion
		elif "-manual-" in packageFilename:
			applicationName = "manual"
			releaseSubdirectoryName = "manual"
			currentVersion = Environment.manualVersion
		elif "-emm-" in packageFilename or "-gui-" in packageFilename:
			applicationName = "gui"
			releaseSubdirectoryName = "emm"
			currentVersion = Environment.frontendVersion
		elif "-statistics-" in packageFilename:
			applicationName = "statistics"
			releaseSubdirectoryName = "statistics"
			currentVersion = Environment.statisticsVersion
		elif "-webservices-" in packageFilename:
			applicationName = "webservices"
			releaseSubdirectoryName = "webservices"
			currentVersion = Environment.webservicesVersion
		elif "-rdir-" in packageFilename:
			applicationName = "rdir"
			releaseSubdirectoryName = "rdir"
			if Environment.isEmmConsoleRdirServer:
				currentVersion = Environment.consoleRdirVersion
			else:
				currentVersion = Environment.rdirVersion
		elif "-tomcat-native-" in packageFilename:
			applicationName = "tomcat-native"
			releaseSubdirectoryName = None
			currentVersion = EMTUtilities.getTomcatNativeVersion(Environment.tomcatNative)
		elif "-tomcat-" in packageFilename:
			applicationName = "tomcat"
			releaseSubdirectoryName = None
			currentVersion = EMTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
		elif "-java-" in packageFilename:
			applicationName = "java"
			releaseSubdirectoryName = None
			currentVersion = EMTUtilities.getJavaVersion(Environment.javaHome)
		else:
			# Handle as complete package
			applicationName = "complete"
			releaseSubdirectoryName = None

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

		versionCompare = EMTUtilities.compareVersions(currentVersion, EMTUtilities.getVersionFromFilename(packageFilename))
		if versionCompare < 1:
			print()
			print("Package contains application type '" + getApplicationDisplayName(applicationName) + "' of Version '" + EMTUtilities.normalizeVersion(EMTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(EMTUtilities.normalizeVersion(currentVersion)) + "'.")
			if interactive:
				if versionCompare == 0:
					print(Colors.YELLOW + "This is the same version." + Colors.DEFAULT)
				else:
					print(Colors.RED + "The update package is an older version!" + Colors.DEFAULT)
				print("Install anyway? (N/y, Blank => Cancel):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if not (choice.startswith("y") or choice.startswith("j")):
					logfile.write("Declined installation of old version '" + EMTUtilities.getVersionFromFilename(packageFilename) + "' of application type '" + applicationName + "'\n")
					return
			else:
				print("Obsolete update is skipped.")
				return
		elif releaseSubdirectoryName is not None and not os.path.isdir("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName):
			print()
			print("Package contains new application '" + getApplicationDisplayName(applicationName) + "'")
			if interactive:
				print("Initially install this application? (N/y, Blank => Cancel):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if choice.startswith("y") or choice.startswith("j"):
					EMTUtilities.createDirectory("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName, applicationUserName)
				else:
					Environment.errors.append("Unexpected update package type '" + getApplicationDisplayName(applicationName) + "'")
					logfile.write("Unexpected update package type '" + applicationName + "'" + "\n")
					return
			else:
				print("Initial update is skipped. Please install manually for first time")
				return
		else:
			print()
			print("Found update package '" + packageFilename + "' for application '" + getApplicationDisplayName(applicationName) + "' with version '" + EMTUtilities.normalizeVersion(EMTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(EMTUtilities.normalizeVersion(currentVersion)) + "'.")
			if interactive:
				print("Continue with update? (Y/n, Blank => Yes):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if not (choice == "" or choice.startswith("y") or choice.startswith("j")):
					errorText = "Update canceled"
					Environment.errors.append(errorText)
					logfile.write(errorText + "\n")
					return
			else:
				if releaseSubdirectoryName is not None and len(releaseSubdirectoryName.strip()) > 0:
					cleanupOldVersions("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName, EMTUtilities.normalizeVersion(EMTUtilities.getVersionFromFilename(packageFilename)), EMTUtilities.normalizeVersion(currentVersion), logfile)

		if applicationName == "backend" \
			and not EMTUtilities.hasRootPermissions() \
			and not Environment.isEmmFrontendServer \
			and not Environment.isEmmConsoleRdirServer \
			and not Environment.isEmmWebservicesServer \
			and not Environment.isEmmStatisticsServer:
			errorText = "You need to start the program with root permissions (sudo) to update the backend application"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		if EMTUtilities.isBlank(releaseSubdirectoryName):
			downloadDestinationFilePath = "/tmp/" + packageFilename
		else:
			downloadDestinationFilePath = "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/" + packageFilename

		# Clean up old update attempt left overs
		if os.path.isfile(downloadDestinationFilePath):
			os.remove(downloadDestinationFilePath)
			logfile.write("Removed already existing file '" + downloadDestinationFilePath + "'\n")

		# Download update file
		print("Download started")
		downloadPageResponse = None
		try:
			username = packageDownloadID
			password = packagePassword
			passwordManager = urllib.request.HTTPPasswordMgrWithDefaultRealm()
			passwordManager.add_password(None, Environment.agnitasCloudUrl + Environment.agnitasCloudWebdavContext, username, password)
			authhandler = urllib.request.HTTPBasicAuthHandler(passwordManager)
			opener = urllib.request.build_opener(authhandler, urllib.request.HTTPSHandler(context = ssl.SSLContext(ssl.PROTOCOL_TLS)))
			downloadPageResponse = opener.open(Environment.agnitasCloudUrl + Environment.agnitasCloudWebdavContext)

			with open(downloadDestinationFilePath, "wb") as downloadDestinationFile:
				total_size = int(downloadPageResponse.info().get_all("Content-Length")[0].strip())
				bytes_read = 0
				chunk_size = 8192
				progressbarSize = 80
				print()
				print()
				while True:
					chunk = downloadPageResponse.read(chunk_size)
					bytes_read += len(chunk)

					if not chunk:
						break

					downloadDestinationFile.write(chunk)
					percentageDone = bytes_read / float(total_size)
					sys.stdout.write("\033[F") # Cursor up one line
					sys.stdout.write("\033[F") # Cursor up one line
					doneCharsSize = int((progressbarSize - 2) * percentageDone)
					todoCharsSize = (progressbarSize - 2) - doneCharsSize
					print("[" + ("*" * doneCharsSize) + " " * todoCharsSize + "]")
					progressString = "{:,}".format(bytes_read) + " of " + "{:,}".format(total_size) + " Bytes (" + "{0:.0f}".format(percentageDone * 100) + "%)"
					print(progressString + " " * (progressbarSize - len(progressString)))

				logfile.write("Downloaded to file '" + downloadDestinationFilePath + "'" + "\n")
			downloadPageResponse.close()
			downloadPageResponse = None
		except:
			if downloadPageResponse is not None:
				downloadPageResponse.close()
			if os.path.isfile(downloadDestinationFilePath):
				os.remove(downloadDestinationFilePath)
			errorText = "Download of update package failed"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			if EMTUtilities.isDebugMode():
				logging.exception(errorText)
				raise
			return
		print("Download finished")

		if EMTUtilities.hasRootPermissions():
			os.system("chown " + applicationUserName + " " + downloadDestinationFilePath)

		installFile(downloadDestinationFilePath, logfile, interactive)

		if releaseSubdirectoryName is None and os.path.isfile(downloadDestinationFilePath):
			os.remove(downloadDestinationFilePath)

		if alreadyOpenLogFile is None:
			if Environment.errors is None or len(Environment.errors) == 0:
				Environment.messages.append("Update package file '" + packageFilename + "' successfully deployed.\nFor logs see '" + os.path.realpath(logfile.name) + "'")
			else:
				Environment.messages.append("Update package file '" + packageFilename + "' deployed with errors.\nFor logs see '" + os.path.realpath(logfile.name) + "'")
	except urllib.error.URLError as e:
		errorText = "AGNITAS Cloud '" + Environment.agnitasCloudUrl + "' is not reachable"
		Environment.errors.append(errorText)
		if logfile is not None:
			logfile.write(errorText + "\n")
	except:
		errorText = "Error while deploying update package"
		Environment.errors.append(errorText)
		if logfile is not None:
			logfile.write(errorText + "\n")
		if EMTUtilities.isDebugMode():
			logging.exception(errorText)
			raise
	finally:
		if logfile is not None and alreadyOpenLogFile is None:
			logfile.write("Update ended at: " + datetime.datetime.now().strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			logfile.close()

def siteUpdateMenuAction(actionParameters):
	print(Environment.applicationName + " Update via AGNITAS Website")

	if not EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer:
		print()
		print(Colors.RED + "If you want to update the OpenEMM package, start OMT as user root" + Colors.DEFAULT)
		print()

	if Environment.isOpenEmmServer:
		print("Download and install latest packages (only openemm-package needs root permissions for OMT)? (N/y, Blank => Cancel):")
	else:
		print("Download and install latest runtime, frontend and backend packages? (N/y, Blank => Cancel):")
	choice = input(" > ")
	choice = choice.strip().lower()
	if choice.startswith("y") or choice.startswith("j"):
		installFileFromWebsite(True)

def installFileFromWebsite(interactive, alreadyOpenLogFile = None):
	if Environment.agnitasDownloadPathVersionInfo is None:
		Environment.errors.append("This is a non-generic version, which may not be updated via Agnitas website")
		return

	print()
	print("Starting " + Environment.applicationName + " Update ...")

	now = datetime.datetime.now()
	nowString = now.strftime("%Y-%m-%d_%H-%M-%S")
	logfile = None
	try:
		if Environment.isOpenEmmServer:
			applicationUserName = "openemm"
		elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
			applicationUserName = "console"
		elif Environment.isEmmRdirServer:
			applicationUserName = "rdir"
		elif Environment.isEmmMergerServer:
			applicationUserName = "merger"
		elif Environment.isEmmMailerServer:
			applicationUserName = "mailout"
		elif Environment.isEmmMailloopServer:
			applicationUserName = "mailloop"
		else:
			applicationUserName = "console"

		if EMTUtilities.hasRootPermissions():
			if not os.path.isdir("/root/release"):
				EMTUtilities.createDirectory("/root/release", "root")
			if not os.path.isdir("/root/release/log"):
				EMTUtilities.createDirectory("/root/release/log", "root")

			if alreadyOpenLogFile is None:
				logfilePath = "/root/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile
		else:
			if not os.path.isdir("/home/" + applicationUserName + "/release"):
				EMTUtilities.createDirectory("/home/" + applicationUserName + "/release", applicationUserName)
			if not os.path.isdir("/home/" + applicationUserName + "/release/log"):
				EMTUtilities.createDirectory("/home/" + applicationUserName + "/release/log", applicationUserName)

			if alreadyOpenLogFile is None:
				logfilePath = "/home/" + applicationUserName + "/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile

		versionInfo = EMTUtilities.downloadVersionInfo(Environment.agnitasDownloadPathVersionInfo)
		downloadFileUrlPaths = {}

		if versionInfo is not None:
			# Check for release upgrade
			if "new-release-backend-startup" in versionInfo and interactive:
				print(Colors.YELLOW + "\nA new release version is available: " + versionInfo["new-release-backend-startup"][0] + Colors.DEFAULT)
				print("Do you want to upgrade? (N/y, Blank => Cancel):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if choice.startswith("y") or choice.startswith("j"):
					logfile.write("Release version upgrade: " + versionInfo["new-release-backend-startup"][0] + " " + versionInfo["new-release-backend-startup"][1] + "\n")
					downloadFileUrlPaths["new-release-backend-startup"] = versionInfo["new-release-backend-startup"][1]
					print(Colors.YELLOW + "\nStarting release upgrade" + Colors.DEFAULT)
					downloadAndInstallNewLicense()
			elif "new-release-runtime" in versionInfo and interactive:
				print(Colors.YELLOW + "\nA new release version is available: " + versionInfo["new-release-runtime"][0] + Colors.DEFAULT)
				print("Do you want to upgrade? (N/y, Blank => Cancel):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if choice.startswith("y") or choice.startswith("j"):
					logfile.write("Release version upgrade: " + versionInfo["new-release-runtime"][0] + " " + versionInfo["new-release-runtime"][1] + "\n")
					downloadFileUrlPaths["new-release-runtime"] = versionInfo["new-release-runtime"][1]
					print(Colors.YELLOW + "\nStarting release upgrade" + Colors.DEFAULT)
					downloadAndInstallNewLicense()

			if len(downloadFileUrlPaths) == 0:
				# Application updates
				if "backend-startup" in versionInfo:
					if not EMTUtilities.hasRootPermissions():
						Environment.warnings.append("The application backend-startup must be updated by an user with root permission")
					else:
						downloadFileUrlPaths["backend-startup"] = versionInfo["backend-startup"][1]

				if "runtime" in versionInfo and (Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
					downloadFileUrlPaths["runtime"] = versionInfo["runtime"][1]

				if "java" in versionInfo and (Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
					downloadFileUrlPaths["java"] = versionInfo["java"][1]

				if "tomcat" in versionInfo and (Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
					downloadFileUrlPaths["tomcat"] = versionInfo["tomcat"][1]

				if ("tomcat-native-" + Environment.osVendor + Environment.osVersion) in versionInfo and (Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
					downloadFileUrlPaths["tomcat-native"] = versionInfo["tomcat-native-" + Environment.osVendor + Environment.osVersion][1]
				elif ("." in Environment.osVersion and ("tomcat-native-" + Environment.osVendor + Environment.osVersion[:Environment.osVersion.index(".")]) in versionInfo) and (Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
					downloadFileUrlPaths["tomcat-native"] = versionInfo["tomcat-native-" + Environment.osVendor + Environment.osVersion[:Environment.osVersion.index(".")]][1]
				elif ("tomcat-native-" + Environment.osVendor) in versionInfo and (Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
					downloadFileUrlPaths["tomcat-native"] = versionInfo["tomcat-native-" + Environment.osVendor][1]

				if Environment.isOpenEmmServer:
					if "complete" in versionInfo and EMTUtilities.hasRootPermissions():
						downloadFileUrlPaths["complete"] = versionInfo["complete"][1]
					elif "openemm" in versionInfo and EMTUtilities.hasRootPermissions():
						downloadFileUrlPaths["openemm"] = versionInfo["openemm"][1]
					else:
						if "frontend" in versionInfo:
							downloadFileUrlPaths["frontend"] = versionInfo["frontend"][1]

						if Environment.osVendor == "Suse" and "backend-suse" in versionInfo:
							downloadFileUrlPaths["backend-suse"] = versionInfo["backend-suse"][1]
						elif "backend" in versionInfo:
							downloadFileUrlPaths["backend"] = versionInfo["backend"][1]

					if "manual" in versionInfo and os.path.isdir("/home/openemm/release/manual"):
						# Only update manual, if it already exists on this server. No initial installation via Website updates.
						downloadFileUrlPaths["manual"] = versionInfo["manual"][1]
				else:
					if Environment.isEmmFrontendServer and "frontend-emm" in versionInfo:
						downloadFileUrlPaths["frontend-emm"] = versionInfo["frontend-emm"][1]
					elif Environment.isEmmFrontendServer and "emm" in versionInfo:
						downloadFileUrlPaths["emm"] = versionInfo["emm"][1]

					if Environment.isEmmStatisticsServer and "frontend-statistics" in versionInfo:
						downloadFileUrlPaths["frontend-statistics"] = versionInfo["frontend-statistics"][1]
					elif Environment.isEmmStatisticsServer and "statistics" in versionInfo:
						downloadFileUrlPaths["statistics"] = versionInfo["statistics"][1]

					if Environment.isEmmWebservicesServer and "frontend-webservices" in versionInfo:
						downloadFileUrlPaths["frontend-webservices"] = versionInfo["frontend-webservices"][1]
					elif Environment.isEmmWebservicesServer and "webservices" in versionInfo:
						downloadFileUrlPaths["webservices"] = versionInfo["webservices"][1]

					if Environment.isEmmConsoleRdirServer and "frontend-rdir" in versionInfo:
						downloadFileUrlPaths["frontend-rdir"] = versionInfo["frontend-rdir"][1]
					elif Environment.isEmmConsoleRdirServer and "rdir" in versionInfo:
						downloadFileUrlPaths["rdir"] = versionInfo["rdir"][1]
					if Environment.isEmmRdirServer and "frontend-rdir" in versionInfo:
						downloadFileUrlPaths["frontend-rdir"] = versionInfo["frontend-rdir"][1]
					elif Environment.isEmmRdirServer and "rdir" in versionInfo:
						downloadFileUrlPaths["rdir"] = versionInfo["rdir"][1]

					if Environment.isEmmFrontendServer and "backend-console" in versionInfo:
						downloadFileUrlPaths["backend-console"] = versionInfo["backend-console"][1]
					elif Environment.isEmmWebservicesServer and "backend-console" in versionInfo:
						downloadFileUrlPaths["backend-console"] = versionInfo["backend-console"][1]
					elif Environment.isEmmConsoleRdirServer and "backend-console" in versionInfo:
						downloadFileUrlPaths["backend-console"] = versionInfo["backend-console"][1]

					elif Environment.isEmmRdirServer and "backend-rdir" in versionInfo:
						downloadFileUrlPaths["backend-rdir"] = versionInfo["backend-rdir"][1]

					if Environment.isEmmMergerServer and "backend-merger" in versionInfo:
						if not EMTUtilities.hasRootPermissions():
							Environment.warnings.append("The application backend-merger must be updated by an user with root permission")
						else:
							downloadFileUrlPaths["backend-merger"] = versionInfo["backend-merger"][1]

					if Environment.isEmmMailerServer and "backend-mailer" in versionInfo:
						if not EMTUtilities.hasRootPermissions():
							Environment.warnings.append("The application backend-mailer must be updated by an user with root permission")
						else:
							downloadFileUrlPaths["backend-mailer"] = versionInfo["backend-mailer"][1]

					if Environment.isEmmMailloopServer and "backend-mailloop" in versionInfo:
						if not EMTUtilities.hasRootPermissions():
							Environment.warnings.append("The application backend-mailloop must be updated by an user with root permission")
						else:
							downloadFileUrlPaths["backend-mailloop"] = versionInfo["backend-mailloop"][1]

					if Environment.isEmmFrontendServer and "manual" in versionInfo:
						downloadFileUrlPaths["manual"] = versionInfo["manual"][1]

		if EMTUtilities.isDebugMode():
			print("downloadFileUrlPaths:")
			for downloadItemName, downloadFileUrlPath in downloadFileUrlPaths.items():
				print(str(downloadItemName) + ": " + str(downloadFileUrlPath))

		for downloadFileUrlPath in downloadFileUrlPaths.values():
			if Environment.errors is not None and len(Environment.errors) > 0:
				break
			elif downloadFileUrlPath is not None:
				logfile.write("Update package url: " + downloadFileUrlPath + "\n")

				downloadPageResponse = None
				try:
					downloadPageResponse = EMTUtilities.openUrlConnection(downloadFileUrlPath)
					effectiveFinalUrl = downloadPageResponse.geturl()
					_, params = cgi.parse_header(downloadPageResponse.headers.get("Content-Disposition", ""))

					if "filename" in params:
						packageFilename = params["filename"]
					else:
						packageFilename = effectiveFinalUrl[effectiveFinalUrl.rfind("/") + 1:]
				finally:
					if downloadPageResponse is not None:
						downloadPageResponse.close()

				logfile.write("Update package file: " + packageFilename + "\n")

				if "-runtime-" in packageFilename:
					applicationName = "runtime"
					releaseSubdirectoryName = "runtime"
					if Environment.isEmmRdirServer:
						currentVersion = Environment.rdirRuntimeVersion
					else:
						currentVersion = Environment.runtimeVersion
				elif "backend-console-" in packageFilename:
					applicationName = "backend-console"
					releaseSubdirectoryName = "backend"
					currentVersion = Environment.emmBackendVersion
				elif "backend-rdir-" in packageFilename:
					applicationName = "backend-rdir"
					releaseSubdirectoryName = "backend"
					currentVersion = Environment.rdirBackendVersion
				elif "-startup-" in packageFilename:
					applicationName = "backend-startup"
					releaseSubdirectoryName = ""
					currentVersion = Environment.startupBackendVersion
				elif "-merger-" in packageFilename:
					applicationName = "backend-merger"
					releaseSubdirectoryName = ""
					currentVersion = Environment.mergerBackendVersion
				elif "-mailer-" in packageFilename:
					applicationName = "backend-mailer"
					releaseSubdirectoryName = ""
					currentVersion = Environment.mailerBackendVersion
				elif "-mailloop-" in packageFilename:
					applicationName = "backend-mailloop"
					releaseSubdirectoryName = ""
					currentVersion = Environment.mailloopBackendVersion
				elif "-backend-" in packageFilename:
					applicationName = "backend"
					releaseSubdirectoryName = "backend"
					currentVersion = Environment.emmBackendVersion
				elif "-manual-" in packageFilename:
					applicationName = "manual"
					releaseSubdirectoryName = "manual"
					currentVersion = Environment.manualVersion
				elif "-emm-" in packageFilename or "-gui-" in packageFilename:
					applicationName = "gui"
					releaseSubdirectoryName = "emm"
					currentVersion = Environment.frontendVersion
				elif "-statistics-" in packageFilename:
					applicationName = "statistics"
					releaseSubdirectoryName = "statistics"
					currentVersion = Environment.statisticsVersion
				elif "-webservices-" in packageFilename:
					applicationName = "webservices"
					releaseSubdirectoryName = "webservices"
					currentVersion = Environment.webservicesVersion
				elif "-rdir-" in packageFilename:
					applicationName = "rdir"
					releaseSubdirectoryName = "rdir"
					if Environment.isEmmConsoleRdirServer:
						currentVersion = Environment.consoleRdirVersion
					else:
						currentVersion = Environment.rdirVersion
				elif "-tomcat-native-" in packageFilename:
					applicationName = "tomcat-native"
					releaseSubdirectoryName = None
					currentVersion = EMTUtilities.getTomcatNativeVersion(Environment.tomcatNative)
				elif "-tomcat-" in packageFilename:
					applicationName = "tomcat"
					releaseSubdirectoryName = None
					currentVersion = EMTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
				elif "-java-" in packageFilename:
					applicationName = "java"
					releaseSubdirectoryName = None
					currentVersion = EMTUtilities.getJavaVersion(Environment.javaHome)
				else:
					# Handle as complete package
					applicationName = "complete"
					releaseSubdirectoryName = None

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

				versionCompare = EMTUtilities.compareVersions(currentVersion, EMTUtilities.getVersionFromFilename(packageFilename))

				if versionCompare < 1:
					print()
					print("Package contains application type '" + getApplicationDisplayName(applicationName) + "' of version '" + EMTUtilities.normalizeVersion(EMTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(EMTUtilities.normalizeVersion(currentVersion)) + "'.")
					if interactive:
						if versionCompare == 0:
							print(Colors.YELLOW + "This is the same version." + Colors.DEFAULT)
						else:
							print(Colors.RED + "The update package is an older version!" + Colors.DEFAULT)
						print("Install anyway? (N/y, Blank => Cancel):")
						choice = input(" > ")
						choice = choice.strip().lower()
						if not (choice.startswith("y") or choice.startswith("j")):
							logfile.write("Declined installation of version '" + EMTUtilities.getVersionFromFilename(packageFilename) + "' for application '" + applicationName + "'\n")
							continue
					else:
						print("Obsolete update of " + getApplicationDisplayName(applicationName) + " is skipped.")
						continue
				elif releaseSubdirectoryName is not None and not os.path.isdir("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName):
					print()
					print("Package contains new application '" + getApplicationDisplayName(applicationName) + "'")
					if interactive:
						print("Initially install this application? (N/y, Blank => Cancel):")
						choice = input(" > ")
						choice = choice.strip().lower()
						if choice.startswith("y") or choice.startswith("j"):
							EMTUtilities.createDirectory("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName, applicationUserName)
						else:
							logfile.write("New application package type '" + applicationName + "' canceled" + "\n")
							continue
					else:
						continue
				elif applicationName == "tomcat-native":
					if Environment.tomcatNative is not None:
						print()
						print("Found update package '" + packageFilename + "' for application '" + getApplicationDisplayName(applicationName) + "' with version '" + EMTUtilities.normalizeVersion(EMTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(EMTUtilities.normalizeVersion(currentVersion)) + "'.")
						if interactive:
							print("Continue with update? (Y/n, Blank => Yes):")
							choice = input(" > ")
							choice = choice.strip().lower()
							if not (choice == "" or choice.startswith("y") or choice.startswith("j")):
								logfile.write("Update of '" + applicationName + "' canceled" + "\n")
								continue
					else:
						if interactive:
							print()
							print("Package contains new application '" + getApplicationDisplayName(applicationName) + "'")
							print("Initially install this application? (N/y, Blank => Cancel):")
							choice = input(" > ")
							choice = choice.strip().lower()
							if not (choice.startswith("y") or choice.startswith("j")):
								logfile.write("New application package type '" + applicationName + "' canceled" + "\n")
								continue
						else:
							continue
				else:
					print()
					print("Found update package '" + packageFilename + "' for application '" + getApplicationDisplayName(applicationName) + "' with version '" + EMTUtilities.normalizeVersion(EMTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(EMTUtilities.normalizeVersion(currentVersion)) + "'.")
					if interactive:
						print("Continue with update? (Y/n, Blank => Yes):")
						choice = input(" > ")
						choice = choice.strip().lower()
						if not (choice == "" or choice.startswith("y") or choice.startswith("j")):
							Environment.warnings.append("Update of '" + getApplicationDisplayName(applicationName) + "' canceled")
							logfile.write("Update of '" + applicationName + "' canceled" + "\n")
							continue
					else:
						if releaseSubdirectoryName is not None and len(releaseSubdirectoryName.strip()) > 0:
							cleanupOldVersions("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName, EMTUtilities.normalizeVersion(EMTUtilities.getVersionFromFilename(packageFilename)), EMTUtilities.normalizeVersion(currentVersion), logfile)

				if applicationName == "backend" \
					and not EMTUtilities.hasRootPermissions() \
					and not Environment.isEmmFrontendServer \
					and not Environment.isEmmConsoleRdirServer \
					and not Environment.isEmmWebservicesServer \
					and not Environment.isEmmStatisticsServer:
					errorText = "You need to start the program with root permissions (sudo) to update the backend application"
					Environment.errors.append(errorText)
					logfile.write(errorText + "\n")
					return

				if EMTUtilities.isBlank(releaseSubdirectoryName):
					downloadDestinationFilePath = "/tmp/" + packageFilename
				else:
					if not os.path.isdir("/home/" + applicationUserName + "/release"):
						EMTUtilities.createDirectory("/home/" + applicationUserName + "/release", applicationUserName)
						logfile.write("Created directory: /home/" + applicationUserName + "/release" + "\n")
					if not os.path.isdir("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName):
						EMTUtilities.createDirectory("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName, applicationUserName)
						logfile.write("Created directory: /home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "\n")
					downloadDestinationFilePath = "/home/" + applicationUserName + "/release/" + releaseSubdirectoryName + "/" + packageFilename

				# Clean up old update attempt left overs
				if os.path.isfile(downloadDestinationFilePath):
					os.remove(downloadDestinationFilePath)
					logfile.write("Removed already existing file '" + downloadDestinationFilePath + "'\n")

				# Download update file
				print("Download started")
				downloadPageResponse = None
				try:
					downloadPageResponse = EMTUtilities.openUrlConnection(downloadFileUrlPath)
					with open(downloadDestinationFilePath, "wb") as downloadDestinationFile:
						total_size = int(downloadPageResponse.info().get_all("Content-Length")[0].strip())
						bytes_read = 0
						chunk_size = 8192
						progressbarSize = 80
						print()
						print()
						while True:
							chunk = downloadPageResponse.read(chunk_size)
							bytes_read += len(chunk)

							if not chunk:
								break

							downloadDestinationFile.write(chunk)
							percentageDone = bytes_read / float(total_size)
							sys.stdout.write("\033[F") # Cursor up one line
							sys.stdout.write("\033[F") # Cursor up one line
							doneCharsSize = int((progressbarSize - 2) * percentageDone)
							todoCharsSize = (progressbarSize - 2) - doneCharsSize
							print("[" + ("*" * doneCharsSize) + " " * todoCharsSize + "]")
							progressString = "{:,}".format(bytes_read) + " of " + "{:,}".format(total_size) + " Bytes (" + "{0:.0f}".format(percentageDone * 100) + "%)"
							print(progressString + " " * (progressbarSize - len(progressString)))

						logfile.write("Downloaded to file '" + downloadDestinationFilePath + "'" + "\n")
					downloadPageResponse.close()
					downloadPageResponse = None
				except:
					if downloadPageResponse is not None:
						downloadPageResponse.close()
					if os.path.isfile(downloadDestinationFilePath):
						os.remove(downloadDestinationFilePath)
					errorText = "Download of update package failed"
					Environment.errors.append(errorText)
					logfile.write(errorText + "\n")
					if EMTUtilities.isDebugMode():
						logging.exception(errorText)
						raise
					return
				print("Download finished")

				installFile(downloadDestinationFilePath, logfile, interactive)

				if (applicationName == "complete" or applicationName == "frontend") and os.path.isfile(downloadDestinationFilePath):
					os.remove(downloadDestinationFilePath)

				if alreadyOpenLogFile is None:
					if Environment.errors is None or len(Environment.errors) == 0:
						Environment.messages.append("Update package file '" + packageFilename + "' successfully deployed.\nFor logs see '" + os.path.realpath(logfile.name) + "'")
					else:
						Environment.messages.append("Update package file '" + packageFilename + "' deployed with errors.\nFor logs see '" + os.path.realpath(logfile.name) + "'")

		if EMTUtilities.isBlank(Environment.getSystemUrl()) or Environment.getSystemUrl().strip() == "Unknown" and DbConnector.checkDbServiceAvailable() and DbConnector.checkDbStructureExists():
			Environment.errors.append("Basic configuration is missing. Please configure.")
			Environment.overrideNextMenu = Environment.configTableMenu
	except urllib.error.URLError as e:
		errorText = "AGNITAS Website '" + Environment.agnitasCloudUrl + "' is not reachable"
		Environment.errors.append(errorText)
		if logfile is not None:
			logfile.write(errorText + "\n")
	except:
		errorText = "Error while deploying update package (Log: " + os.path.realpath(logfile.name) + ")"
		Environment.errors.append(errorText)
		if logfile is not None:
			logfile.write(errorText + "\n")
		if EMTUtilities.isDebugMode():
			logging.exception(errorText)
			raise
	finally:
		if logfile is not None and alreadyOpenLogFile is None:
			logfile.write("Update ended at: " + datetime.datetime.now().strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			logfile.close()

def cloudUpdateMenuAction(actionParameters):
	print(Environment.applicationName + " Update via AGNITAS Cloud")

	if not EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer:
		print()
		print(Colors.RED + "If you want to update the OpenEMM package, start OMT as user root" + Colors.DEFAULT)
		print()

	print("Please enter update package link ID (Blank => Cancel):")
	packageUrl = input(" > ")

	if packageUrl != "":
		installFileFromCloud(packageUrl, True)

def fileUpdateMenuAction(actionParameters, alreadyOpenLogFile = None):
	print(Environment.applicationName + " Update from local file")

	if not EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer:
		print()
		print(Colors.RED + "If you want to update the OpenEMM package, start OMT as user root" + Colors.DEFAULT)
		print()

	print("Please enter update package file path (Blank => Cancel):")
	while True:
		packageFilePath = input(" > ")
		packageFilePath = packageFilePath.strip()
		if packageFilePath == "":
			return
		else:
			if packageFilePath.startswith("'") and packageFilePath.endswith("'"):
				packageFilePath = packageFilePath[1:-1]
			elif packageFilePath.startswith("\"") and packageFilePath.endswith("\""):
				packageFilePath = packageFilePath[1:-1]
			if not os.path.isfile(packageFilePath):
				Environment.errors.append("Invalid file path")
				return False
			else:
				break

	print()
	print("Starting " + Environment.applicationName + " Update ...")

	now = datetime.datetime.now()
	nowString = now.strftime("%Y-%m-%d_%H-%M-%S")
	logfile = None
	try:
		if Environment.isOpenEmmServer:
			applicationUserName = "openemm"
		elif Environment.isEmmRdirServer:
			applicationUserName = "rdir"
		elif Environment.isEmmMergerServer:
			applicationUserName = "merger"
		elif Environment.isEmmMailerServer:
			applicationUserName = "mailout"
		elif Environment.isEmmMailloopServer:
			applicationUserName = "mailloop"
		else:
			applicationUserName = "console"

		if EMTUtilities.hasRootPermissions():
			if not os.path.isdir("/root/release"):
				EMTUtilities.createDirectories("/root/release", "root")
			if not os.path.isdir("/root/release/log"):
				EMTUtilities.createDirectory("/root/release/log", "root")

			if alreadyOpenLogFile is None:
				logfilePath = "/root/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile
		else:
			if not os.path.isdir("/home/" + applicationUserName + "/release"):
				EMTUtilities.createDirectories("/home/" + applicationUserName + "/release", applicationUserName)
			if not os.path.isdir("/home/" + applicationUserName + "/release/log"):
				EMTUtilities.createDirectory("/home/" + applicationUserName + "/release/log", applicationUserName)

			if alreadyOpenLogFile is None:
				logfilePath = "/home/" + applicationUserName + "/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile

		logfile.write("Update package file: " + packageFilePath + "\n")

		packageFilename = os.path.basename(packageFilePath)

		if "-runtime-" in packageFilename:
			applicationName = "runtime"
			releaseSubdirectoryName = "runtime"
			currentVersion = Environment.runtimeVersion
		elif "backend-console-" in packageFilename:
			applicationName = "backend-console"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.emmBackendVersion
		elif "backend-rdir-" in packageFilename:
			applicationName = "backend-rdir"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.rdirBackendVersion
		elif "-startup-" in packageFilename:
			applicationName = "backend-startup"
			releaseSubdirectoryName = ""
			currentVersion = Environment.startupBackendVersion
		elif "-merger-" in packageFilename:
			applicationName = "backend-merger"
			releaseSubdirectoryName = ""
			currentVersion = Environment.mergerBackendVersion
		elif "-mailer-" in packageFilename:
			applicationName = "backend-mailer"
			releaseSubdirectoryName = ""
			currentVersion = Environment.mailerBackendVersion
		elif "-mailloop-" in packageFilename:
			applicationName = "backend-mailloop"
			releaseSubdirectoryName = ""
			currentVersion = Environment.mailloopBackendVersion
		elif "-backend-" in packageFilename:
			applicationName = "backend"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.emmBackendVersion
		elif "-manual-" in packageFilename:
			applicationName = "manual"
			releaseSubdirectoryName = "manual"
			currentVersion = Environment.manualVersion
		elif "-emm-" in packageFilename or "-gui-" in packageFilename:
			applicationName = "gui"
			releaseSubdirectoryName = "emm"
			currentVersion = Environment.frontendVersion
		elif "-statistics-" in packageFilename:
			applicationName = "statistics"
			releaseSubdirectoryName = "statistics"
			currentVersion = Environment.statisticsVersion
		elif "-webservices-" in packageFilename:
			applicationName = "webservices"
			releaseSubdirectoryName = "webservices"
			currentVersion = Environment.webservicesVersion
		elif "-rdir-" in packageFilename:
			applicationName = "rdir"
			releaseSubdirectoryName = "rdir"
			if Environment.isEmmConsoleRdirServer:
				currentVersion = Environment.consoleRdirVersion
			else:
				currentVersion = Environment.rdirVersion
		elif "-tomcat-native-" in packageFilename:
			applicationName = "tomcat-native"
			releaseSubdirectoryName = None
			currentVersion = EMTUtilities.getTomcatNativeVersion(Environment.tomcatNative)
		elif "-tomcat-" in packageFilename:
			applicationName = "tomcat"
			releaseSubdirectoryName = None
			currentVersion = EMTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
		elif "-java-" in packageFilename:
			applicationName = "java"
			releaseSubdirectoryName = None
			currentVersion = EMTUtilities.getJavaVersion(Environment.javaHome)
		else:
			# Handle as complete package
			applicationName = "complete"
			releaseSubdirectoryName = None

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

		versionCompare = EMTUtilities.compareVersions(currentVersion, EMTUtilities.getVersionFromFilename(packageFilename))
		if versionCompare < 1:
			print()
			print("Package contains application type '" + getApplicationDisplayName(applicationName) + "' of Version '" + EMTUtilities.normalizeVersion(EMTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(EMTUtilities.normalizeVersion(currentVersion)) + "'.")
			if versionCompare == 0:
				print(Colors.YELLOW + "This is the same version." + Colors.DEFAULT)
			else:
				print(Colors.RED + "The update package is an older version!" + Colors.DEFAULT)
			print("Install anyway? (N/y, Blank => Cancel):")
			choice = input(" > ")
			choice = choice.strip().lower()
			if not (choice.startswith("y") or choice.startswith("j")):
				logfile.write("Declined installation of old version '" + EMTUtilities.getVersionFromFilename(packageFilename) + "' of application type '" + applicationName + "'\n")
				return

		elif releaseSubdirectoryName is not None and not os.path.isdir("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName):
			print()
			print("Package contains new application '" + getApplicationDisplayName(applicationName) + "'")
			print("Initially install this application? (N/y, Blank => Cancel):")
			choice = input(" > ")
			choice = choice.strip().lower()
			if choice.startswith("y") or choice.startswith("j"):
				EMTUtilities.createDirectory("/home/" + applicationUserName + "/release/" + releaseSubdirectoryName, applicationUserName)
			else:
				Environment.errors.append("Unexpected update package type '" + getApplicationDisplayName(applicationName) + "'")
				logfile.write("Unexpected update package type '" + applicationName + "'" + "\n")
				return
		else:
			print()
			print("Found update package '" + packageFilename + "' for application '" + getApplicationDisplayName(applicationName) + "' with version '" + EMTUtilities.normalizeVersion(EMTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(EMTUtilities.normalizeVersion(currentVersion)) + "'.")
			print("Continue with update? (Y/n, Blank => Yes):")
			choice = input(" > ")
			choice = choice.strip().lower()
			if not (choice == "" or choice.startswith("y") or choice.startswith("j")):
				errorText = "Update canceled"
				Environment.errors.append(errorText)
				logfile.write(errorText + "\n")
				return

		if applicationName == "backend" \
			and not EMTUtilities.hasRootPermissions() \
			and not Environment.isEmmFrontendServer \
			and not Environment.isEmmConsoleRdirServer \
			and not Environment.isEmmWebservicesServer \
			and not Environment.isEmmStatisticsServer:
			errorText = "You need to start the program with root permissions (sudo) to update the backend application"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		installFile(packageFilePath, logfile, True)

		if alreadyOpenLogFile is None:
			if Environment.errors is None or len(Environment.errors) == 0:
				Environment.messages.append("Update package file '" + packageFilename + "' successfully deployed.\nFor logs see '" + os.path.realpath(logfile.name) + "'")
			else:
				Environment.messages.append("Update package file '" + packageFilename + "' deployed with errors.\nFor logs see '" + os.path.realpath(logfile.name) + "'")
	except urllib.error.URLError as e:
		errorText = "AGNITAS Cloud '" + Environment.agnitasCloudUrl + "' is not reachable"
		Environment.errors.append(errorText)
		logfile.write(errorText + "\n")
	except:
		errorText = "Error while deploying update package"
		Environment.errors.append(errorText)
		logfile.write(errorText + "\n")
		if EMTUtilities.isDebugMode():
			logging.exception(errorText)
			raise
	finally:
		if logfile is not None and alreadyOpenLogFile is None:
			logfile.write("Update ended at: " + datetime.datetime.now().strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			logfile.close()

def executeUnattendedUpdate():
	updateFileParamIndex = sys.argv.index("-update") + 1
	if len(sys.argv) >= updateFileParamIndex + 1:
		updateFile = sys.argv[updateFileParamIndex]
		if updateFile == "-debug":
			updateFile = None
	else:
		updateFile = None

	logfile = None
	try:
		if Environment.isOpenEmmServer:
			applicationUserName = "openemm"
		elif Environment.isEmmRdirServer:
			applicationUserName = "rdir"
		elif Environment.isEmmMergerServer:
			applicationUserName = "merger"
		elif Environment.isEmmMailerServer:
			applicationUserName = "mailout"
		elif Environment.isEmmMailloopServer:
			applicationUserName = "mailloop"
		else:
			applicationUserName = "console"

		now = datetime.datetime.now()
		nowString = now.strftime("%Y-%m-%d_%H-%M-%S")

		if EMTUtilities.hasRootPermissions():
			if not os.path.isdir("/root/release"):
				EMTUtilities.createDirectory("/root/release", "root")
			if not os.path.isdir("/root/release/log"):
				EMTUtilities.createDirectory("/root/release/log", "root")
			logfilePath = "/root/release/log/update_" + nowString + ".log"

			logfile = open(logfilePath, "w", encoding="UTF-8")
			logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
		else:
			if not os.path.isdir("/home/" + applicationUserName + "/release"):
				EMTUtilities.createDirectory("/home/" + applicationUserName + "/release", applicationUserName)
			if not os.path.isdir("/home/" + applicationUserName + "/release/log"):
				EMTUtilities.createDirectory("/home/" + applicationUserName + "/release/log", applicationUserName)
			logfilePath = "/home/" + applicationUserName + "/release/log/update_" + nowString + ".log"

			logfile = open(logfilePath, "w", encoding="UTF-8")
			logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")

		if EMTUtilities.isBlank(updateFile):
			logfile.write("Update from website\n")
			installFileFromWebsite(False, logfile)
		elif not os.path.isfile(updateFile):
			logfile.write("Update with package file from cloud: " + updateFile + "\n")
			installFileFromCloud(updateFile, False, logfile)
		else:
			logfile.write("Update with local package file: " + updateFile + "\n")
			installFile(updateFile, logfile, False)

	except:
		errorText = "Error while deploying update package"
		Environment.errors.append(errorText)
		if logfile is not None:
			logfile.write(errorText + "\n")
		if EMTUtilities.isDebugMode():
			logging.exception(errorText)
			raise
	finally:
		if Environment.errors is not None and len(Environment.errors) > 0:
			logfile.write("Update had errors\n")
		elif Environment.rebootNeeded:
			logfile.write("Restarting application\n")

		if logfile is not None:
			logfile.write("Update ended at: " + datetime.datetime.now().strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			logfile.close()

	if Environment.errors is not None and len(Environment.errors) > 0:
		errorsOccurred = True
	else:
		errorsOccurred = False
		if Environment.messages is not None and len(Environment.messages) > 0:
			for message in Environment.messages:
				print(message + "\n")
		if Environment.warnings is not None and len(Environment.warnings) > 0:
			for warning in Environment.warnings:
				print("WARNING: " + warning + "\n")
		if Environment.errors is not None and len(Environment.errors) > 0:
			for error in Environment.errors:
				EMTUtilities.printError("ERROR: " + error + "\n")
	if logfilePath is not None:
		print("Log: " + logfilePath)

	if errorsOccurred:
		sys.exit(1)
	else:
		if Environment.rebootNeeded:
			print("Restarting application\n")
			SupplementalMenu.restartApplication()

		sys.exit(0)

def cleanupOldVersions(releaseDirectoryPath, updateVersion, currentVersion, logfile = None):
	numberOfMajorMinorVersionsToKeep = 2
	updateVersionHotfixVersionsToKeep = 2

	if os.path.isdir(releaseDirectoryPath):
		availableVersionsToDelete = []
		for directoryOrFileName in os.listdir(releaseDirectoryPath):
			version = EMTUtilities.getVersionFromFilename(directoryOrFileName)
			if version is not None and (currentVersion is None or EMTUtilities.compareVersions(version, currentVersion) != 0) and version not in availableVersionsToDelete:
				availableVersionsToDelete.append(version)
		availableVersionsToDelete.sort()

		availableMajorMinorVersions = getAvailableMajorMinorVersions(availableVersionsToDelete)
		availableMajorMinorVersions.sort()
		availableMajorMinorVersions = list(reversed(availableMajorMinorVersions))

		for index in range(0, numberOfMajorMinorVersionsToKeep):
			if len(availableMajorMinorVersions) >= index + 1:
				maximumVersion = getMaximumVersionForMajorMinorVersion(availableMajorMinorVersions[index], availableVersionsToDelete)
				if maximumVersion is not None:
					availableVersionsToDelete.remove(maximumVersion)

		for index in range(0, updateVersionHotfixVersionsToKeep):
			maximumMajorMinorMicroVersion = getMaximumVersionForMajorMinorMicroVersion(updateVersion, availableVersionsToDelete)
			if maximumMajorMinorMicroVersion is not None:
				availableVersionsToDelete.remove(maximumMajorMinorMicroVersion)

		for directoryOrFileName in os.listdir(releaseDirectoryPath):
			version = EMTUtilities.getVersionFromFilename(directoryOrFileName)
			absolutePath = releaseDirectoryPath + "/" + directoryOrFileName
			if version is not None and version in availableVersionsToDelete:
				print("Cleanup of old version '" + version + "': " + absolutePath + "\n")
				if logfile is not None:
					logfile.write("Cleanup of old version '" + version + "': " + absolutePath + "\n")
				if os.path.isdir(absolutePath):
					shutil.rmtree(absolutePath)
				else:
					os.remove(absolutePath)

def getAvailableMajorMinorVersions(versionList):
	majorMinorVersions = []
	for version in versionList:
		versionParts = version.split(".")
		if len(versionParts) >= 2:
			majorMinorVersion = versionParts[0] + "." + versionParts[1]
			if not majorMinorVersion in majorMinorVersions:
				majorMinorVersions.append(majorMinorVersion)
	return majorMinorVersions

def getMaximumVersionForMajorMinorVersion(majorMinorVersion, versionList):
	versionList.sort()
	versionList = list(reversed(versionList))

	maximumMajorMinorVersion = None
	majorMinorVersionParts = majorMinorVersion.split(".")
	if len(majorMinorVersionParts) >= 2:
		for version in versionList:
			versionParts = version.split(".")
			if len(versionParts) >= 2 and int(versionParts[0]) == int(majorMinorVersionParts[0]) and int(versionParts[1]) == int(majorMinorVersionParts[1]):
				if maximumMajorMinorVersion is None or EMTUtilities.compareVersions(version, maximumMajorMinorVersion) < 0:
					maximumMajorMinorVersion = version
	return maximumMajorMinorVersion

def getMaximumVersionForMajorMinorMicroVersion(majorMinorMicroVersion, versionList):
	versionList.sort()
	versionList = list(reversed(versionList))

	maximumMajorMinorVersion = None
	majorMinorMicroVersionParts = majorMinorMicroVersion.split(".")
	if len(majorMinorMicroVersionParts) >= 3:
		for version in versionList:
			versionParts = version.split(".")
			if len(versionParts) >= 3 and int(versionParts[0]) == int(majorMinorMicroVersionParts[0]) and int(versionParts[1]) == int(majorMinorMicroVersionParts[1]) and int(versionParts[2]) == int(majorMinorMicroVersionParts[2]):
				if maximumMajorMinorVersion is None or EMTUtilities.compareVersions(version, maximumMajorMinorVersion) < 0:
					maximumMajorMinorVersion = version
	return maximumMajorMinorVersion

def downloadAndInstallNewLicense():
	licenseProperties = License.License.readLicenseValues()
	if "licenseUrl" in licenseProperties and EMTUtilities.isNotBlank(licenseProperties.get("licenseUrl")):
		licenseUrl = licenseProperties.get("licenseUrl")

		if not EMTUtilities.checkDownloadFileIsAvailable(licenseUrl):
			print("LicenseUrl: " + licenseUrl + " (not reachable)")
			return
		else:
			if EMTUtilities.debugMode:
				print("LicenseUrl: " + licenseUrl)

			downloadDestinationFilePath = "/tmp/emm_license.zip"
			if os.path.isfile(downloadDestinationFilePath):
				os.remove(downloadDestinationFilePath)

			downloadPageResponse = None
			try:
				downloadPageResponse = EMTUtilities.openUrlConnection(licenseUrl)
				with open(downloadDestinationFilePath, "wb") as downloadDestinationFile:
					chunk_size = 8192
					while True:
						chunk = downloadPageResponse.read(chunk_size)

						if not chunk:
							break

						downloadDestinationFile.write(chunk)
			except:
				errorText = "Download of new license file failed"
				if EMTUtilities.debugMode:
					logging.exception(errorText + "\nUrl: " + str(licenseUrl))
					raise
				else:
					raise Exception(errorText)
			finally:
				if downloadPageResponse is not None:
					downloadPageResponse.close()

			if os.path.isfile(downloadDestinationFilePath):
				with tempfile.TemporaryDirectory() as licenseTempDirectory:
					if downloadDestinationFilePath.endswith(".zip"):
						EMTUtilities.unzipFile(downloadDestinationFilePath, licenseTempDirectory)
					elif downloadDestinationFilePath.endswith(".tar.gz"):
						EMTUtilities.unTarGzFile(downloadDestinationFilePath, licenseTempDirectory)

					if not os.path.isfile(licenseTempDirectory + "/emm.license.xml") or not os.path.isfile(licenseTempDirectory + "/emm.license.xml.sig"):
						Environment.errors.append("Given license file archive does not contain expected files (emm.license.xml, emm.license.xml.sig)")
					else:
						with open(licenseTempDirectory + "/emm.license.xml", "r", encoding="UTF-8") as licenseDataFileHandle:
							licenseData = licenseDataFileHandle.read()
						with open(licenseTempDirectory + "/emm.license.xml.sig", "rb") as licenseSignatureFileHandle:
							licenseSignature = licenseSignatureFileHandle.read()

						License.updateLicense(licenseData, licenseSignature)

						DbConnector.update("INSERT INTO server_command_tbl(command, server_name, execution_date, admin_id, description, timestamp) VALUES ('RELOAD_LICENSE_DATA', 'ALL', CURRENT_TIMESTAMP, 1, 'New license data uploaded by Maintenance Tool', CURRENT_TIMESTAMP)")

						Environment.messages.append("Successfully installed new license file")

			if os.path.isfile(downloadDestinationFilePath):
				os.remove(downloadDestinationFilePath)
