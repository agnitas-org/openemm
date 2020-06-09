/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.agnitas.beans.ProfileField;
import org.springframework.stereotype.Component;

import com.agnitas.beans.AgnTagAttributeDto;
import com.agnitas.beans.AgnTagSelectAttributeDto;
import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComProfileFieldDao;
import com.agnitas.service.AgnTagAttributeResolver;

@Component
public class AgnTagColumnAttributeResolver implements AgnTagAttributeResolver {
    private ComProfileFieldDao profileFieldDao;

    public AgnTagColumnAttributeResolver(ComProfileFieldDao profileFieldDao) {
        this.profileFieldDao = profileFieldDao;
    }

    @Override
	public AgnTagAttributeDto resolve(ComAdmin admin, String tag, String attribute) throws Exception {
        if (attribute.equals("column")) {
            List<ProfileField> fields = profileFieldDao.getProfileFields(admin.getCompanyID());
            Map<String, String> options = new LinkedHashMap<>(fields.size());

            for (ProfileField field : fields) {
                options.put(field.getShortname(), field.getColumn());
            }

            return new AgnTagSelectAttributeDto(attribute, options);
        }

        return null;
    }
}
