/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.search.validator;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SortValidator implements ConstraintValidator<ValidSort, String> {

    private Set<String> allowed;

    @Override
    public void initialize(ValidSort ann) {
        allowed = Arrays.stream(ann.enumClass().getEnumConstants())
                .map(e -> ((Enum<?>) e).name().toLowerCase())
                .collect(Collectors.toSet());
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext ctx) {
        if (value == null || allowed.contains(value.toLowerCase())) {
            return true;
        }
        ctx.disableDefaultConstraintViolation();
        ctx.buildConstraintViolationWithTemplate(
            "Invalid sort value '%s'. Allowed values: [%s]".formatted(value, String.join(", ", allowed))
        ).addConstraintViolation();

        return false;
    }
}
