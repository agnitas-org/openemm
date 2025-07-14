/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.form;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.web.forms.FormSearchParams;

import java.util.List;

public class UserFormImagesFormSearchParams implements FormSearchParams<UserFormImagesOverviewFilter> {

    private String fileName;
    private String description;
    private DateRange uploadDate = new DateRange();
    private Integer fileSizeMin;
    private Integer fileSizeMax;
    private Integer heightMin;
    private Integer heightMax;
    private Integer widthMin;
    private Integer widthMax;
    private List<String> mimetypes;

    @Override
    public void storeParams(UserFormImagesOverviewFilter form) {
        this.fileName = form.getFileName();
        this.description = form.getDescription();
        this.uploadDate = form.getUploadDate();
        this.fileSizeMin = form.getFileSizeMin();
        this.fileSizeMax = form.getFileSizeMax();
        this.heightMin = form.getHeightMin();
        this.heightMax = form.getHeightMax();
        this.widthMin = form.getWidthMin();
        this.widthMax = form.getWidthMax();
        this.mimetypes = form.getMimetypes();
    }

    @Override
    public void restoreParams(UserFormImagesOverviewFilter form) {
        form.setFileName(fileName);
        form.setDescription(description);
        form.setUploadDate(uploadDate);
        form.setFileSizeMin(fileSizeMin);
        form.setFileSizeMax(fileSizeMax);
        form.setHeightMin(heightMin);
        form.setHeightMax(heightMax);
        form.setWidthMin(widthMin);
        form.setWidthMax(widthMax);
        form.setMimetypes(mimetypes);
    }

    @Override
    public void resetParams() {
        fileName = null;
        description = null;
        fileSizeMin = null;
        fileSizeMax = null;
        heightMin = null;
        heightMax = null;
        widthMin = null;
        widthMax = null;
        uploadDate = new DateRange();
        mimetypes = null;
    }
}
