/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CLICKER_TRACKED_INDEX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.dao.EmptyResultDataAccessException;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.target.eql.codegen.resolver.MailingType;
import com.agnitas.reporting.birt.external.beans.LightMailing;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.dao.impl.LightMailingDaoImpl;

public class TotalOptimizationDataSet extends MailingSummaryDataSet {
	private static final transient Logger logger = Logger.getLogger(TotalOptimizationDataSet.class);
	
	private int mailingTempTableId;
	private int summaryTempTableId;
	
	public int getMailingTempTableId() {
		if(mailingTempTableId == 0) {
			logger.error("Report mailing data wasn't initialized");
		}
		return mailingTempTableId;
	}
	
	public int getSummaryTempTableId() {
		if (summaryTempTableId == 0) {
			logger.error("Report summary data wasn't initialized");
		}
		return summaryTempTableId;
	}
	
	/**
	 * This method has to be called in initialize function of the report, otherwise getOptimizationMailings will fail!
	 * @param optimizationId
	 * @param companyId
	 * @param showSoftbounces
	 * @throws Exception
	 */
	public void prepareOptimizationReport(int optimizationId, @VelocityCheck int companyId, Boolean showSoftbounces) throws Exception {
		this.mailingTempTableId = prepareOptimizationMailingsData(optimizationId, companyId);
		this.summaryTempTableId = prepareTotalOptimizationSummaryData(companyId, showSoftbounces);
	}
	
	/**
	 * This method has to be called in initialize function of the report, otherwise getOptimizationMailings, getTotalOptimizationSummaryData will fail!
	 * @param optimizationId
	 * @param companyId
	 * @return
	 * @throws Exception
	 */
	private int prepareOptimizationMailingsData(int optimizationId, int companyId) throws Exception {
		int tempTableID = createTempTable();
		List<OptimizationMailingData> mailingData = getOptimizationMailingsData(optimizationId, companyId);
		for (OptimizationMailingData data : mailingData) {
			int mailingId = data.getMailingId();
			data.setAvgMailSize(getAvgMailingSize(mailingId, companyId));
			if (StringUtils.isEmpty(data.getMailingSubject())) {
				data.setMailingSubject(getMailingSubject(companyId, mailingId));
			}
			insertOptimizationMailingData(tempTableID, data);
		}
		return tempTableID;
	}

	private int prepareTotalOptimizationSummaryData(@VelocityCheck int companyID, Boolean showSoftbounces) throws Exception {
		int tempSummaryTableID = createSummaryTempTable();
		List<OptimizationMailingData> optimizationMailingsData = getOptimizationMailings();
		String targetSQL = allocateTargetSQL(optimizationMailingsData, companyID);
		List<Integer> mailingIds = new ArrayList<>();
		for (OptimizationMailingData data : optimizationMailingsData) {
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
	
	public List<OptimizationMailingData> getOptimizationMailings() throws Exception {
		String sql = "SELECT mailing_id, mailing_name, mailing_subject, group_id, is_winner, base_mailing_id, " +
				"target_group_id, target_group_name, avg_mailing_size, send_date" +
						" FROM tmp_report_aggregation_" + mailingTempTableId + "_tbl ORDER BY mailing_id";
		return selectEmbedded(logger, sql, (resultSet, i) -> {
			OptimizationMailingData mdata = new OptimizationMailingData();
			mdata.setMailingId(resultSet.getInt("mailing_id"));
			mdata.setMailingName(resultSet.getString("mailing_name"));
			mdata.setMailingSubject(StringUtils.trimToEmpty(resultSet.getString("mailing_subject")));
			int targetId = resultSet.getInt("target_group_id");
			mdata.setTargetGroupId(targetId == 0 ? ALL_SUBSCRIBERS_TARGETGROUPID : targetId);
			mdata.setTargetGroupName(resultSet.getString("target_group_name"));
			mdata.setWinner(BooleanUtils.toBoolean(resultSet.getInt("is_winner")));
			mdata.setGroupId(resultSet.getInt("group_id"));
			mdata.setResultMailing(resultSet.getInt("base_mailing_id"));
			mdata.setAvgMailSize(resultSet.getLong("avg_mailing_size"));
			mdata.setSendDate(resultSet.getTimestamp("send_date"));
			return mdata;
		});
	}
	
	public List<OptimizationMailingSummaryRow> getTotalOptimizationSummaryData() throws Exception {
		String query = "SELECT " +
				"st.category, st.category_index, st.mailing_id, st.value, st.rate, st.rate_delivered, mt.mailing_name, " +
				"COALESCE(st.group_id, mt.group_id) AS group_id, mt.is_winner, " +
				"(SELECT mailing_name FROM tmp_report_aggregation_" + mailingTempTableId + "_tbl " +
				"	WHERE mailing_id = mt.base_mailing_id) AS base_mailing_name, " +
				"(SELECT group_id FROM tmp_report_aggregation_" + mailingTempTableId + "_tbl " +
				"	WHERE mailing_id = mt.base_mailing_id) AS base_group_id, " +
				"mt.target_group_id, mt.target_group_name " +
				"FROM tmp_report_aggregation_" + summaryTempTableId + "_tbl st " +
				"LEFT JOIN tmp_report_aggregation_" + mailingTempTableId + "_tbl mt ON mt.mailing_id = st.mailing_id AND mt.group_id = st.group_id " +
				"ORDER BY st.category_index, st.mailing_id";

        List<OptimizationMailingSummaryRow> list = selectEmbedded(logger, query, (resultSet, rowNum) -> {
            OptimizationMailingSummaryRow row = new OptimizationMailingSummaryRow();
			row.setCategory(resultSet.getString("category"));
			row.setCategoryindex(resultSet.getInt("category_index"));
			row.setMailingId(resultSet.getInt("mailing_id"));
			row.setMailingName(resultSet.getString("mailing_name"));
			row.setGroupId(resultSet.getInt("group_id"));
			row.setWinner(BooleanUtils.toBoolean(resultSet.getInt("is_winner")));
			row.setBaseMailingName(resultSet.getString("base_mailing_name"));
			row.setBaseGroupId(resultSet.getInt("base_group_id"));
			row.setTargetgroupindex(resultSet.getInt("target_group_id"));
			row.setTargetgroup(resultSet.getString("target_group_name"));
			row.setCount(resultSet.getInt("value"));
			row.setRate(resultSet.getDouble("rate"));
			row.setDeliveredRate(resultSet.getDouble("rate_delivered"));
            return row;
        });
        return list;
    }
	
	private List<OptimizationMailingData> getOptimizationMailingsData(int optimizationId, int companyId) {
		String query = "SELECT res.mid, res.name, res.subject, " +
				"  COALESCE(res.target_id, 0) AS target_id, res.target_name, " +
				"  res.group_id, res.result_mailing, COALESCE(mac.mintime, mds.senddate) AS last_send_date" +
				" FROM (" +
				"  SELECT m.mailing_id AS mid, MAX(m.shortname) AS name, MAX(m.subject) AS subject, " +
				"  MAX(tg.target_id) AS target_id, " +
				"  MAX(tg.target_shortname) AS target_name, " +
				"  MAX(ao.result_mailing_id) AS result_mailing_id, " +
				"  (CASE" +
				"        WHEN m.mailing_id = ao.group1_id THEN 1" +
				"        WHEN m.mailing_id = ao.group2_id THEN 2" +
				"        WHEN m.mailing_id = ao.group3_id THEN 3" +
				"        WHEN m.mailing_id = ao.group4_id THEN 4" +
				"        WHEN m.mailing_id = ao.group5_id THEN 5" +
				"        ELSE 0" +
				"   END) AS group_id, " +
				"  (CASE WHEN ao.final_mailing_id =  m.mailing_id THEN MAX(ao.result_mailing_id) ELSE 0 END) AS result_mailing" +
				"  FROM mailing_tbl m, auto_optimization_tbl ao " +
				"  LEFT JOIN dyn_target_tbl tg ON ao.target_id = tg.target_id" +
				"  WHERE m.mailing_id IN (ao.group1_id, ao.group2_id, ao.group3_id, ao.group4_id, ao.group5_id, ao.final_mailing_id)" +
				"  AND optimization_id = ? AND m.company_id = ?" +
				"  GROUP BY m.mailing_id, ao.group1_id, ao.group2_id, ao.group3_id, ao.group4_id, ao.group5_id, ao.final_mailing_id" +
				") res " +
				" LEFT JOIN (SELECT mailing_id AS mid, MIN(mintime) AS mintime FROM mailing_account_sum_tbl WHERE status_field = 'W' GROUP BY mailing_id) mac ON res.mid = mac.mid" +
				" LEFT JOIN (SELECT mailing_id AS mid, MAX(senddate) AS senddate FROM maildrop_status_tbl WHERE status_field = 'W' GROUP BY mailing_id) mds ON res.mid = mds.mid";

		List<OptimizationMailingData> mailingData = select(logger, query, (resultSet, i) -> {
			OptimizationMailingData mdata = new OptimizationMailingData();
			mdata.setMailingId(resultSet.getInt("mid"));
			mdata.setMailingName(resultSet.getString("name"));
			mdata.setMailingSubject(StringUtils.trimToEmpty(resultSet.getString("subject")));
			int targetId = resultSet.getInt("target_id");
			mdata.setTargetGroupId(targetId == 0 ? ALL_SUBSCRIBERS_TARGETGROUPID : targetId);
			mdata.setTargetGroupName(resultSet.getString("target_name"));
			mdata.setWinner(resultSet.getInt("group_id") == 0);
			mdata.setGroupId(resultSet.getInt("group_id"));
			mdata.setResultMailing(resultSet.getInt("result_mailing"));
			mdata.setSendDate(resultSet.getTimestamp("last_send_date"));
			return mdata;
		}, optimizationId, companyId);
		return mailingData;
	}

	public long getAvgMailingSize(Integer mailingId, int companyId) {
		String query = "SELECT" +
				" " + "COALESCE(SUM(no_of_mailings), 0) AS mails," +
				" " + "COALESCE(SUM(no_of_bytes), 0) AS bytes" +
				" FROM mailing_account_tbl" +
				" WHERE mailing_id = ?" +
				" AND status_field NOT IN ('A', 'T', 'V')";
		return selectObject(logger, query, (resultSet, i) -> {
			try {
				long numberOfSentEmails = resultSet.getLong("mails");
				long numberOfBytes = resultSet.getLong("bytes");


				long dataAmountRequestedFromRdirHistoric = selectLong(logger, "SELECT SUM(content_size * amount) AS bytes FROM rdir_traffic_agr_" + companyId + "_tbl WHERE mailing_id = ?", mailingId);
				numberOfBytes += dataAmountRequestedFromRdirHistoric;

				long dataAmountRequestedFromRdirCurrentDay = selectLong(logger, "SELECT SUM(content_size) AS bytes FROM rdir_traffic_amount_" + companyId + "_tbl WHERE mailing_id = ?", mailingId);
				numberOfBytes += dataAmountRequestedFromRdirCurrentDay;

				// averageMailsize is in kilobytes
				if (numberOfSentEmails > 0) {
					return numberOfBytes / numberOfSentEmails / 1024;
				}
			} catch (Exception e) {
				logger.error("Could not calculate average mail size: ", e);

			}
			return 0L;
		}, mailingId);
	}
	
	@DaoUpdateReturnValueCheck
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
			+ " (category, category_index, mailing_id, group_id, rate)"
			+ " SELECT ?, ?, a.mailing_id, a.group_id, CASE WHEN (b.value = 0) THEN 0 ELSE CAST(a.value AS DOUBLE)/b.value END"
			+ " FROM tmp_report_aggregation_" + tempTableID + "_tbl a INNER JOIN tmp_report_aggregation_" + tempTableID + "_tbl b"
			+ " ON a.category_index = " + CommonKeys.CLICKS_GROSS_INDEX +
				" AND b.category_index = " + CommonKeys.OPENINGS_GROSS_MEASURED_INDEX + " AND a.mailing_id = b.mailing_id AND a.group_id = b.group_id";
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

        for (Map<String, Object> row: selectEmbedded(logger, totalCountQuery.toString())) {
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

        for (Map<String, Object> row: selectEmbedded(logger, totalCountQuery.toString())) {
        	updateEmbedded(logger, updateRateQuery.toString(), ((Number) row.get("total")).intValue(), ((Number) row.get("total")).intValue(), ((Number) row.get("mailing_id")).intValue());
        }
    }
	
	@DaoUpdateReturnValueCheck
	private void insertDeliveredIntoTempTable(int tempTableID, OptimizationMailingData data, int companyID, String targetSQL) throws Exception {
		int mailingId = data.getMailingId();
		if (successTableActivated(companyID) && hasSuccessTableData(companyID, mailingId)) {
            
            boolean isMailingNotExpired = isMailingNotExpired(mailingId);
			int deliveredMails = selectNumberOfDeliveredMails(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, targetSQL, null, null);
			if (deliveredMails > 0 || isMailingNotExpired) {
				
				insertIntoSummaryTempTable(tempTableID, CommonKeys.DELIVERED_EMAILS_DELIVERED, CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX,
						mailingId, deliveredMails, data.getGroupId());
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
					mailingId, value, data.getGroupId());
        } else {
			insertIntoSummaryTempTable(tempTableID, CommonKeys.REVENUE, CommonKeys.REVENUE_INDEX,
					mailingId, 0, data.getGroupId());
		}
	}
	
	@DaoUpdateReturnValueCheck
	private void insertRevenueIntoTempTable(int tempTableID, OptimizationMailingData data, int companyID, String targetSQL) throws Exception {
		if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_num_tbl")) {
			double revenue = selectRevenue(companyID, data.getMailingId(), targetSQL, null, null);
			
			insertIntoSummaryTempTable(tempTableID, CommonKeys.REVENUE, CommonKeys.REVENUE_INDEX,
					data.getMailingId(), (int) (100 * revenue), data.getGroupId());
		}
	}
	
	@DaoUpdateReturnValueCheck
	private void insertOptOutsIntoTempTable(int tempTableID, OptimizationMailingData data, int companyID, String targetSQL) throws Exception {
		int mailingId = data.getMailingId();
		int optouts = selectOptOuts(companyID, mailingId, targetSQL, CommonKeys.TYPE_ALL_SUBSCRIBERS, null, null);
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPT_OUTS, CommonKeys.OPT_OUTS_INDEX,
				mailingId, optouts, data.getGroupId());
	}
	
	@DaoUpdateReturnValueCheck
	private void insertSoftbouncesUndeliverable(int tempTableID, OptimizationMailingData data, int companyID, boolean showSoftbounces) throws Exception {
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
					mailingId, value, data.getGroupId());
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
	private void insertBouncesIntoTempTable(int tempTableID, OptimizationMailingData data, int companyID, String targetSQL) throws Exception {
        int mailingId = data.getMailingId();
		if (!isMailingBouncesExpire(companyID, mailingId) || !isOracleDB()) {
			int hardbounces = selectHardbouncesFromBounces(companyID, mailingId, targetSQL, CommonKeys.TYPE_ALL_SUBSCRIBERS, null, null);

			insertIntoSummaryTempTable(tempTableID, CommonKeys.HARD_BOUNCES, CommonKeys.HARD_BOUNCES_INDEX,
					mailingId, hardbounces, data.getGroupId());
		} else {
			int hardbounces = selectHardbouncesFromBindings(companyID, mailingId, targetSQL, CommonKeys.TYPE_ALL_SUBSCRIBERS, null, null);
			
			insertIntoSummaryTempTable(tempTableID, CommonKeys.HARD_BOUNCES, CommonKeys.HARD_BOUNCES_INDEX,
					mailingId, hardbounces, data.getGroupId());
		}
	}
	
	@DaoUpdateReturnValueCheck
	private void insertOpenedAnonymousIntoTempTable(int tempTableID, OptimizationMailingData data, int companyID) throws Exception {
        int anonymousOpenings = selectAnonymousOpenings(companyID, data.getMailingId(), null, null);
        
        insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENINGS_ANONYMOUS, CommonKeys.OPENINGS_ANONYMOUS_INDEX,
				data.getMailingId(), anonymousOpenings, data.getGroupId());
	}
	
	@DaoUpdateReturnValueCheck
	private void insertOpenedGrossIntoTempTable(int tempTableID, OptimizationMailingData data, int companyID, String targetSQL) throws Exception {
		int openings = selectOpenings(companyID, data.getMailingId(), null, targetSQL, null, null);
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENINGS_GROSS_MEASURED, CommonKeys.OPENINGS_GROSS_MEASURED_INDEX,
				data.getMailingId(), openings, data.getGroupId());
	}
	
	@DaoUpdateReturnValueCheck
	private void insertOpenedInvisibleIntoTempTable(int tempTableID, OptimizationMailingData data, int companyID, String targetSQL) throws Exception {
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
				mailingId, invisibleOpeners, data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_TOTAL, CommonKeys.OPENERS_TOTAL_INDEX,
				mailingId, measuredOpeners + invisibleOpeners, data.getGroupId());
	}
	
	private void insertOpenersIntoTempTable(int tempTableID, OptimizationMailingData data, int companyID, String targetSQL) throws Exception {
        int mailingId = data.getMailingId();
		int totalOpeners = selectOpeners(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, targetSQL, null, null);
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_MEASURED, CommonKeys.OPENERS_MEASURED_INDEX,
				mailingId, totalOpeners, data.getGroupId());

		// If a customer is within one of theses deviceclasses he hasn't opened the mail with any other deviceclass.
		Map<DeviceClass, Integer> openersByDevice =
				selectOpenersByDeviceClassWithoutCombinations(companyID, mailingId, CommonKeys.TYPE_ALL_SUBSCRIBERS, targetSQL, null, null);

		// Calculating the openers which used more than one deviceclass for link-opens
		int openersWithDeviceClassCombinations = totalOpeners;
		for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
			openersWithDeviceClassCombinations -= openersByDevice.getOrDefault(deviceClass, 0);
		}
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_TRACKED, CommonKeys.OPENERS_TRACKED_INDEX,
				mailingId, totalOpeners, data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_PC, CommonKeys.OPENERS_PC_INDEX,
				mailingId, openersByDevice.getOrDefault(DeviceClass.DESKTOP, 0), data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_MOBILE, CommonKeys.OPENERS_MOBILE_INDEX,
				mailingId, openersByDevice.getOrDefault(DeviceClass.MOBILE, 0), data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_TABLET, CommonKeys.OPENERS_TABLET_INDEX,
				mailingId, openersByDevice.getOrDefault(DeviceClass.TABLET, 0), data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_SMARTTV, CommonKeys.OPENERS_SMARTTV_INDEX,
				mailingId, openersByDevice.getOrDefault(DeviceClass.SMARTTV, 0), data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.OPENERS_PC_AND_MOBILE, CommonKeys.OPENERS_PC_AND_MOBILE_INDEX,
				mailingId, openersWithDeviceClassCombinations, data.getGroupId());

	}
	
	@DaoUpdateReturnValueCheck
	private void insertClicksAnonymousIntoTempTable(int tempTableID, OptimizationMailingData data, int companyID) throws Exception {
		int anonymousClicks = selectAnonymousClicks(companyID, data.getMailingId(), null, null);
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKS_ANONYMOUS, CommonKeys.CLICKS_ANONYMOUS_INDEX,
				data.getMailingId(), anonymousClicks, data.getGroupId());
	}
	
	private String allocateTargetSQL(List<OptimizationMailingData> optimizationMailingsData, int companyID) {
		if (CollectionUtils.isEmpty(optimizationMailingsData)) {
			return "";
		}
		
		int targetId = optimizationMailingsData.get(0).getTargetGroupId();
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
	public int insertSendIntoTempTable(int tempTableID, OptimizationMailingData data, @VelocityCheck int companyID, String targetSql) throws Exception {
		String recipientsType = data.getTargetGroupId() == ALL_SUBSCRIBERS_TARGETGROUPID ? CommonKeys.TYPE_WORLDMAILING : CommonKeys.TYPE_ALL_SUBSCRIBERS;
		int mailsSent = getNumberSentMailings(companyID, data.getMailingId(), recipientsType, targetSql, null, null);

        insertIntoSummaryTempTable(tempTableID, CommonKeys.DELIVERED_EMAILS, CommonKeys.DELIVERED_EMAILS_INDEX,
				data.getMailingId(), mailsSent, data.getGroupId());
        
        return mailsSent;
	}
	
	
	@DaoUpdateReturnValueCheck
	public void insertClickersIntoTempTable(int tempTableID, OptimizationMailingData data, @VelocityCheck int companyID, String targetSql, String recipientsType) throws Exception {
        int mailingId = data.getMailingId();
		int totalClicks = selectClicks(companyID, mailingId, recipientsType, targetSql, null, null);
		int totalClickers = selectClickers(companyID, mailingId, recipientsType, targetSql, null, null);
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER, CommonKeys.CLICKER_INDEX,
				mailingId,
				totalClickers, data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKS_GROSS, CommonKeys.CLICKS_GROSS_INDEX,
				mailingId,
				totalClicks, data.getGroupId());

		// If a customer is within one of theses deviceclasses he hasn't clicked in the mail with any other deviceclass.
		Map<DeviceClass,Integer> clickersByDevices =
				selectClickersByDeviceClassWithoutCombinations(companyID, mailingId, recipientsType, targetSql, null, null);

		// Calculating the clickers which used more than one deviceclass for link-clicks
		int unknownDevicesClickers = totalClickers;
		for (DeviceClass deviceClass : CommonKeys.AVAILABLE_DEVICECLASSES) {
			unknownDevicesClickers -= clickersByDevices.getOrDefault(deviceClass, 0);
		}
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_TRACKED, CLICKER_TRACKED_INDEX,
				mailingId, totalClickers, data.getGroupId());

		insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_PC, CommonKeys.CLICKER_PC_INDEX,
				mailingId, clickersByDevices.getOrDefault(DeviceClass.DESKTOP, 0), data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_MOBILE, CommonKeys.CLICKER_MOBILE_INDEX,
				mailingId, clickersByDevices.getOrDefault(DeviceClass.MOBILE, 0), data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_TABLET, CommonKeys.CLICKER_TABLET_INDEX,
				mailingId, clickersByDevices.getOrDefault(DeviceClass.TABLET, 0), data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_SMARTTV, CommonKeys.CLICKER_SMARTTV_INDEX,
				mailingId, clickersByDevices.getOrDefault(DeviceClass.SMARTTV, 0), data.getGroupId());
		
		insertIntoSummaryTempTable(tempTableID, CommonKeys.CLICKER_PC_AND_MOBILE, CommonKeys.CLICKER_PC_AND_MOBILE_INDEX,
				mailingId, unknownDevicesClickers, data.getGroupId());
    }
	
	public int createSummaryTempTable() throws Exception {
		int tempTableID = getNextTmpID();
		String createTable = "CREATE TABLE tmp_report_aggregation_" + tempTableID + "_tbl" +
				" (" +
				"category VARCHAR(200)" +
				", category_index INTEGER" +
				", mailing_id INTEGER" +
				", group_id INTEGER" +
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
				", group_id INTEGER" +
				", is_winner INTEGER" +
				", base_mailing_id INTEGER" +
				", target_group_id INTEGER" +
				", target_group_name VARCHAR(200)" +
				", avg_mailing_size DOUBLE" +
				", send_date TIMESTAMP DEFAULT NULL" +
				")";
		updateEmbedded(logger, createTable);
		return tempTableID;
	}
	
	private void insertIntoSummaryTempTable(int tempTableID, String category, int categoryIndex, int mailingId, int value, int groupId) throws Exception {
		String insertSql = "INSERT INTO tmp_report_aggregation_" + tempTableID + "_tbl " +
				"(category, category_index, mailing_id, group_id, value, rate) VALUES (?, ?, ?, ?, ?, 0)";
        updateEmbedded(logger, insertSql, category, categoryIndex, mailingId, groupId, value);
    }
    
    private void insertTotalValueByCategories(int tempTableID, List<Integer> mailingIds) throws Exception {
		String insertSql = "INSERT INTO tmp_report_aggregation_" + tempTableID + "_tbl " +
				"(category, category_index, mailing_id, group_id, value, rate) " +
				" SELECT a.category, a.category_index, 0, -1, CASE WHEN (SUM(a.value) IS NULL OR SUM(a.value) = 0) THEN 0 ELSE SUM(a.value) END AS total, 0" +
				" FROM tmp_report_aggregation_" + tempTableID + "_tbl a WHERE a.mailing_id IN (" + StringUtils.join(mailingIds, ", ") + ")" +
				" GROUP BY a.category, a.category_index";
		
		updateEmbedded(logger, insertSql);
    }
    
	private void insertOptimizationMailingData(int tempTableID, OptimizationMailingData mailing) throws Exception {
		String insertSql = "INSERT INTO tmp_report_aggregation_" + tempTableID + "_tbl " +
				"(mailing_id, mailing_name, mailing_subject, group_id, is_winner, base_mailing_id, " +
				"target_group_id, target_group_name, avg_mailing_size, send_date) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        updateEmbedded(logger, insertSql, mailing.getMailingId(), mailing.getMailingName(), mailing.getMailingSubject(),
				mailing.getGroupId(), mailing.isWinner(), mailing.getResultMailing(),
				mailing.getTargetGroupId(), mailing.getTargetGroupName(),
				mailing.getAvgMailSize(), mailing.getSendDate());
	}
	
	public static class OptimizationMailingSummaryRow extends MailingSummaryRow {
		private int mailingId;
		private String mailingName;
		private int groupId;
		private String baseMailingName;
		private boolean winner;
		private int baseGroupId;
		
		public OptimizationMailingSummaryRow() {
		}
		
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
		
		public void setGroupId(int groupId) {
			this.groupId = groupId;
		}
		
		public int getGroupId() {
			return groupId;
		}
		
		public String getBaseMailingName() {
			return baseMailingName;
		}
		
		public void setBaseMailingName(String baseMailingName) {
			this.baseMailingName = baseMailingName;
		}
		
		public boolean isWinner() {
			return winner;
		}
		
		public void setWinner(boolean winner) {
			this.winner = winner;
		}
		
		public void setBaseGroupId(int baseGroupId) {
			this.baseGroupId = baseGroupId;
		}
		
		public int getBaseGroupId() {
			return baseGroupId;
		}
	}
	public static class OptimizationMailingData {
		private int mailingId;
		private String mailingName;
		private String mailingSubject;
		private String targetGroupName;
		private boolean isWinner;
		private int resultMailing;
		private int groupId;
		private int targetGroupId;
		private long avgMailSize;
		private Date sendDate;

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
		
		public boolean isWinner() {
			return isWinner;
		}
		
		public void setWinner(boolean winner) {
			isWinner = winner;
		}
		
		public int getResultMailing() {
			return resultMailing;
		}
		
		public void setResultMailing(int resultMailing) {
			this.resultMailing = resultMailing;
		}
		
		public void setGroupId(int groupId) {
			this.groupId = groupId;
		}
		
		public int getGroupId() {
			return groupId;
		}
		
		public void setTargetGroupId(int targetGroupId) {
			this.targetGroupId = targetGroupId;
		}
		
		public int getTargetGroupId() {
			return targetGroupId;
		}
		
		public long getAvgMailSize() {
			return avgMailSize;
		}
		
		public void setAvgMailSize(long avgMailSize) {
			this.avgMailSize = avgMailSize;
		}

		public Date getSendDate() {
			return sendDate;
		}

		public void setSendDate(Date sendDate) {
			this.sendDate = sendDate;
		}
	}
	
	public void destroy() throws Exception {
		dropTempTable(summaryTempTableId);
		dropTempTable(mailingTempTableId);
	}
}
