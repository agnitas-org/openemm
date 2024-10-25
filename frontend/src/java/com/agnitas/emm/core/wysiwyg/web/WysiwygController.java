/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wysiwyg.web;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.wysiwyg.service.WysiwygService;
import com.agnitas.service.AgnTagService;
import com.agnitas.web.mvc.XssCheckAware;
import com.agnitas.web.perm.annotations.PermissionMapping;
import net.sf.json.JSONObject;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

public class WysiwygController implements XssCheckAware {

    private final AgnTagService agnTagService;
    private final WysiwygService wysiwygService;
    private final ComMailingComponentsService mailingComponentsService;

    public WysiwygController(AgnTagService agnTagService, WysiwygService wysiwygService, ComMailingComponentsService mailingComponentsService) {
        this.agnTagService = agnTagService;
        this.wysiwygService = wysiwygService;
        this.mailingComponentsService = mailingComponentsService;
    }

    @RequestMapping("/dialogs/agn-tags.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public ModelAndView showAgnTags(Admin admin) {
        return new ModelAndView("wysiwyg_agn_tags_dialog", "tags", agnTagService.getSupportedAgnTags(admin));
    }

    @RequestMapping("/dialogs/agn-tagsRedesigned.action")
    @PermissionMapping("showAgnTags")
    public ModelAndView showAgnTagsRedesigned(Admin admin) {
        return new ModelAndView("wysiwyg_agn_tags_modal", "tags", agnTagService.getSupportedAgnTags(admin));
    }

    @RequestMapping("/images/names-urls.action")
    public @ResponseBody JSONObject getNamesUrlsJsonMap(final Admin admin,
                                                        @RequestParam(name = "mi", required = false) final int mailingId) {
        return wysiwygService.getImagesLinksWithDescriptionJson(admin, mailingId);
    }

    @RequestMapping("/image-browser.action")
    // TODO: EMMGUI-714: remove when old design will be removed
    public String imageBrowser(@RequestParam int mailingID, Admin admin, Model model) {
        int companyID = admin.getCompanyID();

        model.addAttribute("rdirDomain", admin.getCompany().getRdirDomain());
        model.addAttribute("companyId", companyID);
        model.addAttribute("mailingImages", mailingComponentsService.getImagesNames(mailingID, companyID));
        addExtendedAttrsForImgBrowser(companyID, model);
        return "wysiwyg_agn_tags_window";
    }

    // TODO: EMMGUI-714: remove when old design will be removed
    protected void addExtendedAttrsForImgBrowser(int companyId, Model model) {}

    @RequestMapping("/image-browserRedesigned.action")
    @PermissionMapping("imageBrowser")
    public String imageBrowserRedesigned(@RequestParam int mailingID, Admin admin, Model model) {
        addAttributesForImageBrowser(mailingID, admin, model);
        return "wysiwyg_image_browser_window";
    }

    protected void addAttributesForImageBrowser(int mailingId, Admin admin, Model model) {
        model.addAttribute("rdirDomain", admin.getCompany().getRdirDomain());
        model.addAttribute("companyId", admin.getCompanyID());
        model.addAttribute("mailingImages", mailingComponentsService.getImagesNames(mailingId, admin.getCompanyID()));
    }
}
