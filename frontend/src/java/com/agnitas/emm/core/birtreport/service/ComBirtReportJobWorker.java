/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtreport.service;

import java.io.File;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.JobWorker;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.beans.ComAdmin;
import com.agnitas.dao.ComAdminDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.JavaMailService.MailAttachment;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtreport.util.ComBirtReportUtils;
import com.agnitas.emm.core.birtstatistics.service.BirtStatisticsService;
import com.agnitas.messages.I18nString;

public class ComBirtReportJobWorker extends JobWorker {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComBirtReportJobWorker.class);

	@Override
	public void runJob() {
		try {
			final ComBirtReportService birtReportService = serviceLookupFactory.getBeanBirtReportService();
            final BirtStatisticsService birtStatisticsService = serviceLookupFactory.getBeanBirtStatisticsService();
			final ComCompanyDao companyDao = daoLookupFactory.getBeanCompanyDao();
			final ComAdminDao adminDao = daoLookupFactory.getBeanAdminDao();
			final Date currentDate = new Date();

			// get reports which we need to send
			final List<ComBirtReport> reportsForSend = birtReportService.getReportsToSend(currentDate);

			// iterate through the reports and send each one
			for (ComBirtReport report : reportsForSend) {
				if (logger.isDebugEnabled()) {
					logger.debug(String.format("Processing report '%s' (ID %d)", report.getShortname(), report.getId()));
				}

				try {
					final int companyID = report.getCompanyID();

					final Integer accountId = companyDao.getCompany(companyID).getId();
					final ComAdmin authenticationAdmin = adminDao.getAdminForReport(companyID);
					if (authenticationAdmin == null) {
						logger.error(String.format("Report %d cannot be generated because company %d has no active admin", report.getId(), report.getCompanyID()));
					}
					final Map<String, String> urlsMap = new HashMap<>();
					final List<ComBirtReportSettings> reportSettings = report.getSettings();

					try {
						// if we have a time-triggered report (daily, weekly etc.)
						if (!report.isTriggeredByMailing()) {
							urlsMap.putAll(birtStatisticsService
									.getReportStatisticsUrlMap(reportSettings, authenticationAdmin, currentDate, report, companyID, accountId));

							sendReport(report, urlsMap);
						}
						// if we have a mailing-triggered report (After mailing dispatch option)
						else {
							// even if we have several mailing ids - we need to send each report separately as the report is triggered
							// by mailing sending, so the user will be confused if he gets reports for several mailings in one report-email
							final List<Integer> mailingsIdsToSend = report.getReportMailingSettings().getMailingsIdsToSend();
							for (Integer mailingId : mailingsIdsToSend) {
								report.getReportMailingSettings().setMailingsToSend(Collections.singletonList(mailingId));
								urlsMap.putAll(
										birtStatisticsService.getReportStatisticsUrlMap(
												Collections.singletonList(report.getReportMailingSettings()),
												authenticationAdmin, currentDate, report, companyID, accountId));
								sendReport(report, urlsMap);
							}

							// Restore previously separated list of mailing IDs
							report.getReportMailingSettings().setMailingsToSend(mailingsIdsToSend);
						}
						birtReportService.logSentReport(report);
					} catch (MalformedURLException e) {
						logger.fatal("Malformed report URL reported in report ID: " + report.getId() + ": " + e.getMessage(), e);
						for (Map.Entry<String, String> entry : urlsMap.entrySet()) {
							logger.fatal(String.format("  + Report '%s' : %s", entry.getKey(), entry.getValue()));
						}
					}
				} catch (Exception e) {
					logger.error("Error processing report ID: " + report.getId() + ": " + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			logger.error("Unknown error in ComBirtReportJobWorker.runJob(): " + e.getMessage(), e);
		}
	}

	protected void sendReports(List<ComBirtReport> reportsForSend) {
		try {
			final ComCompanyDao companyDao = daoLookupFactory.getBeanCompanyDao();
			final ComAdminDao adminDao = daoLookupFactory.getBeanAdminDao();
            final BirtStatisticsService birtStatisticsService = serviceLookupFactory.getBeanBirtStatisticsService();
			final Date currentDate = new Date();

			// iterate through the reports and send each one
			for (ComBirtReport report : reportsForSend) {
				final Map<String, String> urlsMap = new HashMap<>();

				try {
					final int companyID = report.getCompanyID();
					final Integer accountId = companyDao.getCompany(companyID).getId();
					final ComAdmin authenticationAdmin = adminDao.getAdminForReport(companyID);
					final List<ComBirtReportSettings> reportSettings = report.getSettings();

					urlsMap.putAll(birtStatisticsService
									.getReportStatisticsUrlMap(reportSettings, authenticationAdmin, currentDate, report, companyID, accountId));
					sendReport(report, urlsMap);
				} catch (MalformedURLException e) {
					logger.fatal("Malformed report URL reported in report ID: " + report.getId() + ": " + e.getMessage(), e);
					for (Map.Entry<String, String> entry : urlsMap.entrySet()) {
						logger.fatal(String.format("  + Report '%s' : %s", entry.getKey(), entry.getValue()));
					}
				} catch (Exception e) {
					logger.error("Error processing report ID " + report.getId() + ": " + e.getMessage(), e);
				}
			}
		} catch (Exception e) {
			logger.error("Unknown error: " + e.getMessage(), e);
		}
	}

	private void sendReport(ComBirtReport report, Map<String, String> urlsMap) throws MalformedURLException {
		String birtUrl = configService.getValue(ConfigValue.BirtUrlIntern);
		if (StringUtils.isBlank(birtUrl)) {
			birtUrl = configService.getValue(ConfigValue.BirtUrl);
		}

		final HttpClient httpClient = ComBirtReportUtils.initializeHttpClient(birtUrl);

		final List<MailAttachment> attachments = new ArrayList<>();
        for(final Map.Entry<String, String> entry : urlsMap.entrySet()) {
        	try {
                logger.error("BIRT report for sending\nreport id: " + report.getId() + "\nURL:" + entry.getValue());
        		File temporaryFile = ComBirtReportUtils.getBirtReportBodyAsTemporaryFile(report.getId(), entry.getValue(), httpClient, logger);
        		attachments.add(new MailAttachment(entry.getKey(), FileUtils.readFileToByteArray(temporaryFile), String.format("application/%s", report.getFormatName())));
        	} catch( Exception e) {
        		logger.error( "Error retrieving report data for BIRT report " + report.getId(), e);
        	}
        }

        final String email = report.getSendEmail();
        final String emailSubject = report.getEmailSubject();
        String content = "";
        if (StringUtils.isBlank(report.getEmailDescription())) {
        	content = I18nString.getLocaleString("report.body.text", new Locale(report.getLanguage()));
        } else {
        	content = report.getEmailDescription();
        }
        final String emailDescription = content;
        serviceLookupFactory.getBeanJavaMailService().sendEmail(email, emailSubject, emailDescription, emailDescription, attachments.toArray(new MailAttachment[attachments.size()]));
	}
}
