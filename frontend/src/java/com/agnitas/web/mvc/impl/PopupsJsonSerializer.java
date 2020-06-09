/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.agnitas.util.GuiConstants;
import org.apache.commons.lang3.StringUtils;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.messages.Message;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

@SuppressWarnings("unchecked")
public class PopupsJsonSerializer extends JsonSerializer<StrutsPopups> {
    @Override
    public void serialize(StrutsPopups popups, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeObject(adopt(popups.getMessages(), popups.getErrors()));
    }

    private PopupsDto adopt(ActionMessages messages, ActionErrors errors) {
        PopupsDto popups = new PopupsDto();

        popups.setSuccess(adopt(messages, ActionMessages.GLOBAL_MESSAGE));
        popups.setWarning(adopt(messages, GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING));
        popups.setAlert(adopt(errors, ActionMessages.GLOBAL_MESSAGE));
        popups.setFields(adoptFieldErrors(errors));

        return popups;
    }

    private List<Message> adopt(ActionMessages messages, String property) {
        Iterator<ActionMessage> iterator = messages.get(property);
        List<Message> popups = new ArrayList<>();

        while (iterator.hasNext()) {
            popups.add(adopt(iterator.next()));
        }

        return popups;
    }

    private Map<String, List<Message>> adoptFieldErrors(ActionErrors errors) {
        Map<String, List<Message>> map = new HashMap<>();

        Iterator<String> iterator = errors.properties();
        
        while (iterator.hasNext()) {
            String property = iterator.next();

            if (StringUtils.equals(property, ActionMessages.GLOBAL_MESSAGE) || map.containsKey(property)) {
                continue;
            }

            map.put(property, adopt(errors, property));
        }

        return map;
    }

    private Message adopt(ActionMessage message) {
        if (message.isResource()) {
            return new Message(message.getKey(), message.getValues());
        } else {
            return new Message(message.getKey(), false);
        }
    }

    public static class PopupsDto {
        private List<Message> success;
        private List<Message> warning;
        private List<Message> alert;
        private Map<String, List<Message>> fields;

        public List<Message> getSuccess() {
            return success;
        }

        public void setSuccess(List<Message> success) {
            this.success = success;
        }

        public List<Message> getWarning() {
            return warning;
        }

        public void setWarning(List<Message> warning) {
            this.warning = warning;
        }

        public List<Message> getAlert() {
            return alert;
        }

        public void setAlert(List<Message> alert) {
            this.alert = alert;
        }

        public Map<String, List<Message>> getFields() {
            return fields;
        }

        public void setFields(Map<String, List<Message>> fields) {
            this.fields = fields;
        }
    }
}
