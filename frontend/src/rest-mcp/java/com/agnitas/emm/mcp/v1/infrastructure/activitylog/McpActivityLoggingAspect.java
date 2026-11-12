/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.activitylog;

import java.util.Arrays;
import java.util.Date;
import java.util.Objects;
import java.util.Optional;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.useractivitylog.bean.McpToolAction;
import com.agnitas.emm.mcp.v1.McpAdminProvider;
import com.agnitas.service.UserActivityLogService;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.annotation.context.McpSyncRequestContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Order(1)
public class McpActivityLoggingAspect {

    private static final Logger logger = LogManager.getLogger(McpActivityLoggingAspect.class);

    private static final int MAX_OBJECT_ID_LENGTH = 1000;

    private final McpAdminProvider adminProvider;
    private final UserActivityLogService userActivityLogService;

    public McpActivityLoggingAspect(McpAdminProvider adminProvider, UserActivityLogService userActivityLogService) {
        this.adminProvider = adminProvider;
        this.userActivityLogService = userActivityLogService;
    }

    @Around("@annotation(mcpTool)")
    public Object logToolInvocation(ProceedingJoinPoint pjp, McpTool mcpTool) throws Throwable {
        Date invocationTime = new Date();
        McpActivityContext context = McpActivityContext.open();

        try {
            return pjp.proceed();
        } finally {
            McpActivityContext.close();
            resolveAdmin().ifPresent(admin -> writeActivityLog(pjp, mcpTool, context, invocationTime, admin));
        }
    }

    private Optional<Admin> resolveAdmin() {
        try {
            return adminProvider.resolveCurrentAdmin();
        } catch (Exception e) {
            logger.warn("Failed to resolve admin for MCP activity log", e);
            return Optional.empty();
        }
    }

    private void writeActivityLog(
            ProceedingJoinPoint pjp, McpTool mcpTool, McpActivityContext context, Date invocationTime, Admin admin
    ) {
        try {
            userActivityLogService.writeMcpUserActivityLog(
                    new McpToolAction(
                            mcpTool.name(),
                            resolveSource(pjp),
                            StringUtils.abbreviate(context.getObjectId(), MAX_OBJECT_ID_LENGTH),
                            invocationTime
                    ),
                    admin
            );
        } catch (Exception e) {
            logger.warn("Failed to write MCP activity log", e);
        }
    }

    private String resolveSource(ProceedingJoinPoint pjp) {
        return Arrays.stream(pjp.getArgs())
                .filter(McpSyncRequestContext.class::isInstance)
                .map(McpSyncRequestContext.class::cast)
                .map(McpSyncRequestContext::clientInfo)
                .filter(Objects::nonNull)
                .map(this::clientDisplayName)
                .filter(StringUtils::isNotBlank)
                .findFirst()
                .orElse(null);
    }

    private String clientDisplayName(McpSchema.Implementation clientInfo) {
        return StringUtils.isNotBlank(clientInfo.title()) ? clientInfo.title() : clientInfo.name();
    }

}
