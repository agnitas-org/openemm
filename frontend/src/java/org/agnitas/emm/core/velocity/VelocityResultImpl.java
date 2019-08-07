/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

import org.agnitas.util.EventHandler;
import org.apache.struts.action.ActionErrors;

/**
 * Default implementation of {@link VelocityResult}.
 */
class VelocityResultImpl implements VelocityResult {

	/** Indicates successful executing of the Velocity script. */
	private final boolean successful;
	
	/** Velocity event handler collecting error messages. */
	private final EventHandler eventHandler;
	
	/**
	 * Creates a new VelocityresultImpl.
	 * 
	 * @param successful true, if executing of script was successful
	 * @param eventHandler Event handler that collected the script errors
	 */
	public VelocityResultImpl( boolean successful, EventHandler eventHandler) {
		this.successful = successful;
		this.eventHandler = eventHandler;
	}
	
	@Override
	public boolean wasSuccessful() {
		return this.successful;
	}

	@Override
	public boolean hasErrors() {
		return eventHandler.getErrors() != null && !eventHandler.getErrors().isEmpty();
	}

	@Override
	public ActionErrors getErrors() {
		if( hasErrors())
			return eventHandler.getErrors();
		else
			return null;
	}

}
