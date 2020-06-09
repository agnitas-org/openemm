/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.agnitas.dao.UserStatus;
import org.agnitas.dao.exception.UnknownUserStatusException;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

public abstract class RecipientsBasedDataSet extends BIRTDataSet {
    private static final transient Logger logger = Logger.getLogger(RecipientsBasedDataSet.class);
	
    protected static String getCustomerBindingTableName(int companyId) {
		return "customer_" + companyId + "_binding_tbl";
	}
	
	protected static String getHstCustomerBindingTableName(int companyID) {
		return "hst_customer_" + companyID + "_binding_tbl";
	}
	
	protected static String getCustomerTableName(int companyId) {
		return "customer_" + companyId + "_tbl";
	}
	
	protected static String getOnepixelDeviceTableName(int companyId) {
		return "onepixellog_device_" + companyId + "_tbl";
	}
	
    protected static UserStatus getUserStatus(int statusCode) {
		try {
			return UserStatus.getUserStatusByID(statusCode);
		} catch (UnknownUserStatusException e) {
			logger.error("User status code (" + statusCode + ") is invalid. Available statuses are [" +
					StringUtils.join(UserStatus.getAvailableStatusCodeList(), ", ") + "] "
			);
		}
		return null;
    }
	
	
    protected Map<Integer, String> mailinglistNamesById = new HashMap<>();
    
	@Override
	protected String getMailinglistName(int companyId, int mailinglistId) {
		return mailinglistNamesById.computeIfAbsent(mailinglistId, id -> super.getMailinglistName(companyId, mailinglistId));
	}
    
    protected static void calculateAmount(UserStatus status, int amount, RecipientsDetailedStatisticsRow row) {
    	if (status == null) {
    		return;
		}
		
    	switch(status) {
			case Active:
				row.countActive += amount;
				break;
			case Bounce:
				row.countBounced += amount;
				break;
			case AdminOut:
				row.countOptout += amount;
				break;
			case UserOut:
				row.countOptout += amount;
				break;
			case WaitForConfirm:
				row.countWaitingForConfirm += amount;
				break;
			case Blacklisted:
				row.countBlacklisted += amount;
				break;
			case Suspend:
				row.countOptout += amount;
				break;
			default:
				// do not count
		}
	}
	
	public static class RecipientsDetailedStatisticsRow {
        protected int mailingListId;
        protected int mailingListGroupId;
        protected int targetGroupId;
        protected int targetGroupGroupId;
        protected String mailingListName;
        protected String targetGroupName;
        protected int countActive;
        protected int countBlacklisted;
        protected int countOptout;
        protected int countBounced;
		protected int countWaitingForConfirm;
        protected Date date;
		
		public RecipientsDetailedStatisticsRow(Date date, int mailinglistId, String mailinglistName, int mailinglistIndex, int targetId, String targetGroupName, int targetGroupIndex) {
			this.date = date;
        	this.mailingListId = mailinglistId;
        	this.mailingListName = mailinglistName;
        	this.mailingListGroupId = mailinglistIndex;
			this.targetGroupId = targetId;
			this.targetGroupName = targetGroupName;
			this.targetGroupGroupId = targetGroupIndex;
		}
		
		public int getMailingListId() {
            return mailingListId;
        }

        public int getMailingListGroupId() {
            return mailingListGroupId;
        }

        public int getTargetGroupId() {
            return targetGroupId;
        }

        public int getTargetGroupGroupId() {
            return targetGroupGroupId;
        }

        public String getMailingListName() {
            return mailingListName;
        }

        public String getTargetGroupName() {
            return targetGroupName;
        }

        public int getCountActive() {
            return countActive;
        }

        public int getCountBlacklisted() {
            return countBlacklisted;
        }

        public int getCountOptout() {
            return countOptout;
        }

        public int getCountBounced() {
            return countBounced;
        }

		public int getCountWaitingForConfirm() {
			return countWaitingForConfirm;
		}

        public Date getDate() {
            return date;
        }
        
        @Override
		public String toString() {
			return (date == null ? "Unknown date " : date.toString()) +
					" active: " + countActive +
					" blacklisted: " + countBlacklisted +
					" optout: " + countOptout +
					" bounced: " + countBounced;
        }
    }
    
    public static class RecipientCollectedStatisticRowMapper implements RowMapper<RecipientCollectedStatisticRow> {
	
	@Override
	public RecipientCollectedStatisticRow mapRow(ResultSet resultSet, int rowNum) throws SQLException {
		RecipientCollectedStatisticRow row = new RecipientCollectedStatisticRow();
		row.setMailingListId(resultSet.getInt("mailinglist_id"));
		row.setMailingList(resultSet.getString("mailinglist_name"));
		row.setCategory(resultSet.getString("category_name"));
		row.setCategoryindex(resultSet.getInt("category_index"));
		row.setTargetgroup(resultSet.getString("targetgroup_name"));
		row.setTargetgroupindex(resultSet.getInt("targetgroup_index"));
		row.setTargetgroupId(resultSet.getInt("targetgroup_id"));
		row.setCount(resultSet.getInt("value"));
		row.setRate(resultSet.getDouble("rate"));
		return row;
	}
}
	
}
