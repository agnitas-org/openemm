/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.exception;

import com.agnitas.emm.mcp.v1.infrastructure.exception.mapper.McpToolErrorMessageResolver;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(0)
public class McpToolExceptionAspect {

    private final McpToolErrorMessageResolver errorMessageResolver;

    public McpToolExceptionAspect(McpToolErrorMessageResolver errorMessageResolver) {
        this.errorMessageResolver = errorMessageResolver;
    }

    @Around("@annotation(org.springframework.ai.mcp.annotation.McpTool)")
    public Object handleToolExceptions(ProceedingJoinPoint pjp) throws Throwable {
        try {
            return pjp.proceed();
        } catch (McpToolException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new McpToolException(errorMessageResolver.resolve(ex));
        }
    }
}

