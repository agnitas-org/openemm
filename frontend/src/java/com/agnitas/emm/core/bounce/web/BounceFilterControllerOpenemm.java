package com.agnitas.emm.core.bounce.web;

import org.agnitas.service.UserActivityLogService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.convert.ConversionService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

import com.agnitas.emm.core.bounce.form.validation.BounceFilterSearchParams;
import com.agnitas.emm.core.bounce.service.BounceFilterService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import com.agnitas.emm.core.mailinglist.service.MailinglistApprovalService;
import com.agnitas.emm.core.userform.service.UserformService;
import com.agnitas.service.WebStorage;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@PermissionMapping("bounce.filter")
@RequestMapping("/administration/bounce")
@SessionAttributes(types = BounceFilterSearchParams.class)
public class BounceFilterControllerOpenemm extends BounceFilterController {

    public BounceFilterControllerOpenemm(@Qualifier("BounceFilterService") BounceFilterService bounceFilterService,
                                          ComMailingBaseService mailingService,
                                          MailinglistApprovalService mailinglistApprovalService,
                                          UserformService userFormService, ConversionService conversionService,
                                          WebStorage webStorage, UserActivityLogService userActivityLogService) {
        super(bounceFilterService, mailingService, mailinglistApprovalService, userFormService, conversionService, webStorage, userActivityLogService);
    }
}
