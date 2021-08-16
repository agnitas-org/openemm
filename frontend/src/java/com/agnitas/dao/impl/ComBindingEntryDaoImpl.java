/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.Mailinglist;
import org.agnitas.beans.impl.BindingEntryImpl;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.dao.impl.mapper.MailinglistRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.beans.ComTarget;
import com.agnitas.dao.ComBindingEntryDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.report.bean.CompositeBindingEntry;
import com.agnitas.emm.core.report.bean.PlainBindingEntry;
import com.agnitas.emm.core.report.bean.impl.CompositeBindingEntryImpl;
import com.agnitas.emm.core.report.bean.impl.PlainBindingEntryImpl;

public class ComBindingEntryDaoImpl extends BaseDaoImpl implements ComBindingEntryDao {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComBindingEntryDaoImpl.class);
	
	private ComRecipientDao recipientDao;
	private ConfigService configService;
	
	@Required
	public final void setRecipientDao(final ComRecipientDao dao) {
		this.recipientDao = Objects.requireNonNull(dao, "Recipient DAO is null");
	}
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}

	@Override
	public boolean getExistingRecipientIDByMailinglistID(Set<Integer> mailinglistIds, @VelocityCheck int companyId) {
		String sql = "SELECT COUNT(customer_id) FROM customer_" + companyId + "_binding_tbl WHERE mailinglist_id IN (" + StringUtils.join(mailinglistIds, ", ") + ")";
		return selectInt(logger, sql) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteRecipientBindingsByMailinglistID(Set<Integer> mailinglistIds, @VelocityCheck int companyId) {
        if (mailinglistIds == null || mailinglistIds.isEmpty()) {
            return;
        }

		String sql = "DELETE FROM customer_" + companyId + "_binding_tbl WHERE mailinglist_id IN (" + StringUtils.join(mailinglistIds, ", ") + ")";
		update(logger, sql);
	}
	
	@Override
	public BindingEntry get(int recipientID, @VelocityCheck int companyID, int mailinglistID, int mediaType) {
		try {
			// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
			String sql = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
			List<BindingEntry> list = select(logger, sql, new BindingEntry_RowMapper(this), recipientID, mailinglistID, mediaType);
			if (list.size() > 0) {
				return list.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<PlainBindingEntry> get(@VelocityCheck int companyId, int recipientId, int mailingId) {
		try {
			String selectMailingListId = "SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?";
			String query = String.format("SELECT * FROM %s", getBindingTableName(companyId)) +
					" WHERE customer_id = ?" +
					String.format(" AND mailinglist_id = (%s)", selectMailingListId);
			return select(logger, query, new PlainBindingEntryRowMapper(), recipientId, mailingId);
		} catch (Exception e) {
			return null;
		}
	}


	@Override
	public void save(@VelocityCheck int companyID, BindingEntry entry) {
		if (companyID <= 0) {
			return;
		} else if (entry.getMailinglistID() <= 0) {
			return;
		}
		
		String existsSql = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
		List<BindingEntry> list = select(logger, existsSql, new BindingEntry_RowMapper(this), entry.getCustomerID(), entry.getMailinglistID(), entry.getMediaType());
		if (list.size() > 0) {
			updateBinding(entry, companyID);
		} else {
			insertNewBinding(entry, companyID);
		}
	}

	/**
	 * Updates this Binding in the Database
	 * 
	 * @return True: Sucess False: Failure
	 * @param companyID
	 *            The company ID of the Binding
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public boolean updateBinding(BindingEntry entry, @VelocityCheck int companyID) {
		try {
			if (companyID <= 0) {
				return false;
			} else if (entry.getMailinglistID() <= 0) {
				return false;
			}
			
			// Check for valid UserStatus code
			UserStatus.getUserStatusByID(entry.getUserStatus());
			
			int touchedLines;
			
			List<String> bindingColumns = DbUtilities.getColumnNames(getDataSource(), "customer_" + companyID + "_binding_tbl");
			
			entry.setChangeDate(new Date());
			
			String sql = "UPDATE customer_" + companyID + "_binding_tbl SET user_status = ?, user_remark = ?, exit_mailing_id = ?, user_type = ?, timestamp = ?";
			List<Object> sqlParameters = new ArrayList<>();
			sqlParameters.add(entry.getUserStatus());
			sqlParameters.add(entry.getUserRemark());
			sqlParameters.add(entry.getExitMailingID());
			sqlParameters.add(entry.getUserType());
			sqlParameters.add(entry.getChangeDate());
			if (bindingColumns.contains("referrer")) {
				sql += ", referrer = ?";
				sqlParameters.add(entry.getReferrer());
			}
			if (bindingColumns.contains("entry_mailing_id")) {
				sql += ", entry_mailing_id = ?";
				sqlParameters.add(entry.getEntryMailingID());
			}
			sql += " WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
			sqlParameters.add(entry.getCustomerID());
			sqlParameters.add(entry.getMailinglistID());
			sqlParameters.add(entry.getMediaType());
			
			touchedLines = update(logger, sql, sqlParameters.toArray());

			return touchedLines >= 1;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean insertNewBinding(BindingEntry entry, @VelocityCheck int companyID) {
		try {
			if (companyID <= 0) {
				return false;
			} else if (entry.getCustomerID() <= 0) {
				return false;
			} else if (entry.getMailinglistID() <= 0) {
				return false;
			} else if (!mailinglistExists(companyID, entry.getMailinglistID())) {
				return false;
			} else {
				if (checkAssignedProfileFieldIsSet(entry, companyID)) {
					// Check for valid UserStatus code
					UserStatus.getUserStatusByID(entry.getUserStatus());
					
					List<String> bindingColumns = DbUtilities.getColumnNames(getDataSource(), "customer_" + companyID + "_binding_tbl");
					
					entry.setCreationDate(new Date());
					entry.setChangeDate(entry.getCreationDate());
					
					String sqlInsertPart = "mailinglist_id, customer_id, user_type, user_status, timestamp, user_remark, creation_date, exit_mailing_id, mediatype";
					String sqlValuePart = "?, ?, ?, ?, ?, ?, ?, ?, ?";
					List<Object> sqlParameters = new ArrayList<>();
					sqlParameters.add(entry.getMailinglistID());
					sqlParameters.add(entry.getCustomerID());
					sqlParameters.add(entry.getUserType());
					sqlParameters.add(entry.getUserStatus());
					sqlParameters.add(entry.getChangeDate());
					sqlParameters.add(entry.getUserRemark());
					sqlParameters.add(entry.getCreationDate());
					sqlParameters.add(entry.getExitMailingID());
					sqlParameters.add(entry.getMediaType());
					
					if (bindingColumns.contains("referrer")) {
						sqlInsertPart += ", referrer";
						sqlValuePart += ", ?";
						sqlParameters.add(entry.getReferrer());
					}
					if (bindingColumns.contains("entry_mailing_id")) {
						sqlInsertPart += ", entry_mailing_id";
						sqlValuePart += ", ?";
						sqlParameters.add(entry.getEntryMailingID());
					}
					String sql = "INSERT INTO customer_" + companyID + "_binding_tbl (" + sqlInsertPart + ") VALUES (" + sqlValuePart + ")";
					
					update(logger, sql, sqlParameters.toArray());
					return true;
				} else {
					return false;
				}
			}
		} catch (Exception e) {
			return false;
		}
	}

	private boolean mailinglistExists(@VelocityCheck int companyID, int mailinglistID) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailinglist_tbl WHERE company_id = ? AND mailinglist_id = ?", companyID, mailinglistID) > 0;
	}

	private final boolean checkAssignedProfileFieldIsSet(final BindingEntry entry, final int companyID) {
		final MediaTypes mediaType = MediaTypes.getMediaTypeForCode(entry.getMediaType());
		
		if(mediaType == null) {
			return true;
		} else {
			if(mediaType.getAssignedProfileField() == null) {
				return true;
			} else {
				final Map<String, Object> map = this.recipientDao.getCustomerDataFromDb(companyID, entry.getCustomerID());
				
				final Object value = map.get(mediaType.getAssignedProfileField());
				
				return value != null && StringUtils.isNotEmpty(value.toString());
			}
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public boolean updateStatus(BindingEntry entry, @VelocityCheck int companyID) {
		try {
			if (companyID <= 0) {
				return false;
			}
			
			// Check for valid UserStatus code
			UserStatus.getUserStatusByID(entry.getUserStatus());
			
			List<String> bindingColumns = DbUtilities.getColumnNames(getDataSource(), "customer_" + companyID + "_binding_tbl");
			
			String sql = "UPDATE customer_" + companyID + "_binding_tbl SET user_status = ?, exit_mailing_id = ?, user_remark = ?, timestamp = CURRENT_TIMESTAMP";
			List<Object> sqlParameters = new ArrayList<>();
			sqlParameters.add(entry.getUserStatus());
			sqlParameters.add(entry.getExitMailingID());
			sqlParameters.add(entry.getUserRemark());
			if (bindingColumns.contains("referrer")) {
				sql += ", referrer = ?";
				sqlParameters.add(entry.getReferrer());
			}
			if (bindingColumns.contains("entry_mailing_id")) {
				sql += ", entry_mailing_id = ?";
				sqlParameters.add(entry.getEntryMailingID());
			}
			sql += " WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
			sqlParameters.add(entry.getCustomerID());
			sqlParameters.add(entry.getMailinglistID());
			sqlParameters.add(entry.getMediaType());
			
			int touchedLines = update(logger, sql, sqlParameters.toArray());
			
			return touchedLines >= 1;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean optOutEmailAdr(String email, @VelocityCheck int companyID) {
		String operator;
		if (companyID <= 0) {
			return false;
		}
		if (email.contains("%") || email.contains("_")) {
			operator = "LIKE";
		} else {
			operator = "=";
		}

		try {
			String sql = "UPDATE customer_" + companyID + "_binding_tbl SET user_status = ? WHERE customer_id IN (SELECT customer_id FROM customer_" + companyID + "_tbl WHERE email " + operator + " ?)";
			int touchedLines = update(logger, sql, UserStatus.AdminOut.getStatusCode(), email);
			return touchedLines >= 1;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean addTargetsToMailinglist(@VelocityCheck int companyID, int mailinglistID, ComTarget target, Set<MediaTypes> mediaTypes) {
		try {
			if (companyID <= 0) {
				return false;
			}
			if (mediaTypes == null || mediaTypes.size() == 0) {
				mediaTypes = new HashSet<>(Arrays.asList(MediaTypes.EMAIL));
			}
			for (MediaTypes mediaType : mediaTypes) {
				String sql = "INSERT INTO customer_" + companyID + "_binding_tbl (customer_id, mailinglist_id, user_type, user_status, user_remark, timestamp, exit_mailing_id, creation_date, mediatype)"
						+ " (SELECT cust.customer_id, " + mailinglistID + ", '" + UserType.World.getTypeCode() + "', 1, " + "'From Target " + target.getId() + "', CURRENT_TIMESTAMP, 0, CURRENT_TIMESTAMP, ? FROM customer_" + companyID + "_tbl cust WHERE " + target.getTargetSQL() + ")";
				update(logger, sql, mediaType.getMediaCode());
			}
			return true;
		} catch (Exception e3) {
			return false;
		}
	}

	@Override
	public boolean getUserBindingFromDB(BindingEntry entry, @VelocityCheck int companyID) {
		String sqlGetBinding = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE mailinglist_id = ? AND customer_id = ? AND mediatype = ?";
		List<BindingEntry> list = select(logger, sqlGetBinding, new BindingEntry_RowMapper(this), entry.getMailinglistID(), entry.getCustomerID(), entry.getMediaType());
		if (list.size() > 0) {
			BindingEntry foundEntry = list.get(0);
			entry.setUserType(foundEntry.getUserType());
            entry.setUserStatus(foundEntry.getUserStatus());
            entry.setUserRemark(foundEntry.getUserRemark());
            entry.setReferrer(foundEntry.getReferrer());
            entry.setChangeDate(foundEntry.getChangeDate());
            entry.setExitMailingID(foundEntry.getExitMailingID());
            entry.setCreationDate(foundEntry.getCreationDate());
			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean exist(int customerId, @VelocityCheck int companyId, int mailinglistId, int mediatype) {
		String sql = "SELECT COUNT(*) FROM customer_" + companyId + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
		return selectInt(logger, sql, customerId, mailinglistId, mediatype) > 0;
	}

	@Override
	public boolean exist(@VelocityCheck int companyId, int mailinglistId) {
		String sql = "SELECT COUNT(*) FROM customer_" + companyId + "_binding_tbl WHERE mailinglist_id = ?";
		return selectInt(logger, sql, mailinglistId) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void delete(int customerId, @VelocityCheck int companyId, int mailinglistId, int mediatype) {
		String sql = "DELETE FROM customer_" + companyId + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
		update(logger, sql, customerId, mailinglistId, mediatype);
	}

	@Override
	public List<BindingEntry> getBindings(@VelocityCheck int companyID, int recipientID) {

		// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
		String sql = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE customer_id = ?";
		return select(logger, sql, new BindingEntry_RowMapper(this), recipientID);
	}

	@Override
	public List<CompositeBindingEntry> getCompositeBindings(@VelocityCheck int companyID, int recipientID) {
		String bindingTable = "customer_" + companyID + "_binding_tbl";
		String recipientTable = "customer_" + companyID + "_tbl";
		String mailinglistTable = "mailinglist_tbl";

		StringBuilder compositeBindingsQuery = new StringBuilder("SELECT bin.*,");
		compositeBindingsQuery.append(" ml.mailinglist_id AS ml_mailinglist_id, ml.company_id AS ml_company_id,");
		compositeBindingsQuery.append(" ml.shortname AS ml_shortname, ml.description AS ml_description,");
		compositeBindingsQuery.append(" ml.change_date AS ml_change_date, ml.creation_date AS ml_creation_date,");
		compositeBindingsQuery.append(" ml.deleted AS ml_deleted");
		compositeBindingsQuery.append(" FROM ").append(bindingTable).append(" bin");
		compositeBindingsQuery.append(" INNER JOIN ").append(recipientTable).append(" rec");
		compositeBindingsQuery.append(" ON bin.customer_id = rec.customer_id");
		compositeBindingsQuery.append(" LEFT JOIN ").append(mailinglistTable).append(" ml");
		compositeBindingsQuery.append(" ON bin.mailinglist_id = ml.mailinglist_id");
		compositeBindingsQuery.append(" WHERE bin.customer_id = ?");

		CompositeBindingEntryRowMapperWithMailinglist compositeBindingEntryRowMapper =
				new CompositeBindingEntryRowMapperWithMailinglist("ml_");

		return select(logger, compositeBindingsQuery.toString(), compositeBindingEntryRowMapper, recipientID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateBindingStatusByEmailPattern(@VelocityCheck int companyId, String emailPattern, int userStatus, String remark) throws Exception {
		// Check for valid UserStatus code
		UserStatus.getUserStatusByID(userStatus);
		
		final boolean useNewWildcards = this.configService.getBooleanValue(ConfigValue.Development.UseNewBlacklistWildcards, companyId);
		
		if(useNewWildcards) {
			final String escapeClause = isOracleDB() ? " ESCAPE '\\'" : "";
			
			final String customerIdByPatternSubselect = String.format(
					"SELECT customer_id FROM customer_%d_tbl WHERE email LIKE REPLACE(REPLACE(?, '_', '\\_'), '*', '%%') %s",
					companyId,
					escapeClause);
			
			final String sql = String.format("UPDATE customer_%d_binding_tbl SET user_status=?, user_remark=?, timestamp=CURRENT_TIMESTAMP WHERE customer_id IN (%s)",
					companyId,
					customerIdByPatternSubselect);
			
			update(logger, sql, userStatus, remark, emailPattern);
		} else {
			String sql = "UPDATE customer_" + companyId + "_binding_tbl " + "SET user_status = ?, user_remark = ?, timestamp = CURRENT_TIMESTAMP WHERE customer_id IN (SELECT customer_id FROM customer_" + companyId + "_tbl WHERE email LIKE ?)";
			update(logger, sql, userStatus, remark, emailPattern);
		}
	}
	
	@Override
	public void lockBindings(int companyId, List<SimpleEntry<Integer, Integer>> cmPairs) {
		if (CollectionUtils.isNotEmpty(cmPairs)) {
			String customerAndMailinglistInClause = cmPairs.stream()
					.map(pair -> "("  + pair.getKey() + "," + pair.getValue() + ")")
					.collect(Collectors.joining(","));

			String query = "SELECT * FROM customer_" + companyId + "_binding_tbl WHERE (customer_id, mailinglist_id) " +
					"IN (" + customerAndMailinglistInClause + ") FOR UPDATE";

			select(logger, query);
		}
	}

	private String getBindingTableName(int companyId) {
		return String.format("customer_%d_binding_tbl", companyId);
	}

	private class PlainBindingEntryRowMapper implements RowMapper<PlainBindingEntry> {
		@Override
		public PlainBindingEntry mapRow(ResultSet resultSet, int i) throws SQLException {
			PlainBindingEntry plainBindingEntry = new PlainBindingEntryImpl();

			plainBindingEntry.setCustomerId(resultSet.getInt("customer_id"));
			plainBindingEntry.setMailingListId(resultSet.getInt("mailinglist_id"));
			plainBindingEntry.setMediaType(resultSet.getInt("mediatype"));
			plainBindingEntry.setUserType(resultSet.getString("user_type"));
			plainBindingEntry.setUserStatus(resultSet.getInt("user_status"));
			plainBindingEntry.setTimestamp(resultSet.getTimestamp("timestamp"));
			plainBindingEntry.setExitMailingId(resultSet.getInt("exit_mailing_id"));
			
			if (DbUtilities.resultsetHasColumn(resultSet, "entry_mailing_id")) {
				plainBindingEntry.setEntryMailingId(resultSet.getInt("entry_mailing_id"));
				if (resultSet.wasNull()) {
					plainBindingEntry.setEntryMailingId(0);
				}
			} else {
				plainBindingEntry.setEntryMailingId(0);
			}
			
			plainBindingEntry.setUserRemark(resultSet.getString("user_remark"));
			plainBindingEntry.setCreationDate(resultSet.getTimestamp("creation_date"));

			return plainBindingEntry;
		}
	}

	protected class BindingEntry_RowMapper implements RowMapper<BindingEntry> {
		private ComBindingEntryDao bindingEntryDao;
		
		public BindingEntry_RowMapper(ComBindingEntryDao bindingEntryDao) {
			this.bindingEntryDao = bindingEntryDao;
		}
		
		@Override
		public BindingEntry mapRow(ResultSet resultSet, int row) throws SQLException {
			BindingEntry readEntry = new BindingEntryImpl();
			readEntry.setBindingEntryDao(bindingEntryDao);
			
			readEntry.setCustomerID(resultSet.getInt("customer_id"));
			readEntry.setMailinglistID(resultSet.getInt("mailinglist_id"));
			readEntry.setMediaType(resultSet.getInt("mediatype"));
			readEntry.setUserType(resultSet.getString("user_type"));
			readEntry.setUserStatus(resultSet.getInt("user_status"));
			readEntry.setChangeDate(resultSet.getTimestamp("timestamp"));
			readEntry.setExitMailingID(resultSet.getInt("exit_mailing_id"));
			if (resultSet.wasNull()) {
				readEntry.setExitMailingID(0);
			}
			
			if (DbUtilities.resultsetHasColumn(resultSet, "entry_mailing_id")) {
				readEntry.setEntryMailingID(resultSet.getInt("entry_mailing_id"));
				if (resultSet.wasNull()) {
					readEntry.setEntryMailingID(0);
				}
			} else {
				readEntry.setEntryMailingID(0);
			}
			
			readEntry.setUserRemark(resultSet.getString("user_remark"));
			if (DbUtilities.resultsetHasColumn(resultSet, "referrer")) {
				readEntry.setReferrer(resultSet.getString("referrer"));
			}
			readEntry.setCreationDate(resultSet.getTimestamp("creation_date"));

			return readEntry;
		}
	}

	public static class CompositeBindingEntryRowMapperWithMailinglist implements RowMapper<CompositeBindingEntry> {

		private static final String DEFAULT_MAILING_LIST_PREFIX = "ml_";

		private final String columnPrefix;
		private final MailinglistRowMapper mailinglistRowMapper;

		public CompositeBindingEntryRowMapperWithMailinglist(){
			columnPrefix = StringUtils.EMPTY;
			mailinglistRowMapper = new MailinglistRowMapper(DEFAULT_MAILING_LIST_PREFIX);
		}

		public CompositeBindingEntryRowMapperWithMailinglist(String mailinglistColumnPrefix) {
			columnPrefix = StringUtils.EMPTY;
			mailinglistRowMapper = new MailinglistRowMapper(mailinglistColumnPrefix);
		}

		@Override
		public CompositeBindingEntry mapRow(ResultSet resultSet, int rowNum) throws SQLException {
			CompositeBindingEntry compositeBindingEntry = new CompositeBindingEntryImpl();

			// mailinglist mapping
			Mailinglist mailinglist = mailinglistRowMapper.mapRow(resultSet, rowNum);
			compositeBindingEntry.setMailingList(mailinglist.getId() > 0 ? mailinglist : null);

			// recipient mapping. This RowMapper adds just Mailinglist entity.
			compositeBindingEntry.setRecipient(null);

			// bindingEntry mapping
			compositeBindingEntry.setCustomerId(resultSet.getInt(columnPrefix + "customer_id"));
			compositeBindingEntry.setMailingListId(resultSet.getInt(columnPrefix + "mailinglist_id"));
			compositeBindingEntry.setMediaType(resultSet.getInt(columnPrefix + "mediatype"));
			compositeBindingEntry.setUserType(resultSet.getString(columnPrefix + "user_type"));
			compositeBindingEntry.setUserStatus(resultSet.getInt(columnPrefix + "user_status"));
			compositeBindingEntry.setTimestamp(resultSet.getTimestamp(columnPrefix + "timestamp"));
			compositeBindingEntry.setExitMailingId(resultSet.getInt(columnPrefix + "exit_mailing_id"));
			compositeBindingEntry.setUserRemark(resultSet.getString(columnPrefix + "user_remark"));
			compositeBindingEntry.setCreationDate(resultSet.getTimestamp(columnPrefix + "creation_date"));

			return compositeBindingEntry;
		}
	}
}
