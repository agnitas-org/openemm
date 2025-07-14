/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.blacklist.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.blacklist.dao.BlacklistDao;
import com.agnitas.emm.core.globalblacklist.forms.BlacklistOverviewFilter;
import com.agnitas.beans.BlackListEntry;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.impl.BlackListEntryImpl;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.dao.impl.BaseDaoImpl;
import com.agnitas.dao.impl.mapper.MailinglistRowMapper;
import com.agnitas.dao.impl.mapper.StringRowMapper;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DbColumnType;
import com.agnitas.util.DbColumnType.SimpleDataType;
import com.agnitas.util.DbUtilities;
import com.agnitas.util.Tuple;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

public class BlacklistDaoImpl extends BaseDaoImpl implements BlacklistDao {
	
	private static final String[] SUPPORTED_COLUMNS = new String[]{"email", "reason", "timestamp"};

	protected static String getCustomerBanTableName(int companyId) {
		return "cust" + companyId + "_ban_tbl";
	}

	private static final MailinglistRowMapper MAILINGLIST_ROW_MAPPER = new MailinglistRowMapper();

	/**
	 * Inserts a new entry in the blacklist table. returns false, if something
	 * went wrong.
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean insert(int companyID, String email, String reason) {
		if (StringUtils.isBlank(email)) {
			return false;
		} else {
			email = AgnUtils.normalizeEmail(email);

			if (exist(companyID, email)) {
				return true;
			} else {
				String sql = "INSERT INTO " + getCustomerBanTableName(companyID) + " (email, reason) VALUES (?, ?)";
				return update(sql, email, reason) == 1;
			}
		}
	}

	/**
	 * Update an entry in the blacklist table. returns false, if something went wrong.
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean update(int companyID, String email, String reason) {
		if (StringUtils.isBlank(email)) {
			return false;
		} else {
			email = AgnUtils.normalizeEmail(email);
			String sql = "UPDATE " + getCustomerBanTableName(companyID) + " SET reason = ? WHERE email = ?";
			return update(sql, reason, email) == 1;
		}
	}
	
	/**
	 * Get the complete list of blacklisted addresses, including company and global
	 */
	@Override
	public Set<String> loadBlackList(int companyID) {
		Set<String> blacklist = new HashSet<>();
		try {
			List<String> blacklistCompany = select("SELECT email FROM cust" + companyID + "_ban_tbl", StringRowMapper.INSTANCE);
			for (String email : blacklistCompany) {
				blacklist.add(email.toLowerCase());
			}
		} catch (Exception e) {
			logger.error("loadBlacklist: " + e);
			throw e;
		}
		return blacklist;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean delete(int companyID, String email) {
		if (StringUtils.isBlank(email)) {
			return false;
		} else {
			String sql = "DELETE FROM " + getCustomerBanTableName(companyID) + " WHERE email = ?";
			return update(sql, AgnUtils.normalizeEmail(email)) == 1;
		}
	}

	@Override
	public boolean delete(Set<String> emails, int companyId) {
		String sql = "DELETE FROM " + getCustomerBanTableName(companyId) + " WHERE " + makeBulkInClauseForString("email", emails);
		return update(sql) > 0;
	}

	@Override
	public PaginatedListImpl<BlackListEntry> getBlacklistedRecipients(BlacklistOverviewFilter filter, int companyID) {
		String sort = getSortableColumn(filter.getSort());

		// Only alphanumeric values may be sorted with upper or lower, which always returns a string value, for keeping the order of numeric values
		String sortClause = "";
		DbColumnType columnType = DbUtilities.getColumnDataType(getDataSource(), getCustomerBanTableName(companyID), sort);

		if (columnType == null) {
			logger.error("Invalid sort field: {}", sort);
		} else {
			if (columnType.getSimpleDataType() == SimpleDataType.Characters) {
				sortClause = " ORDER BY LOWER(" + sort + ")" ;
			} else {
				sortClause = " ORDER BY " + sort;
			}
			if (StringUtils.isNotBlank(filter.getOrder())) {
				sortClause = sortClause + " " + filter.getOrder();
			}
		}

		Tuple<String, List<Object>> queryParts = applyOverviewFilters(filter);
		String whereClause = queryParts.getFirst();
		List<Object> params = queryParts.getSecond();

		int totalRows;
		try {
			totalRows = selectInt("SELECT COUNT(email) FROM " + getCustomerBanTableName(companyID) + whereClause, params.toArray());
		} catch (Exception e) {
			totalRows = 0;
		}

		int page = filter.getPage();
		int rownums = filter.getNumberOfRows();

		// page numeration begins with 1
		if (page < 1) {
			page = 1;
		}
        page = AgnUtils.getValidPageNumber(totalRows, page, rownums);
        int offset = (page - 1) * rownums;
		
		String blackListQuery;
		if (isOracleDB()) {
			blackListQuery = "SELECT * FROM (SELECT selection.*, rownum AS r FROM ("
					+ "SELECT email, reason, timestamp AS creation_date FROM " + getCustomerBanTableName(companyID)
					+ whereClause
					+ sortClause
				+ ") selection)"
				+ " WHERE r BETWEEN ? AND ?";
		} else {
			blackListQuery = "SELECT email, reason, timestamp AS creation_date FROM " + getCustomerBanTableName(companyID)
					+ whereClause
					+ sortClause
					+ " LIMIT ?, ?";
		}

        List<BlackListEntry> blacklistElements = null;
        if (isOracleDB()) {
			ArrayList<Object> paramsCopy = new ArrayList<>(params);
			paramsCopy.addAll(List.of(offset + 1, offset + rownums));
			blacklistElements = select(blackListQuery, new BlackListEntry_RowMapper(), paramsCopy.toArray());
		} else {
			ArrayList<Object> paramsCopy = new ArrayList<>(params);
			paramsCopy.addAll(List.of(offset, rownums));
			blacklistElements = select(blackListQuery, new BlackListEntry_RowMapper(), paramsCopy.toArray());
        }

		// Workaround: if you are on a page higher than 1 and the result of the
		// blacklist-search would be on page 1
		// then nothing is shown. Therefore if we have no result, we query again
		// with page 1 as parameter.
		// if we then find nothing, there is nothing to find.
		if (blacklistElements.isEmpty()) {
			params.addAll(List.of(1, rownums));
			blacklistElements = select(blackListQuery, new BlackListEntry_RowMapper(), params.toArray());
		}

		PaginatedListImpl<BlackListEntry> paginatedList = new PaginatedListImpl<>(blacklistElements, totalRows, rownums, page, sort, filter.getOrder());
		if (filter.isUiFiltersSet()) {
			paginatedList.setNotFilteredFullListSize(selectInt("SELECT COUNT(*) FROM " + getCustomerBanTableName(companyID)));
		}

		return paginatedList;
	}

	private Tuple<String, List<Object>> applyOverviewFilters(BlacklistOverviewFilter filter) {
		StringBuilder whereClause = new StringBuilder(" WHERE ");
		List<Object> params = new ArrayList<>();

		if (StringUtils.isNotBlank(filter.getEmail())) {
			/*
			 * TODO Bugfix for Mantis ID 798
			 * The following statement uses the "lowercase" function of the Database
			 * this is not very efficient. But for now, there are some values in the DB which
			 * are not lowercase. Therfore, as soon as all emails are lowercase remove the
			 * statements for better performance.
			 */
			whereClause.append(getPartialSearchFilter("email"));
			params.add(replaceWildCardCharacters(filter.getEmail()));
		} else {
			whereClause.append("1=1");
		}

		if (StringUtils.isNotBlank(filter.getReason())) {
			whereClause.append(getPartialSearchFilterWithAnd("reason"));
			params.add(filter.getReason());
		}

		whereClause.append(getDateRangeFilterWithAnd("timestamp", filter.getCreationDate(), params));

		return new Tuple<>(whereClause.toString(), params);
	}

	@Override
	public boolean exist(int companyID, String email) {
		return exist(email, getCustomerBanTableName(companyID));
	}

    protected boolean exist(String email, String tableName) {
        try {
            String sql = "SELECT COUNT(email) FROM " + tableName + " WHERE email = ?";
            int resultCount = selectInt(sql, AgnUtils.normalizeEmail(email));
            return resultCount > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }

	@Override
	public List<String> getBlacklist(int companyID) {
		try {
			String blackListQuery = "SELECT email FROM " + getCustomerBanTableName(companyID);
			List<Map<String, Object>> results = select(blackListQuery);
			List<String> blacklistElements = new ArrayList<>();
			if (results != null) {
				for (Map<String, Object> row : results) {
					blacklistElements.add((String) row.get("email"));
				}
			}
			return blacklistElements;
		} catch (DataAccessException e) {
			return null;
		}
	}

    @Override
    public List<BlackListEntry> getBlacklistedRecipients( int companyID) {
        String blackListQuery = "SELECT email, reason, timestamp AS creation_date FROM " + getCustomerBanTableName(companyID) + " ORDER BY email";
        return select(blackListQuery, new BlackListEntry_RowMapper());
    }

	@Override
	public List<Mailinglist> getMailinglistsWithBlacklistedBindings(Set<String> emails, int companyId) {
		String query = "SELECT DISTINCT m.mailinglist_id, m.company_id, m.shortname, m.description FROM mailinglist_tbl m, customer_" + companyId + "_tbl c, customer_" + companyId + "_binding_tbl b" +
			" WHERE c.email IN (" + AgnUtils.csvQMark(emails.size()) + ") AND b.customer_id = c.customer_id AND b.user_status = ? AND b.mailinglist_id = m.mailinglist_id AND m.deleted = 0 AND m.company_id = ?";

		List<Object> params = new ArrayList<>(emails);
		params.addAll(List.of(UserStatus.Blacklisted.getStatusCode(), companyId));

        return select(query, MAILINGLIST_ROW_MAPPER, params.toArray());
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateBlacklistedBindings( int companyId, String email, List<Integer> mailinglistIds, UserStatus userStatus) {
		if (mailinglistIds.size() == 0) {
			if (logger.isInfoEnabled()) {
				logger.info("List of mailinglist IDs is empty - doing nothing");
			}

			return;
		}
		
		String update =
			"UPDATE customer_" + companyId + "_binding_tbl" +
			" SET user_status = ?, timestamp = CURRENT_TIMESTAMP" +
			" WHERE customer_id IN (SELECT customer_id FROM customer_" + companyId + "_tbl WHERE email = ?)" +
			" AND user_status = ? AND mailinglist_id = ?";
		
		for (int mailinglistId : mailinglistIds) {
			if (logger.isDebugEnabled()) {
				logger.debug( email + ": updating user status for mailinglist " + mailinglistId);
			}
			
			update(update, userStatus.getStatusCode(), email, UserStatus.Blacklisted.getStatusCode(), mailinglistId);
		}
	}
	
	/**
	 * Email address blacklist check, but only for the companies custxxx_ban_tbl
	 */
	@Override
	public boolean blacklistCheckCompanyOnly(String email, int companyID) {
		try {
			final String escapeClause = isOracleDB() ? " ESCAPE '\\'" : "";
			
			final String sql = String.format(
					"SELECT COUNT(*) FROM %s WHERE ? LIKE REPLACE(REPLACE(email, '_', '\\_'), '*', '%%') %s", 
					getCustomerBanTableName(companyID),
					escapeClause);

			return selectInt(sql, AgnUtils.normalizeEmail(email)) > 0;
		} catch (Exception e) {
			logger.error("Error checking blacklist for email '" + email + "'", e);

			// For safety, assume email is blacklisted in case of an error
			return true;
		}
	}
	
	@Override
	public boolean blacklistCheck(String email, int companyID) {
		return blacklistCheckCompanyOnly(email, companyID);
	}

	private String getSortableColumn(String column) {
		if (StringUtils.isBlank(column)) {
			return "email";
		}

		column = column.toLowerCase();

		if (ArrayUtils.contains(SUPPORTED_COLUMNS, column)) {
			return column;
		} else {
			return "email";
		}
	}

	public static class BlackListEntry_RowMapper implements RowMapper<BlackListEntry> {
		@Override
		public BlackListEntry mapRow(ResultSet rs, int row) throws SQLException {
			String email = rs.getString("email");
			String reason = rs.getString("reason");
			Date creationDate = rs.getTimestamp("creation_date");
			return new BlackListEntryImpl(email, reason, creationDate);
		}
	}

	@Override
	public List<BlackListEntry> getBlacklistCheckEntries(int companyID, String email) {
		final String escapeClause = isOracleDB() ? " ESCAPE '\\'" : "";
		
		final String sql = String.format(
				"SELECT email, reason, timestamp AS creation_date FROM %s WHERE ? LIKE REPLACE(REPLACE(email, '_', '\\_'), '*', '%%') %s ORDER BY email", 
				getCustomerBanTableName(companyID),
				escapeClause);

		return select(sql, new BlackListEntry_RowMapper(), AgnUtils.normalizeEmail(email));
	}
}
