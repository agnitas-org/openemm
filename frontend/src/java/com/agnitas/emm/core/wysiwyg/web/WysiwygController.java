/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wysiwyg.web;

import com.agnitas.web.mvc.XssCheckAware;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.wysiwyg.service.WysiwygService;
import com.agnitas.service.AgnTagService;

import net.sf.json.JSONObject;

@Controller
@RequestMapping("/wysiwyg")
public class WysiwygController implements XssCheckAware {

    private final AgnTagService agnTagService;
    private final WysiwygService wysiwygService;

    public WysiwygController(AgnTagService agnTagService, WysiwygService wysiwygService) {
        this.agnTagService = agnTagService;
        this.wysiwygService = wysiwygService;
    }

    @RequestMapping("/dialogs/agn-tags.action")
    public ModelAndView showAgnTags(Admin admin) {
        return new ModelAndView("wysiwyg_agn_tags_dialog", "tags", agnTagService.getSupportedAgnTags(admin));
    }

    @RequestMapping("/images/names-urls.action")
    public @ResponseBody JSONObject getNamesUrlsJsonMap(final Admin admin,
                                                        @RequestParam(name = "mi", required = false) final int mailingId) {
        return wysiwygService.getImagesLinksWithDescriptionJson(admin, mailingId);
    }

    @RequestMapping("/image-browser.action")
    public String imageBrowser(Admin admin, Model model) {
        model.addAttribute("rdirDomain", admin.getCompany().getRdirDomain());
        model.addAttribute("companyId", admin.getCompanyID());
        return "wysiwyg_agn_tags_window";
    }
}
