/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.bean.impl;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import org.agnitas.util.DateUtilities;

import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.dto.BirtReportType;

/**
 * Class for BirtReports. Allows managing BirtReports with an easy interface.
 */
public class ComBirtReportImpl implements ComBirtReport {
	private static final long serialVersionUID = 7646744980286070049L;

	protected int reportID;
    protected int companyID;
    protected String shortname;
    protected String description;
    private String sendEmail;
    protected String emailSubject;
    protected String emailDescription;
    private int reportActive;
    private int reportType;
    private int format;
    private Date sendDate;
    private Date sendTime;
    private Date activationDate;
    private Date endDate;
    private int sendDays = 0;
    private int activeTab = 1;
    private boolean hidden = false;
    private Date changeDate;

    /**
     * Last time when this report was generated and delivered
     */
    private Date deliveryDate;

    private String language;

    // reports settings
    private ComBirtReportComparisonSettings reportComparisonSettings = new ComBirtReportComparisonSettings();
    private ComBirtReportMailingSettings reportMailingSettings = new ComBirtReportMailingSettings();
    private ComBirtReportRecipientSettings reportRecipientSettings = new ComBirtReportRecipientSettings();

    private List<ComBirtReportSettings> settings = new ArrayList<>();

    public ComBirtReportImpl(){
        settings.add(reportComparisonSettings);
        settings.add(reportMailingSettings);
        settings.add(reportRecipientSettings);
    }

    @Override
	public boolean isEnabled() {
        for(ComBirtReportSettings setting : settings) {
            if(setting.isEnabled()){
                return true;
            }
        }
        return false;
    }

    @Override
	public List<ComBirtReportSettings> getSettings() {
        return settings;
    }

    @Override
	public ComBirtReportComparisonSettings getReportComparisonSettings() {
        return reportComparisonSettings;
    }

    @Override
	public void setReportComparisonSettings(ComBirtReportComparisonSettings reportComparisonSettings) {
        this.reportComparisonSettings = reportComparisonSettings;
    }

    @Override
	public ComBirtReportMailingSettings getReportMailingSettings() {
        return reportMailingSettings;
    }

    @Override
	public void setReportMailingSettings(ComBirtReportMailingSettings reportMailingSettings) {
        this.reportMailingSettings = reportMailingSettings;
    }

    @Override
	public ComBirtReportRecipientSettings getReportRecipientSettings() {
        return reportRecipientSettings;
    }

    @Override
	public void setReportRecipientSettings(ComBirtReportRecipientSettings reportRecipientSettings) {
        this.reportRecipientSettings = reportRecipientSettings;
    }

    @Override
	public boolean isSend(int day) {
        return (sendDays & day) != 0;
    }

    @Override
	public void setSend(int day, boolean send) {
        if (send) {
            sendDays |= day;
        } else {
            sendDays &= ~day;
        }
    }

    @Override
	public void parseSendDays(String input) {
        sendDays = 0;
        if (input == null || input.length() < 7) {
            return;
        }

        if (input.charAt(0) == '1') {
            sendDays |= MONDAY;
        }

        if (input.charAt(1) == '1') {
            sendDays |= TUESDAY;
        }

        if (input.charAt(2) == '1') {
            sendDays |= WEDNESDAY;
        }

        if (input.charAt(3) == '1') {
            sendDays |= THURSDAY;
        }

        if (input.charAt(4) == '1') {
            sendDays |= FRIDAY;
        }

        if (input.charAt(5) == '1') {
            sendDays |= SATURDAY;
        }

        if (input.charAt(6) == '1') {
            sendDays |= SUNDAY;
        }
    }

    @Override
	public String buildSendDate() {
        StringBuffer output = new StringBuffer("0000000");
        if ((sendDays & MONDAY) != 0) {
            output.setCharAt(0, '1');
        }

        if ((sendDays & TUESDAY) != 0) {
            output.setCharAt(1, '1');
        }

        if ((sendDays & WEDNESDAY) != 0) {
            output.setCharAt(2, '1');
        }

        if ((sendDays & THURSDAY) != 0) {
            output.setCharAt(3, '1');
        }

        if ((sendDays & FRIDAY) != 0) {
            output.setCharAt(4, '1');
        }

        if ((sendDays & SATURDAY) != 0) {
            output.setCharAt(5, '1');
        }

        if ((sendDays & SUNDAY) != 0) {
            output.setCharAt(6, '1');
        }

        return output.toString();
    }

    @Override
	public void calculateSendDate() {
        if (isTriggeredByMailing()) {
            return;
        } else {
        	BirtReportType birtReportType = BirtReportType.getTypeByCode(getReportType());
    		Calendar sendTimeCalendar = new GregorianCalendar();
    		sendTimeCalendar.setTime(sendTime);
	        if (birtReportType == BirtReportType.TYPE_MONTHLY_FIRST) {
	        	String intervalPattern = "M01:" + String.format("%02d", sendTimeCalendar.get(GregorianCalendar.HOUR_OF_DAY)) + String.format("%02d", sendTimeCalendar.get(GregorianCalendar.MINUTE));
	        	sendDate = DateUtilities.calculateNextJobStart(intervalPattern);
	        } else if (birtReportType == BirtReportType.TYPE_MONTHLY_15TH) {
	        	String intervalPattern = "M15:" + String.format("%02d", sendTimeCalendar.get(GregorianCalendar.HOUR_OF_DAY)) + String.format("%02d", sendTimeCalendar.get(GregorianCalendar.MINUTE));
	        	sendDate = DateUtilities.calculateNextJobStart(intervalPattern);
	        } else if (birtReportType == BirtReportType.TYPE_MONTHLY_LAST) {
	        	String intervalPattern = "M99:" + String.format("%02d", sendTimeCalendar.get(GregorianCalendar.HOUR_OF_DAY)) + String.format("%02d", sendTimeCalendar.get(GregorianCalendar.MINUTE));
	        	sendDate = DateUtilities.calculateNextJobStart(intervalPattern);
	        } else if (birtReportType == BirtReportType.TYPE_WEEKLY || birtReportType == BirtReportType.TYPE_BIWEEKLY) {
	    		String intervalPattern = "";
	        	if (isSend(MONDAY)) {
	        		intervalPattern += "Mo";
	        	}
	        	if (isSend(TUESDAY)) {
	        		intervalPattern += "Tu";
	        	}
	        	if (isSend(WEDNESDAY)) {
	        		intervalPattern += "We";
	        	}
	        	if (isSend(THURSDAY)) {
	        		intervalPattern += "Th";
	        	}
	        	if (isSend(FRIDAY)) {
	        		intervalPattern += "Fr";
	        	}
	        	if (isSend(SATURDAY)) {
	        		intervalPattern += "Sa";
	        	}
	        	if (isSend(SUNDAY)) {
	        		intervalPattern += "Su";
	        	}
	        	intervalPattern += ":" + String.format("%02d", sendTimeCalendar.get(GregorianCalendar.HOUR_OF_DAY)) + String.format("%02d", sendTimeCalendar.get(GregorianCalendar.MINUTE));

	            if (birtReportType == BirtReportType.TYPE_BIWEEKLY) {
	            	sendDate = DateUtilities.addDaysToDate(DateUtilities.calculateNextJobStart(intervalPattern), 7);
	            } else {
	            	sendDate = DateUtilities.calculateNextJobStart(intervalPattern);
	            }
	        } else {
	        	String intervalPattern = String.format("%02d", sendTimeCalendar.get(GregorianCalendar.HOUR_OF_DAY)) + String.format("%02d", sendTimeCalendar.get(GregorianCalendar.MINUTE));
	        	sendDate = DateUtilities.calculateNextJobStart(intervalPattern);
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
	public String getSendEmail() {
        return sendEmail;
    }

    @Override
	public void setSendEmail(String sendEmail) {
        this.sendEmail = sendEmail;
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
	public Date getSendDate() {
        return sendDate;
    }

    @Override
	public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }

    @Override
	public Date getSendTime() {
        return sendTime;
    }

    @Override
	public void setSendTime(Date sendTime) {
        this.sendTime = sendTime;
    }

    @Override
	public int getSendDays() {
        return sendDays;
    }

    @Override
	public void setSendDays(int sendDays) {
        this.sendDays = sendDays;
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
        return this.activeTab;
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
        return settings.stream()
                .filter(s -> s.getReportSettingsType().getKey() == activeTab)
                .findFirst()
                .orElse(reportComparisonSettings);
    }

    @Override
    public void setLanguage(final String language) {
    	this.language = language;
    }

    @Override
    public String getLanguage() {
    	return this.language;
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
	public Date getDeliveryDate() {
        return deliveryDate;
    }

    @Override
	public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }
}
