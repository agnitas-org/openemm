/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.agnitas.dao.ImportRecipientsDao;
import org.agnitas.dao.JobQueueDao;
import org.agnitas.emm.core.autoexport.dao.AutoExportDao;
import org.agnitas.emm.core.autoimport.dao.AutoImportDao;
import org.agnitas.util.AgnUtils;
import org.apache.log4j.Logger;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

public class ResetJobQueueContextListener implements ServletContextListener {
	private static final transient Logger logger = Logger.getLogger(ResetJobQueueContextListener.class);

    @Override
    public void contextInitialized(ServletContextEvent event) {
        try {
            ServletContext servletContext = event.getServletContext();
			WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			
            JobQueueDao jobQueueDao = (JobQueueDao) springContext.getBean("JobQueueDao");
            int resettedJobs = jobQueueDao.resetJobsForCurrentHost();
            if (resettedJobs > 0) {
    			logger.error("Resetting " + resettedJobs + " hanging jobs on startup formerly started by current host (" + AgnUtils.getHostName() + ")");
    		}

            if (springContext.containsBean("AutoImportDao")) {
            	AutoImportDao autoImportDao = (AutoImportDao) springContext.getBean("AutoImportDao");
	            int resettedAutoImports = autoImportDao.resetAutoImportsForCurrentHost();
	            if (resettedAutoImports > 0) {
	    			logger.error("Resetting " + resettedAutoImports + " hanging AutoImports on startup formerly started by current host (" + AgnUtils.getHostName() + ")");
	    		}
            }

            if (springContext.containsBean("AutoExportDao")) {
            	AutoExportDao autoExportDao = (AutoExportDao) springContext.getBean("AutoExportDao");
	            int resettedAutoExports = autoExportDao.resetAutoExportsForCurrentHost();
	            if (resettedAutoExports > 0) {
	    			logger.error("Resetting " + resettedAutoExports + " hanging AutoExports on startup formerly started by current host (" + AgnUtils.getHostName() + ")");
	    		}
            }
		} catch (Exception e) {
			logger.error("ResetJobQueueContextListener init: " + e.getMessage(), e);
		}
        
        try {
            ServletContext servletContext = event.getServletContext();
			WebApplicationContext springContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
			ImportRecipientsDao importRecipientsDao = (ImportRecipientsDao) springContext.getBean("ImportRecipientsDao");
            int droppedTables = importRecipientsDao.dropLeftoverTables(AgnUtils.getHostName());
            if (droppedTables > 0) {
    			logger.error("Dropped " + droppedTables + " leftover import data tables on startup of current host (" + AgnUtils.getHostName() + ")");
    		}
		} catch (Exception e) {
			logger.error("DropLeftoverTables at init: " + e.getMessage(), e);
		}
	}
    
	@Override
	public void contextDestroyed(ServletContextEvent event) {
		// nothing to do
	}
}
