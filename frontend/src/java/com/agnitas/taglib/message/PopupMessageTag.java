/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.taglib.message;

import java.util.Collections;
import java.util.List;

import com.agnitas.messages.Message;
import com.agnitas.web.mvc.impl.PopupsImpl;
import jakarta.servlet.jsp.PageContext;
import org.apache.commons.collections4.CollectionUtils;

public interface PopupMessageTag {

    default List<Message> getPopupsMessages(PageContext pageContext, String type) {
        List<PopupsImpl.MessagePopup> messages = (List<PopupsImpl.MessagePopup>) pageContext.findAttribute(PopupsImpl.POPUPS_MESSAGES_KEY);

        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }

        PopupsImpl.MessageType messageType = PopupsImpl.MessageType.valueOf(type);
        return messages.stream()
                .filter(m -> messageType.equals(m.getType()))
                .map(PopupsImpl.MessagePopup::getMessage)
                .toList();
    }
}
