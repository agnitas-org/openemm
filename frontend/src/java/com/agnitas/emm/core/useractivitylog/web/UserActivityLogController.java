/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilter;
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
import com.agnitas.util.UserActivityLogActions;
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
@RequestMapping("/administration/useractivitylog")
@PermissionMapping("user.activity.log")
@SessionAttributes(types = UserActivityLogSearchParams.class)
public class UserActivityLogController extends AbstractUserActivityLogController implements XssCheckAware {

    protected UserActivityLogController(WebStorage webStorage, AdminService adminService, UserActivityLogService userActivityLogService,
                                        UserActivityLogExportWorkerFactory exportWorkerFactory, ConfigService configService) {
        super(webStorage, adminService, userActivityLogService, exportWorkerFactory, configService);
    }

    @Override
    protected List<AdminEntry> getAdminEntries(Admin admin) {
        return adminService.getAdminEntriesForUserActivityLog(admin, UserActivityLogService.UserType.GUI);
    }

    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    // TODO: EMMGUI-714: Remove when removing of old design
    public Pollable<ModelAndView> list(Admin admin, @ModelAttribute("form") UserActivityLogForm listForm, Model model, HttpSession session) {
        return getList(admin, listForm, model, session);
    }

    @RequestMapping(value = "/listRedesigned.action", method = {RequestMethod.GET, RequestMethod.POST})
    @PermissionMapping("list")
    public Pollable<ModelAndView> listRedesigned(Admin admin, @ModelAttribute("filter") UserActivityLogFilter filter, @ModelAttribute UserActivityLogSearchParams searchParams,
                                                 @RequestParam(required = false) Boolean restoreSort, Model model, HttpSession session) {
        FormUtils.syncSearchParams(searchParams, filter, true);
        FormUtils.updateSortingState(webStorage, WebStorage.USERLOG_OVERVIEW, filter, restoreSort);
        return getListRedesigned(admin, filter, model, session);
    }

    @Override
    protected void syncNumberOfRows(PaginationForm form) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.USERLOG_OVERVIEW, form);
    }

    @Override
    protected String redirectToListPage() {
        return "redirect:/administration/useractivitylog/list.action";
    }

    @Override
    protected String redirectToRedesignedListPage() {
        return "redirect:/administration/useractivitylog/listRedesigned.action";
    }

    @Override
    protected String getListViewName() {
        return "useractivitylog_list";
    }

    @Override
    protected void prepareModelAttributesForListPage(Model model, Admin admin) {
        super.prepareModelAttributesForListPage(model, admin);
        model.addAttribute("userActions", Arrays.asList(UserActivityLogActions.values()));
    }

    @Override
    protected PaginatedListImpl<?> preparePaginatedList(UserActivityLogForm form, List<AdminEntry> admins, Admin admin) {
        List<AdminEntry> adminsFilter = admin.permissionAllowed(Permission.MASTERLOG_SHOW) ? null : admins;
        DateTimeFormatter dateFormatter = admin.getDateFormatter();

        LocalDate dateFrom = form.getDateFrom().get(LocalDate.now(), dateFormatter);
        LocalDate dateTo = form.getDateTo().get(LocalDate.now(), dateFormatter);

        return userActivityLogService.getUserActivityLogByFilter(
                admin,
                form.getUsername(),
                form.getUserAction(),
                dateFrom,
                dateTo,
                form.getDescription(),
                form.getPage(),
                form.getNumberOfRows(),
                form.getSort(),
                form.getDir(),
                adminsFilter
        );
    }

    @Override
    protected PaginatedListImpl<?> preparePaginatedListRedesigned(UserActivityLogFilterBase filter, List<AdminEntry> admins, Admin admin) {
        final UserActivityLogFilter ualFilter = (UserActivityLogFilter) filter;
        return userActivityLogService.getUserActivityLogByFilterRedesigned(ualFilter, admins, admin);
    }

    @PostMapping(value = "/download.action")
    // TODO: EMMGUI-714: Remove when removing of old design
    public ResponseEntity<StreamingResponseBody> download(Admin admin, UserActivityLogForm form) {
        return downloadLogs(admin, form, UserActivityLogService.UserType.GUI);
    }

    @GetMapping(value = "/downloadRedesigned.action")
    @PermissionMapping("download")
    public ResponseEntity<FileSystemResource> downloadRedesigned(Admin admin, UserActivityLogFilter filter) {
        return downloadLogsRedesigned(admin, filter, UserActivityLogService.UserType.GUI);
    }

    @GetMapping("/search.action")
    public String search(@ModelAttribute UserActivityLogFilter filter, @ModelAttribute UserActivityLogSearchParams searchParams) {
        FormUtils.syncSearchParams(searchParams, filter, false);
        return redirectToRedesignedListPage() + "?restoreSort=true";
    }

    @ModelAttribute
    public UserActivityLogSearchParams getSearchParams(Admin admin) {
        Date today = getCurrentDate(admin);
        return new UserActivityLogSearchParams(new DateRange(today, today));
    }
}
