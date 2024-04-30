/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.mailing.dto;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mediatypes.common.MediaTypes;
import org.agnitas.beans.RowsCountWebStorageEntry;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MailingOverviewWebStorageEntry extends RowsCountWebStorageEntry {

    @JsonProperty("mailing-types")
    private Set<MailingType> mailingTypes = new HashSet<>(Collections.singletonList(MailingType.NORMAL));

    @JsonProperty("media-types")
    private Set<MediaTypes> mediaTypes = new HashSet<>(Collections.singletonList(MediaTypes.EMAIL));

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

    @Override
    public MailingOverviewWebStorageEntry clone() throws CloneNotSupportedException {
        MailingOverviewWebStorageEntry entry = (MailingOverviewWebStorageEntry) super.clone();
        entry.setMailingTypes(mailingTypes);
        entry.setMediaTypes(mediaTypes);
        return entry;
    }
}
