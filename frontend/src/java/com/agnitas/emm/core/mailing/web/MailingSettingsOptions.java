/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.web;

import java.util.List;

import org.agnitas.web.forms.WorkflowParameters;

import com.agnitas.emm.core.mailing.bean.ComMailingParameter;

public class MailingSettingsOptions {

    private int adminId;
    private int newSplitId;
    private int mailingId;
    private int companyId;
    private int workflowId;
    private int gridTemplateId;
    private boolean isNew;
    private boolean activeOrSent;
    private boolean forCopy;
    private boolean isTemplate;
    private boolean forFollowUp;
    private boolean worldSend;
    private String sessionId;
    private List<ComMailingParameter> mailingParams;
    private WorkflowParameters workflowParams;

    public static Builder builder() {
        return new Builder();
    }

    public int getNewSplitId() {
        return newSplitId;
    }

    public int getAdminId() {
        return adminId;
    }

    public int getMailingId() {
        return mailingId;
    }

    public int getCompanyId() {
        return companyId;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public int getGridTemplateId() {
        return gridTemplateId;
    }

    public boolean isNew() {
        return isNew;
    }

    public boolean isActiveOrSent() {
        return activeOrSent;
    }

    public boolean isTemplate() {
        return isTemplate;
    }

    public boolean isForCopy() {
        return forCopy;
    }

    public boolean isForFollowUp() {
        return forFollowUp;
    }

    public boolean isWorldSend() {
        return worldSend;
    }

    public List<ComMailingParameter> getMailingParams() {
        return mailingParams;
    }

    public WorkflowParameters getWorkflowParams() {
        return workflowParams;
    }

    public String getSessionId() {
        return sessionId;
    }

    public static class Builder {
        private MailingSettingsOptions options = new MailingSettingsOptions();

        public Builder setNewSplitId(int newSplitId) {
            options.newSplitId = newSplitId;
            return this;
        }
        
        public Builder setAdminId(int adminId) {
            options.adminId = adminId;
            return this;
        }

        public Builder setMailingId(int mailingId) {
            options.mailingId = mailingId;
            return this;
        }

        public Builder setCompanyId(int companyId) {
            options.companyId = companyId;
            return this;
        }

        public Builder setWorkflowId(int workflowId) {
            options.workflowId = workflowId;
            return this;
        }

        public Builder setGridTemplateId(int gridTemplateId) {
            options.gridTemplateId = gridTemplateId;
            return this;
        }

        public Builder setActiveOrSent(boolean activeOrSent) {
            options.activeOrSent = activeOrSent;
            return this;
        }

        public Builder setIsNew(boolean isNew) {
            options.isNew = isNew;
            return this;
        }
        
        public Builder setIsTemplate(boolean isTemplate) {
            options.isTemplate = isTemplate;
            return this;
        }

        public Builder setForFollowUp(boolean forFollowUp) {
            options.forFollowUp = forFollowUp;
            return this;
        }

        public Builder setForCopy(boolean forCopy) {
            options.forCopy = forCopy;
            return this;
        }
        
        public Builder setWorldSend(boolean worldSend) {
            options.worldSend = worldSend;
            return this;
        }
        
        public Builder setMailingParams(List<ComMailingParameter> mailingParams) {
            options.mailingParams = mailingParams;
            return this;
        }

        public Builder setSessionId(String sessionId) {
            options.sessionId = sessionId;
            return this;
        }

        public Builder setWorkflowParams(WorkflowParameters workflowParams) {
            options.workflowParams = workflowParams;
            return this;
        }
        
        public MailingSettingsOptions build() {
            MailingSettingsOptions result = this.options;
            this.options = null;
            return result;
        }
    }
}
