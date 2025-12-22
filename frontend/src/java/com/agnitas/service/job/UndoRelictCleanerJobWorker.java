/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import java.util.List;

import com.agnitas.dao.UndoDynContentDao;
import com.agnitas.dao.UndoMailingComponentDao;
import com.agnitas.dao.UndoMailingDao;
import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.quartz.JobWorker;
import org.apache.commons.lang3.StringUtils;

/**
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    VALUES ((SELECT MAX(id) + 1 from job_queue_tbl), 'UndoRelictCleaner', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '0130', CURRENT_TIMESTAMP, NULL, 'com.agnitas.service.job.UndoRelictCleanerJobWorker', 1);
 * 
 *  Optional:
 *  INSERT INTO job_queue_parameter_tbl (job_id, parameter_name, parameter_value) VALUES ((SELECT id FROM job_queue_tbl WHERE description = 'UndoRelictCleaner'), 'retentionTime', '60');
 */
@JobWorker("UndoRelictCleaner")
public class UndoRelictCleanerJobWorker extends JobWorkerBase {
	
	/**
	 * Default retention time for undo records in days.
	 */
	public static final int DEFAULT_RETENTION_TIME = 60;
	
	public static final int UNDO_ID_BLOCK_SIZE = 10;
		
	@Override
	public String runJob() throws Exception {
		final int retentionTime = readRetentionTime();
		final List<Integer> undoIdList = findUndoIdsToCleanup(retentionTime);
		
		doCleanUp(undoIdList);
		
		return null;
	}
	
	private final List<Integer> findUndoIdsToCleanup(final int retentionTime) {
        return daoLookupFactory.getBeanUndoMailingDao().findUndoIdsToCleanup(retentionTime);

	}
	
	private final int readRetentionTime() throws Exception {
		try {
			if (StringUtils.isBlank(job.getParameters().get("retentionTime"))) {
				return DEFAULT_RETENTION_TIME;
			} else {
				return Integer.parseInt(job.getParameters().get("retentionTime"));
			}
		} catch (Exception e) {
			throw new Exception("Parameter retentionTime is missing or invalid", e);
		}
	}
	
	void doCleanUp(List<Integer> undoIds){
		undoIds.sort(Integer::compare);

		for(final int undoId : undoIds) {
			doCleanUp(undoId);
		}
	}
	
	void doCleanUp(final int undoId) {
        final UndoMailingDao undoMailingDao = daoLookupFactory.getBeanUndoMailingDao();
        final UndoMailingComponentDao undoMailingComponentDao = daoLookupFactory.getBeanUndoMailingComponentDao();
        final UndoDynContentDao undoDynContentDao = daoLookupFactory.getBeanUndoDynContentDao();

        undoMailingComponentDao.deleteUndoData(undoId);
		undoDynContentDao.deleteUndoData(undoId);
		undoMailingDao.deleteUndoData(undoId);
	}
}
