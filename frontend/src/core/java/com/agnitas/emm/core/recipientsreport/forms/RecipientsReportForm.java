/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipientsreport.forms;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.recipientsreport.bean.RecipientsReport;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.forms.PaginationForm;

public class RecipientsReportForm extends PaginationForm {

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
