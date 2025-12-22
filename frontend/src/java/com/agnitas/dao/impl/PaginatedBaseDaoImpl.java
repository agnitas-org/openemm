/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.util.ArrayList;
import java.util.List;
import javax.sql.DataSource;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.SafeString;
import com.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

/**
 * Helper class which hides the dependency injection variables and eases some select and update actions and logging.
 * But still the datasource or the JdbcTemplate can be used directly if needed.
 */
public abstract class PaginatedBaseDaoImpl extends BaseDaoImpl {
	public PaginatedBaseDaoImpl(DataSource dataSource, JavaMailService javaMailService) {
		this.dataSource = dataSource;
		this.javaMailService = javaMailService;
	}
	
	public PaginatedBaseDaoImpl() {
		// Nothing to do
	}

	public <T> PaginatedList<T> selectPaginatedList(String query, String sortTable, PaginationForm paginationOptions, RowMapper<T> rowMapper, Object... parameters) {
		return selectPaginatedList(
				query,
				sortTable,
				paginationOptions.getSort(),
				paginationOptions.ascending(),
				paginationOptions.getPage(),
				paginationOptions.getNumberOfRows(),
				rowMapper,
				parameters
		);
	}

	public <T> PaginatedList<T> selectPaginatedList(String selectStatement, int pageNumber, int pageSize, RowMapper<T> rowMapper, Object... parameters) {
		return selectPaginatedList(selectStatement, null, null, true, pageNumber, pageSize, rowMapper, parameters);
	}

	public <T> PaginatedList<T> selectPaginatedList(String selectStatement, String sortTable, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize, RowMapper<T> rowMapper, Object... parameters) {
		if (StringUtils.isNotBlank(sortTable)) {
			sortTable = SafeString.getSafeDbTableName(sortTable);
		}
		if (StringUtils.isNotBlank(sortColumn)) {
			sortColumn = SafeString.getSafeDbColumnName(sortColumn);
		}

		// Only alphanumeric values may be sorted with upper or lower,
		// which always returns a string value.
		// For selecting ordered numeric values use without lower
		String sortClause = "";
		try {
			if (StringUtils.isNotBlank(sortColumn)) {
				if (StringUtils.isNotBlank(sortTable) && DbUtilities.getColumnDataType(getDataSource(), sortTable, sortColumn).getSimpleDataType() == SimpleDataType.Characters) {
					sortClause = "ORDER BY LOWER(" + sortColumn + ")";
				} else {
					sortClause = "ORDER BY " + sortColumn;
				}
				sortClause = sortClause + " " + (sortDirectionAscending ? "asc" : "desc");
			}
		} catch (Exception e) {
			sortClause = "";
		}

		return selectPaginatedListWithSortClause(selectStatement, sortClause, sortColumn, sortDirectionAscending, pageNumber, pageSize, rowMapper, parameters);
	}

	public <T> PaginatedList<T> selectPaginatedListWithSortClause(String query, String sortClause, PaginationForm paginationOptions, RowMapper<T> rowMapper, Object... params) {
		return selectPaginatedListWithSortClause(
				query,
				sortClause,
				paginationOptions.getSort(),
				paginationOptions.ascending(),
				paginationOptions.getPage(),
				paginationOptions.getNumberOfRows(),
				rowMapper,
				params
		);
	}

	public <T> PaginatedList<T> selectPaginatedListWithSortClause(String selectStatement, String sortClause, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize, RowMapper<T> rowMapper, Object... parameters) {
		// Get number of available items to show
		String countQuery;
		if (isOracleDB()) {
			countQuery = "SELECT COUNT(*) FROM (" + selectStatement + ")";
		} else {
			countQuery = "SELECT COUNT(*) FROM (" + selectStatement + ") selection";
		}

		return selectPaginatedListWithSortClause(countQuery, selectStatement, sortClause, sortColumn, sortDirectionAscending, pageNumber, pageSize, rowMapper, parameters);
	}
	
	/**
	 * Parameter "selectCountStatement" may differ from parameter "selectDataStatement" to improve db performace.
	 * Especially "LEFT OUTER JOIN" statement parts do not need to be included in "selectCountStatement".
	 */
	public <T> PaginatedList<T> selectPaginatedListWithSortClause(String selectCountStatement, String selectDataStatement, String sortClause, String sortColumn, boolean sortDirectionAscending, int pageNumber, int pageSize, RowMapper<T> rowMapper, Object... parameters) {
		int totalRows = selectInt(selectCountStatement, parameters);

		// Check pageSize validity
		if (pageSize < 1) {
			pageSize = 10;
			pageNumber = 1;
		}

		if (totalRows > 0) {
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

			if (isOracleDB()) {
				// Borders in oracle dbstatement "between" are included in resultset
				int rowStart = (pageNumber - 1) * pageSize + 1;
				int rowEndInclusive = rowStart + pageSize - 1;

				selectDataStatement = "SELECT * FROM (SELECT selection.*, rownum AS r FROM (" + selectDataStatement + " " + sortClause + ") selection) WHERE r BETWEEN ? AND ?";
				parameters = AgnUtils.extendObjectArray(parameters, rowStart, rowEndInclusive);
			} else {
				int rowStart = (pageNumber - 1) * pageSize;
				selectDataStatement = selectDataStatement + " " + sortClause + " LIMIT ? OFFSET ?";
				parameters = AgnUtils.extendObjectArray(parameters, pageSize, rowStart);
			}

			List<T> resultList = select(selectDataStatement, rowMapper, parameters);
			return new PaginatedList<>(resultList, totalRows, pageSize, pageNumber, sortColumn, sortDirectionAscending);
		} else {
			return new PaginatedList<>(new ArrayList<>(), 0, pageSize, 1, sortColumn, sortDirectionAscending);
		}
	}
}
