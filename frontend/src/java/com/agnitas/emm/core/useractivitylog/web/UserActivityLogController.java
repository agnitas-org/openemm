/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;

import jakarta.servlet.http.HttpSession;

import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.FileResponseBody;
import org.agnitas.beans.factory.UserActivityLogExportWorkerFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.useractivitylog.LoggedUserAction;
import org.agnitas.service.UserActivityLogExportWorker;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.HttpUtils;
import org.agnitas.util.UserActivityLogActions;
import org.agnitas.web.forms.FormUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import com.agnitas.beans.ComAdmin;
import com.agnitas.beans.PollingUid;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogForm;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.perm.annotations.PermissionMapping;


@Controller
@RequestMapping("/administration/useractivitylog")
@PermissionMapping("user.activity.log")
public class UserActivityLogController {

    @SuppressWarnings("unused")
    private static final Logger logger = Logger.getLogger(UserActivityLogController.class);

    private static final String USER_ACTIVITY_LOG_KEY = "userActivityLogKey";

    private WebStorage webStorage;
    private AdminService adminService;
    private UserActivityLogService userActivityLogService;
    private UserActivityLogExportWorkerFactory exportWorkerFactory;

    @Autowired
    public UserActivityLogController(WebStorage webStorage,
                                     AdminService adminService,
                                     UserActivityLogService userActivityLogService,
                                     UserActivityLogExportWorkerFactory exportWorkerFactory) {
        this.webStorage = webStorage;
        this.adminService = adminService;
        this.userActivityLogService = userActivityLogService;
        this.exportWorkerFactory = exportWorkerFactory;
    }

    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public Pollable<ModelAndView> list(ComAdmin admin, UserActivityLogForm listForm, Model model, HttpSession session) {
        String sessionId = session.getId();
        DateTimeFormatter datePickerFormatter = admin.getDateFormatter();
        SimpleDateFormat localTableFormat = admin.getDateTimeFormat();

        FormUtils.syncNumberOfRows(webStorage, ComWebStorage.USERLOG_OVERVIEW, listForm);

        List<AdminEntry> admins = adminService.getAdminEntriesForUserActivityLog(admin);
        List<AdminEntry> adminsFilter = admin.permissionAllowed(Permission.MASTERLOG_SHOW) ? null : admins;

        model.addAttribute("userActions", Arrays.asList(UserActivityLogActions.values()));
        model.addAttribute("admins", admins);
        model.addAttribute("localeTableFormat", localTableFormat);
        AgnUtils.setAdminDateTimeFormatPatterns(admin, model);
        model.addAttribute("defaultDate", LocalDate.now().format(datePickerFormatter));

        LocalDate dateFrom = listForm.getDateFrom().get(LocalDate.now(), datePickerFormatter);
        LocalDate dateTo = listForm.getDateTo().get(LocalDate.now(), datePickerFormatter);

        Map<String, Object> argumentsMap = new HashMap<>();
        argumentsMap.put("sort", listForm.getSort());
        argumentsMap.put("order", listForm.getDir());
        argumentsMap.put("page", listForm.getPage());
        argumentsMap.put("numberOfRows", listForm.getNumberOfRows());
        argumentsMap.put("username", listForm.getUsername());
        argumentsMap.put("dateFrom.date", listForm.getDateFrom().getDate());
        argumentsMap.put("dateTo.date", listForm.getDateTo().getDate());
        argumentsMap.put("description", listForm.getDescription());

        PollingUid pollingUid = PollingUid.builder(sessionId, USER_ACTIVITY_LOG_KEY)
                .arguments(argumentsMap.values().toArray(ArrayUtils.EMPTY_OBJECT_ARRAY))
                .build();

        Callable<ModelAndView> worker = () -> {
            PaginatedListImpl<LoggedUserAction> loggedUserActions =
                    userActivityLogService.getUserActivityLogByFilter(
                            admin,
                            listForm.getUsername(),
                            listForm.getUserAction(),
                            dateFrom,
                            dateTo,
                            listForm.getDescription(),
                            listForm.getPage(),
                            listForm.getNumberOfRows(),
                            listForm.getSort(),
                            listForm.getDir(),
                            adminsFilter);
            model.addAttribute(USER_ACTIVITY_LOG_KEY, loggedUserActions);

            return new ModelAndView("useractivitylog_list", model.asMap());
        };

        ModelAndView modelAndView = new ModelAndView("redirect:/administration/useractivitylog/list.action",
                argumentsMap);

        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, modelAndView, worker);
    }


    @PostMapping(value = "/download.action")
    public ResponseEntity<StreamingResponseBody> download(ComAdmin admin, UserActivityLogForm form) throws Exception {
        DateTimeFormatter datePickerFormatter = admin.getDateFormatter();

        LocalDate localDateFrom = form.getDateFrom().get(LocalDate.now(), datePickerFormatter);
        LocalDate localDateTo = form.getDateTo().get(LocalDate.now(), datePickerFormatter);

        ZoneId zoneId = AgnUtils.getZoneId(admin);
        List<AdminEntry> admins = adminService.getAdminEntriesForUserActivityLog(admin);

        String tempFileName = String.format("user-activity-log-%s.csv", UUID.randomUUID());
        File mailingRecipientsExportTempDirectory = AgnUtils.createDirectory(AgnUtils.getTempDir() + File.separator + "UserActivityLogExport");
        File exportTempFile = new File(mailingRecipientsExportTempDirectory, tempFileName);

        UserActivityLogExportWorker exportWorker = exportWorkerFactory.getBuilderInstance()
                .setFromDate(DateUtilities.toDate(localDateFrom, zoneId))
                .setToDate(DateUtilities.toDate(localDateTo.plusDays(1), zoneId))
                .setFilterDescription(form.getDescription())
                .setFilterAction(form.getUserAction())
                .setFilterAdminUserName(form.getUsername())
                .setFilterAdmins(admins)
                .setExportFile(exportTempFile.getAbsolutePath())
                .setDateFormat(admin.getDateFormat())
                .setDateTimeFormat(admin.getDateTimeFormatWithSeconds())
                .setExportTimezone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId())
                .build();
        exportWorker.call();

        String dateString = new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date());
        String fileName = String.format("user-activity-log-%s.csv", dateString);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=\"%s\"", fileName))
                .contentLength(exportTempFile.length())
                .contentType(MediaType.parseMediaType(HttpUtils.CONTENT_TYPE_CSV))
                .body(new FileResponseBody(exportTempFile, true));
    }
}
