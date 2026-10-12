/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.dto;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.core.validator.ValidationField;
import com.agnitas.messages.Message;

public record PatchField<T>(boolean present, T value) {

    public static <T> PatchField<T> absent() {
        return new PatchField<>(false, null);
    }

    public static <T> PatchField<T> of(T value) {
        return new PatchField<>(true, value);
    }

    public void ifPresent(Consumer<? super T> consumer) {
        if (present) {
            consumer.accept(value);
        }
    }

    public static boolean isPresent(PatchField<?> field) {
        return field != null && field.present();
    }

    public void check(Predicate<T> validator, ValidationField field) {
        if (present && !validator.test(value)) {
            throw ValidationException.invalidValue(field);
        }
    }

    public void check(Predicate<T> validator, ValidationField field, String msgKey) {
        check(validator, field, Message.of(msgKey));
    }

    public void check(Predicate<T> validator, ValidationField field, Message error) {
        if (present && !validator.test(value)) {
            throw ValidationException.invalidValue(field, error);
        }
    }

    public <R> PatchField<R> map(Function<? super T, ? extends R> fn) {
        R mappedValue = value == null ? null : fn.apply(value);
        return present ? new PatchField<>(true, mappedValue) : absent();
    }
}
