/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.autoexport.bean;

import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;

public class AutoExport {
	public static final String INTERVALPATTERN_ONCE = "ONCE";

    public static String DEFAULT_EXPORT_FILENAME_PATTERN = "export_[CID]_[YYYY][MM][DD]-[HH][MI].csv";
    
    public static final int AFTER_MAILING_DELIVERY_MAILING_VALUE = 0;

	/**
	 * The Enum AutoExportType.
	 */
	public enum AutoExportType {
		Recipient("Recipient"),
		ReferenceTable("ReferenceTable"),
		Mailing("Mailing"),
		Reactions("Reactions"),
		ReactionsAndStatus("ReactionsAndStatus"),
		Blacklist("Blacklist");
		
		/**
		  * Instantiates a new AutoExportType.
		  *
		  * @param storageString the storageString
		  */
		AutoExportType(String storageString) {
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
		 * Gets the AutoExportType for a storageString.
		 *
		 * @return the AutoExportType
		 */
		public static AutoExportType getAutoExportTypeFromStorageString(String storageString) throws Exception {
			for (AutoExportType type : AutoExportType.values()) {
				if (type.getStorageString().equalsIgnoreCase(storageString)) {
					return type;
				}
			}
			throw new Exception("Invalid storage string for AutoExportType");
		}

		public boolean isRecipient(){
			return this == Recipient;
		}

		public boolean isReferenceTable(){
			return this == ReferenceTable;
		}

		public boolean isMailing(){
			return this == Mailing;
		}

		public boolean isReactions(){
			return this == Reactions;
		}

		public boolean isReactionsAndStatus(){
			return this == ReactionsAndStatus;
		}

		public boolean isBlacklist(){
			return this == Blacklist;
		}
	}
	
    private int autoExportId;
	private AutoExportType type = AutoExportType.Recipient;
    private int companyId;
    private int adminId;
    private int exportProfileId;
	private int exportCsvDescriptionID;
	private int exportReferenceTableID;
    private String shortname;
    private String description;
    private String filePath;
    private String fileNameWithPatterns;
    private String fileServer;
    private String privateKey;
	private boolean allowUnknownHostKeys = false;
    private boolean oneTime;
    private boolean executed;
    private boolean active;
    private Date autoActivationDate;
    private boolean deactivateByCampaign;
	private Date created;
	private Date changed;
	private Date laststart;
	private boolean running;
	private String lastresult;
	private String intervalpattern;
	private Date nextStart;
	private String lasthostname;
	private String emailForReport;
	private String emailOnError;
	private int mailingID;
	private boolean hidden;
	private String intervalAsJson;
	private String timeZone = "Europe/Berlin";
	private boolean considerLastRun;
	private int retryCount;
	private int maximumRetries = 1;
	private List<Integer> hoursAfterDelivery = null;
	
	private Date currentStart = null;

	// For AutoExportType.Reactions only.
	private List<String> additionalCustomerFields;

	private Locale locale = new Locale("en", "US");

	public int getMailingID() {
		return mailingID;
	}

	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	public int getAutoExportId() {
        return autoExportId;
    }

    public void setAutoExportId(int autoExportId) {
        this.autoExportId = autoExportId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public int getExportProfileId() {
        return exportProfileId;
    }

    public void setExportProfileId(int exportProfileId) {
        this.exportProfileId = exportProfileId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileNameWithPatterns() {
		return fileNameWithPatterns;
	}

	public void setFileNameWithPatterns(String fileNameWithPatterns) {
		this.fileNameWithPatterns = fileNameWithPatterns;
	}

	public String getFileServer() {
        return fileServer;
    }

	public String getFileServerWithoutCredentials() {
		if (StringUtils.isNotBlank(fileServer) && fileServer.contains("@")) {
			return StringEscapeUtils.escapeHtml4(fileServer.substring(fileServer.indexOf("@") + 1));
		} else {
			return StringEscapeUtils.escapeHtml4(fileServer);
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

    public boolean isExecuted() {
        return executed;
    }

    public void setExecuted(boolean executed) {
        this.executed = executed;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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
	
	public AutoExportType getType() {
		return type;
	}

	public void setType(AutoExportType type) {
		this.type = type;
	}
	
	public String getTypeString() {
		return type.getStorageString();
	}

	public void setTypeString(String typeString) throws Exception {
		this.type = AutoExportType.getAutoExportTypeFromStorageString(typeString);
	}

	public int getExportCsvDescriptionID() {
		return exportCsvDescriptionID;
	}

	public void setExportCsvDescriptionID(int exportCsvDescriptionID) {
		this.exportCsvDescriptionID = exportCsvDescriptionID;
	}

	public int getExportReferenceTableID() {
		return exportReferenceTableID;
	}

	public void setExportReferenceTableID(int exportReferenceTableID) {
		this.exportReferenceTableID = exportReferenceTableID;
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

	public boolean getRunning() {
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

	public String getEmailForReport() {
		return emailForReport;
	}

	public void setEmailForReport(String emailForReport) {
		this.emailForReport = emailForReport;
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

	public boolean isHidden() {
		return hidden;
	}

	public void setHidden(boolean hidden) {
		this.hidden = hidden;
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

	public List<String> getAdditionalCustomerFields() {
		return additionalCustomerFields;
	}

	public void setAdditionalCustomerFields(List<String> additionalCustomerFields) {
		this.additionalCustomerFields = additionalCustomerFields;
	}

	public boolean isConsiderLastRun() {
		return considerLastRun;
	}

	public void setConsiderLastRun(boolean considerLastRun) {
		this.considerLastRun = considerLastRun;
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

	public List<Integer> getHoursAfterDelivery() {
		return hoursAfterDelivery;
	}

	public void setHoursAfterDelivery(List<Integer> hoursAfterDelivery) {
		this.hoursAfterDelivery = hoursAfterDelivery;
	}

	public Date getCurrentStart() {
		return currentStart;
	}

	public void setCurrentStart(Date currentStart) {
		this.currentStart = currentStart;
	}

	public DateTimeFormatter getDateTimeFormatter() {
		return AgnUtils.getDateTimeFormatter(getTimeZone(), getLocale());
	}

	public void setLocale(Locale locale) {
		this.locale = locale;
	}

	public Locale getLocale() {
		return locale;
	}
}
