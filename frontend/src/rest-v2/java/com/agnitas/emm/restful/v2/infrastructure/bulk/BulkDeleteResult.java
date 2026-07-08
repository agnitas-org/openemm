/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.bulk;

import java.util.List;

import com.agnitas.emm.restful.v2.infrastructure.exception.ErrorEntry;
import com.agnitas.messages.Message;
import com.fasterxml.jackson.annotation.JsonInclude;

public record BulkDeleteResult(
    int id,
    BulkOperationStatus status,
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ErrorEntry> errors
) implements BulkOperationResult {

    @Override
    public boolean isSuccess() {
        return status == BulkOperationStatus.DELETED;
    }

    public static BulkDeleteResult deleted(int id) {
        return new BulkDeleteResult(id, BulkOperationStatus.DELETED, null);
    }

    public static BulkDeleteResult notFound(int id) {
        return new BulkDeleteResult(id, BulkOperationStatus.NOT_FOUND, List.of(new ErrorEntry("Not found")));
    }

    public static BulkDeleteResult failed(int id, String message) {
        return new BulkDeleteResult(id, BulkOperationStatus.FAILED, List.of(new ErrorEntry(message)));
    }

    public static BulkDeleteResult failed(int id, Message message) {
        return new BulkDeleteResult(id, BulkOperationStatus.FAILED, List.of(new ErrorEntry(message)));
    }
}
