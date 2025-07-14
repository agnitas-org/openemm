// -*- scope: openemm -*-
package com.agnitas.emm.core.calendar.web;

import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.emm.core.calendar.service.CalendarCommentService;
import com.agnitas.emm.core.calendar.service.CalendarService;
import com.agnitas.mailing.autooptimization.service.OptimizationService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.web.perm.annotations.PermissionMapping;
import org.springframework.stereotype.Controller;

@Controller
@PermissionMapping("calendar")
public class CalendarControllerOpenemm extends CalendarController {


    public CalendarControllerOpenemm(AdminService adminService,
                                     CalendarService calendarService,
                                     OptimizationService optimizationService,
                                     CalendarCommentService calendarCommentService,
                                     UserActivityLogService userActivityLogService) {
        super(adminService, calendarService, optimizationService, calendarCommentService, userActivityLogService);
    }
}
