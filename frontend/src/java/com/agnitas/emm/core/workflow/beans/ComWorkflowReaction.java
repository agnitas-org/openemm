/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

public class ComWorkflowReaction {
	private int reactionId;
	private int companyId;
	private int workflowId;
	private int mailinglistId;
	private int triggerMailingId;
    private int triggerLinkId;
	private boolean active;
	private boolean once;
	private Date startDate;
	private TimeZone adminTimezone;
	private WorkflowReactionType reactionType;
	private String profileColumn;
	private String rulesSQL;
	private List<Integer> mailingsToSend = new ArrayList<>();
	private boolean isLegacyMode;

	public int getReactionId() {
		return reactionId;
	}

	public void setReactionId(int reactionId) {
		this.reactionId = reactionId;
	}

	public int getCompanyId() {
		return companyId;
	}

	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}

	public int getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(int workflowId) {
		this.workflowId = workflowId;
	}

	public int getMailinglistId() {
		return mailinglistId;
	}

	public void setMailinglistId(int mailinglistId) {
		this.mailinglistId = mailinglistId;
	}

	public int getTriggerMailingId() {
		return triggerMailingId;
	}

	public void setTriggerMailingId(int triggerMailingId) {
		this.triggerMailingId = triggerMailingId;
	}

	public boolean isActive() {
		return active;
	}

	public void setActive(boolean active) {
		this.active = active;
	}

	public boolean isOnce() {
		return once;
	}

	public void setOnce(boolean once) {
		this.once = once;
	}

	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public TimeZone getAdminTimezone() {
		return adminTimezone;
	}

	public void setAdminTimezone(TimeZone adminTimezone) {
		this.adminTimezone = adminTimezone;
	}

	public WorkflowReactionType getReactionType() {
		return reactionType;
	}

	public void setReactionType(WorkflowReactionType reactionType) {
		this.reactionType = reactionType;
	}

	public String getProfileColumn() {
		return profileColumn;
	}

	public void setProfileColumn(String profileColumn) {
		this.profileColumn = profileColumn;
	}

	public String getRulesSQL() {
		return rulesSQL;
	}

	public void setRulesSQL(String rulesSQL) {
		this.rulesSQL = rulesSQL;
	}

	@Deprecated
	public List<Integer> getMailingsToSend() {
		return mailingsToSend;
	}

	@Deprecated
	public void setMailingsToSend(List<Integer> mailingsToSend) {
		this.mailingsToSend = mailingsToSend;
	}

    public int getTriggerLinkId() {
        return triggerLinkId;
    }

    public void setTriggerLinkId(int triggerLinkId) {
        this.triggerLinkId = triggerLinkId;
    }

	public boolean isLegacyMode() {
		return isLegacyMode;
	}

	public void setLegacyMode(boolean isLegacyMode) {
		this.isLegacyMode = isLegacyMode;
	}
}
