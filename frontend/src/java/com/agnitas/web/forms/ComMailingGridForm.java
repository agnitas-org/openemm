/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import java.util.HashMap;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.web.forms.MailingBaseForm;
import org.apache.struts.action.ActionMapping;

public class ComMailingGridForm extends MailingBaseForm {
	private static final long serialVersionUID = 8693352096908743593L;

	private Map<String, String> styles;

    private int workflowId;

    private int gridTemplateId;

    @Deprecated // Replace by request attribute
    private boolean isMailingUndoAvailable;

    /**
     * Is mailing not active {@link com.agnitas.emm.core.maildrop.service.MaildropService#isActiveMailing(int, int)}
     * or user has permission {@link com.agnitas.emm.core.Permission#MAILING_CONTENT_CHANGE_ALWAYS}
     */
    @Deprecated // Replace by request attribute
    private boolean mailingEditable = false;

    @Override
    public void reset(ActionMapping map, HttpServletRequest request) {
        super.reset(map, request);
        styles = new HashMap<>();
    }

    public Map<String, String> getStyles() {
        if (styles == null) {
            styles = new HashMap<>();
        }
        return styles;
    }

    public void setStyles(Map<String, String> styles) {
        this.styles = styles;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public int getGridTemplateId() {
        return gridTemplateId;
    }

    public void setGridTemplateId(int gridTemplateId) {
        this.gridTemplateId = gridTemplateId;
    }

    @Deprecated // Replace by request attribute
    public boolean getIsMailingUndoAvailable() {
        return isMailingUndoAvailable;
    }

    @Deprecated // Replace by request attribute
    public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
        this.isMailingUndoAvailable = isMailingUndoAvailable;
    }

    @Override
    @Deprecated // Replace by request attribute
    public boolean isMailingEditable() {
        return mailingEditable;
    }

    @Override
    @Deprecated // Replace by request attribute
    public void setMailingEditable(boolean mailingEditable) {
        this.mailingEditable = mailingEditable;
    }
}
