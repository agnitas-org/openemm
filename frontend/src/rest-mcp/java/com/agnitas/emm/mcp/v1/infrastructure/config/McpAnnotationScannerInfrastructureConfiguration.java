/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.config;

import java.lang.annotation.Annotation;
import java.util.Set;

import org.springframework.ai.mcp.annotation.McpComplete;
import org.springframework.ai.mcp.annotation.McpPrompt;
import org.springframework.ai.mcp.annotation.McpResource;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerAnnotationScannerAutoConfiguration;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Role;

/**
 * TEMPORARY: pre-empts McpServerAnnotationScannerAutoConfiguration's beans to avoid
 * a startup warning storm (hundreds of "not eligible for post-processing" warnings
 * caused by the unused McpServerAnnotationScannerProperties constructor parameter).
 * Reproduces the fix from
 * <a href="https://github.com/spring-projects/spring-ai/pull/5817">spring-ai PR #5817</a>
 * locally. Remove this class once that PR is merged and released, and rely on the
 * upstream fix via a normal dependency bump instead.
 */
@Configuration
public class McpAnnotationScannerInfrastructureConfiguration {

    private static final Set<Class<? extends Annotation>> SERVER_MCP_ANNOTATIONS =
            Set.of(McpTool.class, McpResource.class, McpPrompt.class, McpComplete.class);

    @Bean
    @ConditionalOnMissingBean
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    public McpServerAnnotationScannerAutoConfiguration.ServerMcpAnnotatedBeans serverAnnotatedBeanRegistry() {
        return new McpServerAnnotationScannerAutoConfiguration.ServerMcpAnnotatedBeans();
    }

    @Bean
    @ConditionalOnMissingBean
    public static McpServerAnnotationScannerAutoConfiguration.ServerAnnotatedMethodBeanPostProcessor serverAnnotatedMethodBeanPostProcessor(
            McpServerAnnotationScannerAutoConfiguration.ServerMcpAnnotatedBeans serverMcpAnnotatedBeans
    ) {
        return new McpServerAnnotationScannerAutoConfiguration.ServerAnnotatedMethodBeanPostProcessor(
                serverMcpAnnotatedBeans, SERVER_MCP_ANNOTATIONS);
    }

}
