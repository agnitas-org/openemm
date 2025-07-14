/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.uid.builder;

import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;

import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;

/**
 * Interface for implementations to convert a ExtensibleUID object to a String representation.
 * 
 * For supported formats of UID see {@link ExtensibleUID}.
 */
public interface ExtensibleUIDStringBuilder {
	/**
	 * Convert instance of ExtensibleUID to String.
	 * 
	 * @param extensibleUID UID to convert
	 * 
	 * @return String representation of UID
	 * 
	 * @throws RequiredInformationMissingException when required informations are not available
	 * @throws UIDStringBuilderException on errors during conversion
	 */
    String buildUIDString(final ExtensibleUID extensibleUID) throws RequiredInformationMissingException, UIDStringBuilderException;

    /**
     * Returns the version of the UID built by the implementation.
     * 
     * @return version of UID built by implementation
     */
    ExtensibleUidVersion getVersionOfBuiltUIDs();
}
