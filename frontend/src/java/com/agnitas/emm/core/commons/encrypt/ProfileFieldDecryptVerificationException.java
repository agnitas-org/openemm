/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.commons.encrypt;

/**
 * Exception indicating verification errors after decrypting data.
 */
public class ProfileFieldDecryptVerificationException extends ProfileFieldEncryptorException {

	/** Serial version UID. */
	private static final long serialVersionUID = 3502482517183729699L;

	/**
	 * Creates a new exception with given error message
	 * 
	 * @param msg error message
	 */
	public ProfileFieldDecryptVerificationException(String msg) {
		super(msg);
	}
	
}
