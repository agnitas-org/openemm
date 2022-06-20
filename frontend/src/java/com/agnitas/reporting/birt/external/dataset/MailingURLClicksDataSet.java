/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.MailingClickStatsPerTargetRow;

/**
 * BIRT-DataSet for mailing url clicks statistics
 */
public class MailingURLClicksDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = LogManager.getLogger(MailingURLClicksDataSet.class);

	private static String getTempTableName(int companyId) {
		return "tmp_report_aggregation_" + companyId + "_tbl";
	}
	
	private static String getCustomerTableName(int companyId) {
		return "customer_" + companyId + "_tbl";
	}

	public int prepareReport(int mailingID, @VelocityCheck int companyID, String selectedTargets, String recipientsType) throws Exception {
		return prepareReport(mailingID, companyID, selectedTargets, null, recipientsType, new DateFormats());
	}

	public int prepareReport(int mailingID, @VelocityCheck int companyID, String selectedTargets, String recipientsType, DateFormats dateFormats) throws Exception {
		return prepareReport(mailingID, companyID, selectedTargets, null, recipientsType, dateFormats);
	}

	public int prepareReport(int mailingID, @VelocityCheck int companyID, String selectedTargets, String hiddenTargetIdStr, String recipientsType, DateFormats dateFormats) throws Exception {
		int tempTableID = createTempTable();

		insertTargetGroup(CommonKeys.ALL_SUBSCRIBERS, null, mailingID, companyID, tempTableID, CommonKeys.ALL_SUBSCRIBERS_INDEX, recipientsType, dateFormats.getStartDate(), dateFormats.getStopDate());
		List<LightTarget> targetsList = getTargets(selectedTargets, companyID);

		final String hiddenTargetSql = getTargetSqlString(hiddenTargetIdStr, companyID);

		if (CollectionUtils.isNotEmpty(targetsList)) {
			int columnIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
			for (LightTarget target : targetsList) {
				final String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);
				insertTargetGroup(target.getName(), resultTargetSql, mailingID, companyID, tempTableID, ++columnIndex, recipientsType, dateFormats.getStartDate(), dateFormats.getStopDate());
			}
		}
		updateRates(tempTableID);
				
		return tempTableID;
	}
	
	private void insertTargetGroup(final String targetName, final String targetSql, int mailingID, @VelocityCheck int companyID, int tempTableID, final int columnIndex, String recipientsType, String startDateString, String endDateString) throws Exception {
		List<Object> parameters = new ArrayList<>();
		List<Object> parametersAnonymous = new ArrayList<>();
		
		parameters.add(mailingID);
		parametersAnonymous.add(mailingID);
		
		String recipientFilter = "";
		if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
			recipientFilter = " AND cust.customer_id IN (SELECT DISTINCT customer_id FROM customer_" + companyID + "_binding_tbl"
				+ " WHERE user_type NOT IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')"
					+ " AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
			parameters.add(mailingID);
		} else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
			recipientFilter = " AND cust.customer_id IN (SELECT DISTINCT customer_id FROM customer_" + companyID + "_binding_tbl"
				+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')"
					+ " AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
			parameters.add(mailingID);
		}
		
		String timeFilter = "";
		if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
			timeFilter = " AND (? <= rlog.timestamp AND rlog.timestamp < ?)";
			if (startDateString.contains(":")) {
				parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
				parametersAnonymous.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
			} else {
				parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
				parametersAnonymous.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
			}
			
			if (endDateString.contains(":")) {
				parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
				parametersAnonymous.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
			} else {
				parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
				parametersAnonymous.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
			}
		}
		
		Map<Integer, MailingClickStatsPerTargetRow> allDeviceClassesClickItems = new HashMap<>();
		Map<Integer, MailingClickStatsPerTargetRow> mobileClickItems = new HashMap<>();
		
		updateAllLinks(columnIndex, mailingID, targetName, allDeviceClassesClickItems, mobileClickItems);
		
		updateMeasureLinks(companyID, targetSql, recipientFilter, timeFilter, parameters, allDeviceClassesClickItems, false);
		updateMeasureLinks(companyID, targetSql, recipientFilter, timeFilter, parameters, mobileClickItems, true);
		
		updateAnonymousLinks(companyID, timeFilter, parametersAnonymous, allDeviceClassesClickItems, false);
		updateAnonymousLinks(companyID, timeFilter, parametersAnonymous, mobileClickItems, true);
		
		insertIntoTempTable(tempTableID, allDeviceClassesClickItems.values());
		insertIntoTempTable(tempTableID, mobileClickItems.values());
	}
	
	private void updateAnonymousLinks(int companyID, String timeFilter, List<Object> parameters,
									  Map<Integer, MailingClickStatsPerTargetRow> clickItems, boolean isMobile) {
		StringBuilder sql = new StringBuilder("SELECT ");
		sql.append("rlog.url_id,")
				.append(" COUNT(*) AS anonymous")
				.append(" FROM ").append(getRdirLogTableName(companyID)).append(" rlog")
				.append(" WHERE rlog.customer_id = 0")
				.append(" AND rlog.mailing_id = ?");
		
		if(isMobile) {
			sql.append(" AND rlog.device_class_id = ").append(DeviceClass.MOBILE.getId());
		}
		
		sql.append(timeFilter)
				.append(" GROUP BY rlog.url_id")
				.append(" ORDER BY rlog.url_id");
		
		List<Map<String, Object>> resultAnonymous = select(logger, sql.toString(), parameters.toArray(new Object[0]));
		for (Map<String, Object> row : resultAnonymous) {
			int urlID = ((Number) row.get("url_id")).intValue();
			addAnonymousClicksValue(clickItems.get(urlID), row);
		}
	}
	
	private void updateMeasureLinks(int companyID, String targetSql, String recipientFilter, String timeFilter, List<Object> parameters, Map<Integer, MailingClickStatsPerTargetRow> clicksItems, boolean isMobile) {
		StringBuilder sql = new StringBuilder("SELECT rlog.url_id, COUNT(*) AS clicks_gross, COUNT(DISTINCT rlog.customer_id) AS clicks_net");
		
		if ((recipientFilter != null && recipientFilter.contains("cust.")) || (targetSql != null && targetSql.contains("cust."))) {
			// Join customer table
			sql.append(" FROM ").append(getRdirLogTableName(companyID)).append(" rlog, ").append(getCustomerTableName(companyID)).append(" cust");
			sql.append(" WHERE rlog.customer_id = cust.customer_id AND rlog.customer_id != 0 AND rlog.mailing_id = ?");
		} else {
			sql.append(" FROM ").append(getRdirLogTableName(companyID)).append(" rlog");
			sql.append(" WHERE rlog.customer_id != 0 AND rlog.mailing_id = ?");
		}
		
		if (isMobile) {
			sql.append(" AND rlog.device_class_id = ").append(DeviceClass.MOBILE.getId());
		}
		
		if (StringUtils.isNotEmpty(targetSql)) {
			sql.append(" AND (").append(targetSql).append(")");
		}
		
		sql.append(recipientFilter)
				.append(timeFilter)
				.append(" GROUP BY rlog.url_id")
				.append(" ORDER BY rlog.url_id");
		
		List<Map<String, Object>> result = select(logger, sql.toString(), parameters.toArray(new Object[0]));
		for (Map<String, Object> row : result) {
			int urlID = ((Number) row.get("url_id")).intValue();
			addClickGrossAndNetValues(clicksItems.get(urlID), row);
		}
	}
	
	private void addClickGrossAndNetValues(MailingClickStatsPerTargetRow targetRow, Map<String, Object> row) {
		if (targetRow != null) {
			targetRow.addClicks_gross(((Number) row.get("clicks_gross")).intValue());
			targetRow.addClicks_net(((Number) row.get("clicks_net")).intValue());
		}
	}
	
	private void addAnonymousClicksValue(MailingClickStatsPerTargetRow targetRow, Map<String, Object> row) {
		if (targetRow != null) {
			targetRow.addClicks_anonymous(((Number) row.get("anonymous")).intValue());
		}
	}
	
	private void updateAllLinks(int columnIndex, int mailingID, String targetName,
								Map<Integer, MailingClickStatsPerTargetRow> allDeviceClassesClickItems,
								Map<Integer, MailingClickStatsPerTargetRow> mobileClickItems) {
		String queryAllLinks =
			"SELECT"
				+ " url_id,"
				+ " COALESCE(shortname, alt_text, full_url) AS url,"
				+ " admin_link,"
				+ " deleted"
			+ " FROM rdir_url_tbl"
			+ " WHERE mailing_id = ? AND deleted = 0"
			+ " ORDER BY url_id";
		
		List<Map<String, Object>> resultAllLinks = select(logger, queryAllLinks, mailingID);
		for (Map<String, Object> row : resultAllLinks) {
			int urlID = ((Number) row.get("url_id")).intValue();
			
			allDeviceClassesClickItems.put(urlID, getMailingClickStatByTargetRow(urlID, columnIndex, targetName, row, false));
			mobileClickItems.put(urlID, getMailingClickStatByTargetRow(urlID, columnIndex, targetName, row, true));
		}
	}
	
	private MailingClickStatsPerTargetRow getMailingClickStatByTargetRow(int urlId, int columnIndex, String targetName, Map<String,Object> row, boolean isMobile) {
		MailingClickStatsPerTargetRow statsRow = new MailingClickStatsPerTargetRow();
		statsRow.setUrl_id(urlId);
		statsRow.setMobile(isMobile);
		statsRow.setUrl((String) row.get("url"));
		statsRow.setAdmin_link(((Number) row.get("admin_link")).intValue() == 1);
		statsRow.setColumn_index(columnIndex);
		statsRow.setTargetgroup(targetName);
		statsRow.setDeleted(extractBooleanValue(row.get("deleted")));
		return statsRow;
	}
	
	public List<MailingClickStatsPerTargetRow> getUrlClicksData(int tempTableID) throws Exception {
		String select = "SELECT url, url_id, admin_link, target_group_index, target_group, clicks_gross, clicks_net, mobile, rate_gross, rate_net, deleted, anonymous FROM " + getTempTableName(tempTableID) + " ORDER BY clicks_gross DESC, target_group_index, url_id";
		return selectEmbedded(logger, select, (resultSet, rowIndex) -> {
			MailingClickStatsPerTargetRow row = new MailingClickStatsPerTargetRow();
			
			row.setAdmin_link(resultSet.getBigDecimal("admin_link").intValue() == 1);
			row.setUrl(resultSet.getString("url"));
			row.setUrl_id(resultSet.getBigDecimal("url_id").intValue());
			row.setColumn_index(resultSet.getBigDecimal("target_group_index").intValue());
			row.setClicks_gross(resultSet.getBigDecimal("clicks_gross").intValue() );
			row.setClicks_net(resultSet.getBigDecimal("clicks_net").intValue());
			float rateGross = resultSet.getObject("rate_gross") == null ? 0 : resultSet.getBigDecimal("rate_gross").floatValue();
			row.setClicks_gross_percent(rateGross);
			float rateNet = resultSet.getObject("rate_net") == null ? 0 : resultSet.getBigDecimal("rate_net").floatValue();
			row.setClicks_net_percent(rateNet);
			row.setTargetgroup(resultSet.getString("target_group"));
			row.setRow_index(rowIndex);
			row.setMobile(resultSet.getBigDecimal("mobile").intValue() == 1);
			row.setDeleted(extractBooleanValue(resultSet.getBigDecimal("deleted")));
			row.setClicks_anonymous((resultSet.getBigDecimal("anonymous")).intValue());

			return row;
		});
	}

	@DaoUpdateReturnValueCheck
	private void updateRates(int tempTableID) throws Exception {
		List<Tuple<Integer, Integer>> result = selectEmbedded(logger,
			"SELECT COALESCE(SUM(clicks_gross), 0) total_click_gross, COALESCE(SUM(clicks_net), 0) total_click_net FROM " + getTempTableName(tempTableID),
			(resultSet, i) -> {
				int totalGross = resultSet.getBigDecimal("total_click_gross").intValue();
				int totalNet = resultSet.getBigDecimal("total_click_net").intValue();
				return new Tuple<>(totalGross, totalNet);
			});

		int totalGross = 0;
		int totalNet = 0;

		if (!result.isEmpty()) {
			totalGross = result.get(0).getFirst();
			totalNet = result.get(0).getSecond();
		}
		
		updateEmbedded(logger, "UPDATE " + getTempTableName(tempTableID) + " SET rate_gross = " + (totalGross > 0 ? "(" + "clicks_gross" + " * 1.0) / ?" : "?") + ", rate_net = " + (totalNet > 0 ? "(" + "clicks_net" + " * 1.0) / ?" : "?"), totalGross, totalNet);
	}

	private boolean extractBooleanValue(Object intValue){
		return intValue != null && (((Number) intValue).intValue() == 1);
	}

	@DaoUpdateReturnValueCheck
	private void insertIntoTempTable(int tempTableID, Collection<MailingClickStatsPerTargetRow> clicksList) throws Exception {
		String insertQuery = "INSERT INTO " + getTempTableName(tempTableID) + " (url, url_id, admin_link, target_group, target_group_index, clicks_gross, clicks_net, mobile, deleted, anonymous) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		List<Object[]> values = new ArrayList<>();
		for (MailingClickStatsPerTargetRow row : clicksList) {
			values.add(new Object[] {
				row.getUrl(),
				row.getUrl_id(),
				row.isAdmin_link() ? 1 : 0,
				row.getTargetgroup(),
				row.getColumn_index(),
				row.getClicks_gross(),
				row.getClicks_net(),
				row.isMobile() ? 1 : 0,
				row.isDeleted() ? 1 : 0,
				row.getClicks_anonymous()
			});
		}
		batchupdateEmbedded(logger, insertQuery, values);
	}
	
	/**
	 * create a temporary table to collect the values from different queries for private float clicks_gros_percent;
	private float clicks_net_percent;
	private int total_clicks_gros;
	private int total_clicks_net;
	 * the report in one table
	 * 
	 * @return id of the create temporary table
	 * @throws Exception
	 * @throws DataAccessException
	 */
	private int createTempTable() throws DataAccessException, Exception {
		int tempTableID = getNextTmpID();
		executeEmbedded(logger,
			"CREATE TABLE " + getTempTableName(tempTableID) + " ("
				+ "url VARCHAR(2000),"
				+ " url_id INTEGER,"
				+ " target_group VARCHAR(200),"
				+ " target_group_index INTEGER,"
				+ " admin_link INTEGER,"
				+ " mobile INTEGER,"
				+ " clicks_gross INTEGER,"
				+ " clicks_net INTEGER,"
				+ " rate_gross DOUBLE,"
				+ " rate_net DOUBLE,"
				+ " deleted INTEGER,"
				+ " anonymous INTEGER"
			+ ")");
		return tempTableID;
	}
}
