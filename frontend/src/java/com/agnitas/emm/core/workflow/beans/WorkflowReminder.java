/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.function.Consumer;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

import com.agnitas.emm.core.workflow.dao.ComWorkflowStartStopReminderDao.ReminderType;

// Must be kept immutable.
public final class WorkflowReminder {
    public static final int MAX_MESSAGE_LENGTH = 2000;

    private List<WorkflowReminderRecipient> recipients;
    private int senderAdminId;
    private ReminderType type;
    private String message;
    private Date date;

    public static Builder builder() {
        return new Builder();
    }

    private WorkflowReminder(List<WorkflowReminderRecipient> recipients, int senderAdminId, ReminderType type, String message, Date date) {
        this.recipients = recipients;
        this.senderAdminId = senderAdminId;
        this.type = type;
        this.message = message;
        this.date = date;
    }

    public List<WorkflowReminderRecipient> getRecipients() {
        return Collections.unmodifiableList(recipients);
    }

    public int getSenderAdminId() {
        return senderAdminId;
    }

    public ReminderType getType() {
        return type;
    }

    public String getMessage() {
        return message;
    }

    public Date getDate() {
        return new Date(date.getTime());
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("recipients", recipients)
            .append("senderAdminId", senderAdminId)
            .append("type", type)
            .append("message", message)
            .append("date", date)
            .toString();
    }

    public static class Builder {
        private List<WorkflowReminderRecipient> recipients;
        private int senderAdminId;
        private ReminderType type;
        private String message;
        private Date date;

        private Builder() {}

        public Builder recipients(List<WorkflowReminderRecipient> recipientsToUse) {
            if (recipientsToUse == null) {
                this.recipients = null;
            } else {
                this.recipients = new ArrayList<>(recipientsToUse);
            }
            return this;
        }

        public RecipientsBuilder recipients() {
            return new RecipientsBuilder(this);
        }

        public Builder recipients(Consumer<RecipientsBuilder> consumer) {
            RecipientsBuilder recipientsBuilder = recipients();
            consumer.accept(recipientsBuilder);
            recipientsBuilder.end();
            return this;
        }

        public Builder sender(int adminId) {
            this.senderAdminId = adminId;
            return this;
        }

        public Builder type(ReminderType typeToUse) {
            this.type = typeToUse;
            return this;
        }

        public Builder message(String messageToUse) {
            this.message = messageToUse;
            return this;
        }

        public Builder date(Date dateToUse) {
            if (dateToUse == null) {
                this.date = null;
            } else {
                this.date = new Date(dateToUse.getTime());
            }
            return this;
        }

        public WorkflowReminder build() {
            WorkflowReminder reminder = new WorkflowReminder(recipients, senderAdminId, type, message, date);

            if (CollectionUtils.isEmpty(reminder.recipients)) {
                throw new IllegalStateException("Missing required recipients");
            }

            if (reminder.senderAdminId <= 0) {
                throw new IllegalStateException("Missing required sender");
            }

            if (reminder.type == null) {
                throw new IllegalStateException("Missing required type");
            }

            if (StringUtils.length(reminder.message) > MAX_MESSAGE_LENGTH) {
                throw new IllegalStateException("Message is too long");
            }

            if (reminder.date == null) {
                throw new IllegalStateException("Missing required date");
            }

            return reminder;
        }
    }

    public static class RecipientsBuilder {
        private final Builder builder;
        private final List<WorkflowReminderRecipient> recipients = new ArrayList<>();

        private RecipientsBuilder(Builder builder) {
            this.builder = builder;
        }

        public RecipientsBuilder recipient(int adminId) {
            recipients.add(new WorkflowReminderRecipient(adminId));
            return this;
        }

        public RecipientsBuilder recipient(String address) {
            recipients.add(new WorkflowReminderRecipient(address));
            return this;
        }

        public Builder end() {
            return builder.recipients(recipients);
        }
    }
}
