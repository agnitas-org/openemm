/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailing.dto;

import org.agnitas.beans.RowsCountWebStorageEntry;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MailingOverviewWebStorageEntry extends RowsCountWebStorageEntry {
    @JsonProperty("mailing-type-normal")
    private boolean mailingTypeNormal = true;

    @JsonProperty("mailing-type-date")
    private boolean mailingTypeDate = false;

    @JsonProperty("mailing-type-event")
    private boolean mailingTypeEvent = false;

    public boolean isMailingTypeNormal() {
        return mailingTypeNormal;
    }

    public void setMailingTypeNormal(boolean mailingTypeNormal) {
        this.mailingTypeNormal = mailingTypeNormal;
    }

    public boolean isMailingTypeDate() {
        return mailingTypeDate;
    }

    public void setMailingTypeDate(boolean mailingTypeDate) {
        this.mailingTypeDate = mailingTypeDate;
    }

    public boolean isMailingTypeEvent() {
        return mailingTypeEvent;
    }

    public void setMailingTypeEvent(boolean mailingTypeEvent) {
        this.mailingTypeEvent = mailingTypeEvent;
    }

    @Override
    public MailingOverviewWebStorageEntry clone() throws CloneNotSupportedException {
        MailingOverviewWebStorageEntry entry = (MailingOverviewWebStorageEntry) super.clone();
        entry.setMailingTypeNormal(mailingTypeNormal);
        entry.setMailingTypeDate(mailingTypeDate);
        entry.setMailingTypeEvent(mailingTypeEvent);
        return entry;
    }
}
