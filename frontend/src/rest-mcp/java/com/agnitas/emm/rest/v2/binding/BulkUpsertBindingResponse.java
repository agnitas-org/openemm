/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.binding;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.emm.common.UpsertResult;
import com.agnitas.emm.core.binding.UpsertBindingResult;

public record BulkUpsertBindingResponse(
        int total,
        int inserted,
        int updated,
        int notChanged,
        // Structure: Map<MediaType, Map<MailingListId, Map<RecipientId, Result>>>
        Map<String, Map<Integer, Map<Integer, String>>> results
) {

    public static BulkUpsertBindingResponse from(List<UpsertBindingResult> results) {
        int insertedCount = 0;
        int updatedCount = 0;
        int notChangedCount = 0;

        for (UpsertBindingResult r : results) {
            switch (r.result()) {
                case INSERTED -> insertedCount++;
                case UPDATED -> updatedCount++;
                case NOT_CHANGED -> notChangedCount++;
            }
        }
        Map<String, Map<Integer, Map<Integer, String>>> nestedResults = results.stream().collect(Collectors.groupingBy(
                r -> r.mediatype().name(),
                Collectors.groupingBy(
                        UpsertBindingResult::mailinglistId,
                        Collectors.toMap(
                                UpsertBindingResult::recipientId,
                                r -> formatStatus(r.result())
                        )
                )
        ));

        return new BulkUpsertBindingResponse(
                results.size(),
                insertedCount,
                updatedCount,
                notChangedCount,
                nestedResults
        );
    }

    private static String formatStatus(UpsertResult result) {
        return switch (result) {
            case INSERTED -> "inserted";
            case UPDATED -> "updated";
            case NOT_CHANGED -> "not changed";
        };
    }
}
