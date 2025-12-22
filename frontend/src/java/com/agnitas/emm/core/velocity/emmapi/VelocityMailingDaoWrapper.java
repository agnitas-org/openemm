/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity.emmapi;

import java.util.Objects;

import com.agnitas.dao.MailingDao;

import com.agnitas.beans.Mailing;

public final class VelocityMailingDaoWrapper implements VelocityMailingDao {
	
	/** ID of company running current Velocity script. */
	private final int runningCompanyId;
	
	private final MailingDao mailingDao;
	private final CompanyAccessCheck companyAccessCheck;
	
	public VelocityMailingDaoWrapper(final int runningCompanyId, final MailingDao mailingDao, final CompanyAccessCheck companyAccessCheck) {
		this.runningCompanyId = runningCompanyId;
		this.mailingDao = Objects.requireNonNull(mailingDao, "MailingDao is null");
		this.companyAccessCheck = Objects.requireNonNull(companyAccessCheck, "companyAccessCheck is null");
	}

	@Override
	public final Mailing getMailing(final int mailingID, final int companyID) {
		this.companyAccessCheck.checkCompanyAccess(companyID, this.runningCompanyId);
		
		return this.mailingDao.getMailing(mailingID, companyID);
	}

	@Override
	public final int saveMailing(final Mailing mailing) {
		this.companyAccessCheck.checkCompanyAccess(mailing.getCompanyID(), this.runningCompanyId);
		
		return this.mailingDao.saveMailing(mailing, true);
	}

	@Override
	public final int saveMailing(final Mailing mailing, final boolean preserveTrackableLinks) {
		this.companyAccessCheck.checkCompanyAccess(mailing.getCompanyID(), this.runningCompanyId);
		
		return this.mailingDao.saveMailing(mailing, preserveTrackableLinks);
	}

	@Override
	public int saveMailing(Mailing mailing, boolean preserveTrackableLinks, boolean errorTolerant) {
		this.companyAccessCheck.checkCompanyAccess(mailing.getCompanyID(), this.runningCompanyId);
		
		return this.mailingDao.saveMailing(mailing, preserveTrackableLinks, errorTolerant, false);
	}

}
