/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.userform.form;

import com.agnitas.emm.core.userform.dto.ResultSettings;

public class UserFormForm {
	private int formId;
	private String formName;
	private String description;
	private boolean active; // TODO: EMMGUI-714 remove after remove of old design
	private ResultSettings successSettings = new ResultSettings(true);
	private ResultSettings errorSettings = new ResultSettings(false);
	
	public void setFormId(int formId) {
		this.formId = formId;
	}
	
	public int getFormId() {
		return formId;
	}
	
	public void setFormName(String formName) {
		this.formName = formName;
	}
	
	public String getFormName() {
		return formName;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setActive(boolean active) {
		this.active = active;
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void setSuccessSettings(ResultSettings successSettings) {
		this.successSettings = successSettings;
	}
	
	public ResultSettings getSuccessSettings() {
		return successSettings;
	}
	
	public void setErrorSettings(ResultSettings errorSettings) {
		this.errorSettings = errorSettings;
	}
	
	public ResultSettings getErrorSettings() {
		return errorSettings;
	}
}
