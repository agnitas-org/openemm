/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

import org.apache.struts.action.ActionErrors;

/**
 * Result of Velocity evaluation.
 */
public interface VelocityResult {
	
	/**
	 * Checks, if the evaluation was successful.
	 * 
	 * @return true, if evaluation was successful
	 */
	public boolean wasSuccessful();
	
	/**
	 * Checks, if errors occured.
	 * 
	 * @return true, if errors occured
	 */
	public boolean hasErrors();	
	
	/**
	 * Returns the errors occured during evaluation.
	 * 
	 * @return errors
	 */
	public ActionErrors getErrors();					// TODO: That's not quite good. We get a dependency to the view layer. This was done to keep refactoring smaller.
}
