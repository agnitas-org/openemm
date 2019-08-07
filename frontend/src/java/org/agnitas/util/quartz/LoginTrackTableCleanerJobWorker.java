/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.quartz;

import org.agnitas.dao.LoginTrackDao;
import org.agnitas.service.JobWorker;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'LoginTrackTableCleaner', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '0400', CURRENT_TIMESTAMP, NULL, 'org.agnitas.util.quartz.LoginTrackTableCleanerJobWorker', 1);
 *    
 *  Optional:
 *  INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'LoginTrackTableCleaner'), 'retentionTime', '7');
 *  INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'LoginTrackTableCleaner'), 'deleteBlockSize', '1000');
 */
public class LoginTrackTableCleanerJobWorker extends JobWorker {
	private static final transient Logger logger = Logger.getLogger(LoginTrackTableCleanerJobWorker.class);
	
	/**
	 * Default value of retention time (in days) for old records.
	 */
	public static final int DEFAULT_RETENTION_TIME = 60;
	
	/**
	 * Number of records deleted with one statement.
	 */
	public static final int DEFAULT_DELETE_BLOCK_SIZE = 1000;
		
	@Override
	public void runJob() throws Exception {
		int retentionTime;
		try {
			if (StringUtils.isBlank(job.getParameters().get("retentionTime"))) {
				retentionTime = DEFAULT_RETENTION_TIME;
			} else {
				retentionTime = Integer.parseInt(job.getParameters().get("retentionTime"));
			}
		} catch (Exception e) {
			throw new Exception("Parameter retentionTime is missing or invalid", e);
		}
		
		int deleteBlockSize;
		try {
			if (StringUtils.isBlank(job.getParameters().get("deleteBlockSize"))) {
				deleteBlockSize = DEFAULT_DELETE_BLOCK_SIZE;
			} else {
				deleteBlockSize = Integer.parseInt(job.getParameters().get("deleteBlockSize"));
			}
		} catch (Exception e) {
			throw new Exception("Parameter deleteBlockSize is missing or invalid", e);
		}
		
		LoginTrackDao loginTrackDao = daoLookupFactory.getBeanLoginTrackDao();//((LoginTrackDao) applicationContext.getBean("LoginTrackDao"));
		
		int affectedRows;
		
		if(loginTrackDao == null) {
			logger.error("no LoginTrackDao object defined - job stopped");
		} else {
			// Delete in blocks 
			while((affectedRows = loginTrackDao.deleteOldRecords(retentionTime, deleteBlockSize)) > 0) {
				if (logger.isInfoEnabled()) logger.info("deleted " + affectedRows + " records");
			}
		}
	}
}
