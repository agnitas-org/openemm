/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.agnitas.beans.SortingWebStorageEntry;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

// TODO: EMMGUI-714: remove and replace with SortingWebStorageEntry when old design will be removed
//  since filter for this table was removed in redesigned UI
public class FilterTypesAndRowsCountWebStorageEntry extends SortingWebStorageEntry {
    @JsonProperty("types")
    private List<String> filterTypes = Collections.emptyList();

    public List<String> getFilterTypes() {
        return filterTypes;
    }

    public void setFilterTypes(List<String> filterTypes) {
        if (CollectionUtils.isEmpty(filterTypes)) {
            this.filterTypes = Collections.emptyList();
        } else {
            this.filterTypes = filterTypes;
        }
    }

    @Override
    public FilterTypesAndRowsCountWebStorageEntry clone() throws CloneNotSupportedException {
        FilterTypesAndRowsCountWebStorageEntry entry = (FilterTypesAndRowsCountWebStorageEntry) super.clone();
        entry.setFilterTypes(filterTypes.isEmpty() ? Collections.emptyList() : new ArrayList<>(filterTypes));
        return entry;
    }
}
