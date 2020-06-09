/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wysiwyg.web;

import com.agnitas.beans.ComAdmin;
import com.agnitas.service.AgnTagService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/wysiwyg")
public class WysiwygController {
    private AgnTagService agnTagService;

    public WysiwygController(AgnTagService agnTagService) {
        this.agnTagService = agnTagService;
    }

    @RequestMapping("/dialogs/agn-tags.action")
    public ModelAndView showAgnTags(ComAdmin admin) {
        return new ModelAndView("wysiwyg_agn_tags_dialog", "tags", agnTagService.getSupportedAgnTags(admin));
    }
}
