/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc.impl;

import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.mvc.Popups;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@JsonSerialize(using = PopupsJsonSerializer.class)
public class PopupsImpl implements Popups {
    private static final long serialVersionUID = -3930763245124971574L;
    
	public static final String POPUPS_MESSAGES_KEY = "emm.popups.messages";
    public static final String FIELDS_MESSAGES_KEY = "emm.popups.fields_messages";

    public static PopupsImpl get(Map<String, Object> map) {
        List<MessagePopup> messages = (List<MessagePopup>) map.get(POPUPS_MESSAGES_KEY);
        List<FieldMessage> fieldMessages = (List<FieldMessage>) map.get(FIELDS_MESSAGES_KEY);

        if (messages == null || fieldMessages == null) {
            return null;
        }

        return new PopupsImpl(messages, fieldMessages);
    }

    public static void put(Map<String, Object> map, PopupsImpl popups) {
        if (popups == null) {
            map.remove(POPUPS_MESSAGES_KEY);
            map.remove(FIELDS_MESSAGES_KEY);
        } else {
            map.put(POPUPS_MESSAGES_KEY, popups.getPopupsMessages());
            map.put(FIELDS_MESSAGES_KEY, popups.getFieldsMessages());
        }
    }

    private List<MessagePopup> popupsMessages = new ArrayList<>();
    private List<FieldMessage> fieldsMessages = new ArrayList<>();

    public PopupsImpl() {

    }

    public enum MessageType {
        ERROR, WARNING, SUCCESS, INFO
    }

    public static class MessagePopup {
        private final MessageType type;
        private final Message message;

        public MessagePopup(MessageType type, Message message) {
            this.type = type;
            this.message = message;
        }

        public MessageType getType() {
            return type;
        }

        public Message getMessage() {
            return message;
        }
    }

    public static class FieldMessage {

        private final String name;
        private final Message message;
        private final MessageType type;

        public FieldMessage(String name, Message message, MessageType type) {
            this.name = name;
            this.message = message;
            this.type = type;
        }

        public String getName() {
            return name;
        }

        public Message getMessage() {
            return message;
        }

        public MessageType getType() {
            return type;
        }
    }

    private PopupsImpl(List<MessagePopup> popupsMessages, List<FieldMessage> fieldMessages) {
        this.popupsMessages = popupsMessages;
        this.fieldsMessages = fieldMessages;
    }

    @Override
    public Popups alert(Message popup) {
        popupsMessages.add(new MessagePopup(MessageType.ERROR, popup));
        return this;
    }

    @Override
    public Popups alert(String code, Object... arguments) {
        return alert(Message.of(code, arguments));
    }

    @Override
    public Popups exactAlert(String text) {
        return alert(new Message(text, false));
    }

    @Override
    public Popups warning(Message popup) {
        popupsMessages.add(new MessagePopup(MessageType.WARNING, popup));
        return this;
    }

    @Override
    public Popups info(Message popup) {
        popupsMessages.add(new MessagePopup(MessageType.INFO, popup));
        return this;
    }

    @Override
    public Popups info(String code, Object... arguments) {
        return info(Message.of(code, arguments));
    }

    @Override
    public Popups warning(String code, Object... arguments) {
        return warning(Message.of(code, arguments));
    }

    @Override
    public Popups exactWarning(String text) {
        return warning(new Message(text, false));
    }

    @Override
    public Popups success(Message popup) {
        popupsMessages.add(new MessagePopup(MessageType.SUCCESS, popup));
        return this;
    }

    @Override
    public Popups success(String code, Object... arguments) {
        return success(Message.of(code, arguments));
    }

    @Override
    public Popups exactSuccess(String text) {
        return success(new Message(text, false));
    }

    @Override
    public Popups addPopups(ServiceResult<?> serviceResult) {
        serviceResult.getSuccessMessages().forEach(this::success);
        serviceResult.getWarningMessages().forEach(this::warning);
        serviceResult.getErrorMessages().forEach(this::alert);
        serviceResult.getInfoMessages().forEach(this::info);
        return this;
    }

    @Override
    public Popups field(String field, Message popup) {
        return alert(popup);
    }

    @Override
    public Popups field(String field, String code, Object... arguments) {
        return field(field, Message.of(code, arguments));
    }

    @Override
    public Popups fieldError(String field, Message popup) {
        fieldsMessages.add(new FieldMessage(field, popup, MessageType.ERROR));
        return this;
    }

    @Override
    public Popups fieldError(String field, String code, Object... arguments) {
        return fieldError(field, Message.of(code, arguments));
    }

    @Override
    public Popups exactField(String field, String text) {
        return field(field, new Message(text, false));
    }

    @Override
    public int size() {
        return popupsMessages.size() + fieldsMessages.size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean hasAlertPopups() {
        return hasPopups(MessageType.ERROR) || hasFieldMessages(MessageType.ERROR);
    }

    @Override
    public boolean hasWarningPopups() {
        return hasPopups(MessageType.WARNING);
    }

    @Override
    public boolean hasSuccessPopups() {
        return hasPopups(MessageType.SUCCESS);
    }

    private boolean hasPopups(MessageType type) {
        return popupsMessages.stream().anyMatch(m -> type.equals(m.getType()));
    }

    private boolean hasFieldMessages(MessageType type) {
        return fieldsMessages.stream().anyMatch(fm -> type.equals(fm.getType()));
    }

    @Override
    public void clear() {
        popupsMessages.clear();
        fieldsMessages.clear();
    }

    public List<MessagePopup> getPopupsMessages() {
        return popupsMessages;
    }

    public List<FieldMessage> getFieldsMessages() {
        return fieldsMessages;
    }

}
