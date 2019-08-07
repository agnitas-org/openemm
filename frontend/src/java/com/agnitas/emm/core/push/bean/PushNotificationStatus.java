/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.push.bean;

public enum PushNotificationStatus {
    READY,
    SCHEDULED,
    ADMIN,
    NEW,
    DISABLE,
    CANCELED,
    SENT,
    TEST,
    ACTIVE,
    SENDING,
    EDIT,
    SPECIAL			// Special handling (no sent regularily, send mode depends on called method)
    ;

    public String getNameLowerCase() {
        return this.name().toLowerCase();
    }

    public boolean isSent() {
        return this == SENT;
    }

    public boolean isScheduled() {
        return this == SCHEDULED;
    }

    public boolean isCanBeActivated() {
        return this == NEW || this == CANCELED;
    }

    public boolean isCanBeCanceled() {
        return this == SCHEDULED;
    }
}
