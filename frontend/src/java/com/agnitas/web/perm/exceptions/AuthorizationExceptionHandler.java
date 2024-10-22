/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm.exceptions;

import com.agnitas.beans.Admin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.agnitas.web.perm.NotAllowedActionException;

@ControllerAdvice
public class AuthorizationExceptionHandler {
    private static final Logger logger = LogManager.getLogger(AuthorizationExceptionHandler.class);

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String onAuthorizationException(AuthorizationException e) {
        logger.error("User authorization required");
        return "forward:/logonRedesigned.action";
    }

    @ExceptionHandler(NotAllowedActionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String onNotAllowedActionException(NotAllowedActionException e, Admin admin, Model model) {
        logger.error("Permission denied: user " + e.getUsername() + " does not have sufficient privileges for " + e.getToken());
        model.addAttribute("email", admin.getEmail());
        return "permission_denied";
    }
}
