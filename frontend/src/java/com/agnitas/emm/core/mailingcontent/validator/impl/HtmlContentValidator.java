/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.validator.impl;

import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.agnitas.emm.core.LinkService;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;
import com.agnitas.web.mvc.Popups;

@Component
@Order(2)
public class HtmlContentValidator implements DynTagValidator {
    private static final Logger logger = Logger.getLogger(HtmlContentValidator.class);
    public static final Pattern LINK_PATTER = Pattern.compile("(http|https):/+.*", Pattern.CASE_INSENSITIVE);

    private HttpServletRequest request;
    private LinkService linkService;

    public HtmlContentValidator(HttpServletRequest request, LinkService linkService) {
        this.request = request;
        this.linkService = linkService;
    }

    @Override
    public boolean validate(DynTagDto dynTagDto, Popups popups) {
        int companyId = AgnUtils.getCompanyID(request);
        List<DynContentDto> contentBlocks = dynTagDto.getContentBlocks();
        boolean hasNoErrors = true;

        for (DynContentDto contentBlock : contentBlocks) {
            try {
                LinkService.LinkScanResult linkScanResult = linkService.scanForLinks(contentBlock.getContent(), companyId);
                List<LinkService.ErrorneousLink> linksWithErros = linkScanResult.getErrorneousLinks();
                for (LinkService.ErrorneousLink link : linksWithErros) {
                    popups.alert(link.getErrorMessageKey());
                    hasNoErrors = false;
                }

                validatePoorLink(companyId, contentBlock.getContent(), popups);
            } catch (Exception e) {
                String description = String.format("dyn tag id: %d, dyn tag name: %s", dynTagDto.getId(), dynTagDto.getName());
                logger.warn("something went wrong while html content validation in the dyn content. " + description);
            }
        }

        return hasNoErrors;
    }

    private void validatePoorLink(int companyId, String link, Popups popups) {
        boolean isMatching = LINK_PATTER.matcher(link).matches();

        if (isMatching) {
            boolean hasWhitespaces = Objects.nonNull(linkService.validateLink(companyId, link, GridCustomPlaceholderType.Link));
            if (hasWhitespaces) {
                popups.warning("error.mailing.url.blank");
            }
        }
    }
}
