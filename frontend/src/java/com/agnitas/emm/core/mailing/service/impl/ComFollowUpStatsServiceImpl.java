/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailing.service.impl;

import java.sql.Timestamp;
import java.util.Date;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.log4j.Logger;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.mailing.bean.ComFollowUpStats;
import com.agnitas.emm.core.mailing.dao.ComFollowUpStatsDao;
import com.agnitas.emm.core.mailing.job.ComFollowUpStatJob;
import com.agnitas.emm.core.mailing.service.ComFollowUpStatsService;

public class ComFollowUpStatsServiceImpl implements ComFollowUpStatsService {
	private static final transient Logger logger = Logger.getLogger(ComFollowUpStatsServiceImpl.class);
	ComMailingDao mailingDao;
	ComFollowUpStatsDao followupDao;

	public void setFollowupDao(ComFollowUpStatsDao followupDao) {
		this.followupDao = followupDao;
	}

	@Override
	public int getStats(int mailingID, int baseMail, @VelocityCheck int companyID, boolean useTargetGroups) throws Exception {		
		int returnValue = 0;		
		String followUpType = mailingDao.getFollowUpType(mailingID);
		returnValue = mailingDao.getFollowUpStat(mailingID, baseMail, followUpType, companyID, useTargetGroups);
		return returnValue;
	}
	

	@Override
	public int startCalculation(int followupID, int baseMail, @VelocityCheck int companyID, String sessionID, boolean useTargetGroups) {
		ComFollowUpStats followUpStats = followupDao.getStatEntry(followupID, sessionID);
		int resultID = 0;		
		if ( followUpStats != null ) {
			Date changeDateFollowup = mailingDao.getChangeDate(followupID);
			Timestamp creationDateStat = followUpStats.getCreationDate();
			if (changeDateFollowup.before(creationDateStat)) {
				if (logger.isInfoEnabled()) logger.info("Worker-Thread already running. FollowupID: " + followupID + " BaseMailID: " + baseMail + 
						" companyID: " + companyID + " SessionID: " + sessionID);
				resultID = followUpStats.getResultID();
			} else {
				resultID = followupDao.createNewStatEntry(companyID, baseMail, followupID, sessionID, "");
				followUpStats = followupDao.getStatEntry(resultID);	// load freshly generated data
			}
		} else {
			resultID = followupDao.createNewStatEntry(companyID, baseMail, followupID, sessionID, "");
			followUpStats = followupDao.getStatEntry(resultID);	// load freshly generated data
		}		
		// create worker thread		
		ComFollowUpStatJob job = new ComFollowUpStatJob(followUpStats, mailingDao, followupDao);
		job.start();
		return resultID;
	}

	@Override
	public String checkStats(int followUpID, String sessionID) {
		ComFollowUpStats followUpStats = followupDao.getStatEntry(followUpID, sessionID);
		if (followUpStats == null || followUpStats.getDuration() == 0) {
			return null;
		}
		return Integer.toString(followUpStats.getResultValue());
	}	
	
	// set by spring.
	public void setMailingDao(ComMailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

	@Override
	public ComFollowUpStats getStatEntry(int followUpID, String sessionID) {
		ComFollowUpStats followUpStats = followupDao.getStatEntry(followUpID, sessionID);		
		return followUpStats;
	}
}
