package com.agnitas.emm.core.components.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.util.SFtpHelperFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/mailing/{mailingId:\\d+}/images")
@PermissionMapping("mailing.images")
public class MailingImagesControllerOpenemm extends MailingImagesController {
    
    public MailingImagesControllerOpenemm(ComMailingBaseService mailingBaseService, PreviewImageService previewImageService,
                                           MaildropService maildropService, UserActivityLogService userActivityLogService,
                                           ComMailingComponentsService mailingComponentsService,
                                           MailinglistApprovalService mailinglistApprovalService,
                                           SFtpHelperFactory sFtpHelperFactory, ConfigService configService) {
        super(mailingBaseService, previewImageService, maildropService, userActivityLogService,
                mailingComponentsService, configService, mailinglistApprovalService, sFtpHelperFactory);
    }
}
