/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/


package com.agnitas.emm.core.commons.spring.hooks;

import java.util.Objects;

import org.apache.log4j.Logger;
import org.quartz.JobDetail;
import org.quartz.JobExecutionContext;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.SmartLifecycle;

public final class QuartzShutdownHook implements SmartLifecycle {
	
	private static final Logger LOGGER = Logger.getLogger(QuartzShutdownHook.class);

	private boolean isRunning = false;
	private Scheduler scheduler;
	
	@Override
	public final boolean isRunning() {
		return this.isRunning;
	}

	@Override
	public final void start() {
		this.isRunning = true;
	}

	@Override
	public final void stop() {
		if(LOGGER.isInfoEnabled()) {
			LOGGER.info("Received container shutdown event");
		}
		
		try {
			interruptRunningJobs();
			// Shutdown scheduler waiting for jobs to complete
			this.scheduler.shutdown(true);
		} catch(final SchedulerException e) {
			try {
				// Shutdown scheduler killing jobs
				this.scheduler.shutdown(false);
			} catch(final SchedulerException e2) {
				LOGGER.error("Cannot shutdown Quartz scheduler", e2);
			}
		}
		
		this.isRunning = false;
	}

	@Override
	public final int getPhase() {
		return Integer.MAX_VALUE;
	}

	@Override
	public final boolean isAutoStartup() {
		return true;
	}

	@Override
	public final void stop(final Runnable runnable) {
		stop();
		
		runnable.run();
	}
	
	private final void interruptRunningJobs() throws SchedulerException {
	    for(final JobExecutionContext jobExecutionContext : this.scheduler.getCurrentlyExecutingJobs()) {
	        final JobDetail jobDetail = jobExecutionContext.getJobDetail();

	        if(LOGGER.isInfoEnabled()) {
	        	LOGGER.info(String.format(
	        			"Interrupting job '%s' of group='%s'.", 
	        			jobDetail.getKey().getName(), 
	        			jobDetail.getKey().getGroup()));
	        }
	        
	        scheduler.interrupt(jobDetail.getKey());
	    }
	}
	@Required
	public final void setScheduler(final Scheduler scheduler) {
		this.scheduler = Objects.requireNonNull(scheduler, "Scheduler is null");
	}
}
