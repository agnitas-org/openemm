/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wysiwyg.service.impl;

import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.components.service.MailingComponentsService;
import com.agnitas.emm.core.wysiwyg.service.WysiwygService;

import org.json.JSONObject;

public class WysiwygServiceImpl implements WysiwygService {

    private MailingComponentsService mailingComponentsService;

    @Override
    public JSONObject getImagesLinksWithDescriptionJson(Admin admin, int mailingId) {
        final JSONObject imagesJson = new JSONObject();

        if(mailingId <= 0) {
            return imagesJson;
        }

        for (Map.Entry<String, String> imageEntry : mailingComponentsService.getUrlsByNamesForEmmImages(admin, mailingId).entrySet()) {
            imagesJson.put(imageEntry.getKey(), imageEntry.getValue());
        }

        return imagesJson;
    }

    public void setMailingComponentsService(MailingComponentsService mailingComponentsService) {
        this.mailingComponentsService = mailingComponentsService;
    }
}
