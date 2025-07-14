/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wsmanager.form;

import com.agnitas.web.forms.FormSearchParams;

public class WebserviceUserFormSearchParams implements FormSearchParams<WebserviceUserOverviewFilter> {

    private String username;
    private Integer status;
    private Integer companyId;
    private Integer defaultDataSourceId;

    @Override
    public void storeParams(WebserviceUserOverviewFilter filter) {
        this.username = filter.getUsername();
        this.status = filter.getStatus();
        this.companyId = filter.getCompanyId();
        this.defaultDataSourceId = filter.getDefaultDataSourceId();
    }

    @Override
    public void restoreParams(WebserviceUserOverviewFilter filter) {
        filter.setUsername(this.username);
        filter.setStatus(this.status);
        filter.setCompanyId(this.companyId);
        filter.setDefaultDataSourceId(this.defaultDataSourceId);
    }

    @Override
    public void resetParams() {
        username = null;
        status = null;
        companyId = null;
        defaultDataSourceId = null;
    }
}
