/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;

public class DeliveryStat {
    public static final int STATUS_NOT_SENT = 0;
    public static final int STATUS_SCHEDULED = 1;
    public static final int STATUS_GENERATING = 2;
    public static final int STATUS_GENERATED = 3;
    public static final int STATUS_SENDING = 4;
    public static final int STATUS_SENT = 5;
    public static final int STATUS_CANCELLED = 6;
    public static final int STATUS_PAUSED_GENERATION = 7;
    public static final int STATUS_PAUSED_DELIVERY = 8;
    
    private int totalMails = 0;
    private int generatedMails = 0;
    private int sentMails = 0;
    private int deliveryStatus = 0;
    private Date generateStartTime;
    private Date generateEndTime;
    private Date sendStartTime;
    private Date sendEndTime;
    private Date scheduledSendTime;
    private int mailingID = 0;
    private int companyID = 0;
    private Date scheduledGenerateTime;
    private boolean cancelable = false;
    private String lastType = "NO";
    private int lastTotal;
    private int lastGenerated;
    private Date lastDate;
    private String optimizeMailGeneration;
    private boolean stopped;
    private boolean resumable;

    public int getTotalMails() {
        return totalMails;
    }
    public void setTotalMails(int totalMails) {
        this.totalMails = totalMails;
    }

    public int getGeneratedMails() {
        return generatedMails;
    }
    public void setGeneratedMails(int generatedMails) {
        this.generatedMails = generatedMails;
    }

    public int getSentMails() {
        return sentMails;
    }
    public void setSentMails(int sentMails) {
        this.sentMails = sentMails;
    }

    public int getDeliveryStatus() {
        return deliveryStatus;
    }
    public void setDeliveryStatus(int deliveryStatus) {
        this.deliveryStatus = deliveryStatus;
    }

    public Date getGenerateStartTime() {
        return generateStartTime;
    }
    public void setGenerateStartTime(Date generateStartTime) {
        this.generateStartTime = generateStartTime;
    }

    public Date getGenerateEndTime() {
        return generateEndTime;
    }
    public void setGenerateEndTime(Date generateEndTime) {
        this.generateEndTime = generateEndTime;
    }

    public Date getSendStartTime() {
        return sendStartTime;
    }
    public void setSendStartTime(Date sendStartTime) {
        this.sendStartTime = sendStartTime;
    }

    public Date getSendEndTime() {
        return sendEndTime;
    }
    public void setSendEndTime(Date sendEndTime) {
        this.sendEndTime = sendEndTime;
    }

    public Date getScheduledSendTime() {
        return scheduledSendTime;
    }
    public void setScheduledSendTime(Date scheduledSendTime) {
        this.scheduledSendTime = scheduledSendTime;
    }

    public int getMailingID() {
        return mailingID;
    }
    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    public int getCompanyID() {
        return companyID;
    }
    public void setCompanyID(@VelocityCheck int companyID) {
        this.companyID = companyID;
    }

    public Date getScheduledGenerateTime() {
        return scheduledGenerateTime;
    }
    public void setScheduledGenerateTime(Date scheduledGenerateTime) {
        this.scheduledGenerateTime = scheduledGenerateTime;
    }

    public boolean isCancelable() {
        return cancelable;
    }
    public void setCancelable(boolean cancelable) {
        this.cancelable = cancelable;
    }

    public String getLastType() {
        return lastType;
    }
    public void setLastType(String lastType) {
        this.lastType = lastType;
    }

    public int getLastTotal() {
        return lastTotal;
    }
    public void setLastTotal(int lastTotal) {
        this.lastTotal = lastTotal;
    }

    public int getLastGenerated() {
        return lastGenerated;
    }
    public void setLastGenerated(int lastGenerated) {
        this.lastGenerated = lastGenerated;
    }

    public Date getLastDate() {
        return lastDate;
    }
    public void setLastDate(Date lastDate) {
        this.lastDate = lastDate;
    }

    public String getOptimizeMailGeneration() {
        return optimizeMailGeneration;
    }
    public void setOptimizeMailGeneration(String optimizeMailGeneration) {
        this.optimizeMailGeneration = optimizeMailGeneration;
    }

    public void setStopped(boolean stopped) {
        this.stopped = stopped;
    }

    public boolean isStopped() {
        return stopped;
    }

    public void setResumable(boolean resumable) {
        this.resumable = resumable;
    }

    public boolean isResumable() {
        return this.resumable;
    }
}
