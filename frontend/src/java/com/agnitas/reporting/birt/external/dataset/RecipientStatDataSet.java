/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.reporting.birt.external.beans.RecipientMaxValues;
import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.log4j.Logger;

import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.RecipientDetailRow;
import com.agnitas.reporting.birt.external.beans.RecipientMailtypeRow;
import com.agnitas.reporting.birt.external.beans.RecipientStatusRow;

public class RecipientStatDataSet extends BIRTDataSet {
	/** The logger. */
	private static final transient Logger logger = Logger.getLogger(RecipientStatDataSet.class);

	public List<RecipientStatusRow> getRecipientStatus(@VelocityCheck int companyID, String targetID, Integer mailinglistID, int mediaType, String language) {
		if (StringUtils.isBlank(language)) {
			language = "EN";
		}

		StringBuilder query = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		query.append("SELECT bind.user_status AS userstatus, COUNT(DISTINCT cust.customer_id) AS amount");
		query.append(" FROM customer_" + companyID + "_tbl cust");
		query.append(" LEFT JOIN customer_" + companyID + "_binding_tbl bind ON (cust.customer_id = bind.customer_id)");
		query.append(" WHERE (bind.user_status IN (1, 2, 3, 4, 5, 6, 7) OR bind.user_status IS NULL) AND cust.bounceload = 0");

		// bounceload check is NEEDED here, because we also select customers without binding table entries
		if (targetID != null) {
			LightTarget target = getTarget(Integer.parseInt(targetID), companyID);
			if (target != null && StringUtils.isNotBlank(target.getTargetSQL())) {
				query.append(" AND (" + target.getTargetSQL() + ")");
			}
		}

		if (mailinglistID != null){
			query.append(" AND bind.mailinglist_id = ?");
			parameters.add(mailinglistID);
		}

		if (mediaType == 0) {
			query.append(" AND (bind.mediatype = 0 OR bind.mediatype IS NULL)");
		} else {
			query.append(" AND bind.mediatype = ?");
			parameters.add(mediaType);
		}

		query.append(" GROUP BY bind.user_status");

		Map<Integer, Integer> resultMap = new HashMap<>();
		for (Map<String, Object> row : select(logger, query.toString(), parameters.toArray(new Object[0]))) {
			Integer statusCode = null;
			if (row.get("userstatus") != null) {
				statusCode = ((Number) row.get("userstatus")).intValue();
			}
			resultMap.put(statusCode, ((Number) row.get("amount")).intValue());
		}

		List<RecipientStatusRow> returnList = new ArrayList<>();

		// active
		int activeCount = resultMap.getOrDefault(UserStatus.Active.getStatusCode(), 0);
		addToResultList(returnList, activeCount, "recipient.Opt_Ins", language);

		// allout
		int outCount = resultMap.getOrDefault(UserStatus.AdminOut.getStatusCode(), 0);
		outCount += resultMap.getOrDefault(UserStatus.UserOut.getStatusCode(), 0);
		outCount += resultMap.getOrDefault(UserStatus.Suspend.getStatusCode(), 0);
		addToResultList(returnList, outCount, "statistic.Opt_Outs", language);

		// bounced
		int bouncedCount = resultMap.getOrDefault(UserStatus.Bounce.getStatusCode(), 0);
		addToResultList(returnList, bouncedCount, "recipient.MailingState2", language);

		// without confirmation
		int waitForConfirmCount = resultMap.getOrDefault(UserStatus.WaitForConfirm.getStatusCode(), 0);
		addToResultList(returnList, waitForConfirmCount, "Opt_In_NotConfirmed", language);

		// blacklisted
		int blacklisedCount = resultMap.getOrDefault(UserStatus.Blacklisted.getStatusCode(), 0);
		addToResultList(returnList, blacklisedCount, "recipient.MailingState6", language);

		// no mailing
		int noMailingsCount = resultMap.getOrDefault(null, 0);
		addToResultList(returnList, noMailingsCount, "noMailinglistAssigned", language);

		return returnList;
	}

	private void addToResultList(List<RecipientStatusRow> list, int count, String key, String language){
		if (count > 0) {
			String translation = I18nString.getLocaleString(key, language);
			list.add(new RecipientStatusRow(translation, count));
		}
	}

	public List<RecipientMailtypeRow> getRecipientMailtype(@VelocityCheck int companyID, String targetID, Integer mailinglistID) {
		StringBuilder query = new StringBuilder();
		List<Object> parameters = new ArrayList<>();

		query.append("SELECT cust.mailtype AS mailtype, COUNT(DISTINCT cust.customer_id) AS amount");
		query.append(" FROM customer_" + companyID + "_tbl cust, customer_" + companyID + "_binding_tbl bind");
		query.append(" WHERE cust.customer_id = bind.customer_id AND bind.user_status = 1 AND cust.mailtype in (0, 1, 2)");

		// bounceload check is not needed here, because we select on binding table where bounceload-customers has no entries

		if (targetID != null) {
			LightTarget target = getTarget(Integer.parseInt(targetID), companyID);
			if (target != null && StringUtils.isNotBlank(target.getTargetSQL())) {
				query.append(" AND (" + target.getTargetSQL() + ")");
			}
		}

		if (mailinglistID != null){
			query.append(" AND bind.mailinglist_id = ?");
			parameters.add(mailinglistID);
		}

		query.append(" GROUP BY cust.mailtype");
		query.append(" ORDER BY cust.mailtype DESC");

		int total = 0;
		List<RecipientMailtypeRow> returnList = new ArrayList<>();
		List<Map<String, Object>> result = select(logger, query.toString(), parameters.toArray(new Object[0]));
		for (Map<String, Object> row : result) {
			int amount = ((Number) row.get("amount")).intValue();
			total += amount;
		}

		for (Map<String, Object> row : result) {
			int mailtype = ((Number) row.get("mailtype")).intValue();
			int amount = ((Number) row.get("amount")).intValue();
			returnList.add(new RecipientMailtypeRow(mailtype, Math.round(100 * amount / total)));
		}

		return returnList;
	}

	/**
	 * Get recipient statistic entries on the user binding status
	 * 
	 * The day of stopDateString is included in statistics output
	 * 
	 * @param companyID
	 * @param targetID
	 * @param mailinglistID
	 * @param mediaType
	 * @param startDateString
	 * @param stopDateString
	 * @param hourScale
	 * @return
	 * @throws Exception
	 */
    public List<RecipientDetailRow> getRecipientDetails(@VelocityCheck int companyID, String targetID, Integer mailinglistID, Integer mediaType, String startDateString, String stopDateString, Boolean hourScale) throws Exception {
    	List<Object> parameters = new ArrayList<>();
    	String dateSelectPart;

		Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateString);
		Date endDate;
		parameters.add(startDate);
    	if (hourScale) {
    		endDate = DateUtils.addDays(startDate, 1);
    		parameters.add(endDate);
    	} else {
    		endDate = DateUtils.addDays(new SimpleDateFormat("yyyy-MM-dd").parse(stopDateString), 1);
    		parameters.add(endDate);
    	}

    	if (isOracleDB()) {
	    	if (hourScale) {
	    		dateSelectPart = "TO_CHAR(bind.timestamp, 'HH24')";
	    	} else {
	    		dateSelectPart = "TO_CHAR(bind.timestamp, 'dd.mm.yyyy')";
	    	}
    	} else {
	    	if (hourScale) {
	    		dateSelectPart = "DATE_FORMAT(bind.timestamp, '%H')";
	    	} else {
	    		dateSelectPart = "DATE_FORMAT(bind.timestamp, '%d.%m.%Y')";
	    	}
    	}

		RecipientMaxValues recipientMaxValues = new RecipientMaxValues();
		Map<String, RecipientDetailRow> recipientRows = new HashMap<>();

    	StringBuilder sql = new StringBuilder();
		sql.append("SELECT ").append(dateSelectPart).append(" AS time, bind.user_status AS userstatus, COUNT(DISTINCT bind.customer_id) AS amount");
		sql.append(" FROM customer_").append(companyID).append("_binding_tbl bind");
		if (targetID != null ){
			sql.append(" JOIN customer_").append(companyID).append("_tbl cust ON bind.customer_id = cust.customer_id");
		}
		sql.append(" WHERE bind.timestamp >= ? AND bind.timestamp < ?");

		// bounceload check is not needed here, because we select on binding table where bounceload-customers has no entries

		if (targetID != null ) {
			LightTarget target = getTarget(Integer.parseInt(targetID), companyID);
			sql.append(" AND (").append(target.getTargetSQL()).append(")");
		}

		if (mailinglistID != null){
			sql.append(" AND bind.mailinglist_id = ?");
    		parameters.add(mailinglistID);
		}

		if (mediaType != null) {
			if (mediaType == 0) {
				sql.append(" AND (bind.mediatype = 0 OR bind.mediatype IS NULL)");
			} else {
				sql.append(" AND bind.mediatype = ?");
				parameters.add(mediaType);
    		}
		}

		sql.append(" GROUP BY ").append(dateSelectPart).append(", bind.user_status");
		sql.append(" ORDER BY ").append(dateSelectPart).append(", bind.user_status");

		List<Map<String, Object>> result = select(logger, sql.toString(), parameters.toArray(new Object[0]));
		fillRecipientsRows(recipientRows, recipientMaxValues, result);
		
		if (getConfigService().getBooleanValue(ConfigValue.UseBindingHistoryForRecipientStatistics, companyID)) {
			// Select additional data from history tables
			List<Map<String, Object>> hstresult = select(logger, sql.toString().replace("customer_" + companyID + "_binding_tbl", "hst_customer_" + companyID + "_binding_tbl").replace("bind.timestamp,", "bind.timestamp_change,"), parameters.toArray(new Object[0]));
			fillRecipientsRows(recipientRows, recipientMaxValues, hstresult);
		}

		List<RecipientDetailRow> returnList = new ArrayList<>();
		
		// Add missing items and sort all items
		GregorianCalendar selectedDate = new GregorianCalendar();
		selectedDate.setTime(startDate);
		SimpleDateFormat timeFormat;
		if (hourScale) {
			timeFormat = new SimpleDateFormat("HH");
		} else {
			timeFormat = new SimpleDateFormat("dd.MM.yyyy");
		}
		while (selectedDate.getTimeInMillis() < endDate.getTime()) {
			String timeString = timeFormat.format(selectedDate.getTime());
			RecipientDetailRow selectedItem = recipientRows.get(timeString);
			if (selectedItem == null) {
				selectedItem = new RecipientDetailRow();
				selectedItem.setMydate(timeString);
			}
			selectedItem.setRecipientMaxData(recipientMaxValues);
			returnList.add(selectedItem);
			
			if (hourScale) {
				selectedDate.add(Calendar.HOUR_OF_DAY, 1);
			} else {
				selectedDate.add(Calendar.DAY_OF_MONTH, 1);
			}
		}

		return returnList;
    }
    
	private void fillRecipientsRows(Map<String, RecipientDetailRow> recipientRows, RecipientMaxValues recipientMaxValues, List<Map<String, Object>> result) throws Exception {
    	for (Map<String, Object> row : result) {
			String timeString = (String) row.get("time");
			int statusCode = ((Number) row.get("userstatus")).intValue();
			int amount = ((Number) row.get("amount")).intValue();
			RecipientDetailRow currentItem = recipientRows.get(timeString);
			if (currentItem == null) {
				currentItem = new RecipientDetailRow();
				currentItem.setMydate(timeString);
				recipientRows.put(timeString, currentItem);
			}

			switch (UserStatus.getUserStatusByID(statusCode)) {
				case Active:
					currentItem.setActive(currentItem.getActive() + amount);
					recipientMaxValues.setMaxActive(currentItem.getActive());
					break;

				case Bounce:
					currentItem.setBounced(currentItem.getBounced() + amount);
					recipientMaxValues.setMaxBounced(currentItem.getBounced());
					break;

				case WaitForConfirm:
					currentItem.setDoubleOptIn(currentItem.getDoubleOptIn() + amount);
					recipientMaxValues.setMaxDoubleOptIn(currentItem.getDoubleOptIn());
					break;

				case Blacklisted:
					currentItem.setBlacklisted(currentItem.getBlacklisted() + amount);
					recipientMaxValues.setMaxBlacklisted(currentItem.getBlacklisted());
					break;

				default:
					currentItem.setAllout(currentItem.getAllout() + amount);
					recipientMaxValues.setMaxAllOut(currentItem.getAllout());
			}
		}
	}
}
