/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.spring.web.view.tiles3;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.tiles.locale.impl.DefaultLocaleResolver;
import org.apache.tiles.request.Request;
import org.apache.tiles.request.servlet.NotAServletEnvironmentException;
import org.apache.tiles.request.servlet.ServletUtil;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

public class SpringLocaleResolver extends DefaultLocaleResolver {

    @Override
    public Locale resolveLocale(Request request) {
        try {
            HttpServletRequest servletRequest = ServletUtil.getServletRequest(request).getRequest();
            if (servletRequest != null) {
                return RequestContextUtils.getLocale(servletRequest);
            }
        }
        catch (NotAServletEnvironmentException ex) {
            // ignore
        }
        return super.resolveLocale(request);
    }

}
