/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.config;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.ai.util.JacksonUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;

/** Overrides Spring AI's mapper to preserve explicit JSON nulls in tool arguments. */
@Configuration
public class McpJsonMapperConfiguration {

    @Bean(name = "mcpServerJsonMapper")
    public JsonMapper mcpServerJsonMapper() {
        return JsonMapper.builder()
                .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
                .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
                .addModules(JacksonUtils.instantiateAvailableModules())
                .changeDefaultPropertyInclusion(incl ->
                        JsonInclude.Value.construct(JsonInclude.Include.NON_NULL, JsonInclude.Include.ALWAYS))
                .build();
    }

}
