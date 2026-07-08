/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.imports.wizard.form;

import org.springframework.web.multipart.MultipartFile;

public class ImportWizardFileStepForm {

    private int attachmentCsvFileId;
    private boolean useCsvUpload;
    private MultipartFile csvFile;

    public int getAttachmentCsvFileId() {
        return attachmentCsvFileId;
    }

    public void setAttachmentCsvFileId(int attachmentCsvFileId) {
        this.attachmentCsvFileId = attachmentCsvFileId;
    }

    public boolean isUseCsvUpload() {
        return useCsvUpload;
    }

    public void setUseCsvUpload(boolean useCsvUpload) {
        this.useCsvUpload = useCsvUpload;
    }

    public MultipartFile getCsvFile() {
        return csvFile;
    }

    public void setCsvFile(MultipartFile csvFile) {
        this.csvFile = csvFile;
    }
}
