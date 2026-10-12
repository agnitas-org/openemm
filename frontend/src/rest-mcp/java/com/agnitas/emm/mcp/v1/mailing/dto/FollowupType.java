/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.mailing.dto;

import java.util.Arrays;

import com.agnitas.emm.common.FollowUpType;

public enum FollowupType {
    NON_OPENER(FollowUpType.TYPE_FOLLOWUP_NON_OPENER),
    OPENER(FollowUpType.TYPE_FOLLOWUP_OPENER),
    NON_CLICKER(FollowUpType.TYPE_FOLLOWUP_NON_CLICKER),
    CLICKER(FollowUpType.TYPE_FOLLOWUP_CLICKER),
    NON_OPENER_WITHOUT_TRACKING_VETO(FollowUpType.TYPE_FOLLOWUP_NON_OPENER_WITHOUT_TRACKING_VETO),
    NON_CLICKER_WITHOUT_TRACKING_VETO(FollowUpType.TYPE_FOLLOWUP_NON_CKLICKER_WITHOUT_TRACKING_VETO),
    TRACKING_VETO(FollowUpType.TYPE_FOLLOWUP_TRACKING_VETO);

    private final FollowUpType coreVal;

    FollowupType(FollowUpType coreVal) {
        this.coreVal = coreVal;
    }

    public FollowUpType getCoreVal() {
        return coreVal;
    }

    public static FollowupType from(FollowUpType type) {
        return Arrays.stream(values())
                .filter(value -> value.coreVal == type)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown FollowUpType: " + type));
    }
}
