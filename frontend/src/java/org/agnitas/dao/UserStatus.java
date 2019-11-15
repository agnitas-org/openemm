/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import org.agnitas.dao.exception.UnknownUserStatusException;

public enum UserStatus {
	Active(1),
	Bounce(2),
	AdminOut(3),
	UserOut(4),
	WaitForConfirm(5),
	Blacklisted(6),
	Suspend(7); // Sometimes also referred to as status "supended" or "pending"
	
	private int statusCode;
	
	UserStatus(int statusCode) {
		this.statusCode = statusCode;
	}
	
	public int getStatusCode() {
		return statusCode;
	}
	
	public static UserStatus getUserStatusByID(int id) throws UnknownUserStatusException {
		for (UserStatus userStatus : UserStatus.values()) {
			if (userStatus.statusCode == id) {
				return userStatus;
			}
		}
		throw new UnknownUserStatusException(id);
	}
}
