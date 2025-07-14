/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.web;

import java.util.Date;

import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import com.agnitas.web.perm.annotations.Anonymous;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class TranslationsController {

    private static final String ETAG = "W/\"" + new Date().getTime() + "\"";
    
    @Anonymous
    @GetMapping("/translations.js.action")
    public String load(HttpServletResponse response, 
                       @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        if (StringUtils.isNotBlank(ifNoneMatch) && ETAG.equalsIgnoreCase(ifNoneMatch)) {
            response.setStatus(HttpStatus.NOT_MODIFIED.value());
            return null;
       	}
        response.setHeader("etag", ETAG);
        return "js_translations";
    }
}
