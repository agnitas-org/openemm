/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
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

import com.agnitas.beans.Admin;
import com.agnitas.dao.ServerStatusDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.auto_import.bean.AutoImport;
import com.agnitas.emm.core.serverstatus.PuppeteerStatus;
import com.agnitas.emm.core.serverstatus.bean.ServerStatus;
import com.agnitas.emm.core.serverstatus.bean.VersionStatus;
import com.agnitas.emm.core.serverstatus.dto.ConfigValueDto;
import com.agnitas.emm.core.serverstatus.service.ServerStatusService;
import com.agnitas.emm.puppeteer.service.PuppeteerService;
import com.agnitas.messages.Message;
import com.agnitas.service.JobDto;
import com.agnitas.service.JobQueueService;
import com.agnitas.service.SimpleServiceResult;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HttpUtils;
import com.agnitas.util.TarGzUtilities;
import com.agnitas.util.Version;
import com.agnitas.util.ZipUtilities;
import jakarta.servlet.ServletContext;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.commons.util.DateUtil;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

public class ServerStatusServiceImpl implements ServerStatusService {

	private static final Logger logger = LogManager.getLogger(ServerStatusServiceImpl.class);

	private static final String TEMP_LICENSE_DIRECTORY = AgnUtils.getTempDir() + File.separator + "License";

	private static final String ORACLE = "oracle";
	private static final String MARIADB = "mariadb";

	private static final String ERROR = "ERROR";
	private static final String OK = "OK";

	private final ServerStatusDao serverStatusDao;
	private final ConfigService configService;
	private final JobQueueService jobQueueService;
	private final JavaMailService javaMailService;
	private final PuppeteerService puppeteerService;

	@Autowired
	public ServerStatusServiceImpl(ServerStatusDao serverStatusDao, ConfigService configService, JobQueueService jobQueueService,
								   JavaMailService javaMailService, @Autowired(required = false) PuppeteerService puppeteerService) {
		this.serverStatusDao = serverStatusDao;
		this.configService = configService;
		this.jobQueueService = jobQueueService;
		this.javaMailService = javaMailService;
        this.puppeteerService = puppeteerService;
    }

	private boolean checkDatabaseConnection() {
	    return serverStatusDao.checkDatabaseConnection();
    }

    @Override
    public String getDbUrl() {
		return serverStatusDao.getDbUrl();
	}

    @Override
    public String getDbVersion() {
		return serverStatusDao.getDbVersion();
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

	private Map<String, Object> getStatusProperties(ServletContext servletContext) {
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

        try {
            status.put("os.version", AgnUtils.getOSVersion());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

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
			List<String> erroneousStatus = serverStatusDao.getErrorJobsStatuses();
			status.put("emm.errorneaousjobs.count", Integer.toString(erroneousStatus.size()));
			status.put("emm.errorneaousjobs", StringUtils.join(erroneousStatus, ", "));
		} catch (Exception e) {
			status.put("emm.errorneaousjobs", ERROR);
		}

		// Mail version
		status.put("mail.version", AgnUtils.getMailVersion());

		// Tomcat Version
		status.put("tomcat.version", StringUtils.defaultIfEmpty(AgnUtils.getTomcatVersion(), ERROR));

		// Java Version
		status.put("java.version", StringUtils.defaultIfEmpty(System.getProperty("java.version"), ERROR));
		if (StringUtils.isNotBlank(System.getProperty("java.vm.name"))) {
			status.put("java.vendor", StringUtils.defaultIfEmpty(System.getProperty("java.vm.name"), ERROR));
		} else {
			status.put("java.vendor", StringUtils.defaultIfEmpty(System.getProperty("java.vendor"), ERROR));
		}

		// OS name and version
		status.put("os.name", StringUtils.defaultString(System.getProperty("os.name"), ERROR));
		status.put("os.version", StringUtils.defaultString(System.getProperty("os.version"), ERROR));
		try {
			String osDistributionName = "";
			String osDistributionVersion = "";
			File osVersionFile = new File("/etc/os-release");
			if (osVersionFile.exists()) {
				for (String line : FileUtils.readLines(osVersionFile, StandardCharsets.UTF_8)) {
					if (line.startsWith("NAME=")) {
						osDistributionName = line.substring(5).trim().replaceAll("^\"|\"$", "");
					} else if (line.startsWith("VERSION=") && StringUtils.isBlank(osDistributionVersion)) {
						osDistributionVersion = line.substring(8).trim().replaceAll("^\"|\"$", "");
					} else if (line.startsWith("VERSION_ID=")) {
						osDistributionVersion = line.substring(11).trim().replaceAll("^\"|\"$", "");
					}
				}
			}
			status.put("os.distribution", StringUtils.defaultString(osDistributionName + " " + osDistributionVersion, ERROR));
		} catch (@SuppressWarnings("unused") Exception e) {
			// do nothing
		}

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
	public ServerStatus getServerStatus(ServletContext servletContext, Admin admin) {
		String version = configService.getValue(ConfigValue.ApplicationVersion);
		String installPath = servletContext.getRealPath("/");
		SimpleDateFormat dateTimeFormat = new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS);

		List<AutoImport> stallingAutoImports = getStallingAutoImports();
		boolean importStatusOK = getStallingImportsAmount(configService.getIntegerValue(ConfigValue.MaxUserImportDurationMinutes)) == 0 && (stallingAutoImports == null || stallingAutoImports.size() == 0);
		PuppeteerStatus puppeteerStatus = puppeteerService.getStatus();

		return ServerStatus.builder(version, installPath, admin.getLocale(), configService)
				.database(serverStatusDao.getDbVendor(), getDbUrl(), getDbVersion(), checkDatabaseConnection())
				.dateTimeSettings(dateTimeFormat, configService.getStartupTime(), configService.getConfigurationExpirationTime())
				.statuses(isOverallStatusOK() && puppeteerStatus.isAvailable(), isJobQueueStatusOK(), importStatusOK, !isExportStalling(), isDBStatusOK(), isReportStatusOK(), isLicenseStatusOK())
				.dbVersionStatuses(getLatestDBVersionsAndErrors())
				.diskSpaceFreePercentage(calcDiskSpaceFreePercentage())
				.puppeteerStatus(puppeteerStatus)
				.build();
	}

	@Override
    public int calcDiskSpaceFreePercentage() {
        File rootDir = new File("/");
        return (int) (rootDir.getFreeSpace() * 100 / rootDir.getTotalSpace());
    }

	@Override
	public ServerStatus getAnonymousServerStatus(ServletContext servletContext) {

		List<AutoImport> stallingAutoImports = getStallingAutoImports();
		boolean importStatusOK = getStallingImportsAmount(configService.getIntegerValue(ConfigValue.MaxUserImportDurationMinutes)) == 0 && (stallingAutoImports == null || stallingAutoImports.size() == 0);


		return ServerStatus.externalStatusBuilder()
				.statuses(isOverallStatusOK(), isJobQueueStatusOK(), importStatusOK, !isExportStalling(), isDBStatusOK(), isReportStatusOK(), isLicenseStatusOK(), checkDatabaseConnection())
				.externalStatusBuilder();
	}

	@Override
	public SimpleServiceResult sendTestMail(Admin admin, String testMailAddress) {
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

			success = javaMailService.sendEmail(admin.getCompanyID(), testMailAddress, subject, textMessage, htmlMessage);

			message = Message.exact(String.format("Email to %s %s sent with \"from\"-address: %s", testMailAddress,
					success ? "was successfully" : "wasn't successfully", fromAddress));
		} catch (Exception e) {
			logger.error(String.format("Cannot sent test email to %s, cause: %s", testMailAddress, e.getMessage()), e);
			message = Message.of("Error");
		}

		return new SimpleServiceResult(success, message);
	}

	@Override
	public SimpleServiceResult sendDiagnosisInfo(ServletContext servletContext, Admin admin, String sendDiagnosisEmail) {
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

		boolean success = javaMailService.sendEmail(admin.getCompanyID(), sendDiagnosisEmail, subject, textMessage, null);

		Message message = Message.exact(String.format("Email to %s %s sent with \"from\"-address: %s", sendDiagnosisEmail,
				success ? "was successfully" : "wasn't successfully", fromAddress));

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

	@Override
	public boolean isOverallStatusOK() {
		List<AutoImport> stallingAutoImports = getStallingAutoImports();
		boolean importStatusOK = getStallingImportsAmount(configService.getIntegerValue(ConfigValue.MaxUserImportDurationMinutes)) == 0 && (stallingAutoImports == null || stallingAutoImports.size() == 0);
		return isDBStatusOK() && isJobQueueRunning() && isJobQueueStatusOK() && importStatusOK && isReportStatusOK();
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
	public boolean isLicenseStatusOK() {
		return configService.isLicenseStatusOK();
	}

	@Override
	public List<AutoImport> getStallingAutoImports() {
		return null;
	}

	@Override
	public int getStallingImportsAmount(int maxUserImportDurationMinutes) {
		return 0;
	}

	@Override
	public boolean isExportStalling() {
		// Only retrieve the status once per request for performance
		return false;
	}

	private List<Version> getMandatoryDbVersions() {
		List<Version> list = new ArrayList<>();
		String dbVendor = ConfigService.isOracleDB() ? ORACLE : MARIADB;
		try {
			List<String> versionStringList = IOUtils.readLines(getClass().getClassLoader()
							.getResourceAsStream("mandatoryDbChanges_"+ dbVendor + ".csv"),
							StandardCharsets.UTF_8);
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
	public List<JobDto> getErroneousJobs() {
		return jobQueueService.selectErroneousJobs();
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
	public File unzipLicenseFile(MultipartFile archiveFile) throws Exception {
		String currentTimeString = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date());
		File unzippedLicenseDataDirectory = new File(TEMP_LICENSE_DIRECTORY + "/" + currentTimeString);

		if (!new File(TEMP_LICENSE_DIRECTORY).exists()) {
			new File(TEMP_LICENSE_DIRECTORY).mkdirs();
		}

		if (archiveFile.getOriginalFilename().toLowerCase().endsWith(".zip")) {
			File uploadedLicenseDataFile = new File(TEMP_LICENSE_DIRECTORY + "/" + currentTimeString + ".zip");
			try {
				archiveFile.transferTo(uploadedLicenseDataFile);
				ZipUtilities.decompress(uploadedLicenseDataFile, unzippedLicenseDataDirectory);
			} finally {
				if (uploadedLicenseDataFile.exists()) {
					uploadedLicenseDataFile.delete();
				}
			}
		} else if (archiveFile.getOriginalFilename().toLowerCase().endsWith(".tar.gz")) {
			File uploadedLicenseDataFile = new File(TEMP_LICENSE_DIRECTORY + "/" + currentTimeString + ".tar.gz");
			try {
				archiveFile.transferTo(uploadedLicenseDataFile);
				TarGzUtilities.decompress(uploadedLicenseDataFile, unzippedLicenseDataDirectory);
			} finally {
				if (uploadedLicenseDataFile.exists()) {
					uploadedLicenseDataFile.delete();
				}
			}
		} else if (archiveFile.getOriginalFilename().toLowerCase().endsWith(".tgz")) {
			File uploadedLicenseDataFile = new File(TEMP_LICENSE_DIRECTORY + "/" + currentTimeString + ".tgz");
			try {
				archiveFile.transferTo(uploadedLicenseDataFile);
				TarGzUtilities.decompress(uploadedLicenseDataFile, unzippedLicenseDataDirectory);
			} finally {
				if (uploadedLicenseDataFile.exists()) {
					uploadedLicenseDataFile.delete();
				}
			}
		} else {
			throw new IllegalArgumentException("Unknown license data format");
		}

		return unzippedLicenseDataDirectory;
	}

	@Override
	public File downloadConfigFile() throws IOException {
		File zippedFile = File.createTempFile(AgnUtils.getTempDir() + "/ConfigTables_", ".zip");
		try (ZipOutputStream zipOutput = ZipUtilities.openNewZipOutputStream(zippedFile)) {
            Map<String, String> tableSelectsMap = Map.of(
                    "config_tbl","SELECT * FROM config_tbl ORDER BY class, name",
                    "company_tbl","SELECT * FROM company_tbl ORDER BY company_id",
                    "company_info_tbl", "SELECT * FROM company_info_tbl ORDER BY company_id, cname",
                    "serverset_tbl", "SELECT * FROM serverset_tbl ORDER BY set_id",
                    "serverprop_tbl", "SELECT * FROM serverprop_tbl ORDER BY mailer, mvar"
            );

            for (Map.Entry<String, String> entry : tableSelectsMap.entrySet()) {
                try {
					File fullTbl = serverStatusDao.getFullTbl(entry.getValue(), entry.getKey());

                    if (fullTbl != null) {
                        ZipUtilities.addFileToOpenZipFileStream(fullTbl, zipOutput);
                    }

                } catch (IOException ex) {
                    ZipUtilities.closeZipOutputStream(zipOutput);
                    logger.error("Error writing file." + ex);
                    throw ex;
                } catch (Exception e) {
                    throw new RuntimeException("Error occurred when get table data! Table - " + entry.getValue(), e);
                }
            }

			ZipUtilities.closeZipOutputStream(zipOutput);
		}

		return zippedFile;
	}

	@Override
	public JSONArray getSystemStatus() {
		JSONArray allStatus = new JSONArray();
		String[] statusNames = {
				"overall",
				"jobqueue",
				"import",
				"dbOverall",
				"dbConnection",
				"report"
			};
		List<AutoImport> stallingAutoImports = getStallingAutoImports();
		boolean importStatusOK = getStallingImportsAmount(configService.getIntegerValue(ConfigValue.MaxUserImportDurationMinutes)) == 0 && (stallingAutoImports == null || stallingAutoImports.size() == 0);
		boolean[] statusValues = {
				isOverallStatusOK(),
				isJobQueueStatusOK(),
				importStatusOK,
				isDBStatusOK(),
				checkDatabaseConnection(),
				isReportStatusOK()
			};

		for (int i = 0; i < statusNames.length; i++) {
			JSONObject m = new JSONObject();
			m.put("shortname", statusNames[i]);
			m.put("value", statusValues[i]);
			allStatus.put(m);
			m = null;
		}

		return allStatus;
	}

	@Override
	public void acknowledgeErroneousJob(int idToAcknowledge) {
		jobQueueService.acknowledgeErroneousJob(idToAcknowledge);
	}

	@Override
	public List<String> getErroneousImports() {
		return serverStatusDao.getErroneousImports(configService.getIntegerValue(ConfigValue.MaxUserImportDurationMinutes));
	}

	@Override
	public List<String> getErroneousExports() {
		return serverStatusDao.getErroneousExports();
	}

	@Override
	public Version getAvailableUpdateVersion(Version currentVersion) throws Exception {
		return getAvailableUpdateVersion(String.format("https://www.agnitas.de/download/openemm-version-%d.%02d/", currentVersion.getMajorVersion(), currentVersion.getMinorVersion()));
	}

	protected Version getAvailableUpdateVersion(String url) throws Exception {
		String versionData = HttpUtils.executeHttpRequest(url, null, null, configuredSecureTransportLayerProtocol());
		try (BufferedReader reader = new BufferedReader(new StringReader(versionData))) {
			String nextLine;
			while ((nextLine = reader.readLine()) != null) {
				if (nextLine.startsWith("emm:")) {
					String[] frontendLineParts = nextLine.substring(4).trim().split(" ");
					return new Version(frontendLineParts[0].trim());
				} else if (nextLine.startsWith("openemm:")) {
					String[] frontendLineParts = nextLine.substring(8).trim().split(" ");
					return new Version(frontendLineParts[0].trim());
				}
			}
			return null;
		}
	}

	private String configuredSecureTransportLayerProtocol() {
		return this.configService.getValue(ConfigValue.SecureTransportLayerProtocol);
	}

	@Override
	public String getDbVendor() {
		return serverStatusDao.getDbVendor();
	}
}
