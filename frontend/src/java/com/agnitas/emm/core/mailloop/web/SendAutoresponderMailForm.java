/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mailloop.web;

import org.apache.struts.action.ActionForm;

/**
 * Subclass of {@link ActionForm} to transport request data
 * to {@link SendAutoresponderMailAction}.
 */
public class SendAutoresponderMailForm extends ActionForm {

	/** Serial version UID. */
	private static final long serialVersionUID = -848583978386521523L;
	
	/** ID of mailloop used to trigger auto-responder. */
	private int mailloopID;
	
	/** ID of company of customer that triggered the auto-responder. */
	private int companyID;
	
	/** ID of customer that triggered the auto-responder. */
	private int customerID;
	
	/** Security token used to check validity of request. */
	private String securityToken;
	
	/**
	 * Returns ID of mailloop.
	 * 
	 * @return ID of mailloop
	 */
	public int getMailloopID() {
		return mailloopID;
	}
	
	/**
	 * Set ID of mailloop
	 * 
	 * @param mailloopID ID of mailloop
	 */
	public void setMailloopID(final int mailloopID) {
		this.mailloopID = mailloopID;
	}
	
	/**
	 * Returns ID of company.
	 * 
	 * @return ID of company
	 */
	public int getCompanyID() {
		return companyID;
	}
	
	/**
	 * Set company ID.
	 * 
	 * @param companyID company ID
	 */
	public void setCompanyID(final int companyID) {
		this.companyID = companyID;
	}
	
	/**
	 * Returns customer ID.
	 * 
	 * @return customer ID
	 */
	public int getCustomerID() {
		return customerID;
	}
	
	/**
	 * Set customer ID.
	 * 
	 * @param customerID customer ID
	 */
	public void setCustomerID(final int customerID) {
		this.customerID = customerID;
	}

	/**
	 * Returns the security token.
	 * 
	 * @return security token
	 */
	public String getSecurityToken() {
		return this.securityToken;
	}
	
	/**
	 * Set the security token.
	 * 
	 * @param securityToken security token
	 */
	public void setSecurityToken(final String securityToken) {
		this.securityToken = securityToken;
	}
}
