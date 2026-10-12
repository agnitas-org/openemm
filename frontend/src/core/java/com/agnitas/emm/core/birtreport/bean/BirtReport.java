/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.bean;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.birtreport.bean.impl.BirtReportComparisonSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportRecipientSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportTopDomainsSettings;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;

/**
 * Interface for BirtReports. Allows managing BirtReports with an easy interface.
 */
public interface BirtReport {

    BirtReportComparisonSettings getReportComparisonSettings();
    BirtReportMailingSettings getReportMailingSettings();
    BirtReportRecipientSettings getReportRecipientSettings();
    BirtReportTopDomainsSettings getReportTopDomainsSettings();

    void calculateSendDate();

    int getId();
    void setId(int reportID);

    int getCompanyID();
    void setCompanyID(int companyID);

    String getShortname();
    void setShortname(String shortname);

    String getDescription();
    void setDescription(String description);

    String getEmailSubject();
    void setEmailSubject(String emailSubject);

    String getEmailDescription();
    void setEmailDescription(String emailDescription);

    int isReportActive();
    void setReportActive(int reportActive);

    int getReportType();
    void setReportType(int reportType);

    ReportFormat getFormat();
    void setFormat(ReportFormat format);

    List<BirtReportSettings> getSettings();
    void setSettings(Map<ReportSettingsType, BirtReportSettings> settings);

    Date getActivationDate();
    void setActivationDate(Date activationDate);

    Date getEndDate();
    void setEndDate(Date endDate);

    void setActiveTab(int activeTab);
    int getActiveTab();

    boolean isTriggeredByMailing();
    BirtReportSettings getActiveReportSetting();
    
    void setLanguage(final String language);
    String getLanguage();

    void setHidden(boolean isHidden);
    boolean isHidden();

    Date getChangeDate();
    void setChangeDate(Date changeDate);
    
    void setSettingParameter(ReportSettingsType type, String name, Object value);
    
    BirtReportSettings getSetting(ReportSettingsType type);

    void setEmailRecipientList(List<String> emailRecipientList);
    List<String> getEmailRecipientList();
    
	void setIntervalpattern(String intervalpattern);
	String getIntervalpattern();
	
	void setNextStart(Date nextStart);
	Date getNextStart();
	
	void setLastresult(String lastresult);
	String getLastresult();
}
