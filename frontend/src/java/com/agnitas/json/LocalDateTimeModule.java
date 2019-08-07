/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.json;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.agnitas.json.serializers.LocalDateDeserializer;
import com.agnitas.json.serializers.LocalDateSerializer;
import com.agnitas.json.serializers.LocalDateTimeDeserializer;
import com.agnitas.json.serializers.LocalDateTimeSerializer;
import com.agnitas.json.serializers.LocalTimeDeserializer;
import com.agnitas.json.serializers.LocalTimeSerializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

public final class LocalDateTimeModule extends SimpleModule {
    private static final long serialVersionUID = -4939529235615670900L;

    public LocalDateTimeModule() {
        addSerializer(LocalDateTime.class, LocalDateTimeSerializer.instance);
        addSerializer(LocalDate.class, LocalDateSerializer.instance);
        addSerializer(LocalTime.class, LocalTimeSerializer.instance);
        addDeserializer(LocalDateTime.class, LocalDateTimeDeserializer.instance);
        addDeserializer(LocalDate.class, LocalDateDeserializer.instance);
        addDeserializer(LocalTime.class, LocalTimeDeserializer.instance);
    }
}
