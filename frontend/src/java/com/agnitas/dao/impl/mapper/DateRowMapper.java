/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;

import org.springframework.jdbc.core.RowMapper;

public class DateRowMapper implements RowMapper<Date> {
	
	/** Singleton instance. */
	public static final DateRowMapper INSTANCE = new DateRowMapper();
	
	/**
	 * Creates a new instance.
	 * 
	 * @see #INSTANCE
	 */
	private DateRowMapper() {
		// Empty
	}

	@Override
	public Date mapRow(ResultSet resultSet, int row) throws SQLException {
		return resultSet.getTimestamp(1);
	}
	
}
