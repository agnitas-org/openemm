/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.beans.BindingEntry;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.beans.Mailinglist;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.Target;
import com.agnitas.beans.impl.BindingEntryImpl;
import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.RecipientDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.dao.impl.mapper.MailinglistRowMapper;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.binding.service.event.OnBindingChangedHandler;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.recipientsreport.bean.SummedRecipientStatus;
import com.agnitas.emm.core.report.bean.CompositeBindingEntry;
import com.agnitas.emm.core.report.bean.PlainBindingEntry;
import com.agnitas.emm.core.report.bean.impl.CompositeBindingEntryImpl;
import com.agnitas.emm.core.report.bean.impl.PlainBindingEntryImpl;
import com.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class BindingEntryDaoImpl extends PaginatedBaseDaoImpl implements BindingEntryDao {
	
	private final RecipientDao recipientDao;
	private final TargetDao targetDao;
	
	private List<OnBindingChangedHandler> bindingChangedHandlers;
	
	public BindingEntryDaoImpl(RecipientDao recipientDao, TargetDao targetDao) {
		this.recipientDao = Objects.requireNonNull(recipientDao, "recipientDao");
        this.targetDao = targetDao;
        this.bindingChangedHandlers = List.of();
	}
	
	public void setOnBindingChangedHandlers(List<OnBindingChangedHandler> handlers) {
		this.bindingChangedHandlers = handlers != null ? handlers : List.of();
	}

	@Override
	public BindingEntry get(int recipientID, int companyID, int mailinglistID, int mediaType) {
		try {
			// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
			String sql = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
			List<BindingEntry> list = select(sql, new BindingEntry_RowMapper(this), recipientID, mailinglistID, mediaType);
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
			return select(query, new PlainBindingEntryRowMapper(), recipientId, mailingId);
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
		List<BindingEntry> list = select(existsSql, new BindingEntry_RowMapper(this), entry.getCustomerID(), entry.getMailinglistID(), entry.getMediaType());
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
			return updateBindings(companyID, entry) > 0;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public int updateBindings(int companyId, BindingEntry... bindings) {
		return updateBindings(companyId, Arrays.asList(bindings));
	}

	@Override
	@DaoUpdateReturnValueCheck
    public int updateBindings(int companyId, List<BindingEntry> bindings) {
		final List<String> bindingColumns = DbUtilities.getColumnNames(getDataSource(), "customer_" + companyId + "_binding_tbl");

		final boolean containsReferrerColumn = bindingColumns.contains("referrer");
		final boolean containsEntryMailingIdColumn = bindingColumns.contains("entry_mailing_id");

        final String query = "UPDATE customer_" + companyId + "_binding_tbl" +
                " SET user_status = ?, user_remark = ?, exit_mailing_id = ?, user_type = ?, timestamp = CURRENT_TIMESTAMP" +
				(containsReferrerColumn ? ", referrer = ?" : "") +
				(containsEntryMailingIdColumn ? ", entry_mailing_id = ?" : "") +
                " WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
		
		int updated = 0;
		for(int i = 0; i < bindings.size(); i++) {
			final BindingEntry binding = bindings.get(i);

			if (!UserStatus.existsWithId(binding.getUserStatus())) {
				throw new IllegalArgumentException("Invalid binding user status! - " + binding.getUserStatus());
			}
			
			final List<Object> objects = new ArrayList<>();
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

			final int touched = update(query, objects.toArray());
			
			if(touched > 0) {
				updated++;
				
				fireBindingUpdated(companyId, binding);
			}
		}
		
		return updated;
    }

    @Override
	@DaoUpdateReturnValueCheck
    public int insertBindings(int companyId, List<BindingEntry> bindings) {
		if (companyId > 0) {
			final List<String> bindingColumns = DbUtilities.getColumnNames(getDataSource(), "customer_" + companyId + "_binding_tbl");

			final boolean containsReferrerColumn = bindingColumns.contains("referrer");
			final boolean containsEntryMailingIdColumn = bindingColumns.contains("entry_mailing_id");

			final List<String> columns = new ArrayList<>(Arrays.asList(
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
			final String query = "INSERT INTO customer_" + companyId + "_binding_tbl(" + StringUtils.join(columns, ", ") + ", creation_date, timestamp) VALUES (" + StringUtils.repeat("?, ", columns.size()) + "CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";

			final int[] ids = new int[bindings.size()];
			int updated = 0;

			for(int i = 0; i < bindings.size(); i++) {
				final BindingEntry binding = bindings.get(i);

				if (companyId > 0 && binding.getCustomerID() > 0 && binding.getMailinglistID() > 0 && mailinglistExists(companyId, binding.getMailinglistID()) &&  checkAssignedProfileFieldIsSet(binding, companyId)) {
					final List<Object> objects = new ArrayList<>();
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

					ids[i] = update(query, objects.toArray());
					
					updated++;
					fireBindingCreated(companyId, binding);
				}
			}
			
			return updated;
		} else {
			return 0;
		}
    }

	@Override
	public int insertBindings(int companyId, BindingEntry... bindings) {
		return insertBindings(companyId, Arrays.asList(bindings));
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean insertNewBinding(BindingEntry entry, int companyID) {
		try {
			return insertBindings(companyID, entry) > 0;
		} catch (Exception e) {
			logger.warn(String.format("Error inserting new binding to company %d", companyID), e);
			return false;
		}
	}

	private boolean mailinglistExists(int companyID, int mailinglistID) {
		return selectInt("SELECT COUNT(*) FROM mailinglist_tbl WHERE company_id = ? AND mailinglist_id = ?", companyID, mailinglistID) > 0;
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
		if (companyID <= 0 || !UserStatus.existsWithId(entry.getUserStatus())) {
			return false;
		}

		try {
			final List<String> bindingColumns = DbUtilities.getColumnNames(getDataSource(), "customer_" + companyID + "_binding_tbl");
			
			String sql = "UPDATE customer_" + companyID + "_binding_tbl SET user_status = ?, exit_mailing_id = ?, user_remark = ?, timestamp = CURRENT_TIMESTAMP";
			final List<Object> sqlParameters = new ArrayList<>();
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
			
			final int touchedLines = update(sql, sqlParameters.toArray());
			
			if (touchedLines > 0) {
				fireBindingUpdated(companyID, entry);
			}
			
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
			int touchedLines = update(sql, UserStatus.AdminOut.getStatusCode(), email);
			return touchedLines >= 1;
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean addTargetsToMailinglist(int companyID, int mailinglistID, Target target, Set<MediaTypes> mediaTypes) {
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
				update(sql, mediaType.getMediaCode());
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
		List<BindingEntry> list = select(sqlGetBinding, new BindingEntry_RowMapper(this), entry.getMailinglistID(), entry.getCustomerID(), entry.getMediaType());
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
		return selectInt(sql, customerId, mailinglistId, mediatype) > 0;
	}

	@Override
	public boolean exist(int companyId, int mailinglistId) {
		String sql = "SELECT COUNT(*) FROM customer_" + companyId + "_binding_tbl WHERE mailinglist_id = ?";
		return selectInt(sql, mailinglistId) > 0;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void delete(int customerId, int companyId, int mailinglistId, int mediatype) {
		String sql = "DELETE FROM customer_" + companyId + "_binding_tbl WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?";
		update(sql, customerId, mailinglistId, mediatype);
	}

	@Override
	public List<BindingEntry> getBindings(int companyID, int recipientID) {
		// Using "SELECT * ...", because entry_mailing_id and referrer may be missing in sub-client tables
		String sql = "SELECT * FROM customer_" + companyID + "_binding_tbl WHERE customer_id = ?";
		return select(sql, new BindingEntry_RowMapper(this), recipientID);
	}

	@Override
	public PaginatedList<BindingEntry> getBindings(Integer mailinglistId, int companyID, UserStatus status, String timestamp, int page, int size) {
		StringBuilder query = new StringBuilder("SELECT * FROM %s WHERE 1 = 1".formatted(getBindingTableName(companyID)));
		List<Object> params = new ArrayList<>();

		if (mailinglistId != null) {
			query.append(" AND mailinglist_id = ?");
			params.add(mailinglistId);
		}

		if (status != null) {
			query.append(" AND user_status = ?");
			params.add(status.getStatusCode());
		}

		if (StringUtils.isNotBlank(timestamp)) {
			if (isOracleDB() || isPostgreSQL()) {
				query.append(" AND TO_CHAR(timestamp, 'YYYY-MM-DD') LIKE ?");
			} else {
				query.append(" AND DATE_FORMAT(timestamp, '%Y-%m-%d') LIKE ?");
			}
			params.add(timestamp.replace("*", "%"));
		}

		return selectPaginatedList(query.toString(), page, size, new BindingEntry_RowMapper(this), params.toArray());
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

		return select(compositeBindingsQuery.toString(), compositeBindingEntryRowMapper, recipientID);
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateBindingStatusByEmailPattern(int companyId, String emailPattern, UserStatus userStatus, String remark) {
		final String escapeClause = isOracleDB() ? " ESCAPE '\\'" : "";
		
		final String customerIdByPatternSubselect = String.format(
				"SELECT customer_id FROM customer_%d_tbl WHERE email LIKE REPLACE(REPLACE(?, '_', '\\_'), '*', '%%') %s",
				companyId,
				escapeClause);
		
		final String sql = String.format("UPDATE customer_%d_binding_tbl SET user_status=?, user_remark=?, timestamp=CURRENT_TIMESTAMP WHERE customer_id IN (%s)",
				companyId,
				customerIdByPatternSubselect);
		
		update(sql, userStatus.getStatusCode(), remark, emailPattern);
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

			select(query);
		}
	}

	private String getBindingTableName(int companyId) {
		return String.format("customer_%d_binding_tbl", companyId);
	}

	private static class PlainBindingEntryRowMapper implements RowMapper<PlainBindingEntry> {
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

	protected static class BindingEntry_RowMapper implements RowMapper<BindingEntry> {

		private final BindingEntryDao bindingEntryDao;
		
		public BindingEntry_RowMapper(BindingEntryDao bindingEntryDao) {
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
		/*
		 * Note: batchupdate() cannot be used here.
		 * 
		 * Some JDBC drivers (like Mariadb) do not support to return the number of touched lines. 
		 * This is an information we need here.
		 */
		final String batchQuery = String.format("UPDATE customer_%d_binding_tbl SET user_status=?, user_remark=?, timestamp=CURRENT_TIMESTAMP WHERE mailinglist_id=? AND customer_id=?", companyID)
				+ (mediatype != null ? " AND mediatype=?" : "");
		
        int touchedLinesSum = 0;

		for(final int mailinglistId : mailinglistIds) {
			for(final int customerId : customerIDs) {
				final int touched = mediatype != null 
						? update(batchQuery, userStatus.getStatusCode(), userRemark, mailinglistId, customerId, mediatype.getMediaCode())
						: update(batchQuery, userStatus.getStatusCode(), userRemark, mailinglistId, customerId);
				
				if(touched > 0) {
					touchedLinesSum += touched;
					
					fireBindingUpdated(companyID, customerId, mailinglistId, mediatype, userStatus);
				}
			}
		}

        return touchedLinesSum;
	}

	@Override
	public int bulkDelete(int companyID, List<Integer> mailinglistIds, MediaTypes mediatype, List<Integer> customerIDs) {
		String query = "DELETE FROM customer_" + companyID + "_binding_tbl"
			+ " WHERE " + makeBulkInClauseForInteger("mailinglist_id", mailinglistIds) + " AND " + makeBulkInClauseForInteger("customer_id", customerIDs);
		if (mediatype != null) {
			return update(query + " AND mediatype = ?", mediatype.getMediaCode());
		} else {
			return update(query);
		}
	}

	@Override
	public int bulkCreate(int companyID, List<Integer> mailinglistIds, MediaTypes mediatype, UserStatus userStatus, String userRemark, List<Integer> customerIDs) {
		/*
		 * Note: batchupdate() cannot be used here.
		 * 
		 * Some JDBC drivers (like Mariadb) do not support to return the number of touched lines. 
		 * This is an information we need here.
		 */
		final Date now = new Date();
		final String query = "INSERT INTO customer_" + companyID + "_binding_tbl (customer_id, mailinglist_id, mediatype, user_type, user_status, user_remark, creation_date, timestamp) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
		
		int touchedLinesSum = 0;
		
		for (int mailinglistId : mailinglistIds) {
			List<Integer> existingCustomerIDs = select("SELECT customer_id FROM customer_" + companyID + "_binding_tbl WHERE mailinglist_id = ? AND mediatype = ? AND " + makeBulkInClauseForInteger("customer_id", customerIDs), IntegerRowMapper.INSTANCE, mailinglistId, mediatype.getMediaCode());
			for (int customerID : customerIDs) {
				if (!existingCustomerIDs.contains(customerID)) {
					final int touched = update(query, customerID, mailinglistId, mediatype.getMediaCode(), UserType.World.getTypeCode(), userStatus.getStatusCode(), userRemark, now, now);
					
					if(touched > 0) {
						touchedLinesSum += touched;
						
						fireBindingUpdated(companyID, customerID, mailinglistId, mediatype, userStatus);
					}
				}
			}
		}

		return touchedLinesSum;
	}

    @Override
    public Map<String, Integer> getRecipientStatusStat(int mailinglistId, int targetId, int companyId) {
        Map<String, Integer> result = new LinkedHashMap<>();
        List<Object> params = new ArrayList<>();

        String sql = summedStatusesSelectPart(params, mailinglistId, targetId, companyId)
                + notSummedStatusesSelectPart(params, mailinglistId, targetId, companyId);

        query(sql, rs -> result.put(rs.getString("status"), rs.getInt("count")), params.toArray());
        return result;
    }

    private String summedStatusesSelectPart(List<Object> params, int mailinglistId, int targetId, int companyId) {
        return Arrays.stream(SummedRecipientStatus.values())
                .map(status  -> getRecipientStatusSelect(params, status, mailinglistId, targetId, companyId))
                .collect(Collectors.joining());
    }

    private String getRecipientStatusSelect(List<Object> params, SummedRecipientStatus status, int mailinglistId,
											int targetId, int companyId) {
        String sql = "SELECT '" + status.getName() + "' AS status, COUNT(*) AS count" +
                " FROM customer_" + companyId + "_binding_tbl bind";
        sql += getCustomerTblJoinIfNeeded(mailinglistId, targetId, companyId);
        sql += " WHERE " + status.getLikeSql();
        sql += getMailinglistFilter(params, mailinglistId);
        sql += getTargetIdFilter(targetId, companyId);
        sql += " UNION ALL ";
        return sql;
    }

	private String getMailinglistFilter(List<Object> params, int mailinglistId) {
        if (mailinglistId <= 0) {
            return "";
        }
        params.add(mailinglistId);
        return " AND bind.mailinglist_id = ?";
    }

    private String getCustomerTblJoinIfNeeded(int mailinglistId, int targetId, int companyId) {
        if (mailinglistId <= 0 && targetId <= 0) {
            return "";
        }
        return String.format(" JOIN customer_%d_tbl cust ON cust.customer_id = bind.customer_id", companyId);
    }

    private String notSummedStatusesSelectPart(List<Object> params, int mailinglistId, int targetId, int companyId) {
        String sql = " SELECT * FROM (SELECT bind.user_remark AS status, COUNT(*) AS count" +
                " FROM customer_" + companyId + "_binding_tbl bind";
        sql += getCustomerTblJoinIfNeeded(mailinglistId, targetId, companyId);
        sql += " WHERE " + remarksNotLikeSummedStatuses();
        sql += getMailinglistFilter(params, mailinglistId);
        sql += getTargetIdFilter(targetId, companyId);
        sql += " GROUP BY bind.user_remark ORDER BY 2 DESC) subselect";
        return sql;
    }

    private String getTargetIdFilter(int targetId, int companyId) {
        if (targetId <= 0) {
            return "";
        }
        Target target = targetDao.getTarget(targetId, companyId);
        return " AND (" + target.getTargetSQL() + ")";
    }

    private String remarksNotLikeSummedStatuses() {
        return Arrays.stream(SummedRecipientStatus.values())
                .map(SummedRecipientStatus::getNotLikeSql)
                .collect(Collectors.joining(" AND "));
    }

	protected void fireBindingCreated(int companyID, BindingEntry binding) {
		fireBindingCreated(
				companyID,
				binding.getCustomerID(),
				binding.getMailinglistID(),
				MediaTypes.getMediaTypeForCode(binding.getMediaType()),
				UserStatus.getByCode(binding.getUserStatus()));
	}
	
	protected void fireBindingCreated(int companyID, int recipientID, int mailinglistID, MediaTypes mediatype, UserStatus userStatus) {
		final List<OnBindingChangedHandler> list = List.copyOf(this.bindingChangedHandlers);
		
		for(final OnBindingChangedHandler handler : list) {
			handler.bindingCreated(companyID, recipientID, mailinglistID, mediatype, userStatus);
		}
	}

	protected void fireBindingUpdated(int companyID, BindingEntry binding) {
		fireBindingUpdated(
				companyID,
				binding.getCustomerID(),
				binding.getMailinglistID(),
				binding.getMediaType() != -1 ? MediaTypes.getMediaTypeForCode(binding.getMediaType()) : null,
				UserStatus.getByCode(binding.getUserStatus()));
	}
	
	protected void fireBindingUpdated(int companyID, int recipientID, int mailinglistID, MediaTypes mediatype, UserStatus userStatus) {
		final List<OnBindingChangedHandler> list = List.copyOf(this.bindingChangedHandlers);
		
		for(final OnBindingChangedHandler handler : list) {
			handler.bindingChanged(companyID, recipientID, mailinglistID, mediatype, userStatus);
		}
	}

    @Override
	public void cleanAdminAndTestUnsubsriptions(int companyID, int mailingID) {
		String sqlOptout = "UPDATE customer_" + companyID + "_binding_tbl SET exit_mailing_id = 0"
			+ " WHERE exit_mailing_id = ?"
				+ " AND user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')"
				+ " AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?)";
		update(sqlOptout, mailingID, mailingID);
    }
}
