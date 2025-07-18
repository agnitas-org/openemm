/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.forms.FormDateTime;
import com.agnitas.web.forms.PaginationForm;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class RecipientsReportForm extends PaginationForm {

    // TODO: EMMGUI-714: remove this old fields when old design will be removed
    private RecipientsReport.RecipientReportType[] filterTypes;

    // TODO: EMMGUI-714: remove this old fields when old design will be removed
    private FormDateTime filterDateStart = new FormDateTime();

    // TODO: EMMGUI-714: remove this old fields when old design will be removed
    private FormDateTime filterDateFinish = new FormDateTime();

    public RecipientsReport.RecipientReportType[] getFilterTypes() {
        return filterTypes;
    }

    public void setFilterTypes(RecipientsReport.RecipientReportType[] filterTypes) {
        this.filterTypes = filterTypes;
    }

    public FormDateTime getFilterDateStart() {
        return filterDateStart;
    }

    public FormDateTime getFilterDateFinish() {
        return filterDateFinish;
    }

    private Integer datasourceId;
    private int adminId;
    private String fileName;
    private RecipientsReport.EntityType[] types;
    private DateRange reportDate = new DateRange();

    public Integer getDatasourceId() {
        return datasourceId;
    }

    public void setDatasourceId(Integer datasourceId) {
        this.datasourceId = datasourceId;
    }

    public int getAdminId() {
        return adminId;
    }

    public void setAdminId(int adminId) {
        this.adminId = adminId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public RecipientsReport.EntityType[] getTypes() {
        return types;
    }

    public void setTypes(RecipientsReport.EntityType[] types) {
        this.types = types;
    }

    public DateRange getReportDate() {
        return reportDate;
    }

    public void setReportDate(DateRange reportDate) {
        this.reportDate = reportDate;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(fileName) || (types != null && types.length > 0) || reportDate.isPresent()
                || datasourceId != null || adminId > 0;
    }

    @Override
    public boolean ascending() {
        return AgnUtils.sortingDirectionToBoolean(getOrder(), false);
    }
}
