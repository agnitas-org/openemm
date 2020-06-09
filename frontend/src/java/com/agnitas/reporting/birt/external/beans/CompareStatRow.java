/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.beans;

import java.util.Date;

public class CompareStatRow {

	private String category = "";
	private int categoryindex;	
	private int mailingId;
    private String mailingName;
    private String mailingNameFull;
    private int targetGroupId;
    private String targetGroupName;
    private String targetShortName;
    private int targetGroupIndex;
	private int count;
    private double rate;
    private Date sendDate;

    public String getMailingName() {
        return mailingName;
    }

    public void setMailingName(String mailingName) {
        this.mailingName = mailingName;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public int getCategoryindex() {
        return categoryindex;
    }

    public void setCategoryindex(int categoryindex) {
        this.categoryindex = categoryindex;
    }

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public int getTargetGroupIndex() {
        return targetGroupIndex;
    }

    public void setTargetGroupIndex(int targetGroupIndex) {
        this.targetGroupIndex = targetGroupIndex;
    }

    public String getTargetShortName() {
        return targetShortName;
    }

    public void setTargetShortName(String targetShortName) {
        this.targetShortName = targetShortName;
    }

    public double getRate() {
		return rate;
	}
	public void setRate(double rate) {
		this.rate = rate;
	}
    
    public Date getSendDate() {
        return sendDate;
    }
    
    public void setSendDate(Date sendDate) {
        this.sendDate = sendDate;
    }
    
    @Override
	public String toString() {
		StringBuilder builder = new StringBuilder("CompareStatRow : \n");
        builder.append("Category: ").append(getCategory()).append("\n");
        builder.append("CategoryIndex: ").append(getCategoryindex()).append("\n");
        builder.append("TargetGroupIndex: ").append(getCategoryindex()).append("\n");
        builder.append("MalingId: ").append(getMailingId()).append("\n");
        builder.append("TargetId: ").append(getTargetGroupId()).append("\n");
        builder.append("MalingName: ").append(getMailingName()).append("\n");
        builder.append("TargetName: ").append(getTargetGroupName()).append("\n");
        builder.append("TargetNameShort: ").append(getTargetShortName()).append("\n");
        builder.append("MalingNameFull: ").append(getMailingNameFull()).append("\n");
        builder.append("Value: ").append(getCount()).append("\n");
        builder.append("Rate: ").append(getRate()).append("\n");
		return builder.toString();
	}
	
}
