/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.useractivitylog.forms.SoapUserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogForm;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpSession;
import com.agnitas.beans.AdminEntry;
import com.agnitas.beans.factory.UserActivityLogExportWorkerFactory;
import com.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.PaginationForm;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

@Controller
@RequestMapping("/administration/soap-user/activitylog")
@PermissionMapping("soap.user.activity.log")
@SessionAttributes(types = SoapUserActivityLogSearchParams.class)
public class SoapUserActivityLogController extends AbstractUserActivityLogController implements XssCheckAware {

    protected SoapUserActivityLogController(WebStorage webStorage, AdminService adminService, UserActivityLogService userActivityLogService,
                                            UserActivityLogExportWorkerFactory exportWorkerFactory, ConfigService configService) {
        super(webStorage, adminService, userActivityLogService, exportWorkerFactory, configService);
    }

    @Override
    protected List<AdminEntry> getAdminEntries(Admin admin) {
        return adminService.getAdminEntriesForUserActivityLog(admin, UserActivityLogService.UserType.SOAP);
    }

    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    // TODO: EMMGUI-714: Remove when removing of old design
    public Pollable<ModelAndView> list(Admin admin, @ModelAttribute("form") UserActivityLogForm listForm, Model model, HttpSession session) {
        return getList(admin, listForm, model, session);
    }

    @RequestMapping(value = "/listRedesigned.action", method = {RequestMethod.GET, RequestMethod.POST})
    @PermissionMapping("list")
    public Pollable<ModelAndView> listRedesigned(Admin admin, @ModelAttribute("filter") SoapUserActivityLogFilter filter, @ModelAttribute SoapUserActivityLogSearchParams searchParams,
                                                 @RequestParam(required = false) Boolean restoreSort, Model model, HttpSession session) {
        FormUtils.syncSearchParams(searchParams, filter, true);
        FormUtils.updateSortingState(webStorage, WebStorage.SOAP_USERLOG_OVERVIEW, filter, restoreSort);
        return getListRedesigned(admin, filter, model, session);
    }

    @Override
    protected void syncNumberOfRows(PaginationForm form) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.SOAP_USERLOG_OVERVIEW, form);
    }

    @Override
    protected PaginatedListImpl<?> preparePaginatedList(UserActivityLogForm form, List<AdminEntry> admins, Admin admin) {
        List<AdminEntry> adminsFilter = admin.permissionAllowed(Permission.MASTERLOG_SHOW) ? null : admins;
        DateTimeFormatter dateFormatter = admin.getDateFormatter();

        LocalDate dateFrom = form.getDateFrom().get(LocalDate.now(), dateFormatter);
        LocalDate dateTo = form.getDateTo().get(LocalDate.now(), dateFormatter);

        return userActivityLogService.getSoapUserActivityLogByFilter(
                admin,
                form.getUsername(),
                dateFrom,
                dateTo,
                form.getPage(),
                form.getNumberOfRows(),
                form.getSort(),
                form.getDir(),
                adminsFilter
        );
    }

    @Override
    protected PaginatedListImpl<?> preparePaginatedListRedesigned(UserActivityLogFilterBase filter, List<AdminEntry> admins, Admin admin) {
        final SoapUserActivityLogFilter ualFilter = (SoapUserActivityLogFilter) filter;
        return userActivityLogService.getSoapUserActivityLogByFilterRedesigned(ualFilter, admins, admin);
    }

    @Override
    protected String redirectToListPage() {
        return "redirect:/administration/soap-user/activitylog/list.action";
    }

    @Override
    protected String redirectToRedesignedListPage() {
        return "redirect:/administration/soap-user/activitylog/listRedesigned.action";
    }

    @Override
    protected String getListViewName() {
        return "soap_useractivitylog_list";
    }

    @PostMapping(value = "/download.action")
    // TODO: EMMGUI-714: Remove when removing of old design
    public ResponseEntity<StreamingResponseBody> download(Admin admin, UserActivityLogForm form) {
        return downloadLogs(admin, form, UserActivityLogService.UserType.SOAP);
    }

    @GetMapping(value = "/downloadRedesigned.action")
    @PermissionMapping("download")
    public ResponseEntity<FileSystemResource> downloadRedesigned(Admin admin, SoapUserActivityLogFilter filter) {
        return downloadLogsRedesigned(admin, filter, UserActivityLogService.UserType.SOAP);
    }

    @GetMapping("/search.action")
    public String search(@ModelAttribute SoapUserActivityLogFilter filter, @ModelAttribute SoapUserActivityLogSearchParams searchParams) {
        FormUtils.syncSearchParams(searchParams, filter, false);
        return redirectToRedesignedListPage() + "?restoreSort=true";
    }

    @ModelAttribute
    public SoapUserActivityLogSearchParams getSearchParams(Admin admin) {
        Date today = getCurrentDate(admin);
        return new SoapUserActivityLogSearchParams(new DateRange(today, today));
    }
}
