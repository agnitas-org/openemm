/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.trackablelinks.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.agnitas.beans.SortingWebStorageEntry;

public class TrackableLinksOverviewWebStorageEntry extends SortingWebStorageEntry {

    @JsonProperty("include-deleted")
    private boolean includeDeleted;

    public boolean isIncludeDeleted() {
        return includeDeleted;
    }

    public void setIncludeDeleted(boolean includeDeleted) {
        this.includeDeleted = includeDeleted;
    }

    @Override
    public TrackableLinksOverviewWebStorageEntry clone() throws CloneNotSupportedException {
        TrackableLinksOverviewWebStorageEntry entry = (TrackableLinksOverviewWebStorageEntry) super.clone();
        entry.setIncludeDeleted(includeDeleted);
        return entry;
    }
}
