/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.reporting.birt.external.dataset;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS_INDEX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.MailingClickStatsPerTargetRow;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.commons.lang3.time.DateUtils;

/**
 * BIRT-DataSet for mailing url clicks statistics
 */
public class MailingURLClicksDataSet extends BIRTDataSet {

	private enum ShareType {
		ALL(""),
		MOBILE("rlog.device_class_id = " + DeviceClass.MOBILE.getId()),
		ANONYMOUS("rlog.customer_id = 0");

		final String condition;

		ShareType(String condition) {
			this.condition = condition;
		}
	}

	private record TargetGroup(String name, String targetSql, int columnIndex) {
	}

	private record Filter(TargetGroup targetGroup, ShareType share, String condition) {
	}

	private record ClickCounts(int clicksGross, int clicksNet) {
		static final ClickCounts EMPTY = new ClickCounts(0, 0);
	}

	private record UrlInfo(String url, boolean adminLink, boolean deleted) {
	}

	private static String getTempTableName(int companyId) {
		return "tmp_report_aggregation_" + companyId + "_tbl";
	}

	private static String getCustomerTableName(int companyId) {
		return "customer_" + companyId + "_tbl";
	}

	public DateFormats getDateFormatsInstance(String startDate, String stopDate, Boolean hourScale) {
		return new DateFormats(startDate, stopDate, hourScale);
	}

	public int prepareReport(int mailingID, int companyID, String selectedTargets, String recipientsType) {
		return prepareReport(mailingID, companyID, selectedTargets, null, recipientsType, new DateFormats());
	}

	public int prepareReport(int mailingID, int companyID, String selectedTargets, String hiddenTargetIdStr, String recipientsType, DateFormats dateFormats) {
		int tempTableID = createTempTable();

		List<LightTarget> targetsList = getTargets(selectedTargets, companyID);
		String hiddenTargetSql = getTargetSqlString(hiddenTargetIdStr, companyID);

		List<TargetGroup> targetGroups = new ArrayList<>();
		targetGroups.add(new TargetGroup(CommonKeys.ALL_SUBSCRIBERS, null, ALL_SUBSCRIBERS_INDEX));
		int columnIndex = ALL_SUBSCRIBERS_INDEX;
		if (CollectionUtils.isNotEmpty(targetsList)) {
			for (LightTarget target : targetsList) {
				String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);
				targetGroups.add(new TargetGroup(target.getName(), resultTargetSql, ++columnIndex));
			}
		}

		insertAllTargetGroups(tempTableID, mailingID, companyID, targetGroups, recipientsType, dateFormats.getStartDate(), dateFormats.getStopDate());

		updateRates(tempTableID, targetsList);

		return tempTableID;
	}

	private void insertAllTargetGroups(int tempTableID, int mailingID, int companyID, List<TargetGroup> targetGroups, String recipientsType, String startDateString, String endDateString) {
		Map<Integer, UrlInfo> urlInfoById = loadUrlInfo(mailingID);

		List<Filter> filters = new ArrayList<>();
		for (TargetGroup targetGroup : targetGroups) {
			for (ShareType share : ShareType.values()) {
				filters.add(new Filter(targetGroup, share, combineConditions(targetGroup.targetSql(), share.condition)));
			}
		}

		Map<Filter, Map<Integer, ClickCounts>> countsByFilter = loadClickCounts(mailingID, companyID, recipientsType, startDateString, endDateString, filters);

		List<MailingClickStatsPerTargetRow> rows = new ArrayList<>();
		for (Filter filter : filters) {
			Map<Integer, ClickCounts> counts = countsByFilter.get(filter);

			for (Map.Entry<Integer, UrlInfo> entry : urlInfoById.entrySet()) {
				int urlId = entry.getKey();
				UrlInfo info = entry.getValue();
				ClickCounts clickCounts = counts.getOrDefault(urlId, ClickCounts.EMPTY);

				MailingClickStatsPerTargetRow row = new MailingClickStatsPerTargetRow();
				row.setUrlId(urlId);
				row.setUrl(info.url());
				row.setAdminLink(info.adminLink());
				row.setDeleted(info.deleted());
				row.setColumnIndex(filter.targetGroup().columnIndex());
				row.setTargetgroup(filter.targetGroup().name());
				row.setMobile(filter.share() == ShareType.MOBILE);
				row.setAnonymous(filter.share() == ShareType.ANONYMOUS);
				row.setClicksGross(clickCounts.clicksGross());
				row.setClicksNet(clickCounts.clicksNet());
				rows.add(row);
			}
		}

		insertIntoTempTable(tempTableID, rows);
	}

	private Map<Integer, UrlInfo> loadUrlInfo(int mailingID) {
		String queryAllLinks = """
				SELECT url_id, COALESCE(shortname, alt_text, full_url) AS url, admin_link, deleted
				FROM rdir_url_tbl
				WHERE mailing_id = ? AND deleted = 0
				ORDER BY url_id""";

		Map<Integer, UrlInfo> result = new LinkedHashMap<>();
		for (Map<String, Object> row : select(queryAllLinks, mailingID)) {
			int urlID = toInt(row.get("url_id"));
			result.put(urlID, new UrlInfo(
					(String) row.get("url"),
					toInt(row.get("admin_link")) == 1,
					extractBooleanValue(row.get("deleted"))
			));
		}
		return result;
	}

	private Map<Filter, Map<Integer, ClickCounts>> loadClickCounts(
			int mailingID, int companyID, String recipientsType, String startDateString, String endDateString, List<Filter> filters
	) {
		Map<Filter, Map<Integer, ClickCounts>> result = new LinkedHashMap<>();
		for (Filter filter : filters) {
			result.put(filter, new LinkedHashMap<>());
		}

		List<Object> parameters = new ArrayList<>();
		parameters.add(mailingID);

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
				parameters.add(parseDate(startDateString, DATE_PARAMETER_FORMAT_WITH_HOUR));
			} else {
				parameters.add(parseDate(startDateString, DATE_PARAMETER_FORMAT));
			}

			if (endDateString.contains(":")) {
				parameters.add(DateUtils.addHours(parseDate(endDateString, DATE_PARAMETER_FORMAT_WITH_HOUR), 1));
			} else {
				parameters.add(DateUtils.addDays(parseDate(endDateString, DATE_PARAMETER_FORMAT), 1));
			}
		}

		boolean needsCustomerJoin = containsCustomerCondition(recipientFilter)
				|| filters.stream().anyMatch(f -> containsCustomerCondition(f.condition()));

		StringBuilder sql = new StringBuilder("SELECT rlog.url_id");
		for (int i = 0; i < filters.size(); i++) {
			sql.append(", ").append(buildClickPivotColumns(filters.get(i).condition(), i));
		}
		sql.append(" FROM ").append(getRdirLogTableName(companyID)).append(" rlog");
		if (needsCustomerJoin) {
			sql.append(" LEFT JOIN ").append(getCustomerTableName(companyID)).append(" cust ON rlog.customer_id = cust.customer_id");
		}
		sql.append(" WHERE rlog.mailing_id = ?");
		sql.append(recipientFilter);
		sql.append(timeFilter);
		sql.append(" GROUP BY rlog.url_id ORDER BY rlog.url_id");

		for (Map<String, Object> row : select(sql.toString(), parameters.toArray(new Object[0]))) {
			int urlId = toInt(row.get("url_id"));
			for (int i = 0; i < filters.size(); i++) {
				int clicksGross = toInt(row.get("clicks_gross_" + i));
				if (clicksGross == 0) {
					continue;
				}
				int clicksNet = toInt(row.get("clicks_net_" + i));
				result.get(filters.get(i)).put(urlId, new ClickCounts(clicksGross, clicksNet));
			}
		}

		return result;
	}

	private String buildClickPivotColumns(String condition, int index) {
		if (StringUtils.isBlank(condition)) {
			return """
					COUNT(*) AS clicks_gross_%1$d,
					COUNT(DISTINCT rlog.customer_id) AS clicks_net_%1$d""".formatted(index);
		}
		return """
				COUNT(CASE WHEN (%2$s) THEN 1 END) AS clicks_gross_%1$d,
				COUNT(DISTINCT CASE WHEN (%2$s) THEN rlog.customer_id END) AS clicks_net_%1$d""".formatted(index, condition);
	}

	private static String combineConditions(String... conditions) {
		return Arrays.stream(conditions)
				.filter(StringUtils::isNotBlank)
				.map(c -> "(" + c + ")")
				.collect(Collectors.joining(" AND "));
	}

	private static boolean containsCustomerCondition(String sql) {
		return Strings.CS.contains(sql, "cust.");
	}

	public List<MailingClickStatsPerTargetRow> getUrlClicksData(int tempTableID) {
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
	private void updateRates(int tempTableID, List<LightTarget> targets) {
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
	private void insertIntoTempTable(int tempTableID, Collection<MailingClickStatsPerTargetRow> clicksList) {
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
	private int createTempTable() {
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
