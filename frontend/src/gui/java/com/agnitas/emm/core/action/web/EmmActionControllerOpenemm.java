package com.agnitas.emm.core.action.web;

import com.agnitas.beans.factory.ActionOperationFactory;
import com.agnitas.emm.core.action.operations.ActionOperationParametersParser;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.action.service.impl.EmmActionValidationServiceImpl;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/action")
public class EmmActionControllerOpenemm extends EmmActionController {

    public EmmActionControllerOpenemm(EmmActionService emmActionService, MailingService mailingService, ConfigService configService, WorkflowService workflowService, UserActivityLogService userActivityLogService, ConversionService conversionService, ActionOperationParametersParser actionOperationParametersParser, EmmActionValidationServiceImpl validationService, ActionOperationFactory actionOperationFactory, MailinglistApprovalService mailinglistApprovalService, ColumnInfoService columnInfoService) {
        super(emmActionService, mailingService, configService, workflowService, userActivityLogService, conversionService, actionOperationParametersParser, validationService, actionOperationFactory, mailinglistApprovalService, columnInfoService);
    }
}
