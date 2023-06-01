package com.agnitas.emm.core.mailing.web;

import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.service.UserActivityLogService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryBlockingService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/mailing/ajax")
@PermissionMapping("mailing_ajax")
public class MailingAjaxControllerOpenemm extends MailingAjaxController {

    public MailingAjaxControllerOpenemm(MailingService mailingService, UserActivityLogService userActivityLogService, ComMailingBaseService mailingBaseService, @Autowired(required = false) AutoImportService autoImportService, MailingDeliveryBlockingService mailingDeliveryBlockingService, ComTargetService targetService) {
        super(mailingService, userActivityLogService, mailingBaseService, autoImportService, mailingDeliveryBlockingService, targetService);
    }
}
