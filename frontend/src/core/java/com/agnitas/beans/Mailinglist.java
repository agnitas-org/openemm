/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.beans;

import java.util.Date;


public interface Mailinglist {

	void setCompanyID(int id);

	void setId(int id);

	void setShortname(String shortname);

	void setDescription(String description);

	void setChangeDate(Date changeDate);

	void setCreationDate(Date creationDate);

	void setFrequencyCounterEnabled(boolean isFrequencyCounterEnabled);

	boolean isRemoved();

	int getCompanyID();

	int getId();

	String getShortname();

	String getDescription();

	Date getChangeDate();

	Date getCreationDate();

	boolean isFrequencyCounterEnabled();

	void setRemoved(boolean removed);

	String getSenderEmail();

	void setSenderEmail(String senderEmail);

	String getReplyEmail();

	void setReplyEmail(String replyEmail);

	String getSenderName();

	void setSenderName(String senderName);

	String getReplyToName();

	void setReplyToName(String replyToName);

	boolean isRestrictedForSomeAdmins();

	void setRestrictedForSomeAdmins(boolean restrictedForSomeAdmins);

	String getApprovalEmails();

	void setApprovalEmails(String approvalEmails);
}
