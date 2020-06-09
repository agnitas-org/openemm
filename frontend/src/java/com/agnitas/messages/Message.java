/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.messages;

import java.util.Arrays;
import java.util.Objects;

import com.agnitas.json.serializers.MessageSerializer;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import org.agnitas.service.UserMessageException;

@JsonSerialize(using = MessageSerializer.class)
public class Message {
    private String code;
    private boolean resolvable;
    private Object[] arguments;

    public static Message of(String code, Object... arguments) {
        return new Message(code, arguments);
    }

    public static Message of(UserMessageException exception) {
        return of(exception.getErrorMessageKey(), exception.getAdditionalErrorData());
    }

    public static Message exact(String text) {
        return new Message(text, false);
    }

    public Message(String code) {
        this(code, true, null);
    }

    public Message(String code, Object[] arguments) {
        this(code, true, arguments);
    }

    public Message(String code, boolean resolvable) {
        this(code, resolvable, null);
    }

    private Message(String code, boolean resolvable, Object[] arguments) {
        Objects.requireNonNull(code);

        this.code = code;
        this.resolvable = resolvable;
        this.arguments = arguments;
    }

    public String getCode() {
        return code;
    }

    public boolean isResolvable() {
        return resolvable;
    }

    public Object[] getArguments() {
        return arguments;
    }

    @Override
    public boolean equals(Object o) {
        if (o instanceof Message) {
            if (this == o) {
                return true;
            }

            Message rhs = (Message) o;

            return resolvable == rhs.isResolvable()
                    && code.equals(rhs.getCode())
                    && Arrays.equals(arguments, rhs.getArguments());
        }

        return false;
    }

    @Override
    public int hashCode() {
        return code.hashCode();
    }

}
