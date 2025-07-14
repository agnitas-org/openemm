/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.dao;

import java.util.Date;

import com.agnitas.emm.core.useractivitylog.bean.UserAction;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;

public class LoggedUserAction extends UserAction {
	private final String supervisorName;

	private final String username;
	private final Date date;

	public LoggedUserAction(String action, String description, String username, String supervisorName, Date date) {
		super(action, description);
		this.supervisorName = supervisorName;
		this.username = username;
		this.date = date;
	}

	public Date getDate() {
		return date;
	}

	public String getUsername() {
		return username;
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("action", getAction()).append("description", getDescription())
				.append("username", username).append("date", date).toString();
	}

	public String getShownName() {
		if (StringUtils.isBlank(supervisorName)) {
			return getUsername();
		}

		return getUsername() + " (" + supervisorName + ")";
	}
}
