/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.beans.BindingEntry.UserType;
import com.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class MailingOpenersTimeBasedDataSet extends TimeBasedDataSet {

	public List<TimeBasedClickStatRow> getData(int tempTableID) throws Exception {
        List<TimeBasedClickStatRow> returnList = new ArrayList<>();
        List<Map<String, Object>> result = selectEmbedded("SELECT * FROM " + getTempTableName(tempTableID) + " ORDER BY statistic_date, device_class");
        for (Map<String, Object> row : result) {
			TimeBasedClickStatRow readItem = new TimeBasedClickStatRow();
			
			readItem.setClicksNet(toInt(row.get("net")));
			readItem.setClicksGross(toInt(row.get("gross")));
			readItem.setClicksAnonymous(toInt(row.get("anonymous")));
			readItem.setClickTime((Date) row.get("statistic_date"));
			readItem.setDeviceClass(DeviceClass.fromId(toInt(row.get("device_class"))));
			readItem.setTargetgroup((String) row.get("target_group"));
			readItem.setColumnIndex(toInt(row.get("target_group_index")));
			
			returnList.add(readItem);
        }
    	
        return returnList;
	}
	
	@Override
	public void dropTempTable(int tempTableID) {
		// Do not drop this table.
		// It is dropped automatically on next call of getCachedTempTable, if it is outdated
	}
	
	private static String getTempTableName(int tempTableID) {
		return "tmp_open_prog_" + tempTableID + "_tbl";
	}
	
	private int createTempTable() throws Exception {
		int tempTableID = getNextTmpID();
		executeEmbedded(
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

	public int prepareData(Integer mailingID, Integer companyID, String selectedTargets, String startDateString, String endDateString, Boolean hourly, String recipientType) throws Exception {
		try {
			String parameterString = mailingID + "," + companyID + ",[" + selectedTargets + "]," + startDateString + "," + endDateString + "," + hourly + "," + recipientType;
			int tempTableID = getCachedTempTable(getClass().getSimpleName(), parameterString);
			if (tempTableID > 0) {
				return tempTableID;
			}
			
			createBlockEntryInTempTableCache(getClass().getSimpleName(), parameterString);
			
			tempTableID = createTempTable();
			
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
					targetGroups.put(targetGroupIndex, target);
				}
			}

			boolean mustJoinCustomerData = false;
	        String recipientTypeSqlPart = "";
	        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientType)) {
	        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml.mailing_id = opl.mailing_id))";
	        	mustJoinCustomerData = true;
	        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientType)) {
	        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type NOT IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml.mailing_id = opl.mailing_id))";
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
							+ " COUNT(DISTINCT opl.customer_id) AS net,"
							+ " COUNT(*) AS gross"
						+ " FROM onepixellog_device_" + companyID + "_tbl opl"
						+ (StringUtils.isEmpty(targetGroupSql) && !mustJoinCustomerData ? "" : " LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (opl.customer_id = cust.customer_id)")
						+ " WHERE opl.mailing_id = ?"
						+ " AND opl.customer_id != 0"
						+ " AND EXISTS (SELECT 1 FROM onepixellog_device_" + companyID + "_tbl opl2"
						    + " WHERE ? <= opl2.creation"
						    + " AND opl2.creation < ?"
						    + " AND opl.mailing_id = opl2.mailing_id"
						    + " AND opl.customer_id = opl2.customer_id"
						    + " AND opl2.device_class_id = ?)"
						+ " AND NOT EXISTS (SELECT 1 FROM onepixellog_device_" + companyID + "_tbl opl3"
						    + " WHERE ? <= opl3.creation"
						    + " AND opl3.creation < ?"
						    + " AND opl.mailing_id = opl3.mailing_id"
						    + " AND opl.customer_id = opl3.customer_id"
						    + " AND opl3.device_class_id != ?)"
						+ " AND ? <= opl.creation"
						+ " AND opl.creation < ?"
						+ (StringUtils.isEmpty(targetGroupSql) ? "" : " AND " + targetGroupSql)
						+ recipientTypeSqlPart;
						    
						Map<String, Object> row = selectSingleRow(sql, mailingID, currentStartDate, currentEndDate, deviceClass.getId(), currentStartDate, currentEndDate, deviceClass.getId(), currentStartDate, currentEndDate);
						int netValue = toInt(row.get("net"));
						int grossValue = toInt(row.get("gross"));

						String sqlAnonymous = "SELECT"
							+ " COUNT(*) AS value"
						+ " FROM onepixellog_device_" + companyID + "_tbl opl"
						+ (StringUtils.isEmpty(targetGroupSql) && !mustJoinCustomerData ? "" : " LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (opl.customer_id = cust.customer_id)")
						+ " WHERE opl.mailing_id = ?"
						+ " AND opl.customer_id = 0"
						+ " AND EXISTS (SELECT 1 FROM onepixellog_device_" + companyID + "_tbl opl2"
						    + " WHERE ? <= opl2.creation"
						    + " AND opl2.creation < ?"
						    + " AND opl.mailing_id = opl2.mailing_id"
						    + " AND opl.customer_id = opl2.customer_id"
						    + " AND opl2.device_class_id = ?)"
						+ " AND NOT EXISTS (SELECT 1 FROM onepixellog_device_" + companyID + "_tbl opl3"
						    + " WHERE ? <= opl3.creation"
						    + " AND opl3.creation < ?"
						    + " AND opl.mailing_id = opl3.mailing_id"
						    + " AND opl.customer_id = opl3.customer_id"
						    + " AND opl3.device_class_id != ?)"
						+ " AND ? <= opl.creation"
						+ " AND opl.creation < ?"
						+ (StringUtils.isEmpty(targetGroupSql) ? "" : " AND " + targetGroupSql)
						+ recipientTypeSqlPart;
						
						Map<String, Object> rowAnonymous = selectSingleRow(sqlAnonymous, mailingID, currentStartDate, currentEndDate, deviceClass.getId(), currentStartDate, currentEndDate, deviceClass.getId(), currentStartDate, currentEndDate);
						int anonymousValue = toInt(rowAnonymous.get("value"));
						
						updateEmbedded("INSERT INTO " + getTempTableName(tempTableID) + " (net, gross, anonymous, statistic_date, device_class, target_group, target_group_index) VALUES (?, ?, ?, ?, ?, ?, ?)",
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
						+ " COUNT(DISTINCT opl.customer_id) AS net,"
						+ " COUNT(*) AS gross"
					+ " FROM onepixellog_device_" + companyID + "_tbl opl"
					+ (StringUtils.isEmpty(targetGroupSql) && !mustJoinCustomerData ? "" : " LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (opl.customer_id = cust.customer_id)")
					+ " WHERE opl.mailing_id = ?"
					+ " AND opl.customer_id != 0"
		        	+ " AND (SELECT COUNT(DISTINCT device_class_id) FROM onepixellog_device_" + companyID + "_tbl opl2"
		        		+ " WHERE ? <= opl2.creation"
					    + " AND opl2.creation < ?"
					    + " AND opl.mailing_id = opl2.mailing_id"
					    + " AND opl.customer_id = opl2.customer_id) > 1"
					+ " AND ? <= opl.creation"
					+ " AND opl.creation < ?"
					+ (StringUtils.isEmpty(targetGroupSql) ? "" : " AND " + targetGroupSql)
					+ recipientTypeSqlPart;
					    
					Map<String, Object> row = selectSingleRow(sql, mailingID, currentStartDate, currentEndDate, currentStartDate, currentEndDate);
					int netValue = toInt(row.get("net"));
					int grossValue = toInt(row.get("gross"));

					String sqlAnonymous = "SELECT"
						+ " COUNT(*) AS value"
					+ " FROM onepixellog_device_" + companyID + "_tbl opl"
					+ (StringUtils.isEmpty(targetGroupSql) && !mustJoinCustomerData ? "" : " LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (opl.customer_id = cust.customer_id)")
					+ " WHERE opl.mailing_id = ?"
					+ " AND opl.customer_id = 0"
			        + " AND (SELECT COUNT(DISTINCT device_class_id) FROM onepixellog_device_" + companyID + "_tbl opl2"
		        		+ " WHERE ? <= opl2.creation"
					    + " AND opl2.creation < ?"
					    + " AND opl.mailing_id = opl2.mailing_id"
					    + " AND opl.customer_id = opl2.customer_id) > 1"
					+ " AND ? <= opl.creation"
					+ " AND opl.creation < ?"
					+ (StringUtils.isEmpty(targetGroupSql) ? "" : " AND " + targetGroupSql)
					+ recipientTypeSqlPart;
					
					Map<String, Object> rowAnonymous = selectSingleRow(sqlAnonymous, mailingID, currentStartDate, currentEndDate, currentStartDate, currentEndDate);
					int anonymousValue = toInt(rowAnonymous.get("value"));
					
					updateEmbedded("INSERT INTO " + getTempTableName(tempTableID) + " (net, gross, anonymous, statistic_date, device_class, target_group, target_group_index) VALUES (?, ?, ?, ?, ?, ?, ?)",
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
	        
	        storeTempTableInCache(getClass().getSimpleName(), parameterString, tempTableID, getTempTableName(tempTableID));
	        
	        return tempTableID;
		} catch (Exception e) {
			throw new Exception("Cannot query for MailingOpenersTimeBasedDataSet: " + e.getMessage(), e);
		}
	}
}
