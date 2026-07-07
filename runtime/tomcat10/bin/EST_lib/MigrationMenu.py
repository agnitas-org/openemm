import sys
import os
import shutil
import subprocess
import random
import string
import logging
import tempfile
import time
import datetime

from EST_lib.Environment import Environment
from EST_lib import Colors
from EST_lib import DbConnector
from EST_lib import ESTUtilities
from EST_lib.License import License

def startMenuAction(actionParameters):
	print("Migration from OpenEMM to EMM")

	print()

	print("Do you want to migrate the OpenEMM database now (Stop OpenEMM applications before this)? (N/y, Blank => Cancel):")
	answer = input(" > ").lower().strip()
	if answer.startswith("y") or answer.startswith("j"):
		if Environment.isOpenEmmServer:
			applicationUserName = "openemm"
		else:
			applicationUserName = Environment.getFrontendApplicationUserName()

		#print("Stopping OpenEMM")
		#os.system(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/bin/" + Environment.restartToolName + " stop")

		if os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb"):
			print("Creating extended database structure ...")
			if not os.path.isdir(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log"):
				ESTUtilities.createDirectories(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log", applicationUserName)
			sqlUpdateLogfilePath = ESTUtilities.getUserHomeDirectory(applicationUserName) + "/release/log/sql_update_" + datetime.datetime.now().strftime("%Y-%m-%d_%H-%M-%S") + ".log"
			fullDbExtendedScriptSuccess = DbConnector.executeSqlScriptFile(ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-fulldb-extended.sql", sqlUpdateLogfilePath)

			if not fullDbExtendedScriptSuccess:
				if sqlUpdateLogfilePath is not None and os.path.isfile(sqlUpdateLogfilePath):
					with open(sqlUpdateLogfilePath) as sqlUpdateLogfile:
						sqlLogContent = sqlUpdateLogfile.read()
						print(str(sqlLogContent))

				errorText = "Error while executing full database extended script"
				Environment.errors.append(errorText)
				print(errorText + "\n")
				return
			else:
				DbConnector.update("DELETE FROM license_tbl")
				DbConnector.update("DELETE FROM config_tbl WHERE class = 'system' AND name = 'licence'")
				messageText = "Successfully migrated OpenEMM database to EMM database.\nPlease install new EMM license before application startup."
				Environment.messages.append(messageText)
				print(messageText + "\n")

			os.system("chmod u+x " + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh")
			sqlUpdateReturnCode = os.system("/bin/bash -c '" + ESTUtilities.getUserHomeDirectory(applicationUserName) + "/webapps/emm/WEB-INF/sql/mariadb/emm-mariadb-execute-missing-updates.sh " + DbConnector.dbcfgPropertiesFilePath + " 2>&1 | tee " + sqlUpdateLogfilePath + "; test ${PIPESTATUS[0]} -eq 0'")
		else:
			print("Extended database definitions not found. Install EMM frontend application first." + "\n")

		print("Press any key to continue.")
		choice = input(" > ")
	return
