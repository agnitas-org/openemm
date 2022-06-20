/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.BOUNCES;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.BOUNCES_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CLICKER;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.CLICKER_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.DELIVERED_EMAILS;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.DELIVERED_EMAILS_DELIVERED;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.DELIVERED_EMAILS_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPENERS;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPENERS_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPT_OUTS;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.OPT_OUTS_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.REVENUE;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.REVENUE_INDEX;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.sql.DataSource;

import org.agnitas.beans.BindingEntry.UserType;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DbUtilities;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.common.MailingType;
import com.agnitas.reporting.birt.external.beans.CompareStatCsvRow;
import com.agnitas.reporting.birt.external.beans.CompareStatRow;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.dao.ComCompanyDao;
import com.agnitas.reporting.birt.external.dao.impl.ComCompanyDaoImpl;

public class MailingCompareDataSet extends ComparisonBirtDataSet  {
	private static final transient Logger logger = LogManager.getLogger(MailingCompareDataSet.class);

    public static final int TARGET_NAME_LENGTH_MAX = 28;
    
    protected class TempRow {
		private String category;
		private int categoryIndex;
		private int mailingId;
		private String mailingName;
		private int targetGroupId;
		private String targetGroup;
		private int targetGroupIndex;
		private int value;
		private int rate;
  
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
    
        public int getRate() {
            return rate;
        }
    
        public void setRate(int rate) {
            this.rate = rate;
        }
    }

    public int prepareReport(String mailingIdsStr, @VelocityCheck int companyId, String targetsStr, String recipientType) throws Exception {
		int tempTableID = createTempTable();
        if (!StringUtils.containsOnly(mailingIdsStr, "1234567890,")) {
            logger.error("Wrong format of mailing-IDs string");
            return 0;
        }
        if (!StringUtils.containsOnly(targetsStr, "1234567890,")) {
            logger.error("Wrong format of targetGroup-IDs string");
            return 0;
        }
        List<LightTarget> targets = getTargets(targetsStr, companyId);
        if (targets == null) {
            targets = new ArrayList<>();
        }
        insertSendIntoTempTable(mailingIdsStr, tempTableID, companyId, targets, recipientType);
        insertDeliveredIntoTempTable(mailingIdsStr, tempTableID, companyId, targets, recipientType);
        insertOpensIntoTempTable(mailingIdsStr, tempTableID, companyId, targets, recipientType);
        insertClicksIntoTempTable(mailingIdsStr, tempTableID, companyId, targets, recipientType);
        insertOptOutsIntoTempTable(mailingIdsStr, tempTableID, companyId, targets, recipientType);
        insertBouncesIntoTempTable(mailingIdsStr, tempTableID, companyId, targets, recipientType);
        insertRevenueIntoTempTable(mailingIdsStr, tempTableID, companyId, targets, recipientType);
        insertMailingNames(mailingIdsStr, tempTableID, companyId);
        updateRates(mailingIdsStr, tempTableID, companyId, targets);
		return tempTableID;
	}

	@DaoUpdateReturnValueCheck
    private void updateRates(String mailingIdsStr, int tempTableID, @VelocityCheck int companyId, List<LightTarget> targets) throws Exception {
        String categoriesNeedRates = BOUNCES_INDEX + ", " + CLICKER_INDEX + ", " + OPENERS_INDEX + ", " + OPT_OUTS_INDEX;
        String[] mailings = mailingIdsStr.split(",");
        for (String mailingIdStr : mailings) {
            int mailingId = Integer.parseInt(mailingIdStr);
            for (int targetIndex = ALL_SUBSCRIBERS_INDEX; targetIndex <= targets.size() + 1; targetIndex++) {
                String queryTotalSend = "SELECT value FROM " + getTempTableName(tempTableID) + " WHERE targetgroup_index = ? AND category_index = ? AND mailing_id = ? ";
                int totalSend = selectEmbedded(logger, queryTotalSend, Integer.class, targetIndex, DELIVERED_EMAILS_INDEX, mailingId);
                if (totalSend > 0) {
                    String queryUpdateRate = "UPDATE " + getTempTableName(tempTableID) + "  SET rate = (value * 1.0) / ?  WHERE targetgroup_index = ? " +
                            "AND mailing_id = ? AND category_index IN (" + categoriesNeedRates + ")";
                    updateEmbedded(logger, queryUpdateRate , totalSend, targetIndex, mailingId);
                }
            }
        }
    }

    public int getTargetNum(String targetsStr) {
        if (targetsStr != null && !targetsStr.isEmpty()) {
            return targetsStr.split(",").length;
        }

        return 0;
    }

	@DaoUpdateReturnValueCheck
    private void insertMailingNames(String mailingIds, int tempTableID, @VelocityCheck int companyId) throws Exception {
        DataSource dataSource = getDataSource();
        JdbcTemplate template = new JdbcTemplate(dataSource);
        String sql = "select mailing_id, shortname from mailing_tbl where mailing_id in (<MAILING_IDS>)";
        sql = sql.replace("<MAILING_IDS>", mailingIds);
		List<Map<String, Object>> result = template.queryForList(sql);
        for (Map<String, Object> rowMap : result) {
            Integer mailingId = ((Number)rowMap.get("mailing_id")).intValue();
            String name = (String)rowMap.get("shortname");
            String updateStr = "UPDATE " + getTempTableName(tempTableID) + " SET mailing_name = ? WHERE mailing_id = ?";
            updateEmbedded(logger, updateStr, name, mailingId);
        }
    }

    public List<CompareStatRow> getSummaryData(int tempTableID, Locale locale) throws Exception {
		List<CompareStatRow> summaryData = getResultsFromTempTable(tempTableID, locale);
		return summaryData;
	}

    public List<CompareStatCsvRow> getCsvSummaryData(int tempTableID, Locale locale) throws Exception {
        Map<String, CompareStatCsvRow> summaryCsvData = new HashMap<>();
        List<CompareStatRow> summaryData = getResultsFromTempTable(tempTableID, locale);
        for (CompareStatRow compareStat : summaryData) {
            int mailingId = compareStat.getMailingId();
            int groupId = compareStat.getTargetGroupId();
            String key = mailingId + "_" + groupId;
            CompareStatCsvRow statCsvRow = summaryCsvData.get(key);
            if (statCsvRow == null) {
                statCsvRow = new CompareStatCsvRow(mailingId, groupId);
                statCsvRow.setMailingNameFull(compareStat.getMailingNameFull());
                statCsvRow.setTargetGroupName(compareStat.getTargetGroupName());
                for (CompareStatRow compareStatInner : summaryData) {
                    int innerMailingId = compareStatInner.getMailingId();
                    int innerGroupId = compareStatInner.getTargetGroupId();
                    if (innerMailingId == mailingId && innerGroupId == groupId) {
                        int categoryIndex = compareStatInner.getCategoryindex();
                        switch (categoryIndex) {
                            case CommonKeys.DELIVERED_EMAILS_INDEX:
                                statCsvRow.setEmailsSentCount(compareStatInner.getCount());
                                break;
                            case CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX:
                                statCsvRow.setEmailsDeliveredCount(compareStatInner.getCount());
                                break;
                            case CommonKeys.OPENERS_MEASURED_INDEX:
                                statCsvRow.setOpenersCount(compareStatInner.getCount());
                                statCsvRow.setOpenersRate(compareStatInner.getRate());
                                break;
                            case CommonKeys.CLICKER_INDEX:
                                if (statCsvRow.getClickingCount() == 0) {
                                    statCsvRow.setClickingCount(compareStatInner.getCount());
                                    statCsvRow.setClickingRate(compareStatInner.getRate());
                                }
                                break;
                            case CommonKeys.OPT_OUTS_INDEX:
                                statCsvRow.setSignedoffCount(compareStatInner.getCount());
                                statCsvRow.setSignedoffRate(compareStatInner.getRate());
                                break;
                            case CommonKeys.BOUNCES_INDEX:
                                statCsvRow.setBouncesCount(compareStatInner.getCount());
                                statCsvRow.setBouncesRate(compareStatInner.getRate());
                                break;
                            case CommonKeys.REVENUE_INDEX:
                                statCsvRow.setRevenueCount(compareStatInner.getCount());
                                break;
							default:
								break;
                        }
                    }
                }
                summaryCsvData.put(key, statCsvRow);
            }
        }
		return new ArrayList<>(summaryCsvData.values());
	}

    private void insertSendIntoTempTable(String mailingIds, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets, String recipientsType) throws Exception {
        if (CollectionUtils.isNotEmpty(targets) && isMailingTrackingActivated(companyID)) {
            StringBuilder queryBuilder = new StringBuilder("SELECT mailing_id, ");
			
            List<String> sumTargetsParams = new ArrayList<>();
			List<String> caseTargetParams = new ArrayList<>();
			
			for (LightTarget target : targets) {
                sumTargetsParams.add(String.format(" SUM(tg_%1$d_send)  AS tg_%1$d", target.getId()));
                String caseTargetSql = getSendCaseTemplate().replace("<TARGETSQL>",
                        target.getTargetSQL()).replace("<TARGETGROUP>", String.format("tg_%d_send", target.getId()));
                caseTargetParams.add(caseTargetSql.substring(0, caseTargetSql.lastIndexOf(",")));
			}

			queryBuilder.append(StringUtils.join(sumTargetsParams, ", "));
			queryBuilder.append(" FROM ( ");
			
			queryBuilder.append(" SELECT sys_mailing_id AS mailing_id, ");
			queryBuilder.append(StringUtils.join(caseTargetParams, ", "));
			
			queryBuilder
			        .append(" FROM (")
			        .append("      SELECT cust.*, succ.mailing_id AS sys_mailing_id ")
			        .append("        FROM customer_<COMPANYID>_tbl cust ")
			        .append("           , success_<COMPANYID>_tbl succ ")
			        .append("           , mailing_tbl m ")
			        .append("       WHERE cust.customer_id = succ.customer_id ")
			        .append("         AND succ.mailing_id IN (<MAILING_IDS>) ")
			        .append("         AND succ.mailing_id = m.mailing_id ")
			        .append("         AND m.mailing_type <> ").append(MailingType.INTERVAL.getCode())
			        .append("       UNION ALL ")
			        .append("      SELECT cust.*, x.mailing_id AS sys_mailing_id ")
			        .append("        FROM customer_<COMPANYID>_tbl cust ")
			        .append("           , interval_track_<COMPANYID>_tbl x ")
			        .append("           , mailing_tbl m ")
			        .append("       WHERE cust.customer_id = x.customer_id ")
			        .append("         AND x.mailing_id IN (<MAILING_IDS>) ")
			        .append("         AND x.mailing_id = m.mailing_id ")
			        .append("         AND m.mailing_type = ").append(MailingType.INTERVAL.getCode())
			    .append(" ) cust ) main_data GROUP BY mailing_id");
			
            insertCategoryDataToTempTable(mailingIds, companyID, queryBuilder.toString(), tempTableID,
                    DELIVERED_EMAILS, DELIVERED_EMAILS_INDEX, targets, true, false);
        } else if(CollectionUtils.isNotEmpty(targets)) {
            fillCategoryValuesWith(mailingIds, companyID, tempTableID,
                    DELIVERED_EMAILS, DELIVERED_EMAILS_INDEX, targets, true, false, 0);
        }
        String sendQuery = createSendQuery(recipientsType);
        insertCategoryDataToTempTable(mailingIds, companyID, sendQuery, tempTableID,
                DELIVERED_EMAILS, DELIVERED_EMAILS_INDEX, targets, false, true);
	}
	
	private void insertDeliveredIntoTempTable(String mailingIds, int tempTableId, @VelocityCheck int companyId, List<LightTarget> targets, String recipientsType) throws Exception {
        List<Integer> mailingIdsParsed = Arrays.stream(StringUtils.split(mailingIds, ","))
                .map(NumberUtils::toInt)
                .filter(v -> v != 0)
                .collect(Collectors.toList());
        
        if (successTableActivated(companyId)) {
            List<TempRow> results = new ArrayList<>();
            List<LightTarget> allTargets = getTargetListWithAllSubscriberTarget(targets);
    
            int targetDisplayIndex = ALL_SUBSCRIBERS_INDEX;
            for (LightTarget target : allTargets) {
                for (int mailingId : mailingIdsParsed) {
                    int deliveredMails =
                            selectNumberOfDeliveredMails(companyId, mailingId, recipientsType,
                                    target.getTargetSQL(), "", "");
                    
                    TempRow newItem = new TempRow();
                    newItem.setCategory(DELIVERED_EMAILS_DELIVERED);
                    newItem.setCategoryIndex(DELIVERED_EMAILS_DELIVERED_INDEX);
                    newItem.setMailingId(mailingId);
                    newItem.setMailingName("name");
                    newItem.setTargetGroup(target.getName());
                    newItem.setTargetGroupId(target.getId());
                    newItem.setTargetGroupIndex(targetDisplayIndex);
                    newItem.setValue(deliveredMails);
                    newItem.setRate(deliveredMails);
                    results.add(newItem);
                }
                targetDisplayIndex++;
            }
    
            insertIntoTempTable(tempTableId, results);
        } else {
            final boolean isActivated = isMailingTrackingActivated(companyId);
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder
                .append("SELECT mailing_id, targetgroup, targetgroup_index, targetgroup_id,")
                    .append(" SUM(CASE WHEN (category_index = ").append(CommonKeys.DELIVERED_EMAILS_INDEX).append(") THEN value ELSE 0 END)")
                    .append(" -")
                    .append(" SUM(CASE WHEN (category_index = ").append(CommonKeys.HARD_BOUNCES_INDEX).append(") THEN value ELSE 0 END) AS value")
                .append(" FROM ").append(getTempTableName(tempTableId))
                .append(" WHERE category_index IN (").append(CommonKeys.DELIVERED_EMAILS_INDEX).append(", ").append(CommonKeys.HARD_BOUNCES_INDEX).append(")")
                .append(" GROUP BY mailing_id, targetgroup, targetgroup_index, targetgroup_id");
            
            List<Map<String, Object>> selectResult = selectEmbedded(logger, queryBuilder.toString());
            List<Integer> mailingsLeft = mailingIdsParsed;
            for (Map<String, Object> row : selectResult) {
                Integer mailingId = ((Number)row.get("mailing_id")).intValue();
        
                String targetGroup = (String) row.get("targetgroup");
                int targetGroupId = ((Number) row.get("targetgroup_id")).intValue();
                int targetGroupIndex = ((Number) row.get("targetgroup_index")).intValue();
                int value = ((Number) row.get("value")).intValue();
        
                if (targetGroupId != ALL_SUBSCRIBERS_TARGETGROUPID && !isActivated) {
                    value = -1;
                }
                
                insertIntoTempTable(tempTableId, CommonKeys.DELIVERED_EMAILS_DELIVERED, CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX,
                    mailingId, value, targetGroupId, targetGroup, targetGroupIndex);
            }
            
            for (Integer mailing : mailingsLeft) {
                updateEmbedded(logger, getTempInsertQuery(tempTableId), CommonKeys.DELIVERED_EMAILS_DELIVERED, CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX,
                        mailing, 0, ALL_SUBSCRIBERS_TARGETGROUPID, CommonKeys.ALL_SUBSCRIBERS, ALL_SUBSCRIBERS_INDEX);
            }
        }
    }
    
    private void insertIntoTempTable(int tempTableID, List<TempRow> rows) throws Exception {
    	for (TempRow tempRow : rows) {
    		insertIntoTempTable(tempTableID,
                    tempRow.getCategory(),
                    tempRow.getCategoryIndex(),
                    tempRow.getMailingId(),
                    tempRow.getValue(),
                    tempRow.getTargetGroupId(),
                    tempRow.getTargetGroup(),
                    tempRow.getTargetGroupIndex());
    	}
    }

    private void insertIntoTempTable(int tempTableId, String category, int categoryIndex, int mailingId, int value, int targetgroupId, String targetgroup, int targetgroupIndex) throws Exception {
    	String insertSql = getTempInsertQuery(tempTableId);
        updateEmbedded(logger, insertSql, category, categoryIndex, mailingId, value, targetgroupId, targetgroup, targetgroupIndex);
	}

    private void insertOpensIntoTempTable(String mailingIds, int tempTableID, @VelocityCheck int companyID, List<LightTarget> lightTargets, String recipientsType) throws Exception {
        String openQuery = createOpenQuery(lightTargets, recipientsType);
        insertCategoryDataToTempTable(mailingIds, companyID, openQuery, tempTableID, OPENERS, OPENERS_INDEX, lightTargets, true, true);
    }

    private void insertClicksIntoTempTable(String mailingIds, int tempTableID, @VelocityCheck int companyID, List<LightTarget> lightTargets, String recipientsType) throws Exception {
        if (!lightTargets.isEmpty()) {
            String queryForTargets = createClicksQueryForTargets(recipientsType);
            for (LightTarget target : lightTargets) {
                String query = queryForTargets.replace("<TARGETSQL>", target.getTargetSQL());
                query = query.replace("<TG_ID>", String.valueOf(target.getId()));
                insertCategoryDataToTempTable(mailingIds, companyID, query, tempTableID, CLICKER, CLICKER_INDEX, lightTargets, true, false);
            }
        }
        String clicksQuery = createClicksQuery(recipientsType);
        insertCategoryDataToTempTable(mailingIds, companyID, clicksQuery, tempTableID, CLICKER, CLICKER_INDEX, lightTargets, false, true);
    }

    private void insertOptOutsIntoTempTable(String mailingIds, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets, String recipientsType) throws Exception {
		if (!targets.isEmpty()) {
            String bouncesQuery = createOptOutsQueryForTargets(recipientsType, targets);
            insertCategoryDataToTempTable(mailingIds, companyID, bouncesQuery, tempTableID, OPT_OUTS, OPT_OUTS_INDEX, targets, true, false);
        }
        String bouncesQuery = createOptOutsQuery(recipientsType);
        insertCategoryDataToTempTable(mailingIds, companyID, bouncesQuery, tempTableID, OPT_OUTS, OPT_OUTS_INDEX, targets, false, true);
    }

    private void insertBouncesIntoTempTable(String mailingIds, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets, String recipientsType) throws Exception {
        String oldMails = "";
        String newMails = "";
        Map<Integer, Boolean> mailingsSendAge = getMailingBouncesExpire(companyID, mailingIds);
        for (Entry<Integer, Boolean> entry : mailingsSendAge.entrySet()) {
            if (entry.getValue()) {
                oldMails += entry.getKey() + ",";
            } else {
                newMails += entry.getKey() + ",";
            }
        }
        if (!newMails.isEmpty()) {
            newMails = newMails.substring(0, newMails.length() - 1);

            if (!targets.isEmpty()) {
                String bouncesQuery = createBouncesQueryForTargets(recipientsType, targets);
                insertCategoryDataToTempTable(newMails, companyID, bouncesQuery, tempTableID, BOUNCES, BOUNCES_INDEX, targets, true, false);
            }
            String bouncesQuery = createBouncesQuery(recipientsType);
            insertCategoryDataToTempTable(newMails, companyID, bouncesQuery, tempTableID, BOUNCES, BOUNCES_INDEX, targets, false, true);
        }
        if (!oldMails.isEmpty() && DbUtilities.checkIfTableExists(getDataSource(), "benchmark_mailing_stat_tbl")) {
            oldMails = oldMails.substring(0, oldMails.length() - 1);
            insertBouncesFromBenchmarkTable(oldMails, companyID, tempTableID, targets);
        }
    }

	@DaoUpdateReturnValueCheck
    private void insertBouncesFromBenchmarkTable(String mailingIds, @VelocityCheck int companyID, int tempTableID, List <LightTarget> targets) throws Exception {
        String query = "SELECT COALESCE(bounces_hard, 0) bounces, mailing_id FROM benchmark_mailing_stat_tbl " +
                "WHERE company_id = ? and mailing_id in (" + mailingIds + ") ORDER BY days_between DESC";
        JdbcTemplate template = new JdbcTemplate(getDataSource());
        HashMap<Integer, Integer> bounceMap = new HashMap<>();
		List<Map<String, Object>> result = template.queryForList(query, companyID);
        for (Map<String, Object> row : result) {
            int mailingId = ((Number) row.get("mailing_id")).intValue();
            if (bounceMap.get(mailingId) == null) {
                int bounces = ((Number) row.get("bounces")).intValue();
                bounceMap.put(mailingId, bounces);
            }
        }
        String[] ids = mailingIds.split(",");
        for (String id : ids) {
            int mailingId = Integer.parseInt(id.trim());
            bounceMap.putIfAbsent(mailingId, 0);
        }

        String insertQuery = getTempInsertQuery(tempTableID);
        for (Entry<Integer, Integer> entry : bounceMap.entrySet()) {
            updateEmbedded(logger, insertQuery, BOUNCES, BOUNCES_INDEX, entry.getKey(), entry.getValue(),
                    ALL_SUBSCRIBERS_TARGETGROUPID, CommonKeys.ALL_SUBSCRIBERS, ALL_SUBSCRIBERS_INDEX);
            int targetGroupIndex = ALL_SUBSCRIBERS_INDEX + 1;
            // when we take data from benchmark table - we can't provide any data on target groups.
            // so here we just insert 0 values for target groups
            for (LightTarget target : targets) {
                updateEmbedded(logger, insertQuery, BOUNCES, BOUNCES_INDEX, entry.getKey(), 0,
                        target.getId(), target.getName(), targetGroupIndex);
                targetGroupIndex++;
            }
        }
    }

    private void insertRevenueIntoTempTable(String mailingIds, int tempTableID, @VelocityCheck int companyID, List<LightTarget> targets, String recipientsType) throws Exception {
		ComCompanyDao companyDao = new ComCompanyDaoImpl();
		((ComCompanyDaoImpl) companyDao).setDataSource(getDataSource());
        if (DbUtilities.checkIfTableExists(getDataSource(), "rdirlog_" + companyID + "_val_num_tbl")) {
            if (!targets.isEmpty()) {
                String revenueQuery = createRevenueQueryForTargets(recipientsType, targets);
                insertCategoryDataToTempTable(mailingIds, companyID, revenueQuery, tempTableID, REVENUE, REVENUE_INDEX, targets, true, true);
            } else {
                String revenueQuery = createRevenueQuery(recipientsType);
                insertCategoryDataToTempTable(mailingIds, companyID, revenueQuery, tempTableID, REVENUE, REVENUE_INDEX, targets, false, true);
            }
        } else {
            fillCategoryValuesWith(mailingIds, companyID, tempTableID, REVENUE, REVENUE_INDEX, targets, true, true, 0);
        }
	}

	@DaoUpdateReturnValueCheck
    private void insertCategoryDataToTempTable(String mailingIds, @VelocityCheck int companyID, String query, int tempTableID, String category, int categoryIndex,
                                               List <LightTarget> targets, boolean insertTargets, boolean insertTotal) throws Exception {
        String insertQuery = getTempInsertQuery(tempTableID);
		JdbcTemplate template = new JdbcTemplate(getDataSource());
        query = query.replace("<MAILING_IDS>", mailingIds);
        query = query.replace("<COMPANYID>", String.valueOf(companyID));
		List<Map<String, Object>> resultData = template.queryForList(query);
		
        List<Integer> mailingsLeft = Arrays.stream(StringUtils.split(mailingIds, ","))
                .map(NumberUtils::toInt)
                .filter(v -> v !=0)
                .collect(Collectors.toList());
        
        if (resultData != null) {
            for (Map<String, Object> rowMap : resultData) {
                Integer mailingId = ((Number)rowMap.get("mailing_id")).intValue();
                if (insertTotal) {
                    Object objectValue = rowMap.get("category_value");
                    Integer categoryValue = objectValue == null ? 0 : ((Number) objectValue).intValue();
                    updateEmbedded(logger, insertQuery, category, categoryIndex, mailingId, categoryValue,
                            ALL_SUBSCRIBERS_TARGETGROUPID, CommonKeys.ALL_SUBSCRIBERS, ALL_SUBSCRIBERS_INDEX);
                }
                if (insertTargets) {
                    int targetGroupIndex = ALL_SUBSCRIBERS_INDEX + 1;
                    for (LightTarget target : targets) {
                        Object valueAsObject = rowMap.get("tg_" + target.getId());
                        mailingId = ((Number)rowMap.get("mailing_id")).intValue();
                        valueAsObject = valueAsObject == null ? 0 : valueAsObject;
                        updateEmbedded(logger, insertQuery, category, categoryIndex, mailingId,
                            ((Number) valueAsObject).intValue(), target.getId(), target.getName(), targetGroupIndex);
                        targetGroupIndex++;
                    }
                }
                if (mailingsLeft.contains(mailingId)) {
                    mailingsLeft.remove(mailingId);
                }
            }
            for (Integer mailing : mailingsLeft) {
                if (insertTotal) {
                    updateEmbedded(logger, insertQuery, category, categoryIndex, mailing, 0, ALL_SUBSCRIBERS_TARGETGROUPID, CommonKeys.ALL_SUBSCRIBERS, ALL_SUBSCRIBERS_INDEX);
                }
                if (insertTargets) {
                    int targetGroupIndex = ALL_SUBSCRIBERS_INDEX + 1;
                    for (LightTarget target : targets) {
                        updateEmbedded(logger, insertQuery, category, categoryIndex, mailing, 0, target.getId(), target.getName(), targetGroupIndex);
                        targetGroupIndex++;
                    }
                }
            }
        }
    }

	@DaoUpdateReturnValueCheck
    private void fillCategoryValuesWith(String mailingIds, @VelocityCheck int companyID, int tempTableID, String category, int categoryIndex, List <LightTarget> targets, boolean insertTargets, boolean insertTotal, int value) throws Exception {
        String[] ids = mailingIds.split(",");
        String insertQuery = getTempInsertQuery(tempTableID);
        for (String mailingStr : ids) {
            int mailingId = NumberUtils.toInt(mailingStr);
            if (insertTotal) {
                updateEmbedded(logger, insertQuery, category, categoryIndex, mailingId, value, ALL_SUBSCRIBERS_TARGETGROUPID, CommonKeys.ALL_SUBSCRIBERS, ALL_SUBSCRIBERS_INDEX);
            }
            if (insertTargets) {
                int targetGroupIndex = ALL_SUBSCRIBERS_INDEX + 1;
                for (LightTarget target : targets) {
                    updateEmbedded(logger, insertQuery, category, categoryIndex, mailingId, value, target.getId(), target.getName(), targetGroupIndex);
                    targetGroupIndex++;
                }
            }
        }
    }

    private String getTempInsertQuery(int tempTableID) {
		return "INSERT INTO " + getTempTableName(tempTableID)
				+ " (category, category_index, mailing_id, mailing_name, value, targetgroup_id, targetgroup, targetgroup_index, rate)"
				+ " VALUES( ?,?,?,'name',?,?,?,?,0) ";
	}

    private String createSendQuery(String recipientsType) {
        String query = "SELECT mailing_id, COALESCE(SUM(no_of_mailings),0) category_value FROM mailing_account_tbl WHERE mailing_id IN (<MAILING_IDS>) ";
        switch (recipientsType) {
            case CommonKeys.TYPE_ALL_SUBSCRIBERS:
                query += " AND status_field NOT IN ('A', 'T', 'V') ";
                break;
            case CommonKeys.TYPE_ADMIN_AND_TEST:
                query += " AND status_field IN ('A', 'T') ";
                break;
            case CommonKeys.TYPE_WORLDMAILING:
                query += " AND status_field NOT IN ('A', 'T') ";
                break;
			default:
				break;
        }
        query += " GROUP BY mailing_id ";
        return query;
	}

    private String createBouncesQuery(String recipientsType) {
        String recipientFilter = null;
        if(CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            recipientFilter = " AND user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') ";
        }
        if(CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            recipientFilter = " AND user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') ";
        }
        String query = "";
        if (recipientFilter == null) {
            query = "SELECT SUM(hardbounces) AS category_value, mailing_id FROM ("
            		+ "SELECT mailing_id, " +
                    " CASE WHEN DETAIL >= 510 THEN 1 ELSE 0 END AS hardbounces " +
                    " FROM (SELECT DISTINCT customer_id, detail, mailing_id FROM bounce_tbl WHERE mailing_id IN (<MAILING_IDS>)) cust)" +
                    " main_data GROUP BY mailing_id";
        } else {
            query = "SELECT sum(sys_hardbounces) AS category_value, mailing_id FROM " +
                    "(SELECT CASE WHEN DETAIL >= 510 THEN 1 ELSE 0 END AS sys_hardbounces, mailing_id " +
                    " FROM (SELECT DISTINCT customer_id, detail, mailing_id FROM bounce_tbl WHERE mailing_id IN (<MAILING_IDS>) " +
                    "AND customer_id IN (SELECT customer_id FROM customer_<COMPANYID>_binding_tbl WHERE " +
                    " mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ) " +
                    recipientFilter +
                    ")) cust ) main_data GROUP BY mailing_id";
        }
        return query;
    }

    private String createBouncesQueryForTargets(String recipientsType, List<LightTarget> targets) {
        StringBuilder queryBuilder = new StringBuilder("SELECT sum(sys_hardbounces) AS category_value, sys_mailing_id AS mailing_id, ");

        StringBuilder sumBuilder = new StringBuilder("");
        StringBuilder caseBuilder = new StringBuilder("");

        for (LightTarget target : targets) {
            String targetGroup = "tg_" + target.getId();
            sumBuilder.append("sum(" + targetGroup + " ) " + targetGroup + " ,");
            caseBuilder.append(" CASE WHEN ( " + target.getTargetSQL() + " ) AND DETAIL >= 510 THEN 1 ELSE 0 END AS  " + targetGroup + " , ");

        }
        String sum = sumBuilder.toString();
        sum = sum.substring(0, sum.lastIndexOf(","));
        queryBuilder.append(sum);

        queryBuilder.append(" FROM (SELECT mailing_id AS sys_mailing_id, CASE WHEN detail >= 510 THEN 1 ELSE 0 END AS sys_hardbounces, ");

        String cases = caseBuilder.toString();
        cases = cases.substring(0, cases.lastIndexOf(","));

        queryBuilder.append(cases);

        String customerTable = "customer_<COMPANYID>_tbl";

        if (CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            customerTable = "(SELECT cust.* FROM customer_<COMPANYID>_binding_tbl bind JOIN customer_<COMPANYID>_tbl cust ON( cust.customer_id = bind.customer_id) WHERE user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ))";
        }

        if (CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            customerTable = "(SELECT cust.* FROM customer_<COMPANYID>_binding_tbl bind JOIN customer_<COMPANYID>_tbl cust ON( cust.customer_id = bind.customer_id) WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ))";
        }

        queryBuilder.append("FROM (SELECT cust.*, bounce.customer_id AS bounce_cust_id, bounce.detail, mailing_id FROM bounce_tbl bounce JOIN " + customerTable + " cust ON (cust.customer_id = bounce.customer_id) WHERE bounce.mailing_id IN (<MAILING_IDS>) AND bounce.company_id = <COMPANYID>  ) cust) main_data GROUP BY sys_mailing_id");

        String query = queryBuilder.toString();

        return query;
    }

    private String createOptOutsQuery(String recipientsType) {
        String recipientFilter = "";
        if(CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            recipientFilter = " AND bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') ";
        }
        if(CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            recipientFilter = " AND bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') ";
        }
        String query = " SELECT count(DISTINCT bind.customer_id) AS category_value, exit_mailing_id mailing_id FROM " +
                    " customer_<COMPANYID>_binding_tbl bind  " +
                    " WHERE bind.exit_mailing_id IN (<MAILING_IDS>) AND ( bind.user_status = 3 OR  bind.user_status = 4 ) " +
                    recipientFilter + " GROUP BY exit_mailing_id";


        return query;
    }

    private String createOptOutsQueryForTargets(String recipientsType, List<LightTarget> targets) {
        StringBuilder queryBuilder = new StringBuilder("SELECT sys_mailing_id AS mailing_id,");
        StringBuilder sumBuilder = new StringBuilder("");
        StringBuilder caseBuilder = new StringBuilder("");

        for(LightTarget target:targets) {
                String targetgroup = "tg_"+target.getId();
                sumBuilder.append(" sum("+targetgroup+"_net) " + targetgroup + " ," );
                caseBuilder.append(" CASE WHEN ( " + target.getTargetSQL() +" ) THEN 1 ELSE 0 END AS " +targetgroup + "_net ,");
        }

        String sumString = sumBuilder.toString();
        sumString = sumString.substring(0, sumString.lastIndexOf(","));

        String caseString = caseBuilder.toString();
        caseString = caseString.substring(0, caseString.lastIndexOf(","));

        queryBuilder.append(sumString);
        queryBuilder.append(" FROM (SELECT sys_mailing_id, ");
        queryBuilder.append(caseString);

        queryBuilder.append(" FROM ( " +
                " SELECT cust.*, bind.exit_mailing_id AS sys_mailing_id FROM customer_<COMPANYID>_tbl cust JOIN customer_<COMPANYID>_binding_tbl bind ON (cust.customer_id = bind.customer_id) " +
                " WHERE bind.exit_mailing_id IN (<MAILING_IDS>) AND (bind.user_status = 3 OR bind.user_status = 4) <CONSTRAINT> ) cust) main_data GROUP BY sys_mailing_id");

        String constraint = "";

        if(CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            constraint = " AND bind.user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') ";
        }
        if(CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            constraint = " AND bind.user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') ";
        }

        String query = queryBuilder.toString();
        query = query.replace("<CONSTRAINT>", constraint);

        return query;
	}

    private String createRevenueQueryForTargets(String recipientsType, List<LightTarget> targets) {
        StringBuilder queryBuilder = new StringBuilder("SELECT sys_mailing_id AS mailing_id, sum(sys_num_parameter) AS category_value, ");

        StringBuilder sumBuilder = new StringBuilder("");
        StringBuilder caseBuilder = new StringBuilder("");

        for( LightTarget target:targets) {
            String targetGroup = "tg_" + target.getId();
            sumBuilder.append("sum(" + targetGroup + "_net) " + targetGroup + ", " );
            caseBuilder.append(" CASE WHEN ( " + target.getTargetSQL() + " ) THEN sys_num_parameter ELSE 0 END AS " + targetGroup + "_net, ");
        }
        String sum = sumBuilder.toString();
        sum = sum.substring(0, sum.lastIndexOf(","));
        queryBuilder.append(sum);

        queryBuilder.append(" FROM (SELECT sys_mailing_id, sys_num_parameter, " );

        String cases =caseBuilder.toString();
        cases = cases.substring(0, cases.lastIndexOf(","));

        queryBuilder.append( cases );

        String customerTable = "customer_<COMPANYID>_tbl";

        if(CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            customerTable = "(SELECT cust.* FROM customer_<COMPANYID>_binding_tbl bind JOIN customer_<COMPANYID>_tbl cust " +
                    "ON( cust.customer_id = bind.customer_id) WHERE user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ))";
        }

        if(CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            customerTable = "(SELECT cust.* FROM customer_<COMPANYID>_binding_tbl bind JOIN customer_<COMPANYID>_tbl cust " +
                    "ON( cust.customer_id = bind.customer_id) WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ))";
        }

        queryBuilder.append(" FROM ( SELECT cust.*, rdir.num_parameter AS sys_num_parameter, rdir.mailing_id AS sys_mailing_id FROM rdirlog_<COMPANYID>_val_num_tbl rdir LEFT JOIN " +customerTable + " cust " +
                "ON ( cust.customer_id = rdir.customer_id) WHERE rdir.mailing_id IN (<MAILING_IDS>) AND rdir.page_tag = 'revenue') cust) main_data GROUP BY sys_mailing_id");

        String query = queryBuilder.toString();
        return query;
    }

    private String createRevenueQuery(String recipientsType) {
        String query = "SELECT sum(num_parameter) AS category_value, mailing_id "
                + " FROM rdirlog_<COMPANYID>_val_num_tbl rlog  WHERE rlog.mailing_id IN (<MAILING_IDS>) AND rlog.page_tag = 'revenue' ";

        if( CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            query  = query +  " AND rlog.customer_id IN ( SELECT customer_id FROM customer_<COMPANYID>_binding_tbl" +
                    " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ) ) " ;
        }

        if( CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            query  = query +  " AND rlog.customer_id IN ( SELECT customer_id FROM customer_<COMPANYID>_binding_tbl" +
                    " WHERE user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND  mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ) ) " ;
        }
        return query + " GROUP BY mailing_id";
    }

    private String createClicksQuery(String recipientsType) {
        String query = "SELECT count(DISTINCT rlog.customer_id) AS category_value, mailing_id "
                + " FROM rdirlog_<COMPANYID>_tbl rlog  WHERE rlog.mailing_id IN (<MAILING_IDS>) ";

        if( CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            query  = query +  " AND rlog.customer_id IN ( SELECT customer_id FROM customer_<COMPANYID>_binding_tbl" +
                    " WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ) ) " ;
        }

        if( CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            query  = query +  " AND rlog.customer_id IN ( SELECT customer_id FROM customer_<COMPANYID>_binding_tbl" +
                    " WHERE user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND  mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ) ) " ;
        }
        return query + " GROUP BY mailing_id";
    }

    private String createClicksQueryForTargets(String recipientsType) {
        String customerTable = "customer_<COMPANYID>_tbl";

        if(CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
            customerTable = "(SELECT cust.* FROM customer_<COMPANYID>_binding_tbl bind JOIN customer_<COMPANYID>_tbl cust ON( cust.customer_id = bind.customer_id) " +
                    "WHERE user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ))";
        }

        if(CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
            customerTable = "(SELECT cust.* FROM customer_<COMPANYID>_binding_tbl bind JOIN customer_<COMPANYID>_tbl cust ON( cust.customer_id = bind.customer_id) " +
                    "WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ))";
        }

        return "SELECT count(DISTINCT customer_id) AS tg_<TG_ID>, mailing_id "
					+ " FROM (SELECT customer_id, sys_url_id AS url_id, sys_mailing_id AS mailing_id, "
					+ " CASE WHEN ( <TARGETSQL> ) THEN 1 ELSE null END AS tg1 "
					+ " FROM (SELECT cust.*, rlog.url_id AS sys_url_id, rlog.mailing_id AS sys_mailing_id FROM rdirlog_<COMPANYID>_tbl rlog JOIN "+customerTable +" cust ON "
                    + "( rlog.customer_id = cust.customer_id ) WHERE rlog.mailing_id IN (<MAILING_IDS>) ) cust "
					+ " ) main_data WHERE tg1 = 1 GROUP BY mailing_id";
    }

    private String createOpenQuery(List<LightTarget> lightTargets, String recipientsType) {
		if (lightTargets != null && lightTargets.size() > 0) {
			StringBuilder queryBuilder = new StringBuilder(" SELECT count(customer_id) AS category_value, mailing_id, ");

			StringBuilder sumBuilder = new StringBuilder("");
			StringBuilder caseBuilder = new StringBuilder("");

			for (LightTarget target : lightTargets) {
				String targetGroup = "tg_" + target.getId();
				sumBuilder.append("sum(" + targetGroup + "_net) " + targetGroup + ", ");
				caseBuilder.append(getOpenedCaseTemplate().replace("<TARGETSQL>", target.getTargetSQL()).replace("<TARGETGROUP>", targetGroup));
			}

			String sumString = sumBuilder.toString();

			queryBuilder.append(sumString.substring(0, sumString.lastIndexOf(",")));
			queryBuilder.append(" FROM (");
			queryBuilder.append(" SELECT sys_open_count AS open_count, customer_id, sys_mailing_id AS mailing_id, ");

			String caseString = caseBuilder.toString();
			queryBuilder.append(caseString.substring(0, caseString.lastIndexOf(",")));

			String customerTable = "customer_<COMPANYID>_tbl";

			if(CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
				customerTable = "(SELECT cust.* FROM customer_<COMPANYID>_binding_tbl bind JOIN customer_<COMPANYID>_tbl " +
                        "cust ON( cust.customer_id = bind.customer_id) WHERE user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "')  AND mailinglist_id IN (SELECT " +
                        "mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ))";
			}

			if(CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
				customerTable = "(SELECT cust.* FROM customer_<COMPANYID>_binding_tbl bind JOIN customer_<COMPANYID>_tbl " +
                        "cust ON( cust.customer_id = bind.customer_id) WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') AND " +
                        "mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ))";
			}

			queryBuilder.append(" FROM (SELECT cust.*, opl.open_count AS sys_open_count, opl.mailing_id AS sys_mailing_id FROM onepixellog_<COMPANYID>_tbl opl JOIN " +
                    customerTable + " cust ON (opl.customer_id = cust.customer_id) WHERE opl.mailing_id IN (<MAILING_IDS>) ) cust) main_data GROUP BY mailing_id");

			return queryBuilder.toString();
		}
        String query = " SELECT count(customer_id) AS category_value, mailing_id "
			+ " FROM ( "
			+ " SELECT open_count, customer_id, mailing_id "
			+ " FROM (SELECT customer_id , open_count, mailing_id FROM onepixellog_<COMPANYID>_tbl WHERE mailing_id IN (<MAILING_IDS>) ) "
			+ " cust ) main_data GROUP BY mailing_id" ;
		if(CommonKeys.TYPE_WORLDMAILING.equals(recipientsType)) {
			query = "SELECT count(DISTINCT(customer_id)) category_value, mailing_id " +
			"FROM onepixellog_<COMPANYID>_tbl o " +
			"WHERE mailing_id IN (<MAILING_IDS>) AND EXISTS " +
			"(SELECT 1 FROM customer_<COMPANYID>_binding_tbl bind WHERE user_type IN ('" + UserType.World.getTypeCode() + "', '" + UserType.WorldVIP.getTypeCode() + "') " +
			"AND o.customer_id = bind.customer_id " +
			"AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ) ) GROUP BY mailing_id";
		}
		if(CommonKeys.TYPE_ADMIN_AND_TEST.equals(recipientsType)) {
			query = " SELECT count(customer_id) AS category_value, mailing_id FROM" +
			    " (SELECT open_count, customer_id, mailing_id FROM (SELECT customer_id, open_count, mailing_id FROM onepixellog_<COMPANYID>_tbl " +
                "WHERE mailing_id IN (<MAILING_IDS>) AND customer_id IN ( " +
			   	" SELECT bind.customer_id FROM customer_<COMPANYID>_binding_tbl bind  WHERE user_type IN ('" + UserType.Admin.getTypeCode() + "', '" + UserType.TestUser.getTypeCode() + "', '" + UserType.TestVIP.getTypeCode() + "') " +
                "AND mailinglist_id IN (SELECT mailinglist_id FROM mailing_tbl WHERE mailing_id IN (<MAILING_IDS>) ))) cust ) main_data GROUP BY mailing_id";
		}

		return query;
    }

    public String getOpenedCaseTemplate() {
		return "CASE " + " WHEN ( <TARGETSQL> ) " + "  THEN 1 " + "  ELSE 0 "
				+ " END AS <TARGETGROUP>_net,";
	}

	private int createTempTable() throws  Exception {
		int tempTableID = getNextTmpID();
		executeEmbedded(logger,
			"CREATE TABLE " + getTempTableName(tempTableID) + " ("
				+ "category VARCHAR(200),"
				+ " category_index INTEGER,"
				+ " mailing_id INTEGER,"
				+ " mailing_name VARCHAR(200), "
				+ " targetgroup_id INTEGER,"
				+ " targetgroup VARCHAR(200),"
				+ " targetgroup_index INTEGER,"
				+ " value INTEGER,"
				+ " rate DOUBLE"
			+ ")");
		return tempTableID;
	}
	
	private List<CompareStatRow> getResultsFromTempTable(int tempTableID, final Locale locale) throws Exception {
		String query = "SELECT category, category_index, value, rate, mailing_id, mailing_name, targetgroup_id, targetgroup, " +
                "targetgroup_index FROM " + getTempTableName(tempTableID) + " ORDER BY category_index ";
		return selectEmbedded(logger, query, new RowMapper<CompareStatRow>() {
			@Override
			public CompareStatRow mapRow(ResultSet resultSet, int rowNum) throws SQLException {
				CompareStatRow row = new CompareStatRow();
				row.setCategory(resultSet.getString("category"));
                row.setTargetGroupName(resultSet.getString("targetgroup"));
                row.setTargetShortName(createTargetNameShort(row.getTargetGroupName()));
				row.setCategoryindex(resultSet.getInt("category_index"));
                row.setTargetGroupIndex(resultSet.getInt("targetgroup_index"));
                row.setTargetGroupId(resultSet.getInt("targetgroup_id"));
				row.setCount(resultSet.getInt("value"));
                row.setRate(resultSet.getDouble("rate"));
				row.setMailingId(resultSet.getInt("mailing_id"));
                String name = resultSet.getString("mailing_name");
                row.setMailingNameFull(name);
                row.setMailingName(name);
				return row;
			}
		});
	}

    public static String createTargetNameShort(String name) {
        String shortname = name;
        if (name != null && name.length() > TARGET_NAME_LENGTH_MAX) {
            shortname = name.substring(0, TARGET_NAME_LENGTH_MAX - 3) + "...";
        } else if (name == null) {
            shortname = "";
        }
        return shortname;
    }

	private String getSendCaseTemplate() {
		return " CASE " + " WHEN ( <TARGETSQL> ) " + " THEN 1 " + " ELSE 0  "
				+ " END AS <TARGETGROUP> , ";
	}
	
	private String getTempTableName(int id) {
		return "tmp_report_aggregation_" + id + "_tbl";
	}
}
