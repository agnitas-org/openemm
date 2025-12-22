package com.agnitas.emm.core.mailing.web;

import com.agnitas.emm.core.components.service.MailingRecipientsService;
import com.agnitas.emm.core.mailing.forms.MailingRecipientsFormSearchParams;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingRecipientExportWorkerFactory;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/mailing")
@RequiredPermission("mailing.recipients.show")
@SessionAttributes(types = MailingRecipientsFormSearchParams.class)
public class MailingRecipientsControllerOpenemm extends MailingRecipientsController {

    public MailingRecipientsControllerOpenemm(UserActivityLogService userActivityLogService, WebStorage webStorage, ColumnInfoService columnInfoService,
                                              MailingBaseService mailingBaseService, GridServiceWrapper gridService, MailingRecipientsService mailingRecipientsService,
                                              MailingRecipientExportWorkerFactory exportWorkerFactory) {
        super(userActivityLogService, webStorage, columnInfoService, mailingBaseService, gridService, mailingRecipientsService, exportWorkerFactory);
    }

}
