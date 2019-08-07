/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.service.job;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;
import java.util.stream.Collectors;

import org.agnitas.dao.RdirTrafficAmountDao;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.service.JobWorker;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * This JobWorker aggregates the entries in rdir_traffic_amount_tbl by day
 * 
 * Example Insert in DB:
 *  INSERT INTO job_queue_tbl (id, description, created, laststart, running, lastresult, startaftererror, lastduration, `interval`, nextstart, hostname, runclass, deleted)
 *     VALUES ((SELECT MAX(id) + 1 FROM job_queue_tbl), 'AggregateRdirTrafficStatisticJobWorker', CURRENT_TIMESTAMP, null, 0, 'OK', 0, 0, '***0;***5', CURRENT_TIMESTAMP, null, 'com.agnitas.service.job.AggregateRdirTrafficStatisticJobWorker', 0);
 */
public class AggregateRdirTrafficStatisticJobWorker extends JobWorker {
	@SuppressWarnings("unused")
	private static final transient Logger logger = Logger.getLogger(AggregateRdirTrafficStatisticJobWorker.class);

	@Override
	public void runJob() throws Exception {
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
		
		int workingTimeStartHourOfDay = 0;
		String workingTimeStartHourOfDayString = job.getParameters().get("workingTimeStartHourOfDay");
		if (StringUtils.isNotBlank(workingTimeStartHourOfDayString)) {
			workingTimeStartHourOfDay = Integer.parseInt(workingTimeStartHourOfDayString);
		}

		int workingTimeEndHourOfDay = 4;
		String workingTimeEndHourOfDayString = job.getParameters().get("workingTimeEndHourOfDay");
		if (StringUtils.isNotBlank(workingTimeEndHourOfDayString)) {
			workingTimeEndHourOfDay = Integer.parseInt(workingTimeEndHourOfDayString);
		}
		
		RdirTrafficAmountDao rdirTrafficAmountDao = daoLookupFactory.getBeanRdirTrafficAmountDao();
		
		List<Integer> companiesToAggregate = rdirTrafficAmountDao.getCompaniesForAggregation(includedCompanyIds, excludedCompanyIds);
		for (int companyID : companiesToAggregate) {
			if (!checkTime(companyID, workingTimeStartHourOfDay, workingTimeEndHourOfDay)) {
				break;
			} else {
				Date dateToAggregate = DateUtilities.removeTime(DateUtilities.getDateOfDaysAgo(1), TimeZone.getDefault());
				rdirTrafficAmountDao.aggregateExistingTrafficAmountEntries(companyID, dateToAggregate);
			}
		}
	}

	private boolean checkTime(int currentCompanyID, int workingTimeStartHourOfDay, int workingTimeEndHourOfDay) {
		int currentHourOfDay = new GregorianCalendar().get(GregorianCalendar.HOUR_OF_DAY);
		boolean goOnWorking = workingTimeStartHourOfDay <= currentHourOfDay && currentHourOfDay < workingTimeEndHourOfDay;
		if (!goOnWorking) {
			daoLookupFactory.getBeanJavaMailService().sendEmail(configService.getValue(ConfigValue.Mailaddress_Error), "AggregateRdirTrafficStatisticJobWorker didn't finish", "Time is up! Last company aggregated was: " + currentCompanyID, null);
		}
		return goOnWorking;
	}
}
