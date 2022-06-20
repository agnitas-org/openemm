/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.uid.parser.impl;

import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

import org.agnitas.emm.core.commons.uid.parser.ExtensibleUIDParser;
import org.agnitas.emm.core.commons.uid.parser.exception.DeprecatedUIDVersionException;
import org.agnitas.emm.core.commons.uid.parser.exception.InvalidUIDException;
import org.agnitas.emm.core.commons.uid.parser.exception.UIDParseException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.commons.uid.ExtensibleUidVersion;

/**
 * Implementation of ExtensibleUIDParser interface to register a list of parsers.
 * This list is processed parser by parser until the first parser can successfully parse the given UID.
 * A UIDParseException indicates that no parser could handle the UID string.
 */
public final class ComExtensibleUIDParserChain implements ExtensibleUIDParser {

    /** Logger. */
    private static final Logger logger = LogManager.getLogger(ComExtensibleUIDParserChain.class);

    /** List of parsers. */
    private List<ExtensibleUIDParser> parserList;

    public ComExtensibleUIDParserChain() {
        parserList = new Vector<>();
    }

    @Override
    public final ComExtensibleUID parse(final String uidString) throws UIDParseException, DeprecatedUIDVersionException {
        final List<ExtensibleUIDParser> filteredParsers = parserList.stream()
                .filter(parser -> parser.isSupportedUidFormat(uidString))
                .collect(Collectors.toList());

        if (filteredParsers.isEmpty()) {
            throw new UIDParseException("No appropriate parser to the UID. Incorrect format of the uid string.", uidString);
        }

        if(logger.isInfoEnabled()) {
        	logger.info(String.format("Chain parser is trying to parse UID '%s'", uidString));
        }
        	
        for (final ExtensibleUIDParser parser : filteredParsers) {
            final String parserName = parser.getClass().getCanonicalName();
            
            if(logger.isInfoEnabled()) {
            	logger.info(String.format("Trying to decode the uid by parser. uid: %s, parserName: %s", uidString, parserName));
            }
            
            try {
                final ComExtensibleUID uid = parser.parse(uidString);
                if (Objects.nonNull(uid)) {
                	if(logger.isInfoEnabled()) {
                    	logger.info(String.format("UID parsed successfully. uid: %s, parserName: %s", uidString, parserName));

                	}
                	
                    return uid;
                }
            } catch (final UIDParseException e) {
            	if(logger.isInfoEnabled()) {
            		logger.info(String.format("UID '%s' could not be parsed by parser '%s'", uidString, parserName), e);
            	}
            	
            	// Try next parser in chain
            } catch (final InvalidUIDException e) {
            	if(logger.isInfoEnabled()) {
            		logger.info(String.format("UID '%s' is invalid (parser '%s')", uidString, parserName), e);
            	}

            	// Try next parser in chain
            } catch (final DeprecatedUIDVersionException e) {
            	if(logger.isInfoEnabled()) {
            		logger.info(String.format("Version of UID '%s' is deprepcated (parser '%s')", uidString, parserName), e);
            	}

                // We found a deprecated UID version, so we stop processing here.
                // We assume, that the following parser handles older UID version.
                throw e;
            }
        }

        throw new UIDParseException("No registered parser could handle the UID. See log messages above for more details.", uidString);
    }

    @Override
    public final boolean isSupportedUidFormat(final String uidString) {
        return parserList.stream().anyMatch(parser -> parser.isSupportedUidFormat(uidString));
    }

    @Override
    public final ExtensibleUidVersion getHandledUidVersion() {
    	// can return null because this method is called by this chain only
        return null;
    }

    /**
     * Set list of parsers.
     *
     * @param list list of parsers
     */
    @Required
    public final void setParserList(final List<ExtensibleUIDParser> list) {
        this.parserList = new Vector<>(list);
    }
}
