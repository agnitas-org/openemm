/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.uid.impl;

import java.util.Objects;

import org.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import org.agnitas.emm.core.commons.uid.builder.ExtensibleUIDStringBuilder;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import org.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import org.agnitas.emm.core.commons.uid.parser.ExtensibleUIDParser;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;

/**
 * Facade. Implementation of ExtensibleUIDService.
 */
public class ExtensibleUIDServiceImpl implements ExtensibleUIDService {

    /**
     * Parser for UIDs.
     */
    private ExtensibleUIDParser parser;

    /**
     * String builder for UIDs.
     */
    private ExtensibleUIDStringBuilder stringBuilder;

    // ------------------------------------------------------ Business Logic

    @Override
    public String buildUIDString(final ComExtensibleUID extensibleUID) throws UIDStringBuilderException, RequiredInformationMissingException {
        return this.stringBuilder.buildUIDString(extensibleUID);
    }

    @Override
    public ComExtensibleUID parse(final String uidString) throws UIDParseException, InvalidUIDException, DeprecatedUIDVersionException {
        return this.parser.parse(uidString);
    }

    /**
     * Sets the ExtensibleUIDParser.
     *
     * @param parser ExtensibleUIDParser
     */
    @Required
    public final void setParser(final ExtensibleUIDParser parser) {
        this.parser = Objects.requireNonNull(parser, "Parser cannot be null");
    }

    /**
     * Sets the ExtensibleUIDStringBuilder.
     *
     * @param stringBuilder ExtensibleUIDStringBuilder
     */
    @Required
    public final void setStringBuilder(final ExtensibleUIDStringBuilder stringBuilder) {
        this.stringBuilder = Objects.requireNonNull(stringBuilder, "String builder cannot be null");
    }
}
