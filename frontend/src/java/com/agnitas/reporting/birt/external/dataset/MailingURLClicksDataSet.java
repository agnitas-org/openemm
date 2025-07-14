/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.MailingClickStatsPerTargetRow;
import com.agnitas.beans.BindingEntry.UserType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS_INDEX;

/**
 * BIRT-DataSet for mailing url clicks statistics
 */
public class MailingURLClicksDataSet extends BIRTDataSet {

	private static String getTempTableName(int companyId) {
		return "tmp_report_aggregation_" + companyId + "_tbl";
	}

	private static String getCustomerTableName(int companyId) {
		return "customer_" + companyId + "_tbl";
	}

	public DateFormats getDateFormatsInstance(String startDate, String stopDate, Boolean hourScale) {
		return new DateFormats(startDate, stopDate, hourScale);
	}

	public int prepareReport(int mailingID, int companyID, String selectedTargets, String recipientsType) throws Exception {
		return prepareReport(mailingID, companyID, selectedTargets, null, recipientsType, new DateFormats());
	}

	public int prepareReport(int mailingID, int companyID, String selectedTargets, String hiddenTargetIdStr, String recipientsType, DateFormats dateFormats) throws Exception {
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
		updateRates(tempTableID, targetsList);

		return tempTableID;
	}

	private void insertTargetGroup(String targetName, String targetSql, int mailingID, int companyID, int tempTableID, int columnIndex, String recipientsType, String startDateString, String endDateString) throws Exception {
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
		Map<Integer, MailingClickStatsPerTargetRow> anonymousClickItems = new HashMap<>();

		updateAllLinks(columnIndex, mailingID, targetName, allDeviceClassesClickItems, mobileClickItems, anonymousClickItems);

		updateMeasureLinks(companyID, targetSql, recipientFilter, timeFilter, parameters, allDeviceClassesClickItems, false);
		updateMeasureLinks(companyID, targetSql, recipientFilter, timeFilter, parameters, mobileClickItems, true);
		updateMeasureLinks(companyID, targetSql, recipientFilter, timeFilter, parameters, anonymousClickItems, false, true);

		insertIntoTempTable(tempTableID, allDeviceClassesClickItems.values());
		insertIntoTempTable(tempTableID, mobileClickItems.values());
		insertIntoTempTable(tempTableID, anonymousClickItems.values());
	}

	private void updateMeasureLinks(int companyID, String targetSql, String recipientFilter, String timeFilter, List<Object> parameters, Map<Integer, MailingClickStatsPerTargetRow> clicksItems, boolean isMobile) {
        updateMeasureLinks(companyID, targetSql, recipientFilter, timeFilter, parameters, clicksItems, isMobile, false);
    }

	private void updateMeasureLinks(int companyID, String targetSql, String recipientFilter, String timeFilter, List<Object> parameters, Map<Integer, MailingClickStatsPerTargetRow> clicksItems, boolean isMobile, boolean isAnonymous) {
		StringBuilder sql = new StringBuilder("SELECT rlog.url_id, COUNT(*) AS clicks_gross, COUNT(DISTINCT rlog.customer_id) AS clicks_net");

		if ((recipientFilter != null && recipientFilter.contains("cust.")) || (targetSql != null && targetSql.contains("cust."))) {
			// Join customer table
			sql.append(" FROM ").append(getRdirLogTableName(companyID)).append(" rlog, ").append(getCustomerTableName(companyID)).append(" cust");
			sql.append(" WHERE rlog.customer_id = cust.customer_id AND rlog.customer_id != 0 AND rlog.mailing_id = ?");
		} else {
			sql.append(" FROM ").append(getRdirLogTableName(companyID)).append(" rlog");
			sql.append(" WHERE ").append(" rlog.mailing_id = ?");
		}

		if (isMobile) {
			sql.append(" AND rlog.device_class_id = ").append(DeviceClass.MOBILE.getId());
		}
        
        if (isAnonymous) {
            sql.append(" AND rlog.customer_id = 0");
        }

		if (StringUtils.isNotEmpty(targetSql)) {
			sql.append(" AND (").append(targetSql).append(")");
		}

		sql.append(recipientFilter)
				.append(timeFilter)
				.append(" GROUP BY rlog.url_id")
				.append(" ORDER BY rlog.url_id");

		List<Map<String, Object>> result = select(sql.toString(), parameters.toArray(new Object[0]));
		for (Map<String, Object> row : result) {
			int urlID = toInt(row.get("url_id"));
			addClickGrossAndNetValues(clicksItems.get(urlID), row);
		}
	}

	private void addClickGrossAndNetValues(MailingClickStatsPerTargetRow targetRow, Map<String, Object> row) {
		if (targetRow != null) {
			targetRow.addClicksGross(toInt(row.get("clicks_gross")));
			targetRow.addClicksNet(toInt(row.get("clicks_net")));
		}
	}

	private void updateAllLinks(int columnIndex, int mailingID, String targetName,
                                Map<Integer, MailingClickStatsPerTargetRow> allDeviceClassesClickItems,
                                Map<Integer, MailingClickStatsPerTargetRow> mobileClickItems,
                                Map<Integer, MailingClickStatsPerTargetRow> anonymousClickItems) {
		String queryAllLinks =
			"SELECT"
				+ " url_id,"
				+ " COALESCE(shortname, alt_text, full_url) AS url,"
				+ " admin_link,"
				+ " deleted"
			+ " FROM rdir_url_tbl"
			+ " WHERE mailing_id = ? AND deleted = 0"
			+ " ORDER BY url_id";

		List<Map<String, Object>> resultAllLinks = select(queryAllLinks, mailingID);
		for (Map<String, Object> row : resultAllLinks) {
			int urlID = toInt(row.get("url_id"));

			allDeviceClassesClickItems.put(urlID, getMailingClickStatByTargetRow(urlID, columnIndex, targetName, row, false));
			mobileClickItems.put(urlID, getMailingClickStatByTargetRow(urlID, columnIndex, targetName, row, true));
			anonymousClickItems.put(urlID, getAnonymousMailingClickStatByTargetRow(urlID, columnIndex, targetName, row));
		}
	}

	private MailingClickStatsPerTargetRow getAnonymousMailingClickStatByTargetRow(int urlId, int columnIndex, String targetName, Map<String,Object> row) {
        MailingClickStatsPerTargetRow anonymousRow = getMailingClickStatByTargetRow(urlId, columnIndex, targetName, row, false);
        anonymousRow.setAnonymous(true);
        return anonymousRow;
    }
    
	private MailingClickStatsPerTargetRow getMailingClickStatByTargetRow(int urlId, int columnIndex, String targetName, Map<String,Object> row, boolean isMobile) {
		MailingClickStatsPerTargetRow statsRow = new MailingClickStatsPerTargetRow();
		statsRow.setUrlId(urlId);
		statsRow.setMobile(isMobile);
		statsRow.setUrl((String) row.get("url"));
		statsRow.setAdminLink(toInt(row.get("admin_link")) == 1);
		statsRow.setColumnIndex(columnIndex);
		statsRow.setTargetgroup(targetName);
		statsRow.setDeleted(extractBooleanValue(row.get("deleted")));
		return statsRow;
	}

	public List<MailingClickStatsPerTargetRow> getUrlClicksData(int tempTableID) throws Exception {
		String select = "SELECT url, url_id, admin_link, target_group_index, target_group, clicks_gross, clicks_net, mobile, rate_gross, rate_net, deleted, anonymous FROM " + getTempTableName(tempTableID) + " ORDER BY clicks_gross DESC, target_group_index, url_id";
		return selectEmbedded(select, (resultSet, rowIndex) -> {
			MailingClickStatsPerTargetRow row = new MailingClickStatsPerTargetRow();

			row.setAdminLink(resultSet.getBigDecimal("admin_link").intValue() == 1);
			row.setUrl(resultSet.getString("url"));
			row.setUrlId(resultSet.getBigDecimal("url_id").intValue());
			row.setColumnIndex(resultSet.getBigDecimal("target_group_index").intValue());
			row.setClicksGross(resultSet.getBigDecimal("clicks_gross").intValue() );
			row.setClicksNet(resultSet.getBigDecimal("clicks_net").intValue());
			float rateGross = resultSet.getObject("rate_gross") == null ? 0 : resultSet.getBigDecimal("rate_gross").floatValue();
			row.setClicksGrossPercent(rateGross);
			float rateNet = resultSet.getObject("rate_net") == null ? 0 : resultSet.getBigDecimal("rate_net").floatValue();
			row.setClicksNetPercent(rateNet);
			row.setTargetgroup(resultSet.getString("target_group"));
			row.setRowIndex(rowIndex);
			row.setMobile(resultSet.getBigDecimal("mobile").intValue() == 1);
            row.setAnonymous(resultSet.getBigDecimal("anonymous").intValue() == 1);
            row.setDeleted(extractBooleanValue(resultSet.getBigDecimal("deleted")));

			return row;
		});
	}

	@DaoUpdateReturnValueCheck
	private void updateRates(int tempTableID, List<LightTarget> targets) throws Exception {
		for (int targetIndex = ALL_SUBSCRIBERS_INDEX; targetIndex <= targets.size() + 1; targetIndex++) {
			int totalGross = selectEmbeddedIntWithDefault("SELECT COALESCE(SUM(clicks_gross), 0) total_click_gross FROM " + getTempTableName(tempTableID) + " WHERE target_group_index = ?", 0, targetIndex);
			int totalNet = selectEmbeddedIntWithDefault("SELECT COALESCE(SUM(clicks_net), 0) total_click_net FROM " + getTempTableName(tempTableID) + " WHERE target_group_index = ?", 0, targetIndex);
	
			updateEmbedded("UPDATE " + getTempTableName(tempTableID) + " SET rate_gross = " + (totalGross > 0 ? "(" + "clicks_gross" + " * 1.0) / ?" : "?") + ", rate_net = " + (totalNet > 0 ? "(" + "clicks_net" + " * 1.0) / ?" : "?") + " WHERE target_group_index = ?", totalGross, totalNet, targetIndex);
		}
	}

	private boolean extractBooleanValue(Object intValue){
		return intValue != null && toInt(intValue) == 1;
	}

	@DaoUpdateReturnValueCheck
	private void insertIntoTempTable(int tempTableID, Collection<MailingClickStatsPerTargetRow> clicksList) throws Exception {
		String insertQuery = "INSERT INTO " + getTempTableName(tempTableID) + " (url, url_id, admin_link, target_group, target_group_index, clicks_gross, clicks_net, mobile, deleted, anonymous) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		List<Object[]> values = new ArrayList<>();
		for (MailingClickStatsPerTargetRow row : clicksList) {
			values.add(new Object[] {
				row.getUrl(),
				row.getUrlId(),
				row.isAdminLink() ? 1 : 0,
				row.getTargetgroup(),
				row.getColumnIndex(),
				row.getClicksGross(),
				row.getClicksNet(),
				row.isMobile() ? 1 : 0,
				row.isDeleted() ? 1 : 0,
				row.isAnonymous() ? 1 : 0
			});
		}
		batchupdateEmbedded(insertQuery, values);
	}

	/**
	 * create a temporary table to collect the values from different queries for private float clicks_gros_percent;
	private float clicks_net_percent;
	private int total_clicks_gros;
	private int total_clicks_net;
	 * the report in one table
	 *
	 * @return id of the create temporary table
	 */
	private int createTempTable() throws Exception {
		int tempTableID = getNextTmpID();
		executeEmbedded(
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
