/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CLICKER;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CLICKER_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPENERS;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPENERS_INDEX;
import static com.agnitas.reporting.birt.external.utils.BirtReporUtils.BirtReportFigure.CLICKER_DEVICES;
import static com.agnitas.reporting.birt.external.utils.BirtReporUtils.BirtReportFigure.OPENERS_DEVICES;
import static com.agnitas.util.DbUtilities.isTautologicWhereClause;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.birtstatistics.device.bean.DeviceStatisticType;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.DevicesStatRow;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.MailingDevicesStatRow;
import com.agnitas.reporting.birt.external.utils.BirtReporUtils;
import com.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.jdbc.core.RowMapper;

public class DevicesStatDataSet extends BIRTDataSet {

	private static final int FILTER_WITHIN_DAYS = 90;

	/**
	 * Get openers device statistic data for selected mailings and is used in mailing_statistic.rptdesign and mailing_statistic_csv.rptdesign
	 */
	public List<MailingDevicesStatRow> getMailingReportDevicesStat(int companyId, String selectedTargets,
																   String hiddenTargetIdStr, String selectedMailings, String figuresOptions,
																   int devicesMax, String language) {
		List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);
		List<MailingDevicesStatRow> deviceStatistic = new ArrayList<>();

		List<Integer> mailingIds = parseCommaSeparatedIds(selectedMailings);
		if (CollectionUtils.isEmpty(mailingIds)) {
			return deviceStatistic;
		}

		//Collect target groups
		List<LightTarget> targets = new ArrayList<>();
		LightTarget allSubscribers = new LightTarget(); // All recipients
		allSubscribers.setId(0);
		allSubscribers.setName(I18nString.getLocaleString("statistic.all_subscribers", language));
		targets.add(allSubscribers);
		if (StringUtils.isNotBlank(selectedTargets)) {
			targets.addAll(getTargets(selectedTargets, companyId));
		}

		List<DeviceClass> deviceClasses = Arrays.asList(DeviceClass.DESKTOP, DeviceClass.TABLET, DeviceClass.MOBILE, DeviceClass.SMARTTV);
		if (figures.contains(OPENERS_DEVICES)) {
			List<MailingDevicesStatRow> openerDevices = collectDevicesStatistic(mailingIds, deviceClasses, targets, hiddenTargetIdStr, OPENERS_INDEX, OPENERS, companyId, devicesMax, language);
			deviceStatistic.addAll(openerDevices);
		}

		if (figures.contains(CLICKER_DEVICES)) {
			List<MailingDevicesStatRow> clickerDevices = collectDevicesStatistic(mailingIds, deviceClasses, targets, hiddenTargetIdStr, CLICKER_INDEX, CLICKER, companyId, devicesMax, language);
			deviceStatistic.addAll(clickerDevices);
		}

		return deviceStatistic;
	}

	private List<MailingDevicesStatRow> collectDevicesStatistic(List<Integer> mailingIds, List<DeviceClass> deviceClasses,
																List<LightTarget> targets, String hiddenTargetIdStr,
																int categoryIndex, String category, int companyId, int devicesMax, String language) {
		List<MailingDevicesStatRow> deviceStatistic = new ArrayList<>();
		for (int mailingId: mailingIds) {
			for (DeviceClass deviceClass: deviceClasses) {
				Map<Integer, Integer> totals = new HashMap<>();
				Map<Integer, Integer> rowSum = new HashMap<>();

				int targetIndex = 0;
				for (LightTarget target : targets) {
					String customerIdSubSelect = getCustomerIdSubSelect(companyId, target.getId(), hiddenTargetIdStr);
					if (categoryIndex == OPENERS_INDEX) {
						//get OPENER totals by device class and target group
						String sumSql = "SELECT COUNT(*) FROM " + getOnePixelLogDeviceTableName(companyId) + " WHERE mailing_id = ? AND device_class_id = ?";
						int sum = getMailingTotalSum(sumSql, mailingId, deviceClass, customerIdSubSelect);
						totals.put(targetIndex, sum);
					}

					if (categoryIndex == CLICKER_INDEX) {
						//get CLICKER totals by device class and target group
						String sumSql = "SELECT COUNT(*) FROM " + getRdirLogTableName(companyId) + " WHERE mailing_id = ? AND device_class_id = ?";
						int sum = getMailingTotalSum(sumSql, mailingId, deviceClass, customerIdSubSelect);
						totals.put(targetIndex, sum);
					}
					targetIndex++;
				}

				if (totals.get(0) > 0) {
					List<Object> sqlParameters = new ArrayList<>();
					String devicesSQL = "";
					if (categoryIndex == OPENERS_INDEX) {
						devicesSQL = generateOpenerDevicesSQL(companyId, mailingId, targets, hiddenTargetIdStr, deviceClass, devicesMax, sqlParameters);
					}

					if (categoryIndex == CLICKER_INDEX) {
						devicesSQL = generateClickerDevicesSQL(companyId, mailingId, targets, hiddenTargetIdStr, deviceClass, devicesMax, sqlParameters);
					}

					final List<MailingDevicesStatRow> devicesStat = getMailingDevicesStat(rowSum, devicesSQL, sqlParameters, totals, mailingId, deviceClass, categoryIndex, category, targets);
					deviceStatistic.addAll(devicesStat);

					//calculate Other row
                    String otherName = I18nString.getLocaleString("statistic.Other", language);
                    List<MailingDevicesStatRow> otherRows = calculateOtherDeviceRow(rowSum, totals, mailingId, deviceClass, otherName, categoryIndex, category, targets);
                    deviceStatistic.addAll(otherRows);
				}
			}
		}

		return deviceStatistic;
	}

	private List<MailingDevicesStatRow> calculateOtherDeviceRow(Map<Integer, Integer> devicesRowSum, Map<Integer, Integer> totals,
																int mailingId, DeviceClass deviceClass, String otherName,
																int categoryIndex, String category, List<LightTarget> targets) {
		List<MailingDevicesStatRow> otherRows = new ArrayList<>();

		int targetIndex = 0;
		for (LightTarget target : targets) {
			//sum of limited list by deviceMax param
			int sum = devicesRowSum.getOrDefault(targetIndex, 0);
			int total = totals.getOrDefault(targetIndex, 0);
			int otherValue = total - sum;

			if (total > 0 && otherValue > 0) {
				MailingDevicesStatRow row = new MailingDevicesStatRow();
				row.setMailingId(mailingId);
				row.setCategory(category);
				row.setCategoryIndex(categoryIndex);
				row.setTargetIndex(targetIndex);
				row.setTargetName(target.getName());
				row.setDeviceClassId(deviceClass.getId());
				row.setDeviceName(otherName);
				if (categoryIndex == OPENERS_INDEX) {
					row.setOpeningsCount(otherValue);
					row.setOpeningsRate((float) otherValue / total);
				}

				if (categoryIndex == CLICKER_INDEX) {
					row.setClicksCount(otherValue);
					row.setClicksRate((float) otherValue / total);
				}
				otherRows.add(row);
			}

			targetIndex++;
		}

		return otherRows;
	}

	private List<MailingDevicesStatRow> getMailingDevicesStat(Map<Integer, Integer> devicesRowSum, String sql, List<Object> sqlParameters,
															  Map<Integer, Integer> totals, int mailingId, DeviceClass deviceClass,
															  int categoryIndex, String category, List<LightTarget> targets) {
		final List<MailingDevicesStatRow> statistic = new ArrayList<>();

		query(sql, resultSet -> {
			int targetIndex = 0;
			for (LightTarget target : targets) {
				MailingDevicesStatRow row = new MailingDevicesStatRow();
				row.setMailingId(mailingId);
				row.setCategory(category);
				row.setCategoryIndex(categoryIndex);
				row.setTargetIndex(targetIndex);
				row.setTargetName(target.getName());
				row.setDeviceClassId(deviceClass.getId());
				row.setDeviceName(resultSet.getString("description"));

				int value = 0;
				int total = totals.getOrDefault(targetIndex, 0);
				if (categoryIndex == OPENERS_INDEX) {
					if (targetIndex == 0) {
						value = resultSet.getInt("openings");
					} else {
						value = resultSet.getInt("tg_" + target.getId());
					}

					row.setOpeningsCount(value);
					if (total > 0) {
						row.setOpeningsRate((float) value / total);
					}
				}

				if (categoryIndex == CLICKER_INDEX) {
					if (targetIndex == 0) {
						value = resultSet.getInt("clicks");
					} else {
						value = resultSet.getInt("tg_" + target.getId());
					}

					row.setClicksCount(value);
					if (total > 0) {
						row.setClicksRate((float) value / total);
					}
				}

				devicesRowSum.put(targetIndex, devicesRowSum.getOrDefault(targetIndex, 0) + value);

				statistic.add(row);
				targetIndex++;
			}
		}, sqlParameters.toArray());



		return statistic;
	}

	private int getMailingTotalSum(String initialSql, int mailingId, DeviceClass deviceClass, String customerIdSubSelect) {
		List<Object> parameters = new ArrayList<>();
		StringBuilder sqlBuilder = new StringBuilder(initialSql);
		parameters.add(mailingId);
		parameters.add(deviceClass.getId());

		if (StringUtils.isNotBlank(customerIdSubSelect)) {
			sqlBuilder.append(" AND ")
					.append(getFieldNameWithIdentifier("", "customer_id"))
					.append(" IN (").append(customerIdSubSelect).append(")");
		}

		return select(sqlBuilder.toString(), Integer.class, parameters.toArray());
	}

	private String getTargetCase(String tableIdentifier, String customerIdSubSelect, String targetName) {
		return " CASE " + " WHEN ( " + getFieldNameWithIdentifier(tableIdentifier, "customer_id")
				+ " IN (" + customerIdSubSelect + ") ) "
				+ " THEN COUNT(" + getFieldNameWithIdentifier(tableIdentifier, "customer_id") + ") " + " ELSE 0  "
				+ " END AS " + targetName + " ";
	}

	private String generateOpenerDevicesSQL(int companyId, int mailingId, List<LightTarget> targets, String hiddenTargetIdStr,
											DeviceClass deviceClass, int devicesMax, List<Object> sqlParameters) {
		List<String> sumTargetsParams = new ArrayList<>();
        List<String> caseTargetParams = new ArrayList<>();

        for (LightTarget target : targets) {
			String customerIdSubSelect = getCustomerIdSubSelect(companyId, target.getId(), hiddenTargetIdStr);
			if (StringUtils.isNotEmpty(customerIdSubSelect)) {
				String targetOpenerGroup = String.format("tg_%d_send", target.getId());
				String targetAggregation = String.format("tg_%d", target.getId());
				sumTargetsParams.add(String.format(" SUM(%s) AS %s", targetOpenerGroup, targetAggregation));
				caseTargetParams.add(getTargetCase("o", customerIdSubSelect, targetOpenerGroup));
			}
		}

		StringBuilder sqlBuilder = new StringBuilder();

		sqlBuilder
			.append("SELECT o.device_class_id, d.description AS description, COUNT(*) AS openings, 0 AS clicks");

		if (CollectionUtils.isNotEmpty(caseTargetParams)) {
			sqlBuilder.append(", ").append(StringUtils.join(caseTargetParams, ", "));
		}

		sqlBuilder.append(" FROM onepixellog_device_").append(companyId).append("_tbl o, device_tbl d")
				.append(" WHERE o.device_id = d.device_id AND o.device_class_id = ?")
				.append(" AND o.mailing_id = ?");
		sqlParameters.add(deviceClass.getId());
		sqlParameters.add(mailingId);

		sqlBuilder.append(" GROUP BY o.device_class_id, d.description, o.customer_id");

		StringBuilder mainSqlBuilder = new StringBuilder("SELECT device_class_id, description, SUM(openings) AS openings, SUM(clicks) AS clicks");

		if (CollectionUtils.isNotEmpty(sumTargetsParams)) {
			mainSqlBuilder.append(", ").append(StringUtils.join(sumTargetsParams, ", "));
		}

		mainSqlBuilder.append(" FROM (").append(sqlBuilder).append(") x GROUP BY device_class_id, description ORDER BY openings DESC");

		return getSqlWithLimitDevicesMaxClause(mainSqlBuilder, sqlParameters, devicesMax);
	}

	private String generateClickerDevicesSQL(int companyId, int mailingId, List<LightTarget> targets, String hiddenTargetIdStr,
											 DeviceClass deviceClass, int devicesMax, List<Object> sqlParameters) {
		List<String> sumTargetsParams = new ArrayList<>();
        List<String> caseTargetParams = new ArrayList<>();

        for (LightTarget target : targets) {
			String customerIdSubSelect = getCustomerIdSubSelect(companyId, target.getId(), hiddenTargetIdStr);
			if (StringUtils.isNotEmpty(customerIdSubSelect)) {
				String targetOpenerGroup = String.format("tg_%d_send", target.getId());
				String targetAggregation = String.format("tg_%d", target.getId());
				sumTargetsParams.add(String.format(" SUM(%s) AS %s", targetOpenerGroup, targetAggregation));
				caseTargetParams.add(getTargetCase("r", customerIdSubSelect, targetOpenerGroup));
			}
		}

		StringBuilder sqlBuilder = new StringBuilder();

		sqlBuilder
			.append("SELECT r.device_class_id AS device_class_id, d.description AS description, 0 AS openings, COUNT(*) AS clicks");

		if (CollectionUtils.isNotEmpty(caseTargetParams)) {
			sqlBuilder.append(", ").append(StringUtils.join(caseTargetParams, ", "));
		}

		sqlBuilder.append(" FROM rdirlog_").append(companyId).append("_tbl r, device_tbl d")
				.append(" WHERE r.device_id = d.device_id AND r.device_class_id = ?")
				.append(" AND r.mailing_id = ?");
		sqlParameters.add(deviceClass.getId());
		sqlParameters.add(mailingId);

		sqlBuilder.append(" GROUP BY r.device_class_id, d.description, r.customer_id");

		StringBuilder mainSqlBuilder = new StringBuilder("SELECT device_class_id, description, SUM(openings) AS openings, SUM(clicks) AS clicks");

		if (CollectionUtils.isNotEmpty(sumTargetsParams)) {
			mainSqlBuilder.append(", ").append(StringUtils.join(sumTargetsParams, ", "));
		}

		mainSqlBuilder.append(" FROM (").append(sqlBuilder).append(") x GROUP BY device_class_id, description ORDER BY clicks DESC");

		return getSqlWithLimitDevicesMaxClause(mainSqlBuilder, sqlParameters, devicesMax);
	}


	/**
	 * Get devices statistic data and is used in devices_overview.rptdesign
	 *
	 * @param companyId
	 * @param targetId
	 * @param mailingListId
	 * @param devicesMax
	 * @param language
	 * @param statTypeParam ID of statistic type @see{@link DeviceStatisticType}
	 * @return list of data set rows
	 */
	public List<DevicesStatRow> getDevicesStat(int companyId, int targetId, int mailingListId, int devicesMax, String language, int statTypeParam) {
		DeviceStatisticType statisticType = DeviceStatisticType.getById(statTypeParam);
		if (statisticType == null) {
			logger.error("Could not detect statistic type by id: " + statTypeParam);
			return new ArrayList<>();
		}
		
		return getDevicesStat(companyId, targetId, mailingListId, devicesMax, language, statisticType);
	}
	
	private List<DevicesStatRow> getDevicesStat(int companyId, int targetId, int mailingListId, int devicesMax, String language, DeviceStatisticType statType) {
		Date limitDate = DateUtilities.getDateOfDaysAgo(FILTER_WITHIN_DAYS);
		String unknownDeviceName = I18nString.getLocaleString("statistic.unknown_devices", language);
		
		String mailingIdSubSelect = getMailingIdSubSelect(mailingListId);
		String customerIdSubSelect = getCustomerIdSubSelect(companyId, targetId, null);
		
		List<Object> sqlParameters = new ArrayList<>();
		String sql = "";
		
		List<DevicesStatRow> rows = new ArrayList<>();
		switch (statType) {
			case END_DEVICES:
				sql = generateEndDeviceSQL(companyId, limitDate, mailingIdSubSelect, mailingListId, customerIdSubSelect, devicesMax, sqlParameters);
				break;
			case USER_CLIENT:
				sql = generateUserClientSQL(companyId, limitDate, mailingIdSubSelect, mailingListId, customerIdSubSelect, devicesMax, sqlParameters);
				break;
			default:
				//nothing
		}
		
		if (StringUtils.isNotEmpty(sql)) {
			rows = select(sql, new DeviceStatisticRowMapper(unknownDeviceName), sqlParameters.toArray());
		}

		if (CollectionUtils.isNotEmpty(rows)) {
			String openSumSql = "SELECT COUNT(*) FROM " + getOnePixelLogDeviceTableName(companyId) + " WHERE creation >= ?";
			int openingsSum = getTotalSum(openSumSql, limitDate, mailingIdSubSelect, mailingListId, customerIdSubSelect);
			if (openingsSum > 0) {
				for (DevicesStatRow row : rows) {
					row.setOpeningsRate((float) row.getOpeningsCount() / openingsSum);
				}
			}

			String clicksSql = "SELECT COUNT(*) FROM " + getRdirLogTableName(companyId) + " WHERE timestamp >= ?";
			int clicksSum = getTotalSum(clicksSql, limitDate, mailingIdSubSelect, mailingListId, customerIdSubSelect);
			if (clicksSum > 0) {
				for (DevicesStatRow row : rows) {
					row.setClicksRate((float) row.getClicksCount() / clicksSum);
				}
			}
		}

		return rows;
	}
	
	private int getTotalSum(String initialSql, Date limitDate, String mailingIdSubSelect, int mailingListId, String customerIdSubSelect) {
		List<Object> parameters = new ArrayList<>();
		StringBuilder sqlBuilder = new StringBuilder(initialSql);
		parameters.add(limitDate);
		
		addMailingAndCustomerClause(sqlBuilder, parameters, "", mailingIdSubSelect, mailingListId, customerIdSubSelect);
		
		return select(sqlBuilder.toString(), Integer.class, parameters.toArray());
	}
	
	private String generateUserClientSQL(int companyId, Date limitDate, String mailingIdSubSelect, int mailingListId, String customerIdSubSelect, int devicesMax, List<Object> sqlParameters) {
		StringBuilder sqlBuilder = new StringBuilder();

		sqlBuilder
			.append("SELECT cd.description AS description, COUNT(*) AS openings, 0 AS clicks")
			.append(" FROM ").append(getOnePixelLogDeviceTableName(companyId)).append(" o, client_tbl cd")
			.append(" WHERE o.creation >= ?")
			.append(" AND o.client_id > 0")
			.append(" AND o.client_id = cd.client_id");
		sqlParameters.add(limitDate);
		
		addMailingAndCustomerClause(sqlBuilder, sqlParameters, "o", mailingIdSubSelect, mailingListId, customerIdSubSelect);

		sqlBuilder.append(" GROUP BY cd.description");

		sqlBuilder.append(" UNION ALL ");

		sqlBuilder
			.append("SELECT cd.description AS description, 0 AS openings, COUNT(*) AS clicks")
			.append(" FROM ").append(getRdirLogTableName(companyId)).append(" r, client_tbl cd")
			.append(" WHERE r.timestamp >= ?")
			.append(" AND r.client_id > 0")
			.append(" AND r.client_id = cd.client_id");
		sqlParameters.add(limitDate);

		addMailingAndCustomerClause(sqlBuilder, sqlParameters, "r", mailingIdSubSelect, mailingListId, customerIdSubSelect);

		sqlBuilder.append(" GROUP BY cd.description");

		sqlBuilder = new StringBuilder("SELECT description, openings, clicks FROM (SELECT description, SUM(openings) AS openings, SUM(clicks) AS clicks, SUM(openings) + SUM(clicks) AS order_sum")
			.append(" FROM (").append(sqlBuilder).append(") x GROUP BY description) subsel ORDER BY order_sum DESC");

		return getSqlWithLimitDevicesMaxClause(sqlBuilder, sqlParameters, devicesMax);
	}


	private String generateEndDeviceSQL(int companyId, Date limitDate, String mailingIdSubSelect, int mailingListId, String customerIdSubSelect, int devicesMax, List<Object> sqlParameters) {
		StringBuilder sqlBuilder = new StringBuilder();

		sqlBuilder
			.append("SELECT d.description AS description, COUNT(*) AS openings, 0 AS clicks")
			.append(" FROM ").append(getOnePixelLogDeviceTableName(companyId)).append(" o, device_tbl d")
			.append(" WHERE o.creation >= ?")
			.append(" AND o.device_id > 0")
			.append(" AND o.device_id = d.device_id");
		sqlParameters.add(limitDate);

		addMailingAndCustomerClause(sqlBuilder, sqlParameters, "o", mailingIdSubSelect, mailingListId, customerIdSubSelect);

		sqlBuilder.append(" GROUP BY d.description");

		sqlBuilder.append(" UNION ALL ");

		sqlBuilder
			.append("SELECT d.description AS description, 0 AS openings, COUNT(*) AS clicks")
			.append(" FROM ").append(getRdirLogTableName(companyId)).append(" r, device_tbl d")
			.append(" WHERE r.timestamp >= ?")
			.append(" AND r.device_id > 0")
			.append(" AND r.device_id = d.device_id");
		sqlParameters.add(limitDate);

		addMailingAndCustomerClause(sqlBuilder, sqlParameters, "r", mailingIdSubSelect, mailingListId, customerIdSubSelect);

		sqlBuilder.append(" GROUP BY d.description");

		sqlBuilder = new StringBuilder("SELECT description, openings, clicks FROM (SELECT description, SUM(openings) AS openings, SUM(clicks) AS clicks, SUM(openings) + SUM(clicks) AS order_sum")
			.append(" FROM (").append(sqlBuilder).append(") x GROUP BY description) subsel ORDER BY order_sum DESC");

		return getSqlWithLimitDevicesMaxClause(sqlBuilder, sqlParameters, devicesMax);
	}
	
	private void addMailingAndCustomerClause(StringBuilder builder, List<Object> parameters, String tableIdentifier, String mailingIdSubSelect, int mailingListId, String customerIdSubSelect) {
		if (StringUtils.isNotBlank(mailingIdSubSelect)) {
			builder.append(" AND ")
					.append(getFieldNameWithIdentifier(tableIdentifier, "mailing_id"))
					.append(" IN (").append(mailingIdSubSelect).append(")");
			
			parameters.add(mailingListId);
		}
		
		if (StringUtils.isNotBlank(customerIdSubSelect)) {
			builder.append(" AND ")
					.append(getFieldNameWithIdentifier(tableIdentifier, "customer_id"))
					.append(" IN (").append(customerIdSubSelect).append(")");
		}
	}
	
	private String getFieldNameWithIdentifier(String tableIdentifier, String fieldName) {
		if (StringUtils.isEmpty(tableIdentifier)) {
			return fieldName;
		}
		
		return tableIdentifier + "." + fieldName;
	}
	
	private String getSqlWithLimitDevicesMaxClause(StringBuilder sqlBuilder, List<Object> sqlParameters, int devicesMax) {
		if (devicesMax > 0) {
			if (isOracleDB()) {
				sqlBuilder = new StringBuilder("SELECT * FROM (").append(sqlBuilder).append(") z WHERE ROWNUM <= ?");
			} else {
				sqlBuilder.append(" LIMIT ?");
			}
			sqlParameters.add(devicesMax);
		}
		
		return sqlBuilder.toString();
	}
	
	private String getCustomerIdSubSelect(int companyId, int targetId, String hiddenTargetIdStr) {
		String targetExpression = "";
		if (targetId > 0) {
			targetExpression = getTargetSqlString(String.valueOf(targetId), companyId);
		}

		String hiddenTargetExpression = null;
		if(StringUtils.isNotBlank(hiddenTargetIdStr)) {
			hiddenTargetExpression = getTargetSqlString(hiddenTargetIdStr, companyId);
		}

		final String resultExpression = joinWhereClause(targetExpression, hiddenTargetExpression);
		
		// Check for obsolete "1=1"
		if (!isTautologicWhereClause(resultExpression)) {
			return "SELECT cust.customer_id FROM customer_" + companyId + "_tbl cust WHERE " + resultExpression;
		}
		
		return "";
	}
	
	private String getMailingIdSubSelect(int mailingListId) {
		if (mailingListId > 0) {
			return "SELECT mailing_id FROM mailing_tbl WHERE mailinglist_id = ?";
		}
		return "";
	}
	
	private class DeviceStatisticRowMapper implements RowMapper<DevicesStatRow> {
		
		private String unknownDeviceName;
		
		DeviceStatisticRowMapper(String unknownDeviceName) {
			this.unknownDeviceName = unknownDeviceName;
		}
		
		@Override
		public DevicesStatRow mapRow(ResultSet resultSet, int i) throws SQLException {
			DevicesStatRow devicesStatRow = new DevicesStatRow();
			String deviceName = StringUtils.defaultIfEmpty(resultSet.getString("description"), unknownDeviceName);
			devicesStatRow.setDeviceName(deviceName);
			devicesStatRow.setOpeningsCount(resultSet.getInt("openings"));
			devicesStatRow.setClicksCount(resultSet.getInt("clicks"));
			return devicesStatRow;
		}
	}


	/**
	 * Get device statistic data for selected mailing and is used in mailing_devices_overview.rptdesign
	 *
	 * @param companyId
	 * @param selectedTargets
	 * @param mailingId
	 * @param devicesMax
	 * @param language
	 * @return list of data set rows
	 * @throws Exception
	 */
	public List<MailingDevicesStatRow> getMailingDevicesStat(int companyId, String selectedTargets, int mailingId, int devicesMax, String language) throws Exception {
		String unknownDeviceName = I18nString.getLocaleString("statistic.unknown_devices", language);
		List<MailingDevicesStatRow> returnList = new LinkedList<>();

		List<LightTarget> targets = new ArrayList<>();
		LightTarget allSubscribers = new LightTarget(); // All recipients
		allSubscribers.setId(0);
		allSubscribers.setName(I18nString.getLocaleString("statistic.all_subscribers", language));
		targets.add(allSubscribers);
		if (StringUtils.isNotBlank(selectedTargets)) {
			targets.addAll(getTargets(selectedTargets, companyId));
		}

		List<Integer> totals = new ArrayList<>(targets.size());
		for (LightTarget target : targets) {
			if (target.getId() == 0) {
				// All subscribers
				totals.add(getNumberSentMailings(companyId, mailingId, CommonKeys.TYPE_WORLDMAILING, null, null, null));
			} else {
				// selected target group
				totals.add(getNumberSentMailings(companyId, mailingId, CommonKeys.TYPE_WORLDMAILING, target.getTargetSQL(), null, null));
			}
		}

		List<Object> sqlParameters = new ArrayList<>();
		String deviceMailingSQL = generateDeviceMailingSQL(companyId, mailingId, null, devicesMax, sqlParameters);

		// Add data for BIRT pie charts for "All Subscribers" only
		List<Map<String, Object>> chartStat = select(deviceMailingSQL, sqlParameters.toArray());
		for (Map<String, Object> row : chartStat) {
			MailingDevicesStatRow devRow = new MailingDevicesStatRow();
			devRow.setCategoryIndex(0);
			String deviceName = (String) row.get("description");
			if (StringUtils.isBlank(deviceName)) {
				deviceName = unknownDeviceName;
			}
			devRow.setDeviceName(deviceName);
			devRow.setOpeningsCount(toInt(row.get("openings")));
			devRow.setClicksCount(toInt(row.get("clicks")));
			devRow.setDeviceClassId(toInt(row.get("device_class_id")));

			returnList.add(devRow);
		}

		// Add data for BIRT cross tables
		for (int tgIdx = 0; tgIdx < targets.size(); tgIdx++) {
			List<Object> targetSqlParameters = new ArrayList<>();
			DeviceMailingStatisticRowMapper rowMapper = new DeviceMailingStatisticRowMapper(mailingId, unknownDeviceName, tgIdx, targets.get(tgIdx).getName());
			String targetDeviceStat = generateDeviceMailingSQL(companyId, mailingId, targets.get(tgIdx).getTargetSQL(), devicesMax, targetSqlParameters);

			List<MailingDevicesStatRow> devicesStatRowsByTarget = select(targetDeviceStat, rowMapper, targetSqlParameters.toArray());

			int totalSubscribers = totals.get(tgIdx);
			if (totalSubscribers != 0) {
				devicesStatRowsByTarget.forEach(row -> {
					row.setOpeningsRate((float) row.getOpeningsCount() / totalSubscribers);
					row.setClicksRate((float) row.getClicksCount() / totalSubscribers);
				});
			}

			returnList.addAll(devicesStatRowsByTarget);
		}
		return returnList;
	}

	private String generateDeviceMailingSQL(int companyId, int mailingId, String sqlTargetExpression, int devicesMax, List<Object> sqlParameters) {
		StringBuilder sqlBuilder = new StringBuilder();

		sqlBuilder
			.append("SELECT o.device_class_id, d.description AS description, COUNT(*) AS openings, 0 AS clicks")
			.append(" FROM onepixellog_device_").append(companyId).append("_tbl o, device_tbl d");

		if (StringUtils.isNotEmpty(sqlTargetExpression)) {
			sqlBuilder.append(", customer_").append(companyId).append("_tbl cust");
		}

		sqlBuilder.append(" WHERE o.device_id = d.device_id")
			.append(" AND o.mailing_id = ?");
		sqlParameters.add(mailingId);

		if (StringUtils.isNotEmpty(sqlTargetExpression)) {
			sqlBuilder.append(" AND (").append(sqlTargetExpression).append(")");
		}

		sqlBuilder.append(" GROUP BY o.device_class_id, d.description");

		sqlBuilder.append(" UNION ALL ");

		sqlBuilder
			.append("SELECT r.device_class_id AS device_class_id, d.description AS description, 0 AS openings, COUNT(*) AS clicks")
			.append(" FROM rdirlog_").append(companyId).append("_tbl r, device_tbl d");

		if (StringUtils.isNotEmpty(sqlTargetExpression)) {
			sqlBuilder.append(", customer_").append(companyId).append("_tbl cust");
		}

		sqlBuilder.append(" WHERE r.device_id = d.device_id")
			.append(" AND r.mailing_id = ?");
		sqlParameters.add(mailingId);

		if (StringUtils.isNotEmpty(sqlTargetExpression)) {
			sqlBuilder.append(" AND (").append(sqlTargetExpression).append(")");
		}

		sqlBuilder.append(" GROUP BY r.device_class_id, d.description");

		sqlBuilder = new StringBuilder("SELECT device_class_id, description, openings, clicks FROM (SELECT device_class_id, description, SUM(openings) AS openings, SUM(clicks) AS clicks, SUM(openings) + SUM(clicks) AS order_sum")
			.append(" FROM (").append(sqlBuilder).append(") x GROUP BY device_class_id, description) subsel ORDER BY order_sum DESC");

		return getSqlWithLimitDevicesMaxClause(sqlBuilder, sqlParameters, devicesMax);
	}

	private class DeviceMailingStatisticRowMapper implements RowMapper<MailingDevicesStatRow> {

		private String unknownDeviceName;
		private int targetIndex;
		private String targetName;
		private int mailingId;

		public DeviceMailingStatisticRowMapper(int mailingId, String unknownDeviceName, int targetIndex, String targetName) {
			this.mailingId = mailingId;
			this.unknownDeviceName = unknownDeviceName;
			this.targetIndex = targetIndex;
			this.targetName = targetName;
		}

		@Override
		public MailingDevicesStatRow mapRow(ResultSet resultSet, int i) throws SQLException {
			MailingDevicesStatRow devRow = new MailingDevicesStatRow();
			devRow.setCategoryIndex(1);
			devRow.setTargetIndex(targetIndex);
			devRow.setTargetName(targetName);
			devRow.setMailingId(mailingId);
			devRow.setDeviceClassId(resultSet.getInt("device_class_id"));
			String deviceName = StringUtils.defaultString(resultSet.getString("description"), unknownDeviceName);
			devRow.setDeviceName(deviceName);
			int openings = resultSet.getInt("openings");
			int clicks = resultSet.getInt("clicks");
			devRow.setOpeningsCount(openings);
			devRow.setClicksCount(clicks);

			return devRow;
		}
	}


}
