/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.agnitas.emm.core.mailing.beans.LightweightMailing;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.MailingContentType;
import com.agnitas.emm.common.MailingType;

/**
 * {@link RowMapper} for {@link LightweightMailing}.
 */
public class LightweightMailingRowMapper implements RowMapper<LightweightMailing> {
	
	/** Singleton instance. */
	public static final LightweightMailingRowMapper INSTANCE = new LightweightMailingRowMapper();
	
	/**
	 * Creates a new instance.
	 * 
	 * @see #INSTANCE
	 */
	private LightweightMailingRowMapper() {
		// Empty
	}

	@Override
	public LightweightMailing mapRow(ResultSet resultSet, int index) throws SQLException {
		final int companyID = resultSet.getInt("company_id");
		final int mailingID = resultSet.getInt("mailing_id");
		final String shortname = resultSet.getString("shortname") != null ? resultSet.getString("shortname") : "";
		final String description = resultSet.getString("description") != null ? resultSet.getString("description") : "";
		MailingType mailingType;
		try {
			mailingType = MailingType.fromCode(resultSet.getInt("mailing_type"));
		} catch (Exception e) {
			throw new SQLException("Invalid mailingtype code: " + resultSet.getInt("mailing_type"));
		}
		final String workStatus = resultSet.getString("work_status");
		final String contentTypeString = resultSet.getString("content_type");
		
		final MailingContentType contentType = decodeContentType(contentTypeString);
		
		
		return new LightweightMailing(companyID, mailingID, shortname, description, mailingType, workStatus, contentType);
	}

	private MailingContentType decodeContentType(final String contentTypeString) {
		try {
			return MailingContentType.getFromString(contentTypeString);
		} catch(final Exception e) {
			return MailingContentType.advertising;
		}
	}
}
