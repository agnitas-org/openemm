/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mimetypes.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

import javax.activation.MimeType;
import javax.activation.MimeTypeParseException;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

final class MimeTypeRowMapper implements RowMapper<MimeType> {

	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MimeTypeRowMapper.class);
	
	@Override
	public final MimeType mapRow(final ResultSet rs, final int row) throws SQLException {
		final String mimeTypeString = rs.getString("mimetype");

		try {
			return new MimeType(mimeTypeString);
		} catch(final MimeTypeParseException e) {
			final String msg = String.format("Error creating MimeType '%s'", mimeTypeString);
			
			logger.error(msg, e);
			
			throw new SQLException(msg, e);
		}
	}

}
