/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.util.quartz;

import org.agnitas.emm.core.logintracking.dao.LoginTrackDao;
import com.agnitas.service.JobWorker;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;

/**
 * Abstract cleaner for login tracking.
 * 
 * Job parameters:
 * 
 * <ul>
 *   <li><i>retentionTimeHours</i> Retention time of records in hours</li>
 *   <li><i>retentionTime</li> Retention time of records in days. Not used if <i>retentionTimeHours</i> is given.</li>
 *   <li><i>deleteBlockSize</li> Number of records deleted in one block</li>
 * </ul>
 *
 */
public abstract class AbstractLoginTrackTableCleanerJobWorker extends JobWorker {
		
	@Override
	public final String runJob() throws Exception {
		int retentionTimeHours;
		try {
			if(!StringUtils.isBlank(job.getParameters().get("retentionTimeHours"))) {
				retentionTimeHours = Integer.parseInt(job.getParameters().get("retentionTimeHours"));
			} else if(!StringUtils.isBlank(job.getParameters().get("retentionTime"))) {
				retentionTimeHours = Integer.parseInt(job.getParameters().get("retentionTime")) * 24;
			} else {
				retentionTimeHours = getDefaultRetentionTimeHours();
			}
		} catch (Exception e) {
			throw new Exception("Parameter retentionTime is missing or invalid", e);
		}
		
		int deleteBlockSize;
		try {
			if (StringUtils.isBlank(job.getParameters().get("deleteBlockSize"))) {
				deleteBlockSize = getDefaultBlockSize();
			} else {
				deleteBlockSize = Integer.parseInt(job.getParameters().get("deleteBlockSize"));
			}
		} catch (Exception e) {
			throw new Exception("Parameter deleteBlockSize is missing or invalid", e);
		}
		
		workWithLoginTrackDao(getLoginTrackDao(), retentionTimeHours, deleteBlockSize);
		
		return null;
	}
	
	public abstract LoginTrackDao getLoginTrackDao();
	public abstract Logger getLogger();
	public abstract int getDefaultRetentionTimeHours();
	public abstract int getDefaultBlockSize();
	
	private final void workWithLoginTrackDao(final LoginTrackDao loginTrackDao, final int retentionTimeHours, final int deleteBlockSize) {
		if(loginTrackDao == null) {
			getLogger().error("no LoginTrackDao object defined");
		} else {
			int affectedRows;
			// Delete in blocks
			while((affectedRows = loginTrackDao.deleteOldRecordsHours(retentionTimeHours, deleteBlockSize)) > 0) {
				if (getLogger().isInfoEnabled()) {
					getLogger().info("deleted " + affectedRows + " records");
				}
			}
		}
		
	}
}
