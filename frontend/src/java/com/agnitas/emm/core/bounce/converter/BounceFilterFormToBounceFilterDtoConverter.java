/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.converter;

import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterForm;

@Component
public class BounceFilterFormToBounceFilterDtoConverter implements Converter<BounceFilterForm, BounceFilterDto> {
    @Override
    public BounceFilterDto convert(BounceFilterForm bounceFilterForm) {
        BounceFilterDto bounceFilterDto = new BounceFilterDto();
        bounceFilterDto.setId(bounceFilterForm.getId());
        bounceFilterDto.setShortName(bounceFilterForm.getShortName());
        bounceFilterDto.setDescription(bounceFilterForm.getDescription());
        bounceFilterDto.setFilterEmail(bounceFilterForm.getFilterEmail());
        bounceFilterDto.setForwardEmail(bounceFilterForm.getForwardEmail());
        bounceFilterDto.setDoForward(bounceFilterForm.isDoForward());
        bounceFilterDto.setDoSubscribe(bounceFilterForm.isDoSubscribe());
        bounceFilterDto.setMailingListId(bounceFilterForm.getMailingListId());
        bounceFilterDto.setUserFormId(bounceFilterForm.getUserFormId());
        bounceFilterDto.setDoAutoRespond(bounceFilterForm.isDoAutoRespond());
        bounceFilterDto.setArMailingId(bounceFilterForm.getArMailingId());
        return bounceFilterDto;
    }
}
