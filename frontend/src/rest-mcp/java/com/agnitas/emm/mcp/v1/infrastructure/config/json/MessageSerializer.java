/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.config.json;

import java.util.Locale;

import com.agnitas.messages.Message;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

public class MessageSerializer extends ValueSerializer<Message> {

    @Override
    public Class<Message> handledType() {
        return Message.class;
    }

    @Override
    public void serialize(Message value, JsonGenerator gen, SerializationContext ctxt) throws JacksonException {
        gen.writeString(value.resolve(Locale.ENGLISH));
    }
}
