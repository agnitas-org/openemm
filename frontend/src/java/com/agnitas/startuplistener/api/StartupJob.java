/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.api;

/**
 * Implement this interface when coding a startup job.
 */
public interface StartupJob {

	/**
	 * Method called by the startup listener. The context contains additional data which may be useful 
	 * for execution.
	 * 
	 * If the job throws a {@link StartupJobException}, the job is marked as "failed". This exception does not avoid
	 * the execution of subsequent jobs.
	 * 
	 * @param context context
	 * 
	 * @throws StartupJobException on errors executing the job
	 */
	
	/*
	 * Dear coder:
	 * 
	 * If you want to add additional parameters that are required for execution of a job, do not add them
	 * as method parameters here. Add those parameters to the job context! 
	 */
	public void runStartupJob(final JobContext context) throws StartupJobException;
	
}
