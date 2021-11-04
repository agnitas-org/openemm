/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm;

import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Objects;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.agnitas.util.AgnUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.Permission;
import com.agnitas.emm.core.admin.service.AdminService;
import com.agnitas.web.perm.annotations.AlwaysAllowed;
import com.agnitas.web.perm.annotations.Anonymous;
import com.agnitas.web.perm.annotations.PermissionMapping;
import com.agnitas.web.perm.exceptions.AuthorizationException;

public class AuthorizationInterceptor extends HandlerInterceptorAdapter {
	/** The logger. */
    private static final transient Logger logger = Logger.getLogger(AuthorizationInterceptor.class);

    private static final String CONTROLLER_SUFFIX = "_controller";

    private ActionsTokenResolver actionsTokenResolver;
    private AdminService adminService;

    @Autowired
    public AuthorizationInterceptor(ActionsTokenResolver actionsTokenResolver, AdminService service) {
        this.actionsTokenResolver = actionsTokenResolver;
        this.adminService = Objects.requireNonNull(service, "Admin service is null");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        checkAuthorized(request, handler);
        return true;
    }

    private void checkAuthorized(HttpServletRequest request, Object handler) throws Exception {
    	if (!HttpMethod.OPTIONS.matches(request.getMethod())) {
	        if (handler instanceof HandlerMethod) {
	            HandlerMethod m = (HandlerMethod) handler;
	            Class<?> controllerType = m.getBeanType();
	            Method method = m.getMethod();

	            if (isAnonymous(controllerType) || isAnonymous(method)) {
	                return;
	            }

	            if (isAlwaysAllowed(controllerType) || isAlwaysAllowed(method)) {
                    checkAuthorized(request);
                } else {
                    checkAuthorized(request, getNamespace(controllerType), getName(method));
                }
	        } else {
	            checkAuthorized(request);
	        }
    	}
    }

    private void checkAuthorized(HttpServletRequest request) throws Exception {
        checkAuthorized(request, null, null);
    }

    private void checkAuthorized(HttpServletRequest request, String namespace, String method) throws Exception {
        ComAdmin admin = AgnUtils.getAdmin(request);

        if (admin == null) {
            logger.error("Permission denied: anonymous user is not authorized to request");
            throw new AuthorizationException();
        }

        if (!adminService.isEnabled(admin)) {
            request.getSession().invalidate();
            throw new AuthorizationException();
        }

        if (namespace != null) {
            String methodToken = namespace + "." + method;

            String simpleToken = actionsTokenResolver.get(methodToken);
            List<ComplexToken> complexTokens = actionsTokenResolver.getComplex(namespace);

            if (simpleToken == null && complexTokens == null) {
                logger.error("Permission denied: missing permission configuration for token: " + methodToken);
                throw new NotAllowedActionException(admin.getUsername(), methodToken);
            }

            if (simpleToken != null) {
                if (Permission.ALWAYS_DISALLOWED.toString().equals(simpleToken)) {
                    throw new NotAllowedActionException(admin.getUsername(), simpleToken);
                }
                
                if (Permission.ALWAYS_ALLOWED.toString().equals(simpleToken)) {
                    return;
                }
                if (AgnUtils.allowed(request, Permission.getPermissionsByToken(simpleToken))) {
                    return;
                }
            }

            if (checkComplexTokens(complexTokens, method, request)) {
                return;
            }

            throw new NotAllowedActionException(admin.getUsername(), methodToken);
        }
    }

    private boolean isAnonymous(AnnotatedElement element) {
        return element.isAnnotationPresent(Anonymous.class);
    }

    private boolean isAlwaysAllowed(AnnotatedElement element) {
        return element.isAnnotationPresent(AlwaysAllowed.class);
    }

    private String getNamespace(Class<?> controllerType) {
        PermissionMapping permissionMapping = controllerType.getAnnotation(PermissionMapping.class);

        if (permissionMapping == null) {
            // Camel case to snake case.
            String namespace = controllerType.getSimpleName()
                    .replaceAll("([^_A-Z])([A-Z])", "$1_$2")
                    .toLowerCase();

            if (namespace.endsWith(CONTROLLER_SUFFIX)) {
                return namespace.substring(0, namespace.length() - CONTROLLER_SUFFIX.length());
            }

            return namespace;
        }

        return permissionMapping.value();
    }

    private String getName(Method method) {
        PermissionMapping permissionMapping = method.getAnnotation(PermissionMapping.class);

        if (permissionMapping == null) {
            return method.getName();
        }

        return permissionMapping.value();
    }

    private boolean checkComplexTokens(List<ComplexToken> complexTokens, String method, HttpServletRequest request) throws Exception {
        if (CollectionUtils.isEmpty(complexTokens)) {
            return false;
        }

        for (ComplexToken complexToken : complexTokens) {
            if ("*".equals(complexToken.getSubaction()) || method.equals(complexToken.getSubaction())) {
                if ("OR".equalsIgnoreCase(complexToken.getAggregation())) {
                    if (checkORedTokens(complexToken, request)) {
                        return true;
                    }
                } else if ("AND".equalsIgnoreCase(complexToken.getAggregation())) {
                    if (checkANDedTokens(complexToken, request)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean checkORedTokens(ComplexToken complexToken, HttpServletRequest request) throws Exception {
        for (String token : complexToken.getTokens()) {
            if (AgnUtils.allowed(request, Permission.getPermissionsByToken(token))) {
                return true;
            }
        }
        return false;
    }

    private boolean checkANDedTokens(ComplexToken complexToken, HttpServletRequest request) throws Exception {
        for (String token : complexToken.getTokens()) {
            if (!AgnUtils.allowed(request, Permission.getPermissionsByToken(token))) {
                return false;
            }
        }
        return true;
    }
}
