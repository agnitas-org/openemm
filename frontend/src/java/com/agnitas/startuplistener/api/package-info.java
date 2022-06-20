/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

/**
 * This is the public API that is used when one wants to implement a new startup job.
 * 
 * A startup job must implement the interface {@link com.agnitas.startuplistener.api.StartupJob}. 
 * 
 * Errors must be signaled by a {@link com.agnitas.startuplistener.api.StartupJobException}. This exception does the following:
 * <ul>
 *   <li>The job is marked as <i>failed</i> in the job list</li>
 *   <li>The job does not abort the execution of the job list. Execution will continue with the subsequent job.</li>
 * </ul> 
 */
package com.agnitas.startuplistener.api;

