/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao;

import java.util.Date;
import java.util.List;

import org.agnitas.service.JobDto;

public interface JobQueueDao {
	public List<JobDto> readUpcomingJobsForExecution();
	
	public JobDto getJob(int id);
	
	public JobDto getJob(String description);
	
	public boolean initJobStart(int id, Date nextStart);

	public boolean initJobStart(int id, Date nextStart, boolean manuallyOverride);
	
	public int resetJobsForCurrentHost();
	
	/**
	 * Update the jobs status only and ignore the parameters
	 */
	public boolean updateJob(JobDto job);
	
	public boolean deleteJob(int id);
	
	public List<JobDto> selectErrorneousJobs();
	
	public List<JobDto> getAllActiveJobs();
	
	public List<JobDto> getHangingJobs(Date timeLimit);
	
	public void writeJobResult(int job_id, Date time, String result, int durationInSeconds, String hostname);
	
	public boolean setStartCompanyForNextCleanupStart(int currentCompanyID);
	
	public int getStartCompanyForCleanup();
	
	public boolean deleteCleanupStartEntry();

	public boolean updateJobStatus(JobDto job);
}
