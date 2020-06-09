/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.agnitas.util.DateUtilities;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.agnitas.emm.core.birtreport.bean.BirtReportFactory;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;

/**
 * Class for BirtReports. Allows managing BirtReports with an easy interface.
 */
public class ComBirtReportImpl implements ComBirtReport {
	private static final Logger logger = Logger.getLogger(ComBirtReportImpl.class);
	
	@Autowired
	private BirtReportFactory factory;

	protected int reportID;
    protected int companyID;
    protected String shortname;
    protected String description;
    protected String emailSubject;
    protected String emailDescription;
    private int reportActive;
    private int reportType;
    private int format;
    private Date activationDate;
    private Date endDate;
    private int activeTab = 1;
    private boolean hidden = false;
    private Date changeDate;
    private List<String> emailRecipientList;
    private String intervalpattern;
    private Date nextStart;
    private String lastresult;

    private String language;

    private Map<ReportSettingsType, ComBirtReportSettings> settings;
    
    /**
     * To proper initializing ComBirtReportImpl use {@link BirtReportFactory#createReport()}
     */
    public ComBirtReportImpl(Map<ReportSettingsType, ComBirtReportSettings> settings){
        if (settings == null) {
            logger.warn("Added not all supported settings");
            settings = new HashMap<>();
        }
        this.settings = settings;
    }

    @Override
	public List<ComBirtReportSettings> getSettings() {
        return new ArrayList<>(settings.values());
    }

    @Override
    public void setSettings(Map<ReportSettingsType, ComBirtReportSettings> settings) {
        this.settings.putAll(settings);
    }

    @Override
    public ComBirtReportComparisonSettings getReportComparisonSettings() {
        return (ComBirtReportComparisonSettings) getSetting(ReportSettingsType.COMPARISON);
    }

    @Override
    public ComBirtReportMailingSettings getReportMailingSettings() {
        return (ComBirtReportMailingSettings) getSetting(ReportSettingsType.MAILING);
    }

    @Override
    public ComBirtReportRecipientSettings getReportRecipientSettings() {
        return (ComBirtReportRecipientSettings) getSetting(ReportSettingsType.RECIPIENT);
    }

    @Override
    public ComBirtReportTopDomainsSettings getReportTopDomainsSettings() {
        return (ComBirtReportTopDomainsSettings) getSetting(ReportSettingsType.TOP_DOMAIN);
    }

    @Override
	public void calculateSendDate() {
        if (isTriggeredByMailing()) {
            return;
        } else {
        	BirtReportType birtReportType = BirtReportType.getTypeByCode(getReportType());
	        if (birtReportType == BirtReportType.TYPE_MONTHLY_FIRST) {
	        	nextStart = DateUtilities.calculateNextJobStart(intervalpattern);
	        } else if (birtReportType == BirtReportType.TYPE_MONTHLY_15TH) {
	        	nextStart = DateUtilities.calculateNextJobStart(intervalpattern);
	        } else if (birtReportType == BirtReportType.TYPE_MONTHLY_LAST) {
	        	nextStart = DateUtilities.calculateNextJobStart(intervalpattern);
	        } else if (birtReportType == BirtReportType.TYPE_WEEKLY || birtReportType == BirtReportType.TYPE_BIWEEKLY) {
	        	nextStart = DateUtilities.calculateNextJobStart(intervalpattern);
	        } else {
	        	nextStart = DateUtilities.calculateNextJobStart(intervalpattern);
	        }
        }
    }

    @Override
	public int getId() {
        return reportID;
    }

    @Override
	public void setId(int id) {
        this.reportID = id;
    }

    @Override
	public int getCompanyID() {
        return companyID;
    }

    @Override
	public void setCompanyID(int companyID) {
        this.companyID = companyID;
    }

    @Override
	public String getShortname() {
        return shortname;
    }

    @Override
	public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    @Override
	public String getDescription() {
        return description;
    }

    @Override
	public void setDescription(String description) {
        this.description = description;
    }

    @Override
	public String getEmailSubject() {
        return emailSubject;
    }

    @Override
	public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }

    @Override
	public String getEmailDescription() {
        return emailDescription;
    }

    @Override
	public void setEmailDescription(String emailDescription) {
        this.emailDescription = emailDescription;
    }

    @Override
	public int isReportActive() {
        return reportActive;
    }

    @Override
	public void setReportActive(int reportActive) {
        this.reportActive = reportActive;
    }

    @Override
	public int getReportType() {
        return reportType;
    }

    @Override
	public void setReportType(int reportType) {
        this.reportType = reportType;
    }

    @Override
	public int getFormat() {
        return format;
    }

    @Override
	public void setFormat(int format) {
        this.format = format;
    }

    @Override
    public String getFormatName() {
        return (format == FORMAT_CSV_INDEX) ? FORMAT_CSV : FORMAT_PDF;
    }

    @Override
    public Date getActivationDate() {
        return activationDate;
    }

    @Override
    public void setActivationDate(Date activationDate) {
        this.activationDate = activationDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Override
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Override
    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
    }

    @Override
    public int getActiveTab() {
        return activeTab;
    }

    @Override
    public boolean isTriggeredByMailing() {
        BirtReportType typeByCode = BirtReportType.getTypeByCode(reportType);
        
        return BirtReportType.TYPE_AFTER_MAILING_24HOURS == typeByCode ||
                BirtReportType.TYPE_AFTER_MAILING_48HOURS == typeByCode ||
                BirtReportType.TYPE_AFTER_MAILING_WEEK == typeByCode;
    }

    @Override
    public ComBirtReportSettings getActiveReportSetting() {
        Optional<ComBirtReportSettings> first = settings.values().stream()
                .filter(s -> s.getReportSettingsType().getKey() == activeTab)
                .findFirst();
    
        return first.orElseGet(() -> getSetting(ReportSettingsType.COMPARISON));
    
    }

    @Override
    public void setLanguage(final String language) {
    	this.language = language;
    }

    @Override
    public String getLanguage() {
    	return language;
    }

    @Override
    public void setHidden(boolean isHidden) {
        hidden = isHidden;
    }

    @Override
    public boolean isHidden() {
        return hidden;
    }

    @Override
    public Date getChangeDate() {
        return changeDate;
    }

    @Override
    public void setChangeDate(Date changeDate) {
        this.changeDate = changeDate;
    }
    
    @Override
    public void setSettingParameter(ReportSettingsType type, String name, Object value) {
        ComBirtReportSettings comBirtReportSettings = settings.get(type);
        if (comBirtReportSettings != null) {
            comBirtReportSettings.setReportSetting(name, value);
        }
    }
    
    @Override
    public ComBirtReportSettings getSetting(ReportSettingsType type) {
        return settings.computeIfAbsent(type, t -> factory.createReportSettings(type));
    }

	@Override
	public void setEmailRecipientList(List<String> emailRecipientList) {
		this.emailRecipientList = emailRecipientList;
	}

	@Override
	public List<String> getEmailRecipientList() {
		return emailRecipientList;
	}

	@Override
	public void setIntervalpattern(String intervalpattern) {
		this.intervalpattern = intervalpattern;
	}

	@Override
	public String getIntervalpattern() {
		return intervalpattern;
	}

	@Override
	public void setNextStart(Date nextStart) {
		this.nextStart = nextStart;
	}

	@Override
	public Date getNextStart() {
		return nextStart;
	}

	@Override
	public void setLastresult(String lastresult) {
		this.lastresult = lastresult;
	}

	@Override
	public String getLastresult() {
		return lastresult;
	}
}
