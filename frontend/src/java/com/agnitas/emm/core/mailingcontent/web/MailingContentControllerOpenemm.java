package com.agnitas.emm.core.mailingcontent.web;

import org.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailingcontent.validator.DynTagChainValidator;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingContentService;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/mailing/content")
@PermissionMapping("mailing.content")
public class MailingContentControllerOpenemm extends MailingContentController {

    public MailingContentControllerOpenemm(MailinglistApprovalService mailinglistApprovalService, MailingService mailingService, MaildropService maildropService,
                                           MailingContentService mailingContentService, ComTargetService targetService,
                                           UserActivityLogService userActivityLogService, ProfileFieldDao profileFieldDao, MailingPropertiesRules mailingPropertiesRules,
                                           ComMailingBaseService mailingBaseService, GridServiceWrapper gridServiceWrapper, AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory,
                                           AgnTagService agnTagService, PreviewImageService previewImageService, DynTagChainValidator dynTagChainValidator,
                                           ExtendedConversionService extendedConversionService) {

        super(mailinglistApprovalService, mailingService, maildropService, mailingContentService, targetService,
                userActivityLogService, profileFieldDao, mailingPropertiesRules, mailingBaseService, gridServiceWrapper, agnDynTagGroupResolverFactory,
                agnTagService, previewImageService, dynTagChainValidator, extendedConversionService);
    }
}
