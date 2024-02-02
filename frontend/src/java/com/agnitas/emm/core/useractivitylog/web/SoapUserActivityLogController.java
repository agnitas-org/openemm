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
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogForm;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import jakarta.servlet.http.HttpSession;
import org.agnitas.beans.AdminEntry;
import org.agnitas.beans.factory.UserActivityLogExportWorkerFactory;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.agnitas.web.forms.PaginationForm;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Controller
@RequestMapping("/administration/soap-user/activitylog")
@PermissionMapping("soap.user.activity.log")
public class SoapUserActivityLogController extends AbstractUserActivityLogController implements XssCheckAware {

    protected SoapUserActivityLogController(WebStorage webStorage, AdminService adminService, UserActivityLogService userActivityLogService,
                                            UserActivityLogExportWorkerFactory exportWorkerFactory) {
        super(webStorage, adminService, userActivityLogService, exportWorkerFactory);
    }

    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public Pollable<ModelAndView> list(Admin admin, @ModelAttribute("form") UserActivityLogForm listForm, Model model, HttpSession session) {
        return getList(admin, listForm, model, session);
    }

    @Override
    protected String getUserActivityLogKey() {
        return "soapUserActivityLogKey";
    }

    @Override
    protected void syncNumberOfRows(PaginationForm form) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.SOAP_USERLOG_OVERVIEW, form);
    }

    @Override
    protected PaginatedListImpl<?> preparePaginatedList(UserActivityLogForm form, List<AdminEntry> admins, Admin admin) throws Exception {
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
    protected String redirectToListPage() {
        return "redirect:/administration/soap-user/activitylog/list.action";
    }

    @Override
    protected String getListViewName() {
        return "soap_useractivitylog_list";
    }

    @PostMapping(value = "/download.action")
    public ResponseEntity<StreamingResponseBody> download(Admin admin, UserActivityLogForm form) throws Exception {
        return downloadLogs(admin, form, UserActivityLogService.UserType.SOAP);
    }
}
