/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.dto;

import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_FIRSTNAME;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_GENDER;
import static org.agnitas.emm.core.recipient.RecipientUtils.COLUMN_LASTNAME;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.agnitas.beans.RowsCountWebStorageEntry;
import org.agnitas.emm.core.recipient.RecipientUtils;
import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RecipientOverviewWebStorageEntry extends RowsCountWebStorageEntry {
    private static final int MAX_FIELDS_COUNT = RecipientUtils.MAX_SELECTED_FIELDS_COUNT;
    private static final List<String> DEFAULT_FIELDS = Arrays.asList(COLUMN_GENDER, COLUMN_FIRSTNAME, COLUMN_LASTNAME);

    @JsonProperty("fields")
    private List<String> selectedFields = DEFAULT_FIELDS;

    public List<String> getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(List<String> selectedFields) {
        if (CollectionUtils.isEmpty(selectedFields) || selectedFields.size() > MAX_FIELDS_COUNT) {
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
