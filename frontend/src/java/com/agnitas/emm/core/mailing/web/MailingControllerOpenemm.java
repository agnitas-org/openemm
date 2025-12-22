package com.agnitas.emm.core.mailing.web;

import com.agnitas.beans.factory.MailingFactory;
import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.maildrop.service.MaildropService;
import com.agnitas.emm.core.mailing.bean.impl.MailingValidator;
import com.agnitas.emm.core.mailing.forms.validation.MailingSettingsFormValidator;
import com.agnitas.emm.core.mailing.service.CopyMailingService;
import com.agnitas.emm.core.mailing.service.MailingBaseService;
import com.agnitas.emm.core.mailing.service.MailingParameterService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailing.service.MailingSettingsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.MailingExporter;
import com.agnitas.service.MailingLightService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/mailing")
public class MailingControllerOpenemm extends MailingController {

	public MailingControllerOpenemm(ExtendedConversionService conversionService, MailingLightService mailingLightService, MailinglistService mailinglistService, TargetService targetService, MailingService mailingService, ConfigService configService, AdminService adminService, UserActivityLogService userActivityLogService, JavaMailService javaMailService, MailingBaseService mailingBaseService, MailingParameterService mailingParameterService, WorkflowService workflowService, MaildropService maildropService, CopyMailingService copyMailingService, MailingSettingsFormValidator mailingSettingsFormValidator, MailingFactory mailingFactory, MailinglistApprovalService mailinglistApprovalService, MailingExporter mailingExporter, GridServiceWrapper gridService, WebStorage webStorage, MailingSettingsService mailingSettingsService, MailingValidator mailingValidator, ApplicationContext applicationContext) {
		super(conversionService, mailingLightService, mailinglistService, targetService, mailingService, configService, adminService, userActivityLogService, javaMailService, mailingBaseService, mailingParameterService, workflowService, maildropService, copyMailingService, mailingSettingsFormValidator, mailingFactory, mailinglistApprovalService, mailingExporter, gridService, webStorage, mailingSettingsService, mailingValidator, applicationContext);
	}
}
