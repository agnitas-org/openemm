/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.preview.form;

import java.util.List;

import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import org.agnitas.preview.ModeType;
import org.agnitas.preview.Preview;

public class PreviewForm {

    private int mailingId;
    private String mailingShortname;
    private int mailingTemplateId;
    private boolean isTemplate;
    private boolean isMailingUndoAvailable;
    private int workflowId;
    private int format = MailingPreviewHelper.INPUT_TYPE_HTML;
    private String previewContent;
    private int size = Preview.Size.DESKTOP.getValue();
    private ModeType modeType = ModeType.RECIPIENT;
    private String subject;
    private String preHeader;
    private String senderEmail;
    private String smsAddress;
    private int customerID;
    private int customerATID;
    private String customerEmail;
    private boolean useCustomerEmail; // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    private boolean noImages;
    private boolean pure;
    private int targetGroupId;
    private boolean isMailingGrid;
    private int emailFormat;
    private boolean reload;
    private String mediaQuery;
    private String width;
    private boolean anon;
    private boolean onAnonPreserveLinks;
    private List<String> personalizedTestRunRecipients;

    public int getMailingId() {
        return mailingId;
    }

    public void setMailingId(int mailingId) {
        this.mailingId = mailingId;
    }

    public boolean isIsMailingGrid() {
        return isMailingGrid;
    }

    public void setMailingGrid(boolean mailingGrid) {
        isMailingGrid = mailingGrid;
    }

    public int getEmailFormat() {
        return emailFormat;
    }

    public void setEmailFormat(int emailFormat) {
        this.emailFormat = emailFormat;
    }

    public int getFormat() {
        return format;
    }

    public void setFormat(int format) {
        this.format = format;
    }

    public String getPreviewContent() {
        return previewContent;
    }

    public void setPreviewContent(String previewContent) {
        this.previewContent = previewContent;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getModeTypeId() {
        return modeType != null ? modeType.getCode() : 0;
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public void setModeTypeId(int modeTypeId) {
        setModeType(ModeType.getByCode(modeTypeId));
    }

    public ModeType getModeType() {
        return modeType;
    }

    public void setModeType(ModeType modeType) {
        if (modeType == null) {
            modeType = ModeType.RECIPIENT;
        }

        this.modeType = modeType;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getSenderEmail() {
        return senderEmail;
    }

    public void setSenderEmail(String senderEmail) {
        this.senderEmail = senderEmail;
    }

    public int getCustomerID() {
        return customerID;
    }

    public void setCustomerID(int customerID) {
        this.customerID = customerID;
    }

    public int getCustomerATID() {
        return customerATID;
    }

    public void setCustomerATID(int customerATID) {
        this.customerATID = customerATID;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public void setCustomerEmail(String customerEmail) {
        this.customerEmail = customerEmail;
    }

    public boolean isUseCustomerEmail() {
        return useCustomerEmail;
    }

    public void setUseCustomerEmail(boolean useCustomerEmail) {
        this.useCustomerEmail = useCustomerEmail;
    }

    public boolean isNoImages() {
        return noImages;
    }

    public void setNoImages(boolean noImages) {
        this.noImages = noImages;
    }

    public String getWidth() {
        return width;
    }

    public void setWidth(String width) {
        this.width = width;
    }

    public boolean isContainsHeader() {
        if (format == MediaTypes.EMAIL.getMediaCode()) {
            return true;
        }
        int mediaTypeCode = format - 1;
        return mediaTypeCode == MediaTypes.EMAIL.getMediaCode();
    }

    public boolean isPure() {
        return pure;
    }

    public void setPure(boolean pure) {
        this.pure = pure;
    }

    public boolean isReload() {
        return reload;
    }

    public void setReload(boolean reload) {
        this.reload = reload;
    }

    public int getTargetGroupId() {
        return targetGroupId;
    }

    public void setTargetGroupId(int targetGroupId) {
        this.targetGroupId = targetGroupId;
    }

    public String getMediaQuery() {
        return mediaQuery;
    }

    public void setMediaQuery(String mediaQuery) {
        this.mediaQuery = mediaQuery;
    }

    public String getMailingShortname() {
        return mailingShortname;
    }

    public void setMailingShortname(String mailingShortname) {
        this.mailingShortname = mailingShortname;
    }

    public int getMailingTemplateId() {
        return mailingTemplateId;
    }

    public void setMailingTemplateId(int mailingTemplateId) {
        this.mailingTemplateId = mailingTemplateId;
    }

    public boolean getIsTemplate() {
        return isTemplate;
    }

    public void setTemplate(boolean template) {
        isTemplate = template;
    }

    public boolean getIsMailingUndoAvailable() {
        return isMailingUndoAvailable;
    }

    public void setMailingUndoAvailable(boolean mailingUndoAvailable) {
        isMailingUndoAvailable = mailingUndoAvailable;
    }

    public boolean isMailingGrid() {
        return isMailingGrid;
    }

    public int getWorkflowId() {
        return workflowId;
    }

    public void setWorkflowId(int workflowId) {
        this.workflowId = workflowId;
    }

    public String getPreHeader() {
        return preHeader;
    }

    public void setPreHeader(String preHeader) {
        this.preHeader = preHeader;
    }

    public boolean isAnon() {
        return anon;
    }

    public void setAnon(boolean anon) {
        this.anon = anon;
    }

    public boolean isOnAnonPreserveLinks() {
        return onAnonPreserveLinks;
    }

    public void setOnAnonPreserveLinks(boolean onAnonPreserveLinks) {
        this.onAnonPreserveLinks = onAnonPreserveLinks;
    }

    public List<String> getPersonalizedTestRunRecipients() {
        return personalizedTestRunRecipients;
    }

    public void setPersonalizedTestRunRecipients(List<String> personalizedTestRunRecipients) {
        this.personalizedTestRunRecipients = personalizedTestRunRecipients;
    }

    public String getSmsAddress() {
        return smsAddress;
    }

    public void setSmsAddress(String smsAddress) {
        this.smsAddress = smsAddress;
    }
}
