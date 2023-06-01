/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.agnitas.emm.core.workflow.beans.WorkflowReactionType;
import com.agnitas.emm.core.workflow.beans.WorkflowRule;
import com.agnitas.emm.core.workflow.beans.WorkflowStart;
import com.agnitas.emm.core.workflow.beans.WorkflowStartStop;
import com.fasterxml.jackson.annotation.JsonIgnore;

public abstract class WorkflowStartStopImpl extends BaseWorkflowIcon implements WorkflowStartStop {

    private Date date;
    private int hour;
    private int minute;

    private boolean sendReminder;
    private int remindAdminId;
    private WorkflowStart.WorkflowStartEventType event;
    private WorkflowReactionType reaction;

    private int mailingId;
    private int linkId;
    private String profileField;
    private boolean useRules;
    private List<WorkflowRule> rules = new ArrayList<>();

    private String dateProfileField;
    private int dateFieldOperator;
    private String dateFieldValue;
	private String dateFormat;

	private boolean executeOnce;

	private boolean remindAtOnce;
	private boolean scheduleReminder;
	private boolean remindSpecificDate;
	private Date remindDate;
	private int remindHour;
	private int remindMinute;
	private String comment;
    private String recipients; //emails of recipients separated by coma
    private String adminTimezone; //time zone for reminders sending in a case when we specify recipients

    @JsonIgnore
    private String rulesSql;
    @JsonIgnore
    private String rulesRepresentation;
    private int senderAdminId;

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public int getHour() {
        return hour;
    }

    @Override
    public void setHour(int hour) {
        this.hour = hour;
    }

    @Override
    public int getMinute() {
        return minute;
    }

    @Override
    public void setMinute(int minute) {
        this.minute = minute;
    }

    @Override
    public boolean isSendReminder() {
        return sendReminder;
    }

    @Override
    public void setSendReminder(boolean sendReminder) {
        this.sendReminder = sendReminder;
    }

    @Override
    public int getRemindAdminId() {
        return remindAdminId;
    }

    @Override
    public void setRemindAdminId(int remindAdminId) {
        this.remindAdminId = remindAdminId;
    }

    @Override
    public WorkflowStart.WorkflowStartEventType getEvent() {
        return event;
    }

    @Override
    public void setEvent(WorkflowStart.WorkflowStartEventType event) {
        this.event = event;
    }

    @Override
    public WorkflowReactionType getReaction() {
        return reaction;
    }

    @Override
    public void setReaction(WorkflowReactionType reaction) {
        this.reaction = reaction;
    }

    @Override
    public int getMailingId() {
        return mailingId;
    }

    @Override
    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    @Override
    public String getProfileField() {
        return profileField;
    }

    @Override
    public void setProfileField(String profileField) {
        this.profileField = profileField;
    }

    @Override
    public boolean isUseRules() {
        return useRules;
    }

    @Override
    public void setUseRules(boolean useRules) {
        this.useRules = useRules;
    }

    @Override
    public List<WorkflowRule> getRules() {
        return rules;
    }

    @Override
    public void setRules(List<WorkflowRule> rules) {
        this.rules = rules;
    }

    @Override
    public String getDateProfileField() {
        return dateProfileField;
    }

    @Override
    public void setDateProfileField(String dateProfileField) {
        this.dateProfileField = dateProfileField;
    }

    @Override
    public int getDateFieldOperator() {
        return dateFieldOperator;
    }

    @Override
    public void setDateFieldOperator(int dateFieldOperator) {
        this.dateFieldOperator = dateFieldOperator;
    }

	@Override
	public String getDateFormat() {
		return dateFormat;
	}

	@Override
	public void setDateFormat(String dateFormat) {
		this.dateFormat = dateFormat;
	}

	@Override
    public String getRulesSql() {
        return rulesSql;
    }

    @Override
    public void setRulesSql(String rulesSql) {
        this.rulesSql = rulesSql;
    }

    @Override
    public String getRulesRepresentation() {
        return rulesRepresentation;
    }

    @Override
    public void setRulesRepresentation(String rulesRepresentation) {
        this.rulesRepresentation = rulesRepresentation;
    }

    @Override
    public String getDateFieldValue() {
        return dateFieldValue;
    }

    @Override
    public void setDateFieldValue(String dateFieldValue) {
        this.dateFieldValue = dateFieldValue;
    }

	@Override
	public boolean isExecuteOnce() {
		return executeOnce;
	}

	@Override
	public void setExecuteOnce(boolean executeOnce) {
		this.executeOnce = executeOnce;
	}

	@Override
	public boolean isRemindAtOnce() {
		return remindAtOnce;
	}

	@Override
	public void setRemindAtOnce(boolean remindAtOnce) {
		this.remindAtOnce = remindAtOnce;
	}

	@Override
	public boolean isScheduleReminder() {
		return scheduleReminder;
	}

	@Override
	public void setScheduleReminder(boolean scheduleReminder) {
		this.scheduleReminder = scheduleReminder;
	}

	@Override
	public boolean isRemindSpecificDate() {
		return remindSpecificDate;
	}

	@Override
	public void setRemindSpecificDate(boolean remindSpecificDate) {
		this.remindSpecificDate = remindSpecificDate;
	}

	@Override
	public Date getRemindDate() {
		return remindDate;
	}

	@Override
	public void setRemindDate(Date remindDate) {
		this.remindDate = remindDate;
	}

	@Override
	public int getRemindHour() {
		return remindHour;
	}

	@Override
	public void setRemindHour(int remindHour) {
		this.remindHour = remindHour;
	}

	@Override
	public int getRemindMinute() {
		return remindMinute;
	}

	@Override
	public void setRemindMinute(int remindMinute) {
		this.remindMinute = remindMinute;
	}

    @Override
    public int getSenderAdminId() {
        return senderAdminId;
    }

    @Override
    public void setSenderAdminId(int senderAdminId) {
        this.senderAdminId = senderAdminId;
    }

    @Override
    public int getLinkId() {
        return linkId;
    }

    @Override
    public void setLinkId(int linkId) {
        this.linkId = linkId;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public void setRecipients(String recipients) {
        this.recipients = recipients;
    }

    @Override
    public String getRecipients() {
        return recipients;
    }

    @Override
    public String getAdminTimezone() {
        return adminTimezone;
    }

    @Override
    public void setAdminTimezone(String adminTimezone) {
        this.adminTimezone = adminTimezone;
    }
}
