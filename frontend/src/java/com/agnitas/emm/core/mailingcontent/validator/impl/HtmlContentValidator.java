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

import org.agnitas.util.HtmlUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.emm.core.trackablelinks.web.LinkScanResultToPopup;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;
import com.agnitas.web.mvc.Popups;

@Component
@Order(2)
public class HtmlContentValidator implements DynTagValidator {
    private static final Logger logger = Logger.getLogger(HtmlContentValidator.class);
    public static final Pattern LINK_PATTER = Pattern.compile("(http|https):/+.*", Pattern.CASE_INSENSITIVE);

    private LinkService linkService;

    public HtmlContentValidator(LinkService linkService) {
        this.linkService = linkService;
    }

    @Override
    public boolean validate(DynTagDto dynTagDto, Popups popups, ComAdmin comAdmin) {
        List<DynContentDto> contentBlocks = dynTagDto.getContentBlocks();
        boolean hasNoErrors = true;

        for (DynContentDto contentBlock : contentBlocks) {
            if (HtmlUtils.containsElementByTag(contentBlock.getContent(), "script")) {
                popups.alert("error.mailing.content.illegal.script");
                return false;
            }
            if (HtmlUtils.containsElementByTag(contentBlock.getContent(), "iframe")) {
                popups.alert("error.mailing.content.illegal.iframe");
                return false;
            }
            if (HtmlUtils.containsElementByTag(contentBlock.getContent(), "object")) {
                popups.alert("error.mailing.content.illegal.object");
                return false;
            }
            if (HtmlUtils.containsElementByTag(contentBlock.getContent(), "embed")) {
                popups.alert("error.mailing.content.illegal.embed");
                return false;
            }
            if (HtmlUtils.containsElementByTag(contentBlock.getContent(), "applet")) {
                popups.alert("error.mailing.content.illegal.applet");
                return false;
            }
            
            try {
                LinkService.LinkScanResult linkScanResult = linkService.scanForLinks(contentBlock.getContent(), dynTagDto.getCompanyId());
                List<LinkService.ErrorneousLink> linksWithErros = linkScanResult.getErrorneousLinks();
                for (LinkService.ErrorneousLink link : linksWithErros) {
                    popups.alert(link.getErrorMessageKey(), link.getLinkText());
                    hasNoErrors = false;
                }
                
                for (final String url : linkScanResult.getNotTrackableLinks()) {
                	popups.warning("warning.mailing.link.agntag", StringEscapeUtils.escapeHtml4(dynTagDto.getName()), StringEscapeUtils.escapeHtml4(url));
                }
                
                LinkScanResultToPopup.linkWarningsToPopups(linkScanResult, popups);

                validatePoorLink(dynTagDto.getCompanyId(), contentBlock.getContent(), popups);
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
                popups.warning("error.mailing.url.blank", StringEscapeUtils.escapeHtml4(link));
            }
        }
    }
}
