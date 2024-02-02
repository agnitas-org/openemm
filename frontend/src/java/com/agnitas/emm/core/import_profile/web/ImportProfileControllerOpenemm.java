package com.agnitas.emm.core.import_profile.web;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.import_profile.component.ImportProfileChangesDetector;
import com.agnitas.emm.core.import_profile.component.ImportProfileFormValidator;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.service.ImportProfileService;
import org.agnitas.service.UserActivityLogService;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/import-profile")
@PermissionMapping("import.profile")
public class ImportProfileControllerOpenemm extends ImportProfileController {

    public ImportProfileControllerOpenemm(ImportProfileService importProfileService, UserActivityLogService userActivityLogService, AdminService adminService,
                                          WebStorage webStorage, ColumnInfoService columnInfoService, MailinglistApprovalService mailinglistApprovalService,
                                          ConversionService conversionService, ImportProfileChangesDetector changesDetector, ImportProfileFormValidator formValidator,
                                          RecipientService recipientService) {

        super(importProfileService, userActivityLogService, adminService, webStorage, columnInfoService, mailinglistApprovalService,
                conversionService, changesDetector, formValidator, recipientService);
    }
}
