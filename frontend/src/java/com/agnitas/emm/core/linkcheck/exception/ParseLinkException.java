/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.linkcheck.exception;

import com.agnitas.util.Caret;

public class ParseLinkException extends RuntimeException {

    private final String errorLink;
    private final String errorMessage;
    private final Caret caret;

    public ParseLinkException(String message, String errorLink, Caret caret) {
        super(message);
        this.errorLink = errorLink;
        errorMessage = message;
        this.caret = caret;
    }

    public String getErrorLink() {
        return errorLink;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Caret getCaret() {
        return caret;
    }
}
