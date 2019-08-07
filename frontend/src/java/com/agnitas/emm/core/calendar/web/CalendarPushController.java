package com.agnitas.emm.core.calendar.web;


import java.util.Collections;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@PermissionMapping("calendar")
public class CalendarPushController {

    @GetMapping("/calendar/pushes.action")
    public @ResponseBody
    ResponseEntity<?> getNotifications() {
        return ResponseEntity.ok(Collections.emptyList());
    }
}
