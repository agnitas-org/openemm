/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import static com.agnitas.reporting.birt.external.dataset.CommonKeys.ALL_SUBSCRIBERS;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.SendPerDomainStatRow;

public class TopDomainsDataSet extends BIRTDataSet {
	private static final transient Logger logger = LogManager.getLogger(TopDomainsDataSet.class);

	public static final int CATEGORY_TOTAL_SENT_EMAILS = 1;
	public static final int CATEGORY_SENT_EMAILS = 2;
	public static final int CATEGORY_HARDBOUNCES = 3;
	public static final int CATEGORY_SOFTBOUNCES = 4;
	public static final int CATEGORY_TOTAL_OPENERS = 5;
	public static final int CATEGORY_OPENERS = 6;
	public static final int CATEGORY_TOTAL_CLICKERS = 7;
	public static final int CATEGORY_CLICKERS = 8;

	public static final String CATEGORY_NAME_SENT_EMAILS = "report.sentMails";
	public static final String CATEGORY_NAME_HARDBOUNCES = "statistic.bounces.hardbounce";
	public static final String CATEGORY_NAME_SOFTBOUNCES = "report.softbounces";
	public static final String CATEGORY_NAME_OPENERS = "statistic.opener";
	public static final String CATEGORY_NAME_CLICKERS = "statistic.clicker";

	protected static class TempRow {
		private String categoryName;
		private int categoryIndex;
		private String domainName;
		private int domainNameIndex;
		private int targetGroupId;
		private String targetGroup;
		private int targetGroupIndex;
		private int value;
		private double rate;

		public String getCategoryName() {
			return categoryName;
		}

		public void setCategoryName(String categoryName) {
			this.categoryName = categoryName;
		}

		public int getCategoryIndex() {
			return categoryIndex;
		}

		public void setCategoryIndex(int categoryIndex) {
			this.categoryIndex = categoryIndex;
		}

		public String getDomainName() {
			return domainName;
		}

		public void setDomainName(String domainName) {
			this.domainName = domainName;
		}

		public int getDomainNameIndex() {
			return domainNameIndex;
		}

		public void setDomainNameIndex(int domainNameIndex) {
			this.domainNameIndex = domainNameIndex;
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

		public double getRate() {
			return rate;
		}

		/**
		 * Used by JSP
		 */
		public void setRate(double rate) {
			this.rate = rate;
		}
	}

	/**
	 * This method has to be called in initialize function of the report, it loads the data to be retrieved later.
	 * @param mailingID
	 * @param companyID
	 * @param selectedTargetsAsString
	 * @param domainsMax
	 * @param language
	 * @param topLevelDomains
	 * @return identifier of temporary table (just created)
	 * @throws Exception
	 */
	public int prepareReport(int mailingID, int companyID, String selectedTargetsAsString, int domainsMax, String language, boolean topLevelDomains) throws Exception {
		int tempTableID = createTempTable();
		List<LightTarget> targets = getTargets(selectedTargetsAsString, companyID);

		boolean mailingTrackingDataAvailable = isTrackingExists(mailingID, companyID);

		insertSentStatIntoTempTable(tempTableID, mailingID, companyID, targets, language, domainsMax, topLevelDomains, mailingTrackingDataAvailable);
		insertBouncesIntoTempTable(tempTableID, mailingID, companyID, targets, language, domainsMax, topLevelDomains);
		insertOpenersIntoTempTable(tempTableID, mailingID, companyID, targets, language, domainsMax, topLevelDomains);
		insertClickersIntoTempTable(tempTableID, mailingID, companyID, targets, language, domainsMax, topLevelDomains);

		updateRates(tempTableID, targets, mailingTrackingDataAvailable);

		return tempTableID;
	}

	public List<SendPerDomainStatRow> getResultsFromTempTable(int tempTableID) throws Exception {
		return selectEmbedded(logger, "SELECT * FROM " + getTemporaryTableName(tempTableID) + " " +
				"WHERE domainname IS NOT NULL ORDER BY category_index, domainname_index, targetgroup_index",
				(resultSet, index) -> {
			SendPerDomainStatRow row = new SendPerDomainStatRow();
			row.setCategory(resultSet.getString("category_name"));
			row.setCategoryindex(resultSet.getInt("category_index"));
			row.setDomainName(resultSet.getString("domainname"));
			row.setDomainNameIndex(resultSet.getInt("domainname_index"));
			row.setTargetgroup(resultSet.getString("targetgroup"));
			row.setTargetgroupindex(resultSet.getInt("targetgroup_index"));
			row.setCount(resultSet.getInt("value"));
			row.setRate(resultSet.getDouble("rate"));
			return row;
		});
	}

	@DaoUpdateReturnValueCheck
	private void insertSentStatIntoTempTable(int tempTableID, int mailingID, int companyID, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains, boolean mailingTrackingDataAvailable) throws Exception {
		Map<Integer, TempRow> otherDomainsMap = new HashMap<>();

		int overallSentMailings;
		if (mailingTrackingDataAvailable) {
			overallSentMailings = selectInt(logger, "SELECT COUNT(*) FROM mailtrack_" + companyID + "_tbl WHERE mailing_id = ?", mailingID);
		} else {
			overallSentMailings = selectInt(logger, "SELECT SUM(COALESCE(no_of_mailings, 0)) FROM mailing_account_tbl WHERE mailing_id = ?", mailingID);
		}

		TempRow overallRow = new TempRow();
		overallRow.setCategoryName(CATEGORY_NAME_SENT_EMAILS);
		overallRow.setCategoryIndex(CATEGORY_TOTAL_SENT_EMAILS);
		overallRow.setTargetGroupId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
		overallRow.setTargetGroup(ALL_SUBSCRIBERS);
		overallRow.setTargetGroupIndex(CommonKeys.ALL_SUBSCRIBERS_INDEX);
		overallRow.setValue(overallSentMailings);
		otherDomainsMap.put(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID, overallRow);
		insertIntoTempTable(tempTableID, overallRow);

		if (targets != null) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				int targetSentMailings = 0;
				if (mailingTrackingDataAvailable) {
					targetSentMailings = selectInt(logger,
						"SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust, mailtrack_" + companyID + "_tbl track WHERE cust.customer_id = track.customer_id AND track.mailing_id = ?"
							+ " AND (" + target.getTargetSQL() + ")",
						mailingID);
				}

				TempRow targetRow = new TempRow();
				overallRow.setCategoryName(CATEGORY_NAME_SENT_EMAILS);
				targetRow.setCategoryIndex(CATEGORY_TOTAL_SENT_EMAILS);
				targetRow.setTargetGroupId(target.getId());
				targetRow.setTargetGroup(target.getName());
				targetRow.setTargetGroupIndex(targetGroupIndex);
				targetRow.setValue(targetSentMailings);
				otherDomainsMap.put(target.getId(), targetRow);
				insertIntoTempTable(tempTableID, targetRow);
				targetGroupIndex++;
			}
		}

		// CATEGORY_SENT_EMAILS available only if there is mailtracking data
		if (mailingTrackingDataAvailable) {
			String overallDomainsSql = "SELECT COUNT(*) AS mails_per_domain, domain_name"
				+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name FROM customer_" + companyID + "_tbl cust, mailtrack_" + companyID + "_tbl track"
					+ " WHERE cust.customer_id = track.customer_id AND track.mailing_id = ?)" + (isOracleDB() ? "" : " sub")
				+ " GROUP BY domain_name HAVING COUNT(*) > 0 ORDER BY mails_per_domain DESC";
			if (domainsMax > 1) {
				if (isOracleDB()) {
					overallDomainsSql = "SELECT * FROM (" + overallDomainsSql + ") WHERE ROWNUM <= " + domainsMax;
				} else {
					overallDomainsSql = overallDomainsSql + " LIMIT " + domainsMax;
				}
			}

			List<TempRow> overallDomainsRows = select(logger, overallDomainsSql, (resultSet, index) -> {
					TempRow row = new TempRow();
					row.setCategoryName(CATEGORY_NAME_SENT_EMAILS);
					row.setCategoryIndex(CATEGORY_SENT_EMAILS);
					row.setDomainName(resultSet.getString("domain_name"));
					row.setDomainNameIndex(index + 1);
					row.setTargetGroupId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
					row.setTargetGroup(ALL_SUBSCRIBERS);
					row.setTargetGroupIndex(CommonKeys.ALL_SUBSCRIBERS_INDEX);
					row.setValue(resultSet.getInt("mails_per_domain"));
					return row;
				},
				mailingID
			);
			for (TempRow row : overallDomainsRows) {
				otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).value -= row.getValue();
			}
			otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setDomainName(I18nString.getLocaleString("statistic.Other", language));
			otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setDomainNameIndex(domainsMax + 1);
			otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setCategoryIndex(CATEGORY_SENT_EMAILS);
			overallDomainsRows.add(otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID));
			insertIntoTempTable(tempTableID, overallDomainsRows);

			if (targets != null) {
				int domainTargetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
				for (LightTarget target : targets) {
					if (mailingTrackingDataAvailable) {
						String targetDomainsSql = "SELECT COUNT(*) AS mails_per_domain, domain_name"
							+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name FROM customer_" + companyID + "_tbl cust, mailtrack_" + companyID + "_tbl track"
								+ " WHERE cust.customer_id = track.customer_id AND track.mailing_id = ?"
									+ " AND (" + target.getTargetSQL() + "))" + (isOracleDB() ? "" : " sub")
							+ " GROUP BY domain_name HAVING COUNT(*) > 0 ORDER BY mails_per_domain DESC";
						if (domainsMax > 1) {
							if (isOracleDB()) {
								targetDomainsSql = "SELECT * FROM (" + targetDomainsSql + ") WHERE mails_per_domain > 0 AND ROWNUM <= " + domainsMax;
							} else {
								targetDomainsSql = targetDomainsSql + " LIMIT " + domainsMax;
							}
						}

						final int domainTargetGroupIndexFinal = domainTargetGroupIndex;
						List<TempRow> targetDomainsRows = select(logger, targetDomainsSql, (resultSet, index) -> {
								TempRow row = new TempRow();
								row.setCategoryName(CATEGORY_NAME_SENT_EMAILS);
								row.setCategoryIndex(CATEGORY_SENT_EMAILS);
								row.setDomainName(resultSet.getString("domain_name"));
								row.setDomainNameIndex(index + 1);
								row.setTargetGroupId(target.getId());
								row.setTargetGroup(target.getName());
								row.setTargetGroupIndex(domainTargetGroupIndexFinal);
								row.setValue(resultSet.getInt("mails_per_domain"));
								return row;
							},
							mailingID
						);
						for (TempRow row : targetDomainsRows) {
							otherDomainsMap.get(target.getId()).value -= row.getValue();
						}
						otherDomainsMap.get(target.getId()).setDomainName(I18nString.getLocaleString("statistic.Other", language));
						otherDomainsMap.get(target.getId()).setDomainNameIndex(domainsMax + 1);
						otherDomainsMap.get(target.getId()).setCategoryIndex(CATEGORY_SENT_EMAILS);
						targetDomainsRows.add(otherDomainsMap.get(target.getId()));
						insertIntoTempTable(tempTableID, targetDomainsRows);
						domainTargetGroupIndex++;
					}
				}
			}
		}
	}

	private void insertBouncesIntoTempTable(int tempTableID, int mailingID, int companyID, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
		insertHardBouncesIntoTempTable(tempTableID, mailingID, companyID, targets, language, domainsMax, topLevelDomains);
		insertSoftBouncesIntoTempTable(tempTableID, mailingID, companyID, targets, language, domainsMax, topLevelDomains);
	}
	
	private void insertHardBouncesIntoTempTable(int tempTableID, int mailingID, int companyID, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
		insertBouncesIntoTempTable(tempTableID, mailingID, companyID, targets, language, domainsMax, topLevelDomains,
				new Rule("bounce.detail >= 510", CATEGORY_NAME_HARDBOUNCES, CATEGORY_HARDBOUNCES));

	}
	
	private void insertSoftBouncesIntoTempTable(int tempTableID, int mailingID, int companyID, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
		insertBouncesIntoTempTable(tempTableID, mailingID, companyID, targets, language, domainsMax, topLevelDomains,
				new Rule("bounce.detail < 510", CATEGORY_NAME_SOFTBOUNCES, CATEGORY_SOFTBOUNCES));
	}

	public static class Rule {
		private String condition;
		private String categoryName;
		private int categoryIndex;

		Rule(String condition, String categoryName, int categoryIndex) {
			this.condition = condition;
			this.categoryName = categoryName;
			this.categoryIndex = categoryIndex;
		}
		
		public String getCondition() {
			return condition;
		}
		
		public String getCategoryName() {
			return categoryName;
		}
		
		public int getCategoryIndex() {
			return categoryIndex;
		}
	}

	private void insertBouncesIntoTempTable(int tempTableID, int mailingID, int companyID, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains, Rule rule) throws Exception {
		Map<Integer, TempRow> otherDomainsMap = new HashMap<>();

		int overallBouncedMailings = selectInt(logger, "SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust, bounce_tbl bounce WHERE cust.customer_id = bounce.customer_id AND bounce.company_id = ? AND bounce.mailing_id = ? AND " + rule.condition, companyID, mailingID);

		TempRow overallRow = new TempRow();
		overallRow.setCategoryName(rule.categoryName);
		overallRow.setCategoryIndex(rule.categoryIndex);
		overallRow.setTargetGroupId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
		overallRow.setTargetGroup(ALL_SUBSCRIBERS);
		overallRow.setTargetGroupIndex(CommonKeys.ALL_SUBSCRIBERS_INDEX);
		overallRow.setValue(overallBouncedMailings);
		otherDomainsMap.put(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID, overallRow);
		insertIntoTempTable(tempTableID, overallRow);

		if (targets != null) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				int targetSentMailings = selectInt(logger,
					"SELECT COUNT(*) FROM customer_" + companyID + "_tbl cust, bounce_tbl bounce WHERE cust.customer_id = bounce.customer_id AND bounce.company_id = ? AND bounce.mailing_id = ? AND " + rule.condition
						+ " AND (" + target.getTargetSQL() + ")",
					companyID,
					mailingID);

				TempRow targetRow = new TempRow();
				targetRow.setCategoryName(rule.categoryName);
				targetRow.setCategoryIndex(rule.categoryIndex);
				targetRow.setTargetGroupId(target.getId());
				targetRow.setTargetGroup(target.getName());
				targetRow.setTargetGroupIndex(targetGroupIndex);
				targetRow.setValue(targetSentMailings);
				otherDomainsMap.put(target.getId(), targetRow);
				insertIntoTempTable(tempTableID, targetRow);
				targetGroupIndex++;
			}
		}

		String overallDomainsSql = "SELECT COUNT(*) AS bounces_per_domain, domain_name"
			+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name FROM customer_" + companyID + "_tbl cust, bounce_tbl bounce"
				+ " WHERE cust.customer_id = bounce.customer_id AND bounce.company_id = ? AND bounce.mailing_id = ? AND " + rule.condition + ")" + (isOracleDB() ? "" : " sub")
			+ " GROUP BY domain_name HAVING COUNT(*) > 0 ORDER BY bounces_per_domain DESC";
		if (domainsMax > 1) {
			if (isOracleDB()) {
				overallDomainsSql = "SELECT * FROM (" + overallDomainsSql + ") WHERE ROWNUM <= " + domainsMax;
			} else {
				overallDomainsSql = overallDomainsSql + " LIMIT " + domainsMax;
			}
		}

		List<TempRow> overallDomainsRows = select(logger, overallDomainsSql, (resultSet, index) -> {
				TempRow row = new TempRow();
				row.setCategoryName(rule.categoryName);
				row.setCategoryIndex(rule.categoryIndex);
				row.setDomainName(resultSet.getString("domain_name"));
				row.setDomainNameIndex(index + 1);
				row.setTargetGroupId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
				row.setTargetGroup(ALL_SUBSCRIBERS);
				row.setTargetGroupIndex(CommonKeys.ALL_SUBSCRIBERS_INDEX);
				row.setValue(resultSet.getInt("bounces_per_domain"));
				return row;
			},
			companyID,
			mailingID
		);
		for (TempRow row : overallDomainsRows) {
			otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).value -= row.getValue();
		}
		otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setDomainName(I18nString.getLocaleString("statistic.Other", language));
		otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setDomainNameIndex(domainsMax + 1);
		otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setCategoryIndex(rule.categoryIndex);
		overallDomainsRows.add(otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID));
		insertIntoTempTable(tempTableID, overallDomainsRows);

		if (targets != null) {
			int domainTargetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				String targetDomainsSql = "SELECT COUNT(*) AS bounces_per_domain, domain_name"
					+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name FROM customer_" + companyID + "_tbl cust, bounce_tbl bounce"
						+ " WHERE cust.customer_id = bounce.customer_id AND bounce.company_id = ? AND bounce.mailing_id = ? AND " + rule.condition
							+ " AND (" + target.getTargetSQL() + "))" + (isOracleDB() ? "" : " sub")
					+ " GROUP BY domain_name HAVING COUNT(*) > 0 ORDER BY bounces_per_domain DESC";
				if (domainsMax > 1) {
					if (isOracleDB()) {
						targetDomainsSql = "SELECT * FROM (" + targetDomainsSql + ") WHERE bounces_per_domain > 0 AND ROWNUM <= " + domainsMax;
					} else {
						targetDomainsSql = targetDomainsSql + " LIMIT " + domainsMax;
					}
				}

				final int domainTargetGroupIndexFinal = domainTargetGroupIndex;
				List<TempRow> targetDomainsRows = select(logger, targetDomainsSql, (resultSet, index) -> {
						TempRow row = new TempRow();
						row.setCategoryName(rule.categoryName);
						row.setCategoryIndex(rule.categoryIndex);
						row.setDomainName(resultSet.getString("domain_name"));
						row.setDomainNameIndex(index + 1);
						row.setTargetGroupId(target.getId());
						row.setTargetGroup(target.getName());
						row.setTargetGroupIndex(domainTargetGroupIndexFinal);
						row.setValue(resultSet.getInt("bounces_per_domain"));
						return row;
					},
					companyID,
					mailingID
				);
				for (TempRow row : targetDomainsRows) {
					otherDomainsMap.get(target.getId()).value -= row.getValue();
				}
				otherDomainsMap.get(target.getId()).setDomainName(I18nString.getLocaleString("statistic.Other", language));
				otherDomainsMap.get(target.getId()).setDomainNameIndex(domainsMax + 1);
				otherDomainsMap.get(target.getId()).setCategoryIndex(rule.categoryIndex);
				targetDomainsRows.add(otherDomainsMap.get(target.getId()));
				insertIntoTempTable(tempTableID, targetDomainsRows);
				domainTargetGroupIndex++;
			}
		}
	}

	private void insertOpenersIntoTempTable(int tempTableID, int mailingID, int companyID, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
		Map<Integer, TempRow> otherDomainsMap = new HashMap<>();

		int overallOpeners = selectInt(logger, "SELECT COUNT(DISTINCT cust.customer_id) FROM customer_" + companyID + "_tbl cust, onepixellog_device_" + companyID + "_tbl opl WHERE cust.customer_id = opl.customer_id AND opl.mailing_id = ?", mailingID);

		TempRow overallRow = new TempRow();
		overallRow.setCategoryName(CATEGORY_NAME_OPENERS);
		overallRow.setCategoryIndex(CATEGORY_TOTAL_OPENERS);
		overallRow.setTargetGroupId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
		overallRow.setTargetGroup(ALL_SUBSCRIBERS);
		overallRow.setTargetGroupIndex(CommonKeys.ALL_SUBSCRIBERS_INDEX);
		overallRow.setValue(overallOpeners);
		otherDomainsMap.put(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID, overallRow);
		insertIntoTempTable(tempTableID, overallRow);

		if (targets != null) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				int targetSentMailings = selectInt(logger,
					"SELECT COUNT(DISTINCT cust.customer_id) FROM customer_" + companyID + "_tbl cust, onepixellog_device_" + companyID + "_tbl opl WHERE cust.customer_id = opl.customer_id AND opl.mailing_id = ?"
						+ " AND (" + target.getTargetSQL() + ")",
					mailingID);

				TempRow targetRow = new TempRow();
				targetRow.setCategoryName(CATEGORY_NAME_OPENERS);
				targetRow.setCategoryIndex(CATEGORY_TOTAL_OPENERS);
				targetRow.setTargetGroupId(target.getId());
				targetRow.setTargetGroup(target.getName());
				targetRow.setTargetGroupIndex(targetGroupIndex);
				targetRow.setValue(targetSentMailings);
				otherDomainsMap.put(target.getId(), targetRow);
				insertIntoTempTable(tempTableID, targetRow);
				targetGroupIndex++;
			}
		}

		String overallDomainsSql = "SELECT COUNT(DISTINCT customer_id) AS openers_per_domain, domain_name"
			+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name, cust.customer_id FROM customer_" + companyID + "_tbl cust, onepixellog_device_" + companyID + "_tbl opl"
				+ " WHERE cust.customer_id = opl.customer_id AND opl.mailing_id = ?)" + (isOracleDB() ? "" : " sub")
			+ " GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0 ORDER BY openers_per_domain DESC";
		if (domainsMax > 1) {
			if (isOracleDB()) {
				overallDomainsSql = "SELECT * FROM (" + overallDomainsSql + ") WHERE ROWNUM <= " + domainsMax;
			} else {
				overallDomainsSql = overallDomainsSql + " LIMIT " + domainsMax;
			}
		}

		List<TempRow> overallDomainsRows = select(logger, overallDomainsSql, (resultSet, index) -> {
				TempRow row = new TempRow();
				row.setCategoryName(CATEGORY_NAME_OPENERS);
				row.setCategoryIndex(CATEGORY_OPENERS);
				row.setDomainName(resultSet.getString("domain_name"));
				row.setDomainNameIndex(index + 1);
				row.setTargetGroupId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
				row.setTargetGroup(ALL_SUBSCRIBERS);
				row.setTargetGroupIndex(CommonKeys.ALL_SUBSCRIBERS_INDEX);
				row.setValue(resultSet.getInt("openers_per_domain"));
				return row;
			},
			mailingID
		);
		for (TempRow row : overallDomainsRows) {
			otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).value -= row.getValue();
		}
		otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setDomainName(I18nString.getLocaleString("statistic.Other", language));
		otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setDomainNameIndex(domainsMax + 1);
		otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setCategoryIndex(CATEGORY_OPENERS);
		overallDomainsRows.add(otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID));
		insertIntoTempTable(tempTableID, overallDomainsRows);

		if (targets != null) {
			int domainTargetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				String targetDomainsSql = "SELECT COUNT(DISTINCT customer_id) AS openers_per_domain, domain_name"
					+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name, cust.customer_id FROM customer_" + companyID + "_tbl cust, onepixellog_device_" + companyID + "_tbl opl"
						+ " WHERE cust.customer_id = opl.customer_id AND opl.mailing_id = ?"
							+ " AND (" + target.getTargetSQL() + "))" + (isOracleDB() ? "" : " sub")
					+ " GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0 ORDER BY openers_per_domain DESC";
				if (domainsMax > 1) {
					if (isOracleDB()) {
						targetDomainsSql = "SELECT * FROM (" + targetDomainsSql + ") WHERE openers_per_domain > 0 AND ROWNUM <= " + domainsMax;
					} else {
						targetDomainsSql = targetDomainsSql + " LIMIT " + domainsMax;
					}
				}

				final int domainTargetGroupIndexFinal = domainTargetGroupIndex;
				List<TempRow> targetDomainsRows = select(logger, targetDomainsSql, (resultSet, index) -> {
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
					},
					mailingID
				);
				for (TempRow row : targetDomainsRows) {
					otherDomainsMap.get(target.getId()).value -= row.getValue();
				}
				otherDomainsMap.get(target.getId()).setDomainName(I18nString.getLocaleString("statistic.Other", language));
				otherDomainsMap.get(target.getId()).setDomainNameIndex(domainsMax + 1);
				otherDomainsMap.get(target.getId()).setCategoryIndex(CATEGORY_OPENERS);
				targetDomainsRows.add(otherDomainsMap.get(target.getId()));
				insertIntoTempTable(tempTableID, targetDomainsRows);
				domainTargetGroupIndex++;
			}
		}
	}

	@DaoUpdateReturnValueCheck
	private void insertClickersIntoTempTable(int tempTableID, int mailingID, int companyID, List<LightTarget> targets, String language, int domainsMax, boolean topLevelDomains) throws Exception {
		Map<Integer, TempRow> otherDomainsMap = new HashMap<>();

		int overallClickers = selectInt(logger, "SELECT COUNT(DISTINCT cust.customer_id) FROM customer_" + companyID + "_tbl cust, rdirlog_" + companyID + "_tbl rlog WHERE cust.customer_id = rlog.customer_id AND rlog.mailing_id = ?", mailingID);

		TempRow overallRow = new TempRow();
		overallRow.setCategoryName(CATEGORY_NAME_CLICKERS);
		overallRow.setCategoryIndex(CATEGORY_TOTAL_CLICKERS);
		overallRow.setTargetGroupId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
		overallRow.setTargetGroup(ALL_SUBSCRIBERS);
		overallRow.setTargetGroupIndex(CommonKeys.ALL_SUBSCRIBERS_INDEX);
		overallRow.setValue(overallClickers);
		otherDomainsMap.put(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID, overallRow);
		insertIntoTempTable(tempTableID, overallRow);

		if (targets != null) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				int targetSentMailings = selectInt(logger,
					"SELECT COUNT(DISTINCT cust.customer_id) FROM customer_" + companyID + "_tbl cust, rdirlog_" + companyID + "_tbl rlog WHERE cust.customer_id = rlog.customer_id AND rlog.mailing_id = ?"
						+ " AND (" + target.getTargetSQL() + ")",
					mailingID);

				TempRow targetRow = new TempRow();
				targetRow.setCategoryName(CATEGORY_NAME_CLICKERS);
				targetRow.setCategoryIndex(CATEGORY_TOTAL_CLICKERS);
				targetRow.setTargetGroupId(target.getId());
				targetRow.setTargetGroup(target.getName());
				targetRow.setTargetGroupIndex(targetGroupIndex);
				targetRow.setValue(targetSentMailings);
				otherDomainsMap.put(target.getId(), targetRow);
				insertIntoTempTable(tempTableID, targetRow);
				targetGroupIndex++;
			}
		}

		String overallDomainsSql = "SELECT COUNT(DISTINCT customer_id) AS clickers_per_domain, domain_name"
			+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name, cust.customer_id FROM customer_" + companyID + "_tbl cust, rdirlog_" + companyID + "_tbl rlog"
				+ " WHERE cust.customer_id = rlog.customer_id AND rlog.mailing_id = ?)" + (isOracleDB() ? "" : " sub")
			+ " GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0 ORDER BY clickers_per_domain DESC";
		if (domainsMax > 1) {
			if (isOracleDB()) {
				overallDomainsSql = "SELECT * FROM (" + overallDomainsSql + ") WHERE ROWNUM <= " + domainsMax;
			} else {
				overallDomainsSql = overallDomainsSql + " LIMIT " + domainsMax;
			}
		}

		List<TempRow> overallDomainsRows = select(logger, overallDomainsSql, (resultSet, index) -> {
				TempRow row = new TempRow();
				row.setCategoryName(CATEGORY_NAME_CLICKERS);
				row.setCategoryIndex(CATEGORY_CLICKERS);
				row.setDomainName(resultSet.getString("domain_name"));
				row.setDomainNameIndex(index + 1);
				row.setTargetGroupId(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID);
				row.setTargetGroup(ALL_SUBSCRIBERS);
				row.setTargetGroupIndex(CommonKeys.ALL_SUBSCRIBERS_INDEX);
				row.setValue(resultSet.getInt("clickers_per_domain"));
				return row;
			},
			mailingID
		);
		for (TempRow row : overallDomainsRows) {
			otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).value -= row.getValue();
		}
		otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setDomainName(I18nString.getLocaleString("statistic.Other", language));
		otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setDomainNameIndex(domainsMax + 1);
		otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID).setCategoryIndex(CATEGORY_CLICKERS);
		overallDomainsRows.add(otherDomainsMap.get(CommonKeys.ALL_SUBSCRIBERS_TARGETGROUPID));
		insertIntoTempTable(tempTableID, overallDomainsRows);

		if (targets != null) {
			int domainTargetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;
			for (LightTarget target : targets) {
				String targetDomainsSql = "SELECT COUNT(DISTINCT customer_id) AS clickers_per_domain, domain_name"
					+ " FROM (SELECT " + getDomainFromEmailExpression(topLevelDomains) + " AS domain_name, cust.customer_id FROM customer_" + companyID + "_tbl cust, rdirlog_" + companyID + "_tbl rlog"
						+ " WHERE cust.customer_id = rlog.customer_id AND rlog.mailing_id = ?"
							+ " AND (" + target.getTargetSQL() + "))" + (isOracleDB() ? "" : " sub")
					+ " GROUP BY domain_name HAVING COUNT(DISTINCT customer_id) > 0 ORDER BY clickers_per_domain DESC";
				if (domainsMax > 1) {
					if (isOracleDB()) {
						targetDomainsSql = "SELECT * FROM (" + targetDomainsSql + ") WHERE clickers_per_domain > 0 AND ROWNUM <= " + domainsMax;
					} else {
						targetDomainsSql = targetDomainsSql + " LIMIT " + domainsMax;
					}
				}

				final int domainTargetGroupIndexFinal = domainTargetGroupIndex;
				List<TempRow> targetDomainsRows = select(logger, targetDomainsSql, (resultSet, index) -> {
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
					},
					mailingID
				);
				for (TempRow row : targetDomainsRows) {
					otherDomainsMap.get(target.getId()).value -= row.getValue();
				}
				otherDomainsMap.get(target.getId()).setDomainName(I18nString.getLocaleString("statistic.Other", language));
				otherDomainsMap.get(target.getId()).setDomainNameIndex(domainsMax + 1);
				otherDomainsMap.get(target.getId()).setCategoryIndex(CATEGORY_CLICKERS);
				targetDomainsRows.add(otherDomainsMap.get(target.getId()));
				insertIntoTempTable(tempTableID, targetDomainsRows);
				domainTargetGroupIndex++;
			}
		}
	}

	private void updateRates(int tempTableID, List<LightTarget> targets, boolean mailtrackingDataAvailable) throws Exception {
		boolean targetsAvailable = CollectionUtils.isNotEmpty(targets);

		int totalSentEmails = getTotalSentEmails(tempTableID, CommonKeys.ALL_SUBSCRIBERS_INDEX);
		updateRates(tempTableID, CommonKeys.ALL_SUBSCRIBERS_INDEX, totalSentEmails, CATEGORY_SENT_EMAILS, CATEGORY_SOFTBOUNCES, CATEGORY_HARDBOUNCES);

		if (targetsAvailable) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;

            for (int i = 0; i < targets.size(); i++) {
				totalSentEmails = mailtrackingDataAvailable ? getTotalSentEmails(tempTableID, targetGroupIndex) : -1;
				updateRates(tempTableID, targetGroupIndex, totalSentEmails, CATEGORY_SENT_EMAILS, CATEGORY_SOFTBOUNCES, CATEGORY_HARDBOUNCES);
				targetGroupIndex++;
			}
		}

		int totalClickers = getTotalClickers(tempTableID, CommonKeys.ALL_SUBSCRIBERS_INDEX);
		updateRates(tempTableID, CommonKeys.ALL_SUBSCRIBERS_INDEX, totalClickers, CATEGORY_CLICKERS);

		if (targetsAvailable) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;

            for (int i = 0; i < targets.size(); i++) {
				totalClickers = getTotalClickers(tempTableID, targetGroupIndex);
				updateRates(tempTableID, targetGroupIndex, totalClickers, CATEGORY_CLICKERS);
				targetGroupIndex++;
			}
		}

		int totalOpeners = getTotalOpeners(tempTableID, CommonKeys.ALL_SUBSCRIBERS_INDEX);
		updateRates(tempTableID, CommonKeys.ALL_SUBSCRIBERS_INDEX, totalOpeners, CATEGORY_OPENERS);

		if (targetsAvailable) {
			int targetGroupIndex = CommonKeys.ALL_SUBSCRIBERS_INDEX + 1;

            for (int i = 0; i < targets.size(); i++) {
				totalOpeners = getTotalOpeners(tempTableID, targetGroupIndex);
				updateRates(tempTableID, targetGroupIndex, totalOpeners, CATEGORY_OPENERS);
				targetGroupIndex++;
			}
		}
	}

	private void updateRates(int tempTableID, int targetGroupIndex, int base, int... categories) throws Exception {
		if (categories.length == 0) {
			return;
		} else {
			StringBuilder sqlUpdateRatesBuilder = new StringBuilder();

			sqlUpdateRatesBuilder.append("UPDATE ").append(getTemporaryTableName(tempTableID)).append(" SET rate = ");

			if (base <= 0) {
				// Negative value tells that rate isn't available
				sqlUpdateRatesBuilder.append(base < 0 ? -1 : 0);
			} else {
				sqlUpdateRatesBuilder.append("(value * 1.0) / ").append(base);
			}
	
			sqlUpdateRatesBuilder.append(" WHERE category_index IN (" + StringUtils.join(ArrayUtils.toObject(categories), ", ") + ") AND targetgroup_index = ?");
	
			updateEmbedded(logger, sqlUpdateRatesBuilder.toString(), targetGroupIndex);
		}
	}

	private int getTotalSentEmails(int tempTableID, int targetGroupIndex) throws Exception {
		String sqlGetTotalSentEmails = "SELECT value FROM " + getTemporaryTableName(tempTableID) + " WHERE category_index = ? AND targetgroup_index = ? AND domainname IS NULL";
		return selectEmbeddedInt(logger, sqlGetTotalSentEmails, CATEGORY_TOTAL_SENT_EMAILS, targetGroupIndex);
	}

	private int getTotalClickers(int tempTableID, int targetGroupIndex) throws Exception {
		String sqlGetTotalClickers = "SELECT value FROM " + getTemporaryTableName(tempTableID) + " WHERE category_index = ? AND targetgroup_index = ? AND domainname IS NULL";
		return selectEmbeddedInt(logger, sqlGetTotalClickers, CATEGORY_TOTAL_CLICKERS, targetGroupIndex);
	}

	private int getTotalOpeners(int tempTableID, int targetGroupIndex) throws Exception {
		String sqlGetTotalOpeners = "SELECT value FROM " + getTemporaryTableName(tempTableID) + " WHERE category_index = ? AND targetgroup_index = ? AND domainname IS NULL";
		return selectEmbeddedInt(logger, sqlGetTotalOpeners, CATEGORY_TOTAL_OPENERS, targetGroupIndex);
	}

	private void insertIntoTempTable(int tempTableID, List<TempRow> rows) throws Exception {
		for (TempRow row : rows) {
			insertIntoTempTable(tempTableID, row);
		}
	}

	private void insertIntoTempTable(int tempTableID, TempRow row) throws Exception {
		String insertSql = "INSERT INTO " + getTemporaryTableName(tempTableID)
			+ " (category_name, category_index, domainname, domainname_index, targetgroup, targetgroup_id, targetgroup_index, value, rate)"
			+ " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

		updateEmbedded(logger, insertSql,
			row.getCategoryName(),
			row.getCategoryIndex(),
			row.getDomainName(),
			row.getDomainNameIndex(),
			row.getTargetGroup(),
			row.getTargetGroupId(),
			row.getTargetGroupIndex(),
			row.getValue(),
			row.getRate()
		);
	}

	public String getDomainFromEmailExpression(boolean topLevelDomains) {
		String getPosExpression;
		if (isOracleDB()) {
			getPosExpression = topLevelDomains ? "INSTR(email, '.', -1) " : "INSTR(email, '@')";
		} else {
			getPosExpression = topLevelDomains ? "LENGTH(email) - INSTR(REVERSE(email), '.')" : "INSTR(email, '@')";
		}
		return "SUBSTR(email, " + getPosExpression + " + 1)";
	}

	/**
	 * create a temporary table to collect the values from different queries for top_domains.rptdesign report in one table
	 *
	 * @return id of the create temporary table
	 * @throws Exception
	 */
	protected int createTempTable() throws Exception {
		int tempTableID = getNextTmpID();
		executeEmbedded(logger,
			"CREATE TABLE " + getTemporaryTableName(tempTableID) + " ("
				+ "category_name VARCHAR(200),"
				+ " category_index INTEGER,"
				+ " domainname VARCHAR(200),"
				+ " domainname_index INTEGER,"
				+ " targetgroup_id INTEGER,"
				+ " targetgroup VARCHAR(200),"
				+ " targetgroup_index INTEGER,"
				+ " value INTEGER,"
				+ " rate DOUBLE"
			+ ")");
		return tempTableID;
	}
}
