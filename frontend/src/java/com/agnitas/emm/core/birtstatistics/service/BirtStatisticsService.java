/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import java.io.File;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.birtreport.bean.BirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.BirtReportSettings;
import com.agnitas.emm.core.birtreport.dto.BirtReportDownload;
import com.agnitas.emm.core.birtreport.dto.BirtReportStatisticDto;
import com.agnitas.emm.core.birtstatistics.DateMode;
import com.agnitas.emm.core.birtstatistics.domain.dto.DomainStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingComparisonDto;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientProgressStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatisticDto;
import com.agnitas.emm.core.commons.dto.DateTimeRange;
import com.agnitas.emm.core.userform.form.WebFormStatFrom;
import com.agnitas.emm.core.workflow.beans.WorkflowStatisticDto;
import com.agnitas.reporting.birt.external.beans.RecipientStatusRow;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.logging.log4j.Logger;

public interface BirtStatisticsService {
	
	/**
	 * Generates birt URL for domain statistic
	 *
	 * @param domainStatistic Domain statistic data
	 */
	String getDomainStatisticsUrlWithoutFormat(Admin admin, String sessionId, DomainStatisticDto domainStatistic, boolean forInternalUse);

	/**
	 * Generates birt URL for monthly statistic
	 *
	 * @param monthlyStatistic Monthly statistic data
	 */
	String getMonthlyStatisticsUrlWithoutFormat(Admin admin, String sessionId, MonthlyStatisticDto monthlyStatistic, boolean forInternalUse);
	
	/**
	 * Generates birt URL for recipient monthly statistic
	 *
	 * @param monthlyStatistic Recipient monthly statistic data
	 */
	String getRecipientMonthlyStatisticsUrlWithoutFormat(Admin admin, String sessionId, RecipientProgressStatisticDto monthlyStatistic);
	
	/**
	 * Generates birt URL for every active statistic report type
	 *
	 */
	Map<String, String> getReportStatisticsUrlMap(List<BirtReportSettings> reportSettings, Date currentDate, BirtReport report, int companyId, Integer accountId);

    String getRecipientStatisticUrlWithoutFormat(Admin admin, String sessionId, RecipientStatisticDto recipientStatistic);

    String getMailingStatisticUrl(Admin admin, String sessionId, MailingStatisticDto mailingStatistic);

	DateTimeRange getDateTimeRestrictions(DateTimeRange selectedDate, DateMode dateMode, LocalDateTime mailingStart, int year, Month month);

    String changeFormat(String inputUrl, String newFormat);
    
    String getMailingComparisonStatisticUrl(Admin admin, String sessionId, MailingComparisonDto mailingComparisonDto);
	
	File getBirtMailingComparisonTmpFile(String birtURL, MailingComparisonDto mailingComparisonDto, int companyId);
	
	File getBirtReportTmpFile(final int birtReportId, final String birtUrl, final CloseableHttpClient httpClient, final Logger logger);

	File getBirtStatisticsTmpFile(String birtUrl);

	String getWorkflowStatisticUrl(Admin admin, WorkflowStatisticDto workflowStatisticDto);

    boolean isWorldMailing(Mailing mailing);

	String getWebFormStatUrl(WebFormStatFrom from, Admin admin, String sessionId);

	/**
	 * Generates birt URL for report statistic.
	 */
	String getReportStatisticsUrl(Admin admin, BirtReportStatisticDto settings);

	File getBirtReportTmpFile(BirtReportDownload birtDownload, final int companyId);

	List<RecipientStatusRow> getRecipientStatusStatistic(int targetId, int mailinglistId, Admin admin);
}
