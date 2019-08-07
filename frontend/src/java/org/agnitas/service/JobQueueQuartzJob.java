/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import org.apache.log4j.Logger;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.scheduling.quartz.QuartzJobBean;

public class JobQueueQuartzJob extends QuartzJobBean {
	private static final transient Logger logger = Logger.getLogger(JobQueueQuartzJob.class);
	
	private JobQueueService jobQueueService;
	
	@Override
	protected void executeInternal(JobExecutionContext arg0) throws JobExecutionException {
		// look for queued jobs and execute them
		try {
			logger.debug("JobQueueQuartzJob was called");
			jobQueueService.checkAndRunJobs();
		} catch (Exception e) {
			logger.error("JobQueueQuartzJob call returned an error: " + e.getMessage(), e);
			throw new JobExecutionException("JobQueueQuartzJob call returned an error", e, false);
		}
	}
	
	public void setJobQueueService(JobQueueService jobQueueService) {
		this.jobQueueService = jobQueueService;
	}
}
