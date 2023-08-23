package com.agnitas.emm.core.imports.web;

import com.agnitas.emm.core.imports.service.MailingImportService;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/import")
@PermissionMapping("import")
public class ImportControllerOpenemm extends ImportController {

    public ImportControllerOpenemm(MailingImportService mailingImportService, UserActivityLogService userActivityLogService) {
        super(mailingImportService, userActivityLogService);
    }
}
