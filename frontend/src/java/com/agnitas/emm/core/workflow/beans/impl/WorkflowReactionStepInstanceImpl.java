/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.beans.impl;

import java.util.Date;

import com.agnitas.emm.core.workflow.beans.WorkflowReactionStepInstance;

public class WorkflowReactionStepInstanceImpl implements WorkflowReactionStepInstance {
    private int caseId;
    private int stepId;
    private int previousStepId;
    private int reactionId;
    private int companyId;
    private Date date;
    private boolean isDone;

    @Override
    public int getCaseId() {
        return caseId;
    }

    @Override
    public void setCaseId(int caseId) {
        this.caseId = caseId;
    }

    @Override
    public int getStepId() {
        return stepId;
    }

    @Override
    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    @Override
    public int getPreviousStepId() {
        return previousStepId;
    }

    @Override
    public void setPreviousStepId(int previousStepId) {
        this.previousStepId = previousStepId;
    }

    @Override
    public int getReactionId() {
        return reactionId;
    }

    @Override
    public void setReactionId(int reactionId) {
        this.reactionId = reactionId;
    }

    @Override
    public int getCompanyId() {
        return companyId;
    }

    @Override
    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @Override
    public Date getDate() {
        return date;
    }

    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean isDone() {
        return isDone;
    }

    @Override
    public void setDone(boolean isDone) {
        this.isDone = isDone;
    }
}
