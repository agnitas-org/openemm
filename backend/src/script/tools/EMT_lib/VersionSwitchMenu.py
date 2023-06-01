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
import re
import time
import shutil

from EMT_lib.Environment import Environment
from EMT_lib import EMTUtilities
from EMT_lib import InstallAndUpdateMenu

def switchVersionMenuAction(actionParameters):
	print("Currently active application versions:")

	if Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer:
		print(" Runtime:          " + Environment.runtimeVersion)

	if Environment.isOpenEmmServer:
		print(" Frontend:         " + Environment.frontendVersion)
		if os.path.isfile("/home/openemm/webapps/manual") or os.path.isdir("/home/openemm/webapps/manual"):
			print(" Manual:           " + Environment.manualVersion)
	else:
		if Environment.isEmmFrontendServer:
			print(" Frontend:         " + Environment.frontendVersion)
			print(" Manual:           " + Environment.manualVersion)
		if Environment.isEmmConsoleRdirServer:
			print(" ConsoleRdir:      " + Environment.consoleRdirVersion)
		if Environment.isEmmRdirServer:
			print(" Rdir:             " + Environment.rdirVersion)

	if Environment.isOpenEmmServer:
		print(" Backend:          " + Environment.emmBackendVersion)
	else:
		if Environment.isEmmFrontendServer:
			print(" Backend-Console:  " + Environment.emmBackendVersion)
		if Environment.startupBackendVersion is not None:
			print(" Backend-Startup:  " + Environment.startupBackendVersion)
		if Environment.isEmmMergerServer:
			print(" Backend-Merger:   " + Environment.mergerBackendVersion)
		if Environment.isEmmMailerServer:
			print(" Backend-Mailer:   " + Environment.mailerBackendVersion)
		if Environment.isEmmMailloopServer:
			print(" Backend-Mailloop: " + Environment.mailloopBackendVersion)

	print()
	print("Please enter application to change version for: (Blank => Back):")

	choice = input(" > ")
	choice = choice.strip().lower()
	print()
	if choice == "":
		return
	elif choice == "runtime" and (Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer):
		print("Available versions for 'Runtime':")
		availableVersions = getAvailableRuntimeVersions()
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.runtimeVersion:
				activateRuntimeVersion(availableVersions[choice])

				if Environment.isOpenEmmServer:
					applicationUserName = "openemm"
				elif Environment.isEmmRdirServer:
					applicationUserName = "rdir"
				else:
					applicationUserName = "console"

				if os.path.isfile("/home/" + applicationUserName + "/tomcat/conf/version.txt"):
					with open("/home/" + applicationUserName + "/tomcat/conf/version.txt") as runtimeVersionFile:
						Environment.runtimeVersion = runtimeVersionFile.read().strip()
				else:
					Environment.runtimeVersion = "Unknown"
				Environment.rebootNeeded = True
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Runtime': '" + choice + "'")
			return True
	elif choice == "frontend":
		if Environment.isOpenEmmServer:
			print("Available versions for 'Frontend':")
			availableEmmVersions = getAvailableEmmVersions()
			availableStatisticsVersions = getAvailableStatisticsVersions()
			availableWebservicesVersions = getAvailableWebservicesVersions()

			availableVersions = []
			for version in availableEmmVersions:
				validVersion = True
				if Environment.isEmmStatisticsServer and not version in availableStatisticsVersions:
					validVersion = False
				if Environment.isEmmWebservicesServer and not version in availableWebservicesVersions:
					validVersion = False

				# Version 21.04.000 or less needs Tomcat 9, newer versions need Tomcat 10
				neededTomcatMainVersion = "10.0"
				if EMTUtilities.compareVersions("21.05", version) < 0:
					neededTomcatMainVersion = "9.0"
				currentTomcatVersion = EMTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
				if validVersion and not currentTomcatVersion.startswith(neededTomcatMainVersion):
					tomcatVersions = getAvailableTomcatVersions()
					validVersion = False
					for tomcatVersion in tomcatVersions:
						if tomcatVersion.startswith(neededTomcatMainVersion):
							validVersion = True
				if validVersion:
					availableVersions.append(version)

			for version in sorted(availableVersions):
				print(" " + version)
			print()

			print("Please enter new version to use: (Blank => Cancel):")
			choice = input(" > ")
			choice = choice.strip().lower()
			if choice == "":
				return True
			elif choice in availableVersions:
				if choice != Environment.frontendVersion:
					activateEmmVersion(availableEmmVersions[choice])

					activateStatisticsVersion(availableStatisticsVersions[choice])

					if choice in availableWebservicesVersions:
						activateWebservicesVersion(availableWebservicesVersions[choice])

					if Environment.isOpenEmmServer:
						Environment.frontendVersion = EMTUtilities.readPropertiesFile("/home/openemm/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]
					else:
						Environment.frontendVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]

					# Version 21.04.000 or less needs Tomcat 9, newer versions need Tomcat 10
					neededTomcatMainVersion = "10.0"
					if EMTUtilities.compareVersions("21.05", choice) < 0:
						neededTomcatMainVersion = "9.0"
					currentTomcatVersion = EMTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
					if not currentTomcatVersion.startswith(neededTomcatMainVersion):
						tomcatVersions = getAvailableTomcatVersions()
						useTomcatVersion = None
						useTomcatVersionDir = None
						for tomcatVersion in tomcatVersions:
							if tomcatVersion.startswith(neededTomcatMainVersion) and (useTomcatVersion is None or EMTUtilities.compareVersions(useTomcatVersion, tomcatVersion) > 0):
								useTomcatVersion = tomcatVersion
								useTomcatVersionDir = tomcatVersions[tomcatVersion]
						if useTomcatVersionDir is not None:
							activateTomcatVersion(useTomcatVersionDir)

					Environment.rebootNeeded = True
				return True
			else:
				Environment.errors.append("Unavailable application version for 'Frontend': '" + choice + "'")
				return True
		elif Environment.isEmmFrontendServer:
			print("Available versions for 'Frontend':")
			availableEmmVersions = getAvailableEmmVersions()
			availableStatisticsVersions = getAvailableStatisticsVersions()
			availableWebservicesVersions = getAvailableWebservicesVersions()
			availableVersions = []
			for version in availableEmmVersions:
				validVersion = True
				if Environment.isEmmStatisticsServer and not version in availableStatisticsVersions:
					validVersion = False
				if Environment.isEmmWebservicesServer and not version in availableWebservicesVersions:
					validVersion = False
				# Version 21.04.000 or less needs Tomcat 9, newer versions need Tomcat 10
				neededTomcatMainVersion = "10.0"
				if EMTUtilities.compareVersions("21.05", version) < 0:
					neededTomcatMainVersion = "9.0"
				currentTomcatVersion = EMTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
				if validVersion and not currentTomcatVersion.startswith(neededTomcatMainVersion) and EMTUtilities.hasRootPermissions():
					tomcatVersions = getAvailableTomcatVersions()
					validVersion = False
					for tomcatVersion in tomcatVersions:
						if tomcatVersion.startswith(neededTomcatMainVersion):
							validVersion = True
				if validVersion:
					availableVersions.append(version)

			for version in sorted(availableVersions):
				print(" " + version)
			print()

			print("Please enter new version to use: (Blank => Cancel):")
			choice = input(" > ")
			choice = choice.strip().lower()
			if choice == "":
				return True
			elif choice in availableVersions:
				if choice != Environment.frontendVersion:
					activateEmmVersion(availableEmmVersions[choice])
					Environment.frontendVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]

					if Environment.isEmmStatisticsServer:
						activateStatisticsVersion(availableStatisticsVersions[choice])
						Environment.statisticsVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]

					if Environment.isEmmWebservicesServer:
						activateWebservicesVersion(availableWebservicesVersions[choice])
						Environment.webservicesVersion = EMTUtilities.readPropertiesFile("/home/console/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]

					# Version 21.04.000 or less needs Tomcat 9, newer versions need Tomcat 10
					neededTomcatMainVersion = "10.0"
					if EMTUtilities.compareVersions("21.05", choice) < 0:
						neededTomcatMainVersion = "9.0"
					currentTomcatVersion = EMTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
					if not currentTomcatVersion.startswith(neededTomcatMainVersion) and EMTUtilities.hasRootPermissions():
						tomcatVersions = getAvailableTomcatVersions()
						useTomcatVersion = None
						useTomcatVersionDir = None
						for tomcatVersion in tomcatVersions:
							if tomcatVersion.startswith(neededTomcatMainVersion) and (useTomcatVersion is None or EMTUtilities.compareVersions(useTomcatVersion, tomcatVersion) > 0):
								useTomcatVersion = tomcatVersion
								useTomcatVersionDir = tomcatVersions[tomcatVersion]
						if useTomcatVersionDir is not None:
							activateTomcatVersion(useTomcatVersionDir)

					Environment.rebootNeeded = True
				return True
			else:
				Environment.errors.append("Unavailable application version for 'Frontend': '" + choice + "'")
				return True
	elif choice == "rdir":
		print("Available versions for 'Rdir':")
		availableVersions = getAvailableRdirVersions()
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.rdirVersion:
				activateRdirVersion(availableVersions[choice])
				Environment.rdirVersion = EMTUtilities.readPropertiesFile("/home/rdir/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]
				Environment.rebootNeeded = True
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Rdir': '" + choice + "'")
			return True
	elif choice == "backend":
		print("Available versions for 'Backend':")
		availableVersions = getAvailableBackendVersions("openemm")
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.emmBackendVersion:
				activateBackendVersion("openemm", choice)
				Environment.emmBackendVersion = choice
				Environment.rebootNeeded = True
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Backend': '" + choice + "'")
			return True
	elif choice == "backend-console":
		print("Available versions for 'Backend-Console':")
		availableVersions = getAvailableBackendVersions("console")
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.emmBackendVersion:
				activateBackendVersion("console", choice)
				Environment.emmBackendVersion = choice
				Environment.rebootNeeded = True
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Backend-Console': '" + choice + "'")
			return True
	elif choice == "backend-startup":
		print("Available versions for 'Backend-Startup':")
		availableVersions = getAvailableBackendStartupVersions()
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.startupBackendVersion:
				activateBackendStartupVersion(choice)
				Environment.startupBackendVersion = choice
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Backend-Startup': '" + choice + "'")
			return True
	elif choice == "backend-merger":
		print("Available versions for 'Backend-Merger':")
		availableVersions = getAvailableBackendVersions("merger")
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.mergerBackendVersion:
				activateBackendVersion("merger", choice)
				Environment.mergerBackendVersion = choice
				Environment.rebootNeeded = True
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Backend-Merger': '" + choice + "'")
			return True
	elif choice == "backend-mailer":
		print("Available versions for 'Backend-Mailer':")
		availableVersions = getAvailableBackendVersions("mailout")
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.mailerBackendVersion:
				activateBackendVersion("mailout", choice)
				Environment.mailerBackendVersion = choice
				Environment.rebootNeeded = True
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Backend-Mailer': '" + choice + "'")
			return True
	elif choice == "backend-mailloop":
		print("Available versions for 'Backend-Mailloop':")
		availableVersions = getAvailableBackendVersions("mailloop")
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.mailloopBackendVersion:
				activateBackendVersion("mailloop", choice)
				Environment.mailloopBackendVersion = choice
				Environment.rebootNeeded = True
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Backend-Mailloop': '" + choice + "'")
			return True
	elif choice == "manual":
		print("Available versions for 'Manual':")
		availableVersions = getAvailableManualVersions()
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.manualVersion:
				activateManualVersion(availableVersions[choice])
				Environment.manualVersion = choice
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Manual': '" + choice + "'")
			return True
	else:
		Environment.errors.append("Unknown application name: '" + choice + "'")
		return True

def getAvailableRuntimeVersions():
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

	availableVersions = {}
	if os.path.isdir("/home/" + applicationUserName + "/release/runtime"):
		for versionFile in os.listdir("/home/" + applicationUserName + "/release/runtime"):
			if os.path.isfile("/home/" + applicationUserName + "/release/runtime/" + versionFile):
				version = EMTUtilities.getVersionFromFilename(versionFile)
				if version is not None:
					availableVersions[version] = "/home/" + applicationUserName + "/release/runtime/" + versionFile
	return availableVersions

def activateRuntimeVersion(versionFilePath):
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

	InstallAndUpdateMenu.createTemporarySystemFileBackup()

	# Delete old runtime data
	if os.path.islink("/home/" + applicationUserName + "/tomcat"):
		os.unlink("/home/" + applicationUserName + "/tomcat")
	if os.path.isdir("/home/" + applicationUserName + "/webapps-leave-empty"):
		shutil.rmtree("/home/" + applicationUserName + "/webapps-leave-empty")
	for listedFile in os.listdir("/home/" + applicationUserName):
		if re.search("^tomcat.*$", listedFile) and os.path.isdir(listedFile):
			shutil.rmtree(listedFile)

	# Extract targz-file for runtime
	EMTUtilities.unTarGzFile(versionFilePath, "/home/" + applicationUserName + "/", applicationUserName)

	InstallAndUpdateMenu.restoreTemporarySystemFileBackup()

def getAvailableEmmVersions():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"
	availableVersions = {}
	if os.path.isdir("/home/" + applicationUserName + "/release/emm"):
		for versionDir in os.listdir("/home/" + applicationUserName + "/release/emm"):
			if os.path.isdir("/home/" + applicationUserName + "/release/emm/" + versionDir):
				version = EMTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = "/home/" + applicationUserName + "/release/emm/" + versionDir
	return availableVersions

def getAvailableRdirVersions():
	if Environment.isEmmRdirServer:
		applicationUserName = "rdir"
	elif Environment.isEmmConsoleRdirServer:
		applicationUserName = "console"
	availableVersions = {}
	if applicationUserName is not None and os.path.isdir("/home/" + applicationUserName + "/release/rdir"):
		for versionDir in os.listdir("/home/" + applicationUserName + "/release/rdir"):
			if os.path.isdir("/home/" + applicationUserName + "/release/rdir/" + versionDir):
				version = EMTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = "/home/" + applicationUserName + "/release/rdir/" + versionDir
	return availableVersions

def getAvailableStatisticsVersions():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"
	availableVersions = {}
	if os.path.isdir("/home/" + applicationUserName + "/release/statistics"):
		for versionDir in os.listdir("/home/" + applicationUserName + "/release/statistics"):
			if os.path.isdir("/home/" + applicationUserName + "/release/statistics/" + versionDir):
				version = EMTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = "/home/" + applicationUserName + "/release/statistics/" + versionDir
	return availableVersions

def getAvailableWebservicesVersions():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"
	availableVersions = {}
	if os.path.isdir("/home/" + applicationUserName + "/release/webservices"):
		for versionDir in os.listdir("/home/" + applicationUserName + "/release/webservices"):
			if os.path.isdir("/home/" + applicationUserName + "/release/webservices/" + versionDir):
				version = EMTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = "/home/" + applicationUserName + "/release/webservices/" + versionDir
	return availableVersions

def getAvailableTomcatVersions():
	availableVersions = {}
	if os.path.isdir(Environment.optDefaultPath):
		for versionDir in os.listdir(Environment.optDefaultPath):
			if versionDir.startswith("apache-tomcat-"):
				version = EMTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = Environment.optDefaultPath + "/" + versionDir
	return availableVersions

def activateEmmVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"
	if os.path.islink("/home/" + applicationUserName + "/webapps/emm"):
		os.unlink("/home/" + applicationUserName + "/webapps/emm")
	EMTUtilities.createLink(versionDirectoryPath, "/home/" + applicationUserName + "/webapps/emm", applicationUserName)

def activateRdirVersion(versionDirectoryPath):
	if Environment.isEmmRdirServer:
		applicationUserName = "rdir"
	else:
		applicationUserName = "console"
	if os.path.islink("/home/" + applicationUserName + "/webapps/rdir"):
		os.unlink("/home/" + applicationUserName + "/webapps/rdir")
	EMTUtilities.createLink(versionDirectoryPath, "/home/" + applicationUserName + "/webapps/rdir", applicationUserName)

def activateStatisticsVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"
	if os.path.islink("/home/" + applicationUserName + "/webapps/statistics"):
		os.unlink("/home/" + applicationUserName + "/webapps/statistics")
	EMTUtilities.createLink(versionDirectoryPath, "/home/" + applicationUserName + "/webapps/statistics", applicationUserName)

def activateWebservicesVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"
	if os.path.islink("/home/" + applicationUserName + "/webapps/webservices"):
		os.unlink("/home/" + applicationUserName + "/webapps/webservices")
	EMTUtilities.createLink(versionDirectoryPath, "/home/" + applicationUserName + "/webapps/webservices", applicationUserName)

def activateTomcatVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "root"
	if os.path.islink(Environment.optDefaultPath + "/tomcat"):
		os.unlink(Environment.optDefaultPath + "/tomcat")
	EMTUtilities.createLink(versionDirectoryPath, Environment.optDefaultPath + "/tomcat", applicationUserName)

def activateJavaVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "root"
	if os.path.islink(Environment.optDefaultPath + "/java"):
		os.unlink(Environment.optDefaultPath + "/java")
	EMTUtilities.createLink(versionDirectoryPath, Environment.optDefaultPath + "/java", applicationUserName)

def getAvailableBackendVersions(backendUsername):
	if backendUsername == "openemm":
		applicationUserName = "openemm"
		backendReleasePath = "/home/" + applicationUserName + "/release/backend"
	elif backendUsername == "rdir":
		applicationUserName = "rdir"
		backendReleasePath = "/home/" + applicationUserName + "/release/backend"
	elif backendUsername == "merger":
		applicationUserName = "merger"
		backendReleasePath = "/home/" + applicationUserName + "/release"
	elif backendUsername == "mailout":
		applicationUserName = "mailout"
		backendReleasePath = "/home/" + applicationUserName + "/release"
	elif backendUsername == "mailloop":
		applicationUserName = "mailloop"
		backendReleasePath = "/home/" + applicationUserName + "/release"
	else:
		applicationUserName = "console"
		backendReleasePath = "/home/" + applicationUserName + "/release/backend"

	availableVersions = {}
	if os.path.isdir(backendReleasePath):
		for versionDir in os.listdir(backendReleasePath):
			if versionDir.startswith("V"):
				version = versionDir[1:]
				if version is not None:
					availableVersions[version] = backendReleasePath + "/" + versionDir
	return availableVersions

def activateBackendVersion(backendUsername, version):
	if backendUsername == "openemm":
		applicationUserName = "openemm"
		backendReleasePath = "/home/" + applicationUserName + "/release/backend"
		backendRestartToolName = "backend.sh"
	elif backendUsername == "rdir":
		applicationUserName = "rdir"
		backendReleasePath = "/home/" + applicationUserName + "/release/backend"
		backendRestartToolName = "backend.sh"
	elif backendUsername == "merger":
		applicationUserName = "merger"
		backendReleasePath = "/home/" + applicationUserName + "/release"
		backendRestartToolName = "merger.sh"
	elif backendUsername == "mailout":
		applicationUserName = "mailout"
		backendReleasePath = "/home/" + applicationUserName + "/release"
		backendRestartToolName = "mailer.sh"
	elif backendUsername == "mailloop":
		applicationUserName = "mailloop"
		backendReleasePath = "/home/" + applicationUserName + "/release"
		backendRestartToolName = "mailloop.sh"
	else:
		applicationUserName = "console"
		backendReleasePath = "/home/" + applicationUserName + "/release/backend"
		backendRestartToolName = "backend.sh"

	# Stop existing backend
	if os.path.islink("/home/" + applicationUserName + "/bin/" + backendRestartToolName):
		print("*** Stopping backend ***")
		restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
		print("Restart log in file: " + restartLogFile);

		print("Stopping with: " + backendRestartToolName)
		if EMTUtilities.hasRootPermissions():
			os.system("su -c \"/home/" + applicationUserName + "/bin/" + backendRestartToolName + " stop 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
		else:
			os.system("/home/" + applicationUserName + "/bin/" + backendRestartToolName + " stop 2>&1 | tee --append " + restartLogFile)
	else:
		Environment.errors.append("Cannot stop backend")

	if os.path.islink(backendReleasePath + "/previous"):
		os.unlink(backendReleasePath + "/previous")
	if os.path.islink(backendReleasePath + "/current"):
		os.rename(backendReleasePath + "/current", backendReleasePath + "/previous")
	EMTUtilities.createLink("V" + version, backendReleasePath + "/current", applicationUserName)

	# Start new backend
	if os.path.islink("/home/" + applicationUserName + "/bin/" + backendRestartToolName):
		print("*** Starting backend ***")
		restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
		print("Restart log in file: " + restartLogFile);

		print("Starting with: " + backendRestartToolName)
		if EMTUtilities.hasRootPermissions():
			os.system("su -c \"/home/" + applicationUserName + "/bin/" + backendRestartToolName + " start 2>&1 | tee --append " + restartLogFile + "\" " + applicationUserName)
		else:
			os.system("/home/" + applicationUserName + "/bin/" + backendRestartToolName + " start 2>&1 | tee --append " + restartLogFile)
	else:
		Environment.errors.append("Cannot start backend")

def getAvailableBackendStartupVersions():
	availableVersions = {}
	if os.path.isdir("/root/release"):
		for versionDir in os.listdir("/root/release"):
			if versionDir.startswith("V"):
				version = versionDir[1:]
				if version is not None:
					availableVersions[version] = "/root/release/" + versionDir
	return availableVersions

def activateBackendStartupVersion(version):
	if os.path.islink("/root/release/previous"):
		os.unlink("/root/release/previous")
	if os.path.islink("/root/release/current"):
		os.rename("/root/release/current", "/root/release/previous")
	EMTUtilities.createLink("V" + version, "/root/release/current", "root")

def getAvailableManualVersions():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"

	availableVersions = {}
	if os.path.isdir("/home/" + applicationUserName + "/release/manual"):
		for versionDir in os.listdir("/home/" + applicationUserName + "/release/manual"):
			version = EMTUtilities.getVersionFromFilename(versionDir)
			if version is not None:
				availableVersions[version] = "/home/" + applicationUserName + "/release/manual/" + versionDir
	return availableVersions

def activateManualVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"
	if os.path.islink("/home/" + applicationUserName + "/webapps/manual/en"):
		os.unlink("/home/" + applicationUserName + "/webapps/manual/en")
	if os.path.islink("/home/" + applicationUserName + "/webapps/manual/de"):
		os.unlink("/home/" + applicationUserName + "/webapps/manual/de")
	EMTUtilities.createLink(versionDirectoryPath + "/en", "/home/" + applicationUserName + "/webapps/manual/en", applicationUserName)
	EMTUtilities.createLink(versionDirectoryPath + "/de", "/home/" + applicationUserName + "/webapps/manual/de", applicationUserName)
