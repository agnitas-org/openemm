/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.dto;

import java.util.HashMap;
import java.util.Map;

import com.agnitas.emm.core.preview.dto.MailingPreviewSettings;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.agnitas.beans.WebStorageEntry;

public class MailingPreviewWebStorageEntry implements WebStorageEntry {

    @JsonProperty("settings") // mailing id -> preview settings
    private Map<Integer, MailingPreviewSettings> settings = new HashMap<>();

    public Map<Integer, MailingPreviewSettings> getSettings() {
        return settings;
    }

    public void setSettings(Map<Integer, MailingPreviewSettings> settings) {
        this.settings = settings;
    }

    public MailingPreviewSettings getSettings(int mailingId) {
        return settings.computeIfAbsent(mailingId, k -> new MailingPreviewSettings());
    }

    @Override
    public MailingPreviewWebStorageEntry clone() throws CloneNotSupportedException {
        MailingPreviewWebStorageEntry entry = (MailingPreviewWebStorageEntry) super.clone();
        entry.setSettings(settings);
        return entry;
    }
}
