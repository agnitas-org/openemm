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
import subprocess

from EMT_lib.Environment import Environment
from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities
from EMT_lib import InstallAndUpdateMenu

def basicWebappMenuAction(actionParameters):
	currentTomcatNative = None

	if Environment.environmentConfigurationFilePath is None:
		if os.path.isfile("/home/" + Environment.username + "/bin/setenv.sh"):
			Environment.environmentConfigurationFilePath = "/home/" + Environment.username + "/bin/setenv.sh"
		elif os.path.isfile("/home/" + Environment.frontendUserName + "/bin/setenv.sh"):
			Environment.environmentConfigurationFilePath = "/home/" + Environment.frontendUserName + "/bin/setenv.sh"

	if Environment.environmentConfigurationFilePath is not None and os.path.isfile(Environment.environmentConfigurationFilePath):
		environmentProperties = EMTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)
	else:
		environmentProperties = {}
		environmentProperties["JAVA_HOME"] = Environment.javaHome
		environmentProperties["CATALINA_HOME"] = Environment.catalinaHome
		environmentProperties["TOMCAT_NATIVE"] = Environment.tomcatNative
		environmentProperties["WKHTMLTOPDF"] = Environment.wkhtmltopdf
		environmentProperties["WKHTMLTOIMAGE"] = Environment.wkhtmltoimage

	if Environment.isOpenEmmServer:
		applicationUserName = "openemm"
	else:
		applicationUserName = "console"

	if os.path.isfile("/home/" + applicationUserName + "/tomcat/bin/emm.sh.additional.properties"):
		additionalProperties = EMTUtilities.readPropertiesFile("/home/" + applicationUserName + "/tomcat/bin/emm.sh.additional.properties")
		if "LD_LIBRARY_PATH" in additionalProperties:
			if additionalProperties["LD_LIBRARY_PATH"] is not None:
				ldLibraryPath = additionalProperties["LD_LIBRARY_PATH"].strip()
				if len(ldLibraryPath) > 0:
					if ldLibraryPath.endswith("/lib"):
						currentTomcatNative = ldLibraryPath[:-len("/lib")]
					else:
						currentTomcatNative = ldLibraryPath
				else:
					currentTomcatNative = None
			else:
				currentTomcatNative = None
		else:
			# Using the default from emm.sh
			currentTomcatNative = Environment.tomcatNativeEmmShDefault
	else:
		# Using the default from emm.sh
		currentTomcatNative = Environment.tomcatNativeEmmShDefault
	if EMTUtilities.isBlank(currentTomcatNative) or EMTUtilities.getTomcatNativeVersion(currentTomcatNative) is None:
		currentTomcatNative = None

	if environmentProperties is not None and len(environmentProperties) > 1:
		print("Current webapp basic configuration:")
		print(" JAVA_HOME:     " + ("None" if "JAVA_HOME" not in environmentProperties or environmentProperties["JAVA_HOME"] is None else environmentProperties["JAVA_HOME"]))
		print(" CATALINA_HOME: " + ("None" if "CATALINA_HOME" not in environmentProperties or environmentProperties["CATALINA_HOME"] is None else environmentProperties["CATALINA_HOME"]))
		print(" Tomcat-Native: " + ("None" if currentTomcatNative is None else currentTomcatNative))
		if not Environment.isEmmRdirServer:
			print(" WKHTMLTOPDF:   " + ("None" if "WKHTMLTOPDF" not in environmentProperties or environmentProperties["WKHTMLTOPDF"] is None else environmentProperties["WKHTMLTOPDF"]))
			print(" WKHTMLTOIMAGE: " + ("None" if "WKHTMLTOIMAGE" not in environmentProperties or environmentProperties["WKHTMLTOIMAGE"] is None else environmentProperties["WKHTMLTOIMAGE"]))
		if not "PROXY" in environmentProperties:
			print(" PROXY:         " + "None")
		elif environmentProperties["PROXY"] is None or environmentProperties["PROXY"].strip() == 0:
			print(" PROXY:         " + "None (Override systems proxy settings)")
		else:
			print(" PROXY:         " + environmentProperties["PROXY"])
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
		elif choice == "tomcat-native" or choice == "tomcat_native" or choice == "tomcatnative" or choice == "tomcat":
			configureTomcatnative(applicationUserName)
			return False
		elif choice == "wkhtml" or choice == "wkhtmltopdf" or choice == "wkhtmltoimage":
			configureWkhtml(environmentProperties)
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
		configureTomcatnative(applicationUserName)
		configureWkhtml(environmentProperties)
		configureProxy(environmentProperties)
		return False

def configureJavaHome(environmentProperties):
	currentJavaHome = None
	if "JAVA_HOME" in environmentProperties:
		currentJavaHome = environmentProperties["JAVA_HOME"]

	defaultJavaHome = ""
	if EMTUtilities.checkJavaAvailable(currentJavaHome):
		defaultJavaHome = currentJavaHome
	elif "JAVA_HOME" in os.environ and os.environ["JAVA_HOME"] != "" and os.environ["JAVA_HOME"] is not None and EMTUtilities.checkJavaAvailable(os.environ["JAVA_HOME"]):
		defaultJavaHome = os.environ["JAVA_HOME"]

	if defaultJavaHome == "":
		for javaSearchPath in Environment.javaSearchPaths:
			if os.path.isdir(javaSearchPath) and EMTUtilities.checkJavaAvailable(javaSearchPath):
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
				if EMTUtilities.checkJavaAvailable(linkToJavaHome):
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
		elif EMTUtilities.checkJavaAvailable(javaHome):
			break
		else:
			print("Invalid path for JAVA_HOME: " + javaHome)

	if javaHome == "" and Environment.agnitasCloudUrlReachable:
		versionInfo = EMTUtilities.downloadVersionInfo(Environment.agnitasDownloadPathVersionInfo)
		if versionInfo is not None and "java" in versionInfo:
			print("Do you want to install the included JAVA environment now? (Y/n, Blank => Yes):")
			answer = input(" > ").lower().strip()
			if answer == "" or answer.startswith("y") or answer.startswith("j"):
				downloadFileUrlPath = versionInfo["java"][1]

				if not os.path.isdir(Environment.optDefaultPath):
					os.mkdir(Environment.optDefaultPath)
					if EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
						os.chown(Environment.optDefaultPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)

				javaHome = Environment.optDefaultPath + "/java"
				Environment.javaHome = javaHome
				InstallAndUpdateMenu.installFileFromCloud(downloadFileUrlPath, False)

				if EMTUtilities.checkJavaAvailable(javaHome):
					Environment.javaHome = javaHome
					Environment.rebootNeeded = True
				else:
					Environment.javaHome = ""

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
				if not EMTUtilities.checkJavaAvailable(javaHome):
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
	else:
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

def configureCatalinaHome(environmentProperties):
	currentCatalinaHome = None
	if "CATALINA_HOME" in environmentProperties:
		currentCatalinaHome = environmentProperties["CATALINA_HOME"]

	# Initial Tomcat installation
	defaultCatalinaHome = ""
	if EMTUtilities.checkTomcatAvailable(Environment.javaHome, currentCatalinaHome):
		defaultCatalinaHome = currentCatalinaHome
	elif "CATALINA_HOME" in os.environ and os.environ["CATALINA_HOME"] != "" and os.environ["CATALINA_HOME"] is not None and EMTUtilities.checkTomcatAvailable(Environment.javaHome, os.environ["CATALINA_HOME"]):
		defaultCatalinaHome = os.environ["CATALINA_HOME"]

	if defaultCatalinaHome == "":
		for tomcatSearchPath in Environment.tomcatSearchPaths:
			if os.path.isdir(tomcatSearchPath) and EMTUtilities.checkTomcatAvailable(Environment.javaHome, tomcatSearchPath):
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
		elif EMTUtilities.checkTomcatAvailable(Environment.javaHome, catalinaHome):
			break
		else:
			print("Invalid path for CATALINA_HOME")

	if catalinaHome == "" and Environment.agnitasCloudUrlReachable:
		versionInfo = EMTUtilities.downloadVersionInfo(Environment.agnitasDownloadPathVersionInfo)
		if versionInfo is not None and "tomcat" in versionInfo:
			print("Do you want to install the included Tomcat application now? (Y/n, Blank => Yes):")
			answer = input(" > ").lower().strip()
			if answer == "" or answer.startswith("y") or answer.startswith("j"):
				downloadFileUrlPath = versionInfo["tomcat"][1]

				if not os.path.isdir(Environment.optDefaultPath):
					os.mkdir(Environment.optDefaultPath)
					if EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
						os.chown(Environment.optDefaultPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)

				Environment.catalinaHome = Environment.optDefaultPath + "/tomcat"
				InstallAndUpdateMenu.installFileFromCloud(downloadFileUrlPath, False)

				if EMTUtilities.checkTomcatAvailable(Environment.javaHome, Environment.catalinaHome):
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
	else:
		environmentProperties["CATALINA_HOME"] = catalinaHome
		Environment.catalinaHome = catalinaHome
		if Environment.isOpenEmmServer:
			username = "openemm"
		else:
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

def configureTomcatnative(applicationUserName):
	if os.path.isfile("/home/" + applicationUserName + "/tomcat/bin/emm.sh.additional.properties"):
		additionalProperties = EMTUtilities.readPropertiesFile("/home/" + applicationUserName + "/tomcat/bin/emm.sh.additional.properties")
		if "LD_LIBRARY_PATH" in additionalProperties:
			if additionalProperties["LD_LIBRARY_PATH"] is not None:
				ldLibraryPath = additionalProperties["LD_LIBRARY_PATH"].strip()
				if len(ldLibraryPath) > 0:
					if ldLibraryPath.endswith("/lib"):
						currentTomcatNative = ldLibraryPath[:-len("/lib")]
					else:
						currentTomcatNative = ldLibraryPath
				else:
					currentTomcatNative = None
			else:
				currentTomcatNative = None
		else:
			# Using the default from emm.sh
			currentTomcatNative = Environment.tomcatNativeEmmShDefault
	else:
		# Using the default from emm.sh
		currentTomcatNative = Environment.tomcatNativeEmmShDefault
	if EMTUtilities.isBlank(currentTomcatNative) or EMTUtilities.getTomcatNativeVersion(currentTomcatNative) is None:
		currentTomcatNative = None

	# Initial Tomcat-Native installation
	defaultTomcatNative = ""
	if EMTUtilities.getTomcatNativeVersion(currentTomcatNative) is not None:
		defaultTomcatNative = currentTomcatNative

	if defaultTomcatNative == "":
		for tomcatNativeSearchPath in Environment.tomcatNativeSearchPaths:
			if os.path.isdir(tomcatNativeSearchPath) and EMTUtilities.getTomcatNativeVersion(tomcatNativeSearchPath) is not None:
				defaultTomcatNative = tomcatNativeSearchPath
				break

	if defaultTomcatNative != "":
		defaultTomcatNativeHint = ", 'Default' for '" + defaultTomcatNative + "'"
	else:
		defaultTomcatNativeHint = ""
	print("Please enter path for optional Tomcat-Native (Blank => Skip configuration of existing Tomcat-Native and install some" + defaultTomcatNativeHint + "):")
	while True:
		tomcatNative = input(" > ")
		tomcatNative = tomcatNative.strip()
		if tomcatNative.lower() == "default":
			tomcatNative = defaultTomcatNative
		if tomcatNative == "":
			break
		elif EMTUtilities.getTomcatNativeVersion(tomcatNative) is not None:
			break
		else:
			print("Invalid path for Tomcat-Native")

	if tomcatNative == "" and Environment.agnitasCloudUrlReachable:
		versionInfo = EMTUtilities.downloadVersionInfo(Environment.agnitasDownloadPathVersionInfo)

		if versionInfo is not None:
			downloadFileUrlPath = None

			if ("tomcat-native-" + Environment.osVendor + Environment.osVersion) in versionInfo:
				downloadFileUrlPath = versionInfo["tomcat-native-" + Environment.osVendor + Environment.osVersion][1]
			elif "." in Environment.osVersion and ("tomcat-native-" + Environment.osVendor + Environment.osVersion[:Environment.osVersion.index(".")]) in versionInfo:
				downloadFileUrlPath = versionInfo["tomcat-native-" + Environment.osVendor + Environment.osVersion[:Environment.osVersion.index(".")]][1]
			elif ("tomcat-native-" + Environment.osVendor) in versionInfo:
				downloadFileUrlPath = versionInfo["tomcat-native-" + Environment.osVendor][1]

			if downloadFileUrlPath is not None:
				print("Do you want to install the included Tomcat-Native application now? (Y/n, Blank => Yes):")
				answer = input(" > ").lower().strip()
				if answer == "" or answer.startswith("y") or answer.startswith("j"):
					if not os.path.isdir(Environment.optDefaultPath):
						os.mkdir(Environment.optDefaultPath)
						if EMTUtilities.hasRootPermissions() and Environment.isOpenEmmServer and not Environment.username == "openemm":
							os.chown(Environment.optDefaultPath, pwd.getpwnam('openemm').pw_uid, pwd.getpwnam('openemm').pw_uid)

					Environment.tomcatNative = Environment.optDefaultPath + "/tomcat-native"
					InstallAndUpdateMenu.installFileFromCloud(downloadFileUrlPath, False)

					if EMTUtilities.getTomcatNativeVersion(Environment.tomcatNative) is not None:
						tomcatNative = Environment.tomcatNative
			if tomcatNative == "":
				print()
				print("Installation failed. Please install and configure the optional Apache Tomcat Native for Apache Tomcat application manually.")
				print("See " + Environment.applicationName + "'s \"Admin and Install Guide\" for help.")
				if not Environment.osVendor == "Suse":
					print(Colors.RED + "Do NOT use command 'yum install tomcat-native', because it installs Apache Tomcat Native for Apache Tomcat 7, which is not compatible with " + Environment.applicationName + "." + Colors.DEFAULT)
				sys.exit(1)
			else:
				Environment.rebootNeeded = True

	if tomcatNative == "":
		print()
		print("You may install and configure the optional Apache Tomcat Native for Apache Tomcat application manually.")
		print("See " + Environment.applicationName + "'s \"Admin and Install Guide\" for help.")
		if not Environment.osVendor == "Suse":
			print(Colors.RED + "Do NOT use command 'yum install tomcat-native', because it installs Apache Tomcat Native for Apache Tomcat 7, which is not compatible with " + Environment.applicationName + "." + Colors.DEFAULT)
	else:
		Environment.tomcatNative = tomcatNative
		if EMTUtilities.isBlank(Environment.tomcatNative) or EMTUtilities.getTomcatNativeVersion(Environment.tomcatNative) is None:
			Environment.tomcatNative = None
		environmentProperties = EMTUtilities.readEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath)
		environmentProperties["TOMCAT_NATIVE"] = Environment.tomcatNative
		if Environment.isOpenEmmServer:
			username = "openemm"
		else:
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

def configureWkhtml(environmentProperties):
	if not Environment.isEmmRdirServer:
		currentWkhtmlToPdfPath = None
		if "WKHTMLTOPDF" in environmentProperties:
			currentWkhtmlToPdfPath = environmentProperties["WKHTMLTOPDF"]
		currentWkhtmlToImagePath = None
		if "WKHTMLTOIMAGE" in environmentProperties:
			currentWkhtmlToImagePath = environmentProperties["WKHTMLTOIMAGE"]

		defaultWkhtmlToPdfPath = ""
		if currentWkhtmlToPdfPath is not None and currentWkhtmlToPdfPath != "" and os.path.isfile(currentWkhtmlToPdfPath):
			defaultWkhtmlToPdfPath = currentWkhtmlToPdfPath
		elif os.path.isfile("/usr/local/bin/wkhtmltopdf"):
			defaultWkhtmlToPdfPath = "/usr/local/bin/wkhtmltopdf"
		elif os.path.isfile("/usr/bin/wkhtmltopdf"):
			defaultWkhtmlToPdfPath = "/usr/bin/wkhtmltopdf"
		else:
			try:
				defaultWkhtmlToPdfPath = subprocess.check_output("which wkhtmltopdf 2>/dev/null", shell=True).decode("UTF-8").strip()
			except:
				defaultWkhtmlToPdfPath = None
			if defaultWkhtmlToPdfPath is not None:
				defaultWkhtmlToPdfPath = os.path.realpath(defaultWkhtmlToPdfPath)

		if defaultWkhtmlToPdfPath is not None and defaultWkhtmlToPdfPath != "":
			defaultWkhtmlToPdfPathHint = ", 'Default' for '" + defaultWkhtmlToPdfPath + "'"
		else:
			defaultWkhtmlToPdfPathHint = ""
		print("Please enter path for wkhtmltopdf (Blank => Cancel" + defaultWkhtmlToPdfPathHint + "):")
		while True:
			wkhtmlToPdfPath = input(" > ")
			wkhtmlToPdfPath = wkhtmlToPdfPath.strip()
			if wkhtmlToPdfPath.lower() == "default":
				wkhtmlToPdfPath = defaultWkhtmlToPdfPath
			if wkhtmlToPdfPath == "":
				break
			elif os.path.isfile(wkhtmlToPdfPath):
				break
			else:
				print("Invalid path for wkhtmltopdf")

		if wkhtmlToPdfPath == "":
			if currentWkhtmlToPdfPath is not None and currentWkhtmlToPdfPath != "":
				return
			elif Environment.osVendor == "Suse":
				print("Install wkhtmltopdf and wkhtmltoimage by these commands:")
				print("# cd /opt/")
				print("# wget https://github.com/wkhtmltopdf/wkhtmltopdf/releases/download/0.12.4/wkhtmltox-0.12.4_linux-generic-amd64.tar.xz")
				print("# tar xJpf wkhtmltox-0.12.4_linux-generic-amd64.tar.xz")
				print("# rm wkhtmltox-0.12.4_linux-generic-amd64.tar.xz")
				print("# chown -R root. wkhtmltox")
				print("# ln -s /opt/wkhtmltox/bin/wkhtmlto* /usr/local/bin/'")
				sys.exit(1)
			elif Environment.osVendor == "CentOS" and Environment.osVersion == "7":
				installWkhtmltoxCommandCentOs7 = "yum install https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6-1/wkhtmltox-0.12.6-1.centos7.x86_64.rpm"
				print("Install wkhtmltopdf and wkhtmltoimage by command '" + installWkhtmltoxCommandCentOs7 + "'")
				print("Do you want to install wkhtmltopdf and wkhtmltoimage now? (N/y, Blank => Cancel):")
				answer = input(" > ").lower().strip()
				if answer.startswith("y") or answer.startswith("j"):
					os.system(installWkhtmltoxCommandCentOs7)

					try:
						wkhtmlToPdfPath = subprocess.check_output("which wkhtmltopdf 2>/dev/null", shell=True).decode("UTF-8").strip()
					except:
						wkhtmlToPdfPath = None
					if wkhtmlToPdfPath is not None:
						wkhtmlToPdfPath = os.path.realpath(wkhtmlToPdfPath)
					if wkhtmlToPdfPath is None or not os.path.isfile(wkhtmlToPdfPath):
						print("Mandatory wkhtmlToPdfPath is missing, even after installation")
						sys.exit(1)
				else:
					print()
					print("Please install and configure missing mandatory wkhtmlToPdfPath")
					sys.exit(1)
			elif Environment.osVendor == "CentOS":
				installWkhtmltoxCommandCentOs8 = "yum install https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6-1/wkhtmltox-0.12.6-1.centos8.x86_64.rpm"
				print("Install wkhtmltopdf and wkhtmltoimage by command '" + installWkhtmltoxCommandCentOs8 + "'")
				print("Do you want to install wkhtmltopdf and wkhtmltoimage now? (N/y, Blank => Cancel):")
				answer = input(" > ").lower().strip()
				if answer.startswith("y") or answer.startswith("j"):
					os.system(installWkhtmltoxCommandCentOs8)

					try:
						wkhtmlToPdfPath = subprocess.check_output("which wkhtmltopdf 2>/dev/null", shell=True).decode("UTF-8").strip()
					except:
						wkhtmlToPdfPath = None
					if wkhtmlToPdfPath is not None:
						wkhtmlToPdfPath = os.path.realpath(wkhtmlToPdfPath)
					if wkhtmlToPdfPath is None or not os.path.isfile(wkhtmlToPdfPath):
						print("Mandatory wkhtmlToPdfPath is missing, even after installation")
						sys.exit(1)
				else:
					print()
					print("Please install and configure missing mandatory wkhtmlToPdfPath")
					sys.exit(1)
			elif Environment.osVendor == "RedHat" and Environment.osVersion == "7":
				installWkhtmltoxCommandRedHat7 = "yum install https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6-1/wkhtmltox-0.12.6-1.centos7.x86_64.rpm"
				print("Install wkhtmltopdf and wkhtmltoimage by command '" + installWkhtmltoxCommandRedHat7 + "'")
				print("Do you want to install wkhtmltopdf and wkhtmltoimage now? (N/y, Blank => Cancel):")
				answer = input(" > ").lower().strip()
				if answer.startswith("y") or answer.startswith("j"):
					os.system(installWkhtmltoxCommandRedHat7)

					try:
						wkhtmlToPdfPath = subprocess.check_output("which wkhtmltopdf 2>/dev/null", shell=True).decode("UTF-8").strip()
					except:
						wkhtmlToPdfPath = None
					if wkhtmlToPdfPath is not None:
						wkhtmlToPdfPath = os.path.realpath(wkhtmlToPdfPath)
					if wkhtmlToPdfPath is None or not os.path.isfile(wkhtmlToPdfPath):
						print("Mandatory wkhtmlToPdfPath is missing, even after installation")
						sys.exit(1)
				else:
					print()
					print("Please install and configure missing mandatory wkhtmlToPdfPath")
					sys.exit(1)
			elif Environment.osVendor == "RedHat":
				installWkhtmltoxCommandRedHat8 = "yum install https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6-1/wkhtmltox-0.12.6-1.centos8.x86_64.rpm"
				print("Install wkhtmltopdf and wkhtmltoimage by command '" + installWkhtmltoxCommandRedHat8 + "'")
				print("Do you want to install wkhtmltopdf and wkhtmltoimage now? (N/y, Blank => Cancel):")
				answer = input(" > ").lower().strip()
				if answer.startswith("y") or answer.startswith("j"):
					os.system(installWkhtmltoxCommandRedHat8)

					try:
						wkhtmlToPdfPath = subprocess.check_output("which wkhtmltopdf 2>/dev/null", shell=True).decode("UTF-8").strip()
					except:
						wkhtmlToPdfPath = None
					if wkhtmlToPdfPath is not None:
						wkhtmlToPdfPath = os.path.realpath(wkhtmlToPdfPath)
					if wkhtmlToPdfPath is None or not os.path.isfile(wkhtmlToPdfPath):
						print("Mandatory wkhtmlToPdfPath is missing, even after installation")
						sys.exit(1)
				else:
					print()
					print("Please install and configure missing mandatory wkhtmlToPdfPath")
					sys.exit(1)
			elif Environment.osVendor == "Alma" and Environment.osVersion == "7":
				installWkhtmltoxCommandAlma7 = "yum install https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6-1/wkhtmltox-0.12.6-1.centos7.x86_64.rpm"
				print("Install wkhtmltopdf and wkhtmltoimage by command '" + installWkhtmltoxCommandAlma7 + "'")
				print("Do you want to install wkhtmltopdf and wkhtmltoimage now? (N/y, Blank => Cancel):")
				answer = input(" > ").lower().strip()
				if answer.startswith("y") or answer.startswith("j"):
					os.system(installWkhtmltoxCommandAlma7)

					try:
						wkhtmlToPdfPath = subprocess.check_output("which wkhtmltopdf 2>/dev/null", shell=True).decode("UTF-8").strip()
					except:
						wkhtmlToPdfPath = None
					if wkhtmlToPdfPath is not None:
						wkhtmlToPdfPath = os.path.realpath(wkhtmlToPdfPath)
					if wkhtmlToPdfPath is None or not os.path.isfile(wkhtmlToPdfPath):
						print("Mandatory wkhtmlToPdfPath is missing, even after installation")
						sys.exit(1)
				else:
					print()
					print("Please install and configure missing mandatory wkhtmlToPdfPath")
					sys.exit(1)
			elif Environment.osVendor == "Alma":
				installWkhtmltoxCommandAlma8 = "yum install https://github.com/wkhtmltopdf/packaging/releases/download/0.12.6-1/wkhtmltox-0.12.6-1.centos8.x86_64.rpm"
				print("Install wkhtmltopdf and wkhtmltoimage by command '" + installWkhtmltoxCommandAlma8 + "'")
				print("Do you want to install wkhtmltopdf and wkhtmltoimage now? (N/y, Blank => Cancel):")
				answer = input(" > ").lower().strip()
				if answer.startswith("y") or answer.startswith("j"):
					os.system(installWkhtmltoxCommandAlma8)

					try:
						wkhtmlToPdfPath = subprocess.check_output("which wkhtmltopdf 2>/dev/null", shell=True).decode("UTF-8").strip()
					except:
						wkhtmlToPdfPath = None
					if wkhtmlToPdfPath is not None:
						wkhtmlToPdfPath = os.path.realpath(wkhtmlToPdfPath)
					if wkhtmlToPdfPath is None or not os.path.isfile(wkhtmlToPdfPath):
						print("Mandatory wkhtmlToPdfPath is missing, even after installation")
						sys.exit(1)
				else:
					print()
					print("Please install and configure missing mandatory wkhtmlToPdfPath")
					sys.exit(1)
			else:
				print()
				print("Please install and configure missing mandatory wkhtmlToPdfPath")
				sys.exit(1)

		wkhtmlToImagePath = os.path.dirname(wkhtmlToPdfPath) + "/wkhtmltoimage"

		environmentProperties["WKHTMLTOPDF"] = wkhtmlToPdfPath
		Environment.wkhtmltopdf = wkhtmlToPdfPath
		environmentProperties["WKHTMLTOIMAGE"] = wkhtmlToImagePath
		Environment.wkhtmltoimage = wkhtmlToImagePath
		if Environment.isOpenEmmServer:
			username = "openemm"
		else:
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
		# Store new WKHTML data in database config_tbl
		if DbConnector.checkDbConnection():
			DbConnector.updateConfigurationValueInDB("system", "wkhtmltopdf", wkhtmlToPdfPath, Environment.hostname)
			DbConnector.updateConfigurationValueInDB("system", "wkhtmltoimage", wkhtmlToImagePath, Environment.hostname)

def configureProxy(environmentProperties):
	if Environment.isOpenEmmServer:
			username = "openemm"
	else:
		if Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
			username = "console"
		elif Environment.isEmmConsoleRdirServer:
			if Environment.isEmmRdirServer:
				username = "rdir"
			else:
				username = "console"
		else:
			username = "console"

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

	EMTUtilities.updateEnvironmentPropertiesFile(Environment.environmentConfigurationFilePath, None, environmentProperties)

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
			