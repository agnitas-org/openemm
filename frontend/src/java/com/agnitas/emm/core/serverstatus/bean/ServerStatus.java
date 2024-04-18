/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.bean;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.commons.util.DateUtil;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ServerStatus {
    private String licenseName;
    private String hostName;
    private String version;
    private String javaVersion;
    private String tempDir;
    private String sysTime;
    private String sysTimeDb;
    private String buildTime;
    private String startupTime;
    private String configExpirationTime;
    private String installPath;
    private String uptime;
    private boolean overallStatus;
    private boolean jobQueueStatus;
    private boolean importStatus;
    private boolean exportStatus;
    private boolean dbStatus;
    private boolean reportStatus;
    private boolean licenseStatusOK;
    private String dbType;
    private String dbUrl;
    private String dbVersion;
    private boolean dbConnectStatus;
    private int diskSpaceFreePercentage;
    private List<VersionStatus> dbVersionStatuses;

    public String getLicenseName() {
        return licenseName;
    }
    
    public String getHostName() {
        return hostName;
    }
    
    public String getVersion() {
        return version;
    }
    
    public String getJavaVersion() {
        return javaVersion;
    }
    
    public String getTempDir() {
        return tempDir;
    }
    
    public String getSysTime() {
        return sysTime;
    }
    
    public String getSysTimeDb() {
        return sysTimeDb;
    }
    
    public String getBuildTime() {
        return buildTime;
    }
    
    public String getStartupTime() {
        return startupTime;
    }
    
    public String getConfigExpirationTime() {
        return configExpirationTime;
    }
    
    public String getInstallPath() {
        return installPath;
    }
    
    public String getUptime() {
        return uptime;
    }
    
    public boolean isOverallStatus() {
        return overallStatus;
    }
    
    public boolean isJobQueueStatus() {
        return jobQueueStatus;
    }
    
    public boolean isImportStatus() {
        return importStatus;
    }
    
    public boolean isExportStatus() {
        return exportStatus;
    }
    
    public boolean isDbStatus() {
        return dbStatus;
    }
    
    public boolean isReportStatus() {
        return reportStatus;
    }
    
    public boolean isLicenseStatusOK() {
        return licenseStatusOK;
    }
    
    public String getDbType() {
        return dbType;
    }
    
    public String getDbUrl() {
        return dbUrl;
    }
    
    public String getDbVersion() {
        return dbVersion;
    }
    
    public boolean isDbConnectStatus() {
        return dbConnectStatus;
    }
    
    public List<VersionStatus> getDbVersionStatuses() {
        return dbVersionStatuses;
    }

    public int getDiskSpaceFreePercentage() {
        return diskSpaceFreePercentage;
    }

    public static ServerStatus.StatusBuilder builder(String version, String installPath, Locale locale, final ConfigService configService) {
        return new ServerStatus.StatusBuilder(version, installPath, locale, configService);
    }
    
    public static class StatusBuilder {
        private static final Logger logger = LogManager.getLogger(StatusBuilder.class);

        private String version;
        private SimpleDateFormat dateFormat;
        private Date expirationTime;
        private Date startupTime;
        private String installPath;
        private Locale locale;
        private boolean overallStatus;
        private boolean jobQueueStatus;
        private boolean importStatus;
        private boolean exportStatus;
        private boolean dbStatus;
        private boolean reportStatus;
        private boolean licenseStatusOK;
        private List<VersionStatus> dbVersionStatuses = new ArrayList<>();
        private String dbType;
        private String dbUrl;
        private String dbVersion;
        private boolean dbConnectStatus;
        private int diskSpaceFreePercentage;
        private final ConfigService configService;
    
        public StatusBuilder(String version, String installPath, Locale locale, final ConfigService configService) {
            this.version = version;
            this.installPath = installPath;
            this.locale = locale;
            this.configService = Objects.requireNonNull(configService);
        }
        
        public ServerStatus.StatusBuilder dateTimeSettings(SimpleDateFormat dateFormatToUse, Date startupTimeToUse, Date expirationTimeToUse) {
            this.dateFormat = dateFormatToUse;
            this.startupTime = startupTimeToUse;
            this.expirationTime = expirationTimeToUse;
            return this;
        }
        
        public ServerStatus.StatusBuilder statuses(boolean overallStatusToUse, boolean jobQueueStatusToUse, boolean importStatusToUse, boolean exportStatusToUse, boolean dbStatusToUse, boolean reportStatusToUse, boolean licenseStatusOK) {
            this.overallStatus = overallStatusToUse;
            this.jobQueueStatus = jobQueueStatusToUse;
            this.importStatus = importStatusToUse;
            this.exportStatus = exportStatusToUse;
            this.dbStatus = dbStatusToUse;
            this.reportStatus = reportStatusToUse;
            this.licenseStatusOK = licenseStatusOK;
            return this;
        }

        public ServerStatus.StatusBuilder database(String dbTypeToUse, String dbUrlToUse, String dbVersionToUse, boolean dbConnectStatusToUse) {
            this.dbType = dbTypeToUse;
            this.dbUrl = dbUrlToUse;
            this.dbVersion = dbVersionToUse;
            this.dbConnectStatus = dbConnectStatusToUse;
            return this;
        }
        
        public ServerStatus.StatusBuilder databaseConnection(boolean dbConnectStatusToUse) {
            this.dbConnectStatus = dbConnectStatusToUse;
            return this;
        }
        
        public ServerStatus.StatusBuilder dbVersionStatuses(List<VersionStatus> versionStatuses) {
            this.dbVersionStatuses.addAll(versionStatuses);
            return this;
        }

        public ServerStatus.StatusBuilder diskSpaceFreePercentage(int percentage) {
            this.diskSpaceFreePercentage = percentage;
            return this;
        }

        public ServerStatus build() {
            Date now = new Date();
            ServerStatus serverStatus = new ServerStatus();
            if (dateFormat == null) {
                logger.warn("date format was not specified");
                dateFormat = new SimpleDateFormat(DateUtilities.DD_MM_YYYY_HH_MM_SS);
            }
    
            if (locale == null) {
                logger.warn("locale was not specified");
                locale = Locale.ENGLISH;
            }
            
            serverStatus.licenseName = "\"" + configService.getValue(ConfigValue.System_License_Holder) + "\" (ID: " + configService.getValue(ConfigValue.System_Licence) + ", Type: " + configService.getValue(ConfigValue.System_License_Type) + ")";
            
            serverStatus.hostName = AgnUtils.getHostName();
            
            serverStatus.version = version;
            serverStatus.javaVersion = AgnUtils.getJavaVersion();
            serverStatus.tempDir = AgnUtils.getTempDir();
            serverStatus.installPath = installPath;
    
            serverStatus.buildTime = dateFormat.format(ConfigService.getBuildTime());
            serverStatus.startupTime = dateFormat.format(startupTime);
            serverStatus.sysTime = dateFormat.format(now);
            serverStatus.sysTimeDb = dateFormat.format(configService.getCurrentDbTime());
            serverStatus.uptime = startupTime == null ? "0" : DateUtil.getTimespanString(now.getTime() - startupTime.getTime(), locale);
            serverStatus.configExpirationTime = expirationTime == null ? "Null" : dateFormat.format(expirationTime);
    
            
            serverStatus.overallStatus = overallStatus;
            serverStatus.jobQueueStatus = jobQueueStatus;
            serverStatus.importStatus = importStatus;
            serverStatus.exportStatus = exportStatus;
            serverStatus.dbStatus = dbStatus;
            serverStatus.reportStatus = reportStatus;
            serverStatus.licenseStatusOK = licenseStatusOK;
            
            serverStatus.dbType = dbType;
            serverStatus.dbUrl = dbUrl;
            serverStatus.dbVersion = dbVersion;
            serverStatus.dbConnectStatus = dbConnectStatus;
            
            serverStatus.dbVersionStatuses = dbVersionStatuses;
            serverStatus.diskSpaceFreePercentage = diskSpaceFreePercentage;
            return serverStatus;
        }
    }
    
    public static ServerStatus.ExternalStatusBuilder externalStatusBuilder() {
        return new ServerStatus.ExternalStatusBuilder();
    }
    
    
    public static class ExternalStatusBuilder {

        private boolean overallStatus;
        private boolean jobQueueStatus;
        private boolean importStatus;
        private boolean exportStatus;
        private boolean dbStatus;
        private boolean reportStatus;
        private boolean licenseStatusOK;
        private boolean dbConnectStatus;

        public ServerStatus.ExternalStatusBuilder statuses(boolean overallStatusToUse, boolean jobQueueStatusToUse, boolean importStatusToUse, boolean exportStatusToUse, boolean dbStatusToUse, boolean reportStatusToUse, boolean licenseStatusOK, boolean dbConnectStatusToUse) {
            this.overallStatus = overallStatusToUse;
            this.jobQueueStatus = jobQueueStatusToUse;
            this.importStatus = importStatusToUse;
            this.exportStatus = exportStatusToUse;
            this.dbStatus = dbStatusToUse;
            this.reportStatus = reportStatusToUse;
            this.licenseStatusOK = licenseStatusOK;
            this.dbConnectStatus = dbConnectStatusToUse;
            return this;
        }

        public ServerStatus externalStatusBuilder() {
        	
            ServerStatus serverStatus = new ServerStatus();
    
            serverStatus.overallStatus = overallStatus;
            serverStatus.jobQueueStatus = jobQueueStatus;
            serverStatus.importStatus = importStatus;
            serverStatus.exportStatus = exportStatus;
            serverStatus.dbStatus = dbStatus;
            serverStatus.reportStatus = reportStatus;
            serverStatus.licenseStatusOK = licenseStatusOK;
            serverStatus.dbConnectStatus = dbConnectStatus;
            
            return serverStatus;
        }
    }


}
