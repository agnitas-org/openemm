/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.bounce.converter;

import org.agnitas.beans.Mailloop;
import org.agnitas.beans.impl.MailloopImpl;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.bounce.dto.BounceFilterDto;

@Component
public class BounceFilterDtoToMailloopConverter implements Converter<BounceFilterDto, Mailloop> {

    @Override
    public Mailloop convert(BounceFilterDto bounceFilterDto) {
        Mailloop mailloop = new MailloopImpl();
        mailloop.setId(bounceFilterDto.getId());
        mailloop.setShortname(bounceFilterDto.getShortName());
        mailloop.setDescription(bounceFilterDto.getDescription());
        mailloop.setFilterEmail(bounceFilterDto.getFilterEmail());
        mailloop.setForwardEmail(bounceFilterDto.getForwardEmail());
        mailloop.setDoForward(bounceFilterDto.isDoForward());
        mailloop.setDoSubscribe(bounceFilterDto.isDoSubscribe());
        mailloop.setMailinglistID(bounceFilterDto.getMailingListId());
        mailloop.setUserformID(bounceFilterDto.getUserFormId());
        mailloop.setDoAutoresponder(bounceFilterDto.isDoAutoRespond());
        mailloop.setAutoresonderMailingId(bounceFilterDto.getArMailingId());
        mailloop.setSecurityToken(bounceFilterDto.getSecurityToken());
        return mailloop;
    }
}
