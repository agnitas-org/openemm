/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.util.quartz;

import org.agnitas.emm.core.logintracking.dao.LoginTrackDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'LoginTrackTableCleaner', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '0400', CURRENT_TIMESTAMP, NULL, 'org.agnitas.util.quartz.LoginTrackTableCleanerJobWorker', 1);
 * 
 *  Optional:
 *  INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'LoginTrackTableCleaner'), 'retentionTime', '7');
 *  INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'LoginTrackTableCleaner'), 'deleteBlockSize', '1000');
 */
public class LoginTrackTableCleanerJobWorker extends AbstractLoginTrackTableCleanerJobWorker {
	
	/**
	 * Default value of retention time (in days) for old records.
	 */
	public static final int DEFAULT_RETENTION_TIME_DAYS = 60;
	
	/**
	 * Number of records deleted with one statement.
	 */
	public static final int DEFAULT_DELETE_BLOCK_SIZE = 1000;

	/** The logger. */
	private static final transient Logger LOGGER = LogManager.getLogger(LoginTrackTableCleanerJobWorker.class);
	
	@Override
	public LoginTrackDao getLoginTrackDao() {
		return daoLookupFactory.getBeanGuiLoginTrackDao();
	}

	@Override
	public Logger getLogger() {
		return LOGGER;
	}

	@Override
	public int getDefaultRetentionTimeHours() {
		return DEFAULT_RETENTION_TIME_DAYS * 24;
	}

	@Override
	public int getDefaultBlockSize() {
		return DEFAULT_DELETE_BLOCK_SIZE;
	}
}
