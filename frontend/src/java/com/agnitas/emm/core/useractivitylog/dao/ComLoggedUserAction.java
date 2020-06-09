/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao;

import java.util.Date;

import org.agnitas.emm.core.useractivitylog.LoggedUserAction;
import org.apache.commons.lang3.StringUtils;

public class ComLoggedUserAction extends LoggedUserAction {
	private final String supervisorName;

	public ComLoggedUserAction(String action, String description, String username, String supervisorName, Date date) {
		super(action, description, username, date);
		this.supervisorName = supervisorName;
	}

	@Override
	public String getShownName() {
		if (StringUtils.isBlank(supervisorName)) {
			return super.getShownName();
		}

		return getUsername() + " (" + supervisorName + ")";
	}
}
