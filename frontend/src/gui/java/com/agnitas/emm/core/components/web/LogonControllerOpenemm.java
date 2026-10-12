package com.agnitas.emm.core.logon.web;

import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.loginmanager.service.LoginTrackService;
import com.agnitas.emm.core.logon.service.AdminSessionService;
import com.agnitas.emm.core.logon.service.ClientHostIdService;
import com.agnitas.emm.core.logon.service.HostAuthenticationService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.security.sessionbinding.web.service.SessionBindingService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import org.springframework.stereotype.Controller;

@Controller
public class LogonControllerOpenemm extends LogonController {

    public LogonControllerOpenemm(
            LogonService logonService,
            LoginTrackService loginTrackService,
            HostAuthenticationService hostAuthenticationService,
            WebStorage webStorage,
            ConfigService configService,
            UserActivityLogService userActivityLogService,
            ClientHostIdService clientHostIdService,
            SessionBindingService sessionBindingService,
            AdminSessionService adminSessionService
    ) {
        super(logonService, loginTrackService, hostAuthenticationService, webStorage, configService, userActivityLogService,
                clientHostIdService, sessionBindingService, adminSessionService);
    }

}
