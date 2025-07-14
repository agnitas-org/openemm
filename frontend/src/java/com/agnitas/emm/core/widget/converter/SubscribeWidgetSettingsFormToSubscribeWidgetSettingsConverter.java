/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.widget.converter;

import com.agnitas.emm.core.widget.beans.SubscribeWidgetSettings;
import com.agnitas.emm.core.widget.form.SubscribeWidgetSettingsForm;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public class SubscribeWidgetSettingsFormToSubscribeWidgetSettingsConverter implements Converter<SubscribeWidgetSettingsForm, SubscribeWidgetSettings> {

    @Override
    public SubscribeWidgetSettings convert(SubscribeWidgetSettingsForm source) {
        SubscribeWidgetSettings settings = new SubscribeWidgetSettings();

        settings.setMailinglistId(source.mailinglistId());
        settings.setDoiMailingId(source.doiMailingId());
        settings.setSuccessMessage(source.successMessage());
        settings.setErrorMessage(source.errorMessage());

        return settings;
    }

}
