/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dto;

import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.agnitas.beans.SortingWebStorageEntry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class MailingOverviewWebStorageEntry extends SortingWebStorageEntry {

    @JsonProperty("mailing-types")
    private Set<MailingType> mailingTypes = new HashSet<>(Collections.singletonList(MailingType.NORMAL));

    @JsonProperty("media-types")
    private Set<MediaTypes> mediaTypes = new HashSet<>();

    @JsonProperty("fields")
    private List<String> selectedFields = Collections.emptyList();

    @JsonProperty("page")
    private int page = 1;

    private String searchQueryText;

    public Set<MailingType> getMailingTypes() {
        return mailingTypes;
    }

    public void setMailingTypes(Set<MailingType> mailingTypes) {
        this.mailingTypes = mailingTypes;
    }

    public Set<MediaTypes> getMediaTypes() {
        return mediaTypes;
    }

    public void setMediaTypes(Set<MediaTypes> mediaTypes) {
        this.mediaTypes = mediaTypes;
    }

    public List<String> getSelectedFields() {
        return selectedFields;
    }

    public void setSelectedFields(List<String> selectedFields) {
        this.selectedFields = Objects.requireNonNullElse(selectedFields, Collections.emptyList());
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
    public MailingOverviewWebStorageEntry clone() throws CloneNotSupportedException {
        MailingOverviewWebStorageEntry entry = (MailingOverviewWebStorageEntry) super.clone();
        entry.setMailingTypes(mailingTypes);
        entry.setMediaTypes(mediaTypes);
        entry.setSelectedFields(selectedFields.isEmpty() ? Collections.emptyList() : new ArrayList<>(selectedFields));
        entry.setSearchQueryText(searchQueryText);
        entry.setPage(page);
        return entry;
    }
}
