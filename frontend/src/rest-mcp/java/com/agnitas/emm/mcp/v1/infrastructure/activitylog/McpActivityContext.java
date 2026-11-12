/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.infrastructure.activitylog;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds additional information about the currently running MCP tool invocation, which is written to the MCP activity log
 * by {@link McpActivityLoggingAspect} once the tool has finished.
 */
public final class McpActivityContext {

    private static final ThreadLocal<McpActivityContext> CURRENT = new ThreadLocal<>();

    private String objectId;

    private McpActivityContext() {
    }

    /**
     * Returns the context of the running tool invocation, opened by {@link McpActivityLoggingAspect}.
     */
    public static McpActivityContext current() {
        McpActivityContext context = CURRENT.get();
        if (context == null) {
            throw new IllegalStateException("No MCP activity context - method must be invoked as an MCP tool");
        }

        return context;
    }

    static McpActivityContext open() {
        McpActivityContext context = new McpActivityContext();
        CURRENT.set(context);
        return context;
    }

    static void close() {
        CURRENT.remove();
    }

    String getObjectId() {
        return objectId;
    }

    /**
     * Sets the identifier (ID, name, email, ...) of the object the tool is applied to.
     */
    public void setObjectId(Object id) {
        if (id != null) {
            this.objectId = String.valueOf(id);
        }
    }

    /**
     * Sets the identifier for tools that accept alternative identifiers (e.g. ID or email). The first non-null one is used.
     */
    public void setObjectId(Object id, Object alternativeId) {
        setObjectId(id != null ? id : alternativeId);
    }

    /**
     * Sets the identifier for objects identified by several values, e.g. {@code setObjectIdParts("recipientId", 5, "mailinglistId", 3)}
     * results in {@code recipientId=5, mailinglistId=3}. Parts with {@code null} values are skipped.
     */
    public void setObjectIdParts(Object... namesAndValues) {
        if (namesAndValues.length % 2 != 0) {
            throw new IllegalArgumentException("Names and values must come in pairs");
        }

        List<String> parts = new ArrayList<>();
        for (int i = 0; i < namesAndValues.length; i += 2) {
            if (namesAndValues[i + 1] != null) {
                parts.add(namesAndValues[i] + "=" + namesAndValues[i + 1]);
            }
        }

        if (!parts.isEmpty()) {
            setObjectId(String.join(", ", parts));
        }
    }

}
