/*

    Copyright (C) 2019 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/>.

*/

package com.agnitas.emm.core.dashboard.service.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import org.agnitas.beans.impl.PaginatedListImpl;
import org.agnitas.emm.core.velocity.VelocityCheck;
import org.agnitas.util.SafeString;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Required;

import com.agnitas.dao.ComMailingDao;
import com.agnitas.emm.core.dashboard.service.DashboardService;
import com.agnitas.reporting.birt.external.beans.SendStatRow;
import com.agnitas.reporting.birt.external.beans.factory.MailingSummaryDataSetFactory;
import com.agnitas.reporting.birt.external.dataset.CommonKeys;
import com.agnitas.reporting.birt.external.dataset.MailingSummaryDataSet;

import net.sf.json.JSONObject;

public class DashboardServiceImpl implements DashboardService {
    private Logger logger = Logger.getLogger(DashboardServiceImpl.class);
    private static final int DEFAULT_STATISTIC_VALUE = 0;

    private MailingSummaryDataSetFactory summaryDataSetFactory;
    private ComMailingDao mailingDao;

    @Required
    public void setMailingDao(ComMailingDao mailingDao) {
        this.mailingDao = mailingDao;
    }

    @Required
    public void setSummaryDataSetFactory(MailingSummaryDataSetFactory summaryDataSetFactory) {
        this.summaryDataSetFactory = summaryDataSetFactory;
    }

    @Override
    public PaginatedListImpl<Map<String, Object>> getMailings(int companyId, String sort, String direction, int rownums) {
        return mailingDao.getDashboardMailingList(companyId, sort, direction, rownums);
    }

    @Override
    public List<Map<String, Object>> getLastSentWorldMailings(@VelocityCheck int companyID, int rownums) {
        return mailingDao.getLastSentWorldMailings(companyID, rownums);
    }

    @Override
    public JSONObject getStatisticsInfo(int mailingId, Locale locale, int companyId) throws Exception {
        JSONObject statData = new JSONObject();

        Map<Integer, Integer> data = getReportData(mailingId, companyId);

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
        statData.put("clickersPercent", Collections.singletonList(clickerPercent));
        statData.put("openersPercent", Collections.singletonList(openersPercent));


        return statData;
    }

    private Map<Integer, Integer> getReportData(int mailingId, int companyId) throws Exception {
        MailingSummaryDataSet mailingSummaryDataSet = summaryDataSetFactory.create();
        Map<Integer, Integer> data = new HashMap<>();

        if (mailingId != 0) {
            int tempTableID = mailingSummaryDataSet.prepareDashboardForCharts(mailingId, companyId);
            List<? extends SendStatRow> rowList = mailingSummaryDataSet.getSummaryData(tempTableID);

            for (SendStatRow row : rowList) {
                int value = row.getCount();

                // when the value in summary data set is "-1"
                // it's mean that the requested data is not exists in db, so we should set "0"
                if (value < 0) {
                    value = 0;
                }

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
                    messageKey = "mailing.status.sent";
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

        List<Integer> orderedKeys = new ArrayList<>(Arrays.asList(
                CommonKeys.CLICKER_PC_INDEX, CommonKeys.CLICKER_MOBILE_INDEX, CommonKeys.CLICKER_TABLET_INDEX,
                CommonKeys.CLICKER_SMARTTV_INDEX, CommonKeys.CLICKER_PC_AND_MOBILE_INDEX));

        for (int categoryId : orderedKeys) {
            String messageKey = StringUtils.EMPTY;
            Integer categoryVal = data.get(categoryId);

            if (Objects.isNull(categoryVal)) {
                continue;
            }

            switch (categoryId) {
                case CommonKeys.CLICKER_PC_INDEX:
                    messageKey = "predelivery.desktop";
                    break;
                case CommonKeys.CLICKER_TABLET_INDEX:
                    messageKey = "report.openers.tablet.shortname";
                    break;
                case CommonKeys.CLICKER_MOBILE_INDEX:
                    messageKey = "report.openers.mobile.shortname";
                    break;
                case CommonKeys.CLICKER_SMARTTV_INDEX:
                    messageKey = "report.openers.smarttv.shortname";
                    break;
                case CommonKeys.CLICKER_PC_AND_MOBILE_INDEX:
                    messageKey = "report.openers.multiple-devices.shortname";
                    break;
                default:
                	messageKey = "predelivery.desktop";
                    break;
            }

            int clickerTracked = data.getOrDefault(CommonKeys.CLICKER_TRACKED_INDEX, DEFAULT_STATISTIC_VALUE);
            String[] items = getItems(messageKey, locale, clickerTracked, categoryVal);

            clickersStat.add(items);
        }

        return clickersStat;
    }

    private List<String[]> getOpenersStat(Map<Integer, Integer> data, Locale locale) {
        List<String[]> openersStat = new ArrayList<>();

        List<Integer> orderedKeys = new ArrayList<>(Arrays.asList(
                CommonKeys.OPENERS_PC_INDEX, CommonKeys.OPENERS_MOBILE_INDEX, CommonKeys.OPENERS_TABLET_INDEX,
                CommonKeys.OPENERS_SMARTTV_INDEX, CommonKeys.OPENERS_PC_AND_MOBILE_INDEX));

        for (Integer categoryId : orderedKeys) {
            String messageKey = StringUtils.EMPTY;
            Integer categoryVal = data.get(categoryId);

            if (Objects.isNull(categoryVal)) {
                continue;
            }

            switch (categoryId) {
                case CommonKeys.OPENERS_PC_INDEX:
                    messageKey = "predelivery.desktop";
                    break;
                case CommonKeys.OPENERS_TABLET_INDEX:
                    messageKey = "report.openers.tablet.shortname";
                    break;
                case CommonKeys.OPENERS_MOBILE_INDEX:
                    messageKey = "report.openers.mobile.shortname";
                    break;
                case CommonKeys.OPENERS_SMARTTV_INDEX:
                    messageKey = "report.openers.smarttv.shortname";
                    break;
                case CommonKeys.OPENERS_PC_AND_MOBILE_INDEX:
                    messageKey = "report.openers.multiple-devices.shortname";
                    break;
                default:
                	messageKey = "predelivery.desktop";
                    break;
            }

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
}
