/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.target.eql.codegen.resolver;

/**
 * This exception indicates, that the given profile field name could not be resolved.
 * (Problem with database, profile field unknown, ...)
 */
public class UnknownProfileFieldException extends ProfileFieldResolveException {

	/** Serial version UID. */
	private static final long serialVersionUID = -3003677585008204538L;
	
	/** Name of profile field. */
	private final String name;
	
	/**
	 * Creates a new exception.
	 * 
	 * @param name name of profile field
	 */
	public UnknownProfileFieldException(String name) {
		super("Unknown profile field '" + name + "'");
		
		this.name = name;
	}
	
	/**
	 * Returns the name of the profile field.
	 * 
	 * @return name of profile field
	 */
	public String getName() {
		return this.name;
	}
}
