/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.agnitas.beans.AgnTagAttributeDto;
import com.agnitas.beans.AgnTagSelectAttributeDto;
import com.agnitas.beans.Admin;
import com.agnitas.messages.I18nString;
import com.agnitas.service.AgnTagAttributeResolver;

@Component
public class AgnTagDateBaseAttributeResolver implements AgnTagAttributeResolver {
    @Override
	public AgnTagAttributeDto resolve(Admin admin, String tag, String attribute) {
        if (tag.startsWith("agnDATE") && attribute.equals("base")) {
            Map<String, String> options = new LinkedHashMap<>();
            options.put("now", I18nString.getLocaleString("default.sysdate", admin.getLocale()));
            options.put("senddate", I18nString.getLocaleString("mailing.senddate", admin.getLocale()));
            return new AgnTagSelectAttributeDto(attribute, options);
        }

        return null;
    }
}
