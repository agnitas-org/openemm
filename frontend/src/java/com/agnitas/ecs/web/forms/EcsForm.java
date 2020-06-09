/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.ecs.web.forms;

import java.util.Collection;
import java.util.Map;

import org.agnitas.ecs.EcsGlobals;
import org.agnitas.ecs.backend.beans.ClickStatColor;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.web.forms.StrutsFormBase;

public class EcsForm extends StrutsFormBase {
	private static final long serialVersionUID = 6732744608407196246L;

	/**
     * mailing id
     */
    protected int mailingID;

    /**
     * Holds value of property shortname.
     */
    protected String shortname;

    /**
     * test and admin recipients for drop-down box
     */
    protected Map<Integer, String> testRecipients;

    /**
     * selected ECS view mode
     */
    protected int viewMode = EcsGlobals.MODE_GROSS_CLICKS;

    /**
     * selected recipient for mailing content generation
     */
    protected int selectedRecipient;

    /**
     * color codes and ranges for heatmap
     */
    protected Collection<ClickStatColor> rangeColors;

    /**
     * companyId - is passed to ECS-servlet as a parameter
     */
    protected int companyId;

    /**
     * URL of statistics server
     */
    protected String statServerUrl;

    /**
     * select device type for calculating clicks and clickers
     */
    private int deviceType = 0;

    private int workflowId;

    /**
     * isMailingGrid used to compose correct view
     */
    private boolean isMailingGrid = false;

    private int templateId;

    private boolean isMailingUndoAvailable;

    public int getMailingID() {
        return mailingID;
    }

    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    public String getShortname() {
        return shortname;
    }

    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    public int getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(int deviceType) {
        this.deviceType = deviceType;
    }

    public int getSelectedRecipient() {
        return selectedRecipient;
    }

    public void setSelectedRecipient(int selectedRecipient) {
        this.selectedRecipient = selectedRecipient;
    }

    public Map<Integer, String> getTestRecipients() {
        return testRecipients;
    }

    public void setTestRecipients(Map<Integer, String> testRecipients) {
        this.testRecipients = testRecipients;
    }

    public int getViewMode() {
        return viewMode;
    }

    public void setViewMode(int viewMode) {
        this.viewMode = viewMode;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(@VelocityCheck int companyId) {
        this.companyId = companyId;
    }

    public Collection<ClickStatColor> getRangeColors() {
        return rangeColors;
    }

    public void setRangeColors(Collection<ClickStatColor> rangeColors) {
        this.rangeColors = rangeColors;
    }

    public String getStatServerUrl() {
        return statServerUrl;
    }

    public void setStatServerUrl(String statServerUrl) {
        this.statServerUrl = statServerUrl;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public boolean isIsMailingGrid() {
        return isMailingGrid;
    }

    public void setIsMailingGrid(boolean isMailingGrid) {
        this.isMailingGrid = isMailingGrid;
    }

    public int getTemplateId() {
        return templateId;
    }

    public void setTemplateId(int templateId) {
        this.templateId = templateId;
    }

    public boolean getIsMailingUndoAvailable() {
        return isMailingUndoAvailable;
    }

    public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
        this.isMailingUndoAvailable = isMailingUndoAvailable;
    }
}
