/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Mailing;
import com.agnitas.emm.core.workflow.beans.WorkflowStatisticDto;
import org.apache.commons.httpclient.HttpClient;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.logging.log4j.Logger;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtstatistics.domain.dto.DomainStatisticDto;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingComparisonDto;
import com.agnitas.emm.core.birtstatistics.mailing.dto.MailingStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientProgressStatisticDto;
import com.agnitas.emm.core.birtstatistics.optimization.dto.OptimizationStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatisticDto;
import com.agnitas.emm.core.birtstatistics.recipient.dto.RecipientStatusStatisticDto;

public interface BirtStatisticsService {
	
	/**
	 * Generates birt URL for domain statistic
	 *
	 * @param domainStatistic Domain statistic data
	 */
	String getDomainStatisticsUrlWithoutFormat(Admin admin, String sessionId, DomainStatisticDto domainStatistic, boolean forInternalUse) throws Exception;
	
	
	/**
	 * Generates birt URL for monthly statistic
	 *
	 * @param monthlyStatistic Monthly statistic data
	 */
	String getMonthlyStatisticsUrlWithoutFormat(Admin admin, String sessionId, MonthlyStatisticDto monthlyStatistic, boolean forInternalUse) throws Exception;
	
	/**
	 * Generates birt URL for recipient monthly statistic
	 *
	 * @param monthlyStatistic Recipient monthly statistic data
	 */
	String getRecipientMonthlyStatisticsUrlWithoutFormat(Admin admin, String sessionId, RecipientProgressStatisticDto monthlyStatistic) throws Exception;
	
	/**
	 * Generates birt URL for every active statistic report type
	 *
	 */
	Map<String, String> getReportStatisticsUrlMap(List<ComBirtReportSettings> reportSettings, Date currentDate, ComBirtReport report, int companyId, Integer accountId) throws Exception;

	String generateUrlWithParams(Map<String, Object> parameters, boolean internalAccess, final int companyID);

    String getRecipientStatisticUrlWithoutFormat(Admin admin, String sessionId, RecipientStatisticDto recipientStatistic) throws Exception;

    String getMailingStatisticUrl(Admin admin, String sessionId, MailingStatisticDto mailingStatistic) throws Exception;

    String changeFormat(String inputUrl, String newFormat);
    
    String getMailingComparisonStatisticUrl(Admin admin, String sessionId, MailingComparisonDto mailingComparisonDto) throws Exception;
	
	File getBirtMailingComparisonTmpFile(String birtURL, MailingComparisonDto mailingComparisonDto, final int companyId) throws Exception;
	
	/**
	 * @see #getBirtReportTmpFile(int, String, CloseableHttpClient, Logger)
	 */
	@Deprecated
	File getBirtReportTmpFile(int birtReportId, String birtUrl, HttpClient httpClient, Logger logger);
	
	File getBirtReportTmpFile(final int birtReportId, final String birtUrl, final CloseableHttpClient httpClient, final Logger logger);
	
	String getRecipientStatusStatisticUrl(Admin admin, String sessionId, RecipientStatusStatisticDto recipientStatusDto) throws Exception;

	String getOptimizationStatisticUrl(Admin admin, OptimizationStatisticDto optimizationDto) throws Exception;

	String getWorkflowStatisticUrl(Admin admin, WorkflowStatisticDto workflowStatisticDto) throws Exception;

	String getUserFormTrackableLinkStatisticUrl(Admin admin, String sessionId, int formId) throws Exception;

    boolean isWorldMailing(Mailing mailing);
}
