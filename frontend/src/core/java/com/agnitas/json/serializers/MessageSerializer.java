/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.serializers;

import java.io.IOException;

import org.springframework.context.i18n.LocaleContextHolder;

import com.agnitas.messages.I18nString;
import com.agnitas.messages.Message;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class MessageSerializer extends JsonSerializer<Message> {
    @Override
    public void serialize(Message message, JsonGenerator generator, SerializerProvider provider) throws IOException {
        generator.writeString(resolve(message));
    }

    private String resolve(Message message) {
        if (message.isResolvable()) {
            return I18nString.getLocaleString(message.getCode(), LocaleContextHolder.getLocale(), message.getArguments());
        } else {
            return message.getCode();
        }
    }
}
