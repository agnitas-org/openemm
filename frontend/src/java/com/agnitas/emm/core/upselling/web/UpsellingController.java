/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.upselling.web;

import com.agnitas.beans.Admin;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.AlwaysAllowed;
import org.apache.commons.lang3.ArrayUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AlwaysAllowed
public class UpsellingController implements XssCheckAware {

    private static final String[] CUSTOM_VIEWS = new String[]{
            "grid_template_upselling",
            "mailing_predelivery_upselling",
            "auto_export_upselling",
            "auto_import_upselling",
            "manage_tables_upselling",
            "mediapool_upselling",
            "clients_upselling"
    };

    @GetMapping("/upselling.action")
    public String view(@RequestParam String page, Admin admin, Model model) {
        model.addAttribute("adminLocale", admin.getLocale());
        if (ArrayUtils.contains(CUSTOM_VIEWS, page)) {
            return page;
        }
        return "common_upselling";
    }
}
