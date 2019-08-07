/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.perm.exceptions;

import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;

import com.agnitas.web.perm.NotAllowedActionException;

@ControllerAdvice
public class AuthorizationExceptionHandler {
    private static final Logger logger = Logger.getLogger(AuthorizationExceptionHandler.class);

    @ExceptionHandler(AuthorizationException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public String onAuthorizationException(AuthorizationException e) {
        logger.error("User authorization required", e);
        return "forward:/logon.action";
    }

    @ExceptionHandler(NotAllowedActionException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public String onNotAllowedActionException(NotAllowedActionException e) {
        logger.error("Permission denied: user " + e.getUsername() + " does not have sufficient privileges for " + e.getToken(), e);
        return "forward:/permissionDenied.do";
    }
}
