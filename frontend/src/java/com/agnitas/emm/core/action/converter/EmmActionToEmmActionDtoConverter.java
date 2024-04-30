/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.action.converter;

import java.util.List;

import org.agnitas.actions.EmmAction;
import org.apache.commons.collections4.ListUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.action.dto.EmmActionDto;
import com.agnitas.emm.core.action.operations.AbstractActionOperationParameters;

@Component
public class EmmActionToEmmActionDtoConverter implements Converter<EmmAction, EmmActionDto> {

    @Override
    public EmmActionDto convert(EmmAction emmAction) {
        EmmActionDto dto = new EmmActionDto();
        dto.setId(emmAction.getId());
        dto.setShortname(emmAction.getShortname());
        dto.setDescription(emmAction.getDescription());
        dto.setFormNames(emmAction.getFormNameList());
        dto.setCreationDate(emmAction.getCreationDate());
        dto.setChangeDate(emmAction.getChangeDate());
        dto.setActive(emmAction.getIsActive());
        dto.setAdvertising(emmAction.isAdvertising());

        List<AbstractActionOperationParameters> operations = emmAction.getActionOperations();
        dto.setOperations(ListUtils.emptyIfNull(operations));
        return dto;
    }
}
