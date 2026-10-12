/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.action.dto;

import java.util.List;
import java.util.Objects;

import com.agnitas.emm.core.action.bean.EmmActionType;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.validator.TriggerValidationField;
import org.apache.commons.lang3.StringUtils;

public record PatchTriggerCommand(
        int id,
        PatchField<String> name,
        PatchField<String> description,
        PatchField<EmmActionType> type,
        PatchField<Boolean> isActive,
        PatchField<Boolean> isAdvertising,
        PatchField<List<AbstractActionOperationParameters>> operations
) {
    public PatchTriggerCommand {
        name.check(StringUtils::isNotBlank, TriggerValidationField.NAME);
        type.check(Objects::nonNull, TriggerValidationField.TYPE);
    }
}
