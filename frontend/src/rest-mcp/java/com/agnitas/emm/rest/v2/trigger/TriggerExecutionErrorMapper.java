/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.rest.v2.trigger;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

import com.agnitas.emm.core.action.service.EmmActionOperationErrors.ErrorCode;
import com.agnitas.emm.rest.v2.infrastructure.exception.HttpStatusException;
import com.agnitas.messages.Message;
import org.springframework.http.HttpStatus;

/**
 * Maps {@link ErrorCode}s collected while executing a trigger to a HTTP status, so that
 * callers get a precise status (e.g. 400/403/409) instead of a generic 500 whenever the
 * failure is actually caused by the request itself.
 */
final class TriggerExecutionErrorMapper {

    private TriggerExecutionErrorMapper() {
    }

    static HttpStatusException toException(Set<ErrorCode> errorCodes) {
        if (errorCodes.isEmpty()) {
            return new HttpStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    List.of(Message.exact("Can't execute trigger"))
            );
        }

        List<Message> messages = errorCodes.stream()
                .map(ErrorCode::getDescription)
                .map(Message::exact)
                .toList();

        // If several errors of different severity were collected, report the most severe status.
        HttpStatus status = errorCodes.stream()
                .map(TriggerExecutionErrorMapper::statusOf)
                .max(Comparator.comparingInt(HttpStatus::value))
                .orElse(HttpStatus.INTERNAL_SERVER_ERROR);

        return new HttpStatusException(status, messages);
    }

    private static HttpStatus statusOf(ErrorCode code) {
        return switch (code.getFault()) {
            case CALLER -> HttpStatus.BAD_REQUEST;
            case CONFIGURATION, INTERNAL -> HttpStatus.INTERNAL_SERVER_ERROR;
            case QUOTA -> HttpStatus.CONFLICT;
            case PERMISSION -> HttpStatus.FORBIDDEN;
            case EXPIRED -> HttpStatus.GONE;
        };
    }
}
