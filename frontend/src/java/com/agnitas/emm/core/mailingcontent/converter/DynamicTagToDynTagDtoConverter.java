/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.converter;

import com.agnitas.beans.DynamicTag;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.service.ExtendedConversionService;
import com.agnitas.beans.DynamicTagContent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DynamicTagToDynTagDtoConverter implements Converter<DynamicTag, DynTagDto> {

    private final ExtendedConversionService conversionService;

    @Autowired
    public DynamicTagToDynTagDtoConverter(ExtendedConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public DynTagDto convert(DynamicTag dynamicTag) {
        DynTagDto dynTagDto = new DynTagDto();
        dynTagDto.setId(dynamicTag.getId());
        dynTagDto.setMailingId(dynamicTag.getMailingID());
        dynTagDto.setInterestGroup(dynamicTag.getDynInterestGroup());
        dynTagDto.setName(dynamicTag.getDynName());

        List<DynamicTagContent> dynTagContent = dynamicTag.getDynContent().values().stream()
                .sorted(Comparator.comparingInt(DynamicTagContent::getDynOrder))
                .collect(Collectors.toList());
        List<DynContentDto> dynTagContentDtos = conversionService.convert(dynTagContent, DynamicTagContent.class, DynContentDto.class);
        dynTagDto.setContentBlocks(dynTagContentDtos);

        return dynTagDto;
    }
}
