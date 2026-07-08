package com.agnitas.emm.core.mailingcontent.web;

import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingContentService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.preview.PreviewImageService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailing/content")
public class MailingContentControllerOpenemm extends MailingContentController {


    public MailingContentControllerOpenemm(MailinglistApprovalService mailinglistApprovalService, MailingService mailingService, MaildropService maildropService,
                                           MailingContentService mailingContentService, TargetService targetService, UserActivityLogService userActivityLogService,
                                           ProfileFieldDao profileFieldDao, MailingPropertiesRules mailingPropertiesRules, MailingBaseService mailingBaseService,
                                           GridServiceWrapper gridServiceWrapper, PreviewImageService previewImageService, MediaTypesService mediaTypesService) {

        super(mailinglistApprovalService, mailingService, maildropService, mailingContentService, targetService, userActivityLogService,
                profileFieldDao, mailingPropertiesRules, mailingBaseService, gridServiceWrapper,
                previewImageService, mediaTypesService);
    }
}
