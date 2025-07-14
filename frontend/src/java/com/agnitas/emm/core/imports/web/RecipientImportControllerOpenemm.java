package com.agnitas.emm.core.imports.web;

import com.agnitas.dao.DatasourceDescriptionDao;
import com.agnitas.emm.core.action.service.EmmActionService;
import com.agnitas.emm.core.imports.service.RecipientImportService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.agnitas.dao.ImportRecipientsDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.service.ImportProfileService;
import com.agnitas.service.ProfileImportWorkerFactory;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.emm.core.imports.reporter.ProfileImportReporter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/recipient/import")
@PermissionMapping("recipient.import")
public class RecipientImportControllerOpenemm extends RecipientImportController {

    public RecipientImportControllerOpenemm(ImportProfileService importProfileService, MailinglistService mailinglistService, EmmActionService emmActionService, MailinglistApprovalService mailinglistApprovalService, RecipientService recipientService, DatasourceDescriptionDao datasourceDescriptionDao, ProfileImportWorkerFactory profileImportWorkerFactory, ConfigService configService, WebStorage webStorage, ImportRecipientsDao importRecipientsDao, ProfileImportReporter importReporter, UserActivityLogService userActivityLogService, RecipientImportService recipientImportService) {
        super(importProfileService, mailinglistService, emmActionService, mailinglistApprovalService, recipientService, datasourceDescriptionDao, profileImportWorkerFactory, configService, webStorage, importRecipientsDao, importReporter, userActivityLogService, recipientImportService);
    }
}
