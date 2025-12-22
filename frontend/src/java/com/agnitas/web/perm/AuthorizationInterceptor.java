/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.Objects;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.util.AgnUtils;
import com.agnitas.web.perm.annotations.AlwaysAllowed;
import com.agnitas.web.perm.annotations.Anonymous;
import com.agnitas.web.perm.annotations.RequiredPermission;
import com.agnitas.web.perm.exceptions.AuthorizationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

public class AuthorizationInterceptor implements HandlerInterceptor {

    private static final Logger logger = LogManager.getLogger(AuthorizationInterceptor.class);

    private final AdminService adminService;

    public AuthorizationInterceptor(AdminService service) {
        this.adminService = Objects.requireNonNull(service, "Admin service is null");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        checkAuthorized(request, handler);
        return true;
    }

    private void checkAuthorized(HttpServletRequest request, Object handler) {
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return;
        }

        if (handler instanceof HandlerMethod m) {
            Class<?> controllerType = m.getBeanType();
            Method method = m.getMethod();

            if (isAnonymous(controllerType) || isAnonymous(method)) {
                return;
            }

            if (isAlwaysAllowed(controllerType) || isAlwaysAllowed(method)) {
                checkAdmin(request);
            } else {
                String requiredPermission = hasRequiredPermission(method)
                        ? getPermission(method)
                        : getPermission(controllerType);

                checkAuthorized(request, requiredPermission, method.getDeclaringClass().getName() + "#" + method.getName());
            }
        } else {
            checkAdmin(request);
        }
    }

    private void checkAuthorized(HttpServletRequest request, String permission, String qualifiedMethodName) {
        Admin admin = checkAdmin(request);

        if (StringUtils.isBlank(permission)) {
            logger.error("Permission denied: missing permission configuration for method: {}", qualifiedMethodName);
            throw new NotAllowedActionException(admin.getUsername(), qualifiedMethodName);
        }

        if (Permission.ALWAYS_DISALLOWED.toString().equals(permission)) {
            throw new NotAllowedActionException(admin.getUsername(), permission);
        }

        if (Permission.ALWAYS_ALLOWED.toString().equals(permission)) {
            return;
        }

        if (AgnUtils.allowed(request, Permission.getPermissionsByToken(permission))) {
            return;
        }

        throw new NotAllowedActionException(admin.getUsername(), qualifiedMethodName);
    }

    private Admin checkAdmin(HttpServletRequest request) {
        Admin admin = AgnUtils.getAdmin(request);

        if (admin == null) {
            logger.error("Permission denied: anonymous user is not authorized to request");
            throw new AuthorizationException();
        }

        if (!adminService.isEnabled(admin)) {
            request.getSession().invalidate();
            throw new AuthorizationException();
        }

        return admin;
    }

    private boolean isAnonymous(AnnotatedElement element) {
        return element.isAnnotationPresent(Anonymous.class);
    }

    private boolean isAlwaysAllowed(AnnotatedElement element) {
        return element.isAnnotationPresent(AlwaysAllowed.class);
    }

    private String getPermission(AnnotatedElement element) {
        RequiredPermission requiredPermission = element.getAnnotation(RequiredPermission.class);

        if (requiredPermission == null) {
            return null;
        }

        return requiredPermission.value();
    }

    private boolean hasRequiredPermission(AnnotatedElement element) {
        return element.isAnnotationPresent(RequiredPermission.class);
    }

}
