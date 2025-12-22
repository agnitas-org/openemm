/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.utils.BirtReporUtils;
import com.agnitas.emm.common.UserStatus;
import com.agnitas.emm.core.commons.util.ConfigValue;
import com.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.dao.DataAccessException;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ACTIVE_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ACTIVE_STATUS;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.BLACKLISTED;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.BLACKLISTED_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.BOUNCES_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.BOUNCES_STATUS;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CONFIRMED_AND_NOT_ACTIVE_DOI;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CONFIRMED_AND_NOT_ACTIVE_DOI_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CONFIRMED_DOI;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CONFIRMED_DOI_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.NOT_CONFIRMED_AND_DELETED_DOI;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.NOT_CONFIRMED_AND_DELETED_DOI_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.NOT_CONFIRMED_DOI;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.NOT_CONFIRMED_DOI_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPT_OUTS;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPT_OUTS_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.SENT_HTML;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.SENT_HTML_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.SENT_OFFILE_HTML;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.SENT_OFFLINE_HTML_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.SENT_TEXT;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.SENT_TEXT_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.TOTAL_DOI;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.TOTAL_DOI_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.WAITING_FOR_CONFIRM;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.WAITING_FOR_CONFIRM_INDEX;

public class RecipientsStatisticDataSet extends RecipientsBasedDataSet {

    public static final String OPENERS = "report.opens";
    public static final int OPENERS_INDEX = 3;
    public static final String CLICKER = "statistic.clicker";
    public static final int CLICKER_INDEX = 6;

    public static final String OPENERS_TRACKED = "report.individual.opens.measured";
    public static final String OPENERS_PC = "report.individual.opens.pc";
    public static final String OPENERS_MOBILE = "report.individual.opens.mobile";
    public static final String OPENERS_TABLET = "report.individual.opens.tablet";
    public static final String OPENERS_SMARTTV = "report.opens.smarttv";
    public static final String OPENERS_MULTIPLE_DEVICES = "report.openers.multiple-devices";

    public static final String CLICKER_TRACKED = "statistic.clicker";
    public static final int CLICKER_TRACKED_INDEX = 19;

    public static final int ACTIVE_START_DATE_INDEX = 25;
    public static final int ACTIVE_END_DATE_INDEX = 26;
    public static final int CLICKER_END_DATE_INDEX = 27;
    public static final int OPENERS_END_DATE_INDEX = 28;
    public static final int CLICKER_TRACKED_END_DATE_INDEX = 29;
    public static final int CLICKER_PC_END_DATE_INDEX = 30;
    public static final int CLICKER_MOBILE_END_DATE_INDEX = 31;
    public static final int CLICKER_TABLET_END_DATE_INDEX = 32;
    public static final int CLICKER_SMARTTV_END_DATE_INDEX = 33;
    public static final int CLICKER_MULTIPLE_DEVICES_END_DATE_INDEX = 34;
    public static final int OPENERS_TRACKED_END_DATE_INDEX = 35;
    public static final int OPENERS_PC_END_DATE_INDEX = 36;
    public static final int OPENERS_MOBILE_END_DATE_INDEX = 37;
    public static final int OPENERS_TABLET_END_DATE_INDEX = 38;
    public static final int OPENERS_SMARTTV_END_DATE_INDEX = 39;
    public static final int OPENERS_MULTIPLE_DEVICES_END_DATE_INDEX = 40;

    public static final int DATE_CONSTRAINT_BETWEEN = 0;
    public static final int DATE_CONSTRAINT_LESS_THAN_START = 1;
    public static final int DATE_CONSTRAINT_LESS_THAN_STOP = 2;
    public static final int DATE_CONSTRAINT_GREATER_THAN_START = 3;


    private List<RecipientsStatisticCommonRow> statList = new ArrayList<>();
    private List<RecipientCollectedStatisticRow> collectedStatisticList;

    /**
     * Generates temp table name with specific ID
     */
    private static String getTempReportTableName(int tempTableId) {
        return "tmp_report_aggregation_" + tempTableId + "_tbl";
    }


    public Map<String, Integer> initRecipientsStatistic(int companyId, String selectedMailingListsAsString,
                                                        String selectedTargets, String startDateString, String stopDateString, String figuresOptions,
                                                        final String hiddenFilterTargetIdStr) throws Exception {
        try {
            List<Integer> mailingListIds = new ArrayList<>();
            for (String mailingListIdString : selectedMailingListsAsString.split(",")) {
                mailingListIds.add(Integer.parseInt(mailingListIdString));
            }

            List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);
            int tempTableId = createRecipientsStatisticTempTable();

            insertEmptyRowsIntoTempTable(companyId, tempTableId, mailingListIds, selectedTargets);

            if (figures.contains(BirtReporUtils.BirtReportFigure.MAILING_TYPE)) {
                addRecipientStatMailtype(companyId, tempTableId, mailingListIds, selectedTargets, startDateString, stopDateString, hiddenFilterTargetIdStr);
            }

            if (figures.contains(BirtReporUtils.BirtReportFigure.RECIPIENT_DOI)) {
                addRecipientStatDoi(companyId, tempTableId, mailingListIds, startDateString, stopDateString);
            }

            // this method calculate recipients count for chart "Recipient analysis by
            // target groups" which is shown always
            addRecipientStatUserStatus(companyId, tempTableId, mailingListIds, selectedTargets, startDateString, stopDateString, false, hiddenFilterTargetIdStr);

            // we need active in the end of period for crossTab
            if (figures.contains(BirtReporUtils.BirtReportFigure.RECIPIENT_STATUS)
                    || figures.contains(BirtReporUtils.BirtReportFigure.MAILING_TYPE)
                    || figures.contains(BirtReporUtils.BirtReportFigure.RECIPIENT_DEVELOPMENT_NET)
                    || figures.contains(BirtReporUtils.BirtReportFigure.ACTIVITY_ANALYSIS)
                    || figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_MEASURED)
                    || figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL)) {
                addRecipientStatUserStatus(companyId, tempTableId, mailingListIds, selectedTargets, startDateString, stopDateString, true, hiddenFilterTargetIdStr);
            }

            updateNumberOfTargetGroups(tempTableId, selectedTargets.split(",").length);

            int tempTableInfoInColumnId = prepareReport(companyId, selectedTargets, selectedMailingListsAsString,
                    startDateString, stopDateString, figuresOptions, tempTableId, hiddenFilterTargetIdStr);

            Map<String, Integer> tempTableIds = new HashMap<>();
            tempTableIds.put("tempTableInfoInRowId", tempTableId);
            tempTableIds.put("tempTableInfoInColumnId", tempTableInfoInColumnId);
            return tempTableIds;
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw e;
        }
    }

    /**
     * This method returns prepared data by temporary table id Data consists values
     * groped by mailinglists and target groups
     */
    public List<RecipientsStatisticCommonRow> getRecipientsStatistic(int tempTableId) throws Exception {
        if (statList.isEmpty()) {
            String selectMailinglistSql = "SELECT mailinglist_id, mailinglist_name, mailinglist_group_id, targetgroup_id, mailinglist_name, targetgroup_name, count_type_text, count_type_html,"
                    + " count_type_offline_html, count_active, count_active_for_period, count_waiting_for_confirm, count_blacklisted, count_optout, count_bounced, count_gender_male,"
                    + " count_gender_female, count_gender_unknown, count_recipient, count_target_group, count_active_as_of, count_blacklisted_as_of, count_optout_as_of, count_bounced_as_of,"
                    + " count_waiting_for_confirm_as_of, count_recipient_as_of, count_doi_not_confirmed, count_doi_not_confirmed_deleted, count_doi_confirmed, count_doi_confirmed_not_active, count_doi_total"
                    + " FROM " + getTempReportTableName(tempTableId)
                    + " ORDER BY mailinglist_id, targetgroup_id";

            statList = selectEmbedded(selectMailinglistSql, new RecipientsStatisticRowMapper());

            List<RecipientsStatisticCommonRow> statisticRows = new ArrayList<>(statList);
            statisticRows.sort(new RecipientsStatisticRowComparator());

            int previousMailingListId = -1;
            int currentMailingListGroupId = 0;
            for (RecipientsStatisticCommonRow row : statisticRows) {
                if (previousMailingListId != row.getMailingListId()) {
                    currentMailingListGroupId++;
                    previousMailingListId = row.getMailingListId();
                }
                row.setMailingListGroupId(currentMailingListGroupId);
            }
        }
        return statList;
    }

    public List<RecipientsStatisticRowByActivity> getRecipientStatisticByActivity(int tempTableId) throws Exception {
        List<RecipientCollectedStatisticRow> statistics = getData(tempTableId);
        List<RecipientsStatisticRowByActivity> statsByActivity = new ArrayList<>(statistics.size() * 2);

        statistics.stream()
        .filter(statRow -> statRow.getTargetgroupindex() == 1)
        .forEach(statRow -> {
            if (RecipientsStatisticDataSet.OPENERS_END_DATE_INDEX == statRow.getCategoryindex() ||
                    RecipientsStatisticDataSet.CLICKER_END_DATE_INDEX == statRow.getCategoryindex()) {

                boolean isOpeners = RecipientsStatisticDataSet.OPENERS_END_DATE_INDEX == statRow.getCategoryindex();

                RecipientsStatisticRowByActivity total = new RecipientsStatisticRowByActivity();
                total.setMailingListId(statRow.getMailingListId());
                total.setMailingListName(statRow.getMailingList());
                total.setValue(statRow.getRate());
                total.setActive(false);
                total.setOpeners(isOpeners);
                total.setSortOrder(1);

                RecipientsStatisticRowByActivity active = new RecipientsStatisticRowByActivity();
                active.setMailingListId(statRow.getMailingListId());
                active.setMailingListName(statRow.getMailingList());
                active.setValue(1 - statRow.getRate());
                active.setActive(true);
                active.setOpeners(isOpeners);
                active.setSortOrder(2);

                statsByActivity.add(total);
                statsByActivity.add(active);
            }
        });

        return statsByActivity;
    }

    public List<RecipientsStatisticRowByCategoryIndex> getRecipientDoiStatistics(int tempTableId) throws Exception {
        List<RecipientsStatisticCommonRow> statistics = getRecipientsStatistic(tempTableId);
        List<RecipientsStatisticRowByCategoryIndex> statsByMailtype = new ArrayList<>(statistics.size() * 2);

        statistics.stream()
                .filter(statRow -> statRow.getTargetGroupId() == 1)
                .forEach(statRow -> {
                    statsByMailtype.add(getRecipientDoiStatisticRow(statRow, CONFIRMED_DOI_INDEX, CONFIRMED_DOI));
                    statsByMailtype.add(getRecipientDoiStatisticRow(statRow, CONFIRMED_AND_NOT_ACTIVE_DOI_INDEX, CONFIRMED_AND_NOT_ACTIVE_DOI));
                    statsByMailtype.add(getRecipientDoiStatisticRow(statRow, NOT_CONFIRMED_DOI_INDEX, NOT_CONFIRMED_DOI));
                    statsByMailtype.add(getRecipientDoiStatisticRow(statRow, NOT_CONFIRMED_AND_DELETED_DOI_INDEX, NOT_CONFIRMED_AND_DELETED_DOI));
                    statsByMailtype.add(getRecipientDoiStatisticRow(statRow, TOTAL_DOI_INDEX, TOTAL_DOI));
                });

        return statsByMailtype;
    }

    private RecipientsStatisticRowByCategoryIndex getRecipientDoiStatisticRow(RecipientsStatisticCommonRow statRow, int categoryIndex, String categoryNameKey) {
        RecipientsStatisticRowByCategoryIndex row = new RecipientsStatisticRowByCategoryIndex();
        row.setMailingListId(statRow.getMailingListId());
        row.setMailingListName(statRow.getMailingListName());
        row.setCategoryIndex(categoryIndex);
        row.setCategoryNameKey(categoryNameKey);
        row.setMailingListGroupId(statRow.getMailingListGroupId());
        row.setSortOrder(categoryIndex == TOTAL_DOI_INDEX ? -1 : 0);
        int value = 0;

        if (CONFIRMED_DOI_INDEX == categoryIndex) {
            value = statRow.getConfirmedDoiCount();
        } else if (NOT_CONFIRMED_DOI_INDEX == categoryIndex) {
            value = statRow.getNotConfirmedDoiCount();
        } else if (NOT_CONFIRMED_AND_DELETED_DOI_INDEX == categoryIndex) {
            value = statRow.getNotConfirmedAndDeletedDoiCount();
        } else if (CONFIRMED_AND_NOT_ACTIVE_DOI_INDEX == categoryIndex) {
            value = statRow.getConfirmedAndNotActiveDoiCount();
        } else if (TOTAL_DOI_INDEX == categoryIndex) {
            value = statRow.getTotalDoiCount();
        }

        row.setValue(value);
        row.setPercent(calcPercent(value, statRow.getTotalDoiCount()));

        return row;
    }

    public List<RecipientsStatisticRowByCategoryIndex> getRecipientStatisticByMailtype(int tempTableId) throws Exception {
        List<RecipientsStatisticCommonRow> statistics = getRecipientsStatistic(tempTableId);
        List<RecipientsStatisticRowByCategoryIndex> statsByMailtype = new ArrayList<>(statistics.size() * 2);

        statistics.stream()
        .filter(statRow -> statRow.getTargetGroupId() == 1)
        .forEach(statRow -> {
            statsByMailtype.add(getRecipientStatisticRowByMailtype(statRow, SENT_HTML_INDEX, SENT_HTML));
            statsByMailtype.add(getRecipientStatisticRowByMailtype(statRow, SENT_TEXT_INDEX, SENT_TEXT));
            statsByMailtype.add(getRecipientStatisticRowByMailtype(statRow, SENT_OFFLINE_HTML_INDEX, SENT_OFFILE_HTML));
        });

        return statsByMailtype;
    }

    private RecipientsStatisticRowByCategoryIndex getRecipientStatisticRowByMailtype(RecipientsStatisticCommonRow statRow, int categoryIndex, String categoryNameKey) {
        RecipientsStatisticRowByCategoryIndex row = new RecipientsStatisticRowByCategoryIndex();
        row.setMailingListId(statRow.getMailingListId());
        row.setMailingListName(statRow.getMailingListName());
        row.setCategoryIndex(categoryIndex);
        row.setCategoryNameKey(categoryNameKey);
        row.setMailingListGroupId(statRow.getMailingListGroupId());
        row.setSortOrder(categoryIndex == SENT_HTML_INDEX ? 1 : categoryIndex == SENT_TEXT_INDEX ? 2 : 3);
        double value = 0.0;

        if (statRow.getCountActiveAsOf() != 0) {
            if (SENT_HTML_INDEX == categoryIndex) {
                value = statRow.getCountTypeHtml()/(double)statRow.getCountActiveAsOf();
            } else if (SENT_TEXT_INDEX == categoryIndex) {
                value = statRow.getCountTypeText()/(double)statRow.getCountActiveAsOf();
            } else if (SENT_OFFLINE_HTML_INDEX == categoryIndex) {
                value = statRow.getCountTypeOfflineHtml()/(double)statRow.getCountActiveAsOf();
            }
        }

        row.setValue(value);

        return row;
    }

    public List<RecipientsStatisticRowByCategoryIndex> getRecipientStatisticByStatuses(int tempTableId) throws Exception {
        List<RecipientsStatisticCommonRow> statistics = getRecipientsStatistic(tempTableId);
        List<RecipientsStatisticRowByCategoryIndex> statsByStatuses = new ArrayList<>(statistics.size() * 5);

        statistics.forEach(statRow -> {
            statsByStatuses.add(getRecipientStatisticRowByStatus(statRow, ACTIVE_INDEX, ACTIVE_STATUS));
            statsByStatuses.add(getRecipientStatisticRowByStatus(statRow, OPT_OUTS_INDEX, OPT_OUTS));
            statsByStatuses.add(getRecipientStatisticRowByStatus(statRow, BOUNCES_INDEX, BOUNCES_STATUS));
            statsByStatuses.add(getRecipientStatisticRowByStatus(statRow, WAITING_FOR_CONFIRM_INDEX, WAITING_FOR_CONFIRM));
            statsByStatuses.add(getRecipientStatisticRowByStatus(statRow, BLACKLISTED_INDEX, BLACKLISTED));
        });

        return statsByStatuses;
    }

    private RecipientsStatisticRowByCategoryIndex getRecipientStatisticRowByStatus(RecipientsStatisticCommonRow statRow, int categoryIndex, String categoryNameKey) {
        RecipientsStatisticRowByCategoryIndex row = new RecipientsStatisticRowByCategoryIndex();
        row.setMailingListId(statRow.getMailingListId());
        row.setMailingListName(statRow.getMailingListName());
        row.setCategoryIndex(categoryIndex);
        row.setCategoryNameKey(categoryNameKey);
        row.setMailingListGroupId(statRow.getMailingListGroupId());

        if (ACTIVE_INDEX == categoryIndex) {
            row.setValue(statRow.getCountActiveAsOf());
            row.setSortOrder(1);
        } else if (OPT_OUTS_INDEX == categoryIndex) {
            row.setValue(statRow.getCountOptoutAsOf());
            row.setSortOrder(2);
        } else if (BOUNCES_INDEX == categoryIndex) {
            row.setValue(statRow.getCountBouncedAsOf());
            row.setSortOrder(3);
        } else if (WAITING_FOR_CONFIRM_INDEX == categoryIndex) {
            row.setValue(statRow.getCountWaitingForConfirmAsOf());
            row.setSortOrder(4);
        } else if (BLACKLISTED_INDEX == categoryIndex) {
            row.setValue(statRow.getCountBlacklistedAsOf());
            row.setSortOrder(5);
        }

        return row;
    }

    public List<RecipientsStatisticRowByTargetGroup> getRecipientStatisticByTargetGroup(int tempTableId) throws Exception {
        List<RecipientsStatisticCommonRow> statistics = getRecipientsStatistic(tempTableId);

        return statistics.stream()
                .map(statRow -> {
                    RecipientsStatisticRowByTargetGroup row = new RecipientsStatisticRowByTargetGroup();
                    row.setMailingListId(statRow.getMailingListId());
                    row.setMailingListName(statRow.getMailingListName());
                    row.setTargetGroupId(statRow.getTargetGroupId());
                    row.setTargetGroupName(statRow.getTargetGroupName());
                    row.setValue(statRow.getCountRecipientAsOf());
                    row.setMailingListGroupId(statRow.getMailingListGroupId());
                    row.setNumOfTargetGroups(statRow.getCountTargetGroup());
                    return row;
                })
                .collect(Collectors.toList());
    }

    /**
     * Prepare report by collecting the values from different queries in one table
     *
     * @return id of the created temporary table
     **/
    private int prepareReport(int companyId, String selectedTargetsAsString,
                              String selectedMailingListsAsString, String startDateString, String endDateString, String figuresOptions,
                              int tempTableInfoInRowId, String hiddenFilterTargetStr) throws Exception {

        List<Integer> mailingListIds = new ArrayList<>();
        for (String mailingListIdString : selectedMailingListsAsString.split(",")) {
            mailingListIds.add(Integer.parseInt(mailingListIdString));
        }

        List<LightTarget> targetGroups = new ArrayList<>(Collections.singletonList(getAllSubscribersTarget()));
        targetGroups.addAll(getTargets(selectedTargetsAsString, companyId));

        Date startDate = null;
        if (StringUtils.isNotBlank(startDateString)) {
            startDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString);
        }

        Date endDate = null;
        if (StringUtils.isNotBlank(endDateString)) {
            endDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(endDateString);
        }

        List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);
        int tempTableId = createTempTable();

        // add active recipients
        addActiveRecipients(companyId, targetGroups, mailingListIds, startDateString, endDateString, tempTableId,
                tempTableInfoInRowId, figures.contains(BirtReporUtils.BirtReportFigure.RECIPIENT_DEVELOPMENT_NET), hiddenFilterTargetStr);

        if (figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL) || figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_AFTER_DEVICE)) {
            insertClickersIntoTempTable(tempTableId, companyId, targetGroups, mailingListIds, startDate, endDate,
                    figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_AFTER_DEVICE), hiddenFilterTargetStr);
        }

        if (figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_MEASURED)) {
            insertOpenersIntoTempTable(tempTableId, companyId, targetGroups, mailingListIds, startDate, endDate, hiddenFilterTargetStr);
        }

        updateAllRates(tempTableId, companyId, targetGroups, figures);

        return tempTableId;
    }

    /**
     * Get collected values by temporary table id
     *
     * @return list
     */
    public List<RecipientCollectedStatisticRow> getData(int tempTableId) throws Exception {
        if (collectedStatisticList == null) {
            String query = "SELECT mailinglist_id, mailinglist_name, category_name, category_index, targetgroup_id, targetgroup_name, targetgroup_index, value, rate"
                    + " FROM " + getTempReportTableName(tempTableId)
                    + " ORDER BY mailinglist_id, category_index, targetgroup_index";

            collectedStatisticList = selectEmbedded(query, new RecipientCollectedStatisticRowMapper());

            int previousMailingListId = -1;
            int currentMailingListGroupId = 0;
            for (RecipientCollectedStatisticRow row : collectedStatisticList) {
                if (previousMailingListId != row.getMailingListId()) {
                    currentMailingListGroupId++;
                    previousMailingListId = row.getMailingListId();
                }
                row.setMailingListGroupId(currentMailingListGroupId);
            }
        }
        return collectedStatisticList;
    }

    /**
     * create a temporary table to collect the values from different queries for the
     * report in one table
     *
     * @return id of the create temporary table
     **/
    private int createTempTable() throws Exception {
        int tempTableId = getNextTmpID();

        String createTableSQL = "CREATE TABLE " + getTempReportTableName(tempTableId)
                + " (mailinglist_id INTEGER,"
                + " mailinglist_name VARCHAR(200),"
                + " category_name VARCHAR(200),"
                + " category_index INTEGER,"
                + " targetgroup_id INTEGER,"
                + " targetgroup_name VARCHAR(200),"
                + " targetgroup_index INTEGER,"
                + " value INTEGER,"
                + " rate DOUBLE)";

        executeEmbedded(createTableSQL);

        return tempTableId;
    }

    /**
     * Create a temporary table to collect the values from different queries for the
     * report in one table
     *
     * @return id of the create temporary table
     * @throws Exception - trouble during executing statements.
     **/
    private int createRecipientsStatisticTempTable() throws Exception {
        int tempTableId = getNextTmpID();

        String createTableSQL = """
                CREATE TABLE %s
                (
                    mailinglist_id                  INTEGER,
                    mailinglist_group_id            INTEGER,
                    mailinglist_name                VARCHAR(200),
                    category_name                   VARCHAR(200),
                    category_index                  INTEGER,
                    targetgroup_id                  INTEGER,
                    targetgroup_name                VARCHAR(200),
                    targetgroup_index               INTEGER,
                    count_doi_not_confirmed         INTEGER,
                    count_doi_not_confirmed_deleted INTEGER,
                    count_doi_confirmed             INTEGER,
                    count_doi_confirmed_not_active  INTEGER,
                    count_doi_total                 INTEGER,
                    count_type_text                 INTEGER,
                    count_type_html                 INTEGER,
                    count_type_offline_html         INTEGER,
                    count_active                    INTEGER,
                    count_active_for_period         INTEGER,
                    count_waiting_for_confirm       INTEGER,
                    count_blacklisted               INTEGER,
                    count_optout                    INTEGER,
                    count_bounced                   INTEGER,
                    count_gender_male               INTEGER,
                    count_gender_female             INTEGER,
                    count_gender_unknown            INTEGER,
                    count_recipient                 INTEGER,
                    count_target_group              INTEGER,
                    count_active_as_of              INTEGER,
                    count_blacklisted_as_of         INTEGER,
                    count_optout_as_of              INTEGER,
                    count_bounced_as_of             INTEGER,
                    count_waiting_for_confirm_as_of INTEGER,
                    count_recipient_as_of           INTEGER
                )
                """.formatted(getTempReportTableName(tempTableId));

        executeEmbedded(createTableSQL);

        return tempTableId;
    }

    /**
     * Collect clickers recipients values
     */
    private void insertClickersIntoTempTable(int tempTableId, int companyId, List<LightTarget> targetGroups,
                                             List<Integer> mailingListIds, Date startDate, Date endDate, boolean calculateDeviceClasses,
                                             String hiddenFilterTargetStr) throws Exception {
        String insertEmbedded = getTempInsertQuery(tempTableId);

        for (int mailingListId : mailingListIds) {
            String mailinglistName = getMailinglistName(companyId, mailingListId);
            int targetgroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
            for (LightTarget target : targetGroups) {
                target = getDefaultTarget(target);

                final String filterTargetSql = getHiddenTargetSql(companyId, target, hiddenFilterTargetStr);

                if (target.getId() == CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID) {
                    targetgroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
                }

                String targetSql = target.getTargetSQL();
                boolean useTargetSql = false;
                if (StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1")) {
                    useTargetSql = true;
                }

                // Unique deviceclass clickers
                Map<DeviceClass, Integer> uniqueDeviceClassClickers = getUniqueDeviceClassClickers(companyId, mailingListId,
                        startDate, endDate, targetSql, useTargetSql, filterTargetSql);

                // Overall clickers
                int overallClickers = getOverallClickersCounter(companyId, mailingListId, startDate, endDate, targetSql,
                        useTargetSql, filterTargetSql);

                // Others
                int multipleDeviceClassClickers = overallClickers - uniqueDeviceClassClickers.values().stream().mapToInt(Integer::intValue).sum();

                if (calculateDeviceClasses) {
                    updateEmbedded(insertEmbedded, mailingListId, mailinglistName, CommonKeys.CLICKER_PC, CLICKER_PC_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassClickers.get(DeviceClass.DESKTOP));
                    updateEmbedded(insertEmbedded, mailingListId, mailinglistName, CommonKeys.CLICKER_MOBILE, CLICKER_MOBILE_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassClickers.get(DeviceClass.MOBILE));
                    updateEmbedded(insertEmbedded, mailingListId, mailinglistName, CommonKeys.CLICKER_TABLET, CLICKER_TABLET_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassClickers.get(DeviceClass.TABLET));
                    updateEmbedded(insertEmbedded, mailingListId, mailinglistName, CommonKeys.CLICKER_SMARTTV, CLICKER_SMARTTV_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, uniqueDeviceClassClickers.get(DeviceClass.SMARTTV));
                    updateEmbedded(insertEmbedded, mailingListId, mailinglistName, CommonKeys.CLICKER_PC_AND_MOBILE, CLICKER_MULTIPLE_DEVICES_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, multipleDeviceClassClickers);
                }

                updateEmbedded(insertEmbedded, mailingListId, mailinglistName, CLICKER, CLICKER_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, overallClickers);
                updateEmbedded(insertEmbedded, mailingListId, mailinglistName, CLICKER_TRACKED, CLICKER_TRACKED_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, overallClickers);

                targetgroupIndex++;
            }
        }
    }

    private Map<DeviceClass, Integer> getUniqueDeviceClassClickers(int companyId, int mailingListId, Date startDate,
                                                                   Date endDate, String targetSql, boolean useTargetSql, final String filterTargetSql) throws Exception {
        StringBuilder queryClickersPerUniqueDevices = new StringBuilder("SELECT r.device_class_id AS deviceClassId, COUNT(DISTINCT r.customer_id) AS counter FROM " + getRdirLogTableName(companyId) + " r ");
        List<Object> paramsClickersPerUniqueDevices = new ArrayList<>();

        if (useTargetSql && targetSql.contains("cust.") || StringUtils.isNotBlank(filterTargetSql)) {
            queryClickersPerUniqueDevices.append(" JOIN ").append(getCustomerTableName(companyId)).append(" cust ON r.customer_id = cust.customer_id");
        }

        queryClickersPerUniqueDevices.append(" WHERE r.mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND mailinglist_id = ?)");
        paramsClickersPerUniqueDevices.add(companyId);
        paramsClickersPerUniqueDevices.add(mailingListId);

        queryClickersPerUniqueDevices.append(" AND r.customer_id != 0");

        // Exclude clicks of clickers with combinations of deviceclasses
        queryClickersPerUniqueDevices.append(" AND NOT EXISTS (SELECT 1 FROM ").append(getRdirLogTableName(companyId)).append(" rdir ")
                .append(" WHERE rdir.device_class_id != r.device_class_id AND rdir.customer_id = r.customer_id)");

        if (startDate != null) {
            queryClickersPerUniqueDevices.append(" AND ? <= r.timestamp");
            paramsClickersPerUniqueDevices.add(startDate);
        }

        if (endDate != null) {
            queryClickersPerUniqueDevices.append(" AND r.timestamp <= ?");
            paramsClickersPerUniqueDevices.add(endDate);
        }

        if (useTargetSql) {
            queryClickersPerUniqueDevices.append(" AND (").append(targetSql).append(")");
        }

        if (StringUtils.isNotBlank(filterTargetSql)) {
            queryClickersPerUniqueDevices.append(" AND (").append(filterTargetSql).append(")");
        }

        queryClickersPerUniqueDevices.append(" GROUP BY r.device_class_id");

        List<Map<String, Object>> result = selectLongRunning(queryClickersPerUniqueDevices.toString(), paramsClickersPerUniqueDevices.toArray(new Object[0]));

        Map<DeviceClass, Integer> uniqueDeviceClassClickers = new HashMap<>();
        // Initialize default values 0 for no clickers at all
        for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
            uniqueDeviceClassClickers.put(deviceClass, 0);
        }

        mapDeviceClass(result, uniqueDeviceClassClickers);
        return uniqueDeviceClassClickers;
    }

    private int getOverallClickersCounter(int companyId, int mailingListId, Date startDate, Date endDate,
                                          String targetSql, boolean useTargetSql, final String filterTargetSql) throws Exception {
        StringBuilder queryOverallClickers = new StringBuilder("SELECT COUNT(DISTINCT r.customer_id) AS counter FROM ").append(getRdirLogTableName(companyId)).append(" r");
        List<Object> paramsOverallClickers = new ArrayList<>();

        if ((useTargetSql && targetSql.contains("cust."))
                || (StringUtils.isNotBlank(filterTargetSql) && filterTargetSql.contains("cust."))) {
            queryOverallClickers.append(" JOIN ").append(getCustomerTableName(companyId)).append(" cust ON r.customer_id = cust.customer_id");
        }

        queryOverallClickers.append(" WHERE r.mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND mailinglist_id = ?)");
        paramsOverallClickers.add(companyId);
        paramsOverallClickers.add(mailingListId);

        queryOverallClickers.append(" AND r.customer_id != 0");

        if (startDate != null) {
            queryOverallClickers.append(" AND ? <= r.timestamp");
            paramsOverallClickers.add(startDate);
        }

        if (endDate != null) {
            queryOverallClickers.append(" AND r.timestamp <= ?");
            paramsOverallClickers.add(endDate);
        }

        if (useTargetSql) {
            queryOverallClickers.append(" AND (").append(targetSql).append(")");
        }

        if (StringUtils.isNotBlank(filterTargetSql)) {
            queryOverallClickers.append(" AND (").append(filterTargetSql).append(")");
        }

        List<Map<String, Object>> result = selectLongRunning(queryOverallClickers.toString(), paramsOverallClickers.toArray(new Object[0]));

        return toInt(result.get(0).get("counter"));
    }

    private void insertOpenersIntoTempTable(int tempTableId, int companyId, List<LightTarget> targetGroups,
                                            List<Integer> mailingListIds, Date startDate, Date endDate,
                                            String hiddenFilterTargetStr) throws Exception {
        String insertEmbedded = getTempInsertQuery(tempTableId);

        for (int mailingListId : mailingListIds) {
            String mailinglistName = getMailinglistName(companyId, mailingListId);
            int targetgroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
            for (LightTarget target : targetGroups) {
                target = getDefaultTarget(target);

                final String hiddenTargetSql = getHiddenTargetSql(companyId, target, hiddenFilterTargetStr);

                String targetSql = target.getTargetSQL();
                boolean useTargetSql = StringUtils.isNotBlank(targetSql) && !targetSql.replace(" ", "").equals("1=1");

                // Overall openers
                StringBuilder queryOverallOpeners = new StringBuilder("SELECT COUNT(DISTINCT o.customer_id) AS counter FROM " + getOnepixelDeviceTableName(companyId) + " o");
                List<Object> queryOverallOpenersParameters = new ArrayList<>();

                final boolean addJoinWithCustomerTable = (useTargetSql && StringUtils.isNotBlank(targetSql) && targetSql.contains("cust."))
                        || (StringUtils.isNotBlank(hiddenTargetSql) && hiddenTargetSql.contains("cust."));

                if (addJoinWithCustomerTable) {
                    queryOverallOpeners.append(" JOIN ").append(getCustomerTableName(companyId)).append(" cust ON o.customer_id = cust.customer_id");
                }

                queryOverallOpeners.append(" WHERE o.mailing_id IN (SELECT mailing_id FROM mailing_tbl WHERE company_id = ? AND mailinglist_id = ?)");
                queryOverallOpenersParameters.add(companyId);
                queryOverallOpenersParameters.add(mailingListId);

                queryOverallOpeners.append(" AND o.customer_id != 0");

                if (startDate != null) {
                    queryOverallOpeners.append(" AND ? <= o.creation");
                    queryOverallOpenersParameters.add(startDate);
                }

                if (endDate != null) {
                    queryOverallOpeners.append(" AND o.creation <= ?");
                    queryOverallOpenersParameters.add(endDate);
                }

                if (useTargetSql) {
                    queryOverallOpeners.append(" AND (").append(targetSql).append(")");
                }

                if (StringUtils.isNotBlank(hiddenTargetSql)) {
                    queryOverallOpeners.append(" AND (").append(hiddenTargetSql).append(")");
                }

                List<Map<String, Object>> resultOverallOpeners = selectLongRunning(queryOverallOpeners.toString(), queryOverallOpenersParameters.toArray(new Object[0]));
                int overallOpeners = toInt(resultOverallOpeners.get(0).get("counter"));

                updateEmbedded(insertEmbedded, mailingListId, mailinglistName, OPENERS, OPENERS_END_DATE_INDEX, target.getName(), target.getId(), targetgroupIndex, overallOpeners);

                targetgroupIndex++;
            }
        }
    }

    private void mapDeviceClass(List<Map<String, Object>> result, Map<DeviceClass, Integer> uniqueDeviceClassOpeners) {
        for (Map<String, Object> row : result) {
            DeviceClass deviceClass;
            int deviceClassId = toInt(row.get("deviceClassId"));
            deviceClass = DeviceClass.fromIdWithDefault(deviceClassId, DeviceClass.DESKTOP);
            uniqueDeviceClassOpeners.put(deviceClass, toInt(row.get("counter")) + uniqueDeviceClassOpeners.get(deviceClass));
        }
    }

    /**
     * Calculate rates for collected calues
     *
     * @param targetGroups list of selected target groups
     * @param figures      report checkbox parameters
     */
    private void updateAllRates(int tempTableId, int companyId, List<LightTarget> targetGroups, List<BirtReporUtils.BirtReportFigure> figures) throws Exception {
        if (figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL)
                || figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_AFTER_DEVICE)) {
            updateRates(tempTableId, companyId, targetGroups,
                    new Object[]{CommonKeys.ALL_SUBSCRIBERS_INDEX, CLICKER_TRACKED_END_DATE_INDEX},
                    new Integer[]{CLICKER_TRACKED_END_DATE_INDEX, CLICKER_PC_END_DATE_INDEX,
                            CLICKER_MOBILE_END_DATE_INDEX, CLICKER_TABLET_END_DATE_INDEX,
                            CLICKER_SMARTTV_END_DATE_INDEX, CLICKER_MULTIPLE_DEVICES_END_DATE_INDEX});
        }

        if (figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_MEASURED)
                || figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_AFTER_DEVICE)) {
            updateRates(tempTableId, companyId, targetGroups,
                    new Object[]{CommonKeys.ALL_SUBSCRIBERS_INDEX, OPENERS_TRACKED_END_DATE_INDEX},
                    new Integer[]{OPENERS_TRACKED_END_DATE_INDEX, OPENERS_PC_END_DATE_INDEX,
                            OPENERS_MOBILE_END_DATE_INDEX, OPENERS_TABLET_END_DATE_INDEX,
                            OPENERS_SMARTTV_END_DATE_INDEX, OPENERS_MULTIPLE_DEVICES_END_DATE_INDEX});
        }

        updateRates(tempTableId, companyId, targetGroups,
                new Object[]{CommonKeys.ALL_SUBSCRIBERS_INDEX, ACTIVE_INDEX},
                new Integer[]{ ACTIVE_INDEX, OPENERS_INDEX, CLICKER_INDEX});

        updateRates(tempTableId, companyId, targetGroups,
                new Object[]{CommonKeys.ALL_SUBSCRIBERS_INDEX, ACTIVE_END_DATE_INDEX},
                new Integer[]{ACTIVE_END_DATE_INDEX, OPENERS_END_DATE_INDEX, CLICKER_END_DATE_INDEX});
    }

    private void updateRates(int tempTableId, int companyId, List<LightTarget> targetGroups, Object[] mainIndexes, Integer[] indexes) throws Exception {
        String queryTotal = "SELECT mailinglist_id, value FROM " + getTempReportTableName(tempTableId) + " WHERE targetgroup_index = ? AND category_index = ?";
        List<Map<String, Object>> results = selectEmbedded(queryTotal, mainIndexes);
        for (Map<String, Object> row : results) {
            Integer mailingListId = row.get("mailinglist_id") != null ? toInt(row.get("mailinglist_id")) : 0;
            Integer total = row.get("value") != null ? toInt(row.get("value")) : 0;
            if (total > 0) {
                String queryUpdateRate = "UPDATE " + getTempReportTableName(tempTableId) +
                        " SET rate = (value * 1.0) / ?  WHERE targetgroup_index = ? AND mailinglist_id = ? AND category_index IN (" +
                        StringUtils.join(indexes, ", ") +
                        ")";
                updateEmbedded(queryUpdateRate, total, CommonKeys.ALL_SUBSCRIBERS_INDEX, mailingListId);

                if (!CollectionUtils.isEmpty(targetGroups) && isMailingTrackingActivated(companyId)) {
                    int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
                    for (int index = 0; index < targetGroups.size(); index++) {
                        try {
                            ++targetGroupIndex;
                            updateEmbedded(queryUpdateRate, total, targetGroupIndex, mailingListId);
                        } catch (DataAccessException e) {
                            logger.error("No target group data");
                        }
                    }
                }
            }
        }
    }

    private void addRecipientStatDoi(int companyId, int tempTableId, List<Integer> mailingListIds, String startDateStr, String stopDateStr) throws Exception {
        Date startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDateStr);
        Date endDate = DateUtils.addDays(new SimpleDateFormat("yyyy-MM-dd").parse(stopDateStr), 1);

        for (int mailinglistId : mailingListIds) {
            int totalCount = getTotalDoiCount(companyId, mailinglistId, startDate, endDate);
            int notConfirmedCount = getNotConfirmedDoiCount(companyId, mailinglistId, startDate, endDate);
            int notConfirmedAndDeletedCount = getNotConfirmedAndDeletedDoiCount(companyId, mailinglistId, startDate, endDate);
            int confirmedCount = getConfirmedDoiCount(companyId, mailinglistId, startDate, endDate);
            int confirmedAndNotActiveCount = getConfirmedAndNotActiveDoiCount(companyId, mailinglistId, startDate, endDate);

            String updateQuery = "UPDATE " + getTempReportTableName(tempTableId) +
                    " SET count_doi_not_confirmed = ?, count_doi_not_confirmed_deleted = ?, count_doi_confirmed = ?, count_doi_confirmed_not_active = ?, count_doi_total = ?" +
                    " WHERE mailinglist_id = ?";
            updateEmbedded(updateQuery, notConfirmedCount, notConfirmedAndDeletedCount, confirmedCount, confirmedAndNotActiveCount, totalCount, mailinglistId);
        }
    }

    private void addRecipientStatMailtype(int companyId, int tempTableId, List<Integer> mailingListIds,
                                          String selectedTargets, String startDate, String stopDate, String hiddenFilterTargetStr) throws Exception {
        for (int mailinglistId : mailingListIds) {
            // All subscribers
            addRecipientStatMailtype(companyId, tempTableId, mailinglistId, null, startDate, stopDate, hiddenFilterTargetStr);

            // insert data for each target group
            List<LightTarget> targets = getTargets(selectedTargets, companyId);
            for (LightTarget target : targets) {
                addRecipientStatMailtype(companyId, tempTableId, mailinglistId, target, startDate, stopDate, hiddenFilterTargetStr);
            }
        }
    }

    private void addRecipientStatMailtype(int companyId, int tempTableId, int mailinglistId,
                                          LightTarget target, String startDate, String stopDate, String hiddenFilterTargetStr) throws Exception {
        target = getDefaultTarget(target);
        final String hiddenTargetSql = getHiddenTargetSql(companyId, target, hiddenFilterTargetStr);


        StringBuilder recipientStatMailtypeTempate = new StringBuilder("SELECT COUNT(DISTINCT cust.customer_id) AS mailtype_count, cust.mailtype AS mailtype")
                .append(" FROM ").append(getCustomerBindingTableName(companyId)).append(" bind")
                .append(" JOIN ").append(getCustomerTableName(companyId)).append(" cust ON (bind.customer_id = cust.customer_id)")
                .append(" WHERE bind.user_status = ").append(UserStatus.Active.getStatusCode())
                .append(" AND bind.mailinglist_id = ").append(mailinglistId);

        if (StringUtils.isNotBlank(target.getTargetSQL())) {
            recipientStatMailtypeTempate.append(" AND (").append(target.getTargetSQL()).append(")");
        }

        if (StringUtils.isNotBlank(hiddenTargetSql)) {
            recipientStatMailtypeTempate.append(" AND (").append(hiddenTargetSql).append(")");
        }

        // add date constraint
        recipientStatMailtypeTempate.append(getDateConstraint("bind.timestamp", startDate, stopDate, DATE_CONSTRAINT_LESS_THAN_STOP));

        recipientStatMailtypeTempate.append(" GROUP BY cust.mailtype");

        List<Map<String, Object>> result = select(recipientStatMailtypeTempate.toString());
        int countTypeText = 0;
        int countTypeHtml = 0;
        int countTypeOfflineHtml = 0;
        for (Map<String, Object> row : result) {
            int count = toInt(row.get("mailtype_count"));
            switch (MailType.getFromInt(toInt(row.get("mailtype")))) {
                case TEXT:
                    countTypeText = count;
                    break;
                case HTML:
                    countTypeHtml = count;
                    break;
                case HTML_OFFLINE:
                    countTypeOfflineHtml = count;
                    break;
                default:
                    throw new Exception("Invalid MailType");
            }
        }

        String updateQuery = "UPDATE " + getTempReportTableName(tempTableId) +
                " SET count_type_text = ?, count_type_html = ?, count_type_offline_html = ?" +
                " WHERE mailinglist_id = ? AND targetgroup_id = ?";
        updateEmbedded(updateQuery, countTypeText, countTypeHtml, countTypeOfflineHtml, mailinglistId, target.getId());
    }

    private void addRecipientStatUserStatus(int companyId, int tempTableId, List<Integer> mailingListIds,
                                            String selectedTargets, String startDate, String stopDate, boolean isAsOf,
                                            String hiddenFilterTargetStr) throws Exception {
        for (int mailinglistId : mailingListIds) {
            // All subscribers
            insertRecipientStatUserStatus(companyId, tempTableId, mailinglistId, null, startDate, stopDate, isAsOf, hiddenFilterTargetStr);

            // insert data for each target group
            List<LightTarget> targets = getTargets(selectedTargets, companyId);
            if (targets != null) {
                for (LightTarget target : targets) {
                    insertRecipientStatUserStatus(companyId, tempTableId, mailinglistId, target, startDate, stopDate, isAsOf, hiddenFilterTargetStr);
                }
            }
        }
    }

    private void insertRecipientStatUserStatus(int companyId, int tempTableId, int mailinglistId,
                                               LightTarget target, String startDate, String stopDate, boolean isAsOf, String hiddenFilterTargetStr)
            throws Exception {
        target = getDefaultTarget(target);
        final String hiddenTargetSql = getHiddenTargetSql(companyId, target, hiddenFilterTargetStr);

        StringBuilder recipientStatUserStatusTemplate = new StringBuilder("SELECT COUNT(*) AS status_count, bind.user_status AS user_status")
                .append(" FROM ").append(getCustomerBindingTableName(companyId)).append(" bind");

        if (StringUtils.isNotBlank(target.getTargetSQL()) || StringUtils.isNotBlank(hiddenTargetSql)) {
            recipientStatUserStatusTemplate.append(" JOIN ").append(getCustomerTableName(companyId)).append(" cust ON (bind.customer_id = cust.customer_id)");
        }

        recipientStatUserStatusTemplate.append(" WHERE bind.mailinglist_id = ").append(mailinglistId);

        if (StringUtils.isNotBlank(target.getTargetSQL())) {
            recipientStatUserStatusTemplate.append(" AND (").append(target.getTargetSQL()).append(")");
        }

        if (StringUtils.isNotBlank(hiddenTargetSql)) {
            recipientStatUserStatusTemplate.append(" AND (").append(hiddenTargetSql).append(")");
        }

        // add date constraint
        recipientStatUserStatusTemplate.append(getDateConstraint("bind.timestamp", startDate, stopDate, (isAsOf ? DATE_CONSTRAINT_LESS_THAN_STOP : DATE_CONSTRAINT_BETWEEN)));

        recipientStatUserStatusTemplate.append(" GROUP BY bind.user_status");

        int activeCountForPeriod = 0;
        int waitingForConfirmCount = 0;
        int blacklistedCount = 0;
        int optoutCount = 0;
        int bouncedCount = 0;
        int recipientCount = 0;

        List<Map<String, Object>> result = select(recipientStatUserStatusTemplate.toString());
        for (Map<String, Object> row : result) {
            int amount = toInt(row.get("status_count"));
            UserStatus status = getUserStatus(toInt(row.get("user_status")));

            if (status != null) {
                switch (status) {
                    case Active:
                        activeCountForPeriod += amount;
                        break;

                    case Bounce:
                        bouncedCount += amount;
                        break;

                    case Blacklisted:
                        blacklistedCount += amount;
                        break;

                    case AdminOut, UserOut:
                        optoutCount += amount;
                        break;

                    case WaitForConfirm:
                        waitingForConfirmCount += amount;
                        break;

                    default:
                        // do nothing
                }
            }

            recipientCount += amount;
        }

        if (!isAsOf && getConfigService().getBooleanValue(ConfigValue.UseBindingHistoryForRecipientStatistics, companyId)) {
            List<Map<String, Object>> hstResult = select(recipientStatUserStatusTemplate.toString().replace(getCustomerBindingTableName(companyId), getHstCustomerBindingTableName(companyId)));
            for (Map<String, Object> row : hstResult) {
                int amount = toInt(row.get("status_count"));
                UserStatus status = getUserStatus(toInt(row.get("user_status")));

                if (status != null) {
                    switch (status) {
                        case Active:
                            activeCountForPeriod += amount;
                            break;

                        case Bounce:
                            bouncedCount += amount;
                            break;

                        case Blacklisted:
                            blacklistedCount += amount;
                            break;

                        case AdminOut, UserOut:
                            optoutCount += amount;
                            break;

                        case WaitForConfirm:
                            waitingForConfirmCount += amount;
                            break;

                        default:
                            // do nothing
                    }
                }
            }
        }

        StringBuilder update = new StringBuilder("UPDATE ").append(getTempReportTableName(tempTableId));
        if (isAsOf) {
            update.append(" SET count_active_as_of = ?, count_waiting_for_confirm_as_of = ?, count_blacklisted_as_of = ?, count_optout_as_of = ?, count_bounced_as_of = ?, count_recipient_as_of = ?");
        } else {
            update.append(" SET count_active_for_period = ?, count_waiting_for_confirm = ?, count_blacklisted = ?, count_optout = ?, count_bounced = ?, count_recipient = ?");
        }
        update.append(" WHERE mailinglist_id = ? AND targetgroup_id = ?");

        updateEmbedded(update.toString(), activeCountForPeriod, waitingForConfirmCount, blacklistedCount, optoutCount, bouncedCount, recipientCount, mailinglistId, target.getId());
    }

    private String getDateConstraint(String fieldName, String startDate, String stopDate, int constraintType) throws Exception {
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            // validate date format
            switch (constraintType) {
                case DATE_CONSTRAINT_BETWEEN:
                    format.parse(startDate);
                    format.parse(stopDate);
                    break;
                case DATE_CONSTRAINT_LESS_THAN_START, DATE_CONSTRAINT_GREATER_THAN_START:
                    format.parse(startDate);
                    break;
                case DATE_CONSTRAINT_LESS_THAN_STOP:
                    format.parse(stopDate);
                    break;
                default:
                    //should never happen
                    throw new Exception("Can not parse startDate(" + startDate + ") or endDate(" + stopDate + ")");
            }
        } catch (ParseException e) {
            throw new Exception("Can not parse startDate(" + startDate + ") or endDate(" + stopDate + ")", e);
        }

        if (isOracleDB()) {
            switch (constraintType) {
                case DATE_CONSTRAINT_BETWEEN:
                    return " AND " + fieldName + " BETWEEN TO_DATE('" + startDate + "', 'yyyy-mm-dd')" +
                            " AND (TO_DATE('" + stopDate + "', 'yyyy-mm-dd') + 1) ";
                case DATE_CONSTRAINT_LESS_THAN_START:
                    return " AND " + fieldName + " < (TO_DATE('" + startDate + "', 'yyyy-mm-dd') + 1)";
                case DATE_CONSTRAINT_GREATER_THAN_START:
                    return " AND " + fieldName + " > (TO_DATE('" + startDate + "', 'yyyy-mm-dd') + 1)";
                case DATE_CONSTRAINT_LESS_THAN_STOP:
                    return " AND " + fieldName + " < (TO_DATE('" + stopDate + "', 'yyyy-mm-dd') + 1)";
                default:
                    throw new Exception("Can not create DateConstraint");
            }
        } else if (isPostgreSQL()) {
            return switch (constraintType) {
                case DATE_CONSTRAINT_BETWEEN ->
                        " AND %s BETWEEN TO_DATE('%s', 'YYYY-MM-DD') AND TO_DATE('%s', 'YYYY-MM-DD') + INTERVAL '1 DAY' "
                                .formatted(fieldName, startDate, stopDate);
                case DATE_CONSTRAINT_LESS_THAN_START ->
                        " AND %s < TO_DATE('%s', 'YYYY-MM-DD') + INTERVAL '1 DAY'".formatted(fieldName, startDate);
                case DATE_CONSTRAINT_GREATER_THAN_START ->
                        " AND %s > TO_DATE('%s', 'YYYY-MM-DD') + INTERVAL '1 DAY'".formatted(fieldName, startDate);
                case DATE_CONSTRAINT_LESS_THAN_STOP ->
                        " AND %s < TO_DATE('%s', 'YYYY-MM-DD') + INTERVAL '1 DAY'".formatted(fieldName, stopDate);
                default -> throw new Exception("Can not create DateConstraint");
            };
        } else {
            switch (constraintType) {
                case DATE_CONSTRAINT_BETWEEN:
                    return " AND " + fieldName + " BETWEEN STR_TO_DATE('" + startDate + "', '%Y-%m-%d')" +
                            " AND STR_TO_DATE('" + stopDate + "', '%Y-%m-%d') + INTERVAL 1 DAY ";
                case DATE_CONSTRAINT_LESS_THAN_START:
                    return " AND " + fieldName + " < STR_TO_DATE('" + startDate + "', '%Y-%m-%d') + INTERVAL 1 DAY";
                case DATE_CONSTRAINT_GREATER_THAN_START:
                    return " AND " + fieldName + " > STR_TO_DATE('" + startDate + "', '%Y-%m-%d') + INTERVAL 1 DAY";
                case DATE_CONSTRAINT_LESS_THAN_STOP:
                    return " AND " + fieldName + " < STR_TO_DATE('" + stopDate + "', '%Y-%m-%d') + INTERVAL 1 DAY";
                default:
                    throw new Exception("Can not create DateConstraint");
            }
        }
    }

    private void insertEmptyRowsIntoTempTable(int companyId, int tempTableId, List<Integer> mailingListIds, String selectedTargets) throws Exception {
        String tempRecipientsStatRowInsert = "INSERT INTO " + getTempReportTableName(tempTableId) +
                " (mailinglist_id, targetgroup_id, mailinglist_name, targetgroup_name)" +
                " VALUES (?, ?, ?, ?)";

        for (int mailinglistId : mailingListIds) {
            // All subscribers
            String mailinglistName = getMailinglistName(companyId, mailinglistId);
            updateEmbedded(tempRecipientsStatRowInsert,
                    mailinglistId,
                    CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID,
                    mailinglistName,
                    CommonKeys.ALL_SUBSCRIBERS);

            // insert data for each target group
            // we need just insert empty rows
            List<LightTarget> targets = getTargets(selectedTargets, companyId);
            for (LightTarget target : targets) {
                target = getDefaultTarget(target);
                updateEmbedded(tempRecipientsStatRowInsert, mailinglistId, target.getId(),
                        mailinglistName, target.getName());
            }
        }
    }

    private void addActiveRecipients(int companyId, List<LightTarget> targetGroups, List<Integer> mailingListIds,
                                     String startDateString, String endDateString, int tempTableId, int tempTableInfoInRowId,
                                     boolean calculateRecipientDevelopmentNet, String hiddenFilterTargetStr) throws Exception {
        // we expect that list has already sorted by mailinglistId and targetgroupId
        List<RecipientsStatisticCommonRow> statisticRows = getRecipientsStatistic(tempTableInfoInRowId);
        int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
        int previousMailinglistId = -1;
        for (RecipientsStatisticCommonRow row : statisticRows) {

            int targetGroupId = row.getTargetGroupId() > 0 ? row.getTargetGroupId() : CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;
            String targetGroupName = row.getTargetGroupId() > 0 ? row.getTargetGroupName() : CommonKeys.ALL_SUBSCRIBERS;

            if (row.getMailingListId() != previousMailinglistId) {
                previousMailinglistId = row.getMailingListId();
                targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX;
            }

            String insertEmbedded = getTempInsertQuery(tempTableId);
            int mailinglistId = row.getMailingListId();
            String mailinglistName = row.getMailingListName();

            // active recipients for period
            updateEmbedded(insertEmbedded,
                    mailinglistId, mailinglistName, CommonKeys.ACTIVE, ACTIVE_INDEX,
                    targetGroupName, targetGroupId,
                    targetGroupIndex, row.getCountActiveForPeriod());

            // active recipients in the end of period
            updateEmbedded(insertEmbedded,
                    mailinglistId, mailinglistName, endDateString, ACTIVE_END_DATE_INDEX,
                    targetGroupName, targetGroupId, targetGroupIndex, row.getCountActiveAsOf());

            if (calculateRecipientDevelopmentNet) {
                // active recipients in the start of period
                for (int mlID : mailingListIds) {
                    String mlName = getMailinglistName(companyId, mlID);
                    // All subscribers
                    insertRecipientStat(companyId, tempTableId, mlID, mlName, null, CommonKeys.ALL_SUBSCRIBERS_INDEX,
                            startDateString, endDateString, hiddenFilterTargetStr);

                    // insert data for each target group
                    for (LightTarget target : targetGroups) {
                        insertRecipientStat(companyId, tempTableId, mlID, mlName, target, targetGroupIndex, startDateString,
                                endDateString, hiddenFilterTargetStr);
                    }
                }
            }

            targetGroupIndex++;
        }
    }

    private void insertRecipientStat(int companyId, int tempTableId, int mailinglistId,
                                     String mailinglistName, LightTarget target, int targetGroupIndex, String startDateString,
                                     String endDateString, String hiddenFilterTargetStr) throws Exception {
        StringBuilder recipientActiveSql = new StringBuilder("SELECT COUNT(*) AS active_count")
                .append(" FROM  ").append(getCustomerBindingTableName(companyId)).append(" bind")
                .append(" LEFT JOIN ").append(getCustomerTableName(companyId)).append(" cust ON (bind.customer_id = cust.customer_id)")
                .append(" WHERE bind.user_status = ").append(UserStatus.Active.getStatusCode())
                .append(" AND bind.mailinglist_id = ").append(mailinglistId);

        target = getDefaultTarget(target);

        final String filterTargetSql = getHiddenTargetSql(companyId, target, hiddenFilterTargetStr);

        if (StringUtils.isNotBlank(target.getTargetSQL())) {
            recipientActiveSql.append(" AND (").append(target.getTargetSQL()).append(")");
        }

        if (StringUtils.isNotBlank(filterTargetSql)) {
            recipientActiveSql.append(" AND (").append(filterTargetSql).append(")");
        }

        // add date constraint
        recipientActiveSql.append(getDateConstraint("bind.timestamp", startDateString, endDateString, DATE_CONSTRAINT_LESS_THAN_START));

        int recipientCount = selectInt(recipientActiveSql.toString());

		if (getConfigService().getBooleanValue(ConfigValue.UseBindingHistoryForRecipientStatistics, companyId)) {
			String hstRecipientActiveSql = "SELECT COUNT(DISTINCT(bind.customer_id)) AS active_count " +
					" FROM " + getHstCustomerBindingTableName(companyId) + " bind " +
					" LEFT JOIN " + getCustomerTableName(companyId) + " cust ON (bind.customer_id = cust.customer_id)" +
					" WHERE bind.user_status = " + UserStatus.Active.getStatusCode() +
					" AND bind.mailinglist_id = ?";

			if (StringUtils.isNotBlank(target.getTargetSQL())) {
				hstRecipientActiveSql += " AND (" + target.getTargetSQL() + ")";
			}

			if (StringUtils.isNotBlank(filterTargetSql)) {
				hstRecipientActiveSql += " AND (" + filterTargetSql + ")";
			}

			hstRecipientActiveSql += getDateConstraint("bind.timestamp", startDateString, endDateString, DATE_CONSTRAINT_LESS_THAN_START);
			hstRecipientActiveSql += getDateConstraint("bind.timestamp_change", startDateString, endDateString, DATE_CONSTRAINT_GREATER_THAN_START);

			// Select additional data from history tables
			int hstResult = selectInt(hstRecipientActiveSql, mailinglistId);
			recipientCount += hstResult;
        }

        updateEmbedded(getTempInsertQuery(tempTableId),
                mailinglistId,
                mailinglistName,
                startDateString,
                ACTIVE_START_DATE_INDEX,
                target.getName(), target.getId(), targetGroupIndex, recipientCount);
    }


    /**
     * Returns query for inserting collected values into temporary table
     */
    private String getTempInsertQuery(int tempTableId) {
        return "INSERT INTO " + getTempReportTableName(tempTableId) +
                " (mailinglist_id, mailinglist_name, category_name, category_index, targetgroup_name, targetgroup_id, targetgroup_index, value)" +
                " VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
    }

    private void updateNumberOfTargetGroups(int tempTableId, int numberOfTargetGroups) throws Exception {
        updateEmbedded("UPDATE " + getTempReportTableName(tempTableId) + " SET count_target_group = ?", numberOfTargetGroups);
    }
}
