/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

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

    protected final WysiwygService wysiwygService;
    private final AgnTagService agnTagService;

    public WysiwygController(AgnTagService agnTagService, WysiwygService wysiwygService) {
        this.agnTagService = agnTagService;
        this.wysiwygService = wysiwygService;
    }

    @RequestMapping("/dialogs/agn-tags.action")
    public ModelAndView showAgnTags(Admin admin) {
        return new ModelAndView("wysiwyg_agn_tags_modal", "tags", agnTagService.getSupportedAgnTags(admin));
    }

    @RequestMapping("/mailing/images.action")
    public ResponseEntity<Map<String, String>> getMailingImages(@RequestParam int mailingId, Admin admin) {
        return ResponseEntity.ok(wysiwygService.getMailingImages(mailingId, admin));
    }

}
