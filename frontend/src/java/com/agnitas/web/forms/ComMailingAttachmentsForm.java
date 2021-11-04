/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.web.forms.StrutsFormBase;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

public class ComMailingAttachmentsForm extends StrutsFormBase {

    private static final long serialVersionUID = 9042895119857835688L;

	/**
     * Holds value of property mailingID.
     */
    private int mailingID;

    /**
     * Holds value of property shortname.
     */
    private String shortname;

    /**
     * Holds value of property description.
     */
    private String description;

    /**
     * Holds value of property action.
     */
    private int action;

    /**
     *  Holds value of property newAttachmentType.
     */
    private int newAttachmentType;

    /**
     * Holds value of property NewFile.
     */
    private FormFile newFile;

    /**
     * Holds value of property isTemplate.
     */
    private boolean isTemplate;

    /**
     *  Holds value of property newAttachmentBackground.
     */
    private FormFile newAttachmentBackground;

    /**
     *  Holds value of property attachmentTargetID.
     */
    private int attachmentTargetID;

     /**
     * Holds value of property worldMailingSend.
     */
    @Deprecated // Replace by request attribute
    private boolean worldMailingSend;

    /**
     *  Holds value of property attachmentPdfFileID.
     */
    private int attachmentPdfFileID;

    /**
     *  Holds value of property usePdfUpload.
     */

    private boolean usePdfUpload;

    private int attachmentId;

    private String attachmentName;

    private int workflowId;

    private boolean isMailingUndoAvailable;

    /**
     * Reset all properties to their default values.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     */
    @Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {

        this.mailingID = 0;
        this.shortname = ""; // text.getMessage(aLoc, "default.Name");
    }

    /**
     * Validate the properties that have been set from this HTTP request,
     * and return an <code>ActionErrors</code> object that encapsulates any
     * validation errors that have been found.  If no errors are found, return
     * <code>null</code> or an <code>ActionErrors</code> object with no
     * recorded error messages.
     *
     * @param mapping The mapping used to select this instance
     * @param request The servlet request we are processing
     * @return errors
     */
    @Override
	public ActionErrors formSpecificValidate(ActionMapping mapping,
            HttpServletRequest request) {

        ActionErrors errors = new ActionErrors();

        return errors;
    }

    /**
     * Getter for property mailingID.
     *
     * @return Value of property mailingID.
     */
    public int getMailingID() {
        return this.mailingID;
    }

    /**
     * Setter for property mailingID.
     *
     * @param mailingID New value of property mailingID.
     */
    public void setMailingID(int mailingID) {
        this.mailingID = mailingID;
    }

    /**
     * Getter for property shortname.
     *
     * @return Value of property shortname.
     */
    public String getShortname() {
        return this.shortname;
    }

    /**
     * Setter for property shortname.
     *
     * @param shortname New value of property shortname.
     */
    public void setShortname(String shortname) {
        this.shortname = shortname;
    }

    /**
     * Getter for property description.
     *
     * @return Value of property description.
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Setter for property description.
     *
     * @param description New value of property description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Getter for property action.
     *
     * @return Value of property action.
     */
    public int getAction() {
        return this.action;
    }

    /**
     * Setter for property action.
     *
     * @param action New value of property action.
     */
    public void setAction(int action) {
        this.action = action;
    }

    /** Getter for property newAttachmentType.
     * @return Value of property newAttachmentType.
     *
     */
    public int getNewAttachmentType() {
        return this.newAttachmentType;
    }

    /** Setter for property newAttachmentType.
     * @param newAttachmentType New value of property newAttachmentType.
     *
     */
    public void setNewAttachmentType(int newAttachmentType) {
        this.newAttachmentType = newAttachmentType;
    }

    /**
     * Getter for property NewFile.
     *
     * @return Value of property NewFile.
     */
    public FormFile getNewAttachment() {
        return this.newFile;
    }

    /**
     * Setter for property NewFile.
     *
     * @param newImage
     */
    public void setNewAttachment(FormFile newImage) {
        this.newFile = newImage;
    }

    /**
     * Getter for property isTemplate.
     *
     * @return Value of property isTemplate.
     */
    public boolean isIsTemplate() {
        return this.isTemplate;
    }

    /**
     * Setter for property isTemplate.
     *
     * @param isTemplate New value of property isTemplate.
     */
    public void setIsTemplate(boolean isTemplate) {
        this.isTemplate = isTemplate;
    }

    /**
     * Holds value of property newTargetID.
     */
    private int newTargetID;

    /**
     * Getter for property newTargetID.
     *
     * @return Value of property newTargetID.
     */
    public int getNewTargetID() {
        return this.newTargetID;
    }

    /**
     * Setter for property newTargetID.
     *
     * @param newTargetID New value of property newTargetID.
     */
    public void setNewTargetID(int newTargetID) {
        this.newTargetID = newTargetID;
    }

    /**
     * Holds value of property newAttachmentName.
     */
    private String newAttachmentName;

    /**
     * Getter for property newAttachmentName.
     *
     * @return Value of property newAttachmentName.
     */
    public String getNewAttachmentName() {
        return this.newAttachmentName;
    }

    /**
     * Setter for property newAttachmentName.
     *
     * @param newAttachmentName New value of property newAttachmentName.
     */
    public void setNewAttachmentName(String newAttachmentName) {
        this.newAttachmentName = newAttachmentName;
    }

    /** Getter for property newAttachmentBackground.
     * @return Value of property newAttachmentBackground.
     *
     */
    public FormFile getNewAttachmentBackground() {
        return this.newAttachmentBackground;
    }

    /** Setter for property newAttachmentBackground.
     * @param newAttachmentBackground New value of property newAttachmentBackground.
     *
     */
    public void setNewAttachmentBackground(FormFile newAttachmentBackground) {
        this.newAttachmentBackground = newAttachmentBackground;
    }

    /** Getter for property attachmentTargetID.
     * @return Value of property attachmentTargetID.
     */
    public int getAttachmentTargetID() {
        return this.attachmentTargetID;
    }

    /** Setter for property attachmentTargetID.
     * @param attachmentTargetID New value of property attachmentTargetID.
     */
    public void setAttachmentTargetID(int attachmentTargetID) {
        this.attachmentTargetID = attachmentTargetID;
    }

    /** Getter for property attachmentPdfFileID.
     * @return Value of property attachmentPdfFileID.
     */
    public int getAttachmentPdfFileID() {
        return attachmentPdfFileID;
    }

    /** Setter for property attachmentPdfFileID.
     * @param attachmentPdfFileID New value of property attachmentPdfFileID.
     */
    public void setAttachmentPdfFileID(int attachmentPdfFileID) {
        this.attachmentPdfFileID = attachmentPdfFileID;
    }

    public boolean isUsePdfUpload() {
        return usePdfUpload;
    }

    public void setUsePdfUpload(boolean usePdfUpload) {
        this.usePdfUpload = usePdfUpload;
    }

    @Deprecated // Replace by request attribute
    public boolean isWorldMailingSend() {
        return worldMailingSend;
    }

    @Deprecated // Replace by request attribute
    public void setWorldMailingSend(boolean worldMailingSend) {
        this.worldMailingSend = worldMailingSend;
    }

    public int getAttachmentId() {
        return attachmentId;
    }

    public void setAttachmentId(int attachmentId) {
        this.attachmentId = attachmentId;
    }

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public boolean getIsMailingUndoAvailable() {
        return isMailingUndoAvailable;
    }

    public void setIsMailingUndoAvailable(boolean isMailingUndoAvailable) {
        this.isMailingUndoAvailable = isMailingUndoAvailable;
    }
}
