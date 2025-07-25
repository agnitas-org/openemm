/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.datasource.web;

import com.agnitas.beans.Admin;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.web.forms.FormUtils;
import com.agnitas.web.forms.PaginationForm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/importexport/datasource")
@PermissionMapping("datasource")
public class DatasourceController implements XssCheckAware {
	
    private static final Logger logger = LogManager.getLogger(DatasourceController.class);

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
    public String list(Admin admin, @ModelAttribute("datasourceForm") PaginationForm form, Model model, Popups popups) {
        try {
            FormUtils.syncNumberOfRows(webStorage, WebStorage.DATASOURCE_OVERVIEW, form);
            model.addAttribute("datasources", dataSourceService.getDataSourcesJson(admin));
            userActivityLogService.writeUserActivityLog(admin, "datasource list", "show datasource IDs");
        } catch (Exception e) {
            logger.error("Error occurred: {}", e.getMessage(), e);
            popups.alert("error.default.message");
        }
        return "datasource_list";
    }
}
