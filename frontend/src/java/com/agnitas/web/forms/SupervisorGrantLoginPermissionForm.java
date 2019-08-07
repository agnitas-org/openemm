/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.web.forms;

import org.apache.struts.action.ActionForm;

public final class SupervisorGrantLoginPermissionForm extends ActionForm {
	private static final long serialVersionUID = -3334509598080102921L;
	
	private int departmentID;
	private String expireDateLocalized;
	private String limit;
	
	public final void clearData() {
		// Cannot do this in reset(). reset() is called automatically by Struts, which is not desired here.
		this.departmentID = 0;
		this.expireDateLocalized = null;
		this.limit = null;
	}

	public final int getDepartmentID() {
		return this.departmentID;
	}

	public final void setDepartmentID(final int id) {
		this.departmentID = id;
	}
	
	public final String getExpireDateLocalized() {
		return this.expireDateLocalized;
	}
	
	public final void setExpireDateLocalized(final String date) {
		this.expireDateLocalized = date;
	}
	
	public final void setLimit(final String value) {
		this.limit = value;
	}
	
	public final String getLimit() {
		return this.limit;
	}
}
