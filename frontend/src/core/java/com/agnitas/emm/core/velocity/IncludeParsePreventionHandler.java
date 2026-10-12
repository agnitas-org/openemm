/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.velocity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.event.IncludeEventHandler;
import org.apache.velocity.context.Context;

/**
 * Implementation of {@link IncludeEventHandler} to avoid accessing external resources.
 * <p>
 * Instantiated reflectively by Velocity via its fully-qualified name (see {@link AbstractVelocityWrapper},
 * {@code RuntimeConstants.EVENTHANDLER_INCLUDE}), not referenced directly anywhere in Java code.
 */
@SuppressWarnings("unused")
public class IncludeParsePreventionHandler implements IncludeEventHandler {

	private static final Logger logger = LogManager.getLogger(IncludeParsePreventionHandler.class);

	@Override
	public String includeEvent(Context context, String includeResourcePath, String currentResourcePath, String directiveName) {
		logger.warn("Script attempts to use Velocity directive '{}' on resource '{}' ({})", directiveName, includeResourcePath, currentResourcePath);
		
		return null;	// According to Velocity API, null blocks inclusion of references resource (see API of IncludeEventHandler#includeEvent(...))
	}

}
