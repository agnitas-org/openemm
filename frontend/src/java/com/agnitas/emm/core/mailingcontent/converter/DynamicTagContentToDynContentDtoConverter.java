/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.converter;

import com.agnitas.beans.DynamicTagContent;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;

@Component
public class DynamicTagContentToDynContentDtoConverter implements Converter<DynamicTagContent, DynContentDto> {

    @Override
    public DynContentDto convert(DynamicTagContent dynamicTagContent) {
        DynContentDto dynContentDto = new DynContentDto();

        dynContentDto.setContent(dynamicTagContent.getDynContent());
        dynContentDto.setId(dynamicTagContent.getId());
        dynContentDto.setIndex(dynamicTagContent.getDynOrder());
        dynContentDto.setTargetId(dynamicTagContent.getTargetID());

        return dynContentDto;
    }
}
