/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.agnitas.util.GuiConstants;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import com.agnitas.messages.Message;
import com.agnitas.service.ServiceResult;
import com.agnitas.web.mvc.Popups;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize(using = PopupsJsonSerializer.class)
public class StrutsPopups implements Popups {
    private static final long serialVersionUID = 4217085150990004328L;

    public static final String MESSAGES_KEY = "org.apache.struts.action.ACTION_MESSAGE";
    public static final String ERRORS_KEY = "org.apache.struts.action.ERROR";
    public static final String FIELDS_ERRORS_KEY = "POPUPS_FIELDS_ERRORS";

    public static class FieldError {

        private final String fieldName;
        private final Message message;

        public FieldError(String fieldName, Message message) {
            this.fieldName = fieldName;
            this.message = message;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Message getMessage() {
            return message;
        }

        public String getArgumentsStr() {
            return Arrays.stream(message.getArguments())
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
        }
    }

    private static StrutsPopups from(ActionMessages messages, ActionErrors errors, List<FieldError> fieldsErrors) {
        if (messages == null && errors == null && fieldsErrors == null) {
            return null;
        }

        return new StrutsPopups(messages, errors, fieldsErrors);
    }

    public static StrutsPopups get(Map<String, Object> map) {
        ActionMessages messages = (ActionMessages) map.get(MESSAGES_KEY);
        ActionErrors errors = (ActionErrors) map.get(ERRORS_KEY);
        List<FieldError> fieldsErrors = (List<FieldError>) map.get(FIELDS_ERRORS_KEY);

        return from(messages, errors, fieldsErrors);
    }

    public static void put(Map<String, Object> map, StrutsPopups popups) {
        if (popups == null) {
            map.remove(MESSAGES_KEY);
            map.remove(ERRORS_KEY);
            map.remove(FIELDS_ERRORS_KEY);
        } else {
            map.put(MESSAGES_KEY, popups.getMessages());
            map.put(ERRORS_KEY, popups.getErrors());
            map.put(FIELDS_ERRORS_KEY, popups.getFieldsErrors());
        }
    }

    public static void insertMessagesToPopups(final ActionMessages warnings, final ActionMessages errors, final Popups popups) {
        @SuppressWarnings("unchecked")
        final Iterator<ActionMessage> warIt = warnings == null || warnings.isEmpty() ? Collections.emptyIterator() : warnings.get();
        warIt.forEachRemaining(el -> {
            if (el.isResource()) {
                popups.warning(el.getKey(), el.getValues());
            } else {
                popups.exactWarning(el.getKey());
            }
        });

        @SuppressWarnings("unchecked")
        final Iterator<ActionMessage> errIt = errors == null || errors.isEmpty() ? Collections.emptyIterator() : errors.get();
        errIt.forEachRemaining(el -> {
            if (el.isResource()) {
                popups.alert(el.getKey(), el.getValues());
            } else {
                popups.exactAlert(el.getKey());
            }
        });
    }

    private final ActionMessages messages;
    private final ActionErrors errors;
    private final List<FieldError> fieldsErrors;

    public StrutsPopups() {
        messages = new ActionMessages();
        errors = new ActionErrors();
        fieldsErrors = new ArrayList<>();
    }

    private StrutsPopups(ActionMessages messages, ActionErrors errors, List<FieldError> fieldsErrors) {
        if (messages == null) {
            messages = new ActionMessages();
        }

        if (errors == null) {
            errors = new ActionErrors();
        }

        if (fieldsErrors == null) {
            fieldsErrors = new ArrayList<>();
        }

        this.messages = messages;
        this.errors = errors;
        this.fieldsErrors = fieldsErrors;
    }

    @Override
    public Popups alert(Message popup) {
        errors.add(ActionErrors.GLOBAL_MESSAGE, popup.toStrutsMessage());
        return this;
    }

    @Override
    public Popups alert(String code, Object ...arguments) {
        return alert(new Message(code, arguments));
    }

    @Override
    public Popups exactAlert(String text) {
        return alert(new Message(text, false));
    }

    @Override
    public Popups warning(Message popup) {
        messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING, popup.toStrutsMessage());
        return this;
    }
    
    @Override
    public Popups permanentWarning(String code, Object ...arguments) {
        return permanentWarning(new Message(code, arguments));
    }
    
    @Override
    public Popups permanentWarning(Message popup) {
        messages.add(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING_PERMANENT, popup.toStrutsMessage());
        return this;
    }

    @Override
    public Popups warning(String code, Object ...arguments) {
        return warning(new Message(code, arguments));
    }

    @Override
    public Popups exactWarning(String text) {
        return warning(new Message(text, false));
    }

    @Override
    public Popups success(Message popup) {
        messages.add(ActionMessages.GLOBAL_MESSAGE, popup.toStrutsMessage());
        return this;
    }

    @Override
    public Popups success(String code, Object ...arguments) {
        return success(new Message(code, arguments));
    }

    @Override
    public Popups exactSuccess(String text) {
        return success(new Message(text, false));
    }

    @Override
    public Popups addPopups(final ServiceResult<?> serviceResult) {
        serviceResult.getSuccessMessages().forEach(this::success);
        serviceResult.getWarningMessages().forEach(this::warning);
        serviceResult.getErrorMessages().forEach(this::alert);
        return this;
    }

    @Override
    public Popups fieldError(String field, Message popup) {
        fieldsErrors.add(new FieldError(field, popup));
        return this;
    }

    @Override
    public Popups fieldError(String field, String code, Object... arguments) {
        return fieldError(field, new Message(code, arguments));
    }

    @Override
    public Popups field(String field, Message popup) {
        errors.add(field, popup.toStrutsMessage());
        return this;
    }

    @Override
    public Popups field(String field, String code, Object... arguments) {
        return field(field, new Message(code, arguments));
    }

    @Override
    public Popups exactField(String field, String text) {
        return field(field, new Message(text, false));
    }

    @Override
    public int size() {
        return errors.size() + messages.size();
    }

    @Override
    public boolean isEmpty() {
        return errors.isEmpty() && messages.isEmpty();
    }

    @Override
    public boolean hasAlertPopups() {
        return !errors.isEmpty() || !fieldsErrors.isEmpty();
    }

    @Override
    public boolean hasWarningPopups() {
        return messages.size(GuiConstants.ACTIONMESSAGE_CONTAINER_WARNING) > 0;
    }

    @Override
    public boolean hasSuccessPopups() {
        return messages.size(ActionMessages.GLOBAL_MESSAGE) > 0;
    }

    @Override
    public boolean hasFieldPopups() {
        return errors.size() > errors.size(ActionErrors.GLOBAL_MESSAGE);
    }

    @Override
    public boolean hasFieldPopups(String field) {
        return errors.size(field) > 0;
    }

    @Override
    public void clear() {
        errors.clear();
        messages.clear();
        fieldsErrors.clear();
    }

    public ActionMessages getMessages() {
        return messages;
    }

    public ActionErrors getErrors() {
        return errors;
    }

    public List<FieldError> getFieldsErrors() {
        return fieldsErrors;
    }
}
