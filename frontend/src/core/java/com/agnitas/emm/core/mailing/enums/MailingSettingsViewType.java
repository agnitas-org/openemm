/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.enums;

import java.util.Arrays;

public enum MailingSettingsViewType {

    TILES(0, "mailing.setting.view.full"),
    TABS(1, "mailing.setting.view.split");

    private final int id;
    private final String msgKey;

    MailingSettingsViewType(int id, String msgKey) {
        this.id = id;
        this.msgKey = msgKey;
    }

    public int getId() {
        return id;
    }

    public String getMsgKey() {
        return msgKey;
    }

    public static MailingSettingsViewType from(int id) {
        return Arrays.stream(MailingSettingsViewType.values())
                     .filter(e -> e.getId() == id)
                     .findFirst()
                     .orElseThrow(() -> new IllegalArgumentException("Invalid MailingSettingsViewType id: " + id));
    }

    public static MailingSettingsViewType getDefault() {
        return MailingSettingsViewType.TILES;
    }

}
