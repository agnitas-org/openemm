/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.exception;

import java.util.Map;

import com.agnitas.messages.Message;

public class DetailedUiMessageException extends UiMessageException {

    private static final long serialVersionUID = -5960224266603550348L;
    private final Map<String, Object> details;

    public DetailedUiMessageException(Map<String, Object> details, Message... errors) {
        super(errors);
        this.details = details;
    }

    public Map<String, Object> getDetails() {
        return details;
    }

}
