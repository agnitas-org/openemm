/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import java.sql.DriverManager;
import java.sql.SQLException;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DerbyShutdownContextListener implements ServletContextListener {

	private static final Logger logger = LogManager.getLogger(DerbyShutdownContextListener.class);

	/**
	 * The Derby Embedded Driver restricts access to a single JVM, so failing to shut down the database
	 * properly can cause file locking issues or prevent the application from restarting cleanly.
	 * <p>
	 * The method uses the Derby shutdown mechanism by attempting a connection to the database with the URL param
	 * "shutdown=true" <a href="https://db.apache.org/derby/papers/DerbyTut/embedded_intro.html">More info</a>.
	 * This instructs Derby to flush memory and release resources ensuring a proper db shutdown.
	 * <p>
	 * During the shutdown, Derby intentionally throws "Database shutdown" SQLException (code = 50000).
	 * It's caught and ignored, as it signifies a successful shutdown.
	 */
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		try {
			DriverManager.getConnection("jdbc:derby:;shutdown=true");
		} catch (SQLException e) {
			if (e.getErrorCode() == 50000 && "XJ015".equals(e.getSQLState()) && logger.isInfoEnabled()) {
				logger.info("Derby shut down normally.");
			} else {
				logger.error("Derby did not shut down normally: {}", e.getMessage(), e);
			}
		}
	}
}
