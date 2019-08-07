/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.download.service;

/**
 * Exception indication an unknown download ID:
 */
public class DownloadIdNotFoundException extends Exception {

	/** Serial version UID. */
	private static final long serialVersionUID = 5159045007834924781L;
	
	/** Unknown download ID. */
	private final String id;
	
	/**
	 * Creates a new exception with given unknown download ID.
	 * 
	 * @param id unknown download ID
	 */
	public DownloadIdNotFoundException( String id) {
		super( "Unknown download ID: " + id);
		
		this.id = id;
	}
	
	/**
	 * Returns the download ID.
	 * 
	 * @return unknown download ID
	 */
	public String getDownloadId() {
		return this.id;
	}
}
