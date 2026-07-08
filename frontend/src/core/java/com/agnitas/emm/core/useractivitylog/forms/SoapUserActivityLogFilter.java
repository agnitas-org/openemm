/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.forms;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Map;

public class SoapUserActivityLogFilter extends UserActivityLogFilterBase {

    private String endpoint;
    private String ipAddress;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("endpoint", endpoint);
        map.put("ipAddress", ipAddress);

        return map;
    }

    @Override
    public boolean isUiFiltersSet() {
        return isNotBlank(endpoint) || isNotBlank(ipAddress) || super.isUiFiltersSet();
    }
}
