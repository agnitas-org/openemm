/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.monthly.web;


import java.util.Calendar;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.form.MonthlyStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/statistics/monthly")
@PermissionMapping("monthly.statistics")
public class MonthlyStatisticController implements XssCheckAware {
	
	private static final Logger logger = LogManager.getLogger(MonthlyStatisticController.class);
	
	private static final String MONTH_LIST = "monthList";
	private static final String YEAR_LIST = "yearList";
	
	private static final String BIRT_STATISTIC_URL_WITHOUT_FORMAT = "birtStatisticUrlWithoutFormat";
	private static final String BIRT_STATISTIC_URL_CSV_REPORT = "birtStatisticUrlCsvReport";
	
	private final BirtStatisticsService birtStatisticsService;
	private final ConversionService conversionService;
	private final UserActivityLogService userActivityLogService;
	
	public MonthlyStatisticController(BirtStatisticsService birtStatisticsService, ConversionService conversionService, UserActivityLogService userActivityLogService) {
		this.birtStatisticsService = birtStatisticsService;
		this.conversionService = conversionService;
		this.userActivityLogService = userActivityLogService;
	}
	
	@RequestMapping("/view.action")
	public String view(Admin admin, MonthlyStatisticForm form, HttpSession session, Model model) {
		Calendar currentDate = Calendar.getInstance(AgnUtils.getTimeZone(admin));

		if(form.getStartMonth() == -1) {
			form.setStartMonth(currentDate.get(Calendar.MONTH));
		}

		if(form.getStartYear() == 0) {
			form.setStartYear(currentDate.get(Calendar.YEAR));
		}
		
		MonthlyStatisticDto monthlyStatisticDto = conversionService.convert(form, MonthlyStatisticDto.class);

		String urlWithoutFormat = birtStatisticsService.getMonthlyStatisticsUrlWithoutFormat(admin, session.getId(), monthlyStatisticDto, false);

		model.addAttribute(YEAR_LIST, AgnUtils.getYearList(AgnUtils.getStatStartYearForCompany(admin)));
		model.addAttribute(MONTH_LIST, AgnUtils.getMonthList());
		model.addAttribute(BIRT_STATISTIC_URL_WITHOUT_FORMAT, urlWithoutFormat);
		model.addAttribute(BIRT_STATISTIC_URL_CSV_REPORT, urlWithoutFormat.replaceFirst(form.getReportName(), MonthlyStatisticForm.MONTHLY_REPORT_CSV_NAME));
		
		userActivityLogService.writeUserActivityLog(admin, "monthly statistics", "active submenu - monthly overview", logger);
		
		return "stats_birt_month_stat";
	}
}
