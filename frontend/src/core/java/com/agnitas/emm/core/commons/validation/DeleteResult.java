/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.commons.validation;

import static java.util.Collections.emptyList;

import java.util.List;

import com.agnitas.messages.Message;

public record DeleteResult(
        int id,
        Status status,
        List<Message> errors
) {
    public enum Status {
        DELETED,
        NOT_FOUND,
        CONFLICT,
        FAILED,
    }

    public boolean isDeleted() {
        return status == Status.DELETED;
    }

    public boolean isNotFound() {
        return status == Status.NOT_FOUND;
    }

    public boolean isConflict() {
        return status == Status.CONFLICT;
    }

    public static DeleteResult deleted(int id) {
        return new DeleteResult(id, Status.DELETED, emptyList());
    }

    public static DeleteResult from(DeleteCheck check) {
        return new DeleteResult(
                check.id(),
                map(check.status()),
                check.errors()
        );
    }

    private static DeleteResult.Status map(DeleteCheck.Status status) {
        return switch (status) {
            case ALLOWED -> DeleteResult.Status.DELETED;
            case NOT_FOUND -> DeleteResult.Status.NOT_FOUND;
            case CONFLICT -> DeleteResult.Status.CONFLICT;
        };
    }

    public static DeleteResult failed(int id, String msgKey) {
        return notDeleted(id, Status.FAILED, msgKey);
    }

    public static DeleteResult conflict(int id, String msgKey) {
        return notDeleted(id, Status.CONFLICT, msgKey);
    }

    public static DeleteResult notFound(int id, String msgKey) {
        return notDeleted(id, Status.NOT_FOUND, msgKey);
    }

    public static DeleteResult notDeleted(int id, Status status, String msgKey) {
        return new DeleteResult(id, status, List.of(Message.of(msgKey)));
    }

    public static DeleteResult notFound(int id) {
        return notFound(id, "error.general.missing");
    }
}
