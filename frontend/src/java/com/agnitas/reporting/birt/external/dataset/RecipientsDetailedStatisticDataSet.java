/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.agnitas.dao.UserStatus;
import org.agnitas.emm.core.commons.util.ConfigValue;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.DbUtilities;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.reporting.birt.external.beans.LightMailingList;
import com.agnitas.reporting.birt.external.beans.LightTarget;

public class RecipientsDetailedStatisticDataSet extends RecipientsBasedDataSet {
    private static final transient Logger logger = LogManager.getLogger(RecipientsDetailedStatisticDataSet.class);

    private final List<RecipientsDetailedStatisticsRow> statList = new ArrayList<>();
    private final List<RecipientsDetailedStatisticsRow> dynamicStatList = new ArrayList<>();

    /**
     * Get Data for Recipient Report
     * message key "report.recipient.statistics.recipientDevelopmentDetailed.label"
     * en: "Recipient development detailed (Opt-ins, Opt-outs, Bounces)"
     * de: "Empfängerentwicklung detailliert (Anmeldungen, Abmeldungen, Bounces)"
     */
    public void initRecipientsStatistic(@VelocityCheck int companyId, String selectedMailingLists,
                                        String selectedTargetsAsString, String startDate, String stopDate, final String hiddenFilterTargetIdStr)
            throws Exception {

        List<Integer> mailingListIds = new ArrayList<>();
        for (String mailingListIdString : selectedMailingLists.split(",")) {
            mailingListIds.add(NumberUtils.toInt(mailingListIdString));
        }

        Date dateStart = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
        Date dateStop = new SimpleDateFormat("yyyy-MM-dd").parse(stopDate);

        int mailinglistIndex = 0;
        for (LightMailingList mailinglist : getMailingLists(mailingListIds, companyId)) {
            int mailinglistID = mailinglist.getMailingListId();
            mailinglistIndex++;

            int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
            insertStatistic(companyId, mailinglistID, mailinglistIndex, null, targetGroupIndex, dateStart,
                    dateStop, hiddenFilterTargetIdStr);

            for (LightTarget target : getTargets(selectedTargetsAsString, companyId)) {
                targetGroupIndex++;
                insertStatistic(companyId, mailinglistID, mailinglistIndex, target, targetGroupIndex, dateStart,
                        dateStop, hiddenFilterTargetIdStr);
            }
        }
    }

    /**
     * Get Data for Recipient Report
     * message key : report.recipient.statistics.recipientDevelopmentNet.label
     * en: "Net recipient development (progress of active recipients)"
     * de: "Empfängerentwicklung netto (Verlauf der aktiven Empfänger)"
     */
    public void initRecipientsDynamicStatistic(@VelocityCheck int companyId, String selectedMailingLists,
                                               String selectedTargetsAsString, String startDate, String stopDate, final String hiddenFilterTargetIdStr) throws Exception {
        List<Integer> mailingListIds = new ArrayList<>();
        for (String mailingListIdString : selectedMailingLists.split(",")) {
            mailingListIds.add(NumberUtils.toInt(mailingListIdString));
        }

        Date dateStart = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
        Date dateStop = new SimpleDateFormat("yyyy-MM-dd").parse(stopDate);

        int mailinglistIndex = 0;
        for (LightMailingList mailinglist : getMailingLists(mailingListIds, companyId)) {
            int mailinglistID = mailinglist.getMailingListId();
            mailinglistIndex++;

            int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
            insertDynamicStatistic(companyId, mailinglistID, mailinglistIndex, null, targetGroupIndex, dateStart,
                    dateStop, hiddenFilterTargetIdStr);

            for (LightTarget target : getTargets(selectedTargetsAsString, companyId)) {
                targetGroupIndex++;
                insertDynamicStatistic(companyId, mailinglistID, mailinglistIndex, target, targetGroupIndex, dateStart,
                        dateStop, hiddenFilterTargetIdStr);
            }
        }
    }

    private void insertDynamicStatistic(int companyId, int mailinglistID, int mailinglistIndex, LightTarget target,
                                        int targetGroupIndex, Date startDate, Date endDate, String hiddenFilterTargetIdStr) {
        String mailinglistName = getMailinglistName(companyId, mailinglistID);

        List<RecipientsDetailedStatisticsRow> data = getRecipientDetailedOverallStatAmountForEachDay(companyId, mailinglistID, mailinglistName,
                mailinglistIndex, target, targetGroupIndex, startDate, endDate, hiddenFilterTargetIdStr);

        dynamicStatList.addAll(data);
    }

    private void insertStatistic(int companyId, int mailinglistID, int mailinglistIndex, LightTarget target,
                                 int targetGroupIndex, Date dateStart, Date dateStop, String hiddenFilterTargetIdStr) {
        String mailinglistName = getMailinglistName(companyId, mailinglistID);
        statList.addAll(getRecipientDetailedStat(companyId, mailinglistID, mailinglistName, mailinglistIndex, target,
                targetGroupIndex, dateStart, dateStop, hiddenFilterTargetIdStr));
    }

    public List<RecipientsDetailedStatisticsRow> getStatistic() {
        return statList;
    }

    public List<RecipientsDetailedStatisticsRow> getDynamicStatistic() {
        return dynamicStatList;
    }

    private List<RecipientsDetailedStatisticsRow> getRecipientDetailedStat(@VelocityCheck int companyId, int mailinglistId,
                                                                           String mailinglistName, int mailinglistIndex, LightTarget target, int targetGroupIndex, Date startDate, Date endDate,
                                                                           String hiddenFilterTargetIdStr) {
        try {
            TreeMap<String, RecipientsDetailedStatisticsRow> dataMap = new TreeMap<>();
            target = getDefaultTarget(target);

            final String filterTargetSql = getHiddenTargetSql(companyId, target, hiddenFilterTargetIdStr);

            // Create a RecipientsDetailedStatisticsRow entry for each day within the given time period
            Date datePoint = startDate;
            while (datePoint.before(endDate) || datePoint.equals(endDate)) {
                RecipientsDetailedStatisticsRow datePointDetailedStatisticsRow =
                        new RecipientsDetailedStatisticsRow(datePoint,
                                mailinglistId, mailinglistName, mailinglistIndex,
                                target.getId(), target.getName(), targetGroupIndex);
                String datePointFormatted = new SimpleDateFormat("yyyy-MM-dd").format(datePoint);
                dataMap.put(datePointFormatted, datePointDetailedStatisticsRow);

                datePoint = DateUtilities.addDaysToDate(datePoint, 1);
            }

            String changeDateField = (isOracleDB() ? "TRUNC" : "DATE") + "(bind.timestamp)";

            String sql = "SELECT " + changeDateField + " AS changedate, user_status, COUNT(*) AS amount " +
                    " FROM " + getCustomerBindingTableName(companyId) + " bind" +
                    " LEFT JOIN customer_" + companyId + "_tbl cust ON bind.customer_id = cust.customer_id " +
                    " WHERE bind.mailinglist_id = ? " +
                    " AND " + changeDateField + " >= ? " +
                    " AND " + changeDateField + " <= ? ";

            if (StringUtils.isNotBlank(target.getTargetSQL())) {
                sql += " AND (" + target.getTargetSQL() + ")";
            }

            if (StringUtils.isNotBlank(filterTargetSql)) {
                sql += " AND (" + filterTargetSql + ")";
            }

            sql += " GROUP BY " + changeDateField + ", user_status";
            sql += " ORDER BY " + changeDateField + ", user_status";

            List<Map<String, Object>> result = select(logger, sql, mailinglistId, startDate, endDate);
            for (Map<String, Object> resultRow : result) {
                calculateAmount(resultRow, dataMap);
            }

            if (getConfigService().getBooleanValue(ConfigValue.UseBindingHistoryForRecipientStatistics, companyId)) {
                // Select additional data from history tables
                String hstSql = sql
                        .replace(getCustomerBindingTableName(companyId), getHstCustomerBindingTableName(companyId))
                        .replace("COUNT(*) AS amount", "COUNT(DISTINCT(bind.customer_id)) AS amount");

                List<Map<String, Object>> hstResult = select(logger, hstSql, mailinglistId, startDate, endDate);
                for (Map<String, Object> resultRow : hstResult) {
                    calculateAmount(resultRow, dataMap);
                }
            }

            // Sort and collect output data by date
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

    private List<RecipientsDetailedStatisticsRow> getRecipientDetailedOverallStatAmountForEachDay(@VelocityCheck int companyId, int mailinglistId,
                                                                                                  String mailinglistName, int mailinglistIndex, LightTarget target, int targetGroupIndex, Date startDate, Date endDate,
                                                                                                  String hiddenFilterTargetIdStr) {
        try {
            TreeMap<String, RecipientsDetailedStatisticsRow> dataMap = new TreeMap<>();
            target = getDefaultTarget(target);

            final String filterTargetSql = getHiddenTargetSql(companyId, target, hiddenFilterTargetIdStr);

            // Create a RecipientsDetailedStatisticsRow entry for each day within the given time period
            Date datePoint = startDate;
            while (datePoint.before(endDate) || datePoint.equals(endDate)) {
                RecipientsDetailedStatisticsRow datePointDetailedStatisticsRow =
                        new RecipientsDetailedStatisticsRow(datePoint,
                                mailinglistId, mailinglistName, mailinglistIndex,
                                target.getId(), target.getName(), targetGroupIndex);
                String datePointFormatted = new SimpleDateFormat("yyyy-MM-dd").format(datePoint);
                dataMap.put(datePointFormatted, datePointDetailedStatisticsRow);

                datePoint = DateUtilities.addDaysToDate(datePoint, 1);
            }

            String truncDateFn = isOracleDB() ? "TRUNC" : "DATE";
            String countClause = "COUNT(CASE WHEN bind.timestamp < dates.selected_date THEN 1 END)";
            String dateRangeClause = DbUtilities.makeSelectRangeOfDates("selected_date", DateUtils.addDays(startDate, 1), DateUtils.addDays(endDate, 1), isOracleDB());

            String sqlPattern = "SELECT %3$s(dates.selected_date - 1) AS changedate, bind.user_status, " +
                    " %1$s AS amount " +
                    " FROM %2$s bind LEFT JOIN customer_" + companyId + "_tbl cust ON bind.customer_id = cust.customer_id, " +
                    " (%4$s) dates" +
                    " WHERE bind.mailinglist_id = ? ";

            if (StringUtils.isNotBlank(target.getTargetSQL())) {
                sqlPattern += " AND (" + target.getTargetSQL() + ")";
            }

            if (StringUtils.isNotBlank(filterTargetSql)) {
                sqlPattern += " AND (" + filterTargetSql + ")";
            }

            sqlPattern += " GROUP BY dates.selected_date, user_status ORDER BY dates.selected_date, user_status";

            String sql = String.format(sqlPattern, countClause, getCustomerBindingTableName(companyId), truncDateFn, dateRangeClause);
            List<Map<String, Object>> result = select(logger, sql, mailinglistId);
            for (Map<String, Object> resultRow : result) {
                calculateAmount(resultRow, dataMap);
            }

            if (getConfigService().getBooleanValue(ConfigValue.UseBindingHistoryForRecipientStatistics, companyId)) {
                // Select additional data from history tables
                countClause = "COUNT(DISTINCT (CASE WHEN bind.timestamp < dates.selected_date AND bind.timestamp_change > dates.selected_date THEN bind.customer_id END))";
                sql = String.format(sqlPattern, countClause, getHstCustomerBindingTableName(companyId), truncDateFn, dateRangeClause);
                List<Map<String, Object>> hstResult = select(logger, sql, mailinglistId);
                for (Map<String, Object> resultRow : hstResult) {
                    calculateAmount(resultRow, dataMap);
                }
            }

            // Sort and collect output data by date
            List<RecipientsDetailedStatisticsRow> returnList = new ArrayList<>();
            SortedSet<String> keys = new TreeSet<>(dataMap.keySet());
            for (String key : keys) {
                returnList.add(dataMap.get(key));
            }

            return returnList;
        } catch (Exception e) {
            logger.error("Error in getRecipientDetailedOverallStatAmountForEachDay: " + e.getMessage(), e);
            throw e;
        }
    }

    private static void calculateAmount(final Map<String, Object> resultRow, final TreeMap<String, RecipientsDetailedStatisticsRow> dataMap) {
        final Date entryDate = (Date) resultRow.get("changedate");
        final RecipientsDetailedStatisticsRow row = dataMap.get(new SimpleDateFormat("yyyy-MM-dd").format(entryDate));
        final int userStatusCode = ((Number) resultRow.get("user_status")).intValue();
        final int amount = ((Number) resultRow.get("amount")).intValue();
        final UserStatus status = getUserStatus(userStatusCode);
        calculateAmount(status, amount, row);
    }

}
