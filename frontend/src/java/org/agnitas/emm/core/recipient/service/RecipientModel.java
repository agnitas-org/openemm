/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.recipient.service;

import java.util.HashSet;
import java.util.Set;


import org.apache.commons.collections4.map.CaseInsensitiveMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

public class RecipientModel {
	public interface AddGroup {
    	// do nothing
    }
	
	public interface UpdateGroup {
    	// do nothing
    }
	
	public interface DeleteGroup {
    	// do nothing
    }

	private int companyId;
	private int customerId;
	private boolean doubleCheck;
	private String keyColumn;
	private boolean overwrite;
	private CaseInsensitiveMap<String, Object> parameters;			// TODO Change signature of this method and related types (return types, ...) to <String, String>
	private Set<String> columns;
	
	public int getCompanyId() {
		return companyId;
	}
	
	public void setCompanyId(int companyId) {
		this.companyId = companyId;
	}
	
	public int getCustomerId() {
		return customerId;
	}

	public void setCustomerId(int customerId) {
		this.customerId = customerId;
	}

	public boolean isDoubleCheck() {
		return doubleCheck;
	}
	
	public void setDoubleCheck(boolean doubleCheck) {
		this.doubleCheck = doubleCheck;
	}
	
	public String getKeyColumn() {
		return keyColumn;
	}
	
	public void setKeyColumn(String keyColumn) {
		this.keyColumn = keyColumn;
	}
	
	public boolean isOverwrite() {
		return overwrite;
	}
	
	public void setOverwrite(boolean overwrite) {
		this.overwrite = overwrite;
	}
	
	public CaseInsensitiveMap<String, Object> getParameters() {
		return parameters;
	}
	
	public void setParameters(CaseInsensitiveMap<String, Object> parameters) {
		this.parameters = parameters;
	}

	public String getEmail() {
		if (parameters == null) {
			return null;
		}

		return (String) parameters.get("email");
	}

	public void setEmail(String email) {
		parameters.put("email", email);
	}

	public Integer getMailtype() {
		if (parameters == null) {
			return null;
		}

		return toInteger((String) parameters.get("mailtype"));
	}

//	public void setMailtype(Number mailtype) {
//		parameters.put("mailtype", mailtype);
//	}

	public Integer getGender() {
		if (parameters == null) {
			return null;
		}

		return toInteger((String) parameters.get("gender"));
	}

//	public void setGender(Number gender) {
//		parameters.put("gender", gender);
//	}

	public void setColumns(Set<String> columns) {
		this.columns = columns;
	}

	public Set<String> getColumns() {
		if (columns == null) {
			columns = new HashSet<>();
		}
		return columns;
	}

	private Integer toInteger(String s) {
		return StringUtils.isBlank(s) ? null : NumberUtils.toInt(s, -1);
	}
}
