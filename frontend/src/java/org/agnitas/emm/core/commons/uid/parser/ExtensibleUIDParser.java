/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.uid.parser;

import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;

/**
 * Interface for parsing an UID string representation to a ExtensibleUID instance.
 */
public interface ExtensibleUIDParser {

    /**
     * Parses a given UID string representation. Validation is performed after successful
     * parsing. For errors on the UID representation an InvalidUIDException is throws. This
     * includes an invalid format and validation errors.
     * For errors concerning the parse process itself, the UIDParseException is thrown.
     *
     * @param uidString UID string representation
     * @return ExtensibleUID instance
     * @throws UIDParseException             on errors during parsing
     * @throws InvalidUIDException           on errors during validation
     * @throws DeprecatedUIDVersionException if version of UID encoded in given String is deprecated
     */
    ComExtensibleUID parse(final String uidString) throws UIDParseException, InvalidUIDException, DeprecatedUIDVersionException;

    /**
     * Checks if given UID is of format supported by the implementation.
     * 
     * @param uidString UID
     * 
     * @return <code>true</code> if format of UID is supported
     */
    boolean isSupportedUidFormat(String uidString);

    /**
     * Returns the UID version handled by the implementation.
     * 
     * @return version of handled UID.
     */
    int getHandledUidVersion();
}
