/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import java.util.Date;

public interface PasswordResetDao {
	public String getPasswordResetTokenHash(int adminID);

	public void save(int adminID, String tokenHash, Date validUntil, String remoteAddr);

	public void remove(int adminID);

	public void riseErrorCount(int adminID);

	boolean existsPasswordResetTokenHash(String username, String tokenHash);

	boolean isValidPasswordResetTokenHash(String username, String tokenHash);
}
