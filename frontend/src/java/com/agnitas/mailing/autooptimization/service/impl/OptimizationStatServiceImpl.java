/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.service.impl;

import java.util.Hashtable;

import com.agnitas.dao.MailingDao;
import com.agnitas.mailing.autooptimization.beans.Optimization;
import com.agnitas.mailing.autooptimization.dao.OptimizationStatDao;
import com.agnitas.mailing.autooptimization.service.OptimizationStatService;
import com.agnitas.reporting.birt.external.dao.BirtCompanyDao;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.mailing.autooptimization.beans.CampaignStatEntry;

public class OptimizationStatServiceImpl implements OptimizationStatService {

	private OptimizationStatDao statDao;
	private MailingDao mailingDao;
	private BirtCompanyDao birtCompanyDao;
	
	@Override
	public CampaignStatEntry getStat(int mailingID, int companyID) {
		CampaignStatEntry statEntry = new CampaignStatEntry();

		statEntry.setShortname(mailingDao.getMailingName(mailingID, companyID));

		statEntry.setTotalMails(statDao.getSend(mailingID, CommonKeys.TYPE_WORLDMAILING));
		/* AGNEMM-1517: Disabled
		statEntry.setBounces(statDao.getBounces(mailingID, companyID));
		*/
		statEntry.setClicks(statDao.getClicks(mailingID, companyID, CommonKeys.ALL_SUBSCRIBERS));
		statEntry.setOpened(statDao.getOpened(mailingID, companyID, CommonKeys.ALL_SUBSCRIBERS));
		/* AGNEMM-1517: Disabled
		statEntry.setOptouts(statDao.getOptOuts(mailingID, companyID));
		*/
        if (birtCompanyDao.hasDeepTrackingTables(companyID)) {
            statEntry.setRevenue(statDao.getRevenue(mailingID, companyID));
        } else {
            statEntry.setRevenue(0);
        }

		statEntry.setClickRate(statEntry.getTotalMails() != 0 ? ((double)statEntry.getClicks()) / ((double)statEntry.getTotalMails()) : 0  );
		statEntry.setOpenRate(statEntry.getTotalMails() != 0 ? ((double)statEntry.getOpened()) / ((double)statEntry.getTotalMails()) : 0  );
		
		return statEntry;
	}

	@Override
	public Hashtable<Integer, CampaignStatEntry> getStat(Optimization optimization) {
	
		Hashtable<Integer, CampaignStatEntry>  optimizationStat = new Hashtable<>();
		
		if (optimization.getGroup1() > 0) {
			optimizationStat.put(optimization.getGroup1(), getStat(optimization.getGroup1(),optimization.getCompanyID()));
		}
		
		if (optimization.getGroup2() > 0) {
			optimizationStat.put(optimization.getGroup2(), getStat(optimization.getGroup2(),optimization.getCompanyID()));
		}
		
		if (optimization.getGroup3() > 0) {
			optimizationStat.put(optimization.getGroup3(), getStat(optimization.getGroup3(),optimization.getCompanyID()));
		}
		
		if (optimization.getGroup4() > 0) {
			optimizationStat.put(optimization.getGroup4(), getStat(optimization.getGroup4(),optimization.getCompanyID()));
		}
		
		if (optimization.getGroup5() > 0) {
			optimizationStat.put(optimization.getGroup5(), getStat(optimization.getGroup5(),optimization.getCompanyID()));
		}
		
		return optimizationStat;
	}
	
	public void setStatDao(OptimizationStatDao statDao) {
		this.statDao = statDao;
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}

    public void setBirtCompanyDao(BirtCompanyDao birtCompanyDao) {
        this.birtCompanyDao = birtCompanyDao;
    }
}
