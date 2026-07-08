package com.agnitas.emm.core.mailinglist.web;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.mapper.MailinglistMapper;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailinglist")
public class MailinglistControllerOpenemm extends MailinglistController {

    public MailinglistControllerOpenemm(MailinglistService mailinglistService, UserActivityLogService userActivityLogService, MailinglistMapper mailinglistMapper, BirtStatisticsService birtStatisticsService, AdminService adminService) {
        super(mailinglistService, userActivityLogService, mailinglistMapper, birtStatisticsService, adminService);
    }

}