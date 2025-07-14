/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.scriptvalidator;

public class IllegalVelocityDirectiveException extends ScriptValidationException {

	/** Serial version UID. */
	private static final long serialVersionUID = -8453373390363036429L;
	
	private final String directive;
	
	public IllegalVelocityDirectiveException( String directive) {
		super( "Velocity script contains illegal directive '" + directive + "'");
		
		this.directive = directive;
	}

	public String getDirective() {
		return this.directive;
	}

}
