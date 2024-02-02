/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.form;

import com.agnitas.beans.DeliveryStat;
import com.agnitas.emm.core.mailing.forms.MailingIntervalSettingsForm;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.StringUtils;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MailingSendForm extends PaginationForm {

    private int mailingID;
    private int workflowId;
    private boolean isMailingGrid;
    private boolean hasDeletedTargetGroups;
    private boolean isTemplate;
    private int mailingtype;
    private String shortname;
    private boolean isMailingUndoAvailable;
    private boolean worldMailingSend;
    private DeliveryStat deliveryStat;
    private int sendHour;
    private int sendMinute;
    private String sendTime;
    private boolean isPrioritizationDisallowed;
    private boolean isEncryptedSend;
    private boolean statusmailOnErrorOnly;
    private String sendDate;
    private String maxRecipients = "0";
    private int generationOptimization;
    private int blocksize;
    private int stepping;
    private boolean checkForDuplicateRecords;
    private boolean skipWithEmptyTextContent;
    private boolean reportSendAfter24h;
    private boolean reportSendAfter48h;
    private boolean reportSendAfter1Week;
    private String reportSendEmail;
    private int autoImportId;
    private String statusmailRecipients = "";
    private String workStatus = "";
    private String[] filterTypes;
    private int offlineHtmlEmailsCount;
    private int totalSentCount;
    private int htmlEmailsCount;
    private int textEmailsCount;
    private Map<Integer, Integer> sentStatistics = new HashMap<>();
    private int templateId;
    private boolean isActivateAgainToday;
    private Date date;
    private MailingIntervalSettingsForm intervalSettings;
    private SecurityAndNotificationsSettingsForm securitySettings;

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public boolean isIsMailingGrid() {
        return isMailingGrid;
    }

    public void setMailingGrid(boolean mailingGrid) {
        isMailingGrid = mailingGrid;
    }

    public int getMailingID() {
        return mailingID;
    }

    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    public void setHasDeletedTargetGroups(boolean hasDeletedTargetGroups) {
        this.hasDeletedTargetGroups = hasDeletedTargetGroups;
    }

    public boolean getHasDeletedTargetGroups() {
        return hasDeletedTargetGroups;
    }

    public boolean isIsTemplate() {
        return isTemplate;
    }

    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    public int getMailingtype() {
        return mailingtype;
    }

    public void setMailingtype(int mailingtype) {
        this.mailingtype = mailingtype;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public boolean getIsMailingUndoAvailable() {
        return isMailingUndoAvailable;
    }

    public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
        this.isMailingUndoAvailable = isMailingUndoAvailable;
    }

    public boolean isWorldMailingSend() {
        return worldMailingSend;
    }

    public void setWorldMailingSend(boolean worldMailingSend) {
        this.worldMailingSend = worldMailingSend;
    }

    public DeliveryStat getDeliveryStat() {
        return deliveryStat;
    }

    public void setDeliveryStat(DeliveryStat deliveryStat) {
        this.deliveryStat = deliveryStat;
    }

    public int getSendHour() {
        return sendHour;
    }

    public void setSendHour(int sendHour) {
        this.sendHour = sendHour;
    }

    public int getSendMinute() {
        return sendMinute;
    }

    public void setSendMinute(int sendMinute) {
        this.sendMinute = sendMinute;
    }

    public boolean isPrioritizationDisallowed() {
        return isPrioritizationDisallowed;
    }

    public boolean getIsPrioritizationDisallowed() {
        return isPrioritizationDisallowed;
    }

    public void setPrioritizationDisallowed(boolean prioritizationDisallowed) {
        isPrioritizationDisallowed = prioritizationDisallowed;
    }

    public boolean isEncryptedSend() {
        return isEncryptedSend;
    }

    public boolean getIsEncryptedSend() {
        return isEncryptedSend;
    }

    public void setEncryptedSend(boolean encryptedSend) {
        isEncryptedSend = encryptedSend;
    }

    public void setIsPrioritizationDisallowed(boolean isPrioritizationDisallowed) {
        this.isPrioritizationDisallowed = isPrioritizationDisallowed;
    }

    public boolean isStatusmailOnErrorOnly() {
        return statusmailOnErrorOnly;
    }

    public void setStatusmailOnErrorOnly(boolean statusmailOnErrorOnly) {
        this.statusmailOnErrorOnly = statusmailOnErrorOnly;
    }

    public String getSendDate() {
        return sendDate;
    }

    public void setSendDate(String sendDate) {
        this.sendDate = sendDate;
    }

    public String getMaxRecipients() {
        return maxRecipients;
    }

    public void setMaxRecipients(final String maxRecipients) {
        this.maxRecipients = StringUtils.isBlank(maxRecipients) ? "0" : maxRecipients;
    }

    public void setGenerationOptimization(final int mode) {
        this.generationOptimization = mode;
    }

    public int getGenerationOptimization() {
        return this.generationOptimization;
    }

    public int getBlocksize() {
        return blocksize;
    }

    public void setBlocksize(int blocksize) {
        this.blocksize = blocksize;
    }

    public int getStepping() {
        return stepping;
    }

    public void setStepping(int stepping) {
        this.stepping = stepping;
    }

    public boolean isCheckForDuplicateRecords() {
        return this.checkForDuplicateRecords;
    }

    public void setCheckForDuplicateRecords(boolean checkForDuplicateRecords) {
        this.checkForDuplicateRecords = checkForDuplicateRecords;
    }

    public boolean isSkipWithEmptyTextContent() {
        return skipWithEmptyTextContent;
    }

    public void setSkipWithEmptyTextContent(boolean skipWithEmptyTextContent) {
        this.skipWithEmptyTextContent = skipWithEmptyTextContent;
    }

    public boolean isReportSendAfter24h() {
        return reportSendAfter24h;
    }

    public void setReportSendAfter24h(boolean reportSendAfter24h) {
        this.reportSendAfter24h = reportSendAfter24h;
    }

    public boolean isReportSendAfter48h() {
        return reportSendAfter48h;
    }

    public void setReportSendAfter48h(boolean reportSendAfter48h) {
        this.reportSendAfter48h = reportSendAfter48h;
    }

    public boolean isReportSendAfter1Week() {
        return reportSendAfter1Week;
    }

    public void setReportSendAfter1Week(boolean reportSendAfter1Week) {
        this.reportSendAfter1Week = reportSendAfter1Week;
    }

    public String getReportSendEmail() {
        return reportSendEmail;
    }

    public void setReportSendEmail(String reportSendEmail) {
        this.reportSendEmail = reportSendEmail;
    }

    public int getAutoImportId() {
        return autoImportId;
    }

    public void setAutoImportId(int autoImportId) {
        this.autoImportId = autoImportId;
    }

    public String getStatusmailRecipients() {
        return statusmailRecipients;
    }

    public void setStatusmailRecipients(String statusmailRecipients) {
        if (statusmailRecipients != null) {
            this.statusmailRecipients = statusmailRecipients.toLowerCase();
        } else {
            this.statusmailRecipients = statusmailRecipients;
        }
    }

    public String getWorkStatus() {
        return workStatus;
    }

    public void setWorkStatus(String workStatus) {
        this.workStatus = workStatus;
    }

    public void setFilterTypes(String[] filterTypes) {
        this.filterTypes = filterTypes;
    }

    public String[] getFilterTypes() {
        return filterTypes;
    }

    public int getOfflineHtmlEmailsCount() {
        return offlineHtmlEmailsCount;
    }

    public void setOfflineHtmlEmailsCount(int offlineHtmlEmailsCount) {
        this.offlineHtmlEmailsCount = offlineHtmlEmailsCount;
    }

    public int getHtmlEmailsCount() {
        return htmlEmailsCount;
    }

    public void setHtmlEmailsCount(int htmlEmailsCount) {
        this.htmlEmailsCount = htmlEmailsCount;
    }

    public int getTextEmailsCount() {
        return textEmailsCount;
    }

    public void setTextEmailsCount(int textEmailsCount) {
        this.textEmailsCount = textEmailsCount;
    }

    public int getSendStatisticsItem(int id) {
        if (sentStatistics.containsKey(id)) {
            return sentStatistics.get(id);
        }

        return 0;
    }

    public void setSentStatisticsItem(int id, int value) {
        sentStatistics.put(id, value);
    }

    public Map<Integer, Integer> getSentStatistics() {
        return sentStatistics;
    }

    public int getTotalSentCount() {
        return totalSentCount;
    }

    public void setTotalSentCount(int totalSentCount) {
        this.totalSentCount = totalSentCount;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public MailingIntervalSettingsForm getIntervalSettings() {
        if (intervalSettings == null) {
            intervalSettings = new MailingIntervalSettingsForm();
        }

        return intervalSettings;
    }

    public void setIntervalSettings(MailingIntervalSettingsForm intervalSettings) {
        this.intervalSettings = intervalSettings;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isActivateAgainToday() {
        return isActivateAgainToday;
    }

    public void setActivateAgainToday(boolean activateAgainToday) {
        isActivateAgainToday = activateAgainToday;
    }

    public SecurityAndNotificationsSettingsForm getSecuritySettings() {
        if (securitySettings == null) {
            securitySettings = new SecurityAndNotificationsSettingsForm();
        }

        return securitySettings;
    }

    public void setSecuritySettings(SecurityAndNotificationsSettingsForm securitySettings) {
        this.securitySettings = securitySettings;
    }
}
