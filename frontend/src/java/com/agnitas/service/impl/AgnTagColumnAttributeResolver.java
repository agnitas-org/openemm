/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.beans.AgnTagAttributeDto;
import com.agnitas.beans.AgnTagSelectAttributeDto;
import com.agnitas.beans.ProfileField;
import com.agnitas.emm.core.profilefields.service.ProfileFieldService;
import com.agnitas.service.AgnTagAttributeResolver;
import org.agnitas.backend.AgnTag;
import org.agnitas.beans.LightProfileField;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class AgnTagColumnAttributeResolver implements AgnTagAttributeResolver {

    private final ProfileFieldService profileFieldService;

    public AgnTagColumnAttributeResolver(ProfileFieldService profileFieldService) {
        this.profileFieldService = profileFieldService;
    }

    @Override
    public AgnTagAttributeDto resolve(Admin admin, String tag, String attribute) throws Exception {
        if (attribute.equals("column")) {
            List<ProfileField> fields;
            if (AgnTag.DB.getName().equals(tag)) {
                fields = profileFieldService.getVisibleProfileFields(admin.getAdminID(), admin.getCompanyID());
            } else {
                fields = profileFieldService.getProfileFields(admin.getCompanyID());
            }

            Map<String, String> options = new LinkedHashMap<>(fields.size());

            for (LightProfileField field : fields) {
                options.put(field.getColumn(), field.getShortname());
            }

            return new AgnTagSelectAttributeDto(attribute, options);
        }

        return null;
    }
}
