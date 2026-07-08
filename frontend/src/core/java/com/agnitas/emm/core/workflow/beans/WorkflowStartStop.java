/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.Date;
import java.util.List;

public interface WorkflowStartStop extends WorkflowIcon {

    Date getDate();

    void setDate(Date date);

    int getHour();

    void setHour(int hour);

    int getMinute();

    void setMinute(int minute);

    boolean isSendReminder();

    void setSendReminder(boolean sendReminder);

    int getRemindAdminId();

    void setRemindAdminId(int remindAdminId);

    WorkflowStart.WorkflowStartEventType getEvent();

    void setEvent(WorkflowStart.WorkflowStartEventType event);

    WorkflowReactionType getReaction();

    void setReaction(WorkflowReactionType reaction);

    int getMailingId();

    void setMailingId(int mailingId);

    String getProfileField();

    void setProfileField(String profileField);

    boolean isUseRules();

    void setUseRules(boolean useRules);

    List<WorkflowRule> getRules();

    void setRules(List<WorkflowRule> rules);

    String getDateProfileField();

    void setDateProfileField(String dateProfileField);

    int getDateFieldOperator();

    void setDateFieldOperator(int dateFieldOperator);

    String getRulesSql();

    void setRulesSql(String rulesSql);

    String getRulesRepresentation();

    void setRulesRepresentation(String rulesRepresentation);

    void setDateFieldValue(String dateFieldValue);

    String getDateFieldValue();

    boolean isExecuteOnce();

    void setExecuteOnce(boolean executeOnce);

    boolean isRemindAtOnce();

    void setRemindAtOnce(boolean remindAtOnce);

    boolean isScheduleReminder();

    void setScheduleReminder(boolean scheduleReminder);

    boolean isRemindSpecificDate();

    void setRemindSpecificDate(boolean remindSpecificDate);

    Date getRemindDate();

    void setRemindDate(Date remindDate);

    int getRemindHour();

    void setRemindHour(int remindHour);

    int getRemindMinute();

    void setRemindMinute(int remindMinute);

    int getSenderAdminId();

    void setSenderAdminId(int senderAdminId);

    void setDateFormat(String dateFormat);

    String getDateFormat();

    int getLinkId();

    void setLinkId(int linkId);

    String getComment();

    void setComment(String comment);

    String getRecipients();

    void setRecipients(String recipients);

    String getAdminTimezone();

    void setAdminTimezone(String adminTimezone);
}
