/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.util.List;

import com.agnitas.beans.PaginatedList;
import org.springframework.core.convert.ConversionService;
import org.springframework.core.convert.TypeDescriptor;

public interface ExtendedConversionService extends ConversionService {
    default <S, T> List<T> convert(List<S> collection, Class<S> sourceType, Class<T> targetType) {
        try {
			TypeDescriptor sourceTypeDesc = TypeDescriptor.collection(collection.getClass(), TypeDescriptor.valueOf(sourceType));
			TypeDescriptor targetTypeDesc = TypeDescriptor.collection(collection.getClass(), TypeDescriptor.valueOf(targetType));

			@SuppressWarnings("unchecked")
			List<T> returnList = (List<T>) convert(collection, sourceTypeDesc, targetTypeDesc);
			return returnList;
		} catch (Exception e) {
			throw e;
		}
    }

    default <T, S> PaginatedList<T> convertPaginatedList(PaginatedList<S> paginatedList, Class<S> sourceType, Class<T> targetType) {
        List<S> list = paginatedList.getList();
        List<T> convertedList = convert(list, sourceType, targetType);

        PaginatedList<T> result = new PaginatedList<>(
                convertedList,
                paginatedList.getFullListSize(),
                paginatedList.getPageSize(),
                paginatedList.getPageNumber(),
                paginatedList.getSortCriterion(),
                paginatedList.getSortDirection().getId()
        );

        result.setNotFilteredFullListSize(paginatedList.getNotFilteredFullListSize());
        return result;
    }
}
