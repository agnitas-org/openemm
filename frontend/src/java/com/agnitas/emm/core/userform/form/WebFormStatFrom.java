/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.form;

import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.commons.dto.DateRange;

import java.time.LocalDate;

public class WebFormStatFrom {

    private int formId;
    private int year = LocalDate.now().getYear();
    private int month = LocalDate.now().getMonthValue();
    private boolean allowedToChoseForm;
    private DateRange dateRange = new DateRange();
    private DateMode periodMode = DateMode.SELECT_MONTH;

    public int getFormId() {
        return formId;
    }

    public void setFormId(int formId) {
        this.formId = formId;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public DateRange getDateRange() {
        return dateRange;
    }

    public void setDateRange(DateRange dateRange) {
        this.dateRange = dateRange;
    }

    public DateMode getPeriodMode() {
        return periodMode;
    }

    public void setPeriodMode(DateMode periodMode) {
        this.periodMode = periodMode;
    }

    public boolean isAllowedToChoseForm() {
        return allowedToChoseForm;
    }

    public void setAllowedToChoseForm(boolean allowedToChoseForm) {
        this.allowedToChoseForm = allowedToChoseForm;
    }
}
