/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

import java.util.Date;

public class BirtReportCompareStatRow extends SendStatWithMailingIdRow {

    private Date sendDate;
	private Date scheduledSendTime;
	private String mailingName;
	private int categoryRowIndex;
	private String assignedTargets;
    private int rowNum;
    private int orderRule;

	public BirtReportCompareStatRow() {
	}

	public BirtReportCompareStatRow(String category, int categoryindex, String targetgroup, int targetgroupindex, int count,
									double rate, int mailingId, String mailingName, Date sendDate, int categoryRowIndex, String assignedTargets) {
		super(category, categoryindex, targetgroup, targetgroupindex, count, rate, mailingId);
		this.sendDate = sendDate;
		this.mailingName = mailingName;
		this.categoryRowIndex = categoryRowIndex;
		this.assignedTargets = assignedTargets;
	}

	public BirtReportCompareStatRow(SendStatRow sendStatRow, int mailingId, Date sendDate, String mailingName, int categoryRowIndex, String assignedTargets) {
		super(sendStatRow, mailingId);
		this.sendDate = sendDate;
		this.mailingName = mailingName;
		this.categoryRowIndex = categoryRowIndex;
		this.assignedTargets = assignedTargets;
    }

	public Date getSendDate() {
		return sendDate;
	}

	public void setSendDate(Date sendDate) {
		this.sendDate = sendDate;
	}

	public String getMailingName() {
		return mailingName;
	}

	public void setMailingName(String mailingName) {
		this.mailingName = mailingName;
	}

	public int getCategoryRowIndex() {
		return categoryRowIndex;
	}

	public void setCategoryRowIndex(int categoryRowIndex) {
		this.categoryRowIndex = categoryRowIndex;
	}

	public String getAssignedTargets() {
		return assignedTargets;
	}

	public void setAssignedTargets(String assignedTargets) {
		this.assignedTargets = assignedTargets;
	}

    public int getRowNum() {
        return rowNum;
    }

    public void setRowNum(int rowNum) {
        this.rowNum = rowNum;
    }

    public void calcRate(int total) {
        setRate(getCount()/ (double) total);
    }

    public int getOrderRule() {
        return orderRule;
    }

    public void setOrderRule(int orderRule) {
        this.orderRule = orderRule;
    }

	public Date getScheduledSendTime() {
		return scheduledSendTime;
	}

	public void setScheduledSendTime(Date scheduledSendTime) {
		this.scheduledSendTime = scheduledSendTime;
	}
}
