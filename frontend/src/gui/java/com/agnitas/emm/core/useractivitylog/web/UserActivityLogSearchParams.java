/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilter;
import com.agnitas.util.UserActivityLogActions;

public class UserActivityLogSearchParams extends UserActivityLogSearchParamsBase<UserActivityLogFilter> {

    private UserActivityLogActions action;
    private String description;

    public UserActivityLogSearchParams(DateRange timestamp) {
        super(timestamp);
    }

    @Override
    public void storeParams(UserActivityLogFilter filter) {
        super.storeParams(filter);
        this.action = filter.getAction();
        this.description = filter.getDescription();
    }

    @Override
    public void restoreParams(UserActivityLogFilter filter) {
        super.restoreParams(filter);
        filter.setAction(this.action);
        filter.setDescription(this.description);
    }

}
