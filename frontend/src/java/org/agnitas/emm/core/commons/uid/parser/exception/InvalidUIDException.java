/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.uid.parser.exception;

/**
 * Exception indicating an invalid UID.
 */
public class InvalidUIDException extends Exception {
    private static final long serialVersionUID = 1620216426808409911L;

    /**
     * Creates a new instance.
     *
     * @param description invalid UID string
     */
    public InvalidUIDException(String description) {
        super("Invalid UID. " + description);
    }

    /**
     * Creates a new instance with additional description of error.
     *
     * @param description description, why UID string is invalid
     * @param uidString   invalid UID string
     */
    public InvalidUIDException(String description, String uidString) {
        super("Invalid UID. " + description + " encodedUid: " + uidString);
    }

    /**
     * Creates a new instance.
     *
     * @param description invalid UID string
     * @param cause       Throwable causing this exception.
     */
    public InvalidUIDException(String description, Throwable cause) {
        super("Invalid UID. " + description, cause);
    }
}
