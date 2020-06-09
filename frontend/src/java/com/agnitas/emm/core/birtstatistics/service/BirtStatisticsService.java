/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.ComAdmin;
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
import org.apache.commons.httpclient.HttpClient;
import org.apache.log4j.Logger;

public interface BirtStatisticsService {
	
	/**
	 * Generates birt URL for domain statistic
	 *
	 * @param admin
	 * @param sessionId
	 * @param domainStatistic Domain statistic data
	 * @return
	 * @throws Exception
	 */
	String getDomainStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, DomainStatisticDto domainStatistic, boolean forInternalUse) throws Exception;
	
	
	/**
	 * Generates birt URL for monthly statistic
	 *
	 * @param admin
	 * @param sessionId
	 * @param monthlyStatistic Monthly statistic data
	 * @return
	 * @throws Exception
	 */
	String getMonthlyStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, MonthlyStatisticDto monthlyStatistic, boolean forInternalUse) throws Exception;
	
	/**
	 * Generates birt URL for recipient monthly statistic
	 *
	 * @param admin
	 * @param sessionId
	 * @param monthlyStatistic Recipient monthly statistic data
	 * @return
	 * @throws Exception
	 */
	String getRecipientMonthlyStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, RecipientProgressStatisticDto monthlyStatistic) throws Exception;
	
	/**
	 * Generates birt URL for every active statistic report type
	 *
	 * @param reportSettings
	 * @param currentDate
	 * @param report
	 * @param companyId
	 * @param accountId
	 * @return
	 * @throws Exception
	 */
	Map<String, String> getReportStatisticsUrlMap(List<ComBirtReportSettings> reportSettings, Date currentDate, ComBirtReport report, int companyId, Integer accountId) throws Exception;

	String generateUrlWithParamsForInternalAccess(Map<String, Object> parameters);

	String generateUrlWithParamsForExternalAccess(Map<String, Object> parameters);
    
    String getRecipientStatisticUrlWithoutFormat(ComAdmin admin, String sessionId, RecipientStatisticDto recipientStatistic) throws Exception;

    String getMailingStatisticUrl(ComAdmin admin, String sessionId, MailingStatisticDto mailingStatistic) throws Exception;

    String changeFormat(String inputUrl, String newFormat);
    
    String getMailingComparisonStatisticUrl(ComAdmin admin, String sessionId, MailingComparisonDto mailingComparisonDto) throws Exception;
	
	File getBirtMailingComparisonTmpFile(String birtURL, MailingComparisonDto mailingComparisonDto) throws Exception;
	
	File getBirtReportTmpFile(int birtReportId, String birtUrl, HttpClient httpClient, Logger logger);
	
	String getRecipientStatusStatisticUrl(ComAdmin admin, String sessionId, RecipientStatusStatisticDto recipientStatusDto) throws Exception;

	String getOptimizationStatisticUrl(ComAdmin admin, OptimizationStatisticDto optimizationDto) throws Exception;
}
