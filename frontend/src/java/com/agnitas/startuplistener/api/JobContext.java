/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.api;

import java.util.Objects;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.springframework.context.ApplicationContext;

/**
 * Context for startup job.
 * 
 * This class provides additional data that may be useful for execution of a startup job.
 */
public final class JobContext {
	
	/** ID of current job. */
	private final int jobId;

	/** Company ID specified in startup job definition. */
	private final int companyId;
	
	/** JDBC data source. */
	private final DataSource dataSource;
	
	/** Configuration service. */
	private final ConfigService configService;
	
	/** Springs application context. */
	private final ApplicationContext applicationContext;
	
	/**
	 * Creates a new context.
	 * 
	 * @param jobId ID of current job
	 * @param companyId company ID from job definition.
	 * @param dataSource JDBC data source
	 * @param configService configuration service
	 * 
	 * @throws NullPointerException if a parameter is <code>null</code>
	 */
	public JobContext(final int jobId, final int companyId, final DataSource dataSource, final ConfigService configService, final ApplicationContext applicationContext) {
		this.jobId = jobId;
		this.companyId = companyId;
		this.dataSource = Objects.requireNonNull(dataSource, "JDBC DataSource is null");
		this.configService = Objects.requireNonNull(configService, "ConfigService is null");
		this.applicationContext = Objects.requireNonNull(applicationContext, "Application context is nulL");
	}

	/**
	 * Returns the ID of the current job.
	 * 
	 * @return ID of the current job
	 */
	public final int getJobId() {
		return jobId;
	}

	/**
	 * Returns the company ID from job definition. 
	 * 
	 * @return company ID from job definition
	 */
	public final int getCompanyId() {
		return companyId;
	}

	/**
	 * Returns the JDBC data source.
	 * 
	 * @return JDBC data source
	 */
	public final DataSource getDataSource() {
		return dataSource;
	}

	/**
	 * Returns the configuration service.
	 * 
	 * @return configuration service
	 */
	public final ConfigService getConfigService() {
		return configService;
	}

	/**
	 * Returns Springs application context.
	 * 
	 * @return Springs application context
	 */
	public final ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
}
