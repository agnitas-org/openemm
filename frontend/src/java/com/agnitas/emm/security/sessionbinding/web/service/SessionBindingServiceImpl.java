/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.security.sessionbinding.web.service;

import java.util.Base64;
import java.util.UUID;

import com.agnitas.emm.security.sessionbinding.common.SessionBindingConstants;
import com.agnitas.emm.security.sessionbinding.common.SessionBindingData;
import com.agnitas.emm.security.sessionbinding.web.CookieUtils;
import com.agnitas.util.AgnUtils;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

public final class SessionBindingServiceImpl implements SessionBindingService {

    @Override
    public void bindSession(HttpServletRequest request, HttpServletResponse response) {
        final HttpSession session = request.getSession(false);

        if (session != null && AgnUtils.getAdmin(session) != null) {
            final String nameSuffix = createNameSuffix();
            final String securityToken = createSecurityToken();

            final SessionBindingData data = new SessionBindingData(nameSuffix, securityToken);
            final Cookie cookie = CookieUtils.createSessionBindingCookie(data, session.getMaxInactiveInterval() + SessionBindingConstants.SESSION_IDLE_GRACE_SECONDS);

            session.setAttribute(SessionBindingConstants.HTTP_SESSION_BINDING_DATA_ATTRIBUTE, data);
            response.addCookie(cookie);
        }
    }

    private String createNameSuffix() {
        return randomUuidBytesBase64();
    }

    private String createSecurityToken() {
        return randomUuidBytesBase64();
    }

    private String randomUuidBytesBase64() {
        final byte[] data = randomUuidBytes();

        assert data.length == 16;   // UUIDs are 128 bits long

        return Base64.getUrlEncoder().withoutPadding().encodeToString(data);
    }

    private byte[] randomUuidBytes() {
        final UUID uuid = UUID.randomUUID();
        final long hi = uuid.getMostSignificantBits();
        final long lo = uuid.getLeastSignificantBits();

        final byte[] data = new byte[] {
                (byte)(lo & 0x00000000000000ffL >> 0),
                (byte)(lo & 0x000000000000ff00L >> 8),
                (byte)(lo & 0x0000000000ff0000L >> 16),
                (byte)(lo & 0x00000000ff000000L >> 24),
                (byte)(lo & 0x000000ff00000000L >> 32),
                (byte)(lo & 0x0000ff0000000000L >> 40),
                (byte)(lo & 0x00ff000000000000L >> 48),
                (byte)(lo & 0xff00000000000000L >> 56),
                (byte)(hi & 0x00000000000000ffL >> 0),
                (byte)(hi & 0x000000000000ff00L >> 8),
                (byte)(hi & 0x0000000000ff0000L >> 16),
                (byte)(hi & 0x00000000ff000000L >> 24),
                (byte)(hi & 0x000000ff00000000L >> 32),
                (byte)(hi & 0x0000ff0000000000L >> 40),
                (byte)(hi & 0x00ff000000000000L >> 48),
                (byte)(hi & 0xff00000000000000L >> 56)
        };

        return data;
    }
}
