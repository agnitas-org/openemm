/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.wysiwyg.service.impl;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.components.service.ComMailingComponentsService;
import com.agnitas.emm.core.wysiwyg.service.WysiwygService;

import net.sf.json.JSONObject;

public class WysiwygServiceImpl implements WysiwygService {

    private ComMailingComponentsService mailingComponentsService;

    @Override
    public JSONObject getImagesLinksWithDescriptionJson(ComAdmin admin, int mailingId) {
        final JSONObject imagesJson = new JSONObject();

        if(mailingId <= 0) {
            return imagesJson;
        }

        for (Map.Entry<String, String> imageEntry : mailingComponentsService.getUrlsByNamesForEmmImages(admin, mailingId).entrySet()) {
            imagesJson.put(imageEntry.getKey(), imageEntry.getValue());
        }

        return imagesJson;
    }

    @Required
    public void setMailingComponentsService(ComMailingComponentsService mailingComponentsService) {
        this.mailingComponentsService = mailingComponentsService;
    }
}
