package com.agnitas.emm.core.recipient.web;

import org.agnitas.emm.core.blacklist.service.BlacklistService;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.service.ColumnInfoService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.service.WebStorage;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.delivery.service.DeliveryService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.recipient.forms.RecipientsFormSearchParams;
import com.agnitas.emm.core.recipient.service.RecipientLogService;
import com.agnitas.emm.core.target.eql.EqlValidatorService;
import com.agnitas.emm.core.target.eql.emm.querybuilder.EqlToQueryBuilderConverter;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderFilterListBuilder;
import com.agnitas.emm.core.target.eql.emm.querybuilder.QueryBuilderToEqlConverter;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/recipient")
@PermissionMapping("recipient")
@SessionAttributes(types = RecipientsFormSearchParams.class)
public class RecipientControllerOpenemm extends RecipientController {

	public RecipientControllerOpenemm(RecipientService recipientService,
									RecipientLogService recipientLogService,
									MailinglistApprovalService mailinglistApprovalService,
									ComTargetService targetService,
									UserActivityLogService userActivityLogService,
									DeliveryService deliveryService,
									ComMailingBaseService mailingBaseService,
									WebStorage webStorage,
									ConversionService conversionService,
									QueryBuilderFilterListBuilder filterListBuilder,
									ColumnInfoService columnInfoService,
									ConfigService configService,
									BlacklistService blacklistService,
									EqlToQueryBuilderConverter eqlToQueryBuilderConverter,
									QueryBuilderToEqlConverter queryBuilderToEqlConverter,
									ComCompanyDao companyDao,
									EqlValidatorService eqlValidatorService) {
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
				eqlValidatorService);
	}
}
