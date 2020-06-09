/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.mvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.bval.jsr.DefaultMessageInterpolator;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.i18n.LocaleContextHolder;

public class CustomMessageInterpolator extends DefaultMessageInterpolator {
    private static final Logger logger = Logger.getLogger(CustomMessageInterpolator.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String interpolate(String template, Context context) {
        return interpolate(template, context, LocaleContextHolder.getLocale());
    }

    @Override
    public String interpolate(String template, Context context, Locale locale) {
        if (StringUtils.isBlank(template)) {
            return template;
        }

        if (template.startsWith("{") && template.endsWith("}")) {
            return super.interpolate(template, context, locale);
        }

        return resolveTemplate(template, context, locale);
    }

    private String resolveTemplate(String template, Context context, Locale locale) {
        if (template.endsWith("]")) {
            int position = template.indexOf('[');
            if (position > 0) {
                try {
                    String code = template.substring(0, position);
                    String arguments = template.substring(position + 1, template.length() - 1);

                    return code + resolveArguments(arguments, context, locale);
                } catch (Exception e) {
                    logger.error("Error occurred: " + e.getMessage(), e);
                }
            }
        }

        return template;
    }

    private String resolveArguments(String arguments, Context context, Locale locale) {
        if (StringUtils.isBlank(arguments)) {
            return "";
        }

        List<String> sequence = new ArrayList<>();
        for (String argument : arguments.split("\\s*,\\s*")) {
            sequence.add(super.interpolate(argument, context, locale));
        }

        try {
            return objectMapper.writeValueAsString(sequence);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
