/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.List;
import java.util.Map;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.util.DbUtilities;
import org.apache.log4j.Logger;

import com.agnitas.dao.MiaDao;

public class MiaDaoImpl extends BaseDaoImpl implements MiaDao {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MiaDaoImpl.class);

	@Override
	public PaginatedListImpl<Map<String, Object>> getDataPaginated(int companyID, int page, int rownums, String ... hiddenNames) {
		final int offset = page * rownums;
		final int count = selectIntWithDefaultValue(logger, getMiaDataTotalCountQuery(hiddenNames), 0, companyID);

		List<Map<String, Object>> results;
		if (isOracleDB()) {
			results = select(logger, getMiaDataPaginatedQuery(hiddenNames), companyID, offset - rownums + 1, offset);
		} else {
			results = select(logger, getMiaDataPaginatedQuery(hiddenNames), companyID, offset - rownums, rownums);
		}

		return new PaginatedListImpl<>(results, count, rownums, page, "", "");
	}
	
	@Override
	public boolean deleteByCompany(int companyID) {
		update(logger, "DELETE FROM mia_group_tbl WHERE company_id = ?", companyID);
		return selectInt(logger, "SELECT COUNT(*) FROM mia_group_tbl WHERE company_id = ?", companyID) == 0;
	}

	private String getMiaDataPaginatedQuery(String ... hiddenNames) {
		StringBuilder sqlBuilder = new StringBuilder();
		if (isOracleDB()) {
			sqlBuilder.append("SELECT * FROM (")
					.append("SELECT mv.*, ROWNUM AS row_index FROM (");
		}

		sqlBuilder.append(getMiaDataQuery(hiddenNames));

		if (isOracleDB()) {
			sqlBuilder.append(") mv")
					.append(") WHERE row_index BETWEEN ? AND ?");
		} else {
			sqlBuilder.append(" LIMIT ?, ?");
		}

		return sqlBuilder.toString();
	}

	private String getMiaDataTotalCountQuery(String ... hiddenNames) {
		return "SELECT COUNT(*)"
				+ " FROM mia_value_tbl v"
					+ " INNER JOIN mia_name_tbl n ON v.name_id = n.id" + getHideMiaNameCause("n", hiddenNames)
					+ " INNER JOIN mia_group_tbl g ON v.group_id = g.id"
				+ " WHERE g.company_id = ?";
	}

	private String getMiaDataQuery(String ... hiddenNames) {
		return "SELECT n.name, v.value, ml.mailing_id, ml.shortname, md.senddate, g.day"
				+ " FROM mia_value_tbl v"
					+ " INNER JOIN mia_name_tbl n ON v.name_id = n.id" + getHideMiaNameCause("n", hiddenNames)
					+ " INNER JOIN mia_group_tbl g ON v.group_id = g.id"
					+ " LEFT OUTER JOIN mailing_tbl ml ON (ml.mailing_id = v.value AND n.name LIKE 'mia.mailing.%')"
					+ " LEFT OUTER JOIN maildrop_status_tbl md ON (md.mailing_id = ml.mailing_id AND md.status_field = 'W')"
				+ " WHERE g.company_id = ?"
				+ " ORDER BY COALESCE(md.senddate, g.day) DESC";
	}

	private String getHideMiaNameCause(String miaNameTableAlias, String ... hiddenNames){
		if(hiddenNames != null && hiddenNames.length > 0){
			return " AND " + miaNameTableAlias + ".name NOT IN " + DbUtilities.joinForIN(hiddenNames, name -> name) + " ";
		}

		return "";
	}
}
