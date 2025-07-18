/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.form;

import com.agnitas.web.forms.PaginationForm;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class WebserviceUserOverviewFilter extends PaginationForm {

    private String username;
    private Integer status;
    private Integer companyId;
    private Integer defaultDataSourceId;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCompanyId() {
        return companyId;
    }

    public void setCompanyId(Integer companyId) {
        this.companyId = companyId;
    }

    public Integer getDefaultDataSourceId() {
        return defaultDataSourceId;
    }

    public void setDefaultDataSourceId(Integer defaultDataSourceId) {
        this.defaultDataSourceId = defaultDataSourceId;
    }

    public boolean isUiFiltersSet() {
        return isNotBlank(username) || status != null || companyId != null || defaultDataSourceId != null;
    }

}
