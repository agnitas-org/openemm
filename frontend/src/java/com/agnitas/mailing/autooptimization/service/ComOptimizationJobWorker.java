/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.service;

import java.util.List;
import java.util.stream.Collectors;

import org.agnitas.service.JobWorker;
import org.agnitas.util.AgnUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *    VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'AutoOptimization', CURRENT_TIMESTAMP, NULL, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, NULL, 'com.agnitas.mailing.autooptimization.service.ComOptimizationJobWorker', 1);
 */
public class ComOptimizationJobWorker extends JobWorker {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(ComOptimizationJobWorker.class);
		
	@Override
	public String runJob() throws Exception {
		// Invoke the secured method to finish optimizations. This method will terminate, if another thread previously started the process and has not terminated yet.
//		((ComOptimizationService) applicationContext.getBean("optimizationService")).finishOptimizationsSingle();

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
		serviceLookupFactory.getBeanOptimizationService().finishOptimizationsSingle(includedCompanyIds, excludedCompanyIds);
		
		return null;
	}
}
