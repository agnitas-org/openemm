/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.validator.impl;

import java.util.List;
import java.util.Vector;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.web.mvc.Popups;
import org.agnitas.preview.TAGCheck;
import org.agnitas.preview.TAGCheckFactory;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(4)
public class TagContentValidator implements DynTagValidator {
    private static final Logger logger = LogManager.getLogger(TagContentValidator.class);

    private TAGCheckFactory tagCheckFactory;

    public TagContentValidator(TAGCheckFactory tagCheckFactory) {
        this.tagCheckFactory = tagCheckFactory;
    }

    @Override
    public boolean validate(DynTagDto dynTagDto, Popups popups, ComAdmin admin) {
        boolean hasNoErrors = true;

        try {
            StringBuffer tagErrorReport = new StringBuffer();
            List<DynContentDto> contentBlocks = dynTagDto.getContentBlocks();

            TAGCheck tagCheck = tagCheckFactory.createTAGCheck(dynTagDto.getMailingId(), admin.getLocale());
            for (DynContentDto contentBlock : contentBlocks) {
                if (!tagCheck.checkContent(contentBlock.getContent(), tagErrorReport, new Vector<>())) {
                    String description = StringEscapeUtils.escapeHtml4(tagErrorReport.toString());
                    popups.alert("error.html.validation", description);
                    hasNoErrors = false;
                }
            }
            tagCheck.done();
        } catch (Exception e) {
            String description = String.format("dyn tag id: %d, dyn tag name: %s", dynTagDto.getId(), dynTagDto.getName());
            logger.warn("Something went wrong while content tag validation in the dyn tag. " + description);
        }

        return hasNoErrors;
    }
}
