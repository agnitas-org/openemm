/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.supervisor.form;

import com.agnitas.web.forms.PaginationForm;
import com.agnitas.emm.core.commons.dto.DateRange;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class SupervisorOverviewFilter extends PaginationForm {

    private int companyId;
    private String name;
    private String fullname;
    private Integer department;
    private Boolean isActive;
    private DateRange lastLoginDate = new DateRange();

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullname() {
        return fullname;
    }

    public void setFullname(String fullname) {
        this.fullname = fullname;
    }

    public Integer getDepartment() {
        return department;
    }

    public void setDepartment(Integer department) {
        this.department = department;
    }

    public Boolean getActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public DateRange getLastLoginDate() {
        return lastLoginDate;
    }

    public void setLastLoginDate(DateRange lastLoginDate) {
        this.lastLoginDate = lastLoginDate;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(name) || isNotBlank(fullname) || department != null || isActive != null;
    }
}
