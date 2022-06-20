/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.importvalues.MailType;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.jdbc.core.RowMapper;

import com.agnitas.dao.DaoUpdateReturnValueCheck;
import com.agnitas.emm.core.birtreport.dto.FilterType;
import com.agnitas.reporting.birt.external.beans.BirtReportCompareStatRow;
import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.SendStatRow;
import com.agnitas.reporting.birt.external.utils.BirtReporUtils;
import com.agnitas.reporting.birt.external.utils.FormatTools;

public class BirtReportMailingCompareDataSet extends BIRTDataSet {
	private static final transient Logger logger = LogManager.getLogger(BirtReportMailingCompareDataSet.class);

    private Map<Integer, Integer> categoriesByTable = new HashMap<>();

	public static final int CATEGORIES_PER_ROW = 4;

    private void fillCategoriesByTable() {
        categoriesByTable.put(CommonKeys.DELIVERED_EMAILS_INDEX, 1);
        categoriesByTable.put(CommonKeys.DELIVERED_EMAILS_DELIVERED_INDEX, 1);
        categoriesByTable.put(CommonKeys.OPENERS_TOTAL_INDEX, 1);
        categoriesByTable.put(CommonKeys.OPENINGS_ANONYMOUS_INDEX, 1);
        categoriesByTable.put(CommonKeys.CLICKER_INDEX, 1);
        categoriesByTable.put(CommonKeys.CLICKS_ANONYMOUS_INDEX, 1);
        categoriesByTable.put(CommonKeys.HARD_BOUNCES_INDEX, 1);
        categoriesByTable.put(CommonKeys.OPT_OUTS_INDEX, 1);
        categoriesByTable.put(CommonKeys.REVENUE_INDEX, 1);
        categoriesByTable.put(CommonKeys.SEND_DATE_INDEX, 1);
        categoriesByTable.put(CommonKeys.SCHEDULED_SEND_TIME_INDEX, 1);

        categoriesByTable.put(CommonKeys.OPENERS_MEASURED_INDEX, 3);
        categoriesByTable.put(CommonKeys.OPENERS_INVISIBLE_INDEX, 3);

        categoriesByTable.put(CommonKeys.OPENERS_PC_INDEX, 4);
        categoriesByTable.put(CommonKeys.OPENERS_MOBILE_INDEX, 4);
        categoriesByTable.put(CommonKeys.OPENERS_TABLET_INDEX, 4);
        categoriesByTable.put(CommonKeys.OPENERS_SMARTTV_INDEX, 4);
        categoriesByTable.put(CommonKeys.OPENERS_PC_AND_MOBILE_INDEX, 4);

        categoriesByTable.put(CommonKeys.CLICKER_TRACKED_INDEX, 5);
        categoriesByTable.put(CommonKeys.CLICKER_PC_INDEX, 5);
        categoriesByTable.put(CommonKeys.CLICKER_MOBILE_INDEX, 5);
        categoriesByTable.put(CommonKeys.CLICKER_TABLET_INDEX, 5);
        categoriesByTable.put(CommonKeys.CLICKER_SMARTTV_INDEX, 5);
        categoriesByTable.put(CommonKeys.CLICKER_PC_AND_MOBILE_INDEX, 5);

        categoriesByTable.put(CommonKeys.SENT_HTML_INDEX, 6);
        categoriesByTable.put(CommonKeys.SENT_TEXT_INDEX, 6);
        categoriesByTable.put(CommonKeys.SENT_OFFLINE_HTML_INDEX, 6);
    }
    
    public class CompareStatRowRowMapper implements RowMapper<BirtReportCompareStatRow> {
		@Override
		public BirtReportCompareStatRow mapRow(ResultSet resultSet, int index) throws SQLException {
	        try {
				BirtReportCompareStatRow row = new BirtReportCompareStatRow();
				
				row.setCategory(resultSet.getString("category"));
				row.setCategoryindex(resultSet.getInt("category_index"));
				row.setTargetgroup(resultSet.getString("targetgroup"));
				row.setTargetgroupindex(resultSet.getInt("targetgroup_index"));
				row.setCount(resultSet.getInt("value"));
				row.setRate(resultSet.getDouble("rate"));
				row.setMailingId(resultSet.getInt("mailing_id"));
				row.setMailingName(resultSet.getString("mailing_name"));
				row.setSendDate(resultSet.getTimestamp("send_date"));
				row.setScheduledSendTime(resultSet.getTimestamp("scheduled_send_time"));
				row.setAssignedTargets(resultSet.getString("assigned_targets"));
				
				return row;
			} catch (SQLException e) {
				logger.error("Error in CompareStatRowRowMapper: " + e.getMessage(), e);
				throw e;
			} catch (Exception e) {
				logger.error("Error in CompareStatRowRowMapper: " + e.getMessage(), e);
				throw e;
			}
		}
    }

    public int prepareReport(String mailingIdsStr, @VelocityCheck int companyId, String targetsStr, String hiddenTargetIdStr,
                             String figuresOptions) throws Exception {
		List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);
		List<Integer> mailingIds = parseCommaSeparatedIds(mailingIdsStr);
		int ownTempTableId = createTempTable();
		MailingSummaryDataSet mailingSummaryDataSet = new MailingSummaryDataSet();

        final String recipientType = CommonKeys.TYPE_ALL_SUBSCRIBERS;
        final DateFormats dateFormats = new DateFormats();

        final List<LightTarget> targets = getTargets(targetsStr, companyId);
        
        String hiddenTargetSql = getTargetSqlString(hiddenTargetIdStr, companyId);

		for (Integer mailingId : mailingIds) {
			int tempTableId = mailingSummaryDataSet.createTempTable();
			mailingSummaryDataSet.insertSendIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, dateFormats);
			mailingSummaryDataSet.insertDeliveredIntoTempTable(tempTableId, mailingId, companyId, targets, hiddenTargetSql, recipientType, dateFormats);
            mailingSummaryDataSet.insertClickersIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, true, dateFormats);
            if (figures.contains(BirtReporUtils.BirtReportFigure.CLICKS_ANONYMOUS)) {
                mailingSummaryDataSet.insertClicksAnonymousIntoTempTable(mailingId, tempTableId, companyId, dateFormats);
            }
            if (figures.contains(BirtReporUtils.BirtReportFigure.OPENINGS_ANONYMOUS)) {
                mailingSummaryDataSet.insertOpenedAnonymousIntoTempTable(mailingId, tempTableId, companyId, dateFormats);
            }
            if (figures.contains(BirtReporUtils.BirtReportFigure.HARDBOUNCES)) {
                mailingSummaryDataSet.insertBouncesIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, false, dateFormats);
            }
            if (figures.contains(BirtReporUtils.BirtReportFigure.SIGNED_OFF)) {
                mailingSummaryDataSet.insertOptOutsIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, dateFormats);
            }
            if (figures.contains(BirtReporUtils.BirtReportFigure.REVENUE)) {
                mailingSummaryDataSet.insertRevenueIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, dateFormats);
            }
        	mailingSummaryDataSet.insertOpenersIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, true, false, dateFormats);
			mailingSummaryDataSet.insertOpenedInvisibleIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, false, dateFormats);

            if (!figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_AFTER_DEVICE)) {
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_MOBILE_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_PC_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_PC_AND_MOBILE_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_TABLET_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_SMARTTV_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_TRACKED_INDEX);
            }
            if (!figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_AFTER_DEVICE)) {
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.CLICKER_MOBILE_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.CLICKER_PC_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.CLICKER_PC_AND_MOBILE_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.CLICKER_TABLET_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.CLICKER_SMARTTV_INDEX);
                mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.CLICKER_TRACKED_INDEX);
            }

			mailingSummaryDataSet.updateRates(tempTableId, companyId, targets);

//			// we should remove this after updateRates, as it is used for counting rates
//			//mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.CLICKER_TRACKED_INDEX);
//			mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_TRACKED_INDEX);

			migrateDataToOwnTempTable(ownTempTableId, tempTableId, companyId, mailingId);

			// hack for displaying send date in crossTab with all other categories as last column
			insertIntoTempTable(ownTempTableId, new BirtReportCompareStatRow(CommonKeys.SEND_DATE, CommonKeys.SEND_DATE_INDEX, CommonKeys.ALL_SUBSCRIBERS,
					CommonKeys.ALL_SUBSCRIBERS_INDEX, 0, 0, mailingId, "", new Date(), 0, ""));

            // hack for displaying scheduled send time in crossTab with all other categories as last column
            insertIntoTempTable(ownTempTableId, new BirtReportCompareStatRow(CommonKeys.SCHEDULED_SEND_TIME, CommonKeys.SCHEDULED_SEND_TIME_INDEX, CommonKeys.ALL_SUBSCRIBERS,
                    CommonKeys.ALL_SUBSCRIBERS_INDEX, 0, 0, mailingId, "", new Date(), 0, ""));

			dropTempTable(tempTableId);
		}

		addSentMailsByMailtypeData(mailingIds, companyId, ownTempTableId, figures);

        if (!figures.contains(BirtReporUtils.BirtReportFigure.HTML)) {
            mailingSummaryDataSet.removeCategoryData(ownTempTableId, CommonKeys.SENT_HTML_INDEX);
        }
        if (!figures.contains(BirtReporUtils.BirtReportFigure.TEXT)) {
            mailingSummaryDataSet.removeCategoryData(ownTempTableId, CommonKeys.SENT_TEXT_INDEX);
        }
        if (!figures.contains(BirtReporUtils.BirtReportFigure.OFFLINE_HTML)) {
            mailingSummaryDataSet.removeCategoryData(ownTempTableId, CommonKeys.SENT_OFFLINE_HTML_INDEX);
        }

        addMailingNamesAndSendDates(mailingIds, companyId, ownTempTableId);

        return ownTempTableId;
	}

	public List<BirtReportCompareStatRow> getComparisonData(int tempTableID, String sortBy) throws Exception {
        fillCategoriesByTable();
		List<BirtReportCompareStatRow> resultsFromTempTable = getResultsFromTempTable(tempTableID);

		ArrayList<Integer> categories = new ArrayList<>();
		for (BirtReportCompareStatRow statRow : resultsFromTempTable) {
			if (!categories.contains(statRow.getCategoryindex())) {
				categories.add(statRow.getCategoryindex());
			}
		}
        List<BirtReportCompareStatRow> newResult = new ArrayList<>();
		for (BirtReportCompareStatRow statRow : resultsFromTempTable) {
            if (categoriesByTable.containsKey(statRow.getCategoryindex())) {
                statRow.setCategoryRowIndex(categoriesByTable.get(statRow.getCategoryindex()));
                newResult.add(statRow);
            } else {
			    statRow.setCategoryRowIndex(categories.indexOf(statRow.getCategoryindex()) / CATEGORIES_PER_ROW);
            }
		}
        if (sortBy.equals("date")) {
            Collections.sort(newResult, (o1, o2) -> DateUtilities.compare(o1.getSendDate(), o2.getSendDate()));
        } else {
            Collections.sort(newResult, (o1, o2) -> o1.getMailingName().compareToIgnoreCase(o2.getMailingName()));
        }
        int rowNum = 0;
        Map<Integer, Integer> usedMailingId = new HashMap<>();
        for (BirtReportCompareStatRow statRow : newResult) {
            if (!usedMailingId.containsKey(statRow.getMailingId())) {
                rowNum++;
                usedMailingId.put(statRow.getMailingId(), rowNum);
            }
            statRow.setRowNum(usedMailingId.get(statRow.getMailingId()));
        }
        return newResult;
	}

	/**
	 * Count values sentHtml, sentText, sentOfflineHTML if needed
	 * @throws Exception
	 */
	private void addSentMailsByMailtypeData(List<Integer> mailingIds, @VelocityCheck int companyId, int tempTableID, List<BirtReporUtils.BirtReportFigure> figures) throws Exception {
		if (!(figures.contains(BirtReporUtils.BirtReportFigure.HTML) || figures.contains(BirtReporUtils.BirtReportFigure.TEXT) ||
				figures.contains(BirtReporUtils.BirtReportFigure.OFFLINE_HTML))) {
			return;
		}
        if (CollectionUtils.isEmpty(mailingIds)) {
            return;
        }

		String query = "SELECT mailing_id, SUM(no_of_mailings) mails_sent, mailtype FROM mailing_account_tbl WHERE mailing_id IN (" +
				StringUtils.join(mailingIds, ',') + ") AND status_field = 'W' and mailtype is not null AND company_id = ? GROUP BY mailtype, mailing_id";
		List<Map<String, Object>> resultList = select(logger, query, companyId);

        // Map which contains values for mail types "2" and "3" calculation as Offline-HTML mail type
        Map<Integer, BirtReportCompareStatRow> offlineHtmlValues = new HashMap<>();

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
				int mailingId = ((Number) map.get("mailing_id")).intValue();
				int mailsSent = ((Number) map.get("mails_sent")).intValue();

                // use special scenario for Offline-HTML mails calculation
                if (categoryIndex == CommonKeys.SENT_OFFLINE_HTML_INDEX) {
                    BirtReportCompareStatRow row;
                    if (offlineHtmlValues.containsKey(mailingId)) {
                        row = offlineHtmlValues.get(mailingId);
                        row.setCount(row.getCount() + mailsSent);
                    } else {
                        row = new BirtReportCompareStatRow(category, categoryIndex, CommonKeys.ALL_SUBSCRIBERS,
                                CommonKeys.ALL_SUBSCRIBERS_INDEX, mailsSent, 0, mailingId, "", new Date(), 0, "");
                        offlineHtmlValues.put(mailingId, row);
                    }
                } else {
                    //@todo: do we need to count rate here somehow?
                    BirtReportCompareStatRow statRow = new BirtReportCompareStatRow(category, categoryIndex, CommonKeys.ALL_SUBSCRIBERS,
                            CommonKeys.ALL_SUBSCRIBERS_INDEX, mailsSent, 0, mailingId, "", new Date(), 0, "");
                    insertIntoTempTable(tempTableID, statRow);
                }
			}
		}
        // add to the temporary table "Offline-HTML" mail type values
        List<BirtReportCompareStatRow> list = new ArrayList<>(offlineHtmlValues.values());
        for (BirtReportCompareStatRow row: list) {
            insertIntoTempTable(tempTableID, row);
        }
	}

	private void migrateDataToOwnTempTable(int ownTempTableID, int curMailingTempTableID, @VelocityCheck int companyId, Integer mailingId) throws Exception {
		// get data for this mailing from mailing summary temp-table
		MailingSummaryDataSet mailingSummaryDataSet = new MailingSummaryDataSet();
		List<MailingSummaryDataSet.MailingSummaryRow> summaryData = mailingSummaryDataSet.getSummaryData(curMailingTempTableID);
		// insert all the data to this report's temp table
		for (SendStatRow sendStatRow : summaryData) {
			BirtReportCompareStatRow statRow = new BirtReportCompareStatRow(sendStatRow, mailingId, new Date(), "", 0, "");
			insertIntoTempTable(ownTempTableID, statRow);
		}
	}

	@DaoUpdateReturnValueCheck
	private void addMailingNamesAndSendDates(List<Integer> mailingIds, @VelocityCheck int companyId, int tempTableID) throws Exception {
        if (CollectionUtils.isEmpty(mailingIds)) {
            return;
        }

		// get names of this mailing's target groups
		MailingDataSet mailingDataSet = new MailingDataSet();

        Map<Integer, String> mailingTargets = new HashMap<>();
        for (Integer mailingId : mailingIds) {
            List<String> targets = mailingDataSet.getTargets(mailingId, companyId);
            String mailingTargetsStr = StringUtils.join(targets, ",\n");
            mailingTargets.put(mailingId, mailingTargetsStr);
        }

        // get mailings names and send dates
		String query =
			"SELECT"
			+ " mail.mailing_id,"
			+ " mail.shortname AS mailing_name,"
			+ " MAX(md.senddate) AS scheduled_send_time,"
			+ " MAX(ma.timestamp) AS send_date"
			+ " FROM mailing_tbl mail"
			+ " LEFT JOIN maildrop_status_tbl md ON md.mailing_id = mail.mailing_id"
			+ " LEFT JOIN mailing_account_tbl ma ON ma.mailing_id = mail.mailing_id AND ma.status_field NOT IN ('A', 'T')"
			+ " WHERE mail.mailing_id IN (" + StringUtils.join(mailingIds, ", ") + ") AND mail.company_id = ?"
			+ " GROUP BY mail.shortname, mail.mailing_id";
        List<Map<String, Object>> resultList = select(logger, query, companyId);
        for (Map<String, Object> map : resultList) {
            String mailingName = (String) map.get("mailing_name");
            Date sendDate = (Date) map.get("send_date");
            Date scheduledSendTime = (Date) map.get("scheduled_send_time");
            int mailingId = ((Number) map.get("mailing_id")).intValue();
            updateEmbedded(logger, "UPDATE tmp_report_aggregation_" + tempTableID + "_tbl SET mailing_name = ?, send_date = ?, scheduled_send_time = ?, assigned_targets = ? where mailing_id = ?",
            	mailingName, sendDate, scheduledSendTime, mailingTargets.get(mailingId), mailingId);
        }
	}

	private int createTempTable() throws Exception {
		int tempTableID = getNextTmpID();
		executeEmbedded(logger,
			"CREATE TABLE tmp_report_aggregation_" + tempTableID + "_tbl ("
				+ "category VARCHAR(200),"
				+ " category_index INTEGER,"
				+ " targetgroup VARCHAR(200),"
				+ " targetgroup_index INTEGER,"
				+ " value INTEGER,"
				+ " rate DOUBLE,"
				+ " mailing_id INTEGER,"
				+ " send_date TIMESTAMP,"
				+ " scheduled_send_time TIMESTAMP,"
				+ " mailing_name VARCHAR(200),"
				+ " assigned_targets VARCHAR(1000)"
			+ ")");
		return tempTableID;
	}

	@DaoUpdateReturnValueCheck
	private void insertIntoTempTable(int tempTableID, BirtReportCompareStatRow statRow) throws Exception {
		String insert = "INSERT INTO tmp_report_aggregation_" + tempTableID + "_tbl " +
				"(category, category_index, targetgroup, targetgroup_index, value, rate, mailing_id, mailing_name, send_date, scheduled_send_time, assigned_targets) " +
				"VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
		updateEmbedded(logger, insert, statRow.getCategory(), statRow.getCategoryindex(), statRow.getTargetgroup(),
                statRow.getTargetgroupindex(), statRow.getCount(), statRow.getRate(), statRow.getMailingId(),
                statRow.getMailingName(), statRow.getSendDate(), statRow.getScheduledSendTime(), statRow.getAssignedTargets());
	}

	private List<BirtReportCompareStatRow> getResultsFromTempTable(int tempTableID) throws Exception {
		String query = "SELECT * FROM tmp_report_aggregation_" + tempTableID + "_tbl ORDER BY category_index, targetgroup_index ";
		return selectEmbedded(logger, query, new CompareStatRowRowMapper());
	}

    private Map<Map<Integer, Integer>, Map<Integer, BirtReportCompareStatRow>> getCategoriesData(String query, List<Integer> categories, int blockNumber) throws Exception {
        List<BirtReportCompareStatRow> block = selectEmbedded(logger, query.replace("?", StringUtils.join(categories.toArray(), ", ")), new CompareStatRowRowMapper());
        Map<Map<Integer, Integer>, Map<Integer, BirtReportCompareStatRow>> categoriesData = new HashMap<>();
        for (int i = 0; i < block.size(); i++) {
            Map<Integer, Integer> key = new HashMap<>();
            key.put(block.get(i).getTargetgroupindex(), block.get(i).getMailingId());
            if (!categoriesData.containsKey(key)) {
                BirtReportCompareStatRow row = new BirtReportCompareStatRow();
                row.setCategory(CommonKeys.TARGET_CATEGORY);
                row.setCategoryindex(CommonKeys.TARGET_CATEGORY_INDEX);
                row.setTargetgroup(block.get(i).getTargetgroup());
                row.setTargetgroupindex(block.get(i).getTargetgroupindex());
                row.setCount(block.get(i).getCount());
                row.setRate(block.get(i).getRate());
                row.setMailingId(block.get(i).getMailingId());
                row.setMailingName(block.get(i).getMailingName());
                row.setSendDate(block.get(i).getSendDate());
                row.setAssignedTargets(block.get(i).getAssignedTargets());
                row.setCategoryRowIndex(blockNumber);
                Map<Integer, BirtReportCompareStatRow> d = new HashMap<>();
                d.put(row.getCategoryindex(), row);
                categoriesData.put(key, d);
            }
            block.get(i).setCategoryRowIndex(blockNumber);
            categoriesData.get(key).put(block.get(i).getCategoryindex(), block.get(i));
        }
        return categoriesData;
    }

    private void addEmptyRow(int categoryIndex, String category, Map<Integer, BirtReportCompareStatRow> data, int templateCategoryIndex){
        if (!data.containsKey(categoryIndex)) {
            BirtReportCompareStatRow tmpRow = data.get(templateCategoryIndex);
            BirtReportCompareStatRow row = new BirtReportCompareStatRow();
            row.setCategory(category);
            row.setCategoryindex(categoryIndex);
            row.setTargetgroup(tmpRow.getTargetgroup());
            row.setTargetgroupindex(tmpRow.getTargetgroupindex());
            row.setCount(0);
            row.setRate(0);
            row.setMailingId(tmpRow.getMailingId());
            row.setMailingName(tmpRow.getMailingName());
            row.setSendDate(tmpRow.getSendDate());
            row.setAssignedTargets(tmpRow.getAssignedTargets());
            row.setCategoryRowIndex(tmpRow.getCategoryRowIndex());
            data.put(categoryIndex, row);
        }
    }

    public List<BirtReportCompareStatRow> getData(int tempTableID, String sortBy) throws Exception {
        List<BirtReportCompareStatRow> data = new ArrayList<>();
        List<Integer> allCategories = new ArrayList<>();
        allCategories.add(CommonKeys.TARGET_CATEGORY_INDEX);

        String query = "select * from tmp_report_aggregation_" + tempTableID + "_tbl " +
                "where category_index in (?) order by targetgroup_index";
        Map<Map<Integer, Integer>, Map<Integer, BirtReportCompareStatRow>> categoriesData;
        List<Integer> categories;
        //block1
        categories = new ArrayList<>(Arrays.asList(CommonKeys.DELIVERED_EMAILS_INDEX, CommonKeys.OPENERS_TOTAL_INDEX,
                CommonKeys.CLICKER_INDEX, CommonKeys.OPT_OUTS_INDEX, CommonKeys.HARD_BOUNCES_INDEX));
        allCategories.addAll(categories);
        categoriesData = getCategoriesData(query, categories, 1);
        for (Map.Entry<Map<Integer, Integer>, Map<Integer, BirtReportCompareStatRow>> entry : categoriesData.entrySet()) {
            Map<Integer, BirtReportCompareStatRow> blockData = entry.getValue();
            data.addAll(blockData.values());
        }
        //block2
        categories = new ArrayList<>(Arrays.asList(CommonKeys.OPENERS_TOTAL_INDEX, CommonKeys.OPENERS_MEASURED_INDEX,
                CommonKeys.OPENERS_INVISIBLE_INDEX));
        allCategories.addAll(categories);
        categoriesData = getCategoriesData(query, categories, 2);
        for (Map.Entry<Map<Integer, Integer>, Map<Integer, BirtReportCompareStatRow>> entry : categoriesData.entrySet()) {
            Map<Integer, BirtReportCompareStatRow> blockData = entry.getValue();
            BirtReportCompareStatRow totalRow = blockData.get(CommonKeys.OPENERS_TOTAL_INDEX);
            if ((totalRow != null) && (totalRow.getCount() > 0)) {
                totalRow.setRate(1.00);
                BirtReportCompareStatRow measuredRow = blockData.get(CommonKeys.OPENERS_MEASURED_INDEX);
                measuredRow.setRate(FormatTools.roundDecimal((double) measuredRow.getCount() / totalRow.getCount(), 2));
                BirtReportCompareStatRow invisibleRow = blockData.get(CommonKeys.OPENERS_INVISIBLE_INDEX);
                invisibleRow.setRate(1.00 - measuredRow.getRate());
            }
            data.addAll(blockData.values());
        }
        //block3
        categories = new ArrayList<>(Arrays.asList(CommonKeys.OPENERS_MEASURED_INDEX, CommonKeys.OPENERS_PC_INDEX,
                CommonKeys.OPENERS_MOBILE_INDEX, CommonKeys.OPENERS_TABLET_INDEX, CommonKeys.OPENERS_SMARTTV_INDEX, CommonKeys.OPENERS_PC_AND_MOBILE_INDEX));
        allCategories.addAll(categories);
        categoriesData = getCategoriesData(query, categories, 3);
        for (Map.Entry<Map<Integer, Integer>, Map<Integer, BirtReportCompareStatRow>> entry : categoriesData.entrySet()) {
            Map<Integer, BirtReportCompareStatRow> blockData = entry.getValue();
            BirtReportCompareStatRow omeasuredRow = blockData.get(CommonKeys.OPENERS_MEASURED_INDEX);
            if ((omeasuredRow != null) && (omeasuredRow.getCount() > 0)) {
                omeasuredRow.setRate(1.00);
                BirtReportCompareStatRow opcRow = blockData.get(CommonKeys.OPENERS_PC_INDEX);
                opcRow.setRate(FormatTools.roundDecimal((double) opcRow.getCount() / omeasuredRow.getCount(), 2));
                BirtReportCompareStatRow omobileRow = blockData.get(CommonKeys.OPENERS_MOBILE_INDEX);
                omobileRow.setRate(FormatTools.roundDecimal((double) omobileRow.getCount() / omeasuredRow.getCount(), 2));
                BirtReportCompareStatRow otabletRow = blockData.get(CommonKeys.OPENERS_TABLET_INDEX);
                otabletRow.setRate(FormatTools.roundDecimal((double) otabletRow.getCount() / omeasuredRow.getCount(), 2));
                BirtReportCompareStatRow osmarttvRow = blockData.get(CommonKeys.OPENERS_SMARTTV_INDEX);
                osmarttvRow.setRate(FormatTools.roundDecimal((double) osmarttvRow.getCount() / omeasuredRow.getCount(), 2));
                BirtReportCompareStatRow ootherRow = blockData.get(CommonKeys.OPENERS_PC_AND_MOBILE_INDEX);
                ootherRow.setRate(1 - opcRow.getRate() - omobileRow.getRate() - otabletRow.getRate() - osmarttvRow.getRate());
            }
            data.addAll(blockData.values());
        }
        //block4
        categories = new ArrayList<>(Arrays.asList(CommonKeys.CLICKER_TRACKED_INDEX, CommonKeys.CLICKER_PC_INDEX,
                CommonKeys.CLICKER_MOBILE_INDEX, CommonKeys.CLICKER_TABLET_INDEX, CommonKeys.CLICKER_SMARTTV_INDEX, CommonKeys.CLICKER_PC_AND_MOBILE_INDEX));
        allCategories.addAll(categories);
        categoriesData = getCategoriesData(query, categories, 4);
        for (Map.Entry<Map<Integer, Integer>, Map<Integer, BirtReportCompareStatRow>> entry : categoriesData.entrySet()) {
            Map<Integer, BirtReportCompareStatRow> blockData = entry.getValue();
            BirtReportCompareStatRow ctrackedRow = blockData.get(CommonKeys.CLICKER_TRACKED_INDEX);
            if ((ctrackedRow != null) && (ctrackedRow.getCount() > 0)){
                ctrackedRow.setRate(1.00);
                BirtReportCompareStatRow cpcRow = blockData.get(CommonKeys.CLICKER_PC_INDEX);
                cpcRow.setRate(FormatTools.roundDecimal((double) cpcRow.getCount() / ctrackedRow.getCount(), 2));
                BirtReportCompareStatRow cmobileRow = blockData.get(CommonKeys.CLICKER_MOBILE_INDEX);
                cmobileRow.setRate(FormatTools.roundDecimal((double) cmobileRow.getCount() / ctrackedRow.getCount(), 2));
                BirtReportCompareStatRow ctabletRow = blockData.get(CommonKeys.CLICKER_TABLET_INDEX);
                ctabletRow.setRate(FormatTools.roundDecimal((double) ctabletRow.getCount() / ctrackedRow.getCount(), 2));
                BirtReportCompareStatRow csmarttvRow = blockData.get(CommonKeys.CLICKER_SMARTTV_INDEX);
                csmarttvRow.setRate(FormatTools.roundDecimal((double) csmarttvRow.getCount() / ctrackedRow.getCount(), 2));
                BirtReportCompareStatRow cotherRow = blockData.get(CommonKeys.CLICKER_PC_AND_MOBILE_INDEX);
                cotherRow.setRate(1 - cpcRow.getRate() - cmobileRow.getRate() - ctabletRow.getRate() - csmarttvRow.getRate());
            }
            data.addAll(blockData.values());
        }
        //block5
        categories = new ArrayList<>(Arrays.asList(CommonKeys.SENT_HTML_INDEX, CommonKeys.SENT_TEXT_INDEX, CommonKeys.SENT_OFFLINE_HTML_INDEX));
        allCategories.addAll(categories);
        categoriesData = getCategoriesData(query, categories, 5);
        for (Map.Entry<Map<Integer, Integer>, Map<Integer, BirtReportCompareStatRow>> entry : categoriesData.entrySet()) {
            Map<Integer, BirtReportCompareStatRow> blockData = entry.getValue();
            addEmptyRow(CommonKeys.SENT_TEXT_INDEX, CommonKeys.SENT_TEXT, blockData, CommonKeys.SENT_HTML_INDEX);
            addEmptyRow(CommonKeys.SENT_OFFLINE_HTML_INDEX, CommonKeys.SENT_OFFILE_HTML, blockData, CommonKeys.SENT_HTML_INDEX);
            BirtReportCompareStatRow htmlRow = blockData.get(CommonKeys.SENT_HTML_INDEX);
            BirtReportCompareStatRow textRow = blockData.get(CommonKeys.SENT_TEXT_INDEX);
            BirtReportCompareStatRow offlineRow = blockData.get(CommonKeys.SENT_OFFLINE_HTML_INDEX);
            int typeTotalValue = htmlRow.getCount() + textRow.getCount() + offlineRow.getCount();
            htmlRow.setRate(FormatTools.roundDecimal((double) htmlRow.getCount() / typeTotalValue, 2));
            textRow.setRate(FormatTools.roundDecimal((double) textRow.getCount() / typeTotalValue, 2));
            offlineRow.setRate(FormatTools.roundDecimal((double) offlineRow.getCount() / typeTotalValue, 2));
            data.addAll(blockData.values());
        }

        if (sortBy.equals("date")) {
            Collections.sort(data, (o1, o2) -> DateUtilities.compare(o1.getSendDate(), o2.getSendDate()));
        } else {
            Collections.sort(data, (o1, o2) -> o1.getMailingName().compareToIgnoreCase(o2.getMailingName()));
        }

        int rowNum = 0;
        Set<Integer> usedMailingId = new HashSet<>();
        for (BirtReportCompareStatRow row : data) {
            if (!usedMailingId.contains(row.getMailingId())) {
                usedMailingId.add(row.getMailingId());
                rowNum++;
            }
            
            row.setOrderRule(allCategories.indexOf(row.getCategoryindex()));

            row.setRate(BigDecimal.valueOf(100 * row.getRate()).setScale(2, RoundingMode.DOWN).doubleValue());
            row.setRowNum(rowNum);
        }

        return data;
    }

    public String getPredefineMailingName(int mailingFilter, int predefineMailingId, @VelocityCheck int companyId) {
        String sql = "";
        if (FilterType.FILTER_ARCHIVE.getKey() == mailingFilter) {
            sql = "select shortname from campaign_tbl where campaign_id = ? and company_id = ?";
        } else if (FilterType.FILTER_MAILINGLIST.getKey() == mailingFilter) {
            sql = "select shortname from mailinglist_tbl where mailinglist_id = ? and company_id = ?";
        } else if (FilterType.FILTER_TARGET.getKey() == mailingFilter) {
            sql = "SELECT target_shortname FROM dyn_target_tbl WHERE target_id = ? AND company_id = ?";
        } else {
            return "";
        }
        return select(logger, sql, String.class, predefineMailingId, companyId);
    }

}
