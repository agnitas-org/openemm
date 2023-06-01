/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

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
import java.util.Collections;
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
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.MailinglistRowMapper;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	private static final Logger logger = LogManager.getLogger(ComBindingEntryDaoImpl.class);
	
	private ComRecipientDao recipientDao;
	
	@Required
	public final void setRecipientDao(final ComRecipientDao dao) {
		this.recipientDao = Objects.requireNonNull(dao, "Recipient DAO is null");
	}

	@Override
	public boolean getExistingRecipientIDByMailinglistID(Set<Integer> mailinglistIds, int companyId) {
		String sql = "SELECT COUNT(customer_id) FROM customer_" + companyId + "_binding_tbl WHERE mailinglist_id IN (" + StringUtils.join(mailinglistIds, ", ") + ")";
		return selectInt(logger, sql) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteRecipientBindingsByMailinglistID(Set<Integer> mailinglistIds, int companyId) {
        if (mailinglistIds == null || mailinglistIds.isEmpty()) {
            return;
        }

		String sql = "DELETE FROM customer_" + companyId + "_binding_tbl WHERE mailinglist_id IN (" + StringUtils.join(mailinglistIds, ", ") + ")";
		update(logger, sql);
	}
	
	@Override
	public BindingEntry get(int recipientID, int companyID, int mailinglistID, int mediaType) {
		try {
			// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
			String sql = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
			List<BindingEntry> list = select(logger, sql, new BindingEntry_RowMapper(this), recipientID, mailinglistID, mediaType);
			if (!list.isEmpty()) {
				return list.get(0);
			}

			return null;
		} catch (Exception e) {
			return null;
		}
	}

	@Override
	public List<PlainBindingEntry> get(int companyId, int recipientId, int mailingId) {
		try {
			String selectMailingListId = "SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?";
			// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
			String query = String.format("SELECT * FROM %s", getBindingTableName(companyId)) +
					" WHERE customer_id = ?" +
					String.format(" AND mailinglist_id = (%s)", selectMailingListId);
			return select(logger, query, new PlainBindingEntryRowMapper(), recipientId, mailingId);
		} catch (Exception e) {
			return null;
		}
	}


	@Override
	public void save(int companyID, BindingEntry entry) {
		if (companyID <= 0 || entry.getMailinglistID() <= 0) {
			return;
		}

		// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
		String existsSql = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
		List<BindingEntry> list = select(logger, existsSql, new BindingEntry_RowMapper(this), entry.getCustomerID(), entry.getMailinglistID(), entry.getMediaType());
		if (!list.isEmpty()) {
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
	public boolean updateBinding(BindingEntry entry, int companyID) {
		try {
			if (companyID <= 0 || entry.getMailinglistID() <= 0) {
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
    public void updateBindings(int companyId, List<BindingEntry> bindings) throws Exception {
		List<String> bindingColumns = DbUtilities.getColumnNames(getDataSource(), "customer_" + companyId + "_binding_tbl");

		boolean containsReferrerColumn = bindingColumns.contains("referrer");
		boolean containsEntryMailingIdColumn = bindingColumns.contains("entry_mailing_id");

        String query = "UPDATE customer_" + companyId + "_binding_tbl" +
                " SET user_status = ?, user_remark = ?, exit_mailing_id = ?, user_type = ?, timestamp = CURRENT_TIMESTAMP" +
				(containsReferrerColumn ? ", referrer = ?" : "") +
				(containsEntryMailingIdColumn ? ", entry_mailing_id = ?" : "") +
                " WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";

		List<Object[]> params = new ArrayList<>();
		for (BindingEntry binding : bindings) {
			List<Object> objects = new ArrayList<>();
			objects.add(binding.getUserStatus());
			objects.add(binding.getUserRemark());
			objects.add(binding.getExitMailingID());
			objects.add(binding.getUserType());

			if (containsReferrerColumn) {
				objects.add(binding.getReferrer());
			}
			if (containsEntryMailingIdColumn) {
				objects.add(binding.getEntryMailingID());
			}

			objects.add(binding.getCustomerID());
			objects.add(binding.getMailinglistID());
			objects.add(binding.getMediaType());
			params.add(objects.toArray());
		}

		batchupdate(logger, query, params);
    }

    @Override
	@DaoUpdateReturnValueCheck
    public void insertBindings(int companyId, List<BindingEntry> bindings) throws Exception {
		if (companyId > 0) {
			List<String> bindingColumns = DbUtilities.getColumnNames(getDataSource(), "customer_" + companyId + "_binding_tbl");

			boolean containsReferrerColumn = bindingColumns.contains("referrer");
			boolean containsEntryMailingIdColumn = bindingColumns.contains("entry_mailing_id");

			List<String> columns = new ArrayList<>(Arrays.asList(
					"mailinglist_id",
					"customer_id",
					"user_type",
					"user_status",
					"user_remark",
					"exit_mailing_id",
					"mediatype"));
			if (containsReferrerColumn) {
				columns.add("referrer");
			}
			if (containsEntryMailingIdColumn) {
				columns.add("entry_mailing_id");
			}
			String query = "INSERT INTO customer_" + companyId + "_binding_tbl(" + StringUtils.join(columns, ", ") + ", creation_date, timestamp) " +
					"VALUES (" + StringUtils.repeat("?, ", columns.size()) + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

			List<Object[]> params = new ArrayList<>();
			for (BindingEntry binding : bindings) {
				if (checkAssignedProfileFieldIsSet(binding, companyId)) {

					List<Object> objects = new ArrayList<>();
					objects.add(binding.getMailinglistID());
					objects.add(binding.getCustomerID());
					objects.add(binding.getUserType());
					objects.add(binding.getUserStatus());
					objects.add(binding.getUserRemark());
					objects.add(binding.getExitMailingID());
					objects.add(binding.getMediaType());

					if (containsReferrerColumn) {
						objects.add(binding.getReferrer());
					}
					if (containsEntryMailingIdColumn) {
						objects.add(binding.getEntryMailingID());
					}

					params.add(objects.toArray());
				}
			}

			batchupdate(logger, query, params);
		}
    }

	@Override
	@DaoUpdateReturnValueCheck
	public boolean insertNewBinding(BindingEntry entry, int companyID) {
		try {
			if (companyID <= 0 || entry.getCustomerID() <= 0 || entry.getMailinglistID() <= 0 || !mailinglistExists(companyID, entry.getMailinglistID())) {
				return false;
			}

			if (!checkAssignedProfileFieldIsSet(entry, companyID)) {
				return false;
			}

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
		} catch (Exception e) {
			return false;
		}
	}

	private boolean mailinglistExists(int companyID, int mailinglistID) {
		return selectInt(logger, "SELECT COUNT(*) FROM mailinglist_tbl WHERE company_id = ? AND mailinglist_id = ?", companyID, mailinglistID) > 0;
	}

	private boolean checkAssignedProfileFieldIsSet(final BindingEntry entry, final int companyID) {
		final MediaTypes mediaType = MediaTypes.getMediaTypeForCode(entry.getMediaType());
		
		if (mediaType == null || mediaType.getAssignedProfileField() == null) {
			return true;
		}

		final Map<String, Object> map = this.recipientDao.getCustomerDataFromDb(companyID, entry.getCustomerID());
		final Object value = map.get(mediaType.getAssignedProfileField());

		validateAssignedProfileField(mediaType, map, value);

		return value != null && StringUtils.isNotEmpty(value.toString());
	}

	protected void validateAssignedProfileField(MediaTypes mediaType, Map<String, Object> customerData, Object fieldValue) {
		// Override in extended version of this class
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean updateStatus(BindingEntry entry, int companyID) {
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
	public boolean optOutEmailAdr(String email, int companyID) {
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
	public boolean addTargetsToMailinglist(int companyID, int mailinglistID, ComTarget target, Set<MediaTypes> mediaTypes) {
		try {
			if (companyID <= 0) {
				return false;
			}
			if (mediaTypes == null || mediaTypes.isEmpty()) {
				mediaTypes = new HashSet<>(Collections.singletonList(MediaTypes.EMAIL));
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
	public boolean getUserBindingFromDB(BindingEntry entry, int companyID) {
		// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
		String sqlGetBinding = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE mailinglist_id = ? AND customer_id = ? AND mediatype = ?";
		List<BindingEntry> list = select(logger, sqlGetBinding, new BindingEntry_RowMapper(this), entry.getMailinglistID(), entry.getCustomerID(), entry.getMediaType());
		if (!list.isEmpty()) {
			BindingEntry foundEntry = list.get(0);
			entry.setUserType(foundEntry.getUserType());
            entry.setUserStatus(foundEntry.getUserStatus());
            entry.setUserRemark(foundEntry.getUserRemark());
            entry.setReferrer(foundEntry.getReferrer());
            entry.setChangeDate(foundEntry.getChangeDate());
            entry.setExitMailingID(foundEntry.getExitMailingID());
            entry.setCreationDate(foundEntry.getCreationDate());
			return true;
		}

		return false;
	}

	@Override
	public boolean exist(int customerId, int companyId, int mailinglistId, int mediatype) {
		String sql = "SELECT COUNT(*) FROM customer_" + companyId + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
		return selectInt(logger, sql, customerId, mailinglistId, mediatype) > 0;
	}

	@Override
	public boolean exist(int companyId, int mailinglistId) {
		String sql = "SELECT COUNT(*) FROM customer_" + companyId + "_binding_tbl WHERE mailinglist_id = ?";
		return selectInt(logger, sql, mailinglistId) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void delete(int customerId, int companyId, int mailinglistId, int mediatype) {
		String sql = "DELETE FROM customer_" + companyId + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
		update(logger, sql, customerId, mailinglistId, mediatype);
	}

	@Override
	public List<BindingEntry> getBindings(int companyID, int recipientID) {

		// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
		String sql = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE customer_id = ?";
		return select(logger, sql, new BindingEntry_RowMapper(this), recipientID);
	}

	@Override
	public List<CompositeBindingEntry> getCompositeBindings(int companyID, int recipientID) {
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
	public void updateBindingStatusByEmailPattern(int companyId, String emailPattern, int userStatus, String remark) throws Exception {
		// Check for valid UserStatus code
		UserStatus.getUserStatusByID(userStatus);
		
		final String escapeClause = isOracleDB() ? " ESCAPE '\\'" : "";
		
		final String customerIdByPatternSubselect = String.format(
				"SELECT customer_id FROM customer_%d_tbl WHERE email LIKE REPLACE(REPLACE(?, '_', '\\_'), '*', '%%') %s",
				companyId,
				escapeClause);
		
		final String sql = String.format("UPDATE customer_%d_binding_tbl SET user_status=?, user_remark=?, timestamp=CURRENT_TIMESTAMP WHERE customer_id IN (%s)",
				companyId,
				customerIdByPatternSubselect);
		
		update(logger, sql, userStatus, remark, emailPattern);
	}
	
	@Override
	public void lockBindings(int companyId, List<SimpleEntry<Integer, Integer>> cmPairs) {
		if (CollectionUtils.isNotEmpty(cmPairs)) {
			String customerAndMailinglistInClause = cmPairs.stream()
					.map(pair -> "("  + pair.getKey() + "," + pair.getValue() + ")")
					.collect(Collectors.joining(","));
			// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
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

	@Override
	public int bulkUpdateStatus(int companyID, List<Integer> mailinglistIds, MediaTypes mediatype, UserStatus userStatus, String userRemark, List<Integer> customerIDs) {
		String query = "UPDATE customer_" + companyID + "_binding_tbl"
			+ " SET user_status = ?, user_remark = ?, timestamp = CURRENT_TIMESTAMP"
			+ " WHERE " + makeBulkInClauseForInteger("mailinglist_id", mailinglistIds) + " AND " + makeBulkInClauseForInteger("customer_id", customerIDs);
		if (mediatype != null) {
			return update(logger, query + " AND mediatype = ?", userStatus.getStatusCode(), userRemark, mediatype.getKey());
		} else {
			return update(logger, query, userStatus.getStatusCode(), userRemark);
		}
	}

	@Override
	public int bulkDelete(int companyID, List<Integer> mailinglistIds, MediaTypes mediatype, List<Integer> customerIDs) {
		String query = "DELETE FROM customer_" + companyID + "_binding_tbl"
			+ " WHERE " + makeBulkInClauseForInteger("mailinglist_id", mailinglistIds) + " AND " + makeBulkInClauseForInteger("customer_id", customerIDs);
		if (mediatype != null) {
			return update(logger, query + " AND mediatype = ?", mediatype.getKey());
		} else {
			return update(logger, query);
		}
	}

	@Override
	public int bulkCreate(int companyID, List<Integer> mailinglistIds, MediaTypes mediatype, UserStatus userStatus, String userRemark, List<Integer> customerIDs) {
		Date now = new Date();
		String query = "INSERT INTO customer_" + companyID + "_binding_tbl (customer_id, mailinglist_id, mediatype, user_type, user_status, user_remark, creation_date, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		List<Object[]> parameterList = new ArrayList<>();
		for (int mailinglistId : mailinglistIds) {
			List<Integer> existingCustomerIDs = select(logger, "SELECT customer_id FROM customer_" + companyID + "_binding_tbl WHERE mailinglist_id = ? AND mediatype = ? AND " + makeBulkInClauseForInteger("customer_id", customerIDs), IntegerRowMapper.INSTANCE, mailinglistId, mediatype.getMediaCode());
			for (int customerID : customerIDs) {
				if (!existingCustomerIDs.contains(customerID)) {
					parameterList.add(new Object[] { customerID, mailinglistId, mediatype.getMediaCode(), UserType.World.getTypeCode(), userStatus.getStatusCode(), userRemark, now, now });
				}
			}
		}
		if (parameterList.size() > 0) {
	        int[] touchedLinesResults = batchupdate(logger, query, parameterList);
	        int touchedLinesSum = 0;
	        for (int touchedLines : touchedLinesResults) {
	        	touchedLinesSum += touchedLines;
	        }
	        return touchedLinesSum;
		} else {
			return 0;
		}
	}
}
