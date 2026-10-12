/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.infrastructure.exception;

import java.util.HashSet;
import java.util.List;

import com.agnitas.emm.core.commons.validation.DeleteResult;
import com.agnitas.exception.UiMessageException;
import com.agnitas.messages.Message;
import org.springframework.http.HttpStatus;

public class HttpStatusException extends UiMessageException {

    private final HttpStatus status;

    public HttpStatusException(HttpStatus status, List<Message> messages) {
        super(new HashSet<>(messages));
        this.status = status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public static void throwIfFailed(DeleteResult result) {
        switch (result.status()) {
            case DELETED -> { /* Success, do nothing */ }
            case NOT_FOUND -> throw new HttpStatusException(HttpStatus.NOT_FOUND, result.errors());
            case CONFLICT -> throw new HttpStatusException(HttpStatus.CONFLICT, result.errors());
            case FAILED -> throw new HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, result.errors());
        }
    }
}
