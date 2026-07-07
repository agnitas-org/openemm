import os
import re
import time
import shutil

from EST_lib.Environment import Environment
from EST_lib import ESTUtilities, FrontendRuntimeInstaller

def switchVersionMenuAction(actionParameters):
	print("Currently active application versions:")

	if Environment.isOpenEmmServer or Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer or Environment.isEmmConsoleRdirServer or Environment.isEmmRdirServer:
		print(" Runtime:          " + Environment.runtimeVersion)

	if Environment.isOpenEmmServer:
		print(" Frontend:         " + Environment.frontendVersion)
		if os.path.isfile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/manual") or os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/manual"):
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
		if Environment.runtimeBackendVersion is not None:
			print(" Backend-Runtime:  " + Environment.runtimeBackendVersion)
		if Environment.isEmmMergerServer:
			print(" Backend-Merger:   " + Environment.mergerBackendVersion)
		if Environment.isEmmMailerServer:
			print(" Backend-Mailer:   " + Environment.mailerBackendVersion)
		if Environment.isEmmMailloopServer:
			print(" Backend-Mailloop: " + Environment.mailloopBackendVersion)

	print()
	print("Please enter application to change version for (Blank => Back):")

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
					applicationUserName = Environment.getRdirApplicationUserName()
				else:
					applicationUserName = Environment.getFrontendApplicationUserName()

				if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/version.txt"):
					with open(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/version.txt") as runtimeVersionFile:
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
				neededTomcatMainVersion = ["10.", "11."]
				if ESTUtilities.compareVersions("21.05", version) < 0:
					neededTomcatMainVersion = ["9."]
				currentTomcatVersion = ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
				if validVersion and not any(currentTomcatVersion.startswith(p) for p in neededTomcatMainVersion):
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
						Environment.frontendVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory("openemm") + "/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]
					else:
						Environment.frontendVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]

					# Version 21.04.000 or less needs Tomcat 9, newer versions need Tomcat 10
					neededTomcatMainVersion = "10."
					if ESTUtilities.compareVersions("21.05", choice) < 0:
						neededTomcatMainVersion = "9."
					currentTomcatVersion = ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
					if not currentTomcatVersion.startswith(neededTomcatMainVersion):
						tomcatVersions = getAvailableTomcatVersions()
						useTomcatVersion = None
						useTomcatVersionDir = None
						for tomcatVersion in tomcatVersions:
							if tomcatVersion.startswith(neededTomcatMainVersion) and (useTomcatVersion is None or ESTUtilities.compareVersions(useTomcatVersion, tomcatVersion) > 0):
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
				neededTomcatMainVersion = "10."
				if ESTUtilities.compareVersions("21.05", version) < 0:
					neededTomcatMainVersion = "9."
				currentTomcatVersion = ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
				if validVersion and not currentTomcatVersion.startswith(neededTomcatMainVersion) and ESTUtilities.hasRootPermissions():
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
					Environment.frontendVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/emm/WEB-INF/classes/emm.properties")["ApplicationVersion"]

					if Environment.isEmmStatisticsServer:
						activateStatisticsVersion(availableStatisticsVersions[choice])
						Environment.statisticsVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/statistics/WEB-INF/classes/emm.properties")["ApplicationVersion"]

					if Environment.isEmmWebservicesServer:
						activateWebservicesVersion(availableWebservicesVersions[choice])
						Environment.webservicesVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/webapps/webservices/WEB-INF/classes/emm.properties")["ApplicationVersion"]

					# Version 21.04.000 or less needs Tomcat 9, newer versions need Tomcat 10
					neededTomcatMainVersion = "10."
					if ESTUtilities.compareVersions("21.05", choice) < 0:
						neededTomcatMainVersion = "9."
					currentTomcatVersion = ESTUtilities.getTomcatVersion(Environment.javaHome, Environment.catalinaHome)
					if not currentTomcatVersion.startswith(neededTomcatMainVersion) and ESTUtilities.hasRootPermissions():
						tomcatVersions = getAvailableTomcatVersions()
						useTomcatVersion = None
						useTomcatVersionDir = None
						for tomcatVersion in tomcatVersions:
							if tomcatVersion.startswith(neededTomcatMainVersion) and (useTomcatVersion is None or ESTUtilities.compareVersions(useTomcatVersion, tomcatVersion) > 0):
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
				Environment.rdirVersion = ESTUtilities.readPropertiesFile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/webapps/rdir/WEB-INF/classes/emm.properties")["ApplicationVersion"]
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
		availableVersions = getAvailableBackendVersions(Environment.getFrontendApplicationUserName())
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
				activateBackendVersion(Environment.getFrontendApplicationUserName(), choice)
				Environment.emmBackendVersion = choice
				Environment.rebootNeeded = True
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Backend-Console': '" + choice + "'")
			return True
	elif choice == "backend-runtime" or choice == "backend-startup":
		print("Available versions for 'Backend-Runtime':")
		availableVersions = getAvailableBackendRuntimeVersions()
		for version in sorted(availableVersions):
			print(" " + version)
		print()

		print("Please enter new version to use: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return True
		elif choice in availableVersions:
			if choice != Environment.runtimeBackendVersion:
				activateBackendRuntimeVersion(choice)
				Environment.runtimeBackendVersion = choice
			return True
		else:
			Environment.errors.append("Unavailable application version for 'Backend-Runtime': '" + choice + "'")
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
		applicationUserName = Environment.getRdirApplicationUserName()
	elif Environment.isEmmMergerServer:
		applicationUserName = "merger"
	elif Environment.isEmmMailerServer:
		applicationUserName = "mailout"
	elif Environment.isEmmMailloopServer:
		applicationUserName = "mailloop"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()

	availableVersions = {}
	if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/runtime"):
		for versionFile in os.listdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/runtime"):
			if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/runtime/" + versionFile):
				version = ESTUtilities.getVersionFromFilename(versionFile)
				if version is not None:
					availableVersions[version] = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/runtime/" + versionFile
	return availableVersions

def activateRuntimeVersion(versionFilePath):
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

	FrontendRuntimeInstaller.createTemporarySystemFileBackup()

	# Delete old runtime data
	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat"):
		os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat")
	if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps-leave-empty"):
		shutil.rmtree(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps-leave-empty")
	for listedFile in os.listdir(ESTUtilities.getUserHomeDirectory(applicationUserName)):
		if re.search("^tomcat.*$", listedFile) and os.path.isdir(listedFile):
			shutil.rmtree(listedFile)

	# Extract targz-file for runtime
	ESTUtilities.unTarGzFile(versionFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/", applicationUserName)

	FrontendRuntimeInstaller.restoreTemporarySystemFileBackup()

def getAvailableEmmVersions():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
	availableVersions = {}
	if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/emm"):
		for versionDir in os.listdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/emm"):
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/emm/" + versionDir):
				version = ESTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/emm/" + versionDir
	return availableVersions

def getAvailableRdirVersions():
	if Environment.isEmmRdirServer:
		applicationUserName = Environment.getRdirApplicationUserName()
	elif Environment.isEmmConsoleRdirServer:
		applicationUserName = Environment.getFrontendApplicationUserName()
	availableVersions = {}
	if applicationUserName is not None and os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/rdir"):
		for versionDir in os.listdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/rdir"):
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/rdir/" + versionDir):
				version = ESTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/rdir/" + versionDir
	return availableVersions

def getAvailableStatisticsVersions():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
	availableVersions = {}
	if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/statistics"):
		for versionDir in os.listdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/statistics"):
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/statistics/" + versionDir):
				version = ESTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/statistics/" + versionDir
	return availableVersions

def getAvailableWebservicesVersions():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
	availableVersions = {}
	if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/webservices"):
		for versionDir in os.listdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/webservices"):
			if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/webservices/" + versionDir):
				version = ESTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/webservices/" + versionDir
	return availableVersions

def getAvailableTomcatVersions():
	availableVersions = {}
	if os.path.isdir(Environment.optDefaultPath):
		for versionDir in os.listdir(Environment.optDefaultPath):
			if versionDir.startswith("apache-tomcat-"):
				version = ESTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = Environment.optDefaultPath + "/" + versionDir
	if os.path.isdir(Environment.optDefaultPathSecondary):
		for versionDir in os.listdir(Environment.optDefaultPathSecondary):
			if versionDir.startswith("apache-tomcat-"):
				version = ESTUtilities.getVersionFromFilename(versionDir)
				if version is not None:
					availableVersions[version] = Environment.optDefaultPathSecondary + "/" + versionDir
	return availableVersions

def activateEmmVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm"):
		os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm")
	ESTUtilities.createLink(versionDirectoryPath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm", applicationUserName)

def activateRdirVersion(versionDirectoryPath):
	if Environment.isEmmRdirServer:
		applicationUserName = Environment.getRdirApplicationUserName()
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/rdir"):
		os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/rdir")
	ESTUtilities.createLink(versionDirectoryPath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/rdir", applicationUserName)

def activateStatisticsVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/statistics"):
		os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/statistics")
	ESTUtilities.createLink(versionDirectoryPath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/statistics", applicationUserName)

def activateWebservicesVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/webservices"):
		os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/webservices")
	ESTUtilities.createLink(versionDirectoryPath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/webservices", applicationUserName)

def activateTomcatVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "root"
	if os.path.islink(Environment.catalinaHome):
		os.unlink(Environment.catalinaHome)
	ESTUtilities.createLink(versionDirectoryPath, Environment.catalinaHome, applicationUserName)

def activateJavaVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "root"
	if os.path.islink(Environment.javaHome):
		os.unlink(Environment.javaHome)
	ESTUtilities.createLink(versionDirectoryPath, Environment.javaHome, applicationUserName)

def getAvailableBackendVersions(backendUsername):
	if backendUsername == "openemm":
		applicationUserName = "openemm"
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend"
	elif backendUsername == Environment.getRdirApplicationUserName():
		applicationUserName = Environment.getRdirApplicationUserName()
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend"
	elif backendUsername == "merger":
		applicationUserName = "merger"
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"
	elif backendUsername == "mailout":
		applicationUserName = "mailout"
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"
	elif backendUsername == "mailloop":
		applicationUserName = "mailloop"
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend"

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
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend"
		backendRestartToolName = "backend.sh"
	elif backendUsername == Environment.getRdirApplicationUserName():
		applicationUserName = Environment.getRdirApplicationUserName()
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend"
		backendRestartToolName = "backend.sh"
	elif backendUsername == "merger":
		applicationUserName = "merger"
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"
		backendRestartToolName = "merger.sh"
	elif backendUsername == "mailout":
		applicationUserName = "mailout"
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"
		backendRestartToolName = "mailer.sh"
	elif backendUsername == "mailloop":
		applicationUserName = "mailloop"
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release"
		backendRestartToolName = "mailloop.sh"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
		backendReleasePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/backend"
		backendRestartToolName = "backend.sh"

	# Stop existing backend
	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + backendRestartToolName):
		print("*** Stopping backend ***")
		restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
		print("Restart log in file: " + restartLogFile);

		print("Stopping with: " + backendRestartToolName)
		if ESTUtilities.hasRootPermissions():
			os.system("su -c \"" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + backendRestartToolName + " stop 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
		else:
			os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + backendRestartToolName + " stop 2>&1 | tee --append " + restartLogFile)
	else:
		Environment.errors.append("Cannot stop backend")

	if os.path.islink(backendReleasePath + "/previous"):
		os.unlink(backendReleasePath + "/previous")
	if os.path.islink(backendReleasePath + "/current"):
		os.rename(backendReleasePath + "/current", backendReleasePath + "/previous")
	ESTUtilities.createLink("V" + version, backendReleasePath + "/current", applicationUserName)

	# Start new backend
	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + backendRestartToolName):
		print("*** Starting backend ***")
		restartLogFile = "/tmp/emm_restart_" + time.strftime("%Y-%m-%d_%H-%M-%S", time.localtime()) + ".log"
		print("Restart log in file: " + restartLogFile);

		print("Starting with: " + backendRestartToolName)
		if ESTUtilities.hasRootPermissions():
			os.system("su -c \"" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + backendRestartToolName + " start 2>&1 | tee --append " + restartLogFile + "\" - " + applicationUserName)
		else:
			os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + backendRestartToolName + " start 2>&1 | tee --append " + restartLogFile)
	else:
		Environment.errors.append("Cannot start backend")

def getAvailableBackendRuntimeVersions():
	availableVersions = {}
	if os.path.isdir("/root/release"):
		for versionDir in os.listdir("/root/release"):
			if versionDir.startswith("V"):
				version = versionDir[1:]
				if version is not None:
					availableVersions[version] = "/root/release/" + versionDir
	return availableVersions

def activateBackendRuntimeVersion(version):
	if os.path.islink("/root/release/previous"):
		os.unlink("/root/release/previous")
	if os.path.islink("/root/release/current"):
		os.rename("/root/release/current", "/root/release/previous")
	ESTUtilities.createLink("V" + version, "/root/release/current", "root")

def getAvailableManualVersions():
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()

	availableVersions = {}
	if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/manual"):
		for versionDir in os.listdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/manual"):
			version = ESTUtilities.getVersionFromFilename(versionDir)
			if version is not None:
				availableVersions[version] = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/manual/" + versionDir
	return availableVersions

def activateManualVersion(versionDirectoryPath):
	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()
	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/en"):
		os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/en")
	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/de"):
		os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/de")
	ESTUtilities.createLink(versionDirectoryPath + "/en", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/en", applicationUserName)
	ESTUtilities.createLink(versionDirectoryPath + "/de", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manual/de", applicationUserName)
