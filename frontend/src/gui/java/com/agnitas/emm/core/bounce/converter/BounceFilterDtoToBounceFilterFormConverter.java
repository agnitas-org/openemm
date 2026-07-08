/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.converter;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.bounce.dto.BounceFilterDto;
import com.agnitas.emm.core.bounce.form.BounceFilterForm;

@Component
public class BounceFilterDtoToBounceFilterFormConverter implements Converter<BounceFilterDto, BounceFilterForm> {

    @Override
    public BounceFilterForm convert(BounceFilterDto bounceFilterDto) {
        BounceFilterForm filterForm = new BounceFilterForm();
        filterForm.setId(bounceFilterDto.getId());
        filterForm.setShortName(bounceFilterDto.getShortName());
        filterForm.setDescription(bounceFilterDto.getDescription());
        filterForm.setFilterEmail(bounceFilterDto.getFilterEmail());
        if(StringUtils.isNotBlank(bounceFilterDto.getFilterEmail())) {
            filterForm.setOwnForwardEmailSelected(true);
        }
        filterForm.setForwardEmail(bounceFilterDto.getForwardEmail());
        filterForm.setDoForward(bounceFilterDto.isDoForward());
        filterForm.setDoSubscribe(bounceFilterDto.isDoSubscribe());
        filterForm.setMailingListId(bounceFilterDto.getMailingListId());
        filterForm.setUserFormId(bounceFilterDto.getUserFormId());
        filterForm.setDoAutoRespond(bounceFilterDto.isDoAutoRespond());
        filterForm.setArMailingId(bounceFilterDto.getArMailingId());
        filterForm.setSecurityToken(bounceFilterDto.getSecurityToken());
        return filterForm;
    }
}
