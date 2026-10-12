/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.form.dto;

import com.agnitas.util.AgnUtils;
import jakarta.validation.constraints.AssertTrue;
import org.apache.commons.lang3.StringUtils;
import org.springframework.ai.mcp.annotation.McpToolParam;

public record FormResultSettingsRequest(
        @McpToolParam(description = "Velocity template used to render the response when useUrl is false", required = false)
        String template,

        @McpToolParam(description = "URL to redirect to when useUrl is true", required = false)
        String url,

        @McpToolParam(description = "Redirect to url instead of rendering template", required = false)
        Boolean useUrl
) {
    @SuppressWarnings("unused")
    @AssertTrue(message = "url must be a valid URL")
    boolean isValidUrl() {
        if (!Boolean.TRUE.equals(useUrl)) {
            return true;
        }
        return AgnUtils.isValidUrl(url);
    }

    @SuppressWarnings("unused")
    @AssertTrue(message = "template must not be blank")
    boolean isValidTemplate() {
        if (Boolean.TRUE.equals(useUrl)) {
            return true;
        }
        return StringUtils.isNotBlank(template);
    }
}
