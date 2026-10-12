/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.search;

import static java.util.Collections.emptyList;
import static org.apache.commons.collections4.CollectionUtils.isEmpty;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({ "page", "list" })
public record PageResponse<T>(
        @JsonProperty("list") List<T> list,
        @JsonProperty("page") PageMeta page
) {

    public record PageMeta(
            int number, // 1-based
            int size,
            long totalCount
    ) {}

    public static <T> PageResponse<T> empty() {
        return PageResponse.of(emptyList(), 0, 0);
    }

    public static <T> PageResponse<T> of(List<T> fullList, int page, int size) {
        int totalCount = fullList.size();
        int start = Math.min((page - 1) * size, totalCount);
        int end = Math.min(start + size, totalCount);
        List<T> partialList = isEmpty(fullList)
                ? emptyList()
                : fullList.subList(start, end);
        return new PageResponse<>(
                partialList,
                new PageMeta(page, size, totalCount)
        );
    }
}
