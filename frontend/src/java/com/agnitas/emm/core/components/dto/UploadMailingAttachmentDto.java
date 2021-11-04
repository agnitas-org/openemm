/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.dto;

import org.springframework.web.multipart.MultipartFile;

import com.agnitas.emm.core.components.form.AttachmentType;

public class UploadMailingAttachmentDto {
    private boolean usePdfUpload;
    private int uploadId;
    private int targetId;
    private MultipartFile attachmentFile;
    private String name;
    private MultipartFile backgroundFile;
    private AttachmentType type;

    public void setUseUpload(boolean usePdfUpload) {
        this.usePdfUpload = usePdfUpload;
    }

    public boolean isUsePdfUpload() {
        return usePdfUpload;
    }

    public void setUploadId(int uploadId) {
        this.uploadId = uploadId;
    }

    public int getUploadId() {
        return uploadId;
    }

    public void setTargetId(int targetId) {
        this.targetId = targetId;
    }

    public int getTargetId() {
        return targetId;
    }

    public void setAttachmentFile(MultipartFile attachmentFile) {
        this.attachmentFile = attachmentFile;
    }

    public MultipartFile getAttachmentFile() {
        return attachmentFile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setBackgroundFile(MultipartFile backgroundFile) {
        this.backgroundFile = backgroundFile;
    }

    public MultipartFile getBackgroundFile() {
        return backgroundFile;
    }

    public void setType(AttachmentType type) {
        this.type = type;
    }

    public AttachmentType getType() {
        return type;
    }
}
