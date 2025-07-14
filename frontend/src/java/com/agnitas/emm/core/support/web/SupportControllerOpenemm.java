package com.agnitas.emm.core.support.web;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.beans.factory.annotation.Qualifier;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/support")
@PermissionMapping("support")
public class SupportControllerOpenemm extends SupportController {

    public SupportControllerOpenemm(JavaMailService javaMailService, ConfigService configService, @Qualifier("formNotFoundEmailTemplate") String formNotFoundEmailTemplate, @Qualifier("formNotFoundUrlParameterTemplate") String formNotFoundUrlParameterTemplate) {
        super(javaMailService, configService, formNotFoundEmailTemplate, formNotFoundUrlParameterTemplate);
    }
}
