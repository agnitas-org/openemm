/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package org.agnitas.dao.impl;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.dao.OnepixelDao;
import org.agnitas.emm.core.commons.util.ConfigService;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import java.util.List;
import java.util.Map;

public class OnepixelDaoImpl extends BaseDaoImpl implements OnepixelDao {
	
	private static final Logger logger = LogManager.getLogger(OnepixelDaoImpl.class);
	
	private static final String FIELD_OPEN_COUNT = "open_count";
	private static final String FIELD_MOBILE_COUNT = "mobile_count";

	public static String getOnepixellogDeviceTableName(int companyId) {
		return new StringBuilder("onepixellog_device_").append(companyId).append("_tbl").toString();
	}
	
	public static String getOnepixellogTableName(int companyId) {
		return new StringBuilder("onepixellog_").append(companyId).append("_tbl").toString();
	}
	
	private static String getSqlInsertString(int companyId) {
		return "INSERT INTO " + getOnepixellogTableName(companyId) + " (company_id, mailing_id, customer_id, ip_adr, open_count, mobile_count, timestamp, first_open, last_open) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)";
	}
	
	private static String getSqlUpdateString(int companyId) {
		return "UPDATE " + getOnepixellogTableName(companyId) + " SET open_count = open_count + 1, mobile_count = COALESCE(mobile_count, 0) + ?, last_open = CURRENT_TIMESTAMP WHERE mailing_id = ? AND customer_id = ?";
	}
	
	private static String getSqlDeviceInsertString(int companyId) {
		return "INSERT INTO " + getOnepixellogDeviceTableName(companyId) + " (company_id, mailing_id, customer_id, device_class_id, device_id, client_id, creation) VALUES (?, ?, ?, ?, ?, ?, CURRENT_TIMESTAMP)";
	}

    private ConfigService configService;
	
	@Required
	public void setConfigService(ConfigService configService) {
		this.configService = configService;
	}

	private void updateCustomerForOpen(int companyID, int customerID) {
		if(customerID != 0) {
			try {
				String updateLastOpenStatement = "UPDATE customer_" + companyID + "_tbl SET lastopen_date = CURRENT_TIMESTAMP WHERE customer_id = ?";
				update(logger, updateLastOpenStatement, customerID);
			} catch (Exception e) {
				if(logger.isInfoEnabled()) {
					logger.info(String.format("Cannot update last open date for customer %d of company %d", customerID, companyID), e);
				}
			}
		}
	}
	
	@Override
	@DaoUpdateReturnValueCheck
    public boolean writePixel(int companyID, int recipientID, int mailingID, String remoteAddr, DeviceClass deviceClass, int deviceID, int clientID) {
        try {
        	int mobileCountDelta = 0;
        	if (deviceClass == DeviceClass.MOBILE) {
        		mobileCountDelta = 1;
        	}
        	
        	if (recipientID == 0) {
        		// Fallback for anonymous recipients
        		remoteAddr = null;
        	}
        	
    		int touchedLines = update(logger, getSqlUpdateString(companyID), mobileCountDelta, mailingID, recipientID);
    		
        	if (touchedLines == 0) {
				// Insert new entry
        		update(logger, getSqlInsertString(companyID), companyID, mailingID, recipientID, remoteAddr, 1, mobileCountDelta);
			} else if (touchedLines > 1) {
				// If more than 1 row was incremented, then we have to subtract the number of wrongly incremented rows
				final int correctionValue = touchedLines - 1;
				
				logger.error("Invalid data: invalid number of entries found for companyid:" + companyID + ", mailingid:" + mailingID + ", custid:" + recipientID + " , entries:" + touchedLines + " (cleaning up entries exept for the first one)");
				// If more than one entry exists, there was an error on the first insert caused by parallel inserts, which will be repaired now
				// We delete all entries and insert a new single one with the sum of counting of the deleted ones
				// This is a bit of optimistic, because another process could have found this problem too, and tries to resolve it. But this will not happen too many times
				List<Map<String, Object>> result = select(logger, "SELECT " + FIELD_OPEN_COUNT + ", " + FIELD_MOBILE_COUNT + " FROM " + getOnepixellogTableName(companyID) + " WHERE mailing_id = ? AND customer_id = ?", mailingID, recipientID);
				if (result.size() > 1) {
					// Sum up existing entries (not using sum(...) for checking there is still more than 1 entry)
					int countSum = 0;
					int mobileSum = 0;
					for (Map<String, Object> row : result) {
						countSum += ((Number) row.get(FIELD_OPEN_COUNT)).intValue();
						mobileSum += ((Number) row.get(FIELD_MOBILE_COUNT)).intValue();
					}
					// Delete existing entries
					update(logger, "DELETE FROM " + getOnepixellogTableName(companyID) + " WHERE mailing_id = ? AND customer_id = ?", mailingID, recipientID);
					// Insert summed up entry
					update(logger, getSqlInsertString(companyID), companyID, mailingID, recipientID, remoteAddr, countSum - correctionValue, mobileSum - correctionValue);
				}
			}
        	
        	// write additional device entry
			update(logger, getSqlDeviceInsertString(companyID), companyID, mailingID, recipientID, deviceClass.getId(), deviceID, clientID);
			
			// Update customer entry
			if (configService.getBooleanValue(ConfigValue.WriteCustomerOpenOrClickField, companyID)) {
				updateCustomerForOpen(companyID, recipientID);
			}
			
			return true;
        } catch (Exception e) {
            logger.error("Error writing OnePixelLog writePixel", e);
            return false;
        }
    }
	
	@Override
	@DaoUpdateReturnValueCheck
    public void deleteAdminAndTestOpenings(int mailingID, int companyID) {
		// remove from onepixellog_X_tbl
		String sqlOpen = "DELETE FROM onepixellog_" + companyID	+ "_tbl"
			+ " WHERE mailing_id = ?"
				+ " AND customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl"
					+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
		update(logger, sqlOpen, mailingID, mailingID);

        // remove from onepixellog_device_X_tbl
		String sqlOpenDevice = "DELETE FROM onepixellog_device_" + companyID + "_tbl"
			+ " WHERE mailing_id = ?"
				+ " AND customer_id IN (SELECT customer_id FROM customer_" + companyID + "_binding_tbl"
					+ " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id = (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id = ?))";
		update(logger, sqlOpenDevice, mailingID, mailingID);
    }
}
