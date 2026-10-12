/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.useractivitylog.forms.SoapUserActivityLogFilter;

public class SoapUserActivityLogSearchParams extends UserActivityLogSearchParamsBase<SoapUserActivityLogFilter> {

    private String endpoint;
    private String ipAddress;

    public SoapUserActivityLogSearchParams(DateRange timestamp) {
        super(timestamp);
    }

    @Override
    public void storeParams(SoapUserActivityLogFilter filter) {
        super.storeParams(filter);
        this.endpoint = filter.getEndpoint();
        this.ipAddress = filter.getIpAddress();
    }

    @Override
    public void restoreParams(SoapUserActivityLogFilter filter) {
        super.restoreParams(filter);
        filter.setEndpoint(this.endpoint);
        filter.setIpAddress(this.ipAddress);
    }

}
