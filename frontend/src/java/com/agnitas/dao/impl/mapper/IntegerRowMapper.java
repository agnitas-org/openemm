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

/**
 * Simple {@link RowMapper} for retrieving integer values from ResultSet.
 * 
 * The source column must be at index 1.
 */
public class IntegerRowMapper implements RowMapper<Integer> {
	
	/** Singleton of this {@link RowMapper}. */
	public static final IntegerRowMapper INSTANCE = new IntegerRowMapper();
	
	/**
	 * Creates a new instance.
	 * 
	 * @see #INSTANCE
	 */
	private IntegerRowMapper() {
		// Empty
	}
	
	@Override
	public Integer mapRow(ResultSet resultSet, int row) throws SQLException {
		return resultSet.getInt(1);
	}
	
}
