/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import com.agnitas.reporting.birt.external.beans.LightMailingList;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public class RecipientsDetailedStatisticDataSet extends BIRTDataSet {
    private static final transient Logger logger = Logger.getLogger(RecipientsDetailedStatisticDataSet.class);

    private List<RecipientsDetailedStatisticsRow> statList = new ArrayList<>();
    private List<RecipientsDetailedStatisticsRow> dynamicStatList = new ArrayList<>();
    private Map<Integer, LightMailingList> selectedMailinglists = new HashMap<>();
    private List<LightTarget> selectedTargets = new ArrayList<>();

    /**
     * Get Data for Recipient Report
     * message key "report.recipient.statistics.recipientDevelopmentDetailed.label"
     * en: "Recipient development detailed (Opt-ins, Opt-outs, Bounces)"
     * de: "Empfängerentwicklung detailliert (Anmeldungen, Abmeldungen, Bounces)"
     */
    public void initRecipientsStatistic(@VelocityCheck int companyId, String selectedMailingLists, String selectedTargetsAsString, String startDate, String stopDate) throws Exception {
    	List<Integer> mailingListIds = new ArrayList<>();
		for (String mailingListIdString : Arrays.asList(selectedMailingLists.split(","))) {
			mailingListIds.add(Integer.parseInt(mailingListIdString));
		}
		
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStart = format.parse(startDate);
        Date dateStop = format.parse(stopDate);
        
        populateSelectedMailinglists(companyId, mailingListIds);
        populateSelectedTargets(companyId, selectedTargetsAsString);

        for (String mailinglistIDstring : selectedMailingLists.split(",")) {
        	int mailinglistID = Integer.parseInt(mailinglistIDstring);
            for (LightTarget target : addAllSubscribersToTargets(selectedTargets)) {
            	statList.addAll(getRecipientDetailedStat(companyId, mailinglistID, target, dateStart, dateStop));
            }
        }

        updateGroupIds(statList);
    }

    /**
     * Get Data for Recipient Report
     * message key : report.recipient.statistics.recipientDevelopmentNet.label
     * en: "Net recipient development (progress of active recipients)"
     * de: "Empfängerentwicklung netto (Verlauf der aktiven Empfänger)"
     */
    public void initRecipientsDynamicStatistic(@VelocityCheck int companyId, String selectedMailingLists, String selectedTargetsAsString, String startDate, String stopDate) throws Exception {
    	List<Integer> mailingListIds = new ArrayList<>();
		for (String mailingListIdString : Arrays.asList(selectedMailingLists.split(","))) {
			mailingListIds.add(Integer.parseInt(mailingListIdString));
		}
		
    	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        Date dateStart = format.parse(startDate);
        Date dateStop = format.parse(stopDate);
        
        populateSelectedMailinglists(companyId, mailingListIds);
        populateSelectedTargets(companyId, selectedTargetsAsString);

        for (String mailinglistIDstring : selectedMailingLists.split(",")) {
        	int mailinglistID = Integer.parseInt(mailinglistIDstring);
            for (LightTarget target : addAllSubscribersToTargets(selectedTargets)) {
            	RecipientsDetailedStatisticsRow currentAmounts = getRecipientDetailedStatAmountsBeforeDate(companyId, mailinglistID, target, dateStart);
            	List<RecipientsDetailedStatisticsRow> data = getRecipientDetailedStat(companyId, mailinglistID, target, dateStart, dateStop);
            	// Sum up data for absolute numbers per date
            	for (RecipientsDetailedStatisticsRow row : data) {
            		currentAmounts.countActive += row.countActive;
            		currentAmounts.countBounced += row.countBounced;
            		currentAmounts.countOptout += row.countOptout;
            		currentAmounts.countBlacklisted += row.countBlacklisted;
            		row.countActive = currentAmounts.countActive;
            		row.countBounced = currentAmounts.countBounced;
            		row.countOptout = currentAmounts.countOptout;
            		row.countBlacklisted = currentAmounts.countBlacklisted;
            	}
                
                dynamicStatList.addAll(data);
            }
        }

        updateGroupIds(dynamicStatList);
    }

    public List<RecipientsDetailedStatisticsRow> getStatistic() {
        return statList;
    }

    public List<RecipientsDetailedStatisticsRow> getDynamicStatistic() {
        return dynamicStatList;
    }

    private List<RecipientsDetailedStatisticsRow> getRecipientDetailedStat(@VelocityCheck int companyId, int mailinglistId, LightTarget target, Date dateStart, Date dateStop) throws Exception {
    	try {
    		SimpleDateFormat dataMapKeyFormat = new SimpleDateFormat("yyyy-MM-dd");
    		TreeMap<String, RecipientsDetailedStatisticsRow> dataMap = new TreeMap<>();
	        
	        // Create a RecipientsDetailedStatisticsRow entry for each day within the given time period
	        GregorianCalendar nextDate = new GregorianCalendar();
	        nextDate.setTime(dateStart);
	        while (nextDate.getTime().getTime() <= dateStop.getTime()) {
	        	RecipientsDetailedStatisticsRow nextRecipientsDetailedStatisticsRow = new RecipientsDetailedStatisticsRow();
	        	nextRecipientsDetailedStatisticsRow.date = nextDate.getTime();
	        	nextRecipientsDetailedStatisticsRow.mailingListId = mailinglistId;
	        	nextRecipientsDetailedStatisticsRow.targetGroupId = target.getId();
	        	nextRecipientsDetailedStatisticsRow.mailingListName = selectedMailinglists.get(mailinglistId).getShortname();
	        	nextRecipientsDetailedStatisticsRow.targetGroupName = target.getName();
	        	dataMap.put(dataMapKeyFormat.format(nextDate.getTime()), nextRecipientsDetailedStatisticsRow);
	        	nextDate.add(Calendar.DAY_OF_MONTH, 1);
	        }
	        
	        String dateTruncFunction = isOracleDB() ? "TRUNC" : "DATE";
	        
	        String sql = "SELECT " + dateTruncFunction + "(bind.timestamp) changedate, user_status, COUNT(*) amount FROM customer_" + companyId + "_binding_tbl bind";
	        if (StringUtils.isNotBlank(target.getTargetSQL())) {
	        	sql += ", customer_" + companyId + "_tbl cust";
	        }
	        sql += " WHERE bind.mailinglist_id = ?";
	        sql += " AND " + dateTruncFunction + "(bind.timestamp) >= ?";
	        sql += " AND " + dateTruncFunction + "(bind.timestamp) <= ?";
	        
	        if (StringUtils.isNotBlank(target.getTargetSQL())) {
	        	sql += " AND bind.customer_id = cust.customer_id AND (" + target.getTargetSQL() + ")";
	        }
	        
	        sql += " GROUP BY " + dateTruncFunction + "(bind.timestamp), user_status";
	        sql += " ORDER BY " + dateTruncFunction + "(bind.timestamp), user_status";
	        
	        List<Map<String, Object>> result = select(logger, sql, mailinglistId, dateStart, dateStop);
	        for (Map<String, Object> resultRow : result) {
	            Date entryDate = (Date) resultRow.get("changedate");
	            RecipientsDetailedStatisticsRow row = dataMap.get(dataMapKeyFormat.format(entryDate));
	            int userStatusCode = ((Number) resultRow.get("user_status")).intValue();
	            int amount = ((Number) resultRow.get("amount")).intValue();
	            switch(UserStatus.getUserStatusByID(userStatusCode)) {
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
	        
	        // Sort and collect ouput data by date 
	        List<RecipientsDetailedStatisticsRow> returnList = new ArrayList<>();
        	SortedSet<String> keys = new TreeSet<>(dataMap.keySet());
    		for (String key : keys) { 
    		   returnList.add(dataMap.get(key));
    		}
    		
    		return returnList;
    	} catch (Exception e) {
			logger.error("Error in getRecipientDetailedStat: " + e.getMessage(), e);
			throw e;
		}
    }

    private RecipientsDetailedStatisticsRow getRecipientDetailedStatAmountsBeforeDate(@VelocityCheck int companyId, int mailinglistId, LightTarget target, Date beforeDate) throws Exception {
    	try {
        	RecipientsDetailedStatisticsRow nextRecipientsDetailedStatisticsRow = new RecipientsDetailedStatisticsRow();
        	nextRecipientsDetailedStatisticsRow.date = beforeDate;
        	nextRecipientsDetailedStatisticsRow.mailingListId = mailinglistId;
        	nextRecipientsDetailedStatisticsRow.targetGroupId = target.getId();
        	nextRecipientsDetailedStatisticsRow.mailingListName = selectedMailinglists.get(mailinglistId).getShortname();
        	nextRecipientsDetailedStatisticsRow.targetGroupName = target.getName();
	        
	        String sql = "SELECT user_status, COUNT(*) amount FROM customer_" + companyId + "_binding_tbl bind";
	        if (StringUtils.isNotBlank(target.getTargetSQL())) {
	        	sql += ", customer_" + companyId + "_tbl cust";
	        }
	        sql += " WHERE bind.mailinglist_id = ?";
	        sql += " AND bind.timestamp < ?";
	        
	        if (StringUtils.isNotBlank(target.getTargetSQL())) {
	        	sql += " AND bind.customer_id = cust.customer_id AND (" + target.getTargetSQL() + ")";
	        }
	        
	        sql += " GROUP BY user_status";
	        
	        List<Map<String, Object>> result = select(logger, sql, mailinglistId, beforeDate);
	        for (Map<String, Object> resultRow : result) {
	            int userStatusCode = ((Number) resultRow.get("user_status")).intValue();
	            int amount = ((Number) resultRow.get("amount")).intValue();
	            switch(UserStatus.getUserStatusByID(userStatusCode)) {
	            	case Active:
	            		nextRecipientsDetailedStatisticsRow.countActive += amount;
	            		break;
	            	case Bounce:
	            		nextRecipientsDetailedStatisticsRow.countBounced += amount;
	            		break;
	            	case AdminOut:
	            		nextRecipientsDetailedStatisticsRow.countOptout += amount;
	            		break;
	            	case UserOut:
	            		nextRecipientsDetailedStatisticsRow.countOptout += amount;
	            		break;
	            	case WaitForConfirm:
	            		nextRecipientsDetailedStatisticsRow.countWaitingForConfirm += amount;
	            		break;
	            	case Blacklisted:
	            		nextRecipientsDetailedStatisticsRow.countBlacklisted += amount;
	            		break;
	            	case Suspend:
	            		nextRecipientsDetailedStatisticsRow.countOptout += amount;
	            		break;
	            	default:
	            		// do not count
	            }
	        }
    		
    		return nextRecipientsDetailedStatisticsRow;
    	} catch (Exception e) {
			logger.error("Error in getRecipientDetailedStatAmountsBeforeDate: " + e.getMessage(), e);
			throw e;
		}
    }

    private void updateGroupIds(List<RecipientsDetailedStatisticsRow> list) {
        int previousMailingListId = -1;
        int currentMailingListGroupId = 0;
        int previousTargetGroupId = -1;
        int currentTargetGroupGroupId = 0;
        for (RecipientsDetailedStatisticsRow row : list) {
            if (previousMailingListId != row.mailingListId && previousMailingListId != -1) {
                currentMailingListGroupId++;
            }
            previousMailingListId = row.mailingListId;
            row.mailingListGroupId = currentMailingListGroupId;

            if (previousTargetGroupId != row.targetGroupId && previousTargetGroupId != -1) {
                currentTargetGroupGroupId++;
            }
            previousTargetGroupId = row.targetGroupId;
            row.targetGroupGroupId = currentTargetGroupGroupId;
        }
    }

    private void populateSelectedMailinglists(@VelocityCheck int companyId, List<Integer> mailingListIds) {
        if (selectedMailinglists.size() == 0) {
            List<LightMailingList> mailingLists = getMailingLists(mailingListIds, companyId);
            for (LightMailingList mailingList : mailingLists) {
                selectedMailinglists.put(mailingList.getMailingListId(), mailingList);
            }
        }
    }

    private void populateSelectedTargets(@VelocityCheck int companyId, String selectedMailingListsAsString) {
        if (selectedTargets.size() == 0 && !"".equals(selectedMailingListsAsString)) {
            selectedTargets = getTargets(selectedMailingListsAsString, companyId);
        }
    }

    private List<LightTarget> addAllSubscribersToTargets(List<LightTarget> targets) {
        LightTarget allSubscribers = new LightTarget();
        allSubscribers.setId(0);
        allSubscribers.setName("All_subscribers");
        allSubscribers.setTargetSQL("");

        List<LightTarget> lightTargets = new ArrayList<>();
        lightTargets.add(allSubscribers);
        lightTargets.addAll(targets);
        return lightTargets;
    }

    public static class RecipientsDetailedStatisticsRow {
        protected Integer mailingListId;
        protected Integer mailingListGroupId;
        protected Integer targetGroupId;
        protected Integer targetGroupGroupId;
        protected String mailingListName;
        protected String targetGroupName;
        protected Integer countActive = 0;
        protected Integer countBlacklisted = 0;
        protected Integer countOptout = 0;
        protected Integer countBounced = 0;
		protected Integer countWaitingForConfirm = 0;
        protected Date date;

        public Integer getMailingListId() {
            return mailingListId;
        }

        public Integer getMailingListGroupId() {
            return mailingListGroupId;
        }

        public Integer getTargetGroupId() {
            return targetGroupId;
        }

        public Integer getTargetGroupGroupId() {
            return targetGroupGroupId;
        }

        public String getMailingListName() {
            return mailingListName;
        }

        public String getTargetGroupName() {
            return targetGroupName;
        }

        public Integer getCountActive() {
            return countActive;
        }

        public Integer getCountBlacklisted() {
            return countBlacklisted;
        }

        public Integer getCountOptout() {
            return countOptout;
        }

        public Integer getCountBounced() {
            return countBounced;
        }

		public Integer getCountWaitingForConfirm() {
			return countWaitingForConfirm;
		}

        public Date getDate() {
            return date;
        }
        
        @Override
		public String toString() {
        	try {
				return date.toString() + " active: " + countActive + " blacklisted: " + countBlacklisted + " optout: " + countOptout + " bounced: " + countBounced;
			} catch (Exception e) {
				e.printStackTrace();
				return null;
			}
        }
    }
}
