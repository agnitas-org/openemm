/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.operations;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.agnitas.util.DateUtilities;

public class ActionOperationGetArchiveMailingParameters extends AbstractActionOperationParameters {
	private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DateUtilities.DD_MM_YYYY);
	private int expireDay;
	private int expireMonth;
	private int expireYear;
	
	public ActionOperationGetArchiveMailingParameters() {
		super(ActionOperationType.GET_ARCHIVE_MAILING);
	}

	public int getExpireDay() {
		return expireDay;
	}

	public void setExpireDay(int expireDay) {
		this.expireDay = expireDay;
	}

	public int getExpireMonth() {
		return expireMonth;
	}

	public void setExpireMonth(int expireMonth) {
		this.expireMonth = expireMonth;
	}

	public int getExpireYear() {
		return expireYear;
	}

	public void setExpireYear(int expireYear) {
		this.expireYear = expireYear;
	}

	public String getExpireDate() {
		if (expireDay == 0 && expireMonth == 0 && expireYear == 0) {
			return null;
		}

		return FORMATTER.format(LocalDate.of(expireYear, expireMonth, expireDay));
	}

	public void setExpireDate(String dateString) {
		LocalDate date = DateUtilities.parseDate(dateString, FORMATTER);
		if (date == null) {
			expireYear = 0;
			expireMonth = 0;
			expireDay = 0;
		} else {
			expireYear = date.getYear();
			expireMonth = date.getMonthValue();
			expireDay = date.getDayOfMonth();
		}
	}
}
