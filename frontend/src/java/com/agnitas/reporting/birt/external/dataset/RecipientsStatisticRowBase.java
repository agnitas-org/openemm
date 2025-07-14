/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

public abstract class RecipientsStatisticRowBase {

	private int mailingListId;
	private int mailingListGroupId;
	private String mailingListName;
	private int targetGroupId;
	protected String targetGroupName;
	private int categoryIndex;
	private String categoryNameKey;

	public int getMailingListId() {
		return mailingListId;
	}

	public void setMailingListId(int mailingListId) {
		this.mailingListId = mailingListId;
	}

	public String getMailingListName() {
		return mailingListName;
	}

	public void setMailingListName(String mailingListName) {
		this.mailingListName = mailingListName;
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

	public int getCategoryIndex() {
		return categoryIndex;
	}

	public void setCategoryIndex(int categoryIndex) {
		this.categoryIndex = categoryIndex;
	}

	public String getCategoryNameKey() {
		return categoryNameKey;
	}

	public void setCategoryNameKey(String categoryNameKey) {
		this.categoryNameKey = categoryNameKey;
	}

	public int getMailingListGroupId() {
		return mailingListGroupId;
	}

	public void setMailingListGroupId(int mailingListGroupId) {
		this.mailingListGroupId = mailingListGroupId;
	}
}
