/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.components.entity;

import com.agnitas.beans.IntEnum;

public enum TestRunOption implements IntEnum {
    NO_LIMITATION(0, "mailing.send.adminOrTest"),
    TARGET(1, "Target"),
    RECIPIENT(2, "mailing.test.recipient.single"),
    SEND_TO_SELF(3, "GWUA.send.to.me");

    private final int id;
    private final String messageKey;

    TestRunOption(int id, String messageKey) {
        this.id = id;
        this.messageKey = messageKey;
    }

    @Override
    public int getId() {
        return id;
    }

    public String getMessageKey() {
        return messageKey;
    }

    public static TestRunOption fromId(int id) {
        return IntEnum.fromId(TestRunOption.class, id);
    }
}
