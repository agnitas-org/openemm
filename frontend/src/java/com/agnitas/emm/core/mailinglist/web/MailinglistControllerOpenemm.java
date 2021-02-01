package com.agnitas.emm.core.mailinglist.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.ComMailinglistService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/mailinglist")
@PermissionMapping("mailinglist")
public class MailinglistControllerOpenemm extends MailinglistControllerBase {

    public MailinglistControllerOpenemm(ComMailinglistService mailinglistService, UserActivityLogService userActivityLogService, ConversionService conversionService, BirtStatisticsService birtStatisticsService, WebStorage webStorage, AdminService adminService, ConfigService configService, MailinglistApprovalService mailinglistApprovalService) {
        super(mailinglistService, userActivityLogService, conversionService, birtStatisticsService, webStorage, adminService, configService, mailinglistApprovalService);
    }
}