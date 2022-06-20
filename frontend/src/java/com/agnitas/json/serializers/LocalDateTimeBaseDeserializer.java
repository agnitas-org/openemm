/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json.serializers;

import java.io.IOException;
import java.time.DateTimeException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public abstract class LocalDateTimeBaseDeserializer<T> extends StdDeserializer<T> {
    private static final long serialVersionUID = -8084736780445989989L;

    public LocalDateTimeBaseDeserializer(Class<T> type) {
        super(type);
    }

    @Override
    public T deserialize(JsonParser parser, DeserializationContext context) throws IOException {
        JsonToken token = parser.getCurrentToken();

        if (token == JsonToken.VALUE_NULL) {
            return null;
        } else if (token == JsonToken.START_ARRAY) {
            T value;

            parser.nextToken();
            try {
                value = deserializeOne(parser, context);
            } catch (DateTimeException e) {
                throw context.instantiationException(_valueClass, e);
            }

            if (!parser.hasToken(JsonToken.END_ARRAY)) {
                throw context.wrongTokenException(parser, _valueClass, JsonToken.END_ARRAY, "");
            }

            return value;
        } else {
            throw context.wrongTokenException(parser, _valueClass, JsonToken.START_ARRAY, "");
        }
    }

    protected abstract T deserializeOne(JsonParser parser, DeserializationContext context) throws DateTimeException, IOException;

    protected int getInt(JsonParser parser, DeserializationContext context) throws IOException {
        return getInt(parser, context, 0, true);
    }

    protected int getInt(JsonParser parser, DeserializationContext context, int defaultValue) throws IOException {
        return getInt(parser, context, defaultValue, false);
    }

    private int getInt(JsonParser parser, DeserializationContext context, int defaultValue, boolean required) throws IOException {
        if (parser.hasToken(JsonToken.VALUE_NUMBER_INT)) {
            int value = parser.getValueAsInt();
            parser.nextToken();
            return value;
        } else {
            if (required) {
                throw context.wrongTokenException(parser, _valueClass, JsonToken.VALUE_NUMBER_INT, "");
            } else {
                return defaultValue;
            }
        }
    }
}
