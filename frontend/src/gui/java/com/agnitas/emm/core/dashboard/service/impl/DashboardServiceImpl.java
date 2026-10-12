/*

    Copyright (C) 2006-2026 AGNITAS AG (https://www.agnitas.org)

    This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.
    This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.
    You should have received a copy of the GNU Affero General Public License along with this program. If not, see <https://www.gnu.org/licenses/#AGPL>.

*/

package com.agnitas.emm.core.dashboard.service.impl;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.agnitas.beans.Admin;
import com.agnitas.beans.PaginatedList;
import com.agnitas.dao.MailingDao;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService;
import com.agnitas.emm.core.birtstatistics.service.MailingStatisticsService.ByDeviceClass;
import com.agnitas.emm.core.commons.dto.DateRange;
import com.agnitas.emm.core.dashboard.bean.DashboardRecipientReport;
import com.agnitas.emm.core.dashboard.bean.DashboardWorkflow;
import com.agnitas.emm.core.dashboard.bean.ScheduledMailing;
import com.agnitas.emm.core.dashboard.dto.ClickersStatisticsResponse;
import com.agnitas.emm.core.dashboard.dto.DashboardStatisticsDeviceType;
import com.agnitas.emm.core.dashboard.dto.DashboardStatisticsResponse;
import com.agnitas.emm.core.dashboard.dto.OpenersStatisticsResponse;
import com.agnitas.emm.core.dashboard.service.DashboardService;
import com.agnitas.emm.core.maildrop.MaildropStatus;
import com.agnitas.emm.core.mobile.bean.DeviceClass;
import com.agnitas.emm.core.recipientsreport.service.RecipientsReportService;
import com.agnitas.emm.core.workflow.service.WorkflowService;
import com.agnitas.messages.I18nString;
import com.agnitas.util.AgnUtils;
import com.agnitas.util.DateUtilities;
import org.springframework.stereotype.Service;

@Service
public class DashboardServiceImpl implements DashboardService {
	
    private final MailingDao mailingDao;
    private final WorkflowService workflowService;
    private final RecipientsReportService recipientsReportService;
    private final MailingStatisticsService mailingStatisticsService;

    public DashboardServiceImpl(
            MailingDao mailingDao,
            WorkflowService workflowService,
            RecipientsReportService recipientsReportService,
            MailingStatisticsService mailingStatisticsService
    ) {
        this.mailingDao = mailingDao;
        this.workflowService = workflowService;
        this.recipientsReportService = recipientsReportService;
        this.mailingStatisticsService = mailingStatisticsService;
    }

    @Override
    public PaginatedList<Map<String, Object>> getMailings(Admin admin, String sort, String direction, int rownums) {
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
    public DashboardStatisticsResponse getStatistics(int mailingId, int companyId) {
        return new DashboardStatisticsResponse(
                getSentCount(mailingId, companyId),
                mailingStatisticsService.getMeasuredOpenersCount(mailingId, companyId),
                mailingStatisticsService.getClickersCount(mailingId, companyId),
                mailingStatisticsService.getOptOutsCount(mailingId, companyId),
                mailingStatisticsService.getHardBouncesCount(mailingId, companyId)
        );
    }

    @Override
    public ClickersStatisticsResponse getClickersStatistics(int mailingId, int companyId) {
        int clickersCount = mailingStatisticsService.getClickersCount(mailingId, companyId);

        Map<DeviceClass, Integer> clickersByDevice = mailingStatisticsService.getClickersByDevice(mailingId, companyId, new DateRange());
        ByDeviceClass byDeviceClass = mailingStatisticsService.toByDeviceClass(clickersByDevice, clickersCount);

        return new ClickersStatisticsResponse(mapDeviceStatsForUi(byDeviceClass));
    }

    @Override
    public OpenersStatisticsResponse getOpenersStatistics(int mailingId, int companyId) {
        int measuredOpenersCount = mailingStatisticsService.getMeasuredOpenersCount(mailingId, companyId);
        int sentCount = getSentCount(mailingId, companyId);

        Map<DeviceClass, Integer> openersByDevice = mailingStatisticsService.getOpenersByDevice(mailingId, companyId, new DateRange());
        ByDeviceClass byDeviceClass = mailingStatisticsService.toByDeviceClass(openersByDevice, measuredOpenersCount);

        return new OpenersStatisticsResponse(
                mapDeviceStatsForUi(byDeviceClass),
                AgnUtils.calculateRate(measuredOpenersCount, sentCount)
        );
    }

    private int getSentCount(int mailingId, int companyId) {
        return mailingStatisticsService.getSentCount(
                mailingId,
                companyId,
                MaildropStatus.realSendStatuses()
        );
    }

    private Map<DashboardStatisticsDeviceType, Long> mapDeviceStatsForUi(ByDeviceClass byDevice) {
        Map<DashboardStatisticsDeviceType, Long> values = new LinkedHashMap<>();
        for (DashboardStatisticsDeviceType type : DashboardStatisticsDeviceType.values()) {
            Long val = switch (type) {
                case PC -> byDevice.pc();
                case TABLET -> byDevice.tablet();
                case MOBILE -> byDevice.mobile();
                case SMART_TV -> byDevice.smartTv();
                case MULTIPLE -> byDevice.multi();
            };
            values.put(type, val);
        }
        return values;
    }

}
