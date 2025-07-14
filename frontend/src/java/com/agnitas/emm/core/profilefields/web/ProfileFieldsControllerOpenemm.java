package com.agnitas.emm.core.profilefields.web;

import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.objectusage.service.ObjectUsageService;
import com.agnitas.emm.core.profilefields.form.ProfileFieldFormSearchParams;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.reporting.birt.external.dataset.ProfileFieldEvaluationDataSet;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/profiledb")
@PermissionMapping("profiledb")
@SessionAttributes(types = ProfileFieldFormSearchParams.class)
public class ProfileFieldsControllerOpenemm extends ProfileFieldsController {

    public ProfileFieldsControllerOpenemm(RecipientFieldService recipientFieldService, WebStorage webStorage, ConfigService configService,
                                          ProfileFieldValidationService validationService, UserActivityLogService userActivityLogService,
                                          ObjectUsageService objectUsageService, WorkflowService workflowService, TargetService targetService,
                                          MailinglistApprovalService mailinglistApprovalService, BirtStatisticsService birtStatisticsService,
                                          ProfileFieldEvaluationDataSet evaluationDataSet) {

        super(recipientFieldService, webStorage, configService, validationService, userActivityLogService, objectUsageService,
                workflowService, targetService, mailinglistApprovalService, birtStatisticsService, evaluationDataSet);
    }
}
