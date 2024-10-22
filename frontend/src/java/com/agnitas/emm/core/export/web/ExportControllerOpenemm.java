package com.agnitas.emm.core.export.web;

import org.agnitas.service.UserActivityLogService;
import org.agnitas.web.RecipientExportReporter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.mailinglist.service.MailinglistService;
import com.agnitas.emm.core.target.service.ComTargetService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ExportPredefService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/export")
@PermissionMapping("export")
public class ExportControllerOpenemm extends ExportController {

    public ExportControllerOpenemm(ColumnInfoService columnInfoService, ComTargetService targetService, ExportPredefService exportService, MailinglistService mailinglistService, UserActivityLogService userActivityLogService, RecipientExportReporter recipientExportReporter, WebStorage webStorage) {
        super(columnInfoService, targetService, exportService, mailinglistService, userActivityLogService, recipientExportReporter, webStorage);
    }
}
