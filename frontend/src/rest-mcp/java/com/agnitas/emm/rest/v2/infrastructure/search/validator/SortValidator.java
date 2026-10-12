/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.search.validator;

import java.util.Set;

import com.agnitas.emm.rest.v2.infrastructure.search.SortEnum;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class SortValidator implements ConstraintValidator<ValidSort, Pageable> {

    private Set<String> allowed;

    @Override
    public void initialize(ValidSort ann) {
        allowed = SortEnum.allowedValues(ann.value());
    }

    @Override
    public boolean isValid(Pageable pageable, ConstraintValidatorContext ctx) {
        if (pageable == null || pageable.getSort().isUnsorted()) {
            return true;
        }
        for (Sort.Order order : pageable.getSort()) {
            String property = order.getProperty().toLowerCase();
            if (!allowed.contains(property)) {
                ctx.disableDefaultConstraintViolation();
                ctx
                        .buildConstraintViolationWithTemplate(getMessage(order.getProperty()))
                        .addPropertyNode("sort")
                        .addConstraintViolation();
                return false;
            }
        }
        return true;
    }

    private String getMessage(String property) {
        return "Invalid sort value '%s'. Allowed values: [%s]".formatted(property, String.join(", ", allowed));
    }
}
