/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.logon.web.resolver;

import javax.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.agnitas.emm.core.logon.LogonUtils;
import com.agnitas.emm.core.logon.beans.LogonStateBundle;

public final class LogonStateBundleArgumentResolver implements HandlerMethodArgumentResolver {

	@Override
	public final Object resolveArgument(final MethodParameter parameter, final ModelAndViewContainer modelAndViewContainer, final NativeWebRequest request, final WebDataBinderFactory dataBinderFactory) throws Exception {
        return LogonUtils.getBundle(request.getNativeRequest(HttpServletRequest.class), true);
	}

	@Override
	public final boolean supportsParameter(final MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(LogonStateBundle.class);
	}

}
