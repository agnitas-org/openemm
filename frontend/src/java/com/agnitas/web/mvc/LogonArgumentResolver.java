/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.agnitas.emm.core.logon.service.Logon;
import com.agnitas.emm.core.logon.service.impl.LogonImpl;

public class LogonArgumentResolver implements HandlerMethodArgumentResolver {
    private ConfigService configService;

    public LogonArgumentResolver(ConfigService configService) {
        this.configService = configService;
    }

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Logon.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) {
        final boolean useSecureCookies = configService.getBooleanValue(ConfigValue.HostauthenticationCookiesHttpsOnly);
        final String cookieName = configService.getHostAuthenticationHostIdCookieName();
        
        return new LogonImpl(nativeWebRequest.getNativeRequest(HttpServletRequest.class), nativeWebRequest.getNativeResponse(HttpServletResponse.class), useSecureCookies, cookieName);
    }
}
