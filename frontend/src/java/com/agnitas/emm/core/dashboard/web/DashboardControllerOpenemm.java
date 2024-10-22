package com.agnitas.emm.core.dashboard.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.stereotype.Controller;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.dashboard.service.DashboardService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@PermissionMapping("dashboard")
public class DashboardControllerOpenemm extends DashboardController {

    public DashboardControllerOpenemm(AdminService adminService, DashboardService dashboardService, ConfigService configService) {
        super(adminService, dashboardService, configService);
    }
}
