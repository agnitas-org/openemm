/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportComparisonSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportTopDomainsSettings;
import com.agnitas.emm.core.birtreport.dto.ReportSettingsType;

/**
 * Interface for BirtReports. Allows managing BirtReports with an easy interface.
 */
public interface ComBirtReport {

    int FORMAT_PDF_INDEX = 0;
    int FORMAT_CSV_INDEX = 1;

    String FORMAT_PDF = "pdf";
    String FORMAT_CSV = "csv";

    int MAILINGS = 0;
    int MAILINGLIST = 1;
    int TARGETGROUP = 2;

    ComBirtReportComparisonSettings getReportComparisonSettings();
    ComBirtReportMailingSettings getReportMailingSettings();
    ComBirtReportRecipientSettings getReportRecipientSettings();
    ComBirtReportTopDomainsSettings getReportTopDomainsSettings();

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

    int getFormat();
    void setFormat(int format);

    String getFormatName();

    List<ComBirtReportSettings> getSettings();
    void setSettings(Map<ReportSettingsType, ComBirtReportSettings> settings);

    Date getActivationDate();
    void setActivationDate(Date activationDate);

    Date getEndDate();
    void setEndDate(Date endDate);

    void setActiveTab(int activeTab);
    int getActiveTab();

    boolean isTriggeredByMailing();
    ComBirtReportSettings getActiveReportSetting();
    
    void setLanguage(final String language);
    String getLanguage();

    void setHidden(boolean isHidden);
    boolean isHidden();

    Date getChangeDate();
    void setChangeDate(Date changeDate);
    
    void setSettingParameter(ReportSettingsType type, String name, Object value);
    
    ComBirtReportSettings getSetting(ReportSettingsType type);

    void setEmailRecipientList(List<String> emailRecipientList);
    List<String> getEmailRecipientList();
    
	void setIntervalpattern(String intervalpattern);
	String getIntervalpattern();
	
	void setNextStart(Date nextStart);
	Date getNextStart();
	
	void setLastresult(String lastresult);
	String getLastresult();
}
