package com.agnitas.emm.core.action.web;

import com.agnitas.emm.core.action.operations.ActionOperationParametersParser;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.action.service.impl.EmmActionValidationServiceImpl;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.beans.factory.ActionOperationFactory;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.service.UserActivityLogService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/action")
@PermissionMapping("action")
public class EmmActionControllerOpenemm extends EmmActionController {

    public EmmActionControllerOpenemm(WebStorage webStorage, EmmActionService emmActionService, MailingService mailingService,
                                      ConfigService configService, ComWorkflowService workflowService, UserActivityLogService userActivityLogService,
                                      ConversionService conversionService, ActionOperationParametersParser actionOperationParametersParser,
                                      EmmActionValidationServiceImpl validationService, ActionOperationFactory actionOperationFactory,
                                      UserformService userFormService, MailinglistApprovalService mailinglistApprovalService,
                                      ColumnInfoService columnInfoService) {
        super(webStorage, emmActionService, mailingService, configService, workflowService, userActivityLogService, conversionService,
                actionOperationParametersParser, validationService, actionOperationFactory, userFormService, mailinglistApprovalService,
                columnInfoService);
    }
}
