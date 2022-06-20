/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

import java.net.URL;
import java.util.Date;

public interface ComRecipientReaction {

    enum ReactionType {
        OPEN("recipient.reaction.open"),
        CLICK("recipient.reaction.click"),
        PURCHASE("recipient.reaction.purchase");

        private String messageKey;

        ReactionType(String messageKey) {
            this.messageKey = messageKey;
        }

        public static ReactionType getById(int id) {
            ReactionType[] enums = ReactionType.values();
            if (id < 0 || id >= enums.length) {
                throw new IllegalArgumentException("There is no ReactionType having an id " + id);
            }
            return enums[id];
        }

        public int getId() {
            return ordinal();
        }

        public String getMessageKey() {
            return messageKey;
        }
    }

    Date getTimestamp();

    void setTimestamp(Date timestamp);

    int getMailingId();

    void setMailingId(int mailingId);

    String getMailingName();

    void setMailingName(String mailingName);

    ReactionType getReactionType();

    void setReactionType(ReactionType reactionType);

    String getDeviceClass();

    void setDeviceClass(String deviceClass);

    String getDeviceName();

    void setDeviceName(String deviceName);

    URL getClickedUrl();

    void setClickedUrl(URL clickedUrl);
}
