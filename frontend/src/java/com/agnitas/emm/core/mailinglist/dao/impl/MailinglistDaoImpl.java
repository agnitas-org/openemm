/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailinglist.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.Target;
import com.agnitas.beans.impl.MailinglistImpl;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.TargetDao;
import com.agnitas.dao.impl.PaginatedBaseDaoImpl;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.MailinglistRowMapper;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.transaction.annotation.Transactional;

public class MailinglistDaoImpl extends PaginatedBaseDaoImpl implements MailinglistDao {

	protected static final MailinglistRowMapper MAILINGLIST_ROW_MAPPER = new MailinglistRowMapper();

	/** DAO accessing target groups. */
	private TargetDao targetDao;
	
	/**
	 * Set DAO accessing target groups.
	 * 
	 * @param targetDao DAO accessing target groups
	 */
	public void setTargetDao(TargetDao targetDao) {
		this.targetDao = targetDao;
	}

	@Override
	public Mailinglist getMailinglist(int listID, int companyId) {
		return getMailinglist(listID, getMailinglistSqlFieldsForSelect(), companyId);
	}

	protected Mailinglist getMailinglist(int id, String columns, int companyId) {
		if (id == 0 || companyId == 0) {
			logger.info("Unable to load mailinglist (mailinglist ID {}, company ID {})", id, companyId);
			return null;
		}

		return selectObjectDefaultNull(
				"SELECT " + columns + " FROM mailinglist_tbl m " +
						"WHERE mailinglist_id = ? AND deleted = 0 AND company_id = ?",
				MAILINGLIST_ROW_MAPPER, id, companyId);
	}

	@Override
	public String getMailinglistName(int mailinglistId, int companyId) {
		if (mailinglistId > 0 && companyId > 0) {
			String sql = "SELECT shortname FROM mailinglist_tbl WHERE mailinglist_id = ? AND company_id = ?";
			return selectObjectDefaultNull(sql, (rs, index) -> rs.getString("shortname"), mailinglistId, companyId);
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
    @Transactional
	@DaoUpdateReturnValueCheck
	public boolean deleteMailinglist(int listID, int companyId) {
		// should be impossible to delete last available mailinglist
		if (getCountOfMailinglists(companyId) <= 1) {
			return false;
		}
        deleteFromDisabledMailinglistTbl(listID, companyId);
        return update("UPDATE mailinglist_tbl SET deleted = 1, binding_clean = 1, change_date = CURRENT_TIMESTAMP WHERE mailinglist_id = ? AND company_id = ? AND deleted = 0", listID, companyId) > 0;
	}

    protected void deleteFromDisabledMailinglistTbl(int listId, int companyId) {
        // overridden in extended class
    }

    /**
	 * Even deletes the last mailinglist wich would not be deleted by deleteMailinglist(int listID, int companyId)
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteAllMailinglist(int companyId) {
		int result = update("UPDATE mailinglist_tbl SET deleted = 1, binding_clean = 1, change_date = CURRENT_TIMESTAMP WHERE company_id = ? AND deleted = 0", companyId);
		if (result > 0) {
			return true;
		}

		return selectIntWithDefaultValue("SELECT COUNT(*) FROM mailinglist_tbl WHERE company_id = ? AND deleted = 0", 0, companyId) == 0;
	}

	@Override
	public List<Mailinglist> getMailingListsNames(int companyId) {
		String query = "SELECT mailinglist_id, shortname FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ? ORDER BY LOWER(shortname)";
		return select(query, new MailingListNames_RowMapper(), companyId);
	}

	@Override
	public List<Mailinglist> getMailinglists(int companyId) {
		return select(
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
		return select(
				"SELECT mailinglist_id FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ?",
				IntegerRowMapper.INSTANCE, companyId);
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
			Target target = targetDao.getTarget(targetId, companyId);
			if (target != null) {
				sqlStatement += " AND (" + target.getTargetSQL() + ")";
			}
		}

		if (!userTypes.isEmpty()) {
			sqlStatement += " AND bind.user_type IN (" + StringUtils.join(userTypes, ", ") + ")";
		}

		try {
			return selectInt(sqlStatement, id);
		} catch (Exception e) {
			return 0;
		}
	}

	@Override
	public int countSubscribers(int mailinglistId, int companyId, int targetId, boolean includeWorldRecipients, boolean includeAdminRecipients, boolean includeTestRecipients, Set<Integer> bindingStates) {
		// If no status given default to "active"
		final Set<Integer> filterBindingStates = bindingStates == null || bindingStates.isEmpty()
				? Set.of(UserStatus.Active.getStatusCode())
				: bindingStates;

		final Set<String> userTypes = new HashSet<>();

		if (!includeWorldRecipients) {
			if (includeTestRecipients || includeAdminRecipients) {
				if (includeAdminRecipients) {
					userTypes.add("'" + UserType.Admin.getTypeCode() + "'");
				}
				if (includeTestRecipients) {
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
				" bind.mailinglist_id = ? AND cust.customer_id = bind.customer_id";

		// Append binding states
		sqlStatement += " AND bind.user_status IN (";
		sqlStatement += filterBindingStates.stream().map(Object::toString).collect(Collectors.joining(","));
		sqlStatement += ")";

		if (targetId > 0) {
			final Target targetgroup = targetDao.getTarget(targetId, companyId);
			if (targetgroup != null) {
				sqlStatement += " AND (" + targetgroup.getTargetSQL() + ")";
			}
		}

		if (!userTypes.isEmpty()) {
			sqlStatement += " AND bind.user_type IN (" + StringUtils.join(userTypes, ", ") + ")";
		}

		try {
			return selectInt(sqlStatement, mailinglistId);
		} catch (Exception e) {
			// logging was already done
			return 0;
		}
	}

	@Override
	public Map<Integer, Integer> getMailinglistWorldSubscribersStatistics(int companyId, int mailinglistID) {
		Map<Integer, Integer> returnMap = new HashMap<>();
		List<Map<String,Object>> result = select("""
				SELECT bind.user_status AS status, COUNT(*) AS amount
				FROM customer_%d_tbl cust,
				     customer_%d_binding_tbl bind
				WHERE bind.mailinglist_id = ?
				  AND cust.customer_id = bind.customer_id
				GROUP BY bind.user_status
				""".formatted(companyId, companyId), mailinglistID);
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
        return selectInt("SELECT deleted FROM mailinglist_tbl WHERE company_id = ? AND mailinglist_id = ?",
                companyId, mailinglistId) == 1;
    }

    @Override
    public Mailinglist getDeletedMailinglist(int mailinglistId, int companyId) {
        if (mailinglistId == 0 || companyId == 0) {
			logger.info("Unable to load deleted mailinglist (mailinglist ID {}, company ID {})", mailinglistId, companyId);
            return null;
        }

		return selectObjectDefaultNull(
				"SELECT " + getMailinglistSqlFieldsForSelect() + ", deleted FROM mailinglist_tbl " +
						"WHERE mailinglist_id = ? AND deleted = 1 AND company_id = ?",
				MAILINGLIST_ROW_MAPPER, mailinglistId, companyId);
    }

	@Override
	public boolean mailinglistExists(String mailinglistName, int companyId) {
		return selectInt("SELECT COUNT(*) FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ? AND shortname = ?", companyId, mailinglistName) > 0;
	}

	@Override
	public boolean exist(int mailinglistId, int companyId) {
		return selectInt("SELECT COUNT(*) FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ? AND mailinglist_id = ?", companyId, mailinglistId) > 0;
	}

	@Override
	public int getCountOfMailinglists(int companyId) {
		return selectInt("SELECT COUNT(*) FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ?", companyId);
	}

	protected String getMailinglistSqlFieldsForSelect() {
        final String[] fields = new String[] {"mailinglist_id", "company_id", "shortname", "description", "creation_date", "change_date", "sender_email", "reply_email"};
        return StringUtils.join(fields, ", ");
    }

    protected int performInsertForOracle(int companyId, Mailinglist mailinglist) {
        final int newID = selectInt("SELECT mailinglist_tbl_seq.NEXTVAL FROM DUAL");
        final int touchedLines = update(
                "INSERT INTO mailinglist_tbl (mailinglist_id, company_id, shortname, description, creation_date, change_date, sender_email, reply_email) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                newID,
                companyId,
                mailinglist.getShortname(),
                mailinglist.getDescription(),
                mailinglist.getCreationDate(),
                mailinglist.getChangeDate(),
				mailinglist.getSenderEmail(),
				mailinglist.getReplyEmail()
		);

        if (touchedLines == 1) {
            mailinglist.setId(newID);
        }

        return mailinglist.getId();
    }

    protected int performInsertForMySql(int companyId, Mailinglist mailinglist) {
        final String insertStatement = "INSERT INTO mailinglist_tbl (company_id, shortname, description, creation_date, change_date, sender_email, reply_email) VALUES (?, ?, ?, ?, ?, ?, ?)";
        final int newID = insert("mailinglist_id", insertStatement,
                companyId,
                mailinglist.getShortname(),
                mailinglist.getDescription(),
                mailinglist.getCreationDate(),
                mailinglist.getChangeDate(),
				mailinglist.getSenderEmail(),
				mailinglist.getReplyEmail());
        mailinglist.setId(newID);
        return mailinglist.getId();
    }

    protected int performUpdate(int companyId, Mailinglist mailinglist){
        //execute update
        final int touchedLines = update(
                "UPDATE mailinglist_tbl SET shortname = ?, description = ?, change_date = ?, sender_email = ?, reply_email = ? WHERE mailinglist_id = ? AND deleted = 0 AND company_id = ?",
                mailinglist.getShortname(),
                mailinglist.getDescription(),
                mailinglist.getChangeDate(),
				mailinglist.getSenderEmail(),
				mailinglist.getReplyEmail(),
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

}
