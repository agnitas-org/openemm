/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.security.sessionbinding.common;

public final class SessionBindingConstants {

    private SessionBindingConstants() {
        // Just to avoid instantiation
    }

    public static final String HTTP_SESSION_BINDING_DATA_ATTRIBUTE = "com.agnitas.emm.security.sessionbinding.SessionBinding";

    public static final String SKIP_COOKIE_CHECK_ON_FIRST_REQUEST = "sessionBinding.SkipCookieCheckOnFirstRequest";

    public static final String SESSION_BINDING_COOKIE_NAME_PREFIX = "com.agnitas.emm.sb";

    public static final int SESSION_IDLE_GRACE_SECONDS = 36000; // 10 minutes

}
