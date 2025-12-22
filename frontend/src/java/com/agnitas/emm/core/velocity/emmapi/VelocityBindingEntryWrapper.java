/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.velocity.emmapi;

import java.util.Date;
import java.util.Objects;

import com.agnitas.beans.BindingEntry;

public final class VelocityBindingEntryWrapper implements VelocityBindingEntry {
	
	/** ID of company running current Velocity script. */
	private final int runningCompanyId;
	
	private final BindingEntry bindingEntry;
	private final CompanyAccessCheck companyAccessCheck;
	
	public VelocityBindingEntryWrapper(final int runningCompanyId, final BindingEntry bindingEntry, final CompanyAccessCheck companyAccessCheck) {
		this.runningCompanyId = runningCompanyId;
		this.bindingEntry = Objects.requireNonNull(bindingEntry, "BindingEntry is null");
		this.companyAccessCheck = Objects.requireNonNull(companyAccessCheck, "companyAccessCheck is null");
	}

	@Override
	public final int getCustomerID() {
		return bindingEntry.getCustomerID();
	}

	@Override
	public final int getMailinglistID() {
		return bindingEntry.getMailinglistID();
	}

	@Override
	public final String getUserType() {
		return bindingEntry.getUserType();
	}

	@Override
	public final int getUserStatus() {
		return bindingEntry.getUserStatus();
	}

	@Override
	public final String getUserRemark() {
		return bindingEntry.getUserRemark();
	}

	@Override
	public final Date getChangeDate() {
		return bindingEntry.getChangeDate();
	}

	@Override
	public final int getExitMailingID() {
		return bindingEntry.getExitMailingID();
	}

	@Override
	public final int getMediaType() {
		return bindingEntry.getMediaType();
	}

	@Override
	public final void setCustomerID(final int customerID) {
		bindingEntry.setCustomerID(customerID);
	}

	@Override
	public final void setExitMailingID(final int mailingID) {
		bindingEntry.setExitMailingID(mailingID);
	}

	@Override
	public final void setMailinglistID(final int mailinglistID) {
		bindingEntry.setMailinglistID(mailinglistID);
	}

	@Override
	public final void setMediaType(final int mediaType) {
		bindingEntry.setMediaType(mediaType);
	}

	@Override
	public final void setUserRemark(final String remark) {
		bindingEntry.setUserRemark(remark);
	}

	@Override
	public final void setChangeDate(final Date ts) {
		bindingEntry.setChangeDate(ts);
	}

	@Override
	public final void setUserStatus(final int us) {
		bindingEntry.setUserStatus(us);
	}

	@Override
	public final void setUserType(final String ut) {
		bindingEntry.setUserType(ut);
	}

	@Override
	public final boolean insertNewBindingInDB(final int companyID) {
		this.companyAccessCheck.checkCompanyAccess(companyID, this.runningCompanyId);
		
		return bindingEntry.insertNewBindingInDB(companyID);
	}

	@Override
	public final boolean updateBindingInDB(final int companyID) {
		this.companyAccessCheck.checkCompanyAccess(companyID, this.runningCompanyId);
		
		return bindingEntry.updateBindingInDB(companyID);
	}

	@Override
	public final boolean optOutEmailAdr(final String email, final int companyID) {
		this.companyAccessCheck.checkCompanyAccess(companyID, this.runningCompanyId);
		
		return bindingEntry.optOutEmailAdr(email, companyID);
	}

	@Override
	public final boolean getUserBindingFromDB(final int companyID) {
		this.companyAccessCheck.checkCompanyAccess(companyID, this.runningCompanyId);
		
		return bindingEntry.getUserBindingFromDB(companyID);
	}

	@Override
	public final boolean updateStatusInDB(final int companyID) {
		this.companyAccessCheck.checkCompanyAccess(companyID, this.runningCompanyId);
		
		return bindingEntry.updateStatusInDB(companyID);
	}
		
}
