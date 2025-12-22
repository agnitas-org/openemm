package com.agnitas.emm.core.mailing.web;

import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailing/ajax")
public class MailingAjaxControllerOpenemm extends MailingAjaxController {
    public MailingAjaxControllerOpenemm(MailingService mailingService, UserActivityLogService userActivityLogService, MailingBaseService mailingBaseService, TargetService targetService, TrackableLinkService trackableLinkService) {
        super(mailingService, userActivityLogService, mailingBaseService, targetService, trackableLinkService);
    }
}
