/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.config.json;

import com.agnitas.messages.Message;
import tools.jackson.databind.module.SimpleModule;

/**
 * Registers {@link MessageSerializer} for {@link Message}. Discovered via JDK {@link java.util.ServiceLoader}
 * (see {@code META-INF/services/tools.jackson.databind.JacksonModule}) by {@code org.springframework.ai.util.JacksonUtils}'s
 * default {@code JsonMapper} — the mapper actually used by Spring AI's MCP tool method callbacks
 * ({@code AbstractMcpToolMethodCallback}'s static {@code JsonHelper}) to serialize {@code @McpTool} return
 * values. Without this, {@code Message} fields serialize with their raw {@code code}/{@code resolvable}/
 * {@code arguments} properties instead of a resolved display string.
 */
public class MessageJacksonModule extends SimpleModule {

    public MessageJacksonModule() {
        super("MessageModule");
        addSerializer(Message.class, new MessageSerializer());
    }

}
