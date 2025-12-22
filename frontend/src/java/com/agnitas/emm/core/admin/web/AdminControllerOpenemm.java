package com.agnitas.emm.core.admin.web;

import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.commons.password.PasswordCheck;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.service.UserActivityLogService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.admin.form.AdminListFormSearchParams;
import com.agnitas.emm.core.admin.service.AdminChangesLogService;
import com.agnitas.emm.core.admin.service.AdminGroupService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.service.CSVService;
import com.agnitas.service.PdfService;
import com.agnitas.service.WebStorage;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/admin")
@SessionAttributes(types = AdminListFormSearchParams.class)
public class AdminControllerOpenemm extends AdminController {
    
    public AdminControllerOpenemm(ConfigService configService, AdminService adminService, CompanyService companyService, AdminGroupService adminGroupService, WebStorage webStorage, UserActivityLogService userActivityLogService, AdminChangesLogService adminChangesLogService, PasswordCheck passwordCheck, CSVService csvService, PdfService pdfService, ConversionService conversionService, TargetService targetService, LogonService logonService) {
        super(configService, adminService, companyService, adminGroupService, webStorage, userActivityLogService, adminChangesLogService, passwordCheck, csvService, pdfService, conversionService, targetService, logonService);
    }
}
