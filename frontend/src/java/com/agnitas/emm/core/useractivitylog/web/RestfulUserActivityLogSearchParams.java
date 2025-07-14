/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.useractivitylog.forms.RestfulUserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;

public class RestfulUserActivityLogSearchParams extends UserActivityLogSearchParamsBase {

    private String requestUrl;
    private String requestMethod;
    private String description;

    public RestfulUserActivityLogSearchParams(DateRange timestamp) {
        super(timestamp);
    }

    @Override
    public void storeParams(UserActivityLogFilterBase filter) {
        RestfulUserActivityLogFilter restfulFilter = (RestfulUserActivityLogFilter) filter;

        super.storeParams(filter);
        this.requestUrl = restfulFilter.getRequestUrl();
        this.requestMethod = restfulFilter.getRequestMethod();
        this.description = restfulFilter.getDescription();
    }

    @Override
    public void restoreParams(UserActivityLogFilterBase filter) {
        RestfulUserActivityLogFilter restfulFilter = (RestfulUserActivityLogFilter) filter;

        super.restoreParams(filter);
        restfulFilter.setRequestUrl(this.requestUrl);
        restfulFilter.setRequestMethod(this.requestMethod);
        restfulFilter.setDescription(this.description);
    }

    @Override
    public void resetParams() {
        super.resetParams();
        this.requestUrl = null;
        this.requestMethod = null;
        this.description = null;
    }
}
