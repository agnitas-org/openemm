package com.agnitas.emm.core.target.web;

import org.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ComWebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/target")
@PermissionMapping("target")
public class TargetControllerOpenemm extends TargetController {

    public TargetControllerOpenemm(ComTargetService targetService, ComWebStorage webStorage, UserActivityLogService userActivityLogService, MailingService mailingService, ComBirtReportDao birtReportDao) {
        super(targetService, webStorage, userActivityLogService, mailingService, birtReportDao);
    }
}
