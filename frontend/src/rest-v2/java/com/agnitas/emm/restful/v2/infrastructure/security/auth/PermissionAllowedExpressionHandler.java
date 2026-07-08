/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.security.auth;

import java.util.function.Supplier;

import com.agnitas.emm.core.Permission;
import org.aopalliance.intercept.MethodInvocation;
import org.jspecify.annotations.Nullable;
import org.springframework.expression.EvaluationContext;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

public class PermissionAllowedExpressionHandler extends DefaultMethodSecurityExpressionHandler {

    /**
     * This will pass #permission to check in spring {@link PreAuthorize} via {@link PermissionAllowed}
     */
    @Override
    public EvaluationContext createEvaluationContext(Supplier<? extends @Nullable Authentication> authentication, MethodInvocation mi) {
        EvaluationContext context = super.createEvaluationContext(authentication, mi);
        PermissionAllowed ann = mi.getMethod().getAnnotation(PermissionAllowed.class);

        if (ann == null) {
            ann = mi.getMethod().getDeclaringClass().getAnnotation(PermissionAllowed.class);
        }
        if (ann != null) {
            context.setVariable("permissions", Permission.getPermissionsByToken(ann.value()));
        }
        return context;
    }
}
