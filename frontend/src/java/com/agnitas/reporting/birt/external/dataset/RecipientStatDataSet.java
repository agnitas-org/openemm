/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.emm.core.service.RecipientStandardField;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.RecipientDetailRow;
import com.agnitas.reporting.birt.external.beans.RecipientDoiRow;
import com.agnitas.reporting.birt.external.beans.RecipientMailtypeRow;
import com.agnitas.reporting.birt.external.beans.RecipientMaxValues;
import com.agnitas.reporting.birt.external.beans.RecipientStatusRow;
import com.agnitas.emm.common.UserStatus;
import org.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.DateUtilities;
import com.agnitas.util.DbUtilities;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.jdbc.core.RowCallbackHandler;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CONFIRMED_AND_NOT_ACTIVE_DOI;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CONFIRMED_DOI;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.NOT_CONFIRMED_AND_DELETED_DOI;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.NOT_CONFIRMED_DOI;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.TOTAL_DOI;

public class RecipientStatDataSet extends RecipientsBasedDataSet {

	public List<RecipientStatusRow> getRecipientStatus(int companyID, String targetIdStr, Integer mailinglistID,
                                                       int mediaType, String language, final String hiddenFilterTargetIdStr) {
		if (StringUtils.isBlank(language)) {
			language = "EN";
		}

		final int targetId = NumberUtils.toInt(targetIdStr, -1);
		final LightTarget target = targetId < 0 ? null : getDefaultTarget(getTarget(targetId, companyID));
		final String filterTargetSql = getHiddenTargetSql(companyID, target, hiddenFilterTargetIdStr);

		StringBuilder query = new StringBuilder();
		List<Object> parameters = new ArrayList<>();
		query.append("SELECT bind.user_status AS userstatus, COUNT(DISTINCT cust.customer_id) AS amount")
				.append(" FROM ").append(getCustomerTableName(companyID)).append(" cust")
				.append(" LEFT JOIN ").append(getCustomerBindingTableName(companyID)).append(" bind ON (cust.customer_id = bind.customer_id)")
				.append(" WHERE (bind.user_status IN (0, 1, 2, 3, 4, 5, 6, 7) OR bind.user_status IS NULL) AND cust." + RecipientStandardField.Bounceload.getColumnName() + " = 0")
				.append(" AND NOT EXISTS (SELECT 1 FROM " + getCustomerBindingTableName(companyID) + " bind2 WHERE cust.customer_id = bind2.customer_id AND bind2.user_status < bind.user_status");
		if (mailinglistID != null){
			query.append(" AND bind2.mailinglist_id = ?");
			parameters.add(mailinglistID);
		}
		query.append(")");

		// bounceload check is NEEDED here, because we also select customers without binding table entries
		if (target != null && StringUtils.isNotBlank(target.getTargetSQL())) {
			query.append(" AND (").append(target.getTargetSQL()).append(")");
		}

		if(StringUtils.isNotBlank(filterTargetSql)) {
			query.append(" AND (").append(filterTargetSql).append(")");
		}

		if (mailinglistID != null){
			query.append(" AND bind.mailinglist_id = ?");
			parameters.add(mailinglistID);
		}

		// Ignore mediaType parameter by now. This may change in near future and is therefore not removed entirely.
//		if (mediaType == 0) {
//			query.append(" AND (bind.mediatype = 0 OR bind.mediatype IS NULL)");
//		} else {
//			query.append(" AND bind.mediatype = ?");
//			parameters.add(mediaType);
//		}

		query.append(" GROUP BY bind.user_status");

		Map<UserStatus, Integer> resultMap = new HashMap<>();
		RecipientStatusesMapCallback mapCallback = new RecipientStatusesMapCallback(resultMap);
		query(query.toString(), mapCallback, parameters.toArray(new Object[0]));

		List<RecipientStatusRow> returnList = new ArrayList<>();

		// active
		int activeCount = resultMap.getOrDefault(UserStatus.Active, 0);
		addToResultList(returnList, activeCount, "recipient.Opt_Ins", language);

		// allout
		int outCount = resultMap.getOrDefault(UserStatus.AdminOut, 0);
		outCount += resultMap.getOrDefault(UserStatus.UserOut, 0);
		outCount += resultMap.getOrDefault(UserStatus.Suspend, 0);
		addToResultList(returnList, outCount, "statistic.Opt_Outs", language);

		// bounced
		int bouncedCount = resultMap.getOrDefault(UserStatus.Bounce, 0);
		addToResultList(returnList, bouncedCount, "recipient.MailingState2", language);

		// without confirmation
		int waitForConfirmCount = resultMap.getOrDefault(UserStatus.WaitForConfirm, 0);
		addToResultList(returnList, waitForConfirmCount, "Opt_In_NotConfirmed", language);

		// blacklisted
		int blacklisedCount = resultMap.getOrDefault(UserStatus.Blacklisted, 0);
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

	public List<RecipientMailtypeRow> getRecipientMailtype(int companyID, String targetIdStr,
			Integer mailinglistID, String hiddenFilterTargetIdStr) {

		final StringBuilder query = new StringBuilder();
		final List<Object> parameters = new ArrayList<>();
		final int targetId = NumberUtils.toInt(targetIdStr, -1);
		final LightTarget target = targetId < 0 ? null : getDefaultTarget(getTarget(targetId, companyID));
		final String targetSql = target == null ? null : target.getTargetSQL();
		final String filterTargetSql = getHiddenTargetSql(companyID, target, hiddenFilterTargetIdStr);

		query.append("SELECT cust.mailtype AS mailtype, COUNT(DISTINCT cust.customer_id) AS amount ")
				.append(" FROM ")
				.append(getCustomerTableName(companyID)).append(" cust, ")
				.append(getCustomerBindingTableName(companyID)).append(" bind")
				.append(" WHERE cust.customer_id = bind.customer_id AND bind.user_status = 1 AND cust.mailtype in (0, 1, 2)");

		// bounceload check is not needed here, because we select on binding table where bounceload-customers has no entries

		if (StringUtils.isNotBlank(targetSql)) {
			query.append(" AND (").append(targetSql).append(")");
		}

		if (StringUtils.isNotBlank(filterTargetSql)) {
			query.append(" AND (").append(filterTargetSql).append(")");
		}

		if (mailinglistID != null){
			query.append(" AND bind.mailinglist_id = ?");
			parameters.add(mailinglistID);
		}

		query.append(" GROUP BY cust.mailtype");
		query.append(" ORDER BY cust.mailtype DESC");

		int total = 0;
		List<RecipientMailtypeRow> returnList = new ArrayList<>();
		List<Map<String, Object>> result = select(query.toString(), parameters.toArray(new Object[0]));
		for (Map<String, Object> row : result) {
			int amount = toInt(row.get("amount"));
			total += amount;
		}

		if (total > 0) {
			for (Map<String, Object> row : result) {
				int mailtype = toInt(row.get("mailtype"));
				int amount = toInt(row.get("amount"));
				returnList.add(new RecipientMailtypeRow(mailtype, Math.round(100 * amount / total)));
			}
		}
		return returnList;
	}

	public List<RecipientDoiRow> getRecipientsDoiData(int companyId, Integer mailinglistId, String startDateStr, String stopDateStr) throws ParseException {
		Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateStr);
		Date endDate = DateUtils.addDays(new SimpleDateFormat("yyyy-MM-dd").parse(stopDateStr), 1);

		int totalCount = getTotalDoiCount(companyId, mailinglistId, startDate, endDate);
		int notConfirmedCount = getNotConfirmedDoiCount(companyId, mailinglistId, startDate, endDate);
		int notConfirmedAndDeletedCount = getNotConfirmedAndDeletedDoiCount(companyId, mailinglistId, startDate, endDate);
		int confirmedCount = getConfirmedDoiCount(companyId, mailinglistId, startDate, endDate);
		int confirmedAndNotActiveCount = getConfirmedAndNotActiveDoiCount(companyId, mailinglistId, startDate, endDate);

		return List.of(
				new RecipientDoiRow(TOTAL_DOI, 100, totalCount, true),
				new RecipientDoiRow(NOT_CONFIRMED_DOI, calcPercent(notConfirmedCount, totalCount), notConfirmedCount),
				new RecipientDoiRow(NOT_CONFIRMED_AND_DELETED_DOI, calcPercent(notConfirmedAndDeletedCount, totalCount), notConfirmedAndDeletedCount),
				new RecipientDoiRow(CONFIRMED_DOI, calcPercent(confirmedCount, totalCount), confirmedCount),
				new RecipientDoiRow(CONFIRMED_AND_NOT_ACTIVE_DOI, calcPercent(confirmedAndNotActiveCount, totalCount), confirmedAndNotActiveCount)
		);
	}

	/**
	 *  Get recipient statistic entries on the user binding status
	 *
	 *  The day of stopDateString is included in statistics output
	 */
    public List<RecipientDetailRow> getRecipientDetails(int companyID, String targetID,
			Integer mailinglistID, Integer mediaType, String startDateString,
			String stopDateString, Boolean hourScale, String hiddenFilterTargetId) throws Exception {
    	List<Object> parameters = new ArrayList<>();
    	String dateSelectPart;

        String targetSqls = joinWhereClause(getTargetSqlString(targetID, companyID), getTargetSqlString(hiddenFilterTargetId, companyID));

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

    	StringBuilder sql = new StringBuilder("SELECT ");
		sql.append(dateSelectPart).append(" AS time, ")
				.append("bind.user_status AS userstatus, ")
				.append("COUNT(*) AS amount")
				.append(" FROM ").append(getCustomerBindingTableName(companyID)).append(" bind");

		if (!targetSqls.isEmpty()){
			sql.append(" JOIN ").append(getCustomerTableName(companyID)).append(" cust ON bind.customer_id = cust.customer_id");
		}

		sql.append(" WHERE bind.timestamp >= ? AND bind.timestamp < ? ");

		// bounceload check is not needed here, because we select on binding table where bounceload-customers has no entries

		if (!targetSqls.isEmpty()) {
			sql.append(" AND (").append(targetSqls).append(")");
		}

		if (mailinglistID != null) {
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

		if (DbUtilities.checkIfTableExists(getDataSource(), "ahv_" + companyID + "_tbl")) {
			// Exclude bounced recipients that are included in AHV process
			sql.append(" AND (bind.user_status != " + UserStatus.Bounce.getStatusCode() + " OR bind.customer_id NOT IN (SELECT customer_id FROM ahv_" + companyID + "_tbl WHERE bouncecount > 1))");
		}

		sql.append(" GROUP BY ").append(dateSelectPart).append(", bind.user_status");
		sql.append(" ORDER BY ").append(dateSelectPart).append(", bind.user_status");

		String query = sql.toString();


		RecipientMaxValues recipientMaxValues = new RecipientMaxValues();
		Map<String, RecipientDetailRow> recipientRows = new HashMap<>();
		RecipientDetailsMapCallback callback = new RecipientDetailsMapCallback(recipientRows, recipientMaxValues);
		query(query, callback, parameters.toArray(new Object[0]));

		if (getConfigService().getBooleanValue(ConfigValue.UseBindingHistoryForRecipientStatistics, companyID)) {
			// Select additional data from history tables
			String hstQuery = query.replace(getCustomerBindingTableName(companyID), getHstCustomerBindingTableName(companyID));

			query(hstQuery, callback, parameters.toArray(new Object[0]));
		}

		List<RecipientDetailRow> returnList = new ArrayList<>();

		// Add missing items and sort all items
		LocalDateTime selectedZonedDateTime = DateUtilities.getLocalDateTimeForDate(startDate);
		LocalDateTime endZonedDateTime = DateUtilities.getLocalDateTimeForDate(endDate);
		DateTimeFormatter dateTimeFormatter;
		if (hourScale) {
			dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("HH").toFormatter();
		} else {
			dateTimeFormatter = new DateTimeFormatterBuilder().appendPattern("dd.MM.yyyy").toFormatter();
		}
		while (selectedZonedDateTime.isBefore(endZonedDateTime)) {
			String timeString = selectedZonedDateTime.format(dateTimeFormatter);
			RecipientDetailRow selectedItem = recipientRows.get(timeString);
			if (selectedItem == null) {
				selectedItem = new RecipientDetailRow();
				selectedItem.setMydate(timeString);
			}
			selectedItem.setRecipientMaxData(recipientMaxValues);
			returnList.add(selectedItem);

			if (hourScale) {
				selectedZonedDateTime = selectedZonedDateTime.plusHours(1);
			} else {
				selectedZonedDateTime = selectedZonedDateTime.plusDays(1);
			}
		}

		return returnList;
    }

    private class RecipientStatusesMapCallback implements RowCallbackHandler {

		private Map<UserStatus, Integer> map;

		public RecipientStatusesMapCallback(Map<UserStatus, Integer> map) {
			this.map = Objects.requireNonNull(map);
		}

		@Override
		public void processRow(ResultSet rs) throws SQLException {
			int amount = rs.getInt("amount");
			UserStatus status = getUserStatus(rs.getInt("userstatus"));
			if (status == null) {
				map.put(null, map.getOrDefault(null, 0) + amount);
			} else {
				map.put(status, amount);
			}
		}
	}

	private class RecipientDetailsMapCallback implements RowCallbackHandler {
        private Map<String, RecipientDetailRow> dateMap;
        private RecipientMaxValues recipientMaxValues;

        public RecipientDetailsMapCallback(Map<String, RecipientDetailRow> dateMap, RecipientMaxValues recipientMaxValues) {
            this.dateMap = Objects.requireNonNull(dateMap);
            this.recipientMaxValues = Objects.requireNonNull(recipientMaxValues);
        }

        @Override
        public void processRow(ResultSet rs) throws SQLException {
            String timeString = rs.getString("time");
            RecipientDetailRow currentItem = dateMap.computeIfAbsent(timeString, RecipientDetailRow::new);

			UserStatus status = getUserStatus(rs.getInt("userstatus"));
			int amount = rs.getInt("amount");
			if (status == null) {
				currentItem.setAllout(currentItem.getAllout() + amount);
				recipientMaxValues.setMaxAllOut(currentItem.getAllout());
				return;
			}

            switch (status) {
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
