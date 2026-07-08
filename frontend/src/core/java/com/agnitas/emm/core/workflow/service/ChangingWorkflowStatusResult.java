/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import com.agnitas.emm.core.workflow.beans.Workflow;
import com.agnitas.messages.Message;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ChangingWorkflowStatusResult {

    private final boolean anyMailingDeactivated;
    private final Workflow.WorkflowStatus oldStatus;
    private final Workflow.WorkflowStatus newStatus;
    private final Workflow workflow;
    private final Collection<Message> messages;

    public static ChangingWorkflowStatusResult notChanged(Message... messages) {
        return notChanged(List.of(messages));
    }

    public static ChangingWorkflowStatusResult notChanged(Collection<Message> messages) {
        return new ChangingWorkflowStatusResult(null, null, null, false, messages);
    }

    public ChangingWorkflowStatusResult(Workflow.WorkflowStatus oldStatus, Workflow.WorkflowStatus newStatus, Workflow workflow, boolean anyMailingDeactivated, Collection<Message> messages) {
        this.workflow = workflow;
        this.anyMailingDeactivated = anyMailingDeactivated;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
        this.messages = Collections.unmodifiableCollection(messages);
    }

    public boolean isAnyMailingDeactivated() {
        return anyMailingDeactivated;
    }

    public boolean isChanged() {
        return !Objects.equals(oldStatus, newStatus);
    }

    public Collection<Message> getMessages() {
        return messages;
    }

    public Workflow.WorkflowStatus getOldStatus() {
        return oldStatus;
    }

    public Workflow.WorkflowStatus getNewStatus() {
        return newStatus;
    }

    public Workflow getWorkflow() {
        return workflow;
    }
}
