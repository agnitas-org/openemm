/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.enums;

import java.util.Arrays;
import java.util.Objects;

import org.antlr.v4.runtime.misc.Nullable;

public abstract class DatabaseFieldUtils {

	@Nullable
    public static <T> DatabaseField<T, ? extends Enum<?>> getByCode(final Object code, final DatabaseField<T, ? extends Enum<?>>[] values) {
        return Arrays.stream(values)
                .filter(field -> field.getCode().equals(code))
                .findFirst().orElse(null);
    }

    @Nullable
    public static <T> DatabaseField<T, ? extends Enum<?>> getByName(final String readableName, final DatabaseField<T, ? extends Enum<?>>[] values) {
        return Arrays.stream(values)
                .filter(field -> field.getReadableName().equals(readableName))
                .findFirst().orElse(null);
    }

    @Nullable
    public static <T> String getTranslationKeyByCode(final Object code, final DatabaseField<T, ? extends Enum<?>>[] values) {
        DatabaseField<T, ? extends Enum<?>> field = getByCode(code, values);
        return Objects.nonNull(field) ? field.getTranslationKey() : null;
    }

    public static <T> boolean isContainsCode(final Object code, final DatabaseField<T, ? extends Enum<?>>[] values){
        return Objects.nonNull(getByCode(code, values));
    }
}
