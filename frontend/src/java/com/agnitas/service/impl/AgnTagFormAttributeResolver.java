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
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.agnitas.beans.AgnTagAttributeDto;
import com.agnitas.beans.AgnTagSelectAttributeDto;
import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.UserFormDao;
import com.agnitas.service.AgnTagAttributeResolver;
import com.agnitas.userform.bean.UserForm;

@Component
public class AgnTagFormAttributeResolver implements AgnTagAttributeResolver {

    private final UserFormDao userFormDao;

    public AgnTagFormAttributeResolver(final UserFormDao userFormDao) {
        this.userFormDao = userFormDao;
    }


    @Override
    public AgnTagAttributeDto resolve(final ComAdmin admin, final String tag, final String attribute) throws Exception {
        if ("agnFORM".equals(tag) && "name".equals(attribute)) {
            final List<String> names = userFormDao.getUserForms(admin.getCompanyID())
                    .stream().filter(UserForm::isActive).map(UserForm::getFormName).collect(Collectors.toList());

            final Map<String, String> options = new LinkedHashMap<>(names.size());

            for (String name : names) {
                options.put(name, name);
            }

            return new AgnTagSelectAttributeDto(attribute, options);
        }

        return null;
    }

}
