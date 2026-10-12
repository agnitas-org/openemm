package com.agnitas.emm.core.user.web;

import com.agnitas.dao.AdminGroupDao;
import com.agnitas.dao.AdminPreferencesDao;
import com.agnitas.dao.CompanyDao;
import com.agnitas.dao.EmmLayoutBaseDao;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.commons.password.PasswordCheck;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.oauth.service.OAuth2ExternalIdentityService;
import com.agnitas.emm.core.user.service.UserSelfService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.AlwaysAllowed;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/user/self")
@AlwaysAllowed
public class UserSelfControllerOpenemm extends UserSelfController {

    public UserSelfControllerOpenemm(WebStorage webStorage, CompanyDao companyDao, AdminPreferencesDao adminPreferencesDao, AdminService adminService, AdminGroupDao adminGroupDao, ConfigService configService, UserActivityLogService userActivityLogService, PasswordCheck passwordCheck, EmmLayoutBaseDao layoutBaseDao, LogonService logonService, UserSelfService userSelfService, OAuth2ExternalIdentityService oAuth2ExternalIdentityService) {
        super(webStorage, companyDao, adminPreferencesDao, adminService, adminGroupDao, configService, userActivityLogService, passwordCheck, layoutBaseDao, logonService, userSelfService, oAuth2ExternalIdentityService);
    }

}
