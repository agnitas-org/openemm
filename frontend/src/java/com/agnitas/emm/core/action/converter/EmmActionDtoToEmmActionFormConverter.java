/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.converter;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.action.dto.EmmActionDto;
import com.agnitas.emm.core.action.form.EmmActionForm;
import com.agnitas.emm.core.action.operations.ActionOperationParametersParser;

@Component
public class EmmActionDtoToEmmActionFormConverter implements Converter<EmmActionDto, EmmActionForm> {

    @Lazy
    @Autowired
    private final ActionOperationParametersParser parametersParser;

    public EmmActionDtoToEmmActionFormConverter(ActionOperationParametersParser parametersParser) {
        this.parametersParser = parametersParser;
    }

    @Override
    public EmmActionForm convert(EmmActionDto dto) {
        EmmActionForm form = new EmmActionForm();
        form.setId(dto.getId());
        form.setShortname(dto.getShortname());
        form.setDescription(dto.getDescription());
        form.setType(dto.getType());
        form.setActive(dto.isActive());
        form.setModulesSchema(parametersParser.serializeActionModules(dto.getOperations()));
        return form;
    }
}
