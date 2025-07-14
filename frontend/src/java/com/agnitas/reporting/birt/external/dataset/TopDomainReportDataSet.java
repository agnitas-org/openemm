/*

    Copyright (C) 2025 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.common.MailingType;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightMailingList;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.SendPerDomainStatRow;
import com.agnitas.reporting.birt.external.utils.BirtReporUtils;
import com.agnitas.dao.impl.mapper.IntegerRowMapper;
import com.agnitas.util.Tuple;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.jdbc.core.RowMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS_INDEX;
import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID;

public class TopDomainReportDataSet extends TopDomainsDataSet {
    
    private boolean showTotals = false;

	public boolean isShowTotals() {
		return showTotals;
	}

	private List<Integer> getSentMailings(int companyId, List<Integer> mailingListIds, String startDateString, String stopDateString) throws ParseException {
        Date startDate = null;
		if (StringUtils.isNotBlank(startDateString)) {
			startDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(startDateString);
		}
		
		Date endDate = null;
		if (StringUtils.isNotBlank(stopDateString)) {
			endDate = new SimpleDateFormat(DATE_PARAMETER_FORMAT).parse(stopDateString);
		}
		
        String mailingsQuery = "SELECT res.mailing_id FROM" +
                "  (SELECT c.mailing_id, MIN(c.timestamp) AS senddate" +
                "   FROM mailing_account_tbl c" +
                "     LEFT JOIN mailing_tbl a ON a.mailing_id = c.mailing_id" +
                "   WHERE c.company_id = ? AND c.status_field = 'W' AND a.mailing_type = 0  AND  " +
                makeBulkInClauseForInteger("a.mailinglist_id", mailingListIds) + " AND a.deleted = 0" +
                "   GROUP BY c.mailing_id, a.change_date ORDER BY senddate DESC) res" +
                " WHERE res.senddate > ? AND res.senddate <= ?";
        
        return select(mailingsQuery, IntegerRowMapper.INSTANCE, companyId, startDate, endDate);
    }
    
    private List<Integer> getSentMailings(int companyId, int mailingListId, String startDateString, String stopDateString) throws ParseException {
        return getSentMailings(companyId, Collections.singletonList(mailingListId), startDateString, stopDateString);
    }

    public int prepareReport(int companyID, String mailinglistAsString, String targetsAsString, String startDateString, String stopDateString,
							 int domainsMax, String language, boolean topLevelDomains, String figuresOptions) throws Exception {
		int tempTableID = createTopDomainsTempTable();
		
        List<Integer> mailingListIds = Arrays.stream(mailinglistAsString.split(","))
                .map(NumberUtils::toInt)
                .filter(id -> id > 0)
                .collect(Collectors.toList());

		showTotals = mailingListIds.size() > 1;

		List<LightTarget> targets = getTargets(targetsAsString, companyID);
		boolean mailingTrackingAvailable = isMailingTrackingActivated(companyID);
		
		List<LightMailingList> mailingLists = getMailingLists(mailingListIds, companyID);
	
		for (LightMailingList mailingList : mailingLists) {
			List<Integer> sentMailingIds = getSentMailings(companyID,  mailingList.getMailingListId(), startDateString, stopDateString);
			if (CollectionUtils.isNotEmpty(sentMailingIds)) {
				insertStatistic(tempTableID, companyID,
						mailingList.getMailingListId(), mailingList.getShortname(),
						sentMailingIds, targets, language, domainsMax, topLevelDomains,
						figuresOptions);
			}
		}

		updateRates(tempTableID, targets, mailingTrackingAvailable);
		insertEmptyDomainData(tempTableID, mailingLists, targets, figuresOptions);
		
		return tempTableID;
	}

	private void updateRates(int tempTableID, List<LightTarget> targets, boolean mailtrackingDataAvailable) throws Exception {
		boolean targetsAvailable = CollectionUtils.isNotEmpty(targets);

		updateRates(tempTableID, CATEGORY_TOTAL_SENT_EMAILS, CommonKeys.ALL_SUBSCRIBERS_INDEX,
				CATEGORY_SENT_EMAILS, CATEGORY_SOFTBOUNCES, CATEGORY_HARDBOUNCES);
		updateRates(tempTableID, CATEGORY_TOTAL_CLICKERS, CommonKeys.ALL_SUBSCRIBERS_INDEX, CATEGORY_CLICKERS);
		updateRates(tempTableID, CATEGORY_TOTAL_OPENERS, CommonKeys.ALL_SUBSCRIBERS_INDEX, CATEGORY_OPENERS);

		if (targetsAvailable) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;

            for (int i = 0; i < targets.size(); i++) {
				updateRates(tempTableID, CATEGORY_TOTAL_SENT_EMAILS, targetGroupIndex, mailtrackingDataAvailable,
						CATEGORY_SENT_EMAILS, CATEGORY_SOFTBOUNCES, CATEGORY_HARDBOUNCES);
				updateRates(tempTableID, CATEGORY_TOTAL_CLICKERS, targetGroupIndex, CATEGORY_CLICKERS);
				updateRates(tempTableID, CATEGORY_TOTAL_OPENERS, targetGroupIndex, CATEGORY_OPENERS);

				targetGroupIndex++;
			}
		}
	}

	private void updateRates(int tempTableID, int totalCategoryIndex, int targetGroupIndex, int... categories) throws Exception {
		updateRates(tempTableID, totalCategoryIndex, targetGroupIndex, true, categories);
	}

	private void updateRates(int tempTableID, int totalCategoryIndex, int targetGroupIndex, boolean mailTracking, int... categories) throws Exception {
		if (categories != null && categories.length > 0) {
			int totalValue = -1;
			if (mailTracking) {
				totalValue = selectEmbeddedInt("SELECT SUM(value)"
					+ " FROM " + getTemporaryTableName(tempTableID)
					+ " WHERE category_index = ? AND targetgroup_index = ? AND domainname IS NULL"
					+ " GROUP BY category_index, targetgroup_index",
					totalCategoryIndex, targetGroupIndex);
			}

			if (totalValue <= 0) {
				updateEmbedded("UPDATE " + getTemporaryTableName(tempTableID) + " SET rate = 0"
					+ " WHERE category_index IN (" + StringUtils.join(ArrayUtils.toObject(categories), ", ") + ") AND targetgroup_index = ?", targetGroupIndex);
			} else {
				updateEmbedded("UPDATE " + getTemporaryTableName(tempTableID) + " SET rate = value * 1.0 / ?"
					+ " WHERE category_index IN (" + StringUtils.join(ArrayUtils.toObject(categories), ", ") + ") AND targetgroup_index = ?", totalValue, targetGroupIndex);
			}
		}
	}


	private void insertEmptyDomainData(int tempTableID, List<LightMailingList> mailingLists, List<LightTarget> targets, String figuresOptions) throws Exception {
		boolean targetsAvailable = CollectionUtils.isNotEmpty(targets);
		List<Tuple<Integer, String>> categories = new ArrayList<>();

		if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.SENT_MAILS, figuresOptions)) {
			categories.add(new Tuple<>(CATEGORY_SENT_EMAILS, CATEGORY_NAME_SENT_EMAILS));
		}

		if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.HARDBOUNCES, figuresOptions)) {
			categories.add(new Tuple<>(CATEGORY_HARDBOUNCES, CATEGORY_NAME_HARDBOUNCES));
		}

		if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.SOFTBOUNCES, figuresOptions)) {
			categories.add(new Tuple<>(CATEGORY_SOFTBOUNCES, CATEGORY_NAME_SOFTBOUNCES));
		}

		if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.OPENERS, figuresOptions)) {
			categories.add(new Tuple<>(CATEGORY_OPENERS, CATEGORY_NAME_OPENERS));
		}

		if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL, figuresOptions)) {
			categories.add(new Tuple<>(CATEGORY_CLICKERS, CATEGORY_NAME_CLICKERS));
		}

		for (Tuple<Integer, String> category: categories) {
			int categoryIndex = category.getFirst();

			List<Map<String, Object>> domainsRows = selectEmbedded("SELECT DISTINCT domainname, domainname_index FROM " +
				getTemporaryTableName(tempTableID) + " WHERE domainname IS NOT NULL AND category_index = ? AND targetgroup_index = ?", categoryIndex, ALL_SUBSCRIBERS_INDEX);

			for (Map<String, Object> row : domainsRows) {
				String domainName = (String) row.get("domainname");
				int domainIndex = toInt(row.get("domainname_index"));
				for (LightMailingList mailingList: mailingLists) {
					int mlid = mailingList.getMailingListId();
					String mailinglistName = mailingList.getShortname();

					TempRow tempRow = new TempRow();
					tempRow.setCategoryIndex(categoryIndex);
					tempRow.setCategoryName(category.getSecond());

					tempRow.setTargetGroup(ALL_SUBSCRIBERS);
					tempRow.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);
					tempRow.setTargetGroupIndex(ALL_SUBSCRIBERS_INDEX);

					tempRow.setDomainName(domainName);
					tempRow.setDomainNameIndex(domainIndex);

					tempRow.setRate(0);
					tempRow.setValue(0);

					insertEmptyRowIfNotExists(tempTableID, tempRow, mlid, mailinglistName);

					if (targetsAvailable) {
						int targetGroupIndex = ALL_SUBSCRIBERS_INDEX + 1;

						for (LightTarget target : targets) {
							tempRow.setTargetGroupId(target.getId());
							tempRow.setTargetGroup(target.getName());
							tempRow.setTargetGroupIndex(targetGroupIndex);

							insertEmptyRowIfNotExists(tempTableID, tempRow, mlid, mailinglistName);
							targetGroupIndex++;
						}
					}
				}
			}
		}
	}

	private void insertEmptyRowIfNotExists(int tempTableID, TempRow tempRow, int mlid, String mlName) throws Exception {
		boolean exists = selectEmbeddedInt(
				"SELECT COUNT(*) FROM " + getTemporaryTableName(tempTableID) +
						" WHERE domainname = ? AND category_index = ? AND targetgroup_index = ? AND mailinglist_id = ?",
				tempRow.getDomainName(), tempRow.getCategoryIndex(), tempRow.getTargetGroupIndex(), mlid) > 0;

		if (!exists) {
			insertIntoTempTable(tempTableID, tempRow, mlid, mlName);
		}
	}

	private void insertStatistic(int tempTableID, int companyID,
								 int mlId, String mlName, List<Integer> mailingIds, List<LightTarget> targets,
								 String language, int domainsMax, boolean topLevelDomains, String figuresOptions) throws Exception {
    	
    	if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.SENT_MAILS, figuresOptions)) {
			insertSentStatIntoTempTable(tempTableID, companyID, mlId, mlName, mailingIds, targets, language, domainsMax, topLevelDomains);
		}
		
		if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.HARDBOUNCES, figuresOptions)) {
			insertHardBouncesIntoTempTable(tempTableID, companyID, mlId, mlName, mailingIds, targets, language, domainsMax, topLevelDomains);
		}
		
		if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.SOFTBOUNCES, figuresOptions)) {
			insertSoftBouncesIntoTempTable(tempTableID, companyID, mlId, mlName, mailingIds, targets, language, domainsMax, topLevelDomains);
		}
		
		if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.OPENERS, figuresOptions)) {
			insertOpenersIntoTempTable(tempTableID, companyID, mlId, mlName, mailingIds, targets, language, domainsMax, topLevelDomains);
		}

		if (BirtReporUtils.isOptionAllowed(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL, figuresOptions)) {
            insertClickersIntoTempTable(tempTableID, companyID, mlId, mlName, mailingIds, targets, language, domainsMax, topLevelDomains);
		}

	}

    private void insertSoftBouncesIntoTempTable(int tempTableID, int companyID, int mlId, String mlName, List<Integer> mailingIds, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
        insertBouncesIntoTempTable(tempTableID, companyID, mlId, mlName, mailingIds, targets, language, domainsMax, topLevelDomains,
                new TopDomainsDataSet.Rule("bounce.detail < 510", CATEGORY_NAME_SOFTBOUNCES, CATEGORY_SOFTBOUNCES));
	}
	
    private void insertHardBouncesIntoTempTable(int tempTableID, int companyID, int mlId, String mlName, List<Integer> mailingIds, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
        insertBouncesIntoTempTable(tempTableID, companyID, mlId, mlName, mailingIds, targets, language, domainsMax, topLevelDomains,
                new TopDomainsDataSet.Rule("bounce.detail >= 510", CATEGORY_NAME_HARDBOUNCES, CATEGORY_HARDBOUNCES));
    }
    
    @DaoUpdateReturnValueCheck
    private void insertBouncesIntoTempTable(int tempTableID, int companyID, int mlId, String mlName, List<Integer> mailingIds, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains, Rule rule) throws Exception {
    	final String CATEGORY_NAME = rule.getCategoryName();
    	final int CATEGORY_INDEX = rule.getCategoryIndex();
    	final String CATEGORY_CONDITION = rule.getCondition();
        Map<Integer, TempRow> otherDomainsMap = new HashMap<>();
        int otherDomainIndex = domainsMax + 1;
    
        String totalQuery = "SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust, bounce_tbl bounce WHERE cust.customer_id = bounce.customer_id AND " +
                " bounce.company_id = ? AND " + makeBulkInClauseForInteger("bounce.mailing_id", mailingIds) + " AND " + CATEGORY_CONDITION;
    
        int overallBouncedMailings = selectInt(totalQuery, companyID);
        
		TempRow overallRow = new TempRow();
		overallRow.setCategoryName(CATEGORY_NAME);
		overallRow.setCategoryIndex(CATEGORY_INDEX);
		overallRow.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);
		overallRow.setTargetGroup(ALL_SUBSCRIBERS);
		overallRow.setTargetGroupIndex(ALL_SUBSCRIBERS_INDEX);
		overallRow.setValue(overallBouncedMailings);
		
		TempRow otherTotalDomainRow = getOtherDomainRow(CATEGORY_NAME, CATEGORY_INDEX,
                ALL_SUBSCRIBERS, ALL_SUBSCRIBERS_TARGETGROUPID, ALL_SUBSCRIBERS_INDEX,
                otherDomainIndex, language);
		otherTotalDomainRow.setValue(overallBouncedMailings);
		
		insertIntoTempTable(tempTableID, overallRow, mlId, mlName);

		if (CollectionUtils.isNotEmpty(targets)) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
                String queryByTarget = "SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust, bounce_tbl bounce " +
                        " WHERE cust.customer_id = bounce.customer_id " +
                        " AND bounce.company_id = ?" +
                        " AND " + makeBulkInClauseForInteger("bounce.mailing_id", mailingIds) +
                        " AND " + CATEGORY_CONDITION +
                        " AND (" + target.getTargetSQL() + ")";
                
                int targetSentMailings = selectInt(queryByTarget, companyID);

				TempRow targetRow = new TempRow();
				targetRow.setCategoryName(CATEGORY_NAME);
				targetRow.setCategoryIndex(CATEGORY_INDEX);
				targetRow.setTargetGroupId(target.getId());
				targetRow.setTargetGroup(target.getName());
				targetRow.setTargetGroupIndex(targetGroupIndex);
				targetRow.setValue(targetSentMailings);
                insertIntoTempTable(tempTableID, targetRow, mlId, mlName);
                
                TempRow otherTargetDomainRow = getOtherDomainRow(CATEGORY_NAME, CATEGORY_INDEX,
                        target.getName(), target.getId(), targetGroupIndex,
                        otherDomainIndex, language);
                otherTargetDomainRow.setValue(targetSentMailings);
                
                otherDomainsMap.put(target.getId(), otherTargetDomainRow);
				targetGroupIndex++;
			}
		}

		String overallDomainsSql = "SELECT COUNT(*) AS bounces_per_domain, domain_name" +
                " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name FROM customer_" + companyID + "_tbl cust, bounce_tbl bounce" +
                " WHERE cust.customer_id = bounce.customer_id " +
                " AND bounce.company_id = ? " +
                " AND " + makeBulkInClauseForInteger("bounce.mailing_id", mailingIds) +
                " AND " + CATEGORY_CONDITION +
                ")" + (isOracleDB() ? "" : " sub");
		
        overallDomainsSql += " GROUP BY domain_name HAVING COUNT(*) > 0 ORDER BY bounces_per_domain DESC";
		if (domainsMax > 1) {
			if (isOracleDB()) {
				overallDomainsSql = "SELECT * FROM (" + overallDomainsSql + ") WHERE ROWNUM <= " + domainsMax;
			} else {
				overallDomainsSql = overallDomainsSql + " LIMIT " + domainsMax;
			}
		}

		List<TempRow> overallDomainsRows = select(overallDomainsSql, (resultSet, index) -> {
				TempRow row = new TempRow();
				row.setCategoryName(CATEGORY_NAME);
				row.setCategoryIndex(CATEGORY_INDEX);
				row.setDomainName(resultSet.getString("domain_name"));
				row.setDomainNameIndex(index + 1);
				row.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);
				row.setTargetGroup(ALL_SUBSCRIBERS);
				row.setTargetGroupIndex(ALL_SUBSCRIBERS_INDEX);
				row.setValue(resultSet.getInt("bounces_per_domain"));
				return row;
			}, companyID
		);
        int domainsSum = overallDomainsRows.stream().mapToInt(TempRow::getValue).sum();
        otherTotalDomainRow.setValue(otherTotalDomainRow.getValue() - domainsSum);
		overallDomainsRows.add(otherTotalDomainRow);
		insertIntoTempTable(tempTableID, overallDomainsRows, mlId, mlName);

		if (CollectionUtils.isNotEmpty(targets)) {
			int domainTargetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				String targetDomainsSql = "SELECT COUNT(*) AS bounces_per_domain, domain_name" +
                        " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name FROM customer_" + companyID + "_tbl cust, bounce_tbl bounce" +
                        " WHERE cust.customer_id = bounce.customer_id " +
                        " AND bounce.company_id = ? " +
                        " AND " + makeBulkInClauseForInteger("bounce.mailing_id", mailingIds) +
                        " AND " + CATEGORY_CONDITION +
                        " AND (" + target.getTargetSQL() + ")";
				
                targetDomainsSql += ")" + (isOracleDB() ? "" : " sub");
				targetDomainsSql += " GROUP BY domain_name HAVING COUNT(*) > 0 ORDER BY bounces_per_domain DESC";
				if (domainsMax > 1) {
					if (isOracleDB()) {
						targetDomainsSql = "SELECT * FROM (" + targetDomainsSql + ") WHERE bounces_per_domain > 0 AND ROWNUM <= " + domainsMax;
					} else {
						targetDomainsSql = targetDomainsSql + " LIMIT " + domainsMax;
					}
				}

				final int domainTargetGroupIndexFinal = domainTargetGroupIndex;
				List<TempRow> targetDomainsRows = select(targetDomainsSql, (resultSet, index) -> {
						TempRow row = new TempRow();
						row.setCategoryName(CATEGORY_NAME);
						row.setCategoryIndex(CATEGORY_INDEX);
						row.setDomainName(resultSet.getString("domain_name"));
						row.setDomainNameIndex(index + 1);
						row.setTargetGroupId(target.getId());
						row.setTargetGroup(target.getName());
						row.setTargetGroupIndex(domainTargetGroupIndexFinal);
						row.setValue(resultSet.getInt("bounces_per_domain"));
						return row;
					}, companyID
				);
                TempRow otherDomainTargetRow = otherDomainsMap.get(target.getId());
                int targetDomainSum = targetDomainsRows.stream().mapToInt(TempRow::getValue).sum();
                otherDomainTargetRow.setValue(otherDomainTargetRow.getValue() - targetDomainSum);
                targetDomainsRows.add(otherDomainTargetRow);
                insertIntoTempTable(tempTableID, targetDomainsRows, mlId, mlName);
				domainTargetGroupIndex++;
			}
		}
    }

	@DaoUpdateReturnValueCheck
    private void insertOpenersIntoTempTable(int tempTableID, int companyID, int mlId, String mlName, List<Integer> mailingIds, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
		Map<Integer, TempRow> otherDomainsMap = new HashMap<>();
        int otherDomainIndex = domainsMax + 1;

        String totalQuery = "SELECT COUNT(DISTINCT cust.customer_id) FROM customer_" + companyID + "_tbl cust, onepixellog_device_" + companyID + "_tbl opl " +
                " WHERE cust.customer_id = opl.customer_id AND " + makeBulkInClauseForInteger("opl.mailing_id", mailingIds);
    
        int totalValue = selectInt(totalQuery);
        
		TempRow totalRow = new TempRow();
		totalRow.setCategoryName(CATEGORY_NAME_OPENERS);
		totalRow.setCategoryIndex(CATEGORY_TOTAL_OPENERS);
		totalRow.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);
		totalRow.setTargetGroup(ALL_SUBSCRIBERS);
		totalRow.setTargetGroupIndex(ALL_SUBSCRIBERS_INDEX);
		totalRow.setValue(totalValue);
        insertIntoTempTable(tempTableID, totalRow, mlId, mlName);
    
        TempRow otherTotalDomainRow = getOtherDomainRow(CATEGORY_NAME_OPENERS, CATEGORY_OPENERS,
                ALL_SUBSCRIBERS, ALL_SUBSCRIBERS_TARGETGROUPID, ALL_SUBSCRIBERS_INDEX,
                otherDomainIndex, language);
        otherTotalDomainRow.setValue(totalValue);

		if (CollectionUtils.isNotEmpty(targets)) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				String queryByTarget = "SELECT COUNT(DISTINCT cust.customer_id) FROM customer_" + companyID + "_tbl cust, onepixellog_device_" + companyID + "_tbl opl " +
                            " WHERE cust.customer_id = opl.customer_id AND " + makeBulkInClauseForInteger("opl.mailing_id", mailingIds)
						+ " AND (" + target.getTargetSQL() + ")";
				
                int totalByTarget = selectInt(queryByTarget);

				TempRow targetRow = new TempRow();
				targetRow.setCategoryName(CATEGORY_NAME_OPENERS);
				targetRow.setCategoryIndex(CATEGORY_TOTAL_OPENERS);
				targetRow.setTargetGroupId(target.getId());
				targetRow.setTargetGroup(target.getName());
				targetRow.setTargetGroupIndex(targetGroupIndex);
				targetRow.setValue(totalByTarget);
                insertIntoTempTable(tempTableID, targetRow, mlId, mlName);
                
                TempRow otherTargetDomainRow = getOtherDomainRow(CATEGORY_NAME_OPENERS, CATEGORY_OPENERS,
                        target.getName(), target.getId(), targetGroupIndex,
                        otherDomainIndex, language);
                otherTargetDomainRow.setValue(totalByTarget);
                otherDomainsMap.put(target.getId(), otherTargetDomainRow);

				targetGroupIndex++;
			}
		}

		String overallDomainsSql = "SELECT COUNT(DISTINCT customer_id) AS openers_per_domain, domain_name"
			+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name, cust.customer_id FROM customer_" + companyID + "_tbl cust, onepixellog_device_" + companyID + "_tbl opl"
				+ " WHERE cust.customer_id = opl.customer_id AND " + makeBulkInClauseForInteger("opl.mailing_id", mailingIds);
    
        
        overallDomainsSql += ")" + (isOracleDB() ? "" : " sub")
			+ " GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0 ORDER BY openers_per_domain DESC";
		
		if (domainsMax > 1) {
			if (isOracleDB()) {
				overallDomainsSql = "SELECT * FROM (" + overallDomainsSql + ") WHERE ROWNUM <= " + domainsMax;
			} else {
				overallDomainsSql = overallDomainsSql + " LIMIT " + domainsMax;
			}
		}
		
		List<TempRow> overallDomainsRows = select(overallDomainsSql, (resultSet, index) -> {
				TempRow row = new TempRow();
				row.setCategoryName(CATEGORY_NAME_OPENERS);
				row.setCategoryIndex(CATEGORY_OPENERS);
				row.setDomainName(resultSet.getString("domain_name"));
				row.setDomainNameIndex(index + 1);
				row.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);
				row.setTargetGroup(ALL_SUBSCRIBERS);
				row.setTargetGroupIndex(ALL_SUBSCRIBERS_INDEX);
				row.setValue(resultSet.getInt("openers_per_domain"));
				return row;
			}
        );
		
        int domainsSum = overallDomainsRows.stream().mapToInt(TempRow::getValue).sum();
        otherTotalDomainRow.setValue(otherTotalDomainRow.getValue() - domainsSum);
		overallDomainsRows.add(otherTotalDomainRow);
		insertIntoTempTable(tempTableID, overallDomainsRows, mlId, mlName);
		
		if (CollectionUtils.isNotEmpty(targets)) {
			int domainTargetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				String targetDomainsSql = "SELECT COUNT(DISTINCT customer_id) AS openers_per_domain, domain_name"
					+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name, cust.customer_id FROM customer_" + companyID + "_tbl cust, onepixellog_device_" + companyID + "_tbl opl"
						+ " WHERE cust.customer_id = opl.customer_id AND " + makeBulkInClauseForInteger("opl.mailing_id", mailingIds);
                
                targetDomainsSql += " AND (" + target.getTargetSQL() + "))" + (isOracleDB() ? "" : " sub")
					+ " GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0 ORDER BY openers_per_domain DESC";
				if (domainsMax > 1) {
					if (isOracleDB()) {
						targetDomainsSql = "SELECT * FROM (" + targetDomainsSql + ") WHERE openers_per_domain > 0 AND ROWNUM <= " + domainsMax;
					} else {
						targetDomainsSql = targetDomainsSql + " LIMIT " + domainsMax;
					}
				}

				final int domainTargetGroupIndexFinal = domainTargetGroupIndex;
				List<TempRow> targetDomainsRows = select(targetDomainsSql, (resultSet, index) -> {
						TempRow row = new TempRow();
						row.setCategoryName(CATEGORY_NAME_OPENERS);
						row.setCategoryIndex(CATEGORY_OPENERS);
						row.setDomainName(resultSet.getString("domain_name"));
						row.setDomainNameIndex(index + 1);
						row.setTargetGroupId(target.getId());
						row.setTargetGroup(target.getName());
						row.setTargetGroupIndex(domainTargetGroupIndexFinal);
						row.setValue(resultSet.getInt("openers_per_domain"));
						return row;
					}
                );
				
				TempRow otherDomainTargetRow = otherDomainsMap.get(target.getId());
                int targetDomainSum = targetDomainsRows.stream().mapToInt(TempRow::getValue).sum();
                otherDomainTargetRow.setValue(otherDomainTargetRow.getValue() - targetDomainSum);

                targetDomainsRows.add(otherDomainTargetRow);
                insertIntoTempTable(tempTableID, targetDomainsRows, mlId, mlName);

				domainTargetGroupIndex++;
			}
		}
	}
    
    @DaoUpdateReturnValueCheck
	private void insertClickersIntoTempTable(int tempTableID, int companyID, int mlId, String mlName, List<Integer> mailingIds, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
		Map<Integer, TempRow> otherDomainsMap = new HashMap<>();
        int otherDomainIndex = domainsMax + 1;
		
        String totalQuery = "SELECT COUNT(DISTINCT cust.customer_id) FROM customer_" + companyID + "_tbl cust, rdirlog_" + companyID + "_tbl rlog " +
                "WHERE cust.customer_id = rlog.customer_id AND " + makeBulkInClauseForInteger("rlog.mailing_id", mailingIds);
		
        int totalValue = selectInt(totalQuery);
        
        TempRow overallRow = new TempRow();
        overallRow.setCategoryName(CATEGORY_NAME_CLICKERS);
        overallRow.setCategoryIndex(CATEGORY_TOTAL_CLICKERS);
        overallRow.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);
        overallRow.setTargetGroup(ALL_SUBSCRIBERS);
        overallRow.setTargetGroupIndex(ALL_SUBSCRIBERS_INDEX);
        overallRow.setValue(totalValue);
        insertIntoTempTable(tempTableID, overallRow, mlId, mlName);
        
        TempRow otherTotalDomainRow = getOtherDomainRow(CATEGORY_NAME_CLICKERS, CATEGORY_CLICKERS,
                ALL_SUBSCRIBERS, ALL_SUBSCRIBERS_TARGETGROUPID, ALL_SUBSCRIBERS_INDEX,
                otherDomainIndex, language);
        otherTotalDomainRow.setValue(totalValue);
        
        if (CollectionUtils.isNotEmpty(targets)) {
           int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
           for (LightTarget target : targets) {
               String totalQueryByTarget = "SELECT COUNT(DISTINCT cust.customer_id) FROM customer_" + companyID + "_tbl cust, rdirlog_" + companyID + "_tbl rlog " +
                       " WHERE cust.customer_id = rlog.customer_id AND "
                       + makeBulkInClauseForInteger("rlog.mailing_id", mailingIds) +
                       " AND (" + target.getTargetSQL() + ")";
               
              int totalByTarget = selectInt(totalQueryByTarget);
        
              TempRow targetRow = new TempRow();
              targetRow.setCategoryName(CATEGORY_NAME_CLICKERS);
              targetRow.setCategoryIndex(CATEGORY_TOTAL_CLICKERS);
              targetRow.setTargetGroupId(target.getId());
              targetRow.setTargetGroup(target.getName());
              targetRow.setTargetGroupIndex(targetGroupIndex);
              targetRow.setValue(totalByTarget);
              insertIntoTempTable(tempTableID, targetRow, mlId, mlName);
              
              TempRow otherTargetDomainRow = getOtherDomainRow(CATEGORY_NAME_CLICKERS, CATEGORY_CLICKERS,
                        target.getName(), target.getId(), targetGroupIndex,
                        otherDomainIndex, language);
              otherTargetDomainRow.setValue(totalByTarget);
              otherDomainsMap.put(target.getId(), otherTargetDomainRow);
              
              targetGroupIndex++;
           }
        }
        
        String overallDomainsSql = "SELECT COUNT(DISTINCT customer_id) AS clickers_per_domain, domain_name"
           + " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name, cust.customer_id FROM customer_" + companyID + "_tbl cust, rdirlog_" + companyID + "_tbl rlog" +
                " WHERE cust.customer_id = rlog.customer_id " +
                " AND " + makeBulkInClauseForInteger("rlog.mailing_id", mailingIds);
        
        overallDomainsSql += ")" + (isOracleDB() ? "" : " sub")
           + " GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0 ORDER BY clickers_per_domain DESC";
        if (domainsMax > 1) {
           if (isOracleDB()) {
              overallDomainsSql = "SELECT * FROM (" + overallDomainsSql + ") WHERE ROWNUM <= " + domainsMax;
           } else {
              overallDomainsSql = overallDomainsSql + " LIMIT " + domainsMax;
           }
        }
        
        List<TempRow> overallDomainsRows = select(overallDomainsSql, (resultSet, index) -> {
              TempRow row = new TempRow();
              row.setCategoryName(CATEGORY_NAME_CLICKERS);
              row.setCategoryIndex(CATEGORY_CLICKERS);
              row.setDomainName(resultSet.getString("domain_name"));
              row.setDomainNameIndex(index + 1);
              row.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);
              row.setTargetGroup(ALL_SUBSCRIBERS);
              row.setTargetGroupIndex(ALL_SUBSCRIBERS_INDEX);
              row.setValue(resultSet.getInt("clickers_per_domain"));
              return row;
           }
        );
        
        int domainsSum = overallDomainsRows.stream().mapToInt(TempRow::getValue).sum();
        otherTotalDomainRow.setValue(otherTotalDomainRow.getValue() - domainsSum);
		overallDomainsRows.add(otherTotalDomainRow);
		insertIntoTempTable(tempTableID, overallDomainsRows, mlId, mlName);
		
        if (CollectionUtils.isNotEmpty(targets)) {
           int domainTargetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
           for (LightTarget target : targets) {
              String targetDomainsSql = "SELECT COUNT(DISTINCT customer_id) AS clickers_per_domain, domain_name" +
                      " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name, cust.customer_id FROM customer_" + companyID + "_tbl cust, rdirlog_" + companyID + "_tbl rlog" +
                      " WHERE cust.customer_id = rlog.customer_id " +
                      " AND " + makeBulkInClauseForInteger("rlog.mailing_id", mailingIds);
              
              targetDomainsSql += " AND (" + target.getTargetSQL() + "))" + (isOracleDB() ? "" : " sub")
                 + " GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0 ORDER BY clickers_per_domain DESC";
              if (domainsMax > 1) {
                 if (isOracleDB()) {
                    targetDomainsSql = "SELECT * FROM (" + targetDomainsSql + ") WHERE clickers_per_domain > 0 AND ROWNUM <= " + domainsMax;
                 } else {
                    targetDomainsSql = targetDomainsSql + " LIMIT " + domainsMax;
                 }
              }
        
              final int domainTargetGroupIndexFinal = domainTargetGroupIndex;
              List<TempRow> targetDomainsRows = select(targetDomainsSql, (resultSet, index) -> {
                    TempRow row = new TempRow();
                    row.setCategoryName(CATEGORY_NAME_CLICKERS);
                    row.setCategoryIndex(CATEGORY_CLICKERS);
                    row.setDomainName(resultSet.getString("domain_name"));
                    row.setDomainNameIndex(index + 1);
                    row.setTargetGroupId(target.getId());
                    row.setTargetGroup(target.getName());
                    row.setTargetGroupIndex(domainTargetGroupIndexFinal);
                    row.setValue(resultSet.getInt("clickers_per_domain"));
                    return row;
                 }
              );
    
              TempRow otherDomainTargetRow = otherDomainsMap.get(target.getId());
              int targetDomainSum = targetDomainsRows.stream().mapToInt(TempRow::getValue).sum();
              otherDomainTargetRow.setValue(otherDomainTargetRow.getValue() - targetDomainSum);
              targetDomainsRows.add(otherDomainTargetRow);
              insertIntoTempTable(tempTableID, targetDomainsRows, mlId, mlName);
              
              domainTargetGroupIndex++;
           }
        }
	}

    @DaoUpdateReturnValueCheck
    private int getTotalSentMailingWithoutMailTracking(int companyId, List<Integer> mailingIds) {
        String query = "SELECT SUM(COALESCE(no_of_mailings, 0)) FROM mailing_account_tbl " +
                " WHERE company_id = ? " +
                " AND " + makeBulkInClauseForInteger("mailing_id", mailingIds);
        
        return selectInt(query, companyId);
    }
    
    @DaoUpdateReturnValueCheck
    private void insertSentMailingPerDomain(int tempTableID, List<DomainStatisticRow> sentMailingStatistic, List<LightTarget> targets,
                                            int total, Map<Integer, Integer> targetTotals, int mailinglistId, String mailinglist,
											int maxDomains, String language) throws Exception {
        int domainIndex = 1;
        int otherDomainIndex = maxDomains + 1;
        
        Map<Integer, TempRow> otherDomainsByTargetId = new HashMap<>();
        List<TempRow> domainsStatistic = new ArrayList<>();
    
        TempRow otherDomainRow = getOtherDomainRow(
        		CATEGORY_NAME_SENT_EMAILS, CATEGORY_SENT_EMAILS,
                ALL_SUBSCRIBERS, ALL_SUBSCRIBERS_TARGETGROUPID, ALL_SUBSCRIBERS_INDEX,
				otherDomainIndex, language);
        otherDomainRow.setValue(total);
        
        int targetGroupIndex = ALL_SUBSCRIBERS_INDEX;
        for (LightTarget target: targets){
            int targetId = target.getId();
            targetGroupIndex++;
            TempRow otherDomainRowByTarget = getOtherDomainRow(CATEGORY_NAME_SENT_EMAILS, CATEGORY_SENT_EMAILS,
					target.getName(), target.getId(), targetGroupIndex, otherDomainIndex, language);
            otherDomainRowByTarget.setValue(targetTotals.getOrDefault(targetId, 0));
            otherDomainsByTargetId.put(targetId, otherDomainRowByTarget);
        }
    
        for (DomainStatisticRow row: sentMailingStatistic) {
            domainIndex++;
            TempRow tempRow = new TempRow();
            tempRow.setCategoryIndex(CATEGORY_SENT_EMAILS);
            tempRow.setCategoryName(CATEGORY_NAME_SENT_EMAILS);
            tempRow.setTargetGroup(ALL_SUBSCRIBERS);
            tempRow.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);
            tempRow.setTargetGroupIndex(ALL_SUBSCRIBERS_INDEX);
            
            if (domainIndex < otherDomainIndex) {
                tempRow.setDomainName(row.getDomainName());
                tempRow.setDomainNameIndex(domainIndex);
                tempRow.setValue(row.getMailsPerDomain());
                domainsStatistic.add(tempRow);
                
                otherDomainRow.setValue(otherDomainRow.getValue() - row.getMailsPerDomain());
            }
    
            Map<Integer, Integer> mailsByTargets = row.getMailsByTargets();
    
            if (CollectionUtils.isNotEmpty(targets)) {
                targetGroupIndex = ALL_SUBSCRIBERS_INDEX;
                for (LightTarget target: targets) {
                    targetGroupIndex++;
                    int targetGroupId = target.getId();
                    tempRow = new TempRow();
                    tempRow.setCategoryIndex(CATEGORY_SENT_EMAILS);
                    tempRow.setCategoryName(CATEGORY_NAME_SENT_EMAILS);
                    tempRow.setTargetGroup(target.getName());
                    tempRow.setTargetGroupId(targetGroupId);
                    tempRow.setTargetGroupIndex(targetGroupIndex);
                    tempRow.setDomainName(row.getDomainName());
                    tempRow.setDomainNameIndex(domainIndex);
                    
                    if (domainIndex < otherDomainIndex) {
                        tempRow.setDomainName(row.getDomainName());
                        tempRow.setDomainNameIndex(domainIndex);
                        
                        int value = mailsByTargets.getOrDefault(targetGroupId, 0);
                        tempRow.setValue(value);
                        domainsStatistic.add(tempRow);
                        
                        if (value != 0) {
                            TempRow other = otherDomainsByTargetId.get(targetGroupId);
                            other.setValue(other.getValue() - value);
                        }
    
                    }
                }
                
            }
        }
        
        domainsStatistic.add(otherDomainRow);
        domainsStatistic.addAll(otherDomainsByTargetId.values());
        
        insertIntoTempTable(tempTableID, domainsStatistic, mailinglistId, mailinglist);
    }
    
    @DaoUpdateReturnValueCheck
    private void insertSentStatIntoTempTable(int tempTableID, int companyID, int mlId, String mlName,
											 List<Integer> mailingIds, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
        Map<Integer, Integer> targetTotals = new HashMap<>();
        if (isMailingTrackingActivated(companyID)) {
            List<DomainStatisticRow> sentMailingStatistic = getSentMailingsStatistic(companyID, targets, mailingIds, topLevelDomains);
            
            int total = sentMailingStatistic.stream().mapToInt(DomainStatisticRow::getMailsPerDomain).sum();
            
            if (CollectionUtils.isNotEmpty(targets)) {
                for (DomainStatisticRow row: sentMailingStatistic) {
                    for (Map.Entry<Integer, Integer> data: row.getMailsByTargets().entrySet()) {
                        int targetId = data.getKey();
                        int value = data.getValue();
                        int sum = targetTotals.getOrDefault(targetId, 0);
                        targetTotals.put(targetId, sum + value);
                    }
                }
            }
            
            insertTotals(tempTableID, total, targetTotals, targets, mlId, mlName);
            insertSentMailingPerDomain(tempTableID, sentMailingStatistic, targets, total, targetTotals, mlId, mlName,
					domainsMax, language);
        } else {
            int total = getTotalSentMailingWithoutMailTracking(companyID, mailingIds);
            insertTotals(tempTableID, total, targetTotals, targets, mlId, mlName);
        }
    }
    
    @DaoUpdateReturnValueCheck
    private List<DomainStatisticRow> getSentMailingsStatistic(int companyId, List<LightTarget> targets, List<Integer> mailingIds, boolean topLevelDomains) {
        List<Integer> targetIds = targets.stream().map(LightTarget::getId).collect(Collectors.toList());

        StringBuilder queryBuilder = new StringBuilder("SELECT COUNT(mailing_id) AS mails_per_domain, domain_name");

        for (LightTarget target : targets) {
            queryBuilder.append(", SUM(tg_" + target.getId() + "_send) AS tg_" + target.getId());
        }
        
        queryBuilder.append(" FROM ( ");

        queryBuilder.append(" SELECT domain_name, sys_mailing_id AS mailing_id");
        
        for (LightTarget target : targets) {
            queryBuilder.append(", CASE WHEN (" + target.getTargetSQL() + ") THEN 1 ELSE 0 END AS tg_" + target.getId() + "_send");
        }

        queryBuilder
                .append(" FROM (")
                .append("      SELECT cust.*, succ.mailing_id AS sys_mailing_id, ")
                .append(getDomainFromEmailExpression(topLevelDomains)).append(" AS domain_name")
                .append("        FROM customer_" + companyId + "_tbl cust ")
                .append("           , success_" + companyId + "_tbl succ ")
                .append("           , mailing_tbl m ")
                .append("       WHERE cust.customer_id = succ.customer_id ")
                .append("         AND  ").append(makeBulkInClauseForInteger("succ.mailing_id", mailingIds))
                .append("         AND succ.mailing_id = m.mailing_id ")
                .append("         AND m.mailing_type <> ").append(MailingType.INTERVAL.getCode())
                .append("       UNION ALL ")
                .append("      SELECT cust.*, x.mailing_id AS sys_mailing_id, ")
                .append(getDomainFromEmailExpression(topLevelDomains)).append(" AS domain_name")
                .append("        FROM customer_" + companyId + "_tbl cust ")
                .append("           , interval_track_" + companyId + "_tbl x ")
                .append("           , mailing_tbl m ")
                .append("       WHERE cust.customer_id = x.customer_id ")
                .append("         AND  ").append(makeBulkInClauseForInteger("x.mailing_id", mailingIds))
                .append("         AND x.mailing_id = m.mailing_id ")
                .append("         AND m.mailing_type = ").append(MailingType.INTERVAL.getCode())
                .append(" ) cust ")
                .append(") main_data GROUP BY domain_name HAVING COUNT(*) > 0 ORDER BY mails_per_domain DESC");
        
        return select(queryBuilder.toString(), new DomainStatisticMapper(targetIds));
    }
 
	@DaoUpdateReturnValueCheck
    private void insertTotals(int tempTableID, int total, Map<Integer, Integer> targetTotals, List<LightTarget> targets,
                              int mailinglistId, String mailinglist) throws Exception {

    	TempRow tempRow = new TempRow();
        tempRow.setCategoryIndex(CATEGORY_TOTAL_SENT_EMAILS);
        tempRow.setCategoryName(CATEGORY_NAME_SENT_EMAILS);
        tempRow.setTargetGroup(ALL_SUBSCRIBERS);
        tempRow.setTargetGroupId(ALL_SUBSCRIBERS_TARGETGROUPID);
        tempRow.setTargetGroupIndex(ALL_SUBSCRIBERS_INDEX);
        tempRow.setValue(total);

        insertIntoTempTable(tempTableID, tempRow, mailinglistId, mailinglist);
    
        if (CollectionUtils.isNotEmpty(targets)) {
            int targetGroupIndex = ALL_SUBSCRIBERS_INDEX;
            for (LightTarget target: targets) {
                targetGroupIndex++;
                int targetGroupId = target.getId();

                tempRow.setTargetGroup(target.getName());
                tempRow.setTargetGroupId(targetGroupId);
                tempRow.setTargetGroupIndex(targetGroupIndex);
                tempRow.setValue(targetTotals.getOrDefault(targetGroupId, 0));

                insertIntoTempTable(tempTableID, tempRow, mailinglistId, mailinglist);
            }
        }
    }
    
	private TempRow getOtherDomainRow(String categoryName, int categoryIndex, String targetName, int targetGroupId, int targetGroupIndex, int otherDomainIndex, String language) {
        TempRow tempRow = new TempRow();
        tempRow.setCategoryIndex(categoryIndex);
        tempRow.setCategoryName(categoryName);
        tempRow.setTargetGroup(targetName);
        tempRow.setTargetGroupId(targetGroupId);
        tempRow.setTargetGroupIndex(targetGroupIndex);
        tempRow.setDomainNameIndex(otherDomainIndex);
        tempRow.setDomainName(I18nString.getLocaleString("statistic.Other", language));
        return tempRow;
    }
    
	private static class DomainStatisticMapper implements RowMapper<DomainStatisticRow> {
        private final List<Integer> targets;
    
        public DomainStatisticMapper(List<Integer> targets) {
            this.targets = targets;
        }
    
        @Override
        public DomainStatisticRow mapRow(ResultSet resultSet, int i) throws SQLException {
            DomainStatisticRow domainStatisticRow = new DomainStatisticRow();
            domainStatisticRow.setDomainName(resultSet.getString("domain_name"));
            domainStatisticRow.setMailsPerDomain(resultSet.getInt("mails_per_domain"));
    
            Map<Integer, Integer> mailsByTargets = new HashMap<>();
            if (CollectionUtils.isNotEmpty(targets)) {
                for (int targetId: targets) {
                    mailsByTargets.put(targetId, resultSet.getInt("tg_" + targetId));
                }
            }
            
            domainStatisticRow.setMailsByTargets(mailsByTargets);
            return domainStatisticRow;
        }
    }
    
    private static class DomainStatisticRow {
        private int mailsPerDomain;
        private String domainName;
        private Map<Integer, Integer> mailsByTargets = new HashMap<>();
    
        public int getMailsPerDomain() {
            return mailsPerDomain;
        }
    
        public void setMailsPerDomain(int mailsPerDomain) {
            this.mailsPerDomain = mailsPerDomain;
        }
    
        public String getDomainName() {
            return domainName;
        }
    
        public void setDomainName(String domainName) {
            this.domainName = domainName;
        }
    
        public Map<Integer, Integer> getMailsByTargets() {
            return mailsByTargets;
        }
    
        public void setMailsByTargets(Map<Integer, Integer> mailsByTargets) {
            this.mailsByTargets = mailsByTargets;
        }
    }
	
	public List<DomainSendStatRow> getStatisticPerDomain(int tempTableID) throws Exception {
		return selectEmbedded("SELECT * FROM " + getTemporaryTableName(tempTableID) + " " +
				"WHERE domainname IS NOT NULL ORDER BY category_index, domainname_index, targetgroup_index, mailinglist_name, mailinglist_id",
				(resultSet, index) -> {
			DomainSendStatRow row = new DomainSendStatRow();
			row.setCategory(resultSet.getString("category_name"));
			row.setCategoryindex(resultSet.getInt("category_index"));
			row.setDomainName(resultSet.getString("domainname"));
			row.setDomainNameIndex(resultSet.getInt("domainname_index"));
			row.setTargetgroup(resultSet.getString("targetgroup"));
			row.setTargetgroupindex(resultSet.getInt("targetgroup_index"));
			row.setMailinglist(resultSet.getString("mailinglist_name"));
			row.setMailinglistId(resultSet.getInt("mailinglist_id"));
			row.setCount(resultSet.getInt("value"));
			row.setRate(resultSet.getDouble("rate"));
			return row;
		});
	}
	
	private void insertIntoTempTable(int tempTableID, TempRow row, int mailinglistId, String mailinglist) throws Exception {
		String insertSql = "INSERT INTO " + getTemporaryTableName(tempTableID) +
				" (category_name, category_index, domainname, domainname_index, " +
				"targetgroup, targetgroup_id, targetgroup_index, " +
				"mailinglist_name, mailinglist_id, value, rate)" +
				" VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		updateEmbedded(insertSql,
			row.getCategoryName(),
			row.getCategoryIndex(),
			row.getDomainName(),
			row.getDomainNameIndex(),
			row.getTargetGroup(),
			row.getTargetGroupId(),
			row.getTargetGroupIndex(),
			mailinglist,
			mailinglistId,
			row.getValue(),
			row.getRate()
		);
	}
 
	private void insertIntoTempTable(int tempTableID, List<TempRow> rows, int mailinglistId, String mailinglist) throws Exception {
		for (TempRow row : rows) {
			insertIntoTempTable(tempTableID, row, mailinglistId, mailinglist);
		}
	}
	
	/**
	 * create a temporary table to collect the values from different queries for top_domains.rptdesign report in one table
	 *
	 * @return id of the create temporary table
	 */
	private int createTopDomainsTempTable() throws Exception {
		int tempTableID = getNextTmpID();
		executeEmbedded(
			"CREATE TABLE " + getTemporaryTableName(tempTableID) + " ("
				+ "category_name VARCHAR(200),"
				+ " category_index INTEGER,"
				+ " domainname VARCHAR(200),"
				+ " domainname_index INTEGER,"
				+ " targetgroup_id INTEGER,"
				+ " targetgroup VARCHAR(200),"
				+ " targetgroup_index INTEGER,"
				+ " mailinglist_name VARCHAR(200),"
				+ " mailinglist_id INTEGER,"
				+ " value INTEGER,"
				+ " rate DOUBLE"
			+ ")");
		return tempTableID;
	}

    
    public static class DomainSendStatRow extends SendPerDomainStatRow {
        private String mailinglist;
        private int mailinglistId;
    
        public String getMailinglist() {
            return mailinglist;
        }
    
        public void setMailinglist(String mailinglist) {
            this.mailinglist = mailinglist;
        }
    
        public int getMailinglistId() {
            return mailinglistId;
        }
    
        public void setMailinglistId(int mailinglistId) {
            this.mailinglistId = mailinglistId;
        }
    }
}
