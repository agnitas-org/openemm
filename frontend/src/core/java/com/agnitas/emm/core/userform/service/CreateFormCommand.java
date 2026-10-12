/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.userform.service;

import com.agnitas.emm.core.userform.dto.ResultSettings;
import com.agnitas.emm.core.userform.validation.FormConstraints;
import com.agnitas.emm.core.validator.EmmFormValidationField;
import com.agnitas.emm.core.validator.ValidationException;
import com.agnitas.emm.core.validator.ValidationField;
import com.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;

public record CreateFormCommand(
        String name,
        String description,
        int startTriggerId,
        int endTriggerId,
        ResultSettings successSettings,
        ResultSettings errorSettings
) {
    public CreateFormCommand {
        description = description == null ? "" : description;
        if (
                name == null
                || StringUtils.length(name) < FormConstraints.NAME_MIN_LENGTH
                || StringUtils.length(name) > FormConstraints.NAME_MAX_LENGTH
                || !name.matches(FormConstraints.NAME_PATTERN)) {
            throw new ValidationException("error.form.invalid_name");
        }
        if (startTriggerId < 0) {
            throw ValidationException.invalidValue(EmmFormValidationField.START_TRIGGER);
        }
        if (endTriggerId < 0) {
            throw ValidationException.invalidValue(EmmFormValidationField.END_TRIGGER);
        }
        validateSuccessSettings(EmmFormValidationField.SUCCESS_SETTINGS, successSettings);
        validateSuccessSettings(EmmFormValidationField.ERROR_SETTINGS, errorSettings);
    }

    private void validateSuccessSettings(ValidationField field, ResultSettings settings) {
        if (settings == null) {
            throw ValidationException.missing(field);
        }
        if (settings.isUseUrl() && !AgnUtils.isValidUrl(settings.getUrl())) {
            throw new ValidationException("error.userform." + (settings.isSuccess() ? "success" : "error") + ".url.missing");
        }
        if (!settings.isUseUrl() && StringUtils.isBlank(settings.getTemplate())) {
            throw new ValidationException("error.userform." + (settings.isSuccess() ? "success" : "error") + ".html.missing");
        }
    }
}
