/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.web;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

/**
 * In case of an error the webcontainer restarts itself. It could happen that the persisted sessions will move to an other user...
 * It will be better to cleanup the existing session before startup
 */
public class SessionCleanUpListener implements ServletContextListener {
	private static final transient Logger logger = Logger.getLogger(SessionCleanUpListener.class);

	private static final String SESSIONFILESTORE = "sessionfilestore";

	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do
	}

	@Override
	public void contextInitialized(ServletContextEvent event) {
		String sessionfilestoreLocation = event.getServletContext().getRealPath(event.getServletContext().getInitParameter(SESSIONFILESTORE));
		if (sessionfilestoreLocation != null) {
			File sessionfilestoreDirectory = new File(sessionfilestoreLocation);
			if (sessionfilestoreDirectory.exists() && sessionfilestoreDirectory.isDirectory()) {
				try {
					FileUtils.deleteDirectory(sessionfilestoreDirectory);
				} catch (IOException exception) {
					logger.fatal("Sessionfilestore Cleanup: Could not delete sessionfilestore: " + sessionfilestoreLocation, exception);
				}
			} else if (logger.isInfoEnabled()) {
				logger.info("Sessionfilestore Cleanup: The provided context-parameter 'sessionfilestore' <" + sessionfilestoreLocation + "> does not exist or is not a directory");
			}
		}
	}
}
