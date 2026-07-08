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

public class DoubleRowMapper implements RowMapper<Double> {

	public static final DoubleRowMapper INSTANCE = new DoubleRowMapper();

	private DoubleRowMapper() {
		// Empty
	}
	
	@Override
	public Double mapRow(ResultSet resultSet, int row) throws SQLException {
		Object object = resultSet.getObject(1);
		if (object == null) {
			return null;
		}

		if (!(object instanceof Number)) {
			throw new IllegalStateException(
					"Expected a numeric value in column 1, but received: " + object.getClass().getName()
			);
		}

		return ((Number) object).doubleValue();
	}
	
}
