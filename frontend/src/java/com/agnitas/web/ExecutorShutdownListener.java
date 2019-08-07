/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.concurrent.ExecutorService;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ExecutorShutdownListener implements ServletContextListener {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ExecutorShutdownListener.class);
	
	@Override
	public void contextDestroyed(ServletContextEvent servletContextEvent) {
    	try {
			ServletContext servletContext = servletContextEvent.getServletContext();
			WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			ExecutorService workerExecutorService = (ExecutorService) webApplicationContext.getBean("WorkerExecutorService");
			Thread.sleep(1000);
			logger.info("Shutting down WorkerExecutorService");
			int retryCount = 0;
			while (!workerExecutorService.isTerminated() && retryCount < 10) {
				if (retryCount > 0) {
					logger.error("WorkerExecutorService shutdown retryCount: " + retryCount);
					Thread.sleep(1000);
				}
				workerExecutorService.shutdownNow();
				retryCount++;
			}
    	} catch (Exception e) {
			logger.error("Cannot shutdown WorkerExecutorService: " + e.getMessage(), e);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		// Do nothing
	}
}
