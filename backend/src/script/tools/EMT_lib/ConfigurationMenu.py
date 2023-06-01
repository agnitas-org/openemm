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
import logging

from EMT_lib.Environment import Environment
from EMT_lib import Colors
from EMT_lib import DbConnector
from EMT_lib import EMTUtilities

def configTableMenuAction(actionParameters):
	DbConnector.checkConfigurationInDB()

	if Environment.getSystemUrl() == "Unknown":
		print("Set essential configuration properties")
		print()
		print("Please enter system.url (with protocol, e.g.: https://my.openemm.net)")
		choice = input(" > ")
		choice = choice.strip()
		DbConnector.updateConfigurationValueInDB("system", "url", choice, Environment.hostname)
		DbConnector.updateConfigurationValueInDB("birt", "url", choice + "/birt", Environment.hostname)
		DbConnector.updateConfigurationValueInDB("birt", "url.intern", "localhost:8080/birt", Environment.hostname)
		DbConnector.updateConfigurationValueInDB("system", "defaultRdirDomain", choice, Environment.hostname)
		DbConnector.updateConfigurationValueInDB("system", "RdirLandingpage", "", Environment.hostname)
		DbConnector.updateConfigurationValueInDB("system", "support_emergency_url", "", Environment.hostname)
		DbConnector.updateConfigurationValueInDB("webservices", "url", choice + "/2.0/", Environment.hostname)

		birtKeysExist = DbConnector.selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'birt' and name = 'privatekey' AND value != '[to be defined]'") > 0

		applicationUserName = "openemm" if Environment.isOpenEmmServer else "console"
		print()
		if not birtKeysExist and not os.path.isfile("/home/" + applicationUserName + "/tomcat/conf/keys/birt_private.pem"):
			print("Generating Initial Statistics PPK keys")
			try:
				keyGenReturnCode = os.system("openssl genrsa -out \"/home/" + applicationUserName + "/tomcat/conf/keys/birt_private.pem\" 2048; openssl rsa -in \"/home/" + applicationUserName + "/tomcat/conf/keys/birt_private.pem\" -out \"/home/" + applicationUserName + "/tomcat/conf/keys/birt_public.pem\" -pubout")
			except:
				keyGenReturnCode = -1
			if keyGenReturnCode == 0:
				print("Initial Statistics PPK keys have been successfully generated")
			else:
				print("Cannot generate initial Statistics PPK keys")
		else:
			print("Statistics PPK keys exist")

		DbConnector.insertBirtKeysInDb(applicationUserName)

		defaultMailloopDomain = choice
		if defaultMailloopDomain.lower().startswith("http://"):
			defaultMailloopDomain = defaultMailloopDomain[7:]
		elif defaultMailloopDomain.lower().startswith("https://"):
			defaultMailloopDomain = defaultMailloopDomain[8:]
		DbConnector.updateConfigurationValueInDB("system", "defaultMailloopDomain", defaultMailloopDomain, Environment.hostname)
		DbConnector.updateConfigurationValueInDB("mailout", "ini.domain", defaultMailloopDomain, Environment.hostname)

		DbConnector.update("UPDATE company_tbl SET mailloop_domain = ?, rdir_domain = ?, timestamp = CURRENT_TIMESTAMP", defaultMailloopDomain, choice)

		print()

	currentConfigurationValues = DbConnector.readConfigurationFromDB()
	configurationValueDescriptions = {
		"system.url": "Url for " + Environment.applicationName + "-GUI",
		"system.defaultRdirDomain": "Default rdir domain for new created clients (Add the protocol, e.g. https://<rdir_domain>)",
		"system.defaultMailloopDomain":"Default mailloopDomain domain for new created clients (Do not add any protocol)",
		"birt.url": "Url for statistic requests (e.g. https://<emm_domain>/birt",
		"webservices.url": "Url for Webservices: https://<domain>/2.0/"
	}
	if Environment.unsavedConfigChanges is not None:
		for changedConfigurationItem in Environment.unsavedConfigChanges:
			isNewEntry = True
			for configurationItem in currentConfigurationValues:
				if changedConfigurationItem["class"] == configurationItem["class"] and changedConfigurationItem["name"] == configurationItem["name"]:
					isNewEntry = False
			if isNewEntry:
				if (configurationItem["class"] + "." + changedConfigurationItem["name"]) in configurationValueDescriptions:
					description = configurationValueDescriptions[configurationItem["class"] + "." + changedConfigurationItem["name"]]
				else:
					description = ""
				currentConfigurationValues.append({"class": changedConfigurationItem["class"], "name": changedConfigurationItem["name"], "value": changedConfigurationItem["value"], "description": description})

	print(Environment.applicationName + " configuration in DB")
	print()

	for configurationItem in currentConfigurationValues:
		key = configurationItem["class"] + "." + configurationItem["name"]
		value = configurationItem["value"]
		if (configurationItem["class"] + "." + configurationItem["name"]) in configurationValueDescriptions:
			description = configurationValueDescriptions[configurationItem["class"] + "." + configurationItem["name"]]
		else:
			description = ""

		valueColor = Colors.DEFAULT
		if value is not None and ("[to be defined]" in key or "[to be defined]" in value):
			valueColor = Colors.YELLOW

		wasChanged = False
		if Environment.unsavedConfigChanges is not None:
			for changedConfigurationItem in Environment.unsavedConfigChanges:
				if changedConfigurationItem["class"] == configurationItem["class"] and changedConfigurationItem["name"] == configurationItem["name"]:
					value = changedConfigurationItem["value"]
					wasChanged = True
					break
		if wasChanged == True:
			if value == "<delete>":
				print(Colors.RED + " " + key + ((Colors.GREEN + " (" + description + ")" + Colors.DEFAULT) if description is not None and len(description) > 0 else "") + " = " + Colors.RED + "<delete>" + Colors.DEFAULT)
			else:
				print(Colors.YELLOW + " " + key + ((Colors.GREEN + " (" + description + ")" + Colors.DEFAULT) if description is not None and len(description) > 0 else "") + " = " + valueColor + (value if value is not None else "") + Colors.DEFAULT)
		elif (configurationItem["class"] + "." + configurationItem["name"]) in Environment.readonlyConfigProperties:
			print(Colors.RED + " " + key + ((Colors.GREEN + " (" + description + ")" + Colors.DEFAULT) if description is not None and len(description) > 0 else "") + " = " + valueColor + (value if value is not None else "") + Colors.DEFAULT)
		elif configurationItem["class"] == "birt" and configurationItem["name"].endswith("key") and value != "[to be defined]":
			print(" " + key + ((Colors.GREEN + " (" + description + ")" + Colors.DEFAULT) if description is not None and len(description) > 0 else "") + " = " + valueColor + ((value[0:5] + " ... " + value[-5:]) if value is not None else "") + Colors.DEFAULT)
		else:
			print(" " + key + ((Colors.GREEN + " (" + description + ")" + Colors.DEFAULT) if description is not None and len(description) > 0 else "") + " = " + valueColor + (value if value is not None else "") + Colors.DEFAULT)

	print()

	print("Please choose entry name to change (Blank => Back, 'new' => new configuration key" + (", 'save' to save changes, 'cancel' to drop changes" if Environment.unsavedConfigChanges is not None else "") + "):")

	choice = input(" > ")
	choice = choice.strip()

	if choice == "":
		return
	elif Environment.unsavedConfigChanges is not None and choice.lower() == "save":
		try:
			for unsavedConfigChange in Environment.unsavedConfigChanges:
				DbConnector.updateConfigurationValueInDB(unsavedConfigChange["class"], unsavedConfigChange["name"], unsavedConfigChange["value"], Environment.hostname)
		except:
			if EMTUtilities.isDebugMode():
				logging.exception("Cannot save file configurationValues")
			Environment.errors.append("Cannot save file configurationValues")
			return False

		Environment.unsavedConfigChanges = None
		Environment.messages.append("Changes saved")
		return True
	elif Environment.unsavedConfigChanges is not None and choice.lower() == "cancel":
		Environment.unsavedConfigChanges = None
		Environment.messages.append("Changes reverted")
		return True
	elif choice.strip().lower() == "new":
		print("Please enter name for new configuration key: ")
		newKey = input(" > ")
		newKey = newKey.strip()
		if "." in newKey:
			className = newKey[:newKey.find(".")]
			configName = newKey[newKey.find(".") + 1:]
			found = False
			for configurationItem in currentConfigurationValues:
				if className == configurationItem["class"] and configName == configurationItem["name"]:
					found = True
					break
			if not found:
				configurationKey = newKey
				print("Please enter value for key '" + configurationKey + "': ")
				newValue = input(" > ")
				if Environment.unsavedConfigChanges is None:
					Environment.unsavedConfigChanges = []
				Environment.unsavedConfigChanges.append({"class": className, "name": configName, "value": newValue.strip(), "description": "Added via " + Environment.toolName})
				return True
			else:
				Environment.errors.append("New key name already exists: " + newKey)
				return False
		else:
			Environment.errors.append("New key name must contain '.': " + newKey)
			return False
	else:
		className = choice[:choice.find(".")]
		configName = choice[choice.find(".") + 1:]
		found = False
		for configurationItem in currentConfigurationValues:
			if className == configurationItem["class"] and configName == configurationItem["name"]:
				if (configurationItem["class"] + "." + configurationItem["name"]) in Environment.readonlyConfigProperties:
					Environment.errors.append((className + "." + configName) + " is read-only and may not be changed.")
					break
				else:
					found = True
					break
		if found:
			configurationKey = choice
			print("Please enter new value for key '" + configurationKey + "' ('delete' => delete): ")
			newValue = input(" > ")
			if Environment.unsavedConfigChanges is None:
				Environment.unsavedConfigChanges = []
			newValue = newValue.strip()
			if newValue.lower() == "delete":
				itemToRemoveFound = False
				for changedConfigurationItem in Environment.unsavedConfigChanges:
					if changedConfigurationItem["class"] == className and changedConfigurationItem["name"] == configName:
						changedConfigurationItem["value"] = "<delete>"
						itemToRemoveFound = True
				if not itemToRemoveFound:
					Environment.unsavedConfigChanges.append({"class": className, "name": configName, "value": "<delete>"})
				return True
			else:
				Environment.unsavedConfigChanges.append({"class": className, "name": configName, "value": newValue})
				return True
		else:
			Environment.errors.append("Invalid entry key name: " + choice)
			return False

def systemCfgMenuAction(actionParameters):
	print("License configuration")

	for key, value in sorted(Environment.systemCfgProperties.items()):
		color = ""
		if Environment.unsavedSystemCfgChanges is not None and key in Environment.unsavedSystemCfgChanges:
			print(Colors.YELLOW + " " + key + " = " + Environment.unsavedSystemCfgChanges[key] + Colors.DEFAULT)
		elif key in Environment.readonlyLicenseCfgProperties:
			print(Colors.RED + " " + key + " = " + value + Colors.DEFAULT)
		else:
			print(" " + key + " = " + value)

	print()

	if os.access(Environment.systemCfgFilePath, os.W_OK):
		print("Please choose entry name to change (Blank => Back" + (", 'save' to save changes, 'cancel' to drop changes" if (Environment.unsavedSystemCfgChanges is not None and len(Environment.unsavedSystemCfgChanges) > 0) else "") + "):")

		choice = input(" > ")
		choice = choice.strip()

		if choice == "":
			return
		elif Environment.unsavedSystemCfgChanges is not None and choice.lower() == "save":
			if not os.access(Environment.systemCfgFilePath, os.W_OK):
				Environment.errors.append("File is readonly: " + Environment.systemCfgFilePath)
				return False

			try:
				EMTUtilities.updatePropertiesFile(Environment.systemCfgFilePath, Environment.unsavedSystemCfgChanges)
			except:
				if EMTUtilities.isDebugMode():
					logging.exception("Cannot save file: " + Environment.systemCfgFilePath)
				Environment.errors.append("Cannot save file: " + Environment.systemCfgFilePath)
				return False

			Environment.systemCfgProperties = EMTUtilities.readPropertiesFile(Environment.systemCfgFilePath)
			Environment.unsavedSystemCfgChanges = None
			Environment.rebootNeeded = True
			Environment.otherSystemsNeedConfig = True
			Environment.messages.append("Changes saved")
			return True
		elif Environment.unsavedSystemCfgChanges is not None and choice.lower() == "cancel":
			Environment.unsavedSystemCfgChanges = None
			Environment.messages.append("Changes reverted")
			return True
		elif choice in Environment.systemCfgProperties:
			systemCfgKey = choice
			if systemCfgKey in Environment.readonlyLicenseCfgProperties:
				Environment.errors.append(systemCfgKey + " is read-only and may not be changed.")
				return False
			else:
				print("Please enter new value for key '" + systemCfgKey + "': ")
				systemCfgValue = input(" > ")

				systemCfgValue = systemCfgValue.strip()
				if Environment.unsavedSystemCfgChanges is None:
					Environment.unsavedSystemCfgChanges = {}
				Environment.unsavedSystemCfgChanges[systemCfgKey] = systemCfgValue
				return True
		else:
			Environment.errors.append("Invalid system.cfg entry key name: " + choice)
			return False
	else:
		print("Data is readonly (Blank => Back)")
		choice = input(" > ")
		return

def clientMenuAction(actionParameters):
	clientProperties = []
	result = DbConnector.select("SELECT company_id, shortname, description, rdir_domain, mailloop_domain, stat_admin, uid_version, auto_mailing_report_active, mails_per_day, max_recipients, salutation_extended, sector, business_field, enabled_uid_version, export_notify, auto_deeptracking, default_datasource_id, priority_count, contact_tech, company_token FROM company_tbl WHERE status = 'active' ORDER BY company_id")
	for row in result:
		company = {}
		company["company_id"] = row[0]
		company["shortname"] = row[1]
		company["description"] = row[2]
		company["rdir_domain"] = row[3]
		company["mailloop_domain"] = row[4]
		company["stat_admin"] = row[5]
		company["uid_version"] = row[6]
		company["auto_mailing_report_active"] = row[7]
		company["mails_per_day"] = row[8]
		company["max_recipients"] = row[9]
		company["salutation_extended"] = row[10]
		company["sector"] = row[11]
		company["business_field"] = row[12]
		company["enabled_uid_version"] = row[13]
		company["export_notify"] = row[14]
		company["auto_deeptracking"] = row[15]
		company["default_datasource_id"] = row[16]
		company["priority_count"] = row[17]
		company["contact_tech"] = row[18]
		company["company_token"] = row[19]
		clientProperties.append(company)

	result = DbConnector.select("SELECT company_id, cname, cvalue FROM company_info_tbl WHERE company_id = 0 OR company_id IN (SELECT company_id FROM company_tbl WHERE status = 'active') ORDER BY company_id")
	for row in result:
		companyID = row[0]
		propertyName = row[1]
		propertyValue = row[2]
		for company in clientProperties:
			if companyID == 0:
				company[propertyName] = propertyValue
			elif company["company_id"] == companyID:
				company[propertyName] = propertyValue
				break

	companySelected = None
	if Environment.unsavedClientChanges is not None:
		companyId = Environment.unsavedClientChanges["company_id"]
		for company in clientProperties:
			if company["company_id"] == companyId:
				companySelected = company
				break

	if companySelected is None:
		if len(clientProperties) == 1:
			companySelected = clientProperties[0]
		else:
			print("Please choose client id (Blank => Back):")
			for company in clientProperties:
				print(" " + str(company["company_id"]) + ": " + company["shortname"])

			choice = input(" > ")
			choice = choice.strip()
			if choice == "":
				return
			else:
				try:
					companyID = int(choice)
				except:
					Environment.errors.append("Invalid client id: " + choice)
					return False
				for company in clientProperties:
					if company["company_id"] == companyID:
						companySelected = company
						break

				if companySelected is None:
					Environment.errors.append("Invalid client id: " + choice)
					return False

	print("Client configuration for '" + companySelected["shortname"] + "' (ID: " + str(companySelected["company_id"]) + "):")

	for key, value in sorted(companySelected.items()):
		if key in Environment.visibleClientProperties:
			if key in Environment.readonlyClientProperties:
				print(Colors.RED + " " + key + " = " + str(value) + Colors.DEFAULT)
			elif Environment.unsavedClientChanges is not None and key in Environment.unsavedClientChanges:
				print(Colors.YELLOW + " " + key + " = " + str(Environment.unsavedClientChanges[key]) + Colors.DEFAULT)
			else:
				print(" " + key + " = " + str(value))

	print()

	print("Please choose entry name to change (Blank => Back" + (", 'save' to save changes, 'cancel' to drop changes" if Environment.unsavedClientChanges is not None else "") + "):")

	choice = input(" > ")
	choice = choice.strip()

	if choice == "":
		return
	elif Environment.unsavedClientChanges is not None and choice.lower() == "save":
		try:
			companyID = Environment.unsavedClientChanges["company_id"]
			for propertyKey in Environment.unsavedClientChanges:
				if propertyKey != "company_id":
					if propertyKey in Environment.companyInfoProperties:
						itemExists = DbConnector.selectValue("SELECT COUNT(*) FROM company_info_tbl WHERE company_id = ? AND cname = ?", companyID, propertyKey) > 0
						if itemExists:
							DbConnector.update("UPDATE company_info_tbl SET timestamp = CURRENT_TIMESTAMP, description = 'Changed by Maintenance Tool', cvalue = ? WHERE company_id = ? AND cname = ?", Environment.unsavedClientChanges[propertyKey], companyID, propertyKey)
						else:
							DbConnector.update("INSERT INTO company_info_tbl (company_id, cname, cvalue, creation_date, timestamp, description) VALUES (?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'Changed by Maintenance Tool')", companyID, propertyKey, Environment.unsavedClientChanges[propertyKey])
					else:
						DbConnector.update("UPDATE company_tbl SET " + propertyKey + " = ?, timestamp = CURRENT_TIMESTAMP WHERE company_id = ?", Environment.unsavedClientChanges[propertyKey], companyID)
		except:
			if EMTUtilities.isDebugMode():
				logging.exception("Cannot save client data")
			Environment.errors.append("Cannot save client data")
			return

		Environment.unsavedClientChanges = None
		Environment.messages.append("Changes saved")
		return True
	elif Environment.unsavedClientChanges is not None and choice.lower() == "cancel":
		Environment.unsavedClientChanges = None
		Environment.messages.append("Changes reverted")
		return True
	elif choice in companySelected and choice in Environment.visibleClientProperties:
		clientKey = choice
		if clientKey in Environment.readonlyClientProperties:
			Environment.errors.append(clientKey + " is read-only and may not be changed.")
		else:
			print("Please enter new value for key '" + clientKey + "': ")
			clientValue = input(" > ")
			clientValue = clientValue.strip()
			if Environment.unsavedClientChanges is None:
				Environment.unsavedClientChanges = {}
				Environment.unsavedClientChanges["company_id"] = companySelected["company_id"]
			Environment.unsavedClientChanges[clientKey] = clientValue
		return True
	else:
		Environment.errors.append("Invalid entry key name: " + choice)
		return False

def jobqueueMenuAction(actionParameters):
	print("JobQueue configuration")

	jobQueueHosts = DbConnector.readJobQueueHostsFromDB()

	for hostName in jobQueueHosts:
		if jobQueueHosts[hostName]:
			print(" " + Colors.GREEN + hostName + ": active" + Colors.DEFAULT)
		else:
			print(" " + Colors.RED + hostName + ": inactive" + Colors.DEFAULT)

	print()

	print("Please choose: (Blank => Back):")
	print(" 1. Activate JobQueue execution on existing host")
	print(" 2. Inactivate existing host")
	print(" 3. Add new host")
	print(" 4. Remove existing host")

	choice = input(" > ")
	choice = choice.strip()
	if choice == "":
		return
	elif choice == "1":
		print("Please enter hostname to activate: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip()
		if choice != "":
			DbConnector.storeJobQueueHostInDB(choice, True)
			Environment.messages.append("JobQueue changes saved")
		return True
	elif choice == "2":
		print("Please enter hostname to inactivate: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip()
		if choice != "":
			DbConnector.storeJobQueueHostInDB(choice, False)
			Environment.messages.append("JobQueue changes saved")
		return True
	elif choice == "3":
		print("Please enter new hostname: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip()
		if choice != "":
			DbConnector.storeJobQueueHostInDB(choice, True)
			Environment.messages.append("JobQueue changes saved")
		return True
	elif choice == "4":
		print("Please enter existing hostname to remove: (Blank => Cancel):")
		choice = input(" > ")
		choice = choice.strip()
		if choice != "":
			DbConnector.removeJobQueueHostFromDB(choice)
			Environment.messages.append("JobQueue changes saved")
		return True
	else:
		Environment.errors.append("Invalid choice: " + choice)
		return True

def landingPageMenuAction(actionParameters):
	print("Landing page configuration")

	landingPages = DbConnector.readLandingPagesFromDB(Environment.defaultLandingPage)

	for domain in landingPages:
		print(" " + str(domain) + ": " + str(landingPages[domain]))

	print()

	print("Please enter domain name to change: (Blank => Back):")

	choice = input(" > ")
	choice = choice.strip()
	if choice == "":
		return
	else:
		domain = choice
		print("Please enter new landing page for domain '" + domain + "' (Blank => Cancel, 'Remove' => Remove domain):")
		choice = input(" > ")
		choice = choice.strip()
		if choice != "":
			DbConnector.updateLandingPageInDB(domain, choice)
