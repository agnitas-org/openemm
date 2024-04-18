package com.agnitas.emm.core.import_profile.web;

import com.agnitas.dao.ProfileFieldDao;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.import_profile.component.ImportProfileChangesDetector;
import com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingChangesDetector;
import com.agnitas.emm.core.import_profile.component.ImportProfileColumnMappingsValidator;
import com.agnitas.emm.core.import_profile.component.ImportProfileFormValidator;
import com.agnitas.emm.core.import_profile.service.ImportProfileMappingsReadService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.service.ColumnInfoService;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.agnitas.emm.core.recipient.service.RecipientService;
import org.agnitas.service.ImportProfileService;
import org.agnitas.service.UserActivityLogService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/import-profile")
@PermissionMapping("import.profile")
public class ImportProfileControllerOpenemm extends ImportProfileController {

    public ImportProfileControllerOpenemm(ImportProfileService importProfileService, UserActivityLogService userActivityLogService, AdminService adminService,
                                          WebStorage webStorage, ColumnInfoService columnInfoService, MailinglistApprovalService mailinglistApprovalService,
                                          ExtendedConversionService conversionService, ImportProfileChangesDetector changesDetector,
                                          ImportProfileColumnMappingChangesDetector mappingsChangesDetector, ImportProfileFormValidator formValidator,
                                          RecipientService recipientService, ProfileFieldDao profileFieldDao, ImportProfileColumnMappingsValidator mappingsValidator,
                                          ImportProfileMappingsReadService mappingsReadService) {

        super(importProfileService, userActivityLogService, adminService, webStorage, columnInfoService, mailinglistApprovalService,
                conversionService, changesDetector, mappingsChangesDetector, formValidator, recipientService, profileFieldDao,
                mappingsValidator, mappingsReadService);
    }
}
