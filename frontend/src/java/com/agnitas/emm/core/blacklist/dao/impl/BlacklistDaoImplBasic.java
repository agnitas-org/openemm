/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
import java.util.Objects;
import java.util.Set;

import org.agnitas.beans.BlackListEntry;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.BlackListEntryImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.MailinglistRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.blacklist.dao.ComBlacklistDao;

public class BlacklistDaoImplBasic extends BaseDaoImpl implements ComBlacklistDao {
	private static final transient Logger logger = Logger.getLogger(BlacklistDaoImplBasic.class);

	private static final String[] SUPPORTED_COLUMNS = new String[]{"email", "reason", "timestamp"};

	protected static String getCustomerBanTableName(int companyId) {
		return "cust" + companyId + "_ban_tbl";
	}

	private static final MailinglistRowMapper MAILINGLIST_ROW_MAPPER = new MailinglistRowMapper();

	private ConfigService configService;
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}

	final ConfigService getConfigService() {
		return this.configService;
	}
	
	/**
	 * Inserts a new entry in the blacklist table. returns false, if something
	 * went wrong.
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean insert(@VelocityCheck int companyID, String email, String reason) {
		if (StringUtils.isBlank(email)) {
			return false;
		} else {
			email = AgnUtils.normalizeEmail(email);

			if (exist(companyID, email)) {
				return true;
			} else {
				String sql = "INSERT INTO " + getCustomerBanTableName(companyID) + " (email, reason) VALUES (?, ?)";
				return update(logger, sql, email, reason) == 1;
			}
		}
	}

	/**
	 * Update an entry in the blacklist table. returns false, if something went wrong.
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean update(@VelocityCheck int companyID, String email, String reason) {
		if (StringUtils.isBlank(email)) {
			return false;
		} else {
			email = AgnUtils.normalizeEmail(email);
			String sql = "UPDATE " + getCustomerBanTableName(companyID) + " SET reason = ? WHERE email = ?";
			return update(logger, sql, reason, email) == 1;
		}
	}
	
	/**
	 * Get the complete list of blacklisted addresses, including company and global
	 */
	@Override
	public Set<String> loadBlackList(@VelocityCheck int companyID) throws Exception {
		Set<String> blacklist = new HashSet<>();
		try {
			List<String> blacklistCompany = select(logger, "SELECT email FROM cust" + companyID + "_ban_tbl", new StringRowMapper());
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
	public boolean delete(@VelocityCheck int companyID, String email) {
		if (StringUtils.isBlank(email)) {
			return false;
		} else {
			String sql = "DELETE FROM " + getCustomerBanTableName(companyID) + " WHERE email = ?";
			return update(logger, sql, AgnUtils.normalizeEmail(email)) == 1;
		}
	}

	@Override
	public PaginatedListImpl<BlackListEntry> getBlacklistedRecipients(@VelocityCheck int companyID, String sort, String direction, int page, int rownums) {
		return getBlacklistedRecipients(companyID, sort, direction, page, rownums, null);
	}

	@Override
	public PaginatedListImpl<BlackListEntry> getBlacklistedRecipients(@VelocityCheck int companyID, String sort, String direction, int page, int rownums, String likePattern) {
		String wildcardLikePattern = replaceWildCardCharacters(StringUtils.defaultString(likePattern));

		sort = getSortableColumn(sort);

		// Only alphanumeric values may be sorted with upper or lower, which always returns a string value, for keeping the order of numeric values
		String sortClause;
		try {
			if (DbUtilities.getColumnDataType(getDataSource(), getCustomerBanTableName(companyID), sort).getSimpleDataType() == SimpleDataType.Characters) {
				sortClause = " ORDER BY LOWER(" + sort + ")" ;
			} else {
				sortClause = " ORDER BY " + sort;
			}
			if (StringUtils.isNotBlank(direction)) {
				sortClause = sortClause + " " + direction;
			}
		} catch (Exception e) {
			logger.error("Invalid sort field", e);
			sortClause = "";
		}

		/*
		 * TODO Bugfix for Mantis ID 798
		 * The following statement uses the "lowercase" function of the Database
		 * this is not very efficient. But for now, there are some values in the DB which
		 * are not lowercase. Therfore, as soon as all emails are lowercase remove the
		 * statements for better performance.
		 */
		String whereClause = "";
		if (StringUtils.isNotEmpty(wildcardLikePattern)) {
			if (isOracleDB()) {
				whereClause = " WHERE LOWER(email) LIKE LOWER('%' || ? || '%')";
			} else {
				whereClause = " WHERE LOWER(email) LIKE LOWER(CONCAT('%', ?, '%'))";
			}
		}

		int totalRows;
		try {
			if (StringUtils.isEmpty(wildcardLikePattern)) {
				totalRows = selectInt(logger, "SELECT COUNT(email) FROM " + getCustomerBanTableName(companyID));
			} else {
				totalRows = selectInt(logger, "SELECT COUNT(email) FROM " + getCustomerBanTableName(companyID) + whereClause, wildcardLikePattern);
			}
		} catch (Exception e) {
			totalRows = 0;
		}

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
			if (StringUtils.isEmpty(wildcardLikePattern)) {
				blacklistElements = select(logger, blackListQuery, new BlackListEntry_RowMapper(), offset + 1, offset + rownums);
			} else {
				blacklistElements = select(logger, blackListQuery, new BlackListEntry_RowMapper(), wildcardLikePattern, offset + 1, offset + rownums);
			}
        } else {
			if (StringUtils.isEmpty(wildcardLikePattern)) {
				blacklistElements = select(logger, blackListQuery, new BlackListEntry_RowMapper(), offset, rownums);
			} else {
				blacklistElements = select(logger, blackListQuery, new BlackListEntry_RowMapper(), wildcardLikePattern, offset, rownums);
			}
        }

		// Workaround: if you are on a page higher than 1 and the result of the
		// blacklist-search would be on page 1
		// then nothing is shown. Therefore if we have no result, we query again
		// with page 1 as parameter.
		// if we then find nothing, there is nothing to find.
		if (blacklistElements.size() == 0) {
			if (StringUtils.isEmpty(wildcardLikePattern)) {
				blacklistElements = select(logger, blackListQuery, new BlackListEntry_RowMapper(), 1, rownums);
			} else {
				blacklistElements = select(logger, blackListQuery, new BlackListEntry_RowMapper(), wildcardLikePattern, 1, rownums);
			}
		}

		return new PaginatedListImpl<>(blacklistElements, totalRows, rownums, page, sort, direction);
	}

	@Override
	public boolean exist(@VelocityCheck int companyID, String email) {
		return exist(email, getCustomerBanTableName(companyID));
	}

    protected boolean exist(String email, String tableName) {
        try {
            String sql = "SELECT COUNT(email) FROM " + tableName + " WHERE email = ?";
            int resultCount = selectInt(logger, sql, AgnUtils.normalizeEmail(email));
            return resultCount > 0;
        } catch (DataAccessException e) {
            return false;
        }
    }

	@Override
	public List<String> getBlacklist(@VelocityCheck int companyID) {
		try {
			String blackListQuery = "SELECT email FROM " + getCustomerBanTableName(companyID);
			List<Map<String, Object>> results = select(logger, blackListQuery);
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
    public List<BlackListEntry> getBlacklistedRecipients( @VelocityCheck int companyID) {
        String blackListQuery = "SELECT email, reason, timestamp AS creation_date FROM " + getCustomerBanTableName(companyID) + " ORDER BY email";
        return select(logger, blackListQuery, new BlackListEntry_RowMapper());
    }

	@Override
	public List<Mailinglist> getMailinglistsWithBlacklistedBindings( @VelocityCheck int companyId, String email) {
		String query = "SELECT DISTINCT m.mailinglist_id, m.company_id, m.shortname, m.description FROM mailinglist_tbl m, customer_" + companyId + "_tbl c, customer_" + companyId + "_binding_tbl b" +
			" WHERE c.email = ? AND b.customer_id = c.customer_id AND b.user_status = ? AND b.mailinglist_id = m.mailinglist_id AND m.deleted = 0 AND m.company_id = ?";


		List<Mailinglist> list = select(logger, query, MAILINGLIST_ROW_MAPPER, email, UserStatus.Blacklisted.getStatusCode(), companyId);
		
		return list;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateBlacklistedBindings( @VelocityCheck int companyId, String email, List<Integer> mailinglistIds, int userStatus) {
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
			
			update(logger, update, userStatus, email, UserStatus.Blacklisted.getStatusCode(), mailinglistId);
		}
	}
	
	/**
	 * Email address blacklist check, but only for the companies custxxx_ban_tbl
	 */
	@Override
	public boolean blacklistCheckCompanyOnly(String email, @VelocityCheck int companyID) {
		final boolean useNewWildcards = this.getConfigService().getBooleanValue(ConfigValue.Development.UseNewBlacklistWildcards, companyID);
		
		try {
			if(useNewWildcards) {
				final String escapeClause = isOracleDB() ? " ESCAPE '\\'" : "";
				
				final String sql = String.format(
						"SELECT COUNT(*) FROM %s WHERE ? LIKE REPLACE(REPLACE(email, '_', '\\_'), '*', '%%') %s", 
						getCustomerBanTableName(companyID),
						escapeClause);

				return selectInt(logger, sql, AgnUtils.normalizeEmail(email)) > 0;
			} else {
				return selectInt(logger, "SELECT COUNT(*) FROM " + getCustomerBanTableName(companyID) + " WHERE ? LIKE REPLACE(REPLACE(email, '?', '_'), '*', '%')", AgnUtils.normalizeEmail(email)) > 0;
			}
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
		final boolean useNewWildcards = this.getConfigService().getBooleanValue(ConfigValue.Development.UseNewBlacklistWildcards, companyID);

		if(useNewWildcards) {
			final String escapeClause = isOracleDB() ? " ESCAPE '\\'" : "";
			
			final String sql = String.format(
					"SELECT email, reason, timestamp AS creation_date FROM %s WHERE ? LIKE REPLACE(REPLACE(email, '_', '\\_'), '*', '%%') %s ORDER BY email", 
					getCustomerBanTableName(companyID),
					escapeClause);

			return select(logger, sql, new BlackListEntry_RowMapper(), AgnUtils.normalizeEmail(email));
			
		} else {
			return select(logger, "SELECT email, reason, timestamp AS creation_date FROM " + getCustomerBanTableName(companyID) + " WHERE ? LIKE REPLACE(REPLACE(email, '?', '_'), '*', '%') ORDER BY email", new BlackListEntry_RowMapper(), AgnUtils.normalizeEmail(email));
		}
	}
}
