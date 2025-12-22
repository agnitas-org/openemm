/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.impl;

import java.util.Objects;

import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUIDService;
import com.agnitas.emm.core.commons.uid.builder.ExtensibleUIDStringBuilder;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.RequiredInformationMissingException;
import com.agnitas.emm.core.commons.uid.builder.impl.exception.UIDStringBuilderException;
import com.agnitas.emm.core.commons.uid.parser.ExtensibleUIDParser;
import com.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import com.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import com.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Facade. Implementation of ExtensibleUIDService.
 */
public class ExtensibleUIDServiceImpl implements ExtensibleUIDService {

    private static final Logger logger = LogManager.getLogger(ExtensibleUIDServiceImpl.class);

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
    public String buildUIDString(final ExtensibleUID extensibleUID) throws UIDStringBuilderException, RequiredInformationMissingException {
        return this.stringBuilder.buildUIDString(extensibleUID);
    }

    @Override
    public ExtensibleUID parse(final String uidString) throws UIDParseException, InvalidUIDException, DeprecatedUIDVersionException {
        return this.parser.parse(uidString);
    }

    @Override
    public ExtensibleUID parseOrNull(final String uid) {
        if (StringUtils.isBlank(uid)) {
            return null;
        }
        return tryParseUid(uid);
    }

    private ExtensibleUID tryParseUid(String uid) {
        try {
            return parse(uid);
        } catch (DeprecatedUIDVersionException e) {
            logger.error(String.format("Deprecated UID version of UID: %s", uid), e);
        } catch (UIDParseException e) {
            logger.error(String.format("Error parsing UID: %s", uid), e);
        } catch (InvalidUIDException e) {
            logger.error(String.format("Invalid UID: %s", uid), e);
        }
        return null;
    }

    /**
     * Sets the ExtensibleUIDParser.
     *
     * @param parser ExtensibleUIDParser
     */
    public final void setParser(final ExtensibleUIDParser parser) {
        this.parser = Objects.requireNonNull(parser, "Parser cannot be null");
    }

    /**
     * Sets the ExtensibleUIDStringBuilder.
     *
     * @param stringBuilder ExtensibleUIDStringBuilder
     */
    public final void setStringBuilder(final ExtensibleUIDStringBuilder stringBuilder) {
        this.stringBuilder = Objects.requireNonNull(stringBuilder, "String builder cannot be null");
    }
}
