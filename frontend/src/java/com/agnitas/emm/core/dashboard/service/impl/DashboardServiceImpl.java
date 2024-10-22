/*

    Copyright (C) 2022 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.service.impl;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import com.agnitas.beans.Admin;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import com.agnitas.emm.core.dashboard.bean.DashboardWorkflow;
import com.agnitas.emm.core.dashboard.bean.ScheduledMailing;
import com.agnitas.emm.core.dashboard.service.DashboardService;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.workflow.service.ComWorkflowService;
import com.agnitas.messages.I18nString;
import com.agnitas.reporting.birt.external.beans.SendStatRow;
import com.agnitas.reporting.birt.external.beans.factory.MailingSummaryDataSetFactory;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.dataset.MailingSummaryDataSet;
import net.sf.json.JSONObject;
import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.dao.MailingStatus;
import org.agnitas.util.DateUtilities;
import org.agnitas.util.SafeString;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

public class DashboardServiceImpl implements DashboardService {
	
    private static final Logger logger = LogManager.getLogger(DashboardServiceImpl.class);
    
    private static final int DEFAULT_STATISTIC_VALUE = 0;

    private MailingSummaryDataSetFactory mailingSummaryDataSetFactory;
    private MailingDao mailingDao;
    private ComWorkflowService workflowService;
    private RecipientsReportService recipientsReportService;

    private static final List<Integer> OPENERS_STATISTIC_INDEXES = new ArrayList<>(Arrays.asList(
                CommonKeys.OPENERS_PC_INDEX, CommonKeys.OPENERS_MOBILE_INDEX, CommonKeys.OPENERS_TABLET_INDEX,
                CommonKeys.OPENERS_SMARTTV_INDEX, CommonKeys.OPENERS_PC_AND_MOBILE_INDEX));
    
    private static final List<Integer> CLICKERS_STATISTIC_INDEXES = new ArrayList<>(Arrays.asList(
                CommonKeys.CLICKER_PC_INDEX, CommonKeys.CLICKER_MOBILE_INDEX, CommonKeys.CLICKER_TABLET_INDEX,
                CommonKeys.CLICKER_SMARTTV_INDEX, CommonKeys.CLICKER_PC_AND_MOBILE_INDEX));

    @Required
    public void setMailingDao(MailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setMailingSummaryDataSetFactory(MailingSummaryDataSetFactory mailingSummaryDataSetFactory) {
        this.mailingSummaryDataSetFactory = mailingSummaryDataSetFactory;
    }

    @Required
    public void setWorkflowService(ComWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    @Required
    public void setRecipientsReportService(RecipientsReportService recipientsReportService) {
        this.recipientsReportService = recipientsReportService;
    }

    public MailingDao getMailingDao() {
        return mailingDao;
    }

    @Override
    public PaginatedListImpl<Map<String, Object>> getMailings(Admin admin, String sort, String direction, int rownums) {
        return mailingDao.getDashboardMailingList(admin, sort, direction, rownums);
    }

    @Override
    public List<Map<String, Object>> getLastSentWorldMailings(Admin admin, int rownums) {
        return mailingDao.getLastSentWorldMailings(admin, rownums);
    }

    @Override
    public List<ScheduledMailing> getScheduledMailings(Admin admin, Date startDate, Date endDate) {
        endDate = DateUtilities.addDaysToDate(endDate, 1);

        List<ScheduledMailing> mailings = mailingDao.getScheduledMailings(admin, startDate, endDate);

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", admin.getLocale());
        SimpleDateFormat dateFormat = admin.getDateFormat();

        mailings.forEach(m -> {
            m.setWorkstatusIn(I18nString.getLocaleString(m.getWorkstatus(), admin.getLocale()));
            m.setSendDate(DateUtilities.format(m.getMaildropSendDate(), dateFormat));
            m.setSendTime(DateUtilities.format(m.getMaildropSendDate(), timeFormat));
        });

        return mailings;
    }

    @Override
    public List<DashboardWorkflow> getWorkflows(Admin admin) {
        return workflowService.getWorkflowsForDashboard(admin);
    }

    @Override
    public List<DashboardRecipientReport> getRecipientReports(Admin admin) {
        return recipientsReportService.getReportsForDashboard(admin.getCompanyID());
    }

    @Override
    public JSONObject getStatisticsInfo(int mailingId, Locale locale, Admin admin) throws Exception {
        JSONObject statData = new JSONObject();

        Map<Integer, Integer> data = getReportData(mailingId, admin.getCompanyID());

        int clickerTracked = data.getOrDefault(CommonKeys.CLICKER_TRACKED_INDEX, DEFAULT_STATISTIC_VALUE);
        int openersTracked = data.getOrDefault(CommonKeys.OPENERS_TRACKED_INDEX, DEFAULT_STATISTIC_VALUE);
        int deliveredEmails = data.getOrDefault(CommonKeys.DELIVERED_EMAILS_INDEX, DEFAULT_STATISTIC_VALUE);

        double clickerPercent = 0.0;
        double openersPercent = 0.0;

        if (openersTracked != 0 && deliveredEmails != 0) {
            clickerPercent = (double) clickerTracked / openersTracked;
            openersPercent = (double) openersTracked / deliveredEmails;
        }

        statData.put("common", getCommonStat(data, locale));
        statData.put("clickers", getClickersStat(data, locale));
        statData.put("openers", getOpenersStat(data, locale));
        if (!admin.isRedesignedUiUsed()) {
            statData.put("clickersPercent", Collections.singletonList(clickerPercent));
        }
        statData.put("openersPercent", Collections.singletonList(openersPercent));

        return statData;
    }

    private Map<Integer, Integer> getReportData(int mailingId, int companyId) throws Exception {
        MailingSummaryDataSet mailingSummaryDataSet = mailingSummaryDataSetFactory.create();
        Map<Integer, Integer> data = new HashMap<>();

        if (mailingId != 0) {
            int tempTableID = mailingSummaryDataSet.prepareDashboardForCharts(mailingId, companyId);
            List<? extends SendStatRow> rowList = mailingSummaryDataSet.getSummaryData(tempTableID);

            for (SendStatRow row : rowList) {
                // when the value in summary data set is "-1"
                // it's mean that the requested data is not exists in db, so we should set "0"
                int value = Math.max(0, row.getCount());
                data.put(row.getCategoryindex(), value);
            }
        } else {
            logger.error("Parameter mailingId is missed");
        }

        return data;
    }

    private List<String[]> getCommonStat(Map<Integer, Integer> data, Locale locale) {
        List<Integer> dataKeys = new ArrayList<>(data.keySet());
        List<String[]> commonStat = new ArrayList<>();

        Collections.sort(dataKeys);

        for (Integer categoryId : dataKeys) {
            String messageKey;

            switch (categoryId) {
                case CommonKeys.DELIVERED_EMAILS_INDEX:
                    messageKey = MailingStatus.SENT.getMessageKey();
                    break;
                case CommonKeys.OPENERS_INDEX:
                    messageKey = "statistic.opener";
                    break;
                case CommonKeys.CLICKER_INDEX:
                    messageKey = "statistic.clicker";
                    break;
                case CommonKeys.OPT_OUTS_INDEX:
                    messageKey = "statistic.Opt_Outs";
                    break;
                case CommonKeys.HARD_BOUNCES_INDEX:
                    messageKey = "statistic.bounces.hardbounce";
                    break;
                default:
                    continue;
            }

            String[] items = new String[]{
                    SafeString.getLocaleString(messageKey, locale),
                    Double.toString(data.get(categoryId))
            };

            commonStat.add(items);
        }

        return commonStat;
    }

    private List<String[]> getClickersStat(Map<Integer, Integer> data, Locale locale) {
        List<String[]> clickersStat = new ArrayList<>();

        for (int categoryId : CLICKERS_STATISTIC_INDEXES) {
            String messageKey;
            Integer categoryVal = data.get(categoryId);

            if (Objects.isNull(categoryVal)) {
                continue;
            }
            
            messageKey = getDeviceMessageKey(categoryId);

            int clickerTracked = data.getOrDefault(CommonKeys.CLICKER_TRACKED_INDEX, DEFAULT_STATISTIC_VALUE);
            String[] items = getItems(messageKey, locale, clickerTracked, categoryVal);

            clickersStat.add(items);
        }

        return clickersStat;
    }
    
    
    private List<String[]> getOpenersStat(Map<Integer, Integer> data, Locale locale) {
        List<String[]> openersStat = new ArrayList<>();

        for (Integer categoryId : OPENERS_STATISTIC_INDEXES) {
            String messageKey;
            Integer categoryVal = data.get(categoryId);

            if (Objects.isNull(categoryVal)) {
                continue;
            }

            messageKey = getDeviceMessageKey(categoryId);

            int openersTracked = data.getOrDefault(CommonKeys.OPENERS_TRACKED_INDEX, DEFAULT_STATISTIC_VALUE);
            String[] items = getItems(messageKey, locale, openersTracked, categoryVal);

            openersStat.add(items);
        }

        return openersStat;
    }

    private String[] getItems(String messageKey, Locale locale, int divider, int divided) {
        return new String[]{
                SafeString.getLocaleString(messageKey, locale),
                divider == 0 ? Double.toString(0.0) : Double.toString((double) divided / divider)
        };
    }
    
    private String getDeviceMessageKey(int categoryId) {
        switch (categoryId) {
            case CommonKeys.CLICKER_PC_INDEX:
            case CommonKeys.OPENERS_PC_INDEX:
                return "predelivery.desktop";
            case CommonKeys.CLICKER_TABLET_INDEX:
            case CommonKeys.OPENERS_TABLET_INDEX:
                return "report.openers.tablet.shortname";
            case CommonKeys.CLICKER_MOBILE_INDEX:
            case CommonKeys.OPENERS_MOBILE_INDEX:
                return "report.openers.mobile.shortname";
            case CommonKeys.CLICKER_SMARTTV_INDEX:
            case CommonKeys.OPENERS_SMARTTV_INDEX:
                return "report.openers.smarttv.shortname";
            case CommonKeys.CLICKER_PC_AND_MOBILE_INDEX:
            case CommonKeys.OPENERS_PC_AND_MOBILE_INDEX:
                return "report.openers.multiple-devices.shortname";
            default:
                return "predelivery.desktop";
        }
    }
    
}
