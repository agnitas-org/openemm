/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.birtstatistics.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.birtreport.bean.ComBirtReport;
import com.agnitas.emm.core.birtreport.bean.impl.ComBirtReportSettings;
import com.agnitas.emm.core.birtstatistics.domain.dto.DomainStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.MonthlyStatisticDto;
import com.agnitas.emm.core.birtstatistics.monthly.dto.RecipientStatisticDto;

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
	String getDomainStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, DomainStatisticDto domainStatistic) throws Exception;
	
	
	/**
	 * Generates birt URL for monthly statistic
	 *
	 * @param admin
	 * @param sessionId
	 * @param monthlyStatistic Monthly statistic data
	 * @return
	 * @throws Exception
	 */
	String getMonthlyStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, MonthlyStatisticDto monthlyStatistic) throws Exception;
	
	/**
	 * Generates birt URL for recipient monthly statistic
	 *
	 * @param admin
	 * @param sessionId
	 * @param monthlyStatistic Recipient monthly statistic data
	 * @return
	 * @throws Exception
	 */
	String getRecipientMonthlyStatisticsUrlWithoutFormat(ComAdmin admin, String sessionId, RecipientStatisticDto monthlyStatistic) throws Exception;
	
	/**
	 * Generates birt URL for every active statistic report type
	 *
	 * @param reportSettings
	 * @param admin
	 * @param currentDate
	 * @param report
	 * @param companyId
	 * @param accountId
	 * @return
	 * @throws Exception
	 */
	Map<String, String> getReportStatisticsUrlMap(List<ComBirtReportSettings> reportSettings, ComAdmin admin, Date currentDate, ComBirtReport report, int companyId, Integer accountId) throws Exception;
    
    String generateUid(ComAdmin admin) throws Exception;
	
	String generateUrlWithParamsForInternalAccess(Map<String, Object> parameters);

	String generateUrlWithParamsForExternalAccess(Map<String, Object> parameters);
}
