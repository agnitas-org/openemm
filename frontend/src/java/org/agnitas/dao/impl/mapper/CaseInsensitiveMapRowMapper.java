/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.springframework.jdbc.core.RowMapper;

public class CaseInsensitiveMapRowMapper implements RowMapper<CaseInsensitiveMap<String, Object>> {
	@Override
	public CaseInsensitiveMap<String, Object> mapRow(ResultSet resultSet, int row) throws SQLException {
		CaseInsensitiveMap<String, Object> returnMap = new CaseInsensitiveMap<>();
		for (int i = 1 ; i <= resultSet.getMetaData().getColumnCount(); i++) {
			if ("TIMESTAMP".equalsIgnoreCase(resultSet.getMetaData().getColumnTypeName(i))) {
				// Avoid getting the type "oracle.sql.TIMESTAMP" which cannot be handled mostly
				returnMap.put(resultSet.getMetaData().getColumnName(i), resultSet.getTimestamp(i));
			} else {
				returnMap.put(resultSet.getMetaData().getColumnName(i), resultSet.getObject(i));
			}
		}
		return returnMap;
	}
}
