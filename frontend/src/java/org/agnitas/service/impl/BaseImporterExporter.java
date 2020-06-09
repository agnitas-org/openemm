/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package org.agnitas.service.impl;

import java.util.Arrays;
import java.util.List;

import com.agnitas.json.JsonNode;
import org.apache.commons.lang3.StringUtils;

public class BaseImporterExporter {
    
    public static final String EXPORT_JSON_VERSION = "1.1.0";
    public static final String DEFAULT_JSON_VERSION = "1.0.0";
    
    private static final List<String> AVAILABLE_JSON_VERSIONS = Arrays.asList(DEFAULT_JSON_VERSION, EXPORT_JSON_VERSION);
    
    protected void checkIsJsonObject(JsonNode jsonNode) throws Exception {
        if (!jsonNode.isJsonObject()) {
            throw new Exception("Invalid JSON data: not a JSON-Object");
        }
    }
	
    protected void checkJsonVersion(String version) throws Exception {
		if (!AVAILABLE_JSON_VERSIONS.contains(StringUtils.trimToEmpty(version))) {
            throw new Exception("Invalid JSON data version: " + version);
        }
	}
}
