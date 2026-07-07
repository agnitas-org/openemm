import sys
import os
import pwd
import subprocess

from EST_lib.Environment import Environment
from EST_lib import Colors
from EST_lib import DbConnector
from EST_lib import ESTUtilities
from EST_lib import InstallAndUpdateMenu

def basicWebappMenuAction(actionParameters):
	if Environment.environmentConfigurationFilePath is None:
		if os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/setenv.sh"):
			Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin/setenv.sh"
		elif os.path.isfile(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin/setenv.sh"):
			Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin/setenv.sh"

	if Environment.environmentConfigurationFilePath is not None and os.path.isfile(Environment.environmentConfigurationFilePath):
		environmentProperties = ESTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)
	else:
		Environment.environmentConfigurationFilePath = ESTUtilities.getUserHomeDirectory(Environment.username) + "/bin/setenv.sh"
		environmentProperties = {}
		environmentProperties["JAVA_HOME"] = Environment.javaHome
		environmentProperties["CATALINA_HOME"] = Environment.catalinaHome

	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	elif ESTUtilities.userExists(Environment.getRdirApplicationUserName()) and Environment.username == Environment.getRdirApplicationUserName():
		applicationUserName = Environment.getRdirApplicationUserName()
	else:
		applicationUserName = Environment.getFrontendApplicationUserName()

	if environmentProperties is not None and len(environmentProperties) > 1:
		print("Current webapp basic configuration:")
		print(" JAVA_HOME:     " + ("None" if "JAVA_HOME" not in environmentProperties or environmentProperties["JAVA_HOME"] is None else environmentProperties["JAVA_HOME"]))
		print(" CATALINA_HOME: " + ("None" if "CATALINA_HOME" not in environmentProperties or environmentProperties["CATALINA_HOME"] is None else environmentProperties["CATALINA_HOME"]))
		if not "PROXY" in environmentProperties:
			print(" PROXY (optional):         " + "None")
		elif environmentProperties["PROXY"] is None or environmentProperties["PROXY"].strip() == 0:
			print(" PROXY (optional):         " + "None (Override systems proxy settings)")
		else:
			print(" PROXY (optional):         " + environmentProperties["PROXY"])
		print()

		print("Please choose entry to change (Blank => Back):")

		choice = input(" > ")
		choice = choice.strip().lower()
		if choice == "":
			return
		elif choice == "java" or choice == "java_home":
			configureJavaHome(environmentProperties)
			return False
		elif choice == "catalina" or choice == "catalina_home":
			configureCatalinaHome(environmentProperties)
			return False
		elif choice == "proxy":
			configureProxy(environmentProperties)
			return False
		elif len(choice) > 0:
			Environment.errors.append("Invalid unknown entry name: " + choice)
			return False
		else:
			return
	else:
		configureJavaHome(environmentProperties)
		configureCatalinaHome(environmentProperties)
		configureProxy(environmentProperties)
		return False

def configureJavaHome(environmentProperties):
	currentJavaHome = None
	if "JAVA_HOME" in environmentProperties:
		currentJavaHome = environmentProperties["JAVA_HOME"]

	defaultJavaHome = ""
	if ESTUtilities.checkJavaAvailable(currentJavaHome):
		defaultJavaHome = currentJavaHome
	elif "JAVA_HOME" in os.environ and os.environ["JAVA_HOME"] != "" and os.environ["JAVA_HOME"] is not None and ESTUtilities.checkJavaAvailable(os.environ["JAVA_HOME"]):
		defaultJavaHome = os.environ["JAVA_HOME"]

	if defaultJavaHome == "":
		for javaSearchPath in Environment.javaSearchPaths:
			if os.path.isdir(javaSearchPath) and ESTUtilities.checkJavaAvailable(javaSearchPath):
				defaultJavaHome = javaSearchPath
				break

	if defaultJavaHome == "":
		try:
			linkToJava = subprocess.check_output("which java 2>/dev/null", shell=True).decode("UTF-8").strip()
		except:
			linkToJava = None
		if linkToJava is not None:
			linkToJava = os.path.realpath(linkToJava)
			if linkToJava.endswith("/bin/java"):
				linkToJavaHome = linkToJava[:-9]
				if linkToJavaHome.endswith("/jre"):
					linkToJavaHome = linkToJavaHome[:-4]
				if ESTUtilities.checkJavaAvailable(linkToJavaHome):
					defaultJavaHome = linkToJavaHome

	if defaultJavaHome != "":
		defaultJavaHomeHint = ", 'Default' for '" + defaultJavaHome + "'"
	else:
		defaultJavaHomeHint = ""
	print("Please enter path for JAVA_HOME (Blank => Skip configuration of existing JAVA and install some" + defaultJavaHomeHint + "):")
	while True:
		javaHome = input(" > ")
		javaHome = javaHome.strip()
		if javaHome.lower() == "default":
			javaHome = defaultJavaHome
		if javaHome == "":
			break
		elif ESTUtilities.checkJavaAvailable(javaHome):
			break
		else:
			print("Invalid path for JAVA_HOME: " + javaHome)

	if javaHome == "" and Environment.agnitasCloudUrlReachable:
		versionInfo = ESTUtilities.downloadVersionInfo(Environment.agnitasDownloadPathVersionInfo)
		if versionInfo is not None and "java" in versionInfo:
			print("Do you want to install the included JAVA environment now? (Y/n, Blank => Yes):")
			answer = input(" > ").lower().strip()
			if answer == "" or answer.startswith("y") or answer.startswith("j"):
				downloadFileUrlPath = versionInfo["java"][1]
				InstallAndUpdateMenu.installFileFromCloud(downloadFileUrlPath, False)

				if ESTUtilities.checkJavaAvailable(Environment.javaHome):
					javaHome = Environment.javaHome
					Environment.rebootNeeded = True
				else:
					Environment.javaHome = ""
				javaHome = Environment.javaHome

	if javaHome == "":
		if Environment.osVendor == "Suse":
			print("Install a JAVA environment by command 'zypper install -y java-11-openjdk java-11-openjdk-devel'")
			sys.exit(1)
		else:
			javaInstallCommand = "yum install java-11-openjdk java-11-openjdk-devel"
			print("Install a JAVA environment by command '" + javaInstallCommand + "'")
			print("Do you want to install a JAVA environment now? (N/y, Blank => Cancel):")
			answer = input(" > ").lower().strip()
			if answer.startswith("y") or answer.startswith("j"):
				os.system(javaInstallCommand)

				try:
					javaHome = subprocess.check_output("which java 2>/dev/null", shell=True).decode("UTF-8").strip()
				except:
					javaHome = None
				if javaHome is not None:
					javaHome = os.path.realpath(javaHome)
					if javaHome.endswith("/bin/java"):
						javaHome = javaHome[:-9]
					if javaHome.endswith("/jre"):
						javaHome = javaHome[:-4]
				if not ESTUtilities.checkJavaAvailable(javaHome):
					print("Mandatory JAVA environment is missing, even after installation")
					sys.exit(1)
			else:
				print()
				print("Please install and configure missing mandatory JAVA environment")
				sys.exit(1)

	environmentProperties["JAVA_HOME"] = javaHome
	Environment.javaHome = javaHome
	if Environment.isOpenEmmServer:
		username = "openemm"
	elif ESTUtilities.userExists(Environment.getRdirApplicationUserName()) and Environment.username == Environment.getRdirApplicationUserName():
		username = Environment.getRdirApplicationUserName()
	else:
		if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
			username = Environment.getFrontendApplicationUserName()
		elif Environment.isEmmConsoleRdirServer:
			if Environment.isEmmRdirServer:
				username = Environment.rdirApplicationUsername()
			else:
				username = Environment.getFrontendApplicationUserName()
		else:
			username = Environment.getFrontendApplicationUserName()
	ESTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, username, environmentProperties)

def configureCatalinaHome(environmentProperties):
	currentCatalinaHome = None
	if "CATALINA_HOME" in environmentProperties:
		currentCatalinaHome = environmentProperties["CATALINA_HOME"]

	# Initial Tomcat installation
	defaultCatalinaHome = ""
	if ESTUtilities.checkTomcatAvailable(Environment.javaHome, currentCatalinaHome):
		defaultCatalinaHome = currentCatalinaHome
	elif "CATALINA_HOME" in os.environ and os.environ["CATALINA_HOME"] != "" and os.environ["CATALINA_HOME"] is not None and ESTUtilities.checkTomcatAvailable(Environment.javaHome, os.environ["CATALINA_HOME"]):
		defaultCatalinaHome = os.environ["CATALINA_HOME"]

	if defaultCatalinaHome == "":
		for tomcatSearchPath in Environment.tomcatSearchPaths:
			if os.path.isdir(tomcatSearchPath) and ESTUtilities.checkTomcatAvailable(Environment.javaHome, tomcatSearchPath):
				defaultCatalinaHome = tomcatSearchPath
				break

	if defaultCatalinaHome != "":
		defaultCatalinaHomeHint = ", 'Default' for '" + defaultCatalinaHome + "'"
	else:
		defaultCatalinaHomeHint = ""
	print("Please enter path for CATALINA_HOME (Blank => Skip configuration of existing Tomcat and install some" + defaultCatalinaHomeHint + "):")
	while True:
		catalinaHome = input(" > ")
		catalinaHome = catalinaHome.strip()
		if catalinaHome.lower() == "default":
			catalinaHome = defaultCatalinaHome
		if catalinaHome == "":
			break
		elif ESTUtilities.checkTomcatAvailable(Environment.javaHome, catalinaHome):
			break
		else:
			print("Invalid path for CATALINA_HOME")

	if catalinaHome == "" and Environment.agnitasCloudUrlReachable:
		versionInfo = ESTUtilities.downloadVersionInfo(Environment.agnitasDownloadPathVersionInfo)
		if versionInfo is not None and "tomcat" in versionInfo:
			print("Do you want to install the included Tomcat application now? (Y/n, Blank => Yes):")
			answer = input(" > ").lower().strip()
			if answer == "" or answer.startswith("y") or answer.startswith("j"):
				downloadFileUrlPath = versionInfo["tomcat"][1]
				InstallAndUpdateMenu.installFileFromCloud(downloadFileUrlPath, False)

				if ESTUtilities.checkTomcatAvailable(Environment.javaHome, Environment.catalinaHome):
					catalinaHome = Environment.catalinaHome
			if catalinaHome == "":
				print()
				print("Installation failed. Please install and configure the mandatory Apache Tomcat application manually.")
				print("See " + Environment.applicationName + "'s \"Admin and Install Guide\" for help.")
				if not Environment.osVendor == "Suse":
					print(Colors.RED + "Do NOT use command 'yum install tomcat', because it installs Apache Tomcat 7, which is not compatible with " + Environment.applicationName + "." + Colors.DEFAULT)
				sys.exit(1)
			else:
				Environment.rebootNeeded = True

	if catalinaHome == "":
		print()
		print("Please install and configure the mandatory Apache Tomcat application manually.")
		print("See " + Environment.applicationName + "'s \"Admin and Install Guide\" for help.")
		if not Environment.osVendor == "Suse":
			print(Colors.RED + "Do NOT use command 'yum install tomcat', because it installs Apache Tomcat 7, which is not compatible with " + Environment.applicationName + "." + Colors.DEFAULT)
		sys.exit(1)
	else:
		environmentProperties["CATALINA_HOME"] = catalinaHome
		Environment.catalinaHome = catalinaHome
		if Environment.isOpenEmmServer:
			username = "openemm"
		elif ESTUtilities.userExists(Environment.getRdirApplicationUserName()) and Environment.username == Environment.getRdirApplicationUserName():
			username = Environment.getRdirApplicationUserName()
		else:
			if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
				username = Environment.getFrontendApplicationUserName()
			elif Environment.isEmmConsoleRdirServer:
				if Environment.isEmmRdirServer:
					username = Environment.rdirApplicationUsername()
				else:
					username = Environment.getFrontendApplicationUserName()
			else:
				username = Environment.getFrontendApplicationUserName()
		ESTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, username, environmentProperties)


def configureProxy(environmentProperties):
	print("Do you want to use a WebProxy? (N/y, Blank => No):")
	choice = input(" > ").strip().lower()
	if choice.startswith("y") or choice.startswith("j"):
		print("Please enter proxy host, port and authentication data")
		print("  (e.g: \"http://proxyusername:proxypassword@proxyhost:proxyport\" or \"http://proxyhost:proxyport\")")
		proxy = input(" > ").strip()

		print("Please enter proxy exclusions (separated by '|', blank for none)")
		noProxy = input(" > ").strip()

		environmentProperties.pop("PROXY", None)
		if proxy is not None and len(proxy) > 0:
			environmentProperties["PROXY"] = proxy

		environmentProperties.pop("NO_PROXY_HOSTS", None)
		if noProxy is not None and len(noProxy) > 0:
			environmentProperties["NO_PROXY_HOSTS"] = noProxy
	else:
		environmentProperties.pop("PROXY", None)
		environmentProperties.pop("NO_PROXY_HOSTS", None)

		print("Do you want to override any system configured proxy? (N/y, Blank => No):")
		choice = input(" > ").strip().lower()
		if choice.startswith("y") or choice.startswith("j"):
			environmentProperties["PROXY"] = ""

	ESTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, None, environmentProperties)

	# Apply new proxy setting to current process
	if "PROXY" in environmentProperties:
		if environmentProperties["PROXY"] is not None and len(environmentProperties["PROXY"].strip()) > 0:
			os.environ["http_proxy"] = environmentProperties["PROXY"]
			os.environ["https_proxy"] = environmentProperties["PROXY"]
			if "NO_PROXY_HOSTS" in environmentProperties and environmentProperties["NO_PROXY_HOSTS"] is not None and len(environmentProperties["NO_PROXY_HOSTS"].strip()) > 0:
				os.environ["no_proxy"] = environmentProperties["NO_PROXY_HOSTS"]
		else:
			os.environ.pop("http_proxy", None)
			os.environ.pop("https_proxy", None)
			os.environ.pop("no_proxy", None)
			