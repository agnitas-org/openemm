/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stat.beans;

import java.util.Date;

public class MailingStatJobDescriptor {
	
	public static final int STATUS_STARTED = 1;
	public static final int STATUS_SUCCEED = 2;
	public static final int STATUS_FAILED = 3;
	
	public static final int RECIPIENT_TYPE_WORLD = 1;
	public static final int RECIPIENT_TYPE_TEST = 2;
	public static final int RECIPIENT_TYPE_ALL = 3;
	
	private int id;
	private int status;
	private int mailingId;
	private int recipientsType;
	private String targetGroups;
	private Date creationDate;
	private Date changeDate;
	private String statusDescription;

	public MailingStatJobDescriptor() {}

	public MailingStatJobDescriptor(int mailingId, int recipientsType, String targetGroups) {
		this.mailingId = mailingId;
		this.recipientsType = recipientsType;
		this.targetGroups = targetGroups;
		
		status = STATUS_STARTED;
	}
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getMailingId() {
		return mailingId;
	}
	public void setMailingId(int mailingId) {
		this.mailingId = mailingId;
	}
	public int getRecipientsType() {
		return recipientsType;
	}
	public void setRecipientsType(int recipientsType) {
		this.recipientsType = recipientsType;
	}
	public String getTargetGroups() {
		return targetGroups;
	}
	public void setTargetGroups(String targetGroups) {
		this.targetGroups = targetGroups;
	}
	public Date getCreationDate() {
		return creationDate;
	}
	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	public Date getChangeDate() {
		return changeDate;
	}
	public void setChangeDate(Date changeDate) {
		this.changeDate = changeDate;
	}
	
	public String getStatusDescription() {
		return statusDescription;
	}

	public void setStatusDescription(String statusDescription) {
		this.statusDescription = statusDescription;
	}

}
