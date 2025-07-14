/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.TimeZone;

import com.agnitas.beans.Admin;
import com.agnitas.beans.impl.PaginatedListImpl;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.calendar.beans.CalendarComment;
import com.agnitas.emm.core.calendar.beans.CalendarMailingLabel;
import com.agnitas.emm.core.calendar.beans.CalendarUnsentMailing;
import com.agnitas.emm.core.calendar.beans.MailingPopoverInfo;
import com.agnitas.emm.core.calendar.beans.impl.CalendarCommentImpl;
import com.agnitas.emm.core.calendar.form.CommentForm;
import com.agnitas.emm.core.calendar.form.DashboardCalendarForm;
import com.agnitas.emm.core.calendar.service.CalendarCommentService;
import com.agnitas.emm.core.calendar.service.CalendarService;
import com.agnitas.emm.core.mailing.bean.MailingDto;
import com.agnitas.emm.core.mailing.dao.MailingDaoOptions;
import com.agnitas.emm.util.html.xssprevention.ForbiddenTagError;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

public class CalendarController implements XssCheckAware {
    public static final int SELECTOR_START_YEAR_NUM = 2009;
    public static final int SECONDS_BEFORE_WAIT_MESSAGE = 2;

    private static final Logger logger = LogManager.getLogger(CalendarController.class);
    
    private static final int UNSENT_MAILS_LIST_SIZE = 10;
    private static final int PLANNED_MAILS_LIST_SIZE = 10;
    private static final String DATE_FORMAT = "dd-MM-yyyy";
    private static final String DATE_TIME_FORMAT = "dd-MM-yyyy HH:mm";
    protected static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private final AdminService adminService;
    private final CalendarService calendarService;
    private final OptimizationService optimizationService;
    private final CalendarCommentService calendarCommentService;
    private final UserActivityLogService userActivityLogService;

    public CalendarController(AdminService adminService, CalendarService calendarService,
                              OptimizationService optimizationService, CalendarCommentService calendarCommentService, UserActivityLogService userActivityLogService) {
        this.adminService = adminService;
        this.calendarService = calendarService;
        this.optimizationService = optimizationService;
        this.calendarCommentService = calendarCommentService;
        this.userActivityLogService = userActivityLogService;
    }

    @ExceptionHandler(XSSHtmlException.class)
    public String onXSSHtmlException(XSSHtmlException e, Popups popups) {
        for (HtmlCheckError error : e.getErrors()) {
            if (error instanceof ForbiddenTagError) {
                popups.alert("error.html.forbiddenTag.extended");
            } else {
                popups.alert(error.toMessage());
            }
        }

        return "messages";
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    @RequestMapping("/calendar.action")
    public String view(Admin admin, Model model) {
        model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());
        loadAdminData(model, admin);

        return "calendar_view";
    }

    @GetMapping("/calendar/unsent-mailings/unplanned.action")
    @ResponseBody
    public List<CalendarUnsentMailing> unplannedMailings(Admin admin) {
        return calendarService.getUnplannedMailings(admin);
    }

    @GetMapping("/calendar/unsent-mailings/planned.action")
    @ResponseBody
    public List<CalendarUnsentMailing> plannedUnsentMailings(Admin admin) {
        return calendarService.getPlannedUnsentMailings(admin);
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    @RequestMapping("/calendar/getUnsentMailings.action")
    public String getUnsentMailings(Admin admin, Model model) {
        setUnsentMails(model, admin);

        return "calendar_unsent_mailings_list_ajax";
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    @RequestMapping("/calendar/getPlannedMailings.action")
    public String getPlannedMailings(Admin admin, Model model) {
        model.addAttribute("localeDatePattern", admin.getDateFormat().toPattern());
        setPlannedMails(model, admin);

        return "calendar_planned_mailings_list_ajax";
    }

    @GetMapping(value = "/calendar/autoOptimization.action", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody
    List<Object> getAutoOptimization(Admin admin, @RequestParam("startDate") String start, @RequestParam("endDate") String end) {
        LocalDate startDate = DateUtilities.parseDate(start, DATE_FORMATTER);
        LocalDate endDate = DateUtilities.parseDate(end, DATE_FORMATTER);

        return optimizationService.getOptimizationsAsJson(admin, startDate, endDate, DATE_FORMATTER).toList();
    }

    @GetMapping("/calendar/comments.action")
    public ResponseEntity<List<Object>> getComments(Admin admin, @RequestParam("startDate") String start, @RequestParam("endDate") String end) {
        LocalDate startDate = DateUtilities.parseDate(start, DATE_FORMATTER);
        LocalDate endDate = DateUtilities.parseDate(end, DATE_FORMATTER);

        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(calendarCommentService.getComments(admin, startDate, endDate).toList());
    }

    @PostMapping(value = "/calendar/saveComment.action")
    public ResponseEntity<Map<String, Object>> saveComment(Admin admin, CommentForm form) {
        TimeZone timezone = AgnUtils.getTimeZone(admin);
        DateFormat dateFormat = DateUtilities.getFormat(DATE_FORMAT, timezone);
        DateFormat dateTimeFormat = DateUtilities.getFormat(DATE_TIME_FORMAT, timezone);

        Date date = DateUtilities.parse(form.getDate(), dateFormat);
        Date plannedDate = DateUtilities.parse(form.getPlannedSendDate(), admin.isRedesignedUiUsed() ? admin.getDateTimeFormat() : dateTimeFormat);

        if (Objects.isNull(date) || Objects.isNull(plannedDate)) {
            return ResponseEntity.badRequest().build();
        }

        CalendarComment comment = new CalendarCommentImpl();

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
        return ResponseEntity.ok(Map.of("commentId", commentId));
    }

    @PostMapping(value = "/calendar/removeComment.action")
    public ResponseEntity<Map<String, Object>> removeComment(Admin admin, @RequestParam("commentId") int commentId) {
        if (commentId <= 0) {
            return ResponseEntity.badRequest().build();
        }

        boolean isSuccess = calendarCommentService.removeComment(commentId, admin.getCompanyID());
        return ResponseEntity.ok(Map.of("success", isSuccess));
    }

    @GetMapping("/calendar/mailingsLight.action")
    public ResponseEntity<List<Object>> mailingsLight(@RequestParam String start, @RequestParam String end, Admin admin) {
        LocalDate startDate = DateUtilities.parseDate(start, DATE_FORMATTER);
        LocalDate endDate = DateUtilities.parseDate(end, DATE_FORMATTER);

        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        if (startDate.isAfter(endDate)) {
            logger.error("Start date is after end date");
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(calendarService.getMailingsLight(admin, startDate, endDate).toList());
    }

    @GetMapping("/calendar/light/mailings.action")
    public ResponseEntity<List<MailingDto>> getMailings(@RequestParam String start, @RequestParam String end, Admin admin) {
        LocalDate startDate = DateUtilities.parseDate(start, DATE_FORMATTER);
        LocalDate endDate = DateUtilities.parseDate(end, DATE_FORMATTER);

        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            return ResponseEntity.badRequest().build();
        }
        if (startDate.isAfter(endDate)) {
            logger.error("Start date is after end date");
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(calendarService.getMailings(MailingDaoOptions
            .builder()
            .setStartIncl(DateUtilities.toDate(startDate.atStartOfDay(), admin.getZoneId()))
            .setEndExcl(DateUtilities.toDate(endDate.plusDays(1).atStartOfDay(), admin.getZoneId()))
            .includeMailinglistName(true)
            .build(), admin));
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    @GetMapping("/calendar/mailings.action")
    public ResponseEntity<List<Object>> getMailings(@RequestParam(defaultValue = "0") int limit, Admin admin,
                                  @RequestParam("startDate") String start, @RequestParam("endDate") String end) {
        LocalDate startDate = DateUtilities.parseDate(start, DATE_FORMATTER);
        LocalDate endDate = DateUtilities.parseDate(end, DATE_FORMATTER);

        if (Objects.isNull(startDate) || Objects.isNull(endDate)) {
            return ResponseEntity.badRequest().build();
        }

        if (startDate.isAfter(endDate)) {
            logger.error("Start date is after end date");
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.ok(calendarService.getMailings(admin, startDate, endDate, limit).toList());
    }

    @GetMapping("/calendar/mailingsRedesigned.action")
    @ResponseBody
    public List<CalendarMailingLabel> getMailings(DashboardCalendarForm form, Admin admin) {
        return calendarService.getMailingLabels(
            form.getStartDate(admin.getZoneId(), DATE_FORMATTER),
            form.getEndDate(admin.getZoneId(), DATE_FORMATTER),
            form.getDayMailingsLimit(), admin);
    }

    @GetMapping("/calendar/labels.action")
    public ResponseEntity<Map<String, List<?>>> getLabels(DashboardCalendarForm form, Admin admin) {
        return ResponseEntity.ok(calendarService.getLabels(form, admin));
    }

    @GetMapping("/calendar/mailingsPopoverInfo.action")
    @ResponseBody
    public List<MailingPopoverInfo> mailingsPopoverInfo(@RequestParam Set<Integer> mailingIds, Admin admin) {
        return calendarService.mailingsPopoverInfo(mailingIds, admin);
    }
    
    @RequestMapping(value = "/calendar/moveMailing.action", method = RequestMethod.POST)
    public ResponseEntity<Map<String, Boolean>> moveMailing(Admin admin, @RequestParam("mailingId") int mailingId, @RequestParam("date") String newDate) {
        LocalDate date = DateUtilities.parseDate(newDate, DATE_FORMATTER);

        if (mailingId <= 0 || Objects.isNull(date)) {
            return ResponseEntity.badRequest().build();
        }

        boolean isSuccess = calendarService.moveMailing(admin, mailingId, date);
        writeUAL(admin, getMoveMailingUalDescr(mailingId, isSuccess, date));

        return ResponseEntity.ok().body(Map.of("success", isSuccess));
    }

    private static String getMoveMailingUalDescr(int mailingId, boolean isSuccess, LocalDate date) {
        return String.format(isSuccess
            ? "mailing id = %d moved to date - %s"
            : "mailing id = %d attempt to move to date - %s", mailingId, date);
    }

    @PostMapping(value = "/calendar/mailing/{mailingId:\\d+}/clearPlannedDate.action")
    public ResponseEntity<Boolean> clearMailingPlannedDate(@PathVariable int mailingId, Admin admin) {
        boolean success = calendarService.clearMailingPlannedDate(mailingId, admin.getCompanyID());
        writeUAL(admin, String.format(success
            ? "mailing id = %d cleared plan date"
            : "mailing id = %d attempt to clear plan date", mailingId));
        return ResponseEntity.ok(success);
    }

    private void setUnsentMails(Model model, Admin admin) {
        PaginatedListImpl<Map<String, Object>> unsentList = calendarService.getUnsentMailings(admin, UNSENT_MAILS_LIST_SIZE);

        model.addAttribute("unsentMails", unsentList.getList());
        model.addAttribute("unsentMailsListSize", UNSENT_MAILS_LIST_SIZE);
    }

    private void setPlannedMails(Model model, Admin admin) {
        PaginatedListImpl<Map<String, Object>> plannedList = calendarService.getPlannedMailings(admin, PLANNED_MAILS_LIST_SIZE);

        model.addAttribute("plannedMails", plannedList.getList());
        model.addAttribute("plannedMailsListSize", PLANNED_MAILS_LIST_SIZE);
    }

    private void loadAdminData(Model model, Admin admin) {
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

    private void writeUAL(Admin admin, String description) {
        userActivityLogService.writeUserActivityLog(admin, "dashboard calendar change", description);
    }
}
