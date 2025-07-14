package com.agnitas.emm.core.import_profile.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.recipient.service.RecipientService;
import com.agnitas.service.ImportProfileService;
import com.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.import_profile.component.ImportProfileChangesDetector;
import com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingChangesDetector;
import com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingsValidator;
import com.agnitas.emm.core.import_profile.component.ImportProfileFormValidator;
import com.agnitas.emm.core.import_profile.service.ImportProfileMappingsReadService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/import-profile")
@PermissionMapping("import.profile")
public class ImportProfileControllerOpenemm extends ImportProfileController {

    public ImportProfileControllerOpenemm(ImportProfileService importProfileService, UserActivityLogService userActivityLogService, AdminService adminService,
                                          WebStorage webStorage, RecipientFieldService recipientFieldService, MailinglistApprovalService mailinglistApprovalService,
                                          ExtendedConversionService conversionService, ImportProfileChangesDetector changesDetector,
                                          ImportProfileColumnMappingChangesDetector mappingsChangesDetector, ImportProfileFormValidator formValidator,
                                          RecipientService recipientService, ImportProfileColumnMappingsValidator mappingsValidator,
                                          ImportProfileMappingsReadService mappingsReadService, ConfigService configService) {

        super(importProfileService, userActivityLogService, adminService, webStorage, recipientFieldService, mailinglistApprovalService,
                conversionService, changesDetector, mappingsChangesDetector, formValidator, recipientService,
                mappingsValidator, mappingsReadService, configService);
    }
}
