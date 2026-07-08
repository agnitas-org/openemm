/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import com.agnitas.web.mvc.impl.PopupsImpl;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.core.MethodParameter;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.support.RequestContextUtils;

public class PopupsArgumentResolver implements HandlerMethodArgumentResolver {
    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.getParameterType().isAssignableFrom(Popups.class);
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mav, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) {
        return initializePopups(mav.getDefaultModel(), nativeWebRequest.getNativeRequest(HttpServletRequest.class));
    }

    private PopupsImpl initializePopups(ModelMap model, HttpServletRequest request) {
        PopupsImpl popups = PopupsImpl.get(model);

        if (popups == null) {
            popups = new PopupsImpl();

            PopupsImpl.put(model, popups);
        }
        PopupsImpl.put(RequestContextUtils.getOutputFlashMap(request), popups);

        return popups;
    }
}
