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

	/** The logger. */
	private static final transient Logger LOGGER = Logger.getLogger(AnonymizeStatisticsJobWorker.class);

	@Override
	public String runJob() throws Exception {
		try {
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("%s started", this.getClass().getSimpleName()));
			}
			
			final List<Integer> includedCompanyIds = parameterAsIntegerListOrNull("includedCompanyIds");
			final List<Integer> excludedCompanyIds = parameterAsIntegerListOrNull("excludedCompanyIds");
	
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format(
						"Included company IDs (parsed list): %s",
						includedCompanyIds == null || includedCompanyIds.isEmpty() ? "all" : includedCompanyIds.stream().map(x -> x.toString()).collect(Collectors.joining(", "))
						));
				LOGGER.info(String.format(
						"Excluded company IDs (parsed list): %s",
						excludedCompanyIds == null || excludedCompanyIds.isEmpty() ? "none" : excludedCompanyIds.stream().map(x -> x.toString()).collect(Collectors.joining(", "))
						));
			}
					
			final ComCompanyDao companyDao = daoLookupFactory.getBeanCompanyDao();
			final AnonymizeStatisticsDao anonymizeStatisticsDao = daoLookupFactory.getBeanAnonymizeStatisticsDao();
			
			for (final CompanyEntry company : companyDao.getActiveCompaniesLight(true)) {
				final int companyID = company.getCompanyId();
				
				final boolean included = includedCompanyIds == null || includedCompanyIds.contains(companyID);
				final boolean excluded = excludedCompanyIds != null && excludedCompanyIds.contains(companyID);
				final boolean anonymize = configService.getBooleanValue(ConfigValue.AnonymizeTrackingVetoRecipients, companyID);
				
				if(LOGGER.isInfoEnabled()) {
					LOGGER.debug(String.format("Processing company ID %d (included: %b, excluded %b, anonymize: %b)", companyID, included, excluded, anonymize));
				}
				
				if (included && !excluded && anonymize) {
					final boolean anonymizeAll = configService.getBooleanValue(ConfigValue.AnonymizeAllRecipients, companyID);
			
					if(LOGGER.isInfoEnabled()) {
						LOGGER.debug(String.format("Running anonymization on company ID %d (anonymize all: %b)", companyID, anonymizeAll));
					}
					
					anonymizeStatisticsDao.anonymizeStatistics(companyID, anonymizeAll);
				} else {
					if(LOGGER.isInfoEnabled()) {
						LOGGER.debug(String.format("Anonymization not run on company ID %d", companyID));
					}
				}
			}
	
			if(LOGGER.isInfoEnabled()) {
				LOGGER.info(String.format("%s finished without exception", this.getClass().getSimpleName()));
			}
		} catch(final Exception e) {
			LOGGER.info(String.format("%s finished with exception", this.getClass().getSimpleName()), e);
			throw e;
		}
		
		return null;
	}
}
