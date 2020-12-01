/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.service.impl;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.zip.ZipOutputStream;

import javax.servlet.ServletContext;

import org.agnitas.emm.core.autoimport.dao.AutoImportDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.service.JobDto;
import org.agnitas.service.JobQueueService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.ZipUtilities;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComServerStatusDao;
import com.agnitas.emm.core.JavaMailAttachment;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.serverstatus.bean.ServerStatus;
import com.agnitas.emm.core.serverstatus.bean.VersionStatus;
import com.agnitas.emm.core.serverstatus.dto.ConfigValueDto;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.messages.Message;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.Version;

public abstract class ServerStatusServiceImplBasic implements ServerStatusService {
	
	private static final Logger logger = Logger.getLogger(ServerStatusService.class);
	
	private static final String ORACLE = "oracle";
	private static final String MYSQL = "mysql";
	
	private static final String ERROR = "ERROR";
	private static final String OK = "OK";
	
	protected ComServerStatusDao serverStatusDao;
	
	protected AutoImportDao autoImportDao;

	protected ConfigService configService;
	
	protected JobQueueService jobQueueService;
	
	protected JavaMailService javaMailService;
	
	@Override
	public boolean checkDatabaseConnection() {
	    return serverStatusDao.checkDatabaseConnection();
    }
    
    @Override
    public String getDbUrl() {
		try {
			return serverStatusDao.getDbUrl();
		} catch (Exception e) {
			logger.error("Cannot obtain DB url: ", e);
			return "";
		}
	}
	
	@Override
	public boolean isDBStatusOK() {
		// Only retrieve the status once per request for performance
		for (Version version : getMandatoryDbVersions()) {
			if (!serverStatusDao.checkDatabaseVersion(version.getMajorVersion(), version.getMinorVersion(),
					version.getMicroVersion(), version.getHotfixVersion())) {
				return false;
			}
		}
		return true;
	}
	
	@Override
	public List<VersionStatus> getLatestDBVersionsAndErrors() {
		List<VersionStatus> returnList = new ArrayList<>();
		int rownum = 0;
		List<Version> mandatoryDbVersions = getMandatoryDbVersions();
		for (Version version : mandatoryDbVersions) {
			if (serverStatusDao.checkDatabaseVersion(version.getMajorVersion(), version.getMinorVersion(), version.getMicroVersion(), version.getHotfixVersion())) {
				if (rownum >= mandatoryDbVersions.size() - 10) {
					//only show ok versions if they are the latest 10
					returnList.add(new VersionStatus(version, true));
				}
			} else {
				returnList.add(new VersionStatus(version, false));
			}
			rownum++;
		}
		return returnList;
	}
	
	@Override
	public Map<String, Object> getStatusProperties(ServletContext servletContext) throws Exception {
		Map<String, Object> status = new LinkedHashMap<>();
		
		// Various times and paths
		SimpleDateFormat dateFormat = new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS);
		
		status.put("host.name", AgnUtils.getHostName());
		status.put("host.time", dateFormat.format(new Date()));
		status.put("host.build.time", dateFormat.format(ConfigService.getBuildTime()));
		status.put("host.startup.time", dateFormat.format(configService.getStartupTime()));
		status.put("host.up.time", DateUtil.getTimespanString(new Date().getTime() - configService.getStartupTime().getTime(), Locale.getDefault()));
		status.put("emm.config.expiration", configService.getConfigurationExpirationTime() == null ? "Null" : dateFormat.format(configService.getConfigurationExpirationTime()));
		status.put("emm.version", configService.getValue(ConfigValue.ApplicationVersion));
		status.put("emm.tempdir", AgnUtils.getTempDir());
		status.put("emm.installpath", servletContext.getRealPath("/"));
		
		status.put("os.version", AgnUtils.getOSVersion());
		
		// Python Version
		status.put("python.version", StringUtils.defaultString(AgnUtils.getPythonVersion(), ERROR));
		status.put("python.ssl", StringUtils.defaultString(AgnUtils.getPythonSSL(), ERROR));
		
		// MySQL Client Version
		String mysqlVersion = AgnUtils.getMysqlClientVersion();
		if (StringUtils.isNotEmpty(mysqlVersion)) {
			status.put("mysql.client.version", mysqlVersion);
		}
		
		// SqlPlus Client Version
		String sqlPlusVersion = AgnUtils.getSqlPlusClientVersion();
		if (StringUtils.isNotEmpty(sqlPlusVersion)) {
			status.put("sqlplus.client.version", sqlPlusVersion);
		}

		// Database info
		try {
			status.putAll(serverStatusDao.geDbInformation());
		} catch (Exception e) {
			status.put("db.version", ERROR);
		}
		
		status.put("db.url", serverStatusDao.getDbUrl());
		
		// EMM database versions
		List<String> missingDbUpdates = new ArrayList<>();
		for (Version version : getMandatoryDbVersions()) {
			if (!serverStatusDao.checkDatabaseVersion(version.getMajorVersion(),
					version.getMinorVersion(), version.getMicroVersion(),
					version.getHotfixVersion())) {
				missingDbUpdates.add(version.toString());
			}
		}
		if (missingDbUpdates.size() > 0) {
			status.put("db.updates", ERROR);
			status.put("db.updates.missing", StringUtils.join(missingDbUpdates, ", "));
		} else {
			status.put("db.updates", OK);
		}

		// Error Log entry count
		try {
			status.put("db.errors", serverStatusDao.getLogEntryCount());
		} catch (Exception e) {
			status.put("db.errors", ERROR);
		}
		
		// Job queue status
		try {
			List<String> errorneousStatus = serverStatusDao.getErrorJobsStatuses();
			status.put("emm.errorneaousjobs.count", Integer.toString(errorneousStatus.size()));
			status.put("emm.errorneaousjobs", StringUtils.join(errorneousStatus, ", "));
		} catch (Exception e) {
			status.put("emm.errorneaousjobs", ERROR);
		}
		
		// Mail version
		status.put("mail.version", AgnUtils.getMailVersion());
		
		// JAVA Version
		try {
			status.put("java.version", System.getProperty("java.version"));
		} catch (Exception e) {
			status.put("java.version", ERROR);
		}
		
		// JAVA JCE enabled
		try {
			status.put("java.jceunlimitedkeystrength", AgnUtils.isJCEUnlimitedKeyStrenght());
		} catch (Exception e) {
			status.put("java.jceunlimitedkeystrength", ERROR);
		}

		// Tomcat Version
		status.put("tomcat.version", StringUtils.defaultIfEmpty(AgnUtils.getTomcatVersion(), ERROR));
		
		// Wkhtml version
		status.put("wkhtml.version", StringUtils.defaultString(AgnUtils.getWkhtmlVersion(configService), ERROR));
		
		// DKIM keys
		try {
			List<String> dkimKeys = serverStatusDao.getDKIMKeys();
			status.put("emm.dkimkeys.count", dkimKeys.size());
			status.put("emm.dkimkeys", StringUtils.join(dkimKeys, ", "));
		} catch (Exception e) {
			status.put("emm.dkimkeys", ERROR);
		}
		
		// Connections to backend hosts
		status.putAll(configService.getHostSystemProperties());
		
		return status;
	}
	
	@Override
	public ServerStatus getServerStatus(ServletContext servletContext, ComAdmin admin) {
		String version = configService.getValue(ConfigValue.ApplicationVersion);
		String installPath = servletContext.getRealPath("/");
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS);
		return ServerStatus.builder(version, installPath, admin.getLocale())
				.database(serverStatusDao.getDbVendor(), getDbUrl(), checkDatabaseConnection())
				.dateTimeSettings(dateTimeFormat, configService.getStartupTime(), configService.getConfigurationExpirationTime())
				.statuses(isOverallStatusOK(), isJobQueueStatusOK(), !isImportStalling(), isDBStatusOK(), isReportStatusOK())
				.dbVersionStatuses(getLatestDBVersionsAndErrors())
				.build();
	}
	
	@Override
	public SimpleServiceResult sendTestMail(ComAdmin admin, String testMailAddress) {
		boolean success = false;
		Message message;
		
		try {
			String allCharacters = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 äöüßÄÖÜµ!?§@€$%&/\\<>(){}[]'\"´`^°¹²³*#.,;:=+-~_|½¼¬";
			String subject = "Test mail subject: " + allCharacters;
			String textMessage = "Test text mail content: " + allCharacters;
			String htmlMessage = "<h1>Test html mail content:</h1><br />\n" + allCharacters;

			// Create same default from-address for result message as it will be used by javaMailService
			String fromAddress = configService.getValue(ConfigValue.Mailaddress_Sender);
			if (StringUtils.isBlank(fromAddress)) {
				fromAddress = System.getProperty("user.name") + "@" + AgnUtils.getHostName();
			}

			success = javaMailService.sendEmail(testMailAddress, subject, textMessage, htmlMessage,
					new JavaMailAttachment("Tästfile.txt", "Täxt".getBytes("UTF-8"), "text/plain"));
			
			message = Message.exact(String.format("Email to %s %s sent with \"from\"-address: %s", testMailAddress,
					success ? "was successfully" : "wasn't successfully", fromAddress));
		} catch (Exception e) {
			logger.error(String.format("Cannot sent test email to %s, cause: %s", testMailAddress, e.getMessage()), e);
			message = Message.of("Error");
		}
		
		return new SimpleServiceResult(success, message);
	}
	
	@Override
	public SimpleServiceResult sendDiagnosisInfo(ServletContext servletContext, ComAdmin admin, String sendDiagnosisEmail) {
		boolean success = false;
		Message message;
		
		try {
			String subject = "Server status diagnosis data";
			String textMessage = "Server status diagnosis data:\n" +
					getStatusProperties(servletContext).entrySet().stream()
							.map(pair -> String.format("%s: %s", pair.getKey(), String.valueOf(pair.getValue())))
							.collect(Collectors.joining("\n"));

			// Create same default from-address for result message as it will be used by javaMailService
			String fromAddress = configService.getValue(ConfigValue.Mailaddress_Sender);
			if (StringUtils.isBlank(fromAddress)) {
				fromAddress = System.getProperty("user.name") + "@" + AgnUtils.getHostName();
			}

			success = javaMailService.sendEmail(sendDiagnosisEmail, subject, textMessage, null);
			
			message = Message.exact(String.format("Email to %s %s sent with \"from\"-address: %s", sendDiagnosisEmail,
					success ? "was successfully" : "wasn't successfully", fromAddress));
		} catch (Exception e) {
			logger.error(String.format("Cannot sent test email to %s, cause: %s", sendDiagnosisEmail, e.getMessage()), e);
			message = Message.of("Error");
		}
		
		return new SimpleServiceResult(success, message);
	}
	
	@Override
    public boolean saveServerConfig(int companyId, String configName, String configValue, String description) {
		if (companyId < 0 || !configService.getListValue(ConfigValue.EditableConfigValues).contains(configName) || StringUtils.isBlank(configValue)) {
			return false;
		}

		ConfigValue configValueId = ConfigValue.getConfigValueByName(configName);
		configService.writeValue(configValueId, companyId, configValue, description);
		return true;
	}
	
	@Override
	public ConfigValueDto getServerConfigurations(int companyId, String configName) {
 		ConfigValueDto configValueDto = new ConfigValueDto();
		if (companyId <= 0 || StringUtils.isBlank(configName)) {
			return configValueDto;
		}
		
		try {
			ConfigValue configValueId = ConfigValue.getConfigValueByName(configName);
			
			configValueDto.setCompanyId(companyId);
			configValueDto.setName(configName);
			configValueDto.setValue(configService.getValue(configValueId, companyId));
			configValueDto.setDescription(configService.getDescription(configValueId, companyId));
			return configValueDto;
		} catch (Exception e) {
			logger.error("Cannot find config value by name: " + configName);
		}
		
		return configValueDto;
	}
	
	private boolean isOverallStatusOK() {
		return isDBStatusOK() && isJobQueueRunning() && isJobQueueStatusOK() && !isImportStalling() && isReportStatusOK();
	}

	@Override
	public boolean isJobQueueRunning() {
		// Check if last job queue run was in acceptable time
		return jobQueueService.isJobQueueRunning();
	}

	@Override
	public boolean isJobQueueStatusOK() {
		// Only retrieve the status once per request for performance
		return jobQueueService.isStatusOK();
	}

	@Override
	public boolean isReportStatusOK() {
		// Only retrieve the status once per request for performance
		return jobQueueService.isReportOK();
	}

	@Override
	public boolean isImportStalling() {
		// Only retrieve the status once per request for performance
		return autoImportDao != null && autoImportDao.isImportStalling();
	}
	
	private List<Version> getMandatoryDbVersions() {
		List<Version> list = new ArrayList<>();
		String dbVendor = ConfigService.isOracleDB() ? ORACLE : MYSQL;
		try {
			@SuppressWarnings("unchecked")
			List<String> versionStringList = IOUtils.readLines(getClass().getClassLoader()
							.getResourceAsStream("mandatoryDbChanges_"+ dbVendor + ".csv"),
							"UTF-8");
			for (String versionString : versionStringList) {
				if (!versionString.contains("emm-")) {
					list.add(new Version(versionString));
				} else {
					logger.error("Invalid version sign: '" + versionString + "'");
					continue;
				}
			}
		} catch (Exception e) {
			logger.error("mandatoryDbChanges.csv contains invalid data", e);
		}

		Collections.sort(list);
		return list;
	}

	@Override
	public String getVersion() {
		return configService.getValue(ConfigValue.ApplicationVersion);
	}

	@Override
	public List<JobDto> getErrorneousJobs() {
		return jobQueueService.selectErrorneousJobs();
	}
	
	@Override
	public List<String> killRunningImports() {
		return serverStatusDao.killRunningImports();
	}

	@Override
	public boolean checkActiveNode() {
		return jobQueueService.checkActiveNode();
	}
	
	@Override
	public File getFullTbl(String dbStatement, String tableName) throws Exception {
		return serverStatusDao.getFullTbl(dbStatement, tableName);
	}
	
	@Override
	public File downloadConfigFile() throws IOException, Exception {
		File zippedFile = File.createTempFile(AgnUtils.getTempDir() + "/ConfigTables_", ".zip");
		ZipOutputStream zipOutput = ZipUtilities.openNewZipOutputStream(zippedFile);
		
		String[] allTables = {"config_tbl", "company_tbl", "company_info_tbl", "serverset_tbl", "serverprop_tbl"};
		
		String[] allSelects = {"select * from config_tbl order by class, name", 
				"select * from company_tbl order by company_id", 
				"select * from company_info_tbl order by company_id, cname", 
				"select * from serverset_tbl order by set_id", 
				"select * from serverprop_tbl order by mailer, mvar"
				};
		
		for (int i = 0; i < allTables.length; i++) {
			try {
				
				File fullTbl = getFullTbl(allSelects[i], allTables[i]);
				
				if (fullTbl != null) {
					ZipUtilities.addFileToOpenZipFileStream(fullTbl, zipOutput);
				}
			
			} catch (IOException ex) {
				logger.error("Error writing file." + ex);
				throw ex;
			}
		}
		
		ZipUtilities.closeZipOutputStream(zipOutput);
		
		return zippedFile;
	}
}
