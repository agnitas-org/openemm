/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.form;

import com.agnitas.emm.core.commons.dto.DateRange;
import org.agnitas.web.forms.FormSearchParams;

import java.util.List;

public class MailingImagesFormSearchParams implements FormSearchParams<MailingImagesOverviewFilter> {

    private String fileName;
    private DateRange uploadDate = new DateRange();
    private Boolean isMobile;
    private List<String> mimetypes;

    @Override
    public void storeParams(MailingImagesOverviewFilter form) {
        this.fileName = form.getFileName();
        this.uploadDate = form.getUploadDate();
        this.mimetypes = form.getMimetypes();
        this.isMobile = form.getMobile();
    }

    @Override
    public void restoreParams(MailingImagesOverviewFilter form) {
        form.setFileName(fileName);
        form.setUploadDate(uploadDate);
        form.setMimetypes(mimetypes);
        form.setMobile(isMobile);
    }

    @Override
    public void resetParams() {
        fileName = null;
        uploadDate = new DateRange();
        mimetypes = null;
        isMobile = null;
    }
}
