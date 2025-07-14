/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.agnitas.beans.SortingWebStorageEntry;

// TODO: replace with SortingWebStorageEntry after EMMGUI-714 will be finished and old design will removed
public class TargetOverviewWebStorageEntry extends SortingWebStorageEntry {
    @JsonProperty("show-world-targets")
    private boolean showWorldDelivery = true; // TODO: remove after EMMGUI-714 will be finished and old design will removed

    @JsonProperty("show-test-and-admin-targets")
    private boolean showTestAndAdminDelivery = false; // TODO: remove after EMMGUI-714 will be finished and old design will removed

    public boolean isShowWorldDelivery() {
        return showWorldDelivery;
    }

    public void setShowWorldDelivery(boolean showWorldDelivery) {
        this.showWorldDelivery = showWorldDelivery;
    }

    public boolean isShowTestAndAdminDelivery() {
        return showTestAndAdminDelivery;
    }

    public void setShowTestAndAdminDelivery(boolean showTestAndAdminDelivery) {
        this.showTestAndAdminDelivery = showTestAndAdminDelivery;
    }

    @Override
    public TargetOverviewWebStorageEntry clone() throws CloneNotSupportedException {
        TargetOverviewWebStorageEntry entry = (TargetOverviewWebStorageEntry) super.clone();
        entry.setShowWorldDelivery(showWorldDelivery);
        entry.setShowTestAndAdminDelivery(showTestAndAdminDelivery);
        return entry;
    }
}
