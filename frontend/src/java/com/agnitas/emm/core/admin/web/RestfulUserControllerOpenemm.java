package com.agnitas.emm.core.admin.web;

import com.agnitas.emm.core.admin.form.AdminListFormSearchParams;
import com.agnitas.emm.core.admin.service.AdminChangesLogService;
import com.agnitas.emm.core.admin.service.AdminGroupService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.logon.service.ComLogonService;
import com.agnitas.service.ComCSVService;
import com.agnitas.service.ComPDFService;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
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
                                        ComCSVService csvService, ComPDFService pdfService, ConversionService conversionService, ComLogonService logonService) {

        super(configService, adminService, companyService, adminGroupService, webStorage, userActivityLogService,
                adminChangesLogService, csvService, pdfService, conversionService, logonService);
    }
}
