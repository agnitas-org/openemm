/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.backend.AgnTag;
import com.agnitas.beans.Admin;
import com.agnitas.beans.AgnTagAttributeDto;
import com.agnitas.beans.AgnTagSelectAttributeDto;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.core.service.RecipientFieldService;
import com.agnitas.service.AgnTagAttributeResolver;
import org.springframework.stereotype.Component;

@Component
public class AgnTagColumnAttributeResolver implements AgnTagAttributeResolver {

    private final RecipientFieldService recipientFieldService;

    public AgnTagColumnAttributeResolver(RecipientFieldService recipientFieldService) {
        this.recipientFieldService = recipientFieldService;
    }

    @Override
    public AgnTagAttributeDto resolve(Admin admin, String tag, String attribute) {
        if (attribute.equals("column")) {
            List<RecipientFieldDescription> fields = AgnTag.DB.getName().equals(tag)
                    ? recipientFieldService.listVisibleFields(admin.getCompanyID(), admin.getAdminID())
                    : recipientFieldService.getRecipientFields(admin.getCompanyID());

            Map<String, String> options = LinkedHashMap.newLinkedHashMap(fields.size());

            for (RecipientFieldDescription field : fields) {
                options.put(field.getColumnName(), field.getShortName());
            }

            return new AgnTagSelectAttributeDto(attribute, options);
        }

        return null;
    }
}
