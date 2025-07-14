/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.web.forms.PaginationForm;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BirtReportOverviewFilter extends PaginationForm {

    private String name;
    private DateRange changeDate = new DateRange();
    private DateRange lastDeliveryDate = new DateRange();
    private boolean showDeleted;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public DateRange getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(DateRange changeDate) {
        this.changeDate = changeDate;
    }

    public DateRange getLastDeliveryDate() {
        return lastDeliveryDate;
    }

    public void setLastDeliveryDate(DateRange lastDeliveryDate) {
        this.lastDeliveryDate = lastDeliveryDate;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(name) || lastDeliveryDate.isPresent() || changeDate.isPresent();
    }

    public boolean isShowDeleted() {
        return showDeleted;
    }

    public void setShowDeleted(boolean showDeleted) {
        this.showDeleted = showDeleted;
    }
}
