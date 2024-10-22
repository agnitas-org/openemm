package com.agnitas.emm.core.mailing.web;

import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailing/ajax")
@PermissionMapping("mailing_ajax")
public class MailingAjaxControllerOpenemm extends MailingAjaxController {
    public MailingAjaxControllerOpenemm(MailingService mailingService, UserActivityLogService userActivityLogService, ComMailingBaseService mailingBaseService, ComTargetService targetService, TrackableLinkService trackableLinkService) {
        super(mailingService, userActivityLogService, mailingBaseService, targetService, trackableLinkService);
    }
}
