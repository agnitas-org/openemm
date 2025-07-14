/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.dao;

import com.agnitas.emm.core.serverstatus.forms.JobQueueOverviewFilter;
import com.agnitas.service.JobDto;

import java.util.Date;
import java.util.List;
import java.util.Map;

public interface JobQueueDao {

	List<JobDto> readUpcomingJobsForExecution();

    List<JobDto> getJobsWithLostNextStart();

	JobDto getJob(int id);
	
	JobDto getJob(String description);
	
	boolean initJobStart(int id, Date nextStart);

	boolean initJobStart(int id, Date nextStart, boolean manuallyOverride);
	
	int resetJobsForCurrentHost();
	
	/**
	 * Update the jobs status only and ignore the parameters
	 */
	boolean updateJob(JobDto job);
	
	boolean deleteJob(int id);
	
	List<JobDto> selectErroneousJobs();
	
	List<JobDto> getOverview(JobQueueOverviewFilter filter);
	int getCountForOverview();
	List<JobDto> getAllActiveJobs();

	List<JobDto> getHangingJobs(Date timeLimit);
	
	void writeJobResult(int job_id, Date time, String result, int durationInSeconds, String hostname);
	
	boolean updateJobStatus(JobDto job);

	void acknowledgeErroneousJob(int idToAcknowledge);

	void storeDynamicJobParameter(int jobID, String parameterName, String parameterValue);

	List<Map<String, Object>> getLastJobResults(int job_id);

	List<JobDto> selectCriticalErroneousJobs();
}
