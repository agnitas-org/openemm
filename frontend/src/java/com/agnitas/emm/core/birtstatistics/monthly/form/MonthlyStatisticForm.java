/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.monthly.form;

import com.agnitas.web.forms.BirtStatisticForm;

public class MonthlyStatisticForm extends BirtStatisticForm {
	
	private static final String MONTHLY_REPORT_NAME = "Monthly.rptdesign";
	
	public static final String MONTHLY_REPORT_CSV_NAME = "Monthly_csv.rptdesign";
	
	public MonthlyStatisticForm() {
		setReportName(MONTHLY_REPORT_NAME);
	}
	
	private int startYear;
	
	private int startMonth = -1;
	
	private int top10MetricsId;
	
	
	public int getStartYear() {
		return startYear;
	}
	
	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}
	
	public int getStartMonth() {
		return startMonth;
	}
	
	public void setStartMonth(int startMonth) {
		this.startMonth = startMonth;
	}
	
	public int getTop10MetricsId() {
		return top10MetricsId;
	}
	
	public void setTop10MetricsId(int top10MetricsId) {
		this.top10MetricsId = top10MetricsId;
	}
}
