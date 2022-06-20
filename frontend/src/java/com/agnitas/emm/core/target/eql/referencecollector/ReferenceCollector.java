/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.referencecollector;

/**
 * Interface for collecting referenced items (mailings, links, ...) in EQL code.
 */
public interface ReferenceCollector {

	/**
	 * Add referenced auto-import ID.
	 *
	 * @param autoImportId referenced auto-import ID.
	 */
	void addAutoImportReference(int autoImportId);

	/**
	 * Add referenced mailing ID.
	 * 
	 * @param mailingId referenced mailing ID
	 */
	void addMailingReference(int mailingId);

	/**
	 * Add referenced link ID.
	 * 
	 * @param mailingId referenced mailing ID
	 * @param linkId referenced link ID (can be null if no specific link is referenced)
	 */
	void addLinkReference(int mailingId, Integer linkId);

	/**
	 * Add referenced profile field name.
	 * 
	 * @param profileFieldName referenced profile field name
	 */
	void addProfileFieldReference(String profileFieldName);

	/**
	 * Add referenced reference table name.
	 * 
	 * @param referenceTableName name of referenced reference table
	 */
	void addReferenceTableReference(final String referenceTableName);

	/**
	 * Add referenced reference table name and column.
	 * 
	 * @param referenceTableName name of referenced reference table
	 * @param columnName name of column
	 */
	void addReferenceTableReference(String referenceTableName, String columnName);

}
