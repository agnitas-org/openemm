/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

public class CompaniesConstraints {
    private final Set<Integer> includedIds;
    private final Set<Integer> excludedIds;

    public static Set<Integer> collectIds(String ids) {
        if (StringUtils.isBlank(ids)) {
            return null;
        }

        return AgnUtils.splitAndTrimList(ids)
            .stream()
            .map(Integer::parseInt)
            .collect(Collectors.toSet());
    }

    private static Set<Integer> unmodifiable(Set<Integer> ids) {
        return ids == null ? null : Collections.unmodifiableSet(ids);
    }

    public CompaniesConstraints() {
        // No constraints.
        includedIds = null;
        excludedIds = null;
    }

    public CompaniesConstraints(String included, String excluded) {
        includedIds = unmodifiable(CompaniesConstraints.collectIds(included));
        excludedIds = unmodifiable(CompaniesConstraints.collectIds(excluded));
    }

    public Set<Integer> getIncludedIds() {
        return includedIds;
    }

    public Set<Integer> getExcludedIds() {
        return excludedIds;
    }

    public boolean check(int companyId) {
        return (includedIds == null || includedIds.contains(companyId)) &&
            (excludedIds == null || !excludedIds.contains(companyId));
    }
}
