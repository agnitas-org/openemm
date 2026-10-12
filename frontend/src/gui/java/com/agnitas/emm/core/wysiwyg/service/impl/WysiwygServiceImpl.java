/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.wysiwyg.service.impl;

import java.util.Collections;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.core.wysiwyg.service.WysiwygService;
import org.springframework.stereotype.Service;

@Service
public class WysiwygServiceImpl implements WysiwygService {

    private final MailingComponentsService mailingComponentsService;

    public WysiwygServiceImpl(MailingComponentsService mailingComponentsService) {
        this.mailingComponentsService = mailingComponentsService;
    }

    @Override
    public Map<String, String> getMailingImages(int mailingId, Admin admin) {
        return mailingComponentsService.getUrlsByNamesForEmmImages(admin, mailingId);
    }

    @Override
    public Map<String, String> getMediapoolImages(Admin admin) {
        return Collections.emptyMap();
    }

}
