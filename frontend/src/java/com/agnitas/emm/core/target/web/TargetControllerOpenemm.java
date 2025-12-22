package com.agnitas.emm.core.target.web;

import com.agnitas.emm.core.birtreport.dao.BirtReportDao;
import com.agnitas.emm.core.target.form.TargetListFormSearchParams;
import com.agnitas.emm.core.target.service.TargetService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.service.WebStorage;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.SessionAttributes;

@Controller
@RequestMapping("/target")
@SessionAttributes(types = TargetListFormSearchParams.class)
public class TargetControllerOpenemm extends TargetController {

    public TargetControllerOpenemm(TargetService targetService, WebStorage webStorage, UserActivityLogService userActivityLogService, BirtReportDao birtReportDao) {
        super(targetService, webStorage, userActivityLogService, birtReportDao);
    }
}
