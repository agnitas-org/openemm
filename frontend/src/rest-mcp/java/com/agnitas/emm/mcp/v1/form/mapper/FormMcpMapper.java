/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.mcp.v1.form.mapper;

import com.agnitas.beans.PaginatedList;
import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.userform.dto.ResultSettings;
import com.agnitas.emm.core.userform.service.CreateFormCommand;
import com.agnitas.emm.core.userform.service.PatchFormCommand;
import com.agnitas.emm.mcp.v1.common.dto.McpPageable;
import com.agnitas.emm.mcp.v1.common.dto.PageResponse;
import com.agnitas.emm.mcp.v1.common.mapper.McpMapper;
import com.agnitas.emm.mcp.v1.form.dto.CreateFormRequest;
import com.agnitas.emm.mcp.v1.form.dto.FormLightResponse;
import com.agnitas.emm.mcp.v1.form.dto.FormResponse;
import com.agnitas.emm.mcp.v1.form.dto.FormResultSettingsRequest;
import com.agnitas.emm.mcp.v1.form.dto.FormResultSettingsResponse;
import com.agnitas.emm.mcp.v1.form.dto.PatchFormRequest;
import com.agnitas.userform.bean.UserForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
public interface FormMcpMapper extends McpMapper {

    @Mapping(target = "name", source = "formName")
    FormLightResponse toLightResponse(UserForm userForm);

    @Mapping(target = "startTriggerId", source = "startActionID")
    @Mapping(target = "endTriggerId", source = "endActionID")
    @Mapping(target = "name", source = "formName")
    @Mapping(target = "successSettings", expression = "java(toSuccessSettings(userForm))")
    @Mapping(target = "errorSettings", expression = "java(toErrorSettings(userForm))")
    FormResponse toResponse(UserForm userForm);

    default FormResultSettingsResponse toSuccessSettings(UserForm userForm) {
        return new FormResultSettingsResponse(
                userForm.getSuccessTemplate(),
                userForm.getSuccessUrl(),
                userForm.isSuccessUseUrl()
        );
    }

    default FormResultSettingsResponse toErrorSettings(UserForm userForm) {
        return new FormResultSettingsResponse(
                userForm.getErrorTemplate(),
                userForm.getErrorUrl(),
                userForm.isErrorUseUrl()
        );
    }

    @Mapping(target = "sort", source = "sortBy")
    @Mapping(target = "ascending", expression = "java(pageable.ascending() == null ? true : pageable.ascending())")
    PaginatedFilter<String> toPaginationFilter(McpPageable pageable);

    @Mapping(target = "successSettings", source = "successSettings")
    @Mapping(target = "errorSettings", source = "errorSettings")
    CreateFormCommand toCommand(CreateFormRequest request);

    PatchFormCommand toCommand(PatchFormRequest request);

    @Mapping(target = "success", ignore = true)
    @Mapping(target = "startActionId", ignore = true)
    @Mapping(target = "formBuilderJson", ignore = true)
    @Mapping(target = "finalActionId", ignore = true)
    ResultSettings toResultSettings(FormResultSettingsRequest request);

    default PatchField<ResultSettings> mapResultSettings(JsonNullable<FormResultSettingsRequest> source) {
        if (source == null || source.isUndefined()) {
            return PatchField.absent();
        }
        FormResultSettingsRequest value = source.orElse(null);
        return PatchField.of(value != null ? toResultSettings(value) : null);
    }

    default PageResponse<FormLightResponse> toPageResponse(PaginatedList<UserForm> source) {
        return toPageResponse(source, this::toLightResponse);
    }

}
