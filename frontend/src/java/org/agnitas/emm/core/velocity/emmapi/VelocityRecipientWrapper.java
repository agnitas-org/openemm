/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.velocity.emmapi;

import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.BindingEntry;
import org.agnitas.beans.Recipient;

public final class VelocityRecipientWrapper implements VelocityRecipient {
	
	/** ID of company running current Velocity script. */
	private final int runningCompanyId;
	
	private final Recipient recipient;
	private final CompanyAccessCheck companyAccessCheck;
	
	public VelocityRecipientWrapper(final int runningCompanyId, final Recipient recipient, final CompanyAccessCheck companyAccessCheck) {
		this.runningCompanyId = runningCompanyId;
		this.recipient = Objects.requireNonNull(recipient, "Recipient is null");
		this.companyAccessCheck = Objects.requireNonNull(companyAccessCheck, "companyAccessCheck is null");
	}
	
	public final Recipient getWrappedRecipient() {
		return this.recipient;
	}

	@Override
	public final int getCustomerID() {
		return this.recipient.getCustomerID();
	}

	@Override
	public final int getGender() {
		return this.recipient.getGender();
	}

	@Override
	public final String getFirstname() {
		return this.recipient.getFirstname();
	}

	@Override
	public final String getLastname() {
		return this.recipient.getLastname();
	}

	@Override
	public final String getEmail() {
		return this.recipient.getEmail();
	}

	@Override
	public final int findByKeyColumn(final String col, final String value) {
		this.companyAccessCheck.checkCompanyAccess(this.recipient.getCompanyID(), this.runningCompanyId);
		
		return this.recipient.findByKeyColumn(col, value);
	}

	@Override
	public final Map<String, Object> getCustomerDataFromDb() {
		this.companyAccessCheck.checkCompanyAccess(this.recipient.getCompanyID(), this.runningCompanyId);
		
		return this.recipient.getCustomerDataFromDb();
	}

	@Override
	public final int insertNewCust() {
		this.companyAccessCheck.checkCompanyAccess(this.recipient.getCompanyID(), this.runningCompanyId);
		
		try {
			return this.recipient.insertNewCustWithException();
		} catch(final Exception e) {
			return 0;
		}
	}

	@Override
	public final boolean importRequestParameters(final Map<String, Object> requestParameters, final String suffix) {
		return this.recipient.importRequestParameters(requestParameters, suffix);
	}

	@Override
	public final Map<Integer, Map<Integer, BindingEntry>> loadAllListBindings() {
		this.companyAccessCheck.checkCompanyAccess(this.recipient.getCompanyID(), this.runningCompanyId);
		
		return this.recipient.loadAllListBindings();
	}

	@Override
	public final boolean loadCustDBStructure() {
		this.companyAccessCheck.checkCompanyAccess(this.recipient.getCompanyID(), this.runningCompanyId);
		
		return this.recipient.loadCustDBStructure();
	}

	@Override
	public final void resetCustParameters() {
		this.recipient.resetCustParameters();
	}

	@Override
	public final void setCompanyID(final int companyID) {
		this.recipient.setCompanyID(companyID);
	}

	@Override
	public final void setCustomerID(final int customerID) {
		this.recipient.setCustomerID(customerID);
	}

	@Override
	public final boolean updateInDB() {
		this.companyAccessCheck.checkCompanyAccess(this.recipient.getCompanyID(), this.runningCompanyId);
		
		try {
			return this.recipient.updateInDbWithException();
		} catch (Exception e) {
			return false;
		}
	}

	@Override
	public final void deleteCustomerDataFromDb() {
		this.recipient.deleteCustomerDataFromDb();
	}

	@Override
	public final String getCustParameters(final String key) {
		return this.recipient.getCustParameters(key);
	}

	@Override
	public final Map<String, Object> getCustParameters() {
		return this.recipient.getCustParameters();
	}

	@Override
	public final void setCustParameters(final Map<String, Object> custParameters) {
		this.recipient.setCustParameters(custParameters);
	}

	@Override
	public final void setCustParameters(final String aKey, final String custParameters) {
		this.recipient.setCustParameters(aKey, custParameters);
	}

	@Override
	public final String toString() {
		return this.recipient.toString();
	}
}
