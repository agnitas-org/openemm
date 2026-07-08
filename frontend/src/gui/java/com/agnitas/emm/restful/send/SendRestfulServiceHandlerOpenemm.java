package com.agnitas.emm.restful.send;

import com.agnitas.dao.BindingEntryDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailing.service.SendActionbasedMailingService;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.useractivitylog.dao.RestfulUserActivityLogDao;
import com.agnitas.mailing.preview.service.MailingPreviewService;
import com.agnitas.util.ClassicTemplateGenerator;
import org.springframework.stereotype.Component;

@Component("RestfulServiceHandler_send")
public class SendRestfulServiceHandlerOpenemm extends SendRestfulServiceHandler{
    public SendRestfulServiceHandlerOpenemm(ConfigService configService, MailingPreviewService mailingPreviewService, RestfulUserActivityLogDao userActivityLogDao, MailingService mailingService, MailingDao mailingDao, RecipientService recipientService, MailinglistDao mailinglistDao, MaildropService maildropService, ClassicTemplateGenerator classicTemplateGenerator, SendActionbasedMailingService sendActionbasedMailingService, DatasourceDescriptionDao datasourceDescriptionDao, BindingEntryDao bindingEntryDao, RecipientFieldService recipientFieldService, MailingSendService mailingSendService, MailingStopService mailingStopService) {
        super(configService, mailingPreviewService, userActivityLogDao, mailingService, mailingDao, recipientService, mailinglistDao, maildropService, classicTemplateGenerator, sendActionbasedMailingService, datasourceDescriptionDao, bindingEntryDao, recipientFieldService, mailingSendService, mailingStopService);
    }
}
