/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.web.forms.FormSearchParams;

public class UserActivityLogSearchParamsBase implements FormSearchParams<UserActivityLogFilterBase> {

    private DateRange timestamp;
    private String username;

    public UserActivityLogSearchParamsBase(DateRange timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public void storeParams(UserActivityLogFilterBase filter) {
        this.timestamp = filter.getTimestamp();
        this.username = filter.getUsername();
    }

    @Override
    public void restoreParams(UserActivityLogFilterBase filter) {
        filter.setTimestamp(this.timestamp);
        filter.setUsername(this.username);
    }

    @Override
    public void resetParams() {
        timestamp = new DateRange();
        username = null;
    }
}
