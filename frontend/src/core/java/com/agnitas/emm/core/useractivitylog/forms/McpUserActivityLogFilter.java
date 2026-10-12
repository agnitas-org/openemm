/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.useractivitylog.forms;

import static org.apache.commons.lang3.StringUtils.isNotBlank;

import java.util.Map;

public class McpUserActivityLogFilter extends UserActivityLogFilterBase {

    private String source;
    private String toolName;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    @Override
    public Map<String, Object> toMap() {
        Map<String, Object> map = super.toMap();
        map.put("toolName", toolName);
        map.put("source", source);

        return map;
    }

    @Override
    public boolean isUiFiltersSet() {
        return isNotBlank(source) || isNotBlank(toolName) || super.isUiFiltersSet();
    }
}
