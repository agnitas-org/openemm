/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.beans;

/**
 * An interface that all the web-storage data bundle classes must implement.
 *
 * There are a few other conventions that your class should conform:
 * - provide default constructor;
 * - use default values for all the fields (just in case some or all properties are invalid or missing from browser's local storage);
 * - validate passed values in setters (never trust values coming from browser's local storage);
 * - use annotations {@link com.fasterxml.jackson.annotation} to have a control over JSON (de-) serialization.
 */
public interface WebStorageEntry extends Cloneable {
    /**
     * Keep in mind to implement this method properly to make sure that {@link com.agnitas.service.WebStorage#get(org.agnitas.service.WebStorageBundle)}
     * works as intended.
     */
    WebStorageEntry clone() throws CloneNotSupportedException;
}
