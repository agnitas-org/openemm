/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.spring.security.web;

import com.agnitas.web.mvc.Popups;
import com.agnitas.web.perm.annotations.Anonymous;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Anonymous
public class OAuthLoginController {

    private static final String DEFAULT_ERROR_MESSAGE = "Authentication failed";

    @GetMapping("/oauth/logon.action")
    public String loginPage(@RequestParam(required = false) String error, Popups popups, HttpSession session) {
        if (error != null) {
            popups.exactAlert(getErrorMessage(session));
        }

        return "oauth_login";
    }

    private String getErrorMessage(HttpSession session) {
        if (session.getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION) instanceof AuthenticationException ex) {
            return ex.getMessage();
        }

        return DEFAULT_ERROR_MESSAGE;
    }

}
