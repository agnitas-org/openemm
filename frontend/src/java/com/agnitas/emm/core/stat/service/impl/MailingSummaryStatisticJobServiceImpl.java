/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.stat.service.impl;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.commons.util.ConfigService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.annotation.Transactional;

import com.agnitas.emm.core.stat.beans.MailingStatJobDescriptor;
import com.agnitas.emm.core.stat.beans.MailingStatisticTgtGrp;
import com.agnitas.emm.core.stat.beans.StatisticValue;
import com.agnitas.emm.core.stat.dao.MailingStatJobDao;
import com.agnitas.emm.core.stat.dao.MailingStatTgtGrpDao;
import com.agnitas.emm.core.stat.service.MailingSummaryStatisticJobService;
import com.agnitas.reporting.birt.external.beans.SendStatRow;
import com.agnitas.reporting.birt.external.dao.LightTargetDao;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.dataset.MailingSummaryDataSet;

public class MailingSummaryStatisticJobServiceImpl implements MailingSummaryStatisticJobService {
	private static final Logger logger = LogManager.getLogger(MailingSummaryStatisticJobServiceImpl.class);
	
	private static final Map<Integer, String> recipTypeToStr = new HashMap<>();
	static {
		recipTypeToStr.put(MailingStatJobDescriptor.RECIPIENT_TYPE_WORLD, CommonKeys.TYPE_WORLDMAILING);
		recipTypeToStr.put(MailingStatJobDescriptor.RECIPIENT_TYPE_TEST, CommonKeys.TYPE_ADMIN_AND_TEST);
		recipTypeToStr.put(MailingStatJobDescriptor.RECIPIENT_TYPE_ALL, CommonKeys.TYPE_ALL_SUBSCRIBERS);
	}
	private static final MailingSummaryDataSet dataSet = new MailingSummaryDataSet();

	private MailingDao mailingDao;
	private LightTargetDao lightTargetDao;
	private MailingStatJobDao mailingStatJobDao;
	private MailingStatTgtGrpDao mailingStatTgtGrpDao;
    protected ConfigService configService;
    private ExecutorService workerExecutorService;

	private void validateArguments(int mailingId, final int companyId, List<Integer> targetList, Integer recipientsType) {
		StringBuffer errorStr = new StringBuffer();
		
		if (!mailingDao.exist(mailingId, companyId)) {
			errorStr.append(" Mailing ").append(mailingId).append(" doesn't exist.");
		}
		
		if (recipientsType != null && recipTypeToStr.get(recipientsType) == null) {
			errorStr.append(" Bad recipients type: ").append(recipientsType.intValue());
		}
		
		String badTargets = "";
		for (Integer targetId : targetList) {
			if (lightTargetDao.getTarget(targetId, companyId) == null) {
				if (!badTargets.isEmpty()) {
					badTargets += ",";
				}
				badTargets += targetId;
			}
		}
		if (!badTargets.isEmpty()) {
			errorStr.append(" Target(s) do(es)n't exist: ").append(badTargets);
		}
		
		if (!errorStr.toString().isEmpty()) {
			throw new FailedArgumentsValidationException(errorStr.toString());
		}
	}
	
	@Override
	@Transactional
	public int startSummaryStatisticJob(final int mailingId, final int companyId, List<Integer> targetList, final Integer recipientsType) {
		synchronized (dataSet) {
			validateArguments(mailingId, companyId, targetList, recipientsType);

			Collections.sort(targetList);
			final String targetGroups = StringUtils.join(targetList, ',');

			final MailingStatJobDescriptor job = new MailingStatJobDescriptor(mailingId,
					(recipientsType == null) ? MailingStatJobDescriptor.RECIPIENT_TYPE_ALL : recipientsType,
					(targetGroups == null) ? "" : targetGroups);

			int expiredTime = configService.getIntegerValue(ConfigValue.ExpireStatisticSummary);

			List<MailingStatJobDescriptor> jobs = mailingStatJobDao.findMailingStatJobs(mailingId, job.getRecipientsType(), job.getTargetGroups(), expiredTime);
			if (!jobs.isEmpty() && jobs.get(0).getStatus() != MailingStatJobDescriptor.STATUS_FAILED) {
				return jobs.get(0).getId();
			}

			final int id = mailingStatJobDao.createMailingStatJob(job);
			workerExecutorService.execute(() -> {
				try {
					statJob(id, companyId, mailingId, targetGroups, job.getRecipientsType());
				} catch (Throwable e) {
					updateJobStatus(id, MailingStatJobDescriptor.STATUS_FAILED, e.getMessage());
					logger.error("Error while collecting statistic: ", e);
				}
			});

			return id;
		}
	}
	
	@Override
	public List<Integer> parseGroupList(String targetGroups) {
		if (StringUtils.isBlank(targetGroups)) {
			return null;
		}
		String[] tGroupsStr = targetGroups.split(",");
		List<Integer> tGroups = new ArrayList<>();
		
		for (String groupStr : tGroupsStr) {
			tGroups.add(Integer.valueOf(groupStr));
		}
		
		return tGroups;
	}
	
	public int statJob(int jobId, int companyId, int mailingId, String targetGroups, int recipientsType) throws Exception {
		int tempTableID = dataSet.prepareReport(mailingId, companyId, targetGroups, recipTypeToStr.get(recipientsType), true, "", "", false);
		List<MailingSummaryDataSet.MailingSummaryRow> statDatas = dataSet.getSummaryData(tempTableID);
		
		final List<Integer> tGroups;
		try {
			tGroups = parseGroupList(targetGroups);
		} catch (NumberFormatException e) {
			throw new TargetGroupsStringFormatException();
		}
		
		int targetGroupIndex = 1;
		 // DataSet uses target group ID = 1 for 'all subscribers'
		collectSummaryStatistic(jobId, mailingId, 1, recipientsType, statDatas, targetGroupIndex);
		if (tGroups != null) {
			Collections.sort(tGroups);
			for (Integer targetGroup : tGroups) {
				collectSummaryStatistic(jobId, mailingId, targetGroup, recipientsType, statDatas, ++targetGroupIndex);
			}
		}
		
		dataSet.dropTempTable(tempTableID);
		
		updateJobStatus(jobId, MailingStatJobDescriptor.STATUS_SUCCEED, "Prepared successful");
		return 0;
	}
	
	@Override
	public void removeExpiredData() {
		int cleanUpTime = configService.getIntegerValue(ConfigValue.ExpireStatisticSummaryCleanup);
		if (cleanUpTime > 0) {
			final ZonedDateTime threshold = ZonedDateTime.now().minus(cleanUpTime, ChronoUnit.SECONDS);
	
			mailingStatTgtGrpDao.removeExpiredMailingStatTgtGrp(threshold);
			mailingStatJobDao.removeExpiredMailingStatJobs(threshold);
		} else {
			logger.error("Summary statistic clean up time is " + cleanUpTime + ". All data remains in DB!");
		}
	}
	
	private void updateJobStatus(int jobId, int status, String statusDescription) {
		mailingStatJobDao.updateMailingStatJob(jobId, status, statusDescription);
	}

	@Override
	@Transactional
	public MailingStatJobDescriptor getStatisticJob(int jobId) {
		try {
			return mailingStatJobDao.getMailingStatJob(jobId);
		} catch (DataAccessException e) {
			throw new SummaryStatJobNotExistException();
		}
	}
	
	@Override
	@Transactional
	public MailingStatisticTgtGrp getStatisticTgtGrp(int jobId, int targetGroupId) {
		return mailingStatTgtGrpDao.getMailingStatTgtGrpByJobId(jobId, targetGroupId);
	}
	
	@Transactional
	public void saveStatisticTgtGrp(MailingStatisticTgtGrp tgtGrp) {
		mailingStatTgtGrpDao.saveMalingStatTgtGrp(tgtGrp);
	}
	
	private void collectSummaryStatistic(int jobId, int mailingId, int targetGroup, int recipientsType,
			List<? extends SendStatRow> statDatas, int targetGroupIndex) {
		
		MailingStatisticTgtGrp tgtGrp = new MailingStatisticTgtGrp();
		tgtGrp.setJobId(jobId);
		tgtGrp.setMailingId(mailingId);
		tgtGrp.setTargetGroupId((targetGroup == 1) ? 0 : targetGroup);  // use target group ID = 0 for 'all subscribers'
		
		for (SendStatRow sendStatRow : statDatas) {
			if (sendStatRow.getTargetgroupindex() != targetGroupIndex) {
				continue;
			}
			if (sendStatRow.getCategoryindex() == CommonKeys.REVENUE_INDEX) {
				tgtGrp.setRevenue(sendStatRow.getRate());
				continue;
			}
			tgtGrp.getStatValues().put(sendStatRow.getCategoryindex(),
					new StatisticValue(sendStatRow.getCount(), sendStatRow.getRate()));
		}
		
		saveStatisticTgtGrp(tgtGrp);
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	public void setLightTargetDao(LightTargetDao lightTargetDao) {
		this.lightTargetDao = lightTargetDao;
	}

	public void setMailingStatJobDao(MailingStatJobDao mailingStatJobDao) {
		this.mailingStatJobDao = mailingStatJobDao;
	}
	
	public void setMailingStatTgtGrpDao(MailingStatTgtGrpDao mailingStatTgtGrpDao) {
		this.mailingStatTgtGrpDao = mailingStatTgtGrpDao;
	}

    public void setConfigService(ConfigService configService) {
        this.configService = configService;
    }

    public void setWorkerExecutorService(ExecutorService workerExecutorService) {
        this.workerExecutorService = workerExecutorService;
    }
}
