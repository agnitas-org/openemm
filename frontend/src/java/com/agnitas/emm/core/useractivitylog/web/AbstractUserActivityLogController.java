/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.FileResponseBody;
import org.agnitas.beans.factory.UserActivityLogExportWorkerFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.service.ActivityLogExportWorker;
import org.agnitas.service.UserActivityLogExportWorker;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogForm;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Pollable;

import jakarta.servlet.http.HttpSession;

public abstract class AbstractUserActivityLogController {

    protected final WebStorage webStorage;
    protected final AdminService adminService;
    protected final UserActivityLogService userActivityLogService;
    protected final UserActivityLogExportWorkerFactory exportWorkerFactory;

    protected AbstractUserActivityLogController(WebStorage webStorage, AdminService adminService, UserActivityLogService userActivityLogService, UserActivityLogExportWorkerFactory exportWorkerFactory) {
        this.webStorage = webStorage;
        this.adminService = adminService;
        this.userActivityLogService = userActivityLogService;
        this.exportWorkerFactory = exportWorkerFactory;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    public Pollable<ModelAndView> getList(Admin admin, UserActivityLogForm form, Model model, HttpSession session) {
        syncNumberOfRows(form);

        Callable<ModelAndView> worker = () -> {
            List<AdminEntry> admins = adminService.getAdminEntriesForUserActivityLog(admin);
            PaginatedListImpl<?> loggedUserActions = preparePaginatedList(form, admins, admin);

            FormUtils.setPaginationParameters(form, loggedUserActions);

            prepareModelAttributesForListPage(model, admin);
            model.addAttribute("admins", admins);
            model.addAttribute("actions", loggedUserActions);

            return new ModelAndView(getListViewName(), model.asMap());
        };

        Map<String, Object> argumentsMap = new HashMap<>();
        argumentsMap.put("sort", form.getSort());
        argumentsMap.put("order", form.getDir());
        argumentsMap.put("page", form.getPage());
        argumentsMap.put("numberOfRows", form.getNumberOfRows());
        argumentsMap.put("username", form.getUsername());
        argumentsMap.put("dateFrom.date", form.getDateFrom().getDate());
        argumentsMap.put("dateTo.date", form.getDateTo().getDate());
        argumentsMap.put("description", form.getDescription());

        PollingUid pollingUid = PollingUid.builder(session.getId(), getUserActivityLogKey())
                .arguments(argumentsMap.values().toArray(ArrayUtils.EMPTY_OBJECT_ARRAY))
                .build();

        ModelAndView modelAndView = new ModelAndView(redirectToListPage(), argumentsMap);
        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, modelAndView, worker);
    }

    protected Pollable<ModelAndView> getListRedesigned(Admin admin, UserActivityLogFilterBase filter, Model model, HttpSession session) {
        filter.setCompanyId(admin.getCompanyID());
        syncNumberOfRows(filter);

        Callable<ModelAndView> worker = () -> {
            List<AdminEntry> admins = getAdminEntries(admin);
            PaginatedListImpl<?> loggedUserActions = preparePaginatedListRedesigned(filter, admins, admin);

            FormUtils.setPaginationParameters(filter, loggedUserActions);

            prepareModelAttributesForListPage(model, admin);
            model.addAttribute("admins", admins);
            model.addAttribute("actions", loggedUserActions);

            return new ModelAndView(getListViewName(), model.asMap());
        };

        Map<String, Object> argumentsMap = filter.toMap();

        PollingUid pollingUid = PollingUid.builder(session.getId(), getUserActivityLogKey())
                .arguments(argumentsMap.values().toArray(ArrayUtils.EMPTY_OBJECT_ARRAY))
                .build();

        ModelAndView modelAndView = new ModelAndView(redirectToRedesignedListPage(), argumentsMap);
        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, modelAndView, worker);
    }

    protected abstract List<AdminEntry> getAdminEntries(Admin admin);

    protected void prepareModelAttributesForListPage(Model model, Admin admin) {
        model.addAttribute("localeTableFormat", admin.getDateTimeFormat());

        if (admin.isRedesignedUiUsed()) {
            model.addAttribute("adminDateFormat", admin.getDateFormat());
        } else {
            AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        }

        model.addAttribute("defaultDate", LocalDate.now().format(admin.getDateFormatter()));
    }

    protected abstract String getUserActivityLogKey();

    protected abstract void syncNumberOfRows(PaginationForm form);

    protected abstract PaginatedListImpl<?> preparePaginatedList(UserActivityLogForm form, List<AdminEntry> admins, Admin admin) throws Exception;
    protected abstract PaginatedListImpl<?> preparePaginatedListRedesigned(UserActivityLogFilterBase filter, List<AdminEntry> admins, Admin admin) throws Exception;

    protected abstract String redirectToListPage();
    protected abstract String redirectToRedesignedListPage();

    protected abstract String getListViewName();

    public ResponseEntity<StreamingResponseBody> downloadLogs(Admin admin, UserActivityLogForm form, UserActivityLogService.UserType type) throws Exception {
        File exportTempFile = createTempExportFile();

        UserActivityLogExportWorker exportWorker = createExportWorker(admin, form, exportTempFile, type);
        exportWorker.call();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=\"%s\"", getExportFileName(type)))
                .contentLength(exportTempFile.length())
                .contentType(MediaType.parseMediaType(HttpUtils.CONTENT_TYPE_CSV))
                .body(new FileResponseBody(exportTempFile, true));
    }

    public ResponseEntity<StreamingResponseBody> downloadLogsRedesigned(Admin admin, UserActivityLogFilterBase filter, UserActivityLogService.UserType type) throws Exception {
        File exportTempFile = createTempExportFile();

        ActivityLogExportWorker exportWorker = createExportWorkerRedesigned(admin, filter, exportTempFile, type);
        exportWorker.call();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=\"%s\"", getExportFileName(type)))
                .contentLength(exportTempFile.length())
                .contentType(MediaType.parseMediaType(HttpUtils.CONTENT_TYPE_CSV))
                .body(new FileResponseBody(exportTempFile, true));
    }

    private File createTempExportFile() {
        String tempFileName = String.format("user-activity-log-%s.csv", UUID.randomUUID());
        File mailingRecipientsExportTempDirectory = AgnUtils.createDirectory(AgnUtils.getTempDir() + File.separator + "UserActivityLogExport");
        return new File(mailingRecipientsExportTempDirectory, tempFileName);
    }

    // TODO: remove after EMMGUI-714 will be finished and old design will be removed
    private UserActivityLogExportWorker createExportWorker(Admin admin, UserActivityLogForm form, File tempFile, UserActivityLogService.UserType type) {
        DateTimeFormatter datePickerFormatter = admin.getDateFormatter();

        LocalDate localDateFrom = form.getDateFrom().get(LocalDate.now(), datePickerFormatter);
        LocalDate localDateTo = form.getDateTo().get(LocalDate.now(), datePickerFormatter);

        ZoneId zoneId = AgnUtils.getZoneId(admin);
        List<AdminEntry> admins = adminService.getAdminEntriesForUserActivityLog(admin);

        return exportWorkerFactory.getBuilderInstance()
                .setFromDate(DateUtilities.toDate(localDateFrom, zoneId))
                .setToDate(DateUtilities.toDate(localDateTo, zoneId))
                .setFilterDescription(form.getDescription())
                .setFilterAction(form.getUserAction())
                .setFilterAdminUserName(form.getUsername())
                .setFilterAdmins(admins)
                .setExportFile(tempFile.getAbsolutePath())
                .setDateFormat(admin.getDateFormat())
                .setDateTimeFormat(admin.getDateTimeFormatWithSeconds())
                .setUserActivityLogService(userActivityLogService)
                .setUserActivityType(type)
                .setExportTimezone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId()).build();
    }

    private ActivityLogExportWorker createExportWorkerRedesigned(Admin admin, UserActivityLogFilterBase filter, File tempFile, UserActivityLogService.UserType type) {
        List<AdminEntry> admins = getAdminEntries(admin);

        return exportWorkerFactory.getBuilderInstanceRedesigned()
                .setFilter(filter)
                .setFilterAdmins(admins)
                .setExportFile(tempFile.getAbsolutePath())
                .setDateFormat(admin.getDateFormat())
                .setDateTimeFormat(admin.getDateTimeFormatWithSeconds())
                .setUserActivityLogService(userActivityLogService)
                .setUserActivityType(type)
                .setExportTimezone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId()).build();
    }

    private String getExportFileName(UserActivityLogService.UserType userType) {
        String filePrefix = "user-activity-log";

        if (UserActivityLogService.UserType.REST.equals(userType)) {
            filePrefix = "rest-" + filePrefix;
        } else if (UserActivityLogService.UserType.SOAP.equals(userType)) {
            filePrefix = "soap-" + filePrefix;
        }

        return String.format(
                "%s-%s.csv",
                filePrefix,
                new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date())
        );
    }
}
