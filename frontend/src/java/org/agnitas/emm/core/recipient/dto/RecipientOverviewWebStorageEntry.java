/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.dto;

import com.agnitas.emm.core.service.RecipientStandardField;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.agnitas.beans.SortingWebStorageEntry;
import org.agnitas.emm.core.recipient.RecipientUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class RecipientOverviewWebStorageEntry extends SortingWebStorageEntry {
    private static final int MAX_FIELDS_COUNT = RecipientUtils.MAX_SELECTED_FIELDS_COUNT;
    private static final List<String> DEFAULT_FIELDS = Arrays.asList(RecipientStandardField.Gender.getColumnName(), RecipientStandardField.Firstname.getColumnName(), RecipientStandardField.Lastname.getColumnName());

    @JsonProperty("fields")
    private List<String> selectedFields = DEFAULT_FIELDS;

    public List<String> getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(List<String> selectedFields) {
        if (selectedFields == null || selectedFields.size() > MAX_FIELDS_COUNT) {
            this.selectedFields = DEFAULT_FIELDS;
        } else {
            this.selectedFields = selectedFields;
        }
    }

    @Override
    public RecipientOverviewWebStorageEntry clone() throws CloneNotSupportedException {
        RecipientOverviewWebStorageEntry entry = (RecipientOverviewWebStorageEntry) super.clone();
        entry.setSelectedFields(selectedFields.isEmpty() ? Collections.emptyList() : new ArrayList<>(selectedFields));
        return entry;
    }
}
