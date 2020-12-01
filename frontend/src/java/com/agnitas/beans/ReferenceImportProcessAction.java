/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.beans;

public class ReferenceImportProcessAction {
	private int referenceImportactionID;
	private int companyID;
	private String name;
	private String type;
	private String action;
	
	public int getReferenceImportactionID() {
		return referenceImportactionID;
	}

	public int getCompanyID() {
		return companyID;
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getAction() {
		return action;
	}
	
	public ReferenceImportProcessAction(int companyID, int referenceImportactionID, String name, String type, String action) {
		this.companyID = companyID;
		this.referenceImportactionID = referenceImportactionID;
		this.name = name;
		this.type = type;
		this.action = action;
	}
}
