/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.report.converter;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.core.convert.converter.Converter;

/**
 * Extension of base Spring converter.
 * Allow conversion of generic collections into the generic list.
 *
 * @param <E> type of entity which should be converted.
 * @param <D> type of entity in which should be converted the first one.
 */
public interface CollectionConverter<E, D> extends Converter<E, D> {

    /**
     * Converts each entry of convertible collection to the second one and put in into the List.
     *
     * @param entities collection of convertible entities.
     * @return List of converted entities.
     */
    default List<D> convert(final Collection<E> entities) {
        return entities.stream()
                .map(this::convert)
                .collect(Collectors.toList());
    }
}
