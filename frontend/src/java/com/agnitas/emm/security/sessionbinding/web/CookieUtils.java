/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.security.sessionbinding.web;

import com.agnitas.emm.security.sessionbinding.common.SessionBindingConstants;
import com.agnitas.emm.security.sessionbinding.common.SessionBindingData;
import jakarta.servlet.http.Cookie;

public class CookieUtils {

    public static final Cookie createSessionBindingCookie(final SessionBindingData sessionBindingData, final int maxAge) {
        final Cookie cookie = new Cookie(createCookieName(sessionBindingData), sessionBindingData.getSecurityToken());
        cookie.setMaxAge(maxAge);
        cookie.setSecure(false);
        cookie.setHttpOnly(true);

        return cookie;
    }

    public static final String createCookieName(final SessionBindingData sessionBindingData) {
        return String.format("%s.%s", SessionBindingConstants.SESSION_BINDING_COOKIE_NAME_PREFIX, sessionBindingData.getCookieNameSuffix());
    }
}
