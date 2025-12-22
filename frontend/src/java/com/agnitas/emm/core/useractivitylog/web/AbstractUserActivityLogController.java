/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import java.io.File;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.Callable;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.PollingUid;
import com.agnitas.beans.factory.UserActivityLogExportWorkerFactory;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.service.ActivityLogExportWorker;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.UserActivityLogService.UserType;
import com.agnitas.service.WebStorage;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.HttpUtils;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.PaginationForm;
import com.agnitas.web.mvc.DeleteFileAfterSuccessReadResource;
import com.agnitas.web.mvc.Pollable;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.servlet.ModelAndView;

public abstract class AbstractUserActivityLogController {

    protected final WebStorage webStorage;
    protected final UserActivityLogService userActivityLogService;
    protected final UserActivityLogExportWorkerFactory exportWorkerFactory;
    protected final ConfigService configService;

    protected AbstractUserActivityLogController(WebStorage webStorage, UserActivityLogService userActivityLogService, UserActivityLogExportWorkerFactory exportWorkerFactory, ConfigService configService) {
        this.webStorage = webStorage;
        this.userActivityLogService = userActivityLogService;
        this.exportWorkerFactory = exportWorkerFactory;
        this.configService = configService;
    }

    @InitBinder
    public void initBinder(WebDataBinder binder, Admin admin) {
        binder.registerCustomEditor(Date.class, new CustomDateEditor(admin.getDateFormat(), true));
    }

    protected Pollable<ModelAndView> getList(Admin admin, UserActivityLogFilterBase filter, Model model, HttpSession session) {
        syncNumberOfRows(filter);

        Callable<ModelAndView> worker = () -> {
            PaginatedList<?> loggedUserActions = preparePaginatedList(filter, admin);

            FormUtils.setPaginationParameters(filter, loggedUserActions);

            prepareModelAttributesForListPage(model, admin);
            model.addAttribute("usernames", userActivityLogService.getAvailableUsernames(admin, getUserType()));
            model.addAttribute("actions", loggedUserActions);
            model.addAttribute("logExpire", configService.getIntegerValue(ConfigValue.UserActivityLog_Expire, admin.getCompanyID()));

            return new ModelAndView(getListViewName(), model.asMap());
        };

        Map<String, Object> argumentsMap = filter.toMap();

        PollingUid pollingUid = PollingUid.builder(session.getId(), this.getClass().getName())
                .arguments(argumentsMap.values().toArray(ArrayUtils.EMPTY_OBJECT_ARRAY))
                .build();

        ModelAndView modelAndView = new ModelAndView(redirectToListPage(), argumentsMap);
        return new Pollable<>(pollingUid, Pollable.DEFAULT_TIMEOUT, modelAndView, worker);
    }

    protected abstract UserType getUserType();

    protected void prepareModelAttributesForListPage(Model model, Admin admin) {

    }

    protected abstract void syncNumberOfRows(PaginationForm form);

    protected abstract PaginatedList<?> preparePaginatedList(UserActivityLogFilterBase filter, Admin admin);

    protected abstract String redirectToListPage();

    protected abstract String getListViewName();

    public ResponseEntity<FileSystemResource> downloadLogs(Admin admin, UserActivityLogFilterBase filter, UserType type) {
        File exportTempFile = createTempExportFile();

        ActivityLogExportWorker exportWorker = createExportWorker(admin, filter, exportTempFile, type);
        exportWorker.call();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, String.format("attachment;filename=\"%s\"", getExportFileName(type)))
                .contentLength(exportTempFile.length())
                .contentType(MediaType.parseMediaType(HttpUtils.CONTENT_TYPE_CSV))
                .body(new DeleteFileAfterSuccessReadResource(exportTempFile));
    }

    private File createTempExportFile() {
        String tempFileName = String.format("user-activity-log-%s.csv", UUID.randomUUID());
        File mailingRecipientsExportTempDirectory = AgnUtils.createDirectory(AgnUtils.getTempDir() + File.separator + "UserActivityLogExport");
        return new File(mailingRecipientsExportTempDirectory, tempFileName);
    }

    private ActivityLogExportWorker createExportWorker(Admin admin, UserActivityLogFilterBase filter, File tempFile, UserType type) {
        return exportWorkerFactory.getBuilderInstance()
                .setFilter(filter)
                .setAdmin(admin)
                .setExportFile(tempFile.getAbsolutePath())
                .setDateFormat(admin.getDateFormat())
                .setDateTimeFormat(admin.getDateTimeFormatWithSeconds())
                .setUserActivityLogService(userActivityLogService)
                .setUserActivityType(type)
                .setExportTimezone(TimeZone.getTimeZone(admin.getAdminTimezone()).toZoneId()).build();
    }

    private String getExportFileName(UserType userType) {
        String filePrefix = "user-activity-log";

        if (UserType.REST.equals(userType)) {
            filePrefix = "rest-" + filePrefix;
        } else if (UserType.SOAP.equals(userType)) {
            filePrefix = "soap-" + filePrefix;
        }

        return String.format(
                "%s-%s.csv",
                filePrefix,
                new SimpleDateFormat(DateUtilities.YYYY_MM_DD_HH_MM_SS_FORFILENAMES).format(new Date())
        );
    }

    protected Date getCurrentDate(Admin admin) {
        final ZoneId zoneId = admin.getZoneId();
        return DateUtilities.toDate(LocalDate.now(zoneId), zoneId);
    }
}
