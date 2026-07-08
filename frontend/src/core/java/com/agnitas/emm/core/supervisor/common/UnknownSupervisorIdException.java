/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.supervisor.common;

/**
 * Exception indicating an unknown supervisor ID.
 */
public class UnknownSupervisorIdException extends SupervisorException {

	/** Serial version UID. */
	private static final long serialVersionUID = -2542235861801969820L;
	
	/** Supervisor ID. */
	private final int id;
	
	/**
	 * Instantiates a new unknown supervisor id exception.
	 *
	 * @param id unknown supervisor ID
	 */
	public UnknownSupervisorIdException(int id) {
		super("Unknown supervisor ID: " + id);
		
		this.id = id;
	}
	
	/**
	 * Returns the unknown ID.
	 * 
	 * @return unknown supervisor ID
	 */
	public int getId() {
		return this.id;
	}
}
