/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.agnitas.dao.JobQueueDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.agnitas.beans.BeanLookupFactory;
import com.agnitas.dao.ConfigTableDao;
import com.agnitas.dao.impl.DaoLookupFactory;
import com.agnitas.emm.core.birtreport.dao.ComBirtReportDao;
import com.agnitas.service.MailNotificationService;
import com.agnitas.service.impl.ServiceLookupFactory;

public class JobQueueService implements ApplicationContextAware {
	private static final transient Logger logger = LogManager.getLogger(JobQueueService.class);
	
	private JobQueueDao jobQueueDao;
	private ConfigTableDao configDao;
	private ComBirtReportDao birtReportDao;
	private ApplicationContext applicationContext;
	private BeanLookupFactory beanLookupFactory;
	private DaoLookupFactory daoLookupFactory;
	private ServiceLookupFactory serviceLookupFactory;
	private MailNotificationService mailNotificationService;
	private ConfigService configService;
	
	private Date lastCheckAndRunJobTime = null;

	private List<JobWorker> queuedJobWorkers = new ArrayList<>();
	private List<JobDto> queuedJobsTodo = new ArrayList<>();

	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		this.applicationContext = applicationContext;
	}

	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}
	
	@Required
	public void setJobQueueDao(JobQueueDao jobQueueDao) {
		this.jobQueueDao = jobQueueDao;
	}

	@Required
	public void setConfigTableDao(ConfigTableDao configDao) {
		this.configDao = configDao;
	}
	
	@Required
	public void setBirtReportDao(ComBirtReportDao birtReportDao) {
		this.birtReportDao = birtReportDao;
	}

	@Required
	public void setBeanLookupFactory(BeanLookupFactory beanLookupFactory) {
		this.beanLookupFactory = beanLookupFactory;
	}

	@Required
	public void setDaoLookupFactory(DaoLookupFactory daoLookupFactory) {
		this.daoLookupFactory = daoLookupFactory;
	}

	@Required
	public void setServiceLookupFactory(ServiceLookupFactory serviceLookupFactory) {
		this.serviceLookupFactory = serviceLookupFactory;
	}

	@Required
	public void setMailNotificationService(MailNotificationService mailNotificationService) {
		this.mailNotificationService = mailNotificationService;
	}
	
	public synchronized void checkAndRunJobs() {
		logger.info("Looking for queued jobs to execute");
		
		if (checkActiveNode()) {
			alertOnHangingJobs();
			
			List<JobDto> upcomingQueuedJobs = jobQueueDao.readUpcomingJobsForExecution();
			
			if (logger.isInfoEnabled()) {
				logger.info("Found " + upcomingQueuedJobs.size() + " queued job(s)");
			}
			
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
			if (logger.isInfoEnabled()) {
				logger.info("Job queue is inactive for this host");
			}
			
			queuedJobsTodo.clear();
			
			if (checkShutdownNode()) {
				for (JobWorker worker : queuedJobWorkers) {
					worker.setStopSign();
				}
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
					logger.error("Cannot send email with jobqueue error:\n" + errorSubject + "\n" + errorText, e);
				}
			}
			if (StringUtils.isNotBlank(configService.getValue(ConfigValue.SystemAlertMail))) {
				try {
					mailNotificationService.sendNotificationMailWithDuplicateRetention(0, configService.getValue(ConfigValue.SystemAlertMail), errorSubject, errorText);
				} catch (Exception e) {
					logger.error("Cannot send email with jobqueue error:\n" + errorSubject + "\n" + errorText, e);
				}
			}
		}
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
						JobWorker worker = createJobWorker(jobToStart);
						
						queuedJobWorkers.add(worker);
						Thread newThread = new Thread(worker);
						newThread.start();
						if (logger.isDebugEnabled()) {
							logger.debug("Created worker for queued job #" + jobToStart.getId());
						}
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
			
			for (JobWorker worker : queuedJobWorkers) {
				worker.setStopSign();
			}
		}
	}

	private void logWaitingJobWorkers() {
		List<String> runningJobWorkerNames = new ArrayList<>();
		try {
			for (JobWorker queuedJobWorker : queuedJobWorkers) {
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
		
//		logger.warn(queuedJobsTodo.size() + " JobWorker are waiting, because already running " + queuedJobWorkers.size() + " JobWorker\n"
//				+ "JobWorker running: " + StringUtils.join(runningJobWorkerNames, ", ") + "\n"
//				+ "JobWorker waiting: " + StringUtils.join(waitingJobWorkerNames, ", "));
	}

	public List<JobDto> getRunningJobsOnThisServer() {
		List<JobDto> runningJobs = new ArrayList<>();
		for (JobWorker queuedJobWorker : queuedJobWorkers) {
			runningJobs.add(queuedJobWorker.getJob());
		}
		return runningJobs;
	}

	public List<JobDto> getWaitingJobsOnThisServer() {
		List<JobDto> waitingJobs = new ArrayList<>();
		for (JobDto waitingJob : queuedJobsTodo) {
			waitingJobs.add(waitingJob);
		}
		return waitingJobs;
	}

	private JobWorker createJobWorker(JobDto jobToStart) throws InstantiationException, IllegalAccessException, ClassNotFoundException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		JobWorker worker = (JobWorker) Class.forName(jobToStart.getRunClass()).getConstructor().newInstance();
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
	
	public synchronized void showJobEnd(JobWorker worker) {
		if (queuedJobWorkers.contains(null)) {
			logger.error("List 'queuedJobWorkers' contains null before removal of ended job");
			queuedJobWorkers.remove(null);
		}
		
		if (!queuedJobWorkers.remove(worker)) {
			logger.error("Cannot remove " + worker.getJobDescription() + " from 'queuedJobWorkers', maybe it was started manually");
		}

		if (queuedJobWorkers.contains(null)) {
			logger.error("List 'queuedJobWorkers' contains null after removal of ended job");
			queuedJobWorkers.remove(null);
		}
		
		checkAndStartNewWorkers();
	}

	public boolean setStopSignForJob(int jobID) {
		for (JobWorker jobWorker : queuedJobWorkers) {
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
				logger.debug("Jobqueue of " + AgnUtils.getHostName() + " is " + (isActive ? "active" : "inactive"));
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
				logger.debug("Jobqueue of " + AgnUtils.getHostName() + " is" + (shutdownNow ? "" : " NOT") + " requested to shutdown");
			}
			return shutdownNow;
		} catch (Exception e) {
			logger.error("Cannot check for active server node", e);
			return false;
		}
	}
	
	public void startSpecificJobQueueJob(String description) throws Exception {
		if (!checkActiveNode()) {
			throw new Exception("This JobQueueNode is currently not active");
		} else {
			JobDto jobToStart = jobQueueDao.getJob(description);
			
			if (jobToStart == null) {
				throw new Exception("Job not found: " + description);
			} else if (StringUtils.isNotBlank(jobToStart.getRunOnlyOnHosts())
				&& !Arrays.asList(jobToStart.getRunOnlyOnHosts().split(";|,")).contains(AgnUtils.getHostName())) {
				throw new Exception("The Job " + description + " is not allowed to be run on this host: " + AgnUtils.getHostName());
			} else {
				try {
					jobToStart.setNextStart(DateUtilities.calculateNextJobStart(jobToStart.getInterval()));
					// update in db will be done by worker
				} catch (Exception e) {
					jobToStart.setNextStart(null);
					jobToStart.setLastResult("Cannot calculate next start!");
					jobQueueDao.updateJob(jobToStart);
					throw new Exception("Cannot calculate next start!");
				}
				
				if (jobQueueDao.initJobStart(jobToStart.getId(), jobToStart.getNextStart(), true)) {
					jobToStart.setRunning(true);
					
					try {
						JobWorker worker = createJobWorker(jobToStart);
						
						Thread newThread = new Thread(worker);
						newThread.start();
						if (logger.isDebugEnabled()) {
							logger.debug("Created worker for job #" + jobToStart.getId());
						}
					} catch (Exception e) {
						logger.error("Cannot create worker for job #" + jobToStart.getId(), e);
						jobToStart.setRunning(false);
						jobToStart.setNextStart(null);
						jobToStart.setLastResult("Cannot create worker: " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n" + AgnUtils.getStackTraceString(e));
						jobQueueDao.updateJob(jobToStart);
						throw new Exception("Cannot create worker: " + e.getClass().getSimpleName() + ": " + e.getMessage() + "\n" + AgnUtils.getStackTraceString(e));
					}
				} else {
					throw new Exception("Cannot start Job: " + description + " (already running?)");
				}
			}
		}
	}

	public boolean isStatusOK() {
		return jobQueueDao.selectErroneousJobs().size() == 0;
	}

	public boolean isJobQueueRunning() {
		return lastCheckAndRunJobTime != null && (new Date().getTime() - lastCheckAndRunJobTime.getTime()) < 300000; // lastCheckAndRunJobTime was within last 5 minutes
	}
	
	public List<JobDto> getAllActiveJobs() {
		return jobQueueDao.getAllActiveJobs();
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
