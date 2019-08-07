/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity;

import org.apache.log4j.Logger;
import org.apache.velocity.app.event.IncludeEventHandler;

/**
 * Implementation of {@link IncludeEventHandler} to avoid accessing external resources.
 */
public class IncludeParsePreventionHandler implements IncludeEventHandler {
	
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger( IncludeParsePreventionHandler.class);
	
	@Override
	public String includeEvent(String includeResourcePath, String currentResourcePath, String directiveName) {
		logger.warn( "Script attempts to use Velocity directive '" + directiveName + "' on resource '" + includeResourcePath + "' (" + currentResourcePath + ")");
		
		return null;	// According to Velocity API, null blocks inclusion of references resource (see API of IncludeEventHandler#includeEvent(...))
	}

}
