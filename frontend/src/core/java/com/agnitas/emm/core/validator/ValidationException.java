/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.validator;

import java.util.Collection;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.service.SimpleServiceResult;

public class ValidationException extends RuntimeException {

    private final transient SimpleServiceResult result;

    private ValidationException(SimpleServiceResult result) {
        super("Validation failed");
        this.result = result;
    }

    public static ValidationException of(ServiceResult<?> result) {
        return new ValidationException(new SimpleServiceResult(
                result.isSuccess(),
                result.getSuccessMessages(),
                result.getWarningMessages(),
                result.getErrorMessages(),
                result.getInfoMessages(),
                result.getFieldErrors()
        ));
    }

    public ValidationException(Map<ValidationField, Message> fieldErrors) {
        this(SimpleServiceResult.simpleFieldError(fieldErrors));
    }

    public ValidationException(Message... messages) {
        this(SimpleServiceResult.simpleError(messages));
    }

    public ValidationException(Collection<Message> messages) {
        this(SimpleServiceResult.simpleError(messages));
    }

    public ValidationException(String msgKey, Object... additionalData) {
        this(Message.of(msgKey, additionalData));
    }

    public static ValidationException exact(Map<ValidationField, String> errors) {
        return new ValidationException(
                errors.entrySet().stream()
                        .collect(Collectors.toUnmodifiableMap(
                                Map.Entry::getKey,
                                e -> Message.exact(e.getValue())
                        ))
        );
    }

    public static ValidationException exact(String message) {
        return new ValidationException(Message.exact(message));
    }

    public static ValidationException exact(ValidationField field, String message) {
        return exact(Map.of(field, message));
    }

    public static ValidationException invalidPath(String path) {
        return exact("Invalid " + path);
    }

    public static ValidationException invalidValue(ValidationField field) {
        return exact(field, "Invalid value");
    }

    public static ValidationException invalidValue(ValidationField field, String msgKey) {
        return new ValidationException(Map.of(field, Message.of(msgKey)));
    }

    public static ValidationException invalidValue(ValidationField field, Message message) {
        return new ValidationException(Map.of(field, message));
    }

    public static ValidationException notFound(ValidationField field) {
        return exact(field, "Not found");
    }

    public static ValidationException notFound(ValidationField field, int id) {
        return exact(field, "Entity with ID = %d not found".formatted(id));
    }

    public static ValidationException missing(ValidationField field) {
        return invalidValue(field, "error.default.required");
    }

    public SimpleServiceResult getResult() { return result; }

    public Map<ValidationField, Message> getFieldErrors() { return result.getFieldErrors(); }

    public Collection<Message> getErrors()   { return result.getErrorMessages(); }

    public Collection<Message> getWarnings() { return result.getWarningMessages(); }
}
