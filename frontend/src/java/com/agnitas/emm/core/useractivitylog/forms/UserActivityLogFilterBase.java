/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.forms;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.web.forms.PaginationForm;

import java.util.Map;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

public class UserActivityLogFilterBase extends PaginationForm {

    private DateRange timestamp;
    private String username;
    private int companyId;

    public DateRange getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(DateRange timestamp) {
        this.timestamp = timestamp;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("timestamp", timestamp);
        map.put("username", username);
        map.put("companyId", companyId);

        return map;
    }

    public boolean isUiFiltersSet() {
        return timestamp.isPresent() || isNotBlank(username);
    }
}
