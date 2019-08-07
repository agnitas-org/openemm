/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.job;

import org.apache.log4j.Logger;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.mailing.bean.ComFollowUpStats;
import com.agnitas.emm.core.mailing.dao.ComFollowUpStatsDao;

public class ComFollowUpStatJob extends Thread {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(ComFollowUpStatJob.class);
	
	private ComFollowUpStats followUpStats;
	private ComMailingDao mailingDao;
	private ComFollowUpStatsDao followupDao;
	
	public ComFollowUpStatJob(ComFollowUpStats followUpStats, ComMailingDao mailingDao, ComFollowUpStatsDao followupDao) {
		this.followUpStats = followUpStats;
		this.mailingDao = mailingDao;
		this.followupDao = followupDao;
	}
	
	@Override
	public void run() {
		try {
			long duration = System.currentTimeMillis();		
			int recipients = mailingDao.getFollowUpStat(followUpStats, true);
			duration = System.currentTimeMillis() - duration;
			followupDao.updateStatEntry(followUpStats.getResultID(), duration, recipients);
		} catch (Exception e) {
			logger.error("Cannot calculate followUpStats: " + e.getMessage(), e);
		}
	}
}
