/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

public class RecipientCollectedStatisticRow {
	private int mailingListGroupId;
	private String mailingList;
	private int mailingListId;
	private String category = "";
	private int categoryindex;
	private String targetgroup;
	private int targetgroupindex;
	private int targetgroupId;
	private int count;
	private double rate;

	public int getMailingListGroupId() {
		return mailingListGroupId;
	}

	public void setMailingListGroupId(int mailingListGroupId) {
		this.mailingListGroupId = mailingListGroupId;
	}

	public String getMailingList() {
		return mailingList;
	}

	public void setMailingList(String mailingList) {
		this.mailingList = mailingList;
	}

	public int getMailingListId() {
		return mailingListId;
	}

	public void setMailingListId(int mailingListId) {
		this.mailingListId = mailingListId;
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

	public String getTargetgroup() {
		return targetgroup;
	}

	public void setTargetgroup(String targetgroup) {
		this.targetgroup = targetgroup;
	}

	public int getTargetgroupindex() {
		return targetgroupindex;
	}

	public void setTargetgroupindex(int targetgroupindex) {
		this.targetgroupindex = targetgroupindex;
	}
	
	public int getTargetgroupId() {
		return targetgroupId;
	}
	
	public void setTargetgroupId(int targetgroupId) {
		this.targetgroupId = targetgroupId;
	}
	
	public int getCount() {
		return count;
	}
	
	public void setCount(int count) {
		this.count = count;
	}
	
	public double getRate() {
		return rate;
	}
	
	public void setRate(double rate) {
		this.rate = rate;
	}
	
	@Override
	public String toString() {
		return "SendStatRow : \n" + "mailingListGroupId: " + getMailingListGroupId() + "\n" +
				"MailingList: " + getMailingList() + "\n" +
				"MailingListId: " + getMailingListId() + "\n" +
				"Category: " + getCategory() + "\n" +
				"CategoryIndex: " + getCategoryindex() + "\n" +
				"Targetgroup: " + getTargetgroup() + "\n" +
				"TargetgroupIndex: " + getTargetgroupindex() + "\n" +
				"TargetgroupId: " + getTargetgroupId() + "\n" +
				"Value: " + getCount() + "\n" +
				"Rate: " + getRate() + "\n";
	}
}
