/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.displaytag.localization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.jsp.PageContext;
import jakarta.servlet.jsp.tagext.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.displaytag.Messages;
import org.displaytag.localization.I18nResourceProvider;
import org.displaytag.localization.LocaleResolver;
import org.springframework.context.MessageSource;
import org.springframework.web.servlet.support.RequestContextUtils;

import java.util.Locale;

public class I18nSpringAdapter implements LocaleResolver, I18nResourceProvider {

    private final Logger logger = LogManager.getLogger(I18nSpringAdapter.class);

    @Override
	public Locale resolveLocale(HttpServletRequest request) {
        return RequestContextUtils.getLocale(request);
    }

    @Override
	public String getResource(String resourceKey, String defaultValue, Tag tag, PageContext pageContext) {
        MessageSource messageSource = RequestContextUtils.findWebApplicationContext((HttpServletRequest) pageContext.getRequest());
        if (messageSource == null) {
            logger.error("DisplayTag adapter: messageSource not found");
            return null;
        }

        String key = resourceKey != null ? resourceKey : defaultValue;
        String message = messageSource.getMessage(key, null, null, RequestContextUtils.getLocale((HttpServletRequest) pageContext.getRequest()));
        if (message == null && resourceKey != null) {
            logger.debug(Messages.getString("Localization.missingkey", resourceKey));
            message = "???" + resourceKey + "???";
        }

        return message;
    }

}
