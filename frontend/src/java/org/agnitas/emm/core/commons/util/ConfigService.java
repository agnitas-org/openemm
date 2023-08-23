/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.security.PublicKey;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigValue.Webservices;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DataEncryptor;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.ServerCommand.Command;
import org.agnitas.util.ServerCommand.Server;
import org.agnitas.util.Systemconfig;
import org.agnitas.util.Tuple;
import org.agnitas.util.XmlUtilities;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.agnitas.dao.AdminDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComServerMessageDao;
import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.LicenseDao;
import com.agnitas.dao.PermissionDao;
import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.dao.impl.ComServerMessageDaoImpl;
import com.agnitas.dao.impl.ConfigTableDaoImpl;
import com.agnitas.dao.impl.LicenseDaoImpl;
import com.agnitas.dao.impl.PermissionDaoImpl;
import com.agnitas.dao.impl.ProfileFieldDaoImpl;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.JavaMailServiceImpl;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.PermissionType;
import com.agnitas.emm.core.company.bean.CompanyEntry;
import com.agnitas.emm.core.permission.service.PermissionService;
import com.agnitas.emm.core.permission.service.PermissionServiceImpl;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.emm.core.profilefields.service.impl.ProfileFieldServiceImpl;
import com.agnitas.emm.core.supervisor.dao.ComSupervisorDao;
import com.agnitas.emm.wsmanager.dao.WebserviceUserDao;
import com.agnitas.messages.Message;
import com.agnitas.service.LicenseError;
import com.agnitas.util.CryptographicUtilities;
import com.agnitas.util.Version;

import jakarta.servlet.http.HttpServletRequest;

/**
 * ConfigurationService for EMM
 * This class uses buffering of the values of the config_tbl for better performance.
 * The value for refreshing period is also stored in config_tbl and can be changed
 * manually with no need for restarting the server.
 * For refreshing the values the very next time the old period value will be used.
 * Afterwards the new one will take effect.
 * The value 0 means, there will be no buffering.
 */
public class ConfigService {
	
	/** The logger. */
	private static final Logger logger = LogManager.getLogger(ConfigService.class);

	private static final String PUBLIC_LICENSE_KEYSTRING = "-----BEGIN PUBLIC KEY-----\nMIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQCcdArGIy/hseE9bz53siYnClOQ\nABrRFRVs/zdN8HpweXxpFqa4SUcp9SFIjqgQ5l/FRdEE9EFc865oGZI1H2RK9Jl1\nb7NxFBwu6S4kWFpy+0Xlp+FCLMXVkBDxLB3vv96VR714n2bFh11/UanlfptqMYPQ\nq7gZCmP5Bc06ORaxrQIDAQAB\n-----END PUBLIC KEY-----";

    public static final int MAX_GENDER_VALUE_BASIC = 2;
    public static final int MAX_GENDER_VALUE_EXTENDED = 5;
    
    public static final int MAXIMUM_ALLOWED_MAILTYPE = 5;
    
    /** Application startup timestamp **/
    private static final Date STARTUP_TIME = new Date();
    private static Version applicationVersion = null;
    private static Date applicationVersionInstallationTime = null;

	private static Server applicationType = null;
    
    private static ConfigService instance;
    
    private PermissionService permissionService;

	/** DAO for access config table. */
	protected ConfigTableDao configTableDao;
	
	/** DAO for access company_info_tbl. */
	protected CompanyInfoDao companyInfoDao;
	
	/** DAO for access company table. */
	protected ComCompanyDao companyDao;
	
	/** DAO for access admin table. */
	protected AdminDao adminDao; // TODO Replace by AdminService
	
	/** DAO for access webservice user table. */
	protected WebserviceUserDao webserviceUserDao;
	
	/** DAO for access supervisor table. */
	protected ComSupervisorDao supervisorDao;
	
	/** DAO for access license table. */
	protected LicenseDao licenseDao;
	
	/** DataEncryptor. */
	protected DataEncryptor dataEncryptor;
	
	/** DAO for access server_command_tbl table. */
	protected ComServerMessageDao serverMessageDao;
	
	protected ProfileFieldService profileFieldService;
	
	protected JavaMailService javaMailService;
	
	protected boolean licenseOK = true;

	private static Boolean IS_ORACLE_DB = null;
	private static Map<String, Map<Integer, String>> LICENSE_VALUES = null;
	private static Map<String, Map<Integer, String>> EMM_PROPERTIES_VALUES = null;
	private static Map<String, Map<Integer, String>> CONFIGURATIONVALUES = null;
	private static Date LASTREFRESHTIME = null;
	private static Calendar EXPIRATIONTIME = null;
		
	/**
	 * Method for gathering the ConfigService within JSP-files or in (BIRT-)environments without the spring context like "Listener" and "Filter" defined in web.xml
	 * All other access to ConfigService should be done via spring context or dependency injection
	 * 
	 * see also: singleton pattern
	 */
	@Deprecated // TODO Instantiate ConfigService by Spring application context
	public static synchronized ConfigService getInstance() {
		if (instance == null) {
			instance = new ConfigService();
			DataSource dataSource;
			try {
				dataSource = (DataSource) new InitialContext().lookup("java:comp/env/jdbc/emm_db");
			} catch (Exception e) {
				logger.error("Cannot find datasource in JNDI context: " + e.getMessage(), e);
				throw new RuntimeException("Cannot find datasource in JNDI context: " + e.getMessage(), e);
			}
			
			ConfigTableDao configTableDao = new ConfigTableDaoImpl();
			((ConfigTableDaoImpl) configTableDao).setDataSource(dataSource);
			instance.setConfigTableDao(configTableDao);
			
			LicenseDao licenseDao = new LicenseDaoImpl();
			((LicenseDaoImpl) licenseDao).setDataSource(dataSource);
			instance.setLicenseDao(licenseDao);
			
			CompanyInfoDao companyInfoDao = new CompanyInfoDao();
			companyInfoDao.setDataSource(dataSource);
			instance.setCompanyInfoDao(companyInfoDao);
			
			ComServerMessageDao serverMessageDao = new ComServerMessageDaoImpl();
			((ComServerMessageDaoImpl) serverMessageDao).setDataSource(dataSource);
			instance.setServerMessageDao(serverMessageDao);
			
			PermissionDao permissionDao = new PermissionDaoImpl();
			((PermissionDaoImpl) permissionDao).setDataSource(dataSource);
			PermissionService permissionService = new PermissionServiceImpl();
			((PermissionServiceImpl) permissionService).setPermissionDao(permissionDao);
			instance.setPermissionService(permissionService);
			
			ProfileFieldDao profileFieldDao = new ProfileFieldDaoImpl();
			((ProfileFieldDaoImpl) profileFieldDao).setDataSource(dataSource);
			ProfileFieldService profileFieldService = new ProfileFieldServiceImpl();
			((ProfileFieldServiceImpl) profileFieldService).setProfileFieldDao(profileFieldDao);
			instance.setProfileFieldService(profileFieldService);
			
			JavaMailService javaMailService = new JavaMailServiceImpl();
			((JavaMailServiceImpl) javaMailService).setConfigService(instance);
			instance.setJavaMailService(javaMailService);
		}
		return instance;
	}
	
	public static boolean isOracleDB() {
		return BooleanUtils.toBoolean(IS_ORACLE_DB);
	}
	
	/**
	 * This method may only be called in mock-test environments, where there is no real db to detect its vendor
	 * 
	 */
	public static void setDbVendorForMockTestingIsOracleDB(boolean isOracleDB) {
		IS_ORACLE_DB = isOracleDB;
	}
	public static Boolean getDbVendorForMockTestingIsOracleDB() {
		return IS_ORACLE_DB;
	}
	
	public ConfigService() {
		instance = this;
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection

	@Required
	public void setPermissionService(PermissionService permissionService) {
		this.permissionService = permissionService;
		invalidateCache();
		LICENSE_VALUES = null;
	}
	
	/**
	 * Set DAO accessing configuration in DB.
	 * 
	 * @param configTableDao DAO accessing configuration in DB
	 */
	@Required
	public void setConfigTableDao(ConfigTableDao configTableDao) {
		this.configTableDao = configTableDao;
		invalidateCache();
		LICENSE_VALUES = null;
	}
	
	@Required
	public void setCompanyInfoDao(CompanyInfoDao companyInfoDao) {
		this.companyInfoDao = companyInfoDao;
		invalidateCache();
		LICENSE_VALUES = null;
	}
	
	/**
	 * Set DAO accessing company data.
	 * 
	 * @param companyDao DAO accessing company data
	 */
	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
		invalidateCache();
		LICENSE_VALUES = null;
	}
	
	/**
	 * Set DAO accessing license data.
	 * 
	 * @param licenseDao DAO accessing company data
	 */
	@Required
	public void setLicenseDao(LicenseDao licenseDao) {
		this.licenseDao = licenseDao;
		invalidateCache();
		LICENSE_VALUES = null;
	}
	
	/**
	 * Set DAO accessing admin data.
	 * 
	 * @param adminDao DAO accessing admin data
	 */
	@Required
	public void setAdminDao(AdminDao adminDao) {
		this.adminDao = adminDao;
		invalidateCache();
		LICENSE_VALUES = null;
	}
	
	/**
	 * Set DAO accessing admin data.
	 * 
	 * @param webserviceUserDao DAO accessing admin data
	 */
	@Required
	public void setWebserviceUserDao(WebserviceUserDao webserviceUserDao) {
		this.webserviceUserDao = webserviceUserDao;
		invalidateCache();
		LICENSE_VALUES = null;
	}
	
	/**
	 * Set DAO accessing supervisors.
	 * 
	 * @param supervisorDao DAO accessing supervisors
	 */
	@Required
	public void setSupervisorDao(ComSupervisorDao supervisorDao) {
		this.supervisorDao = supervisorDao;
		invalidateCache();
		LICENSE_VALUES = null;
	}
	
	/**
	 * Set data encryptor.
	 * 
	 * @param dataEncryptor data encryptor
	 */
	@Required
	public void setDataEncryptor(DataEncryptor dataEncryptor) {
		this.dataEncryptor = dataEncryptor;
	}
	
	@Required
	public void setServerMessageDao(ComServerMessageDao serverMessageDao) {
		this.serverMessageDao = serverMessageDao;
	}
	
	/**
	 * Set ProfileFieldService
	 * 
	 */
	@Required
	public void setProfileFieldService(ProfileFieldService profileFieldService) {
		this.profileFieldService = profileFieldService;
	}
	
	@Required
	public void setJavaMailService(JavaMailService javaMailService) {
		this.javaMailService = javaMailService;
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic

	private void invalidateCache() {
		CONFIGURATIONVALUES = null;
	}
	
	protected synchronized void refreshValues() {
		try {
			if (IS_ORACLE_DB == null && configTableDao != null) {
				// On ConfigService startup check db vendor
				IS_ORACLE_DB = ((ConfigTableDaoImpl) configTableDao).isOracleDB();
			}
			
			if (EMM_PROPERTIES_VALUES == null) {
				// On ConfigService startup read emm.properties file
				EMM_PROPERTIES_VALUES = readEmmPropertiesFile();
				try {
					applicationVersion = new Version(EMM_PROPERTIES_VALUES.get(ConfigValue.ApplicationVersion.toString()).get(0));
				} catch (Exception e) {
					// The version sign is not valid. Maybe it is text like 'Unknown'.
				}
				if (EMM_PROPERTIES_VALUES.get(ConfigValue.ApplicationType.toString()) != null) {
					applicationType = Server.getServerByName(EMM_PROPERTIES_VALUES.get(ConfigValue.ApplicationType.toString()).get(0));
				}
				
	    		configTableDao.checkAndSetReleaseVersion();
			}

			if (LICENSE_VALUES == null) {
				// On ConfigService startup read license data and check licensefile signature
				LICENSE_VALUES = readLicenseData();

				// On ConfigService startup check licensefile data
				checkLicenseData();
				
				// No need for older license checks, because this server startet right now
				LASTREFRESHTIME = new Date();
			}
			
			if (CONFIGURATIONVALUES == null || EXPIRATIONTIME == null || GregorianCalendar.getInstance().after(EXPIRATIONTIME)) {
				Date now = new Date();
				
				// Check for server reset commands
				if (LICENSE_VALUES != null && !serverMessageDao.getCommand(LASTREFRESHTIME, now, Server.ALL, Command.RELOAD_LICENSE_DATA).isEmpty()) {
					// This is needed to allow checkLicenseData() to not get stuck in dead lock
					LASTREFRESHTIME = now;
					
					LICENSE_VALUES = readLicenseData();
					
					// On license reset check the new license data
					checkLicenseData();
				}
				
				LASTREFRESHTIME = now;
				
				Map<String, Map<Integer, String>> newValues = new HashMap<>();
				joinConfigValues(newValues, EMM_PROPERTIES_VALUES);

				if (configTableDao != null) {
					joinConfigValues(newValues, configTableDao.getAllEntriesForThisHost());
				}

				if (companyInfoDao != null) {
					joinConfigValues(newValues, companyInfoDao.getAllEntriesForThisHost());
				}

				int minutes;
				if (newValues.containsKey(ConfigValue.ConfigurationExpirationMinutes.toString())) {
					minutes = NumberUtils.toInt(newValues.get(ConfigValue.ConfigurationExpirationMinutes.toString()).get(0));
				} else {
					minutes = NumberUtils.toInt(ConfigValue.ConfigurationExpirationMinutes.getDefaultValue());
				}

				if (minutes > 0) {
					GregorianCalendar nextExpirationTime = new GregorianCalendar();
					nextExpirationTime.add(GregorianCalendar.MINUTE, minutes);
					EXPIRATIONTIME = nextExpirationTime;
				} else {
					EXPIRATIONTIME = null;
				}
				
				joinConfigValues(newValues, LICENSE_VALUES);
				
				if (CONFIGURATIONVALUES == null) {
					logger.info("Initialized ConfigService with " + newValues.size() + " values");
				}
				
				CONFIGURATIONVALUES = newValues;
			}
		} catch (LicenseError e) {
			IS_ORACLE_DB = null;
			LICENSE_VALUES = null;
			EMM_PROPERTIES_VALUES = null;
			CONFIGURATIONVALUES = null;
			EXPIRATIONTIME = null;
			
			logger.error(e.getMessage());
			throw e;
		} catch (Exception e) {
			logger.error("Cannot refresh config data from database", e);
		}
	}

	private void joinConfigValues(Map<String, Map<Integer, String>> basicValues, Map<String, Map<Integer, String>> additionalValues) throws Exception {
		for (Entry<String, Map<Integer, String>> configEntry : additionalValues.entrySet()) {
			Map<Integer, String> configValuesMap = basicValues.get(configEntry.getKey());
			if (configValuesMap == null) {
				configValuesMap = new HashMap<>();
				basicValues.put(configEntry.getKey(), configValuesMap);
			}
			for (Entry<Integer, String> companyEntry : configEntry.getValue().entrySet()) {
				configValuesMap.put(companyEntry.getKey(), AgnUtils.replaceVersionPlaceholders(AgnUtils.replaceHomeVariables(companyEntry.getValue()), applicationVersion));
			}
		}
	}

	private Map<String, Map<Integer, String>> readEmmPropertiesFile() throws Exception {
		try (InputStream resourceAsStream = getClass().getClassLoader().getResourceAsStream("emm.properties")) {
			Map<String, Map<Integer, String>> emmPropertiesMap = new HashMap<>();
			Properties properties = new Properties();
			properties.load(resourceAsStream);
			for (String key : properties.stringPropertyNames()) {
				Map<Integer, String> configValueMap = emmPropertiesMap.get(key);
				if (configValueMap == null) {
					configValueMap = new HashMap<>();
					emmPropertiesMap.put(key, configValueMap);
				}
				configValueMap.put(0, AgnUtils.replaceHomeVariables(properties.getProperty(key)));
			}
			return emmPropertiesMap;
		} catch (Exception e) {
			logger.error("Cannot read emm.properties: " + e.getMessage(), e);
			return new HashMap<>();
		}
	}
	
	private Map<String, Map<Integer, String>> readLicenseData() throws Exception {
		ClassLoader classLoader = ConfigService.class.getClassLoader();
		URL licenseURL = classLoader.getResource("emm.license.xml");
		if (licenseURL != null) {
			Date licenseDate = null;
			if (licenseURL.toString().startsWith("file:")) {
				String licenseFilePath = URLDecoder.decode(licenseURL.toString().substring(5), "UTF-8");
				BasicFileAttributes fileAttributes = Files.readAttributes(Paths.get(licenseFilePath), BasicFileAttributes.class);
				FileTime licenseFileTime = fileAttributes.lastModifiedTime();
				if (licenseFileTime == null) {
					licenseFileTime = fileAttributes.creationTime();
				}
				if (licenseFileTime != null) {
					licenseDate = new Date(licenseFileTime.toMillis());
				}
			}
			byte[] licenseDataArray;
			try (InputStream licenseStream = classLoader.getResourceAsStream("emm.license.xml")) {
				licenseDataArray = IOUtils.toByteArray(licenseStream);
			}

			byte[] licenseSignatureDataArray;
			try (InputStream signatureStream = classLoader.getResourceAsStream("emm.license.xml.sig")) {
				licenseSignatureDataArray = IOUtils.toByteArray(signatureStream);
			} catch(Exception e) {
				licenseSignatureDataArray = null;
			}
			
			if (licenseSignatureDataArray != null) {
				PublicKey publicKey = CryptographicUtilities.getPublicKeyFromString(PUBLIC_LICENSE_KEYSTRING);
				boolean success = CryptographicUtilities.verifyData(licenseDataArray, publicKey, licenseSignatureDataArray);
				if (success) {
					licenseDao.storeLicense(licenseDataArray, licenseSignatureDataArray, licenseDate);
				}
			} else {
				// OpenEMM has no license signature
				licenseDao.storeLicense(licenseDataArray, null, licenseDate);
			}
		}
		
		if (licenseDao.hasLicenseData()) {
			byte[] licenseDataArray = licenseDao.getLicenseData();

			Map<String, Map<Integer, String>> licenseData = new HashMap<>();
			Document licenseDocument = XmlUtilities.parseXMLDataAndXSDVerifyByDOM(licenseDataArray, "UTF-8", null);
			List<Node> licenseDataNodes = XmlUtilities.getSubNodes((licenseDocument.getElementsByTagName("emm.license").item(0)));
			for (Node licenseValueNode : licenseDataNodes) {
				if (licenseValueNode.getNodeType() == Node.ELEMENT_NODE) {
					if ("companies".equals(licenseValueNode.getLocalName())) {
						List<Node> companyNodes = XmlUtilities.getSubNodes(licenseValueNode);
						for (Node companyNode : companyNodes) {
							if (companyNode.getNodeType() == Node.ELEMENT_NODE) {
								int companyID = Integer.parseInt(XmlUtilities.getAttributeValue(companyNode, "id"));
								for (Node companyValueNode : XmlUtilities.getSubNodes(companyNode)) {
									if (companyValueNode.getNodeType() == Node.ELEMENT_NODE) {
										Map<Integer, String> configValueMap = licenseData.get(companyValueNode.getNodeName());
										if (configValueMap == null) {
											configValueMap = new HashMap<>();
											licenseData.put(companyValueNode.getNodeName(), configValueMap);
										}
										String value = XmlUtilities.getNodeValue(companyValueNode);
										if ("unlimited".equalsIgnoreCase(value)) {
											value = "-1";
										}
										configValueMap.put(companyID, value);
									}
								}
							}
						}
					} else {
						Map<Integer, String> configValueMap = licenseData.get(licenseValueNode.getNodeName());
						if (configValueMap == null) {
							configValueMap = new HashMap<>();
							licenseData.put(licenseValueNode.getNodeName(), configValueMap);
						}
						String value = XmlUtilities.getNodeValue(licenseValueNode);
						if ("unlimited".equalsIgnoreCase(value)) {
							value = "-1";
						}
						configValueMap.put(0, value);
					}
				}
			}
	
			// Handle different names
			Map<Integer, String> licenseIdValueMap = licenseData.get(ConfigValue.System_Licence.toString());
			if (licenseIdValueMap == null) {
				licenseIdValueMap = new HashMap<>();
				licenseData.put(ConfigValue.System_Licence.toString(), licenseIdValueMap);
			}
			licenseIdValueMap.put(0, licenseData.get("licenseID").get(0));
			licenseData.remove("licenseID");
	
			int licenseID = Integer.parseInt(licenseData.get(ConfigValue.System_Licence.toString()).get(0));
			
			if (licenseID == 0) {
				// OpenEMM: No signature check, but remove all data for company_id > 1
				if (companyDao != null) {
					companyDao.deactivateExtendedCompanies();
				}
			} else {
				byte[] licenseSignatureDataArray = licenseDao.getLicenseSignatureData();
				if (licenseSignatureDataArray == null) {
					throw new Exception("LicenseSignature is missing");
				} else {
					PublicKey publicKey = CryptographicUtilities.getPublicKeyFromString(PUBLIC_LICENSE_KEYSTRING);
					boolean success = CryptographicUtilities.verifyData(licenseDataArray, publicKey, licenseSignatureDataArray);
					if (!success) {
						throw new Exception("LicenseSignature is invalid");
					}
				}
			}
			
			return licenseData;
		} else {
			throw new Exception("Missing license data");
		}
	}
	
	/**
	 * Check validity of license data to current db data
	 * 
	 * @return
	 * @throws LicenseError
	 */
	public Message[] checkLicenseData() throws LicenseError {
		if (applicationType == Server.STATISTICS) {
			// Statistics server may start without license check
			return null;
		} else {
			List<Message> licenseWarnings = new ArrayList<>();
			
			if (LICENSE_VALUES == null) {
				throw new LicenseError("Missing License data");
			}
			
			if (configTableDao != null) {
				if (NumberUtils.toInt(LICENSE_VALUES.get(ConfigValue.System_Licence.toString()).get(0)) > 0) {
					// Check or set license ID in DB
					try {
						String storedLicenseID = configTableDao.getAllEntriesForThisHost().get(ConfigValue.System_Licence.toString()).get(0);
						if (StringUtils.isBlank(storedLicenseID)) {
							configTableDao.storeEntry("system", "licence", null,  LICENSE_VALUES.get(ConfigValue.System_Licence.toString()).get(0), "store licence value by EMM");
							logger.info("Writing new LicenseID: " + LICENSE_VALUES.get(ConfigValue.System_Licence.toString()).get(0));
						} else if (!storedLicenseID.equals(LICENSE_VALUES.get(ConfigValue.System_Licence.toString()).get(0))) {
							throw new LicenseError("Invalid LicenseID", LICENSE_VALUES.get(ConfigValue.System_Licence.toString()).get(0), storedLicenseID);
						}
					} catch (Exception e) {
						throw new LicenseError("Error while checking license id: " + e.getMessage(), e);
					}
				
					// Check license ID in licence.cfg file, if exists
					String licenceCfgLicenseId = Systemconfig.create ().get ("licence");
					
					if ((licenceCfgLicenseId != null) && (!licenceCfgLicenseId.equals(LICENSE_VALUES.get(ConfigValue.System_Licence.toString()).get(0)))) {
						throw new LicenseError("Invalid LicenseID in licence.cfg", LICENSE_VALUES.get(ConfigValue.System_Licence.toString()).get(0), licenceCfgLicenseId);
					}
				}
			}
			
			// Check validity time limit
			Date validUntil;
			if (StringUtils.isNotBlank(LICENSE_VALUES.get(ConfigValue.System_License_ExpirationDate.toString()).get(0))) {
				try {
					validUntil = new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS).parse(LICENSE_VALUES.get(ConfigValue.System_License_ExpirationDate.toString()).get(0) + " 23:59:59");
				} catch (ParseException e) {
					throw new LicenseError("Invalid validity data: " + e.getMessage(), e);
				}
				if (new Date().after(validUntil)) {
					throw new LicenseError("error.license.outdated", LICENSE_VALUES.get(ConfigValue.System_License_ExpirationDate.toString()).get(0), EMM_PROPERTIES_VALUES.get(ConfigValue.Mailaddress_Support.toString()).get(0));
				}
			}
	
			if (companyDao != null) {
				// Check maximum number of companies
				int maximumNumberOfCompanies = NumberUtils.toInt(LICENSE_VALUES.get(ConfigValue.System_License_MaximumNumberOfCompanies.toString()).get(0));
				if (maximumNumberOfCompanies >= 0) {
					int numberOfCompanies = companyDao.getNumberOfCompanies();
					if (numberOfCompanies > maximumNumberOfCompanies) {
						if (ConfigValue.System_License_MaximumNumberOfCompanies.getGracefulExtension() != null
								&& numberOfCompanies <= maximumNumberOfCompanies + ConfigValue.System_License_MaximumNumberOfCompanies.getGracefulExtension()) {
							logger.error("Invalid Number of tenants. Current value is " + numberOfCompanies + ". Limit is " + maximumNumberOfCompanies + ". Gracefully " + ConfigValue.System_License_MaximumNumberOfCompanies.getGracefulExtension() + " more accounts have been permitted");
							licenseWarnings.add(Message.of("error.numberOfCompaniesExceeded.graceful", maximumNumberOfCompanies, numberOfCompanies, ConfigValue.System_License_MaximumNumberOfCompanies.getGracefulExtension()));
		    			} else {
		    				throw new LicenseError("Invalid Number of accounts", maximumNumberOfCompanies, numberOfCompanies);
		    			}
					}
				}
			}
	
			if (supervisorDao != null) {
				// Check maximum number of supervisors
				int maximumNumberOfSupervisors = NumberUtils.toInt(LICENSE_VALUES.get(ConfigValue.System_License_MaximumNumberOfSupervisors.toString()).get(0));
				if (maximumNumberOfSupervisors >= 0) {
					int numberOfSupervisors = supervisorDao.getNumberOfSupervisors();
					if (numberOfSupervisors > maximumNumberOfSupervisors) {
						throw new LicenseError("Invalid Number of supervisors", maximumNumberOfSupervisors, numberOfSupervisors);
					}
				}
			}
			
			licenseOK = true;
	
			if (companyDao != null) {
				for (CompanyEntry activeCompany : companyDao.getActiveCompaniesLight(false)) {
					checkCompany(activeCompany.getCompanyId());
				}
			}
	
			// Company 0 must be checked after the other existing companies because it may change those, too
			checkCompany(0);
			
			if (licenseWarnings.size() > 0) {
				return licenseWarnings.toArray(new Message[0]);
			} else {
				return null;
			}
		}
	}
	
	private void checkCompany(int companyID) {
		if (companyID > 0) {
			if (adminDao != null) {
				// Check maximum number of admins
				int maximumNumberOfAdmins = getLicenseValue(ConfigValue.System_License_MaximumNumberOfAdmins, companyID);
				if (maximumNumberOfAdmins >= 0) {
					int numberOfAdmins = adminDao.getNumberOfGuiAdmins(companyID);
					if (numberOfAdmins > maximumNumberOfAdmins) {
						licenseOK = false;
						throw new LicenseError("Invalid Number of admins of company " + companyID + "", maximumNumberOfAdmins, numberOfAdmins);
					}
				}
			}
	
			if (webserviceUserDao != null) {
				// Check maximum number of Webservice users
				int maximumNumberOfWebserviceUsers = getLicenseValue(ConfigValue.System_License_MaximumNumberOfWebserviceUsers, companyID);
				if (maximumNumberOfWebserviceUsers >= 0) {
					int numberOfWebserviceUsers = webserviceUserDao.getNumberOfWebserviceUsers(companyID);
					if (numberOfWebserviceUsers > maximumNumberOfWebserviceUsers) {
						licenseOK = false;
						throw new LicenseError("Invalid Number of Webservice Users of company " + companyID + "", maximumNumberOfWebserviceUsers, numberOfWebserviceUsers);
					}
				}
			}
	
			if (adminDao != null) {
				// Check maximum number of Restful users
				int maximumNumberOfRestfulUsers = getLicenseValue(ConfigValue.System_License_MaximumNumberOfRestfulUsers, companyID);
				if (maximumNumberOfRestfulUsers >= 0) {
					int numberOfRestfulUsers = adminDao.getNumberOfRestfulUsers(companyID);
					if (numberOfRestfulUsers > maximumNumberOfRestfulUsers) {
						licenseOK = false;
						throw new LicenseError("Invalid Number of Restful Users of company " + companyID + "", maximumNumberOfRestfulUsers, numberOfRestfulUsers);
					}
				}
			}
		}

		if (companyDao != null) {
			if (companyID > 0) {
				// Check maximum number of customers
				int maximumNumberOfCustomers = getLicenseValue(ConfigValue.System_License_MaximumNumberOfCustomers, companyID);
				if (maximumNumberOfCustomers >= 0) {
					int numberOfCustomers = companyDao.getNumberOfCustomers(companyID);
					if (numberOfCustomers > maximumNumberOfCustomers) {
						if (numberOfCustomers > maximumNumberOfCustomers + ConfigValue.System_License_MaximumNumberOfCustomers.getGracefulExtension()) {
							throw new LicenseError("Invalid Number of customers of company " + companyID, maximumNumberOfCustomers, numberOfCustomers);
						} else {
							String licenseErrorText = "Invalid Number of customers of company " + companyID + "."
									+ " Current number of customers: " + numberOfCustomers + "."
									+ " Maximum number of customers: " + maximumNumberOfCustomers + "."
									+ " Allowed by graceful limit extension: " + ConfigValue.System_License_MaximumNumberOfCustomers.getGracefulExtension();
							logger.error(licenseErrorText);
							if (javaMailService != null) {
								javaMailService.sendLicenseErrorMail(licenseErrorText);
							}
							licenseOK = false;
						}
					}
				}
			
				// Check maximum number of profile fields
				int maximumNumberOfProfileFields = getLicenseValue(ConfigValue.System_License_MaximumNumberOfProfileFields, companyID);
				if (maximumNumberOfProfileFields >= 0) {
					int numberOfCompanySpecificProfileFields;
					try {
						numberOfCompanySpecificProfileFields = profileFieldService.getCurrentSpecificFieldCount(companyID);
					} catch (Exception e) {
						throw new LicenseError("Cannot detect number of profileFields of company " + companyID + ": " + e.getMessage(), e);
					}
					if (numberOfCompanySpecificProfileFields > maximumNumberOfProfileFields) {
					 	if (numberOfCompanySpecificProfileFields > maximumNumberOfProfileFields + ConfigValue.System_License_MaximumNumberOfProfileFields.getGracefulExtension()) {
							throw new LicenseError("Invalid Number of profileFields of company " + companyID, maximumNumberOfProfileFields, numberOfCompanySpecificProfileFields);
						} else {
							String licenseErrorText = "Invalid Number of profileFields of company " + companyID + "."
									+ " Current number of profileFields: " + numberOfCompanySpecificProfileFields + "."
									+ " Maximum number of profileFields: " + maximumNumberOfProfileFields + "."
									+ " Allowed by graceful limit extension: " + ConfigValue.System_License_MaximumNumberOfProfileFields.getGracefulExtension();
							logger.error(licenseErrorText);
							if (javaMailService != null) {
								javaMailService.sendLicenseErrorMail(licenseErrorText);
							}
							licenseOK = false;
						}
					}
				}
			
				// Check maximum number of reference tables
				int maximumNumberOfReferenceTables = getLicenseValue(ConfigValue.System_License_MaximumNumberOfReferenceTables, companyID);
				if (maximumNumberOfReferenceTables >= 0) {
					int numberOfReferenceTables;
					try {
						numberOfReferenceTables = licenseDao.getNumberOfCompanyReferenceTables(companyID);
					} catch (Exception e) {
						throw new LicenseError("Cannot detect number of reference tables of company " + companyID + ": " + e.getMessage(), e);
					}
					if (numberOfReferenceTables > maximumNumberOfReferenceTables) {
					 	if (numberOfReferenceTables > maximumNumberOfReferenceTables + ConfigValue.System_License_MaximumNumberOfReferenceTables.getGracefulExtension()) {
							throw new LicenseError("Invalid Number of reference tables of company " + companyID, maximumNumberOfReferenceTables, numberOfReferenceTables);
						} else {
							String licenseErrorText = "Invalid Number of reference tables of company " + companyID + "."
									+ " Current number of reference tables: " + numberOfReferenceTables + "."
									+ " Maximum number of reference tables: " + maximumNumberOfReferenceTables + "."
									+ " Allowed by graceful limit extension: " + ConfigValue.System_License_MaximumNumberOfReferenceTables.getGracefulExtension();
							logger.error(licenseErrorText);
							if (javaMailService != null) {
								javaMailService.sendLicenseErrorMail(licenseErrorText);
							}
							licenseOK = false;
						}
					}
				}
			
				// Check maximum number of AccessLimitingMailingLists (ALML) per company
				int numberOfAccessLimitingMailinglistsPerCompany = licenseDao.getNumberOfAccessLimitingMailinglists(companyID);
				int licenseMaximumOfAccessLimitingMailinglists = getLicenseValue(ConfigValue.System_License_MaximumNumberOfAccessLimitingMailinglistsPerCompany, companyID);
				if (licenseMaximumOfAccessLimitingMailinglists >= 0 && licenseMaximumOfAccessLimitingMailinglists < numberOfAccessLimitingMailinglistsPerCompany) {
					if (numberOfAccessLimitingMailinglistsPerCompany > licenseMaximumOfAccessLimitingMailinglists + ConfigValue.System_License_MaximumNumberOfAccessLimitingMailinglistsPerCompany.getGracefulExtension()) {
						throw new LicenseError("Invalid Number of AccessLimitingMailingLists of company " + companyID + "", licenseMaximumOfAccessLimitingMailinglists, numberOfAccessLimitingMailinglistsPerCompany);
					} else {
						String licenseErrorText = "Invalid Number of AccessLimitingMailinglists of company " + companyID + "."
								+ " Current number of AccessLimitingMailinglists: " + numberOfAccessLimitingMailinglistsPerCompany + "."
								+ " Maximum number of AccessLimitingMailinglists: " + licenseMaximumOfAccessLimitingMailinglists + "."
								+ " Allowed by graceful limit extension: " + ConfigValue.System_License_MaximumNumberOfAccessLimitingMailinglistsPerCompany.getGracefulExtension();
						logger.error(licenseErrorText);
						if (javaMailService != null) {
							javaMailService.sendLicenseErrorMail(licenseErrorText);
						}
						licenseOK = false;
					}
				}
			
				// Check maximum number of AccessLimitingTargetgroups (ALTG) per company
				int numberOfAccessLimitingTargetgroupsPerCompany = licenseDao.getHighestAccessLimitingTargetgroupsPerCompany();
				int licenseMaximumOfAccessLimitingTargetgroups = getLicenseValue(ConfigValue.System_License_MaximumNumberOfAccessLimitingTargetgroupsPerCompany, companyID);
				if (licenseMaximumOfAccessLimitingTargetgroups >= 0 && licenseMaximumOfAccessLimitingTargetgroups < numberOfAccessLimitingTargetgroupsPerCompany) {
					if (numberOfAccessLimitingTargetgroupsPerCompany > licenseMaximumOfAccessLimitingTargetgroups + ConfigValue.System_License_MaximumNumberOfAccessLimitingTargetgroupsPerCompany.getGracefulExtension()) {
						throw new LicenseError("Invalid Number of AccessLimitingTargetgroups of company " + companyID + "", licenseMaximumOfAccessLimitingTargetgroups, numberOfAccessLimitingTargetgroupsPerCompany);
					} else {
						String licenseErrorText = "Invalid Number of AccessLimitingTargetgroups of company " + companyID + "."
								+ " Current number of AccessLimitingTargetgroups: " + numberOfAccessLimitingTargetgroupsPerCompany + "."
								+ " Maximum number of AccessLimitingTargetgroups: " + licenseMaximumOfAccessLimitingTargetgroups + "."
								+ " Allowed by graceful limit extension: " + ConfigValue.System_License_MaximumNumberOfAccessLimitingTargetgroupsPerCompany.getGracefulExtension();
						logger.error(licenseErrorText);
						if (javaMailService != null) {
							javaMailService.sendLicenseErrorMail(licenseErrorText);
						}
						licenseOK = false;
					}
				}
			}
			
			// Also load extended Permissions if available (OpenEMM has no PermissionExtended class)
			try {
				Class.forName("com.agnitas.emm.core.PermissionExtended");
			} catch (ClassNotFoundException e) {
				// do nothing
			}
			
			if (getApplicationType() == Server.EMM) {
				// Check allowed premium features on Frontend Servers only 
				boolean companyHasOwnPremiumFeatures;
				String allowedPremiumFeaturesData = LICENSE_VALUES.get(ConfigValue.System_License_AllowedPremiumFeatures.toString()).get(companyID);
				Set<String> allowedPremiumFeatures = new HashSet<>();
				Set<String> unAllowedPremiumFeatures = new HashSet<>();
				if (allowedPremiumFeaturesData != null) {
					companyHasOwnPremiumFeatures = true;
				} else {
					companyHasOwnPremiumFeatures = false;
					allowedPremiumFeaturesData = LICENSE_VALUES.get(ConfigValue.System_License_AllowedPremiumFeatures.toString()).get(0);
				}
				for (String allowedPremiumFeature : allowedPremiumFeaturesData.split(" |;|,|\\t|\\n")) {
					if (StringUtils.isNotBlank(allowedPremiumFeature) && !allowedPremiumFeature.trim().startsWith("<!--")) {
						allowedPremiumFeatures.add(allowedPremiumFeature.trim());
					}
				}
			
				if (!allowedPremiumFeatures.contains("all") && !allowedPremiumFeatures.contains("ALL")) {
					// Check and set or reset premium features
					for (Permission permission : permissionService.getAllPermissions()) {
						if (permission.getPermissionType() == PermissionType.Premium && !allowedPremiumFeatures.contains(permission.toString())) {
							unAllowedPremiumFeatures.add(permission.toString());
						}
					}
					
					if (!companyHasOwnPremiumFeatures) {
						companyDao.cleanupPremiumFeaturePermissions(companyID);
					} else {
						companyDao.setupPremiumFeaturePermissions(allowedPremiumFeatures, unAllowedPremiumFeatures, "Initial license setup", companyID);
					}
				} else {
					// Init the permission system anyway and assign categories to the rights etc.
					permissionService.getAllPermissions();
				}
			}
		}
	}

	private int getLicenseValue(ConfigValue licenseValue, int companyID) {
		if (LICENSE_VALUES.containsKey(licenseValue.toString())) {
			if (LICENSE_VALUES.get(licenseValue.toString()).containsKey(companyID)) {
				return NumberUtils.toInt(LICENSE_VALUES.get(licenseValue.toString()).get(companyID));
			} else if (LICENSE_VALUES.get(licenseValue.toString()).containsKey(0)) {
				return NumberUtils.toInt(LICENSE_VALUES.get(licenseValue.toString()).get(0));
			} else if (StringUtils.isNotBlank(licenseValue.getDefaultValue())) {
				return NumberUtils.toInt(licenseValue.getDefaultValue());
			} else {
				return 0;
			}
		} else if (StringUtils.isNotBlank(licenseValue.getDefaultValue())) {
			return NumberUtils.toInt(licenseValue.getDefaultValue());
		} else {
			return 0;
		}
	}

	public void writeValue(final ConfigValue configurationValueID, final String value, String description) {
		String[] parts = configurationValueID.toString().split("\\.", 2);
		
		configTableDao.storeEntry(parts[0], parts[1], null, value, description);
		
		invalidateCache();
	}

	
	public void writeValue(final ConfigValue configurationValueID, final int companyID, final String value) {
		writeValue(configurationValueID, companyID, value, null);
	}
	 

	public void writeValue(final ConfigValue configurationValueID, final int companyID, final String value, final String description) {
		companyInfoDao.writeConfigValue(companyID, configurationValueID.toString(), value, description);
		
		invalidateCache();
	}

	public void writeOrDeleteIfDefaultValue(final ConfigValue configurationValueID, final int companyID, final String value, String description){
		String defaultValue = getValue(configurationValueID, 0);
		if (StringUtils.equals(defaultValue, value) && companyID > 0) {
			companyInfoDao.deleteValue(companyID, configurationValueID.toString());
			invalidateCache();
		} else {
			writeValue(configurationValueID, companyID, value, description);
		}
	}

	public void writeBooleanValue(final ConfigValue configurationValueID, final boolean value, String description) {
		String[] parts = configurationValueID.toString().split("\\.", 2);
		
		configTableDao.storeEntry(parts[0], parts[1], null, value ? "true" : "false", description);
		
		invalidateCache();
	}

	public void writeBooleanValue(final ConfigValue configurationValueID, final int companyID, final boolean value, String description) {
		companyInfoDao.writeConfigValue(companyID, configurationValueID.toString(), value ? "true" : "false", description);

		invalidateCache();
	}

	public void writeOrDeleteIfDefaultBooleanValue(final ConfigValue configurationValueID, final int companyID, final boolean value, String description){
		boolean defaultValue = getBooleanValue(configurationValueID, 0);
		if (defaultValue == value && companyID > 0) {
			companyInfoDao.deleteValue(companyID, configurationValueID.toString());
			invalidateCache();
		} else {
			writeBooleanValue(configurationValueID, companyID, value, description);
		}
	}
	
	public String getValue(ConfigValue configurationValueID) {
		try {
			refreshValues();
			
			String value = null;
			
			Map<Integer, String> companyValueMap = CONFIGURATIONVALUES.get(configurationValueID.toString());
			if (companyValueMap != null) {
				value = companyValueMap.get(0);
			}
			
			if (value == null) {
				value = configurationValueID.getDefaultValue();
			}
			return value;
		} catch (LicenseError e) {
			if (ConfigValue.SupportEmergencyUrl.equals(configurationValueID))  {
				try {
					return configTableDao.getAllEntriesForThisHost().get(ConfigValue.SupportEmergencyUrl.toString()).get(0);
				} catch (Exception e1) {
					throw new RuntimeException("Cannot load SupportEmergencyUrl from database", e1);
				}
			} else {
				throw e;
			}
		}
	}
	
	public String getValue(ConfigValue configurationValueID, String defaultValue) {
		String returnValue = getValue(configurationValueID);
		if (returnValue == null || returnValue.length() == 0) {
			return defaultValue;
		} else {
			return returnValue;
		}
	}
	
	public String getEncryptedValue(ConfigValue configurationValueID, int companyID) throws Exception {
		String encryptedDataBase64 = getValue(configurationValueID, companyID);
		
		if (StringUtils.isNotEmpty(encryptedDataBase64)) {
			return dataEncryptor.decrypt(encryptedDataBase64);
		} else {
			return null;
		}
	}
	
	/**
	 * Does a chained search for specified configuration. Search stops at first match:
	 * 
	 * <ol>
	 *   <li>Search configuration value for given company ID and hostname.</li>
	 *   <li>Search configuration value for given hostname and the default company ID 0</li>
	 *   <li>Search configuration value for given company ID.</li>
	 *   <li>Search configuration value for company ID 0.</li>
	 *   <li>Search configuration value without company ID</li>
	 * </ol>
	 * 
	 * If there is no matching configuration, {@code null} is returned.
	 * 
	 * @param configurationValueID configuration value ID
	 * @param companyID company ID

	 * @return configuration value as String or {@code null}
	 */
	public String getValue(ConfigValue configurationValueID, int companyID) {
		refreshValues();
		
		String value = null;
		
		Map<Integer, String> companyValueMap = CONFIGURATIONVALUES.get(configurationValueID.toString());
		if (companyValueMap != null) {
			if (companyValueMap.containsKey(companyID)) {
				value = companyValueMap.get(companyID);
			} else {
				value = companyValueMap.get(0);
			}
		}
		
		if (value == null) {
			value = configurationValueID.getDefaultValue();
		}
		
		return value;
	}
	
	/**
	 * Same as {@link #getValue(ConfigValue, int)} with additional default value.
	 * 
	 * If no matching configuration is found, the specified default value is returned.
	 * 
	 * @param configurationValueID configuration value ID
	 * @param companyID company ID
	 * @param defaultValue default value
	 * 
	 * @return configuration value or specified default value
	 */
	public String getValue(ConfigValue configurationValueID, int companyID, String defaultValue) {
		String value = getValue(configurationValueID, companyID);
		
		if (value == null) {
			return defaultValue;
		} else {
			return value;
		}
	}
	
	public boolean getBooleanValue(ConfigValue configurationValueID) {
		String value = getValue(configurationValueID);
		
		return AgnUtils.interpretAsBoolean(value);
	}
	
	public boolean getBooleanValue(ConfigValue configurationValueID, int companyID) {
		String value = getValue(configurationValueID, companyID);
		
		return AgnUtils.interpretAsBoolean(value);
	}
	
	public <E extends Enum<E>> Optional<E> getEnum(final ConfigValue configValue, final int companyID, final Class<E> enumClass) {
		return getEnum(configValue, companyID, enumClass, configValue.getDefaultValue());
	}
	
	public <E extends Enum<E>> Optional<E> getEnum(final ConfigValue configValue, final int companyID, final Class<E> enumClass, final String defaultValue) {
		final String value = getValue(configValue, companyID, defaultValue);
		
		if(value == null) {
			return Optional.empty(); // If value read is null, then return the empty optional.
		}

		for(final E constant : enumClass.getEnumConstants()) {
			if(constant.name().equals(value)) {
				return Optional.of(constant);
			}
		}
		
		// We encountered an invalid value for the enum type.
		
		// List allowed values, ...
		final String allowed = Arrays.stream(enumClass.getEnumConstants())
				.map(Enum::name)
				.collect(Collectors.joining());
		
		// ... log error message, ...
		logger.error(String.format(
				"Config key '%s' contains invalid value '%s'. Allowed values are: %s",
				configValue.getName(),
				value,
				allowed
				));
		
		// ... and return empty optional.
		return Optional.empty();
	}
	
	/**
	 * Returns an integer value using logic with fallback and default value.
	 * See {@link #getValue(ConfigValue, int, String)} for details on fallback.
	 * 
	 * @param configurationValueID ID of configuration value
	 * @param companyID company ID
	 * @param defaultValue default value
	 * 
	 * @return integer configuration value of default value
	 */
	public int getIntegerValue(ConfigValue configurationValueID, int companyID, int defaultValue) {
		String value = getValue(configurationValueID, companyID, Integer.toString(defaultValue));
		
		return Integer.parseInt(value);
	}
	
	public int getIntegerValue(ConfigValue configurationValueID, int companyID) {
		String value = getValue(configurationValueID, companyID);
		
		return Integer.parseInt(value);
	}
	
	public long getLongValue(ConfigValue configurationValueID, int companyID, int defaultValue) {
		String value = getValue(configurationValueID, companyID, Integer.toString(defaultValue));
		
		return Long.parseLong(value);
	}

	public long getLongValue(ConfigValue configurationValueID) {
		String value = getValue(configurationValueID);
		
		return Long.parseLong(value);
	}
	
	public long getLongValue(ConfigValue configurationValueID, int companyID) {
		String value = getValue(configurationValueID, companyID);
		
		return Long.parseLong(value);
	}

	public float getFloatValue(ConfigValue configurationValueID, int companyID){
		String value = getValue(configurationValueID, companyID);

		return Float.parseFloat(value);
	}
	
	public int getIntegerValue(ConfigValue configurationValueID) {
		String value = getValue(configurationValueID);
		if (StringUtils.isNotEmpty(value)) {
			return Integer.parseInt(value);
		} else {
			return 0;
		}
	}

	public float getFloatValue(ConfigValue configurationValueID) {
		String value = getValue(configurationValueID);
		if (StringUtils.isNotEmpty(value)) {
			return Float.parseFloat(value);
		} else {
			return 0;
		}
	}
	
	public List<String> getListValue(ConfigValue configurationValueID) {
		String value = getValue(configurationValueID);
		if (StringUtils.isNotEmpty(value)) {
			return Arrays.asList(value.split(";"));
		} else {
			return Collections.emptyList();
		}
	}
	
	public String getDescription(ConfigValue configurationValueID, int companyID) {
		String description = companyInfoDao.getDescription(configurationValueID.toString(), companyID);
		if (description == null) {
			description = companyInfoDao.getDescription(configurationValueID.toString(), 0);
		}
		return description;
	}
	
	/**
	 * Checks, if runtime checks for Velocity are enabled. If no settings for
	 * given company ID are found, checks for company ID 0. If no settings for
	 * company ID 0 are found, runtime checks are enabled globally.
	 * 
	 * @param companyId company ID to check
	 * 
	 * @return true, if runtime checks are enabled
	 */
	public boolean isVelocityRuntimeCheckEnabled( int companyId) {
		String value = getValue( ConfigValue.VelocityRuntimeCheck, companyId);
		
		if (value != null) {
			return AgnUtils.interpretAsBoolean( value);
		} else {
			if (companyId != 0) {
				value = getValue(ConfigValue.VelocityRuntimeCheck);
				
				if (value != null) {
					return AgnUtils.interpretAsBoolean( value);
				} else {
					return true;
				}
			} else {
				return true;
			}
		}
	}

	/**
	 * Checks, if invalid Velocity scripts are to be aborted. If no settings for
	 * given company ID are found, checks for company ID 0. If no settings for
	 * company ID 0 are found, abortion of scripts is globally enabled.
	 * 
	 * @param companyId company ID to check
	 * 
	 * @return true if scripts are to be aborted
	 */
	public boolean isVelocityScriptAbortEnabled(int companyId) {
		String value = getValue(ConfigValue.VelocityScriptAbort, companyId);
		
		if (value != null) {
			return AgnUtils.interpretAsBoolean(value);
		} else {
			if (companyId != 0) {
				value = getValue( ConfigValue.VelocityScriptAbort);
				
				if (value != null) {
					return AgnUtils.interpretAsBoolean(value);
				} else {
					return true;
				}
			} else {
				return true;
			}
		}
	}
	
	/**
	 * Checks, if company is allowed to use webservice &quot;&SendServiceMailing&quot;.
	 * 
	 * @param companyID company ID
	 * 
	 * @return <code>true</code> if company is allowed to use webservice
	 */
	public boolean isWebserviceSendServiceMailingEnabled(int companyID) {
		return getBooleanValue(Webservices.WebserviceEnableSendServiceMailing, companyID);
	}

	/**
	 * Returns the size limit for bulk webservices.
	 * 
	 * @param companyID company ID
	 * 
	 * @return size limit
	 */
	public int getWebserviceBulkSizeLimit(final int companyID) {
		return getIntegerValue(Webservices.WebserviceBulkSizeLimit, companyID, 1000);
	}
	
	/**
	 * Returns <code>true</code>, if history on profile fields is enabled.
	 * 
	 * @param companyID company ID
	 * 
	 * @return <code>true</code> if history on profile fields is enabled
	 */
	public boolean isRecipientProfileHistoryEnabled(final int companyID) {
		return getBooleanValue(ConfigValue.RecipientProfileFieldHistory, companyID);
	}

	/**
	 * Returns the maximum allowed number of user-selected profile fields in history.
	 * 
	 * @param companyID company ID
	 * 
	 * @return maximum allowed number of user-selected profile fields in history
	 */
	public int getMaximumNumberOfUserDefinedHistoryProfileFields(final int companyID) {
		return getIntegerValue(ConfigValue.MaximumNumberOfUserSelectedProfileFieldsInHistory, companyID, 5);
	}

	/**
	 * Returns <code>true</code> if push notifications are enabled for given company ID.
	 * 
	 * @param companyID company ID
	 * 
	 * @return <code>true</code> if push notifications are enabled
	 */
	public final boolean isPushNotificationEnabled(final int companyID) {
		return getBooleanValue(ConfigValue.PushNotificationsEnabled, companyID);
	}

	/**
	 * Returns the provider credentials for push server implementation.
	 * The content is JSON encoded.
	 * 
	 * @param companyID company ID
	 * 
	 * @return JSON-encoded provider credentials
	 */
	public final String pushNotificationProviderCredentials(final int companyID) {
		// "{}" represents empty map in JSON
		return getValue(ConfigValue.PushNotificationProviderCredentials, companyID, "{}");
	}

	public String getPushNotificationFileSinkBaseDirectory(int companyID) {
		return getValue(ConfigValue.PushNotificationFileSinkBaseDirectory, companyID, "/tmp");
	}
	
	public final String getPushNotificationResultBaseDirectory() {
		return getValue(ConfigValue.PushNotificationResultBaseDirectory, "/tmp");
	}
	
	public final int getLicenseID() {
		return getIntegerValue(ConfigValue.System_Licence);
	}

	public final String getPushNotificationSftpHost() {
		return getValue(ConfigValue.PushNotificationSftpHost);
	}

	public String getPushNotificationSftpUser() {
		return getValue(ConfigValue.PushNotificationSftpUser);
	}

	public String getPushNotificationSftpBasePath() {
		return getValue(ConfigValue.PushNotificationSftpBasePath);
	}

	public String getPushNotificationSftpKeyFileName() {
		return getValue(ConfigValue.PushNotificationSftpSshKeyFile);
	}

	public String getPushNotificationEncryptedSftpPassphrase() {
		return getValue(ConfigValue.PushNotificationSftpEncryptedSshKeyPassphrase);
	}
	
	public String getPushNotificationClickTrackingUrl(final int companyID) {
		return getValue(ConfigValue.PushNotificationClickTrackingUrl, companyID);
	}
	
	public String getPushNotificationOpenTrackingUrl(final int companyID) {
		return getValue(ConfigValue.PushNotificationOpenTrackingUrl, companyID);
	}

	public boolean isConfigValueExists(ConfigValue configurationValueID, int companyId) {
		refreshValues();
		Map<Integer, String> companyValuesMap = CONFIGURATIONVALUES.get(configurationValueID.toString());
		return companyValuesMap != null && companyValuesMap.containsKey(companyId);
	}

	public static Date getBuildTime() {
		try {
	        URL resource = ConfigService.class.getResource(ConfigService.class.getSimpleName() + ".class");
	        if (resource == null) {
	            throw new IllegalStateException("Failed to find class file for class: " + ConfigService.class.getName());
	        } else if (resource.getProtocol().equals("file")) {
	        	return new Date(new File(resource.toURI()).lastModified());
	        } else if (resource.getProtocol().equals("jar")) {
	            String path = resource.getPath();
	            return new Date(new File(path.substring(5, path.indexOf("!"))).lastModified());
	        } else {
	            throw new IllegalArgumentException("Unhandled url protocol: " + resource.getProtocol() + " for class: " + ConfigService.class.getName() + " resource: " + resource.toString());
	        }
	    } catch (Exception e) {
	    	logger.error("Error in getDeploymentTime: " + e.getMessage(), e);
	        return null;
	    }
	}

	public Date getStartupTime() {
		return STARTUP_TIME == null ? null : (Date) STARTUP_TIME.clone();
	}
	
	public Date getConfigurationExpirationTime() {
		return EXPIRATIONTIME == null ? null : EXPIRATIONTIME.getTime();
	}

	public final String getHostAuthenticationHostIdCookieName() {
		return getValue(ConfigValue.HostAuthenticationHostIdCookieName);
	}
	
	public final int getMaxPendingHostAuthenticationsAgeMinutes() {
		return getIntegerValue(ConfigValue.PendingHostAuthenticationMaxAgeMinutes);
	}

	public final String getFullviewFormName(final int companyID) {
		return getValue(ConfigValue.FullviewFormName, companyID);
	}
	
	public final boolean isSessionHijackingPreventionEnabled() {
		return getBooleanValue(ConfigValue.SessionHijackingPrevention);
	}
	
	public final Map<String, String> getHostSystemProperties() {
		Map<String, String> hostProperties = new HashMap<>();
		Map<String, String> systemProperties = Systemconfig.create ().get();
		
		if(MapUtils.isNotEmpty(systemProperties)) {
			Map<String, String> hostNames = systemProperties.entrySet().stream()
					.filter(pair -> StringUtils.startsWith(pair.getKey(), "hostname-"))
					.map(ConfigService::createHostProperty)
					.collect(Collectors.toMap(Tuple::getFirst, Tuple::getSecond));
			hostProperties.putAll(hostNames);
		} else {
			hostProperties.put("host.nosystemcfg", "true");
		}
		
		return hostProperties;
	}
	
	private static Tuple<String, String> createHostProperty(Entry<String, String> propertyEntry) {
		String hostFunction = StringUtils.removeStart(propertyEntry.getKey(), "hostname-");
		String hostName = propertyEntry.getValue();
		String propertyName = String.format("host.%s.%s.ping", hostFunction, hostName);
		String reachable = "ERROR";
		try {
			InetAddress address = InetAddress.getByName(hostName);
			reachable = address.isReachable(1000) ? "OK" : "ERROR";
		} catch (IOException e) {
			logger.error("Cannot reach to host " + hostName, e);
		}
		return new Tuple<>(propertyName, reachable);
	}

	public void enforceExpiration() {
		EXPIRATIONTIME = new GregorianCalendar();
	}

	public Server getApplicationType() {
		return applicationType;
	}

	public List<Map<String, Object>> getReleaseData(String hostName, String applicationTypeParam) throws Exception {
		return configTableDao.getReleaseData(hostName, applicationTypeParam);
	}

	public Date getCurrentDbTime() {
		return configTableDao.getCurrentDbTime();
	}
	
    public static Date getApplicationVersionInstallationTime() {
		return applicationVersionInstallationTime;
	}

	public static String getApplicationVersionInstallationTimeString(HttpServletRequest request) {
		return AgnUtils.getAdmin(request).getDateTimeFormat().format(applicationVersionInstallationTime);
	}

	public static void setApplicationVersionInstallationTime(Date applicationVersionInstallationTime) {
		ConfigService.applicationVersionInstallationTime = applicationVersionInstallationTime;
	}
	
	public boolean isBasicLB(int companyId) {
        return getBooleanValue(ConfigValue.LayoutBuilderBasic, companyId);
    }

    public boolean isUserBasedFavoriteTargets(int companyId) {
        return getBooleanValue(ConfigValue.UserBasedFavoriteTargets, companyId);
    }

    public boolean isExtendedAltgEnabled(int companyId) {
		return getBooleanValue(ConfigValue.TargetAccessLimitExtended, companyId);
	}

	public boolean isLicenseStatusOK() {
		return licenseOK;
	}
}
