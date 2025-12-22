/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.JobQueueDao;
import com.agnitas.dao.impl.DaoLookupFactory;
import com.agnitas.emm.core.birtreport.dao.BirtReportDao;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.serverstatus.forms.JobQueueOverviewFilter;
import com.agnitas.service.exceptions.JobNotFoundException;
import com.agnitas.service.exceptions.JobQueueException;
import com.agnitas.service.exceptions.JobWorkerNotFoundException;
import com.agnitas.service.impl.ServiceLookupFactory;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.quartz.JobWorkersRegistry;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class JobQueueService implements ApplicationContextAware {

	private static final Logger logger = LogManager.getLogger(JobQueueService.class);

	private static final String WORKER_ERROR_SUBJECT = "JobWorker error";
	private static final String MISSING_NEXT_START_MAIL_TEXT = "ERROR: Missing value for field nextstart in table job_queue_tbl for JobWorker %s";

	private final JobQueueDao jobQueueDao;
	private final ConfigTableDao configDao;
	private final BirtReportDao birtReportDao;
	private final BeanLookupFactory beanLookupFactory;
	private final DaoLookupFactory daoLookupFactory;
	private final ServiceLookupFactory serviceLookupFactory;
	private final MailNotificationService mailNotificationService;
	private final ConfigService configService;
	private final JobWorkersRegistry jobWorkersRegistry;

	private final List<JobWorkerBase> queuedJobWorkers = new ArrayList<>();
	private final List<JobDto> queuedJobsTodo = new ArrayList<>();

	private ApplicationContext applicationContext;
	private Date lastCheckAndRunJobTime = null;

	@Autowired
    public JobQueueService(JobQueueDao jobQueueDao, ConfigTableDao configDao, BirtReportDao birtReportDao, BeanLookupFactory beanLookupFactory,
						   DaoLookupFactory daoLookupFactory, ServiceLookupFactory serviceLookupFactory, MailNotificationService mailNotificationService,
						   ConfigService configService, JobWorkersRegistry jobWorkersRegistry) {
        this.jobQueueDao = jobQueueDao;
        this.configDao = configDao;
        this.birtReportDao = birtReportDao;
        this.beanLookupFactory = beanLookupFactory;
        this.daoLookupFactory = daoLookupFactory;
        this.serviceLookupFactory = serviceLookupFactory;
        this.mailNotificationService = mailNotificationService;
        this.configService = configService;
        this.jobWorkersRegistry = jobWorkersRegistry;
    }

    @Override
	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

	public synchronized void checkAndRunJobs() {
		logger.info("Looking for queued jobs to execute");
		
		if (checkActiveNode()) {
			alertOnHangingJobs();
			alertLostNextStartJobs();
			
			List<JobDto> upcomingQueuedJobs = configService.getBooleanValue(ConfigValue.Development.UseJobWorkerAnnotation)
					? jobQueueDao.readUpcomingJobsForExecution()
					: jobQueueDao.readUpcomingJobsForExecutionByRunClass();
			
			logger.info("Found {} queued job(s)", upcomingQueuedJobs.size());

			for (JobDto queuedJob : upcomingQueuedJobs) {
				if (!containsJob(queuedJobsTodo, queuedJob)
					&& (StringUtils.isBlank(queuedJob.getRunOnlyOnHosts())
						|| Arrays.asList(queuedJob.getRunOnlyOnHosts().split(";|,")).contains(AgnUtils.getHostName()))) {
					
					queuedJobsTodo.add(queuedJob);
				}
			}
			
			checkAndStartNewWorkers();
			
			lastCheckAndRunJobTime = new Date();
		} else {
			logger.info("Job queue is inactive for this host");

			queuedJobsTodo.clear();
			
			if (checkShutdownNode()) {
				for (JobWorkerBase worker : queuedJobWorkers) {
					worker.setStopSign();
				}
			}
		}
	}

	private void alertLostNextStartJobs() {
		for (JobDto job : jobQueueDao.getJobsWithLostNextStart()) {
			String text = MISSING_NEXT_START_MAIL_TEXT.formatted(job.getDescription());
			try {
				if (StringUtils.isNotBlank(job.getEmailOnError())) {
					mailNotificationService.sendNotificationMailWithDuplicateRetention(0, job.getEmailOnError(), WORKER_ERROR_SUBJECT, text);
				} else {
					logger.error(text);
				}
			} catch (Exception e) {
				logSendNotificationError(e, WORKER_ERROR_SUBJECT, text);
			}
		}
	}
	
	private void alertOnHangingJobs() {
		int hoursErrorLimit = 5;
		List<JobDto> hangingJobs = jobQueueDao.getHangingJobs(DateUtilities.getDateOfHoursAgo(hoursErrorLimit));
		for (JobDto job : hangingJobs) {
			String errorSubject = "Error in JobQueue Job " + job.getDescription() + "(" + job.getId() + ") on host '" + AgnUtils.getHostName() + "'";
			String errorText = "Hanging job working for more than " + hoursErrorLimit + " hours";
			if (StringUtils.isNotBlank(job.getEmailOnError())) {
				try {
					mailNotificationService.sendNotificationMailWithDuplicateRetention(0, job.getEmailOnError(), errorSubject, errorText);
				} catch (Exception e) {
					logSendNotificationError(e, errorSubject, errorText);
				}
			}
			if (StringUtils.isNotBlank(configService.getValue(ConfigValue.SystemAlertMail))) {
				try {
					mailNotificationService.sendNotificationMailWithDuplicateRetention(0, configService.getValue(ConfigValue.SystemAlertMail), errorSubject, errorText);
				} catch (Exception e) {
					logSendNotificationError(e, errorSubject, errorText);
				}
			}
		}
	}

	private static void logSendNotificationError(Exception e, String errorSubject, String errorText) {
		logger.error("Cannot send email with jobqueue error:\n{}\n{}", errorSubject, errorText, e);
	}

	private boolean containsJob(List<JobDto> jobList, JobDto job) {
		for (JobDto item : jobList) {
			if (item.getId() == job.getId()) {
				return true;
			}
		}
		return false;
	}
	
	private synchronized void checkAndStartNewWorkers() {
		if (checkActiveNode()) {
			// Fill the queue for jobs with unknown duration
			while (queuedJobsTodo.size() > 0 && queuedJobWorkers.size() < configService.getIntegerValue(ConfigValue.MaximumParallelJobQueueJobs)) {
				JobDto jobToStart = queuedJobsTodo.get(0);
				queuedJobsTodo.remove(jobToStart);
				
				try {
					jobToStart.setNextStart(DateUtilities.calculateNextJobStart(jobToStart.getInterval()));
					// update in db will be done by worker
				} catch (Exception e) {
					jobToStart.setNextStart(null);
					jobToStart.setLastResult("Cannot calculate next start!");
					jobQueueDao.updateJob(jobToStart);
				}

				if (jobQueueDao.initJobStart(jobToStart.getId(), jobToStart.getNextStart())) {
					jobToStart.setRunning(true);
					
					try {
						JobWorkerBase worker = createJobWorker(jobToStart);
						
						queuedJobWorkers.add(worker);
						Thread newThread = new Thread(worker);
						newThread.start();
						logger.debug("Created worker for queued job #{}", jobToStart.getId());
					} catch (Exception e) {
						logger.error("Cannot create worker for queued job #" + jobToStart.getId(), e);
						jobToStart.setRunning(false);
						jobToStart.setNextStart(null);
						jobToStart.setLastResult("Cannot create worker: " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n" + AgnUtils.getStackTraceString(e));
						jobQueueDao.updateJob(jobToStart);
					}
				}
			}
			
			if (queuedJobsTodo.size() > 0) {
				logWaitingJobWorkers();
				
				if (queuedJobWorkers.contains(null)) {
					logger.error("List 'queuedJobWorkers' contains null");
					queuedJobWorkers.remove(null);
				}
			}
		} else {
			queuedJobsTodo.clear();
			
			for (JobWorkerBase worker : queuedJobWorkers) {
				worker.setStopSign();
			}
		}
	}

	private void logWaitingJobWorkers() {
		List<String> runningJobWorkerNames = new ArrayList<>();
		try {
			for (JobWorkerBase queuedJobWorker : queuedJobWorkers) {
				if (queuedJobWorker == null) {
					logger.error("List 'queuedJobWorker' contains 'null' value");
					runningJobWorkerNames.add("<null>");
				} else {
					String queuedJobWorkerName = queuedJobWorker.getJobDescription();
					if (queuedJobWorkerName == null) {
						logger.error("JobWorker returned 'null' value as name");
					} else {
						runningJobWorkerNames.add(queuedJobWorkerName);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Cannot create names list 'runningJobWorkerNames': " + e.getMessage(), e);
		}
		
		List<String> waitingJobWorkerNames = new ArrayList<>();
		try {
			for (JobDto todoJobWorker : queuedJobsTodo) {
				if (todoJobWorker == null) {
					logger.error("List 'queuedJobsTodo' contains 'null' value");
					waitingJobWorkerNames.add("<null>");
				} else {
					String todoJobWorkerName = todoJobWorker.getDescription();
					if (todoJobWorkerName == null) {
						logger.error("JobWorker returned 'null' value as name");
					} else {
						waitingJobWorkerNames.add(todoJobWorkerName);
					}
				}
			}
		} catch (Exception e) {
			logger.error("Cannot create names list 'waitingJobWorkerNames': " + e.getMessage(), e);
		}
	}

	public List<JobDto> getRunningJobsOnThisServer() {
		List<JobDto> runningJobs = new ArrayList<>();
		for (JobWorkerBase queuedJobWorker : queuedJobWorkers) {
			runningJobs.add(queuedJobWorker.getJob());
		}
		return runningJobs;
	}

	public List<JobDto> getWaitingJobsOnThisServer() {
        return List.copyOf(queuedJobsTodo);
	}

	private JobWorkerBase createJobWorker(JobDto jobToStart) throws InstantiationException, IllegalAccessException, ClassNotFoundException, InvocationTargetException, NoSuchMethodException {
		JobWorkerBase worker;
		if (configService.getBooleanValue(ConfigValue.Development.UseJobWorkerAnnotation)) {
			worker = jobWorkersRegistry.findWorker(jobToStart.getName())
					.orElseThrow(() -> new JobWorkerNotFoundException(jobToStart.getName()))
					.getConstructor()
					.newInstance();
		} else {
			worker = (JobWorkerBase) Class.forName(jobToStart.getRunClass()).getConstructor().newInstance();
		}

		worker.setJob(jobToStart);
		worker.setJobQueueService(this);
		worker.setApplicationContext(applicationContext);
		worker.setBeanLookupFactory(beanLookupFactory);
		worker.setDaoLookupFactory(daoLookupFactory);
		worker.setServiceLookupFactory(serviceLookupFactory);
		worker.setJobQueueDao(jobQueueDao);
		worker.setMailNotificationService(mailNotificationService);
		worker.setConfigService(configService);
		return worker;
	}

	public synchronized void showJobEnd(JobWorkerBase worker) {
		if (queuedJobWorkers.contains(null)) {
			logger.error("List 'queuedJobWorkers' contains null before removal of ended job");
			queuedJobWorkers.remove(null);
		}
		
		if (!queuedJobWorkers.remove(worker)) {
			logger.error("Cannot remove {} from 'queuedJobWorkers', maybe it was started manually", worker.getJobDescription());
		}

		if (queuedJobWorkers.contains(null)) {
			logger.error("List 'queuedJobWorkers' contains null after removal of ended job");
			queuedJobWorkers.remove(null);
		}
		
		checkAndStartNewWorkers();
	}

	public boolean setStopSignForJob(int jobID) {
		for (JobWorkerBase jobWorker : queuedJobWorkers) {
			if (jobWorker.getJob().getId() == jobID) {
				jobWorker.setStopSign();
				return true;
			}
		}
		return false;
	}
	
	public boolean checkActiveNode() {
		// Using direct access for no caching delay time of changes
		try {
			boolean isActive = configDao.getJobqueueHostStatus(AgnUtils.getHostName()) > 0;
			if (logger.isDebugEnabled()) {
				logger.debug("Jobqueue of {} is {}", AgnUtils.getHostName(), isActive ? "active" : "inactive");
			}
			return isActive;
		} catch (Exception e) {
			logger.error("Cannot check for active server node", e);
			return false;
		}
	}
	
	private boolean checkShutdownNode() {
		// Using direct access for no caching delay time of changes
		try {
			boolean shutdownNow = configDao.getJobqueueHostStatus(AgnUtils.getHostName()) < 0;
			if (logger.isDebugEnabled()) {
				logger.debug("Jobqueue of {} is{} requested to shutdown", AgnUtils.getHostName(), shutdownNow ? "" : " NOT");
			}
			return shutdownNow;
		} catch (Exception e) {
			logger.error("Cannot check for active server node", e);
			return false;
		}
	}
	
	public void startSpecificJobQueueJob(String description) {
		if (!checkActiveNode()) {
			throw new IllegalStateException("This JobQueueNode is currently not active");
		}

		JobDto jobToStart = jobQueueDao.getJob(description);

		if (jobToStart == null) {
			throw new JobNotFoundException(description);
		}

		if (StringUtils.isNotBlank(jobToStart.getRunOnlyOnHosts())
			&& !Arrays.asList(jobToStart.getRunOnlyOnHosts().split(";|,")).contains(AgnUtils.getHostName())) {
			throw new JobQueueException("The Job " + description + " is not allowed to be run on this host: " + AgnUtils.getHostName());
		}

		try {
			jobToStart.setNextStart(DateUtilities.calculateNextJobStart(jobToStart.getInterval()));
			// update in db will be done by worker
		} catch (Exception e) {
			jobToStart.setNextStart(null);
			jobToStart.setLastResult("Cannot calculate next start!");
			jobQueueDao.updateJob(jobToStart);
			throw new JobQueueException("Cannot calculate next start! Interval: " + jobToStart.getInterval());
		}

		if (!jobQueueDao.initJobStart(jobToStart.getId(), jobToStart.getNextStart(), true)) {
			throw new JobQueueException("Cannot start Job: %s (already running?)".formatted(description));
		}

		jobToStart.setRunning(true);

		try {
			JobWorkerBase worker = createJobWorker(jobToStart);

			Thread newThread = new Thread(worker);
			newThread.start();
			logger.debug("Created worker for job #{}", jobToStart.getId());
		} catch (Exception e) {
			logger.error("Cannot create worker for job #%d".formatted(jobToStart.getId()), e);
			jobToStart.setRunning(false);
			jobToStart.setNextStart(null);
			jobToStart.setLastResult("Cannot create worker: " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n" + AgnUtils.getStackTraceString(e));
			jobQueueDao.updateJob(jobToStart);
			throw new JobQueueException("Cannot create worker: " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n" + AgnUtils.getStackTraceString(e));
		}
	}

	public boolean isStatusOK() {
		return jobQueueDao.selectCriticalErroneousJobs().size() == 0;
	}

	public boolean isJobQueueRunning() {
		return lastCheckAndRunJobTime != null && (new Date().getTime() - lastCheckAndRunJobTime.getTime()) < 300000; // lastCheckAndRunJobTime was within last 5 minutes
	}
	
	public List<JobDto> getOverview(JobQueueOverviewFilter filter) {
		return jobQueueDao.getOverview(filter);
	}

	public int getCountForOverview() {
		return jobQueueDao.getCountForOverview();
	}

	public List<JobDto> selectErroneousJobs() {
		return jobQueueDao.selectErroneousJobs();
	}

	public boolean isReportOK() {
		return birtReportDao.selectErroneousReports().size() == 0;
	}

	public void acknowledgeErroneousJob(int idToAcknowledge) {
		jobQueueDao.acknowledgeErroneousJob(idToAcknowledge);
	}
	
	public JobDto getJob(int id) {
		return jobQueueDao.getJob(id);
	}
}
