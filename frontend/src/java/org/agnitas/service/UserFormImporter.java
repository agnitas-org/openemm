/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.service;

import java.io.InputStream;
import java.util.Set;

public interface UserFormImporter {
	UserFormImportResult importUserFormFromJson(int companyID, InputStream input) throws Exception;

	UserFormImportResult importUserFormFromJson(int companyID, InputStream input, String formName, String description) throws Exception;
	
	class UserFormImportResult {
		private int userFormID;
		private Set<String> warningKeys;

		public UserFormImportResult(int userFormID, Set<String> warningKeys) {
			this.userFormID = userFormID;
			this.warningKeys = warningKeys;
		}

		public int getUserFormID() {
			return userFormID;
		}

		public Set<String> getWarningKeys() {
			return warningKeys;
		}
	}
}
