/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.validator;

import java.util.Objects;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.agnitas.emm.core.validator.annotation.FieldsValueMatch;
import org.springframework.beans.BeanWrapperImpl;

public class FieldValuesEquivalenceValidator implements ConstraintValidator<FieldsValueMatch, Object> {

   private String field;

   private String fieldMatch;

   private String message;

   @Override
    public void initialize(FieldsValueMatch constraint) {
      this.field = constraint.field();
      this.fieldMatch = constraint.fieldMatch();
      this.message = constraint.message();
   }

   @Override
    public boolean isValid(Object candidate, ConstraintValidatorContext context) {
      Object fieldValue = new BeanWrapperImpl(candidate).getPropertyValue(field);
      Object fieldMatchValue = new BeanWrapperImpl(candidate).getPropertyValue(fieldMatch);

      boolean isValid = Objects.equals(fieldValue, fieldMatchValue);

      if(!isValid) {
         context.buildConstraintViolationWithTemplate(message)
                 .addPropertyNode(field)
                 .addConstraintViolation()
                 .buildConstraintViolationWithTemplate(message)
                 .addPropertyNode(fieldMatch)
                 .addConstraintViolation()
                 .disableDefaultConstraintViolation();
      }

      return isValid;
   }
}
