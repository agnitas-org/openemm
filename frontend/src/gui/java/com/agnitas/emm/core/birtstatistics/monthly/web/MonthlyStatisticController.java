/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtstatistics.monthly.web;

import java.time.YearMonth;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtstatistics.monthly.MonthlyStatType;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyOverviewResponseData;
import com.agnitas.emm.core.birtstatistics.monthly.form.MonthlyStatisticForm;
import com.agnitas.emm.core.birtstatistics.service.MonthlyStatisticsService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.MvcUtils;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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

	private final UserActivityLogService userActivityLogService;
	private final MonthlyStatisticsService statisticsService;
	
	public MonthlyStatisticController(
            UserActivityLogService userActivityLogService,
			MonthlyStatisticsService statisticsService
    ) {
		this.userActivityLogService = userActivityLogService;
        this.statisticsService = statisticsService;
    }
	
	@RequestMapping("/view.action")
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
