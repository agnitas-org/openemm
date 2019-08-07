/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportComparisonSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportRecipientSettings;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;

/**
 * Interface for BirtReports. Allows managing BirtReports with an easy interface.
 */
public interface ComBirtReport extends Serializable {

    int FORMAT_PDF_INDEX = 0;
    int FORMAT_CSV_INDEX = 1;

    String FORMAT_PDF = "pdf";
    String FORMAT_CSV = "csv";

    int MONDAY = 0x01;
    int TUESDAY = 0x02;
    int WEDNESDAY = 0x04;
    int THURSDAY = 0x08;
    int FRIDAY = 0x10;
    int SATURDAY = 0x20;
    int SUNDAY = 0x40;

    int MAILINGS = 0;
    int MAILINGLIST = 1;
    int TARGETGROUP = 2;


    ComBirtReportComparisonSettings getReportComparisonSettings();
    void setReportComparisonSettings(ComBirtReportComparisonSettings reportComparisonSettings);

    ComBirtReportMailingSettings getReportMailingSettings();
    void setReportMailingSettings(ComBirtReportMailingSettings reportMailingSettings);

    ComBirtReportRecipientSettings getReportRecipientSettings();
    void setReportRecipientSettings(ComBirtReportRecipientSettings reportRecipientSettings);

    /**
     * Check if the report should be sent on the given day.
     * @param day Check whether the report should be sent on this day.
     * @return true if report should be send on that day, false otherwise.
     */
    boolean isSend(int day);

    /**
     * Enable/Disable the report for the given day.
     * @param day Set the value for this day.
     * @param send Send the report on the given day if true,
     *             otherwise don't send.
     */
    void setSend(int day, boolean send);
    void parseSendDays(String input);

    String buildSendDate();
    void calculateSendDate();

    int getId();
    void setId(int reportID);

    int getCompanyID();
    void setCompanyID(int companyID);

    String getShortname();
    void setShortname(String shortname);

    String getDescription();
    void setDescription(String description);

    String getSendEmail();
    void setSendEmail(String sendEmail);

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

    Date getSendDate();
    void setSendDate(Date sendDate);

    Date getSendTime();
    void setSendTime(Date sendTime);

    int getSendDays();
    void setSendDays(int sendDays);

    List<ComBirtReportSettings> getSettings();

    Date getActivationDate();
    void setActivationDate(Date activationDate);

    Date getEndDate();
    void setEndDate(Date endDate);

    boolean isEnabled();

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

    /**
     * @return Last time when this report was generated and delivered
     */
    Date getDeliveryDate();

    void setDeliveryDate(Date deliveryDate);
}
