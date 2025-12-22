// -*- scope: openemm -*-
package com.agnitas.emm.core.calendar.web;

import com.agnitas.emm.core.calendar.service.CalendarCommentService;
import com.agnitas.emm.core.calendar.service.CalendarService;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.web.perm.annotations.RequiredPermission;
import org.springframework.stereotype.Controller;

@Controller
@RequiredPermission("calendar.show")
public class CalendarControllerOpenemm extends CalendarController {

    public CalendarControllerOpenemm(CalendarService calendarService, CalendarCommentService calendarCommentService, UserActivityLogService userActivityLogService) {
        super(calendarService, calendarCommentService, userActivityLogService);
    }
}
