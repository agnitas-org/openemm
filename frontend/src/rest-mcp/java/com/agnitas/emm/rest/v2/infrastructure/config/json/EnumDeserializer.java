/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.config.json;

import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class EnumDeserializer<T extends Enum<T>> extends ValueDeserializer<T> {

    private final Class<T> enumClass;
    private final Method parseMethod;

    public EnumDeserializer(Class<T> enumClass) {
        this.enumClass = enumClass;
        this.parseMethod = findParseMethod(enumClass);
    }

    private static Method findParseMethod(Class<?> enumClass) {
        try {
            return enumClass.getMethod("parse", String.class);
        } catch (NoSuchMethodException e) {
            try {
                return enumClass.getMethod("valueOf", String.class);
            } catch (NoSuchMethodException ex) {
                throw new IllegalStateException("No parse(String) or valueOf(String) on " + enumClass.getName());
            }
        }
    }

    @Override
    public T deserialize(JsonParser p, DeserializationContext ctxt) {
        String value = p.getValueAsString();
        if (StringUtils.isBlank(value)) {
            return null;
        }
        try {
            @SuppressWarnings("unchecked")
            T result = (T) parseMethod.invoke(null, value.trim());
            return result;
        } catch (Exception e) {
            throw new RestEnumException(p.currentName(), enumClass);
        }
    }
}
