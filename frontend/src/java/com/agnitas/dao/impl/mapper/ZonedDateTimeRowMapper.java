/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

/**
 * {@link RowMapper} to map a {@link Timestamp} column to {@link ZonedDateTime}.
 */
public final class ZonedDateTimeRowMapper implements RowMapper<ZonedDateTime> {

	/** Singleton of this {@link RowMapper}. */
	public static final ZonedDateTimeRowMapper INSTANCE = new ZonedDateTimeRowMapper();
	
	/**
	 * Use {@link #INSTANCE}.
	 */
	private ZonedDateTimeRowMapper() {
		super();
	}
	
	@Override
	public final ZonedDateTime mapRow(final ResultSet resultSet, final int row) throws SQLException {
		final Date date = resultSet.getTimestamp(1);
		
		return ZonedDateTime.ofInstant(date.toInstant(), ZoneId.systemDefault());
	}

}
