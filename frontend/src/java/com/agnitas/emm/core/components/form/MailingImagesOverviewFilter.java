/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.form;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class MailingImagesOverviewFilter extends PaginationForm {

    private String fileName;
    private DateRange uploadDate = new DateRange();
    private Boolean isMobile;
    private List<String> mimetypes;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public DateRange getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(DateRange uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Boolean getMobile() {
        return isMobile;
    }

    public void setMobile(Boolean mobile) {
        isMobile = mobile;
    }

    public List<String> getMimetypes() {
        return mimetypes;
    }

    public void setMimetypes(List<String> mimetypes) {
        this.mimetypes = mimetypes;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(fileName) || isMobile != null || uploadDate.isPresent() || CollectionUtils.isNotEmpty(mimetypes);
    }
}
