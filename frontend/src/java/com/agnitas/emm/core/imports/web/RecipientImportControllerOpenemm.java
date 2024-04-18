package com.agnitas.emm.core.imports.web;

import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.service.ImportProfileService;
import org.agnitas.service.ProfileImportWorkerFactory;
import org.agnitas.service.UserActivityLogService;
import org.agnitas.web.ProfileImportReporter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.dao.ComRecipientDao;
import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/recipient/import")
@PermissionMapping("recipient.import")
public class RecipientImportControllerOpenemm extends RecipientImportController {

    public RecipientImportControllerOpenemm(ImportProfileService importProfileService, MailinglistService mailinglistService, EmmActionService emmActionService,
                                            MailinglistApprovalService mailinglistApprovalService, RecipientService recipientService, DatasourceDescriptionDao datasourceDescriptionDao,
                                            ProfileImportWorkerFactory profileImportWorkerFactory, ConfigService configService, WebStorage webStorage, ImportRecipientsDao importRecipientsDao,
                                            ProfileImportReporter importReporter, UserActivityLogService userActivityLogService, ComRecipientDao recipientDao) {

        super(importProfileService, mailinglistService, emmActionService, mailinglistApprovalService,
                recipientService, datasourceDescriptionDao, profileImportWorkerFactory, configService, webStorage, importRecipientsDao, importReporter,
                userActivityLogService, recipientDao);
    }
}
