/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dto;

import com.agnitas.beans.WebStorageEntry;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DashboardCalendarWebStorageEntry implements WebStorageEntry {

    @JsonProperty("showUnsentList")
    private Boolean showUnsentList;
    @JsonProperty("showUnsentPlanned")
    private Boolean showUnsentPlanned = true;

    public Boolean getShowUnsentList() {
        return showUnsentList;
    }

    public void setShowUnsentList(Boolean showUnsentList) {
        this.showUnsentList = showUnsentList;
    }

    public Boolean getShowUnsentPlanned() {
        return showUnsentPlanned;
    }

    public void setShowUnsentPlanned(Boolean showUnsentPlanned) {
        this.showUnsentPlanned = showUnsentPlanned;
    }

    @Override
    public DashboardCalendarWebStorageEntry clone() throws CloneNotSupportedException {
        DashboardCalendarWebStorageEntry entry = (DashboardCalendarWebStorageEntry) super.clone();
        entry.setShowUnsentList(showUnsentList);
        entry.setShowUnsentPlanned(showUnsentPlanned);
        return entry;
    }
}
