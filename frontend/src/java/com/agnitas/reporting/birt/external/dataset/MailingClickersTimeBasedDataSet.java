/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.util.StringUtils;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public class MailingClickersTimeBasedDataSet extends TimeBasedDataSet {
	private static final transient Logger logger = LogManager.getLogger(MailingClickersTimeBasedDataSet.class);
	
	public List<TimeBasedClickStatRow> getData(int tempTableID) throws Exception {
        List<TimeBasedClickStatRow> returnList = new ArrayList<>();
        List<Map<String, Object>> result = selectEmbedded(logger, "SELECT * FROM " + getTempTableName(tempTableID) + " ORDER BY statistic_date, device_class");
        for (Map<String, Object> row : result) {
			TimeBasedClickStatRow readItem = new TimeBasedClickStatRow();
			
			readItem.setClicks_net(((Number) row.get("net")).intValue());
			readItem.setClicks_gross(((Number) row.get("gross")).intValue());
			readItem.setClicks_anonymous(((Number) row.get("anonymous")).intValue());
			readItem.setClickTime((Date) row.get("statistic_date"));
			readItem.setDeviceClass(DeviceClass.fromId(((Number) row.get("device_class")).intValue()));
			readItem.setTargetgroup((String) row.get("target_group"));
			readItem.setColumn_index(((Number) row.get("target_group_index")).intValue());
			
			returnList.add(readItem);
        }
    	
        return returnList;
	}
	
	@Override
	public void dropTempTable(int tempTableID) throws Exception {
		// Do not drop this table.
		// It is dropped automatically on next call of getCachedTempTable, if it is outdated
	}
	
	private static String getTempTableName(int tempTableID) {
		return "tmp_click_prog_" + tempTableID + "_tbl";
	}
	
	private int createTempTable() throws DataAccessException, Exception {
		int tempTableID = getNextTmpID();
		executeEmbedded(logger,
			"CREATE TABLE " + getTempTableName(tempTableID) + " ("
				+ "net INTEGER,"
				+ " gross INTEGER,"
				+ " anonymous INTEGER,"
				+ " statistic_date TIMESTAMP,"
				+ " device_class INTEGER,"
				+ " target_group VARCHAR(200),"
				+ " target_group_index INTEGER"
			+ ")");
		
		return tempTableID;
	}

	public int prepareData(String keyColumn, Integer id, Integer companyID, String selectedTargets, String startDateString, String endDateString, Boolean hourly, String recipientType) throws Exception {
		try {
			String parameterString = keyColumn + "," + id + "," + companyID + ",[" + selectedTargets + "]," + startDateString + "," + endDateString + "," + hourly + "," + recipientType;
			int tempTableID = getCachedTempTable(logger, getClass().getSimpleName(), parameterString);
			if (tempTableID > 0) {
				return tempTableID;
			}
			
			tempTableID = createTempTable();
			
			createBlockEntryInTempTableCache(logger, getClass().getSimpleName(), parameterString);
			
			Date startDate = parseTimeBasedDate(startDateString, true);
			Date endDate = parseTimeBasedDate(endDateString, false);
			
	        List<LightTarget> lightTargets = getTargets(selectedTargets, companyID);
	        Map<Integer, LightTarget> targetGroups = new HashMap<>();

	        LightTarget allRecipients = new LightTarget();
			allRecipients.setId(0);
			allRecipients.setName(CommonKeys.ALL_SUBSCRIBERS);
			allRecipients.setTargetSQL("");

			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
			targetGroups.put(targetGroupIndex, allRecipients);
			for (LightTarget target : lightTargets) {
				if (target != null) {
					targetGroupIndex++;
					targetGroups.put(target.getId(), target);
				}
			}

			boolean mustJoinCustomerData = false;
	        String recipientTypeSqlPart = "";
	        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientType)) {
	        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml." + keyColumn + " = rlog." + keyColumn + ")) cust";
	        	mustJoinCustomerData = true;
	        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientType)) {
	        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type NOT IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml." + keyColumn + " = rlog." + keyColumn + ")) cust";
	        	mustJoinCustomerData = true;
	        }
	        
	        for (Entry<Integer, LightTarget> targetEntry : targetGroups.entrySet()) {
				int currentTargetGroupIndex = targetEntry.getKey();
				LightTarget targetGroup = targetEntry.getValue();
				String currentTargetGroupName = targetGroup.getName();
				String targetGroupSql = targetGroup.getTargetSQL();

				Date currentStartDate = startDate;
				while (currentStartDate.before(endDate)) {
					Date currentEndDate;
					if (hourly) {
						currentEndDate = DateUtilities.addMinutesToDate(currentStartDate, 60);
					} else {
						currentEndDate = DateUtilities.addDaysToDate(currentStartDate, 1);
					}
					
					for (DeviceClass deviceClass : DeviceClass.getOnlyKnownDeviceClasses()) {
						String sql = "SELECT"
							+ " COUNT(DISTINCT rlog.customer_id) AS net,"
							+ " COUNT(*) AS gross"
						+ " FROM rdirlog_" + companyID + "_tbl rlog"
						+ (StringUtils.isEmpty(targetGroupSql) && !mustJoinCustomerData ? "" : " LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (rlog.customer_id = cust.customer_id)")
						+ " WHERE rlog." + keyColumn + " = ?"
						+ " AND rlog.customer_id != 0"
						+ " AND EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rlog2"
						    + " WHERE ? <= rlog2.timestamp"
						    + " AND rlog2.timestamp < ?"
						    + " AND rlog." + keyColumn + " = rlog2." + keyColumn
						    + " AND rlog.customer_id = rlog2.customer_id"
						    + " AND rlog2.device_class_id = ?)"
						+ " AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rlog3"
						    + " WHERE ? <= rlog3.timestamp"
						    + " AND rlog3.timestamp < ?"
						    + " AND rlog." + keyColumn + " = rlog3." + keyColumn
						    + " AND rlog.customer_id = rlog3.customer_id"
						    + " AND rlog3.device_class_id != ?)"
						+ " AND ? <= rlog.timestamp"
						+ " AND rlog.timestamp < ?"
						+ (StringUtils.isEmpty(targetGroupSql) ? "" : " AND " + targetGroupSql)
						+ recipientTypeSqlPart;
						    
						Map<String, Object> row = selectSingleRow(logger, sql, id, currentStartDate, currentEndDate, deviceClass.getId(), currentStartDate, currentEndDate, deviceClass.getId(), currentStartDate, currentEndDate);
						int netValue = ((Number) row.get("net")).intValue();
						int grossValue = ((Number) row.get("gross")).intValue();

						String sqlAnonymous = "SELECT"
							+ " COUNT(*) AS value"
						+ " FROM rdirlog_" + companyID + "_tbl rlog"
						+ (StringUtils.isEmpty(targetGroupSql) && !mustJoinCustomerData ? "" : " LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (rlog.customer_id = cust.customer_id)")
						+ " WHERE rlog." + keyColumn + " = ?"
						+ " AND rlog.customer_id = 0"
						+ " AND EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rlog2"
						    + " WHERE ? <= rlog2.timestamp"
						    + " AND rlog2.timestamp < ?"
						    + " AND rlog." + keyColumn + " = rlog2." + keyColumn
						    + " AND rlog.customer_id = rlog2.customer_id"
						    + " AND rlog2.device_class_id = ?)"
						+ " AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rlog3"
						    + " WHERE ? <= rlog3.timestamp"
						    + " AND rlog3.timestamp < ?"
						    + " AND rlog." + keyColumn + " = rlog3." + keyColumn
						    + " AND rlog.customer_id = rlog3.customer_id"
						    + " AND rlog3.device_class_id != ?)"
						+ " AND ? <= rlog.timestamp"
						+ " AND rlog.timestamp < ?"
						+ (StringUtils.isEmpty(targetGroupSql) ? "" : " AND " + targetGroupSql)
						+ recipientTypeSqlPart;
						
						Map<String, Object> rowAnonymous = selectSingleRow(logger, sqlAnonymous, id, currentStartDate, currentEndDate, deviceClass.getId(), currentStartDate, currentEndDate, deviceClass.getId(), currentStartDate, currentEndDate);
						int anonymousValue = ((Number) rowAnonymous.get("value")).intValue();
						
						updateEmbedded(logger, "INSERT INTO " + getTempTableName(tempTableID) + " (net, gross, anonymous, statistic_date, device_class, target_group, target_group_index) VALUES (?, ?, ?, ?, ?, ?, ?)",
							netValue,
							grossValue + anonymousValue,
							anonymousValue,
							currentStartDate,
							deviceClass.getId(),
							currentTargetGroupName,
							currentTargetGroupIndex);
					}
					
					// Mixed deviceClass
					String sql = "SELECT"
						+ " COUNT(DISTINCT rlog.customer_id) AS net,"
						+ " COUNT(*) AS gross"
					+ " FROM rdirlog_" + companyID + "_tbl rlog"
					+ (StringUtils.isEmpty(targetGroupSql) && !mustJoinCustomerData ? "" : " LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (rlog.customer_id = cust.customer_id)")
					+ " WHERE rlog." + keyColumn + " = ?"
					+ " AND rlog.customer_id != 0"
		        	+ " AND (SELECT COUNT(DISTINCT device_class_id) FROM rdirlog_" + companyID + "_tbl rlog2"
		        		+ " WHERE ? <= rlog2.timestamp"
					    + " AND rlog2.timestamp < ?"
					    + " AND rlog." + keyColumn + " = rlog2." + keyColumn
					    + " AND rlog.customer_id = rlog2.customer_id) > 1"
					+ " AND ? <= rlog.timestamp"
					+ " AND rlog.timestamp < ?"
					+ (StringUtils.isEmpty(targetGroupSql) ? "" : " AND " + targetGroupSql)
					+ recipientTypeSqlPart;
					    
					Map<String, Object> row = selectSingleRow(logger, sql, id, currentStartDate, currentEndDate, currentStartDate, currentEndDate);
					int netValue = ((Number) row.get("net")).intValue();
					int grossValue = ((Number) row.get("gross")).intValue();

					String sqlAnonymous = "SELECT"
						+ " COUNT(*) AS value"
					+ " FROM rdirlog_" + companyID + "_tbl rlog"
					+ (StringUtils.isEmpty(targetGroupSql) && !mustJoinCustomerData ? "" : " LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (rlog.customer_id = cust.customer_id)")
					+ " WHERE rlog." + keyColumn + " = ?"
					+ " AND rlog.customer_id = 0"
			        + " AND (SELECT COUNT(DISTINCT device_class_id) FROM rdirlog_" + companyID + "_tbl rlog2"
		        		+ " WHERE ? <= rlog2.timestamp"
					    + " AND rlog2.timestamp < ?"
					    + " AND rlog." + keyColumn + " = rlog2." + keyColumn
					    + " AND rlog.customer_id = rlog2.customer_id) > 1"
					+ " AND ? <= rlog.timestamp"
					+ " AND rlog.timestamp < ?"
					+ (StringUtils.isEmpty(targetGroupSql) ? "" : " AND " + targetGroupSql)
					+ recipientTypeSqlPart;
					
					Map<String, Object> rowAnonymous = selectSingleRow(logger, sqlAnonymous, id, currentStartDate, currentEndDate, currentStartDate, currentEndDate);
					int anonymousValue = ((Number) rowAnonymous.get("value")).intValue();
					
					updateEmbedded(logger, "INSERT INTO " + getTempTableName(tempTableID) + " (net, gross, anonymous, statistic_date, device_class, target_group, target_group_index) VALUES (?, ?, ?, ?, ?, ?, ?)",
						netValue,
						grossValue + anonymousValue,
						anonymousValue,
						currentStartDate,
						0,
						currentTargetGroupName,
						currentTargetGroupIndex);
					
					currentStartDate = currentEndDate;
				}
			}
	        
	        storeTempTableInCache(logger, getClass().getSimpleName(), parameterString, tempTableID, getTempTableName(tempTableID));
	        
	        return tempTableID;
		} catch (Exception e) {
			throw new Exception("Cannot query for MailingClickersTimeBasedDataSet: " + e.getMessage(), e);
		}
	}
}
