/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.userform.service;

import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.userform.dto.ResultSettings;
import com.agnitas.emm.core.userform.validation.FormConstraints;
import com.agnitas.emm.core.validator.EmmFormValidationField;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

public record PatchFormCommand(
        PatchField<String> name,
        PatchField<String> description,
        PatchField<Integer> startTriggerId,
        PatchField<Integer> endTriggerId,
        PatchField<ResultSettings> successSettings,
        PatchField<ResultSettings> errorSettings
) {
    public PatchFormCommand {
        name.check(PatchFormCommand::isValidName, EmmFormValidationField.NAME);
        startTriggerId.check(id -> id == null || id >= 0, EmmFormValidationField.START_TRIGGER);
        endTriggerId.check(id -> id == null || id >= 0, EmmFormValidationField.END_TRIGGER);
        successSettings.check(this::isValidResultSettings, EmmFormValidationField.SUCCESS_SETTINGS);
        errorSettings.check(this::isValidResultSettings, EmmFormValidationField.ERROR_SETTINGS);
    }

    private static boolean isValidName(String n) {
        return n != null
               && StringUtils.length(n) >= FormConstraints.NAME_MIN_LENGTH
               && StringUtils.length(n) <= FormConstraints.NAME_MAX_LENGTH
               && n.matches(FormConstraints.NAME_PATTERN);
    }

    private boolean isValidResultSettings(ResultSettings settings) {
        if (settings == null) {
            return false;
        }
        if (settings.isUseUrl()) {
            return AgnUtils.isValidUrl(settings.getUrl());
        }
        return StringUtils.isNotBlank(settings.getTemplate());
    }
}
