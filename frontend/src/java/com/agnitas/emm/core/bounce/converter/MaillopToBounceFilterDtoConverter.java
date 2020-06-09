/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.converter;

import org.agnitas.beans.Mailloop;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.bounce.dto.BounceFilterDto;

@Component
public class MaillopToBounceFilterDtoConverter implements Converter<Mailloop, BounceFilterDto> {
    @Override
    public BounceFilterDto convert(Mailloop mailloop) {
        BounceFilterDto filterDto = new BounceFilterDto();
        filterDto.setId(mailloop.getId());
        filterDto.setShortName(mailloop.getShortname());
        filterDto.setDescription(mailloop.getDescription());
        filterDto.setFilterEmail(mailloop.getFilterEmail());
        filterDto.setForwardEmail(mailloop.getForwardEmail());
        filterDto.setDoForward(mailloop.isDoForward());
        filterDto.setDoSubscribe(mailloop.isDoSubscribe());
        filterDto.setMailingListId(mailloop.getMailinglistID());
        filterDto.setUserFormId(mailloop.getUserformID());
        filterDto.setDoAutoRespond(mailloop.isDoAutoresponder());
        filterDto.setArMailingId(mailloop.getAutoresponderMailingId());
        filterDto.setSecurityToken(mailloop.getSecurityToken());
        return filterDto;
    }
}
