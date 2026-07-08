package com.agnitas.emm.core.preview.web;

import com.agnitas.dao.MailingComponentDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.RecipientDao;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.preview.service.MailingWebPreviewService;
import com.agnitas.preview.TAGCheckFactory;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.PdfService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.RequiredPermission;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailing/preview")
@RequiredPermission("mailing.show")
public class MailingPreviewControllerOpenemm extends MailingPreviewController {


    public MailingPreviewControllerOpenemm(MailingDao mailingDao, MailinglistDao mailinglistDao, RecipientDao recipientDao,
                                           MailingService mailingService, MailingWebPreviewService previewService, GridServiceWrapper gridService,
                                           MailingComponentDao mailingComponentDao, TAGCheckFactory tagCheckFactory, MailingBaseService mailingBaseService,
                                           ConfigService configService, PdfService pdfService, MaildropService maildropService,
                                           MailinglistApprovalService mailinglistApprovalService, WebStorage webStorage) {
        super(mailingDao, mailinglistDao, recipientDao, mailingService, previewService, gridService, mailingComponentDao, tagCheckFactory,
                mailingBaseService, configService, pdfService, maildropService, mailinglistApprovalService, webStorage);
    }
}
