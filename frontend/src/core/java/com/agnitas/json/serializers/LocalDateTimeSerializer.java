/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.serializers;

import java.io.IOException;
import java.time.LocalDateTime;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

public class LocalDateTimeSerializer extends StdSerializer<LocalDateTime> {
    private static final long serialVersionUID = 2235478934823459213L;

    public static final LocalDateTimeSerializer instance = new LocalDateTimeSerializer(LocalDateTime.class);

    private LocalDateTimeSerializer(Class<LocalDateTime> type) {
        super(type);
    }

    @Override
    public void serialize(LocalDateTime value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        if (value == null) {
            generator.writeNull();
        } else {
            generator.writeStartArray();
            generator.writeNumber(value.getYear());
            generator.writeNumber(value.getMonthValue());
            generator.writeNumber(value.getDayOfMonth());
            generator.writeNumber(value.getHour());
            generator.writeNumber(value.getMinute());
            generator.writeNumber(value.getSecond());
            generator.writeNumber(value.getNano());
            generator.writeEndArray();
        }
    }
}
