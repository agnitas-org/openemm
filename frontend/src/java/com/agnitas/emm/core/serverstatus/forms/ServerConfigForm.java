/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.serverstatus.forms;

import org.apache.commons.lang3.math.NumberUtils;


public class ServerConfigForm {
    
    private int companyId;
    private String name;
    private String value;
	private String description;
    
    public int getCompanyId() {
        return companyId;
    }
    
	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}
	
	public void setCompanyIdString(String companyId) {
		this.companyId = NumberUtils.toInt(companyId);
	}
	
	public String getCompanyIdString() {
		return companyId > 0 ? String.valueOf(companyId) : "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
}
