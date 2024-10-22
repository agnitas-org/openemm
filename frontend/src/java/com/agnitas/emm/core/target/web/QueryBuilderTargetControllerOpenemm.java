package com.agnitas.emm.core.target.web;

import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.target.eql.EqlFacade;
import com.agnitas.emm.core.target.form.validator.TargetEditFormValidator;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.emm.core.target.service.TargetCopyService;
import com.agnitas.emm.core.target.web.util.EditorContentSynchronizer;
import com.agnitas.service.GridServiceWrapper;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.target.TargetFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/target")
@PermissionMapping("target")
public class QueryBuilderTargetControllerOpenemm extends QueryBuilderTargetController {

    public QueryBuilderTargetControllerOpenemm(MailinglistApprovalService mailinglistApprovalService, ComTargetService targetService, RecipientService recipientService,
                                               EditorContentSynchronizer editorContentSynchronizer, EqlFacade eqlFacade, TargetCopyService targetCopyService, TargetFactory targetFactory,
                                               UserActivityLogService userActivityLogService, BirtStatisticsService birtStatisticsService, WebStorage webStorage, GridServiceWrapper gridService,
                                               TargetEditFormValidator editFormValidator, ConfigService configService) {
        super(mailinglistApprovalService, targetService, recipientService, editorContentSynchronizer, eqlFacade, targetCopyService, targetFactory,
                userActivityLogService, birtStatisticsService, webStorage, gridService, editFormValidator, configService);
    }

}
