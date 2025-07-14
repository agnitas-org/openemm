package com.agnitas.emm.core.logon.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.logintracking.service.LoginTrackService;
import com.agnitas.emm.security.sessionbinding.web.service.SessionBindingService;
import com.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.logon.service.ClientHostIdService;
import com.agnitas.emm.core.logon.service.HostAuthenticationService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.service.WebStorage;

@Controller
public class LogonControllerOpenemm extends LogonController {
    public LogonControllerOpenemm(LogonService logonService, LoginTrackService loginTrackService, HostAuthenticationService hostAuthenticationService, WebStorage webStorage, ConfigService configService, UserActivityLogService userActivityLogService, final ClientHostIdService clientHostIdService, final AdminService adminService, final SessionBindingService sessionBindingService) {
    	super(logonService, loginTrackService, hostAuthenticationService, webStorage, configService, userActivityLogService, clientHostIdService, adminService, sessionBindingService);
    }
}
