/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.web;

import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.calendar.form.DashboardCalendarForm;
import com.agnitas.emm.core.calendar.service.CalendarService;
import com.agnitas.emm.core.dashboard.enums.DashboardMode;
import com.agnitas.emm.core.dashboard.form.DashboardForm;
import com.agnitas.emm.core.dashboard.service.DashboardService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HttpUtils;
import com.agnitas.web.dto.BooleanResponseDto;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.AlwaysAllowed;
import com.agnitas.web.perm.annotations.RequiredPermission;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public class DashboardController implements XssCheckAware {
	
    private static final Logger logger = LogManager.getLogger(DashboardController.class);

    private final AdminService adminService;
    private final DashboardService dashboardService;
    private final ConfigService configService;
    private final WebStorage webStorage;
    private final CalendarService calendarService;

    public DashboardController(AdminService adminService, DashboardService dashboardService, ConfigService configService, WebStorage webStorage, CalendarService calendarService) {
        this.adminService = adminService;
        this.dashboardService = dashboardService;
        this.configService = configService;
        this.webStorage = webStorage;
        this.calendarService = calendarService;
    }

    @RequestMapping("/dashboard.action")
    @AlwaysAllowed
    public String view(Admin admin, DashboardForm form, DashboardCalendarForm calendarForm, Model model) {
        syncForm(form);
        syncCalendarForm(calendarForm);

        if (form.getMode() == DashboardMode.CALENDAR && admin.permissionAllowed(Permission.CALENDAR_SHOW)) {
            addCalendarViewModelAttrs(model, calendarForm, admin);
            return "dashboard_calendar";
        }

        addGridViewModelAttrs(admin, model, form);
        return "dashboard_grid";
    }

    @GetMapping("/calendar/unsent-mailings.action")
    @RequiredPermission("calendar.show")
    public String calendarUnsentList(Model model, DashboardCalendarForm form, Admin admin) {
        syncCalendarForm(form);
        addUnsentMailingsModelAttr(model, form, admin);
        return "calendar_unsent_list";
    }

    private void addCalendarViewModelAttrs(Model model, DashboardCalendarForm form, Admin admin) {
        addCommonViewModelAttrs(model, admin);
        model.addAttribute("showALlCalendarEntries", configService.getBooleanValue(ConfigValue.DashboardCalendarShowALlEntries, admin.getCompanyID()));
        model.addAttribute("companyAdmins", adminService.mapIdToUsernameByCompanyAndEmail(admin.getCompanyID()));
        addUnsentMailingsModelAttr(model, form, admin);
    }

    private void addUnsentMailingsModelAttr(Model model, DashboardCalendarForm form, Admin admin) {
        if (Boolean.TRUE.equals(form.getShowUnsentList())) {
            model.addAttribute("unsentMailings", Boolean.TRUE.equals(form.getShowUnsentPlanned())
                ? calendarService.getUnsentPlannedMailings(admin)
                : calendarService.getUnsentUnplannedMailings(admin));
        }
    }

    private void syncCalendarForm(DashboardCalendarForm form) {
        webStorage.access(WebStorage.DASHBOARD_CALENDAR, entry -> {
            if (form.getShowUnsentList() != null) {
                entry.setShowUnsentList(form.getShowUnsentList());
                entry.setShowUnsentPlanned(form.getShowUnsentPlanned());
            } else {
                form.setShowUnsentList(entry.getShowUnsentList());
                form.setShowUnsentPlanned(entry.getShowUnsentPlanned());
            }
        });
    }

    protected void addCommonViewModelAttrs(Model model, Admin admin) {
        // overridden
    }

    private void syncForm(DashboardForm form) {
        webStorage.access(WebStorage.DASHBOARD, entry -> {
            if (form.getMode() != null) {
                entry.setMode(form.getMode());
            } else {
                form.setMode(entry.getMode());
            }
        });
    }

    protected void addGridViewModelAttrs(Admin admin, Model model, DashboardForm form) {
        addCommonViewModelAttrs(model, admin);
        PaginatedList<Map<String, Object>> mailingsList = dashboardService.getMailings(admin, form.getSort(), "", form.getNumberOfRows());
        List<Map<String, Object>> worldMailinglist = dashboardService.getLastSentWorldMailings(admin, form.getNumberOfRows());
        model
            .addAttribute("mailinglist", mailingsList)
            .addAttribute("worldmailinglist", worldMailinglist)
            .addAttribute("workflows", dashboardService.getWorkflows(admin))
            .addAttribute("adminDateTimeFormat", admin.getDateTimeFormat().toPattern())
            .addAttribute("recipientReports", dashboardService.getRecipientReports(admin))
            .addAttribute("layout", adminService.getDashboardLayout(admin))
            .addAttribute("language", admin.getAdminLang())
            .addAttribute("adminTimeZone", admin.getAdminTimezone())
            .addAttribute("adminDateFormat", admin.getDateFormat().toPattern());
    }

    @GetMapping(value = "/dashboard/statistics.action", produces = HttpUtils.APPLICATION_JSON_UTF8)
    @RequiredPermission("stats.mailing")
    public @ResponseBody
    Map<String, Object> getStatistics(Admin admin, @RequestParam(name = "mailingId") int mailingId) {
        JSONObject result;

        try {
            result = dashboardService.getStatisticsInfo(mailingId, admin.getLocale(), admin);
        } catch (Exception e) {
            logger.error("execute: ", e);

            result = new JSONObject();
        }

        return result.toMap();
    }

    @GetMapping("/dashboard/scheduledMailings.action")
    @AlwaysAllowed
    public @ResponseBody ResponseEntity<?> getScheduledMailings(Admin admin, @RequestParam("startDate") String start, @RequestParam("endDate") String end) throws ParseException {
        if (StringUtils.isBlank(start) || StringUtils.isBlank(end)) {
            return ResponseEntity.badRequest().build();
        }

        Date startDate = admin.getDateFormat().parse(start);
        Date endDate = admin.getDateFormat().parse(end);

        if (DateUtilities.compare(startDate, endDate) == 1) {
            logger.error("Start date is after end date");
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(dashboardService.getScheduledMailings(admin, startDate, endDate));
    }

    @PostMapping("/dashboard/layout/save.action")
    @AlwaysAllowed
    public ResponseEntity<BooleanResponseDto> saveLayout(@RequestParam("layout") String layout, Admin admin) {
        adminService.saveDashboardLayout(layout, admin);
        return ResponseEntity.ok(new BooleanResponseDto(true));
    }
}
