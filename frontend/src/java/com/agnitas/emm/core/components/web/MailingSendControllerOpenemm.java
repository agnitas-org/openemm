package com.agnitas.emm.core.components.web;

import com.agnitas.dao.ComDkimDao;
import com.agnitas.dao.ComMailingDao;
import com.agnitas.dao.ComTargetDao;
import com.agnitas.dao.MailingStatisticsDao;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.components.service.MailingBlockSizeService;
import com.agnitas.emm.core.components.service.MailingDependencyService;
import com.agnitas.emm.core.components.service.MailingRecipientsService;
import com.agnitas.emm.core.components.service.MailingSendService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailing.service.ComMailingDeliveryStatService;
import com.agnitas.emm.core.mailing.service.MailingDeliveryBlockingService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingSizeCalculationService;
import com.agnitas.emm.core.mailing.service.MailingStopService;
import com.agnitas.emm.core.serverprio.server.ServerPrioService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.TargetCopyService;
import com.agnitas.emm.premium.service.PremiumFeaturesService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.dao.MailinglistDao;
import org.agnitas.emm.core.autoimport.service.AutoImportService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailing/send")
@PermissionMapping("mailing.send")
public class MailingSendControllerOpenemm extends MailingSendController {

    public MailingSendControllerOpenemm(MailingRecipientsService mailingRecipientsService, ComMailingDao mailingDao, ComMailingBaseService mailingBaseService,
            GridServiceWrapper gridService, ComTargetService targetService, ConfigService configService, MaildropService maildropService,
            ComMailingDeliveryStatService deliveryStatService, ComTargetDao targetDao, MailingService mailingService,
            MailingSizeCalculationService mailingSizeCalculationService, MailingDependencyService mailingDependencyService, WebStorage webStorage,
            UserActivityLogService userActivityLogService, MailingBlockSizeService blockSizeService, MailingStopService mailingStopService,
            MailinglistDao mailinglistDao, @Autowired(required = false) AutoImportService autoImportService, ComDkimDao dkimDao, MailingDeliveryBlockingService mailingDeliveryBlockingService,
            MailingStatisticsDao mailingStatisticsDao, BounceFilterService bounceFilterService, MailingSendService mailingSendService, ConversionService conversionService,
            TargetCopyService targetCopyService, PremiumFeaturesService premiumFeaturesService,
            final ServerPrioService serverPrioService) {

        super(mailingRecipientsService, mailingDao, mailingBaseService, gridService, targetService, configService, maildropService, deliveryStatService, targetDao, mailingService, mailingSizeCalculationService, mailingDependencyService, webStorage, userActivityLogService, blockSizeService, mailingStopService, mailinglistDao, autoImportService, dkimDao, mailingDeliveryBlockingService, mailingStatisticsDao, bounceFilterService, mailingSendService, conversionService, targetCopyService, premiumFeaturesService, serverPrioService);
    }
}
