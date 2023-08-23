/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.bean;

import com.agnitas.beans.IntEnum;
import org.agnitas.dao.UserStatus;

public enum ActionSendMailingToUserStatus implements IntEnum {

    WAITING_FOR_CONFIRM(0, "action.mailing.recipients.5", UserStatus.WaitForConfirm),
    WAITING_FOR_CONFIRM_AND_ACTIVE(1, "action.mailing.recipients.active.5", UserStatus.WaitForConfirm, UserStatus.Active),
    ACTIVE(2, "statistic.recipient.active", UserStatus.Active);

    private final int id;
    private final UserStatus[] statuses;
    private final String messageKey;

    ActionSendMailingToUserStatus(int id, String messageKey, UserStatus... statuses) {
        this.id = id;
        this.messageKey = messageKey;
        this.statuses = statuses;
    }

    @Override
    public int getId() {
        return id;
    }

    public UserStatus[] getStatuses() {
        return statuses;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
