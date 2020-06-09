/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import org.agnitas.beans.BindingEntry.UserType;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

public class MailingOpenersTimeBasedDataSet extends TimeBasedDataSet {
	private static final transient Logger logger = Logger.getLogger(MailingOpenersTimeBasedDataSet.class);

	public List<TimeBasedClickStatRow> getData(Integer mailingID, Integer companyID, String selectedTargets, String startDateString, String endDateString, Boolean hourly, String recipientType) throws Exception {
		try {
			Date startDate = parseTimeBasedDate(startDateString, true);
			Date endDate = parseTimeBasedDate(endDateString, false);

			SimpleDateFormat openTimeFormat =
					new SimpleDateFormat(hourly ? DATE_PARAMETER_FORMAT_WITH_BLANK_HOUR : DATE_PARAMETER_FORMAT);
			
	        List<LightTarget> lightTargets = getTargets(selectedTargets, companyID);
	        Map<Integer, LightTarget> targetGroups = new HashMap<>();
	        Map<Integer, Integer> targetGroupIndexes = new HashMap<>();
	        targetGroups.put(0, null);
			int targetgroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
			targetGroupIndexes.put(0, CommonKeys.ALL_SUBSCRIBERS_INDEX);
			for (LightTarget target : lightTargets) {
				targetgroupIndex++;
				targetGroups.put(target.getId(), target);
				targetGroupIndexes.put(target.getId(), targetgroupIndex);
			}
	        
	        List<TimeBasedClickStatRow> returnList = new ArrayList<>();

	        // Show all deviceClasses ("UNKOWN_"-deviceclasses are not included in ...log...-tables)
	        List<DeviceClass> deviceClassesForDisplay = getDeviceClassesToShow();
	        TimeBasedClickStatRow_RowMapper rowMapper =
						new TimeBasedClickStatRow_RowMapper(openTimeFormat);
	        for (Entry<Integer, LightTarget> targetEntry : targetGroups.entrySet()) {
				int targetGroupIndex = targetGroupIndexes.get(targetEntry.getKey());
				LightTarget targetGroup = targetEntry.getValue();
				String targetGroupName = CommonKeys.ALL_SUBSCRIBERS;
				String targetGroupSql = "";
				if (targetGroup != null) {
					targetGroupName = targetGroup.getName();
					targetGroupSql = targetGroup.getTargetSQL();
				}
		
				rowMapper.setTargetgroupName(targetGroupName);
				rowMapper.setTargetgroupIndex(targetGroupIndex);
		
				for (DeviceClass deviceClass : deviceClassesForDisplay) {
					rowMapper.setDeviceClass(deviceClass);
					String sqlStatement = getStatisticSqlStatement(companyID, hourly, recipientType, deviceClass, targetGroupSql);
					returnList.addAll(select(logger, sqlStatement, rowMapper, mailingID, startDate, endDate));
				}
			}

	        // Check if all time periods have at least a 0 entry
        	GregorianCalendar nextPeriodDate = new GregorianCalendar();
        	nextPeriodDate.setTime(startDate);
        	GregorianCalendar endPeriodDate = new GregorianCalendar();
        	endPeriodDate.setTime(endDate);
        	
        	returnList.addAll(
        			getTimeBasedClickStat(deviceClassesForDisplay, targetGroups, targetGroupIndexes, nextPeriodDate, endPeriodDate, hourly));

			// Sort result entries by clickdate, deviceclass and targetgroup
        	sortTimeBasedClickStatResult(returnList);
	        
	        return returnList;
		} catch (Exception e) {
			throw new Exception("Cannot query for MailingOpenersTimeBasedDataSet: " + e.getMessage(), e);
		}
	}

	private String getStatisticSqlStatement(Integer companyID, boolean hourly, String recipientType, DeviceClass deviceClass, String targetGroupSql) {
        String openTimeSqlPart;
        if (isOracleDB()) {
        	openTimeSqlPart = "TO_CHAR(opl.creation, 'yyyy-mm-dd" + (hourly ? " hh24" : "") + "')";
        } else {
        	openTimeSqlPart = "DATE_FORMAT(opl.creation , '%Y-%m-%d" + (hourly ? " %H" : "") + "')";
        }
		
        String openTime2SqlPart;
        if (isOracleDB()) {
        	openTime2SqlPart = "TO_CHAR(opl2.creation, 'yyyy-mm-dd" + (hourly ? " hh24" : "") + "')";
        } else {
        	openTime2SqlPart = "DATE_FORMAT(opl2.creation , '%Y-%m-%d" + (hourly ? " %H" : "") + "')";
        }
        
        String positiveDeviceClassPart;
		String negativeDeviceClassPart = "";
        if (deviceClass == null) {
        	// Mixed deviceClass
        	positiveDeviceClassPart = " AND (SELECT COUNT(DISTINCT device_class_id) FROM onepixellog_device_" + companyID + "_tbl opl2 WHERE " + openTimeSqlPart + " = " + openTime2SqlPart + " AND opl.mailing_id = opl2.mailing_id AND opl.customer_id = opl2.customer_id) > 1";
        } else {
        	positiveDeviceClassPart = " AND EXISTS (SELECT 1 FROM onepixellog_device_" + companyID + "_tbl opl2 WHERE " + openTimeSqlPart + " = " + openTime2SqlPart + " AND opl.mailing_id = opl2.mailing_id AND opl.customer_id = opl2.customer_id AND device_class_id = " + deviceClass.getId() + ")";
			negativeDeviceClassPart = " AND NOT EXISTS (SELECT 1 FROM onepixellog_device_" + companyID + "_tbl opl2 WHERE " + openTimeSqlPart + " = " + openTime2SqlPart + " AND opl.mailing_id = opl2.mailing_id AND opl.customer_id = opl2.customer_id AND device_class_id != " + deviceClass.getId() + ")";
        }
        
        String recipientTypeSqlPart = "";
        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientType)) {
        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml.mailing_id = opl.mailing_id)) cust";
        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientType)) {
        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type NOT IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml.mailing_id = opl.mailing_id)) cust";
        }

		return  "SELECT" +
				" COUNT(DISTINCT opl.customer_id) AS net," +
				" COUNT(opl.customer_id) AS gross," +
				" COUNT(CASE WHEN opl.customer_id = 0 then 1 end) AS anonymous, " +
				openTimeSqlPart + " AS open_time " +
				" FROM " + getOnePixelLogDeviceTableName(companyID) + " opl" +
				" LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (opl.customer_id = cust.customer_id)" +
				" WHERE opl.mailing_id = ? " +
				positiveDeviceClassPart +
				negativeDeviceClassPart +
				" AND ? <= opl.creation AND opl.creation <= ?" +
				recipientTypeSqlPart +
				(StringUtils.isEmpty(targetGroupSql) ? "" : " AND " + targetGroupSql) +
				" GROUP BY " + openTimeSqlPart +
				" ORDER BY " + openTimeSqlPart + " ASC";
	}
	
	private class TimeBasedClickStatRow_RowMapper implements RowMapper<TimeBasedClickStatRow> {
    	private SimpleDateFormat openTimeFormat;
    	private String targetgroupName;
    	private int targetgroupIndex;
    	private DeviceClass deviceClass;
    	
		public TimeBasedClickStatRow_RowMapper(SimpleDateFormat openTimeFormat) {
			this.openTimeFormat = openTimeFormat;
			this.targetgroupName = CommonKeys.ALL_SUBSCRIBERS;
			this.targetgroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
			this.deviceClass = null;
		}

		public void setDeviceClass(DeviceClass deviceClass) {
			this.deviceClass = deviceClass;
		}
		
		public void setTargetgroupName(String targetgroupName) {
			this.targetgroupName = targetgroupName;
		}
		
		public void setTargetgroupIndex(int targetgroupIndex) {
			this.targetgroupIndex = targetgroupIndex;
		}
		
		@Override
		public TimeBasedClickStatRow mapRow(ResultSet resultSet, int row) throws SQLException {
			try {
				TimeBasedClickStatRow readItem = new TimeBasedClickStatRow();
				
				readItem.setClicks_net(resultSet.getBigDecimal("net").intValue());
				readItem.setClicks_gross(resultSet.getBigDecimal("gross").intValue());
				readItem.setClicks_anonymous(resultSet.getBigDecimal("anonymous").intValue());
				readItem.setClickTime(openTimeFormat.parse(resultSet.getString("open_time")));
				readItem.setDeviceClass(deviceClass);
				readItem.setTargetgroup(targetgroupName);
				readItem.setColumn_index(targetgroupIndex);
				return readItem;
			} catch (Exception e) {
				throw new SQLException("Cannot read TimeBasedClickStatRow: " + e.getMessage(), e);
			}
		}
	}
}
