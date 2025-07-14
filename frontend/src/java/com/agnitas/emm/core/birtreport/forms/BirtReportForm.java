/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.forms;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;
import com.agnitas.web.forms.FormDate;
import com.agnitas.web.forms.FormTime;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

public class BirtReportForm {
    private int reportId;
    private String shortname;
    private String description;
    private String emailAddresses;
    private String emailSubject;
    private String emailDescription;
    private int type;
    private int afterMailing;
    private int reportMonthly;
    private Map<Integer, Boolean> sendWeekDays = new HashMap<>();
    
    private int format;

    private FormTime sendDate = new FormTime();
    private FormDate endDate = new FormDate();
    
    //statistic report type settings
    private int activeTab = ReportSettingsType.COMPARISON.getKey();
    
    private Map<ReportSettingsType, Map<String, Object>> settings = new HashMap<>();
    
    public int getReportId() {
        return reportId;
    }
    
    public void setReportId(int reportId) {
        this.reportId = reportId;
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
    
    public String getEmailAddresses() {
        return StringUtils.trimToNull(emailAddresses);
    }
    
    public void setEmailAddresses(String emailAddresses) {
        this.emailAddresses = emailAddresses;
    }
    
    public String getEmailSubject() {
        return emailSubject;
    }
    
    public void setEmailSubject(String emailSubject) {
        this.emailSubject = emailSubject;
    }
    
    public String getEmailDescription() {
        return emailDescription;
    }
    
    public void setEmailDescription(String emailDescription) {
        this.emailDescription = emailDescription;
    }
    
    public int getType() {
        return type;
    }
    
    public void setType(int type) {
        this.type = type;
    }
    
    public int getAfterMailing() {
        return afterMailing;
    }
    
    public void setAfterMailing(int afterMailing) {
        this.afterMailing = afterMailing;
    }
    
    public int getReportMonthly() {
        return reportMonthly;
    }
    
    public void setReportMonthly(int reportMonthly) {
        this.reportMonthly = reportMonthly;
    }
    
    public Map<Integer, Boolean> getSendWeekDays() {
        return sendWeekDays;
    }
    
    public void setSendWeekDays(Map<Integer, Boolean> sendWeekDays) {
        this.sendWeekDays = sendWeekDays;
    }
    
    public int getFormat() {
        return format;
    }
    
    public void setFormat(int format) {
        this.format = format;
    }
    
    public FormTime getSendDate() {
        return sendDate;
    }
    
    public void setSendDate(FormTime sendDate) {
        this.sendDate = sendDate;
    }
    
    public FormDate getEndDate() {
        return endDate;
    }
    
    public int getActiveTab() {
        return activeTab;
    }
    
    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
    }
    
    public Map<ReportSettingsType, Map<String, Object>> getSettings() {
        return settings;
    }
    
    public Map<String, Object> getSettingsByType(ReportSettingsType typeParam) {
        return settings.getOrDefault(typeParam, Collections.emptyMap());
    }
    
    public void setSettings(Map<ReportSettingsType, Map<String, Object>> settings) {
        this.settings = settings;
    }
    
    public boolean daysIsChecked() {
        for(Map.Entry<Integer, Boolean> pair: sendWeekDays.entrySet()) {
            if(BooleanUtils.isTrue(pair.getValue())) {
                return true;
            }
        }
        
        return false;
    }
}
