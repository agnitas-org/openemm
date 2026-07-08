/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.datasource.web;

import com.agnitas.beans.Admin;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.PaginationForm;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/importexport/datasource")
public class DatasourceController implements XssCheckAware {
	
    private final UserActivityLogService userActivityLogService;
    private final DataSourceService dataSourceService;
    private final WebStorage webStorage;

    public DatasourceController(UserActivityLogService userActivityLogService,
                                DataSourceService dataSourceService,
                                WebStorage webStorage) {
        this.userActivityLogService = userActivityLogService;
        this.dataSourceService = dataSourceService;
        this.webStorage = webStorage;
    }

    @RequestMapping("/list.action")
    @RequiredPermission("datasource.show")
    public String list(Admin admin, @ModelAttribute("datasourceForm") PaginationForm form, Model model) {
        FormUtils.syncNumberOfRows(webStorage, WebStorage.DATASOURCE_OVERVIEW, form);
        model.addAttribute("datasources", dataSourceService.getDataSources(admin.getCompanyID()));
        userActivityLogService.writeUserActivityLog(admin, "datasource list", "show datasource IDs");

        return "datasource_list";
    }
}
