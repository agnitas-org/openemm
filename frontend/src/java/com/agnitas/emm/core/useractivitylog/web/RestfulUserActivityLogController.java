/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.useractivitylog.forms.RestfulUserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogForm;
import com.agnitas.http.HttpRequest;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpSession;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.factory.UserActivityLogExportWorkerFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/administration/restful-user/activitylog")
@PermissionMapping("restful.user.activity.log")
@SessionAttributes(types = RestfulUserActivityLogSearchParams.class)
public class RestfulUserActivityLogController extends AbstractUserActivityLogController implements XssCheckAware {

    protected RestfulUserActivityLogController(WebStorage webStorage, AdminService adminService, UserActivityLogService userActivityLogService,
                                               UserActivityLogExportWorkerFactory exportWorkerFactory, ConfigService configService) {
        super(webStorage, adminService, userActivityLogService, exportWorkerFactory, configService);
    }

    @Override
    protected List<AdminEntry> getAdminEntries(Admin admin) {
        return adminService.getAdminEntriesForUserActivityLog(admin, UserActivityLogService.UserType.REST);
    }

    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public Pollable<ModelAndView> list(@ModelAttribute("form") UserActivityLogForm listForm, Admin admin, Model model, HttpSession session) {
        return getList(admin, listForm, model, session);
    }

    @RequestMapping(value = "/listRedesigned.action", method = {RequestMethod.GET, RequestMethod.POST})
    @PermissionMapping("list")
    public Pollable<ModelAndView> listRedesigned(@ModelAttribute("filter") RestfulUserActivityLogFilter filter, @ModelAttribute RestfulUserActivityLogSearchParams searchParams,
                                                 @RequestParam(required = false) boolean restoreSort, Admin admin, Model model, HttpSession session) {
        FormUtils.syncSearchParams(searchParams, filter, true);
        FormUtils.updateSortingState(webStorage, WebStorage.RESTFUL_USERLOG_OVERVIEW, filter, restoreSort);
        return getListRedesigned(admin, filter, model, session);
    }

    @Override
    protected void prepareModelAttributesForListPage(Model model, Admin admin) {
        super.prepareModelAttributesForListPage(model, admin);
        model.addAttribute("httpMethods", HttpRequest.HttpMethod.values());
    }

    @Override
    protected String getUserActivityLogKey() {
        return "restfulUserActivityLogKey";
    }

    @Override
    protected void syncNumberOfRows(PaginationForm form) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.RESTFUL_USERLOG_OVERVIEW, form);
    }

    @Override
    protected PaginatedListImpl<?> preparePaginatedList(UserActivityLogForm form, List<AdminEntry> admins, Admin admin) throws Exception {
        List<AdminEntry> adminsFilter = admin.permissionAllowed(Permission.MASTERLOG_SHOW) ? null : admins;
        DateTimeFormatter dateFormatter = admin.getDateFormatter();

        LocalDate dateFrom = form.getDateFrom().get(LocalDate.now(), dateFormatter);
        LocalDate dateTo = form.getDateTo().get(LocalDate.now(), dateFormatter);

        return userActivityLogService.getRestfulUserActivityLogByFilter(
                admin,
                form.getUsername(),
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
        final RestfulUserActivityLogFilter restfulFilter = (RestfulUserActivityLogFilter) filter;
        return userActivityLogService.getRestfulUserActivityLogByFilterRedesigned(restfulFilter, admins, admin);
    }

    @Override
    protected String redirectToListPage() {
        return "redirect:/administration/restful-user/activitylog/list.action";
    }

    @Override
    protected String redirectToRedesignedListPage() {
        return "redirect:/administration/restful-user/activitylog/listRedesigned.action";
    }

    @Override
    protected String getListViewName() {
        return "restful_useractivitylog_list";
    }

    @PostMapping(value = "/download.action")
    public ResponseEntity<StreamingResponseBody> download(Admin admin, UserActivityLogForm form) throws Exception {
        return downloadLogs(admin, form, UserActivityLogService.UserType.REST);
    }

    @GetMapping(value = "/downloadRedesigned.action")
    @PermissionMapping("download")
    public ResponseEntity<StreamingResponseBody> downloadRedesigned(Admin admin, RestfulUserActivityLogFilter filter) throws Exception {
        filter.setCompanyId(admin.getCompanyID());
        return downloadLogsRedesigned(admin, filter, UserActivityLogService.UserType.REST);
    }

    @GetMapping("/search.action")
    public String search(@ModelAttribute RestfulUserActivityLogFilter filter, @ModelAttribute RestfulUserActivityLogSearchParams searchParams, RedirectAttributes model) {
        FormUtils.syncSearchParams(searchParams, filter, false);
        model.addFlashAttribute("filter", filter);

        return redirectToRedesignedListPage() + "?restoreSort=true";
    }

    @ModelAttribute
    public RestfulUserActivityLogSearchParams getSearchParams() {
        return new RestfulUserActivityLogSearchParams();
    }
}
