/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.mobile.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.AccessData;
import com.agnitas.emm.core.mobile.dao.AccessDataDao;
import com.agnitas.dao.impl.BaseDaoImpl;

public class AccessDataDaoImpl extends BaseDaoImpl implements AccessDataDao {
	
	public static final int MAXIMUM_USERAGENT_STORAGESIZE = 2200;
	public static final int MAXIMUM_REFERRER_STORAGESIZE = 2000;

	@Override
	@DaoUpdateReturnValueCheck
	public void writeData(AccessData accessData) {
		String sql = "INSERT INTO access_data_tbl (creation_date, user_agent, xuid, ip, referer, access_type, mailing_id, customer_id, link_id, device_id) VALUES (CURRENT_TIMESTAMP, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		
		// Cut UserAgent to maximum length
		String userAgentForStorage = accessData.getUserAgent();
		if (userAgentForStorage != null && userAgentForStorage.length() > MAXIMUM_USERAGENT_STORAGESIZE) {
			userAgentForStorage = userAgentForStorage.substring(0, MAXIMUM_USERAGENT_STORAGESIZE - 4) + " ...";
		}

		// Cut referrer to maximum length
		String refererForStorage = accessData.getReferer();
		if (refererForStorage != null && refererForStorage.length() > MAXIMUM_REFERRER_STORAGESIZE) {
			refererForStorage = refererForStorage.substring(0, MAXIMUM_REFERRER_STORAGESIZE - 4) + " ...";
		}
		
		try {
			update(sql, userAgentForStorage, accessData.getXuid(), accessData.getIp(), refererForStorage, accessData.getAccessType().toString(), accessData.getMailingID(), accessData.getCustomerID(), accessData.getLinkID(), accessData.getDeviceID());
		} catch (Exception e) {
			logger.error("Error inserting Data in access_data_tbl. CustomerID: " + accessData.getXuid(), e);
		}
	}
}
