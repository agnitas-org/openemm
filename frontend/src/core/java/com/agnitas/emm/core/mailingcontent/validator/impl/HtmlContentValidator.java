/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailingcontent.validator.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.linkcheck.service.LinkService;
import com.agnitas.emm.core.mailingcontent.dto.DynContentDto;
import com.agnitas.emm.core.mailingcontent.dto.DynTagDto;
import com.agnitas.emm.core.mailingcontent.validator.DynTagValidator;
import com.agnitas.emm.core.trackablelinks.web.LinkScanResultToPopup;
import com.agnitas.emm.grid.grid.beans.GridCustomPlaceholderType;
import com.agnitas.emm.util.html.HtmlChecker;
import com.agnitas.emm.util.html.HtmlCheckerError;
import com.agnitas.emm.util.html.HtmlCheckerException;
import com.agnitas.messages.Message;
import com.agnitas.util.HtmlUtils;
import com.agnitas.web.mvc.Popups;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Order(2)
public class HtmlContentValidator implements DynTagValidator {
	
    private static final Logger logger = LogManager.getLogger(HtmlContentValidator.class);
    
    /** Regular expression for links. */
    public static final Pattern LINK_PATTER = Pattern.compile("(http|https):/+.*", Pattern.CASE_INSENSITIVE);
    
    private static final Set<String> INVALID_HTML_ELEMENTS = Set.of("script", "iframe", "object", "embed", "applet");

    private final LinkService linkService;

    public HtmlContentValidator(LinkService linkService) {
        this.linkService = linkService;
    }
    
    public final List<String> findIllegalTags(final String content) {
    	try {
	    	final List<String> list = new ArrayList<>();
	    	
	    	for(final String element : INVALID_HTML_ELEMENTS) {
	    		if(HtmlUtils.containsElementByTag(content, element)) {
	    			list.add(element);
	    		}
	    	}
	    	
	    	return list;
    	} catch(final Exception e) {
    		logger.error("Error checking content", e);
    		
    		throw e;
    	}
    }

    @Override
    public boolean validate(DynTagDto dynTagDto, Popups popups, Admin admin) {
        for (DynContentDto contentBlock : dynTagDto.getContentBlocks()) {
            validateContentBlock(dynTagDto, popups, contentBlock, admin.getCompanyID());
        }
        return !popups.hasAlertPopups();
    }

    private void validateContentBlock(DynTagDto dynTagDto, Popups popups, DynContentDto contentBlock, int companyId) {
        List<Message> tagErrors = collectTagErrors(contentBlock);
        if (CollectionUtils.isNotEmpty(tagErrors)) {
            tagErrors.forEach(popups::alert);
        }

        try {
            LinkService.LinkScanResult linkScanResult = linkService.scanForLinks(contentBlock.getContent(), companyId);
            for (LinkService.ErroneousLink link : linkScanResult.getErroneousLinks()) {
                popups.alert(link.getErrorMessageKey(), link.getLinkText());
            }
            
            for (final String url : linkScanResult.getNotTrackableLinks()) {
                popups.warning("warning.mailing.link.agntag", StringEscapeUtils.escapeHtml4(dynTagDto.getName()), StringEscapeUtils.escapeHtml4(url));
            }
            
            LinkScanResultToPopup.linkWarningsToPopups(linkScanResult, popups);

            validatePoorLink(contentBlock.getContent(), popups);
        } catch (Exception e) {
            logger.warn("Something went wrong while html content validation in the dyn content (ID: {}, Name: '{}'): {}",
                    dynTagDto.getId(), dynTagDto.getName(), e.getMessage());
        }
    }

    private List<Message> collectTagErrors(DynContentDto contentBlock) {
        List<String> illegalElements = findIllegalTags(contentBlock.getContent());
        if (!illegalElements.isEmpty()) {
            return illegalElements.stream().map(HtmlContentValidator::getIllegalElementMessage).collect(Collectors.toList());
        }
        try {
            HtmlChecker.checkForUnallowedHtmlTags(contentBlock.getContent(), true);
        } catch (HtmlCheckerException e) {
            return e.getErrors().stream().map(HtmlCheckerError::toMessage).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private static Message getIllegalElementMessage(String illegalElement) {
        return Message.of(String.format("error.mailing.content.illegal.%s", illegalElement));
    }

    private void validatePoorLink(String link, Popups popups) {
        boolean isMatching = LINK_PATTER.matcher(link).matches();

        if (isMatching) {
            boolean hasWhitespaces = Objects.nonNull(linkService.validateLink(link, GridCustomPlaceholderType.Link));
            if (hasWhitespaces) {
                popups.warning("error.mailing.url.blank", StringEscapeUtils.escapeHtml4(link));
            }
        }
    }
}
