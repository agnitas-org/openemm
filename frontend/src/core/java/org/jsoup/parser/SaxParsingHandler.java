/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.jsoup.parser;

import org.jsoup.nodes.Attributes;
import org.xml.sax.SAXException;

public interface SaxParsingHandler {
    @SuppressWarnings("unused")	// Required for throws-clause
    default void onDoctype(String name, String publicId, String systemId, boolean isForceQuirks) throws SAXException {
		// nothing to do
    }

    @SuppressWarnings("unused")	// Required for throws-clause
    default void onOpeningTag(String name, Attributes attributes, boolean isStandalone) throws SAXException {
		// nothing to do
    }

    @SuppressWarnings("unused")	// Required for throws-clause
    default void onClosingTag(String name) throws SAXException {
		// nothing to do
    }

    @SuppressWarnings("unused")	// Required for throws-clause
    default void onComment(String data) throws SAXException {
		// nothing to do
    }

    @SuppressWarnings("unused")	// Required for throws-clause
	default void onCharacter(String data) throws SAXException {
		// nothing to do
    }

    @SuppressWarnings("unused")	// Required for throws-clause
    default void onEnd() throws SAXException {
		// nothing to do
    }
}
