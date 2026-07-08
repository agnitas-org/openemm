/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.recipient.dto;

import java.util.Collections;
import java.util.List;

import com.agnitas.beans.SortingWebStorageEntry;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ProfileFieldOverviewWebStorageEntry extends SortingWebStorageEntry {

    private static final int MAX_FIELDS_COUNT = 8;

    @JsonProperty("fields")
    private List<String> selectedFields = Collections.emptyList();

    public List<String> getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(List<String> selectedFields) {
        if (selectedFields == null || selectedFields.size() > MAX_FIELDS_COUNT) {
            this.selectedFields = Collections.emptyList();
        } else {
            this.selectedFields = selectedFields;
        }
    }
}
