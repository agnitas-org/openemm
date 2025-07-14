/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop.web;

/**
 * Form bean for {@link SendAutoresponderController}.
 */
public final class SendAutoresponderForm {

	/** ID of mailloop. */
	private int mailloopID;
	
	/** ID of company. */
	private int companyID;
	
	/** ID of customer. */
	private int customerID;
	
	/** Security token. */
	private String securityToken;
	
	/**
	 * Returns the ID of the mailloop.
	 * 
	 * @return ID of the mailloop
	 */
	public final int getMailloopID() {
		return mailloopID;
	}
	
	/**
	 * Sets the ID of the mailloop.
	 * 
	 * @param mailloopID ID of the mailloop
	 */
	public final void setMailloopID(final int mailloopID) {
		this.mailloopID = mailloopID;
	}
	
	/**
	 * Returns the ID of the company.
	 * 
	 * @return ID of the company
	 */
	public final int getCompanyID() {
		return companyID;
	}
	
	/**
	 * Sets the ID of the company.
	 * 
	 * @param companyID ID of the company
	 */
	public final void setCompanyID(final int companyID) {
		this.companyID = companyID;
	}
	
	/**
	 * Returns the ID of the customer.
	 * 
	 * @return ID of the customer
	 */
	public final int getCustomerID() {
		return customerID;
	}
	
	/**
	 * Sets the ID of the customer.
	 * 
	 * @param customerID ID of the customer
	 */
	public final void setCustomerID(final int customerID) {
		this.customerID = customerID;
	}
	
	/**
	 * Returns the security token.
	 * 
	 * @return security token
	 */
	public final String getSecurityToken() {
		return securityToken;
	}
	
	/**
	 * Sets the security token.
	 * 
	 * @param securityToken security token
	 */
	public final void setSecurityToken(final String securityToken) {
		this.securityToken = securityToken;
	}
		
}
