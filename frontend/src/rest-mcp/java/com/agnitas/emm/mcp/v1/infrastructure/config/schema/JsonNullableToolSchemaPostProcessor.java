/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.config.schema;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.modelcontextprotocol.json.McpJsonDefaults;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import org.jspecify.annotations.NonNull;
import org.springframework.ai.mcp.annotation.McpTool;
import org.springframework.ai.mcp.server.common.autoconfigure.annotations.McpServerAnnotationScannerAutoConfiguration.ServerMcpAnnotatedBeans;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.BeanPostProcessor;

/**
 * Post-processes Spring AI's MCP tool specifications to fix schemas containing
 * {@link org.openapitools.jackson.nullable.JsonNullable} fields.
 * Remove once Spring AI provides native {@code JsonNullable} schema support
 * (<a href="https://github.com/spring-projects/spring-ai/issues/6046">issue #6046</a> /
 * <a href="https://github.com/spring-projects/spring-ai/pull/6071">PR #6071</a>).
 */
public class JsonNullableToolSchemaPostProcessor implements BeanPostProcessor {

    private static final String TOOL_SPECS_BEAN_NAME = "toolSpecs";

    private final ObjectProvider<ServerMcpAnnotatedBeans> annotatedBeans;

    public JsonNullableToolSchemaPostProcessor(ObjectProvider<ServerMcpAnnotatedBeans> annotatedBeans) {
        this.annotatedBeans = annotatedBeans;
    }

    @Override
    public Object postProcessAfterInitialization(@NonNull Object bean, @NonNull String beanName) throws BeansException {
        if (!TOOL_SPECS_BEAN_NAME.equals(beanName) || !(bean instanceof List<?> specs)) {
            return bean;
        }

        Map<String, Method> toolMethodsNeedingFixup = findToolMethodsNeedingFixup();
        if (toolMethodsNeedingFixup.isEmpty()) {
            return bean;
        }

        return specs.stream()
                .map(spec -> spec instanceof SyncToolSpecification syncSpec
                        ? fixIfNeeded(syncSpec, toolMethodsNeedingFixup)
                        : spec)
                .toList();
    }

    private SyncToolSpecification fixIfNeeded(SyncToolSpecification spec, Map<String, Method> toolMethodsNeedingFixup) {
        Method method = toolMethodsNeedingFixup.get(spec.tool().name());
        if (method == null) {
            return spec;
        }

        String correctedSchema = JsonNullableAwareSchemaGenerator.fixMethodSchema(method);
        Tool original = spec.tool();
        Tool corrected = Tool.builder(original.name(), McpJsonDefaults.getMapper(), correctedSchema)
                .title(original.title())
                .description(original.description())
                .outputSchema(original.outputSchema())
                .annotations(original.annotations())
                .icons(original.icons())
                .meta(original.meta())
                .build();

        return SyncToolSpecification.builder().tool(corrected).callHandler(spec.callHandler()).build();
    }

    private Map<String, Method> findToolMethodsNeedingFixup() {
        Map<String, Method> result = new HashMap<>();
        for (Object toolBean : annotatedBeans.getObject().getBeansByAnnotation(McpTool.class)) {
            Class<?> targetClass = AopUtils.getTargetClass(toolBean);
            for (Method method : targetClass.getMethods()) {
                McpTool annotation = method.getAnnotation(McpTool.class);
                if (annotation != null && JsonNullableAwareSchemaGenerator.needsFixup(method)) {
                    result.put(annotation.name(), method);
                }
            }
        }
        return result;
    }

}
