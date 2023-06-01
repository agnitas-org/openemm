/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;

import org.springframework.util.StringUtils;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public class TimeBasedDataSet extends BIRTDataSet {
	protected static String getCustomerTableName(int companyId) {
		return "customer_" + companyId + "_tbl";
	}
	
	protected List<DeviceClass> getDeviceClassesToShow() {
		List<DeviceClass> devices = DeviceClass.getOnlyKnownDeviceClasses();
		// for Mixed deviceclasses
		devices.add(null);
		return devices;
	}
	
	protected static Date parseTimeBasedDate(String dateString, boolean isStartDate) throws Exception {
		Date date;
		 try {
			Calendar startDateCalendar = new GregorianCalendar();
			if (dateString.contains(".") && StringUtils.countOccurrencesOf(dateString, ":") > 1) {
				date = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_SECOND).parse(dateString);
				startDateCalendar.setTime(date);
			} else if (dateString.contains(":")) {
				date = new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(dateString);
				startDateCalendar.setTime(date);
			} else {
				date = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(dateString);
				startDateCalendar.setTime(date);
				startDateCalendar.set(Calendar.HOUR_OF_DAY, isStartDate ? 0 : 23);
			}
			startDateCalendar.set(Calendar.MINUTE, isStartDate ? 0 : 59);
			startDateCalendar.set(Calendar.SECOND, isStartDate ? 0 : 59);
			startDateCalendar.set(Calendar.MILLISECOND, isStartDate ? 0 : 999);
			date = startDateCalendar.getTime();
		} catch (ParseException e) {
			throw new Exception("Error while parsing date: " + dateString, e);
		}
		return date;
	}
	
	protected List<TimeBasedClickStatRow> getTimeBasedClickStat(List<DeviceClass> deviceClassesForDisplay,
																Map<Integer, LightTarget> targetGroups, Map<Integer, Integer> targetGroupIndexes,
																GregorianCalendar nextPeriodDate, GregorianCalendar endPeriodDate, boolean hourly) {
		List<TimeBasedClickStatRow> returnList = new ArrayList<>();
		while (nextPeriodDate.before(endPeriodDate)) {
			for (Map.Entry<Integer, LightTarget> targetEntry : targetGroups.entrySet()) {
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
						timeBasedClickStatRow.setClicksNet(0);
						timeBasedClickStatRow.setDeviceClass(deviceClass);
						timeBasedClickStatRow.setTargetgroup(targetGroups.get(targetEntry.getKey()) == null ?
								CommonKeys.ALL_SUBSCRIBERS : targetGroups.get(targetEntry.getKey()).getName());
						timeBasedClickStatRow.setColumnIndex(targetGroupIndexes.get(targetEntry.getKey()));
						returnList.add(timeBasedClickStatRow);
					}
				}
			}
			
			nextPeriodDate.add(hourly ? Calendar.HOUR_OF_DAY : Calendar.DAY_OF_MONTH, 1);
		}
		
		return returnList;
	}
	
	protected void sortTimeBasedClickStatResult(List<TimeBasedClickStatRow> returnList) {
		returnList.sort((item1, item2) -> {
        	    int cmp = item1.getClickTime().compareTo(item2.getClickTime());
        	    if (cmp == 0) {
        	    	if (item1.getDeviceClass() == item2.getDeviceClass()) {
        	    		cmp = Integer.compare(item1.getColumnIndex(), item2.getColumnIndex());
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
	}
}
