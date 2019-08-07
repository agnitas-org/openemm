/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.impl;

import java.util.List;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.support.DefaultConversionService;

import com.agnitas.service.ExtendedConversionService;

public class ExtendedDefaultConversionService extends DefaultConversionService implements ExtendedConversionService {

    public ExtendedDefaultConversionService() {
        super();
    }

	@Override
    public <S, T> List<T> convert(List<S> collection, Class<S> sourceType, Class<T> targetType) {
        TypeDescriptor sourceTypeDesc = TypeDescriptor.collection(collection.getClass(), TypeDescriptor.valueOf(sourceType));
        TypeDescriptor targetTypeDesc = TypeDescriptor.collection(collection.getClass(), TypeDescriptor.valueOf(targetType));

        @SuppressWarnings("unchecked")
        List<T> returnList = (List<T>) convert(collection, sourceTypeDesc, targetTypeDesc);
        return returnList;
    }

    @Override
    public <T, S> PaginatedListImpl<T> convertPaginatedList(PaginatedListImpl<S> paginatedList, Class<S> sourceType, Class<T> targetType) {
        List<S> list = paginatedList.getList();
        List<T> convertedList = convert(list, sourceType, targetType);

        return new PaginatedListImpl<>(convertedList,
                paginatedList.getFullListSize(),
                paginatedList.getObjectsPerPage(),
                paginatedList.getPageNumber(),
                paginatedList.getSortCriterion(),
                paginatedList.getSortDirection().getName());
    }
}
