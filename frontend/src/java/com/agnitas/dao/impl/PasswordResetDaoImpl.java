/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.Date;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.apache.log4j.Logger;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.PasswordResetDao;

/**
 * DAO implementation for accessing an Oracle DB.
 */
public class PasswordResetDaoImpl extends BaseDaoImpl implements PasswordResetDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(PasswordResetDaoImpl.class);
	
	private static final int MAXIMUM_TOKEN_CHECKS = 3;
	
	@Override
	public String getPasswordResetTokenHash(int adminID) {
		return selectObjectDefaultNull(logger, "SELECT token_hash FROM admin_password_reset_tbl WHERE admin_id = ? AND valid_until > CURRENT_TIMESTAMP AND error_count <= ?", new StringRowMapper(), adminID, MAXIMUM_TOKEN_CHECKS);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void save(int adminID, String tokenHash, Date validUntil, String remoteAddr) {
		update(logger, "INSERT INTO admin_password_reset_tbl (admin_id, token_hash, valid_until, ip_address, time, error_count) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP, 0)", adminID, tokenHash, validUntil, remoteAddr);
	}

	@Override
	public boolean existsPasswordResetTokenHash(String username, String tokenHash) {
		return selectObjectDefaultNull(logger, "SELECT token_hash FROM admin_password_reset_tbl pwd, admin_tbl admin WHERE pwd.admin_id = admin.admin_id AND admin.username = ? AND pwd.token_hash = ? AND pwd.valid_until > CURRENT_TIMESTAMP AND error_count < ?", new StringRowMapper(), username, tokenHash, MAXIMUM_TOKEN_CHECKS) != null;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void remove(int adminID) {
		update(logger, "DELETE FROM admin_password_reset_tbl WHERE admin_id = ?", adminID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void riseErrorCount(int adminID) {
		update(logger, "UPDATE admin_password_reset_tbl SET error_count = error_count + 1 WHERE admin_id = ?", adminID);
	}
}
