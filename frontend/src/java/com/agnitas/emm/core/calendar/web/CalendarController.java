/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.calendar.web;

import java.text.DateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;

import com.agnitas.web.mvc.XssCheckAware;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.calendar.beans.ComCalendarComment;
import com.agnitas.emm.core.calendar.beans.impl.ComCalendarCommentImpl;
import com.agnitas.emm.core.calendar.form.CommentForm;
import com.agnitas.emm.core.calendar.service.CalendarCommentService;
import com.agnitas.emm.core.calendar.service.CalendarService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.web.perm.annotations.PermissionMapping;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

@Controller
@PermissionMapping("calendar")
public class CalendarController implements XssCheckAware {
    public static final int SELECTOR_START_YEAR_NUM = 2009;
    public static final int SECONDS_BEFORE_WAIT_MESSAGE = 2;

    /** The logger. */
    private static final Logger logger = LogManager.getLogger(CalendarController.class);
    
    private static final int UNSENT_MAILS_LIST_SIZE = 10;
    private static final int PLANNED_MAILS_LIST_SIZE = 10;
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private AdminService adminService;
    private CalendarService calendarService;
    private ComOptimizationService optimizationService;
    private CalendarCommentService calendarCommentService;

    public CalendarController(AdminService adminService, CalendarService calendarService,
                              ComOptimizationService optimizationService, CalendarCommentService calendarCommentService) {
        this.adminService = adminService;
        this.calendarService = calendarService;
        this.optimizationService = optimizationService;
        this.calendarCommentService = calendarCommentService;
    }

    @RequestMapping("/calendar.action")
    public String view(ComAdmin admin, Model model) {
        model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());
        loadAdminData(model, admin);

        return "calendar_view";
    }

    @RequestMapping("/calendar/getUnsentMailings.action")
    public String getUnsentMailings(ComAdmin admin, Model model) {
        setUnsentMails(model, admin);

        return "calendar_unsent_mailings_list_ajax";
    }

    @RequestMapping("/calendar/getPlannedMailings.action")
    public String getPlannedMailings(ComAdmin admin, Model model) {
        model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());
        setPlannedMails(model, admin);

        return "calendar_planned_mailings_list_ajax";
    }

    @RequestMapping(value = "/calendar/autoOptimization.action", method = RequestMethod.POST)
    public @ResponseBody
    JSONArray getAutoOptimization(ComAdmin admin, @RequestParam("startDate") String start, @RequestParam("endDate") String end) {
        LocalDate startDate = DateUtilities.parseDate(start, DATE_FORMATTER);
        LocalDate endDate = DateUtilities.parseDate(end, DATE_FORMATTER);

        return optimizationService.getOptimizationsAsJson(admin, startDate, endDate, DATE_FORMATTER);
    }

    @GetMapping("/calendar/comments.action")
    public @ResponseBody ResponseEntity<?> getComments(ComAdmin admin, @RequestParam("startDate") String start, @RequestParam("endDate") String end) {
        LocalDate startDate = DateUtilities.parseDate(start, DATE_FORMATTER);
        LocalDate endDate = DateUtilities.parseDate(end, DATE_FORMATTER);

        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(calendarCommentService.getComments(admin, startDate, endDate));
    }

    @RequestMapping(value = "/calendar/saveComment.action", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> saveComment(ComAdmin admin, CommentForm form) {
        JSONObject result = new JSONObject();
        TimeZone timezone = AgnUtils.getTimeZone(admin);
        DateFormat dateFormat = DateUtilities.getFormat(DATE_FORMAT, timezone);
        DateFormat dateTimeFormat = DateUtilities.getFormat(DATE_TIME_FORMAT, timezone);

        Date date = DateUtilities.parse(form.getDate(), dateFormat);
        Date plannedDate = DateUtilities.parse(form.getPlannedSendDate(), dateTimeFormat);

        if (Objects.isNull(date) || Objects.isNull(plannedDate)) {
            return ResponseEntity.badRequest().build();
        }

        ComCalendarComment comment = new ComCalendarCommentImpl();

        comment.setCompanyId(admin.getCompanyID());
        comment.setCommentId(form.getCommentId());
        comment.setAdminId(admin.getAdminID());
        comment.setDate(date);
        comment.setPlannedSendDate(plannedDate);
        comment.setComment(form.getComment());
        comment.setDeadline(form.isDeadline());
        comment.setSendNow(form.isSendNow());
        comment.setRecipients(calendarCommentService.getRecipients(form.getNotifyAdminId(), form.getRecipients()));

        int commentId = calendarCommentService.saveComment(comment);

        result.put("commentId", commentId);

        return ResponseEntity.ok(result);
    }

    @RequestMapping(value = "/calendar/removeComment.action", method = RequestMethod.POST)
    public @ResponseBody ResponseEntity<?> removeComment(ComAdmin admin, @RequestParam("commentId") int commentId) {
        JSONObject result = new JSONObject();
        boolean isSuccess;

        if (commentId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        isSuccess = calendarCommentService.removeComment(commentId, admin.getCompanyID());

        result.put("success", isSuccess);

        return ResponseEntity.ok(result);
    }

    @GetMapping("/calendar/mailings.action")
    public @ResponseBody
    ResponseEntity<?> getMailings(ComAdmin admin, @RequestParam("startDate") String start, @RequestParam("endDate") String end) {
        LocalDate startDate = DateUtilities.parseDate(start, DATE_FORMATTER);
        LocalDate endDate = DateUtilities.parseDate(end, DATE_FORMATTER);

        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        if (startDate.isAfter(endDate)) {
            logger.error("Start date is after end date");
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(calendarService.getMailings(admin, startDate, endDate));
    }

    @RequestMapping(value = "/calendar/moveMailing.action", method = RequestMethod.POST)
    public @ResponseBody
    ResponseEntity<?> moveMailing(ComAdmin admin, @RequestParam("mailingId") int mailingId, @RequestParam("date") String newDate) {
        JSONObject result = new JSONObject();
        LocalDate date = DateUtilities.parseDate(newDate, DATE_FORMATTER);

        if (mailingId <= 0 || Objects.isNull(date)) {
            return ResponseEntity.badRequest().build();
        }

        boolean isSuccess = calendarService.moveMailing(admin, mailingId, date);

        result.element("success", isSuccess);

        return ResponseEntity.ok().body(result);
    }

    private void setUnsentMails(Model model, ComAdmin admin) {
        PaginatedListImpl<Map<String, Object>> unsentList = calendarService.getUnsentMailings(admin, UNSENT_MAILS_LIST_SIZE);

        model.addAttribute("unsentMails", unsentList.getList());
        model.addAttribute("unsentMailsListSize", UNSENT_MAILS_LIST_SIZE);
    }

    private void setPlannedMails(Model model, ComAdmin admin) {
        PaginatedListImpl<Map<String, Object>> plannedList = calendarService.getPlannedMailings(admin, PLANNED_MAILS_LIST_SIZE);

        model.addAttribute("plannedMails", plannedList.getList());
        model.addAttribute("plannedMailsListSize", PLANNED_MAILS_LIST_SIZE);
    }

    private void loadAdminData(Model model, ComAdmin admin) {
        Locale locale = admin.getLocale();
        int companyId = admin.getCompanyID();
        Map<String, String> adminsMap = adminService.mapIdToUsernameByCompanyAndEmail(companyId);

        model.addAttribute("adminLocale", locale);
        model.addAttribute("firstDayOfWeek", Calendar.getInstance(locale).getFirstDayOfWeek() - 1);
        model.addAttribute("companyId", companyId);
        
        model.addAttribute("adminTimeZone", admin.getAdminTimezone());
        model.addAttribute("adminDateFormat", admin.getDateFormat().toPattern());
        model.addAttribute("adminTimeFormat", admin.getTimeFormat().toPattern());

        model.addAttribute("companyAdmins", adminsMap);
        model.addAttribute("currentAdmin", admin.getAdminID());
        model.addAttribute("currentAdminName", admin.getUsername());
        model.addAttribute("monthlist", AgnUtils.getMonthList());
        model.addAttribute("yearlist", AgnUtils.getCalendarYearList(SELECTOR_START_YEAR_NUM));
    }
}
