/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.web.forms;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.beans.Campaign;
import org.agnitas.web.forms.StrutsFormBase;
import org.apache.commons.lang.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.emm.core.workflow.beans.Workflow;

public class ComWorkflowForm extends StrutsFormBase {
	private static final long serialVersionUID = -7240368411788505349L;

	public static final int SHORTNAME_MIN_LENGTH = 3;

    private Workflow workflow = new Workflow();
    // JSON data from workflow editor
    private String schema = "[]";
    private String workflowUndoHistoryData = "[]";
    private String usingActivatedWorkflow = "";
    private String usingActivatedWorkflowName = "";
    private String partOfActivatedWorkflow = "";
    private String partOfActivatedWorkflowName = "";
    private String newStatus = Workflow.WorkflowStatus.STATUS_OPEN.name();
    protected List<Campaign> campaigns;
    private Set<Integer> bulkIDs = new HashSet<>();

    // For dependency request
    private int type;
    private int entityId;
    private String entityName;

    @Override
    public ActionErrors formSpecificValidate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        String method = request.getParameter("method");
        String forwardName = request.getParameter("forwardName");
        if ("save".equals(method) && StringUtils.isEmpty(forwardName)) {
            if (StringUtils.length(workflow.getShortname()) < SHORTNAME_MIN_LENGTH) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage("error.name.too.short"));
            }
        }
        return errors;
    }

    @Override
    public void reset(ActionMapping map, HttpServletRequest request) {
        super.reset(map, request);
        clearBulkIds();
        setNumberOfRows(-1);

        type = 0;
        entityId = 0;
        entityName = null;
    }

    public void setBulkID(int id, String value) {
        if (value != null && (value.equals("on") || value.equals("yes") || value.equals("true")))
            this.bulkIDs.add(id);
    }

    public String getBulkID(int id) {
        return this.bulkIDs.contains(id) ? "on" : "";
    }

    public Set<Integer> getBulkIds() {
        return this.bulkIDs;
    }

    public void clearBulkIds() {
        this.bulkIDs.clear();
    }

    /**
     * The reset form from StrutsFormBase is not used, because tomcat would call
     * it every time before populating the values...
     */
    public void resetFormValues(ActionMapping mapping, HttpServletRequest request) {
        workflow.setWorkflowId(0);
        workflow.setCompanyId(0);
        workflow.setShortname("");
        workflow.setDescription("");
        workflow.setStatus(Workflow.WorkflowStatus.STATUS_OPEN);
        workflow.setEditorPositionLeft(0);
        workflow.setEditorPositionTop(0);
		workflow.setInner(false);

        setSchema("[]");
        setWorkflowUndoHistoryData("[]");
        setUsingActivatedWorkflow("");
        setUsingActivatedWorkflowName("");
        setPartOfActivatedWorkflow("");
        setPartOfActivatedWorkflowName("");
    }

    public Workflow getWorkflow() {
        return workflow;
    }

    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

    public int getWorkflowId() {
        return workflow.getWorkflowId();
    }

    public void setWorkflowId(int workflowId) {
        workflow.setWorkflowId(workflowId);
    }

    public String getSchema() {
        return schema;
    }

    public void setSchema(String schema) {
        this.schema = schema;
    }

    public List<Campaign> getCampaigns() {
        return campaigns;
    }

    public void setCampaigns(List<Campaign> campaigns) {
        this.campaigns = campaigns;
    }

    public int getEditorPositionTop() {
        return workflow.getEditorPositionTop();
    }

    public void setEditorPositionTop(int editorPositionTop) {
        workflow.setEditorPositionTop(editorPositionTop);
    }

    public int getEditorPositionLeft() {
        return workflow.getEditorPositionLeft();
    }

    public void setEditorPositionLeft(int editorPositionLeft) {
        workflow.setEditorPositionLeft(editorPositionLeft);
    }

    public String getNewStatus() {
        return newStatus;
    }

    public void setNewStatus(String newStatus) {
        this.newStatus = newStatus;
    }

    public String getWorkflowUndoHistoryData() {
        return workflowUndoHistoryData;
    }

    public void setWorkflowUndoHistoryData(String workflowUndoHistoryData) {
        this.workflowUndoHistoryData = workflowUndoHistoryData;
    }

    public String getUsingActivatedWorkflow() {
        return usingActivatedWorkflow;
    }

    public void setUsingActivatedWorkflow(String usingActivatedWorkflow) {
        this.usingActivatedWorkflow = usingActivatedWorkflow;
    }
    public String getUsingActivatedWorkflowName() {
        return usingActivatedWorkflowName;
    }

    public void setUsingActivatedWorkflowName(String usingActivatedWorkflowName) {
        this.usingActivatedWorkflowName = usingActivatedWorkflowName;
    }

    public String getPartOfActivatedWorkflow() {
        return partOfActivatedWorkflow;
    }

    public void setPartOfActivatedWorkflow(String partOfActivatedWorkflow) {
        this.partOfActivatedWorkflow = partOfActivatedWorkflow;
    }

    public String getPartOfActivatedWorkflowName() {
        return partOfActivatedWorkflowName;
    }

    public void setPartOfActivatedWorkflowName(String partOfActivatedWorkflowName) {
        this.partOfActivatedWorkflowName = partOfActivatedWorkflowName;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getEntityId() {
        return entityId;
    }

    public void setEntityId(int entityId) {
        this.entityId = entityId;
    }

    public String getEntityName() {
        return entityName;
    }

    public void setEntityName(String entityName) {
        this.entityName = entityName;
    }
}
