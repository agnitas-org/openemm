/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.forms;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class RestfulUserActivityLogFilter extends UserActivityLogFilterBase {

    private String requestUrl;
    private String requestMethod;
    private String description;

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("requestUrl", requestUrl);
        map.put("requestMethod", requestMethod);
        map.put("description", description);

        return map;
    }

    @Override
    public boolean isUiFiltersSet() {
        return isNotBlank(requestUrl) || isNotBlank(requestMethod) || isNotBlank(description) || super.isUiFiltersSet();
    }
}
