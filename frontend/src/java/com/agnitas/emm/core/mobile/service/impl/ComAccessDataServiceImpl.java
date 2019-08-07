/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.service.impl;

import java.util.Objects;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Required;

import com.agnitas.emm.core.commons.uid.ComExtensibleUID;
import com.agnitas.emm.core.mobile.bean.ComAccessData;
import com.agnitas.emm.core.mobile.bean.ComAccessData.AccessType;
import com.agnitas.emm.core.mobile.dao.ComAccessDataDao;
import com.agnitas.emm.core.mobile.service.ComAccessDataService;

public class ComAccessDataServiceImpl implements ComAccessDataService {
	ComAccessDataDao accessDataDao;

	@Required
	public final void setAccessDataDao(final ComAccessDataDao accessDataDao) {
		this.accessDataDao = Objects.requireNonNull(accessDataDao, "DAO cannot be null");
	}
	
	@Override
	public void writeAccessData(final ComAccessData data) {
		accessDataDao.writeData(data);		
	}

	@Override
	public void logAccess(final HttpServletRequest request, final ComExtensibleUID uid, final int deviceID) {
		final ComAccessData data = new ComAccessData();
		
		data.setIp(request.getRemoteAddr());		
		data.setReferer(request.getHeader("referer")); 	// can be null!
		data.setUserAgent(request.getHeader("User-Agent"));		
		data.setXuid(request.getParameter("uid"));	// can be null
		
		AccessType requestType;
		if (request.getRequestURL().indexOf("g.html") > 0) {
			requestType = AccessType.ONEPIXEL;
		} else if (request.getRequestURL().indexOf("r.html") > 0) {
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
}
