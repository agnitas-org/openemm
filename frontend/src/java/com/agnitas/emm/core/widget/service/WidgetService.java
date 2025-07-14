/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.widget.service;

import com.agnitas.emm.core.widget.beans.WidgetSettingsBase;
import com.agnitas.emm.core.widget.enums.WidgetType;
import com.fasterxml.jackson.core.JsonProcessingException;

public interface WidgetService {

    String generateToken(WidgetType widgetType, WidgetSettingsBase settings, int companyId) throws Exception;

    boolean isTokenValid(String token);

    boolean isTokenValid(String token, WidgetType widgetType);

    WidgetType getWidgetType(String token);

    <T extends WidgetSettingsBase> T parseSettings(String token, Class<T> type) throws JsonProcessingException;

}
