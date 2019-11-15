/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.agnitas.beans.BindingEntry.UserType;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.util.StringUtils;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public class MailingOpenersTimeBasedDataSet extends BIRTDataSet {
	private static final transient Logger logger = Logger.getLogger(MailingOpenersTimeBasedDataSet.class);

	public List<TimeBasedClickStatRow> getData(Integer mailingID, Integer companyID, String selectedTargets, String startDateString, String endDateString, Boolean hourly, String recipientType) throws Exception {
		try {
			Date startDate = getStartDate(startDateString);
			Date endDate = getEndDate(endDateString);

			SimpleDateFormat openTimeFormat;
			if (hourly) {
				openTimeFormat = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_BLANK_HOUR);
			} else {
				openTimeFormat = new SimpleDateFormat(DATE_PARAMETER_FORMAT);
			}
			
	        List<LightTarget> lightTargets = getTargets(selectedTargets, companyID);
	        Map<Integer, LightTarget> targetGroups = new HashMap<>();
	        Map<Integer, Integer> targetGroupIndexes = new HashMap<>();
	        targetGroups.put(0, null);
			int targetgroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
			targetGroupIndexes.put(0, CommonKeys.ALL_SUBSCRIBERS_INDEX);
			if (lightTargets != null) {
				for (LightTarget target : lightTargets) {
					targetgroupIndex++;
					targetGroups.put(target.getId(), target);
					targetGroupIndexes.put(target.getId(), targetgroupIndex);
		        }
	        }
	        
	        List<TimeBasedClickStatRow> returnList = new ArrayList<>();

	        // Show all deviceClasses ("UNKOWN_"-deviceclasses are not included in ...log...-tables)
	        List<DeviceClass> seviceClassesToShow = Arrays.stream(DeviceClass.values()).filter(deviceClassItem -> !deviceClassItem.getName().startsWith("UNKOWN_")).collect(Collectors.toList());
	        seviceClassesToShow.add(null); // for Mixed deviceclasses
	        for (DeviceClass deviceClass : seviceClassesToShow) {
	        	for (Entry<Integer, LightTarget> targetEntry : targetGroups.entrySet()) {
			        List<TimeBasedClickStatRow> results = select(logger,
				    	getStatisticSqlStatement(companyID, hourly, recipientType, deviceClass, targetGroups.get(targetEntry.getKey())),
		        		new TimeBasedClickStatRow_RowMapper(openTimeFormat, (targetGroups.get(targetEntry.getKey()) == null ? CommonKeys.ALL_SUBSCRIBERS : targetGroups.get(targetEntry.getKey()).getName()), targetGroupIndexes.get(targetEntry.getKey()), deviceClass),
		        		mailingID, startDate, endDate);
			        returnList.addAll(results);
	        	}
	        }

	        // Check if all time periods have at least a 0 entry
        	List<DeviceClass> deviceClassesForDisplay = Arrays.stream(DeviceClass.values()).filter(item -> !item.getName().startsWith("UNKNOWN_")).collect(Collectors.toList());
        	deviceClassesForDisplay.add(null); // Added for mixed
        	GregorianCalendar nextPeriodDate = new GregorianCalendar();
        	nextPeriodDate.setTime(startDate);
        	GregorianCalendar endPeriodDate = new GregorianCalendar();
        	endPeriodDate.setTime(endDate);
    		while (nextPeriodDate.before(endPeriodDate)) {
    			for (Entry<Integer, LightTarget> targetEntry : targetGroups.entrySet()) {
	            	for (DeviceClass deviceClass : deviceClassesForDisplay) {
		        		boolean found = false;
		        		for (TimeBasedClickStatRow entry : returnList) {
		        			if (entry.getClickTime().equals(nextPeriodDate.getTime()) && entry.getDeviceClass() == deviceClass) {
		        				found = true;
		        				break;
		        			}
		        		}
		        		if (!found) {
		        			TimeBasedClickStatRow timeBasedClickStatRow = new TimeBasedClickStatRow();
		        			timeBasedClickStatRow.setClickTime(nextPeriodDate.getTime());
		        			timeBasedClickStatRow.setClicks_net(0);
		        			timeBasedClickStatRow.setDeviceClass(deviceClass);
		        			timeBasedClickStatRow.setTargetgroup(targetGroups.get(targetEntry.getKey()) == null ? CommonKeys.ALL_SUBSCRIBERS : targetGroups.get(targetEntry.getKey()).getName());
		        			timeBasedClickStatRow.setColumn_index(targetGroupIndexes.get(targetEntry.getKey()));
		        			returnList.add(timeBasedClickStatRow);
		        		}
		        	}
    			}
        		
            	nextPeriodDate.add(hourly ? Calendar.HOUR_OF_DAY : Calendar.DAY_OF_MONTH, 1);
        	}
        	
        	// Sort result entries by clickdate, deviceclass and targetgroup
        	returnList.sort((item1, item2) -> {
        	    int cmp = item1.getClickTime().compareTo(item2.getClickTime());
        	    if (cmp == 0) {
        	    	if (item1.getDeviceClass() == item2.getDeviceClass()) {
        	    		cmp = Integer.compare(item1.getColumn_index(), item2.getColumn_index());
        	    	} else if (item1.getDeviceClass() == null) {
        	    		cmp = 1;
        	    	} else if (item2.getDeviceClass() == null) {
        	    		cmp = -1;
        	    	} else {
        	    		cmp = item1.getDeviceClass().compareTo(item2.getDeviceClass());
        	    	}
        	    }
        	    return cmp;
        	});
	        
	        return returnList;
		} catch (Exception e) {
			throw new Exception("Cannot query for MailingOpenersTimeBasedDataSet: " + e.getMessage(), e);
		}
	}

	public Date getEndDate(String endDateString) throws Exception {
		Date endDate;
		try {
            Calendar endDateCalendar = new GregorianCalendar();
            if (endDateString.contains(".") && StringUtils.countOccurrencesOf(endDateString, ":") > 1) {
                endDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_SECOND).parse(endDateString);
                endDateCalendar.setTime(endDate);
            } else if (endDateString.contains(":")) {
                endDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString);
                endDateCalendar.setTime(endDate);
            } else {
                endDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString);
                endDateCalendar.setTime(endDate);
                endDateCalendar.set(Calendar.HOUR_OF_DAY, 23);
            }
            endDateCalendar.set(Calendar.MINUTE, 59);
            endDateCalendar.set(Calendar.SECOND, 59);
            endDateCalendar.set(Calendar.MILLISECOND, 999);
            endDate = endDateCalendar.getTime();
        } catch (ParseException e) {
            throw new Exception("Error while parsing end date: " + endDateString, e);
        }
		return endDate;
	}

	public Date getStartDate(String startDateString) throws Exception {
		Date startDate;
		try {
            Calendar startDateCalendar = new GregorianCalendar();
            if (startDateString.contains(".") && StringUtils.countOccurrencesOf(startDateString, ":") > 1) {
                startDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_SECOND).parse(startDateString);
                startDateCalendar.setTime(startDate);
            } else if (startDateString.contains(":")) {
                startDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString);
                startDateCalendar.setTime(startDate);
            } else {
                startDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString);
                startDateCalendar.setTime(startDate);
                startDateCalendar.set(Calendar.HOUR_OF_DAY, 0);
            }
            startDateCalendar.set(Calendar.MINUTE, 0);
            startDateCalendar.set(Calendar.SECOND, 0);
            startDateCalendar.set(Calendar.MILLISECOND, 0);
            startDate = startDateCalendar.getTime();
        } catch (ParseException e) {
            throw new Exception("Error while parsing start date: " + startDateString, e);
        }
		return startDate;
	}

	private String getStatisticSqlStatement(Integer companyID, boolean hourly, String recipientType, DeviceClass deviceClass, LightTarget target) {
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
		String negativeDeviceClassPart;
        if (deviceClass == null) {
        	// Mixed deviceClass
        	positiveDeviceClassPart = " AND (SELECT COUNT(DISTINCT device_class_id) FROM onepixellog_device_" + companyID + "_tbl opl2 WHERE " + openTimeSqlPart + " = " + openTime2SqlPart + " AND opl.mailing_id = opl2.mailing_id AND opl.customer_id = opl2.customer_id) > 1";
        	negativeDeviceClassPart = "";
        } else {
        	positiveDeviceClassPart = " AND EXISTS (SELECT 1 FROM onepixellog_device_" + companyID + "_tbl opl2 WHERE " + openTimeSqlPart + " = " + openTime2SqlPart + " AND opl.mailing_id = opl2.mailing_id AND opl.customer_id = opl2.customer_id AND device_class_id = " + deviceClass.getId() + ")";
			negativeDeviceClassPart = " AND NOT EXISTS (SELECT 1 FROM onepixellog_device_" + companyID + "_tbl opl2 WHERE " + openTimeSqlPart + " = " + openTime2SqlPart + " AND opl.mailing_id = opl2.mailing_id AND opl.customer_id = opl2.customer_id AND device_class_id != " + deviceClass.getId() + ")";
        }
        
        String recipientTypeSqlPart;
        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientType)) {
        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml.mailing_id = opl.mailing_id)) cust";
        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientType)) {
        	recipientTypeSqlPart = " AND cust.customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type NOT IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl ml WHERE ml.mailing_id = opl.mailing_id)) cust";
        } else {
        	recipientTypeSqlPart = "";
        }

		return "SELECT"
            + " COUNT(DISTINCT opl.customer_id) AS net, COUNT(opl.customer_id) AS gross,"
            + " " + openTimeSqlPart + " AS open_time"
        + " FROM onepixellog_device_" + companyID + "_tbl opl"
        + " JOIN customer_" + companyID + "_tbl cust ON (opl.customer_id = cust.customer_id)"
        + " WHERE opl.mailing_id = ?"
            + positiveDeviceClassPart
            + negativeDeviceClassPart
            + " AND ? <= opl.creation AND opl.creation <= ?"
            + recipientTypeSqlPart
            + (target == null || target.getTargetSQL() == null ? "" : " AND " + target.getTargetSQL())
        + " GROUP BY " + openTimeSqlPart
        + " ORDER BY " + openTimeSqlPart + " ASC";
	}
	
	private class TimeBasedClickStatRow_RowMapper implements RowMapper<TimeBasedClickStatRow> {
    	private SimpleDateFormat openTimeFormat;
    	private String targetgroupName;
    	private int targetgroupIndex;
    	private DeviceClass deviceClass;
    	
		public TimeBasedClickStatRow_RowMapper(SimpleDateFormat openTimeFormat, String targetgroupName, int targetgroupIndex, DeviceClass deviceClass) {
			super();
			this.openTimeFormat = openTimeFormat;
			this.targetgroupName = targetgroupName;
			this.targetgroupIndex = targetgroupIndex;
			this.deviceClass = deviceClass;
		}

		@Override
		public TimeBasedClickStatRow mapRow(ResultSet resultSet, int row) throws SQLException {
			try {
				TimeBasedClickStatRow readItem = new TimeBasedClickStatRow();
				
				readItem.setClicks_net(resultSet.getBigDecimal("net").intValue());
				readItem.setClicks_gross(resultSet.getBigDecimal("gross").intValue());
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
