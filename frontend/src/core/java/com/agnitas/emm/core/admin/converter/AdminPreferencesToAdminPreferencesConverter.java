/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.admin.converter;

import com.agnitas.beans.AdminPreferences;
import com.agnitas.beans.impl.AdminPreferencesImpl;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

@Component
public final class AdminPreferencesToAdminPreferencesConverter implements Converter<AdminPreferences, AdminPreferences> {

    @Override
    public AdminPreferences convert(final AdminPreferences preferences) {
        final AdminPreferences adminPreferences = new AdminPreferencesImpl();
        adminPreferences.setMailingContentView(preferences.getMailingContentView());
        adminPreferences.setDashboardMailingsView(preferences.getDashboardMailingsView());
        adminPreferences.setMailingSettingsView(preferences.getMailingSettingsView());
        adminPreferences.setLivePreviewPosition(preferences.getLivePreviewPosition());
        adminPreferences.setStatisticLoadType(preferences.getStatisticLoadType());
        adminPreferences.setMailingSettingsViewType(preferences.getMailingSettingsViewType());
        return adminPreferences;
    }
}
