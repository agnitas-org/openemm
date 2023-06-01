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
import xml.dom.minidom

from datetime import date, datetime

from EMT_lib import Environment
from EMT_lib import EMTUtilities
from EMT_lib import DbConnector

class License:
	licenseProperties = None

	@staticmethod
	def readLicenseValues():
		licenseXmlDocument = None
		if DbConnector.checkDbServiceAvailable():
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
										licenseProperties[companyID][companyValueNode.nodeName] = EMTUtilities.getXmlNodeText(companyValueNode)
						else:
							licenseProperties[0][valueNode.nodeName] = EMTUtilities.getXmlNodeText(valueNode)
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
		License.checkLicenseStatus()
		return newLicenseValues

	@staticmethod
	def getLicenseValue(licenseValueName, companyID=0):
		if License.licenseProperties is None:
			License.readLicenseValues()

		returnValue = None
		if License.licenseProperties is not None:
			if companyID in License.licenseProperties and licenseValueName in License.licenseProperties[companyID] and EMTUtilities.isNotBlank(License.licenseProperties[companyID][licenseValueName]):
				returnValue = License.licenseProperties[companyID][licenseValueName]
			elif companyID != 0 and 0 in License.licenseProperties and licenseValueName in License.licenseProperties[0] and EMTUtilities.isNotBlank(License.licenseProperties[0][licenseValueName]):
				returnValue = License.licenseProperties[0][licenseValueName]
			
			if returnValue is not None and "unlimited" == returnValue:
				returnValue = "-1"
		
		return returnValue
	
	@staticmethod
	def getLicenseIntegerValue(licenseValueName, companyID=0):
		licenseValueString = License.getLicenseValue(licenseValueName, companyID)
		if licenseValueString is None or EMTUtilities.isBlank(licenseValueString):
			return -1
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
				Environment.Environment.errors.append("LicenseID is not set in database")
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
				maximumNumberOfCustomersGracefulExtension = 5000
				if maximumNumberOfCustomers > -1:
					currentNumberOfCustomers = int(DbConnector.selectValue("SELECT COUNT(*) FROM customer_" + str(companyID) + "_tbl"))
					if (maximumNumberOfCustomers + maximumNumberOfCustomersGracefulExtension) < currentNumberOfCustomers:
						Environment.Environment.errors.append("License limit of customers for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfCompanies) + ", Current: " + str(len(companyIds)))
					elif maximumNumberOfCustomers < currentNumberOfCustomers:
						Environment.Environment.warnings.append("License limit of customers for client " + str(companyID) + " is exceeded, but gracefully " + str(maximumNumberOfCustomersGracefulExtension) + " more were permitted. Allowed: " + str(maximumNumberOfCompanies) + ", Current: " + str(len(companyIds)))

				maximumNumberOfProfileFields = License.getLicenseIntegerValue("maximumNumberOfProfileFields", companyID)
				numberOfStandardProfileFields = 18
				maximumNumberOfProfileFieldsGracefulExtension = 10
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
				maximumNumberOfReferenceTablesGracefulExtension = 5
				if maximumNumberOfReferenceTables > -1:
					currentNumberOfCustomers = int(DbConnector.selectValue("SELECT COUNT(*) FROM reference_tbl WHERE company_id = ? AND deleted = 0", companyID))
					if (maximumNumberOfReferenceTables + maximumNumberOfReferenceTablesGracefulExtension) < currentNumberOfCustomers:
						Environment.Environment.errors.append("License limit of reference tables for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfReferenceTables) + ", Current: " + str(currentNumberOfCustomers))
					elif maximumNumberOfReferenceTables < currentNumberOfCustomers:
						Environment.Environment.warnings.append("License limit of reference tables for client " + str(companyID) + " is exceeded, but gracefully " + str(maximumNumberOfReferenceTablesGracefulExtension) + " more were permitted. Allowed: " + str(maximumNumberOfReferenceTables) + ", Current: " + str(currentNumberOfCustomers))

				maximumNumberOfAccessLimitingMailinglists = License.getLicenseIntegerValue("maximumNumberOfAccessLimitingMailinglistsPerCompany", companyID)
				maximumNumberOfAccessLimitingMailinglistsGracefulExtension = 3
				if maximumNumberOfAccessLimitingMailinglists > -1:
					currentNumberOfCustomers = int(DbConnector.selectValue("SELECT COUNT(DISTINCT mailinglist_id) FROM disabled_mailinglist_tbl WHERE company_id = ?", companyID))
					if (maximumNumberOfAccessLimitingMailinglists + maximumNumberOfAccessLimitingMailinglistsGracefulExtension) < currentNumberOfCustomers:
						Environment.Environment.errors.append("License limit of AccessLimitingMailinglists for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfAccessLimitingMailinglists) + ", Current: " + str(currentNumberOfCustomers))
					elif maximumNumberOfAccessLimitingMailinglists < currentNumberOfCustomers:
						Environment.Environment.warnings.append("License limit of AccessLimitingMailinglists for client " + str(companyID) + " is exceeded, but gracefully " + str(maximumNumberOfAccessLimitingMailinglistsGracefulExtension) + " more were permitted. Allowed: " + str(maximumNumberOfAccessLimitingMailinglists) + ", Current: " + str(currentNumberOfCustomers))

				maximumNumberOfAccessLimitingTargetgroups = License.getLicenseIntegerValue("maximumNumberOfAccessLimitingTargetgroupsPerCompany", companyID)
				maximumNumberOfAccessLimitingTargetgroupsGracefulExtension = 0
				if maximumNumberOfAccessLimitingTargetgroups > -1:
					currentNumberOfCustomers = int(DbConnector.selectValue("SELECT COUNT(*) FROM dyn_target_tbl WHERE is_access_limiting = 1 AND company_id = ?", companyID))
					if (maximumNumberOfAccessLimitingTargetgroups + maximumNumberOfAccessLimitingTargetgroupsGracefulExtension) < currentNumberOfCustomers:
						Environment.Environment.errors.append("License limit of AccessLimitingTargetgroups for client " + str(companyID) + " is exceeded. Allowed: " + str(maximumNumberOfAccessLimitingTargetgroups) + ", Current: " + str(currentNumberOfCustomers))
					elif maximumNumberOfAccessLimitingTargetgroups < currentNumberOfCustomers:
						Environment.Environment.warnings.append("License limit of AccessLimitingTargetgroups for client " + str(companyID) + " is exceeded, but gracefully " + str(maximumNumberOfAccessLimitingTargetgroupsGracefulExtension) + " more were permitted. Allowed: " + str(maximumNumberOfAccessLimitingTargetgroups) + ", Current: " + str(currentNumberOfCustomers))
