package com.agnitas.emm.core.mailing.web;

import org.agnitas.beans.factory.MailingFactory;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.mailing.service.CopyMailingService;
import org.agnitas.preview.TAGCheckFactory;
import org.agnitas.service.MailingExporter;
import org.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.dao.DynamicTagDao;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.forms.validation.MailingSettingsFormValidator;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.ComMailingParameterService;
import com.agnitas.emm.core.mailing.service.MailingPropertiesRules;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.service.AgnDynTagGroupResolverFactory;
import com.agnitas.service.AgnTagService;
import com.agnitas.service.ComMailingLightService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.WebStorage;
import com.agnitas.util.preview.PreviewImageService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/mailing")
@PermissionMapping("mailing")
public class MailingControllerOpenemm extends MailingController {

    public MailingControllerOpenemm(ExtendedConversionService conversionService,
                                    ComMailingLightService mailingLightService,
                                    MailinglistService mailinglistService, ComTargetService targetService,
                                    MailingService mailingService, ConfigService configService,
                                    AdminService adminService, UserActivityLogService userActivityLogService,
                                    JavaMailService javaMailService, ComMailingBaseService mailingBaseService,
                                    DynamicTagDao dynamicTagDao, ComMailingParameterService mailingParameterService,
                                    ComWorkflowService workflowService, LinkService linkService,
                                    MaildropService maildropService, CopyMailingService copyMailingService,
                                    AgnTagService agnTagService,
                                    AgnDynTagGroupResolverFactory agnDynTagGroupResolverFactory,
                                    MailingSettingsFormValidator mailingSettingsFormValidator,
                                    TAGCheckFactory tagCheckFactory, MailingFactory mailingFactory,
                                    MailinglistApprovalService mailinglistApprovalService,
                                    PreviewImageService previewImageService, MailingExporter mailingExporter,
                                    GridServiceWrapper gridService, MailingPropertiesRules mailingPropertiesRules,
                                    WebStorage webStorage) {
        super(conversionService, mailingLightService, mailinglistService, targetService, mailingService, configService,
                adminService, userActivityLogService, javaMailService, mailingBaseService, dynamicTagDao,
                mailingParameterService, workflowService, linkService, maildropService, copyMailingService,
                agnTagService, agnDynTagGroupResolverFactory, mailingSettingsFormValidator, tagCheckFactory,
                mailingFactory, mailinglistApprovalService, previewImageService, mailingExporter, gridService,
                mailingPropertiesRules, webStorage);
    }
}
