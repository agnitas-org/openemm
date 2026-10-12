/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.config.json;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NonNull;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class EnumConverterFactory implements ConverterFactory<String, Enum<?>> {

    @Override
    public <T extends Enum<?>> @NonNull Converter<String, ? extends T> getConverter(@NonNull Class<T> targetType) {
        return new CodeEnumConverter<>(targetType);
    }

    private static final class CodeEnumConverter<T extends Enum<?>> implements Converter<String, T> {

        private final Class<T> enumClass;
        private final Method parseMethod;

        CodeEnumConverter(Class<T> enumClass) {
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
                    throw new IllegalStateException("No suitable parse method on " + enumClass.getName());
                }
            }
        }

        @Override
        public T convert(@NonNull String source) {
            if (StringUtils.isBlank(source)) {
                return null;
            }
            try {
                @SuppressWarnings("unchecked")
                T result = (T) parseMethod.invoke(null, source.trim());
                return result;
            } catch (IllegalAccessException | InvocationTargetException e) {
                throw new RestEnumException(enumClass);
            }
        }
    }
}
