/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.uid.parser.exception;

public class MailingNotFoundParseException extends UIDParseException {
	private static final long serialVersionUID = -350656829016037756L;

	public MailingNotFoundParseException(String message) {
        super(message);
    }

    public MailingNotFoundParseException(String message, String uidString) {
        super(message, uidString);
    }

    public MailingNotFoundParseException(String message, String uidString, Throwable cause) {
        super(message, uidString, cause);
    }
}
