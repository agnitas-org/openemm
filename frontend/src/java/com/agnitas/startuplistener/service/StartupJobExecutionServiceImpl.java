/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.startuplistener.service;

import java.lang.reflect.Constructor;
import java.util.List;
import java.util.Objects;

import javax.sql.DataSource;

import org.agnitas.emm.core.commons.util.ConfigService;
import org.apache.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.agnitas.startuplistener.api.JobContext;
import com.agnitas.startuplistener.api.StartupJob;
import com.agnitas.startuplistener.common.JobState;
import com.agnitas.startuplistener.common.StartupJobEntry;
import com.agnitas.startuplistener.common.StartupJobEntryByVersionComparator;
import com.agnitas.startuplistener.dao.StartupJobEntryDao;

public final class StartupJobExecutionServiceImpl implements StartupJobExecutionService, ApplicationContextAware {

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(StartupJobExecutionServiceImpl.class);
	
	private StartupJobEntryDao startupJobEntryDao;
	
	private DataSource dataSource;
	private ConfigService configService;
	private ApplicationContext applicationContext;
	
	@Override
	public final ExecutionResult executeAllPendingJobs() {
		// List all jobs to execute
		final List<StartupJobEntry> entries = this.startupJobEntryDao.listActiveAndPendingJobs();
		
		// Execute jobs in order sorted ascending by version number
		return entries.stream().sorted(new StartupJobEntryByVersionComparator()).map(entry -> executeJob(entry)).collect(ExecutionResultCollector.sumUpResults());
	}
	
	private final ExecutionResult executeJob(final StartupJobEntry entry) {
		// Additional security checks
		if(!entry.isEnabled()) {
			LOGGER.error(String.format("Attempt to execute disabled startup job %d", entry.getId()));
			
			return new ExecutionResult(0, 1);
		} 
		if(entry.getState() != JobState.PENDING) {
			LOGGER.error(String.format("Attempt to execute startup job %d, which is in %s state", entry.getId(), entry.getState()));
			
			return new ExecutionResult(0, 1);
		} 
		
		// Do the job
		try {
			// Mark job as "in progress"
			this.startupJobEntryDao.updateJobState(entry.getId(), JobState.IN_PROGRESS);
			
			final StartupJob job = instantiateJob(entry);
			final JobContext context = createJobContext(entry);
		
			job.runStartupJob(context);
			
			// Mark job as "done"
			this.startupJobEntryDao.updateJobState(entry.getId(), JobState.DONE);
			
			return new ExecutionResult(1, 0);
		} catch(final ReflectiveOperationException e) {
			LOGGER.error(String.format("Cannot instantiate startup job %d", entry.getId()), e);
			
			// On errors set state to FAILED
			this.startupJobEntryDao.updateJobState(entry.getId(), JobState.FAILED);
			
			return new ExecutionResult(0, 1);
		} catch(final Throwable e) {
			LOGGER.error(String.format("Error executing startup job %d", entry.getId()), e);
			
			// On errors set state to FAILED
			this.startupJobEntryDao.updateJobState(entry.getId(), JobState.FAILED);
			
			return new ExecutionResult(0, 1);
		}
	}
	
	private final StartupJob instantiateJob(final StartupJobEntry entry) throws ReflectiveOperationException {
		final Class<?> clazz = Class.forName(entry.getClassname());
		final Constructor<?> constructor = clazz.getConstructor();
		
		final Object instance = constructor.newInstance();
		
		if(!(instance instanceof StartupJob)) {
			throw new InstantiationException(String.format("Class %s is not of type %s", clazz.getCanonicalName(), StartupJob.class.getCanonicalName()));
		}
		
		return (StartupJob) instance;
	}
	
	private final JobContext createJobContext(final StartupJobEntry entry) {
		return new JobContext(entry.getId(), entry.getCompanyId(), dataSource, configService, this.applicationContext);
	}
	
	@Required
	public final void setStartupJobEntryDao(final StartupJobEntryDao dao) {
		this.startupJobEntryDao = Objects.requireNonNull(dao, "StartupJobEntryDao is null");
	}

	public final void setDataSource(final DataSource dataSource) {
		this.dataSource = Objects.requireNonNull(dataSource, "JDBC DataSource is null");
	}
	
	@Required
	public final void setConfigService(final ConfigService service) {
		this.configService = Objects.requireNonNull(service, "ConfigService is null");
	}

	@Required
	@Override
	public final void setApplicationContext(final ApplicationContext context) throws BeansException {
		this.applicationContext = Objects.requireNonNull(context, "Application context is null");
	}

}
