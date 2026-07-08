package com.agnitas.emm.core.birtstatistics.mailing.web;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.archive.service.CampaignService;
import com.agnitas.emm.core.birtreport.service.BirtReportService;
import com.agnitas.emm.core.birtstatistics.mailing.mapper.MailingSummaryStatsPresenter;
import com.agnitas.emm.core.birtstatistics.mailing.mapper.MailingTopDomainsPresenter;
import com.agnitas.emm.core.birtstatistics.mailing.mapper.MailingBounceStatsPresenter;
import com.agnitas.emm.core.birtstatistics.mailing.service.MailingEndDeviceStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.ClickPerLinkMailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.ProgressStatisticsService;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.company.service.CompanyService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.trackablelinks.service.TrackableLinkService;
import com.agnitas.emm.core.workflow.service.WorkflowStatisticsService;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/statistics/mailing")
@RequiredPermission("stats.mailing")
public class MailingStatisticControllerOpenemm extends MailingBirtStatController {

	public MailingStatisticControllerOpenemm(
			MailingBaseService mailingBaseService,
			MailinglistApprovalService mailinglistApprovalService,
			WebStorage webStorage,
			ConversionService conversionService,
			UserActivityLogService userActivityLogService,
			AdminService adminService,
			GridServiceWrapper gridServiceWrapper,
			BirtStatisticsService birtStatisticsService,
			CompanyService companyService,
			TargetService targetService,
			BirtReportService birtReportService,
			OptimizationService optimizationService,
			MaildropService maildropService,
			CampaignService campaignService,
			ClickPerLinkMailingStatisticsService clickPerLinkStatisticsService,
			WorkflowStatisticsService workflowStatisticsService,
			MailingSummaryStatsPresenter mailingSummaryStatsPresenter,
			MailingBounceStatsPresenter bounceStatsPresenter,
			MailingTopDomainsPresenter mailingTopDomainsPresenter,
			MailingStatisticsService mailingStatisticsService,
			ConfigService configService,
			ProgressStatisticsService progressStatisticsService,
			MailingEndDeviceStatisticsService endDeviceStatisticsService,
			TrackableLinkService trackableLinkService
	) {
		super(mailingBaseService,
				mailinglistApprovalService,
				webStorage,
				conversionService,
				userActivityLogService,
				adminService,
				gridServiceWrapper,
				birtStatisticsService,
				companyService,
				targetService,
				birtReportService,
				optimizationService,
				maildropService,
				campaignService,
				clickPerLinkStatisticsService,
				workflowStatisticsService,
				mailingSummaryStatsPresenter,
				mailingTopDomainsPresenter,
				bounceStatsPresenter,
				mailingStatisticsService,
				configService,
				progressStatisticsService,
				endDeviceStatisticsService,
				trackableLinkService
		);
	}

}
