import os  
import xml.dom.minidom
import xml.etree.ElementTree as ET

from datetime import date, datetime
from EST_lib import Environment
from EST_lib import ESTUtilities
from EST_lib import DbConnector

class License:
	licenseProperties = None

	@staticmethod
	def parseCompanySpecificValues(license, company_parameter:list[str]):

		tree = ET.fromstring(license)
		companies = {}
		for item in tree.findall('.//company'):
			id = item.get("id")
			if id:
				companies[id] = {}
				for parameter in company_parameter:
					companySpecificValue = item.find(parameter)
					if companySpecificValue and companySpecificValue.text:
						companies[id][parameter] = companySpecificValue.text
					else:
						companies[id][parameter] = None
		return companies


	@staticmethod
	def readLicenseValues():
		licenseXmlDocument = None
		if DbConnector.checkDbConnection():
			data = DbConnector.selectValue("SELECT data FROM license_tbl WHERE name = 'LicenseData'")
			if data is not None:
				# Read license information from db
				licenseXmlDocument = xml.dom.minidom.parseString(data.decode("utf-8"))

		if licenseXmlDocument is None and Environment.Environment.emmLicenseFilePath is not None and os.path.isfile(Environment.Environment.emmLicenseFilePath):
			# Read license information from file
			licenseXmlDocument = xml.dom.minidom.parse(Environment.Environment.emmLicenseFilePath)

		if licenseXmlDocument is not None:
			licenseRootNodes = licenseXmlDocument.getElementsByTagName("emm.license")
			licenseProperties = {}
			for licenseNode in licenseRootNodes:
				if not 0 in licenseProperties:
					licenseProperties[0] = {}
				for valueNode in licenseNode.childNodes:
					if valueNode.nodeType == valueNode.ELEMENT_NODE:
						if "companies" == valueNode.nodeName:
							for companyNode in valueNode.getElementsByTagName("company"):
								companyID = int(companyNode.getAttribute('id'))
								if not companyID in licenseProperties:
									licenseProperties[companyID] = {}
								for companyValueNode in companyNode.childNodes:
									if companyValueNode.nodeType == companyValueNode.ELEMENT_NODE:
										licenseProperties[companyID][companyValueNode.nodeName] = ESTUtilities.getXmlNodeText(companyValueNode)
						else:
							licenseProperties[0][valueNode.nodeName] = ESTUtilities.getXmlNodeText(valueNode)
			License.licenseProperties = licenseProperties
		else:
			License.licenseProperties = None

		return License.licenseProperties

	@staticmethod
	def updateLicense(licenseData, licenseSignature):
		DbConnector.update("DELETE FROM license_tbl")

		DbConnector.update("INSERT INTO license_tbl (name, change_date) VALUES ('LicenseData', CURRENT_TIMESTAMP)")
		DbConnector.update("INSERT INTO license_tbl (name, change_date) VALUES ('LicenseSignature', CURRENT_TIMESTAMP)")

		DbConnector.update("UPDATE license_tbl SET data = ? WHERE name = 'LicenseData'", licenseData)
		DbConnector.update("UPDATE license_tbl SET data = ? WHERE name = 'LicenseSignature'", licenseSignature)

		newLicenseValues = License.readLicenseValues()
		parsedCompanySpecificValues = License.parseCompanySpecificValues(licenseData, Environment.Environment.companySpecificValues)

		for companyId in parsedCompanySpecificValues.keys():
			for neededParameter in set(Environment.Environment.companySpecificValues) & set(parsedCompanySpecificValues[companyId].keys()):
				parameterValue = parsedCompanySpecificValues[companyId][neededParameter]
				if parameterValue:
					DbConnector.createOrUpdateCompanyInfoTableEntry(companyId, neededParameter, parameterValue)
					DbConnector.deleteCompanyInfoTableEntry(0, neededParameter)

		licenseID = None
		if newLicenseValues is not None and 0 in newLicenseValues and "licenseID" in newLicenseValues[0]:
			licenseID = newLicenseValues[0]["licenseID"]

		if licenseID is not None and DbConnector.selectValue("SELECT COUNT(*) FROM config_tbl WHERE class = 'system' and name = 'licence'") == 0:
			DbConnector.update("INSERT INTO config_tbl (class, name, value) VALUES ('system', 'licence', ?)", licenseID)

		License.checkLicenseStatus()
		return newLicenseValues

	@staticmethod
	def getLicenseValue(licenseValueName, companyID=0):
		if License.licenseProperties is None:
			License.readLicenseValues()

		returnValue = None
		if License.licenseProperties is not None:
			if companyID in License.licenseProperties and licenseValueName in License.licenseProperties[companyID] and ESTUtilities.isNotBlank(License.licenseProperties[companyID][licenseValueName]):
				returnValue = License.licenseProperties[companyID][licenseValueName]
			elif companyID != 0 and 0 in License.licenseProperties and licenseValueName in License.licenseProperties[0] and ESTUtilities.isNotBlank(License.licenseProperties[0][licenseValueName]):
				returnValue = License.licenseProperties[0][licenseValueName]

			if returnValue is not None and "unlimited" == returnValue:
				returnValue = "-1"

		return returnValue

	@staticmethod
	def getLicenseIntegerValue(licenseValueName, companyID=0):
		licenseValueString = License.getLicenseValue(licenseValueName, companyID)
		if licenseValueString is None or ESTUtilities.isBlank(licenseValueString):
			return 0
		else:
			return int(licenseValueString)
		
	@staticmethod
	def getLicenseIntegerValueWithDefault(licenseValueName, companyID=0, defaultValue=0):
		licenseValueString = License.getLicenseValue(licenseValueName, companyID)
		if licenseValueString is None or ESTUtilities.isBlank(licenseValueString):
			return defaultValue
		else:
			return int(licenseValueString)

	@staticmethod
	def getLicenseID():
		return License.getLicenseValue("licenseID")

	@staticmethod
	def getLicenseName():
		return License.getLicenseValue("licenseHolder")

	@staticmethod
	def checkLicenseStatus():
		if Environment.Environment.applicationName == "EMM":
			dbLicenseId = DbConnector.readConfigurationValueFromDB("system", "licence", Environment.Environment.hostname)
			if dbLicenseId is None:
				Environment.Environment.errors.append("LicenseID is not set in database. Maybe a restart of the application is needed.")
			elif License.getLicenseValue("licenseID") != dbLicenseId:
				Environment.Environment.errors.append("LicenseID of database and licensedata do not match")

			if License.getLicenseValue("expirationDate") is not None:
				today = date.today()
				expirationDate = datetime.strptime(License.getLicenseValue("expirationDate"), "%d.%m.%Y")
				if expirationDate < today:
					Environment.Environment.errors.append("License is expired by " + License.getLicenseValue("expirationDate"))

			companyIds = DbConnector.select("SELECT company_id FROM company_tbl WHERE status = 'active'")

			maximumNumberOfCompanies = License.getLicenseIntegerValue("maximumNumberOfCompanies")
			if maximumNumberOfCompanies > -1 and maximumNumberOfCompanies < len(companyIds):
				Environment.Environment.errors.append("License limit of active clients is exceeded. Allowed: " + str(maximumNumberOfCompanies) + " Current: " + str(len(companyIds)))

			if companyIds is not None:
				for companyIdRow in companyIds:
					companyID = int(companyIdRow[0])

					if License.getLicenseValue("maximumNumberOfGuiAdmins", companyID) is None:
						maximumNumberOfGuiAdmins = License.getLicenseIntegerValue("maximumNumberOfAdmins", companyID)
					else:
						maximumNumberOfGuiAdmins = License.getLicenseIntegerValue("maximumNumberOfGuiAdmins", companyID)
					if maximumNumberOfGuiAdmins > -1:
						currentNumberOfGuiAdmins = int(DbConnector.selectValue("SELECT COUNT(*) FROM admin_tbl WHERE company_id = ? AND restful = 0", companyID))
						if maximumNumberOfGuiAdmins < currentNumberOfGuiAdmins:
							Environment.Environment.warnings.append("License limit of GUI users for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfGuiAdmins) + ", Current: " + str(currentNumberOfGuiAdmins))

					maximumNumberOfWebserviceUsers = License.getLicenseIntegerValue("maximumNumberOfWebserviceUsers", companyID)
					if maximumNumberOfWebserviceUsers > -1:
						currentNumberOfWebserviceUsers = int(DbConnector.selectValue("SELECT COUNT(*) FROM webservice_user_tbl WHERE company_id = ? AND active = 1", companyID))
						if maximumNumberOfWebserviceUsers < currentNumberOfWebserviceUsers:
							Environment.Environment.warnings.append("License limit of SOAP webservice users for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfWebserviceUsers) + ", Current: " + str(currentNumberOfWebserviceUsers))

					if License.getLicenseValue("maximumNumberOfRestfulUsers", companyID) is None:
						maximumNumberOfRestfulUsers = 0
					else:
						maximumNumberOfRestfulUsers = License.getLicenseIntegerValue("maximumNumberOfRestfulUsers", companyID)
					if maximumNumberOfRestfulUsers > -1:
						currentNumberOfRestfulUsers = int(DbConnector.selectValue("SELECT COUNT(*) FROM admin_tbl WHERE company_id = ? AND restful = 1", companyID))
						if maximumNumberOfRestfulUsers < currentNumberOfRestfulUsers:
							Environment.Environment.warnings.append("License limit of restful users for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfRestfulUsers) + ", Current: " + str(currentNumberOfRestfulUsers))

					maximumNumberOfCustomers = License.getLicenseIntegerValue("maximumNumberOfCustomers", companyID)
					maximumNumberOfCustomersGracefulExtension = License.getLicenseIntegerValueWithDefault("maximumNumberOfCustomersGraceful", companyID, 5000)
					if maximumNumberOfCustomers > -1:
						currentNumberOfCustomers = int(DbConnector.selectValue("SELECT COUNT(*) FROM customer_" + str(companyID) + "_tbl"))
						if (maximumNumberOfCustomers + maximumNumberOfCustomersGracefulExtension) < currentNumberOfCustomers:
							Environment.Environment.errors.append("License limit of customers for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfCompanies) + ", Current: " + str(len(companyIds)))
						elif maximumNumberOfCustomers < currentNumberOfCustomers:
							Environment.Environment.warnings.append("License limit of customers for client " + str(companyID) + " is exceeded, but gracefully " + str(maximumNumberOfCustomersGracefulExtension) + " more were permitted. Allowed: " + str(maximumNumberOfCompanies) + ", Current: " + str(len(companyIds)))

					maximumNumberOfProfileFields = License.getLicenseIntegerValue("maximumNumberOfProfileFields", companyID)
					numberOfStandardProfileFields = 18
					maximumNumberOfProfileFieldsGracefulExtension = License.getLicenseIntegerValueWithDefault("maximumNumberOfProfileFieldsGraceful", companyID, 10)
					if maximumNumberOfProfileFields > -1:
						if DbConnector.emmDbVendor == "oracle":
							currentNumberOfProfileFields = int(DbConnector.selectValue("SELECT COUNT(column_name) FROM user_tab_columns WHERE LOWER(table_name) = LOWER('customer_" + str(companyID) + "_tbl')"))
						else:
							currentNumberOfProfileFields = int(DbConnector.selectValue("SELECT COUNT(column_name) FROM information_schema.columns WHERE table_name = 'customer_" + str(companyID) + "_tbl'"))
						# Reduce amount by standard profile fields, because the license limit only counts on self defined profile fields
						currentNumberOfProfileFields = currentNumberOfProfileFields - numberOfStandardProfileFields

						if (maximumNumberOfProfileFields + maximumNumberOfProfileFieldsGracefulExtension) < currentNumberOfProfileFields:
							Environment.Environment.errors.append("License limit of profile fields for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfCompanies) + ", Current: " + str(len(companyIds)))
						elif maximumNumberOfProfileFields < currentNumberOfProfileFields:
							Environment.Environment.warnings.append("License limit of profile fields for client " + str(companyID) + " is exceeded, but gracefully " + str(maximumNumberOfProfileFieldsGracefulExtension) + " more were permitted. Allowed: " + str(maximumNumberOfCompanies) + ", Current: " + str(len(companyIds)))

					maximumNumberOfReferenceTables = License.getLicenseIntegerValue("maximumNumberOfReferenceTables", companyID)
					maximumNumberOfReferenceTablesGracefulExtension = License.getLicenseIntegerValueWithDefault("maximumNumberOfReferenceTablesGraceful", companyID, 5)
					if maximumNumberOfReferenceTables > -1:
						currentNumberOfCustomers = int(DbConnector.selectValue("SELECT COUNT(*) FROM reference_tbl WHERE company_id = ? AND deleted = 0", companyID))
						if (maximumNumberOfReferenceTables + maximumNumberOfReferenceTablesGracefulExtension) < currentNumberOfCustomers:
							Environment.Environment.errors.append("License limit of reference tables for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfReferenceTables) + ", Current: " + str(currentNumberOfCustomers))
						elif maximumNumberOfReferenceTables < currentNumberOfCustomers:
							Environment.Environment.warnings.append("License limit of reference tables for client " + str(companyID) + " is exceeded, but gracefully " + str(maximumNumberOfReferenceTablesGracefulExtension) + " more were permitted. Allowed: " + str(maximumNumberOfReferenceTables) + ", Current: " + str(currentNumberOfCustomers))

					maximumNumberOfAccessLimitingMailinglists = License.getLicenseIntegerValue("maximumNumberOfAccessLimitingMailinglistsPerCompany", companyID)
					maximumNumberOfAccessLimitingMailinglistsGracefulExtension = License.getLicenseIntegerValueWithDefault("maximumNumberOfAccessLimitingMailinglistsPerCompanyGraceful", companyID, 3);
					if maximumNumberOfAccessLimitingMailinglists > -1 and DbConnector.checkTableExists("disabled_mailinglist_tbl"):
						currentNumberOfCustomers = int(DbConnector.selectValue("SELECT COUNT(DISTINCT mailinglist_id) FROM disabled_mailinglist_tbl WHERE company_id = ?", companyID))
						if (maximumNumberOfAccessLimitingMailinglists + maximumNumberOfAccessLimitingMailinglistsGracefulExtension) < currentNumberOfCustomers:
							Environment.Environment.errors.append("License limit of AccessLimitingMailinglists for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfAccessLimitingMailinglists) + ", Current: " + str(currentNumberOfCustomers))
						elif maximumNumberOfAccessLimitingMailinglists < currentNumberOfCustomers:
							Environment.Environment.warnings.append("License limit of AccessLimitingMailinglists for client " + str(companyID) + " is exceeded, but gracefully " + str(maximumNumberOfAccessLimitingMailinglistsGracefulExtension) + " more were permitted. Allowed: " + str(maximumNumberOfAccessLimitingMailinglists) + ", Current: " + str(currentNumberOfCustomers))

					maximumNumberOfAccessLimitingTargetgroups = License.getLicenseIntegerValue("maximumNumberOfAccessLimitingTargetgroupsPerCompany", companyID)
					maximumNumberOfAccessLimitingTargetgroupsGracefulExtension = License.getLicenseIntegerValueWithDefault("maximumNumberOfAccessLimitingTargetgroupsPerCompanyGraceful", companyID, 0)
					if maximumNumberOfAccessLimitingTargetgroups > -1 and DbConnector.checkColumnExists("disabled_mailinglist_tbl", "is_access_limiting"):
						currentNumberOfCustomers = int(DbConnector.selectValue("SELECT COUNT(*) FROM dyn_target_tbl WHERE is_access_limiting = 1 AND company_id = ?", companyID))
						if (maximumNumberOfAccessLimitingTargetgroups + maximumNumberOfAccessLimitingTargetgroupsGracefulExtension) < currentNumberOfCustomers:
							Environment.Environment.errors.append("License limit of AccessLimitingTargetgroups for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfAccessLimitingTargetgroups) + ", Current: " + str(currentNumberOfCustomers))
						elif maximumNumberOfAccessLimitingTargetgroups < currentNumberOfCustomers:
							Environment.Environment.warnings.append("License limit of AccessLimitingTargetgroups for client " + str(companyID) + " is exceeded, but gracefully " + str(maximumNumberOfAccessLimitingTargetgroupsGracefulExtension) + " more were permitted. Allowed: " + str(maximumNumberOfAccessLimitingTargetgroups) + ", Current: " + str(currentNumberOfCustomers))
