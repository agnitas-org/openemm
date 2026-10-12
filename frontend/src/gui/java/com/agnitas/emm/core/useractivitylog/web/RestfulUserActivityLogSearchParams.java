/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.useractivitylog.forms.RestfulUserActivityLogFilter;

public class RestfulUserActivityLogSearchParams extends UserActivityLogSearchParamsBase<RestfulUserActivityLogFilter> {

    private String requestUrl;
    private String requestMethod;
    private String description;

    public RestfulUserActivityLogSearchParams(DateRange timestamp) {
        super(timestamp);
    }

    @Override
    public void storeParams(RestfulUserActivityLogFilter filter) {
        super.storeParams(filter);
        this.requestUrl = filter.getRequestUrl();
        this.requestMethod = filter.getRequestMethod();
        this.description = filter.getDescription();
    }

    @Override
    public void restoreParams(RestfulUserActivityLogFilter filter) {
        super.restoreParams(filter);
        filter.setRequestUrl(this.requestUrl);
        filter.setRequestMethod(this.requestMethod);
        filter.setDescription(this.description);
    }

}
