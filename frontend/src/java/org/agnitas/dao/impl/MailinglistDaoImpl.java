/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.ComTarget;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mailinglist.bean.MailinglistEntry;

public class MailinglistDaoImpl extends PaginatedBaseDaoImpl implements MailinglistDao {

	private static final Logger logger = LogManager.getLogger(MailinglistDaoImpl.class);

	protected static final Set<String> SORTABLE_FIELDS = new HashSet<>(Arrays.asList("mailinglist_id", "shortname", "description", "creation_date", "change_date"));

	protected static final MailinglistEntryRowMapper MAILING_LIST_ENTRY_ROW_MAPPER = new MailinglistEntryRowMapper();

	protected static final org.agnitas.dao.impl.mapper.MailinglistRowMapper MAILINGLIST_ROW_MAPPER = new org.agnitas.dao.impl.mapper.MailinglistRowMapper();

	/** DAO accessing target groups. */
	private ComTargetDao targetDao;
	
	/**
	 * Set DAO accessing target groups.
	 * 
	 * @param targetDao DAO accessing target groups
	 */
	@Required
	public void setTargetDao(ComTargetDao targetDao) {
		this.targetDao = targetDao;
	}

	@Override
	public boolean checkMailinglistInUse(int mailinglistId, int companyId) {
		if (mailinglistId > 0 && companyId > 0) {
			return selectInt(logger, "SELECT COUNT(*) FROM mailing_tbl WHERE mailinglist_id = ? AND company_id = ? AND deleted = 0", mailinglistId, companyId) > 0;
		}

		return false;
	}

	@Override
	public Mailinglist getMailinglist(int listID, int companyId) {
		if (listID == 0 || companyId == 0) {
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Unable to load mailinglist (mailinglist ID %d, company ID %d)", listID, companyId));
			}
			
			return null;
		}

		return selectObjectDefaultNull(logger,
				"SELECT " + getMailinglistSqlFieldsForSelect() + " FROM mailinglist_tbl " +
						"WHERE mailinglist_id = ? AND deleted = 0 AND company_id = ?",
				MAILINGLIST_ROW_MAPPER, listID, companyId);
	}

	@Override
	public String getMailinglistName(int mailinglistId, int companyId) {
		if (mailinglistId > 0 && companyId > 0) {
			String sql = "SELECT shortname FROM mailinglist_tbl WHERE mailinglist_id = ? AND company_id = ?";
			return selectObjectDefaultNull(logger, sql, (rs, index) -> rs.getString("shortname"), mailinglistId, companyId);
		}

		return null;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int saveMailinglist(Mailinglist list) {
		if (list == null || list.getCompanyID() == 0) {
			return 0;
		}

		list.setChangeDate(new Date());

		if (list.getId() == 0) {
			list.setCreationDate(new Date());

			// Execute insert
			if (isOracleDB()) {
				return performInsertForOracle(list.getCompanyID(), list);
			}

			return performInsertForMySql(list.getCompanyID(), list);
		}

		return performUpdate(list.getCompanyID(), list);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int createMailinglist(int companyId, Mailinglist list) {
		if(list == null || companyId == 0) {
			return 0;
		}

		list.setChangeDate(new Date());
		list.setCreationDate(new Date());

		// Execute insert
		if (isOracleDB()) {
			return performInsertForOracle(companyId, list);
		}

		return performInsertForMySql(companyId, list);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int updateMailinglist(int companyId, Mailinglist list) {
		if (list == null || companyId == 0) {
			return 0;
		}

		list.setChangeDate(new Date());

		return performUpdate(companyId, list);
	}


	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteMailinglist(int listID, int companyId) {
		// should be impossible to delete last available mailinglist
		if (getCountOfMailinglists(companyId) <= 1) {
			return false;
		}
		update(logger, "DELETE FROM disabled_mailinglist_tbl WHERE mailinglist_id = ? AND company_id = ?", listID, companyId);
		return update(logger, "UPDATE mailinglist_tbl SET deleted = 1, binding_clean = 1, change_date = CURRENT_TIMESTAMP WHERE mailinglist_id = ? AND company_id = ? AND deleted = 0", listID, companyId) > 0;
	}
	
	/**
	 * Even deletes the last mailinglist wich would not be deleted by deleteMailinglist(int listID, int companyId)
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteAllMailinglist(int companyId) {
		int result = update(logger, "UPDATE mailinglist_tbl SET deleted = 1, binding_clean = 1, change_date = CURRENT_TIMESTAMP WHERE company_id = ? AND deleted = 0", companyId);
		if (result > 0) {
			return true;
		}

		return selectIntWithDefaultValue(logger, "SELECT COUNT(*) FROM mailinglist_tbl WHERE company_id = ? AND deleted = 0", 0, companyId) == 0;
	}

	@Override
	public List<Mailinglist> getMailingListsNames(int companyId) {
		String query = "SELECT mailinglist_id, shortname FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ? ORDER BY LOWER(shortname)";
		return select(logger, query, new MailingListNames_RowMapper(), companyId);
	}

	@Override
	public List<Mailinglist> getMailinglists(int companyId) {
		return select(logger,
				"SELECT " + getMailinglistSqlFieldsForSelect() + " FROM mailinglist_tbl " +
						"WHERE deleted = 0 AND company_id = ? ORDER BY LOWER(shortname) ASC",
				MAILINGLIST_ROW_MAPPER, companyId);
	}

	@Override
	public List<Mailinglist> getMailinglists(int companyId, int adminID) {
		return getMailinglists(companyId);
	}

	@Override
	public List<Integer> getMailinglistIds(int companyId) {
		return select(logger,
				"SELECT mailinglist_id FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ?",
				IntegerRowMapper.INSTANCE, companyId);
	}

	@Override
	public PaginatedListImpl<MailinglistEntry> getMailinglists(int companyId, int adminId, String sort, String direction, int page, int rownums) {
		if (!SORTABLE_FIELDS.contains(sort)) {
			sort = "shortname";
		}

		final String selectQuery = "SELECT m.mailinglist_id, m.shortname, m.description, m.creation_date, m.change_date FROM mailinglist_tbl m " +
				"   WHERE m.company_id = ? AND m.deleted = 0";
		
		final boolean sortAscending = AgnUtils.sortingDirectionToBoolean(direction, true);

		if (StringUtils.endsWith(sort, "_date")) {
			final String sortClause = "ORDER BY " + formatDateClause("m." + sort) + " " + (sortAscending ? "ASC" : "DESC");

			return selectPaginatedListWithSortClause(logger, selectQuery, sortClause, sort, sortAscending, page, rownums, MAILING_LIST_ENTRY_ROW_MAPPER, companyId);
		}

		return selectPaginatedList(logger, selectQuery, "mailinglist_tbl", sort, sortAscending, page, rownums, MAILING_LIST_ENTRY_ROW_MAPPER, companyId);
	}

	public String formatDateClause(String sortField) {
		if(isOracleDB()) {
			return "to_char(" + sortField + ", 'YYYYMMDD')";
		}

		return "DATE_FORMAT(" + sortField + ", '%Y%m%d')";
	}

	@Override
	public int getNumberOfActiveAdminSubscribers(int targetId, int companyId, int id) {
		Set<String> userTypes = Set.of("'" + UserType.Admin.getTypeCode() + "'");
		return getActiveSubscribersCount(companyId, targetId, id, userTypes);
	}

	@Override
	public int getNumberOfActiveTestSubscribers(int targetId, int companyId, int id) {
		Set<String> userTypes = new HashSet<>();

		userTypes.add("'" + UserType.Admin.getTypeCode() + "'");
		userTypes.add("'" + UserType.TestUser.getTypeCode() + "'");
		userTypes.add("'" + UserType.TestVIP.getTypeCode() + "'");

		return getActiveSubscribersCount(companyId, targetId, id, userTypes);
	}

	@Override
	public int getNumberOfActiveWorldSubscribers(int targetId, int companyId, int id) {
		return getActiveSubscribersCount(companyId, targetId, id, Collections.emptySet());
	}

	private int getActiveSubscribersCount(int companyId, int targetId, int id, Set<String> userTypes) {
		String sqlStatement = "SELECT COUNT(*) FROM customer_" + companyId + "_tbl cust, customer_" + companyId + "_binding_tbl bind WHERE" +
				" bind.mailinglist_id = ? AND cust.customer_id = bind.customer_id AND bind.user_status = " + UserStatus.Active.getStatusCode();

		if (targetId > 0) {
			ComTarget target = targetDao.getTarget(targetId, companyId);
			if (target != null) {
				sqlStatement += " AND (" + target.getTargetSQL() + ")";
			}
		}

		if (!userTypes.isEmpty()) {
			sqlStatement += " AND bind.user_type IN (" + StringUtils.join(userTypes, ", ") + ")";
		}

		try {
			return selectInt(logger, sqlStatement, id);
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public int getNumberOfActiveSubscribers(boolean admin, boolean test, boolean world, int targetId, int companyId, int id) {
		Set<String> userTypes = new HashSet<>();

		if (!world) {
			if (admin || test) {
				if (admin) {
					userTypes.add("'" + UserType.Admin.getTypeCode() + "'");
				}
				if (test) {
					userTypes.add("'" + UserType.TestUser.getTypeCode() + "'");
					userTypes.add("'" + UserType.TestVIP.getTypeCode() + "'");
				}

				// do not use target-group if pure admin/test-mailing
				// targetId = 0;		// Disabled by EMM-9121
			} else {
				return 0;
			}
		}

		String sqlStatement = "SELECT COUNT(*) FROM customer_" + companyId + "_tbl cust, customer_" + companyId + "_binding_tbl bind WHERE" +
				" bind.mailinglist_id = ? AND cust.customer_id = bind.customer_id AND bind.user_status = " + UserStatus.Active.getStatusCode();

		if (targetId > 0) {
			ComTarget aTarget = targetDao.getTarget(targetId, companyId);
			if (aTarget != null) {
				sqlStatement += " AND (" + aTarget.getTargetSQL() + ")";
			}
		}

		if (!userTypes.isEmpty()) {
			sqlStatement += " AND bind.user_type IN (" + StringUtils.join(userTypes, ", ") + ")";
		}

		try {
			return selectInt(logger, sqlStatement, id);
		} catch (Exception e) {
			// logging was already done
			return 0;
		}
	}

	@Override
	public Map<Integer, Integer> getMailinglistWorldSubscribersStatistics(int companyId, int mailinglistID) {
		Map<Integer, Integer> returnMap = new HashMap<>();
		List<Map<String,Object>> result = select(logger, "SELECT bind.user_status AS status, COUNT(*) AS amount FROM customer_" + companyId + "_tbl cust, customer_" + companyId + "_binding_tbl bind WHERE bind.mailinglist_id = ? AND cust.customer_id = bind.customer_id GROUP BY bind.user_status", mailinglistID);
		for (Map<String,Object> row : result) {
			returnMap.put(((Number) row.get("status")).intValue(), ((Number) row.get("amount")).intValue());
		}
		for (UserStatus userStatus : UserStatus.values() ) {
			if (!returnMap.containsKey(userStatus.getStatusCode())) {
				returnMap.put(userStatus.getStatusCode(), 0);
			}
		}
		return returnMap;
	}

    @Override
    public boolean mailinglistDeleted(int mailinglistId, int companyId) {
        if (mailinglistId <= 0) {
            return false;
        }
        return selectInt(logger, "SELECT deleted FROM mailinglist_tbl WHERE company_id = ? AND mailinglist_id = ?",
                companyId, mailinglistId) == 1;
    }

    @Override
    public Mailinglist getDeletedMailinglist(int mailinglistId, int companyId) {
        if (mailinglistId == 0 || companyId == 0) {
            if (logger.isInfoEnabled()) {
                logger.info(String.format("Unable to load deleted mailinglist (mailinglist ID %d, company ID %d)", mailinglistId, companyId));
            }
            return null;
        }

		return selectObjectDefaultNull(logger,
				"SELECT " + getMailinglistSqlFieldsForSelect() + ", deleted FROM mailinglist_tbl " +
						"WHERE mailinglist_id = ? AND deleted = 1 AND company_id = ?",
				MAILINGLIST_ROW_MAPPER, mailinglistId, companyId);
    }

	@Override
	public boolean mailinglistExists(String mailinglistName, int companyId) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ? AND shortname = ?", companyId, mailinglistName) > 0;
	}

	@Override
	public boolean exist(int mailinglistId, int companyId) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ? AND mailinglist_id = ?", companyId, mailinglistId) > 0;
	}

	@Override
	public int getCountOfMailinglists(int companyId) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ?", companyId);
	}

	protected String getMailinglistSqlFieldsForSelect() {
        final String[] fields = new String[] {"mailinglist_id", "company_id", "shortname", "description", "creation_date", "change_date"};
        return StringUtils.join(fields, ", ");
    }

    protected int performInsertForOracle(final int companyId, final Mailinglist mailinglist) {
        final int newID = selectInt(logger, "SELECT mailinglist_tbl_seq.NEXTVAL FROM DUAL");
        final int touchedLines = update(
                logger,
                "INSERT INTO mailinglist_tbl (mailinglist_id, company_id, shortname, description, creation_date, change_date) VALUES (?, ?, ?, ?, ?, ?)",
                newID,
                companyId,
                mailinglist.getShortname(),
                mailinglist.getDescription(),
                mailinglist.getCreationDate(),
                mailinglist.getChangeDate());

        if (touchedLines == 1) {
            mailinglist.setId(newID);
        }

        return mailinglist.getId();
    }

    protected int performInsertForMySql(final int companyId, final Mailinglist mailinglist) {
        final String insertStatement = "INSERT INTO mailinglist_tbl (company_id, shortname, description, creation_date, change_date) VALUES (?, ?, ?, ?, ?)";
        final int newID = insertIntoAutoincrementMysqlTable(logger, "mailinglist_id", insertStatement,
                companyId,
                mailinglist.getShortname(),
                mailinglist.getDescription(),
                mailinglist.getCreationDate(),
                mailinglist.getChangeDate());
        mailinglist.setId(newID);
        return mailinglist.getId();
    }

    protected int performUpdate(final int companyId, final Mailinglist mailinglist){
        //execute update
        final int touchedLines = update(
                logger,
                "UPDATE mailinglist_tbl SET shortname = ?, description = ?, change_date = ? WHERE mailinglist_id = ? AND deleted = 0 AND company_id = ?",
                mailinglist.getShortname(),
                mailinglist.getDescription(),
                mailinglist.getChangeDate(),
                mailinglist.getId(),
                companyId
        );

        return touchedLines == 1 ? mailinglist.getId() : 0;
    }

	public static class MailingListNames_RowMapper implements RowMapper<Mailinglist> {
		@Override
		public Mailinglist mapRow(ResultSet resultSet, int i) throws SQLException {
			Mailinglist mailing = new MailinglistImpl();

			mailing.setId(resultSet.getInt("mailinglist_id"));
			mailing.setShortname(resultSet.getString("shortname"));

			return mailing;
		}
	}

    public static class MailinglistEntryRowMapper implements RowMapper<MailinglistEntry> {

		@Override
		public MailinglistEntry mapRow(ResultSet resultSet, int i) throws SQLException {
			MailinglistEntry mailinglistEntry = new MailinglistEntry();
			mailinglistEntry.setId(resultSet.getInt("mailinglist_id"));
			mailinglistEntry.setShortname(resultSet.getString("shortname"));
			mailinglistEntry.setDescription(resultSet.getString("description"));
			mailinglistEntry.setCreationDate(resultSet.getTimestamp("creation_date"));
			mailinglistEntry.setChangeDate(resultSet.getTimestamp("change_date"));

			return mailinglistEntry;
		}
	}
}
