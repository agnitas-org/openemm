/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.delivery.dao.impl;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.dao.impl.BaseDaoImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.AgnUtils;
import org.agnitas.util.DbUtilities;
import org.apache.log4j.Logger;

import com.agnitas.emm.core.delivery.beans.DeliveryInfo;
import com.agnitas.emm.core.delivery.dao.DeliveryDao;

public class DeliveryDaoImpl extends BaseDaoImpl implements DeliveryDao {
    private static final transient Logger LOGGER = Logger.getLogger(DeliveryDaoImpl.class);
    
    public static String getDeliveryTableName(int companyId) {
    	return "deliver_" + companyId + "_tbl";
    }

    @Override
    public List<DeliveryInfo> getDeliveriesInfo(@VelocityCheck final int companyId, final int mailingId, final int customerId) {
    	List<Map<String, Object>> result = select(LOGGER, "SELECT timestamp, line FROM " + getDeliveryTableName(companyId) +  " WHERE mailing_id = ? AND customer_id = ? AND (line LIKE '%dsn%' OR line LIKE '%relay%' OR line LIKE '%status%' OR line LIKE '%from%')", mailingId, customerId);
    	Map<String, DeliveryInfo> deliveryInfoByMessageID = new HashMap<>();

		final Pattern dsnPattern = Pattern.compile("dsn=(.*?)(:?, \\w+=|$)");
		final Pattern relayPattern = Pattern.compile("relay=(.*?)(:?, \\w+=|$)");
		final Pattern statusPattern = Pattern.compile("status=(.*?)(:?, \\w+=|$)");
		final Pattern fromPattern = Pattern.compile("from=(.*?)(:?, \\w+=|$)");
		
    	for (Map<String, Object> row : result) {
    		final String line = (String) row.get("line");
    		
        	// Jun  4 06:10:51
        	//String lineDate = line.substring(0, 15);
        	
        	// mailer11
        	int mailerHostShortEndIndex = line.indexOf(" ", 16);
        	//String mailerHostShort = line.substring(16, mailerHostShortEndIndex);
        	
        	// postfix/smtp[21175]:
        	int mtaDescriptorEndIndex = line.indexOf(" ", mailerHostShortEndIndex + 1);
        	//String mtaDescriptor = line.substring(mailerHostShortEndIndex + 1, mtaDescriptorEndIndex);
        	
        	// 14BD1C044D:
        	int messageIdEndIndex = line.indexOf(" ", mtaDescriptorEndIndex + 1);
        	String messageId = line.substring(mtaDescriptorEndIndex + 1, messageIdEndIndex - 1);
        	
        	// to=..., from=..., ...
        	String data = line.substring(messageIdEndIndex + 1);
        	
        	DeliveryInfo deliveryInfo = deliveryInfoByMessageID.get(messageId);
        	if (deliveryInfo == null) {
        		deliveryInfo = new DeliveryInfo();
        		deliveryInfo.setTimestamp((Date) row.get("timestamp"));
        		deliveryInfoByMessageID.put(messageId, deliveryInfo);
        	}
        	
    		final Matcher dsnMatcher = dsnPattern.matcher(data);
            if (dsnMatcher.find()) {
            	deliveryInfo.setDsn(dsnMatcher.group(1).trim());
            }

    		final Matcher relayMatcher = relayPattern.matcher(data);
            if (relayMatcher.find()) {
            	deliveryInfo.setRelay(relayMatcher.group(1).trim());
            }

    		final Matcher statusMatcher = statusPattern.matcher(data);
            if (statusMatcher.find()) {
            	deliveryInfo.setStatus(statusMatcher.group(1).trim());
            }

    		final Matcher fromMatcher = fromPattern.matcher(data);
            if (fromMatcher.find()) {
            	deliveryInfo.setMailerHost(AgnUtils.getDomainFromEmail(fromMatcher.group(1).trim()));
            }
    	}
    	List<DeliveryInfo> deliveryInfos = new ArrayList<>(deliveryInfoByMessageID.values());
		deliveryInfos.sort(Comparator.comparing(DeliveryInfo::getTimestamp));
		return deliveryInfos;
    }

    @Override
    public boolean checkIfDeliveryTableExists(final int companyId) {
        return DbUtilities.checkIfTableExists(getDataSource(), getDeliveryTableName(companyId));
    }
}
