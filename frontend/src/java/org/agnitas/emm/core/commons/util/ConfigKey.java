/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.emm.core.commons.util;

public class ConfigKey {
	private String name;
	private int companyID;
	private String hostName = null;
	
	public ConfigKey(String name, int companyID, String hostName) {
		this.name = name;
		this.companyID = companyID;
		if (hostName != null) {
			this.hostName = hostName.toLowerCase();
		}
	}

	public String getName() {
		return name;
	}

	public int getCompanyID() {
		return companyID;
	}

	public String getHostName() {
		return hostName;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + companyID;
		result = prime * result + ((hostName == null) ? 0 : hostName.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		ConfigKey other = (ConfigKey) obj;
		if (companyID != other.companyID) {
			return false;
		}
		if (hostName == null) {
			if (other.hostName != null) {
				return false;
			}
		} else if (!hostName.equals(other.hostName)) {
			return false;
		}
		if (name == null) {
			if (other.name != null) {
				return false;
			}
		} else if (!name.equals(other.name)) {
			return false;
		}
		return true;
	}
}
