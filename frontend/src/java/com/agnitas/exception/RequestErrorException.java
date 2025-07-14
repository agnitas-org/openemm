/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.exception;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.agnitas.messages.Message;

public class RequestErrorException extends RuntimeException {
	private static final long serialVersionUID = -7339419974375371845L;
	
	private Set<Message> errors = new LinkedHashSet<>();
    private Map<String, Message> fieldsErrors = new HashMap<>();

    public RequestErrorException(Set<Message> errors) {
    	super("RequestErrorException");
        this.errors = errors;
    }

    public RequestErrorException(Message ... errors) {
        this.errors = Set.of(errors);
    }

    public RequestErrorException(String ... keys) {
        errors = Arrays.stream(keys).
                map(Message::of)
                .collect(Collectors.toSet());
    }

    public RequestErrorException(Map<String, Message> fieldsErrors) {
        this.fieldsErrors = fieldsErrors;
    }

    public Set<Message> getErrors() {
        return errors;
    }

    public Map<String, Message> getFieldsErrors() {
        return fieldsErrors;
    }
}
