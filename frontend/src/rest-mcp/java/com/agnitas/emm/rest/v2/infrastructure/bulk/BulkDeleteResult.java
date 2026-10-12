/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.bulk;

import static java.util.Collections.emptyList;

import java.util.List;

import com.agnitas.emm.core.commons.validation.DeleteResult;
import com.agnitas.emm.rest.v2.infrastructure.exception.ErrorEntry;
import com.agnitas.messages.Message;
import com.fasterxml.jackson.annotation.JsonInclude;

public record BulkDeleteResult(
    int id,
    Status status,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ErrorEntry> errors
) implements BulkOperationResult {

    public enum Status {
        DELETED,
        NOT_FOUND,
        CONFLICT,
        FAILED,
    }

    @Override
    public boolean isSuccess() {
        return status == Status.DELETED;
    }

    public static BulkDeleteResult deleted(int id) {
        return new BulkDeleteResult(id, Status.DELETED, emptyList());
    }

    public static BulkDeleteResult notFound(int id) {
        return from(id, Status.NOT_FOUND, "Not found");
    }

    public static BulkDeleteResult conflict(int id, Message message) {
        return from(id, Status.CONFLICT, message);
    }

    public static BulkDeleteResult failed(int id, Message message) {
        return from(id, Status.FAILED, message);
    }

    public static BulkDeleteResult from(int id, Status status, String message) {
        return from(id, status, Message.exact(message));
    }

    public static BulkDeleteResult from(int id, Status status, Message message) {
        return new BulkDeleteResult(id, status, List.of(new ErrorEntry(message)));
    }

    public static BulkDeleteResult from(DeleteResult source) {
        return new BulkDeleteResult(
                source.id(),
                mapStatus(source.status()),
                source.errors().stream().map(ErrorEntry::new).toList()
        );
    }

    private static BulkDeleteResult.Status mapStatus(DeleteResult.Status status) {
        return switch (status) {
            case DELETED -> BulkDeleteResult.Status.DELETED;
            case NOT_FOUND -> BulkDeleteResult.Status.NOT_FOUND;
            case CONFLICT -> BulkDeleteResult.Status.CONFLICT;
            case FAILED -> BulkDeleteResult.Status.FAILED;
        };
    }

}
