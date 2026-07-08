/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.bulk;

import java.util.List;

public record BulkOperationResponse<R extends BulkOperationResult>(
    int total,
    int succeeded,
    int failed,
    List<R> results
) {
    public static <R extends BulkOperationResult> BulkOperationResponse<R> from(List<R> results) {
        int succeeded = (int) results.stream()
            .filter(BulkOperationResult::isSuccess)
            .count();

        return new BulkOperationResponse<>(
            results.size(),
            succeeded,
            results.size() - succeeded,
            results
        );
    }
}
