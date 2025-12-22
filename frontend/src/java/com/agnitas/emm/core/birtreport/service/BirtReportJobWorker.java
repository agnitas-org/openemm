/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.service;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.agnitas.dao.CompanyDao;
import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportMailingSettings;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.service.JobWorkerBase;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.quartz.JobWorker;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@JobWorker("BirtReports")
public class BirtReportJobWorker extends JobWorkerBase {

	private static final Logger logger = LogManager.getLogger(BirtReportJobWorker.class);

	@Override
	public String runJob() {
		try {
	        List<Integer> includedCompanyIds = getIncludedCompanyIdsListParameter();
	        
	        List<Integer> excludedCompanyIds = getExcludedCompanyIdsListParameter();
	        
	        int maximumRunningAutoExports = configService.getIntegerValue(ConfigValue.MaximumParallelReports);
	        int currentlyRunningAutoExports = serviceLookupFactory.getBeanBirtReportService().getRunningReportsByHost(AgnUtils.getHostName());
	        if (currentlyRunningAutoExports < maximumRunningAutoExports) {
	        	final List<BirtReport> reportsForSend = serviceLookupFactory.getBeanBirtReportService().getReportsToSend(maximumRunningAutoExports - currentlyRunningAutoExports, includedCompanyIds, excludedCompanyIds);
	        	sendReports(reportsForSend, false);
	        }
		} catch (Exception e) {
			logger.error(MessageFormat.format("Unknown error in BirtReportJobWorker.runJob(): {0}", e.getMessage()), e);
		}
		
		return null;
	}

	protected void sendReports(final List<BirtReport> reportsForSend, final boolean sendAsWorkflowTriggeredReport) {
		if (reportsForSend != null && !reportsForSend.isEmpty()) {
			for (BirtReport report : reportsForSend) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Processing report '%s' (ID %d)", report.getShortname(), report.getId()));
				}
	
				final BirtStatisticsService birtStatisticsService = serviceLookupFactory.getBeanBirtStatisticsService();
				final CompanyDao companyDao = daoLookupFactory.getBeanCompanyDao();

				final int companyID = report.getCompanyID();
				final Integer accountId = companyDao.getCompany(companyID).getId();
				final Map<String, String> urlsMap = new HashMap<>();
				final List<BirtReportSettings> reportSettings = report.getSettings();

				if (sendAsWorkflowTriggeredReport || !report.isTriggeredByMailing()) {
					// time-triggered report (daily, weekly etc.)
					urlsMap.putAll(birtStatisticsService.getReportStatisticsUrlMap(reportSettings, new Date(), report, companyID, accountId));
					startReportExecution(report, urlsMap);
				} else {
					// mailing-triggered report (After mailing dispatch option)
					// even if we have several mailing ids - we need to send each report separately as the report is triggered
					// by mailing sending, so the user will be confused if he gets reports for several mailings in one report-email
					BirtReportMailingSettings mailingSettings = report.getReportMailingSettings();
					final List<Integer> mailingsIdsToSend = mailingSettings.getMailingsIdsToSend().stream()
							.filter(mailingId -> isMailingExists(mailingId, companyID))
							.collect(Collectors.toList());

					if (mailingsIdsToSend != null && mailingsIdsToSend.size() > 0) {
						for (Integer mailingId : mailingsIdsToSend) {
							mailingSettings.setMailingsToSend(Collections.singletonList(mailingId));
							urlsMap.putAll(birtStatisticsService.getReportStatisticsUrlMap(Collections.singletonList(mailingSettings), new Date(), report, companyID, accountId));
							startReportExecution(report, urlsMap);
						}
					} else {
						if (StringUtils.isBlank(report.getIntervalpattern())) {
							report.setNextStart(DateUtilities.calculateNextJobStart("**00"));
						} else {
							report.setNextStart(DateUtilities.calculateNextJobStart(report.getIntervalpattern()));
						}
						serviceLookupFactory.getBeanBirtReportService().announceStart(report);
						serviceLookupFactory.getBeanBirtReportService().announceEnd(report);
					}

					// Restore previously separated list of mailing IDs
					mailingSettings.setMailingsToSend(mailingsIdsToSend);
				}
			}
		}
	}

	private void startReportExecution(BirtReport birtReport, Map<String, String> urlsMap) {
		BirtReportExecutor birtReportExecutor = new BirtReportExecutor(serviceLookupFactory, birtReport, urlsMap);
		Thread executorThread = new Thread(birtReportExecutor);
		executorThread.start();
	}

	private boolean isMailingExists(int mailingId, int companyId) {
		return serviceLookupFactory.getBeanMailingService().exists(mailingId, companyId);
	}
}
