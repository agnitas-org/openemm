/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.voucher.exception;

/**
 * Exception indicating an invalid table base name.
 */
public class InvalidTableBaseNameException extends VoucherException {

	/** Serial version UID. */
	private static final long serialVersionUID = -3843489290529041514L;
	
	/** Table base name. */
	private final String name;
	
	/**
	 * Instantiates a new invalid table base name exception.
	 *
	 * @param tableBaseName the table base name
	 */
	public InvalidTableBaseNameException( String tableBaseName) {
		super( "Invalid table base name: " + tableBaseName);
		
		this.name = tableBaseName;
	}
	
	/**
	 * Gets the table base name.
	 *
	 * @return the table base name
	 */
	public String getTableBaseName() {
		return this.name;
	}
}
