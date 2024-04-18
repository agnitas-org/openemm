/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.agnitas.beans.MailingBase;
import org.agnitas.beans.impl.MailingBaseImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.agnitas.reporting.birt.external.beans.CompareStatCsvRow;
import com.agnitas.reporting.birt.external.beans.CompareStatRow;

public class MailingCompareDataSet extends ComparisonBirtDataSet  {

	private static final Logger logger = LogManager.getLogger(MailingCompareDataSet.class);

    public static final int TARGET_NAME_LENGTH_MAX = 28;
    private final MailingSummaryDataSet mailingSummaryDataSet = new MailingSummaryDataSet();

    /**
     * Collects summary data for provided mailings and stores it into temp tables.
     * 
     * @param mailingIdsCsv csv ids of the mailings to collect data
     * @param companyId id of the company
     * @return mailingId -> tempTableId map
     */
    public Map<Integer, Integer> prepareReport(String mailingIdsCsv, int companyId) {
        if (!StringUtils.containsOnly(mailingIdsCsv, "1234567890,")) {
            logger.error("Wrong format of mailing-IDs string");
            return Collections.emptyMap();
        }
        List<Integer> mailingIds = Arrays.stream(StringUtils.split(mailingIdsCsv, ","))
                .map(NumberUtils::toInt)
                .filter(v -> v != 0)
                .collect(Collectors.toList());
        return mailingIds.stream().collect(Collectors.toMap(
                Function.identity(),
                mailingId -> tryCollectMailingData(companyId, mailingId)));
    }

    private int tryCollectMailingData(int companyId, Integer mailingId) {
        try {
            return mailingSummaryDataSet
                    .prepareReport(mailingId, companyId, "", CommonKeys.TYPE_ALL_SUBSCRIBERS, true, "", "", false);
        } catch (Exception e) {
            logger.error("Error while collecting mailing data for comparison. Mailing id = {}", mailingId, e);
            return 0;
        }
    }
    
    public int getTargetNum(String targetsStr) {
        if (targetsStr != null && !targetsStr.isEmpty()) {
            return targetsStr.split(",").length;
        }

        return 0;
    }
    
    public List<CompareStatRow> getData(Map<Integer, Integer> tempTables) throws Exception {
        List<CompareStatRow> result = new LinkedList<>();

        for (Entry<Integer, Integer> entry : tempTables.entrySet()) {
            List<MailingSummaryDataSet.MailingSummaryRow> summaryData = mailingSummaryDataSet.getSummaryData(entry.getValue());
            summaryData.forEach(summaryStatRow -> {
                CompareStatRow compareStatRow = summaryRowToCompareRow(summaryStatRow);
                MailingBase mailing = getMailingsBaseInfo(String.valueOf(entry.getKey())).get(0);
                compareStatRow.setMailingId(mailing.getId());
                compareStatRow.setMailingName(mailing.getShortname());
                compareStatRow.setSendDate(mailing.getSenddate());
                result.add(compareStatRow);
            });
        }
        return result;
    }
    
    private CompareStatRow summaryRowToCompareRow(MailingSummaryDataSet.MailingSummaryRow summaryStatRow) {
        CompareStatRow compareStatRow = new CompareStatRow();
        compareStatRow.setCategory(summaryStatRow.getCategory());
        compareStatRow.setTargetGroupName(summaryStatRow.getTargetgroup());
        compareStatRow.setTargetShortName(createTargetNameShort(compareStatRow.getTargetGroupName()));
        compareStatRow.setCategoryindex(summaryStatRow.getCategoryindex());
        compareStatRow.setTargetGroupIndex(summaryStatRow.getTargetgroupindex());
        compareStatRow.setCount(summaryStatRow.getCount());
        compareStatRow.setRate(summaryStatRow.getRate());
        return compareStatRow;
    }

    public List<CompareStatCsvRow> getCsvSummaryData(Map<Integer, Integer> tempTables) throws Exception {
        Map<String, CompareStatCsvRow> summaryCsvData = new HashMap<>();
        List<CompareStatRow> summaryData = getData(tempTables);
        for (CompareStatRow compareStat : summaryData) {
            int mailingId = compareStat.getMailingId();
            int groupId = compareStat.getTargetGroupId();
            String key = mailingId + "_" + groupId;
            CompareStatCsvRow statCsvRow = summaryCsvData.get(key);
            if (statCsvRow == null) {
                statCsvRow = new CompareStatCsvRow(mailingId, groupId, compareStat.getTargetGroupIndex());
                statCsvRow.setMailingNameFull(compareStat.getMailingName());
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
                            case CommonKeys.HARD_BOUNCES_INDEX:
                                statCsvRow.setBouncesCount(compareStatInner.getCount());
                                statCsvRow.setBouncesRate(compareStatInner.getRate());
                                break;
                            case CommonKeys.REVENUE_INDEX:
                                statCsvRow.setRevenueCount(compareStatInner.getCount() / 100);
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

    public static String createTargetNameShort(String name) {
        String shortname = name;
        if (name != null && name.length() > TARGET_NAME_LENGTH_MAX) {
            shortname = name.substring(0, TARGET_NAME_LENGTH_MAX - 3) + "...";
        } else if (name == null) {
            shortname = "";
        }
        return shortname;
    }

    public List<MailingBase> getMailingsBaseInfo(String mailingIdsStr) {
        List<MailingBase> mailings = select(logger,
                "SELECT a.mailing_id, a.shortname, a.description, MIN(c.mintime) send_date " +
                        "FROM mailing_tbl a " +
                        "   LEFT JOIN mailing_account_sum_tbl c ON (a.mailing_id = c.mailing_id) " +
                        "WHERE a.mailing_id IN (" + mailingIdsStr + ") " +
                        "GROUP BY a.mailing_id, a.shortname, a.description ORDER BY a.mailing_id",
                (rs, i) -> {
                    MailingBase mailing = new MailingBaseImpl();
                    mailing.setId(rs.getInt("mailing_id"));
                    mailing.setShortname(rs.getString("shortname"));
                    mailing.setDescription(rs.getString("description"));
                    mailing.setSenddate(rs.getDate("send_date"));
                    return mailing;
                });
        mailings.sort(Comparator.comparing(MailingBase::getSenddate).reversed());
        return mailings;
    }

    // called mailing_compare.rptdesign afterRender()
    public void dropTempTables(Map<Integer, Integer> tempTables) throws Exception {
        for (Integer tableId : tempTables.values()) {
            dropTempTable(tableId);
        }
    }
}
