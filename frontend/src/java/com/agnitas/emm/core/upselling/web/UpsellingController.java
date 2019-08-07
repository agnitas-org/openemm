/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upselling.web;

import com.agnitas.web.perm.annotations.AlwaysAllowed;
import com.agnitas.emm.core.upselling.form.UpsellingForm;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@AlwaysAllowed
public class UpsellingController {
    private static final String GENERAL_VIEW = "general_upselling";

    private static final String[] CUSTOM_VIEWS = new String[]{
            "grid_template_upselling",
            "messenger_upselling",
            "notification_upselling"
    };

    @GetMapping("/upselling.action")
    public String view(UpsellingForm form, Model model) {
        String featureName = form.getFeatureNameKey();
        String activeSideMenu = StringUtils.defaultIfEmpty(form.getSidemenuActive(), featureName);

        model.addAttribute("featureNameKey", featureName);
        model.addAttribute("sidemenuActive", activeSideMenu);
        model.addAttribute("sidemenuSubActive", StringUtils.trimToEmpty(form.getSidemenuSubActive()));

        return getView(form);
    }

    private String getView(UpsellingForm form) {
        if (ArrayUtils.contains(CUSTOM_VIEWS, form.getPage())) {
            return form.getPage();
        }

        return GENERAL_VIEW;
    }
}
