/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.security.xss;

import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

import com.agnitas.beans.Admin;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.emm.util.html.xssprevention.HtmlCheckError;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class McpXssAspect {

    private final McpXssPreventerHelper xssPreventerHelper;
    private final McpAdminProvider adminProvider;

    public McpXssAspect(McpXssPreventerHelper xssPreventerHelper, McpAdminProvider adminProvider) {
        this.xssPreventerHelper = xssPreventerHelper;
        this.adminProvider = adminProvider;
    }

    @Around("@annotation(org.springframework.ai.mcp.annotation.McpTool)")
    public Object preventXss(ProceedingJoinPoint pjp) throws Throwable {
        MethodSignature signature = (MethodSignature) pjp.getSignature();
        Object target = pjp.getTarget();
        Method method = AopUtils.getMostSpecificMethod(signature.getMethod(), target.getClass());
        McpXssExclude exclude = AnnotatedElementUtils.findMergedAnnotation(method, McpXssExclude.class);

        Predicate<String> isExcluded = exclude == null
                ? path -> false
                : excludedByToolMethod(target, exclude.checkMethod(), adminProvider.resolveCurrentAdmin().orElse(null));

        Map<String, Set<HtmlCheckError>> errors = xssPreventerHelper.validate(
                collectArguments(signature.getParameterNames(), pjp.getArgs()),
                exclude == null ? Set.of() : Set.of(exclude.jsonFields()),
                isExcluded
        );

        if (!errors.isEmpty()) {
            throw new McpXssException(errors);
        }
        return pjp.proceed();
    }

    private static Map<String, Object> collectArguments(String[] names, Object[] args) {
        Map<String, Object> arguments = new LinkedHashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (!(args[i] instanceof McpSyncRequestContext)) {
                arguments.put(names != null && i < names.length ? names[i] : "arg" + i, args[i]);
            }
        }
        return arguments;
    }

    private static Predicate<String> excludedByToolMethod(Object tool, String checkMethod, Admin admin) {
        Method method = findCheckMethod(tool, checkMethod);

        return path -> {
            try {
                return (boolean) method.invoke(tool, path, admin);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException("Can't invoke " + checkMethod + " of " + tool.getClass().getName(), e);
            }
        };
    }

    private static Method findCheckMethod(Object tool, String name) {
        try {
            return tool.getClass().getMethod(name, String.class, Admin.class);
        } catch (NoSuchMethodException e) {
            throw new IllegalStateException(
                    "Check method %s(String, Admin) is not found in %s".formatted(name, tool.getClass().getName()), e
            );
        }
    }

}
