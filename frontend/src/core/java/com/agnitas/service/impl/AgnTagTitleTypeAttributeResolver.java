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

import com.agnitas.beans.Title;
import org.springframework.stereotype.Component;

import com.agnitas.beans.AgnTagAttributeDto;
import com.agnitas.beans.AgnTagSelectAttributeDto;
import com.agnitas.beans.Admin;
import com.agnitas.dao.TitleDao;
import com.agnitas.service.AgnTagAttributeResolver;

@Component
public class AgnTagTitleTypeAttributeResolver implements AgnTagAttributeResolver {
    private TitleDao titleDao;

    public AgnTagTitleTypeAttributeResolver(TitleDao titleDao) {
        this.titleDao = titleDao;
    }

    @Override
	public AgnTagAttributeDto resolve(Admin admin, String tag, String attribute) {
        if (tag.startsWith("agnTITLE") && attribute.equals("type")) {
            List<Title> titles = titleDao.getTitles(admin.getCompanyID(), false);
            Map<String, String> options = new LinkedHashMap<>(titles.size());

            for (Title title : titles) {
                options.put(Integer.toString(title.getId()), title.getDescription());
            }

            return new AgnTagSelectAttributeDto(attribute, options);
        }

        return null;
    }
}
