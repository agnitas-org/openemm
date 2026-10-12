/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.forms;

import java.util.List;

import com.agnitas.emm.core.commons.dto.PatchField;
import com.agnitas.emm.core.commons.pagination.PaginatedFilter;
import com.agnitas.emm.core.userform.dto.ResultSettings;
import com.agnitas.emm.core.userform.service.CreateFormCommand;
import com.agnitas.emm.core.userform.service.PatchFormCommand;
import com.agnitas.emm.rest.v2.infrastructure.mapper.RestMapper;
import com.agnitas.userform.bean.UserForm;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.ERROR)
interface FormRestMapper extends RestMapper {

    @Mapping(target = "name", source = "formName")
    FormLightResponse toLightResponse(UserForm userForm);

    List<FormLightResponse> toLightResponse(List<UserForm> userForms);

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

    @Mapping(source = "pageSize", target = "size")
    @Mapping(source = "pageNumber", target = "page")
    @Mapping(source = "sort", target = "ascending")
    PaginatedFilter<String> toPaginationFilter(Pageable pageable);

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
        return PatchField.of(source.isPresent() ? toResultSettings(source.get()) : null);
    }
}
