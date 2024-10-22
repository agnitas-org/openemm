package com.agnitas.emm.core.birtstatistics.recipient.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mediatypes.service.MediaTypesService;
import com.agnitas.emm.core.report.services.RecipientReportService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/statistics/recipient")
@PermissionMapping("recipient.stats")
public class RecipientStatControllerOpenemm extends RecipientStatController {

    public RecipientStatControllerOpenemm(BirtStatisticsService birtStatisticsService, ComTargetService targetService,
                                           RecipientReportService recipientReportService,
                                           MediaTypesService mediaTypesService, ConversionService conversionService,
                                           MailinglistApprovalService mailinglistApprovalService,
                                           UserActivityLogService userActivityLogService, ConfigService configService) {
        super(birtStatisticsService, targetService, recipientReportService, mediaTypesService, conversionService,
                mailinglistApprovalService, userActivityLogService, configService);
    }
}
