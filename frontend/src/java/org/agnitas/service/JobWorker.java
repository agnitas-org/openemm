/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.agnitas.beans.CompaniesConstraints;
import org.agnitas.dao.JobQueueDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
	private static final transient Logger logger = LogManager.getLogger(JobWorker.class);
	
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

	public JobDto getJob() {
		return job;
	}

	public String getJobDescription() {
		if (job != null && StringUtils.isNotEmpty(job.getDescription())) {
			return job.getDescription();
		} else {
			return "Undefined job description";
		}
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
		try {
			previousJobStart = job.getLastStart();
			
			final Date runStart = new Date();
			job.setRunning(true);
			job.setLastStart(runStart);
			jobQueueDao.updateJob(job);
	
			if (logger.isInfoEnabled()) {
				logger.info(String.format("Starting JobWorker: %s (%d)", job.getDescription(), job.getId()));
			}
			
			String resultText;
			try {
				resultText = runJob();
				
				if (StringUtils.isBlank(resultText)) {
					resultText = "OK";
				}
				
				job.setLastResult("OK");
			} catch (final JobStopException e) {
				if(logger.isInfoEnabled()) {
					logger.info(String.format(
							"Job worker %s (%d) received STOP signal", 
							this.getClass().getName(),
							job.getId()
							));
				}
				
				resultText = "OK";
				job.setLastResult("OK");
			} catch (final Throwable t) {
				logger.error(String.format(
						"Error in %s: %s", 
						this.getClass().getName(), 
						t.getMessage()), 
						t);
	
				// Watchout: NullpointerExceptions have Message "null", which would result in another jobrun, so enter some additional text (classname)
				job.setLastResult(String.format(
						"%s: %s%n%s", 
						t.getClass().getSimpleName(), 
						t.getMessage(), 
						AgnUtils.getStackTraceString(t)));
				
				sendErrorMail(job, t);
				resultText = job.getLastResult();
			}
	
			if (logger.isInfoEnabled()) {
				logger.info(String.format("JobWorker done: %s (%d)", job.getDescription(), job.getId()));
			}
			
			job.setRunning(false);
			job.setLastDuration((int) (new Date().getTime() - runStart.getTime()) / 1000);
			jobQueueDao.updateJobStatus(job);
			
			// Write JobResult after job has ended
			jobQueueDao.writeJobResult(job.getId(), new Date(), resultText, job.getLastDuration(), AgnUtils.getHostName());
		} finally {
			// Show job ended
			jobQueueService.showJobEnd(this);
		}
	}
	
	private final void sendErrorMail(final JobDto jobDto, final Throwable t) {
		if (StringUtils.isNotBlank(jobDto.getEmailOnError())) {
			final String subject = "Error in JobQueue Job " + jobDto.getDescription() + "(" + jobDto.getId() + ") on host '" + AgnUtils.getHostName() + "'";
			final String errorText = t.getClass().getSimpleName() + ": " + t.getMessage() + "\n" + AgnUtils.getStackTraceString(t);
			
			try {
				mailNotificationService.sendNotificationMailWithDuplicateRetention(0, jobDto.getEmailOnError(), subject, errorText);
			} catch (Exception e) {
				logger.error("Cannot send email with jobqueue error:\n" + subject + "\n" + errorText, e);
			}
		}
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
	 * This may be ignored by deriving classes, but it would be nice if they use it to control execution
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
	 * Method for optional execution state check.
	 * This one only returns a boolean to let the jobworker manage sub workers
	 * 
	 * This may be ignored by deriving classes, but it would be nice if they use it to control execution
	 */
	protected boolean continueJobWanted() throws JobStopException {
		try {
			while (pause) {
				Thread.sleep(500);
			}
		} catch (InterruptedException e) {
			// nothing to do
		}
		
		return !stop;
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

	protected CompaniesConstraints getCompaniesConstraints() {
		Map<String, String> parameters = job.getParameters();
		return new CompaniesConstraints(parameters.get("includedCompanyIds"), parameters.get("excludedCompanyIds"));
	}
	
	protected ApplicationContext getApplicationContextForJobWorker() {
		return applicationContext;
	}

	/**
	 * Treats the value of given parameter as list of integers.
	 * The parsed list is returned or <code>null</code> if parameter is not defined.
	 * 
	 * Separator of values can be: &quot;,&quot;, &quot;;&quot; &quot;|&quot; or spaces.
	 * 
	 * @param parameterName name of worker parameter
	 * 
	 * @return parsed list or <code>null</code>
	 * @throws Exception 
	 */
	public final List<Integer> parameterAsIntegerListOrNull(final String parameterName) throws Exception {
		final String listAsString = job.getParameters().get(parameterName);
			
		if (StringUtils.isNotBlank(listAsString)) {
			try {
				return AgnUtils.splitAndTrimList(listAsString).stream().filter(x -> StringUtils.isNotBlank(x)).map(x -> StringUtils.trim(x)).map(Integer::parseInt).collect(Collectors.toList());
			} catch (Exception e) {
				throw new Exception("Invalid content for list of integer parameter '" + parameterName + "': " + listAsString, e);
			}
		} else {
			return null;
		}
	}

	public List<Integer> getIncludedCompanyIdsListParameter() throws Exception {
		return parameterAsIntegerListOrNull("includedCompanyIds");
	}

	public List<Integer> getExcludedCompanyIdsListParameter() throws Exception {
		return parameterAsIntegerListOrNull("excludedCompanyIds");
	}
}
