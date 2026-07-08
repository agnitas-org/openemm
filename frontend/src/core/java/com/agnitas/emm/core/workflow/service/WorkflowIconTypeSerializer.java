/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.workflow.service;

import java.io.IOException;

import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class WorkflowIconTypeSerializer extends JsonSerializer<Integer> {
    @Override
    public void serialize(Integer value, JsonGenerator generator, SerializerProvider provider) throws IOException {
        WorkflowIconType type = getType(value);
        generator.writeString(type == null ? "0" : type.getName());
    }

    private WorkflowIconType getType(Integer value) {
        return value == null ? null : WorkflowIconType.fromId(value);
    }
}
