/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoimport.bean;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.StringUtils;

public class AutoImport extends AutoImportLight {
	public static final String INTERVALPATTERN_ONCE = "ONCE";

	/**
	 * The Enum AutoImportType.
	 */
	public enum AutoImportType {
		Recipient("Recipient"),
		ReferenceTable("ReferenceTable");
		
		/**
		  * Instantiates a new AutoImportType.
		  *
		  * @param storageString the storageString
		  */
		AutoImportType(String storageString) {
			this.storageString = storageString;
		}
		
		/** The storageString */
		private final String storageString;
		
		/**
		 * Gets the storageString
		 *
		 * @return the storageString
		 */
		public String getStorageString() {
			return storageString;
		}
		
		/**
		 * Gets the AutoImportType for a storageString.
		 *
		 * @return the AutoImportType
		 */
		public static AutoImportType getAutoImportTypeFromStorageString(String storageString) throws Exception {
			for (AutoImportType type : AutoImportType.values()) {
				if (type.getStorageString().equalsIgnoreCase(storageString)) {
					return type;
				}
			}
			throw new Exception("Invalid storage string for AutoImportType");
		}
	}
	
	private AutoImportType type = AutoImportType.Recipient;
	private int adminId;
	private int importProfileId;
	private int importCsvDescriptionID;
	private int importReferenceTableID;
	private String description;
	private String filePath;
	private String fileServer;
    private String privateKey;
	private boolean allowUnknownHostKeys = false;
	private boolean oneTime;
	private boolean autoResume;
	private boolean executed;
	private Date autoActivationDate;
	private boolean deactivateByCampaign;
	private Date created;
	private Date changed;
	private Date laststart;
	private boolean running;
	private String lastresult;
	private int lastresultID;
	private boolean error;
	private String intervalpattern;
	private String intervalAsJson;
	private Date nextStart;
	private String lasthostname;
	private String emailOnError;
	private boolean alwaysImportFile = false;
	private boolean importMultipleFiles = false;
	private String timeZone;

	private List<Integer> mailinglists = new ArrayList<>();
	
	private int retryCount;
	private int maximumRetries;
	
	private boolean campaignDriven;
	
	public int getAdminId() {
		return adminId;
	}

	public void setAdminId(int adminId) {
		this.adminId = adminId;
	}

	public int getImportProfileId() {
		return importProfileId;
	}

	public void setImportProfileId(int importProfileId) {
		this.importProfileId = importProfileId;
	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public String getFileServer() {
		return fileServer;
	}

	public String getFileServerWithoutCredentials() {
		if (StringUtils.isNotBlank(fileServer) && fileServer.contains("@")) {
			return fileServer.substring(fileServer.indexOf("@") + 1);
		} else {
			return fileServer;
		}
	}

	public void setFileServer(String fileServer) {
		this.fileServer = fileServer;
	}

    public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public boolean isOneTime() {
		return oneTime;
	}

	public void setOneTime(boolean oneTime) {
		this.oneTime = oneTime;
	}

	public boolean isAutoResume() {
		return autoResume;
	}

	public void setAutoResume(boolean autoResume) {
		this.autoResume = autoResume;
	}

	public boolean isExecuted() {
		return executed;
	}

	public void setExecuted(boolean executed) {
		this.executed = executed;
	}

	public List<Integer> getMailinglists() {
		return mailinglists;
	}

	public void setMailinglists(List<Integer> mailinglists) {
		this.mailinglists = mailinglists;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Date getAutoActivationDate() {
		return autoActivationDate;
	}

	public void setAutoActivationDate(Date autoActivationDate) {
		this.autoActivationDate = autoActivationDate;
	}

	public boolean isDeactivateByCampaign() {
		return deactivateByCampaign;
	}

	public void setDeactivateByCampaign(boolean deactivateByCampaign) {
		this.deactivateByCampaign = deactivateByCampaign;
	}
	
	public AutoImportType getType() {
		return type;
	}

	public void setType(AutoImportType type) {
		this.type = type;
	}
	
	public String getTypeString() {
		return type.getStorageString();
	}

	public void setTypeString(String typeString) throws Exception {
		this.type = AutoImportType.getAutoImportTypeFromStorageString(typeString);
	}

	public int getImportCsvDescriptionID() {
		return importCsvDescriptionID;
	}

	public void setImportCsvDescriptionID(int importCsvDescriptionID) {
		this.importCsvDescriptionID = importCsvDescriptionID;
	}

	public int getImportReferenceTableID() {
		return importReferenceTableID;
	}

	public void setImportReferenceTableID(int importReferenceTableID) {
		this.importReferenceTableID = importReferenceTableID;
	}

	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}

	public Date getChanged() {
		return changed;
	}

	public void setChanged(Date changed) {
		this.changed = changed;
	}

	public Date getLaststart() {
		return laststart;
	}

	public void setLaststart(Date laststart) {
		this.laststart = laststart;
	}

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}

	public String getLastresult() {
		return lastresult;
	}

	public void setLastresult(String lastresult) {
		this.lastresult = lastresult;
	}

	public int getLastresultID() {
		return lastresultID;
	}

	public void setLastresultID(int lastresultID) {
		this.lastresultID = lastresultID;
	}

	public void setIsError(boolean error) {
		this.error = error;
	}

	public boolean isError() {
		return error;
	}

	public String getIntervalpattern() {
		return intervalpattern;
	}

	public void setIntervalpattern(String intervalpattern) {
		this.intervalpattern = intervalpattern;
	}

	public Date getNextStart() {
		return nextStart;
	}

	public void setNextStart(Date nextStart) {
		this.nextStart = nextStart;
	}

	public String getLasthostname() {
		return lasthostname;
	}

	public void setLasthostname(String lasthostname) {
		this.lasthostname = lasthostname;
	}

	public String getEmailOnError() {
		return emailOnError;
	}

	public void setEmailOnError(String emailOnError) {
		this.emailOnError = emailOnError;
	}

	public boolean isAllowUnknownHostKeys() {
		return allowUnknownHostKeys;
	}

	public void setAllowUnknownHostKeys(boolean allowUnknownHostKeys) {
		this.allowUnknownHostKeys = allowUnknownHostKeys;
	}

	public boolean isAlwaysImportFile() {
		return alwaysImportFile;
	}

	public void setAlwaysImportFile(boolean alwaysImportFile) {
		this.alwaysImportFile = alwaysImportFile;
	}

	/**
	 * Inverted property to alwaysImportFile
	 */
	public boolean isOnlyImportNewFiles() {
		return !alwaysImportFile;
	}
	/**
	 * Inverted property to alwaysImportFile
	 */
	public void setOnlyImportNewFiles(boolean onlyImportNewFiles) {
		this.alwaysImportFile = !onlyImportNewFiles;
	}

	public void setImportMultipleFiles(boolean importMultipleFiles) {
		this.importMultipleFiles = importMultipleFiles;
	}

	public boolean isImportMultipleFiles() {
		return importMultipleFiles;
	}
	
	public int getRetryCount() {
		return retryCount;
	}

	public void setRetryCount(int retryCount) {
		this.retryCount = retryCount;
	}

	public int getMaximumRetries() {
		return maximumRetries;
	}

	public void setMaximumRetries(int maximumRetries) {
		this.maximumRetries = maximumRetries;
	}

	public String getIntervalAsJson() {
		return intervalAsJson;
	}

	public void setIntervalAsJson(String intervalAsJson) {
		this.intervalAsJson = intervalAsJson;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
	
	public void setCampaignDriven(boolean isCampaignDriven) {
		campaignDriven = isCampaignDriven;
	}
	
	public boolean isCampaignDriven() {
		return campaignDriven;
	}

	public static class UsedFile {
		private String localFileName;
		private String remoteFileName;
		private long fileSize;
		private Date fileDate;
		private Date importDate;
		private long downloadDurationMillis;

		public String getLocalFileName() {
			return localFileName;
		}

		public void setLocalFileName(String localFileName) {
			this.localFileName = localFileName;
		}
		
		public String getRemoteFileName() {
			return remoteFileName;
		}

		public void setRemoteFileName(String remoteFileName) {
			this.remoteFileName = remoteFileName;
		}

		public long getFileSize() {
			return fileSize;
		}

		public void setFileSize(long fileSize) {
			this.fileSize = fileSize;
		}

		public Date getFileDate() {
			return fileDate;
		}

		public void setFileDate(Date fileDate) {
			this.fileDate = fileDate;
		}

		public Date getImportDate() {
			return importDate;
		}

		public void setImportDate(Date importDate) {
			this.importDate = importDate;
		}

		public long getDownloadDurationMillis() {
			return downloadDurationMillis;
		}

		public void setDownloadDurationMillis(long downloadDurationMillis) {
			this.downloadDurationMillis = downloadDurationMillis;
		}
	}

	@Override
	public String toString() {
		return "\"" + getShortname() + "\" (Type: " + type.toString() + " ID: " + getAutoImportId() + ")";
	}
}
