/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.birtreport.service;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.agnitas.emm.core.JavaMailAttachment;
import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.dto.StatisticsReportRequest;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.emm.core.export.exception.ExportException;
import com.agnitas.messages.I18nString;
import com.agnitas.service.impl.ServiceLookupFactory;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.FileDownload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class BirtReportExecutor implements Runnable {

	private static final Logger logger = LogManager.getLogger(BirtReportExecutor.class);

	private final ServiceLookupFactory serviceLookupFactory;
	private final BirtReport birtReport;
	private final Map<String, StatisticsReportRequest> reportRequests;
	private final Map<String, String> urlsMap; // TODO: GWUA-7067: Remove after migration

	public BirtReportExecutor(ServiceLookupFactory serviceLookupFactory, BirtReport birtReport,
	                           Map<String, String> urlsMap, Map<String, StatisticsReportRequest> reportRequests) {
		this.serviceLookupFactory = serviceLookupFactory;
		this.birtReport = birtReport;
		this.urlsMap = urlsMap;
		this.reportRequests = reportRequests;
	}

	@Override
	public void run() {
		executeBirtReport();
	}

	private void executeBirtReport() {
		updateNextStart();

		if(!serviceLookupFactory.getBeanBirtReportService().announceStart(this.birtReport)) {
			logger.info(
					"Cannot start BIRT report job '{}' (id {}) on host '{}'. Maybe the report is running on some other host at the moment.",
					birtReport.getShortname(),
					birtReport.getId(),
					AgnUtils.getHostName()
			);
			return;
		}

		try {
            List<JavaMailAttachment> attachments = serviceLookupFactory.getBeanConfigService().getBooleanValue(ConfigValue.Development.BirtReportsMigration, birtReport.getCompanyID())
					? generateReports()
					: downloadReportFilesOld();

			sendReportMail(attachments);

			birtReport.setLastresult("OK");
			serviceLookupFactory.getBeanBirtReportService().announceEnd(birtReport);
		} catch (Exception e) {
			reportError(e);
			throw new ExportException(false, null, e.getMessage());
		}
	}

	private void updateNextStart() {
		if (StringUtils.isBlank(birtReport.getIntervalpattern())) {
			birtReport.setNextStart(null);
		} else {
			birtReport.setNextStart(DateUtilities.calculateNextJobStart(birtReport.getIntervalpattern()));
		}
	}

	// TODO: GWUA-7067: Remove after migration
	private String determineBirtUrl(final int companyID) {
		String birtUrl = serviceLookupFactory.getBeanConfigService().getValue(ConfigValue.BirtUrlIntern, companyID);

		if (StringUtils.isBlank(birtUrl)) {
			birtUrl = serviceLookupFactory.getBeanConfigService().getValue(ConfigValue.BirtUrl, companyID);
		}

		return birtUrl;
	}

	// TODO: GWUA-7067: Remove after migration
	private List<JavaMailAttachment> downloadReportFilesOld() throws Exception {
		final String birtUrl = determineBirtUrl(this.birtReport.getCompanyID());
		final List<JavaMailAttachment> attachments = new ArrayList<>();

		try (final CloseableHttpClient httpClient = FileDownload.createHttpClientForDownload(birtUrl)) {
			for (final Map.Entry<String, String> entry : urlsMap.entrySet()) {
				try {
					logger.info("BIRT report {}: Downloading report frm '{}'", birtReport.getId(), entry.getValue());

					final File temporaryFile = serviceLookupFactory.getBeanBirtStatisticsService().getReportTmpFile(birtReport.getId(), entry.getValue(), httpClient, logger);

					attachments.add(new JavaMailAttachment(entry.getKey(), FileUtils.readFileToByteArray(temporaryFile), String.format("application/%s", birtReport.getFormat().name().toLowerCase())));
				} catch( Exception e) {
					logger.error("Error retrieving report data for BIRT report {}\nURL:{}", birtReport.getId(), entry.getValue(), e);
				}
			}
		}

		return attachments;
	}

	private List<JavaMailAttachment> generateReports() {
		List<JavaMailAttachment> attachments = new ArrayList<>();

		for (Map.Entry<String, StatisticsReportRequest> entry : reportRequests.entrySet()) {
			try {
				File file = serviceLookupFactory.getBeanBirtStatisticsService()
						.generateReportFile(birtReport.getId(), birtReport.getCompanyID(), entry.getValue());

				if (file == null) {
					logger.error("Error generating report data for BIRT report {}: no file returned for '{}'", birtReport.getId(), entry.getKey());
					continue;
				}

				attachments.add(new JavaMailAttachment(
						entry.getKey(),
						FileUtils.readFileToByteArray(file),
						String.format("application/%s", birtReport.getFormat().name().toLowerCase())
				));
			} catch (Exception e) {
				logger.error("Error generating report data for BIRT report {}: '{}'", birtReport.getId(), entry.getKey(), e);
			}
		}

		return attachments;
	}

	private void sendReportMail(List<JavaMailAttachment> attachments) {
        final String emailRecipientStringList = StringUtils.join(birtReport.getEmailRecipientList(), ",");
        final String emailSubject = birtReport.getEmailSubject();

        String content;
        if (StringUtils.isBlank(birtReport.getEmailDescription())) {
        	content = I18nString.getLocaleString("report.body.text", Locale.of(birtReport.getLanguage()));
        } else {
        	content = birtReport.getEmailDescription();
        }

        final String emailDescription = content;
        serviceLookupFactory.getBeanJavaMailService().sendEmail(birtReport.getCompanyID(), emailRecipientStringList, emailSubject, emailDescription, emailDescription, attachments.toArray(new JavaMailAttachment[0]));
		serviceLookupFactory.getBeanBirtReportService().logSentReport(birtReport);
	}

	private void reportError(Throwable t) {
		logger.error("Error in {}: {}", this.getClass().getName(), t.getMessage(), t);
		// Watchout: NullpointerExceptions have Message "null", which would result in another jobrun, so enter some additional text (classname)
		birtReport.setLastresult(t.getClass().getSimpleName() + ": " + t.getMessage() + "\n" + AgnUtils.getStackTraceString(t));
		serviceLookupFactory.getBeanJavaMailService().sendExceptionMail(birtReport.getCompanyID(), "BirtReport error", t);
		try {
			serviceLookupFactory.getBeanBirtReportService().announceEnd(birtReport);
		} catch (Exception e1) {
			logger.error("Cannot announce BirtReport end: {}", e1.getMessage(), e1);
		}
	}
}
