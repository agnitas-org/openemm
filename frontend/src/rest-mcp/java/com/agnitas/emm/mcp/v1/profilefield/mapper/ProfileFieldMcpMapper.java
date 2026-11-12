/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.profilefield.mapper;

import java.util.List;

import com.agnitas.beans.ProfileFieldMode;
import com.agnitas.emm.core.service.RecipientFieldDescription;
import com.agnitas.emm.mcp.v1.profilefield.dto.ProfileFieldResponse;
import com.agnitas.util.DbColumnType.SimpleDataType;
import org.apache.commons.lang3.StringUtils;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(
        componentModel = "spring",
        imports = ProfileFieldMode.class,
        unmappedTargetPolicy = ReportingPolicy.ERROR
)
public interface ProfileFieldMcpMapper {

    @Mapping(source = "field.columnName", target = "column")
    @Mapping(source = "field.shortName", target = "eqlName")
    @Mapping(source = "field.description", target = "description", qualifiedByName = "blankToNull")
    @Mapping(source = "field.simpleDataType", target = "type")
    @Mapping(target = "maxLength", expression = "java(toMaxLength(field))")
    @Mapping(source = "field.nullable", target = "nullable")
    @Mapping(source = "field.allowedValues", target = "allowedValues", qualifiedByName = "emptyToNull")
    @Mapping(source = "field.defaultValue", target = "defaultValue", qualifiedByName = "blankToNull")
    @Mapping(target = "readOnly", expression = "java(field.getAdminPermission(adminId) == ProfileFieldMode.ReadOnly)")
    ProfileFieldResponse toResponse(RecipientFieldDescription field, int adminId);

    default String toType(SimpleDataType dataType) {
        return switch (dataType) {
            case null -> null;
            case Characters -> "TEXT";
            case Numeric -> "INTEGER";
            case Float -> "FLOAT";
            case Date -> "DATE";
            case DateTime -> "DATETIME";
            case Blob -> "BLOB";
        };
    }

    default Long toMaxLength(RecipientFieldDescription field) {
        return field.getSimpleDataType() == SimpleDataType.Characters && field.getCharacterLength() > 0
                ? field.getCharacterLength()
                : null;
    }

    @Named("blankToNull")
    default String blankToNull(String value) {
        return StringUtils.trimToNull(value);
    }

    @Named("emptyToNull")
    default List<String> emptyToNull(List<String> values) {
        return values == null || values.isEmpty() ? null : values;
    }
}
