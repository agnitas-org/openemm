/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.serializers;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalDate;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

public class LocalDateDeserializer extends LocalDateTimeBaseDeserializer<LocalDate> {
    private static final long serialVersionUID = -8588965014282549523L;

    public static final LocalDateDeserializer instance = new LocalDateDeserializer(LocalDate.class);

    private LocalDateDeserializer(Class<LocalDate> type) {
        super(type);
    }

    @Override
    protected LocalDate deserializeOne(JsonParser parser, DeserializationContext context) throws DateTimeException, IOException {
        return LocalDate.of(getInt(parser, context), getInt(parser, context), getInt(parser, context));
    }
}
