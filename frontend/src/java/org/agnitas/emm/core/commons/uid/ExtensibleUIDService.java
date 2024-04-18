/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.uid;

import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;

/**
 * Facade interface. Provides a combined interface for methods dealing with UIDs.
 */
public interface ExtensibleUIDService {

    /**
     * Converts the UID to its string representation.
     *
     * @param extensibleUID UID
     * @return string representation of the UID
     * @throws UIDStringBuilderException           on errors during conversion
     * @throws RequiredInformationMissingException when required informations are not encoded in UID
     */
    String buildUIDString(final ComExtensibleUID extensibleUID) throws UIDStringBuilderException, RequiredInformationMissingException;

    /**
     * Parses the string representation of a UID.
     *
     * @param uidString string representation
     * @return parsed ExtensibleUID instance
     * @throws UIDParseException             on errors during parsing
     * @throws InvalidUIDException           on errors indicating an invalid UID
     * @throws DeprecatedUIDVersionException if version of UID encoded in given String is deprecated
     */
    ComExtensibleUID parse(final String uidString) throws UIDParseException, InvalidUIDException, DeprecatedUIDVersionException;

    ComExtensibleUID parseOrNull(String uidStr);
}
