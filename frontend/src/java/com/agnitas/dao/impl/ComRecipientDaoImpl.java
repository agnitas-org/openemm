/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao.impl;

import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLDataException;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.beans.DatasourceDescription;
import org.agnitas.beans.Recipient;
import org.agnitas.beans.Title;
import org.agnitas.beans.factory.BindingEntryFactory;
import org.agnitas.beans.factory.RecipientFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.beans.impl.RecipientImpl;
import org.agnitas.beans.impl.ViciousFormDataException;
import org.agnitas.dao.SourceGroupType;
import org.agnitas.dao.UserStatus;
import org.agnitas.dao.impl.PaginatedBaseDaoImpl;
import org.agnitas.dao.impl.mapper.IntegerRowMapper;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.recipient.service.InvalidDataException;
import org.agnitas.emm.core.recipient.service.RecipientsModel.CriteriaEquals;
import org.agnitas.service.ImportException;
import org.agnitas.service.MailingRecipientExportWorker;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.CaseInsensitiveSet;
import org.agnitas.util.CsvColInfo;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbColumnType;
import org.agnitas.util.DbColumnType.SimpleDataType;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.ParameterParser;
import org.agnitas.util.SafeString;
import org.agnitas.util.SqlPreparedInsertStatementManager;
import org.agnitas.util.SqlPreparedUpdateStatementManager;
import org.agnitas.util.Tuple;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

import com.agnitas.beans.Admin;
import com.agnitas.beans.ComRecipientHistory;
import com.agnitas.beans.ComRecipientMailing;
import com.agnitas.beans.ComRecipientReaction;
import com.agnitas.beans.ComTarget;
import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.beans.WebtrackingHistoryEntry;
import com.agnitas.beans.impl.ComRecipientHistoryImpl;
import com.agnitas.beans.impl.ComRecipientLiteImpl;
import com.agnitas.beans.impl.ComRecipientMailingImpl;
import com.agnitas.beans.impl.ComRecipientReactionImpl;
import com.agnitas.beans.impl.RecipientDates;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.action.operations.ActionOperationUpdateCustomerParameters;
import com.agnitas.emm.core.mailing.bean.MailingRecipientStatRow;
import com.agnitas.emm.core.mailing.bean.impl.MailingRecipientStatRowImpl;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.agnitas.emm.core.profilefields.ProfileFieldBulkUpdateException;
import com.agnitas.emm.core.recipient.ProfileFieldHistoryFeatureNotEnabledException;
import com.agnitas.emm.core.recipient.RecipientException;
import com.agnitas.emm.core.recipient.service.RecipientProfileHistoryService;
import com.agnitas.emm.core.recipient.service.RecipientType;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.service.RecipientFieldService.RecipientStandardField;
import com.agnitas.emm.restful.RestfulClientException;
import com.agnitas.emm.util.html.HtmlChecker;
import com.agnitas.emm.util.html.HtmlCheckerException;
import com.agnitas.util.NumericUtil;

public class ComRecipientDaoImpl extends PaginatedBaseDaoImpl implements ComRecipientDao {

	private static final Logger logger = LogManager.getLogger(ComRecipientDaoImpl.class);

	private static final String[] MAILINGLIST_VALUE_FIELDS = {
		ComRecipientHistory.USER_TYPE,
		ComRecipientHistory.USER_STATUS,
		ComRecipientHistory.USER_REMARK,
		ComRecipientHistory.EXIT_MAILING_ID,
		ComRecipientHistory.EMAIL
	};

	private static final List<String> SUPPLEMENTAL_DATE_COLUMN_SUFFIXES = Arrays.asList(
			ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY,
			ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH,
			ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR,
			ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND,
			ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE,
			ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR
	);

	/** Services accessing configuration data. */
	protected ConfigService configService;

	/** DAO accessing company data. */
	protected ComCompanyDao companyDao;

	/** Services providing information about database columns. */
	protected RecipientFieldService recipientFieldService;

	protected BindingEntryFactory bindingEntryFactory;

	/** Factory creating Recipients. */
	protected RecipientFactory recipientFactory;

	/** Servie handling profile field history. */
	private RecipientProfileHistoryService recipientProfileHistoryService;

	private DatasourceDescriptionDao datasourceDescriptionDao;
	
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	public void setCompanyDao(ComCompanyDao companyDao) {
		this.companyDao = companyDao;
	}

	public void setRecipientFieldService(RecipientFieldService recipientFieldService) {
		this.recipientFieldService = recipientFieldService;
	}

	public void setBindingEntryFactory(BindingEntryFactory bindingEntryFactory) {
		this.bindingEntryFactory = bindingEntryFactory;
	}

	public void setRecipientFactory(RecipientFactory recipientFactory) {
		this.recipientFactory = recipientFactory;
	}

	public void setRecipientProfileHistoryService(RecipientProfileHistoryService recipientProfileHistoryService) {
		this.recipientProfileHistoryService = recipientProfileHistoryService;
	}

	public void setDatasourceDescriptionDao(DatasourceDescriptionDao datasourceDescriptionDao) {
		this.datasourceDescriptionDao = datasourceDescriptionDao;
	}

	public static String getCustomerTableName(int companyID) {
		return "customer_" + companyID + "_tbl";
	}

	public static String getCustomerBindingTableName(int companyID) {
		return "customer_" + companyID + "_binding_tbl";
	}

	/**
	 * gets all admin and test recipients (lite version) - id, first name, last name, email
	 *
	 * @param companyID
	 *			- company id
	 * @param mailinglistID
	 *			- mailing id
	 */
	@Override
	public List<ComRecipientLiteImpl> getAdminAndTestRecipients(int companyID, int mailinglistID) {
		if (companyID > 0) {
			
			List<String> userTypes = Arrays.asList(UserType.Admin.getTypeCode(), UserType.TestUser.getTypeCode(), UserType.TestVIP.getTypeCode());
			
			String builder = "SELECT cust.customer_id, cust.email, cust.firstname, cust.lastname " +
					" FROM " + getCustomerTableName(companyID) + " cust WHERE cust.customer_id IN (" +
					"	SELECT bind.customer_id FROM " + getCustomerBindingTableName(companyID) + " bind " +
					"	WHERE " + makeBulkInClauseForString("bind.user_type", userTypes) +
					"	AND bind.user_status = 1 AND bind.mailinglist_id = ? " +
					") ORDER BY cust.customer_id";
			return select(logger, builder, new ComRecipientLite_RowMapper(), mailinglistID);
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public final List<ComRecipientLiteImpl> listAdminAndTestRecipientsByAdmin(final int companyID, final int adminID) {
		if (companyID > 0) {
			final StringBuilder sql = new StringBuilder("SELECT cust.customer_id, cust.email, cust.firstname, cust.lastname")
					.append(String.format(" FROM %s cust WHERE cust.customer_id IN (", getCustomerTableName(companyID)))
					.append(String.format("	SELECT bind.customer_id FROM %s bind ", getCustomerBindingTableName(companyID)))
					.append(" WHERE bind.user_type IN (?,?,?)")
					.append(" AND bind.user_status = 1");
            List<Object> params = Stream.of(UserType.Admin, UserType.TestUser, UserType.TestVIP)
                    .map(UserType::getTypeCode).collect(Collectors.toList());
            
            if (adminID > 0 && configService.isDisabledMailingListsSupported()) {
				sql.append(" AND bind.mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY);
				params.add(adminID);
			}
            sql.append(") ORDER BY cust.customer_id");
            
			return select(logger, sql.toString(), new ComRecipientLite_RowMapper(), params.toArray());
		}

		return new ArrayList<>();
	}

	@Override
	public List<Integer> getAdminAndTestRecipientIds(int companyID, int mailinglistID) {
		if (companyID > 0) {
			String sql = "SELECT bind.customer_id"
				+ " FROM " + getCustomerTableName(companyID) + " cust, " + getCustomerBindingTableName(companyID) + " bind"
				+ " WHERE bind.user_type IN (?, ?, ?) AND bind.user_status = ? AND bind.mailinglist_id = ? AND bind.customer_id = cust.customer_id"
				+ " ORDER BY bind.user_type, bind.customer_id";

			return select(logger, sql, IntegerRowMapper.INSTANCE, UserType.Admin.getTypeCode(), UserType.TestUser.getTypeCode(), UserType.TestVIP.getTypeCode(), UserStatus.Active.getStatusCode(), mailinglistID);
		} else {
			return new ArrayList<>();
		}
	}

	@Override
	public int getAdminOrTestRecipientId(int companyID, int adminId) {
		if (companyID > 0) {
			String sql = "SELECT bind.customer_id " +
					"FROM " + getCustomerTableName(companyID) + " cust, " + getCustomerBindingTableName(companyID) + " bind " +
					"WHERE bind.user_type IN (?, ?, ?) AND bind.user_status = ? " +
					"AND (bind.customer_id = cust.customer_id OR bind.customer_id IS NULL) ";
			List<Object> params = new ArrayList<>(Arrays.asList(
					UserType.TestUser.getTypeCode(),
					UserType.TestVIP.getTypeCode(),
					UserType.Admin.getTypeCode(),
					UserStatus.Active.getStatusCode()));

            if (adminId > 0 && configService.isDisabledMailingListsSupported()) {
				sql += "AND bind.mailinglist_id NOT IN " + DISABLED_MAILINGLIST_QUERY;
				params.add(adminId);
			}
			sql += isOracleDB()
					? "AND ROWNUM = ? ORDER BY bind.user_type, bind.customer_id"
					: "ORDER BY bind.user_type, bind.customer_id LIMIT ?";
			params.add(1);

			return selectInt(logger, sql, params.toArray());
		} else {
			return 0;
		}
	}

	@Override
	public Tuple<Integer, String> findSpecificCustomerColumn(int companyID, String firstname, String lastname, String email, String fieldname) {
		try {
			List<Object> parameters = new ArrayList<>();
		
			String firstNameWhere;
			if (StringUtils.isEmpty(firstname)) {
				if (isOracleDB()) {
					firstNameWhere = "firstname IS NULL";
				} else {
					firstNameWhere = "(firstname IS NULL OR firstname = '')";
				}
			} else {
				firstNameWhere = "firstname = ?";
				parameters.add(firstname);
			}

			String lastNameWhere;
			if (StringUtils.isEmpty(lastname)) {
				if (isOracleDB()) {
					lastNameWhere = "lastname IS NULL";
				} else {
					lastNameWhere = "(lastname IS NULL OR lastname = '')";
				}
			} else {
				lastNameWhere = "lastname = ?";
				parameters.add(lastname);
			}
		
			parameters.add(AgnUtils.normalizeEmail(email));
		
			List<Map<String, Object>> results = select(logger, "SELECT customer_id FROM " + getCustomerTableName(companyID) + " cust WHERE " + firstNameWhere + " AND " + lastNameWhere + " AND email = ? AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0", parameters.toArray());
			if (results.size() > 0) {
				int customerID = ((Number) results.get(0).get("customer_id")).intValue();
			
				List<Map<String, Object>> fieldValue = select(logger, "SELECT " + SafeString.getSafeDbColumnName(fieldname) + " FROM " + getCustomerTableName(companyID) + " cust WHERE customer_id = ?", customerID);
			
				if (fieldValue.size() > 0) {
					// customer found --> return value of requested field
					if (fieldValue.get(0).get(fieldname) == null) {
						return new Tuple<>(customerID, "");
					} else {
						return new Tuple<>(customerID, fieldValue.get(0).get(fieldname).toString());
					}
				} else {
					return new Tuple<>(customerID, "0");
				}
			} else {
				return new Tuple<>(0, "-1");
			}
		} catch (Exception e) {
			logger.error("findSpecificCustomerColumn (CID " + companyID + "): " + e.getMessage(), e);
			return new Tuple<>(0, "-1");
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean updateDataSource(Recipient cust) {
		try {
			if (cust.hasCustParameter("DATASOURCE_ID")) {
				String sql = "SELECT datasource_id FROM " + getCustomerTableName(cust.getCompanyID()) + " WHERE customer_id = ?";
				int oldDatasourceID = selectIntWithDefaultValue(logger, sql, 0, cust.getCustomerID());
				String newDatasourceIDString = cust.getCustParametersNotNull("DATASOURCE_ID");

				if (oldDatasourceID == 0 && StringUtils.isNotEmpty(newDatasourceIDString)) {
					int newDatasourceID = Integer.parseInt(newDatasourceIDString);
					sql = "UPDATE " + getCustomerTableName(cust.getCompanyID()) + " SET datasource_id = ? WHERE customer_id = ?";
					update(logger, sql, newDatasourceID, cust.getCustomerID());
				} else if (oldDatasourceID != 0 && StringUtils.isNotEmpty(newDatasourceIDString)) {
					// update the field "latest_datasource_id"
					int newDatasourceID = Integer.parseInt(newDatasourceIDString);
					sql = "UPDATE " + getCustomerTableName(cust.getCompanyID()) + " SET latest_datasource_id = ? WHERE customer_id = ?";
					update(logger, sql, newDatasourceID, cust.getCustomerID());
				}
				return true;
			} else {
				return false;
			}
		} catch (@SuppressWarnings("unused") Exception e) {
			return false;
		}
	}

	@Override
	public PaginatedListImpl<MailingRecipientStatRow> getMailingRecipients(int mailingId, int companyId, int filterType, int pageNumber, int pageSize, String sortCriterion, boolean sortAscending, List<String> columns) throws Exception {
		Map<String, String> sortableColumns = new CaseInsensitiveMap<>();

		if (columns != null && !columns.isEmpty()) {
			List<String> columnNames = DbUtilities.getColumnNames(getDataSource(), "customer_" + companyId + "_tbl");
			for (String column : columns) {
				if (columnNames.contains(column)) {
					sortableColumns.put(column, "cust." + column);
				}
			}
		} else {
			columns = new ArrayList<>();
		}

		if (!columns.contains("firstname")) {
			columns.add("firstname");
		}
		if (!columns.contains("lastname")) {
			columns.add("lastname");
		}
		if (!columns.contains("email")) {
			columns.add("email");
		}

		sortableColumns.put("firstname", "cust.firstname");
		sortableColumns.put("lastname", "cust.lastname");
		sortableColumns.put("email", "cust.email");
		sortableColumns.put("receive_time", "receive_time");
		sortableColumns.put("open_time", "open_time");
		sortableColumns.put("openings", "openings");
		sortableColumns.put("click_time", "click_time");
		sortableColumns.put("clicks", "clicks");
		sortableColumns.put("bounce_time", "bounce_time");
		sortableColumns.put("optout_time", "optout_time");

		final int mailingListId = selectInt(logger, "SELECT mailinglist_id FROM mailing_tbl WHERE company_id = ? AND mailing_id = ?", companyId, mailingId);

		// Keep the order of requested columns
		List<String> sqlColumns = new ArrayList<>();
		final List<String> toRemove = new ArrayList<>();
		for (String column : columns) {
			if (sortableColumns.get(column) == null) {
				toRemove.add(column);
			} else if (sortableColumns.get(column).startsWith("cust.")) {
				sqlColumns.add(sortableColumns.get(column));
			}
		}
		columns.removeAll(toRemove);

		int totalRows = getNumberOfMailingRecipients(companyId, filterType, mailingId, mailingListId, sqlColumns);
		List<Object> params = new ArrayList<>();
		String selectSql;
		List<MailingRecipientStatRow> recipients;
		if (isRecipientsNumberExceedsLimit(totalRows, companyId)) {
			// if the maximum number of recipients to show is exceeded
			// only the first page of unsorted recipients is shown to discharge the database and its performance
			// BTW: another sql statement will be executed that was optimized
			pageNumber = 1;
			selectSql = getMailingRecipientsQueryWithoutSorting(companyId, mailingId, mailingListId, filterType, columns, pageSize, params);
		} else {
			if ("customer_id".equalsIgnoreCase(sortCriterion)) {
				sortCriterion = "customer_id";
			} else if (StringUtils.isBlank(sortCriterion) || !sortableColumns.containsKey(sortCriterion)) {
				sortCriterion = "receive_time";
			} else if (sortableColumns.containsKey(sortCriterion)) {
				sortCriterion = sortableColumns.get(sortCriterion);
			}

			selectSql =
					"SELECT cust.customer_id,"
							+ " " + StringUtils.join(sqlColumns, ", ") + ","
							+ " MAX(succ.timestamp) AS receive_time,"
							+ " MIN(opl.first_open) AS open_time,"
							+ " COALESCE(MAX(opl.open_count), 0) AS openings,"
							+ " MIN(rlog.timestamp) AS click_time,"
							+ " COUNT(DISTINCT rlog.timestamp) AS clicks,"
							+ " MAX(bind1.timestamp) AS bounce_time,"
							+ " MAX(bind2.timestamp) AS optout_time"
							+ " FROM customer_" + companyId + "_tbl cust"
							+ " JOIN mailtrack_" + companyId + "_tbl track ON track.customer_id = cust.customer_id AND track.mailing_id = ?"
							+ " LEFT OUTER JOIN success_" + companyId + "_tbl succ ON succ.customer_id = cust.customer_id AND succ.mailing_id = ?"
							+ " LEFT OUTER JOIN onepixellog_" + companyId + "_tbl opl ON opl.customer_id = cust.customer_id AND opl.mailing_id = ?"
							+ " LEFT OUTER JOIN rdirlog_" + companyId + "_tbl rlog ON rlog.customer_id = cust.customer_id AND rlog.mailing_id = ?"
							+ " LEFT OUTER JOIN customer_" + companyId + "_binding_tbl bind1 ON bind1.customer_id = cust.customer_id AND bind1.exit_mailing_id = ? AND bind1.user_status = ? AND bind1.user_type NOT IN (?, ?, ?)"
							+ " LEFT OUTER JOIN customer_" + companyId + "_binding_tbl bind2 ON bind2.customer_id = cust.customer_id AND bind2.exit_mailing_id = ? AND bind2.user_status IN (?, ?) AND bind2.user_type IN (?, ?)"
							+ " WHERE EXISTS"
							+ " (SELECT 1 FROM customer_" + companyId + "_binding_tbl bind WHERE bind.customer_id = cust.customer_id AND bind.mailinglist_id = ? AND bind.user_type NOT IN (?, ?, ?))"
							+ " GROUP BY cust.customer_id, " + StringUtils.join(sqlColumns, ", ");

			switch (filterType) {
				case MailingRecipientExportWorker.MAILING_RECIPIENTS_OPENED:
					selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel") + " WHERE open_time IS NOT NULL";
					if (sortCriterion != null && sortCriterion.startsWith("cust.")) {
						sortCriterion = sortCriterion.substring(5);
					}
					break;

				case MailingRecipientExportWorker.MAILING_RECIPIENTS_CLICKED:
					selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel") + " WHERE click_time IS NOT NULL";
					if (sortCriterion != null && sortCriterion.startsWith("cust.")) {
						sortCriterion = sortCriterion.substring(5);
					}
					break;

				case MailingRecipientExportWorker.MAILING_RECIPIENTS_BOUNCED:
					selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel") + " WHERE bounce_time IS NOT NULL";
					if (sortCriterion != null && sortCriterion.startsWith("cust.")) {
						sortCriterion = sortCriterion.substring(5);
					}
					break;

				case MailingRecipientExportWorker.MAILING_RECIPIENTS_UNSUBSCRIBED:
					selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel") + " WHERE optout_time IS NOT NULL";
					if (sortCriterion != null && sortCriterion.startsWith("cust.")) {
						sortCriterion = sortCriterion.substring(5);
					}
					break;

				default:
					// filter nothing
			}

			String sortClause = "ORDER BY ";
			if (isOracleDB()) {
				sortClause += sortCriterion + " " + (sortAscending ? "ASC" : "DESC") + " NULLS LAST";
			} else {
				// Force MySQL sort null values the same way that Oracle does
				if ("receive_time".equals(sortCriterion)
						|| "open_time".equals(sortCriterion)
						|| "openings".equals(sortCriterion)
						|| "click_time".equals(sortCriterion)
						|| "clicks".equals(sortCriterion)
						|| "bounce_time".equals(sortCriterion)
						|| "optout_time".equals(sortCriterion)) {
					if (filterType == 0) {
						selectSql = "SELECT * FROM (" + selectSql + ")" + (isOracleDB() ? "" : " subsel");
					}
				}
				sortClause += "ISNULL(" + sortCriterion + "), " + sortCriterion + " " + (sortAscending ? "ASC" : "DESC");
			}
			sortClause += ", customer_id " + (sortAscending ? "ASC" : "DESC");

			params.add(mailingId);
			params.add(mailingId);
			params.add(mailingId);
			params.add(mailingId);
			params.add(mailingId);
			params.add(UserStatus.Bounce.getStatusCode());
			params.add(BindingEntry.UserType.Admin.getTypeCode());
			params.add(BindingEntry.UserType.TestUser.getTypeCode());
			params.add(BindingEntry.UserType.TestVIP.getTypeCode());
			params.add(mailingId);
			params.add(UserStatus.UserOut.getStatusCode());
			params.add(UserStatus.AdminOut.getStatusCode());
			params.add(BindingEntry.UserType.World.getTypeCode());
			params.add(BindingEntry.UserType.WorldVIP.getTypeCode());
			params.add(mailingListId);
			params.add(BindingEntry.UserType.Admin.getTypeCode());
			params.add(BindingEntry.UserType.TestUser.getTypeCode());
			params.add(BindingEntry.UserType.TestVIP.getTypeCode());

			pageNumber = AgnUtils.getValidPageNumber(totalRows, pageNumber, pageSize);
			int offset = pageNumber * pageSize;

			if (isOracleDB()) {
				selectSql = "SELECT * FROM (SELECT selection.*, rownum AS r FROM (" + selectSql + " " + sortClause + ") selection) WHERE r BETWEEN ? AND ?";
				params.addAll(List.of((offset - pageSize + 1), offset));
			} else {
				selectSql = selectSql + " " + sortClause + " LIMIT ?, ?";
				params.addAll(List.of((offset - pageSize), pageSize));
			}
		}

		List<String> selectedColumns = new ArrayList<>(columns);
		selectedColumns.addAll(List.of(
				"customer_id", "receive_time", "open_time", "openings", "click_time", "clicks", "bounce_time", "optout_time"
		));

		recipients = select(logger, selectSql, new MailingRecipientStatRow_RowMapper(companyId, selectedColumns), params.toArray());
		return new PaginatedListImpl<>(recipients, totalRows, pageSize, pageNumber, sortCriterion, sortAscending);
	}

	private String getMailingRecipientsQueryWithoutSorting(int companyId, int mailingId, int mailingListId, int filterType, List<String> columns, int pageSize, List<Object> params) {
		String sqlColumns = joinWithPrefixes(columns, "cust.");
		String s3Columns = joinWithPrefixes(columns, "s3.");

		String filteringQuery = "SELECT cust.customer_id, " + sqlColumns +
				" FROM customer_" + companyId + "_tbl cust " +
				"    JOIN mailtrack_" + companyId + "_tbl track " +
				"        ON track.customer_id = cust.customer_id AND track.mailing_id = ? " +
				"    JOIN customer_" + companyId + "_binding_tbl cb " +
				"        ON cust.customer_id = cb.customer_id AND cb.mailinglist_id = ? AND cb.user_type NOT IN (?, ?, ?) ";

		params.add(mailingId);
		params.add(mailingListId);
		params.add(BindingEntry.UserType.Admin.getTypeCode());
		params.add(BindingEntry.UserType.TestUser.getTypeCode());
		params.add(BindingEntry.UserType.TestVIP.getTypeCode());

		filteringQuery += createJoinStatementWithMailingRecipientsFiltering(filterType, params, companyId, mailingId);
		filteringQuery += " GROUP BY cust.customer_id, " + sqlColumns +
				" ORDER BY cust.customer_id ";

		String limitedFilteredSelect;

		if (isOracleDB()) {
			String s1Columns = joinWithPrefixes(columns, "s1.");
			String s2Columns = joinWithPrefixes(columns, "s2.");

			String filteredRowNumSelect = String.format("SELECT s1.customer_id, %s, rownum AS r FROM (%s) s1", s1Columns, filteringQuery);
			limitedFilteredSelect = String.format("SELECT s2.customer_id, %s, s2.r FROM (%s) s2 WHERE s2.r BETWEEN 1 AND ?", s2Columns, filteredRowNumSelect);
		} else {
			limitedFilteredSelect = filteringQuery + " LIMIT 0, ?";
		}

		params.add(pageSize);

		String selectQuery = "SELECT s3.customer_id, %s," +
				"       MAX(succ.timestamp)              AS receive_time, " +
				"       MIN(opl.first_open)              AS open_time, " +
				"       COALESCE(MAX(opl.open_count), 0) AS openings, " +
				"       MIN(rlog.timestamp)              AS click_time, " +
				"       COUNT(DISTINCT rlog.timestamp)   AS clicks, " +
				"       MAX(b1.timestamp)                AS bounce_time, " +
				"       MAX(b2.timestamp)                AS optout_time " +
				"FROM (%s) s3 " +
				"     LEFT OUTER JOIN success_" + companyId + "_tbl succ " +
				"             ON succ.customer_id = s3.customer_id AND succ.mailing_id = ? " +
				"     LEFT OUTER JOIN onepixellog_" + companyId + "_tbl opl " +
				"             ON opl.customer_id = s3.customer_id AND opl.mailing_id = ? " +
				"     LEFT OUTER JOIN rdirlog_" + companyId + "_tbl rlog " +
				"             ON rlog.customer_id = s3.customer_id AND rlog.mailing_id = ? " +
				"     LEFT OUTER JOIN customer_" + companyId + "_binding_tbl b1 " +
				"             ON b1.customer_id = s3.customer_id AND b1.exit_mailing_id = ? AND " +
				"                     b1.user_status = ? AND b1.user_type NOT IN (?, ?, ?) " +
				"     LEFT OUTER JOIN customer_" + companyId + "_binding_tbl b2 " +
				"             ON b2.customer_id = s3.customer_id AND b2.exit_mailing_id = ? AND " +
				"                     b2.user_status IN (?, ?) AND b2.user_type IN (?, ?) " +
				"GROUP BY s3.customer_id, %s ";

		params.add(mailingId);
		params.add(mailingId);
		params.add(mailingId);
		params.add(mailingId);
		params.add(UserStatus.Bounce.getStatusCode());
		params.add(BindingEntry.UserType.Admin.getTypeCode());
		params.add(BindingEntry.UserType.TestUser.getTypeCode());
		params.add(BindingEntry.UserType.TestVIP.getTypeCode());

		params.add(mailingId);
		params.add(UserStatus.UserOut.getStatusCode());
		params.add(UserStatus.AdminOut.getStatusCode());
		params.add(BindingEntry.UserType.World.getTypeCode());
		params.add(BindingEntry.UserType.WorldVIP.getTypeCode());

		return String.format(selectQuery, s3Columns, limitedFilteredSelect, s3Columns);
	}

	private String joinWithPrefixes(List<String> strings, String prefix) {
		return strings.stream()
				.map(s -> prefix + s)
				.collect(Collectors.joining(", "));
	}

	private int getNumberOfMailingRecipients(int companyId, int filterType, int mailingId, int mailinglistId, List<String> columns) {
		List<Object> params = new ArrayList<>();
		params.add(mailingId);

		String subSel = "SELECT cust.customer_id, " + StringUtils.join(columns, ", ") +
				" FROM customer_" + companyId + "_tbl cust " +
				"         JOIN mailtrack_" + companyId + "_tbl track ON track.customer_id = cust.customer_id AND track.mailing_id = ? ";

		subSel += createJoinStatementWithMailingRecipientsFiltering(filterType, params, companyId, mailingId);

		subSel += " WHERE EXISTS(SELECT 1 FROM customer_" + companyId + "_binding_tbl bind " +
				" WHERE bind.customer_id = cust.customer_id AND bind.mailinglist_id = ? " +
				" AND bind.user_type NOT IN (?, ?, ?)) " +
				" GROUP BY cust.customer_id, " + StringUtils.join(columns, ", ");

		params.add(mailinglistId);
		params.add(BindingEntry.UserType.Admin.getTypeCode());
		params.add(BindingEntry.UserType.TestUser.getTypeCode());
		params.add(BindingEntry.UserType.TestVIP.getTypeCode());

		return selectInt(logger, String.format("SELECT COUNT(*) FROM (%s) sel", subSel), params.toArray());
	}

	private String createJoinStatementWithMailingRecipientsFiltering(int filterType, List<Object> params, int companyId, int mailingId) {
		String joinStatement = "";

		switch (filterType) {
			case MailingRecipientExportWorker.MAILING_RECIPIENTS_OPENED:
				joinStatement = " JOIN onepixellog_" + companyId + "_tbl opl ON opl.customer_id = cust.customer_id AND opl.mailing_id = ? ";
				params.add(mailingId);
				break;

			case MailingRecipientExportWorker.MAILING_RECIPIENTS_CLICKED:
				joinStatement = " JOIN rdirlog_" + companyId + "_tbl rlog ON rlog.customer_id = cust.customer_id AND rlog.mailing_id = ? ";
				params.add(mailingId);
				break;

			case MailingRecipientExportWorker.MAILING_RECIPIENTS_BOUNCED:
				joinStatement = " JOIN customer_" + companyId + "_binding_tbl bind1 ON bind1.customer_id = cust.customer_id AND bind1.exit_mailing_id = ? " +
						"AND bind1.user_status = ? AND bind1.user_type NOT IN (?, ?, ?)";
				params.add(mailingId);
				params.add(UserStatus.Bounce.getStatusCode());
				params.add(BindingEntry.UserType.Admin.getTypeCode());
				params.add(BindingEntry.UserType.TestUser.getTypeCode());
				params.add(BindingEntry.UserType.TestVIP.getTypeCode());
				break;

			case MailingRecipientExportWorker.MAILING_RECIPIENTS_UNSUBSCRIBED:
				joinStatement = " JOIN customer_" + companyId + "_binding_tbl bind2 ON bind2.customer_id = cust.customer_id AND bind2.exit_mailing_id = ? " +
						"AND bind2.user_status IN (?, ?) AND bind2.user_type IN (?, ?)";
				params.add(mailingId);
				params.add(UserStatus.UserOut.getStatusCode());
				params.add(UserStatus.AdminOut.getStatusCode());
				params.add(BindingEntry.UserType.World.getTypeCode());
				params.add(BindingEntry.UserType.WorldVIP.getTypeCode());
				break;

			default:
				// filter nothing
		}

		return joinStatement;
	}
	
	@Override
	public List<Recipient> getDuplicateRecipients(int companyId, String email, String select, Object[] queryParams) throws Exception {
    	logger.warn("Unsupported method getDuplicateRecipients()");
    	return new ArrayList<>();
	}
	
	public static class RecipientRowMapper implements RowMapper<Recipient> {
		
		private final RecipientFactory recipientFactory;
		private final int companyId;
		
		public RecipientRowMapper(RecipientFactory recipientFactory, int companyId) {
			this.recipientFactory = Objects.requireNonNull(recipientFactory);
			this.companyId = companyId;
		}
		
		@Override
		public Recipient mapRow(ResultSet resultSet, int i) throws SQLException {
			Recipient recipient = recipientFactory.newRecipient(companyId);
			
			ResultSetMetaData metaData = resultSet.getMetaData();
			for (int column = 1; column <= resultSet.getMetaData().getColumnCount(); column++) {
				String columnLabel = metaData.getColumnLabel(column);
				
				Object value = resultSet.getObject(columnLabel);
				String stringValue;
				if (value == null) {
					stringValue = "";
				} else if (value instanceof Number) {
					if ("customer_id".equalsIgnoreCase(columnLabel)) {
						recipient.setCustomerID(((Number) value).intValue());
					}
					stringValue = AgnUtils.stripTrailingZeros(value.toString());
				} else if (value instanceof Date || value instanceof Timestamp) {
					stringValue = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(value);
				} else {
					stringValue = value.toString();
				}
				
				recipient.setCustParameters(columnLabel, stringValue);
			}
			return recipient;
		}
	}

	public class MailingRecipientStatRow_RowMapper implements RowMapper<MailingRecipientStatRow> {
		private int companyId;
		private List<String> selectedColumns;
		
		public MailingRecipientStatRow_RowMapper(int companyId, List<String> selectedColumns) {
			this.companyId = companyId;
			this.selectedColumns = selectedColumns;
		}
		
		@Override
		public MailingRecipientStatRow mapRow(ResultSet resultSet, int row) throws SQLException {
			Recipient recipient = new RecipientImpl();
			recipient.setCompanyID(companyId);
			
			Map<String, Object> recipientValues = new HashMap<>();
			for (String column : selectedColumns) {
				Object value = resultSet.getObject(column);
				if (isOracleDB() && value != null && value.getClass().getName().equalsIgnoreCase("oracle.sql.TIMESTAMP")) {
					recipientValues.put(column, resultSet.getTimestamp(column));
				} else {
					recipientValues.put(column, value);
				}
			}
			recipient.setCustParameters(recipientValues);
			
			MailingRecipientStatRow mailingRecipientStatRow = new MailingRecipientStatRowImpl();
			mailingRecipientStatRow.setRecipient(recipient);
			return mailingRecipientStatRow;
		}
	}

	@Override
	public PaginatedListImpl<Map<String, Object>> getPaginatedRecipientsData(int companyID, Set<String> columns, String statement, Object[] parameters, String sortColumn, boolean sortedAscending, int pageNumber, int pageSize) throws Exception {
		int totalRows = getNumberOfRecipients(companyID, statement, parameters);

		String modifiedSqlStatementForData = statement;

		if (isRecipientsNumberExceedsLimit(totalRows, companyID)) {
			// if the maximum number of recipients to show is exceeded, only the first page of unsorted recipients is shown to discharge the database and its performance
			pageNumber= 1;
			if (isOracleDB()) {
				modifiedSqlStatementForData = String.format("SELECT %s FROM (%s) WHERE rownum BETWEEN 1 AND %d",
						StringUtils.join(columns, ", "),
						modifiedSqlStatementForData, pageSize);
			} else {
				modifiedSqlStatementForData = String.format("SELECT %s FROM (%s) list LIMIT %d",
						StringUtils.join(columns, ", "),
						modifiedSqlStatementForData, pageSize);
			}
		} else {
			String sortClause = getSortClauseForRecipients(companyID, sortColumn, sortColumn, sortedAscending);

			pageNumber = AgnUtils.getValidPageNumber(totalRows, pageNumber, pageSize);
			int offset = pageNumber * pageSize;

			if (isOracleDB()) {
				modifiedSqlStatementForData = "SELECT * FROM (SELECT selection.*, rownum AS r FROM (" + modifiedSqlStatementForData + " " + sortClause + ") selection) WHERE r BETWEEN ? AND ?";
				parameters = AgnUtils.extendObjectArray(parameters, (offset - pageSize + 1), offset);
			} else {
				modifiedSqlStatementForData = modifiedSqlStatementForData + " " + sortClause + " LIMIT ?, ?";
				parameters = AgnUtils.extendObjectArray(parameters, (offset - pageSize), pageSize);
			}
		}

		try {
			List<Map<String, Object>> recipientsData = getRecipientDataList(modifiedSqlStatementForData, parameters);
			return new PaginatedListImpl<>(recipientsData, totalRows, pageSize, pageNumber, sortColumn, sortedAscending);
		} catch(SQLException e) {
			logger.error("Caught SQL exception", e);
		}

		return new PaginatedListImpl<>(new ArrayList<>(), 0, pageSize, 1, sortColumn, sortedAscending);
	}

	protected boolean isRecipientsNumberExceedsLimit(int totalRows, int companyID) {
		int maxRecipients = companyDao.getCompany(companyID).getMaxRecipients();
		return maxRecipients > 0 && totalRows > maxRecipients;
	}

	@Override
	public int getNumberOfRecipients(int companyId) {
		return getNumberOfRecipients(companyId, false);
	}
	
	protected List<Recipient> getRecipientList(int companyID, String statement, Object[] parameters) {
		RecipientRowMapper rowMapper = new RecipientRowMapper(recipientFactory, companyID);
		return select(logger, statement, rowMapper, parameters);
	}

	protected List<Map<String, Object>> getRecipientDataList(String statement, Object[] parameters) throws SQLException {
		try (Connection connection = getDataSource().getConnection()) {
			try {
				final SingleConnectionDataSource scds = new SingleConnectionDataSource(connection, true);
				final JdbcTemplate template = new JdbcTemplate(scds);


				logSqlStatement(logger, statement, parameters);
				List<Map<String, Object>> result = template.queryForList(statement, parameters);
				return result.stream().map(CaseInsensitiveMap::new).collect(Collectors.toList());

			} catch (Exception e) {
				logSqlError(e, logger, statement, parameters);
				throw e;
			}
		}
	}
	
	private String getSortClauseForRecipients(int companyID, String columnName, String sortCriterion, boolean sortedAscending) {
		String sortClause = "";
		if (StringUtils.isNotBlank(sortCriterion)) {
			// Only alphanumeric values may be sorted with upper or lower, which always returns a string value, for keeping the order of numeric values
			try {
				if (DbUtilities.getColumnDataType(getDataSource(), getCustomerTableName(companyID), columnName).getSimpleDataType() == SimpleDataType.Characters) {
					if (isOracleDB()) {
						sortClause = " ORDER BY LOWER(" + sortCriterion + ")" ;
					} else {
						// MySQL DESC sorts null-values to the end by default, oracle DESC sorts null-values to the top
						// MySQL ASC sorts null-values to the top by default, oracle ASC sorts null-values to the end
						if (sortedAscending) {
							sortClause = " ORDER BY IF(" + sortCriterion + " = '' OR " + sortCriterion + " IS NULL, 1, 0), LOWER(" + sortCriterion + ")" ;
						} else {
							sortClause = " ORDER BY IF(" + sortCriterion + " = '' OR " + sortCriterion + " IS NULL, 0, 1), LOWER(" + sortCriterion + ")" ;
						}
					}
				} else {
					sortClause = " ORDER BY " + sortCriterion;
				}
				
				if (sortedAscending) {
					sortClause = sortClause + " ASC";
				} else {
					sortClause = sortClause + " DESC";
				}
			} catch (Exception e) {
				logger.error("Invalid sort field", e);
			}
		}
		return sortClause;
	}
	
	@Override
	public int getNumberOfRecipients(int companyId, boolean ignoreBounceLoadValue) {
		String tableName = getCustomerTableName(companyId);

		if (DbUtilities.checkIfTableExists(getDataSource(), tableName)) {
			if (ignoreBounceLoadValue) {
				return selectInt(logger, String.format("SELECT COUNT(*) FROM %s", tableName));
			} else {
				return selectInt(logger, String.format("SELECT COUNT(*) FROM %s WHERE %s = 0", tableName, RecipientStandardField.Bounceload.getColumnName()));
			}
		}

		return 0;
	}

	@Override
	public boolean isColumnsIndexed(List<String> columns, int companyId) {
		return DbUtilities.checkForIndex(getDataSource(), getCustomerTableName(companyId), columns);
	}

	/**
	 * sqlStatementPartForData may not include the "select * from "-part (stripped sql select statement)
	 */
	@Override
	public int getNumberOfRecipients(int companyID, String statement, Object[] parameters) {
		String selectTotalRows;
		if (StringUtils.startsWithIgnoreCase(statement, "SELECT * FROM")) {
			selectTotalRows = StringUtils.replaceOnce(statement, "SELECT * FROM", "SELECT COUNT(*) FROM");
		} else {
			selectTotalRows = "SELECT COUNT(*) FROM " + statement;
		}

		try {
			return selectInt(logger, selectTotalRows, parameters);
		} catch (Exception e) {
			logger.error("Error occurred: " + e.getMessage(), e);
		}

		return 0;
	}

	@Override
	public int getNumberOfRecipients(int companyId, int mailingListId, String sqlConditions, Object... sqlConditionParameters) throws Exception {
		return getNumberOfRecipients(companyId, mailingListId, null, sqlConditions, sqlConditionParameters);
	}

	@Override
	public int getNumberOfRecipients(int companyId, int mailingListId, List<MediaTypes> mediaTypes, String sqlConditions, Object... sqlConditionParameters) throws Exception {
		String sqlStatement = "SELECT COUNT(DISTINCT cust.customer_id) FROM " + getCustomerTableName(companyId) + " cust, " +
				getCustomerBindingTableName(companyId) + " bind " +
				"WHERE bind.mailinglist_id = ? AND cust.customer_id = bind.customer_id AND bind.user_status = ?";
		
		if (mediaTypes != null && !mediaTypes.isEmpty()) {
			sqlStatement += " AND EXISTS (SELECT 1 FROM " + getCustomerBindingTableName(companyId)  + " bind WHERE cust.customer_id = bind.customer_id"
				+ " AND bind.user_status = " + UserStatus.Active.getStatusCode()
				+ " AND bind.mediatype in (" + mediaTypes.stream().map(x -> Integer.toString(x.getMediaCode())).collect(Collectors.joining(", ")) + "))";
		}
		
		if (StringUtils.isNotBlank(sqlConditions)) {
			sqlStatement += " AND (" + sqlConditions + ")";
		}

		List<Object> sqlParameters = new ArrayList<>();

		sqlParameters.add(mailingListId);
		sqlParameters.add(UserStatus.Active.getStatusCode());
		sqlParameters.addAll(Arrays.asList(sqlConditionParameters));

		return selectInt(logger, sqlStatement, sqlParameters.toArray());
	}

	protected void setRuleOptimizerMode(Connection connection, boolean isRuleMode) throws SQLException {
		try (Statement stmt = connection.createStatement()) {
			stmt.execute("ALTER SESSION SET OPTIMIZER_MODE=" + (isRuleMode ? "RULE" : "ALL_ROWS"));
		}
	}

	/**
	 * Load complete Subscriber-Data from DB. customerID must be set first for this method.
	 *
	 * Hide sign was introduced in a later version. So all normal internal actions shall not obey the hide sign. Only GUI functions are meant to exclude hidden customers.
	 */
	@Override
	public CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID) {
		return getCustomerDataFromDb(companyID, customerID, false);
	}

	@Override
	public CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID, final DateFormat dateFormat) {
		return getCustomerDataFromDb(companyID, customerID, false, dateFormat);
	}

	/**
	 * Load selective Subscriber data from DB. customerID must be set first for this method.
	 *
	 * @return Map with Key/Value-Pairs of customer data
	 */
	@Override
	public CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID, Collection<String> columns) {
		return getCustomerDataFromDb(companyID, customerID, columns, false, new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT));
	}

	/**
	 * Load complete Subscriber-Data from DB. customerID must be set first for this method.
	 *
	 * @return Map with Key/Value-Pairs of customer data
	 */
	@Override
	public CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID, boolean respectHideSignIfSet) {
		return getCustomerDataFromDb(companyID, customerID, Collections.emptySet(), respectHideSignIfSet, new SimpleDateFormat(DateUtilities.ISO_8601_DATETIME_FORMAT));
	}

	private CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID, boolean respectHideSignIfSet, final DateFormat dateFormat) {
		return getCustomerDataFromDb(companyID, customerID, Collections.emptySet(), respectHideSignIfSet, dateFormat);
	}

	private CaseInsensitiveMap<String, Object> getCustomerDataFromDb(int companyID, int customerID, Collection<String> columns, boolean respectHideSignIfSet, final DateFormat dateFormat) {
		String additionalWhereClause = " AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0";
		if (respectHideSignIfSet) {
			boolean respectHideSign = configService.getBooleanValue(ConfigValue.RespectHideDataSign, companyID);
			if (respectHideSign) {
				additionalWhereClause += " AND (hide <= 0 OR hide IS NULL)";
			}
		}

		Recipient customer = this.recipientFactory.newRecipient();
		customer.setCompanyID(companyID);

		Map<String, Object> customerParameters = customer.getCustParameters();
		if (customerParameters == null) {
			customerParameters = new CaseInsensitiveMap<>();
			customer.setCustParameters(customerParameters);
		}

		String sql = "SELECT " + (CollectionUtils.isEmpty(columns) ? "*" : StringUtils.join(columns, ", ")) +
				" FROM " + getCustomerTableName(companyID) +
				" WHERE customer_id = ?" + additionalWhereClause;

		try {
			List<Map<String, Object>> result = select(logger, sql, customerID);

			if (result.size() > 0) {
				CaseInsensitiveSet dateColumns = new CaseInsensitiveSet();
				CaseInsensitiveSet dateTimeColumns = new CaseInsensitiveSet();
				List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(customer.getCompanyID());
				CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));
				
				for (RecipientFieldDescription availableProfileField : recipientFields) {
					if (SimpleDataType.Date == availableProfileField.getSimpleDataType()) {
						dateColumns.add(availableProfileField.getColumnName());
					} else if (SimpleDataType.DateTime == availableProfileField.getSimpleDataType()) {
						dateTimeColumns.add(availableProfileField.getColumnName());
					}
				}

				Map<String, Object> row = result.get(0);
				for (Map.Entry<String, Object> e : row.entrySet()) {
					String columnName = e.getKey();
					Object value = e.getValue();

					if (!recipientFieldsMap.containsKey(columnName)) {
						continue;
					}

					if (dateColumns.contains(columnName) || dateTimeColumns.contains(columnName)) {
						if (value == null) {

							Map<String, String> dateColumnEmptyValues = SUPPLEMENTAL_DATE_COLUMN_SUFFIXES.stream()
									.map(suffix -> columnName + suffix)
									.collect(Collectors.toMap(Function.identity(), pair -> ""));

							dateColumnEmptyValues.put(columnName, "");
							customerParameters.putAll(dateColumnEmptyValues);
						} else {
							GregorianCalendar calendar = new GregorianCalendar();
							calendar.setTime((Date) value);
							customerParameters.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(GregorianCalendar.DAY_OF_MONTH)));
							customerParameters.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(GregorianCalendar.MONTH) + 1));
							customerParameters.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(GregorianCalendar.YEAR)));
							customerParameters.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(calendar.get(GregorianCalendar.HOUR_OF_DAY)));
							customerParameters.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(calendar.get(GregorianCalendar.MINUTE)));
							customerParameters.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(calendar.get(GregorianCalendar.SECOND)));
							customerParameters.put(columnName, dateFormat.format(calendar.getTime()));
						}
					} else {
						if (value == null) {
							customerParameters.put(columnName, "");
						} else if (SimpleDataType.Float == recipientFieldsMap.get(columnName).getSimpleDataType()) {
							customerParameters.put(columnName, AgnUtils.stripTrailingZeros(value.toString()));
						} else {
							customerParameters.put(columnName, value.toString());
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("getCustomerDataFromDb: " + sql, e);
		}

		customer.setChangeFlag(false);

		if (customerParameters instanceof CaseInsensitiveMap) {
			return (CaseInsensitiveMap<String, Object>) customerParameters;
		} else {
			return new CaseInsensitiveMap<>(customerParameters);
		}
	}

    @Override
    public Map<String, Object> getRecipientData(int companyId, int recipientId, boolean respectHideSignIfSet) {
        String additionalWhereClause = " AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0";
		if (respectHideSignIfSet) {
			boolean respectHideSign = configService.getBooleanValue(ConfigValue.RespectHideDataSign, companyId);
			if (respectHideSign) {
				additionalWhereClause += " AND (hide <= 0 OR hide IS NULL)";
			}
		}
		// Using "SELECT * ...", because of flexible columns
		String sql = "SELECT * FROM " + getCustomerTableName(companyId) +
				" WHERE customer_id = ?" + additionalWhereClause;

		Map<String, Object> recipientData = new CaseInsensitiveMap<>();
		List<Map<String, Object>> result = select(logger, sql, recipientId);
		if (!result.isEmpty()) {
			recipientData.putAll(result.get(0));
		}

		return recipientData;
	}

	/**
	 * Find Subscriber by providing a column-name and a value and an customer object. Fills the customer_id of this customer object if found. Only exact matches possible.
	 *
	 * @return customerID or 0 if no matching record found
	 * @param keyColumn
	 *			Column-Name
	 * @param value
	 *			Value to search for in col
	 */
	@Override
	public int findByKeyColumn(Recipient customer, String keyColumn, String value) {
		try {
			RecipientFieldDescription keyColumnDescription = recipientFieldService.getRecipientField(customer.getCompanyID(), keyColumn);
			if (keyColumnDescription != null) {
				List<Map<String, Object>> custList;
				if (keyColumnDescription.getSimpleDataType() == SimpleDataType.Characters) {
					if ("email".equalsIgnoreCase(keyColumn) && configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, customer.getCompanyID())) {
						custList = select(logger, "SELECT customer_id FROM " + getCustomerTableName(customer.getCompanyID()) + " cust WHERE cust." + SafeString.getSafeDbColumnName(keyColumn) + " = ? AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0", value);
					} else {
						custList = select(logger, "SELECT customer_id FROM " + getCustomerTableName(customer.getCompanyID()) + " cust WHERE LOWER(cust." + SafeString.getSafeDbColumnName(keyColumn) + ") = ? AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0", value == null ? null : value.toLowerCase());
					}
				} else {
					if (AgnUtils.isNumber(value)) {
						int intValue = Integer.parseInt(value);
						custList = select(logger, "SELECT customer_id FROM " + getCustomerTableName(customer.getCompanyID()) + " cust WHERE cust." + SafeString.getSafeDbColumnName(keyColumn) + " = ? AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0", intValue);
					} else {
						throw new Exception("Invalid search value for numeric key column: " + value);
					}
				}

				// cannot use queryForInt, because of possible existing duplicates
				if (custList != null && custList.size() > 0) {
					customer.setCustomerID(((Number) custList.get(0).get("customer_id")).intValue());
				} else {
					customer.setCustomerID(0);
				}
			}
		} catch (Exception e) {
			logger.error("Error in ComRecipientDaoImpl.findByKeyColumn()", e);
			customer.setCustomerID(0);
		}
		return customer.getCustomerID();
	}

	@Override
	public int findByColumn(int companyID, String keyColumn, String value) {
		Recipient customer = recipientFactory.newRecipient();
		customer.setCompanyID(companyID);
		return findByKeyColumn(customer, keyColumn, value);
	}

	/**
	 * Find Subscriber by providing a username and password. Only exact matches possible.
	 *
	 * @return customerID or 0 if no matching record found
	 * @param keyColumn name of key column
	 * @param keyColumnValue value of key column
	 * @param passwordColumn name of profile field containing the password
	 * @param passwordColumnValue password
	 */
	@Override
	public int findByUserPassword(int companyID, String keyColumn, String keyColumnValue, String passwordColumn, String passwordColumnValue) {
		if (StringUtils.equalsIgnoreCase(keyColumn, "email")) {
			keyColumnValue = keyColumnValue.toLowerCase();
		}

		String sql = "SELECT customer_id FROM " + getCustomerTableName(companyID) + " cust WHERE cust." + SafeString.getSafeDbColumnName(keyColumn) + " = ? AND cust." + SafeString.getSafeDbColumnName(passwordColumn) + " = ?";

		try {
			return selectInt(logger, sql, keyColumnValue, passwordColumnValue);
		} catch (@SuppressWarnings("unused") Exception e) {
			return 0;
		}
	}

	@Override
	public boolean isMailtrackingEnabled(int companyID) {
		String query = "SELECT mailtracking FROM company_tbl WHERE company_id = ?";
		int mailtracking = selectInt(logger, query, companyID);
		return mailtracking != 0;
	}

    @Override
    public List<ComRecipientMailing> getMailingsDeliveredToRecipient(int customerID, int companyID) {
    	if(!isMailtrackingEnabled(companyID)) {
    		return new ArrayList<>();
    	}
    	
       	final String sql = String.format(" SELECT "
       			+ "   track.mailing_id, "
       			+ "   MAX(track.timestamp) AS send_date, "
       			+ "   count(succ.timestamp) as send_count, "
       			+ "   mail.shortname AS shortname, "
       			+ "   mail.mailing_type as mailing_type, "
       			+ "   mt.param AS mt_param, "
       			+ "   succ.timestamp AS delivery_date, "
       			+ "   opl.openings AS openings, "
       			+ "   rlog.clicks AS clicks "
       			+ "FROM "
       			+ "   mailtrack_%1$d_tbl track "
       			+ "   JOIN mailing_tbl mail ON mail.mailing_id = track.mailing_id "
       			+ "   JOIN mailing_mt_tbl mt ON mt.mailing_id = track.mailing_id AND mt.mediatype IN (0,4) AND (mt.mediatype = track.mediatype OR track.mediatype IS NULL)"
       			+ "   LEFT OUTER JOIN (select mailing_id, max(timestamp) timestamp from success_%1$d_tbl where customer_id = ? group by mailing_id) succ ON succ.mailing_id = track.mailing_id "
       			+ "   LEFT OUTER JOIN (select mailing_id,COUNT(DISTINCT creation) AS openings from onepixellog_device_%1$d_tbl where customer_id = ? group by mailing_id) opl ON opl.mailing_id = track.mailing_id "
       			+ "    LEFT OUTER JOIN (select mailing_id, COUNT(DISTINCT timestamp) AS clicks from rdirlog_%1$d_tbl where customer_id = ? group by mailing_id) rlog ON rlog.mailing_id = track.mailing_id "
       			+ " WHERE "
       			+ "   track.customer_id = ? "
       			+ "GROUP BY "
       			+ "   track.mailing_id, mail.shortname, mail.mailing_type, mt.param, succ.timestamp, opl.openings, rlog.clicks "
       			+ "ORDER BY "
       			+ "   send_date", companyID);
        	
    	
            return select(logger, sql, RecipientMailingRowMapper.INSTANCE, customerID, customerID, customerID, customerID);
    }

    @Override
    public List<Map<String, Object>> getRecipientClicksHistory(int recipientId, int mailingId, int companyId) {
        if (!isMailtrackingEnabled(companyId)) {
            return new ArrayList<>();
        }
       	String sql = "SELECT url.full_url, COUNT(DISTINCT log.timestamp) AS count, MAX(log.timestamp) AS last_time" +
                " FROM rdir_url_tbl url JOIN rdirlog_" + companyId + "_tbl log ON url.url_id = log.url_id" +
                " WHERE log.customer_id = ? AND log.mailing_id = ? GROUP BY url.full_url";
        return select(logger, sql, recipientId, mailingId);
    }

	private static class RecipientMailingRowMapper implements RowMapper<ComRecipientMailing> {
		
		public static final RecipientMailingRowMapper INSTANCE = new RecipientMailingRowMapper();
		
		@Override
		public ComRecipientMailing mapRow(ResultSet resultSet, int i) throws SQLException {
			ComRecipientMailing recipientMailing = new ComRecipientMailingImpl();
			recipientMailing.setMailingId(resultSet.getInt("mailing_id"));
			recipientMailing.setShortName(resultSet.getString("shortname"));
			
			String mailingMtParameter = resultSet.getString("mt_param");
			recipientMailing.setSubject(mailingMtParameter != null ? new ParameterParser(mailingMtParameter).parse("subject") : "");
			
			try {
				recipientMailing.setMailingType(MailingType.fromCode(resultSet.getInt("mailing_type")));
			} catch (@SuppressWarnings("unused") Exception e) {
				throw new SQLException("Invalid MailingType code: " + resultSet.getInt("mailing_type"));
			}
			recipientMailing.setSendDate(resultSet.getTimestamp("send_date"));
			recipientMailing.setDeliveryDate(resultSet.getTimestamp("delivery_date"));
			recipientMailing.setNumberOfOpenings(resultSet.getInt("openings"));
			recipientMailing.setNumberOfClicks(resultSet.getInt("clicks"));
			recipientMailing.setSendCount(resultSet.getInt("send_count"));
			
			return recipientMailing;
		}
	}

	/**
	 * Read all changes of recipient data in chronological order from old to young
	 */
	@Override
	public List<ComRecipientHistory> getRecipientProfileHistory(int recipientID, int companyID) {
		try {
			return recipientProfileHistoryService.listProfileFieldHistory(recipientID, companyID);
		} catch(@SuppressWarnings("unused") ProfileFieldHistoryFeatureNotEnabledException e) {
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Profile field history feature not enabled for company ID %d", companyID));
			}
		
			return new ArrayList<>();
		} catch(Exception e) {
			logger.error(String.format("Error reading profile field history for company %d, recipient %d", companyID, recipientID), e);
		
			return new ArrayList<>();
		}
	}

	@Override
	public List<ComRecipientReaction> getRecipientReactionsHistory(int recipientID, int companyID) {
		logger.info("Get recipient web tracking history is unsupported.");
		return new ArrayList<>();
	}

	@Override
	public PaginatedListImpl<ComRecipientReaction> getRecipientReactionsHistory(int recipientID, int companyID, int pageNumber, int rowsPerPage, String sortCriterion, boolean sortAscending) {
		logger.info("Get recipient reactions history is unsupported.");
		return new PaginatedListImpl<>();
	}

	/**
	 * Read changes of mailing list bindings ordered by mailinglistID and mediatype and within that order from old to young
	 * Converting result set items to list of history POJOs. POJO contains records of both the previous and the current items of result set.
	 * @param recipientID an id of the customer which history should be collected.
	 * @param companyID an id of the company that owns a referenced customer.
	 * @return an ordered list of entities representing changes history.
	 */
	@Override
	public List<ComRecipientHistory> getRecipientBindingHistory(int recipientID, int companyID) {
		boolean companyHasBindingHistory = DbUtilities.checkIfTableExists(getDataSource(), "hst_customer_" + companyID + "_binding_tbl");
		List<ComRecipientHistory> comRecipientHistories = new ArrayList<>();
		
		// Create history entries for existing binding entries
		List<Map<String, Object>> allCurrentBindings = select(logger, "SELECT bind.mailinglist_id, bind.mediatype, bind.user_type, bind.user_status, bind.user_remark, bind.exit_mailing_id, bind.mediatype, customer.email, bind.creation_date"
			+ " FROM " + getCustomerBindingTableName(companyID) + " bind INNER JOIN " + getCustomerTableName(companyID) + " customer ON bind.customer_id = customer.customer_id"
			+ " WHERE bind.customer_id = ?"
			+ " ORDER BY bind.mailinglist_id, bind.mediatype",
			recipientID);
		
		for (Map<String, Object> bindingEntry : allCurrentBindings) {
			int mailinglistID = ((Number) bindingEntry.get("mailinglist_id")).intValue();
			List<Map<String, Object>> mailingListData = select(logger, "SELECT shortname, change_date, deleted FROM mailinglist_tbl WHERE mailinglist_id = ?", mailinglistID);
			String mailinglistName;
			Date mailinglistChangeDate;
			boolean mailinglistIsDeleted;
			if (mailingListData.size() == 1) {
				mailinglistName = "\"" + mailingListData.get(0).get("shortname") + "\" (ID: " + mailinglistID + ")";
				mailinglistChangeDate = (Date) mailingListData.get(0).get("change_date");
				mailinglistIsDeleted = ((Number) mailingListData.get(0).get("deleted")).intValue() > 0;
			} else {
				mailinglistName = "Deleted Mailinglist (ID: " + mailinglistID + ")";
				mailinglistChangeDate = new Date(0);
				mailinglistIsDeleted = true;
			}
			int mediatype = ((Number) bindingEntry.get("mediatype")).intValue();
			
			List<Map<String, Object>> bindingChanges = null;
			if (companyHasBindingHistory) {
				bindingChanges = select(logger, "SELECT *"
				+ " FROM hst_customer_" + companyID + "_binding_tbl"
				+ " WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?"
				+ " ORDER BY timestamp_change ASC",
				recipientID, mailinglistID, mediatype);
			}
			
			if (bindingChanges == null || bindingChanges.isEmpty()) {
				// Add the initial values, if there is no history for this binding entry
				comRecipientHistories.addAll(createChangeHistoryEntries(null, bindingEntry, mailinglistName, mediatype));
			} else {
				// Add all changes for this binding entry
				Iterator<Map<String, Object>> iterator = bindingChanges.iterator();
				Map<String, Object> previous = iterator.next();
				while (iterator.hasNext()) {
					Map<String, Object> next = iterator.next();
					comRecipientHistories.addAll(createChangeHistoryEntries(previous, next, mailinglistName, mediatype));
					previous = next;
				}
				comRecipientHistories.addAll(createChangeHistoryEntries(previous, bindingEntry, mailinglistName, mediatype));
			}
			
			if (mailinglistIsDeleted) {
				ComRecipientHistory recipientHistoryDeletedMailinglist = new ComRecipientHistoryImpl();
				recipientHistoryDeletedMailinglist.setChangeDate(mailinglistChangeDate);
				recipientHistoryDeletedMailinglist.setFieldName(ComRecipientHistory.MAILINGLIST_DELETED);
				recipientHistoryDeletedMailinglist.setMailingList(mailinglistName);
				comRecipientHistories.add(recipientHistoryDeletedMailinglist);
			}
		}
		
		// Create history entries for existing changes of already deleted binding entries
		List<Map<String, Object>> deletedBindingsData = select(logger, "SELECT DISTINCT hst.mailinglist_id, hst.mediatype"
			+ " FROM hst_customer_" + companyID + "_binding_tbl hst"
			+ " WHERE hst.customer_id = ?"
			+ " AND NOT EXISTS (SELECT 1 FROM " + getCustomerBindingTableName(companyID) + " bind WHERE bind.customer_id = hst.customer_id AND bind.mailinglist_id = hst.mailinglist_id AND bind.mediatype = hst.mediatype)",
			recipientID);
		
		for (Map<String, Object> deletedBinding : deletedBindingsData) {
			int mailinglistID = ((Number) deletedBinding.get("mailinglist_id")).intValue();
			List<Map<String, Object>> mailingListData = select(logger, "SELECT shortname, change_date, deleted FROM mailinglist_tbl WHERE mailinglist_id = ?", mailinglistID);
			String mailinglistName;
			Date mailinglistChangeDate;
			boolean mailinglistIsDeleted;
			if (mailingListData.size() == 1) {
				mailinglistName = "\"" + mailingListData.get(0).get("shortname") + "\" (ID: " + mailinglistID + ")";
				mailinglistChangeDate = (Date) mailingListData.get(0).get("change_date");
				mailinglistIsDeleted = ((Number) mailingListData.get(0).get("deleted")).intValue() > 0;
			} else {
				mailinglistName = "Deleted Mailinglist (ID: " + mailinglistID + ")";
				mailinglistChangeDate = new Date(0);
				mailinglistIsDeleted = true;
			}
			int mediatype = ((Number) deletedBinding.get("mediatype")).intValue();
			
			List<Map<String, Object>> bindingChanges = select(logger, "SELECT *"
					+ " FROM hst_customer_" + companyID + "_binding_tbl"
					+ " WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?"
					+ " ORDER BY timestamp_change ASC",
					recipientID, mailinglistID, mediatype);
			
			Iterator<Map<String, Object>> iterator = bindingChanges.iterator();
			Map<String, Object> previous = iterator.next();
			while (iterator.hasNext()) {
				Map<String, Object> next = iterator.next();
				comRecipientHistories.addAll(createChangeHistoryEntries(previous, next, mailinglistName, mediatype));
				previous = next;
			}
			ComRecipientHistory recipientHistoryDeletedBinding = new ComRecipientHistoryImpl();
			recipientHistoryDeletedBinding.setChangeDate((Date) previous.get("timestamp_change"));
			recipientHistoryDeletedBinding.setFieldName(ComRecipientHistory.CUSTOMER_BINDING_DELETED);
			recipientHistoryDeletedBinding.setMailingList(mailinglistName);
			comRecipientHistories.add(recipientHistoryDeletedBinding);
			
			if (mailinglistIsDeleted) {
				ComRecipientHistory recipientHistoryDeletedMailinglist = new ComRecipientHistoryImpl();
				recipientHistoryDeletedMailinglist.setChangeDate(mailinglistChangeDate);
				recipientHistoryDeletedMailinglist.setFieldName(ComRecipientHistory.MAILINGLIST_DELETED);
				recipientHistoryDeletedMailinglist.setMailingList(mailinglistName);
				comRecipientHistories.add(recipientHistoryDeletedMailinglist);
			}
		}
		
		return comRecipientHistories;
	}
	
	private List<ComRecipientHistory> createChangeHistoryEntries(Map<String, Object> previous, Map<String, Object> next, String mailinglistName, int mediatype) {
		ArrayList<ComRecipientHistory> comRecipientHistories = new ArrayList<>();
		
		if (previous == null) {
			Date changeDate = (Date) next.get("creation_date");
			
			ComRecipientHistory recipientHistoryUserType = new ComRecipientHistoryImpl();
			recipientHistoryUserType.setChangeDate(changeDate);
			recipientHistoryUserType.setFieldName(ComRecipientHistory.USER_TYPE);
			recipientHistoryUserType.setMailingList(mailinglistName);
			recipientHistoryUserType.setMediaType(mediatype);
			recipientHistoryUserType.setOldValue("");
			recipientHistoryUserType.setNewValue(next.get(ComRecipientHistory.USER_TYPE));
			comRecipientHistories.add(recipientHistoryUserType);

			ComRecipientHistory recipientHistoryUserStatus = new ComRecipientHistoryImpl();
			recipientHistoryUserStatus.setChangeDate(changeDate);
			recipientHistoryUserStatus.setFieldName(ComRecipientHistory.USER_STATUS);
			recipientHistoryUserStatus.setMailingList(mailinglistName);
			recipientHistoryUserStatus.setMediaType(mediatype);
			recipientHistoryUserStatus.setOldValue(0);
			recipientHistoryUserStatus.setNewValue(next.get(ComRecipientHistory.USER_STATUS));
			comRecipientHistories.add(recipientHistoryUserStatus);

			ComRecipientHistory recipientHistoryRemark = new ComRecipientHistoryImpl();
			recipientHistoryRemark.setChangeDate(changeDate);
			recipientHistoryRemark.setFieldName(ComRecipientHistory.USER_REMARK);
			recipientHistoryRemark.setMailingList(mailinglistName);
			recipientHistoryRemark.setMediaType(mediatype);
			recipientHistoryRemark.setOldValue("");
			recipientHistoryRemark.setNewValue(next.get(ComRecipientHistory.USER_REMARK));
			comRecipientHistories.add(recipientHistoryRemark);
		} else {
			Date changeDate = (Date) previous.get("timestamp_change");
	
			for (String mailinglistValueField : MAILINGLIST_VALUE_FIELDS) {
				// Change of email is not relevant for mailingListBindings
				if (!ComRecipientHistory.EMAIL.equals(mailinglistValueField)) {
					Object previousValue = previous.get(mailinglistValueField);
					Object nextValue = next.get(mailinglistValueField);
				
					if (previousValue instanceof Long) {
						previousValue = ((Number) previousValue).intValue();
					}
				
					if (nextValue instanceof Long) {
						nextValue = ((Number) nextValue).intValue();
					}
	
					if ((previousValue == null && nextValue != null) || (previousValue != null && !previousValue.equals(nextValue))) {
						ComRecipientHistory recipientHistory = new ComRecipientHistoryImpl();
						recipientHistory.setChangeDate(changeDate);
						recipientHistory.setFieldName(mailinglistValueField);
						recipientHistory.setOldValue(previousValue);
						recipientHistory.setNewValue(nextValue);
						recipientHistory.setMailingList(mailinglistName);
						recipientHistory.setMediaType(mediatype);
						comRecipientHistories.add(recipientHistory);
					}
				}
			}
		}
		
		return comRecipientHistories;
	}

	protected String buildCustomerTimestamp(Recipient customer, String fieldName) throws Exception {
		try {
			int day = Integer.parseInt(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY));
			int month = Integer.parseInt(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH));
			int year = Integer.parseInt(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR));
			int hour = NumberUtils.toInt(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR));
			int minute = NumberUtils.toInt(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE));
			int second = NumberUtils.toInt(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND));

			if (isOracleDB()) {
				return DbUtilities.getToDateString_Oracle(day, month, year, hour, minute, second);
			} else {
				return DbUtilities.getToDateString_MySQL(day, month, year, hour, minute, second);
			}
		} catch (Exception e) {
			throw new Exception("Invalid date data for field '" + fieldName + "'", e);
		}
	}

	private static boolean hasTripleDateParameter(Recipient customer, String fieldName) {
		return customer.hasCustParameter(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY)
				&& customer.hasCustParameter(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH)
				&& customer.hasCustParameter(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR);
	}

	private SqlPreparedInsertStatementManager prepareInsertStatement(CaseInsensitiveMap<String, RecipientFieldDescription> customerTableStructure, Recipient customer, boolean withEmptyParameters) throws Exception {
		SqlPreparedInsertStatementManager insertStatementManager = new SqlPreparedInsertStatementManager("INSERT INTO " + getCustomerTableName(customer.getCompanyID()));
		insertStatementManager.addValue("creation_date", "CURRENT_TIMESTAMP", true);
		insertStatementManager.addValue("timestamp", "CURRENT_TIMESTAMP", true);
		
		for (Entry<String, RecipientFieldDescription> entry : customerTableStructure.entrySet()) {
			String fieldName = entry.getKey();
			RecipientFieldDescription profileField = entry.getValue();
			SimpleDataType columnType = profileField.getSimpleDataType();
		
			if (fieldName.equalsIgnoreCase("customer_id")) {
				// customer_id is set in a special way
			} else if (fieldName.equalsIgnoreCase("creation_date")
					|| fieldName.equalsIgnoreCase("timestamp")
					|| fieldName.equalsIgnoreCase("change_date")) {
				// Field is a system timestamp field, which is set in a special way
			} else if ("mailtype".equalsIgnoreCase(fieldName)) {
				// Default value for mailtype is HTML
				final String value = customer.getCustParametersNotNull(fieldName);
				if (StringUtils.isNotBlank(value)) {
					// Check for valid mailtype code
					int mailtypeCode = NumericUtil.tryParseDouble(value, MailType.HTML.getIntValue()).intValue();
					MailType mailtype = MailType.getFromInt(mailtypeCode);
					insertStatementManager.addValue(fieldName, mailtype.getIntValue());
				} else {
					insertStatementManager.addValue(fieldName, MailType.HTML.getIntValue());
				}
			} else if (SimpleDataType.Date == columnType || SimpleDataType.DateTime == columnType) {
				if (hasTripleDateParameter(customer, fieldName)) {
					// Customer table has a timestamp field, which is split into 3 or 6 separate fields (day, month, year) or (day, month, year, hour, minute, second)
					if (StringUtils.isNotBlank(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY))) {
						insertStatementManager.addValue(fieldName, buildCustomerTimestamp(customer, fieldName), true);
					}
				} else {
                    Object dateObj = customer.getCustParameters().get(fieldName);
                    if (dateObj == null || StringUtils.isBlank(dateObj.toString())) {
						// Simple date field
						// Only default values are filled, other values must be set as split timestamp (fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY ...)
						String defaultValue = profileField.getDefaultValue();
						if (StringUtils.isNotBlank(defaultValue)) {
							insertStatementManager.addValue(fieldName, createDateDefaultValueExpression(defaultValue), true);
						}
					} else if (StringUtils.isNotBlank(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT))) {
						Date date;
						try {
							SimpleDateFormat format = new SimpleDateFormat(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT));
							format.setLenient(false);
							date = format.parse(customer.getCustParametersNotNull(fieldName));
						} catch (@SuppressWarnings("unused") ParseException e) {
							throw new Exception("Invalid value for customer field '" + entry.getKey() + "' with expected format '" + (customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT)) + "'");
						}
						insertStatementManager.addValue(fieldName, date);
					} else {
                        Object dateValue = dateObj instanceof String ? tryParseIso8601DateTimeStr(fieldName, dateObj.toString()) : dateObj;
                        insertStatementManager.addValue(fieldName, dateValue, false);
					}
				}
			} else {
				String value = customer.getCustParametersNotNull(fieldName);
				if (fieldName.equalsIgnoreCase("email")) {
					if (!configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, customer.getCompanyID())) {
						value = AgnUtils.normalizeEmail(value);
					}
				} else if (fieldName.equalsIgnoreCase("datasource_id")) {
					logger.trace("Prepare insert. New datasourceID = " + value + " for recipient with email " + customer.getEmail());
				}
			
				if (StringUtils.isEmpty(value)) {
					value = profileField.getDefaultValue();
					if (StringUtils.isBlank(value)) {
						if (withEmptyParameters) {
							//don't miss any parameter - for batch processing
							insertStatementManager.addValue(fieldName, null);
							if (fieldName.equalsIgnoreCase("datasource_id")) {
								logger.trace("Prepare insert. Adding empty datasourceID for recipient with email " + customer.getEmail());
							}
						}
						continue;
					}
				}
				if (SimpleDataType.Numeric == columnType) {
					insertStatementManager.addValue(fieldName, NumberUtils.toInt(value.trim(), 0));
					if (fieldName.equalsIgnoreCase("datasource_id")) {
						logger.trace("Prepare insert. Adding INTEGER datasourceID for recipient with email " + customer.getEmail());
					}
				} else if (SimpleDataType.Float == columnType) {
					insertStatementManager.addValue(fieldName, NumericUtil.tryParseDouble(value, 0));
				} else { // if (columnType.equalsIgnoreCase("VARCHAR") || columnType.equalsIgnoreCase("CHAR")) {
					insertStatementManager.addValue(fieldName, "".equals(value) ? null : value);		// Make "" to null (-> EMM-4948)
				}
			}
		}

		return insertStatementManager;
	}

    private Date tryParseIso8601DateTimeStr(String fieldName, String valueStr) throws Exception {
        try {
            return DateUtilities.parseIso8601DateTimeString(valueStr);
        } catch (@SuppressWarnings("unused") ParseException e) {
            throw new Exception("Invalid date value for field '" + fieldName + "': " + valueStr);
        }
    }

	private SqlPreparedUpdateStatementManager prepareUpdateStatement(CaseInsensitiveMap<String, RecipientFieldDescription> customerTableStructure, Recipient customer, boolean missingFieldsToNull) throws Exception {
		SqlPreparedUpdateStatementManager updateStatementManager = new SqlPreparedUpdateStatementManager("UPDATE " + getCustomerTableName(customer.getCompanyID()) + " SET ");

		updateStatementManager.addValue("timestamp", "CURRENT_TIMESTAMP", true);

		for (Entry<String, RecipientFieldDescription> entry : customerTableStructure.entrySet()) {
			String fieldName = entry.getKey();
			RecipientFieldDescription profileField = entry.getValue();
			SimpleDataType columnType = profileField.getSimpleDataType();
	
			if (fieldName.equalsIgnoreCase("customer_id") || fieldName.equalsIgnoreCase("change_date") || fieldName.equalsIgnoreCase("timestamp") || fieldName.equalsIgnoreCase("creation_date")) {
				// Do not update these special fields
			} else if ("mailtype".equalsIgnoreCase(fieldName)) {
				// Only change mailtype if a value is given
				final String value = customer.getCustParametersNotNull(fieldName);
				if (StringUtils.isNotBlank(value)) {
					// Check for valid mailtype code
					int mailtypeCode = NumericUtil.tryParseDouble(value, MailType.HTML.getIntValue()).intValue();
					MailType mailtype = MailType.getFromInt(mailtypeCode);
					updateStatementManager.addValue(fieldName, mailtype.getIntValue());
				}
			} else if (fieldName.equalsIgnoreCase("datasource_id")) {
				// Only update datasource_id if it is not set yet
				if (customer.hasCustParameter("DATASOURCE_ID")) {
					int datasourceID = selectIntWithDefaultValue(logger, "SELECT datasource_id FROM " + getCustomerTableName(customer.getCompanyID()) + " WHERE customer_id = ?", -1, customer.getCustomerID());
					logger.trace("Prepare update. Existing datasourceID = " + datasourceID + " for recipient with email " + customer.getEmail());
					if (datasourceID <= 0) {
						String value = customer.getCustParametersNotNull("DATASOURCE_ID");
						logger.trace("Prepare update. New datasourceID = " + value + " for recipient with email " + customer.getEmail());
						if (StringUtils.isNotEmpty(value) && AgnUtils.isNumber(value)) {
							logger.trace("Prepare update. Adding for recipient with email " + customer.getEmail());
							updateStatementManager.addValue(fieldName, Integer.parseInt(value));
						}
					}
				}
			} else if (SimpleDataType.Date == columnType || SimpleDataType.DateTime == columnType) {
				if (hasTripleDateParameter(customer, fieldName)) {
					// Customer table has a timestamp field, which is split into 3 or 6 separate fields (day, month, year) or (day, month, year, hour, minute, second)
					if (StringUtils.isNotBlank(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY))) {
						updateStatementManager.addValue(fieldName, buildCustomerTimestamp(customer, fieldName), true);
					} else {
						if (!customer.hasCustParameter(fieldName) && !missingFieldsToNull) {
							continue;
						} else {
							updateStatementManager.addValue(fieldName, null);
						}
					}
				} else if (customer.hasCustParameter(fieldName)) {
					if (StringUtils.isBlank(customer.getCustParametersNotNull(fieldName))) {
						updateStatementManager.addValue(fieldName, null);
					} else if (StringUtils.isNotBlank(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT))) {
						Date date;
						try {
							SimpleDateFormat format = new SimpleDateFormat(customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT));
							format.setLenient(false);
							date = format.parse(customer.getCustParametersNotNull(fieldName));
						} catch (@SuppressWarnings("unused") ParseException e) {
							throw new Exception("Invalid value for customer field '" + entry.getKey() + "' with expected format '" + (customer.getCustParametersNotNull(fieldName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_FORMAT)) + "'");
						}
						updateStatementManager.addValue(fieldName, date);
					} else {
						Date date;
						try {
							date = DateUtilities.parseIso8601DateTimeString(customer.getCustParametersNotNull(fieldName));
						} catch (@SuppressWarnings("unused") ParseException e) {
							throw new Exception("Invalid date value for field '" + fieldName + "': " + customer.getCustParametersNotNull(fieldName));
						}
						updateStatementManager.addValue(fieldName, date);
					}
				} else {
					if (!missingFieldsToNull) {
						continue;
					} else {
						updateStatementManager.addValue(fieldName, null);
					}
				}
			} else {
				if (!customer.hasCustParameter(fieldName) && !missingFieldsToNull) {
					continue;
				}
				final String value = customer.getCustParametersNotNull(fieldName);
				if (SimpleDataType.Numeric == columnType) {
					updateStatementManager.addValue(fieldName, StringUtils.isEmpty(value) ? null : NumericUtil.tryParseDouble(value, 0).intValue());
				} else if (SimpleDataType.Float == columnType) {
					updateStatementManager.addValue(fieldName, StringUtils.isEmpty(value) ? null : NumericUtil.tryParseDouble(value, 0));
				} else { // if (columnType.equalsIgnoreCase("VARCHAR") || columnType.equalsIgnoreCase("CHAR")) {
					updateStatementManager.addValue(fieldName, StringUtils.isEmpty(value) ? null : value);
				}
			}
		}

		updateStatementManager.addWhereClause("customer_id = ?", customer.getCustomerID());

		return updateStatementManager;
	}

	/**
	 * Update or Insert a list of customers
	 * Returns a list of inserted new customerid or updated customerid per customer of list
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public List<Object> insertCustomers(int companyID, List<Recipient> recipients, List<Boolean> doubleCheck, List<Boolean> overwrite, List<String> keyFields) {
		try {
			List<Object> results = new ArrayList<>();
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
			CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));
			
			String currentSqlStatement = null;
			List<Object[]> currentSqlParameters = new ArrayList<>();
			
			boolean allowHtmlTags = configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, companyID);
			
			for (int i = 0; i < recipients.size(); i++) {
				Recipient customer = recipients.get(i);
				
				for (Entry<String, Object> entry : customer.getCustParameters().entrySet()) {
					if (entry.getValue() instanceof String) {
						// Check for unallowed html content
						try {
							HtmlChecker.checkForUnallowedHtmlTags((String) entry.getValue(), allowHtmlTags);
						} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
							throw new Exception("Invalid recipient data containing HTML for recipient field: " + entry.getKey());
						}
					}
				}
				
				if (companyID != customer.getCompanyID()) {
					results.add(false);
					continue;
				}
				
				String nextSqlStatement;
				Object[] nextParameters;
				
				if (!doubleCheck.get(i)) {
					// Insert without duplicate check
					SqlPreparedInsertStatementManager insertStatementManager = prepareInsertStatement(recipientFieldsMap, customer, false);
					if (isOracleDB()) {
						int customerID = selectInt(logger, "SELECT customer_" + customer.getCompanyID() + "_tbl_seq.NEXTVAL FROM DUAL");
						insertStatementManager.addValue("customer_id", customerID);
						results.add(customerID);
					}
					nextSqlStatement = insertStatementManager.getPreparedSqlString();
					nextParameters = insertStatementManager.getPreparedSqlParameters();
				} else {
					// Insert with duplicate check
					String keyField = keyFields.get(i);
					String keyValue = (String) customer.getCustParameters().get(keyField);
					if (StringUtils.isEmpty(keyValue)) {
						// Duplicate check key is empty
						SqlPreparedInsertStatementManager insertStatementManager = prepareInsertStatement(recipientFieldsMap, customer, false);
						if (isOracleDB()) {
							int customerID = selectInt(logger, "SELECT customer_" + customer.getCompanyID() + "_tbl_seq.NEXTVAL FROM DUAL");
							insertStatementManager.addValue("customer_id", customerID);
							results.add(customerID);
						}
						nextSqlStatement = insertStatementManager.getPreparedSqlString();
						nextParameters = insertStatementManager.getPreparedSqlParameters();
					} else {
						// Check, if profilefield from parameter "keyField" exists
						if (!recipientFieldsMap.containsKey(keyField)) {
							throw new Exception ("Invalid profilefield " + keyField + " for company " + companyID);
						}
						
						List<Integer> customerIds = select(logger, "SELECT customer_id FROM " + getCustomerTableName(companyID) +
								" WHERE " + keyField + " = ? AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0",
								IntegerRowMapper.INSTANCE, keyValue);
						int customerIdsSize = CollectionUtils.size(customerIds);
						
						nextSqlStatement = null;
						nextParameters = null;
						if (customerIdsSize == 0) {
							// No duplicate exists
							SqlPreparedInsertStatementManager insertStatementManager = prepareInsertStatement(recipientFieldsMap, customer, false);
							if (isOracleDB()) {
								int customerID = selectInt(logger, "SELECT customer_" + customer.getCompanyID() + "_tbl_seq.NEXTVAL FROM DUAL");
								insertStatementManager.addValue("customer_id", customerID);
								results.add(customerID);
							}
							nextSqlStatement = insertStatementManager.getPreparedSqlString();
							nextParameters = insertStatementManager.getPreparedSqlParameters();
						} else if (customerIdsSize > 1) {
							results.add(new InvalidDataException("Ambiguous recipient property."));
						} else if (overwrite.get(i)) {
							int customerID = customerIds.get(0);
							customer.setCustomerID(customerID);
							// Duplicate found, so update existing data
							SqlPreparedUpdateStatementManager updateStatementManager = prepareUpdateStatement(recipientFieldsMap, customer, false);
							nextSqlStatement = updateStatementManager.getPreparedSqlString();
							nextParameters = updateStatementManager.getPreparedSqlParameters();
							results.add(customerID);
						}
					}
				}
				
				if (nextSqlStatement != null) {
					// Execute bulk sql for sqlstatement group change or block size overflow
					if (currentSqlParameters.size() < 1000 && (currentSqlStatement == null || currentSqlStatement.equals(nextSqlStatement))){
						currentSqlStatement = nextSqlStatement;
						currentSqlParameters.add(nextParameters);
					} else {
						if (currentSqlStatement == null) {
							throw new Exception("currentSqlStatement was null");
						} else if (isOracleDB() || currentSqlStatement.toLowerCase().startsWith("update")) {
							batchupdate(logger, currentSqlStatement, currentSqlParameters);
						} else {
							int[] generatedKeys = batchInsertIntoAutoincrementMysqlTable(logger, currentSqlStatement, currentSqlParameters);
							for (int generatedKey : generatedKeys) {
								results.add(generatedKey);
							}
						}
						currentSqlStatement = nextSqlStatement;
						currentSqlParameters = new ArrayList<>();
						currentSqlParameters.add(nextParameters);
					}
				}
			}
			
			// Execute bulk sql for last sqlstatement block
			if (currentSqlParameters.size() > 0) {
				if (currentSqlStatement == null) {
					throw new Exception("currentSqlStatement was null");
				} else if (isOracleDB() || currentSqlStatement.toLowerCase().startsWith("update")) {
					batchupdate(logger, currentSqlStatement, currentSqlParameters);
				} else {
					int[] generatedKeys = batchInsertIntoAutoincrementMysqlTable(logger, currentSqlStatement, currentSqlParameters);
					for (int generatedKey : generatedKeys) {
						results.add(generatedKey);
					}
				}
			}
			
			return results;
		} catch (Exception e) {
			logger.error("Error in insertCustomers: " + e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	@Override
	public void lockCustomers(int companyId, List<Integer> ids) {
		UUID uuid = UUID.randomUUID();
		String deleteTemp = "delete from cust_temporary_tbl where uuid=?";
		String insertCustomerId = "INSERT INTO cust_temporary_tbl (uuid, customer_id) VALUES (?,?)";

		// Remove all ID 0 and ensure that each ID is unique in this list
		final List<Integer> nonZeroIds = ids.stream().filter(id  -> id != 0).distinct().collect(Collectors.toList());
		
		if(!nonZeroIds.isEmpty()) {
			getJdbcTemplate().batchUpdate(insertCustomerId, new BatchPreparedStatementSetter() {
				@Override
				public void setValues(PreparedStatement ps, int i) throws SQLException {
					logger.fatal(String.format("i=%d, uuid='%s', customer-id=%d", i, uuid.toString(), nonZeroIds.get(i)));
					
					ps.setString(1,uuid.toString());
					ps.setInt(2, nonZeroIds.get(i));
				}
	
				@Override
				public int getBatchSize() {
					return nonZeroIds.size();
				}
			});
		}
		
		String selForUpd = "SELECT * FROM " + getCustomerTableName(companyId) + " WHERE customer_id IN (select customer_id from cust_temporary_tbl where uuid=? ) FOR UPDATE";
		select(logger, selForUpd, uuid.toString());
		update(logger, deleteTemp, uuid.toString());
	}

	/**
	 * Update or Insert a list of customers
	 * Returns a list of booleans with success for update/insert per customer of list
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public List<Object> updateCustomers(int companyID, List<Recipient> recipients) {
		try {
			List<Object> results = new ArrayList<>();
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
			CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));
			
			boolean allowHtmlTags = configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, companyID);
			
			String currentSqlStatement = null;
			List<Object[]> currentSqlParameters = new ArrayList<>();
			for (Recipient customer : recipients) {
				for (Entry<String, Object> entry : customer.getCustParameters().entrySet()) {
					if (entry.getValue() instanceof String) {
						// Check for unallowed html content
						try {
							HtmlChecker.checkForUnallowedHtmlTags((String) entry.getValue(), allowHtmlTags);
						} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
							throw new Exception("Invalid recipient data containing HTML for recipient field: " + entry.getKey());
						}
					}
				}
				
				if (companyID != customer.getCompanyID()) {
					if(logger.isInfoEnabled()) {
						logger.info(String.format("Rejected updating recipient %d: Belongs to foreign company ID", customer.getCustomerID()));
					}
					
					results.add(false);
					continue;
				}
				
				String nextSqlStatement;
				Object[] nextParameters;
				if (customer.getCustomerID() == 0) {
					// Insert new customer
					SqlPreparedInsertStatementManager insertStatementManager = prepareInsertStatement(recipientFieldsMap, customer, false);
					if (isOracleDB()) {
						int customerID = selectInt(logger, "SELECT customer_" + companyID + "_tbl_seq.NEXTVAL FROM DUAL");
						insertStatementManager.addValue("customer_id", customerID);
					}
					nextSqlStatement = insertStatementManager.getPreparedSqlString();
					nextParameters = insertStatementManager.getPreparedSqlParameters();
				} else {
					// Update existing customer
					SqlPreparedUpdateStatementManager updateStatementManager = prepareUpdateStatement(recipientFieldsMap, customer, false);
					nextSqlStatement = updateStatementManager.getPreparedSqlString();
					nextParameters = updateStatementManager.getPreparedSqlParameters();
				}
				
				// Execute bulk sql for sqlstatement group change or block size overflow
				if (currentSqlParameters.size() < 1000 && (currentSqlStatement == null || currentSqlStatement.equals(nextSqlStatement))){
					currentSqlStatement = nextSqlStatement;
					currentSqlParameters.add(nextParameters);
				} else {
					int[] touchedLinesArray = batchupdate(logger, currentSqlStatement, currentSqlParameters);
					for (int touchedLines : touchedLinesArray) {
						if(!(touchedLines > 0 || touchedLines == Statement.SUCCESS_NO_INFO)) {
							logger.warn(String.format("SQL batch update returned code %d", touchedLines));
							logger.warn(String.format("  Statement:  %s", currentSqlStatement));
							logger.warn(String.format("  Parameters: %s", currentSqlParameters));
						}
						
						results.add(touchedLines > 0 || touchedLines == Statement.SUCCESS_NO_INFO);
					}
					currentSqlStatement = nextSqlStatement;
					currentSqlParameters = new ArrayList<>();
					currentSqlParameters.add(nextParameters);
				}
			}
			
			// Execute bulk sql for last sqlstatement block
			if (currentSqlParameters.size() > 0) {
				int[] touchedLinesArray = batchupdate(logger, currentSqlStatement, currentSqlParameters);
				for (int touchedLines : touchedLinesArray) {
					if(!(touchedLines > 0 || touchedLines == Statement.SUCCESS_NO_INFO)) {
						logger.warn(String.format("SQL batch update returned code %d", touchedLines));
						logger.warn(String.format("  Statement:  %s", currentSqlStatement));
						logger.warn(String.format("  Parameters: %s", currentSqlParameters));
					}
					
					results.add(touchedLines > 0 || touchedLines == Statement.SUCCESS_NO_INFO);
				}
			}
			
			return results;
		} catch (Exception e) {
			logger.error("Error in updateCustomers: " + e.getMessage(), e);
			return Collections.emptyList();
		}
	}

	@Override
	public void checkParameters(CaseInsensitiveMap<String, Object> custParameters, int companyID) {
		CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap;
		try {
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
			recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));
		} catch (Exception e) {
			logger.error("Error in checkParameters: getRecipientFields: " + e.getMessage(), e);
			recipientFieldsMap = null;
		}
		
		if (recipientFieldsMap != null) {
			for (String paramName : custParameters.keySet()) {
				for (String suffix : SUPPLEMENTAL_DATE_COLUMN_SUFFIXES) {
					int idx = AgnUtils.indexOfIgnoreCase(paramName, suffix);
					if (idx >= 0) {
						paramName = paramName.substring(0, idx);
					}
				}
				if (!recipientFieldsMap.containsKey(paramName)) {
					throw new IllegalArgumentException("the field " + paramName + " does not exist");
				}
				RecipientFieldDescription field = recipientFieldsMap.get(paramName);
				if (field.getSimpleDataType() == SimpleDataType.Numeric) {
					String value = String.valueOf(custParameters.get(paramName));
					if (StringUtils.isNotBlank(value) && !AgnUtils.isNumber(value)) {
						throw new IllegalArgumentException("Parameter " + paramName + " value for type NUMERIC is invalid: '" + value + "'");
					}
				} else if (field.getSimpleDataType() == SimpleDataType.Float) {
					String value = String.valueOf(custParameters.get(paramName));
					if (StringUtils.isNotBlank(value) && !AgnUtils.isDouble(value)) {
						throw new IllegalArgumentException("Parameter " + paramName + " value for type FLOAT is invalid: '" + value + "'");
					}
				}
			}
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
	public final int insertNewCustWithException(final Recipient customer) throws Exception {
		final int companyID = customer.getCompanyID();
		if (companyID == 0) {
			return 0;
		}
		
		final Object email = customer.getCustParameters().get("email");
		final Object gender = customer.getCustParameters().get("gender");
		final Object firstname = customer.getCustParameters().get("firstname");
		final Object lastname = customer.getCustParameters().get("lastname");
		
		if (!configService.getBooleanValue(ConfigValue.AllowEmptyEmail, customer.getCompanyID()) && StringUtils.isBlank((String) email)) {
			throw new ViciousFormDataException("Cannot create customer, because customer data is invalid: email is empty");
		} else if (email != null && email instanceof String && StringUtils.isNotBlank((String) email) && !AgnUtils.isEmailValid((String) email)) {
			throw new ViciousFormDataException("Cannot create customer, because customer data is invalid: email is invalid");
		} else if (gender == null || (gender instanceof String && StringUtils.isBlank((String) gender))) {
			throw new ViciousFormDataException("Cannot create customer, because customer data is missing or invalid: gender is empty");
		} else if (firstname != null && firstname instanceof String && (((String) firstname).toLowerCase().contains("http:") || ((String) firstname).toLowerCase().contains("https:") || ((String) firstname).toLowerCase().contains("www."))) {
			throw new ViciousFormDataException("Cannot create customer, because customer data field \"firstname\" contains http link data");
		} else if (firstname != null && firstname instanceof String && (((String) firstname).length() > 100)) {
			throw new ViciousFormDataException("Cannot create customer, because customer data field \"firstname\" is to long (maximum 100) characters");
		} else if (lastname != null && lastname instanceof String && (((String) lastname).toLowerCase().contains("http:") || ((String) lastname).toLowerCase().contains("https:") || ((String) lastname).toLowerCase().contains("www."))) {
			throw new ViciousFormDataException("Cannot create customer, because customer data field \"lastname\" contains http link data");
		} else if (lastname != null && lastname instanceof String && (((String) lastname).length() > 100)) {
			throw new ViciousFormDataException("Cannot create customer, because customer data field \"lastname\" is to long (maximum 100) characters");
		}
		
		if (customer.getCustParameters().containsKey("plz") && configService.getBooleanValue(ConfigValue.CheckWellKnownProfileFields, customer.getCompanyID())) {
			String plzValue = customer.getCustParameters().get("plz") == null ? null : customer.getCustParameters().get("plz").toString();
			if (StringUtils.isNotBlank(plzValue) && !Pattern.matches("^[0-9]{5}$", plzValue)) {
				throw new ViciousFormDataException("Cannot create customer, because customer data field \"plz\" is invalid");
			}
		}

		boolean allowHtmlTags = configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, customer.getCompanyID());
		for (Entry<String, Object> entry : customer.getCustParameters().entrySet()) {
			if (entry.getValue() instanceof String) {
				// Check for unallowed html content
				try {
					HtmlChecker.checkForUnallowedHtmlTags((String) entry.getValue(), allowHtmlTags);
				} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
					throw new Exception("Invalid recipient data containing HTML for recipient field: " + entry.getKey());
				}
			}
		}
		
		List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
		CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));

		final SqlPreparedInsertStatementManager insertStatementManager = prepareInsertStatement(recipientFieldsMap, customer, false);

		int customerID = 0;
		// Execute insert
		if (isOracleDB()) {
			// Set customerID for Oracle only, MySql gets it as an statement return value
			customerID = selectInt(logger, "SELECT customer_" + companyID + "_tbl_seq.NEXTVAL FROM DUAL");
			insertStatementManager.addValue("customer_id", customerID);
			update(logger, insertStatementManager.getPreparedSqlString(), insertStatementManager.getPreparedSqlParameters());
		} else {
			customerID = insertIntoAutoincrementMysqlTable(logger, "customer_id", insertStatementManager.getPreparedSqlString(), insertStatementManager.getPreparedSqlParameters());
		}

		if (logger.isDebugEnabled()) {
			logger.debug("new customerID: " + customerID);
		}

		customer.setCustomerID(customerID);
		return customer.getCustomerID();
	}
	
	@Override
	public final boolean updateInDbWithException(final Recipient recipient) throws Exception {
		return updateInDbWithException(recipient, true);
	}
	
	@Override
	public final boolean updateInDbWithException(final Recipient recipient, final boolean missingFieldsToNull) throws Exception {
		return updateInDB(recipient, missingFieldsToNull, true);
	}

	private boolean updateInDB(final Recipient customer, final boolean missingFieldsToNull, final boolean throwExceptionOnError) throws Exception {
		final Object email = customer.getCustParameters().get("email");
		final Object gender = customer.getCustParameters().get("gender");
		final Object firstname = customer.getCustParameters().get("firstname");
		final Object lastname = customer.getCustParameters().get("lastname");

		if (!configService.getBooleanValue(ConfigValue.AllowEmptyEmail, customer.getCompanyID()) && StringUtils.isBlank((String) email)) {
			throw new ViciousFormDataException("Cannot update customer, because customer data is invalid: email is empty");
		} else if (email != null && email instanceof String && StringUtils.isNotBlank((String) email) && !AgnUtils.isEmailValid((String) email)) {
			throw new ViciousFormDataException("Cannot update customer, because customer data is invalid: email is invalid");
		} else if (gender == null || (gender instanceof String && StringUtils.isBlank((String) gender))) {
			throw new ViciousFormDataException("Cannot update customer, because customer data is missing or invalid: gender is empty");
		} else if (firstname != null && firstname instanceof String && (((String) firstname).toLowerCase().contains("http:") || ((String) firstname).toLowerCase().contains("https:") || ((String) firstname).toLowerCase().contains("www."))) {
			throw new ViciousFormDataException("Cannot update customer, because customer data field \"firstname\" contains http link data");
		} else if (firstname != null && firstname instanceof String && (((String) firstname).length() > 100)) {
			throw new ViciousFormDataException("Cannot update customer, because customer data field \"firstname\" is to long (maximum 100) characters");
		} else if (lastname != null && lastname instanceof String && (((String) lastname).toLowerCase().contains("http:") || ((String) lastname).toLowerCase().contains("https:") || ((String) lastname).toLowerCase().contains("www."))) {
			throw new ViciousFormDataException("Cannot update customer, because customer data field \"lastname\" contains http link data");
		} else if (lastname != null && lastname instanceof String && (((String) lastname).length() > 100)) {
			throw new ViciousFormDataException("Cannot update customer, because customer data field \"lastname\" is to long (maximum 100) characters");
		}
		
		if (customer.getCustParameters().containsKey("plz") && configService.getBooleanValue(ConfigValue.CheckWellKnownProfileFields, customer.getCompanyID())) {
			String plzValue = customer.getCustParameters().get("plz") == null ? null : customer.getCustParameters().get("plz").toString();
			if (StringUtils.isNotBlank(plzValue) && !Pattern.matches("^[0-9]{5}$", plzValue)) {
				throw new ViciousFormDataException("Cannot update customer, because customer data field \"plz\" is invalid");
			}
		}

		boolean allowHtmlTags = configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, customer.getCompanyID());
		for (Entry<String, Object> entry : customer.getCustParameters().entrySet()) {
			if (entry.getValue() instanceof String) {
				// Check for unallowed html content
				try {
					HtmlChecker.checkForUnallowedHtmlTags((String) entry.getValue(), allowHtmlTags);
				} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
					throw new Exception("Invalid recipient data containing HTML for recipient field: " + entry.getKey());
				}
			}
		}
		
		if (customer.getCustomerID() == 0) {
			if (logger.isInfoEnabled()) {
				logger.info("updateInDB: creating new customer");
			}
			
			return insertNewCustWithException(customer) > 0;
		}
		if (!customer.isChangeFlag()) {
			if (logger.isInfoEnabled()) {
				logger.info("updateInDB: nothing changed");
			}
			return true;
		}
	
		try {
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(customer.getCompanyID());
			CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));

			final SqlPreparedUpdateStatementManager updateStatementManager = prepareUpdateStatement(recipientFieldsMap, customer, missingFieldsToNull);
			// Execute update
			update(logger, updateStatementManager.getPreparedSqlString(), updateStatementManager.getPreparedSqlParameters());
		} catch (final Exception e) {
			logger.error("Exception in prepareUpdateStatement or new getQueryProperties", e);
			if (!throwExceptionOnError)  {
				return false;
			} else {
				throw e;
			}
		}

		return true;
	}

	/**
	 * Load complete Subscriber-Data from DB by Subscriber.
	 *
	 * @return List of RecipientResult with Key/Value-Pairs of customer data
	 * 
	 * Use EQL variant instead
	 * @see #getSizeOfCustomerDataFromDbList(int, String)
	 */
	@Override
	@Deprecated 
	public int getSizeOfCustomerDataFromDbList(int companyId, boolean matchAll, List<CriteriaEquals> criteriaEquals) {
		int recipientResults = 0;
	
		List<Object> whereParameters = new ArrayList<>();
		StringBuilder sqlWhereCriteria = initWhereParametersAndCreateSqlWhereCriteria(companyId, matchAll, criteriaEquals, whereParameters);
	
		String sql = "SELECT COUNT(customer_id) FROM " + getCustomerTableName(companyId) + " WHERE " + sqlWhereCriteria;
	
		try {
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyId);
			CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));

			validateCriteriaEquals(criteriaEquals, recipientFieldsMap);
		
			recipientResults = selectInt(logger, sql, whereParameters.toArray());
		
		} catch (IllegalArgumentException e) {
			logger.error("getCustomerDataFromDb: " + sql, e);
			throw e;
		} catch (Exception e) {
			logger.error("getCustomerDataFromDb: " + sql, e);
		}

		return recipientResults;
	}

	/**
	 * Load complete Subscriber-Data from DB by Subscriber.
	 *
	 * @return List of RecipientResult with Key/Value-Pairs of customer data
	 */
	@Override
	public int getSizeOfCustomerDataFromDbList(int companyId, final String eql) {
		return 0;
	}

	/**
	 * Load complete Subscriber-Data from DB by Subscriber.
	 *
	 * @return List of RecipientResult with Key/Value-Pairs of customer data
	 * 
	 * Use EQL variant instead
	 * @see #getSizeOfCustomerDataFromDbList(int, String)
	 */
	@Override
	@Deprecated
	public List<Integer> getCustomerDataFromDb(int companyId, boolean matchAll, List<CriteriaEquals> criteriaEquals) {
		List<Integer> recipientResults = new ArrayList<>();
	
		List<Object> whereParameters = new ArrayList<>();
		StringBuilder sqlWhereCriteria = initWhereParametersAndCreateSqlWhereCriteria(companyId, matchAll, criteriaEquals, whereParameters);
	
		String sql = "SELECT customer_id FROM " + getCustomerTableName(companyId) + " WHERE " + sqlWhereCriteria;
	
		try {
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyId);
			CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));
			validateCriteriaEquals(criteriaEquals, recipientFieldsMap);
		
			recipientResults = select(logger, sql, IntegerRowMapper.INSTANCE, whereParameters.toArray());
		
		} catch (IllegalArgumentException e) {
			logger.error("getCustomerDataFromDb: " + sql, e);
			throw e;
		} catch (Exception e) {
			logger.error("getCustomerDataFromDb: " + sql, e);
		}

		return recipientResults;
	}
	
	@Override
	public List<Integer> getCustomerDataFromDb(final int companyId, final String eql) throws RecipientException {
		throw new RecipientException("Method not available in OpenEMM");
	}

	private StringBuilder initWhereParametersAndCreateSqlWhereCriteria(int companyId, boolean matchAll, List<CriteriaEquals> criteriaEquals, List<Object> whereParameters) {
		StringBuilder sqlWhereCriteria = new StringBuilder();
		for (CriteriaEquals criteriaEqualsElement : criteriaEquals) {
			if (sqlWhereCriteria.length() > 0) {
				if (matchAll) {
					sqlWhereCriteria.append(" AND ");
				} else {
					sqlWhereCriteria.append(" OR ");
				}
			} else {
				sqlWhereCriteria.append(" ( ");
			}
			sqlWhereCriteria.append(criteriaEqualsElement.getProfilefield() + " = ? ");
		
			if (null == criteriaEqualsElement.getDateformat()) {
				whereParameters.add(criteriaEqualsElement.getValue());
			} else {
				SimpleDateFormat dateFormat = new SimpleDateFormat(criteriaEqualsElement.getDateformat());
				try {
					whereParameters.add(dateFormat.parse(criteriaEqualsElement.getValue()));
				} catch (ParseException e) {
					throw new IllegalArgumentException("Cannot parse date: " + criteriaEqualsElement.getValue()
							+ " with date format: " + criteriaEqualsElement.getDateformat() + ". " + e.getMessage());
				}
			}
		}
	
		if (sqlWhereCriteria.length() > 0) {
			sqlWhereCriteria.append(" ) ");
			sqlWhereCriteria.append(" AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0");
		} else {
			sqlWhereCriteria.append(RecipientStandardField.Bounceload.getColumnName() + " = 0");
		}

		boolean respectHideSign = configService.getBooleanValue(ConfigValue.RespectHideDataSign, companyId);
		if (respectHideSign) {
			sqlWhereCriteria.append(" AND (hide <= 0 OR hide IS NULL)");
		}
		return sqlWhereCriteria;
	}

	private void validateCriteriaEquals(List<CriteriaEquals> criteriaEquals, CaseInsensitiveMap<String, RecipientFieldDescription> customerDBProfileStructure) {
		for (CriteriaEquals criteriaEqualsElement : criteriaEquals) {
			if (!customerDBProfileStructure.containsKey(criteriaEqualsElement.getProfilefield())) {
				throw new IllegalArgumentException("The profile field " + criteriaEqualsElement.getProfilefield() + " does not exist");
			}
		
			RecipientFieldDescription profileField = customerDBProfileStructure.get(criteriaEqualsElement.getProfilefield());
			boolean isDateField = (SimpleDataType.Date == profileField.getSimpleDataType() || SimpleDataType.DateTime == profileField.getSimpleDataType());
		
			if (isDateField && criteriaEqualsElement.getDateformat() == null) {
				throw new IllegalArgumentException("The \"dateformat\" is missing for a date field: " + criteriaEqualsElement.getProfilefield());
			}
		
			if (!isDateField && criteriaEqualsElement.getDateformat() != null) {
				throw new IllegalArgumentException("The \"dateformat\" is specified for a non-date field: " + criteriaEqualsElement.getProfilefield());
			}
		}
	}

	/**
	 * Delete complete Subscriber-Data from DB. customerID must be set first for this method.
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public void deleteCustomerDataFromDb(int companyID, int customerID) {
		try {
			update(logger, "DELETE FROM " + getCustomerBindingTableName(companyID) + " WHERE customer_id = ?", customerID);
			update(logger, "DELETE FROM " + getCustomerTableName(companyID) + " WHERE customer_id = ?", customerID);
		} catch (Exception e) {
			throw e;
		}
	}

	/**
	 * Loads complete Mailinglist-Binding-Information for given customer-id from Database
	 *
	 * @return Map with key/value-pairs as combinations of mailinglist-id and BindingEntry-Objects
	 */
	@Override
	public Map<Integer, Map<Integer, BindingEntry>> loadAllListBindings(int companyID, int customerID) {
		Recipient cust = this.recipientFactory.newRecipient();
		cust.setListBindings(new Hashtable<>()); // MailingList_ID as keys

		Map<Integer, BindingEntry> mTable = new Hashtable<>(); // Media_ID as key, contains rest of data (user type, status etc.)

		String sqlGetLists = null;
		BindingEntry aEntry = null;
		int tmpMLID = 0;

		try {
			sqlGetLists = "SELECT mailinglist_id, user_type, user_status, user_remark, timestamp, mediatype FROM " + getCustomerBindingTableName(companyID) + " WHERE customer_id = " + customerID + " ORDER BY mailinglist_id, mediatype";
			List<Map<String, Object>> list = select(logger, sqlGetLists);
			for (Map<String, Object> map : list) {
				int listID = ((Number) map.get("mailinglist_id")).intValue();
				int mediaType = ((Number) map.get("mediatype")).intValue();

				aEntry = bindingEntryFactory.newBindingEntry();
			
				aEntry.setCustomerID(customerID);
				aEntry.setMailinglistID(listID);
				aEntry.setUserType((String) map.get("user_type"));
				aEntry.setUserStatus(((Number) map.get("user_status")).intValue());
				aEntry.setUserRemark((String) map.get("user_remark"));
				aEntry.setChangeDate((java.sql.Timestamp) map.get("timestamp"));
				aEntry.setMediaType(mediaType);

				if (tmpMLID != listID) {
					if (tmpMLID != 0) {
						cust.getListBindings().put(tmpMLID, mTable);
						mTable = new Hashtable<>();
						mTable.put(mediaType, aEntry);
						tmpMLID = listID;
					} else {
						mTable.put(mediaType, aEntry);
						tmpMLID = listID;
					}
				} else {
					mTable.put(mediaType, aEntry);
				}
			}
			cust.getListBindings().put(tmpMLID, mTable);
		} catch (Exception e) {
			logger.error("loadAllListBindings: " + sqlGetLists, e);
			return null;
		}
		return cust.getListBindings();
	}

	@Override
	public String getField(String selectVal, int recipientID, int companyID) {
		if (StringUtils.isNotBlank(selectVal) && recipientID > 0 && companyID > 0) {
			String sql = "SELECT " + selectVal + " value FROM " + getCustomerTableName(companyID) + " cust WHERE cust.customer_id = ?";
	
			try {
				List<Map<String, Object>> list = select(logger, sql, recipientID);
	
				if (list.size() > 0) {
					Map<String, Object> map = list.get(0);
					Object temp = map.get("value");
					if (temp != null) {
						return temp.toString();
					} else {
						return "";
					}
				} else {
					return "";
				}
			} catch (Exception e) {
				logger.error("processTag: " + sql, e);
				return null;
			}
		} else {
			return "";
		}
	}

	@Override
	public Map<Integer, Map<Integer, BindingEntry>> getAllMailingLists(int customerID, int companyID) {
		Map<Integer, Map<Integer, BindingEntry>> result = new HashMap<>();
		String sql = "SELECT mailinglist_id, user_type, user_status, user_remark, timestamp, mediatype, exit_mailing_id FROM " + getCustomerBindingTableName(companyID) + " WHERE customer_id = ? ORDER BY mailinglist_id, mediatype";

		if (logger.isInfoEnabled()) {
			logger.info("getAllMailingLists: " + sql);
		}

		List<Map<String, Object>> list = select(logger, sql, customerID);
		for (Map<String, Object> map : list) {
			int listID = ((Number) map.get("mailinglist_id")).intValue();
			int mediaType = ((Number) map.get("mediatype")).intValue();
			Map<Integer, BindingEntry> sub = result.get(listID);

			if (sub == null) {
				sub = new HashMap<>();
			}
			BindingEntry entry = bindingEntryFactory.newBindingEntry();
		
			entry.setCustomerID(customerID);
			entry.setMailinglistID(listID);
			entry.setUserType((String) map.get("user_type"));
			entry.setUserStatus(((Number) map.get("user_status")).intValue());
			entry.setUserRemark((String) map.get("user_remark"));
			entry.setChangeDate((java.sql.Timestamp) map.get("timestamp"));
			entry.setMediaType(mediaType);
			if (map.get("exit_mailing_id") != null)	{
				entry.setExitMailingID(((Number) map.get("exit_mailing_id")).intValue());
			} else {
				entry.setExitMailingID(0);
			}
			sub.put(mediaType, entry);
			result.put(listID, sub);
		}
		return result;
	}

	@Override
	public int sumOfRecipients(int companyID, String target) {
		int recipients = 0;

		String sql = "SELECT COUNT(customer_id) FROM " + getCustomerTableName(companyID) + " cust WHERE " + target + " AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0";
		try {
			recipients = selectInt(logger, sql);
		} catch (@SuppressWarnings("unused") Exception e) {
			recipients = 0;
		}
		return recipients;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public boolean deleteRecipients(int companyID, String target) {
		try {
			// Create a temporary table with the customerids so we do not have a problem with newly created customers between the two delete statements
			if (isOracleDB()) {
				String tablespacePart = "";
				if (DbUtilities.checkOracleTablespaceExists(getDataSource(), "data_temp")) {
					tablespacePart = " TABLESPACE data_temp";
				}
				execute(logger, "CREATE TABLE drop_cust_" + companyID + "_tbl" + tablespacePart + " AS SELECT customer_id FROM " + getCustomerTableName(companyID) + " cust WHERE " + target);
			} else {
				execute(logger, "CREATE TABLE drop_cust_" + companyID + "_tbl ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AS SELECT customer_id FROM " + getCustomerTableName(companyID) + " cust WHERE " + target);
			}
			execute(logger, "CREATE INDEX dropCust" + companyID + "$cid$idx ON drop_cust_" + companyID + "_tbl (customer_id)");

			int stepsize = 200000;
			int touchedLines = 0;
			do {
				if (isOracleDB()) {
					touchedLines = update(logger, "DELETE FROM " + getCustomerBindingTableName(companyID) + " WHERE customer_id in (SELECT customer_id FROM drop_cust_" + companyID + "_tbl) AND ROWNUM <= ?", stepsize);
				} else {
					touchedLines = update(logger, "DELETE FROM " + getCustomerBindingTableName(companyID) + " WHERE customer_id in (SELECT customer_id FROM drop_cust_" + companyID + "_tbl) LIMIT " + stepsize);
				}
			} while (touchedLines == stepsize);
			
			touchedLines = 0;
			do {
				if (isOracleDB()) {
					touchedLines = update(logger, "DELETE FROM " + getCustomerTableName(companyID) + " WHERE customer_id in (SELECT customer_id FROM drop_cust_" + companyID + "_tbl) AND ROWNUM <= ?", stepsize);
				} else {
					touchedLines = update(logger, "DELETE FROM " + getCustomerTableName(companyID) + " WHERE customer_id in (SELECT customer_id FROM drop_cust_" + companyID + "_tbl) LIMIT " + stepsize);
				}
			} while (touchedLines == stepsize);

			return true;
		} catch (Exception e) {
			logger.error("Error while deleting recipients", e);
			return false;
		} finally {
			try {
				DbUtilities.dropTable(getDataSource(), "drop_cust_" + companyID + "_tbl");
			} catch (Exception e) {
				logger.error("Cannot drop temporary table drop_cust_" + companyID + "_tbl", e);
			}
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteAllNoBindings(int companyID, String toBeDeletedTable) {
		final int stepsize = 100000;
		int touchedLines = 0;
		String delete = "DELETE FROM " + getCustomerTableName(companyID)
			+ " WHERE NOT EXISTS (SELECT 1 FROM " + getCustomerBindingTableName(companyID) + " bind WHERE "+ getCustomerTableName(companyID) + ".customer_id = bind.customer_id)"
			+ " AND EXISTS (SELECT 1 FROM " + toBeDeletedTable + " temp WHERE "+ getCustomerTableName(companyID) + ".customer_id = temp.customer_id)";
		
		if (isOracleDB()) {
			delete += " AND ROWNUM <= ?";
		} else {
			delete += "LIMIT ?";
		}
		do {
			touchedLines = update(logger, delete, stepsize);
		} while (touchedLines == stepsize);
		
		DbUtilities.dropTable(getDataSource(), toBeDeletedTable);
	}

	@Override
	public String createTmpTableByMailinglistID(int companyID, int mailinglistID) {
		String optionalTablespacePart = "";
		if (isOracleDB() && DbUtilities.checkOracleTablespaceExists(getDataSource(), "data_temp")) {
			optionalTablespacePart = " TABLESPACE data_temp";
		}
	
		String tableName = "tmp_" + String.valueOf(System.currentTimeMillis()) + "del_tbl";

		String sql;
		if (isOracleDB()) {
			sql = "CREATE TABLE " + tableName + optionalTablespacePart + " AS (" + "SELECT customer_id FROM " + getCustomerTableName(companyID) + " cust "
				+ " WHERE EXISTS (SELECT 1 FROM " + getCustomerBindingTableName(companyID) + " bind WHERE cust.customer_id = bind.customer_id AND mailinglist_id = " + mailinglistID + "))";
		} else {
			sql = "CREATE TABLE " + tableName + optionalTablespacePart + " ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci AS (" + "SELECT customer_id FROM " + getCustomerTableName(companyID) + " cust "
				+ " WHERE EXISTS (SELECT 1 FROM " + getCustomerBindingTableName(companyID) + " bind WHERE cust.customer_id = bind.customer_id AND mailinglist_id = " + mailinglistID + "))";
		}
		
		update(logger, String.format(sql, mailinglistID));
		
		sql = "CREATE INDEX " + tableName + "_idx ON " + tableName + " (customer_id)";
		execute(logger, sql);
		return tableName;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void deleteRecipientsBindings(int mailinglistID, int companyID, boolean activeOnly, boolean notAdminsAndTests) {
		StringBuilder sql = new StringBuilder("DELETE FROM " + getCustomerBindingTableName(companyID) + " WHERE mailinglist_id = ?");

		if (activeOnly) {
			sql.append(" ").append(String.format("AND user_status = %d", UserStatus.Active.getStatusCode()));
		}
	
		if (notAdminsAndTests) {
			sql.append(" ").append(String.format("AND user_type <> '%s' AND user_type <> '%s' AND user_type <> '%s'", UserType.Admin.getTypeCode(), UserType.TestUser.getTypeCode(), UserType.TestVIP.getTypeCode()));
		}

		update(logger, sql.toString(), mailinglistID);
	}

	@Override
	public CaseInsensitiveMap<String, CsvColInfo> readDBColumns(int companyID, int adminID, List<String> keyColumns) {
		CaseInsensitiveMap<String, CsvColInfo> dbAllColumns = new CaseInsensitiveMap<>();
		
		CaseInsensitiveSet keyColumnsSet = new CaseInsensitiveSet(keyColumns);

		try {
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
			for (RecipientFieldDescription profileField : recipientFields) {
				ProfileFieldMode adminPermission = profileField.getAdminPermission(adminID);
	        	if (adminPermission == ProfileFieldMode.Editable || (adminPermission == ProfileFieldMode.ReadOnly && keyColumnsSet.contains(profileField.getColumnName()))) {
					CsvColInfo csvColInfo = new CsvColInfo();

					csvColInfo.setName(profileField.getColumnName());
					csvColInfo.setLength(profileField.getSimpleDataType() == SimpleDataType.Characters ? profileField.getCharacterLength() : profileField.getNumericScale());
					csvColInfo.setActive(false);
					csvColInfo.setNullable(profileField.isNullable());
					csvColInfo.setType(dbTypeToCsvType(profileField.getSimpleDataType()));

					dbAllColumns.put(profileField.getColumnName(), csvColInfo);
				}
			}
		} catch (Exception e) {
			logger.error("readDBColumns (companyID: " + companyID + ")", e);
		}
		return dbAllColumns;
	}

	private static int dbTypeToCsvType(SimpleDataType type) {
		switch (type) {
			case Numeric:
			case Float:
				return CsvColInfo.TYPE_NUMERIC;
			case Characters:
				return CsvColInfo.TYPE_CHAR;
			case Date:
			case DateTime:
				return CsvColInfo.TYPE_DATE;
			default:
				return CsvColInfo.TYPE_UNKNOWN;
		}
	}

	@Override
	public Map<Integer, String> getAdminAndTestRecipientsDescription(int companyId, int mailingId) {
        List<Map<String, Object>> tmpList = select(logger, getMailingAdminAndTestRecipientsSql(companyId), mailingId);
		HashMap<Integer, String> result = new HashMap<>();
		for (Map<String, Object> map : tmpList) {
			int id = ((Number) map.get("customer_id")).intValue();
			String email = (String) map.get("email");
			String firstName = (String) map.get("firstname");
			String lastName = (String) map.get("lastname");

			if (firstName == null) {
				firstName = "";
			}

			if (lastName == null) {
				lastName = "";
			}

			result.put(id, firstName + " " + lastName + " &lt;" + email + "&gt;");
		}
		return result;
	}

    private String getMailingAdminAndTestRecipientsSql(int companyId) {
        return "SELECT bind.customer_id, cust.email, cust.firstname, cust.lastname" +
                " FROM mailing_tbl mail, " +
                getCustomerTableName(companyId) + " cust, " + getCustomerBindingTableName(companyId) + " bind" +
                " WHERE bind.user_type IN ('" +
                UserType.Admin.getTypeCode() + "', '" +
                UserType.TestUser.getTypeCode() + "', '" +
                UserType.TestVIP.getTypeCode() + "')" +
                " AND bind.user_status = 1" +
                " AND bind.mailinglist_id = mail.mailinglist_id" +
                " AND bind.customer_id = cust.customer_id" +
                " AND mail.mailing_id = ?" +
                " ORDER BY bind.user_type, bind.customer_id";
    }

    @Override
    public List<ComRecipientLiteImpl> getMailingAdminAndTestRecipients(int mailingId, int companyId) {
        String sql = getMailingAdminAndTestRecipientsSql(companyId);
        return select(logger, sql, new ComRecipientLite_RowMapper(), mailingId);
    }

	@Override
	public int getPreviewRecipient(int companyId, int mailingId) {
		String sql = "SELECT bind.customer_id"
				+ " FROM mailing_tbl m, " + getCustomerTableName(companyId) + " cust, " + getCustomerBindingTableName(companyId) + " bind"
				+ " WHERE bind.user_type in (?, ?, ?) AND bind.user_status = ? AND bind.mailinglist_id = m.mailinglist_id AND bind.customer_id = cust.customer_id AND m.mailing_id = ?"
				+ " ORDER BY bind.user_type, bind.customer_id";

		if (isOracleDB()) {
			// Outer query required to honour a sorting
			sql = "SELECT * FROM (" + sql + ") WHERE ROWNUM = 1";
		} else {
			sql = sql + " LIMIT 1";
		}

		return selectIntWithDefaultValue(logger, sql, 0, UserType.Admin.getTypeCode(), UserType.TestUser.getTypeCode(), UserType.TestVIP.getTypeCode(), UserStatus.Active.getStatusCode(), mailingId);
	}

	@Override
	public int getCustomerIdWithEmailInMailingList(int companyId, int mailingId, String email) {
		String sql = "SELECT DISTINCT bind.customer_id" + " FROM mailing_tbl mail, " + getCustomerTableName(companyId) + " cust, " + getCustomerBindingTableName(companyId) + " bind"
				+ " WHERE cust.email = ? AND bind.user_status = 1 AND bind.mailinglist_id = mail.mailinglist_id AND bind.customer_id = cust.customer_id AND mail.mailing_id = ?";

		try {
			List<Map<String, Object>> tmpList = select(logger, sql, AgnUtils.normalizeEmail(email), mailingId);
			if (tmpList.size() > 0) {
				return ((Number) tmpList.get(0).get("customer_id")).intValue();
			} else {
				return 0;
			}
		} catch (@SuppressWarnings("unused") Exception e) {
			return 0;
		}
	}

	@Override
	public List<Recipient> getBouncedMailingRecipients(int companyId, int mailingId) {
		String sqlStatement = "SELECT cust.email AS email, cust.firstname AS firstname, cust.lastname AS lastname, cust.gender AS gender"
				+ " FROM " + getCustomerBindingTableName(companyId) + " bind, " + getCustomerTableName(companyId) + " cust"
				+ "	WHERE bind.customer_id = cust.customer_id AND exit_mailing_id = ? AND user_status = 2 AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?)"
				+ " AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0";

		List<Map<String, Object>> tmpList = select(logger, sqlStatement, mailingId, mailingId);
		List<Recipient> result = new ArrayList<>();
		for (Map<String, Object> row : tmpList) {
			Map<String, Object> customerData = new HashMap<>();

			customerData.put("gender", row.get("GENDER"));
			customerData.put("firstname", row.get("FIRSTNAME"));
			customerData.put("lastname", row.get("LASTNAME"));
			customerData.put("email", row.get("EMAIL"));

			Recipient newBean = this.recipientFactory.newRecipient();
			newBean.setCustParameters(customerData);

			result.add(newBean);
		}
		return result;
	}

	@Override
	public List<Map<String, Object>> getBouncedRecipients(int companyID, Date fromDate) {
		String sqlStatement = "SELECT cust.email AS email, MAX(bind.timestamp) AS bouncetimestamp"
			+ " FROM " + getCustomerBindingTableName(companyID) + " bind, " + getCustomerTableName(companyID) + " cust"
			+ "	WHERE bind.customer_id = cust.customer_id"
			+ " AND user_status = 2"
			+ " AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0"
			+ (fromDate != null ? " AND bind.timestamp > ?" : "")
			+ " GROUP BY cust.email"
			+ " ORDER BY cust.email";

		if (fromDate != null) {
			return select(logger, sqlStatement, fromDate);
		} else {
			return select(logger, sqlStatement);
		}
	}

	@Override
	public List<Map<String, Object>> getUnsubscribedRecipients(int companyID, Date fromDate) {
		String sqlStatement = "SELECT cust.email AS email, bind.timestamp AS unsubscribetimestamp, ml.shortname AS mailinglistname"
			+ " FROM " + getCustomerBindingTableName(companyID) + " bind, " + getCustomerTableName(companyID) + " cust, mailinglist_tbl ml"
			+ "	WHERE bind.customer_id = cust.customer_id AND bind.mailinglist_id = ml.mailinglist_id"
			+ " AND user_status in (3, 4)"
			+ " AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0"
			+ (fromDate != null ? " AND bind.timestamp > ?" : "")
			+ " ORDER BY cust.email, ml.shortname";

		if (fromDate != null) {
			return select(logger, sqlStatement, fromDate);
		} else {
			return select(logger, sqlStatement);
		}
	}

	@Override
	@DaoUpdateReturnValueCheck
	public int deleteRecipients(int companyID, List<Integer> list) {
		if (list == null || list.size() < 1) {
			throw new RuntimeException("Invalid customerID list size");
		}

		String where = " WHERE " + makeBulkInClauseForInteger("customer_id", list);

		update(logger, "DELETE FROM " + getCustomerBindingTableName(companyID) + where);
		return update(logger, "DELETE FROM " + getCustomerTableName(companyID) + where);
	}

	@Override
	public boolean exist(int customerId, int companyId) {
		return selectInt(logger, "SELECT COUNT(*) FROM " + getCustomerTableName(companyId) + " WHERE customer_id = ?", customerId) > 0;
	}

	protected String createDateDefaultValueExpression(String defaultValue) {
		if (StringUtils.isBlank(defaultValue)) {
			return "NULL";
		}  else if (defaultValue.toLowerCase().equals("now()") || defaultValue.toLowerCase().equals("current_timestamp") || defaultValue.toLowerCase().equals("current_timestamp()") || defaultValue.toLowerCase().equals("sysdate") || defaultValue.toLowerCase().equals("sysdate()")) {
			return "CURRENT_TIMESTAMP";
		} else {
			if (isOracleDB()) {
				if (defaultValue.toUpperCase().startsWith("TO_DATE(")) {
					return defaultValue;
				} else if (defaultValue.length() <= 10) {
					if (defaultValue.contains("-")) {
						return "TO_DATE('" + defaultValue + "', 'YYYY-MM-DD')";
					} else if (defaultValue.contains("/")) {
						return "TO_DATE('" + defaultValue + "', 'MM/DD/YYYY')";
					} else {
						return "TO_DATE('" + defaultValue + "', 'DD.MM.YYYY')";
					}
				} else {
					if (defaultValue.contains("-")) {
						return "TO_DATE('" + defaultValue + "', 'YYYY-MM-DD HH24:MI:SS')";
					} else if (defaultValue.contains("/")) {
						return "TO_DATE('" + defaultValue + "', 'MM/DD/YYYY HH24:MI:SS')";
					} else {
						return "TO_DATE('" + defaultValue + "', 'DD.MM.YYYY HH24:MI:SS')";
					}
				}
			} else {
				if (defaultValue.toUpperCase().startsWith("STR_TO_DATE(")) {
					return defaultValue;
				} else if (defaultValue.length() <= 10) {
					if (defaultValue.contains("-")) {
						return "STR_TO_DATE('" + defaultValue + "', '%Y-%m-%d')";
					} else if (defaultValue.contains("/")) {
						return "STR_TO_DATE('" + defaultValue + "', '%m/%d/%Y')";
					} else {
						return "STR_TO_DATE('" + defaultValue + "', '%d.%m.%Y')";
					}
				} else {
					if (defaultValue.contains("-")) {
						return "STR_TO_DATE('" + defaultValue + "', '%Y-%m-%d %H:%i:%s')";
					} else if (defaultValue.contains("/")) {
						return "STR_TO_DATE('" + defaultValue + "', '%m/%d/%Y %H:%i:%s')";
					} else {
						return "STR_TO_DATE('" + defaultValue + "', '%d.%m.%Y %H:%i:%s')";
					}
				}
			}
		}
	}

	/**
	 * Get data of several recipients<br />
	 * Caution: All values are of type String, especially customer_id
	 */
	@Override
	public List<CaseInsensitiveMap<String, Object>> getCustomers(List<Integer> customerIDs, int companyID) {
		List<RecipientFieldDescription> recipientFields;
		try {
			recipientFields = recipientFieldService.getRecipientFields(companyID);
		} catch (Exception e) {
			logger.error("getCustomers: Exception in getQueryProperties", e);
			return Collections.emptyList();
		}

		String query = "SELECT * FROM " + getCustomerTableName(companyID) + " WHERE customer_id IN (:ids) AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0";
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", customerIDs);

		NamedParameterJdbcTemplate namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(getDataSource());
		List<Map<String, Object>> queryResult = namedParameterJdbcTemplate.queryForList(query, parameters);
	
		List<CaseInsensitiveMap<String, Object>> results = new ArrayList<>();
	
		GregorianCalendar calendar = new GregorianCalendar();
	
		for (Map<String, Object> row : queryResult) {
			CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();
		
			for (RecipientFieldDescription recipientFieldDescription : recipientFields) {
				String columnName = recipientFieldDescription.getColumnName();
				SimpleDataType columnType = recipientFieldDescription.getSimpleDataType();
				Object value = row.get(columnName);
				if (SimpleDataType.Date == columnType || SimpleDataType.DateTime == columnType) {
					if (value == null) {
						Map<String, String> dateColumnEmptyValues = SUPPLEMENTAL_DATE_COLUMN_SUFFIXES.stream()
								.map(suffix -> columnName + suffix)
								.collect(Collectors.toMap(Function.identity(), pair -> ""));
						dateColumnEmptyValues.put(columnName, "");

						params.putAll(dateColumnEmptyValues);
					} else {
						calendar.setTime((Date) value);
						params.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_DAY, Integer.toString(calendar.get(GregorianCalendar.DAY_OF_MONTH)));
						params.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MONTH, Integer.toString(calendar.get(GregorianCalendar.MONTH) + 1));
						params.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_YEAR, Integer.toString(calendar.get(GregorianCalendar.YEAR)));
						params.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_HOUR, Integer.toString(calendar.get(GregorianCalendar.HOUR_OF_DAY)));
						params.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_MINUTE, Integer.toString(calendar.get(GregorianCalendar.MINUTE)));
						params.put(columnName + ComRecipientDao.SUPPLEMENTAL_DATECOLUMN_SUFFIX_SECOND, Integer.toString(calendar.get(GregorianCalendar.SECOND)));
						params.put(columnName, new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS).format(calendar.getTime()));
					}
				} else {
					if (value == null) {
						value = "";
					}
					params.put(columnName, value.toString());
				}
			}
		
			results.add(params);
		}

		return results;
	}

	/**
	 * Get data of several recipients<br />
	 * Keeps datatypes in Object class (differs from method "getCustomers")<br />
	 * Dates are returned as items of class Date
	 */
	@Override
	public List<CaseInsensitiveMap<String, Object>> getCustomersData(List<Integer> customerIDs, int companyID) {
		List<RecipientFieldDescription> recipientFields;
		try {
			recipientFields = recipientFieldService.getRecipientFields(companyID);
		} catch (Exception e) {
			logger.error("getCustomers: Exception in getQueryProperties", e);
			return Collections.emptyList();
		}

		String query = "SELECT * FROM " + getCustomerTableName(companyID) + " WHERE customer_id IN (:ids) AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0";
		MapSqlParameterSource parameters = new MapSqlParameterSource();
		parameters.addValue("ids", customerIDs);

		NamedParameterJdbcTemplate jTmpl = new NamedParameterJdbcTemplate(getDataSource());
		List<Map<String, Object>> queryResult = jTmpl.queryForList(query, parameters);
	
		List<CaseInsensitiveMap<String, Object>> results = new ArrayList<>();

		for (Map<String, Object> row : queryResult) {
			CaseInsensitiveMap<String, Object> params = new CaseInsensitiveMap<>();
		
			for (RecipientFieldDescription recipientFieldDescription : recipientFields) {
				String columnName = recipientFieldDescription.getColumnName();
				Object value = row.get(columnName);
				params.put(columnName, value);
			}
		
			results.add(params);
		}
		
		return results;
	}

	@Override
	@DaoUpdateReturnValueCheck
	public void updateStatusByColumn(int companyId, String columnName, String columnValue, int newStatus, String remark) throws Exception {
		// Check for valid UserStatus code
		UserStatus.getUserStatusByID(newStatus);
		
		String query = "SELECT customer_id FROM " + getCustomerTableName(companyId) + " WHERE " + columnName + " = ?";
		List<Integer> list = select(logger, query, IntegerRowMapper.INSTANCE, columnValue);

		String update = "UPDATE " + getCustomerBindingTableName(companyId) + " SET user_status = ?, user_remark = ?, timestamp = CURRENT_TIMESTAMP"
				+ " WHERE customer_id = ? AND user_status != ?";
		for (int customerId : list) {
			update(logger, update, newStatus, remark, customerId, newStatus);
		}
	}

	@Override
	public CaseInsensitiveMap<String, RecipientFieldDescription> getAvailableProfileFields(int companyID) throws Exception {
		List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
		CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));
		return recipientFieldsMap;
	}

	@Override
	public int getDefaultDatasourceID(String username, int companyID) {
		String sql = "SELECT default_data_source_id FROM webservice_user_tbl WHERE username = ? AND company_id = ?";
		return selectInt(logger, sql, username, companyID);
	}

	@Override
	public List<WebtrackingHistoryEntry> getRecipientWebtrackingHistory(int companyID, int customerID) {
		logger.info("Get recipient web tracking history is unsupported.");
		return new ArrayList<>();
	}

	protected class WebtrackingHistoryEntry_RowMapper implements RowMapper<WebtrackingHistoryEntry> {

		@Override
		public WebtrackingHistoryEntry mapRow(ResultSet resultSet, int row) throws SQLException {
			WebtrackingHistoryEntry newItem = new WebtrackingHistoryEntry();
			newItem.setDate(resultSet.getTimestamp("timestamp"));
			newItem.setMailingName(resultSet.getString("shortname"));
			newItem.setMailingID(resultSet.getBigDecimal("mailing_id").intValue());
			newItem.setName(resultSet.getString("page_tag"));
			newItem.setValue(resultSet.getString("value"));
			newItem.setIpAddress(resultSet.getString("ip_adr"));
			return newItem;
		}
	}

	@Override
	public void updateForActionOperationUpdateCustomer(int companyID, String columnName, int updateType, String updateValue, int customerID) throws Exception {
		SimpleDataType columnType = DbUtilities.getColumnDataType(getDataSource(), getCustomerTableName(companyID), columnName).getSimpleDataType();

		StringBuffer updateStatement = new StringBuffer("UPDATE " + getCustomerTableName(companyID) + " SET timestamp = CURRENT_TIMESTAMP, " + columnName + " = ");
		Object value = null;
		if (columnType == SimpleDataType.Numeric || columnType == SimpleDataType.Float) {
			switch (updateType) {
				case ActionOperationUpdateCustomerParameters.TYPE_INCREMENT_BY:
					updateStatement.append("COALESCE(" + columnName + ", 0) + ?");
					break;

				case ActionOperationUpdateCustomerParameters.TYPE_DECREMENT_BY:
					updateStatement.append("COALESCE(" + columnName + ", 0) - ?");
					break;

				case ActionOperationUpdateCustomerParameters.TYPE_SET_VALUE:
					updateStatement.append("?");
					break;
				
				default:
					throw new Exception("Invalid update value type");
			}
		
			try {
				value = Double.parseDouble(updateValue);
			} catch (@SuppressWarnings("unused") Exception e) {
				value = 0.0;
			}
		} else if (columnType == SimpleDataType.Characters) {
			boolean allowHtmlTags = configService.getBooleanValue(ConfigValue.AllowHtmlTagsInReferenceAndProfileFields, companyID);
			// Check for unallowed html content
			try {
				HtmlChecker.checkForUnallowedHtmlTags(updateValue, allowHtmlTags);
			} catch(@SuppressWarnings("unused") final HtmlCheckerException e) {
				throw new RestfulClientException("Invalid recipient data containing HTML for recipient field: " + columnName);
			}
			
			switch (updateType) {
				case ActionOperationUpdateCustomerParameters.TYPE_INCREMENT_BY:
					final String CONCATENATE_PART = isOracleDB()
						? columnName +" || ?"
						: "CONCAT(" + columnName + ", ?)";
					updateStatement.append(CONCATENATE_PART);
					break;

				case ActionOperationUpdateCustomerParameters.TYPE_DECREMENT_BY:
					//TODO: This operation is not allowed on char-types and should be prevented in GUI
					updateStatement.append(columnName + " - ?");
					break;

				case ActionOperationUpdateCustomerParameters.TYPE_SET_VALUE:
					updateStatement.append("?");
					break;
					
				default:
					throw new Exception("Invalid update value type");
			}

			value = updateValue;
		} else if (columnType == SimpleDataType.Date || columnType == SimpleDataType.DateTime) {
			if (updateType == ActionOperationUpdateCustomerParameters.TYPE_INCREMENT_BY) {
				if (isOracleDB()) {
					updateStatement.append(columnName + " + ?");
				} else {
					// 86400 seconds per day, this factor is used to adopt MySQL date calucation to Oracle date calculation
					updateStatement.append(columnName + " + INTERVAL (? * 86400) SECOND");
				}
				try {
					value = Double.parseDouble(updateValue);
				} catch (@SuppressWarnings("unused") Exception e) {
					throw new Exception("Invalid value for increment of '" + columnName + "': " + updateValue);
				}
			} else if (updateType == ActionOperationUpdateCustomerParameters.TYPE_DECREMENT_BY) {
				if (isOracleDB()) {
					updateStatement.append(columnName + " - ?");
				} else {
					// 86400 seconds per day, this factor is used to adopt MySQL date calucation to Oracle date calculation
					updateStatement.append(columnName + " - INTERVAL (? * 86400) SECOND");
				}
				try {
					value = Double.parseDouble(updateValue);
				} catch (@SuppressWarnings("unused") Exception e) {
					throw new Exception("Invalid value for decrement of '" + columnName + "': " + updateValue);
				}
			} else if (updateType == ActionOperationUpdateCustomerParameters.TYPE_SET_VALUE) {
				Matcher matcher = ActionOperationUpdateCustomerParameters.DATE_ARITHMETICS_PATTERN.matcher(updateValue.toUpperCase());
				if (matcher.matches()) {
					updateStatement.append("CURRENT_TIMESTAMP");
					if (matcher.group(2) != null) {
						// Is safe, because group 2 must match "+" or "-" according to reg exp.
						updateStatement.append(matcher.group(2));
						if (isOracleDB()) {
							updateStatement.append(" ?");
						} else {
							// 86400 seconds per day, this factor is used to adopt MySQL date calucation to Oracle date calculation
							updateStatement.append(" INTERVAL (? * 86400) SECOND");
						}
						value = Double.parseDouble(matcher.group(3));
					}
				} else {
					if (isOracleDB()) {
						updateStatement.append("TO_DATE(?, 'yyyymmdd')");
					} else {
						updateStatement.append("STR_TO_DATE(?, '%Y%m%d')");
					}
					value = updateValue;
				}
			} else {
				throw new Exception("Invalid update value type");
			}
		} else {
			throw new Exception("Invalid update value type");
		}

		updateStatement.append(" WHERE customer_id = ?");
	
		if (value == null) {
			update(logger, updateStatement.toString(), customerID);
		} else {
			update(logger, updateStatement.toString(), value, customerID);
		}
	}

	@Override
	public String selectCustomerValue(String selectValue, int companyID, int customerId) {
		return select(logger, "SELECT " + selectValue + " FROM " + getCustomerTableName(companyID) + " cust WHERE cust.customer_id = ?", String.class, customerId);
	}

	@Override
	public int bulkUpdateEachRecipientsFields(int companyId, int adminId, int mailingListId, String sqlTargetExpression, Map<String, Object> updateValues) throws Exception {
		if (companyId <= 0 || MapUtils.isEmpty(updateValues)) {
			return 0;
		}
		
		List<String> alreadyRunningImports = select(logger, "SELECT description FROM import_temporary_tables WHERE import_table_name = ?", StringRowMapper.INSTANCE, "customer_" + companyId + "_tbl");
		if (alreadyRunningImports.size() > 0) {
			throw new ImportException(false, "error.import.AlreadyRunning", alreadyRunningImports.get(0));
		}
		
		Set<String> guiBulkImmutableFields = RecipientStandardField.getBulkImmutableRecipientStandardFieldColumnNames();
		Map<String, Object> excludedImmutable = updateValues.entrySet().stream()
				.filter(pair -> !guiBulkImmutableFields.contains(pair.getKey()))
				.collect(Collectors.toMap(Entry::getKey, Entry::getValue));
		
		if(excludedImmutable.size() == 0) {
            logger.error("No field to change found. Maybe all fields given are immutable for company {}", companyId);
            throw new Exception("No field to change found. Maybe all fields given are immutable?");
        }
		
		update(logger, "INSERT INTO import_temporary_tables (session_id, temporary_table_name, import_table_name, host, description) VALUES(?, ?, ?, ?, ?)", "", "", "customer_" + companyId + "_tbl", AgnUtils.getHostName(), "Bulk recipient update");
		
		try {
			String query = "UPDATE " + getCustomerTableName(companyId) + " cust SET timestamp = CURRENT_TIMESTAMP";
			int dataSourceId = getDatasourceDescription("Bulk recipient update").getId();
			
			List<Object> subStatementParameter = new ArrayList<>();
			String whereSubStatement = getWhereSubStatement(subStatementParameter, companyId, adminId, mailingListId, sqlTargetExpression);
			
			int touchedLines = 0;
			for(Map.Entry<String, Object> fieldData: excludedImmutable.entrySet()) {
				String fieldName = StringUtils.lowerCase(fieldData.getKey());
				Object fieldValue = replaceEmptyStringsWithNull(fieldData.getValue());
				List<Object> fieldParams = new ArrayList<>();
				StringBuilder sqlUpdateStatement = new StringBuilder(query);
				
				sqlUpdateStatement.append(", latest_datasource_id = ?");
				fieldParams.add(dataSourceId);
				
				sqlUpdateStatement.append(", cust.").append(fieldName).append(" = ?");
				fieldParams.add(fieldValue);
				
				sqlUpdateStatement.append(whereSubStatement);
				fieldParams.addAll(subStatementParameter);
				
				sqlUpdateStatement.append(" AND ");
				if (fieldValue != null) {
					sqlUpdateStatement.append("(cust.").append(fieldName).append(" != ? OR cust.").append(fieldName).append(" IS NULL)");
					fieldParams.add(fieldValue);
				} else {
					sqlUpdateStatement.append("cust.").append(fieldName).append(" IS NOT NULL");
				}
				
				try {
					int updated = update(logger, sqlUpdateStatement.toString(), fieldParams.toArray());
					touchedLines += updated;
				} catch (Exception e) {
					if(e.getCause() instanceof SQLDataException) {
						throw new ProfileFieldBulkUpdateException(fieldName, fieldValue, companyId, e.getCause());
					}
					throw e;
				}
			}
			return touchedLines;
		} finally {
			update(logger, "DELETE FROM import_temporary_tables WHERE import_table_name = ? AND description = ?", "customer_" + companyId + "_tbl", "Bulk recipient update");
		}
	}

	@Override
	public int getRecipientsAmountForTargetGroup(int companyId, int adminId, int mailingListId, String sqlTargetExpression) {
		if (companyId <= 0) {
			return 0;
		}

		List<Object> parameters = new ArrayList<>();
		String whereSubStatement = getWhereSubStatement(parameters, companyId, adminId, mailingListId, sqlTargetExpression);
		String wholeAmountStatement = String.format("SELECT COUNT(DISTINCT cust.customer_id) FROM %s cust", getCustomerTableName(companyId)) +
				whereSubStatement;

		return selectInt(logger, wholeAmountStatement, parameters.toArray());
	}

	/**
	 * <h2>Adds SQL restrictions for preventing retrieving recipients which bound to blocked mailing lists.</h2>
	 *
	 * <p>
	 * Notice, that recipients, which enters in few mailing list and some of this mailing lists are not blocked
	 * should be present in selection. In other words, recipient will be retrieved if it enters, at least,
	 * in one not blocked mailing list.
	 * </p>
	 *
	 * {@code mailingListId} variable represents either type of operation or mailing list id.
	 * <ul>
	 *     <li>if {@code mailingListId < -1} - retrieving recipients without any bindings to mailing lists;</li>
	 *     <li>if {@code mailingListId = 0} - retrieving booth recipients with bindings(except blocked)
	 *     		and without any bindings;</li>
	 *     <li>if {@code mailingListId > 0} - retrieving recipients with bindings
	 *     		to certain mailing list (except blocked).</li>
	 * </ul>
	 *
	 * @param parameters          - parameters of whole sql statements
	 * @param companyId           - id of current company
	 * @param adminId             - id of current admin
	 * @param mailingListId       - either type of operation or mailing list id
	 * @param sqlTargetExpression - sql from of some target groups
	 * @return
	 */
	protected String getWhereSubStatement(List<Object> parameters, int companyId, int adminId, int mailingListId, String sqlTargetExpression) {
		return getWhereSubStatement(parameters, companyId, adminId, mailingListId, sqlTargetExpression, false);
	}
	
	protected String getWhereSubStatement(List<Object> parameters, int companyId, int adminId, int mailingListId, String sqlTargetExpression, final boolean checkDisabledMailingLists) {
		String selectBindings = String.format("SELECT 1 FROM %s bind WHERE bind.customer_id = cust.customer_id", getCustomerBindingTableName(companyId));

		String whereSubStatement = " WHERE ";

		if (mailingListId > 0) {
			// options with certain mailing list in drop down
			// selects customers bound to a given mailing list unless they are also bound to at least one disallowed mailing list
			if (checkDisabledMailingLists) {
				whereSubStatement += "EXISTS (" + selectBindings + " AND bind.mailinglist_id = ?) ";
				parameters.add(mailingListId);
				parameters.add(companyId);
				parameters.add(adminId);
			} else {
				whereSubStatement += "EXISTS (" + selectBindings + " AND bind.mailinglist_id = ?)";
				parameters.add(mailingListId);
			}
		} else if (mailingListId < 0) {
			// option 'No mailing list' or 'None' in drop down
			// select all customers without bindings
			whereSubStatement += "NOT EXISTS (" + selectBindings + ")";
		} else {
			// if current admin has no restriction then we display all the customers without limitation
			whereSubStatement += "1 = 1";
		}

		// add target expression if any
		if (StringUtils.isNotBlank(sqlTargetExpression)) {
			whereSubStatement += String.format(" AND (%s)", sqlTargetExpression);
		}

		return whereSubStatement;
	}
	
	private static Object replaceEmptyStringsWithNull(Object value) {
		if (value instanceof String) {
			return StringUtils.trimToNull((String) value);
		}
		
		return value;
	}

	@Override
	public RecipientDates getRecipientDates(int companyId, int recipientId) {
		String sqlGetDates = "SELECT creation_date, lastsend_date, lastopen_date, lastclick_date FROM " + getCustomerTableName(companyId) + " WHERE customer_id = ?";
		return selectObjectDefaultNull(logger, sqlGetDates, new RecipientDatesRowMapper(), recipientId);
	}

	@Override
	public List<Integer> insertTestRecipients(int companyId, int mailingListId, UserStatus userStatus, String remark, List<String> addresses) throws Exception {
		if (companyId <= 0 || CollectionUtils.isEmpty(addresses)) {
			return Collections.emptyList();
		}

		// Normalize e-mails (to be compared to values in database), keep original collection untouched.
		addresses = addresses.stream().map(AgnUtils::normalizeEmail).collect(Collectors.toList());

		String sqlGetCustomers = "SELECT email, MIN(customer_id) AS customer_id " +
				"FROM " + getCustomerTableName(companyId) + " WHERE LOWER(email) IN (" +
				AgnUtils.repeatString("?", addresses.size(), ", ") +
				") GROUP BY email";

		Map<String, Integer> addressMap = new HashMap<>();
		query(logger, sqlGetCustomers, new EmailMapCallback(addressMap), addresses.toArray());

		if (addresses.size() != addressMap.size()) {
			for (String address : addresses) {
				if(!addressMap.containsKey(address)) {
					addressMap.put(address, insertTestRecipient(companyId, address));
				}
			}
		}

		List<Integer> customerIds = new ArrayList<>(addressMap.values());

		return customerIds;
	}

	@Override
	public String getEmail(int companyId, int customerId) {
		String query = String.format("SELECT email FROM %s WHERE customer_id = ?", getCustomerTableName(companyId));
		return selectWithDefaultValue(logger, query, String.class, null, customerId);
	}

	@Override
	public boolean checkAddressInUse(String email, int recipientId, int companyId) {
		if (StringUtils.isBlank(email) || companyId <= 0) {
			return false;
		}

		// Keep in mind that recipientId can be equal to 0.
		String sqlCheckAddressInUse = String.format("SELECT COUNT(*) FROM %s WHERE LOWER(email) = ? AND customer_id <> ?", getCustomerTableName(companyId));
		return selectInt(logger, sqlCheckAddressInUse, StringUtils.trimToEmpty(email).toLowerCase(), recipientId) > 0;
	}
	
	@Override
	public int getRecipientIdByAddress(String email, int recipientId, int companyId) {
		if (StringUtils.isBlank(email) || companyId <= 0) {
			return 0;
		}

		// Keep in mind that recipientId can be equal to 0.
		String sqlCheckAddressInUse = String.format("SELECT customer_id FROM %s WHERE LOWER(email) = ? AND customer_id <> ?", getCustomerTableName(companyId));
		if (isOracleDB()) {
		    sqlCheckAddressInUse += " AND ROWNUM=1";
        } else {
            sqlCheckAddressInUse += " LIMIT 1";
        }
		return selectInt(logger, sqlCheckAddressInUse, StringUtils.trimToEmpty(email).toLowerCase(), recipientId);
	}

	@Override
	public final void writeEmailAddressChangeRequest(final int companyID, final int customerID, final String newEmailAddress, final String confirmationCode) {
		final String sql = "INSERT INTO pending_email_change_tbl (company_ref, customer_ref, new_email_address, confirmation_code, creation_date) VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
		
		update(logger, sql, companyID, customerID, newEmailAddress, confirmationCode);
	}
	
	@Override
	public final String readEmailAddressForPendingChangeRequest(final int companyID, final int customerID, final String confirmationCode) {
		final String sql = "SELECT new_email_address FROM pending_email_change_tbl WHERE company_ref = ? AND customer_ref = ? AND confirmation_code = ?";

		final List<String> list = select(logger, sql, StringRowMapper.INSTANCE, companyID, customerID, confirmationCode);
		
		if(list.size() == 0) {
			return null;
		} else {
			return list.get(0);
		}
	}

	@Override
	public final void deletePendingEmailAddressChangeRequest(final int companyID, final int customerID, final String confirmationCode) {
		final String sql = "DELETE FROM pending_email_change_tbl WHERE company_ref = ? AND customer_ref = ? AND confirmation_code = ?";
		
		update(logger, sql, companyID, customerID, confirmationCode);
	}

    @Override
    public int getOrCreateRecipientOfAdmin(Admin admin) throws Exception {
        int recipientId = getRecipientIdByAddress(admin.getEmail(), -1, admin.getCompanyID());
        return recipientId > 0
                ? recipientId
                : insertTestRecipient(admin.getCompanyID(), admin.getEmail());
    }

	private int insertTestRecipient(int companyId, String address) throws Exception {
		Recipient recipient = recipientFactory.newRecipient();

		recipient.setCompanyID(companyId);
		recipient.getCustParameters().put("email", address);
		recipient.getCustParameters().put("mailtype", MailType.HTML.getIntValue());
		recipient.getCustParameters().put("gender", Title.GENDER_UNKNOWN);

		return insertNewCustWithException(recipient);
	}

	private static class RecipientDatesRowMapper implements RowMapper<RecipientDates> {
		@Override
		public RecipientDates mapRow(ResultSet rs, int i) throws SQLException {
			RecipientDates dates = new RecipientDates();

			dates.setCreationDate(rs.getTimestamp("creation_date"));
			dates.setLastSendDate(rs.getTimestamp("lastsend_date"));
			dates.setLastOpenDate(rs.getTimestamp("lastopen_date"));
			dates.setLastClickDate(rs.getTimestamp("lastclick_date"));

			return dates;
		}
	}

	public static class ComRecipientLite_RowMapper implements RowMapper<ComRecipientLiteImpl> {

		private final String columnPrefix;

		public ComRecipientLite_RowMapper(){
			columnPrefix = StringUtils.EMPTY;
		}

		public ComRecipientLite_RowMapper(String columnPrefix){
			this.columnPrefix = StringUtils.defaultString(columnPrefix, StringUtils.EMPTY);
		}

		@Override
		public ComRecipientLiteImpl mapRow(ResultSet resultSet, int row) throws SQLException {
			ComRecipientLiteImpl comRecipientLite = new ComRecipientLiteImpl();

			comRecipientLite.setId(resultSet.getInt(columnPrefix + "customer_id"));
			comRecipientLite.setEmail(resultSet.getString(columnPrefix + "email"));
			comRecipientLite.setFirstname(resultSet.getString(columnPrefix + "firstname"));
			comRecipientLite.setLastname(resultSet.getString(columnPrefix + "lastname"));

			return comRecipientLite;
		}
	}

	public static class ComRecipientReactionRowMapper implements RowMapper<ComRecipientReaction> {

		@Override
		public ComRecipientReaction mapRow(ResultSet rs, int rowNum) throws SQLException {
			ComRecipientReaction entry = new ComRecipientReactionImpl();
			entry.setTimestamp(rs.getTimestamp("timestamp"));
			entry.setMailingId(rs.getInt("mailing_id"));
			entry.setMailingName(rs.getString("mailing_name"));
			entry.setReactionType(ComRecipientReaction.ReactionType.getById(rs.getInt("action_type")));
			entry.setDeviceClass(rs.getString("device_class"));
			entry.setDeviceName(rs.getString("device_name"));

            try {
				// optional field
                entry.setClickedUrl(new URL(rs.getString("full_url")));
            } catch (@SuppressWarnings("unused") SQLException | MalformedURLException ignored) {
            	// do nothing
            }

			return entry;
		}
	}

	private static class EmailMapCallback implements RowCallbackHandler {
		private Map<String, Integer> addressMap;

		public EmailMapCallback(Map<String, Integer> addressMap) {
			this.addressMap = Objects.requireNonNull(addressMap);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			addressMap.put(rs.getString("email"), rs.getInt("customer_id"));
		}
	}

	private DatasourceDescription getDatasourceDescription(String description) throws Exception {
		DatasourceDescription datasourceDescription = datasourceDescriptionDao.getByDescription(SourceGroupType.Other, 0, description);
		if (Objects.isNull(datasourceDescription)) {
			throw new Exception("Missing datasourceDescription: " + description);
		}

		return datasourceDescription;
	}

	@Override
	public List<Integer> getMailingRecipientIds(int companyID, int mailinglistID, MediaTypes mediaTypes, String fullTargetSql, List<UserStatus> userstatusList) {
		String sql = "SELECT cust.customer_id FROM customer_" + companyID + "_tbl cust, customer_" + companyID + "_binding_tbl bind WHERE cust.customer_id = bind.customer_id AND bind.mailinglist_id = ? AND bind.mediatype = ?";
		if (StringUtils.isNotBlank(fullTargetSql)) {
			sql += " AND (" + fullTargetSql + ")";
		}
		List<Object> parameter = new ArrayList<>();
		parameter.add(mailinglistID);
		parameter.add(mediaTypes.getMediaCode());
		if (userstatusList != null && !userstatusList.isEmpty()) {
			sql += " AND user_status IN (" + AgnUtils.repeatString("?", userstatusList.size(), ", ") + ")";
			for (UserStatus userstatus : userstatusList) {
				parameter.add(userstatus.getStatusCode());
			}
		}
		return select(logger, sql, IntegerRowMapper.INSTANCE, parameter.toArray(new Object[0]));
	}

	@Override
	public List<CaseInsensitiveMap<String, Object>> getMailinglistRecipients(int companyID, int mailinglistID, MediaTypes mediaTypes, String targetSql, List<String> profileFieldsList, List<UserStatus> userstatusList, TimeZone timeZone) throws Exception {
		String fieldsPart = "customer_id";
		if (profileFieldsList != null && !profileFieldsList.isEmpty()) {
			fieldsPart = StringUtils.join(profileFieldsList, ", ");
		}
		
		String sql = "SELECT " + fieldsPart + " FROM customer_" + companyID + "_tbl WHERE customer_id in ("
			+ "SELECT cust.customer_id FROM customer_" + companyID + "_tbl cust, customer_" + companyID + "_binding_tbl bind WHERE cust.customer_id = bind.customer_id AND bind.mailinglist_id = ? AND bind.mediatype = ?";
		if (StringUtils.isNotBlank(targetSql)) {
			sql += " AND (" + targetSql + ")";
		}
		List<Object> parameter = new ArrayList<>();
		parameter.add(mailinglistID);
		parameter.add(mediaTypes.getMediaCode());
		if (userstatusList != null && !userstatusList.isEmpty()) {
			sql += " AND user_status IN (" + AgnUtils.repeatString("?", userstatusList.size(), ", ") + ")";
			for (UserStatus userstatus : userstatusList) {
				parameter.add(userstatus.getStatusCode());
			}
		}
		sql += ")";
		sql += " AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0";

		boolean respectHideSign = configService.getBooleanValue(ConfigValue.RespectHideDataSign, companyID);
		if (respectHideSign) {
			sql += " AND (hide <= 0 OR hide IS NULL)";
		}
		
		try {
			List<Map<String, Object>> result = select(logger, sql, parameter.toArray(new Object[0]));

			List<CaseInsensitiveMap<String, Object>> customerData = new ArrayList<>();
			for (Map<String, Object> row : result) {
				List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
				CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));
				ZoneId dbTimezone = ZoneId.systemDefault();
				
				CaseInsensitiveMap<String, Object> customerParameters = new CaseInsensitiveMap<>();

				for (Entry<String, Object> entry : row.entrySet()) {
					String columnName = entry.getKey();
					Object value = entry.getValue();

					if (recipientFieldsMap.containsKey(columnName)) {
						if (recipientFieldsMap.get(columnName).getSimpleDataType() == SimpleDataType.DateTime && timeZone != null) {
							if (value == null) {
								customerParameters.put(columnName, value);
							} else {
								Timestamp timestamp = new Timestamp(((Date) value).getTime());
								ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(timestamp.toInstant(), dbTimezone);
								ZonedDateTime exportZonedDateTime = dbZonedDateTime.withZoneSameInstant(timeZone.toZoneId());
								customerParameters.put(columnName, Date.from(exportZonedDateTime.toInstant()));
							}
						} else {
							customerParameters.put(columnName, value);
						}
					}
				}
				
				customerData.add(customerParameters);
			}
			
			return customerData;
		} catch (Exception e) {
			logger.error("getCustomerData: " + sql, e);
			throw e;
		}
	}

	@Override
	public void logMailingDelivery(int companyID, int maildropStatusID, int customerID, int mailingID) {
		update(logger, "INSERT INTO mailtrack_" + companyID + "_tbl (maildrop_status_id, customer_id, mailing_id, timestamp) VALUES (?, ?, ?, ?)", maildropStatusID, customerID, mailingID, new Date());
	}

	@Override
	public DbColumnType getColumnDataType(int companyId, String columnName) throws Exception {
		DbColumnType columnDataType = DbUtilities.getColumnDataType(getDataSource(), "customer_" + companyId + "_tbl", columnName);
		if (columnDataType != null) {
			return columnDataType;
		} else {
			throw new Exception("Unknown column '" + columnName + "' in company '" + companyId + "'");
		}
	}

	@Override
	public List<Integer> getRecipientIDs(int companyID, String keyColumn, String keyValue) {
		return select(logger, "SELECT customer_id FROM customer_" + companyID + "_tbl WHERE " + SafeString.getSafeDbColumnName(keyColumn) + " = ? AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0", IntegerRowMapper.INSTANCE, keyValue);
	}

	@Override
	public CaseInsensitiveMap<String, Object> getCustomerData(int companyID, int customerID) {
		String additionalWhereClause = " AND " + RecipientStandardField.Bounceload.getColumnName() + " = 0";

		boolean respectHideSign = configService.getBooleanValue(ConfigValue.RespectHideDataSign, companyID);
		if (respectHideSign) {
			additionalWhereClause += " AND (hide <= 0 OR hide IS NULL)";
		}

		CaseInsensitiveMap<String, Object> customerParameters = new CaseInsensitiveMap<>();

		String sql = "SELECT * FROM " + getCustomerTableName(companyID) + " WHERE customer_id = ?" + additionalWhereClause;

		try {
			List<Map<String, Object>> result = select(logger, sql, customerID);

			if (result.size() > 0) {
				List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
				CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));

				Map<String, Object> row = result.get(0);
				for (Entry<String, Object> entry : row.entrySet()) {
					String columnName = entry.getKey();
					Object value = entry.getValue();

					if (recipientFieldsMap.containsKey(columnName)) {
						customerParameters.put(columnName, value);
					}
				}
			}
		} catch (Exception e) {
			logger.error("getCustomerData: " + sql, e);
		}

		return customerParameters;
	}

	@Override
	public boolean isRecipientMatchTarget(int companyId, String targetExpression, int customerId) {
		return selectInt(logger, "SELECT COUNT(cust.customer_id) FROM customer_" + companyId + "_tbl cust WHERE cust.customer_id = ? AND (" + targetExpression + ")", customerId) > 0;
	}

	@Override
	public boolean isNotSavedRecipientDataMatchTarget(int companyId, int recipientId, String targetExpression, Map<String, Object> entry) throws Exception {
		if (companyId == 0) {
			return false;
		}

		List<String> columns = new ArrayList<>(entry.size());
		List<Object> sqlParameters = new ArrayList<>(entry.size());
		for (String column : entry.keySet()) {
			Object value = entry.get(column);
			if (column.equalsIgnoreCase(RecipientStandardField.CustomerID.getColumnName())) {
				sqlParameters.add(recipientId);
				columns.add("? AS " + SafeString.getSafeDbColumnName(column));
			} else if (column.equalsIgnoreCase(RecipientStandardField.ChangeDate.getColumnName()) ||
					column.equalsIgnoreCase(RecipientStandardField.CreationDate.getColumnName())) {
				if (value == null) {
					columns.add("CURRENT_TIMESTAMP AS " + SafeString.getSafeDbColumnName(column));
				} else {
					sqlParameters.add(value);
					columns.add("? AS " + SafeString.getSafeDbColumnName(column));
				}
			} else {
				sqlParameters.add(value);
				columns.add("? AS " + SafeString.getSafeDbColumnName(column));
			}
		}

		String sql = "SELECT COUNT(cust.customer_id) FROM (SELECT " + StringUtils.join(columns, ", ") + " FROM DUAL) cust WHERE " + targetExpression;

		return selectInt(logger, sql, sqlParameters.toArray()) > 0;
    }

	/**
	 * Update recipient data (NO INSERT)
	 * For insert of new recipients use insertNewCustWithException, because it checks and sets default values
	 * @return if save successfully returns recipient ID otherwise 0
	 * @throws Exception 
	 */
	@Override
	@DaoUpdateReturnValueCheck
	public int saveRecipient(int companyId, int recipientId, Map<String, Object> recipientValues) throws Exception {
		if (companyId == 0) {
			return 0;
		}

		if (recipientId > 0 && exist(recipientId, companyId)) {
			List<Object> sqlParameters = new ArrayList<>(recipientValues.size());
			StringBuilder dataPart = new StringBuilder();

			for (String column : recipientValues.keySet()) {
				//ignore customer id column
				if (!column.equalsIgnoreCase(RecipientStandardField.CustomerID.getColumnName())) {
					if (dataPart.length() > 0) {
						dataPart.append(", ");
					}

					if (column.equalsIgnoreCase(RecipientStandardField.ChangeDate.getColumnName())) {
						dataPart.append(SafeString.getSafeDbColumnName(column)).append(" = CURRENT_TIMESTAMP");
					} else {
						if (column.equalsIgnoreCase(RecipientStandardField.Email.getColumnName()) && !configService.getBooleanValue(ConfigValue.AllowUnnormalizedEmails, companyId)) {
							recipientValues.put(column, AgnUtils.normalizeEmail((String) recipientValues.get(column)));
						}
						
						dataPart.append(SafeString.getSafeDbColumnName(column)).append(" = ?");
                        sqlParameters.add(recipientValues.get(column));
					}
				}
			}
			String sql = "UPDATE " + getCustomerTableName(companyId) + " SET " + dataPart.toString() + " WHERE customer_id = ?";
			sqlParameters.add(recipientId);
			boolean updated = update(logger, sql, sqlParameters.toArray()) > 0;
			return updated ? recipientId : 0;
		} else {
			throw new Exception("Invalid call of saveRecipient for new recipient");
		}
	}

	@Override
	public List<Integer> listRecipientIdsByTargetGroup(final int companyId, final ComTarget target) {
		final String sql = String.format("SELECT customer_id FROM customer_%d_tbl cust WHERE %s", companyId, target.getTargetSQL());
		
		return select(logger, sql, IntegerRowMapper.INSTANCE);
	}

	@Override
	public List<Recipient> findByData(int companyID, Map<String, Object> searchDataMap) throws Exception {
		String selectSql = "SELECT * FROM customer_" + companyID + "_tbl WHERE " + RecipientStandardField.Bounceload.getColumnName() + " = 0";

		boolean respectHideSign = configService.getBooleanValue(ConfigValue.RespectHideDataSign, companyID);
		if (respectHideSign) {
			selectSql += " AND (hide <= 0 OR hide IS NULL)";
		}
		
		ArrayList<Object> sqlParametersForData = new ArrayList<>();
		
		for (Entry<String, Object> entry : searchDataMap.entrySet()) {
			selectSql += " AND " + SafeString.getSafeDbColumnName(entry.getKey()) + " = ?";
			sqlParametersForData.add(entry.getValue());
		}

		return getRecipientList(companyID, selectSql, sqlParametersForData.toArray(new Object[0]));
	}

	@Override
	/**
	 * TODO: check if the same!!! {@link com.agnitas.dao.impl.ComBindingEntryDaoImpl#get(int, int, int, int)}.
	 * */
	public BindingEntry getMailinglistBinding(int companyID, int customerID, int mailinglistId, int mediaType) throws Exception {
		List<Map<String, Object>> list = select(logger, "SELECT user_type, user_status, user_remark, timestamp, mediatype, exit_mailing_id FROM " + getCustomerBindingTableName(companyID) + " WHERE customer_id = ? AND mailinglist_id = ? AND mediatype = ?", customerID, mailinglistId, mediaType);
		if (list == null || list.isEmpty()) {
			return null;
		} else if (list.size() != 1) {
			throw new Exception ("Invalid number of binding entries (" + list.size() + ") found for customer: " + companyID + "/" + customerID);
		} else {
			Map<String, Object> map = list.get(0);
			
			BindingEntry bindingEntry = bindingEntryFactory.newBindingEntry();

			bindingEntry.setCustomerID(customerID);
			bindingEntry.setMailinglistID(mailinglistId);
			bindingEntry.setMediaType(mediaType);
			
			bindingEntry.setUserType((String) map.get("user_type"));
			bindingEntry.setUserStatus(((Number) map.get("user_status")).intValue());
			bindingEntry.setUserRemark((String) map.get("user_remark"));
			bindingEntry.setChangeDate((java.sql.Timestamp) map.get("timestamp"));
			if (map.get("exit_mailing_id") != null)	{
				bindingEntry.setExitMailingID(((Number) map.get("exit_mailing_id")).intValue());
			} else {
				bindingEntry.setExitMailingID(0);
			}
			
			return bindingEntry;
		}
	}

	@Override
	public int getMinimumCustomerId(int companyID) {
		return selectIntWithDefaultValue(logger, "SELECT MIN(customer_id) FROM customer_" + companyID + "_tbl", 0);
	}

	@Override
	public boolean isRecipientTrackingAllowed(int companyID, int recipientID) {
		final String sql = String.format("SELECT sys_tracking_veto FROM customer_%d_tbl WHERE customer_ID=?", companyID);
		
		final int doNotTrack = selectIntWithDefaultValue(logger, sql, 0, recipientID);

		return doNotTrack == 0;
	}

	@Override
	public List<CaseInsensitiveMap<String, Object>> getTargetRecipients(int companyID, String targetSql, List<String> profileFieldsList, TimeZone timeZone) throws Exception {
		String fieldsPart = "customer_id";
		if (profileFieldsList != null && !profileFieldsList.isEmpty()) {
			fieldsPart = StringUtils.join(profileFieldsList, ", ");
		}
		
		String sql = "SELECT " + fieldsPart + " FROM customer_" + companyID + "_tbl WHERE customer_id in ("
			+ "SELECT cust.customer_id FROM customer_" + companyID + "_tbl cust";
		
		if (targetSql != null && targetSql.contains("bind.")) {
			sql += " JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id";
		}
		
		sql += " WHERE " + RecipientStandardField.Bounceload.getColumnName() + " = 0 AND (" + targetSql + ")";
		sql += ")";
		
		List<Map<String, Object>> result = select(logger, sql);
		List<CaseInsensitiveMap<String, Object>> customerData = new ArrayList<>();
		for (Map<String, Object> row : result) {
			List<RecipientFieldDescription> recipientFields = recipientFieldService.getRecipientFields(companyID);
			CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap = new CaseInsensitiveMap<>(recipientFields.stream().collect(Collectors.toMap(RecipientFieldDescription::getColumnName, Function.identity())));
			ZoneId dbTimezone = ZoneId.systemDefault();
			
			CaseInsensitiveMap<String, Object> customerParameters = new CaseInsensitiveMap<>();

			for (Entry<String, Object> entry : row.entrySet()) {
				String columnName = entry.getKey();
				Object value = entry.getValue();

				if (recipientFieldsMap.containsKey(columnName)) {
					if (recipientFieldsMap.get(columnName).getSimpleDataType() == SimpleDataType.DateTime && timeZone != null) {
						if (value == null) {
							customerParameters.put(columnName, value);
						} else {
							Timestamp timestamp = new Timestamp(((Date) value).getTime());
							ZonedDateTime dbZonedDateTime = ZonedDateTime.ofInstant(timestamp.toInstant(), dbTimezone);
							ZonedDateTime exportZonedDateTime = dbZonedDateTime.withZoneSameInstant(timeZone.toZoneId());
							customerParameters.put(columnName, Date.from(exportZonedDateTime.toInstant()));
						}
					} else {
						customerParameters.put(columnName, value);
					}
				}
			}
			
			customerData.add(customerParameters);
		}
		
		return customerData;
	}

	@Override
	public List<Integer> getFilteredRecipientIDs(int companyID, CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap, Map<String, String> recipientFilters) {
		String sql = "SELECT customer_id FROM customer_" + companyID + "_tbl"
			+ " WHERE " + RecipientStandardField.Bounceload.getColumnName() + " = 0";
		List<Object> parameters = new ArrayList<>();
		for (Entry<String, String> parameter : recipientFilters.entrySet()) {
			if (recipientFieldsMap.get(parameter.getKey()).getSimpleDataType() == SimpleDataType.Date) {
				if (isOracleDB()) {
					sql += " AND TO_CHAR(" + SafeString.getSafeDbColumnName(parameter.getKey()) + ", 'YYYY-MM-DD') LIKE ?";
				} else {
					sql += " AND DATE_FORMAT(" + SafeString.getSafeDbColumnName(parameter.getKey()) + ", 'YYYY-MM-DD') LIKE ?";
				}
				parameters.add(parameter.getValue().replace("*", "%").replace("?", "_").replace("T", " "));
			} else if (recipientFieldsMap.get(parameter.getKey()).getSimpleDataType() == SimpleDataType.DateTime) {
				if (isOracleDB()) {
					sql += " AND TO_CHAR(" + SafeString.getSafeDbColumnName(parameter.getKey()) + ", 'YYYY-MM-DD HH24:MI:SS') LIKE ?";
				} else {
					sql += " AND DATE_FORMAT(" + SafeString.getSafeDbColumnName(parameter.getKey()) + ", '%Y-%m-%d %H:%i:%s') LIKE ?";
				}
				parameters.add(parameter.getValue().replace("*", "%").replace("?", "_").replace("T", " "));
			} else {
				sql += " AND " + SafeString.getSafeDbColumnName(parameter.getKey()) + " LIKE ?";
				parameters.add(parameter.getValue().replace("*", "%").replace("?", "_"));
			}
		}
		return select(logger, sql, IntegerRowMapper.INSTANCE, parameters.toArray(new Object[0]));
	}

	@Override
	public int countFilteredRecipientIDs(int companyID, CaseInsensitiveMap<String, RecipientFieldDescription> recipientFieldsMap, Map<String, String> recipientFilters) {
		String sql = "SELECT COUNT(*) FROM customer_" + companyID + "_tbl"
			+ " WHERE " + RecipientStandardField.Bounceload.getColumnName() + " = 0";
		List<Object> parameters = new ArrayList<>();
		for (Entry<String, String> parameter : recipientFilters.entrySet()) {
			if (recipientFieldsMap.get(parameter.getKey()).getSimpleDataType() == SimpleDataType.Date) {
				if (isOracleDB()) {
					sql += " AND TO_CHAR(" + SafeString.getSafeDbColumnName(parameter.getKey()) + ", 'YYYY-MM-DD') LIKE ?";
				} else {
					sql += " AND DATE_FORMAT(" + SafeString.getSafeDbColumnName(parameter.getKey()) + ", 'YYYY-MM-DD') LIKE ?";
				}
				parameters.add(parameter.getValue().replace("*", "%").replace("?", "_").replace("T", " "));
			} else if (recipientFieldsMap.get(parameter.getKey()).getSimpleDataType() == SimpleDataType.DateTime) {
				if (isOracleDB()) {
					sql += " AND TO_CHAR(" + SafeString.getSafeDbColumnName(parameter.getKey()) + ", 'YYYY-MM-DD HH24:MI:SS') LIKE ?";
				} else {
					sql += " AND DATE_FORMAT(" + SafeString.getSafeDbColumnName(parameter.getKey()) + ", '%Y-%m-%d %H:%i:%s') LIKE ?";
				}
				parameters.add(parameter.getValue().replace("*", "%").replace("?", "_").replace("T", " "));
			} else {
				sql += " AND " + SafeString.getSafeDbColumnName(parameter.getKey()) + " LIKE ?";
				parameters.add(parameter.getValue().replace("*", "%").replace("?", "_"));
			}
		}
		return selectInt(logger, sql, parameters.toArray(new Object[0]));
	}

	@Override
	public List<String> fetchRecipientNames(Set<Integer> ids, int companyID) {
		if (CollectionUtils.isEmpty(ids)) {
			return Collections.emptyList();
		}

		String nameQueryPart;
		if (isOracleDB()) {
			nameQueryPart = "CASE WHEN NVL(TRIM(firstname), '') IS NOT NULL AND NVL(TRIM(lastname), '') IS NOT NULL " +
					" THEN firstname || ' ' || lastname ELSE email END AS result";
		} else {
			nameQueryPart = "CASE WHEN (COALESCE(firstname, '') <> '' AND COALESCE(lastname, '') <> '') THEN CONCAT(firstname, ' ', lastname) " +
					" ELSE email END AS result";
		}

		String query = "SELECT " + nameQueryPart + " FROM customer_" + companyID + "_tbl " +
				"WHERE customer_id IN (" + AgnUtils.csvQMark(ids.size()) + ")";

		return select(logger, query, StringRowMapper.INSTANCE, ids.toArray());
	}

    @Override
    public int getBounceDetail(int mailingId, int recipientId, int companyId) {
        String sql = "SELECT detail FROM bounce_tbl" +
                " WHERE mailing_id = ? AND customer_id = ? AND company_id = ?" +
                " ORDER BY timestamp DESC";
        if (isOracleDB()) {
            sql = String.format("SELECT * FROM (%s) WHERE rownum = 1", sql);
        } else {
            sql += " LIMIT 1";
        }
        return selectInt(logger, sql, mailingId, recipientId, companyId);
    }

	@Override
	public int getNumberOfRecipients(int companyID, int mailinglistID, RecipientType... recipientTypes) {
		String sql = "SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust"
			+ " JOIN customer_" + companyID + "_binding_tbl bind ON cust.customer_id = bind.customer_id"
			+ " WHERE bind.user_status = ? AND bind.mailinglist_id = ?";
		List<Object> params = new ArrayList<>();
		params.add(UserStatus.Active.getStatusCode());
		params.add(mailinglistID);
		
		if (recipientTypes != null && recipientTypes.length > 0) {
			sql += " AND bind.user_type IN (" + StringUtils.repeat("?", ", ", recipientTypes.length) + ")";
			for (RecipientType recipientType : recipientTypes) {
				params.add(recipientType.getLetter());
			}
		}
				
		return selectInt(logger, sql, params.toArray());
	}
}
