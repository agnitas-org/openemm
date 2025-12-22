package com.agnitas.emm.core.components.web;

import com.agnitas.emm.core.components.form.MailingImagesFormSearchParams;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/mailing/{mailingId:\\d+}/images")
@SessionAttributes(types = MailingImagesFormSearchParams.class)
public class MailingImagesControllerOpenemm extends MailingImagesController {

    public MailingImagesControllerOpenemm(MailingBaseService mailingBaseService, PreviewImageService previewImageService,
                                          MaildropService maildropService, UserActivityLogService userActivityLogService,
                                          MailingComponentsService mailingComponentsService,
                                          MailinglistApprovalService mailinglistApprovalService, ConfigService configService, WebStorage webStorage) {
        super(mailingBaseService, previewImageService, maildropService, userActivityLogService,
                mailingComponentsService, configService, mailinglistApprovalService, webStorage);
    }
}
