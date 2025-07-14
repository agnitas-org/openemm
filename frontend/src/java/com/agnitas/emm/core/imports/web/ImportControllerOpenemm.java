package com.agnitas.emm.core.imports.web;

import com.agnitas.emm.core.imports.service.MailingImportService;
import com.agnitas.emm.core.mailing.service.MailingService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/import")
@PermissionMapping("import")
public class ImportControllerOpenemm extends ImportController {

    public ImportControllerOpenemm(MailingImportService mailingImportService, UserActivityLogService userActivityLogService, MailingService mailingService) {
        super(mailingImportService, userActivityLogService, mailingService);
    }
}
