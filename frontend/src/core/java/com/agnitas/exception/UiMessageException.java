/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.exception;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.messages.Message;

public class UiMessageException extends RuntimeException {

    private final Set<Message> errors;
    private final Map<String, Message> fieldsErrors;

    public UiMessageException(Set<Message> errors) {
        super("UiMessageException");
        this.errors = errors;
        this.fieldsErrors = Collections.emptyMap();
    }

    public UiMessageException(Message ... errors) {
        this.errors = Set.of(errors);
        this.fieldsErrors = Collections.emptyMap();
    }

    public UiMessageException(String ... keys) {
        this.fieldsErrors = Collections.emptyMap();
        errors = Arrays.stream(keys).
                map(Message::of)
                .collect(Collectors.toSet());
    }

    public UiMessageException(Map<String, Message> fieldsErrors) {
        this.errors = Collections.emptySet();
        this.fieldsErrors = fieldsErrors;
    }

    public Set<Message> getErrors() {
        return errors;
    }

    public Map<String, Message> getFieldsErrors() {
        return fieldsErrors;
    }

}
