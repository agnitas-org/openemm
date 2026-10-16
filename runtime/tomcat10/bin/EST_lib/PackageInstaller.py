import datetime
import logging
import os
import pathlib
import pwd
import re
import shlex
import shutil
import subprocess
import sys
import tarfile
import tempfile
import time
from abc import ABC, abstractmethod
from enum import Enum, auto
from itertools import chain
from pathlib import Path
from subprocess import CalledProcessError
from typing import NoReturn


from EST_lib import ESTUtilities, Colors, DbConnector
from EST_lib.ESTUtilities import restart, userExists
from EST_lib.Environment import Environment

logger = logging.getLogger(__name__)
updateLogger = logging.getLogger("update")

class PackageInstallationException(Exception):
	def __init__(self, message, errors):
		super().__init__(message)
		self.errors = errors


class PackageType(Enum):
	RUNTIME = auto()
	MANUAL = auto()
	TOMCAT = auto()
	JAVA = auto()
	FRONTEND_EMM = auto()
	FRONTEND_RDIR = auto()
	FRONTEND_STATISTICS = auto()
	FRONTEND_WEBSERVICES = auto()
	REST_MCP = auto()
	BACKEND_CONSOLE = auto()
	OPENEMM_BACKEND = auto()
	BACKEND_MAILER = auto()
	BACKEND_MAILLOOP = auto()
	BACKEND_MERGER = auto()
	BACKEND_ONE = auto()
	BACKEND_RDIR = auto()
	BACKEND_RUNTIME = auto()
	APPLICATION_BUNDLE = auto()
	UNKNOWN = auto()

restartTriggerApplicationTypes = [PackageType.BACKEND_RUNTIME, PackageType.RUNTIME]

def detectApplicationBundle(packageFile):
	with tarfile.open(packageFile,'r:gz') as tar:
		for tarEntry in tar.getnames():
			if not tarEntry.endswith("tar.gz"):
				return False
			if len(tarEntry.split("/")) > 2:
				break
		return True

def detectType(filename: str) -> PackageType:
	if "backend-runtime-" in filename or "-startup-" in filename:
		return PackageType.BACKEND_RUNTIME
	elif "-runtime-" in filename:
		return PackageType.RUNTIME
	elif "backend-one-" in filename:
		return PackageType.BACKEND_ONE
	elif "backend-console-" in filename:
		return PackageType.BACKEND_CONSOLE
	elif "openemm-backend" in filename:
		return PackageType.OPENEMM_BACKEND
	elif "backend-rdir-" in filename:
		return PackageType.BACKEND_RDIR
	elif "-merger-" in filename:
		return PackageType.BACKEND_MERGER
	elif "-mailer-" in filename:
		return PackageType.BACKEND_MAILER
	elif "-mailloop-" in filename:
		return PackageType.BACKEND_MAILLOOP
	elif "backend-one-" in filename or "backend" in filename:
		return PackageType.BACKEND_ONE
	elif "-emm-" in filename or "-gui-" in filename:
		return PackageType.FRONTEND_EMM
	elif "-statistics-" in filename:
		return PackageType.FRONTEND_STATISTICS
	elif "rest-mcp" in filename:
		return PackageType.REST_MCP
	elif "-webservices-" in filename:
		return PackageType.FRONTEND_WEBSERVICES
	elif "-rdir-" in filename:
		return PackageType.FRONTEND_RDIR
	elif "-manual-" in filename:
		return PackageType.MANUAL
	elif "-java-" in filename:
		return PackageType.JAVA
	elif "-tomcat-" in filename:
		return PackageType.TOMCAT
	elif "openemm-code" in filename or detectApplicationBundle(filename):
		return PackageType.APPLICATION_BUNDLE
	return PackageType.UNKNOWN

def detectTypeAndReturnHandler(filename: str, *args, **kwargs):
	packageType = detectType(filename)
	handler = HANDLERS.get(packageType)
	return handler(packageType, filename, *args, **kwargs)


class PackageHandler(ABC):
	def __init__(self, packageType: PackageType, packageFile: str, interactive: bool):
		self.userHomeDirectory = None
		self.packageType = packageType
		self.packageFile = Path(packageFile)
		self.interactive = interactive

	@staticmethod
	def refreshApplicationVersion(applicationName: str):
		return getattr(Environment.applicationVersions, applicationName.lower()).refresh()

	def promptInstallation(self):
		applicationName = self.detectNecessaryApplicationName()
		applicationDisplayName = self.getApplicationDisplayName(applicationName)
		applicationUserName = self.detectNecessaryApplicationUserName()

		currentVersion = getattr(Environment.applicationVersions, applicationName.lower()).refresh()
		installationBaseDirectory = self.getInstallBaseDirectory(applicationName, applicationUserName)
		versionCompare = ESTUtilities.compareVersions(currentVersion, ESTUtilities.getVersionFromFilename(self.packageFile))

		print()
		if versionCompare < 1:
			print(
				f"Package contains application type '{applicationDisplayName}' of version '{ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(self.packageFile))}'.\n"
				f"Your current version is '{ESTUtilities.normalizeVersion(currentVersion)}'.")

			if self.interactive:
				if versionCompare == 0:
					ESTUtilities.printWarning("This is the same version.")
				else:
					ESTUtilities.printError("The update package is an older version!")
				print("Install anyway? (N/y, Blank => Cancel):")
				choice = input(" > ").strip().lower()
				if choice.startswith(("y", "j")):
					return True
			else:
				print(f"Obsolete update of {applicationDisplayName} is skipped.")

		elif self.checkForInitialInstallation(installationBaseDirectory):
			print(f"Package contains new application '{applicationDisplayName}'")
			if self.interactive:
				print("Initially install this application? (N/y, Blank => Cancel):")
				choice = input(" > ").strip().lower()
				if choice.startswith(("y", "j")):
					# ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/" + releaseSubdirectoryName, applicationUserName)
					return True
		else:
			print(
				f"Found update package '{self.packageFile.name}' for application '{applicationDisplayName}' with version '{ESTUtilities.normalizeVersion(ESTUtilities.getVersionFromFilename(self.packageFile))}'.\nYour current version is '{ESTUtilities.normalizeVersion(currentVersion)}'.")
			if self.interactive:
				print("Continue with update? (Y/n, Blank => Yes):")
				choice = input(" > ").strip().lower()
				if choice and not choice.startswith(("y", "j")):
					Environment.warnings.append(f"Update of '{applicationDisplayName}' canceled")
					updateLogger.debug("Update of '" + applicationName + "' canceled" + "\n")
					return False
				else:
					return True

			else:
				if not self.checkForInitialInstallation():
					self.cleanupOldVersions(installationBaseDirectory,
									   ESTUtilities.normalizeVersion(
						                   ESTUtilities.getVersionFromFilename(self.packageFile)),
									   ESTUtilities.normalizeVersion(currentVersion))

	def cleanupOldVersions(self,releaseDirectoryPath: str, updateVersion: str, currentVersion: str):
		if os.path.isdir(releaseDirectoryPath):
			availableVersionsToDelete: list[str] = []
			for directoryOrFileName in os.listdir(releaseDirectoryPath):
				version = ESTUtilities.getVersionFromFilename(directoryOrFileName)
				if version is not None and (currentVersion is None or ESTUtilities.compareVersions(version,
				                                                                                   currentVersion) != 0) and version not in availableVersionsToDelete:
					availableVersionsToDelete.append(version)
			availableVersionsToDelete.sort()

			availableMajorMinorVersions = self.getAvailableMajorMinorVersions(availableVersionsToDelete)
			availableMajorMinorVersions.sort()
			availableMajorMinorVersions = list(reversed(availableMajorMinorVersions))

			for index in range(0, Environment.numberOfMajorMinorVersionsToKeep):
				if len(availableMajorMinorVersions) >= index + 1:
					maximumVersion = self.getMaximumVersionForMajorMinorVersion(availableMajorMinorVersions[index],
					                                                       availableVersionsToDelete)
					if maximumVersion is not None:
						availableVersionsToDelete.remove(maximumVersion)

			for index in range(0, Environment.updateVersionHotfixVersionsToKeep):
				maximumMajorMinorMicroVersion = self.getMaximumVersionForMajorMinorMicroVersion(updateVersion,
				                                                                           availableVersionsToDelete)
				if maximumMajorMinorMicroVersion is not None:
					availableVersionsToDelete.remove(maximumMajorMinorMicroVersion)

			for directoryOrFileName in os.listdir(releaseDirectoryPath):
				version = ESTUtilities.getVersionFromFilename(directoryOrFileName)
				absolutePath = Path(releaseDirectoryPath, directoryOrFileName)
				if version in availableVersionsToDelete:
					print("Cleanup of old version '{version}': {absolutePath}\n")
					updateLogger.info(f"Cleanup of old version 'version': {absolutePath}\n")
					if absolutePath.is_dir():
						shutil.rmtree(absolutePath)
					else:
						os.remove(absolutePath)

	def getAvailableMajorMinorVersions(versionList: list[str]):
		majorMinorVersions = []
		for version in versionList:
			versionParts = version.split(".")
			if len(versionParts) >= 2:
				majorMinorVersion = versionParts[0] + "." + versionParts[1]
				if not majorMinorVersion in majorMinorVersions:
					majorMinorVersions.append(majorMinorVersion)
		return majorMinorVersions

	def getMaximumVersionForMajorMinorVersion(majorMinorVersion: str, versionList: list[str]):
		versionList.sort()
		versionList = list(reversed(versionList))

		maximumMajorMinorVersion = None
		majorMinorVersionParts = majorMinorVersion.split(".")
		if len(majorMinorVersionParts) >= 2:
			for version in versionList:
				versionParts = version.split(".")
				if len(versionParts) >= 2 and int(versionParts[0]) == int(majorMinorVersionParts[0]) and int(
						versionParts[1]) == int(majorMinorVersionParts[1]):
					if maximumMajorMinorVersion is None or ESTUtilities.compareVersions(version,
					                                                                    maximumMajorMinorVersion) < 0:
						maximumMajorMinorVersion = version
		return maximumMajorMinorVersion

	def getMaximumVersionForMajorMinorMicroVersion(majorMinorMicroVersion: str, versionList: list[str]):
		versionList.sort()
		versionList = list(reversed(versionList))

		maximumMajorMinorVersion = None
		majorMinorMicroVersionParts = majorMinorMicroVersion.split(".")
		if len(majorMinorMicroVersionParts) >= 3:
			for version in versionList:
				versionParts = version.split(".")
				if len(versionParts) >= 3 and int(versionParts[0]) == int(majorMinorMicroVersionParts[0]) and int(
						versionParts[1]) == int(majorMinorMicroVersionParts[1]) and int(versionParts[2]) == int(
						majorMinorMicroVersionParts[2]):
					if maximumMajorMinorVersion is None or ESTUtilities.compareVersions(version,
					                                                                    maximumMajorMinorVersion) < 0:
						maximumMajorMinorVersion = version
		return maximumMajorMinorVersion

	def promptRestartIfNecessary(self):
		if self.packageType in restartTriggerApplicationTypes:
			if self.interactive:
				print(Colors.YELLOW + "To let the new version take effect, you must exit and restart this program!" + Colors.DEFAULT)
				print("Do you want to exit and restart now? (Y/n, Blank => Exit):")
				choice = input(" > ").strip().lower()
				if not choice or choice.startswith(("y", "j")):
					restart(Environment.scriptFilePath, *sys.argv)
			else:
				print(Colors.YELLOW + "Restart for Update..." + Colors.DEFAULT)
				restart(Environment.scriptFilePath, *sys.argv)

	def detectNecessaryApplicationName(self) -> str | None:
		return ""

	def detectNecessaryApplicationUserName(self):
		return Environment.getFrontendApplicationUserName()

	@abstractmethod
	def process(self) -> None:
		pass

	@staticmethod
	def getApplicationDisplayName(applicationName: str) -> str:
		if applicationName == "tomcat":
			return "Apache Tomcat"
		elif applicationName == "openemm-backend":
			return "backend"
		elif applicationName in ["backend-runtime","backend-startup","backend-one", "backend-console", "backend-rdir", "backend-merger", "backend-mailer", "backend-mailloop"]:
			return applicationName
		elif applicationName in ["runtime", "backend", "manual", "statistics", "webservices", "rdir", "manual"]:
			return applicationName.capitalize()
		elif applicationName == "openemm" or applicationName in ["emm", "complete", "frontend", "gui"] and Environment.isOpenEmmServer:
			return "OpenEMM"
		elif applicationName in ["complete", "frontend", "gui"]:
			return Environment.applicationName
		else:
			return applicationName.upper()

	def getInstallBaseDirectory(self, applicationName: str, username: str):
		return ESTUtilities.getUserHomeDirectory(username) + "/release/" + applicationName

	def createReleaseSubDirectoryIfNotExists(self, applicationName: str, username: str) -> str | NoReturn:
		installBaseDirectoryPath = ESTUtilities.getUserHomeDirectory(username) + "/release/" + applicationName

		pathlib.Path(installBaseDirectoryPath).mkdir(parents=True, exist_ok=True)
		shutil.chown(installBaseDirectoryPath, username)

		return installBaseDirectoryPath

	def checkForInitialInstallation(self,releaseSubDirectory=None) -> bool:
		if not releaseSubDirectory:
			releaseSubDirectory = self.getInstallBaseDirectory(self.detectNecessaryApplicationName(), self.detectNecessaryApplicationUserName())
		if not os.path.isdir(releaseSubDirectory):
			return True
		else:
			return False

	@staticmethod
	def fail(errorMessage: str, informativeMessage: str | None=None) -> NoReturn:
		Environment.errors.append(errorMessage)
		updateLogger.exception(errorMessage)
		raise PackageInstallationException(informativeMessage or "During Package Installation an error occurred: ", errorMessage)

	def relinkApplicationVersion(self, releaseBaseDir: str | Path, releasePackagePath: str | Path, createLinkInWebapps=False) -> None:
		updateLogger.debug("Updating Application Links")
		packagePath = pathlib.Path(releasePackagePath)
		releaseBaseDir = pathlib.Path(releaseBaseDir)

		previousVersion = releaseBaseDir / "previous"
		currentVersion  = releaseBaseDir / "current"
		previousVersion.unlink(missing_ok=True)
		if currentVersion.exists():
			previousVersion.symlink_to(currentVersion.with_name("previous"), target_is_directory=True)

		currentVersion.unlink(missing_ok=True)
		currentVersion.symlink_to(packagePath,target_is_directory=True)

		if createLinkInWebapps:
			applicationLinkPath = pathlib.Path(self.userHomeDirectory) / "webapps" / self.getReleaseSubDirectory()
			if applicationLinkPath.is_symlink():
				applicationLinkPath.unlink()
			elif applicationLinkPath.is_dir():
				shutil.rmtree(applicationLinkPath)

			applicationLinkPath.symlink_to(currentVersion, True)

	def getReleaseSubDirectory(self):
		return ""


class BackendPackageHandler(PackageHandler):
	def __init__(self, packageType: PackageType, packageFile: str, interactive: bool):
		super().__init__(packageType, packageFile, interactive)
		self.operationalHomeDirectory: Path | None = None

	def getReleaseSubDirectory(self):
		if self.packageType in (PackageType.BACKEND_MAILER, PackageType.BACKEND_MAILLOOP, PackageType.BACKEND_MERGER):
			return ""
		elif self.packageType in (PackageType.BACKEND_CONSOLE, PackageType.BACKEND_RDIR, PackageType.BACKEND_ONE, PackageType.OPENEMM_BACKEND):
			return "backend"
		else:
			return "backend"

	def getInstallBaseDirectory(self, applicationName: str, username: str):
		return ESTUtilities.getUserHomeDirectory(username) + "/release/" + self.getReleaseSubDirectory()

	def detectNecessaryApplicationName(self) -> str:
		return {
			PackageType.BACKEND_RUNTIME: "backend-runtime",
			PackageType.BACKEND_ONE: "backend-one",
			PackageType.BACKEND_CONSOLE: "backend-console",
			PackageType.BACKEND_RDIR: "backend-rdir",
			PackageType.BACKEND_MERGER: "backend-merger",
			PackageType.BACKEND_MAILER: "backend-mailer",
			PackageType.BACKEND_MAILLOOP: "backend-mailloop"
		 }.get(self.packageType, "backend")

	def detectNecessaryApplicationUserName(self):
		if Environment.isOpenEmmServer:
			return "openemm"
		elif Environment.isEmmRdirServer and self.packageType in (PackageType.BACKEND_RUNTIME, PackageType.BACKEND_RDIR):
			return Environment.getRdirApplicationUserName()
		elif Environment.isEmmMergerServer and self.packageType in (PackageType.BACKEND_MERGER, PackageType.BACKEND_RUNTIME):
			return "merger"
		elif Environment.isEmmMailerServer and self.packageType in (PackageType.BACKEND_MAILER, PackageType.BACKEND_RUNTIME):
			return "mailout"
		elif Environment.isEmmMailloopServer and self.packageType in(PackageType.BACKEND_MAILLOOP, PackageType.BACKEND_RUNTIME):
			return "mailloop"
		else:
			return Environment.getFrontendApplicationUserName()

	def unpackBackendWhereNeeded(self, applicationUserName, releaseSubDirectoryName):
		if self.operationalHomeDirectory.parts[1] == "root":
			applicationUserName = "root"
			unpackingTool = ESTUtilities.unTarGzFileAsRoot
		elif not os.access(self.operationalHomeDirectory, os.R_OK):
			unpackingTool = ESTUtilities.unTarGzFileAsRoot
		else:
			unpackingTool = ESTUtilities.unTarGzFile

		releaseDirectoryFilePath = self.createReleaseSubDirectoryIfNotExists(releaseSubDirectoryName,
		                                                                     applicationUserName)
		releasePackagePath = Path(releaseDirectoryFilePath) / (
					"V" + ESTUtilities.getVersionFromFilename(str(self.packageFile)))

		updateLogger.debug(f"Unpacking {self.packageType} into the directory {releaseDirectoryFilePath}")

		unpackingTool(self.packageFile, releaseDirectoryFilePath)
		return Path(releasePackagePath)

	def getOperationalHomeDirectory(self, applicationUserName: str):
		homeDirectory = Path(ESTUtilities.getUserHomeDirectory(applicationUserName))
		if homeDirectory.exists():
			return homeDirectory
		else:
			return Path("/root")

	def process(self) -> None:
		applicationUserName = self.detectNecessaryApplicationUserName()
		releaseSubDirectory = self.getReleaseSubDirectory()
		if not userExists(applicationUserName):
			self.fail("Necessary User for this application is missing: " + applicationUserName)
		self.operationalHomeDirectory = self.getOperationalHomeDirectory(applicationUserName)
		releasePackagePath = self.unpackBackendWhereNeeded(applicationUserName, releaseSubDirectory)
		self.stopBackend()
		self.relinkApplicationVersion(releasePackagePath.parent, releasePackagePath)
		getattr(Environment.applicationVersions, applicationUserName.lower()).refresh()
		self.postProcess()
		self.startBackend()

	def postProcess(self) -> None:
		backendBinDir = self.operationalHomeDirectory / "bin"
		currentBinDir = self.operationalHomeDirectory / "release/current/bin"

		if not Environment.isOpenEmmServer and not any(
				(backendBinDir.is_symlink(), backendBinDir.is_dir(), currentBinDir.exists())):
			backendBinDir.symlink_to(currentBinDir, target_is_directory=True)

		self.promptRestartIfNecessary()

	def backendSH(self, command):
		applicationUserName = self.detectNecessaryApplicationUserName()
		backendTool = Path(applicationUserName, "bin/backend.sh")
		if backendTool.exists():
			if ESTUtilities.hasRootPermissions():
				result = subprocess.run(["su", "-c", f"{backendTool} {command}", applicationUserName], capture_output=True,
				                        text=True)
			else:
				result = subprocess.run([backendTool, command], capture_output=True, text=True)

			for line in result.stdout.splitlines():
				logger.info(line)
			for line in result.stderr.splitlines():
				logger.error(line)

	def startBackend(self):
		print("Starting with: backend.sh")
		self.backendSH("start")

	def stopBackend(self):
		print("Stopping with: backend.sh")
		self.backendSH("stop")

class ComponentPackageHandler(PackageHandler):
	def getInstallBaseDirectory(self, applicationName: str, username: str|None):

		if applicationName == "tomcat":
			componentHomeParent = ESTUtilities.getParentPath(Environment.catalinaHome) if Environment.catalinaHome else None
		elif applicationName == "java":
			componentHomeParent = ESTUtilities.getParentPath(Environment.javaHome) if Environment.javaHome else None
		else:
			return None
		for componentPath in filter(None,[componentHomeParent, Environment.optDefaultPath, Environment.optDefaultPathSecondary]):
			if ESTUtilities.writePathIfWritable(componentPath):
				return componentPath
		return None

	def postProcess(self) -> None:
		if self.packageFile.exists() and self.packageFile.is_relative_to('/tmp'):
			self.packageFile.unlink()


	def detectNecessaryApplicationName(self) -> str | None:
		if self.packageType is PackageType.JAVA:
			return "java"
		elif self.packageType is PackageType.TOMCAT:
			return "tomcat"
		else:
			return None


	def relinkComponent(self, applicationName: str, updatedApplicationDirectoryPath: str | Path):
		componentHome: Path
		if applicationName == "java":
			componentHome = Path(Environment.javaHome)
		elif applicationName == "tomcat":
			componentHome = Path(Environment.catalinaHome)
		else:
			self.fail(f"Handling of Application {applicationName} seems not to be implemented")

		updatedApplicationPath = Path(updatedApplicationDirectoryPath)
		if updatedApplicationPath.is_symlink():
			updatedApplicationPath.unlink()
			updatedApplicationPath.symlink_to(Environment.javaHome)
			Environment.rebootNeeded = True

		if componentHome.is_symlink():
			componentHome.unlink()
			componentHome.symlink_to(updatedApplicationPath)
			Environment.rebootNeeded = True
			updateLogger.debug(f"Created new application link '{componentHome}'")
		elif componentHome.is_file() or componentHome.is_dir():
			errorText = f"Cannot update {applicationName.upper()} in '{componentHome}'"
			Environment.errors.append(errorText)
			self.fail(errorText)
		else:
			alternativeComponentPath = updatedApplicationPath.parent / applicationName
			alternativeComponentPath.symlink_to(updatedApplicationPath)
			Environment.rebootNeeded = True
			updateLogger.debug(f"Created new application link '{alternativeComponentPath}'")

	def process(self) -> None:
		applicationName = self.detectNecessaryApplicationName()
		applicationUserName = self.detectNecessaryApplicationUserName()
		installBaseDirectoryPath = self.getInstallBaseDirectory(applicationName, applicationUserName)
		if not installBaseDirectoryPath:
			self.fail("Component for Update is unknown")


		if not installBaseDirectoryPath:
			errorText = f"The {applicationName.upper()} installation directory '{installBaseDirectoryPath}' is not writable for the current user '{Environment.username}'. So only users with root permissions (sudo) may install or update {applicationName.upper()} there.\nPlease restart the tool with root permissions (sudo) for installation or update there."
			Environment.errors.append(errorText)
			self.fail(errorText)
		self.updateEnvironmentComponentPath(applicationName, installBaseDirectoryPath)

		extractedArchive = ESTUtilities.unTarGzFile(self.packageFile, str(installBaseDirectoryPath))
		updateLogger.debug(f"Extracted file '{self.packageFile}' to '{extractedArchive}'")

		self.adjustUserAndGroupPermissions(installBaseDirectoryPath)

		self.relinkComponent(applicationName, extractedArchive)
		self.refreshApplicationVersion(applicationName)
		self.postProcess()

	def updateEnvironmentComponentPath(self, applicationName: str, installBaseDirectoryPath: str | Path):
		environmentProperties = ESTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath) or {}
		if applicationName == "tomcat":
			Environment.catalinaHome = str(installBaseDirectoryPath) + "/tomcat"
			environmentProperties["CATALINA_HOME"] = Environment.catalinaHome
		elif applicationName == "java":
			Environment.javaHome = str(installBaseDirectoryPath) + "/java"
			environmentProperties["JAVA_HOME"] = Environment.javaHome
		else:
			return
		ESTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, self.detectNecessaryApplicationName(),
		                                             environmentProperties)

	@staticmethod
	def adjustUserAndGroupPermissions(installBaseDirectoryPath):
		if ESTUtilities.hasRootPermissions():
			if Environment.isOpenEmmServer:
				ESTUtilities.chownRecursive(installBaseDirectoryPath, "openemm")
			else:
				ESTUtilities.chownRecursive(installBaseDirectoryPath, "root", "agnitas")


class RuntimePackageHandler(PackageHandler):
	def process(self) -> None:
		applicationUserName = self.detectNecessaryApplicationUserName()
		userHome = Path(ESTUtilities.getUserHomeDirectory(applicationUserName))
		self.createMissingDirectories(applicationUserName, userHome)
		self.promptBackup()
		self.createTemporarySystemFileBackup()
		self.cleanRuntimeData(userHome)

		ESTUtilities.unTarGzFile(self.packageFile, userHome, applicationUserName)

		self.restoreTemporarySystemFileBackup()

		serverXML = userHome / "tomcat/conf/server.xml"
		serverXMLTemplate = userHome / "tomcat/conf/server.xml.template"

		Environment.messages.append(Environment.applicationName + " runtime was updated")

		originalToolPath = userHome / "tomcat/bin" / Environment.restartToolName
		newToolPath = userHome / "bin" / Environment.restartToolName

		originalEmmShPath = userHome / "bin/emm.sh"
		newEmmShPath = userHome / "tomcat/bin/emm.sh"

		confPath = userHome / "conf"
		logsPath = userHome / "logs"

		managerPath = userHome / "webapps/manager"
		optTomcatPath = userHome / "/opt/tomcat"

		# Create needed links, if missing
		if not newToolPath.is_file() and not newToolPath.is_symlink() and originalToolPath.is_file():
			newToolPath.symlink_to(originalToolPath)
		if not newEmmShPath.is_file() and not newEmmShPath.is_symlink() and originalEmmShPath.is_file():
			newEmmShPath.symlink_to(originalEmmShPath)
		if not confPath.is_symlink() and not confPath.is_dir():
			confPath.symlink_to(userHome / "tomcat/conf")
		if not logsPath.is_symlink() and not logsPath.is_dir():
			logsPath.symlink_to(userHome / "tomcat/logs")

		if managerPath.is_symlink() and optTomcatPath.is_dir():
			managerPath.unlink()
			managerPath.symlink_to("opt/tomcat/webapps/manager")
			#logfile.write("Created new link for tomcat manager app" + "\n")

		# Create server.xml from template, if missing
		if not serverXML.is_file() and serverXMLTemplate.is_file():
			shutil.copy(serverXMLTemplate, serverXML)
			ESTUtilities.chown(serverXML, applicationUserName, applicationUserName)
			ESTUtilities.manageTlsCertificateForTomcat(
				userHome / "etc/ssl",
				userHome / "tomcat/conf/server.xml",
				Environment.applicationName)
			# logfile.write("Created file '" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/server.xml" + "'\n")

		# Read new version info
		Environment.applicationVersions.runtime.refresh()
		self.promptRestartIfNecessary()

	@staticmethod
	def createMissingDirectories(userName: str, userHomePath: Path):
		if not (userHomePath / "temp").is_dir():
			ESTUtilities.createDirectory(userHomePath / "temp", userName)
		if not (userHomePath / "bin").is_dir():
			ESTUtilities.createDirectory(userHomePath / "bin", userName)

	def promptBackup(self):
		print(
			"You are currently in the process of conducting an update. Do you want to make a backup of directories?)\n"
			"Please enter the absolute path of the desired directories, separated by space: (Blank => Cancel)")
		choice = input("> ")
		backup_choices = shlex.split(choice)

		if backup_choices:
			output_path = self.createTarGZSystemFileBackup(backup_choices)
			if output_path:
				Environment.messages.append("Created Backup: " + str(output_path))
				print("Created Backup:", output_path)  # In case of the user restarting the runtime immediately after update, environment message won't be shown

	@staticmethod
	def createTarGZSystemFileBackup(backup_choices):
		""" Create temporary backup of system specific configuration files and folders, compressed into a tar file"""
		applicationUserName = Environment.getApplicationUsername()
		home = Path(ESTUtilities.getUserHomeDirectory(applicationUserName))
		filename = "backup-emm-update-" + time.strftime("%Y%m%d-%H%M%S") + ".tar.gz"
		output_path = home / filename
		unixPathValidationPattern = re.compile(r"^(/[^/ ]*)+/?$|^\.(/[^/ ]*)+/?$|^\.\./([^/ ]*/)*[^/ ]*$")

		validBackupChoices = [path for path in backup_choices if unixPathValidationPattern.match(path)]
		higherAccessBackupChoices = [path for path in validBackupChoices if
									 not ESTUtilities.checkWriteAccessibility(path)]

		if validBackupChoices:
			print("Depending on the size of the provided directories, this might take some time...")
			if higherAccessBackupChoices:
				with tempfile.TemporaryDirectory() as backupTempDirectory:
					try:
						subprocess.run(
							["sudo", "cp", "--parents", "--no-preserve", "ownership", "-r", *higherAccessBackupChoices,
							 "-t", backupTempDirectory], check=False)
						subprocess.run(["sudo", "chown", "-R",
										str(pwd.getpwnam(Environment.username).pw_uid) + ":" + str(
											pwd.getpwnam(Environment.username).pw_uid), backupTempDirectory],
									   check=True)
						subprocess.run(["sudo", "-k"], check=True)  # expire sudo session immediately
					except subprocess.CalledProcessError:
						return None

					with tarfile.open(output_path, "w:gz") as tar:
						for path in list(
								filter(lambda item: item not in higherAccessBackupChoices, validBackupChoices)):
							print("Backing up: " + path)
							tar.add(path, arcname=os.path.relpath(path, home))

						base_dir = pathlib.Path(backupTempDirectory)
						backupChoices = [path for path in higherAccessBackupChoices if
										 (base_dir / path.lstrip("/")).exists() or (
													 base_dir / path.lstrip("/")).is_symlink()]
						if backupChoices:
							print("Backing up: ", *backupChoices)
							tar.add(backupTempDirectory, arcname=".")
			else:
				with tarfile.open(output_path, "w:gz", dereference=True) as tar:
					for path in validBackupChoices:
						print("Backing up: " + path)
						tar.add(path, arcname=os.path.relpath(path, home))
			return output_path
		else:
			print("No valid paths for System File Backup were provided")
			Environment.errors.append("Backup Failed: No valid paths for System File Backup were provided")
			return None

	@staticmethod
	def createTemporarySystemFileBackup():
		"""Create temporary backup of system specific configuration files"""

		applicationUserName = Environment.getApplicationUsername()
		applicationUserHome = Path(ESTUtilities.getUserHomeDirectory(applicationUserName))
		temporaryBackupPath = Path("/tmp/EmmConfigurationBackup")
		if temporaryBackupPath.is_dir():
			shutil.rmtree(temporaryBackupPath)
		temporaryBackupPath.mkdir()

		for systemSpecificConfigFile in Environment.systemSpecificConfigFiles:
			systemSpecificConfigFilePath = Path(applicationUserHome / systemSpecificConfigFile)
			if systemSpecificConfigFilePath.is_file():
				systemSpecificBackupFilePath = temporaryBackupPath / systemSpecificConfigFile
				if systemSpecificBackupFilePath.is_file():
					systemSpecificBackupFilePath.unlink()
				systemSpecificBackupFilePath.mkdir(parents=True, exist_ok=True)
				shutil.copy(systemSpecificConfigFilePath, systemSpecificBackupFilePath)

		for directoryToPreserve in Environment.directoriesToPreserve:
			preservableDirectoryPath = applicationUserHome / directoryToPreserve
			if preservableDirectoryPath.is_dir():
				backupDestinationPath =temporaryBackupPath / directoryToPreserve
				if backupDestinationPath.is_dir():
					shutil.rmtree(backupDestinationPath)
				shutil.move(preservableDirectoryPath, backupDestinationPath)

	@staticmethod
	def restoreTemporarySystemFileBackup():
		""" Restore system specific configuration files from temporary backup"""

		applicationUserName = Environment.getApplicationUsername()
		applicationUserHome = Path(ESTUtilities.getUserHomeDirectory(applicationUserName))
		temporaryBackupPath = Path("/tmp/EmmConfigurationBackup")
		if temporaryBackupPath.is_dir():
			for systemSpecificConfigFile in Environment.systemSpecificConfigFiles:
				systemSpecificBackupFilePath = temporaryBackupPath / systemSpecificConfigFile
				systemSpecificDestinationFilePath = applicationUserHome / systemSpecificConfigFile
				if systemSpecificBackupFilePath.is_file() and not systemSpecificDestinationFilePath.is_file():
					shutil.copy(systemSpecificBackupFilePath, systemSpecificDestinationFilePath)

			for preservedDirectory in Environment.directoriesToPreserve:
				preservedDirectoryBackupFilePath = temporaryBackupPath / preservedDirectory
				targetDestinationDirectoryFilePath = applicationUserHome / preservedDirectory
				if preservedDirectoryBackupFilePath.is_dir():
					if targetDestinationDirectoryFilePath.is_dir():
						shutil.rmtree(targetDestinationDirectoryFilePath)
					shutil.move(preservedDirectoryBackupFilePath, targetDestinationDirectoryFilePath)

			shutil.rmtree(temporaryBackupPath)

	def detectNecessaryApplicationUserName(self):
		if Environment.isOpenEmmServer:
			return "openemm"
		elif Environment.isEmmRdirServer:
			return Environment.getRdirApplicationUserName()
		elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
			return Environment.getFrontendApplicationUserName()
		else:
			return Environment.getFrontendApplicationUserName()
	def detectNecessaryApplicationName(self) -> str | None:
		return "runtime"

	@staticmethod
	def cleanRuntimeData(userHome):
		tomcatPath = userHome / "tomcat"
		webappsLeaveEmptyPath = userHome / "webapps-leave-empty"
		try:
			# Delete old runtime data
			if tomcatPath.is_symlink():
				tomcatPath.unlink()
			if webappsLeaveEmptyPath.is_dir():
				shutil.rmtree(webappsLeaveEmptyPath)
			if ESTUtilities.checkWriteAccessibility(userHome):
				for listedFile in userHome.iterdir():
					if re.search("^tomcat.*$", listedFile.name) and listedFile.is_dir():
						shutil.rmtree(userHome / listedFile)
		except NotImplementedError:
			if not tomcatPath.is_symlink():
				tomcatPath.symlink_to(userHome / "tomcat10")
			if not webappsLeaveEmptyPath.is_dir():
				webappsLeaveEmptyPath.mkdir()


class FrontendPackageHandler(PackageHandler):
	def getReleaseSubDirectory(self) -> str:
		if self.packageType in (PackageType.FRONTEND_EMM, PackageType.FRONTEND_RDIR, PackageType.FRONTEND_STATISTICS, PackageType.FRONTEND_WEBSERVICES, PackageType.FRONTEND_RDIR, PackageType.MANUAL, PackageType.REST_MCP):
			return self.detectNecessaryApplicationName()
		else:
			return "backend"

	def detectNecessaryApplicationName(self) -> str:
		if self.packageType is PackageType.FRONTEND_STATISTICS:
			return "statistics"
		elif self.packageType is PackageType.FRONTEND_WEBSERVICES:
			return "webservices"
		elif self.packageType is PackageType.FRONTEND_RDIR:
			return "rdir"
		elif self.packageType is PackageType.REST_MCP:
			return "rest-mcp"
		elif self.packageType is PackageType.MANUAL:
			return "manual"
		elif self.packageType is PackageType.FRONTEND_EMM:
			return "emm"
		else:
			return Environment.getFrontendApplicationUserName()

	@staticmethod
	def executeDatabaseUpdates(executionBaseDirectory: str | Path):
		if not FrontendPackageHandler.getDBConnectivity():
			errorMsg = "Failed to confirm DB Connectivity. Execution of missing updates could not be carried out.\nPlease check your dbcfg parameters."
			Environment.errors.append(errorMsg)
			logger.error(errorMsg)
			return
		elif DbConnector.emmDbVendor in ["mariadb", "postgres"]:
			updateScriptPath = Path(executionBaseDirectory) / "WEB-INF/sql" / DbConnector.emmDbVendor
		else:
			updateScriptPath = Path(executionBaseDirectory) / "WEB-INF/sql/oracle"

		updateLogger.debug("Starting execution of missing updates.")
		DbConnector.executeMissingUpdates(updateScriptPath)

	@staticmethod
	def getDBConnectivity():
		return DbConnector.emmDbVendor is not None

	@staticmethod
	def installNPMPackages(directory: str | Path, user:str):
		npmExecutionDirectory =  Path(directory) / "WEB-INF"
		try:
			subprocess.run(["npm","install"], cwd=str(npmExecutionDirectory), user=user, stdout=subprocess.PIPE, check=True)
		except CalledProcessError as npmError:
			updateLogger.debug(f"Running npm install returned: {npmError}")

	def detectNecessaryApplicationUserName(self):
		return Environment.getFrontendApplicationUserName()

	def process(self) -> None:
		applicationName = self.detectNecessaryApplicationName()
		applicationUserName = self.detectNecessaryApplicationUserName()
		self.userHomeDirectory = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName())

		releaseSubDirectory = self.getReleaseSubDirectory()
		if not self.getDBConnectivity():
			if applicationName == "emm":
				self.fail("Mandatory database configuration not found")
			else:
				self.fail("Mandatory database client is NOT available, but needed for frontend application update")

		releaseDirectoryFilePath = self.createReleaseSubDirectoryIfNotExists(releaseSubDirectory, applicationUserName)
		releasePackagePath = pathlib.Path(releaseDirectoryFilePath) / Path(self.packageFile).stem.removesuffix(".tar")
		cleanupNeeded = releasePackagePath.exists() and any(releasePackagePath.iterdir())

		if cleanupNeeded:
			updateLogger.debug("Removing previous installation remnants from identical package version.")
			shutil.rmtree(releasePackagePath)
		packageVersion = ESTUtilities.getVersionFromFilename(str(self.packageFile.resolve()))
		updateLogger.debug(f"Unpacking: {self.packageFile} into {releaseDirectoryFilePath}")
		ESTUtilities.unTarGzFile(self.packageFile, releaseDirectoryFilePath)

		self.relinkApplicationVersion(releaseDirectoryFilePath, releasePackagePath, True)

		if applicationName == "emm":
			updateLogger.debug("Triggering Installation of npm packages.")
			self.installNPMPackages(releasePackagePath, applicationUserName)
			updateLogger.debug("Check and Execute necessary migration")
			self.handleFrontendDatabaseMigration(packageVersion)
			updateLogger.debug("Carrying over session timeout settings from previous version.")
			self.preserveSessionTimeoutSettings(releasePackagePath)
			updateLogger.debug("Further check and repair of potentially missing DB structure")
			self.handleDatabaseStructure(releasePackagePath)
			updateLogger.debug("Executing missing Database Updates")
			self.executeDatabaseUpdates(releasePackagePath)
		self.refreshApplicationVersion(applicationName)

	def handleFrontendDatabaseMigration(self, version: str):
		if version:
			majorVersion, minorVersion, microVersion = map(int, version.split(".")[:3])
			versionToCheck = None
			if (minorVersion, microVersion) == (4, 0):
				versionToCheck = str(majorVersion - 1) + ".10.000"
			elif (minorVersion, microVersion) == (10, 0):
				versionToCheck = str(majorVersion - 1) + ".04.000"
			if versionToCheck and DbConnector.checkTableExists("release_log_tbl"):
				startupCount = DbConnector.selectValue("SELECT COUNT(*) FROM release_log_tbl WHERE application_name = 'EMM'") or 0

				sql = "SELECT COUNT(*) FROM release_log_tbl WHERE application_name = ?"
				if DbConnector.emmDbVendor in ["oracle", "postgres"]:
					sqlCondition = " AND REGEXP_LIKE(version_number, '^0*' || ? || '.0*' || ? || '.000(.[0-9]+)*$')"
				else:
					sqlCondition = " AND version_number REGEXP CONCAT('^0*', ?, '.0*', ?, '.000(.[0-9]+)*$')"

				startupCountOfInstallVersion = DbConnector.selectValue(sql + sqlCondition,'EMM', majorVersion, minorVersion) or 0
				startupCountOfFormerLtsVersion = DbConnector.selectValue(sql + sqlCondition,'EMM', ESTUtilities.getMajorVersion(versionToCheck), ESTUtilities.getMinorVersion(versionToCheck)) or 0

				if startupCount > 0 and startupCountOfInstallVersion < 1 and startupCountOfFormerLtsVersion < 1:
					self.fail("EMM version " + versionToCheck + " needs to be started first because of data migration issues. Please install and run EMM version " + versionToCheck + " first before this EMM version " + version)

	@staticmethod
	def preserveSessionTimeoutSettings(updatedApplicationDirectoryPath: str | Path):
		webXMLPath = Path(updatedApplicationDirectoryPath) / "WEB-INF/web.xml"
		if not webXMLPath.exists():
			return
		pattern = re.compile(r"<session-timeout>(\d+)</session-timeout>")
		sessionTimeoutMinutes = list(chain.from_iterable(pattern.findall(line) for line in open(webXMLPath)))
		
		if sessionTimeoutMinutes:
			print(f"Preserving session timeout: {sessionTimeoutMinutes[0]}\n")
			updateLogger.debug("Preserving session timeout: " + sessionTimeoutMinutes[0] + "\n")
			text = webXMLPath.read_text()
			text = pattern.sub(f"<session-timeout>{sessionTimeoutMinutes[0]}</session-timeout>", text)
			webXMLPath.write_text(text)

	def handleDatabaseStructure(self, updatedApplicationDirectoryPath: str | Path):
		SQLDirectory = Path(updatedApplicationDirectoryPath) / "WEB-INF/sql"
		databaseOptions = {}
		for path in SQLDirectory.iterdir():
			if path.is_dir():
				databaseOptions[path.name] = path

		oracleFullDB = SQLDirectory / "oracle/emm-oracle-execute-initial-fulldb.sh"
		if not DbConnector.checkDbStructureExists():
			if DbConnector.emmDbVendor == "oracle":
				print("Creating basic database structure ...")
				sqlUpdateExecution = subprocess.run([oracleFullDB, DbConnector.dbcfgPropertiesFilePath])
				if sqlUpdateExecution.returncode != 0:
					raise Exception("SQL database updates caused an error.")

			elif DbConnector.emmDbVendor in ["mariadb", "postgres"]:
				print("Creating basic database structure ...")
				sqlUpdateLogfilePath = f"{self.userHomeDirectory}/release/log/emm-{DbConnector.emmDbVendor}-fulldb_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
				fullDBScripts: list[Path] = sorted(databaseOptions[DbConnector.emmDbVendor].glob("*fulldb*.sql"))
				for fullDBScript in fullDBScripts:
					fullDbScriptSuccess = DbConnector.executeSqlScriptFile(fullDBScript, sqlUpdateLogfilePath)
					if not fullDbScriptSuccess:
						if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
							with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
								sqlLogContent = sqlUpdateLogfile.read()
								print(str(sqlLogContent))
						self.fail("Error while executing full database script")
			else:
				self.fail("Database Vendor could not get identified for supported Systems.")

			if not DbConnector.checkDbStructureExists():
				self.fail("Error while executing full database script. EMM Db structure is still missing")
			else:
				DbConnector.storeJobQueueHostInDB(Environment.hostname, True)
				DbConnector.removeJobQueueHostFromDB("<hostname>[to be defined]")
				DbConnector.insertBirtKeysInDb(Environment.getFrontendApplicationUserName())

class BundledPackageHandler(PackageHandler):
	def detectNecessaryApplicationName(self):
		if "openemm" in self.packageFile.name:
			return "openemm"

	def promptInitialInstallation(self, applicationName) -> bool:
		return True

	def getInstallBaseDirectory(self, applicationName: str, username: str):
		baseDirectory = ESTUtilities.getUserHomeDirectory(username) + "/release/" + applicationName
		Path(baseDirectory).mkdir(exist_ok=True)
		return baseDirectory

	def process(self) -> None:
		with tempfile.TemporaryDirectory() as temporaryExtractionDirectory:
			extractionFilePath: Path = ESTUtilities.unTarGzFile(self.packageFile, temporaryExtractionDirectory)
			for application in reversed(sorted(extractionFilePath.iterdir(), key=lambda x: '-backend-' not in x.name)):
				if self.interactive:
					handler = detectTypeAndReturnHandler(str(application.absolute()), interactive=self.interactive)
					if handler.checkForInitialInstallation():
						if not handler.promptInstallation():
							continue
					handler.process()
				else:
					detectTypeAndReturnHandler(str(application.absolute()), interactive=self.interactive).process()

HANDLERS = {
	PackageType.RUNTIME: RuntimePackageHandler,
	PackageType.MANUAL: FrontendPackageHandler,
	PackageType.TOMCAT: ComponentPackageHandler,
	PackageType.JAVA: ComponentPackageHandler,
	PackageType.FRONTEND_EMM: FrontendPackageHandler,
	PackageType.FRONTEND_RDIR: FrontendPackageHandler,
	PackageType.FRONTEND_STATISTICS: FrontendPackageHandler,
	PackageType.FRONTEND_WEBSERVICES: FrontendPackageHandler,
	PackageType.REST_MCP: FrontendPackageHandler,
	PackageType.BACKEND_CONSOLE: BackendPackageHandler,
	PackageType.OPENEMM_BACKEND: BackendPackageHandler,
	PackageType.BACKEND_MAILER: BackendPackageHandler,
	PackageType.BACKEND_MAILLOOP: BackendPackageHandler,
	PackageType.BACKEND_MERGER: BackendPackageHandler,
	PackageType.BACKEND_ONE: BackendPackageHandler,
	PackageType.BACKEND_RDIR: BackendPackageHandler,
	PackageType.BACKEND_RUNTIME: BackendPackageHandler,
	PackageType.APPLICATION_BUNDLE: BundledPackageHandler
}

