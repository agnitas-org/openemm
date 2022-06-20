/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import org.agnitas.preview.ModeType;
import org.agnitas.preview.Preview;

import com.agnitas.emm.core.mailing.web.MailingPreviewHelper;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;

public class PreviewForm {
    
    protected int format = MailingPreviewHelper.INPUT_TYPE_HTML;
    
    protected String previewContent;

    protected int size = Preview.Size.DESKTOP.getValue();
    
    protected ModeType modeType = ModeType.RECIPIENT;

    protected String subject;

    protected String senderEmail;
    
    protected int customerID;

    protected int customerATID;

    protected String customerEmail;

	protected boolean useCustomerEmail;
	
    protected boolean noImages;
    
    protected boolean pure;
    
    protected boolean hasPreviewRecipient;
    
    protected int targetGroupId;
    
    /**
     * Is use for GUI only
     */
    protected boolean reload;
    
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
    
    //getPreviewWidth
	public int getWidth(){
        int width;

        switch (size) {
            case 1:
                width = 1022;
                break;
            case 2:
                width = 320;
                break;
            case 3:
                width = 356;
                break;
            case 4:
                width = 768;
                break;
            case 5:
                width = 1024;
                break;
            default:
                size = Preview.Size.DESKTOP.getValue();
                width = 800;
                break;
        }

        return width + 2;
    }
    
    public String getMediaQuery(){
        String mediaQuery;

        switch (size) {
            case 2:
            case 3:
            case 4:
                mediaQuery = "true";
                break;
            case 5:
            case 1:
            default:
                mediaQuery = "false";
                break;
        }
        return mediaQuery;
    }
    
    public void setHasPreviewRecipient(boolean hasPreviewRecipient) {
        this.hasPreviewRecipient = hasPreviewRecipient;
    }
    
    public boolean isHasPreviewRecipient() {
        return hasPreviewRecipient;
    }
}
