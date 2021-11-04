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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.report.enums.fields.MailingTypes;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.reporting.birt.external.beans.LightMailing;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.SendStatRow;
import com.agnitas.reporting.birt.external.dao.impl.LightMailingDaoImpl;
import com.agnitas.reporting.birt.external.utils.BirtReporUtils;

/**
 * Methods in this class must be kept "public", so they can be called from Birt via "rptdesign"-files, which use the class "MailingStatisticDataSet".
 * Otherwise there will be some InvocationTargetException because of different used Classlodaers, etc.
 */
public class MailingSummaryDataSet extends ComparisonBirtDataSet {
    private static final transient Logger logger = Logger.getLogger(MailingSummaryDataSet.class);

    private class TempRow {
        private String category;
        private int categoryIndex;
        private int targetGroupId;
        private String targetGroup;
        private int targetGroupIndex;
        private int value;

        public String getCategory() {
            return category;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public int getCategoryIndex() {
            return categoryIndex;
        }

        public void setCategoryIndex(int categoryIndex) {
            this.categoryIndex = categoryIndex;
        }

        public int getTargetGroupId() {
            return targetGroupId;
        }

        public void setTargetGroupId(int targetGroupId) {
            this.targetGroupId = targetGroupId;
        }

        public String getTargetGroup() {
            return targetGroup;
        }

        public void setTargetGroup(String targetGroup) {
            this.targetGroup = targetGroup;
        }

        public int getTargetGroupIndex() {
            return targetGroupIndex;
        }

        public void setTargetGroupIndex(int targetGroupIndex) {
            this.targetGroupIndex = targetGroupIndex;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }
    }

    /**
     * Create a temporary table to collect the values from different queries for the report in one table.
     *
     * @throws Exception
     */
    public int createTempTable() throws Exception {
        int tempTableID = getNextTmpID();
        StringBuilder createTable = new StringBuilder("CREATE TABLE ");
        createTable.append(getTempTableName(tempTableID))
                .append(" (")
                .append("category VARCHAR(200)")
                .append(", category_index INTEGER")
                .append(", targetgroup_id INTEGER")
                .append(", targetgroup VARCHAR(200)")
                .append(", targetgroup_index INTEGER")
                .append(", value INTEGER")
                .append(", rate DOUBLE")
                .append(", rate_delivered DOUBLE")
                .append(")");
        updateEmbedded(logger, createTable.toString());
        return tempTableID;
    }

    /**
     * This method has to be called in initialize function of the report, otherwise getSummaryData will fail !
     *
     * @param mailingID
     * @param companyID
     * @param selectedTargetsAsString
     * @return
     * @throws Exception
     */
    public int prepareReport(int mailingID, @VelocityCheck int companyID, String selectedTargetsAsString, String recipientsType, Boolean showSoftbounces, String startDate, String stopDate, Boolean hourScale) throws Exception {
        int tempTableID = createTempTable();
        List<LightTarget> targets = Optional.ofNullable(getTargets(selectedTargetsAsString, companyID)).orElse(new ArrayList<>());
        DateFormats dateFormats = new DateFormats(startDate, stopDate, hourScale);
        insertSendIntoTempTable(mailingID, tempTableID, companyID, targets, null, recipientsType, dateFormats);
        insertClickersIntoTempTable(mailingID, tempTableID, companyID, targets, null, recipientsType, true, dateFormats);
        insertClicksAnonymousIntoTempTable(mailingID, tempTableID, companyID, dateFormats);
        insertOpenersIntoTempTable(mailingID, tempTableID, companyID, targets, null, recipientsType, true, false, dateFormats);
        insertOpenedInvisibleIntoTempTable(mailingID, tempTableID, companyID, targets, null, recipientsType, dateFormats);
        insertOpenedGrossIntoTempTable(mailingID, tempTableID, companyID, targets, dateFormats);
        insertOpenedAnonymousIntoTempTable(mailingID, tempTableID, companyID, dateFormats);
        insertBouncesIntoTempTable(mailingID, tempTableID, companyID, targets, null, recipientsType, false, dateFormats);
        insertSoftbouncesUndeliverable(mailingID, tempTableID, companyID, targets, recipientsType, showSoftbounces);
        insertOptOutsIntoTempTable(mailingID, tempTableID, companyID, targets, null, recipientsType, dateFormats);
        insertRevenueIntoTempTable(mailingID, tempTableID, companyID, targets, null, dateFormats);
        insertDeliveredIntoTempTable(tempTableID, mailingID, companyID, targets, null, recipientsType, dateFormats);
        updateRates(tempTableID, companyID, targets);
        return tempTableID;
    }

    @DaoUpdateReturnValueCheck
    public int prepareReportOptimization(int optimizationID, @VelocityCheck int companyID, String selectedTargetsAsString, String recipientsType, Boolean showSoftbounces, String startDate, String stopDate, Boolean hourScale) throws Exception {
        int cumulateTableId = createTempTable();
        List<Integer> mailings = getAutoOptimizationMailings(optimizationID, companyID);

        if ((mailings != null) && (mailings.size() > 0)) {
            List<Integer> mailingTempTablesId = new ArrayList<>();
            List<List<MailingSummaryRow>> mailingTempTablesContent = new ArrayList<>();
            for (Integer mailingID : mailings) {
                if (mailingID != 0) {
                    int tempTableID = prepareReport(mailingID, companyID, selectedTargetsAsString, recipientsType, showSoftbounces, startDate, stopDate, hourScale);
                    mailingTempTablesId.add(tempTableID);
                    mailingTempTablesContent.add(getResultsFromTempTable(tempTableID));
                }
            }
            int countTables = mailingTempTablesId.size();
            int countRows = 0;
            if (countTables > 0) {
                countRows = mailingTempTablesContent.get(0).size();
                for (int i = 0; i < countRows; i++) {
                    String category = mailingTempTablesContent.get(0).get(i).getCategory();
                    int categoryIndex = mailingTempTablesContent.get(0).get(i).getCategoryindex();
                    int value = 0;
                    for (int j = 0; j < countTables; j++) {
                        value += mailingTempTablesContent.get(j).get(i).getCount();
                    }
                    insertIntoTempTable(cumulateTableId, category, categoryIndex, CommonKeys.ALL_SUBSCRIBERS, CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID, CommonKeys.ALL_SUBSCRIBERS_INDEX, value);
                }
                for (Integer tempID : mailingTempTablesId) {
                    dropTempTable(tempID);
                }
                List<LightTarget> targets = getTargets(selectedTargetsAsString, companyID);
                updateRates(cumulateTableId, companyID, targets);
            }
        }

        return cumulateTableId;
    }

    public void insertDeliveredIntoTempTable(int tempTableID, int mailingID, @VelocityCheck int companyID, List<LightTarget> targets,
                                             LightTarget hiddenTarget, String recipientsType, DateFormats dateFormats) throws Exception {
        if (successTableActivated(companyID) && hasSuccessTableData(companyID, mailingID)) {
            insertDeliveredMailsFromSuccessTbl(tempTableID, mailingID, companyID, targets, hiddenTarget, recipientsType, dateFormats);
        } else if (isMailingNotExpired(mailingID)) {
            insertDeliveredMailsCalculated(tempTableID, companyID);
        }
    }

    @DaoUpdateReturnValueCheck
    public void insertMailformatsIntoTempTable(int tempTableID, int mailingID, @VelocityCheck int companyID, List<BirtReporUtils.BirtReportFigure> figures, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder();
        List<Object> parameters = new ArrayList<>();

        queryBuilder
                .append("SELECT mailtypes.id AS mailtype, SUM(CASE WHEN no_of_mailings IS NULL THEN 0 ELSE no_of_mailings END) mails_sent ")
                .append("FROM ( ")
                .append("SELECT ").append(MailType.HTML.getIntValue()).append(" AS id FROM dual ")
                .append("UNION ALL SELECT ").append(MailType.HTML_OFFLINE.getIntValue()).append(" AS id FROM dual ")
                // adds unknown mail type "3" for correct sent mails calculation
                .append("UNION ALL SELECT ").append(3).append(" AS id FROM dual ")
                .append("UNION ALL SELECT ").append(MailType.TEXT.getIntValue()).append(" AS id FROM dual) mailtypes ")
                .append("LEFT JOIN mailing_account_tbl ma ")
                .append("ON ma.mailtype = mailtypes.id ")
                .append("AND ma.company_id = ? ")
                .append("AND ma.mailing_id = ? ");
        parameters.add(companyID);
        parameters.add(mailingID);

        int mailingType = getMailingType(mailingID);
        if (mailingType == MailingTypes.DATE_BASED.getCode()) {
            queryBuilder.append("AND ma.status_field = 'R' ");
        } else if (mailingType == MailingTypes.ACTION_BASED.getCode()) {
            queryBuilder.append("AND ma.status_field = 'E' ");
        } else if (mailingType == MailingTypes.INTERVAL.getCode()) {
            queryBuilder.append("AND ma.status_field = 'D' ");
        } else {
            queryBuilder.append("AND ma.status_field = 'W' ");
        }

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= ma.timestamp AND ma.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        queryBuilder.append("GROUP BY mailtypes.id");

        if (!(figures.contains(BirtReporUtils.BirtReportFigure.HTML) || figures.contains(BirtReporUtils.BirtReportFigure.TEXT) ||
                figures.contains(BirtReporUtils.BirtReportFigure.OFFLINE_HTML))) {
            return;
        }

        List<Map<String, Object>> resultList = select(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));
        // variable to count both mail type "2" and "3" as Offline-HTML
        int sentOfflineHtml = 0;
        for (Map<String, Object> map : resultList) {
            int mailtype = ((Number) map.get("mailtype")).intValue();
            int categoryIndex = 0;
            String category = "";
            if (mailtype == MailType.HTML.getIntValue() && figures.contains(BirtReporUtils.BirtReportFigure.HTML)) {
                categoryIndex = CommonKeys.SENT_HTML_INDEX;
                category = CommonKeys.SENT_HTML;
            } else if (mailtype == MailType.TEXT.getIntValue() && figures.contains(BirtReporUtils.BirtReportFigure.TEXT)) {
                categoryIndex = CommonKeys.SENT_TEXT_INDEX;
                category = CommonKeys.SENT_TEXT;
            } else if ((mailtype == MailType.HTML_OFFLINE.getIntValue() || mailtype == 3) && figures.contains(BirtReporUtils.BirtReportFigure.OFFLINE_HTML)) {
                categoryIndex = CommonKeys.SENT_OFFLINE_HTML_INDEX;
                category = CommonKeys.SENT_OFFILE_HTML;
            }
            if (categoryIndex != 0) {
                int mailsSent = ((Number) map.get("mails_sent")).intValue();
                // delaying of insert the mail sent value to count both mail types "2" and "3" as Offline-HTML
                if (categoryIndex == CommonKeys.SENT_OFFLINE_HTML_INDEX) {
                    sentOfflineHtml += mailsSent;
                } else {
                    insertIntoTempTable(tempTableID, category, categoryIndex, CommonKeys.ALL_SUBSCRIBERS, CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID, CommonKeys.ALL_SUBSCRIBERS_INDEX, mailsSent);
                    insertMailformatRate(tempTableID, CommonKeys.ALL_SUBSCRIBERS_INDEX, categoryIndex);
                }
            }
        }
        // Inserts the Offline-HTML value witch was deferred
        if (figures.contains(BirtReporUtils.BirtReportFigure.OFFLINE_HTML)) {
            insertIntoTempTable(tempTableID, CommonKeys.SENT_OFFILE_HTML, CommonKeys.SENT_OFFLINE_HTML_INDEX, CommonKeys.ALL_SUBSCRIBERS, CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID, CommonKeys.ALL_SUBSCRIBERS_INDEX, sentOfflineHtml);
            insertMailformatRate(tempTableID, CommonKeys.ALL_SUBSCRIBERS_INDEX, CommonKeys.SENT_OFFLINE_HTML_INDEX);
        }
    }

    public int prepareDashboardForCharts(int mailingID, @VelocityCheck int companyID) throws Exception {
        int tempTableID = createTempTable();
        DateFormats dateFormats = new DateFormats();
        insertSendIntoTempTable(mailingID, tempTableID, companyID, null, null, CommonKeys.TYPE_ALL_SUBSCRIBERS, dateFormats);
        insertClickersIntoTempTable(mailingID, tempTableID, companyID, null, null, CommonKeys.TYPE_ALL_SUBSCRIBERS, true, dateFormats);
        insertOpenersIntoTempTable(mailingID, tempTableID, companyID, null, null, CommonKeys.TYPE_ALL_SUBSCRIBERS, true, false, dateFormats);
        insertBouncesIntoTempTable(mailingID, tempTableID, companyID, null, null, CommonKeys.TYPE_ALL_SUBSCRIBERS, false, dateFormats);
        insertOptOutsIntoTempTable(mailingID, tempTableID, companyID, null, null, CommonKeys.TYPE_ALL_SUBSCRIBERS, dateFormats);
        updateDashboardRates(tempTableID);
        return tempTableID;
    }

    @DaoUpdateReturnValueCheck
    public void insertOpenersIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets,
                                           LightTarget hiddenTarget, String recipientsType, boolean readForAllDeviceClasses,
                                           boolean useOwnTargetGroups, DateFormats dateFormats) throws Exception {
        List<LightTarget> needTargets = useOwnTargetGroups ? getTargets(getTargetIds(mailingID, companyID), companyID) : targets;
        List<LightTarget> allTargets = getTargetListWithAllSubscriberTarget(needTargets);
        int targetDisplayIndex = CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;

        List<TempRow> results = new ArrayList<>();

        final String hiddenTargetSql = hiddenTarget == null ? null : hiddenTarget.getTargetSQL();

        for (LightTarget target : allTargets) {

            String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);

            int totalOpeners = selectOpeners(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());

            TempRow allSubscribersRow = new TempRow();
            allSubscribersRow.setCategory(target.getId() == CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID && !readForAllDeviceClasses ? CommonKeys.OPENERS : CommonKeys.OPENERS_MEASURED);
            allSubscribersRow.setCategoryIndex(CommonKeys.OPENERS_INDEX);
            allSubscribersRow.setTargetGroup(target.getName());
            allSubscribersRow.setTargetGroupId(target.getId());
            allSubscribersRow.setTargetGroupIndex(targetDisplayIndex);
            allSubscribersRow.setValue(totalOpeners);
            results.add(allSubscribersRow);

            if (readForAllDeviceClasses) {
                // If a customer is within one of theses deviceclasses he hasn't opened the mail with any other deviceclass.
                Map<DeviceClass, Integer> openersByDeviceClassWithoutCombinations =
                        selectOpenersByDeviceClassWithoutCombinations(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());

                // Calculating the openers which used more than one deviceclass for link-opens
                int openersWithDeviceClassCombinations = totalOpeners;
                for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
                    if (openersByDeviceClassWithoutCombinations.containsKey(deviceClass)) {
                        openersWithDeviceClassCombinations -= openersByDeviceClassWithoutCombinations.get(deviceClass);
                    }
                }

                TempRow openersTrackedRow = new TempRow();
                openersTrackedRow.setCategory(CommonKeys.OPENERS_TRACKED);
                openersTrackedRow.setCategoryIndex(CommonKeys.OPENERS_TRACKED_INDEX);
                openersTrackedRow.setTargetGroup(target.getName());
                openersTrackedRow.setTargetGroupId(target.getId());
                openersTrackedRow.setTargetGroupIndex(targetDisplayIndex);
                openersTrackedRow.setValue(totalOpeners);
                results.add(openersTrackedRow);

                TempRow openerDesktopRow = new TempRow();
                openerDesktopRow.setCategory(CommonKeys.OPENERS_PC);
                openerDesktopRow.setCategoryIndex(CommonKeys.OPENERS_PC_INDEX);
                openerDesktopRow.setTargetGroup(target.getName());
                openerDesktopRow.setTargetGroupId(target.getId());
                openerDesktopRow.setTargetGroupIndex(targetDisplayIndex);
                openerDesktopRow.setValue(openersByDeviceClassWithoutCombinations.get(DeviceClass.DESKTOP));
                results.add(openerDesktopRow);

                TempRow openerMobileRow = new TempRow();
                openerMobileRow.setCategory(CommonKeys.OPENERS_MOBILE);
                openerMobileRow.setCategoryIndex(CommonKeys.OPENERS_MOBILE_INDEX);
                openerMobileRow.setTargetGroup(target.getName());
                openerMobileRow.setTargetGroupId(target.getId());
                openerMobileRow.setTargetGroupIndex(targetDisplayIndex);
                openerMobileRow.setValue(openersByDeviceClassWithoutCombinations.get(DeviceClass.MOBILE));
                results.add(openerMobileRow);

                TempRow openerTabletRow = new TempRow();
                openerTabletRow.setCategory(CommonKeys.OPENERS_TABLET);
                openerTabletRow.setCategoryIndex(CommonKeys.OPENERS_TABLET_INDEX);
                openerTabletRow.setTargetGroup(target.getName());
                openerTabletRow.setTargetGroupId(target.getId());
                openerTabletRow.setTargetGroupIndex(targetDisplayIndex);
                openerTabletRow.setValue(openersByDeviceClassWithoutCombinations.get(DeviceClass.TABLET));
                results.add(openerTabletRow);

                TempRow openerSmarttvRow = new TempRow();
                openerSmarttvRow.setCategory(CommonKeys.OPENERS_SMARTTV);
                openerSmarttvRow.setCategoryIndex(CommonKeys.OPENERS_SMARTTV_INDEX);
                openerSmarttvRow.setTargetGroup(target.getName());
                openerSmarttvRow.setTargetGroupId(target.getId());
                openerSmarttvRow.setTargetGroupIndex(targetDisplayIndex);
                openerSmarttvRow.setValue(openersByDeviceClassWithoutCombinations.get(DeviceClass.SMARTTV));
                results.add(openerSmarttvRow);

                TempRow openerPcAndMobileRow = new TempRow();
                openerPcAndMobileRow.setCategory(CommonKeys.OPENERS_PC_AND_MOBILE);
                openerPcAndMobileRow.setCategoryIndex(CommonKeys.OPENERS_PC_AND_MOBILE_INDEX);
                openerPcAndMobileRow.setTargetGroup(target.getName());
                openerPcAndMobileRow.setTargetGroupId(target.getId());
                openerPcAndMobileRow.setTargetGroupIndex(targetDisplayIndex);
                openerPcAndMobileRow.setValue(openersWithDeviceClassCombinations);
                results.add(openerPcAndMobileRow);
            }

            targetDisplayIndex++;
        }

        insertIntoTempTable(tempTableID, results);
    }

    @DaoUpdateReturnValueCheck
    public void insertClickersIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets,
                                            LightTarget hiddenTarget, String recipientsType, boolean readForAllDeviceClasses,
                                            DateFormats dateFormats) throws Exception {
        List<LightTarget> allTargets = getTargetListWithAllSubscriberTarget(targets);
        int targetDisplayIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;

        List<TempRow> results = new ArrayList<>();

        final String hiddenTargetSql = hiddenTarget == null ? null : hiddenTarget.getTargetSQL();

        for (LightTarget target : allTargets) {

            final String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);

            int totalClicks = selectClicks(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());
            int totalClickers = selectClickers(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());

            TempRow clickerIndexRow = new TempRow();
            clickerIndexRow.setCategory(CommonKeys.CLICKER);
            clickerIndexRow.setCategoryIndex(CommonKeys.CLICKER_INDEX);
            clickerIndexRow.setTargetGroup(target.getName());
            clickerIndexRow.setTargetGroupId(target.getId());
            clickerIndexRow.setTargetGroupIndex(targetDisplayIndex);
            clickerIndexRow.setValue(totalClickers);
            results.add(clickerIndexRow);

            TempRow clicksGrossRow = new TempRow();
            clicksGrossRow.setCategory(CommonKeys.CLICKS_GROSS);
            clicksGrossRow.setCategoryIndex(CommonKeys.CLICKS_GROSS_INDEX);
            clicksGrossRow.setTargetGroup(target.getName());
            clicksGrossRow.setTargetGroupId(target.getId());
            clicksGrossRow.setTargetGroupIndex(targetDisplayIndex);
            clicksGrossRow.setValue(totalClicks);
            results.add(clicksGrossRow);

            if (readForAllDeviceClasses) {
                // If a customer is within one of theses deviceclasses he hasn't clicked in the mail with any other deviceclass.
                Map<DeviceClass, Integer> clickersByDeviceClassWithoutCombinations =
                        selectClickersByDeviceClassWithoutCombinations(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());

                // Calculating the clickers which used more than one deviceclass for link-clicks
                int clickersWithDeviceClassCombinations = totalClickers;
                for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
                    if (clickersByDeviceClassWithoutCombinations.containsKey(deviceClass)) {
                        clickersWithDeviceClassCombinations -= clickersByDeviceClassWithoutCombinations.get(deviceClass);
                    }
                }

                TempRow clickerTrackedRow = new TempRow();
                clickerTrackedRow.setCategory(CommonKeys.CLICKER_TRACKED);
                clickerTrackedRow.setCategoryIndex(CommonKeys.CLICKER_TRACKED_INDEX);
                clickerTrackedRow.setTargetGroup(target.getName());
                clickerTrackedRow.setTargetGroupId(target.getId());
                clickerTrackedRow.setTargetGroupIndex(targetDisplayIndex);
                clickerTrackedRow.setValue(totalClickers);
                results.add(clickerTrackedRow);

                TempRow clickerDesktopRow = new TempRow();
                clickerDesktopRow.setCategory(CommonKeys.CLICKER_PC);
                clickerDesktopRow.setCategoryIndex(CommonKeys.CLICKER_PC_INDEX);
                clickerDesktopRow.setTargetGroup(target.getName());
                clickerDesktopRow.setTargetGroupId(target.getId());
                clickerDesktopRow.setTargetGroupIndex(targetDisplayIndex);
                clickerDesktopRow.setValue(clickersByDeviceClassWithoutCombinations.get(DeviceClass.DESKTOP));
                results.add(clickerDesktopRow);

                TempRow clickerMobileRow = new TempRow();
                clickerMobileRow.setCategory(CommonKeys.CLICKER_MOBILE);
                clickerMobileRow.setCategoryIndex(CommonKeys.CLICKER_MOBILE_INDEX);
                clickerMobileRow.setTargetGroup(target.getName());
                clickerMobileRow.setTargetGroupId(target.getId());
                clickerMobileRow.setTargetGroupIndex(targetDisplayIndex);
                clickerMobileRow.setValue(clickersByDeviceClassWithoutCombinations.get(DeviceClass.MOBILE));
                results.add(clickerMobileRow);

                TempRow clickerTabletRow = new TempRow();
                clickerTabletRow.setCategory(CommonKeys.CLICKER_TABLET);
                clickerTabletRow.setCategoryIndex(CommonKeys.CLICKER_TABLET_INDEX);
                clickerTabletRow.setTargetGroup(target.getName());
                clickerTabletRow.setTargetGroupId(target.getId());
                clickerTabletRow.setTargetGroupIndex(targetDisplayIndex);
                clickerTabletRow.setValue(clickersByDeviceClassWithoutCombinations.get(DeviceClass.TABLET));
                results.add(clickerTabletRow);

                TempRow clickerSmarttvRow = new TempRow();
                clickerSmarttvRow.setCategory(CommonKeys.CLICKER_SMARTTV);
                clickerSmarttvRow.setCategoryIndex(CommonKeys.CLICKER_SMARTTV_INDEX);
                clickerSmarttvRow.setTargetGroup(target.getName());
                clickerSmarttvRow.setTargetGroupId(target.getId());
                clickerSmarttvRow.setTargetGroupIndex(targetDisplayIndex);
                clickerSmarttvRow.setValue(clickersByDeviceClassWithoutCombinations.get(DeviceClass.SMARTTV));
                results.add(clickerSmarttvRow);

                TempRow clickerPcAndMobileRow = new TempRow();
                clickerPcAndMobileRow.setCategory(CommonKeys.CLICKER_PC_AND_MOBILE);
                clickerPcAndMobileRow.setCategoryIndex(CommonKeys.CLICKER_PC_AND_MOBILE_INDEX);
                clickerPcAndMobileRow.setTargetGroup(target.getName());
                clickerPcAndMobileRow.setTargetGroupId(target.getId());
                clickerPcAndMobileRow.setTargetGroupIndex(targetDisplayIndex);
                clickerPcAndMobileRow.setValue(clickersWithDeviceClassCombinations);
                results.add(clickerPcAndMobileRow);
            }

            targetDisplayIndex++;
        }

        insertIntoTempTable(tempTableID, results);
    }

    @DaoUpdateReturnValueCheck
    public void insertClicksAnonymousIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, DateFormats dateFormats) throws Exception {
        int anonymousClicks = selectAnonymousClicks(companyID, mailingID, dateFormats.getStartDate(), dateFormats.getStopDate());

        insertIntoTempTable(tempTableID,
                CommonKeys.CLICKS_ANONYMOUS,
                CommonKeys.CLICKS_ANONYMOUS_INDEX,
                CommonKeys.ALL_SUBSCRIBERS,
                CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID,
                CommonKeys.ALL_SUBSCRIBERS_INDEX,
                anonymousClicks);
    }

    @DaoUpdateReturnValueCheck
    public void insertRecipientsNumberToTemplate(MailingDataSet mailingDataSet, int mailingId, int tempTableID, DateFormats dateFormats) {
        try {
            Map<String, Object> mailStats = mailingDataSet.getMailingStats(mailingId, dateFormats.getStartDate(), dateFormats.getStopDate());
            int mailsNum = ((Number) mailStats.get("MAILS")).intValue();
            insertIntoTempTable(tempTableID,
                    CommonKeys.RECIPIENTS_NUMBER,
                    CommonKeys.RECIPIENTS_NUMBER_INDEX,
                    CommonKeys.ALL_SUBSCRIBERS,
                    CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID,
                    CommonKeys.ALL_SUBSCRIBERS_INDEX,
                    mailsNum);
        } catch (Exception ex) {
            logger.error("Please check RecipientsNumber method !", ex);
        }
    }

    @DaoUpdateReturnValueCheck
    public void updateRates(int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets) throws Exception {
        List<Integer> totalIndexes = new ArrayList<>();
        totalIndexes.add(CommonKeys.DELIVERED_EMAILS_INDEX);

        List<Integer> totalIndexesDelivered = new ArrayList<>();
        totalIndexesDelivered.add(CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX);

        Map<Integer, Integer> allTargets = getTargetMap(
                isMailingTrackingActivated(companyID) ? getTargetListWithAllSubscriberTarget(targets) : getTargetListWithAllSubscriberTarget(null),
                CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID
        );
        List<Integer> categoryIndexes = new ArrayList<>();
        for (int i = 1; i <= CommonKeys.SENT_OFFLINE_HTML_INDEX; i++) {
            categoryIndexes.add(i);
        }

        updateRatesByCategories(tempTableID, totalIndexes, new ArrayList<>(allTargets.values()), categoryIndexes);
        updateDeliveredRatesByCategories(tempTableID, totalIndexesDelivered, new ArrayList<>(allTargets.values()), categoryIndexes);

        // mobile/PC clicks
        allTargets.clear();
        allTargets = getTargetMap(getTargetListWithAllSubscriberTarget(targets), CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
        List<Integer> clickerIndexes = Arrays.asList(CommonKeys.CLICKER_PC_INDEX,
                CommonKeys.CLICKER_MOBILE_INDEX,
                CommonKeys.CLICKER_TABLET_INDEX,
                CommonKeys.CLICKER_SMARTTV_INDEX,
                CommonKeys.CLICKER_PC_AND_MOBILE_INDEX
        );
        updateRatesByCategories(tempTableID, clickerIndexes, new ArrayList<>(allTargets.values()), clickerIndexes);

        // mobile/PC openings
        List<Integer> openingIndexes = Arrays.asList(CommonKeys.OPENERS_PC_INDEX,
                CommonKeys.OPENERS_TABLET_INDEX,
                CommonKeys.OPENERS_MOBILE_INDEX,
                CommonKeys.OPENERS_SMARTTV_INDEX,
                CommonKeys.OPENERS_PC_AND_MOBILE_INDEX
        );
        updateRatesByCategories(tempTableID, openingIndexes, new ArrayList<>(allTargets.values()), openingIndexes);

        Integer[] measuredCategories = {
                CommonKeys.CLICKER_TRACKED_INDEX,
                CommonKeys.OPENERS_TRACKED_INDEX
        };

        String sqlUpdateMeasuredCategories = "UPDATE " +
                getTempTableName(tempTableID) +
                " SET rate = 1 WHERE targetgroup_index IN (" +
                StringUtils.join(allTargets.values(), ", ") +
                ") AND category_index IN (" +
                StringUtils.join(measuredCategories, ", ") +
                ")";

        updateEmbedded(logger, sqlUpdateMeasuredCategories);

        // Gross openings / gross clicks
        updateEmbedded(logger, getUpdateResponseRateQuery(tempTableID), CommonKeys.ACTIVITY_RATE, CommonKeys.ACTIVITY_RATE_INDEX);
    }

    private void updateRatesByCategories(int tempTableID, List<Integer> allCategoryIndex, List<Integer> allTargetgroupIndex, List<Integer> categoryIndex) throws Exception {
        StringBuilder totalCountQuery = new StringBuilder();
        totalCountQuery
                .append("SELECT a.targetgroup_index, CASE WHEN (SUM(a.value) IS NULL OR SUM(a.value) = 0) THEN -1 ELSE SUM(a.value) END AS total")
                .append(" FROM ").append(getTempTableName(tempTableID)).append(" a")
                .append(" WHERE a.targetgroup_index IN (").append(StringUtils.join(allTargetgroupIndex, ", ")).append(")")
                .append(" AND a.category_index IN (").append(StringUtils.join(allCategoryIndex, ", ")).append(")")
                .append(" GROUP BY a.targetgroup_index");

        StringBuilder updateRateQuery = new StringBuilder();
        updateRateQuery
                .append("UPDATE ").append(getTempTableName(tempTableID)).append(" t")
                .append(" SET t.rate = (CASE WHEN (? = -1) THEN -1 ELSE 1.0 * t.value / ? END)")
                .append(" WHERE t.targetgroup_index = ? AND t.category_index IN (").append(StringUtils.join(categoryIndex, ", ")).append(")");

        for (Map<String, Object> row : selectEmbedded(logger, totalCountQuery.toString())) {
            updateEmbedded(logger, updateRateQuery.toString(), ((Number) row.get("total")).intValue(), ((Number) row.get("total")).intValue(), ((Number) row.get("targetgroup_index")).intValue());
        }
    }

    private void updateDeliveredRatesByCategories(int tempTableID, List<Integer> allCategoryIndex, List<Integer> allTargetgroupIndex, List<Integer> categoryIndex) throws Exception {
        StringBuilder totalCountQuery = new StringBuilder();
        totalCountQuery
                .append("SELECT a.targetgroup_index, CASE WHEN (SUM(a.value) IS NULL OR SUM(a.value) = 0) THEN -1 ELSE SUM(a.value) END AS total")
                .append(" FROM ").append(getTempTableName(tempTableID)).append(" a")
                .append(" WHERE a.targetgroup_index IN (").append(StringUtils.join(allTargetgroupIndex, ", ")).append(")")
                .append(" AND a.category_index IN (").append(StringUtils.join(allCategoryIndex, ", ")).append(")")
                .append(" GROUP BY a.targetgroup_index");

        StringBuilder updateRateQuery = new StringBuilder();
        updateRateQuery
                .append("UPDATE ").append(getTempTableName(tempTableID))
                .append(" SET rate_delivered = (CASE WHEN category_index IN (" + CommonKeys.DELIVERED_EMAILS_INDEX + "," + CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX + ") THEN rate ELSE (CASE WHEN (? = -1) THEN -1 ELSE 1.0 * value / ? END) END) ")
                .append(" WHERE targetgroup_index = ? AND category_index IN (").append(StringUtils.join(categoryIndex, ", ")).append(")");

        for (Map<String, Object> row : selectEmbedded(logger, totalCountQuery.toString())) {
            updateEmbedded(logger, updateRateQuery.toString(), ((Number) row.get("total")).intValue(), ((Number) row.get("total")).intValue(), ((Number) row.get("targetgroup_index")).intValue());
        }
    }

    @DaoUpdateReturnValueCheck
    private void insertMailformatRate(int tempTableID, int targetGroupIndex, int categoryIndex) throws Exception {
        try {
            String queryTotalSend = "SELECT value FROM " + getTempTableName(tempTableID) + " WHERE targetgroup_index = ? AND category_index = ? ";
            int totalSend = selectEmbedded(logger, queryTotalSend, Integer.class, targetGroupIndex, CommonKeys.RECIPIENTS_NUMBER_INDEX);
            if (totalSend != 0) {
                String queryUpdateRate = "UPDATE " + getTempTableName(tempTableID) + " SET rate = ( value * 1.0 ) / ? WHERE targetgroup_index = ? AND category_index in (" + categoryIndex + ")";
                updateEmbedded(logger, queryUpdateRate, totalSend, targetGroupIndex);
            }
        } catch (DataAccessException e) {
            logger.error("MailingSummaryDataSet.insertMailformatRate : " + e);
        }
    }

    public List<MailingSummaryRow> getSummaryData(Integer tempTableID) throws Exception {
        if (tempTableID == null) {
            logger.error("tempTableID is null in call for getSummaryData");
            throw new Exception("tempTableID is null in call for getSummaryData");
        } else {
            return getResultsFromTempTable(tempTableID);
        }
    }

    @DaoUpdateReturnValueCheck
    public void insertRevenueIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets,
                                           LightTarget hiddenTarget, DateFormats dateFormats) throws Exception {
        if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_num_tbl")) {
            final String hiddenTargetSql = hiddenTarget == null ? null : hiddenTarget.getTargetSQL();

            List<LightTarget> allTargets = getTargetListWithAllSubscriberTarget(targets);

            int targetDisplayIndex = CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;
            List<TempRow> results = new ArrayList<>();
            for (LightTarget target : allTargets) {
                final String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);

                double revenue = selectRevenue(companyID, mailingID, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());

                TempRow newItem = new TempRow();
                newItem.setCategory(CommonKeys.REVENUE);
                newItem.setCategoryIndex(CommonKeys.REVENUE_INDEX);
                newItem.setTargetGroup(target.getName());
                newItem.setTargetGroupId(target.getId());
                newItem.setTargetGroupIndex(targetDisplayIndex);
                newItem.setValue((int) (100 * revenue));
                results.add(newItem);

                targetDisplayIndex++;
            }

            insertIntoTempTable(tempTableID, results);
        }
    }

    @DaoUpdateReturnValueCheck
    public void insertOptOutsIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets,
                                           LightTarget hiddenTarget, String recipientsType, DateFormats dateFormats) throws Exception {
        List<LightTarget> allTargets = getTargetListWithAllSubscriberTarget(targets);

        int targetDisplayIndex = CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;

        final String hiddenTargetSql = hiddenTarget == null ? null : hiddenTarget.getTargetSQL();

        List<TempRow> results = new ArrayList<>();
        for (LightTarget target : allTargets) {

            final String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);

            int optouts = selectOptOuts(companyID, mailingID, resultTargetSql, recipientsType, dateFormats.getStartDate(), dateFormats.getStopDate());

            TempRow newItem = new TempRow();
            newItem.setCategory(CommonKeys.OPT_OUTS);
            newItem.setCategoryIndex(CommonKeys.OPT_OUTS_INDEX);
            newItem.setTargetGroup(target.getName());
            newItem.setTargetGroupId(target.getId());
            newItem.setTargetGroupIndex(targetDisplayIndex);
            newItem.setValue(optouts);
            results.add(newItem);

            targetDisplayIndex++;
        }

        insertIntoTempTable(tempTableID, results);
    }

    public void insertOpenedGrossIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets, DateFormats dateFormats) throws Exception {
        List<LightTarget> allTargets = getTargetListWithAllSubscriberTarget(targets);

        List<TempRow> results = new ArrayList<>();
        int targetDisplayIndex = CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;
        for (LightTarget target : allTargets) {
            int openings = selectOpenings(companyID, mailingID, null, target.getTargetSQL(), dateFormats.getStartDate(), dateFormats.getStopDate());

            TempRow row = new TempRow();
            row.setCategory(CommonKeys.OPENINGS_GROSS_MEASURED);
            row.setCategoryIndex(CommonKeys.OPENINGS_GROSS_MEASURED_INDEX);
            row.setTargetGroup(target.getName());
            row.setTargetGroupId(target.getId());
            row.setTargetGroupIndex(targetDisplayIndex);
            row.setValue(openings);
            results.add(row);

            targetDisplayIndex++;
        }
        insertIntoTempTable(tempTableID, results);
    }

    @DaoUpdateReturnValueCheck
    public void insertOpenedAnonymousIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, DateFormats dateFormats) throws Exception {
        int anonymousOpenings = selectAnonymousOpenings(companyID, mailingID, dateFormats.getStartDate(), dateFormats.getStopDate());

        insertIntoTempTable(tempTableID,
                CommonKeys.OPENINGS_ANONYMOUS,
                CommonKeys.OPENINGS_ANONYMOUS_INDEX,
                CommonKeys.ALL_SUBSCRIBERS,
                CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID,
                CommonKeys.ALL_SUBSCRIBERS_INDEX,
                anonymousOpenings);
    }

    public void insertOpenedInvisibleIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets,
                                                   LightTarget hiddenTarget, String recipientsType, DateFormats dateFormats) throws Exception {
        insertOpenedInvisibleIntoTempTable(mailingID, tempTableID, companyID, targets, hiddenTarget, recipientsType, false, dateFormats);
    }

    @DaoUpdateReturnValueCheck
    public void insertOpenedInvisibleIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> subTargets,
                                                   LightTarget hiddenTarget, String recipientsType, boolean useOwnTargetGroups, DateFormats dateFormats) throws Exception {
        Map<Integer, Integer> measuredAll = getMeasuredFromTempTable(tempTableID);
        List<LightTarget> needTargets = useOwnTargetGroups ? getTargets(getTargetIds(mailingID, companyID), companyID) : subTargets;
        List<LightTarget> allTargets = getTargetListWithAllSubscriberTarget(needTargets);
        List<TempRow> results = new ArrayList<>();
        int targetDisplayIndex = CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;

        final String hiddenTargetSql = hiddenTarget == null ? null : hiddenTarget.getTargetSQL();

        for (LightTarget target : allTargets) {

            final String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);

            int openingClickers = selectOpeningClickers(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());
            int nonOpeningClickers = selectNonOpeningClickers(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());
            int maximumOverallOpeners;
            if (successTableActivated(companyID) && isMailingNotExpired(mailingID)) {
                maximumOverallOpeners = selectNumberOfDeliveredMails(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());
            } else {
                if (isMailingNotExpired(mailingID)) {
                    if (StringUtils.isBlank(resultTargetSql) || resultTargetSql.replace(" ", "").equals("1=1")) {
                        // this calculation only works for the "all_recipients" target
                        int mailsSent = getNumberSentMailings(companyID, mailingID, CommonKeys.TYPE_WORLDMAILING, null, dateFormats.getStartDate(), dateFormats.getStopDate());
                        int hardbounces = selectHardbouncesFromBounces(companyID, mailingID, null, recipientsType, dateFormats.getStartDate(), dateFormats.getStopDate());
                        maximumOverallOpeners = mailsSent - hardbounces;
                    } else {
                        maximumOverallOpeners = 0;
                    }
                } else {
                    if (StringUtils.isBlank(resultTargetSql) || resultTargetSql.replace(" ", "").equals("1=1")) {
                        // this calculation only works for the "all_recipients" target
                        int mailsSent = getNumberSentMailings(companyID, mailingID, CommonKeys.TYPE_WORLDMAILING, null, dateFormats.getStartDate(), dateFormats.getStopDate());
                        maximumOverallOpeners = mailsSent;
                    } else {
                        maximumOverallOpeners = 0;
                    }
                }
            }

            int measuredOpeners = measuredAll.getOrDefault(target.getId(), 0);
            int invisibleOpeners;
            if (openingClickers > 0 && measuredOpeners > 0 && maximumOverallOpeners > 0 && (openingClickers * 100 / measuredOpeners) >= 5) {
                // Only extrapolate invisible openers if the base number "openingClickers per measuredOpeners" is at least 5 percent
                // Watch out for int max overflow
                invisibleOpeners = (int) ((((long) measuredOpeners) * ((long) nonOpeningClickers)) / (openingClickers));
                // Limit all openers to 100 percent of sentMails, just to not confuse the user
                int maxInvisibleOpeners = maximumOverallOpeners - measuredOpeners;
                invisibleOpeners = Math.max(0, Math.min(maxInvisibleOpeners, invisibleOpeners));
            } else {
                invisibleOpeners = nonOpeningClickers;
            }

            TempRow invisibleOpenersRow = new TempRow();
            invisibleOpenersRow.setCategory(CommonKeys.OPENERS_INVISIBLE);
            invisibleOpenersRow.setCategoryIndex(CommonKeys.OPENERS_INVISIBLE_INDEX);
            invisibleOpenersRow.setTargetGroup(target.getName());
            invisibleOpenersRow.setTargetGroupId(target.getId());
            invisibleOpenersRow.setTargetGroupIndex(targetDisplayIndex);
            invisibleOpenersRow.setValue(invisibleOpeners);
            results.add(invisibleOpenersRow);

            TempRow visibleOpenersRow = new TempRow();
            visibleOpenersRow.setCategory(CommonKeys.OPENERS_TOTAL);
            visibleOpenersRow.setCategoryIndex(CommonKeys.OPENERS_TOTAL_INDEX);
            visibleOpenersRow.setTargetGroup(target.getName());
            visibleOpenersRow.setTargetGroupId(target.getId());
            visibleOpenersRow.setTargetGroupIndex(targetDisplayIndex);
            visibleOpenersRow.setValue(measuredOpeners + invisibleOpeners);
            results.add(visibleOpenersRow);

            targetDisplayIndex++;
        }

        insertIntoTempTable(tempTableID, results);
    }

    public void insertBouncesIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets,
                                           LightTarget hiddenTarget, String recipientsType, boolean includeSoftbounces,
                                           DateFormats dateFormats) throws Exception {
        List<LightTarget> allTargets = getTargetListWithAllSubscriberTarget(targets);

        List<TempRow> results = new ArrayList<>();

        int targetDisplayIndex = CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;

        final String hiddenTargetSql = hiddenTarget == null ? null : hiddenTarget.getTargetSQL();

        if (!isMailingBouncesExpire(companyID, mailingID) || !isOracleDB()) {
            for (LightTarget target : allTargets) {
                final String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);

                int hardbounces = selectHardbouncesFromBounces(companyID, mailingID, resultTargetSql, recipientsType, dateFormats.getStartDate(), dateFormats.getStopDate());

                TempRow row = new TempRow();
                row.setCategory(CommonKeys.HARD_BOUNCES);
                row.setCategoryIndex(CommonKeys.HARD_BOUNCES_INDEX);
                row.setTargetGroup(target.getName());
                row.setTargetGroupId(target.getId());
                row.setTargetGroupIndex(targetDisplayIndex);
                row.setValue(hardbounces);
                results.add(row);

                if (includeSoftbounces) {
                    int softbounces = selectSoftbouncesFromBounces(companyID, mailingID, resultTargetSql, recipientsType, dateFormats.getStartDate(), dateFormats.getStopDate());

                    row = new TempRow();
                    row.setCategory(CommonKeys.SOFT_BOUNCES);
                    row.setCategoryIndex(CommonKeys.SOFT_BOUNCES_INDEX);
                    row.setTargetGroup(target.getName());
                    row.setTargetGroupId(target.getId());
                    row.setTargetGroupIndex(targetDisplayIndex);
                    row.setValue(softbounces);
                    results.add(row);
                }

                targetDisplayIndex++;
            }
        } else {
            for (LightTarget target : allTargets) {
                final String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);

                int hardbounces = selectHardbouncesFromBindings(companyID, mailingID, resultTargetSql, recipientsType, dateFormats.getStartDate(), dateFormats.getStopDate());

                TempRow row = new TempRow();
                row.setCategory(CommonKeys.HARD_BOUNCES);
                row.setCategoryIndex(CommonKeys.HARD_BOUNCES_INDEX);
                row.setTargetGroup(target.getName());
                row.setTargetGroupId(target.getId());
                row.setTargetGroupIndex(targetDisplayIndex);
                row.setValue(hardbounces);
                results.add(row);

                if (includeSoftbounces) {
                    int softbounces = selectSoftbouncesFromBindings(companyID, mailingID, resultTargetSql, recipientsType, dateFormats.getStartDate(), dateFormats.getStopDate());

                    row = new TempRow();
                    row.setCategory(CommonKeys.SOFT_BOUNCES);
                    row.setCategoryIndex(CommonKeys.SOFT_BOUNCES_INDEX);
                    row.setTargetGroup(target.getName());
                    row.setTargetGroupId(target.getId());
                    row.setTargetGroupIndex(targetDisplayIndex);
                    row.setValue(softbounces);
                    results.add(row);
                }

                targetDisplayIndex++;
            }
        }

        insertIntoTempTable(tempTableID, results);
    }

    @DaoUpdateReturnValueCheck
    public void insertSendIntoTempTable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets,
                                        LightTarget hiddenTarget, String recipientsType, DateFormats dateFormats) throws Exception {
		/*
		Date startDate = null;
		Date endDate = null;

		if (dateFormats.isDateSlice()) {
			SimpleDateFormat dateFormat;
			if (dateFormats.isHourScale()) {
				dateFormat = DATEFORMAT_HOURLY;
			} else {
				dateFormat = DATEFORMAT_DAILY;
			}
			startDate = dateFormat.parse(dateFormats.getStartDate());
			endDate = dateFormat.parse(dateFormats.getStopDate());
		}
		*/

        final String hiddenTargetSql = hiddenTarget == null ? null : hiddenTarget.getTargetSQL();

        int mailsSent = getNumberSentMailings(companyID, mailingID, CommonKeys.TYPE_WORLDMAILING, hiddenTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());

        List<TempRow> results = new ArrayList<>();

        int targetDisplayIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;

        TempRow mailsSentItem = new TempRow();
        mailsSentItem.setCategory(CommonKeys.DELIVERED_EMAILS);
        mailsSentItem.setCategoryIndex(CommonKeys.DELIVERED_EMAILS_INDEX);
        mailsSentItem.setTargetGroup(CommonKeys.ALL_SUBSCRIBERS);
        mailsSentItem.setTargetGroupId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
        mailsSentItem.setTargetGroupIndex(targetDisplayIndex);
        mailsSentItem.setValue(mailsSent);
        results.add(mailsSentItem);

        if (CollectionUtils.isNotEmpty(targets)) {
            boolean mailingTrackingDataAvailable = isMailingTrackingDataAvailable(mailingID, companyID);

            for (LightTarget target : targets) {
                targetDisplayIndex++;

                int numberSentMailings = -1;
                if (mailingTrackingDataAvailable) {
                    final String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);
                    numberSentMailings = getNumberSentMailings(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());
                }

                TempRow newItem = new TempRow();
                newItem.setCategory(CommonKeys.DELIVERED_EMAILS);
                newItem.setCategoryIndex(CommonKeys.DELIVERED_EMAILS_INDEX);
                newItem.setTargetGroup(target.getName());
                newItem.setTargetGroupId(target.getId());
                newItem.setTargetGroupIndex(targetDisplayIndex);
                newItem.setValue(numberSentMailings);
                results.add(newItem);
            }
        }

        insertIntoTempTable(tempTableID, results);
    }

    @DaoUpdateReturnValueCheck
    public void removeCategoryData(int tempTableID, int categoryIndex) throws Exception {
        String query = "DELETE FROM " + getTempTableName(tempTableID) + " WHERE category_index = ?";
        updateEmbedded(logger, query, categoryIndex);
    }

    @DaoUpdateReturnValueCheck
    private void insertDeliveredMailsCalculated(int tempTableID, @VelocityCheck int companyID) throws Exception {
        final boolean isActivated = isMailingTrackingActivated(companyID);
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder
                .append("SELECT targetgroup, targetgroup_index, targetgroup_id,")
                .append(" SUM(CASE WHEN (category_index = ").append(CommonKeys.DELIVERED_EMAILS_INDEX).append(") THEN value ELSE 0 END)")
                .append(" -")
                .append(" SUM(CASE WHEN (category_index = ").append(CommonKeys.HARD_BOUNCES_INDEX).append(") THEN value ELSE 0 END) AS value")
                .append(" FROM ").append(getTempTableName(tempTableID))
                .append(" WHERE category_index IN (").append(CommonKeys.DELIVERED_EMAILS_INDEX).append(", ").append(CommonKeys.HARD_BOUNCES_INDEX).append(")")
                .append(" GROUP BY targetgroup, targetgroup_index, targetgroup_id");

        List<Map<String, Object>> selectResult = selectEmbedded(logger, queryBuilder.toString());
        for (Map<String, Object> row : selectResult) {
            String targetGroupName = (String) row.get("targetgroup");
            int targetGroupId = ((Number) row.get("targetgroup_id")).intValue();
            int targetGroupIndex = ((Number) row.get("targetgroup_index")).intValue();
            int value = ((Number) row.get("value")).intValue();

            if (targetGroupId != CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID && !isActivated) {
                value = -1;
            }

            insertIntoTempTable(
                    tempTableID,
                    CommonKeys.DELIVERED_EMAILS_DELIVERED,
                    CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX,
                    targetGroupName,
                    targetGroupId,
                    targetGroupIndex,
                    value
            );
        }
    }

    @DaoUpdateReturnValueCheck
    private void insertDeliveredMailsFromSuccessTbl(int tempTableID, int mailingID, @VelocityCheck int companyID, List<LightTarget> targets,
                                                    LightTarget hiddenTarget, String recipientsType, DateFormats dateFormats) throws Exception {
        boolean isMailingNotExpired = isMailingNotExpired(mailingID);
        List<LightTarget> allTargets = getTargetListWithAllSubscriberTarget(targets);
        int targetDisplayIndex = CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;
        List<TempRow> results = new ArrayList<>();

        final String hiddenTargetSql = hiddenTarget == null ? null : hiddenTarget.getTargetSQL();


        for (LightTarget target : allTargets) {

            final String resultTargetSql = joinWhereClause(target.getTargetSQL(), hiddenTargetSql);

            int deliveredMails = selectNumberOfDeliveredMails(companyID, mailingID, recipientsType, resultTargetSql, dateFormats.getStartDate(), dateFormats.getStopDate());
            if (deliveredMails > 0 || isMailingNotExpired) {
                TempRow newItem = new TempRow();
                newItem.setCategory(CommonKeys.DELIVERED_EMAILS_DELIVERED);
                newItem.setCategoryIndex(CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX);
                newItem.setTargetGroup(target.getName());
                newItem.setTargetGroupId(target.getId());
                newItem.setTargetGroupIndex(targetDisplayIndex);
                newItem.setValue(deliveredMails);
                results.add(newItem);
            }

            targetDisplayIndex++;
        }

        insertIntoTempTable(tempTableID, results);
    }

    @DaoUpdateReturnValueCheck
    private void updateDashboardRates(int tempTableID) throws Exception {
        String queryTotalSend = "SELECT value FROM " + getTempTableName(tempTableID) + " WHERE targetgroup_index = ? AND category_index = ? ";

        int totalSend = selectEmbedded(logger, queryTotalSend, Integer.class, CommonKeys.ALL_SUBSCRIBERS_INDEX, CommonKeys.DELIVERED_EMAILS_INDEX);

        if (totalSend != 0) {
            String queryUpdateRate = "UPDATE " + getTempTableName(tempTableID) + " SET rate = (value * 1.0) / ?";
            updateEmbedded(logger, queryUpdateRate, totalSend);
        }
    }

    @DaoUpdateReturnValueCheck
    private void insertSoftbouncesUndeliverable(int mailingID, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets, String recipientsType, boolean showSoftbounces) throws Exception {
        if (!successTableActivated(companyID) || !showSoftbounces) {
            return;
        }

        LightMailing mailing = new LightMailingDaoImpl(getDataSource()).getMailing(mailingID, companyID);
        if (mailing != null && mailing.getMailingType() == MailingType.NORMAL.getCode()) {
            int deliveredMails = selectNumberOfDeliveredMails(companyID, mailingID, recipientsType, null, null, null);
            insertSoftbouncesIntoTempTable(tempTableID, deliveredMails, null, CommonKeys.ALL_SUBSCRIBERS_INDEX);
            if (targets != null && targets.size() > 0) {
                int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
                for (LightTarget target : targets) {
                    targetGroupIndex++;
                    int deliveredMailsByTarget = selectNumberOfDeliveredMails(companyID, mailingID, recipientsType, target.getTargetSQL(), null, null);
                    insertSoftbouncesIntoTempTable(tempTableID, deliveredMailsByTarget, target, targetGroupIndex);
                }
            }
        }
    }

    protected void insertSoftbouncesIntoTempTable(int tempTableID, int deliveredMails, LightTarget target, int targetGroupIndex) throws Exception {
        target = getDefaultTarget(target);

        int sentMails = getTempTableValuesByCategoryAndTargetGroupId(tempTableID, CommonKeys.DELIVERED_EMAILS, target.getId());
        int hardBounces = getTempTableValuesByCategoryAndTargetGroupId(tempTableID, CommonKeys.HARD_BOUNCES, target.getId());

        int value = sentMails - deliveredMails - hardBounces;
        value = Math.max(0, value);
        insertIntoTempTable(tempTableID, CommonKeys.SOFT_BOUNCES_UNDELIVERABLE, CommonKeys.SOFT_BOUNCES_UNDELIVERABLE_INDEX, target.getName(), target.getId(), targetGroupIndex, value);
    }

    protected int selectOpeningClickers(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(DISTINCT(r.customer_id)) counter FROM rdirlog_" + companyID + "_tbl r");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE r.mailing_id = ? AND r.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE r.mailing_id = ?");
        }

        // Exclude anonymous entries.
        queryBuilder.append(" AND r.customer_id <> 0");

        parameters.add(mailingID);

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= r.timestamp AND r.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        // here comes the only difference to selectNonOpeningClickers (AND EXISTS)
        queryBuilder.append(" AND EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl o WHERE o.mailing_id = r.mailing_id AND o.customer_id = r.customer_id)");

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));
        return ((Number) result.get(0).get("counter")).intValue();
    }

    protected int selectNonOpeningClickers(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(DISTINCT(r.customer_id)) counter FROM rdirlog_" + companyID + "_tbl r");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE r.mailing_id = ? AND r.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE r.mailing_id = ?");
        }

        // Exclude anonymous entries.
        queryBuilder.append(" AND r.customer_id <> 0");

        parameters.add(mailingID);

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= r.timestamp AND r.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        // here comes the only difference to selectOpeningClickers (AND NOT EXISTS)
        queryBuilder.append(" AND NOT EXISTS (SELECT 1 FROM onepixellog_" + companyID + "_tbl o WHERE o.mailing_id = r.mailing_id AND o.customer_id = r.customer_id)");

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));
        return ((Number) result.get(0).get("counter")).intValue();
    }

    /**
     * Selects a map with numbers of clickers per deviceclass for a single mailingid.
     * Watch out:
     * If a customer is within one of these deviceclasses, then he hasn't clicked in the mail with any other deviceclass.
     * Mixed combination clickers are explicitly excluded by intent.
     * This means: totalClicks - deviceclass1_clicks - deviceclass2_clicks ... - deviceclassX_clicks
     * = clicks of clickers that clicked a link of this mailing with any combination of more than one deviceclass
     *
     * @param companyID
     * @param mailingID
     * @param recipientsType
     * @param targetSql
     * @param startDate
     * @param endDate
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unused")
    private Map<DeviceClass, Integer> selectClicksByDeviceClass(int companyID, int mailingID, String recipientsType, String targetSql, Date startDate, Date endDate) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT r.device_class_id AS deviceClassId, COUNT(*) AS counter FROM rdirlog_" + companyID + "_tbl r");

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE r.mailing_id = ? AND r.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE r.mailing_id = ?");
        }

        // Exclude clicks of clickers with combinations of deviceclasses
        queryBuilder.append(" AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.device_class_id != r.device_class_id AND rdir.mailing_id = r.mailing_id AND rdir.customer_id = r.customer_id)");

        if (startDate != null && endDate != null) {
            queryBuilder.append(" AND ? <= r.timestamp AND r.timestamp <= ?");
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        }

        queryBuilder.append(" GROUP BY r.device_class_id");

        List<Map<String, Object>> result;
        if (startDate != null && endDate != null) {
            result = selectLongRunning(logger, queryBuilder.toString(), mailingID, startDate, endDate);
        } else {
            result = selectLongRunning(logger, queryBuilder.toString(), mailingID);
        }

        Map<DeviceClass, Integer> returnMap = new HashMap<>();
        // Initialize default values 0 for no clickers at all
        for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
            returnMap.put(deviceClass, 0);
        }
        for (Map<String, Object> row : result) {
            if (row.get("deviceClassId") == null) {
                // Some old entries dont't have a deviceclassid, those are desktop values
                returnMap.put(DeviceClass.DESKTOP, ((Number) row.get("counter")).intValue());
            } else {
                returnMap.put(DeviceClass.fromId(((Number) row.get("deviceClassId")).intValue()), ((Number) row.get("counter")).intValue());
            }
        }

        return returnMap;
    }

    protected int selectClicks(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) AS counter FROM rdirlog_" + companyID + "_tbl r");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_").append(companyID).append("_tbl cust");
            queryBuilder.append(" WHERE r.mailing_id = ? AND r.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE r.mailing_id = ?");
        }
        parameters.add(mailingID);

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= r.timestamp AND r.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyID).append("_binding_tbl bind WHERE bind.user_type IN ('")
                    .append(UserType.World.getTypeCode()).append("', '").append(UserType.WorldVIP.getTypeCode())
                    .append("') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyID).append("_binding_tbl bind WHERE bind.user_type IN ('")
                    .append(UserType.Admin.getTypeCode()).append("', '").append(UserType.TestUser.getTypeCode()).append("', '").append(UserType.TestVIP.getTypeCode()).append("') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("counter")).intValue();
    }

    protected int selectAnonymousClicks(int companyID, int mailingID, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) AS counter FROM rdirlog_" + companyID + "_tbl r");
        List<Object> parameters = new ArrayList<>();

        queryBuilder.append(" WHERE r.mailing_id = ? AND r.customer_id = 0");
        parameters.add(mailingID);

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= r.timestamp AND r.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray());

        return ((Number) result.get(0).get("counter")).intValue();
    }

    /**
     * Selects a map with numbers of clickers per deviceclass for a single mailingid.
     * Watch out:
     * If a customer is within one of these deviceclasses, then he hasn't clicked in the mail with any other deviceclass.
     * Mixed combination clickers are explicitly excluded by intent.
     * This means: totalClickers - deviceclass1_clickers - deviceclass2_clickers ... - deviceclassX_clickers
     * = clickers that clicked a link of this mailing with any combination of more than one deviceclass
     *
     * @param companyID
     * @param mailingID
     * @param recipientsType
     * @param targetSql
     * @param startDateString
     * @param endDateString
     * @return
     * @throws Exception
     */
    protected Map<DeviceClass, Integer> selectClickersByDeviceClassWithoutCombinations(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT r.device_class_id AS deviceClassId, COUNT(DISTINCT r.customer_id) AS counter FROM rdirlog_" + companyID + "_tbl r");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE r.mailing_id = ? AND r.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE r.mailing_id = ?");
        }

        // Exclude anonymous entries.
        queryBuilder.append(" AND r.customer_id <> 0");

        parameters.add(mailingID);

        // Exclude clickers with combinations of deviceclasses
        queryBuilder.append(" AND NOT EXISTS (SELECT 1 FROM rdirlog_" + companyID + "_tbl rdir WHERE rdir.device_class_id != r.device_class_id AND rdir.mailing_id = r.mailing_id AND rdir.customer_id = r.customer_id)");

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= r.timestamp AND r.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        }

        queryBuilder.append(" GROUP BY r.device_class_id");

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        Map<DeviceClass, Integer> returnMap = new HashMap<>();
        // Initialize default values 0 for no clickers at all
        for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
            returnMap.put(deviceClass, 0);
        }
        for (Map<String, Object> row : result) {
            if (row.get("deviceClassId") == null) {
                // Some old entries dont't have a deviceclassid, those are desktop values
                returnMap.put(DeviceClass.DESKTOP, ((Number) row.get("counter")).intValue());
            } else {
                returnMap.put(DeviceClass.fromId(((Number) row.get("deviceClassId")).intValue()), ((Number) row.get("counter")).intValue());
            }
        }

        return returnMap;
    }

    protected int selectClickers(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(DISTINCT r.customer_id) AS counter FROM rdirlog_" + companyID + "_tbl r");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_").append(companyID).append("_tbl cust");
            queryBuilder.append(" WHERE r.mailing_id = ? AND r.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE r.mailing_id = ?");
        }

        // Exclude anonymous entries.
        queryBuilder.append(" AND r.customer_id <> 0");

        parameters.add(mailingID);

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= r.timestamp AND r.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyID).append("_binding_tbl bind WHERE bind.user_type IN ('").append(UserType.World.getTypeCode()).append("', '").append(UserType.WorldVIP.getTypeCode()).append("') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_").append(companyID).append("_binding_tbl bind WHERE bind.user_type IN ('").append(UserType.Admin.getTypeCode()).append("', '").append(UserType.TestUser.getTypeCode()).append("', '").append(UserType.TestVIP.getTypeCode()).append("') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = r.mailing_id) AND bind.customer_id = r.customer_id)");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("counter")).intValue();
    }

    /**
     * Selects a map with numbers of openers per deviceclass for a single mailingid.
     * Watch out:
     * If a customer is within one of these deviceclasses, then he hasn't opened the mail with any other deviceclass.
     * Mixed combination openers are explicitly excluded by intent.
     * This means: totalOpeners - deviceclass1_openers - deviceclass2_openers ... - deviceclassX_openers
     * = openers that opened this mailing with any combination of more than one deviceclass
     *
     * @param companyID
     * @param mailingID
     * @param recipientsType
     * @param targetSql
     * @param startDateString
     * @param endDateString
     * @return
     * @throws Exception
     */
    protected Map<DeviceClass, Integer> selectOpenersByDeviceClassWithoutCombinations(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT o.device_class_id AS deviceClassId, COUNT(DISTINCT o.customer_id) AS counter FROM onepixellog_device_" + companyID + "_tbl o");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_").append(companyID).append("_tbl cust");
            queryBuilder.append(" WHERE o.mailing_id = ? AND o.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE o.mailing_id = ?");
        }

        // Exclude anonymous entries.
        queryBuilder.append(" AND o.customer_id <> 0");

        parameters.add(mailingID);

        // Exclude clickers with combinations of deviceclasses
        queryBuilder.append(" AND NOT EXISTS (SELECT 1 FROM onepixellog_device_").append(companyID).append("_tbl opl WHERE opl.device_class_id != o.device_class_id AND opl.mailing_id = o.mailing_id AND opl.customer_id = o.customer_id)");

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= o.creation AND o.creation < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = o.mailing_id) AND bind.customer_id = o.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = o.mailing_id) AND bind.customer_id = o.customer_id)");
        }

        queryBuilder.append(" GROUP BY o.device_class_id");

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        Map<DeviceClass, Integer> returnMap = new HashMap<>();
        // Initialize default values 0 for no clickers at all
        for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
            returnMap.put(deviceClass, 0);
        }
        for (Map<String, Object> row : result) {
            if (row.get("deviceClassId") == null) {
                // Some old entries dont't have a deviceclassid, those are desktop values
                returnMap.put(DeviceClass.DESKTOP, ((Number) row.get("counter")).intValue());
            } else {
                returnMap.put(DeviceClass.fromId(((Number) row.get("deviceClassId")).intValue()), ((Number) row.get("counter")).intValue());
            }
        }

        return returnMap;
    }

    protected int selectOpeners(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(DISTINCT o.customer_id) AS counter FROM onepixellog_device_" + companyID + "_tbl o");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE o.mailing_id = ? AND o.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE o.mailing_id = ?");
        }

        // Exclude anonymous entries.
        queryBuilder.append(" AND o.customer_id <> 0");

        parameters.add(mailingID);

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= o.creation AND o.creation < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = o.mailing_id) AND bind.customer_id = o.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = o.mailing_id) AND bind.customer_id = o.customer_id)");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("counter")).intValue();
    }

    protected int selectOpenings(int companyID, int mailingID, String recipientsType, String targetSql, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) AS counter FROM onepixellog_device_" + companyID + "_tbl o");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE o.mailing_id = ? AND o.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE o.mailing_id = ?");
        }
        parameters.add(mailingID);

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= o.creation AND o.creation < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = o.mailing_id) AND bind.customer_id = o.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = o.mailing_id) AND bind.customer_id = o.customer_id)");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("counter")).intValue();
    }

    protected int selectAnonymousOpenings(int companyID, int mailingID, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(*) AS counter FROM onepixellog_device_" + companyID + "_tbl o");
        List<Object> parameters = new ArrayList<>();

        queryBuilder.append(" WHERE o.mailing_id = ? AND o.customer_id = 0");
        parameters.add(mailingID);

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= o.creation AND o.creation < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray());

        return ((Number) result.get(0).get("counter")).intValue();
    }

    @SuppressWarnings("unused")
    private int getTempTableValuesByCategoryAndTargetGroupIndex(int tempTableID, String category, int targetGroupIndex) throws Exception {
        String query = "SELECT value FROM " + getTempTableName(tempTableID) + " WHERE category = ? AND targetgroup_index = ?";
        return selectEmbedded(logger, query, Integer.class, category, targetGroupIndex);
    }

    private int getTempTableValuesByCategoryAndTargetGroupId(int tempTableID, String category, int targetGroupId) throws Exception {
        String query = "SELECT value FROM " + getTempTableName(tempTableID) + " WHERE category = ? AND targetgroup_id = ?";
        int value;
        try {
            value = selectEmbedded(logger, query, Integer.class, category, targetGroupId);
        } catch (EmptyResultDataAccessException e) {
            logger.error("No data found for category: " + category + ", targetId: " + targetGroupId);
            value = 0;
        }
        return value;
    }

    private String getTargetIds(int mailingId, @VelocityCheck int companyId) throws Exception {
        String query = "SELECT target_expression FROM mailing_tbl WHERE mailing_id = ? AND company_id = ?";
        String targetExpression = selectObjectDefaultNull(logger, query, new StringRowMapper(), mailingId, companyId);
        final Pattern pattern = Pattern.compile("^.*?(\\d+)(.*)$");
        Set<Integer> targetIds = new HashSet<>();
        if (targetExpression != null) {
            Matcher matcher = pattern.matcher(targetExpression);
            while (matcher.matches()) {
                targetIds.add(Integer.parseInt(matcher.group(1)));
                targetExpression = matcher.group(2);
                matcher = pattern.matcher(targetExpression);
            }
        }
        return StringUtils.join(targetIds, ",");
    }

    private void insertIntoTempTable(int tempTableID, List<TempRow> rows) throws Exception {
        for (TempRow tempRow : rows) {
            insertIntoTempTable(tempTableID, tempRow.getCategory(), tempRow.getCategoryIndex(), tempRow.getTargetGroup(), tempRow.getTargetGroupId(), tempRow.getTargetGroupIndex(), tempRow.getValue());
        }
    }

    private void insertIntoTempTable(int tempTableID, String category, int categoryIndex, String targetgroup, int targetgroupId, int targetgroupIndex, int value) throws Exception {
        String insertSql = "INSERT INTO " + getTempTableName(tempTableID) + " (category, category_index, targetgroup, targetgroup_id, targetgroup_index, value, rate) values (?, ?, ?, ?, ?, ?, 0)";
        updateEmbedded(logger, insertSql, category, categoryIndex, targetgroup, targetgroupId, targetgroupIndex, value);
    }

    private String getUpdateResponseRateQuery(int tempTableID) {
        return "INSERT INTO " + getTempTableName(tempTableID)
                + " (category, category_index, targetgroup, targetgroup_id, targetgroup_index, rate)"
                + " SELECT ?, ?, a.targetgroup, a.targetgroup_id, a.targetgroup_index, CASE WHEN (b.value = 0) THEN 0 ELSE CAST(a.value AS DOUBLE)/b.value END"
                + " FROM " + getTempTableName(tempTableID) + " a INNER JOIN " + getTempTableName(tempTableID) + " b"
                + " ON a.category_index = " + CommonKeys.CLICKS_GROSS_INDEX + " AND b.category_index = " + CommonKeys.OPENINGS_GROSS_MEASURED_INDEX + " AND a.targetgroup_id = b.targetgroup_id";
    }

    private List<MailingSummaryRow> getResultsFromTempTable(int tempTableID) throws Exception {
        String query = "SELECT category, category_index, targetgroup_id, targetgroup, targetgroup_index, value , rate, rate_delivered "
                + "FROM " + getTempTableName(tempTableID) + " ORDER BY category_index, targetgroup_index ";
        List<MailingSummaryRow> list = selectEmbedded(logger, query, (resultSet, rowNum) -> {
            MailingSummaryRow row = new MailingSummaryRow();
            row.setCategory(resultSet.getString("category"));
            row.setCategoryindex(resultSet.getInt("category_index"));
            row.setTargetgroup(resultSet.getString("targetgroup"));
            row.setTargetgroupindex(resultSet.getInt("targetgroup_index"));
            row.setCount(resultSet.getInt("value"));
            row.setRate(resultSet.getDouble("rate"));
            row.setDeliveredRate(resultSet.getDouble("rate_delivered"));
            return row;
        });
        return list;
    }

    private String getTempTableName(int id) {
        return "tmp_report_aggregation_" + id + "_tbl";
    }

    private Map<Integer, Integer> getTargetMap(List<LightTarget> targets, int firstIndex) {
        Map<Integer, Integer> result = new HashMap<>();
        int index = firstIndex;
        for (LightTarget t : targets) {
            result.put(t.getId(), index);
            index++;
        }
        return result;
    }

    protected Map<Integer, Integer> getMeasuredFromTempTable(int tempTableId) throws Exception {
        String query = "SELECT value, targetgroup_id FROM " + getTempTableName(tempTableId) + " WHERE category_index = ?";
        Map<Integer, Integer> result = new HashMap<>();
        selectEmbedded(logger, query, (resultSet, i) -> {
            result.put(resultSet.getInt("targetgroup_id"), resultSet.getInt("value"));
            return null;
        }, CommonKeys.OPENERS_MEASURED_INDEX);
        return result;
    }

    protected double selectRevenue(int companyID, int mailingID, String targetSql, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COALESCE(SUM(r.num_parameter), 0) AS revenue FROM rdirlog_" + companyID + "_val_num_tbl r");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE r.mailing_id = ? AND r.page_tag = 'revenue' AND r.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE r.mailing_id = ? AND r.page_tag = 'revenue'");
        }
        parameters.add(mailingID);

        queryBuilder.append(" AND r.num_parameter IS NOT NULL");

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= r.timestamp AND r.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("revenue")).doubleValue();
    }

    protected int selectOptOuts(int companyID, int mailingID, String targetSql, String recipientsType, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(DISTINCT b.customer_id) AS optouts FROM customer_" + companyID + "_binding_tbl b");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE b.exit_mailing_id = ? AND b.user_status IN (3, 4) AND b.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE b.exit_mailing_id = ? AND b.user_status IN (3, 4)");
        }
        parameters.add(mailingID);

        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND b.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')");
        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND b.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "')");
        }

        queryBuilder.append(" AND b.mailinglist_id = (SELECT mail.mailinglist_id FROM mailing_tbl mail WHERE mail.mailing_id = b.exit_mailing_id)");

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= b.timestamp AND b.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("optouts")).intValue();
    }

    /**
     * Read softbounces from binding table
     *
     * @param companyID
     * @param mailingID
     * @param targetSql
     * @param recipientsType
     * @param dateFormats
     * @return
     * @throws Exception
     */
    protected int selectSoftbouncesFromBindings(int companyID, int mailingID, String targetSql, String recipientsType, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(DISTINCT b.customer_id) AS softbounces FROM customer_" + companyID + "_binding_tbl b");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE b.exit_mailing_id = ? AND b.user_remark IN ('Softbounce', 'bounce:soft') AND b.user_status = 2 AND b.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE b.exit_mailing_id = ? AND b.user_remark IN ('Softbounce', 'bounce:soft') AND b.user_status = 2");
        }
        parameters.add(mailingID);

        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND b.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')");
        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND b.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "')");
        }

        queryBuilder.append(" AND b.mailinglist_id = (SELECT mail.mailinglist_id FROM mailing_tbl mail WHERE mail.mailing_id = b.exit_mailing_id)");

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= b.timestamp AND b.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("softbounces")).intValue();
    }

    /**
     * Read hardbounces from binding table
     *
     * @param companyID
     * @param mailingID
     * @param targetSql
     * @param recipientsType
     * @param dateFormats
     * @return
     * @throws Exception
     */
    protected int selectHardbouncesFromBindings(int companyID, int mailingID, String targetSql, String recipientsType, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(DISTINCT b.customer_id) AS hardbounces FROM customer_" + companyID + "_binding_tbl b");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE b.exit_mailing_id = ? AND (b.user_remark = 'bounce' OR b.user_remark LIKE 'bounce:%') AND b.user_status = 2 AND b.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE b.exit_mailing_id = ? AND (b.user_remark = 'bounce' OR b.user_remark LIKE 'bounce:%') AND b.user_status = 2");
        }
        parameters.add(mailingID);

        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND b.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "')");
        } else if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND b.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "')");
        }

        queryBuilder.append(" AND b.mailinglist_id = (SELECT mail.mailinglist_id FROM mailing_tbl mail WHERE mail.mailing_id = b.exit_mailing_id)");

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= b.timestamp AND b.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("hardbounces")).intValue();
    }

    /**
     * Read softbounces from bounces table
     *
     * @param companyID
     * @param mailingID
     * @param targetSql
     * @param recipientsType
     * @param dateFormats
     * @return
     * @throws Exception
     */
    protected int selectSoftbouncesFromBounces(int companyID, int mailingID, String targetSql, String recipientsType, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(DISTINCT b.customer_id) AS softbounces FROM bounce_tbl b");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE b.company_id = ? AND b.mailing_id = ? AND b.detail < 510 AND b.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE b.company_id = ? AND b.mailing_id = ? AND b.detail < 510");
        }
        parameters.add(companyID);
        parameters.add(mailingID);

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = b.mailing_id) AND bind.customer_id = b.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = b.mailing_id) AND bind.customer_id = b.customer_id)");
        }

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= b.timestamp AND b.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("softbounces")).intValue();
    }

    /**
     * Read hardbounces from bounces table
     *
     * @param companyID
     * @param mailingID
     * @param targetSql
     * @param recipientsType
     * @param dateFormats
     * @return
     * @throws Exception
     */
    protected int selectHardbouncesFromBounces(int companyID, int mailingID, String targetSql, String recipientsType, String startDateString, String endDateString) throws Exception {
        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(DISTINCT b.customer_id) AS hardbounces FROM bounce_tbl b");
        List<Object> parameters = new ArrayList<>();

        if (targetSql != null && targetSql.contains("cust.")) {
            queryBuilder.append(", customer_" + companyID + "_tbl cust");
            queryBuilder.append(" WHERE b.company_id = ? AND b.mailing_id = ? AND b.detail >= 510 AND b.customer_id = cust.customer_id");
        } else {
            queryBuilder.append(" WHERE b.company_id = ? AND b.mailing_id = ? AND b.detail >= 510");
        }
        parameters.add(companyID);
        parameters.add(mailingID);

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = b.mailing_id) AND bind.customer_id = b.customer_id)");
        } else if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            queryBuilder.append(" AND EXISTS (SELECT 1 FROM customer_" + companyID + "_binding_tbl bind WHERE bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND bind.mailinglist_id = (SELECT mtbl.mailinglist_id FROM mailing_tbl mtbl WHERE mtbl.mailing_id = b.mailing_id) AND bind.customer_id = b.customer_id)");
        }

        if (StringUtils.isNotBlank(startDateString) && StringUtils.isNotBlank(endDateString)) {
            queryBuilder.append(" AND (? <= b.timestamp AND b.timestamp < ?)");
            if (startDateString.contains(":")) {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(startDateString));
            } else {
                parameters.add(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString));
            }
            if (endDateString.contains(":")) {
                parameters.add(DateUtils.addHours(new SimpleDateFormat(DATE_PARAMETER_FORMAT_WITH_HOUR).parse(endDateString), 1));
            } else {
                parameters.add(DateUtils.addDays(new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString), 1));
            }
        }

        if (targetSql != null && StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
            queryBuilder.append(" AND (").append(targetSql).append(")");
        }

        List<Map<String, Object>> result = selectLongRunning(logger, queryBuilder.toString(), parameters.toArray(new Object[0]));

        return ((Number) result.get(0).get("hardbounces")).intValue();
    }

    public static class MailingSummaryRow extends SendStatRow {

        private double deliveredRate;

        public MailingSummaryRow() {
        }

        public MailingSummaryRow(String category, int categoryindex, String targetgroup, int targetgroupindex, int count, double rate, double deliveredRate) {
            super(category, categoryindex, targetgroup, targetgroupindex, count, rate);
            this.deliveredRate = deliveredRate;
        }

        public double getDeliveredRate() {
            return deliveredRate;
        }

        public void setDeliveredRate(double deliveredRate) {
            this.deliveredRate = deliveredRate;
        }
    }
}
