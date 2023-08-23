/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.common.MailingType;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.workflow.beans.WorkflowIcon;
import com.agnitas.emm.core.workflow.beans.WorkflowIconType;
import com.agnitas.emm.core.workflow.service.ComWorkflowDataParser;
import com.agnitas.emm.core.workflow.service.util.WorkflowUtils;
import com.agnitas.reporting.birt.external.beans.LightMailing;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.dao.impl.LightMailingDaoImpl;
import org.agnitas.dao.impl.mapper.StringRowMapper;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CLICKER_TRACKED_INDEX;

public class WorkflowDataSet extends TotalOptimizationDataSet {

    private static final Logger logger = LogManager.getLogger(WorkflowDataSet.class);
    private static final Set<Integer> MAILING_ICONS_IDS = Set.of(
            WorkflowIconType.Constants.MAILING_ID,
            WorkflowIconType.Constants.FOLLOWUP_MAILING_ID,
            WorkflowIconType.Constants.ACTION_BASED_MAILING_ID,
            WorkflowIconType.Constants.DATE_BASED_MAILING_ID
    );

    private final ComWorkflowDataParser workflowDataParser;

    private int mailingTempTableId;
    private int summaryTempTableId;

    public WorkflowDataSet(ComWorkflowDataParser workflowDataParser) {
        this.workflowDataParser = workflowDataParser;
    }

    /**
     * This method has to be called in initialize function of the report, otherwise getWorkflowMailings will fail!
     */
    public void prepareWorkflowReport(int workflowId, int companyId, Boolean showSoftbounces) throws Exception {
        this.mailingTempTableId = prepareWorkflowMailingsData(workflowId, companyId);
        this.summaryTempTableId = prepareTotalWorkflowSummaryData(companyId, showSoftbounces);
    }

    private int prepareWorkflowMailingsData(int workflowId, int companyId) throws Exception {
        int tempTableID = createTempTable();
        List<WorkflowMailingData> mailingData = getWorkflowMailingsData(workflowId, companyId);
        for (WorkflowMailingData data : mailingData) {
            int mailingId = data.getMailingId();

            data.setAvgMailSize(getAvgMailingSize(mailingId, companyId));
            if (StringUtils.isEmpty(data.getMailingSubject())) {
                data.setMailingSubject(getMailingSubject(companyId, mailingId));
            }

            insertWorkflowMailingData(tempTableID, data);
        }
        return tempTableID;
    }

    private int prepareTotalWorkflowSummaryData(int companyID, Boolean showSoftbounces) throws Exception {
        int tempSummaryTableID = createSummaryTempTable();
        List<WorkflowMailingData> workflowMailingsData = getWorkflowMailings();

        if (workflowMailingsData.isEmpty()) {
            return tempSummaryTableID;
        }

        String targetSQL = allocateTargetSQL(workflowMailingsData, companyID);
        List<Integer> mailingIds = new ArrayList<>();
        for (WorkflowMailingData data : workflowMailingsData) {
            insertSendIntoTempTable(tempSummaryTableID, data, companyID, targetSQL);
            insertClickersIntoTempTable(tempSummaryTableID, data, companyID, targetSQL, CommonKeys.TYPE_ALL_SUBSCRIBERS);
            insertClicksAnonymousIntoTempTable(tempSummaryTableID, data, companyID);
            insertOpenersIntoTempTable(tempSummaryTableID, data, companyID, targetSQL);
            insertOpenedInvisibleIntoTempTable(tempSummaryTableID, data, companyID, targetSQL);
            insertOpenedGrossIntoTempTable(tempSummaryTableID, data, companyID, targetSQL);
            insertOpenedAnonymousIntoTempTable(tempSummaryTableID, data, companyID);
            insertBouncesIntoTempTable(tempSummaryTableID, data, companyID, targetSQL);
            insertSoftbouncesUndeliverable(tempSummaryTableID, data, companyID, showSoftbounces);
            insertOptOutsIntoTempTable(tempSummaryTableID, data, companyID, targetSQL);
            insertRevenueIntoTempTable(tempSummaryTableID, data, companyID, targetSQL);
            insertDeliveredIntoTempTable(tempSummaryTableID, data, companyID, targetSQL);
            mailingIds.add(data.getMailingId());
        }

        insertTotalValueByCategories(tempSummaryTableID, mailingIds);
        mailingIds.add(0); //mailing row for total value by category
        updateRates(tempSummaryTableID, mailingIds);
        return tempSummaryTableID;
    }

    public String getMailingNameBySortOrder(int order) throws Exception {
        String sql = "SELECT mailing_name FROM tmp_report_aggregation_" + mailingTempTableId + "_tbl WHERE sort_order = ?";
        return selectEmbedded(logger, sql, String.class, order);
    }

    public Integer getSortOrderByMailingName(String name) throws Exception {
        String sql = "SELECT sort_order FROM tmp_report_aggregation_" + mailingTempTableId + "_tbl WHERE mailing_name = ?";
        return selectEmbedded(logger, sql, Integer.class, name);
    }

    public List<WorkflowMailingData> getWorkflowMailings() throws Exception {
        String sql = "SELECT mailing_id, mailing_name, mailing_subject, sort_order, " +
                "target_group_id, target_group_name, avg_mailing_size" +
                " FROM tmp_report_aggregation_" + mailingTempTableId + "_tbl ORDER BY mailing_id";
        return selectEmbedded(logger, sql, (resultSet, i) -> {
            WorkflowMailingData data = new WorkflowMailingData();
            data.setMailingId(resultSet.getInt("mailing_id"));
            data.setMailingName(resultSet.getString("mailing_name"));
            data.setMailingSubject(StringUtils.trimToEmpty(resultSet.getString("mailing_subject")));
            int targetId = resultSet.getInt("target_group_id");
            data.setTargetGroupId(targetId == 0 ? ALL_SUBSCRIBERS_TARGETGROUPID : targetId);
            data.setTargetGroupName(resultSet.getString("target_group_name"));
            data.setOrder(resultSet.getInt("sort_order"));
            data.setAvgMailSize(resultSet.getLong("avg_mailing_size"));
            return data;
        });
    }

    public List<WorkflowMailingSummaryRow> getTotalWorkflowSummaryData() throws Exception {
        String query = "SELECT " +
                "st.category, st.category_index, st.mailing_id, st.value, st.rate, st.rate_delivered, mt.mailing_name, " +
                "COALESCE(st.sort_order, mt.sort_order) AS sort_order, " +
                "mt.target_group_id, mt.target_group_name " +
                "FROM tmp_report_aggregation_" + summaryTempTableId + "_tbl st " +
                "LEFT JOIN tmp_report_aggregation_" + mailingTempTableId + "_tbl mt ON mt.mailing_id = st.mailing_id AND mt.sort_order = st.sort_order " +
                "ORDER BY st.category_index, st.mailing_id";

        return selectEmbedded(logger, query, (resultSet, rowNum) -> {
            WorkflowMailingSummaryRow row = new WorkflowMailingSummaryRow();
            row.setCategory(resultSet.getString("category"));
            row.setCategoryindex(resultSet.getInt("category_index"));
            row.setMailingId(resultSet.getInt("mailing_id"));
            row.setMailingName(resultSet.getString("mailing_name"));
            row.setOrder(resultSet.getInt("sort_order"));
            row.setTargetgroupindex(resultSet.getInt("target_group_id"));
            row.setTargetgroup(resultSet.getString("target_group_name"));
            row.setCount(resultSet.getInt("value"));
            row.setRate(resultSet.getDouble("rate"));
            row.setDeliveredRate(resultSet.getDouble("rate_delivered"));
            return row;
        });
    }

    private List<WorkflowMailingData> getWorkflowMailingsData(int workflowId, int companyId) {
        Map<Integer, Integer> mailingsOrder = new HashMap<>();
        List<Integer> mailings = getWorkflowMailingsIds(workflowId, companyId);

        if (mailings.isEmpty()) {
            return Collections.emptyList();
        }

        for (int i = 0; i < mailings.size(); i++) {
            mailingsOrder.put(mailings.get(i), i);
        }

        String query = String.format("SELECT res.mid, " +
                "       res.name, " +
                "       res.mt_param " +
                "FROM (SELECT m.mailing_id      AS mid, " +
                "             MAX(m.shortname)  AS name, " +
                "             mt.param          AS mt_param " +
                "      FROM mailing_tbl m " +
                "               LEFT JOIN mailing_mt_tbl mt ON m.mailing_id = mt.mailing_id AND mediatype = 0 " +
                "      WHERE m.mailing_id IN (%s) " +
                "        AND m.company_id = ? " +
                "      GROUP BY m.mailing_id, mt.param) res", StringUtils.join(mailings, ", "));

        return select(logger, query, (resultSet, i) -> {
            WorkflowMailingData data = new WorkflowMailingData();
            data.setMailingId(resultSet.getInt("mid"));
            data.setMailingName(resultSet.getString("name"));
            data.setMailingSubject(getSubjectFromMTParam(resultSet.getString("mt_param")));
            data.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);

            data.setOrder(1 + mailingsOrder.get(data.getMailingId()));

            return data;
        }, companyId);
    }

    private List<Integer> getWorkflowMailingsIds(int workflowId, int companyId) {
        String sqlGetSchema = "SELECT workflow_schema FROM workflow_tbl WHERE workflow_id = ? AND company_id = ?";
        String schema = selectObjectDefaultNull(logger, sqlGetSchema, StringRowMapper.INSTANCE, workflowId, companyId);

        if (StringUtils.isBlank(schema)) {
            return Collections.emptyList();
        }

        List<WorkflowIcon> icons = workflowDataParser.deSerializeWorkflowIconsList(schema);
        return collectMailingIds(icons);
    }

    /**
     * Collect distinct set of mailings managed by given {@code icons}.
     *
     * @param icons a list of icons representing the workflow structure.
     * @return a list of mailing identifiers in order, as on UI.
     */
    private List<Integer> collectMailingIds(List<WorkflowIcon> icons) {
        return icons.stream()
                .filter(i -> MAILING_ICONS_IDS.contains(i.getType()))
                .filter(i -> WorkflowUtils.getMailingId(i) > 0 && i.isFilled())
                .sorted(Comparator.comparingInt(WorkflowIcon::getId))
                .map(WorkflowUtils::getMailingId)
                .collect(Collectors.toList());
    }

    @DaoUpdateReturnValueCheck
    @Override
    public void updateRates(int tempTableID, List<Integer> mailingIds) throws Exception {
        List<Integer> categoryIndexes = new ArrayList<>();
        for (int i = 1; i <= CommonKeys.SENT_OFFLINE_HTML_INDEX; i++) {
            categoryIndexes.add(i);
        }

        updateRatesByCategories(tempTableID, Collections.singletonList(CommonKeys.DELIVERED_EMAILS_INDEX), mailingIds, categoryIndexes);
        updateDeliveredRatesByCategories(tempTableID, Collections.singletonList(CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX), mailingIds, categoryIndexes);

        // mobile/PC clicks
        List<Integer> clickerIndexes = Arrays.asList(CommonKeys.CLICKER_PC_INDEX,
                CommonKeys.CLICKER_MOBILE_INDEX,
                CommonKeys.CLICKER_TABLET_INDEX,
                CommonKeys.CLICKER_SMARTTV_INDEX,
                CommonKeys.CLICKER_PC_AND_MOBILE_INDEX,
                CommonKeys.CLICKS_ANONYMOUS_INDEX
        );
        updateRatesByCategories(tempTableID, Collections.singletonList(CommonKeys.CLICKER_TRACKED_INDEX), mailingIds, clickerIndexes);

        // mobile/PC openings
        List<Integer> openingIndexes = Arrays.asList(
                CommonKeys.OPENERS_PC_INDEX,
                CommonKeys.OPENERS_TABLET_INDEX,
                CommonKeys.OPENERS_MOBILE_INDEX,
                CommonKeys.OPENERS_SMARTTV_INDEX,
                CommonKeys.OPENERS_PC_AND_MOBILE_INDEX,
                CommonKeys.OPENINGS_ANONYMOUS_INDEX
        );
        updateRatesByCategories(tempTableID, Collections.singletonList(CommonKeys.OPENERS_TRACKED_INDEX), mailingIds, openingIndexes);

        Integer[] measuredCategories = {
                CommonKeys.CLICKER_TRACKED_INDEX,
                CommonKeys.OPENERS_TRACKED_INDEX
        };

        String sqlUpdateMeasuredCategories = "UPDATE " +
                "tmp_report_aggregation_" + tempTableID + "_tbl" +
                " SET rate = 1 WHERE mailing_id IN (" +
                StringUtils.join(mailingIds, ", ") +
                ") AND category_index IN (" +
                StringUtils.join(measuredCategories, ", ") +
                ")";

        updateEmbedded(logger, sqlUpdateMeasuredCategories);

        // Gross openings / gross clicks
        updateEmbedded(logger, getUpdateResponseRateQuery(tempTableID), CommonKeys.ACTIVITY_RATE, CommonKeys.ACTIVITY_RATE_INDEX);
    }

    private String getUpdateResponseRateQuery(int tempTableID) {
        return "INSERT INTO tmp_report_aggregation_" + tempTableID + "_tbl"
                + " (category, category_index, mailing_id, sort_order, rate)"
                + " SELECT ?, ?, a.mailing_id, a.sort_order, CASE WHEN (b.value = 0) THEN 0 ELSE CAST(a.value AS DOUBLE)/b.value END"
                + " FROM tmp_report_aggregation_" + tempTableID + "_tbl a INNER JOIN tmp_report_aggregation_" + tempTableID + "_tbl b"
                + " ON a.category_index = " + CommonKeys.CLICKS_GROSS_INDEX +
                " AND b.category_index = " + CommonKeys.OPENINGS_GROSS_MEASURED_INDEX + " AND a.mailing_id = b.mailing_id AND a.sort_order = b.sort_order";
    }

    private void updateRatesByCategories(int tempTableID, List<Integer> allCategoryIndex, List<Integer> mailingIds, List<Integer> categoryIndex) throws Exception {
        StringBuilder totalCountQuery = new StringBuilder();
        totalCountQuery
                .append("SELECT a.mailing_id, CASE WHEN (SUM(a.value) IS NULL OR SUM(a.value) < 0) THEN -1 ELSE SUM(a.value) END AS total")
                .append(" FROM ").append("tmp_report_aggregation_").append(tempTableID).append("_tbl").append(" a")
                .append(" WHERE a.mailing_id IN (").append(StringUtils.join(mailingIds, ", ")).append(")")
                .append(" AND a.category_index IN (").append(StringUtils.join(allCategoryIndex, ", ")).append(")")
                .append(" GROUP BY a.mailing_id");

        StringBuilder updateRateQuery = new StringBuilder();
        updateRateQuery
                .append("UPDATE ").append("tmp_report_aggregation_").append(tempTableID).append("_tbl").append(" t")
                .append(" SET t.rate = (CASE WHEN (? <= 0) THEN -1 ELSE 1.0 * t.value / ? END)")
                .append(" WHERE t.mailing_id = ? AND t.category_index IN (").append(StringUtils.join(categoryIndex, ", ")).append(")");

        for (Map<String, Object> row : selectEmbedded(logger, totalCountQuery.toString())) {
            updateEmbedded(logger, updateRateQuery.toString(), ((Number) row.get("total")).intValue(), ((Number) row.get("total")).intValue(), ((Number) row.get("mailing_id")).intValue());
        }
    }

    private void updateDeliveredRatesByCategories(int tempTableID, List<Integer> allCategoryIndex, List<Integer> mailingIds, List<Integer> categoryIndex) throws Exception {
        StringBuilder totalCountQuery = new StringBuilder();
        totalCountQuery
                .append("SELECT a.mailing_id, CASE WHEN (SUM(a.value) IS NULL OR SUM(a.value) = 0) THEN -1 ELSE SUM(a.value) END AS total")
                .append(" FROM ").append("tmp_report_aggregation_").append(tempTableID).append("_tbl").append(" a")
                .append(" WHERE a.mailing_id IN (").append(StringUtils.join(mailingIds, ", ")).append(")")
                .append(" AND a.category_index IN (").append(StringUtils.join(allCategoryIndex, ", ")).append(")")
                .append(" GROUP BY a.mailing_id");

        StringBuilder updateRateQuery = new StringBuilder();
        updateRateQuery
                .append("UPDATE ").append("tmp_report_aggregation_").append(tempTableID).append("_tbl t")
                .append(" SET t.rate_delivered = (CASE WHEN t.category_index " +
                        " IN (" + CommonKeys.DELIVERED_EMAILS_INDEX + "," + CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX + ") THEN t.rate " +
                        " ELSE (CASE WHEN (? <= 0) THEN -1 ELSE 1.0 * t.value / ? END) END) ")
                .append(" WHERE t.mailing_id = ? AND t.category_index IN (").append(StringUtils.join(categoryIndex, ", ")).append(")");

        for (Map<String, Object> row : selectEmbedded(logger, totalCountQuery.toString())) {
            updateEmbedded(logger, updateRateQuery.toString(), ((Number) row.get("total")).intValue(), ((Number) row.get("total")).intValue(), ((Number) row.get("mailing_id")).intValue());
        }
    }

    @DaoUpdateReturnValueCheck
    private void insertDeliveredIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID, String targetSQL) throws Exception {
        int mailingId = data.getMailingId();
        if (successTableActivated(companyID) && hasSuccessTableData(companyID, mailingId)) {

            boolean isMailingNotExpired = isMailingNotExpired(mailingId);
            int deliveredMails = selectNumberOfDeliveredMails(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, targetSQL, null, null);
            if (deliveredMails > 0 || isMailingNotExpired) {

                insertIntoSummaryTempTable(tempTableID, CommonKeys.DELIVERED_EMAILS_DELIVERED, CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX,
                        mailingId, deliveredMails, data.getOrder());
            }
        } else if (isMailingNotExpired(mailingId)) {
            final boolean isActivated = isMailingTrackingActivated(companyID);

            int value;
            if (data.getTargetGroupId() != CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID && !isActivated) {
                value = 0;
            } else {
                String queryBuilder = "SELECT " +
                        " SUM(CASE WHEN (category_index = " + CommonKeys.DELIVERED_EMAILS_INDEX + ") THEN value ELSE 0 END)" +
                        " -" +
                        " SUM(CASE WHEN (category_index = " + CommonKeys.HARD_BOUNCES_INDEX + ") THEN value ELSE 0 END) AS value" +
                        " FROM " + "tmp_report_aggregation_" + tempTableID + "_tbl" +
                        " WHERE mailing_id =? AND category_index IN (" + CommonKeys.DELIVERED_EMAILS_INDEX + ", " + CommonKeys.HARD_BOUNCES_INDEX + ")" +
                        " GROUP BY mailing_id";

                value = selectEmbeddedInt(logger, queryBuilder, mailingId);

            }

            insertIntoSummaryTempTable(tempTableID, CommonKeys.REVENUE, CommonKeys.REVENUE_INDEX,
                    mailingId, value, data.getOrder());
        } else {
            insertIntoSummaryTempTable(tempTableID, CommonKeys.REVENUE, CommonKeys.REVENUE_INDEX,
                    mailingId, 0, data.getOrder());
        }
    }

    @DaoUpdateReturnValueCheck
    private void insertRevenueIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID, String targetSQL) throws Exception {
        if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_num_tbl")) {
            double revenue = selectRevenue(companyID, data.getMailingId(), targetSQL, null, null);

            insertIntoSummaryTempTable(tempTableID, CommonKeys.REVENUE, CommonKeys.REVENUE_INDEX,
                    data.getMailingId(), (int) (100 * revenue), data.getOrder());
        }
    }

    @DaoUpdateReturnValueCheck
    private void insertOptOutsIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID, String targetSQL) throws Exception {
        int mailingId = data.getMailingId();
        int optouts = selectOptOuts(companyID, mailingId, targetSQL, CommonKeys.TYPE_ALL_SUBSCRIBERS, null, null);

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPT_OUTS, CommonKeys.OPT_OUTS_INDEX,
                mailingId, optouts, data.getOrder());
    }

    @DaoUpdateReturnValueCheck
    private void insertSoftbouncesUndeliverable(int tempTableID, WorkflowMailingData data, int companyID, boolean showSoftbounces) throws Exception {
        if (!successTableActivated(companyID) || !showSoftbounces) {
            return;
        }
        int mailingId = data.getMailingId();

        LightMailing mailing = new LightMailingDaoImpl(getDataSource()).getMailing(mailingId, companyID);
        if (mailing != null && mailing.getMailingType() == MailingType.NORMAL.getCode()) {
            int deliveredMails = selectNumberOfDeliveredMails(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, null, null, null);

            int sentMails = getTempTableValuesByCategoryAndMailingId(tempTableID, CommonKeys.DELIVERED_EMAILS, mailingId);
            int hardBounces = getTempTableValuesByCategoryAndMailingId(tempTableID, CommonKeys.HARD_BOUNCES, mailingId);

            int value = sentMails - deliveredMails - hardBounces;
            value = Math.max(0, value);

            insertIntoSummaryTempTable(tempTableID, CommonKeys.SOFT_BOUNCES_UNDELIVERABLE, CommonKeys.SOFT_BOUNCES_UNDELIVERABLE_INDEX,
                    mailingId, value, data.getOrder());
        }
    }

    private int getTempTableValuesByCategoryAndMailingId(int tempTableID, String category, int mailingId) throws Exception {
        String query = "SELECT value FROM tmp_report_aggregation_" + tempTableID + "_tbl WHERE category = ? AND mailing_id = ?";
        int value;
        try {
            value = selectEmbedded(logger, query, Integer.class, category, mailingId);
        } catch (EmptyResultDataAccessException e) {
            logger.error("No data found for category: " + category + ", mailingId: " + mailingId);
            value = 0;
        }
        return value;
    }

    @DaoUpdateReturnValueCheck
    private void insertBouncesIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID, String targetSQL) throws Exception {
        int mailingId = data.getMailingId();
        int hardbounces;
        if (!isMailingBouncesExpire(companyID, mailingId) || !isOracleDB()) {
            hardbounces = selectHardbouncesFromBounces(companyID, mailingId, targetSQL, CommonKeys.TYPE_ALL_SUBSCRIBERS, null, null);
        } else {
            hardbounces = selectHardbouncesFromBindings(companyID, mailingId, targetSQL, CommonKeys.TYPE_ALL_SUBSCRIBERS, null, null);
        }

        insertIntoSummaryTempTable(tempTableID, CommonKeys.HARD_BOUNCES, CommonKeys.HARD_BOUNCES_INDEX,
                mailingId, hardbounces, data.getOrder());
    }

    @DaoUpdateReturnValueCheck
    private void insertOpenedAnonymousIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID) throws Exception {
        int anonymousOpenings = selectAnonymousOpenings(companyID, data.getMailingId(), null, null);

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENINGS_ANONYMOUS, CommonKeys.OPENINGS_ANONYMOUS_INDEX,
                data.getMailingId(), anonymousOpenings, data.getOrder());
    }

    @DaoUpdateReturnValueCheck
    private void insertOpenedGrossIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID, String targetSQL) throws Exception {
        int openings = selectOpenings(companyID, data.getMailingId(), null, targetSQL, null, null);

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENINGS_GROSS_MEASURED, CommonKeys.OPENINGS_GROSS_MEASURED_INDEX,
                data.getMailingId(), openings, data.getOrder());
    }

    @DaoUpdateReturnValueCheck
    private void insertOpenedInvisibleIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID, String targetSQL) throws Exception {
        int mailingId = data.getMailingId();
        int measuredOpeners = selectEmbeddedInt(logger,
                "SELECT value FROM tmp_report_aggregation_" + tempTableID + "_tbl WHERE category_index = ? AND mailing_id = ?",
                CommonKeys.OPENERS_MEASURED_INDEX, mailingId);
        boolean isAllRecipientGroup = StringUtils.isBlank(targetSQL) || targetSQL.replace(" ", "").equals("1=1");

        int openingClickers = selectOpeningClickers(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, targetSQL, null, null);
        int nonOpeningClickers = selectNonOpeningClickers(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, targetSQL, null, null);
        int maximumOverallOpeners;
        if (successTableActivated(companyID) && isMailingNotExpired(mailingId)) {
            maximumOverallOpeners = selectNumberOfDeliveredMails(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, targetSQL, null, null);
        } else if (isAllRecipientGroup) {
            if (isMailingNotExpired(mailingId)) {
                // this calculation only works for the "all_recipients" target
                int mailsSent = getNumberSentMailings(companyID, mailingId, CommonKeys.TYPE_WORLDMAILING, null, null, null);
                int hardbounces = selectHardbouncesFromBounces(companyID, mailingId, null, CommonKeys.TYPE_ALL_SUBSCRIBERS, null, null);
                maximumOverallOpeners = mailsSent - hardbounces;
            } else {
                // this calculation only works for the "all_recipients" target
                maximumOverallOpeners = getNumberSentMailings(companyID, mailingId, CommonKeys.TYPE_WORLDMAILING, null, null, null);
            }
        } else {
            maximumOverallOpeners = 0;
        }

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

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_INVISIBLE, CommonKeys.OPENERS_INVISIBLE_INDEX,
                mailingId, invisibleOpeners, data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_TOTAL, CommonKeys.OPENERS_TOTAL_INDEX,
                mailingId, measuredOpeners + invisibleOpeners, data.getOrder());
    }

    private void insertOpenersIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID, String targetSQL) throws Exception {
        int mailingId = data.getMailingId();
        int totalOpeners = selectOpeners(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, targetSQL, null, null, null);

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_MEASURED, CommonKeys.OPENERS_MEASURED_INDEX,
                mailingId, totalOpeners, data.getOrder());

        // If a customer is within one of theses deviceclasses he hasn't opened the mail with any other deviceclass.
        Map<DeviceClass, Integer> openersByDevice =
                selectOpenersByDeviceClassWithoutCombinations(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, targetSQL, null, null);

        // Calculating the openers which used more than one deviceclass for link-opens
        int openersWithDeviceClassCombinations = totalOpeners;
        for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
            openersWithDeviceClassCombinations -= openersByDevice.getOrDefault(deviceClass, 0);
        }

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_TRACKED, CommonKeys.OPENERS_TRACKED_INDEX,
                mailingId, totalOpeners, data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_PC, CommonKeys.OPENERS_PC_INDEX,
                mailingId, openersByDevice.getOrDefault(DeviceClass.DESKTOP, 0), data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_MOBILE, CommonKeys.OPENERS_MOBILE_INDEX,
                mailingId, openersByDevice.getOrDefault(DeviceClass.MOBILE, 0), data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_TABLET, CommonKeys.OPENERS_TABLET_INDEX,
                mailingId, openersByDevice.getOrDefault(DeviceClass.TABLET, 0), data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_SMARTTV, CommonKeys.OPENERS_SMARTTV_INDEX,
                mailingId, openersByDevice.getOrDefault(DeviceClass.SMARTTV, 0), data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_PC_AND_MOBILE, CommonKeys.OPENERS_PC_AND_MOBILE_INDEX,
                mailingId, openersWithDeviceClassCombinations, data.getOrder());

    }

    @DaoUpdateReturnValueCheck
    private void insertClicksAnonymousIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID) throws Exception {
        int anonymousClicks = selectAnonymousClicks(companyID, data.getMailingId(), null, null);

        insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKS_ANONYMOUS, CommonKeys.CLICKS_ANONYMOUS_INDEX,
                data.getMailingId(), anonymousClicks, data.getOrder());
    }

    private String allocateTargetSQL(List<WorkflowMailingData> workflowMailingsData, int companyID) {
        if (CollectionUtils.isEmpty(workflowMailingsData)) {
            return "";
        }

        int targetId = workflowMailingsData.get(0).getTargetGroupId();
        if (targetId == ALL_SUBSCRIBERS_TARGETGROUPID) {
            return "";
        }
        LightTarget target = getTarget(targetId, companyID);
        if (target == null) {
            return "";
        }
        return target.getTargetSQL();
    }

    @DaoUpdateReturnValueCheck
    public int insertSendIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID, String targetSql) throws Exception {
        String recipientsType = data.getTargetGroupId() == ALL_SUBSCRIBERS_TARGETGROUPID ? CommonKeys.TYPE_WORLDMAILING : CommonKeys.TYPE_ALL_SUBSCRIBERS;
        int mailsSent = getNumberSentMailings(companyID, data.getMailingId(), recipientsType, targetSql, null, null);

        insertIntoSummaryTempTable(tempTableID, CommonKeys.DELIVERED_EMAILS, CommonKeys.DELIVERED_EMAILS_INDEX,
                data.getMailingId(), mailsSent, data.getOrder());

        return mailsSent;
    }

    @DaoUpdateReturnValueCheck
    public void insertClickersIntoTempTable(int tempTableID, WorkflowMailingData data, int companyID, String targetSql, String recipientsType) throws Exception {
        int mailingId = data.getMailingId();
        int totalClicks = selectClicks(companyID, mailingId, recipientsType, targetSql, null, null);
        int totalClickers = selectClickers(companyID, mailingId, recipientsType, targetSql, null, null);

        insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER, CommonKeys.CLICKER_INDEX,
                mailingId,
                totalClickers, data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKS_GROSS, CommonKeys.CLICKS_GROSS_INDEX,
                mailingId,
                totalClicks, data.getOrder());

        // If a customer is within one of theses deviceclasses he hasn't clicked in the mail with any other deviceclass.
        Map<DeviceClass, Integer> clickersByDevices =
                selectClickersByDeviceClassWithoutCombinations(companyID, mailingId, recipientsType, targetSql, null, null);

        // Calculating the clickers which used more than one deviceclass for link-clicks
        int unknownDevicesClickers = totalClickers;
        for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
            unknownDevicesClickers -= clickersByDevices.getOrDefault(deviceClass, 0);
        }

        insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_TRACKED, CLICKER_TRACKED_INDEX,
                mailingId, totalClickers, data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_PC, CommonKeys.CLICKER_PC_INDEX,
                mailingId, clickersByDevices.getOrDefault(DeviceClass.DESKTOP, 0), data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_MOBILE, CommonKeys.CLICKER_MOBILE_INDEX,
                mailingId, clickersByDevices.getOrDefault(DeviceClass.MOBILE, 0), data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_TABLET, CommonKeys.CLICKER_TABLET_INDEX,
                mailingId, clickersByDevices.getOrDefault(DeviceClass.TABLET, 0), data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_SMARTTV, CommonKeys.CLICKER_SMARTTV_INDEX,
                mailingId, clickersByDevices.getOrDefault(DeviceClass.SMARTTV, 0), data.getOrder());

        insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_PC_AND_MOBILE, CommonKeys.CLICKER_PC_AND_MOBILE_INDEX,
                mailingId, unknownDevicesClickers, data.getOrder());
    }

    @Override
    public int createSummaryTempTable() throws Exception {
        int tempTableID = getNextTmpID();
        String createTable = "CREATE TABLE tmp_report_aggregation_" + tempTableID + "_tbl" +
                " (" +
                "category VARCHAR(200)" +
                ", category_index INTEGER" +
                ", mailing_id INTEGER" +
                ", sort_order INTEGER" +
                ", value INTEGER" +
                ", rate DOUBLE" +
                ", rate_delivered DOUBLE" +
                ")";
        updateEmbedded(logger, createTable);
        return tempTableID;
    }

    @Override
    public int createTempTable() throws Exception {
        int tempTableID = getNextTmpID();
        String createTable = "CREATE TABLE tmp_report_aggregation_" + tempTableID + "_tbl" +
                " (" +
                "  mailing_id INTEGER" +
                ", mailing_name VARCHAR(200)" +
                ", mailing_subject VARCHAR(200)" +
                ", sort_order INTEGER" +
                ", target_group_id INTEGER" +
                ", target_group_name VARCHAR(200)" +
                ", avg_mailing_size DOUBLE" +
                ")";
        updateEmbedded(logger, createTable);
        return tempTableID;
    }

    private void insertIntoSummaryTempTable(int tempTableID, String category, int categoryIndex, int mailingId, int value, int order) throws Exception {
        String insertSql = "INSERT INTO tmp_report_aggregation_" + tempTableID + "_tbl " +
                "(category, category_index, mailing_id, sort_order, value, rate) VALUES (?, ?, ?, ?, ?, 0)";
        updateEmbedded(logger, insertSql, category, categoryIndex, mailingId, order, value);
    }

    private void insertTotalValueByCategories(int tempTableID, List<Integer> mailingIds) throws Exception {
        String insertSql = "INSERT INTO tmp_report_aggregation_" + tempTableID + "_tbl " +
                "(category, category_index, mailing_id, sort_order, value, rate) " +
                " SELECT a.category, a.category_index, 0, -1, CASE WHEN (SUM(a.value) IS NULL OR SUM(a.value) = 0) THEN 0 ELSE SUM(a.value) END AS total, 0" +
                " FROM tmp_report_aggregation_" + tempTableID + "_tbl a WHERE a.mailing_id IN (" + StringUtils.join(mailingIds, ", ") + ")" +
                " GROUP BY a.category, a.category_index";

        updateEmbedded(logger, insertSql);
    }

    private void insertWorkflowMailingData(int tempTableID, WorkflowMailingData mailing) throws Exception {
        String insertSql = "INSERT INTO tmp_report_aggregation_" + tempTableID + "_tbl " +
                "(mailing_id, mailing_name, mailing_subject, sort_order, " +
                "target_group_id, target_group_name, avg_mailing_size) VALUES (?, ?, ?, ?, ?, ?, ?)";
        updateEmbedded(logger, insertSql, mailing.getMailingId(), mailing.getMailingName(), mailing.getMailingSubject(),
                mailing.getOrder(), mailing.getTargetGroupId(), mailing.getTargetGroupName(),
                mailing.getAvgMailSize());
    }

    public static class WorkflowMailingSummaryRow extends MailingSummaryRow {

        private int mailingId;
        private String mailingName;
        private int order;

        public int getMailingId() {
            return mailingId;
        }

        public void setMailingId(int mailingId) {
            this.mailingId = mailingId;
        }

        public String getMailingName() {
            return mailingName;
        }

        public void setMailingName(String mailingName) {
            this.mailingName = mailingName;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }
    }

    public static class WorkflowMailingData {

        private int mailingId;
        private String mailingName;
        private String mailingSubject;
        private String targetGroupName;
        private int targetGroupId;
        private long avgMailSize;
        private int order;

        public int getMailingId() {
            return mailingId;
        }

        public void setMailingId(int mailingId) {
            this.mailingId = mailingId;
        }

        public String getMailingName() {
            return mailingName;
        }

        public void setMailingName(String mailingName) {
            this.mailingName = mailingName;
        }

        public String getMailingSubject() {
            return mailingSubject;
        }

        public void setMailingSubject(String mailingSubject) {
            this.mailingSubject = mailingSubject;
        }

        public String getTargetGroupName() {
            return targetGroupName;
        }

        public void setTargetGroupName(String targetGroupName) {
            this.targetGroupName = targetGroupName;
        }

        public void setTargetGroupId(int targetGroupId) {
            this.targetGroupId = targetGroupId;
        }

        public int getTargetGroupId() {
            return targetGroupId;
        }

        public int getOrder() {
            return order;
        }

        public void setOrder(int order) {
            this.order = order;
        }

        public long getAvgMailSize() {
            return avgMailSize;
        }

        public void setAvgMailSize(long avgMailSize) {
            this.avgMailSize = avgMailSize;
        }
    }

    @Override
    public void destroy() throws Exception {
        dropTempTable(summaryTempTableId);
        dropTempTable(mailingTempTableId);
    }
}
