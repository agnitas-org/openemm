/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.company.rowmapper;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

import com.agnitas.emm.core.company.bean.CompanyEntry;

/**
 * Implementation of {@link RowMapper} for {@link CompanyEntry}s.
 */
public class CompanyEntryRowMapper implements RowMapper<CompanyEntry> {
	
	@Override
	public CompanyEntry mapRow(ResultSet resultSet, int row) throws SQLException {
		CompanyEntry companyEntry = new CompanyEntry();
		
		companyEntry.setCompanyId(resultSet.getInt("company_id"));
		companyEntry.setShortname(resultSet.getString("shortname"));
		companyEntry.setDescription(resultSet.getString("description"));
		companyEntry.setStatus(resultSet.getString("status"));
		companyEntry.setTimestamp(resultSet.getTimestamp("timestamp"));
			
		return companyEntry;
	}

}
