/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.mapper;

import java.util.List;
import java.util.function.Function;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.rest.v2.infrastructure.search.PageResponse;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.Sort;

public interface RestMapper {

    default boolean toDirection(Sort sort) {
        return sort.isUnsorted()
                || !sort.iterator().hasNext()
                || sort.iterator().next().getDirection().isAscending();
    }

    default String toSortProperty(Sort sort) {
        if (sort.isUnsorted() || !sort.iterator().hasNext()) {
            return null;
        }
        return sort.iterator().next().getProperty();
    }

    default <T> PatchField<T> map(JsonNullable<T> source) {
        if (source == null || source.isUndefined()) {
            return PatchField.absent();
        }
        return PatchField.of(source.orElse(null)); // explicit null = clear field
    }

    default <T, R> PageResponse<R> toPageResponse(PaginatedList<T> source, Function<T, R> mapper) {
        List<R> list = source.getList().stream()
                .map(mapper)
                .toList();
        PageResponse.PageMeta page = new PageResponse.PageMeta(
                source.getPageNumber(),
                source.getPageSize(),
                source.getFullListSize()
        );
        return new PageResponse<>(list, page);
    }

    default <S, T> PatchField<T> map(JsonNullable<S> source, Function<S, T> converter) {
        if (source == null || source.isUndefined()) {
            return PatchField.absent();
        }
        S v = source.orElse(null);
        return PatchField.of(v == null ? null : converter.apply(v));
    }
}
