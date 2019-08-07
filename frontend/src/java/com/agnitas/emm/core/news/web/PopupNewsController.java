package com.agnitas.emm.core.news.web;


import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.agnitas.web.perm.annotations.PermissionMapping;

@Controller
@RequestMapping("/administration/popupnews")
@PermissionMapping("popup.news")
public class PopupNewsController {

    @PermissionMapping("get.news.counters")
    @GetMapping("/getNewsCounters.action")
    public @ResponseBody Object getNewsCounters() {
    	return "";
    }
}
