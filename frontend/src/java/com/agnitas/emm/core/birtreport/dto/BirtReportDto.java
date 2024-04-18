/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.dto;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class BirtReportDto {
    
    private static final int DEFAULT_ACTIVE_TAB = ReportSettingsType.COMPARISON.getKey();
    
    private int id;
    private String shortname;
    private String description;
    private int type;
    
    private int activeTab = DEFAULT_ACTIVE_TAB;
    
    private String emailSubject;
    private String emailDescription;
    
    private Date endDate;
    private Map<ReportSettingsType, Map<String, Object>> settings = new HashMap<>();
    
    private int format;
    
    private List<String> emailRecipientList;
    private String intervalpattern;
    private Date nextStart;
    private String lastresult;

    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public Date getEndDate() {
        return endDate;
    }
    
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
    
    public String getShortname() {
        return shortname;
    }
    
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getActiveTab() {
        return activeTab;
    }
    
    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
    }
    
    public Map<ReportSettingsType, Map<String, Object>> getSettings() {
        return getSettings(true);
    }
    
    public Map<ReportSettingsType, Map<String, Object>> getSettings(boolean isMailTrackingEnabled) {
        if (isMailTrackingEnabled) {
            return settings;
        }
    
        return settings.entrySet().stream()
                .filter(typeEntry -> !typeEntry.getKey().isMailTrackingRequired())
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
    }
    
    public void setSettings(Map<ReportSettingsType, Map<String, Object>> settings) {
        this.settings = settings;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }
    
    public String getEmailSubject() {
        return emailSubject;
    }
    
    public void setEmailDescription(String emailDescription) {
        this.emailDescription = emailDescription;
    }
    
    public String getEmailDescription() {
        return emailDescription;
    }
    
    public int getFormat() {
        return format;
    }
    
    public void setFormat(int format) {
        this.format = format;
    }

	public void setEmailRecipientList(List<String> emailRecipientList) {
		this.emailRecipientList = emailRecipientList;
	}

	public List<String> getEmailRecipientList() {
		return emailRecipientList;
	}

	public void setIntervalpattern(String intervalpattern) {
		this.intervalpattern = intervalpattern;
	}

	public String getIntervalpattern() {
		return intervalpattern;
	}

	public void setNextStart(Date nextStart) {
		this.nextStart = nextStart;
	}

	public Date getNextStart() {
		return nextStart;
	}

	public void setLastresult(String lastresult) {
		this.lastresult = lastresult;
	}

	public String getLastresult() {
		return lastresult;
	}

}
