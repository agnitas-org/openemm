/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.usergroup.form;

import org.agnitas.web.forms.FormSearchParams;

public class UserGroupSearchParams extends UserGroupOverviewFilter implements FormSearchParams<UserGroupOverviewFilter> {

    @Override
    public void storeParams(UserGroupOverviewFilter form) {
        this.setGroupName(form.getGroupName());
        this.setDescription(form.getDescription());
        this.setClientId(form.getClientId());
        this.setClientName(form.getClientName());
    }

    @Override
    public void restoreParams(UserGroupOverviewFilter form) {
        form.setGroupName(this.getGroupName());
        form.setDescription(this.getDescription());
        form.setClientId(this.getClientId());
        form.setClientName(this.getClientName());
    }

    @Override
    public void resetParams() {
        this.setGroupName("");
        this.setDescription("");
        this.setClientName("");
        this.setClientId(0);
    }
}
