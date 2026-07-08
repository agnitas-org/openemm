/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.exception;

public class FormNotFoundException extends RuntimeException {

	/** Serial version UID. */
	private static final long serialVersionUID = -3880039723225788494L;
	
	private final int companyID;
	private final String formName;

	public FormNotFoundException(final int companyID, final String formName) {
		super(String.format("Form '%s' not found for company ID %d", formName, companyID));
		
		this.companyID = companyID;
		this.formName = formName;
	}

	public int getCompanyID() {
		return companyID;
	}

	public String getFormName() {
		return formName;
	}
}
