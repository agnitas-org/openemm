/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.config;

import com.agnitas.beans.Admin;
import com.agnitas.emm.restful.RestUserAction;
import com.agnitas.service.UserActivityLogService;
import com.agnitas.util.AgnUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.NonNull;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class UserActionLoggingInterceptor implements HandlerInterceptor {

    private final UserActivityLogService userActivityLogService;

    public UserActionLoggingInterceptor(UserActivityLogService userActivityLogService) {
        this.userActivityLogService = userActivityLogService;
    }

    @Override
    public boolean preHandle(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull Object handler
    ) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }
        Admin admin = (Admin) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String host = request.getHeader("Host");
        if (StringUtils.isBlank(host)) {
            host = AgnUtils.getHostName();
        }

        userActivityLogService.writeRestUserActivityLog(new RestUserAction(
            request.getMethod(),
            request.getRequestURI(),
            handlerMethod.getMethod().getName(),
            host
        ), admin);

        return true;
    }
}
