/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.delivery.service.impl;

import com.agnitas.beans.Admin;
import com.agnitas.emm.core.delivery.beans.DeliveryInfo;
import com.agnitas.emm.core.delivery.beans.SuccessfulDeliveryInfo;
import com.agnitas.emm.core.delivery.dao.DeliveryDao;
import com.agnitas.emm.core.delivery.service.DeliveryService;
import com.agnitas.emm.core.mailing.service.ComMailingBaseService;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.util.JSONUtils;
import org.agnitas.util.DateUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;

import static com.agnitas.emm.core.Permission.RECIPIENT_HISTORY_MAILING_DELIVERY;

public class DeliveryServiceImpl implements DeliveryService {

    private DeliveryDao deliveryDao;
    private ComMailingBaseService mailingBaseService;

    @Override
    public JSONArray getDeliveriesInfo(final int companyId, final int mailingId, final int customerId) {
        List<DeliveryInfo> deliveriesInfo = deliveryDao.getDeliveriesInfo(companyId, mailingId, customerId);
        if (CollectionUtils.isEmpty(deliveriesInfo)) {
            return new JSONArray();
        } else {
	        return mapToJson(deliveriesInfo);
        }
    }

    @Override
    public JSONArray getSuccessfulDeliveriesInfo(int companyId, int mailingId, int recipientId) {
        List<SuccessfulDeliveryInfo> deliveries = deliveryDao.getSuccessfulDeliveriesInfo(companyId, mailingId, recipientId);

        if (CollectionUtils.isEmpty(deliveries)) {
            return new JSONArray();
        }

        return successfulDeliveriesToJson(deliveries, companyId);
    }

    @Override
    public boolean isDeliveryHistoryEnabled(Admin admin) {
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

    private JSONArray successfulDeliveriesToJson(List<SuccessfulDeliveryInfo> deliveriesInfo, int companyId) {
        JSONArray jsonArray = new JSONArray();

        for (SuccessfulDeliveryInfo deliveryInfo: deliveriesInfo) {
            jsonArray.element(successfulDeliveryToJson(deliveryInfo, companyId));
        }

        return jsonArray;
    }

    private JSONObject successfulDeliveryToJson(SuccessfulDeliveryInfo deliveryInfo, int companyId) {
        JSONObject entry = new JSONObject();

        entry.element("timestamp", DateUtilities.toLong(deliveryInfo.getTimestamp()));
        entry.element("mailing", mailingBaseService.getMailingName(deliveryInfo.getMailingId(), companyId));

        return entry;
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

    @Required
    public void setMailingBaseService(ComMailingBaseService mailingBaseService) {
        this.mailingBaseService = mailingBaseService;
    }
}
