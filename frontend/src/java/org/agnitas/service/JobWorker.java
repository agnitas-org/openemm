/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.Date;
import java.util.Map;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.dao.JobQueueDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.context.ApplicationContext;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.dao.impl.DaoLookupFactory;
import com.agnitas.service.MailNotificationService;
import com.agnitas.service.impl.ServiceLookupFactory;

/**
 * Worker class for queued jobs.
 * 
 * This worker takes the relevant parameters from the job item and executes the implementation class of it.
 * It also takes control of the job state and manages,together with JobQueueService, the execution of jobs.
 * 
 * Derviving classes must implement the method 'runJob' for executing actions.
 * 
 * JobQueue documentation: http://wiki.agnitas.local/doku.php?id=abteilung:technik:entwicklung:cron
 */
public abstract class JobWorker implements Runnable {
	/**
	 * Logger of the JobWorker class. The implementing classes should have their own logger
	 */
	private static final transient Logger logger = Logger.getLogger(JobWorker.class);
	
	/**
	 * Service instance having control on this JobWorker object
	 */
	protected JobQueueService jobQueueService;
	
	/**
	 * Database object containing all data on this jobqueue entry
	 */
	protected JobDto job;
	
	/**
	 * ApplicationContext for some special actions of the jobs.
	 * Use of this should be avoided whenever possible, because it is a dirty style
	 * 
	 * Only PIDSyncWorker still needs this
	 */
	@Deprecated
	protected ApplicationContext applicationContext;
	protected BeanLookupFactory beanLookupFactory;
	protected DaoLookupFactory daoLookupFactory;
	protected ServiceLookupFactory serviceLookupFactory;
	protected MailNotificationService mailNotificationService;
	
	/** Latest Jobstart before the current one */
	protected Date previousJobStart = null;
	
	/**
	 * Dao instance for use only within this class
	 */
	protected JobQueueDao jobQueueDao;
	
	protected ConfigService configService;
	
	/**
	 * Pause sign
	 * This sign may be used optionally by the implementing class
	 * When set the executing thread will be paused until further notice
	 */
	protected boolean pause = false;
	
	/**
	 * Stop sign
	 * This sign may be used optionally by the implementing class
	 * When set the executing job should come to an end on a convenient position
	 */
	protected boolean stop = false;

	// ----------------------------------------------------------------------------------------------------------------
	// Dependency Injection
	
	public void setJobQueueService(JobQueueService jobQueueService) {
		this.jobQueueService = jobQueueService;
	}

	public void setJob(JobDto job) {
		this.job = job;
	}
	
	@Deprecated
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}
	public void setBeanLookupFactory(BeanLookupFactory beanLookupFactory) {
		this.beanLookupFactory = beanLookupFactory;
	}

	public void setDaoLookupFactory(DaoLookupFactory daoLookupFactory) {
		this.daoLookupFactory = daoLookupFactory;
	}
	
	public void setServiceLookupFactory(ServiceLookupFactory serviceLookupFactory) {
		this.serviceLookupFactory = serviceLookupFactory;
	}
	
	public void setMailNotificationService(MailNotificationService mailNotificationService) {
		this.mailNotificationService = mailNotificationService;
	}

	public void setJobQueueDao(JobQueueDao jobQueueDao) {
		this.jobQueueDao = jobQueueDao;
	}

	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	// ----------------------------------------------------------------------------------------------------------------
	// Business Logic
	
	/**
	 * Runnable.run implementation for basic execution
	 * 
	 * This method MUST NOT be overridden in deriving classes.
	 * 'runJob' is the method, which implements the code to execute in a job run.
	 */
	@Override
	public void run() {
		previousJobStart = job.getLastStart();
		
		Date runStart = new Date();
		job.setRunning(true);
		job.setLastStart(runStart);
		jobQueueDao.updateJob(job);

		logger.info("Starting JobWorker: " + job.getDescription() + " (" + job.getId() + ")");
		
		String resultText;
		try {
			resultText = runJob();
			
			if (StringUtils.isBlank(resultText)) {
				resultText = "OK";
			}
			
			job.setLastResult("OK");
		} catch (Throwable t) {
			logger.error("Error in " + this.getClass().getName() + ": " + t.getMessage(), t);
			// Watchout: NullpointerExceptions have Message "null", which would result in another jobrun, so enter some additional text (classname)
			job.setLastResult(t.getClass().getSimpleName() + ": " + t.getMessage() + "\n" + AgnUtils.getStackTraceString(t));
			if (StringUtils.isNotBlank(job.getEmailOnError())) {
				String subject = "Error in JobQueue Job " + job.getDescription() + "(" + job.getId() + ") on host '" + AgnUtils.getHostName() + "'";
				String errorText = t.getClass().getSimpleName() + ": " + t.getMessage() + "\n" + AgnUtils.getStackTraceString(t);
				try {
					mailNotificationService.sendNotificationMailWithDuplicateRetention(job.getEmailOnError(), subject, errorText);
				} catch (Exception e) {
					logger.error("Cannot send email with jobqueue error:\n" + subject + "\n" + errorText, e);
				}
			}
			resultText = job.getLastResult();
		}
		
		logger.info("JobWorker done: " + job.getDescription() + " (" + job.getId() + ")");
		
		job.setRunning(false);
		job.setLastDuration((int) (new Date().getTime() - runStart.getTime()) / 1000);
		jobQueueDao.updateJobStatus(job);
		
		// Write JobResult after job has ended
		jobQueueDao.writeJobResult(job.getId(), new Date(), resultText, job.getLastDuration(), AgnUtils.getHostName());

		// show report ended
		jobQueueService.showJobEnd(this);
	}

	/**
	 * Hook method for derived classes to execute some action
	 * 
	 * JobQueue error management will be started on any Exception that is thrown on this method
	 * 
	 * @throws Exception
	 */
	public abstract String runJob() throws Exception;

	/**
	 * Method for optional execution state check.
	 * 
	 * This may be ignored by deriving classes, but it would be nice if they use it to controll execution
	 * 
	 * @throws JobStopException
	 */
	protected void checkForPrematureEnd() throws JobStopException {
		try {
			while (pause) {
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			// nothing to do
		}
		
		if (stop) {
			throw new JobStopException();
		}
	}
	
	/**
	 * Set signal to stop exection
	 * 
	 * This method is used by JobQueueService to stop execution
	 */
	public void setStopSign() {
		stop = true;
		pause = false;
	}

	protected CompaniesConstraints getCompaniesConstrains() {
		Map<String, String> parameters = job.getParameters();
		return new CompaniesConstraints(parameters.get("includedCompanyIds"), parameters.get("excludedCompanyIds"));
	}
}
