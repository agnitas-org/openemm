/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.validation;

import java.util.List;

import com.agnitas.messages.Message;

public record DeleteCheck(
        int id,
        Status status,
        List<Message> errors
) {
    public enum Status {
        ALLOWED,
        NOT_FOUND,
        CONFLICT,
    }

    public boolean isAllowed() {
        return status == Status.ALLOWED;
    }

    public boolean isNotFound() {
        return status == Status.NOT_FOUND;
    }

    public static DeleteCheck allowed(int id) {
        return new DeleteCheck(id, Status.ALLOWED, List.of());
    }

    public static DeleteCheck notFound(int id, String msgKey) {
        return new DeleteCheck(
                id,
                Status.NOT_FOUND,
                List.of(Message.of(msgKey))
        );
    }

    public static DeleteCheck notFound(int id) {
        return notFound(id, "error.general.missing");
    }

    public static DeleteCheck conflict(int id, String msgKey) {
        return DeleteCheck.conflict(id, Message.of(msgKey));
    }

    public static DeleteCheck conflict(int id, Message message) {
        return DeleteCheck.conflict(id, List.of(message));
    }

    public static DeleteCheck conflict(int id, List<Message> messages) {
        return new DeleteCheck(
                id,
                Status.CONFLICT,
                messages
        );
    }
}
