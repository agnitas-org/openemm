/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.agnitas.beans.ComTarget;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mailinglist.bean.MailinglistEntry;
import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.MailinglistImpl;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

public class MailinglistDaoImpl extends PaginatedBaseDaoImpl implements MailinglistDao {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(MailinglistDaoImpl.class);

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
	public boolean checkMailinglistInUse(int mailinglistId, @VelocityCheck int companyId) {
		if (mailinglistId > 0 && companyId > 0) {
			return selectInt(logger, "SELECT COUNT(*) FROM mailing_tbl WHERE mailinglist_id = ? AND company_id = ? AND deleted = 0", mailinglistId, companyId) > 0;
		} else {
			return false;
		}
	}

	@Override
	public Mailinglist getMailinglist(int listID, @VelocityCheck int companyId) {
		if (listID == 0 || companyId == 0) {
			return null;
		} else {
			return selectObjectDefaultNull(logger,
					"SELECT " + getMailinglistSqlFieldsForSelect() + " FROM mailinglist_tbl " +
							"WHERE mailinglist_id = ? AND deleted = 0 AND company_id = ?",
					MAILINGLIST_ROW_MAPPER, listID, companyId);
		}
	}

	@Override
	public String getMailinglistName(int mailinglistId, @VelocityCheck int companyId) {
		if (mailinglistId > 0 && companyId > 0) {
			String sql = "SELECT shortname FROM mailinglist_tbl WHERE mailinglist_id = ? AND company_id = ?";
			return selectObjectDefaultNull(logger, sql, (rs, index) -> rs.getString("shortname"), mailinglistId, companyId);
		} else {
			return null;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int saveMailinglist(Mailinglist list) {
		if (list == null || list.getCompanyID() == 0) {
			return 0;
		} else {
			list.setChangeDate(new Date());
			
			if (list.getId() == 0) {
				list.setCreationDate(new Date());
				
				// Execute insert
				if (isOracleDB()) {
					return performInsertForOracle(list.getCompanyID(), list);
				} else {
					return performInsertForMySql(list.getCompanyID(), list);
				}
			} else {
				return performUpdate(list.getCompanyID(), list);
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int createMailinglist(@VelocityCheck int companyId, Mailinglist list) {
		if(list == null || companyId == 0) {
			return 0;
		}

		list.setChangeDate(new Date());
		list.setCreationDate(new Date());

		// Execute insert
		if (isOracleDB()) {
			return performInsertForOracle(companyId, list);
		} else {
			return performInsertForMySql(companyId, list);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int updateMailinglist(@VelocityCheck int companyId, Mailinglist list) {
		if(list == null || companyId == 0) {
			return 0;
		}

		list.setChangeDate(new Date());

		return performUpdate(companyId, list);
	}


	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteMailinglist(int listID, @VelocityCheck int companyId) {
		// Always keep the mailinglist with the lowest mailinglist_id. Should be the "Standard ... NICHT LÖSCHEN!!!" mailinglist
		// This must be done in two steps because mysql doesn't allow to update same table as used in a subquery
		Integer alwaysKeepMailinglistID = select(logger, "SELECT MIN(mailinglist_id) FROM mailinglist_tbl WHERE company_id = ? AND deleted = 0", Integer.class, companyId);
		if (alwaysKeepMailinglistID != null && alwaysKeepMailinglistID != listID) {
			return update(logger, "UPDATE mailinglist_tbl SET deleted = 1, binding_clean = 1, change_date = CURRENT_TIMESTAMP WHERE mailinglist_id = ? AND company_id = ? AND deleted = 0", listID, companyId) > 0;
		} else {
			return false;
		}
	}
	
	/**
	 * Even deletes the last mailinglist wich would not be deleted by deleteMailinglist(int listID, @VelocityCheck int companyId)
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteAllMailinglist(@VelocityCheck int companyId) {
		int result = update(logger, "UPDATE mailinglist_tbl SET deleted = 1, binding_clean = 1, change_date = CURRENT_TIMESTAMP WHERE company_id = ? AND deleted = 0", companyId);
		if (result > 0) {
			return true;
		} else {
			return selectIntWithDefaultValue(logger, "SELECT COUNT(*) FROM mailinglist_tbl WHERE company_id = ? AND deleted = 0", 0, companyId) == 0;
		}
	}

	@Override
	public List<Mailinglist> getMailingListsNames(@VelocityCheck int companyId) {
		String query = "SELECT mailinglist_id, shortname FROM mailinglist_tbl WHERE deleted = 0 AND company_id = ? ORDER BY LOWER(shortname)";
		return select(logger, query, new MailingListNames_RowMapper(), companyId);
	}

	@Override
	public List<Mailinglist> getMailinglists(@VelocityCheck int companyId) {
		return select(logger,
				"SELECT " + getMailinglistSqlFieldsForSelect() + " FROM mailinglist_tbl " +
						"WHERE deleted = 0 AND company_id = ? ORDER BY LOWER(shortname) ASC",
				MAILINGLIST_ROW_MAPPER, companyId);
	}

	@Override
	public PaginatedListImpl<MailinglistEntry> getMailinglists(@VelocityCheck int companyId, int adminId, String sort, String direction, int page, int rownums) {
		if(!SORTABLE_FIELDS.contains(sort)) {
			sort = "shortname";
		}

		final String selectQuery = "SELECT m.mailinglist_id, m.shortname, m.description, m.creation_date, m.change_date FROM mailinglist_tbl m " +
				"   WHERE m.company_id = ? AND m.deleted = 0";
		
		final boolean sortAscending = AgnUtils.sortingDirectionToBoolean(direction, true);

		if(StringUtils.endsWith(sort, "_date")) {
			final String sortClause = "ORDER BY " + formatDateClause("m." + sort) + " " + (sortAscending ? "ASC" : "DESC");

			return selectPaginatedListWithSortClause(logger, selectQuery, sortClause, sort, sortAscending, page, rownums, MAILING_LIST_ENTRY_ROW_MAPPER, companyId);
		} else {
			return selectPaginatedList(logger, selectQuery, "mailinglist_tbl", sort, sortAscending, page, rownums, MAILING_LIST_ENTRY_ROW_MAPPER, companyId);
		}
	}

	public String formatDateClause(String sortField) {
		if(isOracleDB()) {
			return "to_char(" + sortField + ", 'YYYYMMDD')";
		} else {
			return "DATE_FORMAT(" + sortField + ", '%Y%m%d')";
		}
	}

	/**
	 * deletes the bindings for this mailinglist (invocated before the mailinglist is deleted to avoid orphaned mailinglist bindings)
	 * 
	 * @return return code
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteBindings(int id, @VelocityCheck int companyId) {
		try {
			return update(logger, "DELETE FROM customer_" + companyId + "_binding_tbl WHERE mailinglist_id = ?", id) > 0;
		} catch (Exception e) {
			// logging was already done
			return false;
		}
	}

	@Override
	public int getNumberOfActiveSubscribers(boolean admin, boolean test, boolean world, int targetId, @VelocityCheck int companyId, int id) {
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
				targetId = 0;
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
	public boolean mailinglistExists(String mailinglistName, @VelocityCheck int companyId) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailinglist_tbl WHERE deleted=0 AND company_id = ? AND shortname = ?", companyId, mailinglistName) > 0;
	}

	@Override
	public boolean exist(int mailinglistId, @VelocityCheck int companyId) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailinglist_tbl WHERE deleted=0 AND company_id = ? AND mailinglist_id = ?", companyId, mailinglistId) > 0;
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
                "UPDATE mailinglist_tbl SET shortname = ?, description = ?, change_date = ? WHERE mailinglist_id = ? AND deleted=0 AND company_id = ?",
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
			mailinglistEntry.setCreationDate(resultSet.getDate("creation_date"));
			mailinglistEntry.setChangeDate(resultSet.getDate("change_date"));

			return mailinglistEntry;
		}
	}
}
