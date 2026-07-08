/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.common;

import java.util.Objects;

import com.agnitas.util.Version;

/**
 * Class representing an entry in the startup job list.
 */
public final class StartupJobEntry {

	/** ID of the entry. */
	private final int id;
	
	/** EMM version. */
	private final Version version;
	
	/** Class name of startup job. */
	private final String classname;
	
	/** Company ID definied in job entry. */
	private final int companyId;
	
	/** Flag, if job is enabled. */
	private final boolean enabled;
	
	/** State of job. */
	private final JobState state;
	
	/**
	 * Creates a new instance.
	 * 
	 * @param id ID of job
	 * @param version EMM version
	 * @param classname class name of startup job
	 * @param companyId company ID from job definition
	 * @param enabled flag if job is enabled
	 * @param state state of job
	 */
	public StartupJobEntry(final int id, final Version version, final String classname, final int companyId, final boolean enabled, final JobState state) {
		this.id = id;
		this.version = Objects.requireNonNull(version, "Version is null");
		this.classname = Objects.requireNonNull(classname, "Class name is null");
		this.companyId = companyId;
		this.enabled = enabled;
		this.state = Objects.requireNonNull(state, "Job state is null");
	}

	/**
	 * Returns the job ID. 
	 * 
	 * @return job ID
	 */
	public final int getId() {
		return this.id;
	}
	
	/**
	 * Returns the EMM version set for this job.
	 * @return EMM version for this job
	 */
	public final Version getVersion() {
		return version;
	}

	/**
	 * Name of class implementing the job.
	 * 
	 * @return name of job class
	 */
	public final String getClassname() {
		return classname;
	}

	/**
	 * Returns the company ID set in job definition.
	 * 
	 * @return company ID from job definition
	 */
	public final int getCompanyId() {
		return companyId;
	}

	/**
	 * Returns <code>true</code> if job is enabled.
	 * 
	 * @return <code>true</code> if job is enabled
	 */
	public final boolean isEnabled() {
		return enabled;
	}

	/**
	 * Returns the state of the job.
	 * 
	 * @return job state
	 */
	public final JobState getState() {
		return state;
	}
	
}
