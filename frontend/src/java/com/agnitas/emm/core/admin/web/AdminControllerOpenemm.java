package com.agnitas.emm.core.admin.web;

import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.emm.core.commons.password.PasswordCheck;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.admin.form.AdminListFormSearchParams;
import com.agnitas.emm.core.admin.service.AdminChangesLogService;
import com.agnitas.emm.core.admin.service.AdminGroupService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ComCSVService;
import com.agnitas.service.ComPDFService;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/admin")
@PermissionMapping("admin")
@SessionAttributes(types = AdminListFormSearchParams.class)
public class AdminControllerOpenemm extends AdminControllerBase {
    
    public AdminControllerOpenemm(ConfigService configService, AdminService adminService, CompanyService companyService, AdminGroupService adminGroupService, WebStorage webStorage, UserActivityLogService userActivityLogService, AdminChangesLogService adminChangesLogService, PasswordCheck passwordCheck, ComCSVService csvService, ComPDFService pdfService, ConversionService conversionService, ComTargetService targetService, ComLogonService logonService) {
        super(configService, adminService, companyService, adminGroupService, webStorage, userActivityLogService, adminChangesLogService, passwordCheck, csvService, pdfService, conversionService, targetService, logonService);
    }
}
