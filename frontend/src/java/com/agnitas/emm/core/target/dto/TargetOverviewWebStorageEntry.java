/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.dto;

import org.agnitas.beans.RowsCountWebStorageEntry;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TargetOverviewWebStorageEntry extends RowsCountWebStorageEntry {
    @JsonProperty("show-world-targets")
    private boolean showWorldDelivery = true;

    @JsonProperty("show-test-and-admin-targets")
    private boolean showTestAndAdminDelivery = false;

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
