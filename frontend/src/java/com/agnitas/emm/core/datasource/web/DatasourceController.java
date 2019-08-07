/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.datasource.web;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.agnitas.web.forms.FormUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.datasource.bean.DataSource;
import com.agnitas.emm.core.datasource.form.DatasourceForm;
import com.agnitas.service.ComWebStorage;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/importexport/datasource")
@PermissionMapping("datasource")
public class DatasourceController {
    private static final Logger logger = Logger.getLogger(DatasourceController.class);

    private UserActivityLogService userActivityLogService;
    private ExtendedConversionService conversionService;
    private DataSourceService dataSourceService;
    private WebStorage webStorage;

    public DatasourceController(UserActivityLogService userActivityLogService,
                                ExtendedConversionService conversionService,
                                DataSourceService dataSourceService,
                                WebStorage webStorage) {
        this.userActivityLogService = userActivityLogService;
        this.conversionService = conversionService;
        this.dataSourceService = dataSourceService;
        this.webStorage = webStorage;
    }

    @RequestMapping("/list.action")
    public String list(ComAdmin admin, DatasourceForm form, Model model, Popups popups) {
        try {
            FormUtils.syncNumberOfRows(webStorage, ComWebStorage.DATASOURCE_OVERVIEW, form);
            PaginatedListImpl<DataSource> dataSources = dataSourceService.getPaginatedDataSources(
                    admin.getCompanyID(),
                    form.getSort(),
                    form.getPage(),
                    form.getNumberOfRows(),
                    form.getDir());
            model.addAttribute("datasources", dataSources);
            userActivityLogService.writeUserActivityLog(admin, "datasource list", "show datasource IDs");
        } catch (Exception e) {
            logger.error("Error occurred: " + e.getMessage(), e);
            popups.alert("error.default.message");
        }
        return "datasource_list";
    }
}
