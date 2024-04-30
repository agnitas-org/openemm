/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dto;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.agnitas.emm.core.mailing.dto.MailingOverviewWebStorageEntry;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ComMailingOverviewWebStorageEntry extends MailingOverviewWebStorageEntry {
    @JsonProperty("fields")
    private List<String> selectedFields = Collections.emptyList();

    @JsonProperty("page")
    private int page = 1;

    private String searchQueryText;

    public List<String> getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(List<String> selectedFields) {
        if (selectedFields == null) {
            this.selectedFields = Collections.emptyList();
        } else {
            this.selectedFields = selectedFields;
        }
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public String getSearchQueryText() {
        return searchQueryText;
    }

    public void setSearchQueryText(String searchQueryText) {
        this.searchQueryText = searchQueryText;
    }

    @Override
    public ComMailingOverviewWebStorageEntry clone() throws CloneNotSupportedException {
        ComMailingOverviewWebStorageEntry entry = (ComMailingOverviewWebStorageEntry) super.clone();
        entry.setSelectedFields(selectedFields.isEmpty() ? Collections.emptyList() : new ArrayList<>(selectedFields));
        entry.setSearchQueryText(searchQueryText);
        entry.setPage(page);
        return entry;
    }
}
