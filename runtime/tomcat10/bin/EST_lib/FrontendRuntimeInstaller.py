import glob
import pathlib
import pwd
import subprocess
import sys
import os
import re
import shutil
import tarfile
import tempfile
import time
import shlex
from tarfile import TarFile

from EST_lib import Colors
from EST_lib.Environment import Environment
from EST_lib import ESTUtilities
from EST_lib.ESTUtilities import restart

# Frontend, Statistics, Webservices or Rdir
def installFile(interactive, logfile, applicationUserName, applicationName, updatedApplicationDirectoryPath, packageFilePath):
	# Create needed directories, if missing
	if Environment.isOpenEmmServer:
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/temp"):
			ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("openemm") + "/temp", "openemm")
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory("openemm") + "/bin"):
			ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory("openemm") + "/bin", "openemm")
	elif Environment.isEmmRdirServer:
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/temp"):
			ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/temp", Environment.getRdirApplicationUserName())
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin"):
			ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getRdirApplicationUserName()) + "/bin", Environment.getRdirApplicationUserName())
	elif Environment.isEmmFrontendServer or Environment.isEmmStatisticsServer or Environment.isEmmWebservicesServer:
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/temp"):
			ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/temp", Environment.getFrontendApplicationUserName())
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin"):
			ESTUtilities.createDirectory(ESTUtilities.getUserHomeDirectory(Environment.getFrontendApplicationUserName()) + "/bin", Environment.getFrontendApplicationUserName())


	print("You are currently in the process of conducting an update. Do you want to make a backup of directories? \nPlease enter the absolute path of the desired directories, separated by space: (Blank => Cancel)")
	choice = input("> ")
	backup_choices = shlex.split(choice)

	if backup_choices:
		output_path = createTarGZSystemFileBackup(backup_choices)
		if output_path:
			Environment.messages.append("Created Backup: " + output_path)
			print("Created Backup: " + output_path) # In case of the user restarting the runtime immediately after update, environment message won't be shown
	createTemporarySystemFileBackup()
	try:

		# Delete old runtime data
		if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat"):
			os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat")
		if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps-leave-empty"):
			shutil.rmtree(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps-leave-empty")
		if ESTUtilities.check_tree_permissions(userHomePath := ESTUtilities.getUserHomeDirectory(applicationUserName)):
			for listedFile in os.listdir(userHomePath):
				if re.search("^tomcat.*$", listedFile) and os.path.isdir(os.path.join(userHomePath,listedFile)):
					shutil.rmtree(os.path.join(userHomePath,listedFile))
	except:
		if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat"):
			os.link(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat10", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat")
		if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps-leave-empty"):
			os.mkdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps-leave-empty")

	# Extract targz-file for runtime
	ESTUtilities.unTarGzFile(packageFilePath, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/", applicationUserName)

	restoreTemporarySystemFileBackup()

	logfile.write("Extracted file '" + packageFilePath + "' in path '" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + "'" + "\n")

	# Create server.xml from template, if missing
	if not os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/server.xml") and os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/server.xml.template"):
		shutil.copy(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/server.xml.template", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/server.xml")
		ESTUtilities.chown(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/server.xml", applicationUserName, applicationUserName)
		ESTUtilities.manageTlsCertificateForTomcat(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/etc/ssl", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/server.xml", Environment.applicationName)
		logfile.write("Created file '" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/server.xml" + "'\n")

	Environment.messages.append(Environment.applicationName + " runtime was updated")

	# Create needed links, if missing
	if not os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + Environment.restartToolName) and os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/bin/" + Environment.restartToolName):
		ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/bin/" + Environment.restartToolName, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + Environment.restartToolName, applicationUserName)
	if not os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/emm.sh") and os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/bin/emm.sh"):
		ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/bin/emm.sh", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/emm.sh", applicationUserName)
	if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/conf") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/conf"):
		ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/conf", applicationUserName)
	if not os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/logs") and not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/logs"):
		ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/logs", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/logs", applicationUserName)

	if os.path.islink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manager") and os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/opt/tomcat"):
		os.unlink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manager")
		ESTUtilities.createLink(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/opt/tomcat/webapps/manager", ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/manager", applicationUserName)
		logfile.write("Created new link for tomcat manager app" + "\n")

	# Read new version info
	if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/version.txt"):
		with open(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/tomcat/conf/version.txt") as runtimeVersionFile:
			Environment.runtimeVersion = runtimeVersionFile.read().strip()
	else:
		Environment.runtimeVersion = "Unknown"

	if interactive:
		print(Colors.YELLOW + "To let the new Runtime version take effect, you must exit and restart this program!" + Colors.DEFAULT)
		print("Do you want to exit and restart now? (Y/n, Blank => Exit):")
		answer = input(" > ").lower().strip()
		if answer == "" or answer.startswith("y") or answer.startswith("j"):
			restart(Environment.scriptFilePath, *sys.argv)
	else:
		print(Colors.YELLOW + "Restart for Runtime Update..." + Colors.DEFAULT)
		restart(Environment.scriptFilePath, *sys.argv)

def createTemporarySystemFileBackup():
	# Create temporary backup of system specific configuration files

	applicationUserName = Environment.getApplicationUsername()

	if os.path.isdir("/tmp/EmmConfigurationBackup"):
		shutil.rmtree("/tmp/EmmConfigurationBackup")
	os.mkdir("/tmp/EmmConfigurationBackup")
	for systemSpecificConfigFile in Environment.systemSpecificConfigFiles:
		if os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + systemSpecificConfigFile):
			if os.path.isfile("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile):
				os.remove("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile)
			os.makedirs(os.path.dirname("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile), exist_ok=True)
			shutil.copy(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + systemSpecificConfigFile, "/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile)
	for directoryToPreserve in Environment.directoriesToPreserve:
		if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + directoryToPreserve):
			if os.path.isdir("/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve):
				shutil.rmtree("/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve)
			shutil.move(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + directoryToPreserve, "/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve)

def createTarGZSystemFileBackup_old():
	""" Create temporary backup of system specific configuration files and folders, compressed into a tar file"""
	applicationUserName = Environment.getApplicationUsername()
	home = ESTUtilities.getUserHomeDirectory(applicationUserName)
	filename = "backup-tomcat-conf_" + time.strftime("%Y%m%d-%H%M%S") + ".tar.gz"
	output_path = home + "/" + filename
	directory_path = home + "/tomcat/conf/"
	with tarfile.open(output_path, "w:gz") as tar:
		if os.path.exists(os.path.realpath(directory_path)):
			tar.add(directory_path, arcname=os.path.relpath(directory_path, home))
			return output_path

def createTarGZSystemFileBackup(backup_choices):
	""" Create temporary backup of system specific configuration files and folders, compressed into a tar file"""
	applicationUserName = Environment.getApplicationUsername()
	home = ESTUtilities.getUserHomeDirectory(applicationUserName)
	filename = "backup-emm-update-" + time.strftime("%Y%m%d-%H%M%S") + ".tar.gz"
	output_path = home + "/" + filename
	unixPathValidationPattern = re.compile("^(/[^/ ]*)+/?$|^\.(/[^/ ]*)+/?$|^\.\./([^/ ]*/)*[^/ ]*$")

	validBackupChoices = [path for path in backup_choices if unixPathValidationPattern.match(path)]
	higherAccessBackupChoices = [path for path in validBackupChoices if not ESTUtilities.check_tree_permissions(path)]

	if validBackupChoices:
		print("Depending on the size of the provided directories, this might take some time...")
		if higherAccessBackupChoices:
			with tempfile.TemporaryDirectory() as backupTempDirectory:
				try:
					subprocess.run(["sudo", "cp", "--parents", "--no-preserve","ownership", "-r", *higherAccessBackupChoices, "-t", backupTempDirectory], check=False)
					subprocess.run(["sudo", "chown", "-R", str(pwd.getpwnam(Environment.username).pw_uid) + ":"+ str(pwd.getpwnam(Environment.username).pw_uid), backupTempDirectory], check=True)
					subprocess.run(["sudo","-k"], check=True) #expire sudo session immediately
				except subprocess.CalledProcessError:
					return None

				with tarfile.open(output_path, "w:gz") as tar:
					for path in list(filter(lambda item: item not in higherAccessBackupChoices, validBackupChoices)) :
						print("Backing up: " + path)
						tar.add(path, arcname=os.path.relpath(path, home))

					base_dir = pathlib.Path(backupTempDirectory)
					backupChoices = [path for path in higherAccessBackupChoices if (base_dir / path.lstrip("/")).exists() or (base_dir / path.lstrip("/")).is_symlink()]
					if backupChoices:
						print("Backing up: ",*backupChoices)
						tar.add(backupTempDirectory,arcname=".")
		else:
			with tarfile.open(output_path, "w:gz",  dereference=True) as tar:
				for path in validBackupChoices:
					print("Backing up: " + path)
					tar.add(path, arcname=os.path.relpath(path, home))
		return output_path
	else:
		print("No valid paths for System File Backup were provided")
		Environment.errors.append("Backup Failed: No valid paths for System File Backup were provided")
		return None


def restoreTemporarySystemFileBackup():
	# Restore system specific configuration files from temporary backup

	applicationUserName = Environment.getApplicationUsername()

	if os.path.isdir("/tmp/EmmConfigurationBackup"):
		for systemSpecificConfigFile in Environment.systemSpecificConfigFiles:
			if os.path.isfile("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile) and not os.path.isfile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + systemSpecificConfigFile):
				shutil.copy("/tmp/EmmConfigurationBackup" + "/" + systemSpecificConfigFile, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + systemSpecificConfigFile)
		for directoryToPreserve in Environment.directoriesToPreserve:
			if os.path.isdir("/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve):
				if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + directoryToPreserve):
					shutil.rmtree(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + directoryToPreserve)
				shutil.move("/tmp/EmmConfigurationBackup" + "/" + directoryToPreserve, ESTUtilities.getUserHomeDirectory(applicationUserName) + "/" + directoryToPreserve)
		shutil.rmtree("/tmp/EmmConfigurationBackup")
