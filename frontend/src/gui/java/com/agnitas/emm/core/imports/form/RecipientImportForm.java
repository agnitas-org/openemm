/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.imports.form;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.web.forms.PaginationForm;
import org.springframework.web.multipart.MultipartFile;

public class RecipientImportForm extends PaginationForm {

    private int uploadId;
    private int profileId;
    private MultipartFile uploadFile;
    private int invalidRecipientsSize;
    private final Map<Integer, String> selectedMailinglists = new HashMap<>();

    public int getUploadId() {
        return uploadId;
    }

    public void setUploadId(int uploadId) {
        this.uploadId = uploadId;
    }

    public int getProfileId() {
        return profileId;
    }

    public void setProfileId(int profileId) {
        this.profileId = profileId;
    }

    public MultipartFile getUploadFile() {
        return uploadFile;
    }

    public void setUploadFile(MultipartFile uploadFile) {
        this.uploadFile = uploadFile;
    }

    public Map<Integer, String> getSelectedMailinglist() {
        return selectedMailinglists;
    }

    public void setSelectedMailinglists(Map<Integer, String> map) {
        this.selectedMailinglists.clear();
        this.selectedMailinglists.putAll(map);
    }

    public int getInvalidRecipientsSize() {
        return invalidRecipientsSize;
    }

    public void setInvalidRecipientsSize(int invalidRecipientsSize) {
        this.invalidRecipientsSize = invalidRecipientsSize;
    }
}
