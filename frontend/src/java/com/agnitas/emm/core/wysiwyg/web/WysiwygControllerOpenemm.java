package com.agnitas.emm.core.wysiwyg.web;

import com.agnitas.emm.core.wysiwyg.service.WysiwygService;
import com.agnitas.service.AgnTagService;
import com.agnitas.web.perm.annotations.AlwaysAllowed;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/wysiwyg")
@AlwaysAllowed
public class WysiwygControllerOpenemm extends WysiwygController {
    public WysiwygControllerOpenemm(AgnTagService agnTagService, WysiwygService wysiwygService) {
        super(agnTagService, wysiwygService);
    }
}
