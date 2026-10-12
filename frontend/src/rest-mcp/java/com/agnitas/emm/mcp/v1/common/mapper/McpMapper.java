/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.common.mapper;

import java.util.List;
import java.util.function.Function;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import org.openapitools.jackson.nullable.JsonNullable;

public interface McpMapper {

    default <T> PatchField<T> map(JsonNullable<T> source) {
        if (source == null || source.isUndefined()) {
            return PatchField.absent();
        }

        return PatchField.of(source.orElse(null)); // explicit null = clear field
    }

    default <S, T> PatchField<T> map(JsonNullable<S> source, Function<S, T> converter) {
        if (source == null || source.isUndefined()) {
            return PatchField.absent();
        }

        S value = source.orElse(null);
        return PatchField.of(value == null ? null : converter.apply(value));
    }

    default <T, R> PageResponse<R> toPageResponse(PaginatedList<T> source, Function<T, R> mapper) {
        if (source == null) {
            return null;
        }

        List<R> list = source.getList()
                .stream()
                .map(mapper)
                .toList();

        PageResponse.PageMeta meta = new PageResponse.PageMeta(
                source.getPageNumber(),
                source.getPageSize(),
                source.getFullListSize()
        );

        return new PageResponse<>(list, meta);
    }

}
