package com.agnitas.emm.core.profilefields.web;

import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.objectusage.service.ObjectUsageService;
import com.agnitas.emm.core.profilefields.form.ProfileFieldFormSearchParams;
import com.agnitas.emm.core.profilefields.service.ProfileFieldValidationService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.reporting.birt.external.dataset.ProfileFieldEvaluationDataSet;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.RequiredPermission;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/profiledb")
@RequiredPermission("profileField.show")
@SessionAttributes(types = ProfileFieldFormSearchParams.class)
public class ProfileFieldsControllerOpenemm extends ProfileFieldsController {

    public ProfileFieldsControllerOpenemm(RecipientFieldService recipientFieldService, WebStorage webStorage, ConfigService configService, ProfileFieldValidationService validationService, UserActivityLogService userActivityLogService, ObjectUsageService objectUsageService, WorkflowService workflowService, TargetService targetService, MailinglistApprovalService mailinglistApprovalService, ProfileFieldEvaluationDataSet evaluationDataSet) {
        super(recipientFieldService, webStorage, configService, validationService, userActivityLogService, objectUsageService, workflowService, targetService, mailinglistApprovalService, evaluationDataSet);
    }
}
