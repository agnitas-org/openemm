/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.reporting.birt.external.dataset;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.agnitas.reporting.birt.external.beans.LightTarget;
import com.agnitas.reporting.birt.external.beans.MailingClickStatsPerTargetRow;
import com.agnitas.reporting.birt.external.beans.MailingClickStatsPerTargetWithMailingIdRow;
import com.agnitas.reporting.birt.external.beans.SendStatRow;
import com.agnitas.reporting.birt.external.beans.SendStatWithMailingIdRow;
import com.agnitas.reporting.birt.external.utils.BirtReporUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MailingStatisticDataSet extends BIRTDataSet {

	private static final Logger logger = LogManager.getLogger(MailingStatisticDataSet.class);

    private final MailingSummaryDataSet mailingSummaryDataSet = new MailingSummaryDataSet();
    private final MailingURLClicksDataSet mailingURLClicksDataSet = new MailingURLClicksDataSet();
    private final MailingDataSet mailingDataSet = new MailingDataSet();

    public DateFormats getDateFormatsInstance(String startDate, String stopDate, Boolean hourScale) {
        return new DateFormats(startDate, stopDate, hourScale);
    }

    private int prepareSummaryReport(int mailingId, int companyId, String targetsStr, String hiddenTargetIdStr,
                                     String recipientType, List<BirtReporUtils.BirtReportFigure> figures, DateFormats dateFormats) throws Exception {
        try {
			int tempTableId = mailingSummaryDataSet.createTempTable();
			List<LightTarget> targets = getTargets(targetsStr, companyId);
            String hiddenTargetSql = getTargetSqlString(hiddenTargetIdStr, companyId);
			
			// @todo: what should we do with options html/text/offlineHtml ?

			boolean mailingTrackingAvailable = isTrackingExists(mailingId, companyId);

			if (dateFormats == null) {
			    dateFormats = new DateFormats();
			}
			mailingSummaryDataSet.insertSendIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, dateFormats);
			mailingSummaryDataSet.insertRecipientsNumberToTemplate(mailingDataSet, mailingId, tempTableId, dateFormats);

			if (figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_MEASURED)
			    || figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_AFTER_DEVICE)) {
			    mailingSummaryDataSet.insertOpenersIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, true, false, dateFormats);
			}
			if (figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_INVISIBLE)
                    || figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_TOTAL)) {
			    mailingSummaryDataSet.insertOpenedInvisibleIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, dateFormats);
			}

			if (figures.contains(BirtReporUtils.BirtReportFigure.OPENINGS_ANONYMOUS)) {
			    mailingSummaryDataSet.insertOpenedAnonymousIntoTempTable(mailingId, tempTableId, companyId, dateFormats);
			}
			if (figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL)
			    || figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_AFTER_DEVICE)) {
			    mailingSummaryDataSet.insertClickersIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, true, dateFormats);
			}
			if (figures.contains(BirtReporUtils.BirtReportFigure.CLICKS_ANONYMOUS)) {
			    mailingSummaryDataSet.insertClicksAnonymousIntoTempTable(mailingId, tempTableId, companyId, dateFormats);
			}
			if (figures.contains(BirtReporUtils.BirtReportFigure.SIGNED_OFF)) {
			    mailingSummaryDataSet.insertOptOutsIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, dateFormats);
			}
			if (figures.contains(BirtReporUtils.BirtReportFigure.HARDBOUNCES) || figures.contains(BirtReporUtils.BirtReportFigure.SOFTBOUNCES)) {
			    mailingSummaryDataSet.insertBouncesIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, recipientType, true, dateFormats);
			}

			mailingSummaryDataSet.insertDeliveredIntoTempTable(tempTableId, mailingId, companyId, targets, hiddenTargetSql, recipientType, dateFormats);

			if (figures.contains(BirtReporUtils.BirtReportFigure.REVENUE)) {
			    mailingSummaryDataSet.insertRevenueIntoTempTable(mailingId, tempTableId, companyId, targets, hiddenTargetSql, dateFormats);
			}

			// now we need to remove mobile/tablet/multiple_device data if it is not selected by user
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

			if (!figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_MEASURED)) {
			    mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_MEASURED_INDEX);
			}
			if (!figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_INVISIBLE)) {
			    mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_INVISIBLE_INDEX);
			}
			if (!figures.contains(BirtReporUtils.BirtReportFigure.OPENERS_TOTAL)) {
			    mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.OPENERS_TOTAL_INDEX);
			}
			if (!figures.contains(BirtReporUtils.BirtReportFigure.CLICKERS_TOTAL)) {
			    mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.CLICKER_INDEX);
			}

			// now we need to remove hardbounces/softbounces data if it is not selected by user
			if (!figures.contains(BirtReporUtils.BirtReportFigure.HARDBOUNCES)) {
			    mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.HARD_BOUNCES_INDEX);
			}
			if (!figures.contains(BirtReporUtils.BirtReportFigure.SOFTBOUNCES) || !mailingTrackingAvailable) {
			    mailingSummaryDataSet.removeCategoryData(tempTableId, CommonKeys.SOFT_BOUNCES_INDEX);
			}

			mailingSummaryDataSet.insertMailformatsIntoTempTable(tempTableId, mailingId, companyId, figures, dateFormats.getStartDate(), dateFormats.getStopDate());

			mailingSummaryDataSet.updateRates(tempTableId, companyId, targets);

			return tempTableId;
		} catch (Throwable e) {
			logger.error("Error while creating mailing summary for " + companyId + "/" + mailingId, e);
			throw e;
		}
    }

    public List<SendStatWithMailingIdRow> getSummaryData(int companyID, String mailings, String targetGroupIds, String figuresOptions) throws Exception {
        return getSummaryData(companyID, mailings, targetGroupIds, null, figuresOptions, null);
    }

    public List<SendStatWithMailingIdRow> getSummaryData(int companyID, String mailings, String targetGroupIds, String figuresOptions, DateFormats dateFormats) throws Exception {
        return getSummaryData(companyID, mailings, targetGroupIds, null, figuresOptions, dateFormats);
    }

    public List<SendStatWithMailingIdRow> getSummaryData(int companyID, String mailings, String targetGroupIds, String hiddenTargetGroup, String figuresOptions, DateFormats dateFormats) throws Exception {
        List<SendStatWithMailingIdRow> resultList = new LinkedList<>();
        List<Integer> mailingIds = parseCommaSeparatedIds(mailings);
        List<BirtReporUtils.BirtReportFigure> figures = BirtReporUtils.unpackFigures(figuresOptions);

        for (Integer mailingId : mailingIds) {
            int tempTableID = prepareSummaryReport(mailingId, companyID, targetGroupIds, hiddenTargetGroup, CommonKeys.TYPE_ALL_SUBSCRIBERS, figures, dateFormats);
            List<MailingSummaryDataSet.MailingSummaryRow> summaryData = mailingSummaryDataSet.getSummaryData(tempTableID);

            for (SendStatRow row : summaryData) { 
                if (row.getCategoryindex() == CommonKeys.REVENUE_INDEX) {
                    //cheat for possibility sort data by category index
                    //and this fix will let to place revenue to the bottom of table
                    row.setCategoryindex(CommonKeys.REVENUE_SHIFTED_INDEX);
                }
                resultList.add(new SendStatWithMailingIdRow(row, mailingId));
            }
        }
        return resultList;
    }

    public List<MailingClickStatsPerTargetWithMailingIdRow>  getLinkClicksData(int companyID, String mailings,
                                                                               String targetGroupIds, String hiddenTargetId,
                                                                               DateFormats dateFormats) throws Exception {
        List<MailingClickStatsPerTargetWithMailingIdRow> resultList = new LinkedList<>();
        List<Integer> mailingIds = parseCommaSeparatedIds(mailings);

        List<LightTarget> targets = getTargets(targetGroupIds, companyID);
        Map<Integer, LinkGeneralInfo> links = new HashMap<>();
        Map<Integer, Integer> urlIdToItemLinkNumber = new HashMap<>();

        for (Integer mailingId : mailingIds) {
            int itemLinkNumber = 0;
            int tempTableID = mailingURLClicksDataSet.prepareReport(mailingId, companyID, targetGroupIds, hiddenTargetId, CommonKeys.TYPE_ALL_SUBSCRIBERS, dateFormats);
            List<MailingClickStatsPerTargetWithMailingIdRow> resultListLeadingLinks = new LinkedList<>();
            List<MailingClickStatsPerTargetWithMailingIdRow> resultListAdministrativeLinks = new LinkedList<>();
            List<MailingClickStatsPerTargetRow> urlClicksData = mailingURLClicksDataSet.getUrlClicksData(tempTableID);
            for (MailingClickStatsPerTargetRow row : urlClicksData) {
                if (!links.containsKey(row.getUrlId())) {
                    LinkGeneralInfo linkGeneralInfo = new LinkGeneralInfo();
                    links.put(row.getUrlId(), linkGeneralInfo);

                    linkGeneralInfo.setMailingId(mailingId);
                    linkGeneralInfo.setUrl(row.getUrl());
                    linkGeneralInfo.setUrlId(row.getUrlId());
                    linkGeneralInfo.setAdminLink(row.isAdminLink());
                    if (row.isAdminLink()) {
                        linkGeneralInfo.setLinkItemNumber(0);
                        urlIdToItemLinkNumber.put(row.getUrlId(), 0);
                    } else {
                        itemLinkNumber++;
                        linkGeneralInfo.setLinkItemNumber(itemLinkNumber);
                        urlIdToItemLinkNumber.put(row.getUrlId(), itemLinkNumber);
                    }
                }
                if (!row.isMobile()) {
                    if (row.isAdminLink()) {
                        resultListAdministrativeLinks.add(new MailingClickStatsPerTargetWithMailingIdRow(row, mailingId, 0));
                    } else {
                        resultListLeadingLinks.add(new MailingClickStatsPerTargetWithMailingIdRow(row, mailingId, urlIdToItemLinkNumber.get(row.getUrlId())));
                    }
                    links.get(row.getUrlId()).getTargetGroups().add(row.getTargetgroup());
                }
            }

            resultList.addAll(resultListLeadingLinks);

            for (MailingClickStatsPerTargetWithMailingIdRow administrativeLink : resultListAdministrativeLinks) {
                if (links.get(administrativeLink.getUrlId()).getLinkItemNumber() == 0) {
                    itemLinkNumber++;
                    links.get(administrativeLink.getUrlId()).setLinkItemNumber(itemLinkNumber);
                    urlIdToItemLinkNumber.put(administrativeLink.getUrlId(), itemLinkNumber);
                }
                administrativeLink.setLinkItemNumber(urlIdToItemLinkNumber.get(administrativeLink.getUrlId()));
            }
            resultList.addAll(resultListAdministrativeLinks);
        }

        for (MailingStatisticDataSet.LinkGeneralInfo entry : links.values()) {
            // +1 target group for all subscribers
            if (CollectionUtils.isNotEmpty(targets) && entry.getTargetGroups().size() < (targets.size() + 1)) {
                for (LightTarget target : targets) {
                    if (!entry.getTargetGroups().contains(target.getName())) {
                        MailingClickStatsPerTargetRow missedRow = new MailingClickStatsPerTargetRow();
                        missedRow.setTargetgroup(target.getName());
                        missedRow.setUrlId(entry.getUrlId());
                        missedRow.setUrl(entry.getUrl());
                        missedRow.setAdminLink(entry.isAdminLink());
                        missedRow.setColumnIndex(targets.indexOf(target) + 2);
                        resultList.add(new MailingClickStatsPerTargetWithMailingIdRow(missedRow, entry.getMailingId(), entry.getLinkItemNumber()));
                    }
                }
            }
        }

        // we need to count rates in a way that it show the percent according to that all clicks of all recipients is 100%
        // count the totals (gross and net)
        for (int mailingId : mailingIds) {
            int totalGross = 0;
            int totalNet = 0;
            for (MailingClickStatsPerTargetWithMailingIdRow row : resultList) {
                if (row.getMailingId() == mailingId && row.getColumnIndex() == CommonKeys.ALL_SUBSCRIBERS_INDEX) {
                    totalGross += row.getClicksGross();
                    totalNet += row.getClicksNet();
                }
            }
            // count the rates
            for (MailingClickStatsPerTargetWithMailingIdRow row : resultList) {
                if (row.getMailingId() == mailingId) {
                    if (totalGross != 0) {
                        row.setClicksGrossPercent(((float) row.getClicksGross()) / ((float) totalGross));
                    } else {
                        row.setClicksGrossPercent(0.0f);
                    }
                    if (totalNet != 0) {
                        row.setClicksNetPercent(((float) row.getClicksNet()) / ((float) totalNet));
                    } else {
                        row.setClicksNetPercent(0.0f);
                    }
                }
            }
        }

        return resultList;
    }

    public List<LightTarget> getTargets(int companyID, String targetGroupIds) {
        return getTargets(targetGroupIds, companyID);
    }

    private static class LinkGeneralInfo {
        private int linkItemNumber;
        private int mailingId;
        private int urlId;
        private String url;
        private boolean adminLink;
        private Set<String> targetGroups = new HashSet<>();

        public int getLinkItemNumber() {
            return linkItemNumber;
        }

        public void setLinkItemNumber(int linkItemNumber) {
            this.linkItemNumber = linkItemNumber;
        }

        public int getMailingId() {
            return mailingId;
        }

        public void setMailingId(int mailingId) {
            this.mailingId = mailingId;
        }

        public int getUrlId() {
            return urlId;
        }

        public void setUrlId(int urlId) {
            this.urlId = urlId;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public boolean isAdminLink() {
            return adminLink;
        }

        public void setAdminLink(boolean adminLink) {
            this.adminLink = adminLink;
        }

        public Set<String> getTargetGroups() {
            return targetGroups;
        }

        /**
         * Used by JSP
         */
        @SuppressWarnings("unused")
        public void setTargetGroups(Set<String> targetGroups) {
            this.targetGroups = targetGroups;
        }
    }
}
