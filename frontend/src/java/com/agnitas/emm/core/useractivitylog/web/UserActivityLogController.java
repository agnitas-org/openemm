/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import java.util.Arrays;
import java.util.Date;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.factory.UserActivityLogExportWorkerFactory;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.UserActivityLogService.UserType;
import com.agnitas.service.WebStorage;
import com.agnitas.util.UserActivityLogActions;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.PaginationForm;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.AlwaysAllowed;
import jakarta.servlet.http.HttpSession;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.SessionAttributes;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/administration/useractivitylog")
@AlwaysAllowed
@SessionAttributes(types = UserActivityLogSearchParams.class)
public class UserActivityLogController extends AbstractUserActivityLogController implements XssCheckAware {

    protected UserActivityLogController(WebStorage webStorage, UserActivityLogService userActivityLogService,
                                        UserActivityLogExportWorkerFactory exportWorkerFactory, ConfigService configService) {
        super(webStorage, userActivityLogService, exportWorkerFactory, configService);
    }

    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public Pollable<ModelAndView> list(Admin admin, @ModelAttribute("filter") UserActivityLogFilter filter, @ModelAttribute UserActivityLogSearchParams searchParams,
                                       @RequestParam(required = false) Boolean restoreSort, Model model, HttpSession session) {
        searchParams.restoreParams(filter);
        FormUtils.updateSortingState(webStorage, WebStorage.USERLOG_OVERVIEW, filter, restoreSort);
        return getList(admin, filter, model, session);
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
    protected String getListViewName() {
        return "useractivitylog_list";
    }

    @Override
    protected void prepareModelAttributesForListPage(Model model, Admin admin) {
        super.prepareModelAttributesForListPage(model, admin);
        model.addAttribute("userActions", Arrays.asList(UserActivityLogActions.values()));
    }

    @Override
    protected PaginatedList<?> preparePaginatedList(UserActivityLogFilterBase filter, Admin admin) {
        final UserActivityLogFilter ualFilter = (UserActivityLogFilter) filter;
        return userActivityLogService.getUserActivityLogByFilter(ualFilter, admin);
    }

    @GetMapping(value = "/download.action")
    public ResponseEntity<FileSystemResource> download(Admin admin, UserActivityLogFilter filter) {
        return downloadLogs(admin, filter, UserType.GUI);
    }

    @GetMapping("/search.action")
    public String search(@ModelAttribute UserActivityLogFilter filter, @ModelAttribute UserActivityLogSearchParams searchParams) {
        searchParams.storeParams(filter);
        return redirectToListPage() + "?restoreSort=true";
    }

    @ModelAttribute
    public UserActivityLogSearchParams getSearchParams(Admin admin) {
        Date today = getCurrentDate(admin);
        return new UserActivityLogSearchParams(new DateRange(today, today));
    }

    @Override
    protected UserType getUserType() {
        return UserType.GUI;
    }

}
