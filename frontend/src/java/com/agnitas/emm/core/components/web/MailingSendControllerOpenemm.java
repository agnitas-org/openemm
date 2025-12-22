package com.agnitas.emm.core.components.web;

import com.agnitas.dao.DkimDao;
import com.agnitas.dao.MailingDao;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.dao.TargetDao;
import com.agnitas.emm.core.birtreport.service.BirtReportService;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.components.service.MailingBlockSizeService;
import com.agnitas.emm.core.components.service.MailingDependencyService;
import com.agnitas.emm.core.components.service.MailingRecipientsService;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryBlockingService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingSizeCalculationService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.mailinglist.dao.MailinglistDao;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.serverprio.server.ServerPrioService;
import com.agnitas.emm.core.target.service.TargetCopyService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.emm.premium.service.PremiumFeaturesService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.emm.core.auto_import.service.AutoImportService;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailing/send")
public class MailingSendControllerOpenemm extends MailingSendController {

    public MailingSendControllerOpenemm(MailingRecipientsService mailingRecipientsService, MailingDao mailingDao, MailingBaseService mailingBaseService,
                                        GridServiceWrapper gridService, TargetService targetService, ConfigService configService, MaildropService maildropService,
                                        MailingDeliveryStatService deliveryStatService, TargetDao targetDao, MailingService mailingService,
                                        MailingSizeCalculationService mailingSizeCalculationService, MailingDependencyService mailingDependencyService, WebStorage webStorage,
                                        UserActivityLogService userActivityLogService, MailingBlockSizeService blockSizeService, MailingStopService mailingStopService,
                                        MailinglistDao mailinglistDao, @Autowired(required = false) AutoImportService autoImportService, DkimDao dkimDao, MailingDeliveryBlockingService mailingDeliveryBlockingService,
                                        MailingStatisticsDao mailingStatisticsDao, BounceFilterService bounceFilterService, MailingSendService mailingSendService,
                                        ConversionService conversionService, TargetCopyService targetCopyService, PremiumFeaturesService premiumFeaturesService,
                                        ServerPrioService serverPrioService, MailinglistApprovalService mailinglistApprovalService, BirtReportService birtReportService, WorkflowService workflowService) {

        super(mailingRecipientsService, mailingDao, mailingBaseService, gridService, targetService, configService, maildropService,
                deliveryStatService, targetDao, mailingService, mailingSizeCalculationService, mailingDependencyService, webStorage,
                userActivityLogService, blockSizeService, mailingStopService, mailinglistDao, autoImportService, dkimDao,
                mailingDeliveryBlockingService, mailingStatisticsDao, bounceFilterService, mailingSendService, conversionService,
                targetCopyService, premiumFeaturesService, serverPrioService, mailinglistApprovalService, birtReportService, workflowService);
    }
}
