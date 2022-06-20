/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;


public class CompareStatCsvRow {

	private int mailingId;
    private String mailingNameFull;
    private int targetGroupId;
    private String targetGroupName;
	private int emailsSentCount;
	private int emailsDeliveredCount;
	private int openersCount;
	private double openersRate;
    private int clickingCount;
	private double clickingRate;
    private int signedoffCount;
	private double signedoffRate;
    private int bouncesCount;
	private double bouncesRate;
    private int revenueCount;

    public CompareStatCsvRow(int mailingId, int targetGroupId){
        this.mailingId = mailingId;
        this.targetGroupId = targetGroupId;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public String getMailingNameFull() {
        return mailingNameFull;
    }

    public void setMailingNameFull(String mailingNameFull) {
        this.mailingNameFull = mailingNameFull;
    }

    public int getTargetGroupId() {
        return targetGroupId;
    }

    public void setTargetGroupId(int targetGroupId) {
        this.targetGroupId = targetGroupId;
    }

    public String getTargetGroupName() {
        return targetGroupName;
    }

    public void setTargetGroupName(String targetGroupName) {
        this.targetGroupName = targetGroupName;
    }

    public int getEmailsSentCount() {
        return emailsSentCount;
    }

    public void setEmailsSentCount(int emailsSentCount) {
        this.emailsSentCount = emailsSentCount;
    }

    public int getEmailsDeliveredCount() {
        return emailsDeliveredCount;
    }
    
    public void setEmailsDeliveredCount(int emailsDeliveredCount) {
        this.emailsDeliveredCount = emailsDeliveredCount;
    }
    
    public int getOpenersCount() {
        return openersCount;
    }

    public void setOpenersCount(int openersCount) {
        this.openersCount = openersCount;
    }

    public double getOpenersRate() {
        return openersRate;
    }

    public void setOpenersRate(double openersRate) {
        this.openersRate = openersRate;
    }

    public int getClickingCount() {
        return clickingCount;
    }

    public void setClickingCount(int clickingCount) {
        this.clickingCount = clickingCount;
    }

    public double getClickingRate() {
        return clickingRate;
    }

    public void setClickingRate(double clickingRate) {
        this.clickingRate = clickingRate;
    }

    public int getSignedoffCount() {
        return signedoffCount;
    }

    public void setSignedoffCount(int signedoffCount) {
        this.signedoffCount = signedoffCount;
    }

    public double getSignedoffRate() {
        return signedoffRate;
    }

    public void setSignedoffRate(double signedoffRate) {
        this.signedoffRate = signedoffRate;
    }

    public int getBouncesCount() {
        return bouncesCount;
    }

    public void setBouncesCount(int bouncesCount) {
        this.bouncesCount = bouncesCount;
    }

    public double getBouncesRate() {
        return bouncesRate;
    }

    public void setBouncesRate(double bouncesRate) {
        this.bouncesRate = bouncesRate;
    }

    public int getRevenueCount() {
        return revenueCount;
    }

    public void setRevenueCount(int revenueCount) {
        this.revenueCount = revenueCount;
    }
}
