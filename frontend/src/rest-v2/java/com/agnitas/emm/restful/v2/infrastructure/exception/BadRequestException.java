/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.exception;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.messages.Message;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.server.ResponseStatusException;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class BadRequestException extends ResponseStatusException {

    private final Set<Message> errors = new LinkedHashSet<>();
    private final Map<String, Message> fieldErrors = new HashMap<>();

    public BadRequestException(String detail) {
        super(HttpStatus.BAD_REQUEST, detail);
    }

    public BadRequestException(Set<Message> errors) {
        super(HttpStatus.BAD_REQUEST);
        this.errors.addAll(errors);
    }

    public BadRequestException(Map<String, String> errors) {
        super(HttpStatus.BAD_REQUEST);
        this.fieldErrors.putAll(errors.entrySet().stream().collect(Collectors.toMap(
                Map.Entry::getKey,
                s -> Message.exact(s.getValue())
        )));
    }

    public Set<Message> getErrors() {
        return errors;
    }

    public Map<String, Message> getFieldErrors() {
        return fieldErrors;
    }
}
