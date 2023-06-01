/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import java.util.Locale;
import java.util.TimeZone;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.util.AgnUtils;
import org.springframework.context.i18n.LocaleContext;
import org.springframework.context.i18n.TimeZoneAwareLocaleContext;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.logon.LogonUtils;

public class AdminLocaleResolver extends SessionLocaleResolver {
    private static Locale getLocale(HttpServletRequest request) {
        Admin admin = AgnUtils.getAdmin(request);

        if (admin == null) {
            admin = LogonUtils.getAdmin(request);

            if (admin == null) {
                return request.getLocale();
            }
        }

        return admin.getLocale();
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        return getLocale(request);
    }

    @Override
    public LocaleContext resolveLocaleContext(final HttpServletRequest request) {
        return new TimeZoneAwareLocaleContext() {
            @Override
			public Locale getLocale() {
                return AdminLocaleResolver.getLocale(request);
            }

            @Override
			public TimeZone getTimeZone() {
                return AgnUtils.getTimeZone(request);
            }
        };
    }
}
