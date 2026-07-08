/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import org.springframework.util.StringUtils;

public class TimeBasedDataSet extends BIRTDataSet {

	protected static String getCustomerTableName(int companyId) {
		return "customer_" + companyId + "_tbl";
	}
	
	protected static Date parseTimeBasedDate(String dateString, boolean isStartDate) {
		Date date;
		Calendar startDateCalendar = new GregorianCalendar();
		if (dateString.contains(".") && StringUtils.countOccurrencesOf(dateString, ":") > 1) {
			date = parseDate(dateString, DATE_PARAMETER_FORMAT_WITH_SECOND);
			startDateCalendar.setTime(date);
		} else if (dateString.contains(":")) {
			date = parseDate(dateString, DATE_PARAMETER_FORMAT_WITH_HOUR);
			startDateCalendar.setTime(date);
		} else {
			date = parseDate(dateString, DATE_PARAMETER_FORMAT);
			startDateCalendar.setTime(date);
			startDateCalendar.set(Calendar.HOUR_OF_DAY, isStartDate ? 0 : 23);
		}

		startDateCalendar.set(Calendar.MINUTE, isStartDate ? 0 : 59);
		startDateCalendar.set(Calendar.SECOND, isStartDate ? 0 : 59);
		startDateCalendar.set(Calendar.MILLISECOND, isStartDate ? 0 : 999);
		date = startDateCalendar.getTime();

		return date;
	}

}
