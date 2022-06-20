/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.web;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.agnitas.util.AgnUtils;
import org.agnitas.util.HttpUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.displaytag.pagination.PaginatedList;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.calendar.web.CalendarController;
import com.agnitas.emm.core.dashboard.form.DashboardForm;
import com.agnitas.emm.core.dashboard.service.DashboardService;
import com.agnitas.emm.core.news.enums.NewsType;
import com.agnitas.web.perm.annotations.PermissionMapping;

import net.sf.json.JSONObject;

@Controller
@PermissionMapping("dashboard")
public class DashboardController {
	
	/** The logger. */
    private static final Logger logger = LogManager.getLogger(DashboardController.class);

    private AdminService adminService;
    private DashboardService dashboardService;

    public DashboardController(AdminService adminService, DashboardService dashboardService) {
        this.adminService = adminService;
        this.dashboardService = dashboardService;
    }

    @RequestMapping("/dashboard.action")
    public String view(ComAdmin admin, DashboardForm form, Model model) {
        PaginatedList mailingList = dashboardService.getMailings(admin, form.getSort(), "", form.getNumberOfRows());
        List<Map<String, Object>> worldMailinglist = dashboardService.getLastSentWorldMailings(admin, form.getNumberOfRows());

        if (CollectionUtils.isNotEmpty(worldMailinglist)) {
            int lastSentMailingId = (Integer) (worldMailinglist.get(0).get("mailingid"));

            form.setLastSentMailingId(lastSentMailingId);
        }

        model.addAttribute("mailinglist", mailingList);
        model.addAttribute("worldmailinglist", worldMailinglist);
        model.addAttribute("newsTypes", NewsType.values());

        loadData(model, admin.getLocale(), admin);

        return "dashboard_view";
    }

    @RequestMapping(value = "/dashboard/statistics.action", produces = HttpUtils.APPLICATION_JSON_UTF8)
    public @ResponseBody
    JSONObject getStatistics(ComAdmin admin, @RequestParam(name = "mailingId") int mailingId) {
        JSONObject result;

        try {
            result = dashboardService.getStatisticsInfo(mailingId, admin.getLocale(), admin.getCompanyID());
        } catch (Exception e) {
            logger.error("execute: ", e);

            result = new JSONObject();
        }

        return result;
    }

    private void loadData(Model model, Locale locale, ComAdmin admin) {
        int companyId = admin.getCompanyID();
        Map<String, String> adminsMap = adminService.mapIdToUsernameByCompanyAndEmail(companyId);

        //admin's parameters
        model.addAttribute("language", admin.getAdminLang());
        model.addAttribute("currentAdmin", admin.getAdminID());
        model.addAttribute("companyAdmins", adminsMap);
        model.addAttribute("currentAdminName", admin.getUsername());

        //admin's time configurations
        model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());
        model.addAttribute("localeTablePattern", admin.getDateFormat().toPattern());
        model.addAttribute("adminTimeZone", admin.getAdminTimezone());
        model.addAttribute("adminDateFormat", admin.getDateFormat().toPattern());
        model.addAttribute("adminTimeFormat", admin.getTimeFormat().toPattern());

        //other
        model.addAttribute("firstDayOfWeek", Calendar.getInstance(locale).getFirstDayOfWeek() - 1);
        model.addAttribute("monthlist", AgnUtils.getMonthList());
        model.addAttribute("yearlist", AgnUtils.getCalendarYearList(CalendarController.SELECTOR_START_YEAR_NUM));
    }
}
