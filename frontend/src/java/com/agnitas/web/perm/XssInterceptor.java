/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm;

import java.util.Collections;
import java.util.Set;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import com.agnitas.emm.util.html.xssprevention.XSSHtmlException;
import com.agnitas.emm.util.html.xssprevention.http.RequestParameterXssPreventerHelper;
import com.agnitas.web.mvc.XssCheckAware;

public class XssInterceptor extends HandlerInterceptorAdapter {
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {
            HandlerMethod handlerMethod = (HandlerMethod) handler;

            Set<HtmlCheckError> errors = checkForXss(request, handlerMethod);
            if (CollectionUtils.isNotEmpty(errors)) {
                throw new XSSHtmlException(errors);
            }
        }

        return true;
    }

    private Set<HtmlCheckError> checkForXss(final HttpServletRequest request, final HandlerMethod handlerMethod) {
        RequestParameterXssPreventerHelper helper = getXssPreventHelper(AgnUtils.getAdmin(request), handlerMethod);

        if (helper == null) {
            return Collections.emptySet();
        }

        return helper.validateRequestParameters(request);
    }

    private RequestParameterXssPreventerHelper getXssPreventHelper(final ComAdmin admin, final HandlerMethod handlerMethod) {
        Object bean = handlerMethod.getBean();

        if (bean instanceof XssCheckAware) {
            XssCheckAware controller = (XssCheckAware) bean;
            String controllerMethodName = handlerMethod.getMethod().getName();

            return new RequestParameterXssPreventerHelper(parameterName -> controller.isParameterExcludedForUnsafeHtmlTagCheck(admin, parameterName, controllerMethodName));
        }

        return null;
        // return new RequestParameterXssPreventerHelper();
    }
}
