/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.springws.util.impl;

import java.util.List;
import java.util.Objects;

import com.agnitas.beans.impl.AdminImpl;
import org.agnitas.emm.core.useractivitylog.UserAction;
import org.agnitas.emm.springws.util.SecurityContextAccess;
import org.agnitas.emm.springws.util.UserActivityLogAccess;
import org.agnitas.service.UserActivityLogService;

import com.agnitas.beans.Admin;
import org.springframework.stereotype.Component;

@Component("userActivityLogAccess")
public final class UserActivityLogAccessImpl implements UserActivityLogAccess {

	private final UserActivityLogService service;
	private final SecurityContextAccess securityContextAccess;

	public UserActivityLogAccessImpl(final UserActivityLogService service, final SecurityContextAccess securityContextAccess) {
		this.service = Objects.requireNonNull(service, "userActivityLogService");
		this.securityContextAccess = Objects.requireNonNull(securityContextAccess, "securityContextAccess");
    }
	
	@Override
	public void writeLog(List<UserAction> userActions) {
		final Admin admin = getAdminForUserActivityLog();
		
		for(final UserAction action : userActions) {
			service.writeUserActivityLog(admin, action);
		}
	}

    private Admin getAdminForUserActivityLog() {
		final Admin admin = new AdminImpl();
		admin.setUsername(this.securityContextAccess.getWebserviceUserName());
		
		return admin;
	}
}
