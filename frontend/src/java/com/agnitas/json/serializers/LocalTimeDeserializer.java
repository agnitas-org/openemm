/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.serializers;

import java.io.IOException;
import java.time.DateTimeException;
import java.time.LocalTime;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;

public class LocalTimeDeserializer extends LocalDateTimeBaseDeserializer<LocalTime> {
    private static final long serialVersionUID = -2219352205564138533L;

    public static final LocalTimeDeserializer instance = new LocalTimeDeserializer(LocalTime.class);

    private LocalTimeDeserializer(Class<LocalTime> type) {
        super(type);
    }

    @Override
    protected LocalTime deserializeOne(JsonParser parser, DeserializationContext context) throws DateTimeException, IOException {
        return LocalTime.of(
            getInt(parser, context),
            getInt(parser, context, 0),
            getInt(parser, context,0),
            getInt(parser, context, 0)
        );
    }
}
