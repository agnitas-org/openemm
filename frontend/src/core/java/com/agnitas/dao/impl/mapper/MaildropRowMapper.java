/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.MaildropEntry;
import com.agnitas.beans.impl.MaildropEntryImpl;

/**
 * Implementation of {@link RowMapper} for {@link MaildropEntry}.
 */
public final class MaildropRowMapper implements RowMapper<MaildropEntry> {
	
	/** Singleton instance. */
	public static final MaildropRowMapper INSTANCE = new MaildropRowMapper();
	
	/**
	 * Creates a new instance.
	 * 
	 * @see #INSTANCE
	 */
	private MaildropRowMapper() {
		// Empty
	}

	@Override
	public final MaildropEntry mapRow(final ResultSet resultSet, final int row) throws SQLException {
		final MaildropEntryImpl entry = new MaildropEntryImpl();
	
		entry.setAdminTestTargetID(resultSet.getInt("admin_test_target_id"));
		entry.setBlocksize(resultSet.getInt("blocksize"));
		entry.setCompanyID(resultSet.getInt("company_id"));
		entry.setGenChangeDate(resultSet.getTimestamp("genchange"));
		entry.setGenDate(resultSet.getTimestamp("gendate"));
		entry.setGenStatus(resultSet.getInt("genstatus"));
		entry.setId(resultSet.getInt("status_id"));
		entry.setMailGenerationOptimization(resultSet.getString("optimize_mail_generation"));
		entry.setMailingID(resultSet.getInt("mailing_id"));
		entry.setMaxRecipients(resultSet.getInt("max_recipients"));
		entry.setSendDate(resultSet.getTimestamp("senddate"));
		entry.setStatus(resultSet.getString("status_field").charAt(0));	// TODO: Very bad... "status_field" is of type "STRING(10)"
		entry.setStepping(resultSet.getInt("step"));
	
		return entry;
	}
}
