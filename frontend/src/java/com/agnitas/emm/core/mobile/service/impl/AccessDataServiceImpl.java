/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.service.impl;

import java.util.Objects;

import com.agnitas.emm.core.commons.uid.ExtensibleUID;
import com.agnitas.emm.core.mobile.bean.AccessData;
import com.agnitas.emm.core.mobile.bean.AccessData.AccessType;
import com.agnitas.emm.core.mobile.dao.AccessDataDao;
import com.agnitas.emm.core.mobile.service.AccessDataService;
import jakarta.servlet.http.HttpServletRequest;

public class AccessDataServiceImpl implements AccessDataService {

	private AccessDataDao accessDataDao;
	
	@Override
	public void logAccess(final HttpServletRequest request, final ExtensibleUID uid, final int deviceID) {
		final AccessData data = new AccessData();

		data.setIp(request.getRemoteAddr());
		data.setReferer(request.getHeader("referer")); 	// can be null!
		data.setUserAgent(request.getHeader("User-Agent"));
		data.setXuid(request.getParameter("uid"));	// can be null

		AccessType requestType;
		if (request.getRequestURL().indexOf("g.html") > 0) {
			requestType = AccessType.ONEPIXEL;
		} else if (request.getRequestURL().indexOf("r.html") > 0 || request.getRequestURL().indexOf("/r/") > 0) {
			requestType = AccessType.REDIRECT;
		} else {
			requestType = AccessType.UNKNOWN;
		}
		data.setAccessType(requestType);

		if (uid != null) {
			data.setMailingID(uid.getMailingID());
			data.setCustomerID(uid.getCustomerID());
			data.setLinkID(uid.getUrlID());
		}

		data.setDeviceID(deviceID);

		writeAccessData(data);
	}

	private void writeAccessData(AccessData data) {
		accessDataDao.writeData(data);
	}

	public void setAccessDataDao(AccessDataDao accessDataDao) {
		this.accessDataDao = Objects.requireNonNull(accessDataDao, "DAO cannot be null");
	}
}
