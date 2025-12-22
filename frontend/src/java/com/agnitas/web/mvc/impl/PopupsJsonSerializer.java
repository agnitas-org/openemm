/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc.impl;

import com.agnitas.messages.Message;
import com.agnitas.messages.entity.MessagePopup;
import com.agnitas.messages.enums.MessageType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import org.apache.commons.collections4.CollectionUtils;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PopupsJsonSerializer extends JsonSerializer<PopupsImpl> {
    @Override
    public void serialize(PopupsImpl popups, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeObject(adopt(popups));
    }

    private PopupsDto adopt(PopupsImpl source) {
        List<MessagePopup> popupsMessages = source.getPopupsMessages();

        PopupsDto popups = new PopupsDto();

        popups.setSuccess(getMessages(popupsMessages, MessageType.SUCCESS));
        popups.setAlert(getMessages(popupsMessages, MessageType.ERROR));
        popups.setWarning(getMessages(popupsMessages, MessageType.WARNING));
        popups.setInfo(getMessages(popupsMessages, MessageType.INFO));
        popups.setFields(source.getFieldsMessages());

        return popups;
    }

    private List<Message> getMessages(List<MessagePopup> messages, MessageType type) {
        if (CollectionUtils.isEmpty(messages)) {
            return Collections.emptyList();
        }

        return messages.stream()
                .filter(m -> type.equals(m.getType()))
                .map(MessagePopup::getMessage)
                .collect(Collectors.toList());
    }

    public static class PopupsDto {

        private List<Message> success;
        private List<Message> warning;
        private List<Message> alert;
        private List<Message> info;
        private List<PopupsImpl.FieldMessage> fields;

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

        public List<Message> getInfo() {
            return info;
        }

        public void setInfo(List<Message> info) {
            this.info = info;
        }

        public List<PopupsImpl.FieldMessage> getFields() {
            return fields;
        }

        public void setFields(List<PopupsImpl.FieldMessage> fields) {
            this.fields = fields;
        }
    }
}
