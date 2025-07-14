package com.agnitas.emm.core.admin.web;

import com.agnitas.emm.core.admin.form.AdminListFormSearchParams;
import com.agnitas.emm.core.admin.service.AdminChangesLogService;
import com.agnitas.emm.core.admin.service.AdminGroupService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.logon.service.LogonService;
import com.agnitas.service.CSVService;
import com.agnitas.service.PdfService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.agnitas.emm.core.company.service.CompanyService;
import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.service.UserActivityLogService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/restfulUser")
@PermissionMapping("restfulUser")
@SessionAttributes(types = AdminListFormSearchParams.class)
public class RestfulUserControllerOpenemm extends RestfulUserController {

    public RestfulUserControllerOpenemm(ConfigService configService, AdminService adminService, CompanyService companyService, AdminGroupService adminGroupService,
                                        WebStorage webStorage, UserActivityLogService userActivityLogService, AdminChangesLogService adminChangesLogService,
                                        CSVService csvService, PdfService pdfService, ConversionService conversionService, LogonService logonService) {

        super(configService, adminService, companyService, adminGroupService, webStorage, userActivityLogService,
                adminChangesLogService, csvService, pdfService, conversionService, logonService);
    }
}
