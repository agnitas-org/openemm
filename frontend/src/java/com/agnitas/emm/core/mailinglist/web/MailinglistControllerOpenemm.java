package com.agnitas.emm.core.mailinglist.web;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.service.UserActivityLogService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailinglist")
public class MailinglistControllerOpenemm extends MailinglistController {

    public MailinglistControllerOpenemm(MailinglistService mailinglistService, UserActivityLogService userActivityLogService, ConversionService conversionService, BirtStatisticsService birtStatisticsService, AdminService adminService, ConfigService configService) {
        super(mailinglistService, userActivityLogService, conversionService, birtStatisticsService, adminService, configService);
    }

}