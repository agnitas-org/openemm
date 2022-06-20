/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service.impl;

public class RecipientsSizeLimitExceededExeption extends RuntimeException {

    private static final long serialVersionUID = -5779986865865388568L;

    public RecipientsSizeLimitExceededExeption() {
        super();
    }

    public RecipientsSizeLimitExceededExeption(String message, Throwable cause, boolean enableSuppression,
            boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public RecipientsSizeLimitExceededExeption(String message, Throwable cause) {
        super(message, cause);
    }

    public RecipientsSizeLimitExceededExeption(String message) {
        super(message);
    }

    public RecipientsSizeLimitExceededExeption(Throwable cause) {
        super(cause);
    }

}
