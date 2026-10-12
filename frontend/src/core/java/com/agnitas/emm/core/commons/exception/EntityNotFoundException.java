/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.exception;

import java.util.Objects;
import java.util.Optional;

import com.agnitas.messages.Message;

public class EntityNotFoundException extends RuntimeException {

    private final transient Message message;

    public EntityNotFoundException() {
        this("Entity not found");
    }

    public EntityNotFoundException(String entityName, long id) {
        this(entityName, "ID = [%d]".formatted(id));
    }

    public EntityNotFoundException(String entityName, String identifier) {
        this(buildMessage(entityName, identifier));
    }

    public EntityNotFoundException(String message) {
        super(message);
        this.message = null;
    }

    public EntityNotFoundException(Message message) {
        super(Objects.toString(message, "Entity not found"));
        this.message = message;
    }

    public Optional<Message> getMessageOpt() {
        return Optional.ofNullable(message);
    }

    private static String buildMessage(String entityName, String identifier) {
        return "%s was not found for %s".formatted(
                Objects.toString(entityName, "Entity"),
                identifier
        );
    }
}
