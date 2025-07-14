/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans;

import com.agnitas.emm.core.workflow.service.util.WorkflowUtils.Deadline;

public interface WorkflowReactionStepDeclaration {
    int getStepId();
    void setStepId(int stepId);
    int getPreviousStepId();
    void setPreviousStepId(int previousStepId);
    int getReactionId();
    void setReactionId(int reactionId);
    int getCompanyId();
    void setCompanyId(int companyId);
    Deadline getDeadline();
    void setDeadline(Deadline deadline);
    int getTargetId();
    void setTargetId(int targetId);
    boolean isTargetPositive();
    void setTargetPositive(boolean isTargetPositive);
    int getMailingId();
    void setMailingId(int mailingId);
}
