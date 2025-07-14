/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.mailing.autooptimization.service.impl;

import com.agnitas.mailing.autooptimization.beans.impl.AutoOptimizationStatus;
import com.agnitas.beans.impl.MaildropDeleteException;
import com.agnitas.emm.core.maildrop.dao.MaildropStatusDao;
import com.agnitas.emm.common.MailingStatus;

import com.agnitas.dao.MailingDao;
import com.agnitas.mailing.autooptimization.beans.Optimization;
import com.agnitas.mailing.autooptimization.dao.OptimizationDao;
import com.agnitas.mailing.autooptimization.service.OptimizationCommonService;

public class OptimizationCommonServiceImpl implements OptimizationCommonService {
	
	private OptimizationDao optimizationDao;
	private MaildropStatusDao maildropStatusDao;
	private MailingDao mailingDao;
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see com.agnitas.mailing.autooptimization.service.OptimizationService#save(com.agnitas.mailing.autooptimization.beans.Optimization)
	 */
	@Override
	public int save(Optimization optimization) {
		return optimizationDao.save(optimization);
	}

	@Override
	public void unscheduleOptimization(Optimization optimization) throws MaildropDeleteException {
		unscheduleOptimization(optimization, false);
	}

    @Override
    public void unscheduleOptimization(Optimization optimization, boolean testComplete) throws MaildropDeleteException {
        final int previousStatus = optimization.getStatus();

        // remove the send date of the optimization
        optimization.setSendDate(null);
        optimization.setStatus(AutoOptimizationStatus.NOT_STARTED.getCode());
        save(optimization);

        int droppedEntriesCount = 0;

        // remove the maildrop entries of all mailings
        for (Integer mailingID : optimization.getTestmailingIDs()) {
            droppedEntriesCount += maildropStatusDao.deleteUnsentEntries(mailingID);
            if (!(optimization.isTestRun() && testComplete)) {
                mailingDao.updateStatus(optimization.getCompanyID(), mailingID, MailingStatus.CANCELED, null);
            }
        }

        int finalMailingId = optimization.getFinalMailingId();
        if (finalMailingId > 0) {
            droppedEntriesCount += maildropStatusDao.deleteUnsentEntries(finalMailingId);
            if (!(optimization.isTestRun() && testComplete)) {
                mailingDao.updateStatus(optimization.getCompanyID(), finalMailingId, MailingStatus.CANCELED, null);
            }
        }

        if (previousStatus != AutoOptimizationStatus.NOT_STARTED.getCode() && droppedEntriesCount == 0) {
            throw new MaildropDeleteException(
                    "Failure while unscheduling , could not drop entries from maildrop_status_tbl !");
        }
    }

    public void setOptimizationDao(OptimizationDao optimizationDao) {
		this.optimizationDao = optimizationDao;
	}

	public void setMaildropStatusDao(MaildropStatusDao maildropStatusDao) {
		this.maildropStatusDao = maildropStatusDao;
	}

	public void setMailingDao(MailingDao mailingDao) {
		this.mailingDao = mailingDao;
	}
}
