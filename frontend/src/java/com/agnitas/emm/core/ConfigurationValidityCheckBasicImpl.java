/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.context.WebApplicationContext;

import com.agnitas.beans.ComCompany;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.LayoutDao;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.reporting.birt.util.RSACryptUtil;

/**
 * ATTENTION: Before changing something in Database affecting all companies, please rethink.
 * Try if it is possible to make a soft rollout and activate the changes for single companyIDs first. If not, you HAVE TO talk to AGNITAS developer first.
 */
public class ConfigurationValidityCheckBasicImpl implements ConfigurationValidityCheck {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ConfigurationValidityCheckBasicImpl.class);

	protected DataSource dataSource;
	protected JdbcTemplate jdbcTemplate;

	protected ConfigService configService;

	protected ConfigTableDao configTableDao;

	protected ComCompanyDao companyDao;

	protected ComTargetService targetService;
	
	private LayoutDao layoutDao;
	
	@Required
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
		jdbcTemplate = new JdbcTemplate(dataSource);
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	@Required
	public void setConfigTableDao(ConfigTableDao configTableDao) {
		this.configTableDao = configTableDao;
	}

	@Required
	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	@Required
	public void setTargetService(ComTargetService targetService) {
		this.targetService = targetService;
	}

	@Required
	public void setLayoutDao(LayoutDao layoutDao) {
		this.layoutDao = layoutDao;
	}

	@Override
	public void checkValidity(WebApplicationContext webApplicationContext) {
    	try {
            initializeTargetComplexityIndices();

    		// Migrate SystemUrl from emm.properties into db (keep until EMM Versions 19.10 and 20.04)
    		migrateEmmPropertiesValueToDb(ConfigValue.SystemUrl, "system", "url");

    		// Migrate BirtUrl from emm.properties into db
    		migrateEmmPropertiesValueToDb(ConfigValue.BirtUrl, "birt", "url");

    		// Migrate BirtUrlIntern from emm.properties into db
    		migrateEmmPropertiesValueToDb(ConfigValue.BirtUrlIntern, "birt", "url.intern");

    		// Migrate BirtDrilldownUrl from emm.properties into db
    		migrateEmmPropertiesValueToDb(ConfigValue.BirtDrilldownUrl, "birt", "drilldownurl");

			migrateBirtKeys();
    		
    		// Migrate Wkhtml values
    		migrateEmmPropertiesValueToDb(ConfigValue.WkhtmlToImageToolPath, "system", "wkhtmltoimage");
    		migrateEmmPropertiesValueToDb(ConfigValue.WkhtmlToPdfToolPath, "system", "wkhtmltopdf");

    		// Migrate HostauthenticationCookiesHttpsOnly from emm.properties into db
    		migrateEmmPropertiesValueToDb(ConfigValue.HostauthenticationCookiesHttpsOnly, "hostauthentication", "cookies.https.only");

    		// Migrate SmtpMailRelayHostname from emm.properties into db
    		migrateEmmPropertiesValueToDb(ConfigValue.SmtpMailRelayHostname, "system", "mail.host");

    		// Migrate mailaddresses from emm.properties into db
    		migrateEmmPropertiesValueToDb(ConfigValue.Mailaddress_Sender, "mailaddress", "sender");
    		migrateEmmPropertiesValueToDb(ConfigValue.Mailaddress_ReplyTo, "mailaddress", "replyto");
    		migrateEmmPropertiesValueToDb(ConfigValue.Mailaddress_Bounce, "mailaddress", "bounce");
    		migrateEmmPropertiesValueToDb(ConfigValue.Mailaddress_Frontend, "mailaddress", "frontend");
    		migrateEmmPropertiesValueToDb(ConfigValue.Mailaddress_Support, "mailaddress", "support");
    		migrateEmmPropertiesValueToDb(ConfigValue.Mailaddress_Error, "mailaddress", "error");
    		migrateEmmPropertiesValueToDb(ConfigValue.MailAddress_UploadDatabase, "mailaddress", "upload.database");
    		migrateEmmPropertiesValueToDb(ConfigValue.MailAddress_UploadSupport, "mailaddress", "upload.support");
    		migrateEmmPropertiesValueToDb(ConfigValue.Mailaddress_ReportArchive, "mailaddress", "report_archive");
    		
    		migrateEmmWsPropertiesValueToDb("wsdlLocationUri", ConfigValue.WebservicesUrl, "webservices", "url");

    		// Migrate mailgun/mailout values from emm.properties into db
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_Loglevel, ConfigValue.MailOut_Loglevel, "mailout", "ini.loglevel");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_MailDir, ConfigValue.MailOut_MailDir, "mailout", "ini.maildir");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_DefaultEncoding, ConfigValue.MailOut_DefaultEncoding, "mailout", "ini.default_encoding");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_DefaultCharset, ConfigValue.MailOut_DefaultCharset, "mailout", "ini.default_charset");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_Blocksize, ConfigValue.MailOut_Blocksize, "mailout", "ini.blocksize");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_MetaDir, ConfigValue.MailOut_MetaDir, "mailout", "ini.metadir");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_Xmlback, ConfigValue.MailOut_Xmlback, "mailout", "ini.xmlback");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_AccountLogfile, ConfigValue.MailOut_AccountLogfile, "mailout", "ini.account_logfile");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_Xmlvalidate, ConfigValue.MailOut_Xmlvalidate, "mailout", "ini.xmlvalidate");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_Domain, ConfigValue.MailOut_Domain, "mailout", "ini.domain");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_Boundary, ConfigValue.MailOut_Boundary, "mailout", "ini.boundary");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_MailLogNumber, ConfigValue.MailOut_MailLogNumber, "mailout", "ini.mail_log_number");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_EOL, ConfigValue.MailOut_EOL, "mailout", "ini.eol");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_Mailer, ConfigValue.MailOut_Mailer, "mailout", "ini.mailer");
    		migrateMailgunEmmPropertiesValueToDb(ConfigValue.MailGun_DirectDir, ConfigValue.MailOut_DirectDir, "mailout", "ini.directdir");
    		
    		migrateOldBirtReportDefinitions();
    		
    		migrateCompanyValues();
    		
    		migrateLogosAndImages(webApplicationContext);

			/**
			 * ATTENTION: Before changing something in Database affecting all companies, please rethink.
			 * Try if it is possible to make a soft rollout and activate the changes for single companyIDs first. If not, you HAVE TO talk to AGNITAS developer first.
			 */
		} catch (Exception e) {
			logger.error("Cannot check installation validity: " + e.getMessage(), e);
		}
    }

	private void migrateOldBirtReportDefinitions() {
    	for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT * FROM birtreport_tbl WHERE laststart IS NULL AND nextstart IS NULL")) {
    		int reportId = ((Number) row.get("report_id")).intValue();
    		
    		if (row.get("email") != null && StringUtils.isNotBlank(((String) row.get("email")))) {
        		List<String> reportRecipientList = AgnUtils.splitAndTrimList((String) row.get("email"));
	    		jdbcTemplate.update("DELETE FROM birtreport_recipient_tbl WHERE birtreport_id = ?", reportId);
	    		if (reportRecipientList != null) {
	    			for (String email : AgnUtils.removeObsoleteItemsFromList(reportRecipientList)) {
	    				jdbcTemplate.update("INSERT INTO birtreport_recipient_tbl (birtreport_id, email) VALUES (?, ?)", reportId, email);
	    			}
	    		}
    		}
    		
    		Date lastDeliveryDate = (Date) row.get("delivery_date");
    		jdbcTemplate.update("UPDATE birtreport_tbl SET laststart = ? WHERE report_id = ?", lastDeliveryDate, reportId);
    		
    		Date sendDate = (Date) row.get("send_date");
    		jdbcTemplate.update("UPDATE birtreport_tbl SET nextstart = ? WHERE report_id = ?", sendDate, reportId);
    	
    		String sendDaysBitMap = (String) row.get("send_days");
    		Date sendTimeDate = (Date) row.get("send_time");
    		if (StringUtils.isNotBlank(sendDaysBitMap) && sendTimeDate != null) {
    			BirtReportType reportType = BirtReportType.getTypeByCode(((Number) row.get("report_type")).intValue());
    			GregorianCalendar sendTime = new GregorianCalendar();
    			sendTime.setTime(sendTimeDate);
    			
    			if (reportType == BirtReportType.TYPE_DAILY) {
    				String intervalPattern = String.format("%02d%02d", sendTime.get(Calendar.HOUR_OF_DAY), sendTime.get(Calendar.MINUTE));
        			jdbcTemplate.update("UPDATE birtreport_tbl SET intervalpattern = ? WHERE report_id = ?", intervalPattern, reportId);
    			} else if (reportType == BirtReportType.TYPE_MONTHLY_FIRST) {
    				String intervalPattern = String.format("M01:%02d%02d", sendTime.get(Calendar.HOUR_OF_DAY), sendTime.get(Calendar.MINUTE));
        			jdbcTemplate.update("UPDATE birtreport_tbl SET intervalpattern = ? WHERE report_id = ?", intervalPattern, reportId);
    			} else if (reportType == BirtReportType.TYPE_MONTHLY_15TH) {
    				String intervalPattern = String.format("M15:%02d%02d", sendTime.get(Calendar.HOUR_OF_DAY), sendTime.get(Calendar.MINUTE));
        			jdbcTemplate.update("UPDATE birtreport_tbl SET intervalpattern = ? WHERE report_id = ?", intervalPattern, reportId);
    			} else if (reportType == BirtReportType.TYPE_MONTHLY_LAST) {
    				String intervalPattern = String.format("M99:%02d%02d", sendTime.get(Calendar.HOUR_OF_DAY), sendTime.get(Calendar.MINUTE));
        			jdbcTemplate.update("UPDATE birtreport_tbl SET intervalpattern = ? WHERE report_id = ?", intervalPattern, reportId);
    			} else if (reportType == BirtReportType.TYPE_AFTER_MAILING_24HOURS) {
    				String intervalPattern = "";
        			jdbcTemplate.update("UPDATE birtreport_tbl SET intervalpattern = ? WHERE report_id = ?", intervalPattern, reportId);
    			} else if (reportType == BirtReportType.TYPE_AFTER_MAILING_48HOURS) {
    				String intervalPattern = "";
        			jdbcTemplate.update("UPDATE birtreport_tbl SET intervalpattern = ? WHERE report_id = ?", intervalPattern, reportId);
    			} else if (reportType == BirtReportType.TYPE_AFTER_MAILING_WEEK) {
    				String intervalPattern = "";
        			jdbcTemplate.update("UPDATE birtreport_tbl SET intervalpattern = ? WHERE report_id = ?", intervalPattern, reportId);
    			} else {
	    			String intervalPattern = "";
	    			if (sendDaysBitMap.charAt(0) == '1') {
	    				intervalPattern += "Mo";
	    			}
	    			if (sendDaysBitMap.charAt(1) == '1') {
	    				intervalPattern += "Tu";
	    			}
	    			if (sendDaysBitMap.charAt(2) == '1') {
	    				intervalPattern += "We";
	    			}
	    			if (sendDaysBitMap.charAt(3) == '1') {
	    				intervalPattern += "Th";
	    			}
	    			if (sendDaysBitMap.charAt(4) == '1') {
	    				intervalPattern += "Fr";
	    			}
	    			if (sendDaysBitMap.charAt(5) == '1') {
	    				intervalPattern += "Sa";
	    			}
	    			if (sendDaysBitMap.charAt(6) == '1') {
	    				intervalPattern += "Su";
	    			}
	    			if (reportType == BirtReportType.TYPE_BIWEEKLY) {
	    				intervalPattern += "Ev";
	    			}
	    			
	    			intervalPattern += String.format(":%02d%02d", sendTime.get(Calendar.HOUR_OF_DAY), sendTime.get(Calendar.MINUTE));
	    			jdbcTemplate.update("UPDATE birtreport_tbl SET intervalpattern = ? WHERE report_id = ?", intervalPattern, reportId);
    			}
    		}
    	}
	}

	private void initializeTargetComplexityIndices() {
		for (ComCompany company : companyDao.getAllActiveCompanies()) {
			int companyId = company.getId();

			if (configService.getBooleanValue(ConfigValue.InitializeTargetGroupComplexityIndicesOnStartup, companyId)) {
				try {
					targetService.initializeComplexityIndex(companyId);
					configService.writeOrDeleteIfDefaultBooleanValue(ConfigValue.InitializeTargetGroupComplexityIndicesOnStartup, companyId, false);
				} catch (Exception e) {
					logger.error("Failed to initialize target groups complexity indices for company #" + companyId);
				}
			}
		}
    }

	private void migrateEmmPropertiesValueToDb(ConfigValue configValueItem, String configClass, String configName) throws Exception {
		String currentValue = configService.getValue(configValueItem);
		if (StringUtils.isNotBlank(currentValue) && currentValue.startsWith(AgnUtils.getUserHomeDir())) {
			currentValue = currentValue.replace(AgnUtils.getUserHomeDir(), "${home}");
		}
		
		if (StringUtils.isNotBlank(currentValue)) {
			boolean foundThisValue = false;
			boolean foundOtherValue = false;
			for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT hostname, value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", configClass, configName)) {
				String hostname = (String) row.get("hostname");
				String value = (String) row.get("value");
				if (StringUtils.isNotBlank(value) && value.startsWith(AgnUtils.getUserHomeDir())) {
					value = value.replace(AgnUtils.getUserHomeDir(), "${home}");
				}
				
				if (StringUtils.isNotBlank(currentValue) && currentValue.equals(value) && (StringUtils.isBlank(hostname) || AgnUtils.getHostName().toLowerCase().equals(hostname))) {
					foundThisValue = true;
				} else if (StringUtils.isNotBlank(value) && !value.equals("[to be defined]")) {
					foundOtherValue = true;
				}
			}
			if (!foundThisValue) {
				if (foundOtherValue) {
					configTableDao.storeEntry(configClass, configName, AgnUtils.getHostName(), currentValue);
				} else {
					configTableDao.storeEntry(configClass, configName, currentValue);
				}
			}
		}
	}

	private void migrateEmmWsPropertiesValueToDb(String propertyName, ConfigValue configValueItem, String configClass, String configName) throws Exception {
		if (getClass().getClassLoader().getResource("emm-ws.properties") != null && (StringUtils.isBlank(configService.getValue(configValueItem)) || "[to be defined]".equals(configService.getValue(configValueItem)))) {
			Properties wsProperties = new Properties();
			wsProperties.load(getClass().getClassLoader().getResourceAsStream("emm-ws.properties"));
		
			String currentValue = wsProperties.getProperty(propertyName);
			if (StringUtils.isNotBlank(currentValue)) {
				boolean foundThisValue = false;
				boolean foundOtherValue = false;
				for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT hostname, value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", configClass, configName)) {
					String hostname = (String) row.get("hostname");
					String value = (String) row.get("value");
					if (StringUtils.isNotBlank(value) && value.startsWith(AgnUtils.getUserHomeDir())) {
						value = value.replace(AgnUtils.getUserHomeDir(), "${home}");
					}
					
					if (StringUtils.isNotBlank(currentValue) && currentValue.equals(value) && (StringUtils.isBlank(hostname) || AgnUtils.getHostName().toLowerCase().equals(hostname))) {
						foundThisValue = true;
					} else if (StringUtils.isNotBlank(value) && !value.equals("[to be defined]")) {
						foundOtherValue = true;
					}
				}
				if (!foundThisValue) {
					if (foundOtherValue) {
						configTableDao.storeEntry(configClass, configName, AgnUtils.getHostName(), currentValue);
					} else {
						configTableDao.storeEntry(configClass, configName, currentValue);
					}
				}
			}
		}
	}

	private void migrateMailgunEmmPropertiesValueToDb(ConfigValue mailgunConfigValueItem, ConfigValue mailoutConfigValueItem, String configClass, String configName) throws Exception {
		String currentValue = configService.getValue(mailgunConfigValueItem);
		if (StringUtils.isNotBlank(currentValue) && currentValue.startsWith(AgnUtils.getUserHomeDir())) {
			currentValue = currentValue.replace(AgnUtils.getUserHomeDir(), "${home}");
		}
		
		if (StringUtils.isNotBlank(currentValue)) {
			boolean foundThisValue = false;
			boolean foundOtherValue = false;
			for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT hostname, value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", configClass, configName)) {
				String hostname = (String) row.get("hostname");
				String value = (String) row.get("value");
				if (StringUtils.isNotBlank(value) && value.startsWith(AgnUtils.getUserHomeDir())) {
					value = value.replace(AgnUtils.getUserHomeDir(), "${home}");
				}
				
				if (StringUtils.isNotBlank(currentValue) && currentValue.equals(value) && (StringUtils.isBlank(hostname) || AgnUtils.getHostName().toLowerCase().equals(hostname))) {
					foundThisValue = true;
				} else if (StringUtils.isNotBlank(value) && !value.equals("[to be defined]")) {
					foundOtherValue = true;
				}
			}
			if (!foundThisValue) {
				if (foundOtherValue) {
					configTableDao.storeEntry(configClass, configName, AgnUtils.getHostName(), currentValue);
				} else {
					configTableDao.storeEntry(configClass, configName, currentValue);
				}
			}
		}
	}

	private void migrateBirtKeys() throws Exception {
		// Migrate Birt configuration to db (birt.privatekey)
		boolean foundThisBirtPrivateKeyValue = false;
		boolean foundOtherBirtPrivateKeyValue = false;
		String birtPrivateKeyFile = configService.getValue(ConfigValue.BirtPrivateKeyFile);
		if (StringUtils.isNotBlank(birtPrivateKeyFile) && new File(birtPrivateKeyFile).exists()) {
			String birtPrivateKey = RSACryptUtil.getPrivateKey(birtPrivateKeyFile);
			for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT hostname, value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", "birt", "privatekey")) {
				String hostname = (String) row.get("hostname");
				String birtPrivateKeyFromDB = (String) row.get("value");
				
				if (StringUtils.isNotBlank(birtPrivateKey) && birtPrivateKey.equals(birtPrivateKeyFromDB) && (StringUtils.isBlank(hostname) || AgnUtils.getHostName().equalsIgnoreCase(hostname))) {
					foundThisBirtPrivateKeyValue = true;
				} else if (StringUtils.isNotBlank(birtPrivateKeyFromDB) && !birtPrivateKeyFromDB.equals("[to be defined]")) {
					foundOtherBirtPrivateKeyValue = true;
				}
			}
			if (!foundThisBirtPrivateKeyValue) {
				if (foundOtherBirtPrivateKeyValue) {
					configTableDao.storeEntry("birt", "privatekey", AgnUtils.getHostName(), birtPrivateKey);
				} else {
					configTableDao.storeEntry("birt", "privatekey", birtPrivateKey);
				}
				logger.info("Added new birt.privatekey to config_tbl");
				configService.enforceExpiration();
			}
		}

		// Migrate Birt configuration to db (birt.publickey)
		boolean foundThisBirtPublicKeyValue = false;
		boolean foundOtherBirtPublicKeyValue = false;
		String birtPublicKeyFile = configService.getValue(ConfigValue.BirtPublicKeyFile);
		if (StringUtils.isNotBlank(birtPublicKeyFile) && new File(birtPublicKeyFile).exists()) {
			String birtPublicKey = RSACryptUtil.getPublicKey(birtPublicKeyFile);
			for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT hostname, value FROM config_tbl WHERE class = ? AND name = ? AND (hostname IS NULL OR hostname = '')", "birt", "publickey")) {
				String hostname = (String) row.get("hostname");
				String birtPublicKeyFromDB = (String) row.get("value");
				
				if (StringUtils.isNotBlank(birtPublicKey) && birtPublicKey.equals(birtPublicKeyFromDB) && (StringUtils.isBlank(hostname) || AgnUtils.getHostName().equalsIgnoreCase(hostname))) {
					foundThisBirtPublicKeyValue = true;
				} else if (StringUtils.isNotBlank(birtPublicKeyFromDB) && !birtPublicKeyFromDB.equals("[to be defined]")) {
					foundOtherBirtPublicKeyValue = true;
				}
			}
			if (!foundThisBirtPublicKeyValue) {
				if (foundOtherBirtPublicKeyValue) {
					configTableDao.storeEntry("birt", "publickey", AgnUtils.getHostName(), birtPublicKey);
				} else {
					configTableDao.storeEntry("birt", "publickey", birtPublicKey);
				}
				logger.info("Added new birt.publickey to config_tbl");
				configService.enforceExpiration();
			}
		}
	}
	
	/**
	 * EMM-7052: Migrate company_tbl values to configservice
	 */
    private void migrateCompanyValues() {
    	for (int companyID : jdbcTemplate.queryForList("SELECT company_id FROM company_tbl", Integer.class)) {
    		if (!configService.getBooleanValue(ConfigValue.CompanyValuesMigrated, companyID)) {
    			int maxadminmailsDefault = configService.getIntegerValue(ConfigValue.MaxAdminMails);
    			int expireCookieDefault = configService.getIntegerValue(ConfigValue.CookieExpire);
    			int expireStatDefault = configService.getIntegerValue(ConfigValue.ExpireStatistics);
    			int expireBounceDefault = configService.getIntegerValue(ConfigValue.ExpireBounce);
    			int expireOnePixelDefault = configService.getIntegerValue(ConfigValue.ExpireOnePixel);
    			int expireRecipientDefault = configService.getIntegerValue(ConfigValue.ExpireRecipient);
    			int expireUploadDefault = configService.getIntegerValue(ConfigValue.ExpireUpload);
    			int expireSuccessDefault = configService.getIntegerValue(ConfigValue.ExpireSuccess);
    			int maxFieldsDefault = configService.getIntegerValue(ConfigValue.MaxFields);
		    	for (Map<String, Object> row : jdbcTemplate.queryForList("SELECT maxadminmails, expire_cookie, expire_stat, expire_bounce, expire_onepixel, expire_recipient, expire_upload, expire_success FROM company_tbl WHERE company_id = ?", companyID)) {
		    		if (row.get("maxadminmails") != null) {
			    		int maxadminmails = ((Number) row.get("maxadminmails")).intValue();
			    		if (maxadminmailsDefault != maxadminmails) {
			    			configService.writeValue(ConfigValue.MaxAdminMails, companyID, Integer.toString(maxadminmails));
			    		}
		    		}

		    		if (row.get("expire_cookie") != null) {
			    		int expireCookie = ((Number) row.get("expire_cookie")).intValue();
			    		if (expireCookieDefault != expireCookie) {
			    			configService.writeValue(ConfigValue.CookieExpire, companyID, Integer.toString(expireCookie));
			    		}
		    		}

		    		if (row.get("expire_stat") != null) {
			    		int expireStat = ((Number) row.get("expire_stat")).intValue();
			    		if (expireStatDefault != expireStat) {
			    			configService.writeValue(ConfigValue.ExpireStatistics, companyID, Integer.toString(expireStat));
			    		}
		    		}

		    		if (row.get("expire_bounce") != null) {
			    		int expireBounce = ((Number) row.get("expire_bounce")).intValue();
			    		if (expireBounceDefault != expireBounce) {
			    			configService.writeValue(ConfigValue.ExpireBounce, companyID, Integer.toString(expireBounce));
			    		}
		    		}

		    		if (row.get("expire_onepixel") != null) {
			    		int expireOnePixel = ((Number) row.get("expire_onepixel")).intValue();
			    		if (expireOnePixelDefault != expireOnePixel) {
			    			configService.writeValue(ConfigValue.ExpireOnePixel, companyID, Integer.toString(expireOnePixel));
			    		}
		    		}

		    		if (row.get("expire_recipient") != null) {
			    		int expireRecipient = ((Number) row.get("expire_recipient")).intValue();
			    		if (expireRecipientDefault != expireRecipient) {
			    			configService.writeValue(ConfigValue.ExpireRecipient, companyID, Integer.toString(expireRecipient));
			    		}
		    		}

		    		if (row.get("expire_upload") != null) {
			    		int expireUpload = ((Number) row.get("expire_upload")).intValue();
			    		if (expireUploadDefault != expireUpload) {
			    			configService.writeValue(ConfigValue.ExpireUpload, companyID, Integer.toString(expireUpload));
			    		}
		    		}

		    		if (row.get("expire_success") != null) {
			    		int expireSuccess = ((Number) row.get("expire_success")).intValue();
			    		if (expireSuccessDefault != expireSuccess) {
			    			configService.writeValue(ConfigValue.ExpireSuccess, companyID, Integer.toString(expireSuccess));
			    		}
		    		}

		    		if (row.get("max_fields") != null) {
			    		int maxFields = ((Number) row.get("max_fields")).intValue();
			    		if (maxFieldsDefault != maxFields) {
			    			configService.writeValue(ConfigValue.MaxFields, companyID, Integer.toString(maxFields));
			    		}
		    		}
		    	}
		    	configService.writeBooleanValue(ConfigValue.CompanyValuesMigrated, companyID, true);
    		}
    	}
	}
    
    public void migrateLogosAndImages(WebApplicationContext webApplicationContext) {
		if (layoutDao.getLayoutData().size() == 0) {
			File faviconFile = new File(webApplicationContext.getServletContext().getRealPath("favicon.ico"));
			if (faviconFile.exists()) {
				try (InputStream faviconInputStream = new FileInputStream(faviconFile)) {
					layoutDao.saveLayoutData(0, "favicon.ico", IOUtils.toByteArray(faviconInputStream));
				} catch (Exception e) {
					logger.error("Cannot find faviconFile: " + faviconFile.getAbsolutePath(), e);
				}
			} else {
				logger.error("Cannot find faviconFile: " + faviconFile.getAbsolutePath());
			}

			File emmLogoSvgFile = new File(webApplicationContext.getServletContext().getRealPath("/assets/core/images/facelift/agnitas-emm-logo.svg"));
			if (emmLogoSvgFile.exists()) {
				try (InputStream logoSvgInputStream = new FileInputStream(emmLogoSvgFile)) {
					layoutDao.saveLayoutData(0, "logo.svg", IOUtils.toByteArray(logoSvgInputStream));
				} catch (Exception e) {
					logger.error("Cannot find emmLogoSvgFile: " + emmLogoSvgFile.getAbsolutePath(), e);
				}
			} else {
				logger.error("Cannot find emmLogoSvgFile: " + emmLogoSvgFile.getAbsolutePath());
			}

			File emmLogoPngFile = new File(webApplicationContext.getServletContext().getRealPath("/assets/core/images/facelift/agnitas-emm-logo.png"));
			if (emmLogoPngFile.exists()) {
				try (InputStream logoPngInputStream = new FileInputStream(emmLogoPngFile)) {
					layoutDao.saveLayoutData(0, "logo.png", IOUtils.toByteArray(logoPngInputStream));
				} catch (Exception e) {
					logger.error("Cannot find emmLogoPngFile: " + emmLogoPngFile.getAbsolutePath(), e);
				}
			} else {
				logger.error("Cannot find emmLogoPngFile: " + emmLogoPngFile.getAbsolutePath());
			}

			File editionLogoFile = new File(webApplicationContext.getServletContext().getRealPath("/assets/core/images/facelift/edition_logo.png"));
			if (editionLogoFile.exists()) {
				try (InputStream editionLogoInputStream = new FileInputStream(editionLogoFile)) {
					layoutDao.saveLayoutData(0, "edition_logo.png", IOUtils.toByteArray(editionLogoInputStream));
				} catch (Exception e) {
					logger.error("Cannot find editionLogoFile: " + editionLogoFile.getAbsolutePath(), e);
				}
			} else {
				logger.error("Cannot find editionLogoFile: " + editionLogoFile.getAbsolutePath());
			}
		}
    }
}
