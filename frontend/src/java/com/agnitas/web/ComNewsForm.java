/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web;

import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

public final class ComNewsForm extends ActionForm {
	private static final long serialVersionUID = 5626673515699873079L;

	private int action;
	private int companyID;
	private Map<Integer, String> message;

	public Map<Integer, String> getMessage() {
		return message;
	}

	public void setMessage(Map<Integer, String> message) {
		this.message = message;
	}

	public int getCompanyID() {
		return companyID;
	}

	public void setCompanyID(@VelocityCheck int companyID) {
		this.companyID = companyID;
	}

	/**
	 * Reset all properties to their default values.
	 *
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 */
	@Override
	public void reset(ActionMapping mapping, HttpServletRequest request) {
		// nothing to do
	}

	/**
	 * Validate the properties that have been set from this HTTP request, and return an <code>ActionErrors</code> object that encapsulates any validation errors that have been found. If no errors are
	 * found, return <code>null</code> or an <code>ActionErrors</code> object with no recorded error messages.
	 *
	 * @param mapping
	 *            The mapping used to select this instance
	 * @param request
	 *            The servlet request we are processing
	 */
	@Override
	public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {

		ActionErrors errors = new ActionErrors();
		return errors;
	}

	public void setAction(int action) {
		this.action = action;
	}

	public int getAction() {
		return this.action;
	}
}
