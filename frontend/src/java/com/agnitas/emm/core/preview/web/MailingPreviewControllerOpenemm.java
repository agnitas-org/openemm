package com.agnitas.emm.core.preview.web;

import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.dao.MailingComponentDao;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.preview.TAGCheckFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.dao.MailingDao;
import com.agnitas.dao.ComRecipientDao;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.preview.service.MailingWebPreviewService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.PdfService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/mailing/preview")
@PermissionMapping("mailing.preview")
public class MailingPreviewControllerOpenemm extends MailingPreviewController {


    public MailingPreviewControllerOpenemm(MailingDao mailingDao, MailinglistDao mailinglistDao, ComRecipientDao recipientDao,
                                           MailingService mailingService, MailingWebPreviewService previewService, GridServiceWrapper gridService,
                                           MailingComponentDao mailingComponentDao, TAGCheckFactory tagCheckFactory, ComMailingBaseService mailingBaseService,
                                           ConfigService configService, PdfService pdfService, MaildropService maildropService,
                                           MailinglistApprovalService mailinglistApprovalService) {
        super(mailingDao, mailinglistDao, recipientDao, mailingService, previewService, gridService, mailingComponentDao, tagCheckFactory,
                mailingBaseService, configService, pdfService, maildropService, mailinglistApprovalService);
    }
}
