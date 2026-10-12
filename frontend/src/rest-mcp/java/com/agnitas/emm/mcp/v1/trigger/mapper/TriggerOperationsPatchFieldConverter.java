/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.trigger.mapper;

import java.util.List;

import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.action.operations.ActionOperationParametersParser;
import com.agnitas.emm.core.commons.dto.PatchField;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.stereotype.Component;

@Component
class TriggerOperationsPatchFieldConverter {

    private final ActionOperationParametersParser operationsParser;

    TriggerOperationsPatchFieldConverter(ActionOperationParametersParser operationsParser) {
        this.operationsParser = operationsParser;
    }

    PatchField<List<AbstractActionOperationParameters>> toPatchField(JsonNullable<String> operations) {
        if (operations == null || operations.isUndefined()) {
            return PatchField.absent();
        }
        String json = operations.orElse(null);
        List<AbstractActionOperationParameters> parsed = json != null
                ? operationsParser.deSerializeActionModulesList(json)
                : null;
        return PatchField.of(parsed);
    }

}
