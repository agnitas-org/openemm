/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.form;

import com.agnitas.web.forms.PaginationForm;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class BounceFilterListForm extends PaginationForm {

    private int companyId;
    private String name;
    private String description;
    private String filterAddress;
    private String companyDomain;

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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getFilterAddress() {
        return filterAddress;
    }

    public void setFilterAddress(String filterAddress) {
        this.filterAddress = filterAddress;
    }

    public String getCompanyDomain() {
        return companyDomain;
    }

    public void setCompanyDomain(String companyDomain) {
        this.companyDomain = companyDomain;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(name) || isNotBlank(description) || isNotBlank(filterAddress);
    }
}
