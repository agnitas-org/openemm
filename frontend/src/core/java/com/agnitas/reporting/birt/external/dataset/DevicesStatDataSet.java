/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.reporting.birt.external.dataset;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CLICKER;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CLICKER_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPENERS;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPENERS_INDEX;
import static com.agnitas.reporting.birt.external.utils.BirtReportUtils.BirtReportFigure.CLICKER_DEVICES;
import static com.agnitas.reporting.birt.external.utils.BirtReportUtils.BirtReportFigure.OPENERS_DEVICES;
import static com.agnitas.util.DbUtilities.isTautologicWhereClause;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.MailingDevicesStatRow;
import com.agnitas.reporting.birt.external.utils.BirtReportUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

public class DevicesStatDataSet extends BIRTDataSet {

	/**
	 * Get openers device statistic data for selected mailings and is used in mailing_statistic.rptdesign and mailing_statistic_csv.rptdesign
	 */
	public List<MailingDevicesStatRow> getMailingReportDevicesStat(int companyId, String selectedTargets,
																   String hiddenTargetIdStr, String selectedMailings, String figuresOptions,
																   int devicesMax, String language) {
		List<BirtReportUtils.BirtReportFigure> figures = BirtReportUtils.unpackFigures(figuresOptions);
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
		String tableName = categoryIndex == OPENERS_INDEX ? getOnePixelLogDeviceTableName(companyId) : getRdirLogTableName(companyId);

		for (int mailingId: mailingIds) {
			for (DeviceClass deviceClass: deviceClasses) {
				Map<Integer, Integer> rowSum = new HashMap<>();
				Map<Integer, Integer> totals = getMailingTotals(tableName, mailingId, deviceClass, targets, hiddenTargetIdStr, companyId);

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

	private Map<Integer, Integer> getMailingTotals(String tableName, int mailingId, DeviceClass deviceClass,
													List<LightTarget> targets, String hiddenTargetIdStr, int companyId) {
		Map<Integer, Integer> totals = new HashMap<>();
		Map<Integer, String> targetColumnByIndex = new HashMap<>();
		List<Object> parameters = new ArrayList<>();

		StringBuilder sqlBuilder = new StringBuilder("SELECT COUNT(*) AS total_count");

		int targetIndex = 0;
		for (LightTarget target : targets) {
			String customerIdSubSelect = getCustomerIdSubSelect(companyId, target.getId(), hiddenTargetIdStr);
			if (StringUtils.isNotEmpty(customerIdSubSelect)) {
				String columnAlias = "tg_" + target.getId();
				sqlBuilder.append(", COALESCE(SUM(CASE WHEN customer_id IN (")
						.append(customerIdSubSelect).append(") THEN 1 ELSE 0 END), 0) AS ").append(columnAlias);
				targetColumnByIndex.put(targetIndex, columnAlias);
			}
			targetIndex++;
		}

		sqlBuilder.append(" FROM ").append(tableName).append(" WHERE mailing_id = ? AND device_class_id = ?");
		parameters.add(mailingId);
		parameters.add(deviceClass.getId());

		Map<String, Object> row = selectSingleRow(sqlBuilder.toString(), parameters.toArray());
		int totalCount = toInt(row.get("total_count"));

		for (int idx = 0; idx < targets.size(); idx++) {
			String columnAlias = targetColumnByIndex.get(idx);
			totals.put(idx, columnAlias != null ? toInt(row.get(columnAlias)) : totalCount);
		}

		return totals;
	}

	private String getTargetCase(String tableIdentifier, String customerIdSubSelect, String targetName) {
		return " SUM(CASE WHEN ( " + getFieldNameWithIdentifier(tableIdentifier, "customer_id")
				+ " IN (" + customerIdSubSelect + ") ) THEN 1 ELSE 0 END) AS " + targetName + " ";
	}

	private String generateOpenerDevicesSQL(int companyId, int mailingId, List<LightTarget> targets, String hiddenTargetIdStr,
											DeviceClass deviceClass, int devicesMax, List<Object> sqlParameters) {
        List<String> caseTargetParams = new ArrayList<>();

        for (LightTarget target : targets) {
			String customerIdSubSelect = getCustomerIdSubSelect(companyId, target.getId(), hiddenTargetIdStr);
			if (StringUtils.isNotEmpty(customerIdSubSelect)) {
				caseTargetParams.add(getTargetCase("o", customerIdSubSelect, String.format("tg_%d", target.getId())));
			}
		}

		StringBuilder sqlBuilder = new StringBuilder();

		sqlBuilder
			.append("SELECT o.device_class_id AS device_class_id, d.description AS description, COUNT(*) AS openings, 0 AS clicks");

		if (CollectionUtils.isNotEmpty(caseTargetParams)) {
			sqlBuilder.append(", ").append(StringUtils.join(caseTargetParams, ", "));
		}

		sqlBuilder.append(" FROM onepixellog_device_").append(companyId).append("_tbl o, device_tbl d")
				.append(" WHERE o.device_id = d.device_id AND o.device_class_id = ?")
				.append(" AND o.mailing_id = ?");
		sqlParameters.add(deviceClass.getId());
		sqlParameters.add(mailingId);

		sqlBuilder.append(" GROUP BY o.device_class_id, d.description");
		sqlBuilder.append(" ORDER BY openings DESC");

		return getSqlWithLimitDevicesMaxClause(sqlBuilder, sqlParameters, devicesMax);
	}

	private String generateClickerDevicesSQL(int companyId, int mailingId, List<LightTarget> targets, String hiddenTargetIdStr,
											 DeviceClass deviceClass, int devicesMax, List<Object> sqlParameters) {
        List<String> caseTargetParams = new ArrayList<>();

        for (LightTarget target : targets) {
			String customerIdSubSelect = getCustomerIdSubSelect(companyId, target.getId(), hiddenTargetIdStr);
			if (StringUtils.isNotEmpty(customerIdSubSelect)) {
				caseTargetParams.add(getTargetCase("r", customerIdSubSelect, String.format("tg_%d", target.getId())));
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

		sqlBuilder.append(" GROUP BY r.device_class_id, d.description");
		sqlBuilder.append(" ORDER BY clicks DESC");

		return getSqlWithLimitDevicesMaxClause(sqlBuilder, sqlParameters, devicesMax);
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
	
}
