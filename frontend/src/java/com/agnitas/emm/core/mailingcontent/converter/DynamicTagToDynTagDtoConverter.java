/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.converter;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.agnitas.beans.DynamicTagContent;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import com.agnitas.beans.DynamicTag;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.service.ExtendedConversionService;

@Component
public class DynamicTagToDynTagDtoConverter implements Converter<DynamicTag, DynTagDto> {

    @Override
    public DynTagDto convert(DynamicTag dynamicTag) {
        ExtendedConversionService extendedConversionService = getExtendedConversionService();

        DynTagDto dynTagDto = new DynTagDto();
        dynTagDto.setId(dynamicTag.getId());
        dynTagDto.setMailingId(dynamicTag.getMailingID());
        dynTagDto.setInterestGroup(dynamicTag.getDynInterestGroup());
        dynTagDto.setName(dynamicTag.getDynName());

        List<DynamicTagContent> dynTagContent = dynamicTag.getDynContent().values().stream()
                .sorted(Comparator.comparingInt(DynamicTagContent::getDynOrder))
                .collect(Collectors.toList());
        List<DynContentDto> dynTagContentDtos = extendedConversionService.convert(dynTagContent, DynamicTagContent.class, DynContentDto.class);
        dynTagDto.setContentBlocks(dynTagContentDtos);

        return dynTagDto;
    }

    @Lookup
    public ExtendedConversionService getExtendedConversionService() {
        return null;
    }
}
