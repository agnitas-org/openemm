
//-*- scope: openemm -*-
package com.agnitas.emm.core.admin.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.component.service.ComponentService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.company.service.CompanyTokenService;
import com.agnitas.emm.core.mobile.service.ComDeviceService;
import com.agnitas.emm.core.userform.service.UserFormExecutionService;
import com.agnitas.emm.core.userform.web.UserFormExecuteController;
import com.agnitas.service.AgnTagService;

@Controller
@RequestMapping("/")
public class UserFormExecuteControllerOpenemm extends UserFormExecuteController {
	public UserFormExecuteControllerOpenemm(final ConfigService configService, final UserFormExecutionService userFormExecutionService, final ComCompanyDao companyDao, final CompanyTokenService companyTokenService, final AgnTagService agnTagService, final ComDeviceService deviceService, final ComponentService componentService) {
		super(configService, userFormExecutionService, companyDao, companyTokenService, agnTagService, deviceService, componentService);
	}
}
