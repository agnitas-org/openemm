/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.importquota.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.core.importquota.common.ImportSize;
import com.agnitas.emm.core.importquota.common.ImportType;

final class ImportSizeRowMapper implements RowMapper<ImportSize> {

	public static final ImportSizeRowMapper INSTANCE = new ImportSizeRowMapper();
	
	@Override
	public final ImportSize mapRow(final ResultSet resultSet, final int rowNum) throws SQLException {
		final int companyID = resultSet.getInt("company_ref");
		final int importID = resultSet.getInt("import_ref");
		final ImportType importType = importType(resultSet);
		final ZonedDateTime timestamp = ZonedDateTime.ofInstant(resultSet.getTimestamp("timestamp").toInstant(), ZoneId.systemDefault());
		final int lineCount = resultSet.getInt("lines_count");
		
		return new ImportSize(companyID, importID, importType, timestamp, lineCount);
	}
	
	private static final ImportType importType(final ResultSet rs) {
		try {
			return ImportType.valueOf(rs.getString("import_type"));
		} catch(final Exception e) {
			return null;
		}
	}

}
