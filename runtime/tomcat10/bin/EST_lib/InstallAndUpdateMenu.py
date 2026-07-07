import re
import sys
import os
import pwd
import time
import ssl
import datetime
import getpass
import shutil
import logging
import tempfile
import urllib.request, urllib.error, urllib.parse
from urllib.parse import urlencode

from EST_lib.Environment import Environment
from EST_lib.License import License
from EST_lib import Colors
from EST_lib import DbConnector
from EST_lib import ESTUtilities
from EST_lib import FrontendInstaller
from EST_lib import FrontendRuntimeInstaller
from EST_lib import SupplementalMenu
from EST_lib.ESTUtilities import restart, parse_header

def installFile(packageFilePath, logfile, interactive):
	logfile.write("Installing \"" + packageFilePath + "\"\n")

	packageFilename = os.path.basename(packageFilePath)

	if "backend-runtime-" in packageFilename or "-startup-" in packageFilename:
		applicationName = "backend-runtime"
		releaseSubdirectoryName = ""
	elif "-runtime-" in packageFilename:
		applicationName = "runtime"
		releaseSubdirectoryName = "runtime"
	elif "backend-one-" in packageFilename:
		applicationName = "backend-one"
		releaseSubdirectoryName = "backend"
	elif "backend-console-" in packageFilename:
		applicationName = "backend-console"
		releaseSubdirectoryName = "backend"
	elif "backend-rdir-" in packageFilename:
		applicationName = "backend-rdir"
		releaseSubdirectoryName = "backend"
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
	elif "-tomcat-" in packageFilename:
		applicationName = "tomcat"
		releaseSubdirectoryName = None

		if Environment.catalinaHome is not None and os.path.isdir(ESTUtilities.getParentPath(Environment.catalinaHome)) and ESTUtilities.isWritable(ESTUtilities.getParentPath(Environment.catalinaHome)):
			installBaseDirectoryPath = ESTUtilities.getParentPath(Environment.catalinaHome)
		elif os.path.isdir(Environment.optDefaultPath) and ESTUtilities.isWritable(Environment.optDefaultPath):
			installBaseDirectoryPath = Environment.optDefaultPath
		else:
			installBaseDirectoryPath = Environment.optDefaultPathSecondary

			if not os.path.isdir(installBaseDirectoryPath):
				os.mkdir(installBaseDirectoryPath)
				if ESTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
					os.chown(installBaseDirectoryPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)

			if not ESTUtilities.isWritable(installBaseDirectoryPath):
				errorText = "The Tomcat installation directory '" + installBaseDirectoryPath + "' is not writable for the current user '" + Environment.username + "'. So only users with root permissions (sudo) may install or update Tomcat there.\nPlease restart the tool with root permissions (sudo) for installation or update there."
				Environment.errors.append(errorText)
				logfile.write(errorText + "\n")
				return

		Environment.catalinaHome = installBaseDirectoryPath + "/tomcat"
	elif "-java-" in packageFilename:
		applicationName = "java"
		releaseSubdirectoryName = None

		if Environment.javaHome is not None and os.path.isdir(ESTUtilities.getParentPath(Environment.javaHome)) and ESTUtilities.isWritable(ESTUtilities.getParentPath(Environment.javaHome)):
			installBaseDirectoryPath = ESTUtilities.getParentPath(Environment.javaHome)
		elif os.path.isdir(Environment.optDefaultPath) and ESTUtilities.isWritable(Environment.optDefaultPath):
			installBaseDirectoryPath = Environment.optDefaultPath
		else:
			installBaseDirectoryPath = Environment.optDefaultPathSecondary

			if not os.path.isdir(installBaseDirectoryPath):
				os.mkdir(installBaseDirectoryPath)
				if ESTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
					os.chown(installBaseDirectoryPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)

			if not ESTUtilities.isWritable(installBaseDirectoryPath):
				errorText = "The JAVA installation directory '" + installBaseDirectoryPath + "' is not writable for the current user '" + Environment.username + "'. So only users with root permissions (sudo) may install or update JAVA there.\nPlease restart the tool with root permissions (sudo) for installation or update there."
				Environment.errors.append(errorText)
				logfile.write(errorText + "\n")
				return

		Environment.javaHome = installBaseDirectoryPath + "/java"
	else:
		# Handle as complete package
		# Extract files in /tmp/
		now = datetime.datetime.now()
		nowString = now.strftime("%Y-%m-%d_%H-%M-%S")
		temporaryExtractionDirectory = "/tmp/Emm_" + nowString
		ESTUtilities.createDirectory(temporaryExtractionDirectory)
		ESTUtilities.unTarGzFile(packageFilePath, temporaryExtractionDirectory)

		# Apply installation order
		frontendFile = None
		statisticsFile = None
		webservicesFile = None
		rdirFile = None
		backendOneFile = None
		backendConsoleFile = None
		backendRdirFile = None
		backendRuntimeFile = None
		backendMergerFile = None
		backendMailerFile = None
		backendMailloopFile = None
		backendGlobalFile = None

		updateFilesInInstallOrder = []
		for listedFileName in os.listdir(temporaryExtractionDirectory):
			if "backend-runtime-" in listedFileName or "-startup-" in listedFileName:
				backendRuntimeFile = listedFileName
			elif frontendFile is None and ("-emm-" in listedFileName or "-gui-" in listedFileName):
				frontendFile = listedFileName
			elif statisticsFile is None and "-statistics-" in listedFileName:
				statisticsFile = listedFileName
			elif webservicesFile is None and "-webservices-" in listedFileName:
				webservicesFile = listedFileName
			elif rdirFile is None and "-rdir-" in listedFileName:
				rdirFile = listedFileName
			elif listedFileName.startswith("backend-one-"):
				backendOneFile = listedFileName
			elif listedFileName.startswith("backend-console-"):
				backendConsoleFile = listedFileName
			elif listedFileName.startswith("backend-rdir-"):
				backendRdirFile = listedFileName
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
		if backendOneFile is not None:
			updateFilesInInstallOrder.append(backendOneFile)
		if backendConsoleFile is not None:
			updateFilesInInstallOrder.append(backendConsoleFile)
		if backendRdirFile is not None:
			updateFilesInInstallOrder.append(backendRdirFile)
		if backendRuntimeFile is not None:
			updateFilesInInstallOrder.append(backendRuntimeFile)
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
	elif Environment.isEmmRdirServer and (applicationName == "rdir" or applicationName == "backend-runtime"):
		applicationUserName = Environment.getRdirApplicationUserName()
	elif Environment.isEmmMergerServer and (applicationName == "backend-merger" or applicationName == "backend-runtime"):
		applicationUserName = "merger"
	elif Environment.isEmmMailerServer and (applicationName == "backend-mailer" or applicationName == "backend-runtime"):
		applicationUserName = "mailout"
	elif Environment.isEmmMailloopServer and (applicationName == "backend-mailloop" or applicationName == "backend-runtime"):
		applicationUserName = "mailloop"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()

	if releaseSubdirectoryName is not None:
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName):
			if interactive:
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

		if Environment.isOpenEmmServer:
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/release"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("openemm") + "/release", applicationUserName)
				logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory("openemm") + "/release" + "\n")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/release/" + releaseSubdirectoryName):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("openemm") + "/release/" + releaseSubdirectoryName, "openemm")
				logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory("openemm") + "/release/" + releaseSubdirectoryName + "\n")
			expectedFilePath = ESTUtilities.getUserHomeDirectory("openemm") + "/release/" + releaseSubdirectoryName + "/" + packageFilename
			installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory("openemm") + "/release/" + releaseSubdirectoryName
		else:
			if (Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer) and applicationName == "runtime":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName, Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = None
			elif Environment.isEmmRdirServer and applicationName == "runtime":#
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release", Environment.getRdirApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/" + releaseSubdirectoryName):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/" + releaseSubdirectoryName, Environment.getRdirApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = None
			elif Environment.isEmmFrontendServer and applicationName == "gui":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName, Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName
			elif Environment.isEmmFrontendServer and applicationName == "manual":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName, Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName
			elif Environment.isEmmStatisticsServer and applicationName == "statistics":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName, Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName
			elif Environment.isEmmWebservicesServer and applicationName == "webservices":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName, Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName
			elif Environment.isEmmRdirServer and applicationName == "rdir":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release", Environment.getRdirApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/" + releaseSubdirectoryName):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/" + releaseSubdirectoryName, Environment.getRdirApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/" + releaseSubdirectoryName
			elif Environment.isEmmConsoleRdirServer and applicationName == "rdir":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName, Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName + "/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/" + releaseSubdirectoryName
			elif applicationName == "backend-runtime":
				if not os.path.isdir("/root/release"):
					ESTUtilities.createDirectory("/root/release", "root")
					logfile.write("Created directory: /root/release" + "\n")
				expectedFilePath = "/root/release/" + packageFilename
				installBaseDirectoryPath = "/root/release"
			elif Environment.isEmmMergerServer and applicationName == "backend-merger":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory("merger") + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("merger") + "/release", "merger")
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory("merger") + "/release" + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory("merger") + "/release/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory("merger") + "/release"
			elif Environment.isEmmMailerServer and applicationName == "backend-mailer":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory("mailout") + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("mailout") + "/release", "mailout")
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory("mailout") + "/release" + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory("mailout") + "/release/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory("mailout") + "/release"
			elif Environment.isEmmMailloopServer and applicationName == "backend-mailloop":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory("mailloop") + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("mailloop") + "/release", "mailloop")
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory("mailloop") + "/release" + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory("mailloop") + "/release/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory("mailloop") + "/release"
			elif Environment.isEmmUnifiedServer and applicationName == "backend-one":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend" + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend"
			elif (Environment.isEmmFrontendServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer) and applicationName == "backend-console":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend", Environment.getFrontendApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend" + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/release/backend"
			elif Environment.isEmmRdirServer and applicationName == "backend-rdir":
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release", Environment.getRdirApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release" + "\n")
				if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend", Environment.getRdirApplicationUserName())
					logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend" + "\n")
				expectedFilePath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend/" + packageFilename
				installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/release/backend"
			else:
				# Application is not needed for this server
				return
		updatedApplicationDirectoryPath = expectedFilePath.replace(".tar.gz", "")
	else:
		expectedFilePath = None
		if not os.path.isdir(installBaseDirectoryPath):
			os.mkdir(installBaseDirectoryPath)
			if ESTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
				os.chown(installBaseDirectoryPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)
		updatedApplicationDirectoryPath = installBaseDirectoryPath + "/" + packageFilename.replace(".tar.gz", "")

	if installBaseDirectoryPath is not None and not os.path.isdir(installBaseDirectoryPath):
		ESTUtilities.createDirectory(installBaseDirectoryPath, applicationUserName)

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
		ESTUtilities.unTarGzFile(packageFilePath, installBaseDirectoryPath)
		if ESTUtilities.hasRootPermissions():
			if Environment.isOpenEmmServer:
				ESTUtilities.chownRecursive(installBaseDirectoryPath, "openemm")
			else:
				ESTUtilities.chownRecursive(installBaseDirectoryPath, "root", "agnitas")
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + installBaseDirectoryPath + "'" + "\n")

		if os.path.islink(Environment.javaHome):
			os.unlink(Environment.javaHome)
			ESTUtilities.createLink(updatedApplicationDirectoryPath, Environment.javaHome)
			logfile.write("Created new application link '" + Environment.javaHome + "'\n")
			Environment.rebootNeeded = True
		elif os.path.isfile(Environment.javaHome) or os.path.isdir(Environment.javaHome):
			errorText = "Cannot update JAVA in '" + Environment.javaHome + "'"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
		else:
			Environment.javaHome = os.path.abspath(installBaseDirectoryPath + "/java")
			ESTUtilities.createLink(updatedApplicationDirectoryPath, Environment.javaHome)
			logfile.write("Created new application link '" + Environment.javaHome + "'\n")
			Environment.rebootNeeded = True
		if os.path.isfile(packageFilePath):
			os.remove(packageFilePath)
	elif applicationName == "tomcat":
		# Extract targz-file (tomcat package contains subdirectory with same name as file)
		ESTUtilities.unTarGzFile(packageFilePath, installBaseDirectoryPath)
		if ESTUtilities.hasRootPermissions():
			if Environment.isOpenEmmServer:
				ESTUtilities.chownRecursive(installBaseDirectoryPath, "openemm")
			else:
				ESTUtilities.chownRecursive(installBaseDirectoryPath, "root", "agnitas")
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + installBaseDirectoryPath + "'" + "\n")

		if os.path.islink(Environment.catalinaHome):
			os.unlink(Environment.catalinaHome)
			ESTUtilities.createLink(updatedApplicationDirectoryPath, Environment.catalinaHome)
			logfile.write("Created new application link '" + Environment.catalinaHome + "'\n")
			Environment.rebootNeeded = True
		elif os.path.isfile(Environment.catalinaHome) or os.path.isdir(Environment.catalinaHome):
			errorText = "Cannot update tomcat in '" + Environment.catalinaHome + "'"
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
		else:
			Environment.catalinaHome = os.path.abspath(installBaseDirectoryPath + "/tomcat")
			ESTUtilities.createLink(updatedApplicationDirectoryPath, Environment.catalinaHome)
			logfile.write("Created new application link '" + Environment.catalinaHome + "'\n")
			Environment.rebootNeeded = True
		if os.path.isfile(packageFilePath):
			os.remove(packageFilePath)
	elif applicationName == "runtime":
		FrontendRuntimeInstaller.installFile(interactive, logfile, applicationUserName, applicationName, updatedApplicationDirectoryPath, packageFilePath)
	elif applicationName == "backend-runtime":
		newBackendVersion = ESTUtilities.getVersionFromFilename(packageFilename)
		if newBackendVersion is None:
			errorText = "Invalid backend package file name: " + packageFilename
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Remove existing older extracted directory of same version
		if os.path.isdir("/root/release/V" + newBackendVersion):
			shutil.rmtree("/root/release/V" + newBackendVersion)

		# Extract targz-file for backend
		ESTUtilities.unTarGzFileAsRoot(packageFilePath, "/root/release/")

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
		ESTUtilities.createLink("V" + newBackendVersion, "/root/release/current", "root")

		# Create basic symlink only on initial installation
		if not os.path.islink("/root/bin") and not os.path.isdir("/root/bin") and os.path.isdir("/root/release/current/bin"):
			ESTUtilities.createLink("/root/release/current/bin", "/root/bin", "root")

		logfile.write("Created new backend symlinks\n")

		if interactive:
			print(Colors.YELLOW + "To let the new version take effect, you must exit and restart this program!" + Colors.DEFAULT)
			print("Do you want to exit and restart now? (Y/n, Blank => Exit):")
			answer = input(" > ").lower().strip()
			if answer == "" or answer.startswith("y") or answer.startswith("j"):
				restart(Environment.scriptFilePath, *sys.argv)
		else:
			print(Colors.YELLOW + "Restart for Update..." + Colors.DEFAULT)
			restart(Environment.scriptFilePath, *sys.argv)
	elif applicationName == "backend-merger" or applicationName == "backend-mailer" or applicationName == "backend-mailloop":
		newBackendVersion = ESTUtilities.getVersionFromFilename(packageFilename)
		if newBackendVersion is None:
			errorText = "Invalid backend package file name: " + packageFilename
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Remove existing older extracted directory of same version
		if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/V" + newBackendVersion):
			shutil.rmtree(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/V" + newBackendVersion)

		# Extract targz-file for backend
		if Environment.username == applicationUserName:
			ESTUtilities.unTarGzFile(packageFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/")
		else:
			ESTUtilities.unTarGzFileAsRoot(packageFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/")
		ESTUtilities.unTarGzFileAsRoot(packageFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/")
		ESTUtilities.chownRecursive(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/", applicationUserName, get_group_from_owner = True)

		updatedApplicationDirectoryPath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/V" + newBackendVersion

		if not os.path.isdir(updatedApplicationDirectoryPath):
			Environment.errors.append("Extraction of updatePackageFile '" + packageFilePath + "' was not successful")
			logfile.write("Extraction of updatePackageFile '" + packageFilePath + "' was not successful in path '" + updatedApplicationDirectoryPath + "'" + "\n")
			return

		os.remove(packageFilePath)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		print("new Backend Version: " + newBackendVersion)

		if applicationName == "backend-merger":
			backendRestartTool = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/merger.sh"
		elif applicationName == "backend-mailer":
			backendRestartTool = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/mailer.sh"
		elif applicationName == "backend-mailloop":
			backendRestartTool = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/mailloop.sh"

		# Stop existing backend
		if os.path.islink(backendRestartTool):
			restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
			print("Restart log in file: " + restartLogFile);

			print("Stopping with: " + backendRestartTool)
			if ESTUtilities.hasRootPermissions():
				os.system("su -c \"" + backendRestartTool + " stop 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
			else:
				os.system(backendRestartTool + " stop 2>&1 | tee --append " + restartLogFile)

		# Create new sym-link
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/previous"):
			os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/previous")
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current"):
			os.rename(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/previous")
		ESTUtilities.createLink("V" + newBackendVersion, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current", applicationUserName)

		# Create basic backend symlinks only on initial installation
		if applicationName == "backend-mailloop":
			if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib"):
				os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib")
			elif os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib"):
				shutil.rmtree(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib")
			if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib") and os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current/lib"):
				ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current/lib", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib", applicationUserName)

		if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/scripts") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/scripts"):
			ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current/scripts", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/scripts", applicationUserName)
			os.system("cd " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin && ln -s ../release/backend/current/bin/* . 2>/dev/null")
			logfile.write("Created new initial backend symlinks\n")

		if applicationName == "backend-merger":
			if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/JAVA") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/JAVA"):
				ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current/JAVA", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/JAVA", applicationUserName)

		logfile.write("Created new backend symlinks\n")

		if applicationName == "backend-merger":
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current"):
				Environment.mergerBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current"))
				if Environment.mergerBackendVersion is not None and Environment.mergerBackendVersion.startswith("V"):
					Environment.mergerBackendVersion = Environment.mergerBackendVersion[1:len(Environment.mergerBackendVersion)]
			else:
				Environment.mergerBackendVersion = None
		elif applicationName == "backend-mailer":
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current"):
				Environment.mailerBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current"))
				if Environment.mailerBackendVersion is not None and Environment.mailerBackendVersion.startswith("V"):
					Environment.mailerBackendVersion = Environment.mailerBackendVersion[1:len(Environment.mailerBackendVersion)]
			else:
				Environment.mailerBackendVersion = None
		elif applicationName == "backend-mailloop":
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current"):
				Environment.mailloopBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/current"))
				if Environment.mailloopBackendVersion is not None and Environment.mailloopBackendVersion.startswith("V"):
					Environment.mailloopBackendVersion = Environment.mailloopBackendVersion[1:len(Environment.mailloopBackendVersion)]
			else:
				Environment.mailloopBackendVersion = None

		# Start new backend
		if os.path.isfile(backendRestartTool):
			restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
			print("Restart log in file: " + restartLogFile);

			print("Starting with: " + backendRestartTool)
			if ESTUtilities.hasRootPermissions():
				os.system("su -c \"" + backendRestartTool + " start 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
			else:
				os.system(backendRestartTool + " start 2>&1 | tee --append " + restartLogFile)
		else:
			Environment.errors.append("Cannot find backend start tool '" + backendRestartTool + "'")
	elif applicationName == "backend-one" or applicationName == "backend-console" or applicationName == "backend-rdir":
		newBackendVersion = ESTUtilities.getVersionFromFilename(packageFilename)
		if newBackendVersion is None:
			errorText = "Invalid backend package file name: " + packageFilename
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Remove existing older extracted directory of same version
		if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion):
			shutil.rmtree(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion)

		# Extract targz-file for backend
		ESTUtilities.unTarGzFile(packageFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/")

		updatedApplicationDirectoryPath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion

		if not os.path.isdir(updatedApplicationDirectoryPath):
			Environment.errors.append("Extraction of updatePackageFile '" + packageFilePath + "' was not successful")
			logfile.write("Extraction of updatePackageFile '" + packageFilePath + "' was not successful in path '" + updatedApplicationDirectoryPath + "'" + "\n")
			return

		os.remove(packageFilePath)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		print("new Backend Version: " + newBackendVersion)

		# Create new sym-link
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/previous"):
			os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/previous")
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/current"):
			os.rename(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/current", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/previous")
		ESTUtilities.createLink("V" + newBackendVersion, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/current", applicationUserName)

		# Create basic backend symlinks only on initial installation
		if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/scripts") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/scripts"):
			ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current/scripts", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/scripts", applicationUserName)

			os.system("cd " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin && ln -s ../release/backend/current/bin/* . 2>/dev/null")
			logfile.write("Created new initial backend symlinks\n")

		logfile.write("Created new backend symlinks\n")

		if applicationName == "backend-one":
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current"):
				Environment.emmBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current"))
				if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
					Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]
			else:
				Environment.emmBackendVersion = None
		elif applicationName == "backend-console":
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current"):
				Environment.emmBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current"))
				if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
					Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]
			else:
				Environment.emmBackendVersion = None
		elif applicationName == "backend-rdir":
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current"):
				Environment.rdirBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current"))
				if Environment.rdirBackendVersion is not None and Environment.rdirBackendVersion.startswith("V"):
					Environment.rdirBackendVersion = Environment.rdirBackendVersion[1:len(Environment.rdirBackendVersion)]
			else:
				Environment.rdirBackendVersion = None

	elif applicationName == "backend":
		newBackendVersion = ESTUtilities.getVersionFromFilename(packageFilename)
		if newBackendVersion is None:
			errorText = "Invalid backend package file name: " + packageFilename
			Environment.errors.append(errorText)
			logfile.write(errorText + "\n")
			return

		# Remove existing older extracted directory of same version
		if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion):
			shutil.rmtree(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion)

		# Extract targz-file for backend
		if "OpenEMM" in Environment.applicationName:
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/var/log") and not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/var/log"):
				ESTUtilities.createDirectories(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/var/log", "openemm", "openemm")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/var/tmp") and not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/var/tmp"):
				ESTUtilities.createDirectories(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/var/tmp", "openemm", "openemm")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/opt/python3") and not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/opt/python3") \
				and os.path.isdir("/opt/agnitas.com/software/python3"):
				ESTUtilities.createLink("/opt/agnitas.com/software/python3", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/opt/python3", "openemm", "openemm")
			ESTUtilities.unTarGzFile(packageFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/", "openemm", "openemm")
		elif ESTUtilities.hasRootPermissions():
			ESTUtilities.unTarGzFileAsRoot(packageFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/")
		else:
			ESTUtilities.unTarGzFile(packageFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/")

		updatedApplicationDirectoryPath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/V" + newBackendVersion

		if not os.path.isdir(updatedApplicationDirectoryPath):
			Environment.errors.append("Extraction of updatePackageFile '" + packageFilePath + "' was not successful")
			logfile.write("Extraction of updatePackageFile '" + packageFilePath + "' was not successful in path '" + updatedApplicationDirectoryPath + "'" + "\n")
			return

		os.remove(packageFilePath)
		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		print("new Backend Version: " + newBackendVersion)

		# Stop existing backend
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/backend.sh"):
			restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
			print("Restart log in file: " + restartLogFile);

			print("Stopping with: backend.sh")
			if ESTUtilities.hasRootPermissions():
				os.system("su -c \"" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/backend.sh stop 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
			else:
				os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/backend.sh stop 2>&1 | tee --append " + restartLogFile)

		# Create new sym-link
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/previous"):
			os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/previous")
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/current"):
			os.rename(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/current", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/previous")
		ESTUtilities.createLink("V" + newBackendVersion, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/current", applicationUserName)

		# Create basic backend symlinks only on initial installation
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib"):
			os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib")
		elif os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib"):
			shutil.rmtree(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib")
		if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib"):
			ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current/lib", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/lib", applicationUserName)

		if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/scripts") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/scripts"):
			ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current/scripts", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/scripts", applicationUserName)
		if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/JAVA") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/JAVA"):
			ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current/JAVA", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/JAVA", applicationUserName)
			os.system("cd " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin && ln -s ../release/backend/current/bin/* . 2>/dev/null")
			logfile.write("Created new initial backend symlinks\n")

		logfile.write("Created new backend symlinks\n")

		if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current"):
			Environment.emmBackendVersion = os.path.basename(os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend/current"))
			if Environment.emmBackendVersion is not None and Environment.emmBackendVersion.startswith("V"):
				Environment.emmBackendVersion = Environment.emmBackendVersion[1:len(Environment.emmBackendVersion)]
		else:
			Environment.emmBackendVersion = None

		# Start new backend
		if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/backend.sh"):
			restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
			print("Restart log in file: " + restartLogFile);

			print("Starting with: backend.sh")
			if ESTUtilities.hasRootPermissions():
				os.system("su -c \"" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/backend.sh start 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
			else:
				os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/backend.sh start 2>&1 | tee --append " + restartLogFile)
		else:
			Environment.errors.append("Cannot start backend")
	elif applicationName == "manual":
		# Extract targz-file for manuals
		ESTUtilities.createDirectory(updatedApplicationDirectoryPath, applicationUserName)
		ESTUtilities.unTarGzFile(packageFilePath, updatedApplicationDirectoryPath, applicationUserName)

		# Remove obsolete sub directory
		if os.path.isdir(updatedApplicationDirectoryPath + "/" + packageFilename.replace(".tar.gz", "")):
			os.system("mv '" + updatedApplicationDirectoryPath + "/" + packageFilename.replace(".tar.gz", "") + "' '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'")
			os.system("mv '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'/* '" + updatedApplicationDirectoryPath + "/'")
			os.system("rmdir '" + updatedApplicationDirectoryPath + "/toBeRemovedDir'")

		logfile.write("Extracted file '" + packageFilePath + "' in path '" + updatedApplicationDirectoryPath + "'" + "\n")

		# Create default directory and link for manuals
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual"):
			ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual", applicationUserName)
		if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/default"):
			ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/en", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/default", applicationUserName)

		# Create new webapps-link for manuals
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/en"):
			os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/en")
		ESTUtilities.createLink(updatedApplicationDirectoryPath + "/en", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/en", applicationUserName)
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/de"):
			os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/de")
		ESTUtilities.createLink(updatedApplicationDirectoryPath + "/de", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/de", applicationUserName)
		logfile.write("Created new manual links '" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual" + "'\n")

		# Read new version info
		manualApplicationPath = os.path.realpath(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/de")
		manualVersion = ESTUtilities.getVersionFromFilename(manualApplicationPath)
		if manualVersion is not None:
			Environment.manualVersion = manualVersion
		else:
			Environment.manualVersion = "Unknown"
	else:
		FrontendInstaller.installFile(interactive, logfile, applicationUserName, applicationName, updatedApplicationDirectoryPath, packageFilePath)

	updateContentHubEntry()

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
	elif applicationName == "tomcat":
		return "Apache Tomcat"
	elif applicationName == "java":
		return "JAVA"
	elif applicationName == "backend-runtime":
		return "backend-runtime"
	elif applicationName == "backend-startup":
		return "backend-startup"
	elif applicationName == "backend-one":
		return "backend-one"
	elif applicationName == "backend-console":
		return "backend-console"
	elif applicationName == "backend-rdir":
		return "backend-rdir"
	elif applicationName == "backend-merger":
		return "backend-merger"
	elif applicationName == "backend-mailer":
		return "backend-mailer"
	elif applicationName == "backend-mailloop":
		return "backend-mailloop"
	else:
		return applicationName.upper()

def installFileFromCloud(packageUrl, interactive, alreadyOpenLogFile = None):
	print()
	print("Starting " + Environment.applicationName + " Update ...")
	if ESTUtilities.isDebugMode():
		print("Package Url: " + packageUrl)

	now = datetime.datetime.now()
	nowString = now.strftime("%Y-%m-%d_%H-%M-%S")
	logfile = None
	try:
		if Environment.isOpenEmmServer:
			applicationUserName = "openemm"
		elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer:
			applicationUserName = Environment.getFrontendApplicationUserName()
		elif Environment.isEmmRdirServer:
			applicationUserName = Environment.getRdirApplicationUserName()
		elif Environment.isEmmMergerServer:
			applicationUserName = Environment.getMergerApplicationUserName()
		elif Environment.isEmmMailerServer:
			applicationUserName = Environment.getMailerApplicationUserName()
		elif Environment.isEmmMailloopServer:
			applicationUserName = Environment.getMailloopApplicationUserName()
		else:
			applicationUserName = Environment.getFrontendApplicationUserName()

		if ESTUtilities.hasRootPermissions():
			if not os.path.isdir("/root/release"):
				ESTUtilities.createDirectory("/root/release", "root")
			if not os.path.isdir("/root/release/log"):
				ESTUtilities.createDirectory("/root/release/log", "root")

			if alreadyOpenLogFile is None:
				logfilePath = "/root/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile
		else:
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release", applicationUserName)
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log", applicationUserName)

			if alreadyOpenLogFile is None:
				logfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile

		logfile.write("Update package url/id: " + packageUrl + "\n")
		packageUrl = re.sub(r'\/(download|$)', '', packageUrl)
		packageDownloadID = packageUrl.split("/")[-1]

		if not ESTUtilities.checkDownloadFileIsAvailable(Environment.agnitasCloudUrl + "/index.php/s/" + packageDownloadID):
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
				params = parse_header(downloadPageResponse.headers.get("Content-Disposition", ""))
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

		if "backend-runtime-" in packageFilename or "-startup-" in packageFilename:
			applicationName = "backend-runtime"
			releaseSubdirectoryName = ""
			currentVersion = Environment.runtimeBackendVersion
			currentVersion = Environment.runtimeBackendVersion
		elif "-runtime-" in packageFilename:
			applicationName = "runtime"
			releaseSubdirectoryName = "runtime"
			currentVersion = Environment.runtimeVersion
		elif "backend-one-" in packageFilename:
			applicationName = "backend-one"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.emmBackendVersion
		elif "backend-console-" in packageFilename:
			applicationName = "backend-console"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.emmBackendVersion
		elif "backend-rdir-" in packageFilename:
			applicationName = "backend-rdir"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.rdirBackendVersion
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
		elif "-tomcat-" in packageFilename:
			applicationName = "tomcat"
			releaseSubdirectoryName = None
			currentVersion = ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
		elif "-java-" in packageFilename:
			applicationName = "java"
			releaseSubdirectoryName = None
			currentVersion = ESTUtilities.getJavaVersion(Environment.javaHome)
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

		versionCompare = ESTUtilities.compareVersions(currentVersion, ESTUtilities.getVersionFromFilename(packageFilename))
		if versionCompare < 1:
			print()
			print("Package contains application type '" + getApplicationDisplayName(applicationName) + "' of Version '" + ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(ESTUtilities.normalizeVersion(currentVersion)) + "'.")
			if interactive:
				if versionCompare == 0:
					print(Colors.YELLOW + "This is the same version." + Colors.DEFAULT)
				else:
					print(Colors.RED + "The update package is an older version!" + Colors.DEFAULT)
				print("Install anyway? (N/y, Blank => Cancel):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if not (choice.startswith("y") or choice.startswith("j")):
					logfile.write("Declined installation of old version '" + ESTUtilities.getVersionFromFilename(packageFilename) + "' of application type '" + applicationName + "'\n")
					return
			else:
				print("Obsolete update is skipped.")
				return
		elif releaseSubdirectoryName is not None and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName):
			print()
			print("Package contains new application '" + getApplicationDisplayName(applicationName) + "'")
			if interactive:
				print("Initially install this application? (N/y, Blank => Cancel):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if choice.startswith("y") or choice.startswith("j"):
					ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName, applicationUserName)
				else:
					Environment.errors.append("Unexpected update package type '" + getApplicationDisplayName(applicationName) + "'")
					logfile.write("Unexpected update package type '" + applicationName + "'" + "\n")
					return
			else:
				print("Initial update is skipped. Please install manually for first time")
				return
		else:
			print()
			print("Found update package '" + packageFilename + "' for application '" + getApplicationDisplayName(applicationName) + "' with version '" + ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(ESTUtilities.normalizeVersion(currentVersion)) + "'.")
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
					cleanupOldVersions(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName, ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(packageFilename)), ESTUtilities.normalizeVersion(currentVersion), logfile)

		if ESTUtilities.isBlank(releaseSubdirectoryName):
			downloadDestinationFilePath = "/tmp/" + packageFilename
		else:
			downloadDestinationFilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/" + packageFilename

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
				nextProgressBarUpdate = None
				while True:
					chunk = downloadPageResponse.read(chunk_size)
					bytes_read += len(chunk)

					if not chunk:
						break

					downloadDestinationFile.write(chunk)

					# Update progress bar
					now = datetime.datetime.now()
					if nextProgressBarUpdate is None or nextProgressBarUpdate < now:
						ESTUtilities.printProgressBar(total_size, bytes_read, progressbarSize)
						nextProgressBarUpdate = now + datetime.timedelta(0, 1)
				ESTUtilities.printProgressBar(total_size, total_size, progressbarSize)

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
			if ESTUtilities.isDebugMode():
				logging.exception(errorText)
				raise
			return
		print("Download finished")

		if ESTUtilities.hasRootPermissions():
			os.system("chown " + applicationUserName + " " + downloadDestinationFilePath)

		installFile(downloadDestinationFilePath, logfile, interactive)

		if releaseSubdirectoryName is None and os.path.isfile(downloadDestinationFilePath):
			os.remove(downloadDestinationFilePath)

		if alreadyOpenLogFile is None:
			if Environment.errors is None or len(Environment.errors) == 0:
				Environment.messages.append("Update package file '" + packageFilename + "' successfully deployed.\nFor logs see '" + os.path.realpath(logfile.name) + "'")
			else:
				Environment.errors.append("Update package file '" + packageFilename + "' deployed with errors.\nFor logs see '" + os.path.realpath(logfile.name) + "'")
	except urllib.error.URLError as e:
		errorText = "AGNITAS Cloud '" + e.url + "' is not reachable (HTTP code: " + str(e.code) + ")"
		Environment.errors.append(errorText)
		if logfile is not None:
			logfile.write(errorText + "\n")
	except:
		errorText = "Error while deploying update package"
		Environment.errors.append(errorText)
		if logfile is not None:
			logfile.write(errorText + "\n")
		if ESTUtilities.isDebugMode():
			logging.exception(errorText)
			raise
	finally:
		if logfile is not None and alreadyOpenLogFile is None:
			logfile.write("Update ended at: " + datetime.datetime.now().strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			logfile.close()

def siteUpdateMenuAction(actionParameters):
	print(Environment.applicationName + " Update via AGNITAS Website")

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
			applicationUserName = Environment.getFrontendApplicationUserName()
		elif Environment.isEmmRdirServer:
			applicationUserName = Environment.getRdirApplicationUserName()
		elif Environment.isEmmMergerServer:
			applicationUserName = "merger"
		elif Environment.isEmmMailerServer:
			applicationUserName = "mailout"
		elif Environment.isEmmMailloopServer:
			applicationUserName = "mailloop"
		else:
			applicationUserName = Environment.getFrontendApplicationUserName()

		if not Environment.isOpenEmmServer \
			and not Environment.isEmmFrontendServer and not Environment.isEmmStatisticsServer and not Environment.isEmmWebservicesServer \
			and not Environment.isEmmConsoleRdirServer and not Environment.isEmmRdirServer \
			and not Environment.isEmmMergerServer and not Environment.isEmmMailerServer and not Environment.isEmmMailloopServer:
			initialInstall = True
		else:
			initialInstall = False

		if ESTUtilities.hasRootPermissions():
			if not os.path.isdir("/root/release"):
				ESTUtilities.createDirectory("/root/release", "root")
			if not os.path.isdir("/root/release/log"):
				ESTUtilities.createDirectory("/root/release/log", "root")

			if alreadyOpenLogFile is None:
				logfilePath = "/root/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile
		else:
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release", applicationUserName)
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log", applicationUserName)

			if alreadyOpenLogFile is None:
				logfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile

		versionInfo = ESTUtilities.downloadVersionInfo(Environment.agnitasDownloadPathVersionInfo)
		downloadFileUrlPaths = {}

		if versionInfo is not None:
			# Check for release upgrade
			if "new-release-backend-runtime" in versionInfo and interactive:
				print(Colors.YELLOW + "\nA new release version is available: " + versionInfo["new-release-backend-runtime"][0] + Colors.DEFAULT)
				if "new-release-notes" in versionInfo:
					releaseNotes = ESTUtilities.downloadTextFile(versionInfo["new-release-notes"][1])
					print()
					print("Release notes: ")
					print(Colors.YELLOW + releaseNotes + Colors.DEFAULT)
					print()
				print("Do you want to upgrade? (N/y, Blank => Cancel):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if choice.startswith("y") or choice.startswith("j"):
					logfile.write("Release version upgrade: " + versionInfo["new-release-backend-runtime"][0] + " " + versionInfo["new-release-backend-runtime"][1] + "\n")
					downloadFileUrlPaths["new-release-backend-runtime"] = versionInfo["new-release-backend-runtime"][1]
					print(Colors.YELLOW + "\nStarting release upgrade" + Colors.DEFAULT)
					downloadAndInstallNewLicense()
			elif "new-release-backend-startup" in versionInfo and interactive:
				print(Colors.YELLOW + "\nA new release version is available: " + versionInfo["new-release-backend-startup"][0] + Colors.DEFAULT)
				if "new-release-notes" in versionInfo:
					releaseNotes = ESTUtilities.downloadTextFile(versionInfo["new-release-notes"][1])
					print()
					print("Release notes: ")
					print(Colors.YELLOW + releaseNotes + Colors.DEFAULT)
					print()
				print("Do you want to upgrade? (N/y, Blank => Cancel):")
				choice = input(" > ")
				choice = choice.strip().lower()
				if choice.startswith("y") or choice.startswith("j"):
					logfile.write("Release version upgrade: " + versionInfo["new-release-backend-startup"][0] + " " + versionInfo["new-release-backend-startup"][1] + "\n")
					downloadFileUrlPaths["new-release-backend-runtime"] = versionInfo["new-release-backend-startup"][1]
					print(Colors.YELLOW + "\nStarting release upgrade" + Colors.DEFAULT)
					downloadAndInstallNewLicense()
			elif "new-release-runtime" in versionInfo and interactive:
				print(Colors.YELLOW + "\nA new release version is available: " + versionInfo["new-release-runtime"][0] + Colors.DEFAULT)
				if "new-release-notes" in versionInfo:
					releaseNotes = ESTUtilities.downloadTextFile(versionInfo["new-release-notes"][1])
					print()
					print("Release notes: ")
					print(Colors.YELLOW + releaseNotes + Colors.DEFAULT)
					print()
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
				if "backend-runtime" in versionInfo:
					if not ESTUtilities.hasRootPermissions():
						Environment.warnings.append("The application backend-runtime must be updated by an user with root permission")
					else:
						downloadFileUrlPaths["backend-runtime"] = versionInfo["backend-runtime"][1]
				elif "backend-startup" in versionInfo:
					if not ESTUtilities.hasRootPermissions():
						Environment.warnings.append("The application backend-startup must be updated by an user with root permission")
					else:
						downloadFileUrlPaths["backend-runtime"] = versionInfo["backend-startup"][1]

				if "runtime" in versionInfo and (initialInstall or Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
					downloadFileUrlPaths["runtime"] = versionInfo["runtime"][1]

				if "java" in versionInfo and (initialInstall or Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
					downloadFileUrlPaths["java"] = versionInfo["java"][1]

				if "tomcat" in versionInfo and (initialInstall or Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
					downloadFileUrlPaths["tomcat"] = versionInfo["tomcat"][1]

				if Environment.isOpenEmmServer:
					if "complete" in versionInfo:
						downloadFileUrlPaths["complete"] = versionInfo["complete"][1]
					elif "openemm" in versionInfo:
						downloadFileUrlPaths["openemm"] = versionInfo["openemm"][1]
					else:
						if "frontend" in versionInfo:
							downloadFileUrlPaths["frontend"] = versionInfo["frontend"][1]

						if Environment.osVendor == "Suse" and "backend-suse" in versionInfo:
							downloadFileUrlPaths["backend-suse"] = versionInfo["backend-suse"][1]
						elif "backend" in versionInfo:
							downloadFileUrlPaths["backend"] = versionInfo["backend"][1]

					if "manual" in versionInfo:
						downloadFileUrlPaths["manual"] = versionInfo["manual"][1]
				else:
					if (initialInstall or Environment.isEmmFrontendServer) and "frontend-emm" in versionInfo:
						downloadFileUrlPaths["frontend-emm"] = versionInfo["frontend-emm"][1]
					elif (initialInstall or Environment.isEmmFrontendServer) and "emm" in versionInfo:
						downloadFileUrlPaths["emm"] = versionInfo["emm"][1]

					if (initialInstall or Environment.isEmmStatisticsServer) and "frontend-statistics" in versionInfo:
						downloadFileUrlPaths["frontend-statistics"] = versionInfo["frontend-statistics"][1]
					elif (initialInstall or Environment.isEmmStatisticsServer) and "statistics" in versionInfo:
						downloadFileUrlPaths["statistics"] = versionInfo["statistics"][1]

					if (initialInstall or Environment.isEmmWebservicesServer) and "frontend-webservices" in versionInfo:
						downloadFileUrlPaths["frontend-webservices"] = versionInfo["frontend-webservices"][1]
					elif (initialInstall or Environment.isEmmWebservicesServer) and "webservices" in versionInfo:
						downloadFileUrlPaths["webservices"] = versionInfo["webservices"][1]

					if Environment.isEmmConsoleRdirServer and "frontend-rdir" in versionInfo:
						downloadFileUrlPaths["frontend-rdir"] = versionInfo["frontend-rdir"][1]
					elif Environment.isEmmConsoleRdirServer and "rdir" in versionInfo:
						downloadFileUrlPaths["rdir"] = versionInfo["rdir"][1]
					if (initialInstall or Environment.isEmmRdirServer) and "frontend-rdir" in versionInfo:
						downloadFileUrlPaths["frontend-rdir"] = versionInfo["frontend-rdir"][1]
					elif (initialInstall or Environment.isEmmRdirServer) and "rdir" in versionInfo:
						downloadFileUrlPaths["rdir"] = versionInfo["rdir"][1]

					if Environment.isEmmUnifiedServer and "backend-one" in versionInfo:
						downloadFileUrlPaths["backend-one"] = versionInfo["backend-one"][1]
					elif (initialInstall or Environment.isEmmFrontendServer) and "backend-console" in versionInfo:
						downloadFileUrlPaths["backend-console"] = versionInfo["backend-console"][1]
					elif Environment.isEmmWebservicesServer and "backend-console" in versionInfo:
						downloadFileUrlPaths["backend-console"] = versionInfo["backend-console"][1]
					elif Environment.isEmmConsoleRdirServer and "backend-console" in versionInfo:
						downloadFileUrlPaths["backend-console"] = versionInfo["backend-console"][1]

					elif Environment.isEmmRdirServer and "backend-rdir" in versionInfo:
						downloadFileUrlPaths["backend-rdir"] = versionInfo["backend-rdir"][1]

					if (initialInstall or Environment.isEmmMergerServer) and "backend-merger" in versionInfo:
						downloadFileUrlPaths["backend-merger"] = versionInfo["backend-merger"][1]

					if (initialInstall or Environment.isEmmMailerServer) and "backend-mailer" in versionInfo:
						downloadFileUrlPaths["backend-mailer"] = versionInfo["backend-mailer"][1]

					if (initialInstall or Environment.isEmmMailloopServer) and "backend-mailloop" in versionInfo:
						downloadFileUrlPaths["backend-mailloop"] = versionInfo["backend-mailloop"][1]

					if (initialInstall or Environment.isEmmFrontendServer) and "manual" in versionInfo:
						downloadFileUrlPaths["manual"] = versionInfo["manual"][1]

		if ESTUtilities.isDebugMode():
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
					downloadPageResponse = ESTUtilities.openUrlConnection(downloadFileUrlPath)
					effectiveFinalUrl = downloadPageResponse.geturl()
					params = parse_header(downloadPageResponse.headers.get("Content-Disposition", ""))

					if "filename" in params:
						packageFilename = params["filename"]
					else:
						packageFilename = effectiveFinalUrl[effectiveFinalUrl.rfind("/") + 1:]
				finally:
					if downloadPageResponse is not None:
						downloadPageResponse.close()

				logfile.write("Update package file: " + packageFilename + "\n")

				if "backend-runtime-" in packageFilename or "-startup-" in packageFilename:
					applicationName = "backend-runtime"
					releaseSubdirectoryName = ""
					currentVersion = Environment.runtimeBackendVersion
				elif "-runtime-" in packageFilename:
					applicationName = "runtime"
					releaseSubdirectoryName = "runtime"
					if Environment.isEmmRdirServer:
						currentVersion = Environment.rdirRuntimeVersion
					else:
						currentVersion = Environment.runtimeVersion
				elif "backend-one-" in packageFilename:
					applicationName = "backend-one"
					releaseSubdirectoryName = "backend"
					currentVersion = Environment.emmBackendVersion
				elif "backend-console-" in packageFilename:
					applicationName = "backend-console"
					releaseSubdirectoryName = "backend"
					currentVersion = Environment.emmBackendVersion
				elif "backend-rdir-" in packageFilename:
					applicationName = "backend-rdir"
					releaseSubdirectoryName = "backend"
					currentVersion = Environment.rdirBackendVersion
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
				elif "-tomcat-" in packageFilename:
					applicationName = "tomcat"
					releaseSubdirectoryName = None
					currentVersion = ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
				elif "-java-" in packageFilename:
					applicationName = "java"
					releaseSubdirectoryName = None
					currentVersion = ESTUtilities.getJavaVersion(Environment.javaHome)
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

				versionCompare = ESTUtilities.compareVersions(currentVersion, ESTUtilities.getVersionFromFilename(packageFilename))

				if versionCompare < 1:
					print()
					print("Package contains application type '" + getApplicationDisplayName(applicationName) + "' of version '" + ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(ESTUtilities.normalizeVersion(currentVersion)) + "'.")
					if interactive:
						if versionCompare == 0:
							print(Colors.YELLOW + "This is the same version." + Colors.DEFAULT)
						else:
							print(Colors.RED + "The update package is an older version!" + Colors.DEFAULT)
						print("Install anyway? (N/y, Blank => Cancel):")
						choice = input(" > ")
						choice = choice.strip().lower()
						if not (choice.startswith("y") or choice.startswith("j")):
							logfile.write("Declined installation of version '" + ESTUtilities.getVersionFromFilename(packageFilename) + "' for application '" + applicationName + "'\n")
							continue
					else:
						print("Obsolete update of " + getApplicationDisplayName(applicationName) + " is skipped.")
						continue
				elif releaseSubdirectoryName is not None and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName):
					print()
					print("Package contains new application '" + getApplicationDisplayName(applicationName) + "'")
					if interactive:
						print("Initially install this application? (N/y, Blank => Cancel):")
						choice = input(" > ")
						choice = choice.strip().lower()
						if choice.startswith("y") or choice.startswith("j"):
							ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName, applicationUserName)
						else:
							logfile.write("New application package type '" + applicationName + "' canceled" + "\n")
							continue
					else:
						continue
				else:
					print()
					print("Found update package '" + packageFilename + "' for application '" + getApplicationDisplayName(applicationName) + "' with version '" + ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(ESTUtilities.normalizeVersion(currentVersion)) + "'.")
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
							cleanupOldVersions(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName, ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(packageFilename)), ESTUtilities.normalizeVersion(currentVersion), logfile)

				if ESTUtilities.isBlank(releaseSubdirectoryName):
					downloadDestinationFilePath = "/tmp/" + packageFilename
				else:
					if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"):
						ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release", applicationUserName)
						logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release" + "\n")
					if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName):
						ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName, applicationUserName)
						logfile.write("Created directory: " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "\n")
					downloadDestinationFilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName + "/" + packageFilename

				# Clean up old update attempt left overs
				if os.path.isfile(downloadDestinationFilePath):
					os.remove(downloadDestinationFilePath)
					logfile.write("Removed already existing file '" + downloadDestinationFilePath + "'\n")

				# Download update file
				print("Download started")
				downloadPageResponse = None
				try:
					downloadPageResponse = ESTUtilities.openUrlConnection(downloadFileUrlPath)
					with open(downloadDestinationFilePath, "wb") as downloadDestinationFile:
						total_size = int(downloadPageResponse.info().get_all("Content-Length")[0].strip())
						bytes_read = 0
						chunk_size = 8192
						progressbarSize = 80
						print()
						print()
						nextProgressBarUpdate = None
						while True:
							chunk = downloadPageResponse.read(chunk_size)
							bytes_read += len(chunk)

							if not chunk:
								break

							downloadDestinationFile.write(chunk)

							# Update progress bar
							now = datetime.datetime.now()
							if nextProgressBarUpdate is None or nextProgressBarUpdate < now:
								ESTUtilities.printProgressBar(total_size, bytes_read, progressbarSize)
								nextProgressBarUpdate = now + datetime.timedelta(0, 1)
						ESTUtilities.printProgressBar(total_size, total_size, progressbarSize)

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
					if ESTUtilities.isDebugMode():
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
						Environment.errors.append("Update package file '" + packageFilename + "' deployed with errors.\nFor logs see '" + os.path.realpath(logfile.name) + "'")

		if ESTUtilities.isBlank(Environment.getSystemUrl()) or Environment.getSystemUrl().strip() == "Unknown" and DbConnector.checkDbServiceAvailable() and DbConnector.checkDbStructureExists():
			Environment.errors.append("Basic configuration is missing. Please configure.")
			Environment.overrideNextMenu = Environment.configTableMenu
	except urllib.error.URLError as e:
		errorText = "AGNITAS Cloud '" + e.url + "' is not reachable (HTTP code: " + str(e.code) + ")"
		Environment.errors.append(errorText)
		if logfile is not None:
			logfile.write(errorText + "\n")
	except:
		errorText = "Error while deploying update package (Log: " + os.path.realpath(logfile.name) + ")"
		Environment.errors.append(errorText)
		if logfile is not None:
			logfile.write(errorText + "\n")
		if ESTUtilities.isDebugMode():
			logging.exception(errorText)
			raise
	finally:
		if logfile is not None and alreadyOpenLogFile is None:
			logfile.write("Update ended at: " + datetime.datetime.now().strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			logfile.close()

def cloudUpdateMenuAction(actionParameters):
	print(Environment.applicationName + " Update via AGNITAS Cloud")

	print("Please enter update package link ID (Blank => Cancel):")
	packageUrl = input(" > ")

	if packageUrl != "":
		installFileFromCloud(packageUrl, True)

def fileUpdateMenuAction(actionParameters, alreadyOpenLogFile = None):
	print(Environment.applicationName + " Update from local file")

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
			applicationUserName = Environment.getRdirApplicationUserName()
		elif Environment.isEmmMergerServer:
			applicationUserName = "merger"
		elif Environment.isEmmMailerServer:
			applicationUserName = "mailout"
		elif Environment.isEmmMailloopServer:
			applicationUserName = "mailloop"
		else:
			applicationUserName = Environment.getFrontendApplicationUserName()

		if ESTUtilities.hasRootPermissions():
			if not os.path.isdir("/root/release"):
				ESTUtilities.createDirectories("/root/release", "root")
			if not os.path.isdir("/root/release/log"):
				ESTUtilities.createDirectory("/root/release/log", "root")

			if alreadyOpenLogFile is None:
				logfilePath = "/root/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile
		else:
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"):
				ESTUtilities.createDirectories(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release", applicationUserName)
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log", applicationUserName)

			if alreadyOpenLogFile is None:
				logfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/update_" + nowString + ".log"
				logfile = open(logfilePath, "w", encoding="UTF-8")
				logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
			else:
				logfile = alreadyOpenLogFile

		logfile.write("Update package file: " + packageFilePath + "\n")

		packageFilename = os.path.basename(packageFilePath)

		if "backend-runtime-" in packageFilename or "-startup-" in packageFilename:
			applicationName = "backend-runtime"
			releaseSubdirectoryName = ""
			currentVersion = Environment.runtimeBackendVersion
		elif "-runtime-" in packageFilename:
			applicationName = "runtime"
			releaseSubdirectoryName = "runtime"
			currentVersion = Environment.runtimeVersion
		elif "backend-one-" in packageFilename:
			applicationName = "backend-one"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.emmBackendVersion
		elif "backend-console-" in packageFilename:
			applicationName = "backend-console"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.emmBackendVersion
		elif "backend-rdir-" in packageFilename:
			applicationName = "backend-rdir"
			releaseSubdirectoryName = "backend"
			currentVersion = Environment.rdirBackendVersion
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
		elif "-tomcat-" in packageFilename:
			applicationName = "tomcat"
			releaseSubdirectoryName = None
			currentVersion = ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
		elif "-java-" in packageFilename:
			applicationName = "java"
			releaseSubdirectoryName = None
			currentVersion = ESTUtilities.getJavaVersion(Environment.javaHome)
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

		versionCompare = ESTUtilities.compareVersions(currentVersion, ESTUtilities.getVersionFromFilename(packageFilename))
		if versionCompare < 1:
			print()
			print("Package contains application type '" + getApplicationDisplayName(applicationName) + "' of Version '" + ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(ESTUtilities.normalizeVersion(currentVersion)) + "'.")
			if versionCompare == 0:
				print(Colors.YELLOW + "This is the same version." + Colors.DEFAULT)
			else:
				print(Colors.RED + "The update package is an older version!" + Colors.DEFAULT)
			print("Install anyway? (N/y, Blank => Cancel):")
			choice = input(" > ")
			choice = choice.strip().lower()
			if not (choice.startswith("y") or choice.startswith("j")):
				logfile.write("Declined installation of old version '" + ESTUtilities.getVersionFromFilename(packageFilename) + "' of application type '" + applicationName + "'\n")
				return

		elif releaseSubdirectoryName is not None and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName):
			print()
			print("Package contains new application '" + getApplicationDisplayName(applicationName) + "'")
			print("Initially install this application? (N/y, Blank => Cancel):")
			choice = input(" > ")
			choice = choice.strip().lower()
			if choice.startswith("y") or choice.startswith("j"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName, applicationUserName)
			else:
				Environment.errors.append("Unexpected update package type '" + getApplicationDisplayName(applicationName) + "'")
				logfile.write("Unexpected update package type '" + applicationName + "'" + "\n")
				return
		else:
			print()
			print("Found update package '" + packageFilename + "' for application '" + getApplicationDisplayName(applicationName) + "' with version '" + ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(packageFilename)) + "'.\nYour current version is '" + str(ESTUtilities.normalizeVersion(currentVersion)) + "'.")
			print("Continue with update? (Y/n, Blank => Yes):")
			choice = input(" > ")
			choice = choice.strip().lower()
			if not (choice == "" or choice.startswith("y") or choice.startswith("j")):
				errorText = "Update canceled"
				Environment.errors.append(errorText)
				logfile.write(errorText + "\n")
				return

		installFile(packageFilePath, logfile, True)

		if alreadyOpenLogFile is None:
			if Environment.errors is None or len(Environment.errors) == 0:
				Environment.messages.append("Update package file '" + packageFilename + "' successfully deployed.\nFor logs see '" + os.path.realpath(logfile.name) + "'")
			else:
				Environment.errors.append("Update package file '" + packageFilename + "' deployed with errors.\nFor logs see '" + os.path.realpath(logfile.name) + "'")
	except urllib.error.URLError as e:
		errorText = "AGNITAS Cloud '" + e.url + "' is not reachable (HTTP code: " + str(e.code) + ")"
		Environment.errors.append(errorText)
		logfile.write(errorText + "\n")
	except:
		errorText = "Error while deploying update package"
		Environment.errors.append(errorText)
		logfile.write(errorText + "\n")
		if ESTUtilities.isDebugMode():
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
			applicationUserName = Environment.getRdirApplicationUserName()
		elif Environment.isEmmMergerServer:
			applicationUserName = "merger"
		elif Environment.isEmmMailerServer:
			applicationUserName = "mailout"
		elif Environment.isEmmMailloopServer:
			applicationUserName = "mailloop"
		else:
			applicationUserName = Environment.getFrontendApplicationUserName()

		now = datetime.datetime.now()
		nowString = now.strftime("%Y-%m-%d_%H-%M-%S")

		if ESTUtilities.hasRootPermissions():
			if not os.path.isdir("/root/release"):
				ESTUtilities.createDirectory("/root/release", "root")
			if not os.path.isdir("/root/release/log"):
				ESTUtilities.createDirectory("/root/release/log", "root")
			logfilePath = "/root/release/log/update_" + nowString + ".log"

			logfile = open(logfilePath, "w", encoding="UTF-8")
			logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")
		else:
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release", applicationUserName)
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log"):
				ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log", applicationUserName)
			logfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/update_" + nowString + ".log"

			logfile = open(logfilePath, "w", encoding="UTF-8")
			logfile.write("Update started at: " + now.strftime("%Y-%m-%d_%H:%M:%S") + "\n")

		if ESTUtilities.isBlank(updateFile):
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
		if ESTUtilities.isDebugMode():
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
				ESTUtilities.printError("ERROR: " + error + "\n")
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
			version = ESTUtilities.getVersionFromFilename(directoryOrFileName)
			if version is not None and (currentVersion is None or ESTUtilities.compareVersions(version, currentVersion) != 0) and version not in availableVersionsToDelete:
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
			version = ESTUtilities.getVersionFromFilename(directoryOrFileName)
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
				if maximumMajorMinorVersion is None or ESTUtilities.compareVersions(version, maximumMajorMinorVersion) < 0:
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
				if maximumMajorMinorVersion is None or ESTUtilities.compareVersions(version, maximumMajorMinorVersion) < 0:
					maximumMajorMinorVersion = version
	return maximumMajorMinorVersion

def downloadAndInstallNewLicense():
	licenseProperties = License.readLicenseValues()
	if "licenseUrl" in licenseProperties and ESTUtilities.isNotBlank(licenseProperties.get("licenseUrl")):
		licenseUrl = licenseProperties.get("licenseUrl")

		if not ESTUtilities.checkDownloadFileIsAvailable(licenseUrl):
			print("LicenseUrl: " + licenseUrl + " (not reachable)")
			return
		else:
			if ESTUtilities.debugMode:
				print("LicenseUrl: " + licenseUrl)

			downloadDestinationFilePath = "/tmp/emm_license.zip"
			if os.path.isfile(downloadDestinationFilePath):
				os.remove(downloadDestinationFilePath)

			downloadPageResponse = None
			try:
				downloadPageResponse = ESTUtilities.openUrlConnection(licenseUrl)
				with open(downloadDestinationFilePath, "wb") as downloadDestinationFile:
					chunk_size = 8192
					while True:
						chunk = downloadPageResponse.read(chunk_size)

						if not chunk:
							break

						downloadDestinationFile.write(chunk)
			except:
				errorText = "Download of new license file failed"
				if ESTUtilities.debugMode:
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
						ESTUtilities.unzipFile(downloadDestinationFilePath, licenseTempDirectory)
					elif downloadDestinationFilePath.endswith(".tar.gz"):
						ESTUtilities.unTarGzFile(downloadDestinationFilePath, licenseTempDirectory)

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


def updateContentHubEntry():
	if Environment.contentHubUrl:
		licenseId =  License.getLicenseID()
		emmVersion = Environment.frontendVersion
		operatingSystem = Environment.osVendor
		if Environment.osVersion:
			operatingSystem = Environment.osVendor + " " + str(Environment.osVersion)

		tomcat = ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
		java = ESTUtilities.getJavaVersion(Environment.javaHome)
		node = ESTUtilities.getNodeVersion()
		ESTUtilities.openUrlConnection(Environment.contentHubUrl + "?" + urlencode({"lsid" : licenseId, "vs" : emmVersion, "os" : operatingSystem, "tv" : tomcat, "jv" : java, "nv" : node}))
