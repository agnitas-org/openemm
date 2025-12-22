/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wysiwyg.web;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.wysiwyg.service.WysiwygService;
import com.agnitas.service.AgnTagService;
import com.agnitas.web.mvc.XssCheckAware;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

public class WysiwygController implements XssCheckAware {

    private final AgnTagService agnTagService;
    private final WysiwygService wysiwygService;

    public WysiwygController(AgnTagService agnTagService, WysiwygService wysiwygService) {
        this.agnTagService = agnTagService;
        this.wysiwygService = wysiwygService;
    }

    @RequestMapping("/dialogs/agn-tags.action")
    public ModelAndView showAgnTags(Admin admin) {
        return new ModelAndView("wysiwyg_agn_tags_modal", "tags", agnTagService.getSupportedAgnTags(admin));
    }

    @RequestMapping("/images/names-urls.action")
    public ResponseEntity<Map<String, Object>> getNamesUrlsJsonMap(Admin admin, @RequestParam(name = "mi", required = false) int mailingId) {
        return ResponseEntity.ok(wysiwygService.getImagesLinksWithDescriptionJson(admin, mailingId).toMap());
    }

}
