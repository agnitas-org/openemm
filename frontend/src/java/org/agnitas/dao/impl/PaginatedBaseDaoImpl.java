/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.util.ArrayList;
import java.util.List;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.SafeString;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

/**
 * Helper class which hides the dependency injection variables and eases some select and update actions and logging.
 * But still the datasource or the JdbcTemplate can be used directly if needed.
 * 
 * The logger of this class is not used for db actions to log, because it would hide the calling from the derived classes.
 * Therefore every simplified update and select method demands an logger delivered as parameter.
 */
public abstract class PaginatedBaseDaoImpl extends BaseDaoImpl {

	public <T> PaginatedListImpl<T> selectPaginatedList(Logger logger, String selectStatement, String sortTable, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize, RowMapper<T> rowMapper, Object... parameters) {
		if (StringUtils.isNotBlank(sortTable)) {
			sortTable = SafeString.getSafeDbTableName(sortTable);
		}
		if (StringUtils.isNotBlank(sortColumn)) {
			sortColumn = SafeString.getSafeDbColumnName(sortColumn);
		}
		
		// Only alphanumeric values may be sorted with upper or lower,
		// which always returns a string value.
		// For selecting ordered numeric values use without lower
		String sortClause;
		try {
			if (StringUtils.isNotBlank(sortTable) && DbUtilities.getColumnDataType(getDataSource(), sortTable, sortColumn).getSimpleDataType() == SimpleDataType.Characters) {
				sortClause = "ORDER BY LOWER(" + sortColumn + ")";
			} else {
				sortClause = "ORDER BY " + sortColumn;
			}
			sortClause = sortClause + " " + (sortDirectionAscending ? "asc" : "desc");
		} catch (Exception e) {
			sortClause = "";
		}

		return selectPaginatedListWithSortClause(logger, selectStatement, sortClause, sortColumn, sortDirectionAscending, pageNumber, pageSize, rowMapper, parameters);
	}
	
	public <T> PaginatedListImpl<T> selectPaginatedListWithSortClause(Logger logger, String selectStatement, String sortClause, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize, RowMapper<T> rowMapper, Object... parameters) {
		// Get number of available items to show
		String countQuery;
		if (isOracleDB()) {
			countQuery = "SELECT COUNT(*) FROM (" + selectStatement + ")";
		} else {
			countQuery = "SELECT COUNT(*) FROM (" + selectStatement + ") selection";
		}
		int totalRows = selectInt(logger, countQuery, parameters);

		if (totalRows > 0) {
			// Check pageSize validity
			if (pageSize < 1) {
				pageSize = 10;
				pageNumber = 1;
			}
	
			// Check pagenumber validity
			if (pageNumber < 1) {
				// Pagenumber starts with 1, not 0
				pageNumber = 1;
			} else if (pageNumber != 1) {
				// Check pagenumber lies beneath the maximum of available pages
				int maximumPagenumber = (int) Math.ceil(totalRows / (float) pageSize);
				if (maximumPagenumber < pageNumber) {
					// pagenumber exceeds maximum, so set it to first page
					pageNumber = 1;
				}
			}
	
			String selectDataStatement;
			if (isOracleDB()) {
				// Borders in oracle dbstatement "between" are included in resultset
				int rowStart = (pageNumber - 1) * pageSize + 1;
				int rowEnd_inclusive = rowStart + pageSize - 1;
				selectDataStatement = "SELECT * FROM (SELECT selection.*, rownum AS r FROM (" + selectStatement + " " + sortClause + ") selection) WHERE r BETWEEN ? AND ?";
				parameters = AgnUtils.extendObjectArray(parameters, rowStart, rowEnd_inclusive);
			} else {
				int rowStart = (pageNumber - 1) * pageSize;
				selectDataStatement = selectStatement + " " + sortClause + " LIMIT ?, ?";
				parameters = AgnUtils.extendObjectArray(parameters, rowStart, pageSize);
			}
	
			List<T> resultList = select(logger, selectDataStatement, rowMapper, parameters);
			return new PaginatedListImpl<>(resultList, totalRows, pageSize, pageNumber, sortColumn, sortDirectionAscending);
		} else {
			return new PaginatedListImpl<>(new ArrayList<>(), 0, pageSize, 1, sortColumn, sortDirectionAscending);
		}
	}
}
