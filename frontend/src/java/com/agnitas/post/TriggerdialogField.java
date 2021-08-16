/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.post;

public class TriggerdialogField {
	private int companyID;
	private int mailingID;
	private String fieldName;
	private VariableType fieldType;

	public TriggerdialogField(int companyID, int mailingID, String fieldName, VariableType fieldType) {
		this.companyID = companyID;
		this.mailingID = mailingID;
		this.fieldName = fieldName;
		this.fieldType = fieldType;
	}

	public TriggerdialogField() {
	}

	public int getCompanyID() {
		return companyID;
	}

	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	public int getMailingID() {
		return mailingID;
	}

	public void setMailingID(int mailingID) {
		this.mailingID = mailingID;
	}

	public String getFieldName() {
		return fieldName;
	}

	public void setFieldName(String fieldName) {
		this.fieldName = fieldName;
	}

	public VariableType getFieldType() {
		return fieldType;
	}

	public void setFieldType(VariableType fieldType) {
		this.fieldType = fieldType;
	}
}
