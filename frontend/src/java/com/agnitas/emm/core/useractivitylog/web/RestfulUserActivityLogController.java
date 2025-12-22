/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.useractivitylog.web;

import java.util.Date;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.beans.factory.UserActivityLogExportWorkerFactory;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.useractivitylog.forms.RestfulUserActivityLogFilter;
import com.agnitas.emm.core.useractivitylog.forms.UserActivityLogFilterBase;
import com.agnitas.http.HttpRequest;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.UserActivityLogService.UserType;
import com.agnitas.service.WebStorage;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.PaginationForm;
import com.agnitas.web.mvc.Pollable;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
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
@RequestMapping("/administration/restful-user/activitylog")
@RequiredPermission("adminlog.show")
@SessionAttributes(types = RestfulUserActivityLogSearchParams.class)
public class RestfulUserActivityLogController extends AbstractUserActivityLogController implements XssCheckAware {

    protected RestfulUserActivityLogController(WebStorage webStorage, UserActivityLogService userActivityLogService,
                                               UserActivityLogExportWorkerFactory exportWorkerFactory, ConfigService configService) {
        super(webStorage, userActivityLogService, exportWorkerFactory, configService);
    }

    @RequestMapping(value = "/list.action", method = {RequestMethod.GET, RequestMethod.POST})
    public Pollable<ModelAndView> list(@ModelAttribute("filter") RestfulUserActivityLogFilter filter, @ModelAttribute RestfulUserActivityLogSearchParams searchParams,
                                       @RequestParam(required = false) Boolean restoreSort, Admin admin, Model model, HttpSession session) {
        searchParams.restoreParams(filter);
        FormUtils.updateSortingState(webStorage, WebStorage.RESTFUL_USERLOG_OVERVIEW, filter, restoreSort);
        return getList(admin, filter, model, session);
    }

    @Override
    protected void prepareModelAttributesForListPage(Model model, Admin admin) {
        super.prepareModelAttributesForListPage(model, admin);
        model.addAttribute("httpMethods", HttpRequest.HttpMethod.values());
    }

    @Override
    protected void syncNumberOfRows(PaginationForm form) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.RESTFUL_USERLOG_OVERVIEW, form);
    }

    @Override
    protected PaginatedList<?> preparePaginatedList(UserActivityLogFilterBase filter, Admin admin) {
        final RestfulUserActivityLogFilter restfulFilter = (RestfulUserActivityLogFilter) filter;
        return userActivityLogService.getRestfulUserActivityLogByFilter(restfulFilter, admin);
    }

    @Override
    protected String redirectToListPage() {
        return "redirect:/administration/restful-user/activitylog/list.action";
    }

    @Override
    protected String getListViewName() {
        return "restful_useractivitylog_list";
    }

    @GetMapping(value = "/download.action")
    public ResponseEntity<FileSystemResource> download(Admin admin, RestfulUserActivityLogFilter filter) {
        return downloadLogs(admin, filter, UserType.REST);
    }

    @GetMapping("/search.action")
    public String search(@ModelAttribute RestfulUserActivityLogFilter filter, @ModelAttribute RestfulUserActivityLogSearchParams searchParams) {
        searchParams.storeParams(filter);
        return redirectToListPage() + "?restoreSort=true";
    }

    @ModelAttribute
    public RestfulUserActivityLogSearchParams getSearchParams(Admin admin) {
        Date today = getCurrentDate(admin);
        return new RestfulUserActivityLogSearchParams(new DateRange(today, today));
    }

    @Override
    protected UserType getUserType() {
        return UserType.REST;
    }
}
