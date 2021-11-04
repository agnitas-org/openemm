/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.delivery.service.impl;

import static com.agnitas.emm.core.Permission.RECIPIENT_HISTORY_MAILING_DELIVERY;

import java.util.List;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.beans.ComAdmin;
import com.agnitas.emm.core.delivery.beans.DeliveryInfo;
import com.agnitas.emm.core.delivery.dao.DeliveryDao;
import com.agnitas.emm.core.delivery.service.DeliveryService;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;

public class DeliveryServiceImpl implements DeliveryService {

    private DeliveryDao deliveryDao;

    @Override
    public JSONArray getDeliveriesInfo(@VelocityCheck final int companyId, final int mailingId, final int customerId) {
        List<DeliveryInfo> deliveriesInfo = deliveryDao.getDeliveriesInfo(companyId, mailingId, customerId);
        if (CollectionUtils.isEmpty(deliveriesInfo)) {
            return new JSONArray();
        } else {
	        return mapToJson(deliveriesInfo);
        }
    }

    @Override
    public boolean isDeliveryHistoryEnabled(ComAdmin admin) {
        if (admin != null) {
            return admin.permissionAllowed(RECIPIENT_HISTORY_MAILING_DELIVERY)
                    && deliveryDao.checkIfDeliveryTableExists(admin.getCompanyID());
        }
        return false;
    }

    private JSONArray mapToJson(final List<DeliveryInfo> deliveriesInfo) {
        final JSONArray jsonArray = new JSONArray();
        for (DeliveryInfo deliveryInfo: deliveriesInfo) {
            jsonArray.element(mapToJson(deliveryInfo));
        }
        return jsonArray;
    }

    private JSONObject mapToJson(final DeliveryInfo deliveryInfo) {
		final JSONObject entry = new JSONObject();

		entry.element("timestamp", DateUtilities.toLong(deliveryInfo.getTimestamp()));
		entry.element("dsn", deliveryInfo.getDsn());
		entry.element("status", deliveryInfo.getStatus());
		entry.element("mailer", deliveryInfo.getMailerHost());

		final String relay = deliveryInfo.getRelay();
		if (StringUtils.startsWith(relay, "[")) {
			entry.element("relay", JSONUtils.quote(deliveryInfo.getRelay()));
		} else {
			entry.element("relay", deliveryInfo.getRelay());
		}

		return entry;
    }

    @Required
    public void setDeliveryDao(DeliveryDao deliveryDao) {
        this.deliveryDao = deliveryDao;
    }
}
