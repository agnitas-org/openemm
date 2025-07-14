/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.service;

import jakarta.servlet.http.HttpServletRequest;

import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.mobile.bean.AccessData;

public interface AccessDataService {

	void writeAccessData(final AccessData data);
	
	void logAccess(final HttpServletRequest request, final ExtensibleUID uid, final int deviceID);
}
