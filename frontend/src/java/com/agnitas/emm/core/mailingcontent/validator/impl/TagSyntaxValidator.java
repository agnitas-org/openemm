/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.validator.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.preview.AgnTagError;
import org.agnitas.preview.TagSyntaxChecker;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.web.mvc.Popups;

@Component
@Order(3)
public class TagSyntaxValidator implements DynTagValidator {
    private static final Logger logger = LogManager.getLogger(TagSyntaxValidator.class);
    private static final Pattern TAG_WRONG_SPACES_PATTERN = Pattern.compile("(\\[\\s+agn)|(\\/\\s+\\])");

    private TagSyntaxChecker tagSyntaxChecker;

    public TagSyntaxValidator(TagSyntaxChecker tagSyntaxChecker) {
        this.tagSyntaxChecker = tagSyntaxChecker;
    }

    @Override
    public boolean validate(DynTagDto dynTagDto, Popups popups, ComAdmin admin) {
        boolean hasNoErrors = true;
        Locale locale = admin.getLocale();
        if (locale == null) {
            locale = Locale.ENGLISH;
        }

        try {
            List<AgnTagError> agnTagSyntaxErrors = new ArrayList<>();
            List<DynContentDto> contentBlocks = dynTagDto.getContentBlocks();

            for (DynContentDto contentBlock : contentBlocks) {
                if (!tagSyntaxChecker.check(dynTagDto.getCompanyId(), contentBlock.getContent(), agnTagSyntaxErrors)) {
                    for (AgnTagError agnTagError : agnTagSyntaxErrors) {
                        String localizedMessage = agnTagError.getLocalizedMessage(locale);
                        popups.alert("error.mailing.agntags", agnTagError.getFullAgnTagText(), localizedMessage);
                    }

                    hasNoErrors = false;
                }

                validateTagSpaces(contentBlock.getContent(), popups);
            }
        } catch (Exception e) {
            String description = String.format("dyn tag id: %d, dyn tag name: %s", dynTagDto.getId(), dynTagDto.getName());
            logger.warn("Something went wrong while syntax validation in the dyn tag. " + description);
        }

        return hasNoErrors;
    }

    private void validateTagSpaces(String content, Popups popups) {
        Matcher matcher = TAG_WRONG_SPACES_PATTERN.matcher(content);
        while (matcher.find()) {
            String tagName = "";
            if (StringUtils.equals("[ agn", matcher.group())) {
                tagName = StringUtils.substring(content, matcher.start(), content.indexOf("]", matcher.start()) + 1);
            } else if (StringUtils.equals("/ ]", matcher.group())) {
                tagName = StringUtils.substring(content, content.lastIndexOf("[agn", matcher.end()), matcher.end());
            }
            popups.warning("warning.mailing.agntags", tagName);
        }
    }
}
