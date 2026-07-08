/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import com.agnitas.dao.ImportRecipientsDao;
import com.agnitas.dao.JobQueueDao;
import com.agnitas.emm.core.auto_import.dao.AutoImportDao;
import com.agnitas.emm.core.autoexport.dao.AutoExportDao;
import com.agnitas.emm.core.birtreport.dao.BirtReportDao;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ResetJobQueueContextListener implements ServletContextListener {

	private static final Logger logger = LogManager.getLogger(ResetJobQueueContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
		String hostName = AgnUtils.getHostName();
		try {
            ServletContext servletContext = event.getServletContext();
			WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			
            JobQueueDao jobQueueDao = (JobQueueDao) springContext.getBean("JobQueueDao");

			jobQueueDao.resetJobsForCurrentHost();

            if (springContext.containsBean("AutoImportDao")) {
            	AutoImportDao autoImportDao = (AutoImportDao) springContext.getBean("AutoImportDao");
	            int resettedAutoImports = autoImportDao.resetAutoImportsForCurrentHost();
	            if (resettedAutoImports > 0) {
	    			logger.error("Resetting {} hanging AutoImports on startup formerly started by current host ({})", resettedAutoImports, hostName);
	    		}
            }

            if (springContext.containsBean("AutoExportDao")) {
            	AutoExportDao autoExportDao = (AutoExportDao) springContext.getBean("AutoExportDao");
	            int resettedAutoExports = autoExportDao.resetAutoExportsForCurrentHost();
	            if (resettedAutoExports > 0) {
	    			logger.error("Resetting {} hanging AutoExports on startup formerly started by current host ({})", resettedAutoExports, hostName);
	    		}
            }

            if (springContext.containsBean("BirtReportDao")) {
            	BirtReportDao birtReportDao = (BirtReportDao) springContext.getBean("BirtReportDao");
	            int resettedReports = birtReportDao.resetBirtReportsForCurrentHost();
	            if (resettedReports > 0) {
	    			logger.error("Resetting {} hanging BirtReports on startup formerly started by current host ({})", resettedReports, hostName);
	    		}
            }
		} catch (Exception e) {
			logger.error("ResetJobQueueContextListener init: {}", e.getMessage(), e);
		}
        
        try {
            ServletContext servletContext = event.getServletContext();
			WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			ImportRecipientsDao importRecipientsDao = (ImportRecipientsDao) springContext.getBean("ImportRecipientsDao");
            int droppedTables = importRecipientsDao.dropLeftoverTables(hostName);
            if (droppedTables > 0) {
    			logger.error("Dropped {} leftover import data tables on startup of current host ({})", droppedTables, hostName);
    		}
		} catch (Exception e) {
			logger.error("DropLeftoverTables at init: {}", e.getMessage(), e);
		}
	}
    
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do
	}
}
