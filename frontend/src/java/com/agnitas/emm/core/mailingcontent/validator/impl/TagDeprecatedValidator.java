/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.validator.impl;

import java.util.List;
import java.util.stream.Collectors;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.service.AgnTagService;
import com.agnitas.web.mvc.Popups;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(6)
public class TagDeprecatedValidator implements DynTagValidator {

    private final AgnTagService agnTagService;

    public TagDeprecatedValidator(AgnTagService agnTagService) {
        this.agnTagService = agnTagService;
    }

    @Override
    public boolean validate(DynTagDto dynTagDto, Popups popups, ComAdmin admin) {
        List<String> deprecatedTagNames = dynTagDto.getContentBlocks()
                .stream()
                .flatMap(block -> agnTagService.parseDeprecatedTagNamesFromString(block.getContent(), dynTagDto.getCompanyId()).stream())
                .distinct()
                .collect(Collectors.toList());

        deprecatedTagNames.forEach(tagName -> popups.warning("warning.mailing.agntag.deprecated", tagName));

        return true;
    }
}
