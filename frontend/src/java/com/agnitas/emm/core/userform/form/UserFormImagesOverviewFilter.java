/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.form;

import com.agnitas.beans.FormComponent;
import com.agnitas.emm.core.commons.dto.DateRange;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class UserFormImagesOverviewFilter extends PaginationForm {

    private String fileName;
    private String description;
    private DateRange uploadDate = new DateRange();
    private Integer fileSizeMin;
    private Integer fileSizeMax;
    private Integer heightMin;
    private Integer heightMax;
    private Integer widthMin;
    private Integer widthMax;
    private int companyId;
    private int formId;
    private FormComponent.FormComponentType type = FormComponent.FormComponentType.IMAGE;
    private List<String> mimetypes;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public DateRange getUploadDate() {
        return uploadDate;
    }

    public void setUploadDate(DateRange uploadDate) {
        this.uploadDate = uploadDate;
    }

    public Integer getFileSizeMin() {
        return fileSizeMin;
    }

    public void setFileSizeMin(Integer fileSizeMin) {
        this.fileSizeMin = fileSizeMin;
    }

    public Integer getFileSizeMax() {
        return fileSizeMax;
    }

    public void setFileSizeMax(Integer fileSizeMax) {
        this.fileSizeMax = fileSizeMax;
    }

    public Integer getHeightMin() {
        return heightMin;
    }

    public void setHeightMin(Integer heightMin) {
        this.heightMin = heightMin;
    }

    public Integer getHeightMax() {
        return heightMax;
    }

    public void setHeightMax(Integer heightMax) {
        this.heightMax = heightMax;
    }

    public Integer getWidthMin() {
        return widthMin;
    }

    public void setWidthMin(Integer widthMin) {
        this.widthMin = widthMin;
    }

    public Integer getWidthMax() {
        return widthMax;
    }

    public void setWidthMax(Integer widthMax) {
        this.widthMax = widthMax;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public FormComponent.FormComponentType getType() {
        return type;
    }

    public List<String> getMimetypes() {
        return mimetypes;
    }

    public void setMimetypes(List<String> mimetypes) {
        this.mimetypes = mimetypes;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(fileName) || isNotBlank(description) || uploadDate.isPresent() || fileSizeMin != null || fileSizeMax != null
                || heightMin != null || heightMax != null || widthMin != null || widthMax != null || CollectionUtils.isNotEmpty(mimetypes);
    }
}
