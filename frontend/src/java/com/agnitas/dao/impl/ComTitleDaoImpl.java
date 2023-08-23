/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
	
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(ComTitleDaoImpl.class);
		
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
	public List<Title> getTitles(int companyID) {
		return select(logger, "SELECT company_id, title_id, description FROM title_tbl WHERE company_id IN (0, ?) ORDER BY LOWER(description)", new TitleLight_RowMapper(), companyID);
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
