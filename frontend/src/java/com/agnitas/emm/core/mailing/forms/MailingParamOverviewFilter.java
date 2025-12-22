/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.forms;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.forms.PaginationForm;

public class MailingParamOverviewFilter extends PaginationForm {

    private Integer mailingId;
    private String name;
    private String description;
    private DateRange changeDate = new DateRange();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getMailingId() {
        return mailingId;
    }

    public void setMailingId(Integer mailingId) {
        this.mailingId = mailingId;
    }

    public DateRange getChangeDate() {
        return changeDate;
    }

    public void setChangeDate(DateRange changeDate) {
        this.changeDate = changeDate;
    }

    @Override
    public boolean ascending() {
        return AgnUtils.sortingDirectionToBoolean(getOrder(), false);
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(name) || isNotBlank(description) || changeDate.isPresent() || mailingId != null;
    }
}
