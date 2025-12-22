package com.agnitas.emm.core.support.web;

import com.agnitas.emm.core.JavaMailService;
import com.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/support")
public class SupportControllerOpenemm extends SupportController {

    public SupportControllerOpenemm(JavaMailService javaMailService, ConfigService configService, @Qualifier("formNotFoundEmailTemplate") String formNotFoundEmailTemplate, @Qualifier("formNotFoundUrlParameterTemplate") String formNotFoundUrlParameterTemplate) {
        super(javaMailService, configService, formNotFoundEmailTemplate, formNotFoundUrlParameterTemplate);
    }
}
