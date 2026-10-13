import re
import os
import ssl
import datetime
import getpass
import logging
import tempfile
import urllib.request, urllib.error, urllib.parse
from pathlib import Path
from typing import Any
from urllib.parse import urlencode

from EST_lib.Environment import Environment
from EST_lib.License import License
from EST_lib import PackageInstaller, DbConnector
from EST_lib import ESTUtilities
from EST_lib.ESTUtilities import parse_header, printError, printWarning

logger = logging.getLogger(__name__)

newline = print


def downloadPackageWithPassword(downloadDestinationFilePath, authHandler):
	# Download update file
	print("Download started")
	downloadPageResponse = None
	try:
		opener = urllib.request.build_opener(authHandler, urllib.request.HTTPSHandler(context=ssl.SSLContext(ssl.PROTOCOL_TLS)))
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

			#logfile.write("Downloaded to file '" + downloadDestinationFilePath + "'" + "\n")
		downloadPageResponse.close()
		downloadPageResponse = None
	except:
		if downloadPageResponse is not None:
			downloadPageResponse.close()
		if os.path.isfile(downloadDestinationFilePath):
			os.remove(downloadDestinationFilePath)
		errorText = "Download of update package failed"
		Environment.errors.append(errorText)
		#logfile.write(errorText + "\n")
		if ESTUtilities.isDebugMode():
			logging.exception(errorText)
			raise
		return
	print("Download finished")

def probeAndSetupCloudPackageDownload(packageUrl):
	packageUrl = re.sub(r'/(download|$)', '', packageUrl)
	result: dict[str, Any] = {
		'packageDownloadID': packageUrl.split("/")[-1]
	}
	if not ESTUtilities.checkDownloadFileIsAvailable(Environment.agnitasCloudUrl + "/index.php/s/" + result.get('packageDownloadID','')):
		Environment.errors.append("Unknown update package id: " + result.get('packageDownloadID',''))
		return "CLOUDUPDATE"

	tryDownload = True
	packagePassword = ""
	while tryDownload:
		try:
			username = result.get('packageDownloadID')
			password = packagePassword
			passwordManager = urllib.request.HTTPPasswordMgrWithDefaultRealm()
			passwordManager.add_password(None, Environment.agnitasCloudUrl + Environment.agnitasCloudWebdavContext,
										 username, password)
			authhandler = urllib.request.HTTPBasicAuthHandler(passwordManager)
			opener = urllib.request.build_opener(authhandler,
												 urllib.request.HTTPSHandler(context=ssl.SSLContext(ssl.PROTOCOL_TLS)))
			with opener.open(
					Environment.agnitasCloudUrl + Environment.agnitasCloudWebdavContext) as downloadPageResponse:
				params = parse_header(downloadPageResponse.headers.get("Content-Disposition", ""))
				result['packageFilename'] = params["filename"]
				result['auth'] = authhandler
				downloadPageResponse.close()
				tryDownload = False
		except urllib.error.HTTPError as e:
			if e.code == 401:
				# Unauthorized
				if packagePassword != "":
					printError("Invalid password")
				print("Please enter password (Blank => Cancel):")
				packagePassword = getpass.getpass(" > ")
				if packagePassword == "":
					Environment.messages.append("Update canceled")
					return None
				else:
					tryDownload = True
			else:
				raise
		return result

def installFileFromCloud(packageUrl: str, interactive, alreadyOpenLogFile = None):
	#logfile.write("Update package url/id: " + packageUrl + "\n")
	packageDetails = probeAndSetupCloudPackageDownload(packageUrl)

	application = PackageInstaller.detectTypeAndReturnHandler(packageDetails.get('packageFilename'), interactive)
	applicationName = application.detectNecessaryApplicationName()
	applicationUserName = application.detectNecessaryApplicationUserName()
	destinationFilePath = Path(application.getInstallBaseDirectory(applicationName, applicationUserName), packageDetails.get('packageFilename'))

	downloadPackageWithPassword(destinationFilePath, packageDetails.get('auth'))
	application.packageFile = destinationFilePath
	if interactive:
		if application.promptInstallation():
			application.process()
	else:
		application.process()

	if Environment.errors is None or len(Environment.errors) == 0:
		Environment.messages.append(
			f"Update package file '{packageDetails.get('packageFilename')}' successfully deployed.")
	else:
		Environment.errors.append(
			f"Update package file '{packageDetails.get('packageFilename')}' deployed with errors.")


def siteUpdateMenuAction(actionParameters):
	print(Environment.applicationName, "Update via AGNITAS Website")

	print("Download and install latest runtime, frontend and backend packages? (N/y, Blank => Cancel):")
	choice = input(" > ")
	choice = choice.strip().lower()
	if choice.startswith(("y", "j")):
		installFileFromWebsite(True)

def parseDownloadablePackages(versionInfo: dict[str,list[str]]) -> dict[str,str]:
	initialInstall = not any((Environment.isOpenEmmServer, Environment.isEmmFrontendServer, Environment.isEmmStatisticsServer, Environment.isEmmWebservicesServer, Environment.isEmmConsoleRdirServer, Environment.isEmmRdirServer, Environment.isEmmMergerServer, Environment.isEmmMailerServer, Environment.isEmmMailloopServer))
	applicationServerCondition = initialInstall or any(
		(Environment.isOpenEmmServer, Environment.isEmmFrontendServer, Environment.isEmmStatisticsServer,
		 Environment.isEmmWebservicesServer, Environment.isEmmConsoleRdirServer, Environment.isEmmRdirServer))
	checkMapping: dict[str,bool] = {
		"runtime": applicationServerCondition,
		"java": applicationServerCondition,
		"tomcat": applicationServerCondition,
	}
	if Environment.isOpenEmmServer:
		checkMapping |= {
			"complete": Environment.isOpenEmmServer,
			"openemm": Environment.isOpenEmmServer,
			"backend-suse": Environment.osVendor == "Suse" and "backend-suse" in versionInfo,
			"frontend": "frontend" in versionInfo,
			"backend-suse|backend": "backend" in versionInfo,
			"manual": "manual" in versionInfo
		}
	else:
		checkMapping |= {
			"frontend-emm": (initialInstall or Environment.isEmmFrontendServer) and "frontend-emm" in versionInfo,
			"frontend-emm|emm": (initialInstall or Environment.isEmmFrontendServer) and "emm" in versionInfo,
			"frontend-rdir": (initialInstall or Environment.isEmmRdirServer) and "frontend-rdir" in versionInfo,
			"frontend-rdir|rdir": (initialInstall or Environment.isEmmRdirServer) and "rdir" in versionInfo,
			"frontend-statistics": (initialInstall or Environment.isEmmStatisticsServer) and "frontend-statistics" in versionInfo,
			"frontend-statistics|statistics": (initialInstall or Environment.isEmmStatisticsServer) and "statistics" in versionInfo,
			"frontend-webservices": (initialInstall or Environment.isEmmWebservicesServer) and "frontend-webservices" in versionInfo,
			"frontend-webservices|webservices": (initialInstall or Environment.isEmmWebservicesServer) and "webservices" in versionInfo,

			"backend-one": Environment.isEmmUnifiedServer and "backend-one" in versionInfo,
			"backend-one|backend-console": (initialInstall or any((Environment.isEmmFrontendServer,
			                                                      Environment.isEmmWebservicesServer,
			                                                      Environment.isEmmConsoleRdirServer))) and "backend-console" in versionInfo,
			"backend-one|backend-console|backend-rdir": Environment.isEmmRdirServer and "backend-rdir" in versionInfo,
			"backend-merger": (initialInstall or Environment.isEmmMergerServer) and "backend-merger" in versionInfo,
			"backend-mailer": (initialInstall or Environment.isEmmMailerServer) and "backend-mailer" in versionInfo,
			"backend-mailloop": (initialInstall or Environment.isEmmMailloopServer) and "backend-mailloop" in versionInfo,
			"manual": (initialInstall or Environment.isEmmFrontendServer) and "manual" in versionInfo
		}
	downloadFileUrlPaths = {}
	for key, value in checkMapping.items():
		if '|' in key:
			conditionItems = key.split('|')
			if any(downloadFileUrlPaths.get(element) for element in conditionItems[:-1]):
				continue
			else:
				key = conditionItems[-1]
		if value and versionInfo.get(key):
			downloadFileUrlPaths[key] = versionInfo[key][1]
	return downloadFileUrlPaths

def downloadPackageWithProgressBar(url: str, chunk_size: int = 8192, progressbarSize: int = 80):
	try:
		with urllib.request.urlopen(url) as response:
			with tempfile.NamedTemporaryFile(delete=False) as tmp_file:
				total_size = int(response.info().get_all("Content-Length")[0].strip())
				bytes_read = 0
				nextProgressBarUpdate: datetime.datetime | None = None
				while True:
					chunk = response.read(chunk_size)
					if not chunk:
						break
					tmp_file.write(chunk)
					bytes_read += len(chunk)
					now = datetime.datetime.now()
					if nextProgressBarUpdate is None or nextProgressBarUpdate < now:
						ESTUtilities.printProgressBar(total_size, bytes_read, progressbarSize)
						nextProgressBarUpdate = now + datetime.timedelta(seconds=1)
					ESTUtilities.printProgressBar(total_size, total_size, progressbarSize)

					logger.info(f"Downloaded to file '{tmp_file.name}'")
			return tmp_file.name
	except:
		errorText = "Download of update package failed"
		Environment.errors.append(errorText)
		logger.error(errorText)
		if ESTUtilities.isDebugMode():
			logging.exception(errorText)
			raise


def downloadPackage(downloadFileUrlPath: str, downloadDestinationFilePath: str | Path) -> bool:
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

			logger.debug(f"Downloaded to file '{downloadDestinationFilePath}'\n")
		downloadPageResponse.close()
		downloadPageResponse = None
		return True
	except:
		if downloadPageResponse is not None:
			downloadPageResponse.close()
		if os.path.isfile(downloadDestinationFilePath):
			os.remove(downloadDestinationFilePath)
		errorText = "Download of update package failed"
		Environment.errors.append(errorText)
		logger.debug(errorText + "\n")
		if ESTUtilities.isDebugMode():
			logging.exception(errorText)
			raise
		return False

def installFileFromWebsite(interactive: bool, alreadyOpenLogFile = None):

	versionInfo: dict[str,list[str]] = ESTUtilities.downloadVersionInfo(Environment.agnitasDownloadPathVersionInfo)

	if versionInfo is not None:
		downloadFileUrlPaths = {}
		if interactive:
			if "new-release-backend-runtime" in versionInfo:
				newReleaseInformation: list[str] = versionInfo.get("new-release-backend-runtime")
			elif "new-release-backend-startup" in versionInfo:
				newReleaseInformation: list[str] = versionInfo.get("new-release-backend-startup")
			elif "new-release-runtime" in versionInfo:
				newReleaseInformation: list[str] = versionInfo.get("new-release-runtime")
			else:
				newReleaseInformation = None
			if newReleaseInformation:
				newline()
				printWarning("A new release version is available: " + newReleaseInformation[0])
				if "new-release-notes" in versionInfo:
					releaseNotes = ESTUtilities.downloadTextFile(versionInfo["new-release-notes"][1])
					newline()
					print("Release notes: ")
					printWarning(releaseNotes)
					newline()

				print("Do you want to upgrade? (N/y, Blank => Cancel):")
				choice = input(" > ").strip().lower()

				if choice.startswith(('y','j')):
					logger.info("Release version upgrade: " + " ".join(newReleaseInformation))
					downloadFileUrlPaths["new-release-backend-runtime"] = versionInfo["new-release-backend-runtime"][1]
					newline()
					printWarning("Starting release upgrade")
					downloadAndInstallNewLicense()

			if "backend-runtime" in versionInfo or "backend-startup" in versionInfo:
				if not ESTUtilities.hasRootPermissions():
					Environment.warnings.append("The application backend-runtime must be updated by an user with root permission")
				else:
					pass

			if len(downloadFileUrlPaths) == 0:
				downloadFileUrlPaths = parseDownloadablePackages(versionInfo)

			if ESTUtilities.isDebugMode():
				print("downloadFileUrlPaths:")
				for downloadItemName, downloadFileUrlPath in downloadFileUrlPaths.items():
					print(str(downloadItemName) + ": " + str(downloadFileUrlPath))

		for downloadItemName, downloadFileUrlPath in downloadFileUrlPaths.items():
			if Environment.errors is not None and len(Environment.errors) > 0:
				break
			elif downloadFileUrlPath is not None:
				#logfile.write("Update package url: " + downloadFileUrlPath + "\n")

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

				#logfile.write("Update package file: " + packageFilename + "\n")
				application = PackageInstaller.detectTypeAndReturnHandler(packageFilename,interactive)
				applicationName = application.detectNecessaryApplicationName()
				applicationUserName = application.detectNecessaryApplicationUserName()

				installationBaseDirectory = application.getInstallBaseDirectory(downloadItemName.lower(), applicationUserName)

				if not application.promptInstallation():
					continue
				downloadDestinationFilePath = Path(installationBaseDirectory, packageFilename)
				downloadDestinationFilePath.parent.mkdir(parents=True, exist_ok=True)
				if downloadDestinationFilePath.exists():
					downloadDestinationFilePath.unlink()

				# Download update file
				print("Download started")

				downloadStatus = downloadPackage(downloadFileUrlPath, downloadDestinationFilePath)
				if not downloadStatus:
					return
				print("Download finished")
				application.packageFile = downloadDestinationFilePath
				application.process()

				if (applicationName == "complete" or applicationName == "frontend") and os.path.isfile(downloadDestinationFilePath):
					os.remove(downloadDestinationFilePath)

				if alreadyOpenLogFile is None:
					if Environment.errors is None or len(Environment.errors) == 0:
						Environment.messages.append(f"Update package file '{packageFilename}' successfully deployed.\nFor logs see '{logger.name}'")
					else:
						Environment.errors.append(f"Update package file '{packageFilename}' deployed with errors.\nFor logs see '{logger.name}'")
		if not Environment.getSystemUrl() or Environment.getSystemUrl().strip() == "Unknown" and DbConnector.checkDbServiceAvailable() and DbConnector.checkDbStructureExists():
			Environment.errors.append("Basic configuration is missing. Please configure.")
			Environment.overrideNextMenu = Environment.configTableMenu


def cloudUpdateMenuAction(actionParameters):
	print(Environment.applicationName + " Update via AGNITAS Cloud")

	print("Please enter update package link ID (Blank => Cancel):")
	packageUrl = input(" > ")

	if packageUrl != "":
		installFileFromCloud(packageUrl, True)

def fileUpdateMenuAction(actionParameters, alreadyOpenLogFile = None):
	print(Environment.applicationName, "Update from local file")

	print("Please enter update package file path (Blank => Cancel):")
	while True:
		packageFilePath = input(" > ").strip("'\"")
		if packageFilePath == "":
			return None
		else:
			if not os.path.isfile(packageFilePath):
				Environment.errors.append("Invalid file path")
				return False
			else:
				break


	application = PackageInstaller.detectTypeAndReturnHandler(packageFilePath,True)
	if application.promptInstallation():
		print(f"\nStarting {Environment.applicationName} Update ...")
		application.process()
		if alreadyOpenLogFile is None:
			if Environment.errors is None or len(Environment.errors) == 0:
				Environment.messages.append(
					f"Update package file '{os.path.basename(packageFilePath)}' successfully deployed.\nFor logs see '{logger.name}'")
			else:
				Environment.errors.append(
					f"Update package file '{os.path.basename(packageFilePath)}' deployed with errors.\nFor logs see '{logger.name}'")

	return None

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

					if not os.path.isfile(licenseTempDirectory + "/emm.license.xml") or not os.path.isfile(
							licenseTempDirectory + "/emm.license.xml.sig"):
						Environment.errors.append(
							"Given license file archive does not contain expected files (emm.license.xml, emm.license.xml.sig)")
					else:
						with open(licenseTempDirectory + "/emm.license.xml", "r",
						          encoding="UTF-8") as licenseDataFileHandle:
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
		emmVersion = Environment.applicationVersions.frontendVersion.refresh()
		operatingSystem = Environment.osVendor
		if Environment.osVersion:
			operatingSystem = Environment.osVendor + " " + str(Environment.osVersion)

		tomcat = ESTUtilities.getTomcatVersion(Environment.catalinaHome)
		java = ESTUtilities.getJavaVersion(Environment.javaHome)
		node = ESTUtilities.getNodeVersion()
		ESTUtilities.openUrlConnection(Environment.contentHubUrl + "?" + urlencode({"lsid" : licenseId, "vs" : emmVersion, "os" : operatingSystem, "tv" : tomcat, "jv" : java, "nv" : node}))
