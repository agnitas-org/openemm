/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.monthly.web;

import java.time.YearMonth;
import java.util.Calendar;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.birtstatistics.monthly.MonthlyStatType;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyOverviewResponseData;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.form.MonthlyStatisticForm;
import com.agnitas.emm.core.birtstatistics.monthly.form.MonthlyStatisticFormOld;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MonthlyStatisticsService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import jakarta.servlet.http.HttpSession;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
@RequestMapping("/statistics/monthly")
@RequiredPermission("stats.month")
public class MonthlyStatisticController implements XssCheckAware {
	
	private static final Logger logger = LogManager.getLogger(MonthlyStatisticController.class);

	private final BirtStatisticsService birtStatisticsService;
	private final ConversionService conversionService;
	private final UserActivityLogService userActivityLogService;
	private final MonthlyStatisticsService statisticsService;
	
	public MonthlyStatisticController(
            BirtStatisticsService birtStatisticsService,
            ConversionService conversionService,
            UserActivityLogService userActivityLogService,
			MonthlyStatisticsService statisticsService
    ) {
		this.birtStatisticsService = birtStatisticsService;
		this.conversionService = conversionService;
		this.userActivityLogService = userActivityLogService;
        this.statisticsService = statisticsService;
    }
	
	@RequestMapping("/view.action")
	// TODO: GWUA-6883: remove when deleting Monthly.rptdesign
	public String view(Admin admin, @ModelAttribute("monthlyStatisticForm") MonthlyStatisticFormOld form, HttpSession session, Model model) {
		if (admin.permissionAllowed(Permission.MONTHLY_OVERVIEW_STAT_MIGRATION)) {
			return "forward:/statistics/monthly/viewNew.action";
		}

		Calendar currentDate = Calendar.getInstance(AgnUtils.getTimeZone(admin));

		if(form.getStartMonth() == -1) {
			form.setStartMonth(currentDate.get(Calendar.MONTH));
		}

		if(form.getStartYear() == 0) {
			form.setStartYear(currentDate.get(Calendar.YEAR));
		}
		
		MonthlyStatisticDto monthlyStatisticDto = conversionService.convert(form, MonthlyStatisticDto.class);

		String urlWithoutFormat = birtStatisticsService.getMonthlyStatisticsUrlWithoutFormat(admin, session.getId(), monthlyStatisticDto, false);

		model.addAttribute("yearList", AgnUtils.getYearList(AgnUtils.getStatStartYearForCompany(admin)));
		model.addAttribute("monthList", AgnUtils.getMonthList());
		model.addAttribute("birtStatisticUrlWithoutFormat", urlWithoutFormat);
		model.addAttribute("birtStatisticUrlCsvReport", urlWithoutFormat.replaceFirst(form.getReportName(), MonthlyStatisticFormOld.MONTHLY_REPORT_CSV_NAME));
		
		userActivityLogService.writeUserActivityLog(admin, "monthly statistics", "active submenu - monthly overview", logger);
		
		return "stats_birt_month_stat";
	}

	@RequestMapping("/viewNew.action")
	public String view(@ModelAttribute("form") MonthlyStatisticForm form, Admin admin, Model model) {
		model.addAttribute("years", AgnUtils.getYearList(AgnUtils.getStatStartYearForCompany(admin)));
		userActivityLogService.writeUserActivityLog(admin, "monthly statistics", "active submenu - monthly overview", logger);

		return "monthly_overview_stat";
	}

	@ModelAttribute("form")
	public MonthlyStatisticForm getForm(Admin admin) {
		YearMonth now = YearMonth.now(admin.getZoneId());
		return new MonthlyStatisticForm(now.getYear(), now.getMonthValue(), MonthlyStatType.getDefault());
	}

	@GetMapping("/data.action")
	public ResponseEntity<MonthlyOverviewResponseData> getData(MonthlyStatisticForm form, Admin admin) {
		return ResponseEntity.ok(statisticsService.getData(form, admin));
	}

	@RequestMapping(value = "/csv.action", method = {RequestMethod.POST, RequestMethod.GET})
	public ResponseEntity<InputStreamResource> export(MonthlyStatisticForm form, Admin admin) throws Exception {
        return MvcUtils.csvFileResponse(
				statisticsService.getCsv(form, admin),
				"monthly_overview.csv"
		);
	}

}
