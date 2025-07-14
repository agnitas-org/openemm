/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import com.agnitas.service.JobWorker;
import org.apache.commons.lang3.StringUtils;

/**
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'DeletedContentblockCleaner', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '0135', CURRENT_TIMESTAMP, NULL, 'com.agnitas.service.job.DeletedContentblockCleanerJobWorker', 1);
 * 
 *  Optional:
 *  INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'DeletedContentblockCleaner'), 'retentionTime', '60');
 */
public class DeletedContentblockCleanerJobWorker extends JobWorker {
	
	/**
	 * Default retention time for undo records in days.
	 */
	public static final int DEFAULT_RETENTION_TIME = 60;
		
	@Override
	public String runJob() throws Exception {
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
		daoLookupFactory.getBeanDynamicTagDao().deleteDynamicTagsMarkAsDeleted(retentionTime);
		
		return null;
	}
}
