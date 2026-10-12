/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.admin.validation;

import java.util.Set;
import java.util.TimeZone;

public class UiUserConstraints {

    public static final Set<String> AVAILABLE_TIMEZONES = Set.of(TimeZone.getAvailableIDs());
    public static final int USERNAME_MIN_LENGTH = 3;
    public static final int USERNAME_MAX_LENGTH = 180;
    public static final int COMPANY_MIN_LENGTH = 2;
    public static final int COMPANY_MAX_LENGTH = 100;
    public static final int FIRSTNAME_MAX_LENGTH = 100;
    public static final int LASTNAME_MAX_LENGTH = 100;
    public static final String LANGUAGE_REGEX = "^[a-z]{2}_[A-Z]{2}$";
    public static final String LANGUAGE_MESSAGE = "Language must be in the format 'lang_COUNTRY' (e.g., 'en_US')";
    public static final String ALTG_MESSAGE =
            "Specify either accessLimitingTargetGroupIds or accessLimitingTargetGroupId, but not both";


    private UiUserConstraints() {
    }
}
