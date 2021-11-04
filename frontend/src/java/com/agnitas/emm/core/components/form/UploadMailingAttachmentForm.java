/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.form;

import org.springframework.web.multipart.MultipartFile;

public class UploadMailingAttachmentForm {

    private String attachmentName;
    private MultipartFile attachment;
    private AttachmentType type = AttachmentType.NORMAL;
    private MultipartFile backgroundAttachment;
    private int targetId;
    private boolean usePdfUpload;
    private int pdfUploadId;

    public String getAttachmentName() {
        return attachmentName;
    }

    public void setAttachmentName(String attachmentName) {
        this.attachmentName = attachmentName;
    }

    public MultipartFile getAttachment() {
        return attachment;
    }

    public void setAttachment(MultipartFile attachment) {
        this.attachment = attachment;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public boolean isUsePdfUpload() {
        return usePdfUpload;
    }

    public void setUsePdfUpload(boolean usePdfUpload) {
        this.usePdfUpload = usePdfUpload;
    }

    public int getPdfUploadId() {
        return pdfUploadId;
    }

    public void setPdfUploadId(int pdfUploadId) {
        this.pdfUploadId = pdfUploadId;
    }

    public AttachmentType getType() {
        return type;
    }

    public void setType(AttachmentType type) {
        this.type = type;
    }

    public MultipartFile getBackgroundAttachment() {
        return backgroundAttachment;
    }

    public void setBackgroundAttachment(MultipartFile backgroundAttachment) {
        this.backgroundAttachment = backgroundAttachment;
    }
}
