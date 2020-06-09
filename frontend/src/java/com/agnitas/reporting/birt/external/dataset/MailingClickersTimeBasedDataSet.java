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

public class MailingClickersTimeBasedDataSet extends TimeBasedDataSet {
	private static final transient Logger logger = Logger.getLogger(MailingClickersTimeBasedDataSet.class);

	public List<TimeBasedClickStatRow> getData(String keyColumn, Integer id, Integer companyID, String selectedTargets, String startDateString, String endDateString, Boolean hourly, String recipientType) throws Exception {
		try {
			Date startDate = parseTimeBasedDate(startDateString, true);
			Date endDate = parseTimeBasedDate(endDateString, false);
	        
	        SimpleDateFormat clickTimeFormat =
					new SimpleDateFormat(hourly ? DATE_PARAMETER_FORMAT_WITH_BLANK_HOUR : DATE_PARAMETER_FORMAT);
			
	        List<LightTarget> lightTargets = getTargets(selectedTargets, companyID);
	        Map<Integer, LightTarget> targetGroups = new HashMap<>();
	        Map<Integer, Integer> targetGroupIndexes = new HashMap<>();
	        targetGroups.put(0, null);
	        targetGroupIndexes.put(0, CommonKeys.ALL_SUBSCRIBERS_INDEX);
			int targetgroupIndex = 2;
			for (LightTarget target : lightTargets) {
				targetGroups.put(target.getId(), target);
				targetGroupIndexes.put(target.getId(), targetgroupIndex);
				targetgroupIndex++;
			}
			
			List<TimeBasedClickStatRow> returnList = new ArrayList<>();
			List<DeviceClass> deviceClassesForDisplay = getDeviceClassesToShow();
			TimeBasedClickStatRow_RowMapper rowMapper = new TimeBasedClickStatRow_RowMapper(clickTimeFormat);

			for (Entry<Integer, LightTarget> targetEntry : targetGroups.entrySet()) {
				int targetId = targetEntry.getKey();
				LightTarget target = getDefaultTarget(targetEntry.getValue());
				
				rowMapper.setTargetgroupName(target.getName());
				rowMapper.setTargetgroupIndex(targetId);
				
				for (DeviceClass deviceClass : deviceClassesForDisplay) {
					rowMapper.setDeviceClass(deviceClass);
			        List<TimeBasedClickStatRow> results = select(logger,
				    	getStatisticSqlStatement(companyID, hourly, recipientType, deviceClass, target.getTargetSQL(), keyColumn),
		        		rowMapper, id, startDate, endDate);
			        returnList.addAll(results);
	        	}
			}

	        // Check if all time periods have at least a 0 entry
        	deviceClassesForDisplay.add(null); // Added for mixed
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
			throw new Exception("Cannot query for MailingClickersTimeBasedDataSet: " + e.getMessage(), e);
		}
	}
	
	private String getStatisticSqlStatement(Integer companyID, boolean hourly, String recipientType, DeviceClass deviceClass, String targetSql, String keyColumn) {
        String clickTimeSqlPart;
        if (isOracleDB()) {
        	clickTimeSqlPart = "TO_CHAR(rlog.timestamp, 'yyyy-mm-dd" + (hourly ? " hh24" : "") + "')";
        } else {
        	clickTimeSqlPart = "DATE_FORMAT(rlog.timestamp , '%Y-%m-%d" + (hourly ? " %H" : "") + "')";
        }
		
        String clickTime2SqlPart;
        if (isOracleDB()) {
        	clickTime2SqlPart = "TO_CHAR(rlog2.timestamp, 'yyyy-mm-dd" + (hourly ? " hh24" : "") + "')";
        } else {
        	clickTime2SqlPart = "DATE_FORMAT(rlog2.timestamp , '%Y-%m-%d" + (hourly ? " %H" : "") + "')";
        }
        
        String positiveDeviceClassPart;
		String negativeDeviceClassPart = "";
        if (deviceClass == null) {
        	// Mixed deviceClass
        	positiveDeviceClassPart = " AND (SELECT COUNT(DISTINCT device_class_id) FROM rdirlog_" + companyID + "_tbl rlog2 WHERE " + clickTimeSqlPart + " = " + clickTime2SqlPart + " AND rlog." + keyColumn + " = rlog2." + keyColumn + " AND rlog.customer_id = rlog2.customer_id) > 1";
        } else {
        	positiveDeviceClassPart = " AND EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rlog2 WHERE " + clickTimeSqlPart + " = " + clickTime2SqlPart + " AND rlog." + keyColumn + " = rlog2." + keyColumn + " AND rlog.customer_id = rlog2.customer_id AND device_class_id = " + deviceClass.getId() + ")";
			negativeDeviceClassPart = " AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rlog2 WHERE " + clickTimeSqlPart + " = " + clickTime2SqlPart + " AND rlog." + keyColumn + " = rlog2." + keyColumn + " AND rlog.customer_id = rlog2.customer_id AND device_class_id != " + deviceClass.getId() + ")";
        }
        
        String recipientTypeSqlPart = "";
        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientType)) {
        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml.mailing_id = rlog.mailing_id)) cust";
        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientType)) {
        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml.mailing_id = rlog.mailing_id)) cust";
        }
		
		return "SELECT "+
				" COUNT(DISTINCT rlog.customer_id) AS net, " +
				" COUNT(*) AS gross, " +
				" COUNT(CASE WHEN rlog.customer_id = 0 then 1 end) AS anonymous, " +
				clickTimeSqlPart + " AS click_time " +
				" FROM " + getRdirLogTableName(companyID) + " rlog " +
				" LEFT JOIN " + getCustomerTableName(companyID) + " cust ON (rlog.customer_id = cust.customer_id) " +
				" WHERE rlog." + keyColumn + " = ? " +
				positiveDeviceClassPart +
				negativeDeviceClassPart +
				" AND ? <= rlog.timestamp AND rlog.timestamp <= ? " +
				recipientTypeSqlPart +
				(StringUtils.isEmpty(targetSql) ? "" : " AND " + targetSql) +
				" GROUP BY " + clickTimeSqlPart +
				" ORDER BY " + clickTimeSqlPart + " ASC";
	}
	
	private class TimeBasedClickStatRow_RowMapper implements RowMapper<TimeBasedClickStatRow> {
    	private SimpleDateFormat clickTimeFormat;
    	private String targetgroupName;
    	private int targetgroupIndex;
    	private DeviceClass deviceClass;
    	
		public TimeBasedClickStatRow_RowMapper(SimpleDateFormat clickTimeFormat) {
			this.clickTimeFormat = clickTimeFormat;
			this.targetgroupName = CommonKeys.ALL_SUBSCRIBERS;
			this.targetgroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
			this.deviceClass = null;
		}
		
		public void setTargetgroupName(String targetgroupName) {
			this.targetgroupName = targetgroupName;
		}
		
		public void setTargetgroupIndex(int targetgroupIndex) {
			this.targetgroupIndex = targetgroupIndex;
		}
		
		public void setDeviceClass(DeviceClass deviceClass) {
			this.deviceClass = deviceClass;
		}
		
		@Override
		public TimeBasedClickStatRow mapRow(ResultSet resultSet, int row) throws SQLException {
			try {
				TimeBasedClickStatRow readItem = new TimeBasedClickStatRow();
				
				readItem.setClicks_net(resultSet.getBigDecimal("net").intValue());
				readItem.setClicks_gross(resultSet.getBigDecimal("gross").intValue());
				readItem.setClicks_anonymous(resultSet.getBigDecimal("anonymous").intValue());
				readItem.setClickTime(clickTimeFormat.parse(resultSet.getString("click_time")));
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
