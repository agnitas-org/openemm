package com.agnitas.emm.core.birtstatistics.mailing.web;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtreport.service.ComBirtReportService;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.mailing.autooptimization.service.ComOptimizationService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.company.service.CompanyService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/statistics/mailing")
@PermissionMapping("mailing.stat")
public class MailingStatisticControllerOpenemm extends MailingBirtStatController {
    public MailingStatisticControllerOpenemm(
    		ComMailingBaseService mailingBaseService,
    		MailinglistApprovalService mailinglistApprovalService,
            WebStorage webStorage,
            ConversionService conversionService,
            UserActivityLogService userActivityLogService,
            AdminService adminService,
            GridServiceWrapper gridServiceWrapper,
            BirtStatisticsService birtStatisticsService,
            CompanyService companyService,
            ComTargetService targetService,
            ComBirtReportService birtReportService,
			ComOptimizationService optimizationService) {
        super(mailingBaseService,
				mailinglistApprovalService,
				webStorage,
				conversionService,
				userActivityLogService,
				adminService,
				gridServiceWrapper,
				birtStatisticsService,
				companyService,
				targetService,
				birtReportService,
				optimizationService);
    }
}