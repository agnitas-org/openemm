/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.form;

import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientProgressStatisticDto;

public class MailinglistForm {
	
	private int id;
	private String shortname;
	private String description;
	private boolean frequencyCounterEnabled;
	private RecipientProgressStatisticDto statistic;
	private String senderEmail;
	private String replyEmail;
	private String approvalEmails;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getShortname() {
		return shortname;
	}

	public void setShortname(String shortname) {
		this.shortname = shortname;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public RecipientProgressStatisticDto getStatistic() {
        return statistic;
    }

    public void setStatistic(RecipientProgressStatisticDto statistic) {
        this.statistic = statistic;
    }

	public boolean getFrequencyCounterEnabled() {
		return frequencyCounterEnabled;
	}

	public void setFrequencyCounterEnabled(boolean markedForFrequencyCounter) {
		this.frequencyCounterEnabled = markedForFrequencyCounter;
	}

	public String getSenderEmail() {
		return senderEmail;
	}

	public void setSenderEmail(String senderEmail) {
		this.senderEmail = senderEmail;
	}

	public String getReplyEmail() {
		return replyEmail;
	}

	public void setReplyEmail(String replyEmail) {
		this.replyEmail = replyEmail;
	}

	public String getApprovalEmails() {
		return approvalEmails;
	}

	public void setApprovalEmails(String approvalEmails) {
		this.approvalEmails = approvalEmails;
	}
}
