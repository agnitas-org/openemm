/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import java.util.List;
import java.util.stream.Collectors;

import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.JobWorker;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.dao.AnonymizeStatisticsDao;
import com.agnitas.dao.ComCompanyDao;
import com.agnitas.emm.core.company.bean.CompanyEntry;

/**
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *     VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'AnonymizeStatistics', CURRENT_TIMESTAMP, null, 0, 'OK', 0, 0, '0000', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.AnonymizeStatisticsJobWorker', 0);
 */
public class AnonymizeStatisticsJobWorker extends JobWorker {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(AnonymizeStatisticsJobWorker.class);

	@Override
	public String runJob() throws Exception {
		List<Integer> includedCompanyIds = null;
		String includedCompanyIdsString = job.getParameters().get("includedCompanyIds");
		if (StringUtils.isNotBlank(includedCompanyIdsString)) {
			includedCompanyIds = AgnUtils.splitAndTrimList(includedCompanyIdsString).stream().map(Integer::parseInt).collect(Collectors.toList());
		}
		List<Integer> excludedCompanyIds = null;
		String excludedCompanyIdsString = job.getParameters().get("excludedCompanyIds");
		if (StringUtils.isNotBlank(excludedCompanyIdsString)) {
			excludedCompanyIds = AgnUtils.splitAndTrimList(excludedCompanyIdsString).stream().map(Integer::parseInt).collect(Collectors.toList());
		}
		
		ComCompanyDao companyDao = daoLookupFactory.getBeanCompanyDao();
		AnonymizeStatisticsDao anonymizeStatisticsDao = daoLookupFactory.getBeanAnonymizeStatisticsDao();
		
		for (CompanyEntry company : companyDao.getActiveCompaniesLight()) {
			int companyID = company.getCompanyId();
			if ((includedCompanyIds == null || includedCompanyIds.contains(companyID))
				&& (excludedCompanyIds == null || !excludedCompanyIds.contains(companyID))
				&& configService.getBooleanValue(ConfigValue.AnonymizeTrackingVetoRecipients, companyID)) {
				anonymizeStatisticsDao.anonymizeStatistics(companyID);
			}
		}
		
		return null;
	}
}
