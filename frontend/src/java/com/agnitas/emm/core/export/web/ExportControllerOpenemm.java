package com.agnitas.emm.core.export.web;

import com.agnitas.emm.core.export.reporter.RecipientExportReporter;
import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ExportPredefService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/export")
public class ExportControllerOpenemm extends ExportController {

    public ExportControllerOpenemm(ColumnInfoService columnInfoService, TargetService targetService, ExportPredefService exportService, MailinglistService mailinglistService, UserActivityLogService userActivityLogService, RecipientExportReporter recipientExportReporter, WebStorage webStorage) {
        super(columnInfoService, targetService, exportService, mailinglistService, userActivityLogService, recipientExportReporter, webStorage);
    }
}
