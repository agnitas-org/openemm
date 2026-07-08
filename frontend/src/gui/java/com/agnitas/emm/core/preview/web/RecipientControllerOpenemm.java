package com.agnitas.emm.core.recipient.web;

import com.agnitas.dao.CompanyDao;
import com.agnitas.emm.core.blacklist.service.BlacklistService;
import com.agnitas.emm.core.delivery.service.DeliveryService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.recipient.forms.RecipientsFormSearchParams;
import com.agnitas.emm.core.recipient.service.RecipientLogService;
import com.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.emm.core.recipient.service.SubscriberLimitCheck;
import com.agnitas.emm.core.target.eql.EqlValidatorService;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderFilterListBuilder;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConverter;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.DataSourceService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/recipient")
@SessionAttributes(types = RecipientsFormSearchParams.class)
public class RecipientControllerOpenemm extends RecipientController {

	public RecipientControllerOpenemm(RecipientService recipientService,
									  RecipientLogService recipientLogService,
									  MailinglistApprovalService mailinglistApprovalService,
									  TargetService targetService,
									  UserActivityLogService userActivityLogService,
									  DeliveryService deliveryService,
									  MailingBaseService mailingBaseService,
									  WebStorage webStorage,
									  ConversionService conversionService,
									  QueryBuilderFilterListBuilder filterListBuilder,
									  ColumnInfoService columnInfoService,
									  ConfigService configService,
									  BlacklistService blacklistService,
									  EqlToQueryBuilderConverter eqlToQueryBuilderConverter,
									  QueryBuilderToEqlConverter queryBuilderToEqlConverter,
									  CompanyDao companyDao,
									  EqlValidatorService eqlValidatorService,
									  SubscriberLimitCheck subscriberLimitCheck,
									  DataSourceService dataSourceService) {
		super(recipientService,
				recipientLogService,
				mailinglistApprovalService,
				targetService,
				userActivityLogService,
				deliveryService,
				mailingBaseService,
				webStorage,
				conversionService,
				filterListBuilder,
				columnInfoService,
				configService,
				blacklistService,
				eqlToQueryBuilderConverter,
				queryBuilderToEqlConverter,
				companyDao,
				eqlValidatorService,
				subscriberLimitCheck,
				dataSourceService);
	}
}
