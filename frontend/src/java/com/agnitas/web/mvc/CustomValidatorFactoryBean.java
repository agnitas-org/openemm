/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import java.util.Set;

import javax.validation.ConstraintViolation;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.NotReadablePropertyException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import com.agnitas.messages.Message;

public class CustomValidatorFactoryBean extends LocalValidatorFactoryBean {
    @Override
    protected void processConstraintViolations(Set<ConstraintViolation<Object>> violations, Errors errors) {
        for (ConstraintViolation<Object> violation : violations) {
            String field = determineField(violation);

            if (checkNoBindingFailure(errors, field)) {
                try {
                    if (errors instanceof BindingResult) {
                        BindingResult bindingResult = (BindingResult) errors;
                        String nestedField = bindingResult.getNestedPath() + field;

                        if ("".equals(nestedField)) {
                            Message message = getMessage(violation, violation.getInvalidValue());

                            if (message.isResolvable()) {
                                bindingResult.addError(new ObjectError(errors.getObjectName(), new String[]{message.getCode()}, message.getArguments(), null));
                            } else {
                                bindingResult.addError(new ObjectError(errors.getObjectName(), null, null, message.getCode()));
                            }
                        } else {
                            Object rejectedValue = getRejectedValue(field, violation, bindingResult);
                            Message message = getMessage(violation, rejectedValue);

                            if (message.isResolvable()) {
                                bindingResult.addError(new FieldError(errors.getObjectName(), nestedField, rejectedValue, false, new String[]{message.getCode()}, message.getArguments(), null));
                            } else {
                                bindingResult.addError(new FieldError(errors.getObjectName(), nestedField, rejectedValue, false, null, null, message.getCode()));
                            }
                        }
                    } else {
                        Message message = getMessage(violation, violation.getInvalidValue());

                        if (message.isResolvable()) {
                            errors.rejectValue(field, message.getCode(), message.getArguments(), null);
                        } else {
                            errors.rejectValue(field, null, null, message.getCode());
                        }
                    }
                } catch (NotReadablePropertyException e) {
                    throw new IllegalStateException("JSR-303 validated property '" + field + "' does not have a corresponding accessor for Spring data binding - check your DataBinder's configuration (bean property versus direct field access)", e);
                }
            }
        }
    }

    private boolean checkNoBindingFailure(Errors errors, String field) {
        FieldError error = errors.getFieldError(field);
        return error == null || !error.isBindingFailure();
    }

    private boolean checkTemplateRequiresInterpolation(String template) {
        if (StringUtils.isNotEmpty(template)) {
            return template.startsWith("{") && template.endsWith("}");
        } else {
            return false;
        }
    }

    private Message getMessage(ConstraintViolation<Object> violation, Object invalidValue) {
        String template = violation.getConstraintDescriptor().getMessageTemplate();

        if (checkTemplateRequiresInterpolation(template)) {
            return Message.exact(violation.getMessage());
        } else {
            return Message.of(template, invalidValue);
        }
    }
}
