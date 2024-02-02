package com.agnitas.emm.core.mailing.web;

import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailing")
@PermissionMapping("mailing.recipients")
public class MailingRecipientsControllerOpenemm extends MailingRecipientsController {

    public MailingRecipientsControllerOpenemm(UserActivityLogService userActivityLogService, WebStorage webStorage, ColumnInfoService columnInfoService, ComMailingBaseService mailingBaseService, GridServiceWrapper gridService) {
        super(userActivityLogService, webStorage, columnInfoService, mailingBaseService, gridService);
    }
}
