/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.web;

import java.io.File;

import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.agnitas.startuplistener.service.ExecutionResult;
import com.agnitas.startuplistener.service.StartupJobExecutionService;
import com.agnitas.util.web.WebAppFileUtil;

/**
 * Implementation of {@link ServletContextListener} running startup jobs.
 */
public final class StartUpExecutionListener implements ServletContextListener {

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(StartUpExecutionListener.class);

	@Override
	public final void contextInitialized(final ServletContextEvent servletContextEvent) {
		try {
			final WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContextEvent.getServletContext());
			final StartupJobExecutionService executionService = webApplicationContext.getBean("StartupJobExecutionService", StartupJobExecutionService.class);
			
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Starting execution of startup jobs");
			}
			
			final File whitelistFile = new File(WebAppFileUtil.getWebInfDirectoryFile(servletContextEvent.getServletContext()), "startup-jobs.whitelist");
			final ExecutionResult result = executionService.executeAllPendingJobs(whitelistFile);
			
			logResult(result);
		} catch (final Exception e) {
			LOGGER.error("Error processing startup jobs", e);
		}
	}

	@Override
	public final void contextDestroyed(final ServletContextEvent servletContextEvent) {
		// Do nothing.
	}

	/**
	 * Logs the result of the execution of the job list.
	 * The log level depends on whether failed jobs exists or not.
	 * 
	 * @param result execution result to log
	 */
	private final void logResult(final ExecutionResult result) {
		final String msg = String.format("Executed %d startup jobs (%d success, %d failures)", result.getTotalCount(), result.getSuccessCount(), result.getFailureCount());
		
		if (result.getFailureCount() > 0) {
			LOGGER.error(msg);
		} else {
			LOGGER.info(msg);
		}
	}}
