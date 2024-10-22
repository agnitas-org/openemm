/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import static org.apache.commons.lang3.StringUtils.defaultString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import com.agnitas.emm.core.salutation.form.SalutationOverviewFilter;
import org.agnitas.beans.SalutationEntry;
import org.agnitas.beans.Title;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.beans.impl.SalutationEntryImpl;
import org.agnitas.beans.impl.TitleImpl;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.ComTitleDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;

public class ComTitleDaoImpl extends PaginatedBaseDaoImpl implements ComTitleDao {
	
	private static final Logger logger = LogManager.getLogger(ComTitleDaoImpl.class);
		
	@Override
	public Title getTitle(int titleID, int companyID) {
		return selectObjectDefaultNull(logger, "SELECT company_id, title_id, description FROM title_tbl WHERE title_id = ? AND (company_id = ? OR company_id = 0)", new Title_RowMapper(), titleID, companyID);
	}

	@DaoUpdateReturnValueCheck
	@Override
	public boolean delete(int titleID, int companyID) {
		if (titleID == 0 || companyID == 0) {
			return false;
		} else {
			try {
				update(logger, "DELETE FROM title_gender_tbl WHERE title_id = ?", titleID);
				return update(logger, "DELETE FROM title_tbl WHERE title_id = ? AND company_id = ?", titleID, companyID) == 1;
			} catch (Exception e) {
				return false;
			}
		}
	}

	@Override
	public PaginatedListImpl<Title> overview(SalutationOverviewFilter filter) {
		List<Object> params = new ArrayList<>();
		String sql = "SELECT * FROM ("
			+ " SELECT t.title_id, t.description, t.company_id,"
			+ "   MAX(CASE WHEN tg.gender = 0 THEN tg.title END) AS gender0,"
			+ "   MAX(CASE WHEN tg.gender = 1 THEN tg.title END) AS gender1,"
			+ "   MAX(CASE WHEN tg.gender = 2 THEN tg.title END) AS gender2,"
			+ "   MAX(CASE WHEN tg.gender = 4 THEN tg.title END) AS gender4,"
			+ "   MAX(CASE WHEN tg.gender = 5 THEN tg.title END) AS gender5"
			+ " FROM title_tbl t"
			+ "   LEFT JOIN title_gender_tbl tg ON t.title_id = tg.title_id"
			+ " GROUP BY t.title_id, t.description, t.company_id) sub"
			+ applyOverviewFilter(filter, params);

		String sortCol = filter.getSortOrDefault("title_id");
		String sortClause = " ORDER BY " + sortCol + (isOracleDB()
			? " " + filter.getOrder() + " NULLS LAST"
			: " IS NULL, " + sortCol + " " + filter.getOrder());

		PaginatedListImpl<Title> list = selectPaginatedListWithSortClause(logger, sql, sortClause, sortCol,
			filter.ascending(), filter.getPage(), filter.getNumberOfRows(),
			new OverviewRowMapper(), params.toArray());

		if (filter.isUiFiltersSet()) {
			list.setNotFilteredFullListSize(getTotalUnfilteredCountForOverview(filter.getCompanyId()));
		}
		return list;
	}

	private int getTotalUnfilteredCountForOverview(int companyId) {
		StringBuilder query = new StringBuilder("SELECT COUNT(*) FROM title_tbl t");
		List<Object> params = applyRequiredOverviewFilter(query, companyId);

		return selectIntWithDefaultValue(logger, query.toString(), 0, params.toArray());
	}

	private StringBuilder applyOverviewFilter(SalutationOverviewFilter filter, List<Object> params) {
		StringBuilder sql = new StringBuilder();
		params.addAll(applyRequiredOverviewFilter(sql, filter.getCompanyId()));
		if (filter.getSalutationId() != null) {
			sql.append(getPartialSearchFilterWithAnd("title_id", filter.getSalutationId(), params));
		}
		if (StringUtils.isNotBlank(filter.getName())) {
			sql.append(getPartialSearchFilterWithAnd("description"));
			params.add(filter.getName());
		}
		if (StringUtils.isNotBlank(filter.getGender0())) {
			sql.append(getPartialSearchFilterWithAnd("gender0"));
			params.add(filter.getGender0());
		}
		if (StringUtils.isNotBlank(filter.getGender1())) {
			sql.append(getPartialSearchFilterWithAnd("gender1"));
			params.add(filter.getGender1());
		}
		if (StringUtils.isNotBlank(filter.getGender2())) {
			sql.append(getPartialSearchFilterWithAnd("gender2"));
			params.add(filter.getGender2());
		}
		if (StringUtils.isNotBlank(filter.getGender4())) {
			sql.append(getPartialSearchFilterWithAnd("gender4"));
			params.add(filter.getGender4());
		}
		if (StringUtils.isNotBlank(filter.getGender5())) {
			sql.append(getPartialSearchFilterWithAnd("gender5"));
			params.add(filter.getGender5());
		}
		return sql;
	}

	private List<Object> applyRequiredOverviewFilter(StringBuilder query, int companyId) {
		query.append(" WHERE company_id IN (0, ?)");
		return new ArrayList<>(List.of(companyId));
	}

	@Override
	public PaginatedListImpl<SalutationEntry> getSalutationList(int companyID, String sortColumn, String sortDirection, int pageNumber, int pageSize) {
		if (StringUtils.isBlank(sortColumn)) {
			sortColumn = "title_id";
		}

		boolean sortDirectionAscending = AgnUtils.sortingDirectionToBoolean(sortDirection);

		return selectPaginatedList(logger, "SELECT company_id, title_id, description FROM title_tbl WHERE company_id IN (0, ?)", "title_tbl", sortColumn, sortDirectionAscending, pageNumber, pageSize, new SalutationEntry_RowMapper(), companyID);
	}

	/**
	 * Get a List of light title entries for dropdown display in a JSP
	 */
	@Override
	public List<Title> getTitles(int companyID, boolean includeGenders) {
		String query = "SELECT company_id, title_id, description FROM title_tbl WHERE company_id IN (0, ?) ORDER BY LOWER(description)";
		return select(logger, query, includeGenders ? new Title_RowMapper() : new TitleLight_RowMapper(), companyID);
	}

	@Override
	public boolean deleteTitlesByCompanyID(int companyID) {
		try {
			update(logger, "DELETE FROM title_gender_tbl WHERE title_id IN (SELECT title_id FROM title_tbl WHERE company_id = ?)", companyID);
			update(logger, "DELETE FROM title_tbl WHERE company_id = ?", companyID);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@DaoUpdateReturnValueCheck
	@Override
	public void save(Title title) throws Exception {
		if (title.getCompanyID() <= 0) {
			throw new Exception("Invalid company for new title entry");
		} else if (title.getId() <= 0) {
			// Insert new Title
			if (isOracleDB()) {
				int newID = selectInt(logger, "SELECT title_tbl_seq.nextval FROM DUAL");
				update(logger, "INSERT INTO title_tbl (title_id, company_id, description) VALUES (?, ?, ?)", newID, title.getCompanyID(), title.getDescription());
				title.setId(newID);
			} else {
				int newID = insertIntoAutoincrementMysqlTable(logger, "title_id", "INSERT INTO title_tbl (company_id, description) VALUES (?, ?)", title.getCompanyID(), title.getDescription());
				title.setId(newID);
			}
			
			// Only save gender mapping if the item has some new mapping
			if (title.getTitleGender() != null) {
				// Save gender mapping
				for (final Map.Entry<Integer,String> entry : title.getTitleGender().entrySet()) {
					update(logger, "INSERT INTO title_gender_tbl (title_id, gender, title) VALUES (?, ?, ?)", title.getId(), entry.getKey(), entry.getValue());
				}
			}
		} else {
			// Update existing Title
			int touchedRows = update(logger, "UPDATE title_tbl SET description = ? WHERE company_id = ? AND title_id = ?", title.getDescription(), title.getCompanyID(), title.getId());

			if (touchedRows == 1) {
				// Only delete gender mapping if the item has some new mapping
				if (title.getTitleGender() != null) {
					// Delete old gender mapping
					update(logger, "DELETE FROM title_gender_tbl WHERE title_id = ?", title.getId());

					// Save new gender mapping
					for (final Map.Entry<Integer, String> entry : title.getTitleGender().entrySet()) {
						update(logger, "INSERT INTO title_gender_tbl (title_id, gender, title) VALUES (?, ?, ?)", title.getId(), entry.getKey(), entry.getValue());
					}
				}
			}
		}
	}

	private static class OverviewRowMapper implements RowMapper<Title> {
		@Override
		public Title mapRow(ResultSet resultSet, int row) throws SQLException {
			Title title = new TitleImpl();
			title.setId(resultSet.getInt("title_id"));
			title.setCompanyID(resultSet.getInt("company_id"));
			title.setDescription(resultSet.getString("description"));
			title.setTitleGender(new TreeMap<>(Map.of(
				0, defaultString(resultSet.getString("gender0")),
				1, defaultString(resultSet.getString("gender1")),
				2, defaultString(resultSet.getString("gender2")),
				4, defaultString(resultSet.getString("gender4")),
				5, defaultString(resultSet.getString("gender5"))
				)));
			return title;
		}
	}

    protected class Title_RowMapper implements RowMapper<Title> {
		@Override
		public Title mapRow(ResultSet resultSet, int row) throws SQLException {
			Title title = new TitleImpl();
			title.setId(resultSet.getInt("title_id"));
			title.setCompanyID(resultSet.getInt("company_id"));
			title.setDescription(resultSet.getString("description"));

			Map<Integer, String> genderMap = new HashMap<>();
			query(logger, "SELECT gender, title FROM title_gender_tbl WHERE title_id = ?", new GenderMapCallback(genderMap), title.getId());
			title.setTitleGender(genderMap);

			return title;
		}
	}

	protected static class GenderMapCallback implements RowCallbackHandler {
		private Map<Integer, String> genderMap;

		public GenderMapCallback(Map<Integer, String> genderMap) {
			this.genderMap = Objects.requireNonNull(genderMap);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			genderMap.put(rs.getInt("gender"), rs.getString("title"));
		}
	}

    protected static class TitleLight_RowMapper implements RowMapper<Title> {
		@Override
		public Title mapRow(ResultSet resultSet, int row) throws SQLException {
			Title title = new TitleImpl();
			title.setId(resultSet.getInt("title_id"));
			title.setCompanyID(resultSet.getInt("company_id"));
			title.setDescription(resultSet.getString("description"));
			return title;
		}
	}
    
    protected static class SalutationEntry_RowMapper implements RowMapper<SalutationEntry> {
		@Override
		public SalutationEntry mapRow(ResultSet resultSet, int row) throws SQLException {
			return new SalutationEntryImpl(resultSet.getInt("title_id"), resultSet.getString("description"), resultSet.getInt("company_id"));
		}
	}
}
