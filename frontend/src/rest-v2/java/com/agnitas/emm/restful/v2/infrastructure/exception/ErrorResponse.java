/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.restful.v2.infrastructure.exception;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import com.agnitas.messages.Message;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.http.HttpStatus;

public record ErrorResponse(
    int status,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    String detail,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSX", timezone = "UTC")
    Instant timestamp,

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    List<ErrorEntry> errors
) {

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private int status;
        private String detail;
        private final List<ErrorEntry> errors = new ArrayList<>();

        public Builder status(HttpStatus status) {
            this.status = status.value();
            return this;
        }

        public Builder detail(String detail) {
            this.detail = detail;
            return this;
        }

        public Builder addError(Message error) {
            this.errors.add(new ErrorEntry(error));
            return this;
        }

        public Builder addError(String error) {
            this.errors.add(new ErrorEntry(error));
            return this;
        }

        public Builder addError(ErrorEntry errorEntry) {
            this.errors.add(errorEntry);
            return this;
        }

        public Builder addErrors(List<ErrorEntry> errors) {
            this.errors.addAll(errors);
            return this;
        }

        public Builder messages(List<Message> errors) {
            return this.addErrors(errors.stream().map(ErrorEntry::new).toList());
        }

        public ErrorResponse build() {
            return new ErrorResponse(
                status,
                detail,
                Instant.now(),
                List.copyOf(errors)
            );
        }
    }
}
