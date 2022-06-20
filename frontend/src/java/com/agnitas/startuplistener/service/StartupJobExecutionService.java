/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.service;

import java.io.File;

import com.agnitas.startuplistener.api.StartupJobException;
import com.agnitas.startuplistener.common.JobState;

/**
 * Service interface to run startup jobs.
 */
public interface StartupJobExecutionService {

	/**
	 * Runs all pending jobs.
	 * 
	 * To run a job, the job must be
	 * <ul>
	 *   <li>whitelisted in given file</li>
	 *   <li>enabled and </li>
	 *   <li>in state {@link JobState#PENDING}</li>
	 * </ul>
	 * 	
	 * @param whitelistFile file containing whitelist
	 * 
	 * @return result of execution
	 * 
	 * @throws StartupJobException if loading whitelist failed
	 */
	public ExecutionResult executeAllPendingJobs(final File whitelistFile) throws StartupJobServiceException;

}
