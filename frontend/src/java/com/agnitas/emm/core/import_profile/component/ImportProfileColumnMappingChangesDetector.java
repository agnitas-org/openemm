/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.import_profile.component;

import org.agnitas.beans.ColumnMapping;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

@Component
public class ImportProfileColumnMappingChangesDetector {

    public static final String NO_VALUE = "<no value>";

    public StringBuilder detectChanges(ColumnMapping newMapping, ColumnMapping oldMapping) {
        StringBuilder changesBuilder = new StringBuilder();

        if (oldMapping != null) {
            appendExistingColumnLog(newMapping, oldMapping, changesBuilder);
        } else {
            appendNewColumnLog(newMapping, changesBuilder);
        }

        return changesBuilder;
    }

    private void appendExistingColumnLog(ColumnMapping newMapping, ColumnMapping existingMapping, StringBuilder builder) {
        String oldMappingValue = StringUtils.defaultIfEmpty(existingMapping.getDatabaseColumn(), NO_VALUE);
        String newMappingValue = StringUtils.defaultIfEmpty(newMapping.getDatabaseColumn(), NO_VALUE);

        if (!StringUtils.equals(oldMappingValue, newMappingValue)) {
            builder.append(", ").append(String.format("changed database mapping from \"%s\" to \"%s\"",
                    oldMappingValue, newMappingValue));
        }

        if (existingMapping.isMandatory() != newMapping.isMandatory()) {
            builder.append(", ").append(String.format("mandatory set to %b", newMapping.isMandatory()));
        }

        String oldDefaultValue = StringUtils.defaultIfEmpty(existingMapping.getDefaultValue(), NO_VALUE);
        String newDefaultValue = StringUtils.defaultIfEmpty(newMapping.getDefaultValue(), NO_VALUE);

        if (!StringUtils.equals(oldDefaultValue, newDefaultValue)) {
            builder.append(", ").append(String.format("default value changed from %s to %s", oldDefaultValue, newDefaultValue));
        }
    }

    private void appendNewColumnLog(ColumnMapping newMapping, StringBuilder builder) {
        String dbColumn = StringUtils.defaultIfEmpty(newMapping.getDatabaseColumn(), NO_VALUE);
        String defaultValue = StringUtils.defaultIfEmpty(newMapping.getDefaultValue(), NO_VALUE);

        builder.append(", ").append(String.format("database mapping set to %s", dbColumn))
                .append(String.format(", mandatory set to %b", newMapping.isMandatory()))
                .append(String.format(", default value set to %s", defaultValue));
    }
}
